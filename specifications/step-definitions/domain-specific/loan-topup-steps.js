const { Given, When, Then } = require('@cucumber/cucumber');
const { expect } = require('chai');
const LoanTopupPage = require('../../support/pages/loan-topup.page');
const LoansOverviewPage = require('../../support/pages/loans-overview.page');
const NotificationService = require('../../support/services/notification.service');
const AccountService = require('../../support/services/account.service');

// Import the Clojure DSL through a Node.js bridge
const ClojureDSL = require('../../support/bridges/clojure-dsl-bridge');
const FunctionalDSL = ClojureDSL.loadDSL('../../dsl/functional-clj/domains/lending/loan_topup_example');

// Page objects
const loansOverviewPage = new LoansOverviewPage();
const loanTopupPage = new LoanTopupPage();

// Initialize DSL environment
let dslEnvironment;

// Background steps
Given('I am authenticated in the mobile banking app', async function() {
  // Implementation would use the authentication helper from common steps
  await this.authenticate();
  
  // Initialize DSL environment with empty frame
  dslEnvironment = FunctionalDSL.createGlobalEnvironment();
});

Given('I have an existing loan that is eligible for top-up', async function() {
  // Set up test data for an eligible loan
  this.existingLoan = {
    id: 'LOAN123456',
    currentBalance: 10000,
    originalAmount: 15000,
    monthlyPayment: 250,
    remainingTerm: 48, // months
    interestRate: 5.9
  };
  
  await loansOverviewPage.setupTestLoan(this.existingLoan);
  
  // Add loan to DSL environment
  FunctionalDSL.defineVariable('currentLoan', this.existingLoan, dslEnvironment);
});

Given('I have been pre-approved for a loan top-up', async function() {
  // Set up pre-approval test data
  this.topupPreApproval = {
    maxAmount: 5000,
    minAmount: 1000,
    interestRate: 6.2,
    isPreApproved: true
  };
  
  await loansOverviewPage.setupTopupPreApproval(this.topupPreApproval);
  
  // Add pre-approval to DSL environment
  FunctionalDSL.defineVariable('preApproval', this.topupPreApproval, dslEnvironment);
});

Given('my current region is set to {string}', async function(region) {
  this.region = region;
  
  // Set up region in test framework
  await this.setRegion(region);
  
  // Add region to DSL environment
  const regionObject = FunctionalDSL.regions[region];
  FunctionalDSL.defineVariable('region', regionObject, dslEnvironment);
});

Given('my customer segment is {string}', async function(segment) {
  this.segment = segment;
  
  // Set up customer segment in test framework
  await this.setCustomerSegment(segment);
  
  // Add customer segment to DSL environment
  const segmentObject = FunctionalDSL.customerSegments[segment];
  FunctionalDSL.defineVariable('segment', segmentObject, dslEnvironment);
  
  // Create customer object
  const customer = {
    id: 'CUST123456',
    firstName: 'Test',
    lastName: 'Customer',
    segment: segment,
    region: this.region
  };
  
  FunctionalDSL.defineVariable('customer', customer, dslEnvironment);
});

Given('the current date and time is {string}', async function(datetimeStr) {
  this.datetime = new Date(datetimeStr);
  
  // Set up mock date/time in test framework
  await this.setMockDateTime(this.datetime);
  
  // Add datetime to DSL environment
  FunctionalDSL.defineVariable('currentDateTime', this.datetime, dslEnvironment);
});

// Initial offer steps
When('I navigate to the {string} section', async function(sectionName) {
  await this.navigateTo(sectionName);
  
  // Evaluate the initial offer screen expression
  const screenContent = FunctionalDSL.evaluate(
    FunctionalDSL.loanTopupJourney.initialOfferScreen,
    dslEnvironment
  );
  
  // Store for later verification
  this.screenContent = screenContent;
});

Then('I should see a localised pre-approved message in the appropriate language', async function() {
  const visibleMessage = await loansOverviewPage.getTopupOfferMessage();
  
  // Verify using the DSL-generated content
  expect(visibleMessage).to.include(this.screenContent.localizedText.pre_approved_message);
});

Then('I should see my current loan details in the local currency format:', async function(dataTable) {
  const expectedDetails = dataTable.raw()[0];
  const loanSummary = await loansOverviewPage.getLoanSummary();
  
  // Verify the loan details are displayed and correctly formatted
  expectedDetails.forEach(detail => {
    expect(loanSummary).to.include(detail);
  });
  
  // Verify currency formatting using DSL
  const formattedAmount = this.screenContent.formattedLoanDetails.currentBalance;
  expect(loanSummary).to.include(formattedAmount);
});

Then('I should see all monetary values in {string} format', async function(currencyFormat) {
  const screenText = await loansOverviewPage.getAllText();
  
  // Check if amounts are formatted according to the correct currency
  if (currencyFormat.includes('GBP')) {
    expect(screenText).to.match(/£[0-9,]+\.[0-9]{2}/);
  } else if (currencyFormat.includes('HKD')) {
    expect(screenText).to.match(/HK\$[0-9,]+\.[0-9]{2}/);
  }
});

// Amount selection steps
Given('I am on the loan top-up offer screen', async function() {
  await loanTopupPage.navigateToOfferScreen();
  
  // Evaluate the offer screen expression
  const screenContent = FunctionalDSL.evaluate(
    FunctionalDSL.loanTopupJourney.initialOfferScreen,
    dslEnvironment
  );
  
  // Store for later verification
  this.screenContent = screenContent;
});

When('I select the localised version of {string}', async function(buttonTextKey) {
  // Get localized button text from DSL
  const localizedButtonText = this.screenContent.localizedText[buttonTextKey.toLowerCase().replace(/\s+/g, '_')];
  
  // Click the button with localized text
  await loanTopupPage.clickButton(localizedButtonText);
  
  // If this is the amount selection button, evaluate amount screen
  if (buttonTextKey === 'Select Amount') {
    const amountScreenContent = FunctionalDSL.evaluate(
      FunctionalDSL.loanTopupJourney.amountSelectionScreen,
      dslEnvironment
    );
    
    this.amountScreenContent = amountScreenContent;
  }
});

Then('I should see at least three pre-defined amount options in {string} format', async function(currencyFormat) {
  const amountOptions = await loanTopupPage.getAmountOptions();
  
  // Verify at least 3 options
  expect(amountOptions.length).to.be.at.least(3);
  
  // Verify currency format
  amountOptions.forEach(option => {
    if (currencyFormat.includes('GBP')) {
      expect(option).to.match(/£[0-9,]+\.[0-9]{2}/);
    } else if (currencyFormat.includes('HKD')) {
      expect(option).to.match(/HK\$[0-9,]+\.[0-9]{2}/);
    }
  });
});

Then('the pre-defined options should be appropriate for my customer segment', async function() {
  const amountOptions = await loanTopupPage.getAmountOptions();
  
  // Get segment-specific limits from DSL
  const segmentObject = FunctionalDSL.customerSegments[this.segment];
  const minAmount = segmentObject.minTopupAmounts[this.region];
  const maxAmount = segmentObject.maxTopupAmounts[this.region];
  
  // Verify amount options are within segment limits
  amountOptions.forEach(option => {
    const amount = parseFloat(option.replace(/[^0-9.]/g, ''));
    expect(amount).to.be.at.least(minAmount);
    expect(amount).to.be.at.most(maxAmount);
  });
});

// Custom amount steps
Given('I am on the amount selection screen', async function() {
  await loanTopupPage.navigateToAmountSelectionScreen();
  
  // Evaluate amount screen expression
  const amountScreenContent = FunctionalDSL.evaluate(
    FunctionalDSL.loanTopupJourney.amountSelectionScreen,
    dslEnvironment
  );
  
  this.amountScreenContent = amountScreenContent;
});

When('I select the option to enter a custom amount', async function() {
  // Get localized text for custom amount option
  const customAmountText = this.amountScreenContent.localizedText.custom_amount_option;
  
  // Click custom amount option
  await loanTopupPage.clickOption(customAmountText);
});

Then('I should see a slider and input field to specify an amount in {string}"', async function(currency) {
  const hasSlider = await loanTopupPage.hasAmountSlider();
  const hasInputField = await loanTopupPage.hasAmountInput();
  
  expect(hasSlider).to.be.true;
  expect(hasInputField).to.be.true;
  
  // Verify currency symbol is displayed
  const currencySymbol = currency.includes('GBP') ? '£' : 'HK$';
  const inputLabel = await loanTopupPage.getAmountInputLabel();
  expect(inputLabel).to.include(currencySymbol);
});

Then('the amount should be constrained between {string} and {string}"', async function(minAmount, maxAmount) {
  // Attempt to enter amounts outside the range
  await loanTopupPage.tryEnterAmountBelowMinimum(minAmount);
  let currentAmount = await loanTopupPage.getCurrentAmount();
  expect(currentAmount).to.equal(minAmount);
  
  await loanTopupPage.tryEnterAmountAboveMaximum(maxAmount);
  currentAmount = await loanTopupPage.getCurrentAmount();
  expect(currentAmount).to.equal(maxAmount);
});

// Terms viewing steps
Given('I have selected a top-up amount', async function() {
  // Get a valid amount for the segment
  const segmentObject = FunctionalDSL.customerSegments[this.segment];
  const minAmount = segmentObject.minTopupAmounts[this.region];
  const maxAmount = segmentObject.maxTopupAmounts[this.region];
  this.selectedAmount = Math.floor((minAmount + maxAmount) / 2);
  
  // Select the amount in the UI
  await loanTopupPage.selectAmount(this.selectedAmount);
  
  // Add selected amount to DSL environment
  FunctionalDSL.defineVariable('selectedAmount', this.selectedAmount, dslEnvironment);
});

When('I click the localised version of {string}"', async function(buttonTextKey) {
  // Get localized button text
  const localizedButtonText = this.amountScreenContent.localizedText[buttonTextKey.toLowerCase().replace(/\s+/g, '_')];
  
  // Click the button
  await loanTopupPage.clickButton(localizedButtonText);
  
  // If this is the Continue button, evaluate terms screen
  if (buttonTextKey === 'Continue') {
    const termsScreenContent = FunctionalDSL.evaluate(
      FunctionalDSL.loanTopupJourney.termsReviewScreen,
      dslEnvironment
    );
    
    this.termsScreenContent = termsScreenContent;
  }
});

Then('I should see a detailed breakdown of my loan top-up in {string}:', async function(currency, dataTable) {
  const expectedSections = dataTable.raw();
  const detailsText = await loanTopupPage.getDetailsBreakdown();
  
  // Verify sections are present
  expectedSections.forEach(row => {
    row.forEach(item => {
      expect(detailsText).to.include(item);
    });
  });
  
  // Verify currency formatting
  const currencySymbol = currency.includes('GBP') ? '£' : 'HK$';
  expect(detailsText).to.include(currencySymbol);
});

Then('I should see the regulatory information specific to {string} including:', async function(region, dataTable) {
  const expectedRegInfo = dataTable.raw()[0];
  const regulatoryInfo = await loanTopupPage.getRegulatoryInfo();
  
  // Verify regulatory information is present
  expectedRegInfo.forEach(item => {
    if (item.includes("<regulatory_body>")) {
      // Replace placeholder with actual regulatory body
      const regBody = region === 'UK' ? 'FCA' : 'HKMA';
      expect(regulatoryInfo).to.include(item);
    }
  });
});

Then('I should see the {string} displayed prominently', async function(segmentBenefit) {
  const screenText = await loanTopupPage.getScreenText();
  expect(screenText).to.include(segmentBenefit);
  
  // Check if benefit is marked as prominent
  const isPremium = await loanTopupPage.isElementHighlighted(segmentBenefit);
  expect(isPremium).to.be.true;
});

// Confirmation steps
Given('I am on the detailed terms screen', async function() {
  await loanTopupPage.navigateToDetailedTermsScreen();
  
  // Evaluate terms screen expression
  const termsScreenContent = FunctionalDSL.evaluate(
    FunctionalDSL.loanTopupJourney.termsReviewScreen,
    dslEnvironment
  );
  
  this.termsScreenContent = termsScreenContent;
});

When('I select the localised version of {string}', async function(buttonTextKey) {
  // Get localized button text
  const localizedButtonText = this.termsScreenContent.localizedText[buttonTextKey.toLowerCase().replace(/\s+/g, '_')];
  
  // Click the button
  await loanTopupPage.clickButton(localizedButtonText);
  
  // If this is the Accept button, evaluate confirmation screen
  if (buttonTextKey === 'Accept') {
    const confirmationScreenContent = FunctionalDSL.evaluate(
      FunctionalDSL.loanTopupJourney.confirmationScreen,
      dslEnvironment
    );
    
    this.confirmationScreenContent = confirmationScreenContent;
  }
});

Then('I should see a confirmation screen with content appropriate for {string}:', async function(region, dataTable) {
  const expectedSections = dataTable.raw()[0];
  const confirmationText = await loanTopupPage.getConfirmationScreenText();
  
  // Verify sections are present
  expectedSections.forEach(section => {
    expect(confirmationText).to.include(section);
  });
  
  // Verify region-specific content
  if (region === 'UK') {
    expect(confirmationText).to.include('UK');
    expect(confirmationText).to.include('pounds');
  } else if (region === 'HK') {
    expect(confirmationText).to.include('Hong Kong');
    expect(confirmationText).to.include('Hong Kong dollars');
  }
});

Then('I should see the {string} information', async function(disclosureType) {
  const disclosureText = await loanTopupPage.getDisclosureText();
  expect(disclosureText).to.include(disclosureType);
});

// Final confirmation steps
Given('I am on the confirmation screen', async function() {
  await loanTopupPage.navigateToConfirmationScreen();
  
  // Evaluate confirmation screen expression
  const confirmationScreenContent = FunctionalDSL.evaluate(
    FunctionalDSL.loanTopupJourney.confirmationScreen,
    dslEnvironment
  );
  
  this.confirmationScreenContent = confirmationScreenContent;
});

Given('I have checked the agreement box', async function() {
  await loanTopupPage.checkAgreementBox();
});

Then('I should see a processing indicator', async function() {
  const isProcessing = await loanTopupPage.isProcessingIndicatorVisible();
  expect(isProcessing).to.be.true;
});

Then('then I should see a success screen showing region-appropriate information:', async function(dataTable) {
  const expectedSections = dataTable.raw();
  
  // Wait for processing to complete
  await loanTopupPage.waitForProcessingToComplete();
  
  // Evaluate success screen expression
  const successScreenContent = FunctionalDSL.evaluate(
    FunctionalDSL.loanTopupJourney.successScreen,
    dslEnvironment
  );
  
  this.successScreenContent = successScreenContent;
  
  const successText = await loanTopupPage.getSuccessScreenText();
  
  // Verify sections are present
  expectedSections.forEach(row => {
    row.forEach(item => {
      // Handle placeholders
      if (item.includes("<currency>")) {
        const currencySymbol = this.region === 'UK' ? '£' : 'HK$';
        expect(successText).to.include(currencySymbol);
      } else {
        expect(successText).to.include(item);
      }
    });
  });
});

Then('I should see the {string} regarding when funds will be available', async function(fundMessage) {
  const successText = await loanTopupPage.getSuccessScreenText();
  expect(successText).to.include(fundMessage);
});

Then('I should see the {string} if applicable', async function(specialNotice) {
  if (specialNotice !== 'None') {
    const successText = await loanTopupPage.getSuccessScreenText();
    expect(successText).to.include(specialNotice);
  }
});

// Disbursement steps
Given('I have confirmed my loan top-up', async function() {
  this.referenceNumber = 'TOP' + Math.floor(Math.random() * 1000000);
  await loanTopupPage.confirmTopup();
  await loanTopupPage.setReferenceNumber(this.referenceNumber);
  
  // Add reference number to DSL environment
  FunctionalDSL.defineVariable('referenceNumber', this.referenceNumber, dslEnvironment);
});

Then('the funds should be credited to my designated account according to {string} banking procedures', async function(region) {
  const account = await AccountService.getAccountDetails(this.customerData.primaryAccountId);
  const transaction = account.recentTransactions.find(t => 
    t.description.includes('Loan Top-up') && 
    t.amount === this.selectedAmount
  );
  
  expect(transaction).to.not.be.undefined;
  
  // Verify region-specific processing was applied
  if (region === 'UK') {
    expect(transaction.description).to.include('UK');
  } else if (region === 'HK') {
    expect(transaction.description).to.include('HK');
  }
});

Then('I should receive push notification in my preferred language confirming the top-up', async function() {
  const notifications = await NotificationService.getRecentNotifications(this.customerData.id);
  const topupNotification = notifications.find(n => 
    n.type === 'LOAN_TOPUP_CONFIRMATION' && 
    n.content.includes(this.referenceNumber)
  );
  
  expect(topupNotification).to.not.be.undefined;
  
  // Verify language of notification
  if (this.region === 'UK') {
    expect(topupNotification.language).to.equal('en-GB');
  } else if (this.region === 'HK') {
    // Could be English or Chinese based on preference
    expect(['en-HK', 'zh-HK']).to.include(topupNotification.language);
  }
});

Then('I should receive any {string} applicable to my customer segment', async function(segmentBenefit) {
  if (segmentBenefit !== 'Standard servicing') {
    const account = await AccountService.getAccountDetails(this.customerData.primaryAccountId);
    const benefits = account.recentBenefits || [];
    
    const matchingBenefit = benefits.find(b => b.description.includes(segmentBenefit));
    expect(matchingBenefit).to.not.be.undefined;
  }
});

// Timeframe steps
Then('I should see the {string} message', async function(timeframeMessage) {
  const successText = await loanTopupPage.getSuccessScreenText();
  expect(successText).to.include(timeframeMessage);
});

Then('my funds should be processed according to {string}', async function(schedule) {
  // This would check the processing time based on schedule
  const processingTimestamp = await AccountService.getProcessingTimestamp(this.customerData.primaryAccountId, this.referenceNumber);
  
  if (schedule === 'Instant processing') {
    expect(processingTimestamp).to.be.closeTo(new Date().getTime(), 5000); // Within 5 seconds
  } else if (schedule === 'Next working day processing') {
    // Would check next working day logic here
    const isNextWorkingDay = await AccountService.isNextWorkingDay(processingTimestamp, this.region);
    expect(isNextWorkingDay).to.be.true;
  }
});

// Abandonment steps
Given('I am in the middle of the loan top-up process', async function() {
  // Navigate to a middle step in the journey
  await loanTopupPage.navigateToAmountSelectionScreen();
  await loanTopupPage.selectAmount(3000);
  
  // Add selected amount to DSL environment
  FunctionalDSL.defineVariable('selectedAmount', 3000, dslEnvironment);
});

Then('my progress should be saved', async function() {
  // Check journey state is saved
  const savedState = await loanTopupPage.getSavedJourneyState();
  expect(savedState).to.not.be.null;
  expect(savedState.selectedAmount).to.equal(3000);
});

Then('I should be able to resume from the same point within {string} days', async function(days) {
  // Implementation would verify the saved state expiry
  const expiryDate = await loanTopupPage.getJourneyStateExpiry();
  const expectedExpiry = new Date();
  expectedExpiry.setDate(expectedExpiry.getDate() + parseInt(days));
  
  expect(expiryDate.getTime()).to.be.closeTo(expectedExpiry.getTime(), 1000 * 60 * 60); // Within an hour
});

Then('I should receive a reminder notification after {string} days if I haven\'t completed the process', async function(reminderDays) {
  // Mock future date
  const futureDate = new Date();
  futureDate.setDate(futureDate.getDate() + parseInt(reminderDays));
  
  // Set mock time to future date
  await this.setMockDateTime(futureDate);
  
  // Check for reminder notification
  const notifications = await NotificationService.getRecentNotifications(this.customerData.id);
  const reminderNotification = notifications.find(n => 
    n.type === 'LOAN_TOPUP_REMINDER' && 
    n.createdAt > new Date(futureDate.getTime() - 1000 * 60 * 60) // Within the last hour
  );
  
  expect(reminderNotification).to.not.be.undefined;
});

// Declining offer steps
Then('the top-up offer should remain available', async function() {
  await this.navigateTo('My Loans');
  const isOfferAvailable = await loansOverviewPage.isTopupOfferAvailable();
  expect(isOfferAvailable).to.be.true;
});

Then('I should not see the offer again for at least {string} days', async function(days) {
  // Implementation would validate suppression settings
  const suppressionSettings = await loansOverviewPage.getOfferSuppressionSettings();
  const expectedMinDate = new Date();
  expectedMinDate.setDate(expectedMinDate.getDate() + parseInt(days));
  
  expect(suppressionSettings.suppressUntil.getTime()).to.be.at.least(expectedMinDate.getTime());
});

// Follow-up steps
Given('I have either completed or abandoned my loan top-up journey', async function() {
  // Set up state for either scenario
  this.journeyOutcome = Math.random() > 0.5 ? 'completed' : 'abandoned';
  
  if (this.journeyOutcome === 'completed') {
    await loanTopupPage.completeJourney();
  } else {
    await loanTopupPage.abandonJourney();
  }
  
  // Add outcome to DSL environment
  FunctionalDSL.defineVariable('journeyOutcome', this.journeyOutcome, dslEnvironment);
});

When('{string} days have passed', async function(days) {
  // Mock future date
  const futureDate = new Date();
  futureDate.setDate(futureDate.getDate() + parseInt(days));
  
  // Set mock time to future date
  await this.setMockDateTime(futureDate);
  
  // Add to DSL environment
  FunctionalDSL.defineVariable('currentDateTime', futureDate, dslEnvironment);
});

Then('I should receive the appropriate {string} in my preferred language', async function(followUpType) {
  // Check for follow-up communication
  const communications = await NotificationService.getRecentCommunications(this.customerData.id);
  const followUpComm = communications.find(c => 
    c.type === followUpType.toUpperCase().replace(/\s+/g, '_') && 
    c.createdAt > new Date(new Date().getTime() - 1000 * 60 * 60 * 24) // Within the last day
  );
  
  expect(followUpComm).to.not.be.undefined;
  
  // Verify language
  if (this.region === 'UK') {
    expect(followUpComm.language).to.equal('en-GB');
  } else if (this.region === 'HK') {
    // Could be English or Chinese based on preference
    expect(['en-HK', 'zh-HK']).to.include(followUpComm.language);
  }
});