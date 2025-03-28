# Loan Top-up Journey BDD Specifications

This directory contains the Behaviour Driven Development (BDD) specifications for the Loan Top-up customer journey, including internationalization for multiple regions, customer segmentation, and time-sensitive content.

## Overview

The Loan Top-up journey allows pre-approved customers to borrow additional funds on their existing loan. This streamlined process eliminates the need for a full new loan application, providing customers with quick access to extra funds when needed.

## Journey Flow

1. **Initial Offer Screen**: Customer is presented with a personalised offer to borrow more
2. **Amount Selection**: Customer selects or customises the additional amount they wish to borrow
3. **Terms & Impact**: Customer reviews how the top-up affects their loan details
4. **Confirmation**: Customer reviews and accepts the final offer
5. **Disbursement**: Funds are made available in the customer's account

## Key Complexity Dimensions

### 1. Internationalization (UK and Hong Kong)
- **Currency**: GBP (£) for UK and HKD ($) for Hong Kong
- **Language**: English for UK, English and Traditional Chinese for Hong Kong
- **Date Formats**: Regional date formatting standards
- **Regulations**: FCA requirements for UK, HKMA requirements for Hong Kong
- **Banking Hours**: Different processing timeframes based on local banking hours

### 2. Customer Segmentation (Basic and Wealth)
- **Interest Rates**: Different rates based on customer segment
- **Borrowing Limits**: Higher amounts available for Wealth segment
- **Benefits**: Segment-specific benefits and special offers
- **Follow-up**: Different follow-up processes based on segment 

### 3. Time-Sensitive Content
- **Promotional Banners**: Seasonal offers displayed based on current date
- **Service Hours Messaging**: Different messaging based on time of day
- **Processing Timeframes**: Varied fund availability messaging based on banking hours
- **Holiday Periods**: Special notices during bank holidays and festive periods

## BDD Files Structure

```
features/domains/loans/
└── loan-topup.feature            # Gherkin feature file with scenarios

step-definitions/domain-specific/
└── loan-topup-steps.js           # Step implementations for loan top-up scenarios

dsl/
└── loan-terms.json               # Domain-specific language definitions for loans
```

## Key Scenarios

The BDD specifications cover these key scenarios:

1. **Offer Presentation**: How the pre-approved offer is presented to eligible customers
2. **Amount Selection**: How customers select or customise their top-up amount
3. **Terms Review**: How loan details and impacts are presented
4. **Confirmation Process**: How customers confirm their acceptance
5. **Disbursement**: How and when funds are made available
6. **Edge Cases**: Handling scenarios like abandonment, declining offers, etc.

Each scenario includes variations for:
- Different regions (UK, Hong Kong)
- Different customer segments (Basic, Wealth)
- Different times of day and seasonal periods

## Testing with These Specifications

To run tests based on these specifications:

```bash
# Run all loan top-up scenarios
npm test -- --tags @loan-topup

# Run specific scenarios
npm test -- --tags @loan-topup-amount-selection

# Run against a specific environment
npm test -- --tags @loan-topup --env staging

# Run for a specific region
npm test -- --tags @loan-topup --region UK

# Run for a specific customer segment
npm test -- --tags @loan-topup --segment Wealth

# Run for specific time scenarios
npm test -- --tags @loan-topup --time-period evening
```

## Test Data Requirements

The tests require the following test data to be set up:

1. Test customers for each segment (Basic and Wealth) in each region (UK and HK)
2. Pre-approval flags and limits set for these customers
3. Bank accounts for disbursement testing
4. Multiple language preferences configured
5. Mock date/time capabilities for testing time-sensitive scenarios
6. Mock seasonal promotion configurations

## Related Documents

- [Loan Top-up Business Requirements](../../../docs/requirements/loan-topup-requirements.md)
- [Loan Top-up UI/UX Designs](../../../designs/loan-topup-designs.md)
- [API Specifications](../../../apis/loan-topup-api.md)
- [Internationalization Guidelines](../../../docs/internationalization/i18n-guidelines.md)
- [Customer Segmentation Strategy](../../../docs/business/customer-segmentation.md)
- [Promotional Calendar](../../../docs/marketing/promotional-calendar.md)

## Contributing

When adding new scenarios or modifying existing ones:

1. Follow the domain-specific language defined in `loan-terms.json`
2. Ensure scenarios are atomic and focused on a single aspect of behaviour
3. Include appropriate tags to categorise scenarios
4. Update step definitions to support new or modified steps
5. Verify scenarios pass in the development environment before submitting PR
6. Ensure all scenarios support the required regions, customer segments, and time-sensitive variations

## Regulatory Considerations

The loan top-up journey must comply with:

### United Kingdom
- Consumer Credit Act regulations
- Financial Conduct Authority (FCA) requirements
- Treating Customers Fairly (TCF) principles
- Responsible lending guidelines

### Hong Kong
- Hong Kong Monetary Authority (HKMA) requirements
- Money Lenders Ordinance
- Code of Banking Practice

All scenarios should be reviewed for compliance with these regulations.