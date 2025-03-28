# BDD Specifications Repository

This repository contains Behaviour Driven Development (BDD) specifications for our banking applications, organized by business domains. These specifications serve as living documentation of system behavior and form the basis for automated acceptance testing.

## Latest Addition: Loan Top-up Journey

We've recently added the [Loan Top-up Journey](features/domains/lending/README.md), which demonstrates:
- Internationalization for UK and Hong Kong markets
- Customer segmentation (Basic and Wealth)
- Time-sensitive content handling
- SICP-inspired functional DSL implementation

This implementation serves as a reference architecture for future journey specifications.

## Repository Structure

```
bdd-specifications/
│
├── features/                     # Gherkin feature files
│   ├── domains/                  # Organised by business domains
│   │   ├── authentication/       # Authentication features
│   │   ├── payments/             # Payment features
│   │   ├── lending/              # Lending features (including loan top-up)
│   │   └── ...                   # Other domains
│   ├── cross-cutting/            # Cross-domain features
│   └── meta/                     # Features about the BDD process itself
│
├── step-definitions/             # Step implementation patterns
│   ├── common/                   # Reusable steps across domains
│   ├── domain-specific/          # Domain-specific steps
│   └── support/                  # Support code for steps
│
├── templates/                    # Reusable templates
│
├── dsl/                          # Domain-specific language definitions
│   └── loan-topup-functional-dsl.js # Functional DSL for loan top-up
│
├── docs/                         # Documentation
│   ├── getting-started.md
│   └── best-practices.md
│
├── tools/                        # Utilities specific to BDD
│
├── tests/                        # Tests for the BDD framework itself
│
├── examples/                     # Complete examples
│
├── .github/                      # GitHub configuration
│
├── config/                       # Configuration files
│
└── README.md                     # This README file
```

## Getting Started

1. Clone this repository
2. Install dependencies:
   ```bash
   npm install
   ```
3. Run the tests:
   ```bash
   npm test
   ```

## Working with BDD Specifications

### Creating New Features

1. Create a new feature file in the appropriate domain directory
2. Follow the templates in `/templates/scenario-templates/`
3. Implement step definitions in `/step-definitions/domain-specific/`
4. Add domain terminology to the appropriate DSL file

### Running Tests

```bash
# Run all tests
npm test

# Run tests for a specific domain
npm test -- --tags @lending

# Run tests for a specific feature
npm test -- --tags @loan-topup

# Run tests against a specific environment
npm test -- --env staging
```

## Functional DSL Approach

The Loan Top-up journey introduces a functional programming approach inspired by "Structure and Interpretation of Computer Programs" (SICP) principles. This approach:

1. Represents business rules as expressions
2. Uses a proper evaluator for expressions
3. Implements lexical scoping and closures
4. Creates clear abstraction barriers
5. Makes rules analyzable and transformable

For more details, see the [Loan Top-up Feature README](features/domains/lending/README.md).

## Key Domain Areas

| Domain | Description | Key Features |
|--------|-------------|--------------|
| Authentication | User identity and access | Login, registration, account recovery |
| Payments | Money transfers and payments | Domestic transfers, international payments, scheduled payments |
| Lending | Borrowing products and services | Loan top-up, mortgage applications, overdrafts |
| Accounts | Account management | Account opening, statements, transactions |
| Cards | Card management | Card applications, PIN management, card controls |
| Investments | Investment products | Trading, portfolios, fund selection |

## Contributing

1. Fork this repository
2. Create a feature branch
3. Add or modify specifications
4. Ensure all tests pass
5. Submit a pull request

Please follow our [contribution guidelines](.github/CONTRIBUTING.md) when submitting changes.

## Best Practices

See our [BDD Best Practices](docs/best-practices.md) document for guidance on:
- Writing effective Gherkin scenarios
- Creating reusable step definitions
- Managing test data
- Organizing features by domain

## Contact

For questions or support, contact the BDD Framework team.