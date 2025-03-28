(ns finapp-dsl.loan-topup-example-test
  (:require [clojure.test :refer :all]
            [finapp-dsl.core :as dsl]
            [finapp-dsl.loan-topup-example :as loan-topup]))

(deftest amount-rules-test
  (testing "UK region amount rules"
    (is (= 1000 (loan-topup/get-min-amount-by-region "UK")))
    (is (= 25000 (loan-topup/get-max-amount-by-region "UK"))))
  
  (testing "HK region amount rules"
    (is (= 5000 (loan-topup/get-min-amount-by-region "HK")))
    (is (= 150000 (loan-topup/get-max-amount-by-region "HK"))))
  
  (testing "Segment multipliers"
    (is (= 1.0 (loan-topup/get-amount-multiplier-by-segment "Basic")))
    (is (= 1.5 (loan-topup/get-amount-multiplier-by-segment "Wealth")))))

(deftest apply-amount-rule-test
  (testing "Basic loan amount calculation"
    (is (= 10000 (loan-topup/apply-amount-rule "UK" "Basic" 10000))))
  
  (testing "Wealth segment gets higher amount"
    (is (= 15000 (loan-topup/apply-amount-rule "UK" "Wealth" 10000))))
  
  (testing "Maximum amount cap is enforced"
    (is (= 25000 (loan-topup/apply-amount-rule "UK" "Wealth" 20000))))
  
  (testing "Minimum amount floor is enforced"
    (is (= 1000 (loan-topup/apply-amount-rule "UK" "Basic" 500)))))

(deftest loan-topup-environment-setup-test
  (testing "Environment setup includes required functions"
    (let [env (loan-topup/setup-loan-topup-environment)
          eq-var (dsl/make-variable "equal?")
          add-var (dsl/make-variable "add")
          rule-var (dsl/make-variable "apply-amount-rule")]
      
      ;; Check that functions are defined
      (is (fn? (dsl/lookup-variable-value eq-var env)))
      (is (fn? (dsl/lookup-variable-value add-var env)))
      (is (fn? (dsl/lookup-variable-value rule-var env)))
      
      ;; Check that functions work as expected
      (let [eq-fn (dsl/lookup-variable-value eq-var env)
            add-fn (dsl/lookup-variable-value add-var env)
            rule-fn (dsl/lookup-variable-value rule-var env)]
        (is (eq-fn "UK" "UK"))
        (is (not (eq-fn "UK" "HK")))
        (is (= 7 (add-fn 3 4)))
        (is (= 15000 (rule-fn "UK" "Wealth" 10000)))))))

(deftest build-offer-calculation-test
  (testing "Offer calculation expression structure"
    (let [env (dsl/create-global-environment)
          calc-expr (loan-topup/build-offer-calculation env)]
      
      ;; Verify it's an application expression
      (is (= :application (:type calc-expr)))
      
      ;; Verify it calls apply-amount-rule function
      (is (= "apply-amount-rule" (get-in calc-expr [:operator :name])))
      
      ;; Verify it has three arguments
      (is (= 3 (count (:operands calc-expr)))))))

(deftest dsl-integration-test
  (testing "Full loan topup calculation using DSL"
    (let [env (loan-topup/setup-loan-topup-environment)
          calc-expr (loan-topup/build-offer-calculation env)
          
          ;; Set up test values for UK Wealth customer with 10000 base amount
          env-with-values (-> env
                             (dsl/define-variable! (dsl/make-variable "region") "UK")
                             (dsl/define-variable! (dsl/make-variable "segment") "Wealth")
                             (dsl/define-variable! (dsl/make-variable "baseAmount") 10000))
          
          ;; Evaluate using the DSL
          result (dsl/evaluate calc-expr env-with-values)]
      
      ;; Should be 10000 * 1.5 = 15000
      (is (= 15000 result))
      
      ;; Try with a different region and segment
      (let [env-hk-basic (-> env
                            (dsl/define-variable! (dsl/make-variable "region") "HK")
                            (dsl/define-variable! (dsl/make-variable "segment") "Basic")
                            (dsl/define-variable! (dsl/make-variable "baseAmount") 4000))
            result-hk (dsl/evaluate calc-expr env-hk-basic)]
        
        ;; Should be minimum amount for HK (5000) since 4000 is below minimum
        (is (= 5000 result-hk))))))) 