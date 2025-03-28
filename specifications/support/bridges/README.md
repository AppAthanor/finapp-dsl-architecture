# JavaScript-Clojure DSL Bridge

This directory contains bridge files that allow JavaScript code (such as Cucumber step definitions) to communicate with the Clojure implementation of the functional DSL.

## How It Works

The bridge uses the following approach to enable JavaScript to Clojure interoperability:

1. **Proxy Interface**: Creates a JavaScript object that mimics the API of the previous JavaScript implementation but delegates to Clojure functions
2. **Lumo Integration**: Uses Lumo (ClojureScript runtime for Node.js) to evaluate Clojure code from JavaScript
3. **API Mapping**: Maps JavaScript function names to their Clojure equivalents

## Usage

To use the Clojure DSL from JavaScript:

```javascript
// Import the bridge
const ClojureDSL = require('../../support/bridges/clojure-dsl-bridge');

// Load the Clojure DSL module
const FunctionalDSL = ClojureDSL.loadDSL('../../dsl/functional-clj/domains/lending/loan_topup_example');

// Use it just like you would use the JavaScript version
const result = FunctionalDSL.evaluate(
  FunctionalDSL.makeApplication(
    FunctionalDSL.makeVariable('isWithinLimits'),
    [
      FunctionalDSL.makeQuoted(customer),
      FunctionalDSL.makeQuoted(region),
      FunctionalDSL.makeQuoted(amount)
    ]
  ),
  environment
);
```

## Prerequisites

To enable Clojure-JavaScript interop, you need:

1. Node.js (v14+)
2. Lumo ClojureScript runtime (`npm install -g lumo-cljs` or `npm run lumo:install`)
3. The Clojure implementation must be compiled to `.cljs` files or be compatible with ClojureScript

## Implementation Details

The bridge uses several techniques to maintain compatibility:

1. **Function Name Conversion**: Kebab-case (Clojure) to camelCase (JavaScript) conversion
2. **Data Structure Translation**: JavaScript objects/arrays to Clojure maps/vectors and back
3. **Error Handling**: Proper error propagation between languages
4. **Performance Optimization**: Caching of frequently used functions and evaluation results 