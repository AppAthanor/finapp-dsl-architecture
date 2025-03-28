# FinApp DSL

A functional domain-specific language (DSL) for financial applications, focusing on business rules, customer journeys, and multi-region support.

## Overview

FinApp DSL is a Clojure-based implementation of a functional programming model that powers test execution and business logic for financial applications. It enables precise definition of business rules, customer journeys, and calculations with support for:

- Multi-region behavior (UK, Hong Kong, etc.)
- Customer segmentation
- Business rule evaluation
- Financial calculations
- Consistent behavior across platforms

## Installation

Add the following dependency to your `project.clj`:

```clojure
[finapp-dsl "0.1.0-SNAPSHOT"]
```

Or install from source:

```bash
git clone https://github.com/AppAthanor/finapp-dsl-architecture.git
cd finapp-dsl-architecture/specifications/dsl/functional-clj/finapp-dsl
lein install
```

## Documentation

The API documentation is available at:
- [Online documentation](https://appanthanor.github.io/finapp-dsl-architecture/)
- Local: Generate with `make docs` and view at `doc/api/index.html`

For more details on the documentation system, see [Documentation Automation](doc/AUTOMATION.md).

## Usage

```clojure
(require '[finapp-dsl.core :as dsl])

;; Create an evaluation environment with business context
(def env (dsl/create-global-environment))

;; Define a simple business rule
(def eligibility-rule 
  (dsl/make-business-rule
    "BR001"
    (dsl/make-lambda ["customer" "region"] 
      (dsl/make-application >= [(dsl/make-application :balance [(dsl/make-variable "customer")]) 1000]))
    (dsl/make-lambda ["customer" "region"]
      (dsl/make-quoted "Customer is eligible"))))

;; Evaluate expressions against an environment
(def customer {:id "C123" :balance 1500 :segment "Basic"})
(dsl/evaluate 
  (dsl/make-application eligibility-rule [customer dsl/uk-region])
  env)
;; => "Customer is eligible"
```

## Examples

### Business Rule Definition

```clojure
(def topup-amount-limits-rule
  (dsl/make-business-rule
    "BR002"
    ;; Condition expression that checks if amount is within segment limits
    (dsl/make-lambda ["customer" "region" "amount"] 
      (dsl/make-application <= [(dsl/make-variable "amount") 
                               (dsl/make-application :max-amount 
                                 [(dsl/make-variable "customer") 
                                  (dsl/make-variable "region")])])),
    ;; Action expression that validates or adjusts the amount
    (dsl/make-lambda ["customer" "region" "amount"] 
      (dsl/make-if 
        (dsl/make-application <= [(dsl/make-variable "amount") 0])
        (dsl/make-quoted "Amount must be positive")
        (dsl/make-variable "amount")))))
```

### Customer Segmentation

```clojure
(def wealth-segment-benefits
  (dsl/make-lambda ["region"]
    (dsl/make-if
      (dsl/make-application = [(dsl/make-variable "region") dsl/uk-region])
      (dsl/make-quoted "Premier rate guarantee and priority service")
      (dsl/make-quoted "Jade member priority processing and rate discount"))))
```

## Development

Run tests:
```bash
lein test
```

Format code:
```bash
lein cljfmt fix
```

## License

Copyright © 2025 AppAthanor

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
