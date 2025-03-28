(ns finapp-dsl.core
  (:gen-class))

;; ===== Core Expression Types =====
;; In Clojure, we can represent expressions directly using maps

;; Expression constructors - much simpler in Clojure due to natural support for maps
(defn make-variable [name] {:type :variable :name name})
(defn make-application [operator operands] {:type :application :operator operator :operands operands})
(defn make-lambda [params body] {:type :lambda :params params :body body})
(defn make-if [predicate consequent alternative] {:type :if :predicate predicate :consequent consequent :alternative alternative})
(defn make-assignment [variable value] {:type :assignment :variable variable :value value})
(defn make-sequence [expressions] {:type :sequence :expressions expressions})
(defn make-quoted [data] {:type :quoted :data data})

;; Expression predicates
(defn variable? [exp] (and (map? exp) (= (:type exp) :variable)))
(defn application? [exp] (and (map? exp) (= (:type exp) :application)))
(defn lambda? [exp] (and (map? exp) (= (:type exp) :lambda)))
(defn if? [exp] (and (map? exp) (= (:type exp) :if)))
(defn assignment? [exp] (and (map? exp) (= (:type exp) :assignment)))
(defn sequence? [exp] (and (map? exp) (= (:type exp) :sequence)))
(defn quoted? [exp] (and (map? exp) (= (:type exp) :quoted)))
(defn self-evaluating? [exp] (not (map? exp)))

;; ===== Environment Model =====

;; Create a frame from variables and values
(defn make-frame [variables values]
  (into {} (map vector variables values)))

;; Extend an environment with a new frame
(defn extend-environment [variables values base-env]
  (if (not= (count variables) (count values))
    (throw (Exception. "Variables and values must have the same length"))
    {:frame (make-frame variables values) :parent base-env}))

;; Look up a variable value in an environment
(defn lookup-variable-value [variable env]
  (if (nil? env)
    (throw (Exception. (str "Unbound variable: " variable)))
    (let [frame (:frame env)]
      (if (contains? frame variable)
        (get frame variable)
        (lookup-variable-value variable (:parent env))))))

;; Define a variable in an environment
(defn define-variable! [variable value env]
  (swap! env assoc-in [:frame variable] value)
  value)

;; Set a variable's value in an environment (mutates environment)
(defn set-variable-value! [variable value env]
  (if (nil? env)
    (throw (Exception. (str "Unbound variable: " variable)))
    (if (contains? (:frame @env) variable)
      (do (swap! env assoc-in [:frame variable] value) value)
      (set-variable-value! variable value (:parent @env)))))

;; ===== Core Evaluator =====

;; Procedure representation
(defn make-procedure [parameters body environment]
  {:type :procedure
   :parameters parameters
   :body body
   :environment environment})

;; Forward declaration for mutual recursion
(declare evaluate apply-procedure evaluate-sequence)

;; Evaluate an expression in an environment
(defn evaluate [expression environment]
  (cond
    ;; Self-evaluating expressions
    (self-evaluating? expression)
    expression
    
    ;; Variables
    (variable? expression)
    (lookup-variable-value (:name expression) environment)
    
    ;; Quoted expressions
    (quoted? expression)
    (:data expression)
    
    ;; Assignments (requires mutable environment)
    (assignment? expression)
    (let [value (evaluate (:value expression) environment)]
      (set-variable-value! (:name (:variable expression)) value environment)
      value)
    
    ;; Conditionals
    (if? expression)
    (let [predicate (evaluate (:predicate expression) environment)]
      (if predicate
        (evaluate (:consequent expression) environment)
        (if (:alternative expression)
          (evaluate (:alternative expression) environment)
          nil)))
    
    ;; Sequences
    (sequence? expression)
    (evaluate-sequence (:expressions expression) environment)
    
    ;; Lambda expressions
    (lambda? expression)
    (make-procedure (:params expression) (:body expression) environment)
    
    ;; Applications
    (application? expression)
    (let [procedure (evaluate (:operator expression) environment)
          args (map #(evaluate % environment) (:operands expression))]
      (apply-procedure procedure args))
    
    :else
    (throw (Exception. (str "Unknown expression type: " (:type expression))))))

;; Helper for sequences
(defn evaluate-sequence [expressions environment]
  (if (empty? (rest expressions))
    (evaluate (first expressions) environment)
    (do
      (evaluate (first expressions) environment)
      (evaluate-sequence (rest expressions) environment))))

;; Apply a procedure to arguments
(defn apply-procedure [procedure args]
  (cond
    (fn? procedure)
    (apply procedure args)
    
    (and (map? procedure) (= (:type procedure) :procedure))
    (let [env (extend-environment (:parameters procedure) args (:environment procedure))]
      (evaluate (:body procedure) env))
    
    :else
    (throw (Exception. (str "Unknown procedure type: " procedure)))))

;; ===== Domain-Specific Constructors =====

;; Region constructors and accessors
(defn make-region [code properties]
  {:type :region
   :code code
   :properties properties})

(defn region? [exp] (and (map? exp) (= (:type exp) :region)))
(defn get-region-code [region] (:code region))
(defn get-region-currency [region] (get-in region [:properties :currency]))
(defn get-region-currency-symbol [region] (get-in region [:properties :currencySymbol]))
(defn get-region-date-format [region] (get-in region [:properties :dateFormat]))
(defn get-region-regulatory-body [region] (get-in region [:properties :regulatoryBody]))

;; Customer segment constructors and accessors
(defn make-customer-segment [code properties]
  {:type :customer_segment
   :code code
   :properties properties})

(defn customer-segment? [exp] (and (map? exp) (= (:type exp) :customer_segment)))
(defn get-segment-interest-rate [segment region-code] (get-in segment [:properties :interestRates region-code]))
(defn get-segment-min-topup-amount [segment region-code] (get-in segment [:properties :minTopupAmounts region-code]))
(defn get-segment-max-topup-amount [segment region-code] (get-in segment [:properties :maxTopupAmounts region-code]))
(defn get-segment-benefits [segment region-code] (or (get-in segment [:properties :benefits region-code]) []))

;; Time period constructors and accessors
(defn make-time-period [code properties]
  {:type :time_period
   :code code
   :properties properties})

(defn time-period? [exp] (and (map? exp) (= (:type exp) :time_period)))
(defn get-time-period-hours [period region-code] (get-in period [:properties region-code]))

;; Promotion constructors and accessors
(defn make-promotion [code properties]
  {:type :promotion
   :code code
   :properties properties})

(defn promotion? [exp] (and (map? exp) (= (:type exp) :promotion)))
(defn promotion-active? [promotion region-code current-date]
  (let [region-promo (get-in promotion [:properties :regions region-code])]
    (if (nil? region-promo)
      false
      (let [start-date (get region-promo :startDate)
            end-date (get region-promo :endDate)]
        (and
         (or (nil? start-date) (>= (.getTime current-date) (.getTime start-date)))
         (or (nil? end-date) (<= (.getTime current-date) (.getTime end-date))))))))

;; Business rule constructor and accessor
(defn make-business-rule [id condition action metadata]
  {:type :business_rule
   :id id
   :condition condition
   :action action
   :metadata (or metadata {})})

(defn business-rule? [exp] (and (map? exp) (= (:type exp) :business_rule)))

;; Apply a business rule in an environment
(defn apply-business-rule [rule environment]
  (let [condition-result (evaluate (:condition rule) environment)]
    (if condition-result
      (evaluate (:action rule) environment)
      nil)))

;; Entry point for command-line usage
(defn -main [& args]
  (println "FinApp Functional DSL in Clojure")) 