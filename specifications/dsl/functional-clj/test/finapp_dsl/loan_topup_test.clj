(ns finapp-dsl.loan-topup-test
  (:require [clojure.test :refer :all]
            [finapp-dsl.core :as core]
            [finapp-dsl.loan-topup-example :as loan]))

(deftest topup-amount-validation-test
  (testing "Basic customer UK amount validation"
    (is (loan/validate-topup-amount "UK" "Basic" 5000))
    (is (not (loan/validate-topup-amount "UK" "Basic" 500)))
    (is (not (loan/validate-topup-amount "UK" "Basic" 30000))))
  
  (testing "Wealth customer UK amount validation"
    (is (loan/validate-topup-amount "UK" "Wealth" 10000))
    (is (not (loan/validate-topup-amount "UK" "Wealth" 3000)))
    (is (not (loan/validate-topup-amount "UK" "Wealth" 150000))))
  
  (testing "Basic customer HK amount validation"
    (is (loan/validate-topup-amount "HK" "Basic" 15000))
    (is (not (loan/validate-topup-amount "HK" "Basic" 5000)))
    (is (not (loan/validate-topup-amount "HK" "Basic" 250000))))
  
  (testing "Wealth customer HK amount validation"
    (is (loan/validate-topup-amount "HK" "Wealth" 100000))
    (is (not (loan/validate-topup-amount "HK" "Wealth" 40000)))
    (is (not (loan/validate-topup-amount "HK" "Wealth" 900000)))))

(deftest business-rule-test
  (testing "Amount limits rule application for valid amounts"
    (is (= 15000 (loan/apply-amount-rule "UK" "Basic" 15000)))
    (is (= 75000 (loan/apply-amount-rule "UK" "Wealth" 75000)))
    (is (= 100000 (loan/apply-amount-rule "HK" "Basic" 100000)))
    (is (= 500000 (loan/apply-amount-rule "HK" "Wealth" 500000))))
  
  (testing "Amount limits rule application for invalid amounts"
    (is (= {:error "Amount outside limits"} (loan/apply-amount-rule "UK" "Basic" 500)))
    (is (= {:error "Amount outside limits"} (loan/apply-amount-rule "UK" "Wealth" 3000)))
    (is (= {:error "Amount outside limits"} (loan/apply-amount-rule "HK" "Basic" 5000)))
    (is (= {:error "Amount outside limits"} (loan/apply-amount-rule "HK" "Wealth" 850000))))) 