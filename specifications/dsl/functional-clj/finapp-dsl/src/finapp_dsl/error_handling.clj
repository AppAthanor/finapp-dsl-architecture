(ns finapp-dsl.error-handling
  (:require [finapp-dsl.core :as dsl]
            [clojure.string :as str]))

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
  {:InsufficientFunds {:category :ValidationError
                       :code "PAYMENT_001"
                       :message "Insufficient funds available"}
   :PaymentLimitExceeded {:category :ValidationError
                          :code "PAYMENT_002"
                          :message "This payment exceeds your daily limit"}
   :RecipientNotFound {:category :ValidationError
                       :code "PAYMENT_003"
                       :message "Recipient account not found"}
   :InvalidAmount {:category :ValidationError
                   :code "PAYMENT_004"
                   :message "Please enter a valid amount"}})

;; Journey Error Hub Ownership
(def journey-error-ownership
  {:PaymentJourney {:owns #{:InsufficientFunds :PaymentLimitExceeded 
                            :RecipientNotFound :InvalidAmount}
                    :delegates #{:NetworkError :AuthenticationError :AuthorisationError}}})

;; Journey Error Handlers
(def journey-error-handlers
  {:PaymentJourney
   {:InsufficientFunds {:display :ErrorBanner
                        :location "PaymentEntryScreen"
                        :properties {:message (fn [error] (:message error))
                                     :actionText "Check balance"
                                     :action #(navigate :AccountDetailsScreen)}
                        :recovery {:allowRetry true
                                   :alternativeFlow #(navigate :AccountListScreen)}}
    :PaymentLimitExceeded {:display :ModalDialog
                           :location "PaymentEntryScreen"
                           :properties {:title "Daily limit reached"
                                        :message (fn [error] (:message error))
                                        :primaryAction "View limits"
                                        :secondaryAction "Go back"}
                           :recovery {:allowRetry false
                                      :alternativeFlow #(navigate :LimitsScreen)}}
    :RecipientNotFound {:display :ErrorBanner
                        :location "PaymentEntryScreen"
                        :properties {:message (fn [error] (:message error))
                                     :actionText "Add recipient"
                                     :action #(navigate :AddRecipientScreen)}
                        :recovery {:allowRetry true}}
    :InvalidAmount {:display :ErrorBanner
                    :location "PaymentEntryScreen"
                    :properties {:message (fn [error] (:message error))}
                    :recovery {:allowRetry true
                               :focusField "amountField"}}}})

;; Platform Default Error Handlers
(def platform-default-handlers
  {:NetworkError {:display :RetryDialog
                  :properties {:title "Connection issue"
                               :message "We're having trouble connecting. Please try again."
                               :retryAction "Retry"
                               :cancelAction "Cancel"}
                  :recovery {:retryStrategy #(exponential-backoff % 3 1000)
                             :cancelFlow #(navigate-back)}}
   :AuthenticationError {:display :SecurityDialog
                         :properties {:message "Please log in again to continue"
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
  {:PaymentService
   {:createPayment
    {:400 {"INSUFFICIENT_BALANCE" :InsufficientFunds
           "DAILY_LIMIT_EXCEEDED" :PaymentLimitExceeded
           "RECIPIENT_NOT_FOUND" :RecipientNotFound
           "INVALID_AMOUNT" :InvalidAmount}
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

;; ===== Usage Example =====

(defn process-payment
  "Example function that processes a payment and handles any errors."
  [journey-id payment-data]
  (try
    ;; Simulate an API call and error response
    (let [amount (:amount payment-data 0)
          status-code (cond
                        (< amount 10) 400
                        (> amount 10000) 400
                        :else 200)
          error-message (cond
                          (< amount 10) "INVALID_AMOUNT"
                          (> amount 10000) "DAILY_LIMIT_EXCEEDED"
                          :else nil)]
      
      (if (= status-code 200)
        (do
          (println "Payment processed successfully!")
          {:success true
           :payment-id (str "PAY-" (int (rand 1000000)))
           :timestamp (java.util.Date.)})
        
        (do
          (println "Payment failed with status" status-code "and error" error-message)
          (handle-api-error journey-id :PaymentService :createPayment 
                           status-code error-message)
          {:success false})))
    
    (catch Exception e
      (println "Unexpected error:" (.getMessage e))
      (handle-api-error journey-id :PaymentService :createPayment 
                       500 "INTERNAL_SERVER_ERROR")
      {:success false})))

(defn -main
  "Demo of the error handling system."
  [& args]
  (println "\n=== FinApp DSL Error Handling Demo ===\n")
  
  (println "Scenario 1: Invalid Amount (too small)")
  (process-payment :PaymentJourney {:amount 5
                                   :from-account "12345678"
                                   :to-account "87654321"
                                   :reference "Test payment"})
  
  (println "\nScenario 2: Payment Limit Exceeded")
  (process-payment :PaymentJourney {:amount 50000
                                   :from-account "12345678"
                                   :to-account "87654321"
                                   :reference "Large payment"})
  
  (println "\nScenario 3: Successful Payment")
  (process-payment :PaymentJourney {:amount 100
                                   :from-account "12345678"
                                   :to-account "87654321"
                                   :reference "Normal payment"})
  
  (println "\n=== Demo Complete ==="))

;; For REPL testing
(comment
  (-main)
  
  ;; Test individual functions
  (map-api-error :PaymentService :createPayment 400 "INSUFFICIENT_BALANCE")
  (get-error-details :InsufficientFunds)
  (get-error-owner :PaymentJourney :NetworkError)
  (get-error-handler :PaymentJourney :InsufficientFunds)
  
  ;; Test error handling for specific scenarios
  (handle-api-error :PaymentJourney :PaymentService :createPayment 400 "INSUFFICIENT_BALANCE")
  (handle-api-error :PaymentJourney :PaymentService :createPayment 401 nil)
  (handle-api-error :PaymentJourney :PaymentService :createPayment 503 nil)
  
  ;; Test payment processing with different amounts
  (process-payment :PaymentJourney {:amount 5}) ;; Invalid amount
  (process-payment :PaymentJourney {:amount 50000}) ;; Limit exceeded
  (process-payment :PaymentJourney {:amount 100}) ;; Success
) 