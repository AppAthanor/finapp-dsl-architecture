(ns finapp-dsl.loan-topup
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

(def sg-region
  (core/make-region "SG"
    {:currency "SGD"
     :currencySymbol "S$"
     :dateFormat "DD/MM/YYYY"
     :regulatoryBody "MAS"
     :coolingOffPeriod 7
     :language "en-SG"
     :translationsKey "sg_translations"}))

;; Customer segment definitions
(def basic-segment
  (core/make-customer-segment "Basic"
    {:description "Regular retail banking customers"
     :interestRates {"UK" 6.5, "HK" 6.9, "SG" 6.7}
     :minTopupAmounts {"UK" 1000, "HK" 10000, "SG" 1500}
     :maxTopupAmounts {"UK" 25000, "HK" 200000, "SG" 30000}
     :benefits {"UK" ["Standard processing"], 
                "HK" ["Standard processing"],
                "SG" ["Standard processing"]}
     :eligibilityCriteria {:minAge 21
                          :minIncome {"UK" 15000, "HK" 120000, "SG" 24000}
                          :minAccountAge 6}}))

(def wealth-segment
  (core/make-customer-segment "Wealth"
    {:description "Premier or priority banking customers with higher value accounts"
     :interestRates {"UK" 5.4, "HK" 5.8, "SG" 5.5}
     :minTopupAmounts {"UK" 5000, "HK" 50000, "SG" 7500}
     :maxTopupAmounts {"UK" 100000, "HK" 800000, "SG" 150000}
     :benefits {"UK" ["Preferential rates" "Dedicated relationship manager" "Fee waivers"]
               "HK" ["Priority processing" "Jade status points" "Fee waivers"]
               "SG" ["Priority processing" "Fee waivers" "Reward points"]}
     :eligibilityCriteria {:minAge 21
                          :minIncome {"UK" 75000, "HK" 600000, "SG" 120000}
                          :minAccountAge 3}}))

(def private-segment
  (core/make-customer-segment "Private"
    {:description "Private banking customers with significant investments"
     :interestRates {"UK" 4.9, "HK" 5.2, "SG" 5.0}
     :minTopupAmounts {"UK" 10000, "HK" 100000, "SG" 15000}
     :maxTopupAmounts {"UK" 250000, "HK" 2000000, "SG" 300000}
     :benefits {"UK" ["Bespoke rates" "Dedicated private banker" "All fees waived" "Expedited processing"]
               "HK" ["Bespoke rates" "Dedicated private banker" "All fees waived" "Expedited processing"]
               "SG" ["Bespoke rates" "Dedicated private banker" "All fees waived" "Expedited processing"]}
     :eligibilityCriteria {:minAge 21
                          :minIncome {"UK" 150000, "HK" 1200000, "SG" 240000}
                          :minAccountAge 1}}))

;; Business rules
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
    {:description "Amount must be within min/max limits"
     :regulatory true
     :version "1.0"
     :lastUpdated "2023-10-15"}))

(def eligibility-rule
  (core/make-business-rule
    "BR002"
    ;; Condition: is customer eligible for topup?
    (core/make-lambda
      ["customer" "region"]
      (core/make-application
        (core/make-variable "and")
        [(core/make-application
           (core/make-variable ">=")
           [(core/make-application
              (core/make-variable "getCustomerAge")
              [(core/make-variable "customer")])
            (core/make-application
              (core/make-variable "getMinAge")
              [(core/make-variable "customer")
               (core/make-variable "region")])])
         (core/make-application
           (core/make-variable ">=")
           [(core/make-application
              (core/make-variable "getCustomerIncome")
              [(core/make-variable "customer")])
            (core/make-application
              (core/make-variable "getMinIncome")
              [(core/make-variable "customer")
               (core/make-variable "region")])])
         (core/make-application
           (core/make-variable ">=")
           [(core/make-application
              (core/make-variable "getCustomerAccountAge")
              [(core/make-variable "customer")])
            (core/make-application
              (core/make-variable "getMinAccountAge")
              [(core/make-variable "customer")
               (core/make-variable "region")])])]))
    ;; Action: return eligibility result
    (core/make-lambda
      ["customer" "region"]
      (core/make-if
        (core/make-application
          (core/make-variable "isEligible")
          [(core/make-variable "customer")
           (core/make-variable "region")])
        (core/make-quoted {:eligible true})
        (core/make-quoted {:eligible false, :reason "Customer does not meet eligibility criteria"})))
    ;; Metadata
    {:description "Check if customer meets eligibility criteria for loan topup"
     :regulatory true
     :version "1.1"
     :lastUpdated "2023-11-22"}))

;; Utility functions
(defn get-min-amount [customer region]
  (let [segment (cond
                  (= (:segmentCode customer) "Private") private-segment
                  (= (:segmentCode customer) "Wealth") wealth-segment
                  :else basic-segment)
        region-code (core/get-region-code region)]
    (core/get-segment-min-topup-amount segment region-code)))

(defn get-max-amount [customer region]
  (let [segment (cond
                  (= (:segmentCode customer) "Private") private-segment
                  (= (:segmentCode customer) "Wealth") wealth-segment
                  :else basic-segment)
        region-code (core/get-region-code region)]
    (core/get-segment-max-topup-amount segment region-code)))

(defn is-within-limits [customer region amount]
  (let [min-amount (get-min-amount customer region)
        max-amount (get-max-amount customer region)]
    (and (>= amount min-amount) (<= amount max-amount))))

(defn get-customer-age [customer]
  (or (:age customer) 0))

(defn get-min-age [customer region]
  (let [segment (cond
                  (= (:segmentCode customer) "Private") private-segment
                  (= (:segmentCode customer) "Wealth") wealth-segment
                  :else basic-segment)]
    (get-in segment [:eligibilityCriteria :minAge])))

(defn get-customer-income [customer]
  (or (:annualIncome customer) 0))

(defn get-min-income [customer region]
  (let [segment (cond
                  (= (:segmentCode customer) "Private") private-segment
                  (= (:segmentCode customer) "Wealth") wealth-segment
                  :else basic-segment)
        region-code (core/get-region-code region)]
    (get-in segment [:eligibilityCriteria :minIncome region-code])))

(defn get-customer-account-age [customer]
  (or (:accountAgeMonths customer) 0))

(defn get-min-account-age [customer region]
  (let [segment (cond
                  (= (:segmentCode customer) "Private") private-segment
                  (= (:segmentCode customer) "Wealth") wealth-segment
                  :else basic-segment)]
    (get-in segment [:eligibilityCriteria :minAccountAge])))

(defn is-eligible [customer region]
  (and (>= (get-customer-age customer) (get-min-age customer region))
       (>= (get-customer-income customer) (get-min-income customer region))
       (>= (get-customer-account-age customer) (get-min-account-age customer region))))

;; Create the global environment with all necessary bindings
(defn create-global-environment []
  {:frame
   {"UKRegion" uk-region
    "HKRegion" hk-region
    "SGRegion" sg-region
    "BasicSegment" basic-segment
    "WealthSegment" wealth-segment
    "PrivateSegment" private-segment
    "AmountLimitsRule" amount-limits-rule
    "EligibilityRule" eligibility-rule
    
    ;; Utility functions
    "getMinAmount" get-min-amount
    "getMaxAmount" get-max-amount
    "isWithinLimits" is-within-limits
    "getCustomerAge" get-customer-age
    "getMinAge" get-min-age
    "getCustomerIncome" get-customer-income
    "getMinIncome" get-min-income
    "getCustomerAccountAge" get-customer-account-age
    "getMinAccountAge" get-min-account-age
    "isEligible" is-eligible
    
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

;; API Functions
(defn validate-topup-amount [region-code segment-code amount]
  (let [env (create-global-environment)
        customer {:id "C123456"
                 :name "John Smith"
                 :segmentCode segment-code}
        region (cond
                (= region-code "UK") uk-region
                (= region-code "HK") hk-region
                (= region-code "SG") sg-region
                :else uk-region)]
    (core/evaluate
      (core/make-application
        (core/make-variable "isWithinLimits")
        [(core/make-quoted customer)
         (core/make-quoted region)
         (core/make-quoted amount)])
      env)))

(defn apply-amount-rule [region-code segment-code amount]
  (let [env (create-global-environment)
        customer {:id "C123456"
                 :name "John Smith"
                 :segmentCode segment-code}
        region (cond
                (= region-code "UK") uk-region
                (= region-code "HK") hk-region
                (= region-code "SG") sg-region
                :else uk-region)]
    (core/apply-business-rule 
      amount-limits-rule
      (core/extend-environment 
        ["customer" "region" "amount"]
        [customer region amount]
        env))))

(defn check-customer-eligibility [customer-id region-code]
  (let [env (create-global-environment)
        ;; In a real implementation, this would fetch customer data from a database
        customer {:id customer-id
                 :name "Example Customer"
                 :segmentCode (if (= customer-id "C999999") "Private" 
                                (if (= customer-id "C888888") "Wealth" "Basic"))
                 :age 35
                 :annualIncome (if (= customer-id "C999999") 200000
                                 (if (= customer-id "C888888") 80000 30000))
                 :accountAgeMonths (if (= customer-id "C999999") 24
                                     (if (= customer-id "C888888") 12 8))}
        region (cond
                (= region-code "UK") uk-region
                (= region-code "HK") hk-region
                (= region-code "SG") sg-region
                :else uk-region)]
    (core/apply-business-rule
      eligibility-rule
      (core/extend-environment
        ["customer" "region"]
        [customer region]
        env))))

(defn calculate-interest-rate [customer-id region-code amount]
  (let [env (create-global-environment)
        ;; In a real implementation, this would fetch customer data from a database
        customer {:id customer-id
                 :segmentCode (if (= customer-id "C999999") "Private" 
                                (if (= customer-id "C888888") "Wealth" "Basic"))}
        region (cond
                (= region-code "UK") uk-region
                (= region-code "HK") hk-region
                (= region-code "SG") sg-region
                :else uk-region)
        segment (cond
                  (= (:segmentCode customer) "Private") private-segment
                  (= (:segmentCode customer) "Wealth") wealth-segment
                  :else basic-segment)
        region-code (core/get-region-code region)
        base-rate (core/get-segment-interest-rate segment region-code)]
    ;; Apply business logic for custom rate determination
    (cond
      (> amount 50000) (- base-rate 0.25)  ; Discount for large amounts
      (< amount 5000) (+ base-rate 0.5)    ; Premium for small amounts
      :else base-rate)))