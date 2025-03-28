# Loan Top-up Feature: Multi-DSL Implementation

This directory contains the Loan Top-up feature implementation using our multi-layered Domain-Specific Language (DSL) approach.

## Overview

The Loan Top-up journey allows existing loan customers to borrow additional funds on their current loan without going through a full application process. This creates a streamlined, friction-free experience while maintaining compliance with regulatory requirements.

## The Three DSL Layers

Our implementation uses three complementary layers:

### Layer 1: Behavior Specification (.feature)

[loan-topup.feature](loan-topup.feature) contains user-focused scenarios written in Gherkin syntax. These scenarios define what the application should do from a business perspective, serving as:

- Executable requirements documentation
- Acceptance criteria for development
- Test scenarios for validation

Example:
```gherkin
Scenario: Customer in Hong Kong sees regional regulatory information
  Given I am authenticated in the mobile banking app
  And I have an existing loan that is eligible for top-up
  And my current region is set to "HK"
  When I navigate to the "My Loans" section
  Then I should see all monetary values in "HKD" format
  And I should see the regulatory information specific to "HK" including:
    | HKMA approval notice | Cooling-off period | Risk disclosure |
```

### Layer 2: Test Implementation DSL (.js)

[loan-topup-functional-dsl.js](../../../bdd-specifications/dsl/loan-topup-functional-dsl.js) implements a sophisticated functional programming model inspired by SICP (Structure and Interpretation of Computer Programs) principles. This DSL:

- Defines an evaluator for business rules and expressions
- Implements business rules as expressions
- Provides region and segment-specific calculations
- Handles time-sensitive content logic
- Powers the test validation through a clean abstraction layer

Key concepts in this implementation:
- Expression types (variables, applications, lambdas, conditionals)
- Environment model with frames and bindings
- Business rule constructors with condition/action pairs
- Domain-specific primitives for regions and customer segments

### Layer 3: UI/UX Specification DSL (.finapp)

[loan-topup.finapp](../../../bdd-specifications/dsl/finapp/loan-topup.finapp) provides a declarative specification of the user interface, screens, and interactions. This format:

- Defines screens, components, and layouts
- Specifies data models and validation rules
- Declares navigation flows and transitions
- Configures API endpoints and contracts
- Handles regional variations and localizations

This specification can be consumed by development teams to implement the feature consistently across iOS, Android, and web platforms.

## Technical Deep Dive: Functional DSL

### Core Architecture

The functional DSL follows a lambda calculus-inspired approach:

1. **Expression Representation**: All business logic is represented as data structures (not just code):
   ```javascript
   // Example of a lambda expression in our DSL
   const calculateNewMonthlyPayment = makeLambda(
     ['currentBalance', 'topupAmount', 'interestRate', 'remainingTerm'],
     makeApplication(
       makeVariable('calculatePayment'),
       [
         makeApplication(
           makeVariable('+'),
           [makeVariable('currentBalance'), makeVariable('topupAmount')]
         ),
         makeVariable('interestRate'),
         makeVariable('remainingTerm')
       ]
     )
   );
   ```

2. **Evaluator Implementation**: The DSL includes a full evaluator that processes these expressions:
   ```javascript
   // Core evaluation function (simplified)
   const evaluate = (expression, environment) => {
     if (isSelfEvaluating(expression)) return expression;
     if (isVariable(expression)) return lookupVariableValue(expression.name, environment);
     if (isApplication(expression)) {
       const procedure = evaluate(expression.operator, environment);
       const args = expression.operands.map(operand => evaluate(operand, environment));
       return applyProcedure(procedure, args);
     }
     // Other expression types handled similarly...
   };
   ```

3. **Environment Model**: The DSL uses lexical scoping with frames and bindings:
   ```javascript
   // Environment operations
   const extendEnvironment = (variables, values, baseEnv) => {
     const frame = makeFrame(variables, values);
     return { frame, parent: baseEnv };
   };
   
   const lookupVariableValue = (variable, env) => {
     if (!env) throw new Error(`Unbound variable: ${variable}`);
     if (variable in env.frame) return env.frame[variable];
     return lookupVariableValue(variable, env.parent);
   };
   ```

### Domain-Specific Components

The DSL defines banking domain concepts as first-class objects:

1. **Regions**:
   ```javascript
   const ukRegion = makeRegion('UK', {
     currency: 'GBP',
     currencySymbol: '£',
     dateFormat: 'DD/MM/YYYY',
     regulatoryBody: 'FCA',
     coolingOffPeriod: 14,
     language: 'en-GB',
     translationsKey: 'uk_translations'
   });
   ```

2. **Customer Segments**:
   ```javascript
   const wealthSegment = makeCustomerSegment('Wealth', {
     description: 'Premier or priority banking customers with higher value accounts',
     interestRates: {
       UK: 5.4,
       HK: 5.8
     },
     minTopupAmounts: {
       UK: 5000,
       HK: 50000
     },
     maxTopupAmounts: {
       UK: 100000,
       HK: 800000
     },
     benefits: {
       UK: ['Preferential rates', 'Dedicated relationship manager', 'Fee waivers'],
       HK: ['Priority processing', 'Jade status points', 'Fee waivers']
     }
   });
   ```

3. **Business Rules**:
   ```javascript
   const topupAmountLimitsRule = makeBusinessRule(
     'BR001',
     // Condition expression
     makeLambda(['customer', 'region', 'amount'], /* ... */),
     // Action expression
     makeLambda(['customer', 'region', 'amount'], /* ... */),
     {
       description: 'Top-up amount must be between the minimum and maximum pre-approved limits',
       rationale: 'Ensures the additional borrowing is within affordability parameters',
       errorMessageKey: 'amount_limit_error'
     }
   );
   ```

4. **Screen Renderers**:
   ```javascript
   const renderAmountSelectionScreen = makeLambda(
     ['customer', 'region', 'currentDateTime'],
     makeSequence([
       // Get customer segment
       makeAssignment(
         makeVariable('segment'),
         makeApplication(makeVariable('getCustomerSegment'), [makeVariable('customer')])
       ),
       
       // Get segment-specific limits
       makeAssignment(
         makeVariable('minAmount'),
         makeApplication(
           makeVariable('getSegmentMinTopupAmount'),
           [makeVariable('segment'), makeApplication(makeVariable('getRegionCode'), [makeVariable('region')])]
         )
       ),
       
       // Additional expressions...
       
       // Return composed screen data
       makeQuoted({
         amountOptions: makeVariable('amountOptions'),
         minAmount: makeVariable('minAmount'),
         maxAmount: makeVariable('maxAmount'),
         timeBasedContent: makeVariable('timeBasedContent'),
         specialOffers: makeVariable('specialOffers'),
         localizedText: makeVariable('localizedText')
       })
     ])
   );
   ```

### Integration with Test Framework

The DSL connects to test fixtures through a carefully designed interface:

1. **Environment Initialization**:
   ```javascript
   // In loan-topup-steps.js
   Given('I have an existing loan that is eligible for top-up', async function() {
     // Set up test data
     this.existingLoan = {
       id: 'LOAN123456',
       currentBalance: 10000,
       originalAmount: 15000,
       monthlyPayment: 250,
       remainingTerm: 48,
       interestRate: 5.9
     };
     
     await loansOverviewPage.setupTestLoan(this.existingLoan);
     
     // Add loan to DSL environment
     FunctionalDSL.defineVariable('currentLoan', this.existingLoan, dslEnvironment);
   });
   ```

2. **Expression Evaluation for Assertions**:
   ```javascript
   Then('I should see a localised pre-approved message in the appropriate language', async function() {
     const visibleMessage = await loansOverviewPage.getTopupOfferMessage();
     
     // Evaluate the expected message using the DSL
     const screenContent = FunctionalDSL.evaluate(
       FunctionalDSL.loanTopupJourney.initialOfferScreen,
       dslEnvironment
     );
     
     // Verify using the DSL-generated content
     expect(visibleMessage).to.include(screenContent.localizedText.pre_approved_message);
   });
   ```

### Cross-Platform Testing Architecture

Our testing infrastructure supports multiple platforms while using the same functional DSL:

1. **Base Page Object**:
   ```javascript
   class LoanTopupPage {
     // Abstract methods to be implemented by platform-specific subclasses
     async getAmountOptions() {
       throw new Error("Must be implemented by platform-specific subclass");
     }
     
     async selectAmount(amount) {
       throw new Error("Must be implemented by platform-specific subclass");
     }
     
     // Shared methods that use the platform-specific implementations
     async selectPresetAmount(index) {
       const options = await this.getAmountOptions();
       return this.selectAmount(options[index]);
     }
   }
   ```

2. **Platform-Specific Implementations**:
   ```javascript
   // iOS implementation
   class iOSLoanTopupPage extends LoanTopupPage {
     async getAmountOptions() {
       return await this.driver.findElements(this.ios.amountOptionSelector)
         .map(element => element.getText());
     }
     
     async selectAmount(amount) {
       // iOS-specific implementation
     }
   }
   
   // Android implementation
   class AndroidLoanTopupPage extends LoanTopupPage {
     async getAmountOptions() {
       return await this.driver.findElements(this.android.amountOptionSelector)
         .map(element => element.getText());
     }
     
     async selectAmount(amount) {
       // Android-specific implementation
     }
   }
   ```

3. **Platform Determination Logic**:
   ```javascript
   // Factory function to get the right page object
   function getLoanTopupPage() {
     if (config.platform === 'ios') {
       return new iOSLoanTopupPage();
     } else if (config.platform === 'android') {
       return new AndroidLoanTopupPage();
     } else {
       return new WebLoanTopupPage();
     }
   }
   
   // Used in step definitions
   const loanTopupPage = getLoanTopupPage();
   ```

## Code Generation from .finapp to Platform-Specific Code

The `.finapp` file serves as the source for code generation across platforms:

### 1. Code Generation Pipeline

1. **Parsing**: The `.finapp` file is parsed into an Abstract Syntax Tree (AST)
2. **Analysis**: The AST is analyzed for completeness and correctness
3. **Transformation**: The AST is transformed into platform-specific code models
4. **Generation**: Code files are generated for each target platform

### 2. iOS Example (Swift)

```swift
// Generated from screen TopupAmountSelection in loan-topup.finapp
class TopupAmountSelectionViewController: UIViewController {
    // Properties for screen parameters
    var loanId: String
    var offerId: String
    
    // Data formatting functions
    func formatCurrency(value: Double, region: String) -> String {
        return region == "UK" ? "£\(String(format: "%.2f", value))" : "HK$\(String(format: "%.2f", value))"
    }
    
    func getPresetAmounts(minAmount: Double, maxAmount: Double, segment: String) -> [Double] {
        let spread = maxAmount - minAmount
        let step = spread / 4
        return [
            round(minAmount + step),
            round(minAmount + step * 2),
            round(minAmount + step * 3)
        ]
    }
    
    // UI components and layout
    // ...
}
```

### 3. Android Example (Kotlin)

```kotlin
// Generated from screen TopupAmountSelection in loan-topup.finapp
@Composable
fun TopupAmountSelectionScreen(
    loanId: String,
    offerId: String,
    onContinue: (amount: Double) -> Unit,
    onBack: () -> Unit
) {
    // State management
    var selectedAmount by remember { mutableStateOf<Double?>(null) }
    var customAmount by remember { mutableStateOf(false) }
    var newMonthlyPayment by remember { mutableStateOf<Double?>(null) }
    
    // Data functions
    fun formatCurrency(value: Double, region: String): String {
        return if (region == "UK") "£${value.format(2)}" else "HK$${value.format(2)}"
    }
    
    // UI Components
    Column {
        // Header
        Text(
            text = "Select Top-up Amount",
            style = MaterialTheme.typography.h5
        )
        
        // Layout components
        // ...
    }
}
```

### 4. Web Example (React/TypeScript)

```tsx
// Generated from screen TopupAmountSelection in loan-topup.finapp
import React, { useState, useEffect } from 'react';

interface TopupAmountSelectionProps {
  loanId: string;
  offerId: string;
  onContinue: (amount: number) => void;
  onBack: () => void;
}

export const TopupAmountSelection: React.FC<TopupAmountSelectionProps> = ({
  loanId,
  offerId,
  onContinue,
  onBack
}) => {
  // State
  const [selectedAmount, setSelectedAmount] = useState<number | null>(null);
  const [customAmount, setCustomAmount] = useState(false);
  const [newMonthlyPayment, setNewMonthlyPayment] = useState<number | null>(null);
  
  // Data functions
  const formatCurrency = (value: number, region: string): string => {
    return region === 'UK' ? `£${value.toFixed(2)}` : `HK$${value.toFixed(2)}`;
  };
  
  // Component rendering
  return (
    <div className="screen-container">
      {/* Header */}
      <h2>Select Top-up Amount</h2>
      
      {/* Content */}
      {/* ... */}
    </div>
  );
};
```

## Regional and Segment Considerations

The Loan Top-up implementation handles variations across:

### Regions
- **United Kingdom**: FCA regulations, GBP currency, UK-specific disclosures
- **Hong Kong**: HKMA regulations, HKD currency, HK-specific disclosures

### Customer Segments
- **Basic**: Standard interest rates and limits
- **Wealth**: Premium interest rates, higher limits, additional benefits

### Time-sensitive Processing
- Standard business hours: Immediate processing
- Evening/weekend hours: Modified processing timeframes
- Holiday periods: Special handling and notifications

## Integration with Core System

The Loan Top-up feature integrates with the core banking system through:

1. **API Contracts**: Defined in the `.finapp` file under the `api` section
2. **Data Models**: Shared between the DSL and the core system
3. **Authentication**: Leveraging the core authentication framework
4. **Error Handling**: Following the centralized error management patterns

## Getting Started with Development

To work with this feature:

1. **Understand the DSLs**: Review all three DSL files to understand the feature
2. **Run the Tests**: Execute the BDD scenarios to see expected behavior
3. **Modify Specifications**: Make changes to the DSLs when requirements change
4. **Generate Code**: Use the Code Generation Engine to create platform code
5. **Validate Implementation**: Run tests against the implemented feature

## Using This Approach for Other Features

This multi-layered DSL approach can be applied to other financial features that require:
- Regional adaptations
- Customer segmentation
- Complex business rules
- Sophisticated UI/UX specifications

It provides a blueprint for creating maintainable, cross-platform specifications that serve both business and technical stakeholders.

## Related Banking Features

The lending domain includes other features related to borrowing products:

- Loan origination (application and approval process)
- Mortgage applications
- Overdraft management
- Credit card applications
- Loan repayment calculators

## Regulatory Considerations

All lending features must comply with:

### United Kingdom
- Consumer Credit Act regulations
- Financial Conduct Authority (FCA) requirements
- Treating Customers Fairly (TCF) principles
- Responsible lending guidelines

### Hong Kong
- Hong Kong Monetary Authority (HKMA) requirements
- Money Lenders Ordinance
- Code of Banking Practice

## Contributing

When adding new lending features or enhancing existing ones:

1. Follow the domain-specific language patterns established in the functional DSL
2. Ensure scenarios cover all relevant region and customer segment variations
3. Consider time-sensitive content where applicable
4. Include appropriate regulatory checks and disclosures
5. Update this README with new feature information