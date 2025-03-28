const { Given, When, Then } = require('@cucumber/cucumber');
const { expect } = require('chai');
const LoanTopupPage = require('../../support/pages/loan-topup.page');
const LoansOverviewPage = require('../../support/pages/loans-overview.page');
const NotificationService = require('../../support/services/notification.service');
const AccountService = require('../../support/services/account.service');

// Page objects
const loansOverviewPage = new LoansOverviewPage();
const loanTopupPage = new LoanTopupPage();

// Background steps
Given('I am authenticated in the mobile banking app', async function() {
  // Implementation would use the authentication helper from common steps
  await this.authenticate();
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
});

// Initial offer steps
When('I navigate to the {string} section', async function(sectionName) {
  await this.navigateTo(sectionName);
});

Then('I should see a {string} message', async function(message) {
  const visibleMessage = await loansOverviewPage.getTopupOfferMessage();
  expect(visibleMessage).to.include(message);
});

Then('I should see my current loan details:', async function(dataTable) {
  const expectedDetails = dataTable.raw()[0];
  const loanSummary = await loansOverviewPage.getLoanSummary();
  
  expectedDetails.forEach(detail => {
    expect(loanSummary).to.include(detail);
  });
});

Then('I should see a primary call-to-action button {string}', async function(buttonText) {
  const isVisible = await loansOverviewPage.isPrimaryButtonVisible(buttonText);
  expect(isVisible).to.be.true;
});

Then('I should see a secondary option {string}', async function(optionText) {
  const isVisible = await loansOverviewPage.isSecondaryOptionVisible(optionText);
  expect(isVisible).to.be.true;
});

// Exploration steps
Given('I am on the {string} section', async function(sectionName) {
  await this.navigateTo(sectionName);
});

When('I select {string}', async function(option) {
  await this.clickOn(option);
});

Then('I should see a personalised message welcoming me to the top-up journey', async function() {
  const welcomeMessage = await loanTopupPage.getWelcomeMessage();
  expect(welcomeMessage).to.include(this.customerData.firstName);
});

Then('I should see my pre-approved maximum top-up amount', async function() {
  const maxAmount = await loanTopupPage.getMaxTopupAmount();
  expect(maxAmount).to.equal(this.topupPreApproval.maxAmount);
});

Then('I should see how this would affect my monthly payments', async function() {
  const monthlyPaymentInfo = await loanTopupPage.getMonthlyPaymentInfo();
  expect(monthlyPaymentInfo).to.not.be.empty;
});

// Amount selection steps
Given('I am on the loan top-up offer screen', async function() {
  await loanTopupPage.navigateToOfferScreen();
});

Then('I should see at least three pre-defined amount options', async function() {
  const amountOptions = await loanTopupPage.getAmountOptions();
  expect(amountOptions.length).to.be.at.least(3);
});

Then('each option should display:', async function(dataTable) {
  const expectedDetails = dataTable.raw()[0];
  const optionDetails = await loanTopupPage.getFirstAmountOptionDetails();
  
  expectedDetails.forEach(detail => {
    expect(optionDetails).to.include(detail);
  });
});

Then('the {string} button should be disabled until an amount is selected', async function(buttonText) {
  let isDisabled = await loanTopupPage.isButtonDisabled(buttonText);
  expect(isDisabled).to.be.true;
  
  await loanTopupPage.selectFirstAmountOption();
  
  isDisabled = await loanTopupPage.isButtonDisabled(buttonText);
  expect(isDisabled).to.be.false;
});

// Custom amount steps
Given('I am on the amount selection screen', async function() {
  await loanTopupPage.navigateToAmountSelectionScreen();
});

Then('I should see a slider and input field to specify an amount', async function() {
  const hasSlider = await loanTopupPage.hasAmountSlider();
  const hasInputField = await loanTopupPage.hasAmountInput();
  
  expect(hasSlider).to.be.true;
  expect(hasInputField).to.be.true;
});

Then('the amount should be constrained between the minimum and maximum allowed', async function() {
  await loanTopupPage.tryEnterAmountBelowMinimum();
  let currentAmount = await loanTopupPage.getCurrentAmount();
  expect(currentAmount).to.equal(this.topupPreApproval.minAmount);
  
  await loanTopupPage.tryEnterAmountAboveMaximum();
  currentAmount = await loanTopupPage.getCurrentAmount();
  expect(currentAmount).to.equal(this.topupPreApproval.maxAmount);
});

// Exceeding maximum steps
Given('I am on the custom amount selection screen', async function() {
  await loanTopupPage.navigateToCustomAmountScreen();
});

When('I try to enter an amount greater than my pre-approved limit', async function() {
  await loanTopupPage.enterAmount(this.topupPreApproval.maxAmount + 1000);
});

Then('the system should prevent me from exceeding the maximum', async function() {
  const currentAmount = await loanTopupPage.getCurrentAmount();
  expect(currentAmount).to.equal(this.topupPreApproval.maxAmount);
});

Then('I should see an error message explaining the limitation', async function() {
  const errorMessage = await loanTopupPage.getErrorMessage();
  expect(errorMessage).to.include('maximum');
});

// Terms viewing steps
Given('I have selected a top-up amount', async function() {
  this.selectedAmount = 3000; // Example amount
  await loanTopupPage.selectAmount(this.selectedAmount);
});

Then('I should see a detailed breakdown of my loan top-up:', async function(dataTable) {
  const expectedSections = dataTable.raw();
  const detailsText = await loanTopupPage.getDetailsBreakdown();
  
  expectedSections.forEach(row => {
    row.forEach(item => {
      expect(detailsText).to.include(item);
    });
  });
});

Then('I should see the regulatory information including representative APR', async function() {
  const regulatoryInfo = await loanTopupPage.getRegulatoryInfo();
  expect(regulatoryInfo).to.include('APR');
});

// Confirmation steps
Given('I am on the detailed terms screen', async function() {
  await loanTopupPage.navigateToDetailedTermsScreen();
});

Then('I should see a confirmation screen with:', async function(dataTable) {
  const expectedSections = dataTable.raw()[0];
  const confirmationText = await loanTopupPage.getConfirmationScreenText();
  
  expectedSections.forEach(section => {
    expect(confirmationText).to.include(section);
  });
});

Then('the {string} button should be disabled until I check the agreement box', async function(buttonText) {
  let isDisabled = await loanTopupPage.isButtonDisabled(buttonText);
  expect(isDisabled).to.be.true;
  
  await loanTopupPage.checkAgreementBox();
  
  isDisabled = await loanTopupPage.isButtonDisabled(buttonText);
  expect(isDisabled).to.be.false;
});

// Final confirmation steps
Given('I am on the confirmation screen', async function() {
  await loanTopupPage.navigateToConfirmationScreen();
});

Given('I have checked the agreement box', async function() {
  await loanTopupPage.checkAgreementBox();
});

Then('I should see a processing indicator', async function() {
  const isProcessing = await loanTopupPage.isProcessingIndicatorVisible();
  expect(isProcessing).to.be.true;
});

Then('then I should see a success screen showing:', async function(dataTable) {
  const expectedSections = dataTable.raw();
  
  // Wait for processing to complete
  await loanTopupPage.waitForProcessingToComplete();
  
  const successText = await loanTopupPage.getSuccessScreenText();
  
  expectedSections.forEach(row => {
    row.forEach(item => {
      expect(successText).to.include(item);
    });
  });
});

// Disbursement steps
Given('I have confirmed my loan top-up', async function() {
  this.referenceNumber = 'TOP' + Math.floor(Math.random() * 1000000);
  await loanTopupPage.confirmTopup();
  await loanTopupPage.setReferenceNumber(this.referenceNumber);
});

Then('the funds should be credited to my designated account', async function() {
  const account = await AccountService.getAccountDetails(this.customerData.primaryAccountId);
  const transaction = account.recentTransactions.find(t => 
    t.description.includes('Loan Top-up') && 
    t.amount === this.selectedAmount
  );
  
  expect(transaction).to.not.be.undefined;
});

Then('I should receive push notification confirming the top-up', async function() {
  const notifications = await NotificationService.getRecentNotifications(this.customerData.id);
  const topupNotification = notifications.find(n => 
    n.type === 'LOAN_TOPUP_CONFIRMATION' && 
    n.content.includes(this.referenceNumber)
  );
  
  expect(topupNotification).to.not.be.undefined;
});

// Abandonment steps
Given('I am in the middle of the loan top-up process', async function() {
  // Navigate to a middle step in the journey
  await loanTopupPage.navigateToAmountSelectionScreen();
  await loanTopupPage.selectAmount(3000);
});

Then('my progress should be saved', async function() {
  // Check journey state is saved
  const savedState = await loanTopupPage.getSavedJourneyState();
  expect(savedState).to.not.be.null;
  expect(savedState.selectedAmount).to.equal(3000);
});

Then('I should be able to resume from the same point within {int} days', async function(days) {
  // Implementation would verify the saved state expiry
  const expiryDate = await loanTopupPage.getJourneyStateExpiry();
  const expectedExpiry = new Date();
  expectedExpiry.setDate(expectedExpiry.getDate() + days);
  
  expect(expiryDate.getTime()).to.be.closeTo(expectedExpiry.getTime(), 1000 * 60 * 60); // Within an hour
});

// Declining offer steps
Then('the top-up offer should remain available', async function() {
  await this.navigateTo('My Loans');
  const isOfferAvailable = await loansOverviewPage.isTopupOfferAvailable();
  expect(isOfferAvailable).to.be.true;
});

Then('I should not see the offer again for at least {int} days', async function(days) {
  // Implementation would validate suppression settings
  const suppressionSettings = await loansOverviewPage.getOfferSuppressionSettings();
  const expectedMinDate = new Date();
  expectedMinDate.setDate(expectedMinDate.getDate() + days);
  
  expect(suppressionSettings.suppressUntil.getTime()).to.be.at.least(expectedMinDate.getTime());
});