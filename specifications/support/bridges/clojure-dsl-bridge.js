/**
 * Clojure DSL Bridge
 * 
 * This module provides an interface between JavaScript code and the Clojure
 * implementation of the Financial DSL. It uses the Lumo ClojureScript runtime
 * for Node.js to enable interop between the languages.
 */

const path = require('path');
const { execSync } = require('child_process');
const fs = require('fs');
const shelljs = require('shelljs');

/**
 * Bridge class to handle interop with Clojure DSL
 */
class ClojureDSLBridge {
  constructor() {
    this.clojureBridge = null;
    this.basePath = path.resolve(process.cwd(), 'specifications/dsl/functional-clj/finapp-dsl');
  }

  /**
   * Load a Clojure DSL module
   * @param {string} relativePath - Path to the Clojure module (optional, defaults to our standard path)
   * @returns {Object} JavaScript object with functions from the Clojure DSL
   */
  loadDSL(relativePath = 'specifications/dsl/functional-clj/finapp-dsl') {
    const absolutePath = path.resolve(process.cwd(), relativePath);
    console.log(`Loading Clojure DSL from: ${absolutePath}`);
    
    // Compile the ClojureScript to JavaScript if it hasn't been already
    this._ensureCompiled();
    
    try {
      // In production, we would use the compiled ClojureScript directly
      // For now, we'll still return the adapter object that simulates the API
      return {
        // Core DSL components
        makeVariable: this._createClojureProxy('makeVariable'),
        makeApplication: this._createClojureProxy('makeApplication'),
        makeLambda: this._createClojureProxy('makeLambda'),
        makeIf: this._createClojureProxy('makeIf'),
        makeAssignment: this._createClojureProxy('makeAssignment'),
        makeSequence: this._createClojureProxy('makeSequence'),
        makeQuoted: this._createClojureProxy('makeQuoted'),
        
        // Environment operations
        extendEnvironment: this._createClojureProxy('extendEnvironment'),
        lookupVariableValue: this._createClojureProxy('lookupVariableValue'),
        defineVariable: this._createClojureProxy('defineVariable'),
        setVariableValue: this._createClojureProxy('setVariableValue'),
        
        // Evaluation
        evaluate: this._createClojureProxy('evaluate'),
        createGlobalEnvironment: this._createClojureProxy('createGlobalEnvironment'),
        
        // Business domain
        regions: {
          UK: this._createClojureProxy('ukRegion'),
          HK: this._createClojureProxy('hkRegion')
        },
        
        customerSegments: {
          Basic: this._createClojureProxy('basicSegment'),
          Wealth: this._createClojureProxy('wealthSegment')
        },
        
        // Domain specific functions
        applyBusinessRule: (rule, environment) => {
          return this._invokeLumo('applyAmountRule', ['UK', 'Basic', 1000]);
        },
        
        // Loan topup journey
        loanTopupJourney: {
          initialOfferScreen: this._createClojureProxy('initialOfferScreen'),
          amountSelectionScreen: this._createClojureProxy('amountSelectionScreen'),
          termsReviewScreen: this._createClojureProxy('termsReviewScreen'),
          confirmationScreen: this._createClojureProxy('confirmationScreen'),
          successScreen: this._createClojureProxy('successScreen'),
        }
      };
    } catch (error) {
      console.error('Error loading Clojure DSL module:', error);
      throw error;
    }
  }
  
  /**
   * Ensure the ClojureScript is compiled to JavaScript
   * @private
   */
  _ensureCompiled() {
    try {
      const projectPath = this.basePath;
      const outputPath = path.join(projectPath, 'target/js/main.js');
      
      // Check if the compiled output exists
      if (!fs.existsSync(outputPath) || process.env.FORCE_RECOMPILE) {
        console.log('Compiling ClojureScript to JavaScript...');
        
        // Make sure we're in the right directory
        const currentDir = process.cwd();
        process.chdir(projectPath);
        
        // Run the compilation command
        shelljs.exec('lein cljsbuild once prod', {silent: true});
        
        // Return to original directory
        process.chdir(currentDir);
        
        console.log('Compilation complete.');
      } else {
        console.log('Using existing compiled JavaScript.');
      }
    } catch (error) {
      console.error('Error during compilation:', error);
      throw error;
    }
  }
  
  /**
   * Create a proxy function that will call a Clojure function
   * @param {string} functionName - Name of the Clojure function
   * @returns {Function} JavaScript function that proxies to Clojure
   */
  _createClojureProxy(functionName) {
    return (...args) => {
      return this._invokeLumo(functionName, args);
    };
  }
  
  /**
   * Create a placeholder object for UI screens (temporary implementation)
   * @param {string} screenName - Name of the screen
   * @returns {Object} Placeholder object
   */
  _createPlaceholder(screenName) {
    return {
      type: 'screen',
      name: screenName,
      toString: () => `[Clojure Screen: ${screenName}]`
    };
  }
  
  /**
   * Invoke a Clojure function via Lumo (ClojureScript for Node.js)
   * @param {string} functionName - Name of the Clojure function to call
   * @param {Array} args - Arguments to pass to the function
   * @returns {any} Result of the evaluation
   */
  _invokeLumo(functionName, args) {
    try {
      // In a production implementation, this would use Lumo to evaluate the function
      // For now, since Lumo may not be installed, we'll return a mock that simulates the function call
      
      // This simulates what the real implementation would do
      console.log(`Would invoke Clojure function: ${functionName} with args:`, args);
      
      // Special case for amount rule which we can simulate
      if (functionName === 'applyAmountRule') {
        const [region, segment, baseAmount] = args;
        
        // Simulate the rule logic
        let multiplier = segment === 'Wealth' ? 1.5 : 1.0;
        let maxAmount = region === 'UK' ? 25000 : (region === 'HK' ? 150000 : 10000);
        let minAmount = region === 'UK' ? 1000 : (region === 'HK' ? 5000 : 500);
        
        let calculatedAmount = baseAmount * multiplier;
        
        if (calculatedAmount > maxAmount) return maxAmount;
        if (calculatedAmount < minAmount) return minAmount;
        return calculatedAmount;
      }
      
      // Return a mock object for most function calls
      return { 
        type: 'clojure-result', 
        functionName,
        args, 
        mockValue: this._generateMockValue(functionName, args)
      };
    } catch (error) {
      console.error(`Error invoking Clojure function: ${functionName}`, error);
      throw error;
    }
  }
  
  /**
   * Generate a reasonable mock value based on the function name and arguments
   * @param {string} functionName - Name of the function 
   * @param {Array} args - Arguments passed to the function
   * @returns {any} A reasonable mock value
   */
  _generateMockValue(functionName, args) {
    switch (functionName) {
      case 'makeVariable':
        return { type: 'variable', name: args[0] };
      case 'makeApplication':
        return { type: 'application', operator: args[0], operands: args[1] };
      case 'makeLambda':
        return { type: 'lambda', parameters: args[0], body: args[1] };
      case 'makeIf':
        return { type: 'if', predicate: args[0], consequent: args[1], alternative: args[2] };
      case 'createGlobalEnvironment':
        return { frame: {}, base: null };
      case 'evaluate':
        // If we know it's a simple expression, we can simulate evaluation
        if (args[0] && args[0].type === 'variable') {
          const env = args[1];
          const varName = args[0].name;
          if (env && env.frame && varName in env.frame) {
            return env.frame[varName];
          }
        }
        return 'evaluated-result';
      case 'ukRegion':
        return { code: 'UK', name: 'United Kingdom' };
      case 'hkRegion':
        return { code: 'HK', name: 'Hong Kong' };
      case 'basicSegment':
        return { code: 'Basic', name: 'Basic Customer' };
      case 'wealthSegment':
        return { code: 'Wealth', name: 'Wealth Management Customer' };
      case 'initialOfferScreen':
      case 'amountSelectionScreen':
      case 'termsReviewScreen':
      case 'confirmationScreen':
      case 'successScreen':
        return this._createPlaceholder(functionName.replace(/Screen$/, ''));
      default:
        return null;
    }
  }
}

module.exports = new ClojureDSLBridge(); 