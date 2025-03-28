# Clojure Implementation of FinApp Functional DSL

This directory contains a refactored version of the original JavaScript functional DSL using Clojure. Clojure is a modern Lisp dialect that runs on the JVM and provides powerful features for implementing Domain-Specific Languages.

## Why Clojure?

The original JavaScript implementation of our functional DSL was inspired by SICP (Structure and Interpretation of Computer Programs) principles. Clojure provides several advantages that make it a natural fit for this kind of interpreter-based DSL:

1. **Homoiconicity**: Code is represented as data, making it easier to manipulate expressions
2. **Immutable Data Structures**: Simplifies the implementation of our evaluator
3. **First-class Functions**: Native support for higher-order functions
4. **Lisp Heritage**: Direct lineage to the language family discussed in SICP
5. **JVM Integration**: Easy integration with existing Java-based enterprise systems

## Structure

The Clojure implementation follows the same architecture as the JavaScript version but takes advantage of Clojure's strengths:

- `src/finapp_dsl/core.clj`: The core evaluator and expression types
- `domains/lending/loan_topup_example.clj`: Domain-specific implementation for loan top-ups

## Key Improvements

1. **Simpler Expression Handling**: Clojure's natural support for maps makes representing expressions more concise
2. **More Functional Style**: Implementation follows a more purely functional approach
3. **Pattern Matching**: Uses Clojure's conditional expressions for cleaner evaluation logic
4. **Improved Error Handling**: Better exception system for runtime errors
5. **REPL Integration**: Interactive development through Clojure's REPL

## Usage

You can interact with the DSL through the Clojure REPL:

```clojure
;; Start a REPL session
lein repl

;; Import the loan topup example
(require '[finapp-dsl.loan-topup-example :as loan])

;; Test a loan top-up validation
(loan/validate-topup-amount "UK" "Wealth" 10000)
;; => true

;; Apply a business rule
(loan/apply-amount-rule "HK" "Basic" 5000)
;; => {:error "Amount outside limits"}
```

## Extending the DSL

To add new domain-specific functionality:

1. Create new functions in your domain namespace
2. Add them to the global environment
3. Create expression structures using the core constructors
4. Evaluate them with the core evaluator

## Benefits Over JavaScript Implementation

- **Cleaner Code**: More concise and expressive syntax
- **Better Performance**: JVM optimization for production use
- **Stronger Abstractions**: More powerful functional programming constructs
- **Interactive Development**: REPL-driven development and debugging
- **Macros**: Potential for extending the language with macros (advanced feature) 