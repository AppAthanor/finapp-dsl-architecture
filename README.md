# FinApp Multi-DSL Architecture

This monorepo contains a complete architecture for specifying, generating, and validating financial applications using a multi-layered Domain-Specific Language (DSL) approach.

## Core Concepts

We're using three complementary approaches to define, document, and implement financial application features:

1. **Gherkin BDD Specifications** (.feature files) - Human-readable scenarios that define application behavior from a user perspective
2. **Functional DSL Implementation** (.js files) - A JavaScript implementation of a functional programming model that powers test execution and business logic
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
│       ├── functional/         # Functional DSLs (.js files)
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
1. The Output Management component runs verification on the generated code
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

Our monorepo connects through:
- Shared domain models across all components
- Well-defined interfaces between components
- Standardized configuration for consistency
- Integration test suites that validate the entire pipeline

By understanding this architecture, developers can effectively contribute to the right parts of the system while maintaining the integrity of the overall workflow.

## Development Guidelines

When working in this monorepo, follow these guidelines:

- **Core Framework** (`/core`): Use for shared utilities, interfaces, and documentation
- **BDD Specifications** (`/specifications`): Contains feature files, DSLs, and test implementations
- **Code Generation Engine** (`/code-generation`): Contains parsers, templates, and transformation logic
- **Output Management** (`/output`): Contains generated code, validation and deployment tools

## Getting Started

1. Clone this repository
2. Initialize the monorepo components:
   ```bash
   npm run init
   ```
3. Browse the `.feature` files to understand the expected behaviors
4. Review the functional DSL implementation to see the testing approach
5. Examine the `.finapp` files to understand the UI specification format

For more details on the loan top-up implementation, see the [Lending Domain README](specifications/features/domains/lending/README.md).