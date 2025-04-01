(ns finapp-dsl.loan-topup
  (:require [finapp-dsl.core :as dsl]
            [clojure.string :as str]))

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

;; ===== Error Handling Framework Implementation =====

;; Error Category Registry
(def error-categories
  {:NetworkError {:codes #{408 502 503 504}
                  :severity :high
                  :retryable true}
   :ValidationError {:codes #{400 422}
                     :severity :medium
                     :retryable false}
   :AuthenticationError {:codes #{401}
                         :severity :high
                         :retryable false}
   :AuthorisationError {:codes #{403}
                        :severity :high
                        :retryable false}})

;; Error Registry
(def error-registry
  {:IneligibleLoan {:category :ValidationError
                   :code "LOAN_001"
                   :message "Your loan is not eligible for a top-up at this time"}
   :InsufficientIncome {:category :ValidationError
                        :code "LOAN_002"
                        :message "Your income does not meet the minimum requirement for this top-up"}
   :AmountTooLow {:category :ValidationError
                  :code "LOAN_003"
                  :message "The requested amount is below the minimum allowed"}
   :AmountTooHigh {:category :ValidationError
                   :code "LOAN_004"
                   :message "The requested amount exceeds your maximum eligible amount"}
   :LoanOfferExpired {:category :ValidationError
                     :code "LOAN_005"
                     :message "Your loan top-up offer has expired"}
   :AccountTooNew {:category :ValidationError
                  :code "LOAN_006"
                  :message "Your account is too new for a loan top-up"}})

;; Journey Error Hub Ownership
(def journey-error-ownership
  {:LoanTopupJourney 
   {:owns #{:IneligibleLoan :InsufficientIncome :AmountTooLow :AmountTooHigh :LoanOfferExpired :AccountTooNew}
    :delegates #{:NetworkError :AuthenticationError :AuthorisationError}}})

;; Journey Error Handlers
(def journey-error-handlers
  {:LoanTopupJourney
   {:IneligibleLoan {:display :ErrorBanner
                    :location "LoansOverview"
                    :properties {:message (fn [error] (:message error))
                                 :actionText "View loan details"
                                 :action #(navigate :LoanDetailsScreen)}
                    :recovery {:allowRetry false}}
    
    :InsufficientIncome {:display :ModalDialog
                         :location "TopupOfferScreen"
                         :properties {:title "Income requirement not met"
                                      :message "Based on our records, your income doesn't meet the minimum requirement for this loan top-up. Please update your income details or try again later."
                                      :primaryAction "Update income details"
                                      :secondaryAction "Go back"}
                         :recovery {:allowRetry false
                                    :alternativeFlow #(navigate :IncomeUpdateScreen)}}
    
    :AmountTooLow {:display :ErrorBanner
                   :location "AmountSelectionScreen"
                   :properties {:message (fn [error] (:message error))}
                   :recovery {:allowRetry true
                              :focusField "amountField"}}
    
    :AmountTooHigh {:display :ErrorBanner
                   :location "AmountSelectionScreen"
                   :properties {:message (fn [error] (:message error))}
                   :recovery {:allowRetry true
                              :focusField "amountField"}}
    
    :LoanOfferExpired {:display :ModalDialog
                      :location "ConfirmationScreen"
                      :properties {:title "Offer expired"
                                   :message "Your loan top-up offer has expired. Please start a new application."
                                   :primaryAction "Start new application"
                                   :secondaryAction "Go to loans overview"}
                      :recovery {:primaryFlow #(navigate :LoansOverview)
                                 :secondaryFlow #(navigate :LoansOverview)}}
    
    :AccountTooNew {:display :ErrorBanner
                   :location "TopupOfferScreen"
                   :properties {:message (fn [error] (:message error))}
                   :recovery {:allowRetry false}}}})

;; Platform Default Error Handlers
(def platform-default-handlers
  {:NetworkError {:display :RetryDialog
                 :properties {:title "Connection issue"
                              :message "We're having trouble connecting to our services. Please try again."
                              :retryAction "Retry"
                              :cancelAction "Cancel"}
                 :recovery {:retryStrategy #(exponential-backoff % 3 1000)
                            :cancelFlow #(navigate-back)}}
   
   :AuthenticationError {:display :SecurityDialog
                        :properties {:message "Your session has expired. Please log in again to continue."
                                     :action "Log in"}
                        :recovery {:preserveState true
                                   :flow #(navigate :LoginScreen)}}
   
   :AuthorisationError {:display :ModalDialog
                       :properties {:title "Access denied"
                                    :message "You don't have permission to perform this action."
                                    :primaryAction "Go to home"
                                    :secondaryAction "Go back"}
                       :recovery {:primaryFlow #(navigate :HomeScreen)
                                  :secondaryFlow #(navigate-back)}}})

;; API Error Mappings
(def api-error-mappings
  {:LoanTopupService
   {:checkEligibility
    {:400 {"LOAN_NOT_ELIGIBLE" :IneligibleLoan
           "INCOME_BELOW_THRESHOLD" :InsufficientIncome
           "ACCOUNT_TOO_NEW" :AccountTooNew}
     :401 :AuthenticationError
     :403 :AuthorisationError
     :5xx :NetworkError}
    
    :createTopup
    {:400 {"AMOUNT_TOO_LOW" :AmountTooLow
           "AMOUNT_TOO_HIGH" :AmountTooHigh
           "LOAN_OFFER_EXPIRED" :LoanOfferExpired}
     :401 :AuthenticationError
     :403 :AuthorisationError
     :5xx :NetworkError}}})

;; ===== Error Handling Core Functions =====

(defn map-api-error
  "Maps an API error response to a domain error based on HTTP status code and error message."
  [service operation status-code error-message]
  (let [status-mappings (get-in api-error-mappings [service operation])
        ;; Handle 5xx range specially
        status-key (if (and (>= status-code 500) (< status-code 600))
                     :5xx
                     status-code)
        error-mapping (get status-mappings status-key)]
    (cond
      ;; If error mapping is a map, look up by error message
      (map? error-mapping) (get error-mapping error-message)
      ;; If error mapping is a direct error type, use it
      (keyword? error-mapping) error-mapping
      ;; Otherwise, default to generic error
      :else :UnknownError)))

(defn get-error-details
  "Retrieves full error details for a given error type."
  [error-type]
  (let [error-info (get error-registry error-type)
        category-info (get error-categories (:category error-info {}))]
    (merge category-info error-info {:type error-type})))

(defn get-error-owner
  "Determines which component owns an error - journey or platform."
  [journey-id error-type]
  (let [ownership (get journey-error-ownership journey-id)
        owns (get ownership :owns #{})
        delegates (get ownership :delegates #{})]
    (cond
      (contains? owns error-type) :journey
      (contains? delegates error-type) :platform
      :else :platform))) ; Default to platform

(defn get-error-handler
  "Gets the appropriate error handler for an error."
  [journey-id error-type]
  (let [owner (get-error-owner journey-id error-type)]
    (case owner
      :journey (get-in journey-error-handlers [journey-id error-type])
      :platform (get platform-default-handlers error-type)
      nil)))

(defn apply-handler-properties
  "Applies dynamic property functions in an error handler."
  [handler error]
  (update handler :properties
          (fn [props]
            (reduce-kv
             (fn [m k v]
               (assoc m k (if (fn? v) (v error) v)))
             {}
             props))))

;; ===== Error Handling Execution Functions =====

(defn navigate
  "Navigates to a specified screen."
  [screen-id]
  (println "Navigating to screen:" screen-id))

(defn navigate-back
  "Navigates back to the previous screen."
  []
  (println "Navigating back to previous screen"))

(defn exponential-backoff
  "Implements exponential backoff for retries."
  [retry-fn max-attempts base-delay-ms]
  (fn [current-attempt]
    (if (< current-attempt max-attempts)
      (let [delay-ms (* base-delay-ms (Math/pow 2 current-attempt))
            jitter (+ 0.8 (rand 0.4))] ; Add 20% jitter
        (println "Retry attempt" (inc current-attempt) "in" (* delay-ms jitter) "ms")
        ;; In a real implementation, we would use a proper async delay
        ;; For this example, we just print the message
        (Thread/sleep (long (* delay-ms jitter)))
        (retry-fn))
      (println "Maximum retry attempts reached"))))

(defn display-error-component
  "Displays an error component based on the handler configuration."
  [component-type properties]
  (println "Displaying" component-type "with properties:")
  (doseq [[k v] properties]
    (when (not (fn? v)) ; Skip function properties for display
      (println " -" k ":" v))))

(defn handle-api-error
  "Main function to handle an API error response."
  [journey-id service operation status-code error-message]
  (let [error-type (map-api-error service operation status-code error-message)
        error-details (get-error-details error-type)
        handler (get-error-handler journey-id error-type)]
    
    (if handler
      (let [handler-with-props (apply-handler-properties handler error-details)
            display-type (:display handler-with-props)
            properties (:properties handler-with-props)
            recovery (:recovery handler-with-props)]
        
        (println "Handling error:" (:message error-details))
        (display-error-component display-type properties)
        
        ;; Apply recovery strategy if needed
        (when recovery
          (if (:allowRetry recovery)
            (println "Action: Allow retry with field focus on" (:focusField recovery))
            (when-let [alt-flow (:alternativeFlow recovery)]
              (alt-flow))))
        
        true) ; Error was handled
      
      (do
        (println "No handler found for error:" error-type)
        false)))) ; Error was not handled

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

;; Enhanced journey executor function with error handling
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
                       "C777777" {:id customer-id
                                 :segment "Basic"
                                 :age 25
                                 :annualIncome 10000  ;; Too low for min requirement
                                 :accountAgeMonths 12}
                       "C666666" {:id customer-id
                                 :segment "Basic"
                                 :age 22
                                 :annualIncome 20000
                                 :accountAgeMonths 2} ;; Too new
                       ;; Default
                       {:id customer-id
                        :segment "Basic"
                        :age 30
                        :annualIncome 30000
                        :accountAgeMonths 12})
        
        ;; Check eligibility first
        eligibility-result (apply-eligibility-rule customer-data region-code)]
    
    (if (:eligible eligibility-result)
      ;; Proceed with normal flow
      (try
        ;; Calculate the offer
        (let [segment (:segment customer-data)
              base-amount (or initial-amount 5000)
              topup-amount (apply-amount-rule region-code segment base-amount)]
          
          ;; Validate amount
          (cond
            (< topup-amount (get-min-amount-by-region region-code))
            (do
              (handle-api-error :LoanTopupJourney :LoanTopupService :createTopup 
                              400 "AMOUNT_TOO_LOW")
              {:success false})
            
            (> topup-amount (get-max-amount-by-region region-code))
            (do
              (handle-api-error :LoanTopupJourney :LoanTopupService :createTopup 
                              400 "AMOUNT_TOO_HIGH")
              {:success false})
            
            :else
            (do
              (println "Loan top-up processed successfully!")
              {:success true
               :customer-id customer-id
               :topup-amount topup-amount
               :interest-rate (calculate-interest-rate segment region-code topup-amount)
               :benefits (:benefits eligibility-result)})))
        
        (catch Exception e
          (println "Unexpected error:" (.getMessage e))
          (handle-api-error :LoanTopupJourney :LoanTopupService :createTopup 
                          500 "INTERNAL_SERVER_ERROR")
          {:success false}))
      
      ;; Not eligible
      (do
        (let [error-reason (:reason eligibility-result)
              error-code (cond
                           (str/includes? error-reason "income") "INCOME_BELOW_THRESHOLD"
                           (str/includes? error-reason "account") "ACCOUNT_TOO_NEW"
                           :else "LOAN_NOT_ELIGIBLE")]
          (handle-api-error :LoanTopupJourney :LoanTopupService :checkEligibility 
                          400 error-code))
        {:success false
         :reason (:reason eligibility-result)}))))

(defn -main
  "Demo of the loan top-up journey with error handling."
  [& args]
  (println "\n=== FinApp DSL Loan Top-up Demo ===\n")
  
  (println "Scenario 1: Successful Loan Top-up (Private Customer)")
  (execute-loan-topup-journey "C999999" "UK" 10000)
  
  (println "\nScenario 2: Insufficient Income")
  (execute-loan-topup-journey "C777777" "UK" 5000)
  
  (println "\nScenario 3: Account Too New")
  (execute-loan-topup-journey "C666666" "UK" 5000)
  
  (println "\nScenario 4: Amount Too Low")
  (execute-loan-topup-journey "C999999" "UK" 100)
  
  (println "\nScenario 5: Amount Too High")
  (execute-loan-topup-journey "C999999" "UK" 100000)
  
  (println "\n=== Demo Complete ==="))

;; For REPL testing
(comment
  (-main)
  
  ;; Test individual functions
  (map-api-error :LoanTopupService :checkEligibility 400 "INCOME_BELOW_THRESHOLD")
  (get-error-details :InsufficientIncome)
  (get-error-owner :LoanTopupJourney :InsufficientIncome)
  (get-error-handler :LoanTopupJourney :InsufficientIncome)
  
  ;; Test error handling for specific scenarios
  (handle-api-error :LoanTopupJourney :LoanTopupService :checkEligibility 400 "INCOME_BELOW_THRESHOLD")
  (handle-api-error :LoanTopupJourney :LoanTopupService :checkEligibility 401 nil)
  (handle-api-error :LoanTopupJourney :LoanTopupService :createTopup 503 nil)
  
  ;; Test journey with different scenarios
  (execute-loan-topup-journey "C999999" "UK" 10000) ;; Success
  (execute-loan-topup-journey "C777777" "UK" 5000)  ;; Insufficient income
  (execute-loan-topup-journey "C666666" "UK" 5000)  ;; Account too new
  (execute-loan-topup-journey "C999999" "UK" 100)   ;; Amount too low
  (execute-loan-topup-journey "C999999" "UK" 100000) ;; Amount too high
) 