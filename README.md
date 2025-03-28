# BDD Specifications with Domain-Specific Languages

This repository demonstrates a modern approach to specifying and testing financial application features using Behaviour-Driven Development (BDD) combined with Domain-Specific Languages (DSLs).

## Core Concepts

We're using three complementary approaches to define, document, and test financial application features:

1. **Gherkin BDD Specifications** (.feature files) - Human-readable scenarios that define application behavior from a user perspective
2. **Functional DSL Implementation** (.js files) - A JavaScript implementation of a functional programming model that powers test execution
3. **UI/UX Specification DSL** (.finapp files) - A declarative specification of the UI, screens, navigation, and business logic

This multi-layered approach enables product, development, and testing teams to collaborate effectively with clear separation of concerns.

## Repository Ecosystem

This BDD Specifications Repository is one of four interconnected repositories that form our complete application development ecosystem:

1. **Core Framework Repository**
   - Central shared libraries and utilities
   - Common configuration and standards
   - Integration interfaces for other components
   - Documentation hub linking to all components

2. **BDD Specifications Repository** (this repository)
   - Feature files and BDD scenarios
   - Acceptance criteria templates
   - Domain-specific language definitions
   - Behaviour validation frameworks

3. **Code Generation Engine Repository**
   - Templates and generation rules
   - Transformation logic
   - Target platform adapters
   - Quality assurance checks

4. **Output Management Repository**
   - Generated code management
   - Validation and verification tools
   - Deployment utilities
   - Feedback mechanisms to BDD layer

### Development Guidelines

When working across these repositories, follow these guidelines:

- **Core libraries and utilities** should be placed in the Core Framework Repository
- **Specifications, DSLs, and test scenarios** belong in this BDD Specifications Repository
- **Code generation templates and transformers** should go in the Code Generation Engine Repository
- **Generated outputs and deployment scripts** belong in the Output Management Repository

All four repositories work together in the complete workflow, with this BDD Specifications Repository serving as the source of truth for application behavior.

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

#### Functional DSL (.js)
- Implements a formal model of business logic in JavaScript
- Created by domain experts and engineers
- Defines an evaluator for business rules and expressions
- Provides a precise, executable specification of expected behavior
- Example:
  ```javascript
  // Business rule for top-up amount validation
  const topupAmountLimitsRule = makeBusinessRule(
    'BR001',
    // Condition expression that checks if amount is within segment limits
    makeLambda(['customer', 'region', 'amount'], /* condition expression */),
    // Action expression that validates or adjusts the amount
    makeLambda(['customer', 'region', 'amount'], /* action expression */)
  );
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
1. Business requirements are captured as Gherkin scenarios in `.feature` files
2. Business rules are formalized in the functional DSL (`.js`)
3. UI/UX specifications are defined in the UI/UX DSL (`.finapp`)

#### Stage 2: Code Generation
1. The Code Generation Engine parses the `.finapp` files
2. It transforms them into platform-specific code:
   - iOS (Swift/UIKit/SwiftUI)
   - Android (Kotlin/Jetpack Compose)
   - Web (TypeScript/React/Angular)
3. The generated code references shared business logic from the functional DSL

#### Stage 3: Test Implementation
1. BDD step definitions connect the Gherkin scenarios to automated tests
2. Platform-specific page objects handle interactions with each platform
3. The functional DSL provides expected outcomes for assertions
4. Cross-platform adapters ensure tests work consistently on all platforms

#### Stage 4: Validation & Deployment
1. The Output Management Repository runs verification on the generated code
2. Automated tests validate the implementations against the specifications
3. Successful builds are deployed to testing environments
4. Results feed back to inform future specifications

### 3. Understanding the Functional DSL

The functional DSL is the core of our architecture and works as follows:

#### Expression Types
- **Variables**: Reference values in the environment (e.g., `customer`, `amount`)
- **Applications**: Function calls (e.g., `formatCurrency(amount, region)`)
- **Lambdas**: Anonymous functions for complex logic
- **Conditionals**: If/then/else expressions for branching logic
- **Assignments**: Storing values in the environment
- **Sequences**: Multiple expressions evaluated in order

#### Environment Model
- Frames contain variable bindings (name-value pairs)
- Environments can be nested to support lexical scoping
- Business context is represented as an environment structure

#### Business Domain Objects
- Regions (UK, Hong Kong) with currency formats and regulations
- Customer segments with different rates, limits, and benefits
- Time periods affecting processing rules and notifications
- Promotions with time and region-specific applicability

#### Evaluation Process
1. The functional DSL evaluates expressions against an environment
2. Results represent expected system behavior
3. Tests compare actual system behavior to these expected results

### 4. Cross-Platform Testing Strategy

To ensure consistent behavior across platforms:

#### Platform Adapters
- Each platform (iOS, Android, Web) has adapters that:
  - Implement platform-specific UI interactions
  - Connect to the same functional DSL for assertions
  - Handle platform UI differences while testing the same behavior

#### Example:
```javascript
// Platform-specific implementations with shared behavior
class iOSLoanTopupPage extends LoanTopupPage {
  async getAmountOptions() {
    // iOS-specific code to get amount options
  }
}

class AndroidLoanTopupPage extends LoanTopupPage {
  async getAmountOptions() {
    // Android-specific code to get amount options
  }
}

// Shared step definition uses the functional DSL for assertions
Then('I should see pre-defined amount options', async function() {
  const amountOptions = await loanTopupPage.getAmountOptions();
  
  // Get expected options from functional DSL
  const expectedOptions = FunctionalDSL.evaluate(
    FunctionalDSL.getPresetAmounts,
    dslEnvironment
  );
  
  // Verify actual UI matches expected behavior
  expect(amountOptions).to.deep.equal(expectedOptions);
});
```

### 5. Complete Example Workflow

Let's trace how a specific business requirement flows through our architecture:

#### Business Requirement
- Wealth segment customers in Hong Kong should see special promotional messages

#### BDD Specification
```gherkin
Scenario: Wealth segment customers in Hong Kong see special promotional messages
  Given I am authenticated in the mobile banking app
  And my current region is set to "HK"
  And my customer segment is "Wealth"
  When I navigate to the "My Loans" section
  Then I should see "Jade member priority processing" displayed prominently
```

#### Functional DSL Implementation
```javascript
const wealthSegment = makeCustomerSegment('Wealth', {
  benefits: {
    UK: ['Premier rate guarantee and priority service'],
    HK: ['Jade member priority processing and rate discount']
  }
});

// Business rule for segment benefits
const segmentBenefitsRule = makeBusinessRule(
  'BR005',
  // Condition: customer has a segment with benefits
  makeLambda(['customer', 'region'], /* condition expression */),
  // Action: return segment benefits
  makeLambda(['customer', 'region'], /* action expression */)
);
```

#### UI/UX DSL Definition
```
type: conditional
condition: "userProfile.customerSegment === 'Wealth' && userProfile.region === 'HK'"
components: [
  {
    type: text
    value: "Jade member priority processing and rate discount"
    properties: {
      style: "highlight",
      icon: "star"
    }
  }
]
```

#### Generated Code (iOS Example)
```swift
// Generated iOS Swift code
if userProfile.customerSegment == "Wealth" && userProfile.region == "HK" {
    let benefitLabel = UILabel()
    benefitLabel.text = "Jade member priority processing and rate discount"
    benefitLabel.font = UIFont.systemFont(ofSize: 16, weight: .bold)
    benefitLabel.textColor = UIColor(named: "PrimaryColor")
    
    let starIcon = UIImageView(image: UIImage(named: "star"))
    
    let stackView = UIStackView(arrangedSubviews: [starIcon, benefitLabel])
    stackView.axis = .horizontal
    stackView.spacing = 8
    
    containerView.addSubview(stackView)
    // Add constraints...
}
```

#### Test Implementation
```javascript
Then('I should see {string} displayed prominently', async function(segmentBenefit) {
  const screenText = await loanTopupPage.getScreenText();
  expect(screenText).to.include(segmentBenefit);
  
  const isPremium = await loanTopupPage.isElementHighlighted(segmentBenefit);
  expect(isPremium).to.be.true;
});
```

### 6. Key Integration Points

Our repositories connect through:
- Shared domain models across all repositories
- Cross-repository references (Git submodules or package dependencies)
- Standardized interfaces between components
- Common configuration for consistency
- Integration test suites that validate the entire pipeline

By understanding this architecture, developers can effectively contribute to the right parts of the system while maintaining the integrity of the overall workflow.

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