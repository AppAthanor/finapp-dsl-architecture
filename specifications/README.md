# Specifications

This directory contains the behavior specifications and domain-specific languages (DSLs) that define our financial applications.

## Overview

The Specifications component serves as the source of truth for application behavior. It includes:

1. **Gherkin Feature Files**: Human-readable scenarios that define behavior
2. **Step Definitions**: Implementations that connect scenarios to tests
3. **Domain-Specific Languages**: Formal definitions of business rules and UI

## Directory Structure

```
specifications/
├── features/                  # Gherkin feature files
│   └── domains/               # Organized by business domains
│       └── lending/           # Lending-related features
│           ├── loan-topup.feature  # Loan top-up BDD scenarios
│           └── README.md      # Domain-specific documentation
│
├── step-definitions/          # Cucumber step implementations
│   └── domain-specific/       # Domain-specific step definitions
│       ├── loan-topup-steps.js  # Steps for loan top-up
│       └── ...                # Other domain-specific steps
│
└── dsl/                       # Domain-Specific Language implementations
    ├── functional-clj/        # Functional DSLs in Clojure
    │   └── domains/
    │       └── lending/
    │           └── loan_topup_example.clj  # DSL for loan top-up in Clojure
    └── finapp/                # UI/UX DSLs (.finapp files)
        └── domains/
            └── lending/
                └── loan-topup.finapp  # UI/UX specification for loan top-up
```

## Key Components

### Gherkin Feature Files

Feature files in Gherkin syntax define expected behavior from a user perspective:

```gherkin
Feature: Loan Top-up
  As a customer with an existing loan
  I want to apply for additional funds on my loan
  So that I can access more money without applying for a new loan

  Scenario: Customer views pre-approved top-up offer
    Given I am authenticated in the mobile banking app
    And I have an existing loan that is eligible for top-up
    When I navigate to the "My Loans" section
    Then I should see a pre-approved top-up offer
```

### Step Definitions

Step definitions implement the Gherkin steps using the functional DSL:

```javascript
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

### Domain-Specific Languages

#### Functional DSL (.clj)

The functional DSL provides a formal model of business rules:

```clojure
;; Loan top-up DSL example in Clojure
(def render-amount-selection-screen
  (core/make-lambda
    ["customer" "region" "currentDateTime"]
    (core/make-sequence
      [(core/make-assignment
         (core/make-variable "segment")
         (core/make-application
           (core/make-variable "getCustomerSegment")
           [(core/make-variable "customer")]))
       ;; More expressions...
       ])))
```

#### Functional DSL (.clj)
- Defines a formal executable model of business rules in Clojure
- Creates evaluators for testing edge cases and conditions
- Supports multiple regions and customer segments

```clojure
;; Business rule for top-up amount validation
(def topup-amount-limits-rule
  (core/make-business-rule
    "BR001"
    ;; Condition that checks if amount is within limits
    (core/make-lambda
      ["customer" "region" "amount"]
      (core/make-application
        (core/make-variable "and")
        [(core/make-application 
           (core/make-variable ">=")
           [(core/make-variable "amount")
            (core/make-application
              (core/make-variable "getMinAmount")
              [(core/make-variable "customer") 
               (core/make-variable "region")])])
         (core/make-application
           (core/make-variable "<=")
           [(core/make-variable "amount")
            (core/make-application 
              (core/make-variable "getMaxAmount")
              [(core/make-variable "customer")
               (core/make-variable "region")])])])))
    ;; Action that validates the amount
    (core/make-lambda
      ["customer" "region" "amount"]
      (core/make-if
        ;; condition
        (core/make-application
          (core/make-variable "isWithinLimits")
          [(core/make-variable "customer")
           (core/make-variable "region")
           (core/make-variable "amount")])
        ;; then
        (core/make-variable "amount")
        ;; else
        (core/make-quoted {:error "Amount outside limits"})))))
```

#### UI/UX DSL (.finapp)

The UI/UX DSL defines screens, components, and interactions:

```
screen TopupAmountSelection {
  title: "Select Top-up Amount"
  
  params: {
    loanId: string required,
    offerId: string required
  }
  
  dataFunctions: [
    {
      name: "formatCurrency"
      params: ["value", "region"]
      expression: "region === 'UK' ? `£${value.toFixed(2)}` : `HK$${value.toFixed(2)}`"
    }
  ]
  
  layout: {
    type: stack
    components: [
      // Components go here
    ]
  }
}
```

## Working with Specifications

### Adding New Features

1. Create a new feature file in the appropriate domain directory
2. Define scenarios using Gherkin syntax
3. Implement step definitions that connect to the functional DSL
4. Define business rules in the functional DSL
5. Define UI/UX specifications in the .finapp DSL

### Running Tests

Tests can be run using the test runner:

```bash
npm run test:specs
```

To run tests for a specific domain:

```bash
npm run test:specs -- --tags @lending
```

### Validating Specifications

The specifications can be validated for consistency:

```bash
npm run validate:specs
```

## Development Guidelines

1. **Feature Organization**: Organize features by business domain
2. **Scenario Focus**: Each scenario should focus on a single aspect of behavior
3. **Step Reuse**: Reuse step definitions where possible
4. **DSL Consistency**: Maintain consistent naming and structure in DSLs
5. **Documentation**: Document domain-specific terminology and concepts 