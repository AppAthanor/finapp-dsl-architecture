# Loan Top-up Journey

This directory contains the Behaviour Driven Development (BDD) specifications and Domain-Specific Language (DSL) implementation for the Loan Top-up customer journey.

## Overview

The Loan Top-up journey allows pre-approved customers to borrow additional funds on their existing loan. The implementation handles:
- Multiple regions (UK and Hong Kong)
- Different customer segments (Basic and Wealth)
- Time-sensitive content (promotional offers, time-of-day specific messaging)

## Files and Directory Structure

### BDD Specifications
- `features/domains/loans/loan-topup.feature` - Gherkin feature file with scenarios
- `step-definitions/domain-specific/loan-topup-steps.js` - Step implementations

### DSL Implementation
- `dsl/banking-terms.json` - Domain terminology and business rules in JSON format
- `dsl/loan-topup-functional-dsl.js` - SICP-inspired functional DSL implementation

### Documentation
- `docs/loan-topup-dsl-explanation.md` - Detailed explanation of the functional DSL approach
- `features/domains/loans/README.md` - This README file

## Functional DSL Approach

This implementation uses a functional programming approach inspired by "Structure and Interpretation of Computer Programs" (SICP) principles:

1. **Metalinguistic Abstraction** - Building a domain-specific language for loan top-ups
2. **Expression Evaluation** - Using a proper evaluator for expressions
3. **Environment Model** - Implementing lexical scoping and closures
4. **Data Abstraction** - Creating clear separation between implementation and interface
5. **Business Rules as Data** - Representing rules as expressions that can be analyzed and manipulated

This approach allows us to handle the complex requirements of internationalization, customer segmentation, and time-sensitive content in a clean, maintainable way.

## Running the Tests

To run tests based on these specifications:

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

## Test Data Requirements

For testing, you'll need:
1. Test customers for each segment (Basic and Wealth) in each region (UK and HK)
2. Pre-approval flags and limits set for these customers
3. Mock date/time capabilities for testing time-sensitive scenarios

## Implementation Guide

### 1. Start with BDD
Review the `loan-topup.feature` file to understand the journey from a user perspective, including all the variations for regions, customer segments, and time-sensitive content.

### 2. Understand the DSL
The `loan-topup-functional-dsl.js` file contains the DSL implementation that powers the journey. The DSL is based on expressions that can be evaluated, analyzed, and transformed.

### 3. Connect BDD to DSL
The step definitions in `loan-topup-steps.js` show how the BDD scenarios map to expressions in the DSL.

### 4. Extend as Needed
To add new regions, customer segments, or time-sensitive content:
- Add new region/segment definitions in the DSL
- Add new business rules as needed
- Update the BDD scenarios to cover new variations

## Related Documentation

For more information, see:
- [Loan Top-up Business Requirements](../../../docs/requirements/loan-topup-requirements.md)
- [Internationalization Guidelines](../../../docs/internationalization/i18n-guidelines.md)
- [Customer Segmentation Strategy](../../../docs/business/customer-segmentation.md)
- [SICP-Enhanced Functional DSL Architecture](../../../docs/architecture/functional-dsl-architecture.md)