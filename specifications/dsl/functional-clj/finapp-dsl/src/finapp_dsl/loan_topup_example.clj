(ns finapp-dsl.loan-topup-example
  (:require [finapp-dsl.core :as dsl]))

;; Loan topup business rules

(defn get-max-amount-by-region [region]
  (case region
    "UK" 25000
    "HK" 150000
    10000)) ; default

(defn get-min-amount-by-region [region]
  (case region
    "UK" 1000
    "HK" 5000
    500)) ; default

(defn get-amount-multiplier-by-segment [segment]
  (case segment
    "Basic" 1.0
    "Wealth" 1.5
    1.0)) ; default

(defn apply-amount-rule
  "Apply business rules to determine the eligible loan topup amount"
  [region segment base-amount]
  (let [max-amount (get-max-amount-by-region region)
        min-amount (get-min-amount-by-region region)
        multiplier (get-amount-multiplier-by-segment segment)
        calculated-amount (* base-amount multiplier)]
    (cond
      (> calculated-amount max-amount) max-amount
      (< calculated-amount min-amount) min-amount
      :else calculated-amount)))

;; Loan topup journey using the DSL

(defn build-offer-calculation [env]
  (let [region-var (dsl/make-variable "region")
        segment-var (dsl/make-variable "segment")
        base-amount-var (dsl/make-variable "baseAmount")
        
        region-check (dsl/make-if
                      (dsl/make-application
                       (dsl/make-variable "equal?")
                       [(dsl/make-variable "region")
                        (dsl/make-quoted "UK")])
                      (dsl/make-quoted "UK")
                      (dsl/make-quoted "Other"))
                      
        calc-expr (dsl/make-application
                   (dsl/make-variable "apply-amount-rule")
                   [region-var 
                    segment-var 
                    base-amount-var])]
    calc-expr))

(defn setup-loan-topup-environment []
  (let [env (dsl/create-global-environment)
        
        ;; Define base functions
        equal-fn (fn [a b] (= a b))
        add-fn (fn [a b] (+ a b))
        subtract-fn (fn [a b] (- a b))
        multiply-fn (fn [a b] (* a b))
        
        ;; Define domain-specific functions
        apply-amount-rule-fn apply-amount-rule]
    
    ;; Extend environment with functions
    (-> env
        (dsl/define-variable! (dsl/make-variable "equal?") equal-fn)
        (dsl/define-variable! (dsl/make-variable "add") add-fn)
        (dsl/define-variable! (dsl/make-variable "subtract") subtract-fn)
        (dsl/define-variable! (dsl/make-variable "multiply") multiply-fn)
        (dsl/define-variable! (dsl/make-variable "apply-amount-rule") apply-amount-rule-fn))))

;; UI journey screens (simplified representations for demonstration)
(def initial-offer-screen {:type "screen" :name "InitialOfferScreen"})
(def amount-selection-screen {:type "screen" :name "AmountSelectionScreen"})
(def terms-review-screen {:type "screen" :name "TermsReviewScreen"})
(def confirmation-screen {:type "screen" :name "ConfirmationScreen"})
(def success-screen {:type "screen" :name "SuccessScreen"})

;; Test function to demonstrate DSL usage
(defn test-loan-topup-calculation []
  (let [env (setup-loan-topup-environment)
        calc-expr (build-offer-calculation env)
        
        ;; Set up test values
        env-with-values (-> env
                           (dsl/define-variable! (dsl/make-variable "region") "UK")
                           (dsl/define-variable! (dsl/make-variable "segment") "Wealth")
                           (dsl/define-variable! (dsl/make-variable "baseAmount") 10000))
        
        ;; Evaluate
        result (dsl/evaluate calc-expr env-with-values)]
    
    (println "Loan topup calculation result:" result)
    result)) 