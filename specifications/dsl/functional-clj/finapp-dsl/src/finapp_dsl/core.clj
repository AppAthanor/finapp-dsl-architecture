(ns finapp-dsl.core
  "Core namespace for Financial Application DSL. Provides functions to create, manipulate,
  and evaluate financial expressions and rules within a domain-specific language for
  financial applications."
  (:gen-class))

;; Expression types
(defn make-variable
  "Creates a variable expression with the given name.
   
   Parameters:
     name - A string or symbol representing the variable name
   
   Returns:
     A map representing a variable expression with :type and :name keys"
  [name]
  {:type :variable :name name})

(defn make-application
  "Creates an application expression representing a function call.
   
   Parameters:
     operator - The function to apply
     operands - A sequence of arguments to pass to the function
   
   Returns:
     A map representing an application expression with :type, :operator, and :operands keys"
  [operator operands]
  {:type :application :operator operator :operands operands})

(defn make-lambda
  "Creates a lambda expression representing an anonymous function.
   
   Parameters:
     parameters - A sequence of parameter names
     body - The body expression to evaluate when the function is called
   
   Returns:
     A map representing a lambda expression with :type, :parameters, and :body keys"
  [parameters body]
  {:type :lambda :parameters parameters :body body})

(defn make-if
  "Creates a conditional expression.
   
   Parameters:
     predicate - The condition to evaluate
     consequent - The expression to evaluate if predicate is true
     alternative - The expression to evaluate if predicate is false
   
   Returns:
     A map representing a conditional expression with :type, :predicate, :consequent, and :alternative keys"
  [predicate consequent alternative]
  {:type :if :predicate predicate :consequent consequent :alternative alternative})

(defn make-assignment
  "Creates an assignment expression to update a variable's value.
   
   Parameters:
     variable - The variable to assign to
     value - The value to assign
   
   Returns:
     A map representing an assignment expression with :type, :variable, and :value keys"
  [variable value]
  {:type :assignment :variable variable :value value})

(defn make-sequence
  "Creates a sequence expression to evaluate multiple expressions in order.
   
   Parameters:
     expressions - A sequence of expressions to evaluate
   
   Returns:
     A map representing a sequence expression with :type and :expressions keys"
  [expressions]
  {:type :sequence :expressions expressions})

(defn make-quoted
  "Creates a quoted expression that returns its text without evaluation.
   
   Parameters:
     text - The text to quote
   
   Returns:
     A map representing a quoted expression with :type and :text keys"
  [text]
  {:type :quoted :text text})

;; Environment operations
(defn extend-environment
  "Extends an environment with new variable bindings.
   
   Parameters:
     vars - A sequence of variable names
     vals - A sequence of values to bind to the variables
     base-env - The environment to extend
   
   Returns:
     A new environment containing the new bindings with the base environment as parent"
  [vars vals base-env]
  (assoc base-env :frame (zipmap vars vals) :base base-env))

(defn lookup-variable-value
  "Looks up the value of a variable in an environment.
   
   Parameters:
     var - The variable to look up
     env - The environment to search in
   
   Returns:
     The value bound to the variable
   
   Throws:
     Exception if the variable is unbound in the environment"
  [var env]
  (let [name (:name var)]
    (cond
      (nil? env) (throw (Exception. (str "Unbound variable: " name)))
      (contains? (:frame env) name) (get-in env [:frame name])
      :else (lookup-variable-value var (:base env)))))

(defn define-variable!
  "Defines a variable in the current environment frame.
   
   Parameters:
     var - The variable to define
     val - The value to bind to the variable
     env - The environment to define the variable in
   
   Returns:
     The updated environment with the new binding"
  [var val env]
  (let [name (:name var)]
    (assoc-in env [:frame name] val)))

(defn set-variable-value!
  "Sets the value of an existing variable in an environment.
   
   Parameters:
     var - The variable to set
     val - The new value for the variable
     env - The environment to update
   
   Returns:
     The updated environment with the new value
   
   Throws:
     Exception if the variable is unbound in the environment"
  [var val env]
  (let [name (:name var)]
    (cond
      (nil? env) (throw (Exception. (str "Unbound variable: " name)))
      (contains? (:frame env) name) (assoc-in env [:frame name] val)
      :else (set-variable-value! var val (:base env)))))

(defn create-global-environment
  "Creates a new global environment with an empty frame.
   
   Returns:
     A new environment with an empty frame and no parent environment"
  []
  {:frame {} :base nil})

;; Evaluation
(declare evaluate)

(defn eval-sequence
  "Evaluates a sequence of expressions in order, returning the result of the last expression.
   
   Parameters:
     expressions - The sequence of expressions to evaluate
     env - The environment for evaluation
   
   Returns:
     The result of evaluating the last expression in the sequence"
  [expressions env]
  (if (empty? (rest expressions))
    (evaluate (first expressions) env)
    (do
      (evaluate (first expressions) env)
      (eval-sequence (rest expressions) env))))

(defn eval-assignment
  "Evaluates an assignment expression by updating a variable in the environment.
   
   Parameters:
     exp - The assignment expression
     env - The environment for evaluation
   
   Returns:
     The value that was assigned"
  [exp env]
  (let [var (:variable exp)
        val (evaluate (:value exp) env)]
    (set-variable-value! var val env)
    val))

(defn eval-definition
  "Evaluates a definition expression by adding a new binding to the environment.
   
   Parameters:
     exp - The definition expression
     env - The environment for evaluation
   
   Returns:
     The value that was defined"
  [exp env]
  (let [var (:variable exp)
        val (evaluate (:value exp) env)]
    (define-variable! var val env)
    val))

(defn eval-if
  "Evaluates a conditional expression by first evaluating the predicate,
   then evaluating either the consequent or alternative based on the result.
   
   Parameters:
     exp - The conditional expression
     env - The environment for evaluation
   
   Returns:
     The result of evaluating either the consequent or alternative"
  [exp env]
  (let [predicate-value (evaluate (:predicate exp) env)]
    (if predicate-value
      (evaluate (:consequent exp) env)
      (evaluate (:alternative exp) env))))

(defn eval-lambda
  "Evaluates a lambda expression by creating a function that captures the environment.
   
   Parameters:
     exp - The lambda expression
     env - The environment at definition time
   
   Returns:
     A function that, when called, evaluates the body in an extended environment"
  [exp env]
  (fn [& args]
    (let [params (:parameters exp)
          new-env (extend-environment params args env)]
      (evaluate (:body exp) new-env))))

(defn eval-application
  "Evaluates a function application by evaluating the operator and operands,
   then applying the operator function to the operand values.
   
   Parameters:
     exp - The application expression
     env - The environment for evaluation
   
   Returns:
     The result of applying the operator to the operands"
  [exp env]
  (let [operator (evaluate (:operator exp) env)
        operands (map #(evaluate % env) (:operands exp))]
    (apply operator operands)))

(defn evaluate
  "Evaluates an expression in a given environment.
   
   Parameters:
     exp - The expression to evaluate
     env - The environment for evaluation
   
   Returns:
     The result of evaluating the expression"
  [exp env]
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
(def uk-region 
  "Region definition for the United Kingdom"
  {:code "UK" :name "United Kingdom"})

(def hk-region 
  "Region definition for Hong Kong"
  {:code "HK" :name "Hong Kong"})

(def basic-segment 
  "Customer segment definition for basic customers"
  {:code "Basic" :name "Basic Customer"})

(def wealth-segment 
  "Customer segment definition for wealth management customers"
  {:code "Wealth" :name "Wealth Management Customer"})

;; Main function
(defn -main
  "Financial DSL entry point. Initializes the DSL environment and
   prepares it for evaluating financial expressions.
   
   Parameters:
     args - Command line arguments (not used)
   
   Returns:
     nil"
  [& args]
  (println "Financial DSL running...")
  (let [env (create-global-environment)]
    (println "Environment created")))
