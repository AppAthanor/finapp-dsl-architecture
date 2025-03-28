(ns finapp-dsl.cljs-bridge
  (:require [finapp-dsl.core :as dsl]
            [finapp-dsl.loan-topup-example :as loan-topup]
            [cljs.nodejs :as nodejs]
            [cljs.core :refer [*command-line-args*]]))

;; Define exports object
(def exports #js {})

;; Core DSL functions
(set! (.-makeVariable exports)
      (fn [name]
        (clj->js (dsl/make-variable name))))

(set! (.-makeApplication exports)
      (fn [operator operands]
        (clj->js (dsl/make-application 
                   (js->clj operator :keywordize-keys true)
                   (js->clj operands)))))

(set! (.-makeLambda exports)
      (fn [parameters body]
        (clj->js (dsl/make-lambda
                   (js->clj parameters)
                   (js->clj body :keywordize-keys true)))))

(set! (.-makeIf exports)
      (fn [predicate consequent alternative]
        (clj->js (dsl/make-if
                   (js->clj predicate :keywordize-keys true)
                   (js->clj consequent :keywordize-keys true)
                   (js->clj alternative :keywordize-keys true)))))

(set! (.-makeAssignment exports)
      (fn [variable value]
        (clj->js (dsl/make-assignment
                   (js->clj variable :keywordize-keys true)
                   (js->clj value :keywordize-keys true)))))

(set! (.-makeSequence exports)
      (fn [expressions]
        (clj->js (dsl/make-sequence
                   (js->clj expressions :keywordize-keys true)))))

(set! (.-makeQuoted exports)
      (fn [text]
        (clj->js (dsl/make-quoted text))))

;; Environment operations
(set! (.-createGlobalEnvironment exports)
      (fn []
        (clj->js (dsl/create-global-environment))))

(set! (.-extendEnvironment exports)
      (fn [vars vals base-env]
        (clj->js (dsl/extend-environment
                   (js->clj vars)
                   (js->clj vals)
                   (js->clj base-env :keywordize-keys true)))))

(set! (.-lookupVariableValue exports)
      (fn [var env]
        (clj->js (dsl/lookup-variable-value
                   (js->clj var :keywordize-keys true)
                   (js->clj env :keywordize-keys true)))))

(set! (.-defineVariable exports)
      (fn [var val env]
        (clj->js (dsl/define-variable!
                   (js->clj var :keywordize-keys true)
                   (js->clj val)
                   (js->clj env :keywordize-keys true)))))

(set! (.-setVariableValue exports)
      (fn [var val env]
        (clj->js (dsl/set-variable-value!
                   (js->clj var :keywordize-keys true)
                   (js->clj val)
                   (js->clj env :keywordize-keys true)))))

;; Evaluation
(set! (.-evaluate exports)
      (fn [exp env]
        (clj->js (dsl/evaluate
                   (js->clj exp :keywordize-keys true)
                   (js->clj env :keywordize-keys true)))))

;; Business domain
(set! (.-ukRegion exports)
      (clj->js dsl/uk-region))

(set! (.-hkRegion exports)
      (clj->js dsl/hk-region))

(set! (.-basicSegment exports)
      (clj->js dsl/basic-segment))

(set! (.-wealthSegment exports)
      (clj->js dsl/wealth-segment))

;; Loan topup specific functions
(set! (.-applyAmountRule exports)
      (fn [region segment baseAmount]
        (loan-topup/apply-amount-rule 
          (js->clj region)
          (js->clj segment)
          (js->clj baseAmount))))

(set! (.-setupLoanTopupEnvironment exports)
      (fn []
        (clj->js (loan-topup/setup-loan-topup-environment))))

(set! (.-buildOfferCalculation exports)
      (fn [env]
        (clj->js (loan-topup/build-offer-calculation 
                   (js->clj env :keywordize-keys true)))))

;; UI components
(set! (.-initialOfferScreen exports)
      (clj->js loan-topup/initial-offer-screen))

(set! (.-amountSelectionScreen exports)
      (clj->js loan-topup/amount-selection-screen))

(set! (.-termsReviewScreen exports)
      (clj->js loan-topup/terms-review-screen))

(set! (.-confirmationScreen exports)
      (clj->js loan-topup/confirmation-screen))

(set! (.-successScreen exports)
      (clj->js loan-topup/success-screen))

;; Set the module exports
(set! (.-exports js/module) exports)

;; Enable command line testing
(defn -main [& args]
  (println "ClojureScript Financial DSL Bridge loaded."))

;; Set up main function for Node.js
(set! *main-cli-fn* -main) 