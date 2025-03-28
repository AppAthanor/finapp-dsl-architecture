(ns finapp-dsl.core
  (:gen-class))

;; Expression types
(defn make-variable [name]
  {:type :variable :name name})

(defn make-application [operator operands]
  {:type :application :operator operator :operands operands})

(defn make-lambda [parameters body]
  {:type :lambda :parameters parameters :body body})

(defn make-if [predicate consequent alternative]
  {:type :if :predicate predicate :consequent consequent :alternative alternative})

(defn make-assignment [variable value]
  {:type :assignment :variable variable :value value})

(defn make-sequence [expressions]
  {:type :sequence :expressions expressions})

(defn make-quoted [text]
  {:type :quoted :text text})

;; Environment operations
(defn extend-environment [vars vals base-env]
  (assoc base-env :frame (zipmap vars vals) :base base-env))

(defn lookup-variable-value [var env]
  (let [name (:name var)]
    (cond
      (nil? env) (throw (Exception. (str "Unbound variable: " name)))
      (contains? (:frame env) name) (get-in env [:frame name])
      :else (lookup-variable-value var (:base env)))))

(defn define-variable! [var val env]
  (let [name (:name var)]
    (assoc-in env [:frame name] val)))

(defn set-variable-value! [var val env]
  (let [name (:name var)]
    (cond
      (nil? env) (throw (Exception. (str "Unbound variable: " name)))
      (contains? (:frame env) name) (assoc-in env [:frame name] val)
      :else (set-variable-value! var val (:base env)))))

(defn create-global-environment []
  {:frame {} :base nil})

;; Evaluation
(declare evaluate)

(defn eval-sequence [expressions env]
  (if (empty? (rest expressions))
    (evaluate (first expressions) env)
    (do
      (evaluate (first expressions) env)
      (eval-sequence (rest expressions) env))))

(defn eval-assignment [exp env]
  (let [var (:variable exp)
        val (evaluate (:value exp) env)]
    (set-variable-value! var val env)
    val))

(defn eval-definition [exp env]
  (let [var (:variable exp)
        val (evaluate (:value exp) env)]
    (define-variable! var val env)
    val))

(defn eval-if [exp env]
  (let [predicate-value (evaluate (:predicate exp) env)]
    (if predicate-value
      (evaluate (:consequent exp) env)
      (evaluate (:alternative exp) env))))

(defn eval-lambda [exp env]
  (fn [& args]
    (let [params (:parameters exp)
          new-env (extend-environment params args env)]
      (evaluate (:body exp) new-env))))

(defn eval-application [exp env]
  (let [operator (evaluate (:operator exp) env)
        operands (map #(evaluate % env) (:operands exp))]
    (apply operator operands)))

(defn evaluate [exp env]
  (cond
    (map? exp) (case (:type exp)
                 :variable (lookup-variable-value exp env)
                 :quoted (:text exp)
                 :assignment (eval-assignment exp env)
                 :definition (eval-definition exp env)
                 :if (eval-if exp env)
                 :lambda (eval-lambda exp env)
                 :sequence (eval-sequence (:expressions exp) env)
                 :application (eval-application exp env))
    :else exp))

;; Business domain
(def uk-region {:code "UK" :name "United Kingdom"})
(def hk-region {:code "HK" :name "Hong Kong"})

(def basic-segment {:code "Basic" :name "Basic Customer"})
(def wealth-segment {:code "Wealth" :name "Wealth Management Customer"})

;; Main function
(defn -main
  "Financial DSL entry point"
  [& args]
  (println "Financial DSL running...")
  (let [env (create-global-environment)]
    (println "Environment created")))
