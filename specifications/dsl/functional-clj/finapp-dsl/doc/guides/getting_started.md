# Getting Started with FinApp DSL

This guide will help you get started with the FinApp DSL, a domain-specific language for implementing financial business rules and customer journeys.

## Prerequisites

- [Leiningen](https://leiningen.org/) 2.9.0 or higher
- Clojure 1.11.0 or higher
- Basic familiarity with Clojure syntax

## Installation

Add FinApp DSL to your project's dependencies in `project.clj`:

```clojure
(defproject your-project "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [finapp-dsl "0.1.0-SNAPSHOT"]])
```

Or, clone the repository and build from source:

```bash
git clone https://github.com/AppAthanor/finapp-dsl-architecture.git
cd finapp-dsl-architecture/specifications/dsl/functional-clj/finapp-dsl
lein install
```

## Core Concepts

The FinApp DSL is built around these key concepts:

1. **Expressions** - The building blocks of the DSL
2. **Environment** - The context in which expressions are evaluated
3. **Business Rules** - Encapsulated business logic
4. **Domain Concepts** - Financial domain entities like regions and customer segments

### DSL Expression Types

The FinApp DSL provides several types of expressions:

- **Variables**: References to values in the environment
- **Applications**: Function application (calling functions)
- **Lambdas**: Anonymous functions
- **Conditionals**: If-then-else expressions
- **Sequences**: Series of expressions
- **Quoted values**: Literal values

## Your First DSL Program

Let's create a simple program that calculates a loan repayment amount:

```clojure
(ns my-app.loan-calculator
  (:require [finapp-dsl.core :as dsl]))

;; Set up the environment with functions and values
(defn setup-environment []
  (let [env (dsl/create-global-environment)
        
        ;; Define a simple interest calculator function
        calc-interest (fn [principal rate term]
                        (* principal (/ rate 100) (/ term 12)))]
    
    ;; Add function to environment
    (dsl/define-variable! 
      (dsl/make-variable "calculate-interest") 
      calc-interest 
      env)))

;; Build a DSL expression for calculating total repayment
(defn build-repayment-expression []
  (let [principal-var (dsl/make-variable "principal")
        rate-var (dsl/make-variable "rate")
        term-var (dsl/make-variable "term")
        
        ;; Expression to calculate interest
        interest-expr (dsl/make-application
                       (dsl/make-variable "calculate-interest")
                       [principal-var rate-var term-var])
        
        ;; Expression to calculate total repayment (principal + interest)
        repayment-expr (dsl/make-application
                        (dsl/make-variable "+")
                        [principal-var interest-expr])]
    
    repayment-expr))

;; Function to calculate repayment using the DSL
(defn calculate-repayment [principal rate term]
  (let [env (setup-environment)
        expr (build-repayment-expression)
        
        ;; Add loan values to environment
        env-with-values (-> env
                          (dsl/define-variable! (dsl/make-variable "principal") principal)
                          (dsl/define-variable! (dsl/make-variable "rate") rate)
                          (dsl/define-variable! (dsl/make-variable "term") term))]
    
    ;; Evaluate the expression
    (dsl/evaluate expr env-with-values)))

;; Example usage
(comment
  (calculate-repayment 10000 5.5 36) ;; => 11650.0
)
```

## Adding Business Rules

Business rules encapsulate validation and calculation logic:

```clojure
;; Create a business rule for loan eligibility
(def eligibility-rule
  (dsl/make-business-rule 
    "BR001"
    ;; Condition: is customer eligible?
    (dsl/make-lambda 
      ["income" "loan-amount" "credit-score"]
      (dsl/make-application
        (dsl/make-variable "and")
        [(dsl/make-application
           (dsl/make-variable ">=")
           [(dsl/make-variable "income")
            (dsl/make-application
              (dsl/make-variable "*")
              [(dsl/make-variable "loan-amount")
               (dsl/make-quoted 0.3)])])
         (dsl/make-application
           (dsl/make-variable ">=")
           [(dsl/make-variable "credit-score")
            (dsl/make-quoted 650)])]))
    ;; Action: return approval status
    (dsl/make-lambda
      ["income" "loan-amount" "credit-score"]
      (dsl/make-if
        (dsl/make-application
          (dsl/make-variable "is-eligible")
          [(dsl/make-variable "income")
           (dsl/make-variable "loan-amount")
           (dsl/make-variable "credit-score")])
        (dsl/make-quoted {:approved true})
        (dsl/make-quoted {:approved false, :reason "Income or credit score below requirements"})))))
```

## Working with Regions and Segments

The FinApp DSL includes built-in support for regions and customer segments:

```clojure
;; Get region-specific loan parameters
(defn get-loan-parameters [region-code segment-code]
  (let [env (create-global-environment)
        customer {:segmentCode segment-code}
        region (if (= region-code "UK") uk-region hk-region)]
    
    {:min-amount (get-min-amount customer region)
     :max-amount (get-max-amount customer region)
     :interest-rate (get-interest-rate customer region)}))
```

## Next Steps

Now that you understand the basics, you can:

1. Explore the [Loan Top-Up example](loan_topup.md) for a complete implementation
2. Learn about the [Regions](../concepts/regions.md) and [Customer Segments](../concepts/segments.md) concepts
3. Understand how to create [Business Rules](../concepts/business_rules.md) for your domain

## Development Tools

When developing with the FinApp DSL, these commands are helpful:

```bash
# Generate documentation
lein docs

# Run tests
lein test

# Run the REPL
lein repl
```

## Debugging DSL Programs

To debug DSL programs:

1. Use the REPL to evaluate expressions step by step
2. Print intermediate environments and expression values:

```clojure
(defn debug-expression [expr env]
  (println "Evaluating expression:" expr)
  (let [result (dsl/evaluate expr env)]
    (println "Result:" result)
    result))
```

## Common Patterns

Here are some common patterns when working with the FinApp DSL:

### Building Complex Expressions

```clojure
(defn build-complex-rule []
  (dsl/make-if
    ;; Condition
    (dsl/make-application
      (dsl/make-variable "check-condition")
      [(dsl/make-variable "value")])
    
    ;; Then branch
    (dsl/make-sequence
      [(dsl/make-application
        (dsl/make-variable "log-action")
        [(dsl/make-quoted "Condition met")])
       (dsl/make-variable "result-when-true")])
    
    ;; Else branch
    (dsl/make-variable "result-when-false")))
```

### Extending the Environment

```clojure
(defn extend-with-utilities [base-env]
  (-> base-env
      (dsl/define-variable! (dsl/make-variable "format-currency") 
                           (fn [amount] (str "£" (format "%.2f" amount))))
      (dsl/define-variable! (dsl/make-variable "calculate-tax") 
                           (fn [amount rate] (* amount (/ rate 100))))))
``` 