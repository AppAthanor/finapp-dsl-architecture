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

- Defines an expression evaluator with proper lexical scoping
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

## Testing

To test the Loan Top-up feature, the step implementations in [loan-topup-steps.js](../../../bdd-specifications/step-definitions/domain-specific/loan-topup-steps.js) connect the Gherkin scenarios to the functional DSL. The tests can validate:

- Region-specific formatting and content
- Segment-appropriate offers and limits
- Time-sensitive processing rules
- Proper regulatory disclosures
- Correct calculations and terms

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