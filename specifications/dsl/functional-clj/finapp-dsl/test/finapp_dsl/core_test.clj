(ns finapp-dsl.core-test
  (:require [clojure.test :refer :all]
            [finapp-dsl.core :refer :all]))

(deftest variable-test
  (testing "Variable creation"
    (let [var (make-variable "x")]
      (is (= :variable (:type var)))
      (is (= "x" (:name var))))))

(deftest application-test
  (testing "Application creation"
    (let [op (make-variable "add")
          args [(make-variable "x") (make-variable "y")]
          app (make-application op args)]
      (is (= :application (:type app)))
      (is (= op (:operator app)))
      (is (= args (:operands app))))))

(deftest lambda-test
  (testing "Lambda creation"
    (let [params ["x" "y"]
          body (make-application 
                 (make-variable "add") 
                 [(make-variable "x") (make-variable "y")])
          lambda (make-lambda params body)]
      (is (= :lambda (:type lambda)))
      (is (= params (:parameters lambda)))
      (is (= body (:body lambda))))))

(deftest environment-test
  (testing "Environment operations"
    (let [env (create-global-environment)
          var (make-variable "x")
          val 42
          env2 (define-variable! var val env)]
      (is (= val (lookup-variable-value var env2)))
      
      (let [env3 (set-variable-value! var 99 env2)]
        (is (= 99 (lookup-variable-value var env3))))
      
      (let [var2 (make-variable "y")
            env4 (define-variable! var2 100 env2)]
        (is (= val (lookup-variable-value var env4)))
        (is (= 100 (lookup-variable-value var2 env4)))))))

(deftest evaluation-test
  (testing "Basic evaluation"
    (let [env (create-global-environment)
          
          ;; Define add function in environment
          add-fn (fn [a b] (+ a b))
          env-with-add (define-variable! (make-variable "add") add-fn env)
          
          ;; Create expression: (add 2 3)
          expr (make-application 
                 (make-variable "add") 
                 [2 3])]
      
      (is (= 5 (evaluate expr env-with-add)))))
  
  (testing "Lambda evaluation"
    (let [env (create-global-environment)
          
          ;; Define a lambda: (lambda (x y) (+ x y))
          lambda-expr (make-lambda 
                        ["x" "y"] 
                        (make-application 
                          (make-variable "+") 
                          [(make-variable "x") (make-variable "y")]))
          
          ;; Define + function in environment
          plus-fn (fn [a b] (+ a b))
          env-with-plus (define-variable! (make-variable "+") plus-fn env)
          
          ;; Evaluate lambda to get a function
          lambda-fn (evaluate lambda-expr env-with-plus)
          
          ;; Apply the function to arguments
          result (lambda-fn 10 20)]
      
      (is (= 30 result))))
  
  (testing "If expression evaluation"
    (let [env (create-global-environment)
          
          ;; Define > function in environment
          gt-fn (fn [a b] (> a b))
          env-with-gt (define-variable! (make-variable ">") gt-fn env)
          
          ;; Create if expression: (if (> 5 3) "yes" "no")
          if-expr (make-if
                    (make-application 
                      (make-variable ">") 
                      [5 3])
                    "yes"
                    "no")]
      
      (is (= "yes" (evaluate if-expr env-with-gt)))
      
      ;; Create another if: (if (> 2 7) "yes" "no")
      (let [if-expr2 (make-if
                       (make-application 
                         (make-variable ">") 
                         [2 7])
                       "yes"
                       "no")]
        (is (= "no" (evaluate if-expr2 env-with-gt)))))))

(deftest sequence-test
  (testing "Sequence evaluation"
    (let [env (create-global-environment)
          
          ;; Define necessary functions
          env-with-fns (-> env
                          (define-variable! (make-variable "+") (fn [a b] (+ a b)))
                          (define-variable! (make-variable "*") (fn [a b] (* a b))))
          
          ;; Create sequence: (begin (+ 1 2) (* 3 4))
          seq-expr (make-sequence 
                     [(make-application 
                        (make-variable "+") 
                        [1 2])
                      (make-application 
                        (make-variable "*") 
                        [3 4])])]
      
      ;; Last expression's value should be returned
      (is (= 12 (evaluate seq-expr env-with-fns))))))

(deftest quoted-test
  (testing "Quoted values"
    (let [env (create-global-environment)
          quoted-expr (make-quoted "hello")]
      (is (= "hello" (evaluate quoted-expr env))))))

(deftest assignment-test
  (testing "Assignment evaluation"
    (let [env (create-global-environment)
          var (make-variable "x")
          
          ;; Define x initially
          env-with-x (define-variable! var 10 env)
          
          ;; Create assignment: (set! x 42)
          assign-expr (make-assignment var 42)]
      
      (let [env-after-assign (evaluate assign-expr env-with-x)]
        (is (= 42 (lookup-variable-value var env-after-assign)))))))
