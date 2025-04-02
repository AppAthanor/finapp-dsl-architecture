# RFC: Separation of Business and Mobile DSL

## 1. Summary

This RFC proposes separating our current FinApp DSL into two specialised domain-specific languages:
1. A **Business DSL** (.bizapp) focused on business rules, calculations, and domain models
2. A **Mobile DSL** (.mobileapp) focused on UI/UX, navigation, and platform-specific implementation details

This separation creates clearer boundaries of responsibility, improves maintainability, and provides better tools for different team roles while ensuring our architecture can scale across all 22 markets.

## 2. Motivation

Our current architecture combines business logic and UI implementation in a single `.finapp` file, which has led to several challenges:

1. **Unclear Ownership** - Business analysts and mobile developers share responsibility for the same files, leading to confusion
2. **Cognitive Load** - Files contain too many concerns, making them difficult to understand and maintain
3. **Reuse Limitations** - Business logic cannot be easily reused across channels (mobile, web, API)
4. **Testing Complexity** - Testing business rules requires navigating through UI-specific code

By separating these concerns, we can address these issues while providing specialist tooling for each domain.

## 3. Detailed Design

### 3.1 File Format Separation

We will replace the existing `.finapp` files with two separate file formats:

#### 3.1.1 Business DSL (.bizapp)

The Business DSL will focus on:
- Domain models and data structures
- Business rules and validation
- Calculations and formulas
- Region-specific configurations
- Customer segmentation rules
- API contracts and error mappings

#### 3.1.2 Mobile DSL (.mobileapp)

The Mobile DSL will focus on:
- UI component definitions and layout
- Screen flows and navigation
- Data binding and state management
- Animation and transitions
- Platform-specific customisations
- Plugin configuration and delivery mechanisms

### 3.2 Cross-References Between DSLs

The Mobile DSL will reference the Business DSL through import statements:

```
// In loan-topup.mobileapp
import {
  LoanTopupOffer,
  EligibilityRules,
  calculateNewMonthlyPayment
} from "./loan-topup.bizapp"
```

### 3.3 Example: Business DSL (.bizapp)

```
// loan-topup.bizapp

models {
  LoanTopupOffer {
    loanId: string required
    maxTopupAmount: number required
    minTopupAmount: number required
    interestRate: number required
    expiryDate: date required
  }
}

rules {
  EligibilityRules {
    rule amountWithinLimits {
      condition: amount >= segment.minTopupAmount[region.code] && 
                amount <= segment.maxTopupAmount[region.code]
      message: "Amount must be between {segment.minTopupAmount[region.code]} and {segment.maxTopupAmount[region.code]}"
    }
  }
}

calculations {
  function calculateNewMonthlyPayment(currentBalance, topupAmount, interestRate, remainingTerm) {
    totalLoan = currentBalance + topupAmount
    monthlyInterestRate = interestRate / 12 / 100
    return totalLoan * (monthlyInterestRate * Math.pow(1 + monthlyInterestRate, remainingTerm)) / 
           (Math.pow(1 + monthlyInterestRate, remainingTerm) - 1)
  }
}

regions {
  UK {
    currency: "GBP"
    currencySymbol: "£"
    dateFormat: "DD/MM/YYYY"
    regulatoryBody: "FCA"
    coolingOffPeriod: 14
  }
  
  HK {
    currency: "HKD"
    currencySymbol: "HK$"
    dateFormat: "DD/MM/YYYY"
    regulatoryBody: "HKMA"
    coolingOffPeriod: 10
  }
}
```

### 3.4 Example: Mobile DSL (.mobileapp)

```
// loan-topup.mobileapp
import {
  LoanTopupOffer, 
  EligibilityRules,
  calculateNewMonthlyPayment
} from "./loan-topup.bizapp"

plugin {
  id: "com.banking.journeys.loantopup"
  version: "1.0.0"
  tier: 2 // Common functionality, dynamic loading
  dependencies: ["com.banking.core", "com.banking.accounts"]
}

screens {
  screen AmountSelectionScreen {
    title: "Select Top-up Amount"
    
    dataBindings: {
      offer: api.getTopupOffer({ loanId: route.params.loanId }),
      selectedAmount: state(null),
      calculatedPayment: computed(
        (selectedAmount, loan) => selectedAmount ? 
          calculateNewMonthlyPayment(
            loan.currentBalance, 
            selectedAmount, 
            offer.interestRate, 
            loan.remainingTerm
          ) : null
      )
    }
    
    layout: {
      type: "stack",
      components: [
        // UI layout details
      ]
    }
    
    validation: {
      selectedAmount: {
        rule: EligibilityRules.amountWithinLimits,
        onError: showErrorBanner(message)
      }
    }
  }
}
```

## 4. Benefits

### 4.1 For Business Teams
- Focus on business rules without UI implementation details
- Validate logic independently of UI changes
- Create consistent rules that apply across channels
- Easier to map requirements to implementation

### 4.2 For Mobile Development Teams
- Focus on UI/UX without mixing business logic
- Change UI implementation without affecting business rules
- Better platform-specific optimisations
- Clearer ownership of code

### 4.3 For Overall Architecture
- Better reuse across channels
- Independent versioning of business rules and UI
- Clearer security and compliance boundaries
- More maintainable codebase

## 5. Compatibility and Migration

To ensure smooth migration:

1. **Backward Compatibility** - Create tools to convert existing `.finapp` files to separate `.bizapp` and `.mobileapp` files
2. **Gradual Migration** - Support both formats during transition
3. **Validation Tools** - Provide tools to validate that separated files maintain equivalent behaviour

## 6. Conclusion

Separating our DSL into business and mobile concerns provides clearer boundaries, better team specialisation, and improved maintainability. This change will enable us to scale across markets more effectively while improving quality and reducing time-to-market.

We recommend approving this RFC to begin the implementation of separate Business and Mobile DSLs.
