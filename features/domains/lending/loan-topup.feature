Feature: Loan Top-up
  As a borrower with an existing loan
  I want to top up my loan with additional funds
  So that I can access more credit without applying for a new loan

  Background:
    Given I am logged in as a borrower
    And I have an existing active loan

  Scenario: Eligible borrower requests loan top-up
    Given my loan is in good standing
    And I have made at least 3 consecutive payments
    When I request a loan top-up
    Then I should see available top-up options
    And the maximum top-up amount should not exceed 50% of my original loan

  Scenario: Borrower selects top-up amount
    Given I am eligible for a loan top-up
    When I select a top-up amount of £500
    Then I should see the new total loan amount
    And I should see the new repayment schedule
    And I should see the updated interest calculations

  Scenario: Borrower confirms loan top-up
    Given I have selected a top-up amount
    When I confirm the loan top-up
    Then the additional funds should be disbursed to my account
    And my loan agreement should be updated with the new terms
    And I should receive a confirmation notification

  Scenario: Ineligible borrower attempts loan top-up
    Given my loan has missed payments
    When I request a loan top-up
    Then I should be informed that I am not eligible
    And I should be provided with the reasons for ineligibility 