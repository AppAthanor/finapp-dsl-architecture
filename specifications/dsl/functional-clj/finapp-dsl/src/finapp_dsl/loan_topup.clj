(ns finapp-dsl.loan-topup
  (:require [finapp-dsl.core :as dsl]))

;; ===== Domain-Specific Constants =====

;; Region definitions
(def uk-region {:code "UK" :name "United Kingdom"
                :currency "GBP"
                :currencySymbol "£"
                :dateFormat "DD/MM/YYYY"
                :regulatoryBody "FCA"
                :coolingOffPeriod 14
                :language "en-GB"
                :translationsKey "uk_translations"})

(def hk-region {:code "HK" :name "Hong Kong"
                :currency "HKD"
                :currencySymbol "HK$"
                :dateFormat "DD/MM/YYYY"
                :regulatoryBody "HKMA"
                :coolingOffPeriod 10
                :language "zh-HK"
                :translationsKey "hk_translations"})

(def sg-region {:code "SG" :name "Singapore"
                :currency "SGD"
                :currencySymbol "S$"
                :dateFormat "DD/MM/YYYY"
                :regulatoryBody "MAS"
                :coolingOffPeriod 7
                :language "en-SG"
                :translationsKey "sg_translations"})

;; Amount rules by region
(defn get-max-amount-by-region [region]
  (case region
    "UK" 25000
    "HK" 150000
    "SG" 30000
    10000)) ; default

(defn get-min-amount-by-region [region]
  (case region
    "UK" 1000
    "HK" 5000
    "SG" 1500
    500)) ; default

;; Segment definitions
(def customer-segments
  {"Basic" {:multiplier 1.0
            :interestRates {"UK" 6.5, "HK" 6.9, "SG" 6.7}
            :minIncomes {"UK" 15000, "HK" 120000, "SG" 24000}
            :minAccountAge 6
            :benefits ["Standard processing"]}
   "Wealth" {:multiplier 1.5
             :interestRates {"UK" 5.4, "HK" 5.8, "SG" 5.5}
             :minIncomes {"UK" 75000, "HK" 600000, "SG" 120000}
             :minAccountAge 3
             :benefits ["Preferential rates", "Dedicated relationship manager", "Fee waivers"]}
   "Private" {:multiplier 2.0
              :interestRates {"UK" 4.9, "HK" 5.2, "SG" 5.0}
              :minIncomes {"UK" 150000, "HK" 1200000, "SG" 240000}
              :minAccountAge 1
              :benefits ["Bespoke rates", "Dedicated private banker", "All fees waived", "Expedited processing"]}})

(defn get-amount-multiplier-by-segment [segment]
  (get-in customer-segments [segment :multiplier] 1.0))

(defn get-interest-rate-by-segment-and-region [segment region]
  (get-in customer-segments [segment :interestRates region] 7.5)) ; default rate

(defn get-min-income-by-segment-and-region [segment region]
  (get-in customer-segments [segment :minIncomes region] 0))

(defn get-min-account-age-by-segment [segment]
  (get-in customer-segments [segment :minAccountAge] 12))

(defn get-benefits-by-segment [segment]
  (get-in customer-segments [segment :benefits] ["Standard processing"]))

;; Business rules

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

(defn apply-eligibility-rule
  "Check if customer meets eligibility criteria for loan topup"
  [customer-data region]
  (let [segment (:segment customer-data)
        age (:age customer-data 0)
        annual-income (:annualIncome customer-data 0)
        account-age-months (:accountAgeMonths customer-data 0)
        min-income (get-min-income-by-segment-and-region segment region)
        min-account-age (get-min-account-age-by-segment segment)]
    (cond
      (< age 21) {:eligible false, :reason "Customer must be at least 21 years old"}
      (< annual-income min-income) {:eligible false, :reason "Customer income below minimum requirement"}
      (< account-age-months min-account-age) {:eligible false, :reason "Account not established long enough"}
      :else {:eligible true, :benefits (get-benefits-by-segment segment)})))

(defn calculate-interest-rate
  "Calculate interest rate with adjustments based on amount"
  [segment region amount]
  (let [base-rate (get-interest-rate-by-segment-and-region segment region)]
    (cond
      (> amount 50000) (- base-rate 0.25)  ; Discount for large amounts
      (< amount 5000) (+ base-rate 0.5)    ; Premium for small amounts
      :else base-rate)))

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
        apply-amount-rule-fn apply-amount-rule
        apply-eligibility-rule-fn apply-eligibility-rule
        calculate-interest-rate-fn calculate-interest-rate]
    
    ;; Extend environment with functions
    (-> env
        (dsl/define-variable! (dsl/make-variable "equal?") equal-fn)
        (dsl/define-variable! (dsl/make-variable "add") add-fn)
        (dsl/define-variable! (dsl/make-variable "subtract") subtract-fn)
        (dsl/define-variable! (dsl/make-variable "multiply") multiply-fn)
        (dsl/define-variable! (dsl/make-variable "apply-amount-rule") apply-amount-rule-fn)
        (dsl/define-variable! (dsl/make-variable "apply-eligibility-rule") apply-eligibility-rule-fn)
        (dsl/define-variable! (dsl/make-variable "calculate-interest-rate") calculate-interest-rate-fn))))

;; UI journey screens
(def journey-screens
  {:initial-offer {:type "screen" 
                  :name "InitialOfferScreen"
                  :fields ["customerSegment", "existingLoanAmount", "maxTopupAmount", "interestRate"]}
   :amount-selection {:type "screen" 
                     :name "AmountSelectionScreen"
                     :fields ["suggestedAmount", "minAmount", "maxAmount", "selectedAmount"]}
   :terms-review {:type "screen" 
                 :name "TermsReviewScreen"
                 :fields ["selectedAmount", "term", "monthlyPayment", "totalRepayable", "apr"]}
   :confirmation {:type "screen" 
                 :name "ConfirmationScreen"
                 :fields ["selectedAmount", "term", "monthlyPayment", "acceptTerms"]}
   :success {:type "screen" 
            :name "SuccessScreen"
            :fields ["selectedAmount", "accountNumber", "estimatedFundsAvailableDate"]}})

;; Journey executor function
(defn execute-loan-topup-journey [customer-id region-code initial-amount]
  (let [env (setup-loan-topup-environment)
        calc-expr (build-offer-calculation env)
        
        ;; Mock customer data - in production this would come from a database
        customer-data (case customer-id
                       "C999999" {:id customer-id
                                 :segment "Private" 
                                 :age 35
                                 :annualIncome 200000
                                 :accountAgeMonths 24}
                       "C888888" {:id customer-id
                                 :segment "Wealth"
                                 :age 32
                                 :annualIncome 80000
                                 :accountAgeMonths 12}
                       {:id customer-id
                        :segment "Basic"
                        :age 30
                        :annualIncome 30000
                        :accountAgeMonths 8})
        
        ;; Set up environment with customer data
        env-with-values (-> env
                           (dsl/define-variable! (dsl/make-variable "region") region-code)
                           (dsl/define-variable! (dsl/make-variable "segment") (:segment customer-data))
                           (dsl/define-variable! (dsl/make-variable "baseAmount") initial-amount))
        
        ;; Check eligibility
        eligibility-result (apply-eligibility-rule customer-data region-code)
        
        ;; If eligible, calculate offer details
        journey-result (if (:eligible eligibility-result)
                        (let [topup-amount (dsl/evaluate calc-expr env-with-values)
                              interest-rate (calculate-interest-rate 
                                            (:segment customer-data) 
                                            region-code 
                                            topup-amount)
                              region-currency (case region-code
                                              "UK" "GBP"
                                              "HK" "HKD" 
                                              "SG" "SGD"
                                              "USD")]
                          {:success true
                           :journeyId (str "TOPUP-" (System/currentTimeMillis))
                           :customerId customer-id
                           :eligibility eligibility-result
                           :offer {:amount topup-amount
                                  :currency region-currency
                                  :interestRate interest-rate
                                  :term 36  ; Default term in months
                                  :monthlyPayment (/ (* topup-amount (+ 1 (/ interest-rate 100))) 36)}})
                        {:success false
                         :customerId customer-id
                         :eligibility eligibility-result})]
    
    journey-result)) 