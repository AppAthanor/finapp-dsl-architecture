Feature: Loan Top-up for Pre-approved Customers
  As a pre-approved loan customer
  I want to borrow additional funds on my existing loan
  So that I can meet my financial needs without applying for a new loan

  Background:
    Given I am authenticated in the mobile banking app
    And I have an existing loan that is eligible for top-up
    And I have been pre-approved for a loan top-up
    And my current region is set to "<region>"
    And my customer segment is "<segment>"
    And the current date and time is "<datetime>"

  Examples:
    | region | segment | datetime               | description                      |
    | UK     | Basic   | 2025-03-15T14:30:00Z   | UK Basic customer, standard hours |
    | UK     | Wealth  | 2025-03-15T14:30:00Z   | UK Wealth customer, standard hours |
    | HK     | Basic   | 2025-03-15T10:30:00+08 | HK Basic customer, standard hours |
    | HK     | Wealth  | 2025-03-15T10:30:00+08 | HK Wealth customer, standard hours |
    | UK     | Basic   | 2025-03-15T02:30:00Z   | UK Basic customer, off-peak hours |
    | HK     | Basic   | 2025-03-15T20:30:00+08 | HK Basic customer, evening hours  |

  Scenario: Customer is presented with personalised loan top-up offer
    When I navigate to the "My Loans" section
    Then I should see a localised pre-approved message in the appropriate language
    And I should see my current loan details in the local currency format:
      | Current balance remaining | Remaining term | Current monthly payment |
    And I should see a primary call-to-action button with localised text
    And I should see a secondary option with localised text
    
  Scenario Outline: Region-specific offer presentation
    Given my current region is set to "<region>"
    When I navigate to the "My Loans" section
    Then I should see all monetary values in "<currency>" format
    And I should see dates in "<date_format>" format
    And I should see the terms and conditions specific to "<region>" regulations
    
    Examples:
      | region | currency | date_format      | 
      | UK     | GBP (£)  | DD/MM/YYYY       | 
      | HK     | HKD ($)  | DD/MM/YYYY       |
      
  Scenario Outline: Customer segment determines interest rates and limits
    Given my current region is set to "<region>"
    And my customer segment is "<segment>"
    When I navigate to the "My Loans" section
    And I select "View Your Offer"
    Then I should see interest rate of "<interest_rate>"
    And I should see a maximum top-up amount of up to "<max_amount>"
    
    Examples:
      | region | segment | interest_rate | max_amount |
      | UK     | Basic   | 6.9% APR      | £25,000    |
      | UK     | Wealth  | 5.4% APR      | £100,000   |
      | HK     | Basic   | 7.2% APR      | HK$200,000 |
      | HK     | Wealth  | 5.8% APR      | HK$800,000 |
      
  Scenario Outline: Time-sensitive promotional content
    Given my current region is set to "<region>"
    And the current date and time is "<datetime>"
    When I navigate to the "My Loans" section
    Then I should see the "<promotional_banner>" displayed
    
    Examples:
      | region | datetime               | promotional_banner                   |
      | UK     | 2025-03-15T14:30:00Z   | Spring Home Improvement Promotion    |
      | UK     | 2025-03-15T02:30:00Z   | Night Owl Banking Benefits           |
      | UK     | 2025-05-20T14:30:00Z   | Summer Holiday Financing Options     |
      | HK     | 2025-01-25T10:30:00+08 | Lunar New Year Special Offers        |
      | HK     | 2025-03-15T20:30:00+08 | Evening Banking Exclusive Benefits   |

  Scenario: Customer explores initial loan top-up offer
    Given I am on the "My Loans" section
    When I select "View Your Offer"
    Then I should see a personalised message welcoming me to the top-up journey
    And I should see my pre-approved maximum top-up amount
    And I should see how this would affect my monthly payments
    And I should see primary call-to-action "Select Amount"
    And I should see secondary option "Maybe Later"

  Scenario Outline: Customer selects from pre-defined loan top-up amounts based on segment
    Given my current region is set to "<region>"
    And my customer segment is "<segment>"
    And I am on the loan top-up offer screen
    When I select the localised version of "Select Amount"
    Then I should see at least three pre-defined amount options in "<currency>" format
    And the pre-defined options should be appropriate for my customer segment
    And each option should display in the local language and currency:
      | Top-up amount | New total loan | New monthly payment |
    And I should see an option to enter a custom amount
    And I should see a localised "Continue" button
    And the "Continue" button should be disabled until an amount is selected
    
    Examples:
      | region | segment | currency |
      | UK     | Basic   | GBP (£)  |
      | UK     | Wealth  | GBP (£)  |
      | HK     | Basic   | HKD ($)  |
      | HK     | Wealth  | HKD ($)  |

  Scenario Outline: Customer enters a custom loan top-up amount with segment-specific minimums
    Given my current region is set to "<region>"
    And my customer segment is "<segment>"
    And I am on the amount selection screen
    When I select the option to enter a custom amount
    Then I should see a slider and input field to specify an amount in "<currency>"
    And the amount should be constrained between "<min_amount>" and "<max_amount>"
    And I should see my current loan details for reference in local format
    And I should see how my selection impacts in local currency:
      | New total loan amount | New monthly payment | Remaining term |
    And the localised "Continue" button should enable once a valid amount is entered
    
    Examples:
      | region | segment | currency | min_amount | max_amount  |
      | UK     | Basic   | GBP (£)  | £1,000     | £25,000     |
      | UK     | Wealth  | GBP (£)  | £5,000     | £100,000    |
      | HK     | Basic   | HKD ($)  | HK$10,000  | HK$200,000  |
      | HK     | Wealth  | HKD ($)  | HK$50,000  | HK$800,000  |

  Scenario: Customer attempts to exceed maximum top-up amount
    Given I am on the custom amount selection screen
    When I try to enter an amount greater than my pre-approved limit
    Then the system should prevent me from exceeding the maximum
    And I should see a localised error message explaining the limitation
    And the input should be automatically adjusted to the maximum allowed amount

  Scenario Outline: Time-of-day specific loan offers and messaging
    Given my current region is set to "<region>"
    And the current date and time is "<datetime>"
    And I am on the amount selection screen
    Then I should see the "<special_message>" displayed
    And the "<special_offer>" should be available if applicable
    
    Examples:
      | region | datetime               | special_message                   | special_offer                   |
      | UK     | 2025-03-15T23:30:00Z   | Night owl banking active          | Fee-free top-up processing      |
      | UK     | 2025-03-15T14:30:00Z   | Standard service hours            | None                            |
      | UK     | 2025-12-24T14:30:00Z   | Holiday period service notice     | Festive payment holiday option  |
      | HK     | 2025-03-15T03:30:00+08 | After-hours processing notice     | Next-day processing guarantee   |
      | HK     | 2025-03-15T10:30:00+08 | Standard service hours            | None                            |

  Scenario Outline: Customer views detailed loan top-up terms with region-specific regulations
    Given my current region is set to "<region>"
    And my customer segment is "<segment>"
    And I have selected a top-up amount
    When I click the localised version of "Continue"
    Then I should see a detailed breakdown of my loan top-up in "<currency>":
      | Original loan amount | Current balance | Top-up amount | New total balance |
      | Current monthly payment | New monthly payment | Remaining term |
      | Interest rate | Total interest payable | Total repayable |
    And I should see the regulatory information specific to "<region>" including:
      | Representative APR | "<regulatory_body>" disclosures | Cooling-off period |
    And I should see localised options to "Accept" or "Go Back"
    
    Examples:
      | region | segment | currency | regulatory_body |
      | UK     | Basic   | GBP (£)  | FCA             |
      | UK     | Wealth  | GBP (£)  | FCA             |
      | HK     | Basic   | HKD ($)  | HKMA            |
      | HK     | Wealth  | HKD ($)  | HKMA            |

  Scenario Outline: Segment-specific benefits displayed on terms screen
    Given my current region is set to "<region>"
    And my customer segment is "<segment>"
    And I have selected a top-up amount
    When I reach the detailed terms screen
    Then I should see the "<segment_benefit>" displayed prominently
    
    Examples:
      | region | segment | segment_benefit                            |
      | UK     | Basic   | No early repayment charges                 |
      | UK     | Wealth  | Premier rate guarantee and priority service|
      | HK     | Basic   | Standard repayment flexibility             |
      | HK     | Wealth  | Jade member priority processing and rate discount |

  Scenario Outline: Customer reviews and accepts loan top-up offer with local regulations
    Given my current region is set to "<region>"
    And I am on the detailed terms screen
    When I select the localised version of "Accept"
    Then I should see a confirmation screen with content appropriate for "<region>":
      | Summary of the top-up details |
      | Region-specific terms and conditions |
      | Checkbox to confirm I've read and agree to the terms in my local language |
    And the localised "Confirm Top-up" button should be disabled until I check the agreement box
    And I should see the "<region_specific_disclosure>" information
    
    Examples:
      | region | region_specific_disclosure                                 |
      | UK     | Credit reference agency notice and financial ombudsman details |
      | HK     | HKMA regulatory notice and complaint procedure details       |

  Scenario Outline: Customer confirms loan top-up with region-specific processing
    Given my current region is set to "<region>"
    And I am on the confirmation screen
    And I have checked the agreement box
    When I select the localised version of "Confirm Top-up"
    Then I should see a processing indicator
    And then I should see a success screen showing region-appropriate information:
      | Localised confirmation message | Top-up amount in "<currency>" | Reference number |
      | Funds available according to "<region>" banking hours | New monthly payment amount |
      | Date of first new payment in local format |
    And I should see options in the local language to view updated loan details or return to account overview
    
    Examples:
      | region | currency | 
      | UK     | GBP (£)  |
      | HK     | HKD ($)  |
      
  Scenario Outline: Time-sensitive confirmation experiences
    Given my current region is set to "<region>"
    And the current date and time is "<datetime>"
    And I am on the confirmation screen
    And I have checked the agreement box
    When I select the localised version of "Confirm Top-up"
    Then I should see the "<funds_availability_message>" regarding when funds will be available
    And I should see the "<special_notice>" if applicable
    
    Examples:
      | region | datetime               | funds_availability_message       | special_notice                           |
      | UK     | 2025-03-15T14:30:00Z   | Funds available immediately      | None                                     |
      | UK     | 2025-03-15T23:30:00Z   | Funds available immediately      | Night owl banking service active         |
      | UK     | 2025-03-15T16:59:00Z   | Funds available immediately      | None                                     |
      | UK     | 2025-03-15T17:01:00Z   | Funds available immediately      | Evening processing confirmation          |
      | UK     | 2025-12-24T14:30:00Z   | Funds available immediately      | Holiday period service notice            |
      | HK     | 2025-03-15T10:30:00+08 | Funds available immediately      | None                                     |
      | HK     | 2025-03-15T16:31:00+08 | Funds available immediately      | None                                     |
      | HK     | 2025-01-25T10:30:00+08 | Funds available immediately      | Lunar New Year holiday processing notice |

  Scenario Outline: Funds are disbursed after loan top-up confirmation with region-specific processing
    Given my current region is set to "<region>"
    And my customer segment is "<segment>"
    And I have confirmed my loan top-up
    When the success screen appears
    Then the funds should be credited to my designated account according to "<region>" banking procedures
    And I should receive push notification in my preferred language confirming the top-up
    And I should receive an email confirmation to my registered email address in the appropriate language
    And my loan details in the app should be updated with the new information in "<currency>" format
    And I should receive any "<segment_benefit>" applicable to my customer segment
    
    Examples:
      | region | segment | currency | segment_benefit                       |
      | UK     | Basic   | GBP (£)  | Standard servicing                    |
      | UK     | Wealth  | GBP (£)  | Premier thank you message and reward points |
      | HK     | Basic   | HKD ($)  | Standard servicing                    |
      | HK     | Wealth  | HKD ($)  | Jade member status points and priority service notice |

  Scenario Outline: Region-specific disbursement timeframes
    Given my current region is set to "<region>"
    And the current date and time is "<datetime>"
    And I have confirmed my loan top-up
    Then I should see the "<disbursement_timeframe>" message
    And my funds should be processed according to "<processing_schedule>"
    
    Examples:
      | region | datetime               | disbursement_timeframe            | processing_schedule          |
      | UK     | 2025-03-15T14:30:00Z   | Immediate disbursement            | Instant processing           |
      | UK     | 2025-03-15T23:30:00Z   | Immediate disbursement            | Instant processing           |
      | UK     | 2025-03-14T22:00:00Z   | Immediate disbursement            | Instant processing           |
      | UK     | 2025-12-25T14:30:00Z   | Bank holiday processing notice    | Next working day processing  |
      | HK     | 2025-03-15T10:30:00+08 | Immediate disbursement            | Instant processing           |
      | HK     | 2025-03-15T19:30:00+08 | Immediate disbursement            | Instant processing           |
      | HK     | 2025-01-25T10:30:00+08 | Lunar New Year processing notice  | Next working day processing  |

  Scenario Outline: Customer abandons loan top-up journey with region-specific resumption
    Given my current region is set to "<region>"
    And I am in the middle of the loan top-up process
    When I select the localised version of "Maybe Later" or navigate away from the journey
    Then my progress should be saved
    And I should be able to resume from the same point within "<resumption_period>" days
    And I should receive a reminder notification after "<reminder_days>" days if I haven't completed the process
    And the reminder should be in my preferred language
    
    Examples:
      | region | resumption_period | reminder_days |
      | UK     | 14                | 3             |
      | HK     | 14                | 2             |

  Scenario Outline: Customer declines loan top-up offer with region-specific suppression
    Given my current region is set to "<region>"
    And I am on the initial offer screen
    When I select the localised version of "No thanks, maybe later"
    Then I should return to the "My Loans" section
    And the top-up offer should remain available
    And I should see an option to reconsider the top-up offer in my local language
    And I should not see the offer again for at least "<suppression_days>" days
    
    Examples:
      | region | suppression_days |
      | UK     | 7                |
      | HK     | 7                |
      
  Scenario Outline: Segment-specific follow-up after completion or abandonment
    Given my current region is set to "<region>"
    And my customer segment is "<segment>"
    And I have either completed or abandoned my loan top-up journey
    When "<follow_up_days>" days have passed
    Then I should receive the appropriate "<follow_up_type>" in my preferred language
    
    Examples:
      | region | segment | follow_up_days | follow_up_type                          |
      | UK     | Basic   | 7              | Standard satisfaction survey            |
      | UK     | Wealth  | 3              | Relationship manager courtesy call      |
      | HK     | Basic   | 7              | Standard satisfaction survey            |
      | HK     | Wealth  | 2              | Priority relationship manager follow-up |