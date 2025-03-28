# Lending Domain BDD Specifications

This directory contains the Behaviour Driven Development (BDD) specifications for lending-related features, including the Loan Top-up journey.

## Features

### Loan Top-up

The [Loan Top-up journey](loan-topup.feature) allows pre-approved customers to borrow additional funds on their existing loan without applying for a new loan. This streamlined process includes:

- Initial offer presentation
- Amount selection and customisation 
- Terms and impact review
- Confirmation and fund disbursement

The implementation handles:
- Multiple regions (UK and Hong Kong)
- Different customer segments (Basic and Wealth)
- Time-sensitive content (promotional offers, time-of-day messaging)

#### Key Files

- [loan-topup.feature](loan-topup.feature) - Gherkin feature file with scenarios
- [loan-topup-steps.js](../../../step-definitions/domain-specific/loan-topup-steps.js) - Step implementations
- [loan-topup-functional-dsl.js](../../../dsl/loan-topup-functional-dsl.js) - SICP-inspired functional DSL implementation

#### Testing the Loan Top-up Journey

To run tests for the loan top-up journey:

```bash
# Run all loan top-up scenarios
npm test -- --tags @loan-topup

# Run for a specific region
npm test -- --tags @loan-topup --region UK

# Run for a specific customer segment
npm test -- --tags @loan-topup --segment Wealth

# Run for specific time scenarios
npm test -- --tags @loan-topup --time-period evening
```

## Functional DSL Approach

The Loan Top-up journey implements a functional DSL approach inspired by "Structure and Interpretation of Computer Programs" (SICP) principles:

1. **Metalinguistic Abstraction** - Creating a domain-specific language for loan features
2. **Expression Evaluation** - Using a proper evaluator for expressions
3. **Environment Model** - Implementing lexical scoping and closures
4. **Data Abstraction** - Separating implementation from interface
5. **Business Rules as Data** - Representing rules as expressions that can be analyzed

This approach allows for elegant handling of complex variations in region, customer segment, and time-sensitive content.

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