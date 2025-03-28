# BDD Specifications with Domain-Specific Languages

This repository demonstrates a modern approach to specifying and testing financial application features using Behaviour-Driven Development (BDD) combined with Domain-Specific Languages (DSLs).

## Core Concepts

We're using three complementary approaches to define, document, and test financial application features:

1. **Gherkin BDD Specifications** (.feature files) - Human-readable scenarios that define application behavior from a user perspective
2. **Functional DSL Implementation** (.js files) - A JavaScript implementation of a functional programming model that powers test execution
3. **UI/UX Specification DSL** (.finapp files) - A declarative specification of the UI, screens, navigation, and business logic

This multi-layered approach enables product, development, and testing teams to collaborate effectively with clear separation of concerns.

## Loan Top-up Feature

The main feature we're implementing is a loan top-up journey that allows customers to borrow additional funds on their existing loans without applying for a new loan. This feature demonstrates:

- Region-specific functionality (UK and Hong Kong)
- Customer segmentation (Basic and Wealth)
- Time-sensitive content and processing
- Complex business rules and validations

## Repository Structure

```
bdd-specifications/
│
├── features/                     # BDD specifications in Gherkin syntax
│   └── domains/                  # Organized by business domains
│       └── lending/              # Lending-related features
│           ├── loan-topup.feature  # Loan top-up BDD scenarios
│           └── README.md         # Lending domain documentation
│
├── bdd-specifications/           # Implementation code for tests
│   ├── step-definitions/         # Cucumber step implementations
│   │   └── domain-specific/      # Domain-specific step definitions
│   │       └── loan-topup-steps.js  # Test steps for loan top-up
│   │
│   └── dsl/                      # Domain-Specific Language implementations
│       ├── loan-topup-functional-dsl.js  # Functional DSL for testing
│       └── finapp/               # UI/UX specification DSL files
│           └── loan-topup.finapp # Declarative UI/UX specification
│
└── README.md                     # This README file
```

## The Three-Layer Approach

### 1. BDD Feature Files (.feature)

Gherkin-syntax files define user-centric scenarios that describe what the application should do from a business perspective. Example:

```gherkin
Scenario: Customer selects a pre-defined top-up amount in their local currency
  Given I am authenticated in the mobile banking app
  And I have an existing loan that is eligible for top-up
  And I am on the amount selection screen
  When I select the second pre-defined amount option
  Then I should see detailed impact calculations in my local currency
```

### 2. Functional DSL (.js)

A sophisticated functional programming model that:
- Represents business rules as expressions
- Uses evaluators to process these expressions
- Implements environment-based scoping
- Enables testing of complex business logic

### 3. UI/UX Specification DSL (.finapp)

A declarative specification format that defines:
- Screens, components, and layouts
- Data models and validation rules
- Navigation paths and transitions
- API endpoints and contracts
- Regional variations and localizations

## Using This Repository

This repository serves as a blueprint for implementing the multi-DSL approach to financial application specification. It demonstrates best practices for:

1. Organizing specifications by domain
2. Creating clear separation between different specification layers
3. Handling regional and segment-specific variations
4. Implementing time-sensitive and conditional content
5. Defining reusable components and patterns

## Getting Started

1. Browse the `.feature` files to understand the expected behaviors
2. Review the functional DSL implementation to see the testing approach
3. Examine the `.finapp` files to understand the UI specification format

For more details on the lending domain implementation, see the [Lending Domain README](features/domains/lending/README.md).