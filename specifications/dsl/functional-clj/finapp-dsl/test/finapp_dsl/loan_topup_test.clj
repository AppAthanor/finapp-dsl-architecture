(ns finapp-dsl.loan-topup-test
  (:require [clojure.test :refer :all]
            [finapp-dsl.core :as dsl]
            [finapp-dsl.loan-topup :as loan-topup]))

(deftest region-definitions-test
  (testing "Region definitions"
    (is (= "UK" (:code loan-topup/uk-region)))
    (is (= "GBP" (:currency loan-topup/uk-region)))
    (is (= "HK" (:code loan-topup/hk-region)))
    (is (= "HKD" (:currency loan-topup/hk-region)))
    (is (= "SG" (:code loan-topup/sg-region)))
    (is (= "SGD" (:currency loan-topup/sg-region)))))

(deftest amount-rules-test
  (testing "UK region amount rules"
    (is (= 1000 (loan-topup/get-min-amount-by-region "UK")))
    (is (= 25000 (loan-topup/get-max-amount-by-region "UK"))))
  
  (testing "HK region amount rules"
    (is (= 5000 (loan-topup/get-min-amount-by-region "HK")))
    (is (= 150000 (loan-topup/get-max-amount-by-region "HK"))))
  
  (testing "SG region amount rules"
    (is (= 1500 (loan-topup/get-min-amount-by-region "SG")))
    (is (= 30000 (loan-topup/get-max-amount-by-region "SG"))))
  
  (testing "Segment multipliers"
    (is (= 1.0 (loan-topup/get-amount-multiplier-by-segment "Basic")))
    (is (= 1.5 (loan-topup/get-amount-multiplier-by-segment "Wealth")))
    (is (= 2.0 (loan-topup/get-amount-multiplier-by-segment "Private")))))

(deftest customer-segments-test
  (testing "Basic segment definitions"
    (is (= 6 (loan-topup/get-min-account-age-by-segment "Basic")))
    (is (= 15000 (loan-topup/get-min-income-by-segment-and-region "Basic" "UK")))
    (is (= 120000 (loan-topup/get-min-income-by-segment-and-region "Basic" "HK")))
    (is (= 24000 (loan-topup/get-min-income-by-segment-and-region "Basic" "SG"))))
  
  (testing "Wealth segment definitions"
    (is (= 3 (loan-topup/get-min-account-age-by-segment "Wealth")))
    (is (= 75000 (loan-topup/get-min-income-by-segment-and-region "Wealth" "UK")))
    (is (= 600000 (loan-topup/get-min-income-by-segment-and-region "Wealth" "HK")))
    (is (= 120000 (loan-topup/get-min-income-by-segment-and-region "Wealth" "SG"))))
  
  (testing "Private segment definitions"
    (is (= 1 (loan-topup/get-min-account-age-by-segment "Private")))
    (is (= 150000 (loan-topup/get-min-income-by-segment-and-region "Private" "UK")))
    (is (= 1200000 (loan-topup/get-min-income-by-segment-and-region "Private" "HK")))
    (is (= 240000 (loan-topup/get-min-income-by-segment-and-region "Private" "SG")))))

(deftest interest-rate-test
  (testing "Base interest rates by segment and region"
    (is (= 6.5 (loan-topup/get-interest-rate-by-segment-and-region "Basic" "UK")))
    (is (= 5.4 (loan-topup/get-interest-rate-by-segment-and-region "Wealth" "UK")))
    (is (= 4.9 (loan-topup/get-interest-rate-by-segment-and-region "Private" "UK"))))
  
  (testing "Interest rate adjustments based on amount"
    (is (= 6.5 (loan-topup/calculate-interest-rate "Basic" "UK" 10000)))
    (is (= 7.0 (loan-topup/calculate-interest-rate "Basic" "UK" 4000)))
    (is (= 5.15 (loan-topup/calculate-interest-rate "Wealth" "UK" 60000)))))

(deftest apply-amount-rule-test
  (testing "Basic loan amount calculation"
    (is (= 10000 (loan-topup/apply-amount-rule "UK" "Basic" 10000))))
  
  (testing "Wealth segment gets higher amount"
    (is (= 15000 (loan-topup/apply-amount-rule "UK" "Wealth" 10000))))
  
  (testing "Private segment gets highest amount"
    (is (= 20000 (loan-topup/apply-amount-rule "UK" "Private" 10000))))
  
  (testing "Maximum amount cap is enforced"
    (is (= 25000 (loan-topup/apply-amount-rule "UK" "Wealth" 20000))))
  
  (testing "Minimum amount floor is enforced"
    (is (= 1000 (loan-topup/apply-amount-rule "UK" "Basic" 500)))))

(deftest apply-eligibility-rule-test
  (testing "Customer meets all criteria"
    (let [customer {:id "C123456"
                    :segment "Basic"
                    :age 30
                    :annualIncome 20000
                    :accountAgeMonths 12}
          result (loan-topup/apply-eligibility-rule customer "UK")]
      (is (:eligible result))
      (is (vector? (:benefits result)))))
  
  (testing "Customer below minimum age"
    (let [customer {:id "C123456"
                    :segment "Basic"
                    :age 20
                    :annualIncome 20000
                    :accountAgeMonths 12}
          result (loan-topup/apply-eligibility-rule customer "UK")]
      (is (not (:eligible result)))
      (is (= "Customer must be at least 21 years old" (:reason result)))))
  
  (testing "Customer below minimum income"
    (let [customer {:id "C123456"
                    :segment "Basic"
                    :age 30
                    :annualIncome 10000
                    :accountAgeMonths 12}
          result (loan-topup/apply-eligibility-rule customer "UK")]
      (is (not (:eligible result)))
      (is (= "Customer income below minimum requirement" (:reason result)))))
  
  (testing "Customer below minimum account age"
    (let [customer {:id "C123456"
                    :segment "Basic"
                    :age 30
                    :annualIncome 20000
                    :accountAgeMonths 3}
          result (loan-topup/apply-eligibility-rule customer "UK")]
      (is (not (:eligible result)))
      (is (= "Account not established long enough" (:reason result))))))

(deftest loan-topup-environment-setup-test
  (testing "Environment setup includes required functions"
    (let [env (loan-topup/setup-loan-topup-environment)
          eq-var (dsl/make-variable "equal?")
          add-var (dsl/make-variable "add")
          amount-rule-var (dsl/make-variable "apply-amount-rule")
          eligibility-rule-var (dsl/make-variable "apply-eligibility-rule")
          interest-rate-var (dsl/make-variable "calculate-interest-rate")]
      
      ;; Check that functions are defined
      (is (fn? (dsl/lookup-variable-value eq-var env)))
      (is (fn? (dsl/lookup-variable-value add-var env)))
      (is (fn? (dsl/lookup-variable-value amount-rule-var env)))
      (is (fn? (dsl/lookup-variable-value eligibility-rule-var env)))
      (is (fn? (dsl/lookup-variable-value interest-rate-var env)))
      
      ;; Check that functions work as expected
      (let [eq-fn (dsl/lookup-variable-value eq-var env)
            add-fn (dsl/lookup-variable-value add-var env)
            amount-rule-fn (dsl/lookup-variable-value amount-rule-var env)]
        (is (eq-fn "UK" "UK"))
        (is (not (eq-fn "UK" "HK")))
        (is (= 7 (add-fn 3 4)))
        (is (= 15000 (amount-rule-fn "UK" "Wealth" 10000)))))))

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
        (is (= 5000 result-hk))))))

(deftest journey-execution-test
  (testing "End-to-end journey execution for eligible customer"
    (let [result (loan-topup/execute-loan-topup-journey "C888888" "UK" 10000)]
      
      (is (:success result))
      (is (string? (:journeyId result)))
      (is (= "C888888" (:customerId result)))
      (is (:eligible (:eligibility result)))
      (is (map? (:offer result)))
      (is (= 15000 (get-in result [:offer :amount])))
      (is (= "GBP" (get-in result [:offer :currency])))
      (is (number? (get-in result [:offer :interestRate])))
      (is (= 36 (get-in result [:offer :term])))
      (is (number? (get-in result [:offer :monthlyPayment])))))
  
  (testing "End-to-end journey execution for private customer"
    (let [result (loan-topup/execute-loan-topup-journey "C999999" "UK" 10000)]
      (is (:success result))
      (is (= 20000 (get-in result [:offer :amount])))
      (is (< (get-in result [:offer :interestRate]) 5.0))))) 