// Loan Top-up Functional DSL Specification
// Following SICP-enhanced functional architecture principles

// ===== Core Expression Types =====

// Expression constructors
const makeVariable = name => ({ type: 'variable', name });
const makeApplication = (operator, operands) => ({ type: 'application', operator, operands });
const makeLambda = (params, body) => ({ type: 'lambda', params, body });
const makeIf = (predicate, consequent, alternative) => ({ type: 'if', predicate, consequent, alternative });
const makeAssignment = (variable, value) => ({ type: 'assignment', variable, value });
const makeSequence = expressions => ({ type: 'sequence', expressions });
const makeQuoted = data => ({ type: 'quoted', data });

// Expression testers
const isVariable = exp => exp.type === 'variable';
const isApplication = exp => exp.type === 'application';
const isLambda = exp => exp.type === 'lambda';
const isIf = exp => exp.type === 'if';
const isAssignment = exp => exp.type === 'assignment';
const isSequence = exp => exp.type === 'sequence';
const isQuoted = exp => exp.type === 'quoted';
const isSelfEvaluating = exp => typeof exp !== 'object' || exp === null;

// ===== Domain-Specific Constructors =====

// Region constructors
const makeRegion = (code, properties) => ({
  type: 'region',
  code,
  properties
});

// Customer segment constructors
const makeCustomerSegment = (code, properties) => ({
  type: 'customer_segment',
  code,
  properties
});

// Time period constructors
const makeTimePeriod = (code, properties) => ({
  type: 'time_period',
  code,
  properties
});

// Promotion constructors
const makePromotion = (code, properties) => ({
  type: 'promotion',
  code,
  properties
});

// ===== Domain-Specific Testers =====

const isRegion = exp => exp.type === 'region';
const isCustomerSegment = exp => exp.type === 'customer_segment';
const isTimePeriod = exp => exp.type === 'time_period';
const isPromotion = exp => exp.type === 'promotion';

// ===== Domain-Specific Business Rules =====

// Business rule constructor
const makeBusinessRule = (id, condition, action, metadata) => ({
  type: 'business_rule',
  id,
  condition,
  action,
  metadata
});

// Business rule application
const applyBusinessRule = (rule, environment) => {
  const conditionResult = evaluate(rule.condition, environment);
  if (conditionResult) {
    return evaluate(rule.action, environment);
  }
  return null;
};

// ===== Environment Model =====

// Create a frame from variables and values
const makeFrame = (variables, values) => {
  const frame = {};
  for (let i = 0; i < variables.length; i++) {
    frame[variables[i]] = values[i];
  }
  return frame;
};

// Extend an environment with a new frame
const extendEnvironment = (variables, values, baseEnv) => {
  if (variables.length !== values.length) {
    throw new Error('Variables and values must have the same length');
  }
  
  const frame = makeFrame(variables, values);
  return { frame, parent: baseEnv };
};

// Look up a variable value in an environment
const lookupVariableValue = (variable, env) => {
  if (!env) {
    throw new Error(`Unbound variable: ${variable}`);
  }
  
  if (variable in env.frame) {
    return env.frame[variable];
  }
  
  return lookupVariableValue(variable, env.parent);
};

// Define a variable in an environment
const defineVariable = (variable, value, env) => {
  env.frame[variable] = value;
  return value;
};

// Set a variable's value in an environment
const setVariableValue = (variable, value, env) => {
  if (!env) {
    throw new Error(`Unbound variable: ${variable}`);
  }
  
  if (variable in env.frame) {
    env.frame[variable] = value;
    return value;
  }
  
  return setVariableValue(variable, value, env.parent);
};

// ===== Core Evaluator =====

// Procedure representation
const makeProcedure = (parameters, body, environment) => ({
  type: 'procedure',
  parameters,
  body,
  environment
});

// Evaluate an expression in an environment
const evaluate = (expression, environment) => {
  // Self-evaluating expressions
  if (isSelfEvaluating(expression)) {
    return expression;
  }
  
  // Variables
  if (isVariable(expression)) {
    return lookupVariableValue(expression.name, environment);
  }
  
  // Quoted expressions
  if (isQuoted(expression)) {
    return expression.data;
  }
  
  // Assignments
  if (isAssignment(expression)) {
    const value = evaluate(expression.value, environment);
    setVariableValue(expression.variable.name, value, environment);
    return value;
  }
  
  // Conditionals
  if (isIf(expression)) {
    const predicate = evaluate(expression.predicate, environment);
    if (predicate) {
      return evaluate(expression.consequent, environment);
    } else if (expression.alternative) {
      return evaluate(expression.alternative, environment);
    } else {
      return null;
    }
  }
  
  // Sequences
  if (isSequence(expression)) {
    return evaluateSequence(expression.expressions, environment);
  }
  
  // Lambda expressions
  if (isLambda(expression)) {
    return makeProcedure(
      expression.params,
      expression.body,
      environment
    );
  }
  
  // Applications
  if (isApplication(expression)) {
    const procedure = evaluate(expression.operator, environment);
    const args = expression.operands.map(operand => evaluate(operand, environment));
    
    return applyProcedure(procedure, args);
  }
  
  throw new Error(`Unknown expression type: ${expression.type}`);
};

// Helper for sequences
const evaluateSequence = (expressions, environment) => {
  let result;
  for (const expression of expressions) {
    result = evaluate(expression, environment);
  }
  return result;
};

// Apply a procedure to arguments
const applyProcedure = (procedure, args) => {
  if (typeof procedure === 'function') {
    return procedure(...args);
  }
  
  if (procedure.type === 'procedure') {
    const env = extendEnvironment(
      procedure.parameters,
      args,
      procedure.environment
    );
    return evaluate(procedure.body, env);
  }
  
  throw new Error(`Unknown procedure type: ${procedure}`);
};

// ===== Loan Top-up Domain Definitions =====

// Define regions
const ukRegion = makeRegion('UK', {
  currency: 'GBP',
  currencySymbol: '£',
  dateFormat: 'DD/MM/YYYY',
  regulatoryBody: 'FCA',
  coolingOffPeriod: 14, // days
  language: 'en-GB',
  translationsKey: 'uk_translations'
});

const hkRegion = makeRegion('HK', {
  currency: 'HKD',
  currencySymbol: 'HK$',
  dateFormat: 'DD/MM/YYYY',
  regulatoryBody: 'HKMA',
  coolingOffPeriod: 14, // days
  languages: ['en-HK', 'zh-HK'],
  translationsKey: 'hk_translations'
});

// Define customer segments
const basicSegment = makeCustomerSegment('Basic', {
  description: 'Standard retail banking customers',
  interestRates: {
    UK: 6.9,
    HK: 7.2
  },
  minTopupAmounts: {
    UK: 1000,
    HK: 10000
  },
  maxTopupAmounts: {
    UK: 25000,
    HK: 200000
  }
});

const wealthSegment = makeCustomerSegment('Wealth', {
  description: 'Premier or priority banking customers with higher value accounts',
  interestRates: {
    UK: 5.4,
    HK: 5.8
  },
  minTopupAmounts: {
    UK: 5000,
    HK: 50000
  },
  maxTopupAmounts: {
    UK: 100000,
    HK: 800000
  },
  benefits: {
    UK: ['Preferential rates', 'Dedicated relationship manager', 'Fee waivers'],
    HK: ['Priority processing', 'Jade status points', 'Fee waivers']
  }
});

// Define time periods
const standardHours = makeTimePeriod('standard_hours', {
  UK: '08:00-17:00 GMT/BST Mon-Fri',
  HK: '09:00-17:00 HKT Mon-Fri',
  processingTimeframe: 'Immediate'
});

const eveningHours = makeTimePeriod('evening_hours', {
  UK: '17:00-22:00 GMT/BST Mon-Fri',
  HK: '17:00-22:00 HKT Mon-Fri',
  processingTimeframe: 'Immediate'
});

const nightHours = makeTimePeriod('night_hours', {
  UK: '22:00-08:00 GMT/BST',
  HK: '22:00-09:00 HKT',
  processingTimeframe: 'Immediate'
});

const weekendHours = makeTimePeriod('weekend_hours', {
  UK: 'All day Sat-Sun',
  HK: 'All day Sat-Sun',
  processingTimeframe: 'Immediate'
});

// Define seasonal promotions
const springHomePromoUK = makePromotion('spring_home_uk', {
  name: 'Spring Home Improvement',
  region: 'UK',
  startDate: '2025-03-01',
  endDate: '2025-05-31',
  bannerKey: 'spring_home_banner',
  offer: 'No fee on home improvement loan top-ups'
});

const lunarNewYearPromoHK = makePromotion('lunar_new_year_hk', {
  name: 'Lunar New Year Special',
  region: 'HK',
  startDate: '2025-01-15',
  endDate: '2025-02-28',
  bannerKey: 'lunar_new_year_banner',
  offer: 'Lucky rate reduction on loan top-ups'
});

// ===== Business Rules =====

// Rule 1: Top-up amount limits by segment and region
const topupAmountLimitsRule = makeBusinessRule(
  'BR001',
  makeLambda(
    ['customer', 'region', 'amount'],
    makeApplication(
      makeVariable('and'),
      [
        makeApplication(
          makeVariable('>='),
          [
            makeVariable('amount'),
            makeApplication(
              makeVariable('getMinTopupAmount'),
              [makeVariable('customer'), makeVariable('region')]
            )
          ]
        ),
        makeApplication(
          makeVariable('<='),
          [
            makeVariable('amount'),
            makeApplication(
              makeVariable('getMaxTopupAmount'),
              [makeVariable('customer'), makeVariable('region')]
            )
          ]
        )
      ]
    )
  ),
  makeLambda(
    ['customer', 'region', 'amount'],
    makeApplication(
      makeVariable('validateAmount'),
      [makeVariable('amount')]
    )
  ),
  {
    description: 'Top-up amount must be between the minimum and maximum pre-approved limits for the customer\'s segment and region',
    rationale: 'Ensures the additional borrowing is within affordability parameters appropriate for customer segment and local market conditions',
    errorMessageKey: 'amount_limit_error'
  }
);

// Rule 2: Term remains unchanged
const unchangedTermRule = makeBusinessRule(
  'BR002',
  makeLambda(
    ['currentLoan', 'topupDetails'],
    makeApplication(
      makeVariable('==='),
      [
        makeApplication(makeVariable('get'), [makeVariable('topupDetails'), 'term']),
        makeApplication(makeVariable('get'), [makeVariable('currentLoan'), 'remainingTerm'])
      ]
    )
  ),
  makeLambda(
    ['currentLoan', 'topupDetails'],
    makeSequence([
      makeAssignment(
        makeVariable('topupDetails'),
        makeApplication(
          makeVariable('assoc'),
          [makeVariable('topupDetails'), 'term', makeApplication(makeVariable('get'), [makeVariable('currentLoan'), 'remainingTerm'])]
        )
      ),
      makeVariable('topupDetails')
    ])
  ),
  {
    description: 'The remaining term does not change as a result of the top-up',
    rationale: 'Maintains the original loan end date while adjusting the monthly payment',
    errorMessageKey: 'term_modification_error'
  }
);

// Rule 3: Interest rate determination by segment
const interestRateRule = makeBusinessRule(
  'BR003',
  makeLambda(
    ['customer', 'region'],
    makeApplication(makeVariable('hasSegment'), [makeVariable('customer')])
  ),
  makeLambda(
    ['customer', 'region'],
    makeApplication(
      makeVariable('getInterestRate'),
      [
        makeApplication(makeVariable('getSegment'), [makeVariable('customer')]),
        makeVariable('region')
      ]
    )
  ),
  {
    description: 'Interest rates must be determined based on customer segment and region',
    rationale: 'Different customer segments qualify for different rates in each region'
  }
);

// Rule 4: Promotional offers by date and region
const promotionalOffersRule = makeBusinessRule(
  'BR004',
  makeLambda(
    ['currentDate', 'region', 'promotion'],
    makeApplication(
      makeVariable('and'),
      [
        makeApplication(
          makeVariable('>='),
          [makeVariable('currentDate'), makeApplication(makeVariable('get'), [makeVariable('promotion'), 'startDate'])]
        ),
        makeApplication(
          makeVariable('<='),
          [makeVariable('currentDate'), makeApplication(makeVariable('get'), [makeVariable('promotion'), 'endDate'])]
        ),
        makeApplication(
          makeVariable('==='),
          [makeApplication(makeVariable('get'), [makeVariable('promotion'), 'region']), makeVariable('region')]
        )
      ]
    )
  ),
  makeLambda(
    ['currentDate', 'region', 'promotion'],
    makeApplication(
      makeVariable('displayPromotion'),
      [makeVariable('promotion')]
    )
  ),
  {
    description: 'Promotional offers must be displayed based on current date and region',
    rationale: 'Ensures time-sensitive offers are only displayed when valid in the correct region'
  }
);

// Rule 5: Time-based processing information
const processingTimeframeRule = makeBusinessRule(
  'BR005',
  makeLambda(
    ['currentDateTime', 'region'],
    makeApplication(
      makeVariable('isHoliday'),
      [makeVariable('currentDateTime'), makeVariable('region')]
    )
  ),
  makeLambda(
    ['currentDateTime', 'region'],
    makeIf(
      makeApplication(
        makeVariable('isHoliday'),
        [makeVariable('currentDateTime'), makeVariable('region')]
      ),
      makeApplication(
        makeVariable('getHolidayProcessingMessage'),
        [makeVariable('region')]
      ),
      makeApplication(
        makeVariable('getStandardProcessingMessage'),
        [makeVariable('currentDateTime'), makeVariable('region')]
      )
    )
  ),
  {
    description: 'Time-sensitive processing information must be displayed based on current time and holidays',
    rationale: 'Sets appropriate expectations for processing times based on banking hours'
  }
);

// ===== Specific Journey Step Definitions =====

// Initial offer screen
const renderInitialOfferScreen = makeLambda(
  ['customer', 'region', 'currentDateTime'],
  makeSequence([
    // Apply region-specific formatting
    makeAssignment(
      makeVariable('formattedLoanDetails'),
      makeApplication(
        makeVariable('formatLoanDetails'),
        [makeApplication(makeVariable('getLoanDetails'), [makeVariable('customer')]), makeVariable('region')]
      )
    ),
    
    // Apply time-sensitive content
    makeAssignment(
      makeVariable('timeBasedContent'),
      makeApplication(
        makeVariable('getTimeBasedContent'),
        [makeVariable('currentDateTime'), makeVariable('region')]
      )
    ),
    
    // Apply promotional content
    makeAssignment(
      makeVariable('promotionalContent'),
      makeApplication(
        makeVariable('getApplicablePromotions'),
        [makeVariable('currentDateTime'), makeVariable('region')]
      )
    ),
    
    // Build screen components
    makeApplication(
      makeVariable('createScreen'),
      [
        makeVariable('formattedLoanDetails'),
        makeVariable('timeBasedContent'),
        makeVariable('promotionalContent'),
        makeApplication(
          makeVariable('getLocalizedText'),
          [makeVariable('region'), 'initial_offer_screen']
        )
      ]
    )
  ])
);

// Amount selection screen
const renderAmountSelectionScreen = makeLambda(
  ['customer', 'region', 'currentDateTime'],
  makeSequence([
    // Get customer segment
    makeAssignment(
      makeVariable('segment'),
      makeApplication(
        makeVariable('getCustomerSegment'),
        [makeVariable('customer')]
      )
    ),
    
    // Get segment-specific limits
    makeAssignment(
      makeVariable('minAmount'),
      makeApplication(
        makeVariable('getMinTopupAmount'),
        [makeVariable('segment'), makeVariable('region')]
      )
    ),
    
    makeAssignment(
      makeVariable('maxAmount'),
      makeApplication(
        makeVariable('getMaxTopupAmount'),
        [makeVariable('segment'), makeVariable('region')]
      )
    ),
    
    // Generate pre-defined amount options
    makeAssignment(
      makeVariable('amountOptions'),
      makeApplication(
        makeVariable('generateAmountOptions'),
        [makeVariable('minAmount'), makeVariable('maxAmount'), makeVariable('segment'), makeVariable('region')]
      )
    ),
    
    // Apply time-sensitive content
    makeAssignment(
      makeVariable('timeBasedContent'),
      makeApplication(
        makeVariable('getTimeBasedContent'),
        [makeVariable('currentDateTime'), makeVariable('region')]
      )
    ),
    
    // Build screen components
    makeApplication(
      makeVariable('createScreen'),
      [
        makeVariable('amountOptions'),
        makeVariable('timeBasedContent'),
        makeApplication(
          makeVariable('getLocalizedText'),
          [makeVariable('region'), 'amount_selection_screen']
        )
      ]
    )
  ])
);

// Terms review screen
const renderTermsReviewScreen = makeLambda(
  ['customer', 'region', 'currentDateTime', 'selectedAmount'],
  makeSequence([
    // Get customer segment
    makeAssignment(
      makeVariable('segment'),
      makeApplication(
        makeVariable('getCustomerSegment'),
        [makeVariable('customer')]
      )
    ),
    
    // Calculate new loan details
    makeAssignment(
      makeVariable('newLoanDetails'),
      makeApplication(
        makeVariable('calculateNewLoanDetails'),
        [
          makeApplication(makeVariable('getCurrentLoan'), [makeVariable('customer')]),
          makeVariable('selectedAmount'),
          makeApplication(
            makeVariable('getInterestRate'),
            [makeVariable('segment'), makeVariable('region')]
          )
        ]
      )
    ),
    
    // Format for display in local currency
    makeAssignment(
      makeVariable('formattedLoanDetails'),
      makeApplication(
        makeVariable('formatLoanDetails'),
        [makeVariable('newLoanDetails'), makeVariable('region')]
      )
    ),
    
    // Get segment-specific benefits
    makeAssignment(
      makeVariable('segmentBenefits'),
      makeApplication(
        makeVariable('getSegmentBenefits'),
        [makeVariable('segment'), makeVariable('region')]
      )
    ),
    
    // Get regulatory information
    makeAssignment(
      makeVariable('regulatoryInfo'),
      makeApplication(
        makeVariable('getRegulatoryInformation'),
        [makeVariable('region')]
      )
    ),
    
    // Build screen components
    makeApplication(
      makeVariable('createScreen'),
      [
        makeVariable('formattedLoanDetails'),
        makeVariable('segmentBenefits'),
        makeVariable('regulatoryInfo'),
        makeApplication(
          makeVariable('getLocalizedText'),
          [makeVariable('region'), 'terms_review_screen']
        )
      ]
    )
  ])
);

// Confirmation screen
const renderConfirmationScreen = makeLambda(
  ['customer', 'region', 'currentDateTime', 'loanDetails'],
  makeSequence([
    // Format for display in local currency
    makeAssignment(
      makeVariable('formattedLoanDetails'),
      makeApplication(
        makeVariable('formatLoanDetails'),
        [makeVariable('loanDetails'), makeVariable('region')]
      )
    ),
    
    // Get region-specific terms and conditions
    makeAssignment(
      makeVariable('termsAndConditions'),
      makeApplication(
        makeVariable('getTermsAndConditions'),
        [makeVariable('region')]
      )
    ),
    
    // Get processing timeframe message
    makeAssignment(
      makeVariable('processingTimeframe'),
      makeApplication(
        makeVariable('getProcessingTimeframe'),
        [makeVariable('currentDateTime'), makeVariable('region')]
      )
    ),
    
    // Build screen components
    makeApplication(
      makeVariable('createScreen'),
      [
        makeVariable('formattedLoanDetails'),
        makeVariable('termsAndConditions'),
        makeVariable('processingTimeframe'),
        makeApplication(
          makeVariable('getLocalizedText'),
          [makeVariable('region'), 'confirmation_screen']
        )
      ]
    )
  ])
);

// Success screen
const renderSuccessScreen = makeLambda(
  ['customer', 'region', 'currentDateTime', 'loanDetails', 'referenceNumber'],
  makeSequence([
    // Format for display in local currency
    makeAssignment(
      makeVariable('formattedLoanDetails'),
      makeApplication(
        makeVariable('formatLoanDetails'),
        [makeVariable('loanDetails'), makeVariable('region')]
      )
    ),
    
    // Get processing timeframe message
    makeAssignment(
      makeVariable('processingTimeframe'),
      makeApplication(
        makeVariable('getProcessingTimeframe'),
        [makeVariable('currentDateTime'), makeVariable('region')]
      )
    ),
    
    // Get segment-specific follow-up message
    makeAssignment(
      makeVariable('followUpMessage'),
      makeApplication(
        makeVariable('getFollowUpMessage'),
        [
          makeApplication(makeVariable('getCustomerSegment'), [makeVariable('customer')]),
          makeVariable('region')
        ]
      )
    ),
    
    // Build screen components
    makeApplication(
      makeVariable('createScreen'),
      [
        makeVariable('formattedLoanDetails'),
        makeVariable('processingTimeframe'),
        makeVariable('referenceNumber'),
        makeVariable('followUpMessage'),
        makeApplication(
          makeVariable('getLocalizedText'),
          [makeVariable('region'), 'success_screen']
        )
      ]
    )
  ])
);

// ===== Complete Journey Definition =====

const loanTopupJourney = {
  initialOfferScreen: renderInitialOfferScreen,
  amountSelectionScreen: renderAmountSelectionScreen,
  termsReviewScreen: renderTermsReviewScreen,
  confirmationScreen: renderConfirmationScreen,
  successScreen: renderSuccessScreen,
  
  // Business rules
  rules: [
    topupAmountLimitsRule,
    unchangedTermRule,
    interestRateRule, 
    promotionalOffersRule,
    processingTimeframeRule
  ],
  
  // Domain definitions
  regions: {
    UK: ukRegion,
    HK: hkRegion
  },
  
  customerSegments: {
    Basic: basicSegment,
    Wealth: wealthSegment
  },
  
  timePeriods: {
    standardHours,
    eveningHours,
    nightHours,
    weekendHours
  },
  
  promotions: {
    springHomePromoUK,
    lunarNewYearPromoHK
    // Additional promotions would be defined here
  }
};

// Export for use in the system
module.exports = loanTopupJourney;