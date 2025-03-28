(ns finapp-dsl.loan-topup-example
  (:require [finapp-dsl.core :as core]))

;; ===== Domain-Specific Constants =====

;; Region definitions
(def uk-region
  (core/make-region "UK"
    {:currency "GBP"
     :currencySymbol "£"
     :dateFormat "DD/MM/YYYY"
     :regulatoryBody "FCA"
     :coolingOffPeriod 14
     :language "en-GB"
     :translationsKey "uk_translations"}))

(def hk-region
  (core/make-region "HK"
    {:currency "HKD"
     :currencySymbol "HK$"
     :dateFormat "DD/MM/YYYY"
     :regulatoryBody "HKMA"
     :coolingOffPeriod 10
     :language "zh-HK"
     :translationsKey "hk_translations"}))

;; Customer segment definitions
(def basic-segment
  (core/make-customer-segment "Basic"
    {:description "Regular retail banking customers"
     :interestRates {"UK" 6.5, "HK" 6.9}
     :minTopupAmounts {"UK" 1000, "HK" 10000}
     :maxTopupAmounts {"UK" 25000, "HK" 200000}
     :benefits {"UK" ["Standard processing"], "HK" ["Standard processing"]}}))

(def wealth-segment
  (core/make-customer-segment "Wealth"
    {:description "Premier or priority banking customers with higher value accounts"
     :interestRates {"UK" 5.4, "HK" 5.8}
     :minTopupAmounts {"UK" 5000, "HK" 50000}
     :maxTopupAmounts {"UK" 100000, "HK" 800000}
     :benefits {"UK" ["Preferential rates" "Dedicated relationship manager" "Fee waivers"]
               "HK" ["Priority processing" "Jade status points" "Fee waivers"]}}))

;; Create a simple business rule for amount limits
(def amount-limits-rule
  (core/make-business-rule 
    "BR001"
    ;; Condition: is amount within limits?
    (core/make-lambda 
      ["customer" "region" "amount"]
      (core/make-application
        (core/make-variable "and")
        [(core/make-application
           (core/make-variable ">=")
           [(core/make-variable "amount")
            (core/make-application
              (core/make-variable "getMinAmount")
              [(core/make-variable "customer")
               (core/make-variable "region")])])
         (core/make-application
           (core/make-variable "<=")
           [(core/make-variable "amount")
            (core/make-application
              (core/make-variable "getMaxAmount")
              [(core/make-variable "customer")
               (core/make-variable "region")])])]))
    ;; Action: return amount or error
    (core/make-lambda
      ["customer" "region" "amount"]
      (core/make-if
        (core/make-application
          (core/make-variable "isWithinLimits")
          [(core/make-variable "customer")
           (core/make-variable "region")
           (core/make-variable "amount")])
        (core/make-variable "amount")  
        (core/make-quoted {:error "Amount outside limits"})))
    ;; Metadata
    {:description "Amount must be within min/max limits"}))

;; Utility functions
(defn get-min-amount [customer region]
  (let [segment (if (= (:segmentCode customer) "Wealth") wealth-segment basic-segment)
        region-code (core/get-region-code region)]
    (core/get-segment-min-topup-amount segment region-code)))

(defn get-max-amount [customer region]
  (let [segment (if (= (:segmentCode customer) "Wealth") wealth-segment basic-segment)
        region-code (core/get-region-code region)]
    (core/get-segment-max-topup-amount segment region-code)))

(defn is-within-limits [customer region amount]
  (let [min-amount (get-min-amount customer region)
        max-amount (get-max-amount customer region)]
    (and (>= amount min-amount) (<= amount max-amount))))

;; Create the global environment with all necessary bindings
(defn create-global-environment []
  {:frame
   {"UKRegion" uk-region
    "HKRegion" hk-region
    "BasicSegment" basic-segment
    "WealthSegment" wealth-segment
    "AmountLimitsRule" amount-limits-rule
    
    ;; Utility functions
    "getMinAmount" get-min-amount
    "getMaxAmount" get-max-amount
    "isWithinLimits" is-within-limits
    
    ;; Primitives
    "getRegionCode" core/get-region-code
    "getRegionCurrency" core/get-region-currency
    "getRegionCurrencySymbol" core/get-region-currency-symbol
    "getSegmentMinTopupAmount" core/get-segment-min-topup-amount
    "getSegmentMaxTopupAmount" core/get-segment-max-topup-amount
    "getSegmentInterestRate" core/get-segment-interest-rate
    "getSegmentBenefits" core/get-segment-benefits
    
    ;; Primitive operations
    "+" +
    "-" -
    "*" *
    "/" /
    "=" =
    "<" <
    ">" >
    "<=" <=
    ">=" >=
    "and" #(and %1 %2)
    "or" #(or %1 %2)
    "not" not}
   :parent nil})

;; Example of using the DSL - validate a top-up amount
(defn validate-topup-amount [region-code segment-code amount]
  (let [env (create-global-environment)
        customer {:id "C123456"
                 :name "John Smith"
                 :segmentCode segment-code}
        region (if (= region-code "UK") uk-region hk-region)]
    (core/evaluate
      (core/make-application
        (core/make-variable "isWithinLimits")
        [(core/make-quoted customer)
         (core/make-quoted region)
         (core/make-quoted amount)])
      env)))

;; Example of applying a business rule
(defn apply-amount-rule [region-code segment-code amount]
  (let [env (create-global-environment)
        customer {:id "C123456"
                 :name "John Smith"
                 :segmentCode segment-code}
        region (if (= region-code "UK") uk-region hk-region)]
    (core/apply-business-rule 
      amount-limits-rule
      (core/extend-environment 
        ["customer" "region" "amount"]
        [customer region amount]
        env))))