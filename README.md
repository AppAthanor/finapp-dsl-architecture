# FinApp Multi-DSL Architecture

This monorepo contains a complete architecture for specifying, generating, and validating financial applications using a multi-layered Domain-Specific Language (DSL) approach.

## Documentation

The API documentation for the FinApp DSL is available online at:
[https://appanthanor.github.io/finapp-dsl-architecture/](https://appanthanor.github.io/finapp-dsl-architecture/)

This documentation includes complete API references, usage examples, and conceptual guides to help you understand and work with the DSL.

## Core Concepts

We're using three complementary approaches to define, document, and implement financial application features:

1. **Gherkin BDD Specifications** (.feature files) - Human-readable scenarios that define application behavior from a user perspective
2. **Functional DSL Implementation** (.clj files) - A Clojure implementation of a functional programming model that powers test execution and business logic
3. **UI/UX Specification DSL** (.finapp files) - A declarative specification of the UI, screens, navigation, and business logic

This multi-layered approach enables product, development, and testing teams to collaborate effectively with clear separation of concerns.

## Monorepo Structure

This repository is organized into four main areas, each previously planned as separate repositories:

```
finapp-dsl-architecture/
│
├── core/                       # Core Framework
│   ├── lib/                    # Central shared libraries and utilities
│   ├── config/                 # Common configuration and standards
│   ├── integration/            # Integration interfaces for other components
│   └── docs/                   # Documentation hub linking all components
│
├── specifications/             # BDD Specifications
│   ├── features/               # Gherkin feature files
│   │   └── domains/            # Organized by business domains
│   │       └── lending/        # Lending-related features
│   │           ├── loan-topup.feature  # Loan top-up BDD scenarios
│   │           └── README.md         # Lending domain documentation
│   ├── step-definitions/       # Cucumber step implementations
│   └── dsl/                    # Domain-Specific Language implementations
│       ├── functional-clj/     # Functional DSLs (.clj files)
│       └── finapp/             # UI/UX DSLs (.finapp files)
│
├── code-generation/            # Code Generation Engine
│   ├── parsers/                # Parsers for .finapp files
│   ├── templates/              # Code generation templates
│   ├── transformers/           # Transformation logic
│   └── adapters/               # Target platform adapters
│
├── output/                     # Output Management
│   ├── generated/              # Generated code
│   │   ├── ios/                # Generated iOS code
│   │   ├── android/            # Generated Android code
│   │   └── web/                # Generated web code
│   ├── validation/             # Validation and verification tools
│   └── deployment/             # Deployment utilities
│
└── README.md                   # This README file
```

## How Our Multi-DSL Architecture Works

This section provides a detailed explanation of how our complete architecture functions, from specification to implementation and testing.

### 1. The Three DSL Layers and Their Purposes

Our architecture uses three complementary DSLs, each serving a distinct purpose:

#### BDD Feature Files (.feature)
- Written in Gherkin syntax to describe user behaviors
- Created by business analysts and product owners
- Define acceptance criteria from a user perspective
- Example:
  ```gherkin
  Scenario: Customer selects a pre-defined top-up amount
    Given I am on the amount selection screen
    When I select the second pre-defined amount option
    Then I should see detailed impact calculations in my local currency
  ```

#### Functional DSL (.clj)
- Implements a formal model of business logic in Clojure
- Created by domain experts and engineers
- Defines an evaluator for business rules and expressions
- Provides a precise, executable specification of expected behavior
- Example:
  ```clojure
  ;; Business rule for top-up amount validation
  (def topup-amount-limits-rule
    (core/make-business-rule
      "BR001"
      ;; Condition expression that checks if amount is within segment limits
      (core/make-lambda ["customer" "region" "amount"] /* condition expression */),
      ;; Action expression that validates or adjusts the amount
      (core/make-lambda ["customer" "region" "amount"] /* action expression */)))
  ```

#### UI/UX Specification DSL (.finapp)
- Declarative definition of screens, components, and interactions
- Created by UI/UX designers and product owners
- Specifies layouts, data models, and navigation flows
- Handles regional variations and conditional content
- Example:
  ```
  screen TopupAmountSelection {
    // Screen definition including components, data functions and layout
  }
  ```

### 2. End-to-End Workflow

Here's how a feature flows through our complete architecture:

#### Stage 1: Specification
1. Business requirements are captured as Gherkin scenarios in `.feature`