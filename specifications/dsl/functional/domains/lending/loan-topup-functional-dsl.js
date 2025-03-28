// Loan Top-up Functional DSL Implementation
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
const isVariable = exp => exp && exp.type === 'variable';
const isApplication = exp => exp && exp.type === 'application';
const isLambda = exp => exp && exp.type === 'lambda';
const isIf = exp => exp && exp.type === 'if';
const isAssignment = exp => exp && exp.type === 'assignment';
const isSequence = exp => exp && exp.type === 'sequence';
const isQuoted = exp => exp && exp.type === 'quoted';
const isSelfEvaluating = exp => typeof exp !== 'object' || exp === null;

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
  
  if (procedure && procedure.type === 'procedure') {
    const env = extendEnvironment(
      procedure.parameters,
      args,
      procedure.environment
    );
    return evaluate(procedure.body, env);
  }
  
  throw new Error(`Unknown procedure type: ${procedure}`);
};

// ===== Domain-Specific Constructors =====

// Region constructors and accessors
const makeRegion = (code, properties) => ({
  type: 'region',
  code,
  properties
});

const isRegion = exp => exp && exp.type === 'region';
const getRegionCode = region => region.code;
const getRegionCurrency = region => region.properties.currency;
const getRegionCurrencySymbol = region => region.properties.currencySymbol;
const getRegionDateFormat = region => region.properties.dateFormat;
const getRegionRegulatoryBody = region => region.properties.regulatoryBody;

// Customer segment constructors and accessors
const makeCustomerSegment = (code, properties) => ({
  type: 'customer_segment',
  code,
  properties
});

const isCustomerSegment = exp => exp && exp.type === 'customer_segment';
const getSegmentCode = segment => segment.code;
const getSegmentInterestRate = (segment, regionCode) => segment.properties.interestRates[regionCode];
const getSegmentMinTopupAmount = (segment, regionCode) => segment.properties.minTopupAmounts[regionCode];
const getSegmentMaxTopupAmount = (segment, regionCode) => segment.properties.maxTopupAmounts[regionCode];
const getSegmentBenefits = (segment, regionCode) => segment.properties.benefits?.[regionCode] || [];

// Time period constructors and accessors
const makeTimePeriod = (code, properties) => ({
  type: 'time_period',
  code,
  properties
});

const isTimePeriod = exp => exp && exp.type === 'time_period';
const getTimePeriodCode = period => period.code;
const getTimePeriodHours = (period, regionCode) => period.properties[regionCode];
const getTimePeriodProcessingTimeframe = period => period.properties.processingTimeframe;

// Promotion constructors and accessors
const makePromotion = (code, properties) => ({
  type: 'promotion',
  code,
  properties
});

const isPromotion = exp => exp && exp.type === 'promotion';
const getPromotionCode = promotion => promotion.code;
const getPromotionName = promotion => promotion.properties.name;
const getPromotionRegion = promotion => promotion.properties.region;
const getPromotionStartDate = promotion => promotion.properties.startDate;
const getPromotionEndDate = promotion => promotion.properties.endDate;
const getPromotionOffer = promotion => promotion.properties.offer;

// ===== Business Rule Constructors =====

// Business rule constructor
const makeBusinessRule = (id, condition, action, metadata) => ({
  type: 'business_rule',
  id,
  condition,
  action,
  metadata
});

// Apply a business rule
const applyBusinessRule = (rule, environment) => {
  const conditionResult = evaluate(rule.condition, environment);
  if (conditionResult) {
    return evaluate(rule.action, environment);
  }
  return null;
};

// ===== Domain-Specific Primitives =====

// Region definitions
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

// Customer segment definitions
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

// Time period definitions
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

// Promotion definitions
const springHomePromoUK = makePromotion('spring_home_uk', {
  name: 'Spring Home Improvement Promotion',
  region: 'UK',
  startDate: '2025-03-01',
  endDate: '2025-05-31',
  bannerKey: 'spring_home_banner',
  offer: 'No fee on home improvement loan top-ups'
});

const lunarNewYearPromoHK = makePromotion('lunar_new_year_hk', {
  name: 'Lunar New Year Special Offers',
  region: 'HK',
  startDate: '2025-01-15',
  endDate: '2025-02-28',
  bannerKey: 'lunar_new_year_banner',
  offer: 'Lucky rate reduction on loan top-ups'
});

const nightOwlPromoUK = makePromotion('night_owl_uk', {
  name: 'Night Owl Banking Benefits',
  region: 'UK',
  startDate: '2025-01-01',
  endDate: '2025-12-31',
  bannerKey: 'night_owl_banner',
  offer: 'Fee-free top-up processing',
  timeRestriction: 'night_hours'
});

const summerHolidayPromoUK = makePromotion('summer_holiday_uk', {
  name: 'Summer Holiday Financing Options',
  region: 'UK',
  startDate: '2025-05-01',
  endDate: '2025-07-31',
  bannerKey: 'summer_holiday_banner',
  offer: 'Deferred first payment on holiday loan top-ups'
});

const eveningBankingPromoHK = makePromotion('evening_banking_hk', {
  name: 'Evening Banking Exclusive Benefits',
  region: 'HK',
  startDate: '2025-01-01',
  endDate: '2025-12-31',
  bannerKey: 'evening_banking_banner',
  offer: 'Next-day processing guarantee',
  timeRestriction: 'evening_hours'
});

// ===== Business Rules =====

// Rule 1: Top-up amount limits by segment and region
const topupAmountLimitsRule = makeBusinessRule(
  'BR001',
  // Condition: Amount is within valid range for segment and region
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
              makeVariable('getSegmentMinTopupAmount'),
              [
                makeApplication(makeVariable('getCustomerSegment'), [makeVariable('customer')]), 
                makeApplication(makeVariable('getRegionCode'), [makeVariable('region')])
              ]
            )
          ]
        ),
        makeApplication(
          makeVariable('<='),
          [
            makeVariable('amount'),
            makeApplication(
              makeVariable('getSegmentMaxTopupAmount'),
              [
                makeApplication(makeVariable('getCustomerSegment'), [makeVariable('customer')]), 
                makeApplication(makeVariable('getRegionCode'), [makeVariable('region')])
              ]
            )
          ]
        )
      ]
    )
  ),
  // Action: Return validated amount or adjust if needed
  makeLambda(
    ['customer', 'region', 'amount'],
    makeIf(
      makeApplication(
        makeVariable('<'),
        [
          makeVariable('amount'),
          makeApplication(
            makeVariable('getSegmentMinTopupAmount'),
            [
              makeApplication(makeVariable('getCustomerSegment'), [makeVariable('customer')]), 
              makeApplication(makeVariable('getRegionCode'), [makeVariable('region')])
            ]
          )
        ]
      ),
      // If below minimum, return minimum
      makeApplication(
        makeVariable('getSegmentMinTopupAmount'),
        [
          makeApplication(makeVariable('getCustomerSegment'), [makeVariable('customer')]), 
          makeApplication(makeVariable('getRegionCode'), [makeVariable('region')])
        ]
      ),
      makeIf(
        makeApplication(
          makeVariable('>'),
          [
            makeVariable('amount'),
            makeApplication(
              makeVariable('getSegmentMaxTopupAmount'),
              [
                makeApplication(makeVariable('getCustomerSegment'), [makeVariable('customer')]), 
                makeApplication(makeVariable('getRegionCode'), [makeVariable('region')])
              ]
            )
          ]
        ),
        // If above maximum, return maximum
        makeApplication(
          makeVariable('getSegmentMaxTopupAmount'),
          [
            makeApplication(makeVariable('getCustomerSegment'), [makeVariable('customer')]), 
            makeApplication(makeVariable('getRegionCode'), [makeVariable('region')])
          ]
        ),
        // Otherwise return the amount (valid)
        makeVariable('amount')
      )
    )
  ),
  {
    description: 'Top-up amount must be between the minimum and maximum pre-approved limits for the customer\'s segment and region',
    rationale: 'Ensures the additional borrowing is within affordability parameters appropriate for customer segment and local market conditions',
    errorMessageKey: 'amount_limit_error'
  }
);

// Rule 2: Interest rate by customer segment
const interestRateRule = makeBusinessRule(
  'BR002',
  // Condition: Customer has a segment
  makeLambda(
    ['customer', 'region'],
    makeApplication(
      makeVariable('hasProperty'),
      [makeVariable('customer'), 'segment']
    )
  ),
  // Action: Calculate interest rate
  makeLambda(
    ['customer', 'region'],
          makeApplication(
      makeVariable('getSegmentInterestRate'),
      [
        makeApplication(makeVariable('getCustomerSegment'), [makeVariable('customer')]), 
        makeApplication(makeVariable('getRegionCode'), [makeVariable('region')])
      ]
    )
  ),
  {
    description: 'Interest rates must be determined based on customer segment and region',
    rationale: 'Different customer segments qualify for different rates in each region',
    errorMessageKey: 'interest_rate_calculation'
  }
);

// Rule 3: Promotional offers by date, region and time
const promotionalOffersRule = makeBusinessRule(
  'BR003',
  // Condition: Check if promotion is valid for date, region and time
  makeLambda(
    ['currentDateTime', 'region', 'promotion'],
    makeApplication(
      makeVariable('and'),
      [
        // Date range check
        makeApplication(
          makeVariable('>='),
          [makeVariable('currentDateTime'), makeApplication(makeVariable('parseDate'), [makeApplication(makeVariable('getPromotionStartDate'), [makeVariable('promotion')])])]
        ),
        makeApplication(
          makeVariable('<='),
          [makeVariable('currentDateTime'), makeApplication(makeVariable('parseDate'), [makeApplication(makeVariable('getPromotionEndDate'), [makeVariable('promotion')])])]
        ),
        // Region check
        makeApplication(
          makeVariable('==='),
          [
            makeApplication(makeVariable('getPromotionRegion'), [makeVariable('promotion')]), 
            makeApplication(makeVariable('getRegionCode'), [makeVariable('region')])
          ]
        ),
        // Time restriction check (if any)
        makeIf(
          makeApplication(
            makeVariable('hasProperty'),
            [makeVariable('promotion'), 'timeRestriction']
          ),
          makeApplication(
            makeVariable('isInTimePeriod'),
            [
              makeVariable('currentDateTime'),
              makeApplication(makeVariable('getProperty'), [makeVariable('promotion'), 'timeRestriction']),
              makeApplication(makeVariable('getRegionCode'), [makeVariable('region')])
            ]
          ),
          true // No time restriction, so always true
        )
      ]
    )
  ),
  // Action: Return promotion details for display
  makeLambda(
    ['currentDateTime', 'region', 'promotion'],
    makeQuoted({
      name: makeApplication(makeVariable('getPromotionName'), [makeVariable('promotion')]),
      offer: makeApplication(makeVariable('getPromotionOffer'), [makeVariable('promotion')]),
      bannerKey: makeApplication(makeVariable('getProperty'), [makeVariable('promotion'), 'bannerKey'])
    })
  ),
  {
    description: 'Promotional offers must be displayed based on current date, region, and time',
    rationale: 'Ensures time-sensitive offers are only displayed when valid in the correct region and time period',
    errorMessageKey: 'promotion_display'
  }
);

// Rule 4: Processing timeframe based on time and holidays
const processingTimeframeRule = makeBusinessRule(
  'BR004',
  // Condition: Always true (we always want to show processing timeframe)
  makeLambda(
    ['currentDateTime', 'region'],
    true
  ),
  // Action: Determine processing message
  makeLambda(
    ['currentDateTime', 'region'],
    makeIf(
      makeApplication(
        makeVariable('isHoliday'),
        [makeVariable('currentDateTime'), makeApplication(makeVariable('getRegionCode'), [makeVariable('region')])]
      ),
      makeQuoted({
        message: "Bank holiday processing notice",
        timeframe: "Next working day processing"
      }),
      makeIf(
        makeApplication(
          makeVariable('isInTimePeriod'),
          [
            makeVariable('currentDateTime'),
            'standard_hours',
            makeApplication(makeVariable('getRegionCode'), [makeVariable('region')])
          ]
        ),
        makeQuoted({
          message: "Immediate disbursement",
          timeframe: "Instant processing"
        }),
        makeIf(
          makeApplication(
            makeVariable('isInTimePeriod'),
            [
              makeVariable('currentDateTime'),
              'evening_hours',
              makeApplication(makeVariable('getRegionCode'), [makeVariable('region')])
            ]
          ),
          makeQuoted({
            message: "Immediate disbursement",
            timeframe: "Instant processing",
            notice: "Evening processing confirmation"
          }),
          makeQuoted({
            message: "Immediate disbursement",
            timeframe: "Instant processing",
            notice: "Night owl banking service active"
          })
        )
      )
    )
  ),
  {
    description: 'Processing timeframe information must be displayed based on current time and holidays',
    rationale: 'Sets appropriate expectations for processing times based on banking hours',
    errorMessageKey: 'processing_timeframe'
  }
);

// Rule 5: Segment benefits
const segmentBenefitsRule = makeBusinessRule(
  'BR005',
  // Condition: Customer has a segment with benefits
  makeLambda(
    ['customer', 'region'],
    makeApplication(
      makeVariable('hasSegmentBenefits'),
      [
        makeApplication(makeVariable('getCustomerSegment'), [makeVariable('customer')]), 
        makeApplication(makeVariable('getRegionCode'), [makeVariable('region')])
      ]
    )
  ),
  // Action: Return segment benefits
  makeLambda(
    ['customer', 'region'],
    makeApplication(
      makeVariable('getSegmentBenefits'),
      [
        makeApplication(makeVariable('getCustomerSegment'), [makeVariable('customer')]), 
        makeApplication(makeVariable('getRegionCode'), [makeVariable('region')])
      ]
    )
  ),
  {
    description: 'Customer benefits must be determined based on customer segment and region',
    rationale: 'Different customer segments qualify for different benefits in each region',
    errorMessageKey: 'segment_benefits'
  }
);

// ===== Journey Screen Definitions =====

// Initial offer screen
const renderInitialOfferScreen = makeLambda(
  ['customer', 'region', 'currentDateTime'],
  makeSequence([
    // Apply region-specific formatting
    makeAssignment(
      makeVariable('formattedLoanDetails'),
      makeApplication(
        makeVariable('formatLoanDetails'),
        [makeApplication(makeVariable('getCurrentLoan'), [makeVariable('customer')]), makeVariable('region')]
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
    
    // Get localized text
    makeAssignment(
      makeVariable('localizedText'),
      makeApplication(
        makeVariable('getLocalizedText'),
        [makeVariable('region'), 'initial_offer_screen']
      )
    ),
    
    // Return composed screen data
    makeQuoted({
      formattedLoanDetails: makeVariable('formattedLoanDetails'),
      timeBasedContent: makeVariable('timeBasedContent'),
      promotionalContent: makeVariable('promotionalContent'),
      localizedText: makeVariable('localizedText')
    })
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
        makeVariable('getSegmentMinTopupAmount'),
        [makeVariable('segment'), makeApplication(makeVariable('getRegionCode'), [makeVariable('region')])]
      )
    ),
    
    makeAssignment(
      makeVariable('maxAmount'),
      makeApplication(
        makeVariable('getSegmentMaxTopupAmount'),
        [makeVariable('segment'), makeApplication(makeVariable('getRegionCode'), [makeVariable('region')])]
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
    
    // Get special offers
    makeAssignment(
      makeVariable('specialOffers'),
      makeApplication(
        makeVariable('getSpecialOffersForTime'),
        [makeVariable('currentDateTime'), makeVariable('region')]
      )
    ),
    
    // Get localized text
    makeAssignment(
      makeVariable('localizedText'),
      makeApplication(
        makeVariable('getLocalizedText'),
        [makeVariable('region'), 'amount_selection_screen']
      )
    ),
    
    // Return composed screen data
    makeQuoted({
      amountOptions: makeVariable('amountOptions'),
      minAmount: makeVariable('minAmount'),
      maxAmount: makeVariable('maxAmount'),
      timeBasedContent: makeVariable('timeBasedContent'),
      specialOffers: makeVariable('specialOffers'),
      localizedText: makeVariable('localizedText')
    })
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
            makeVariable('getSegmentInterestRate'),
            [makeVariable('segment'), makeApplication(makeVariable('getRegionCode'), [makeVariable('region')])]
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
        [makeVariable('segment'), makeApplication(makeVariable('getRegionCode'), [makeVariable('region')])]
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
    
    // Get localized text
    makeAssignment(
      makeVariable('localizedText'),
      makeApplication(
        makeVariable('getLocalizedText'),
        [makeVariable('region'), 'terms_review_screen']
      )
    ),
    
    // Return composed screen data
    makeQuoted({
      formattedLoanDetails: makeVariable('formattedLoanDetails'),
      segmentBenefits: makeVariable('segmentBenefits'),
      regulatoryInfo: makeVariable('regulatoryInfo'),
      localizedText: makeVariable('localizedText')
    })
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
    
    // Get localized text
    makeAssignment(
      makeVariable('localizedText'),
      makeApplication(
        makeVariable('getLocalizedText'),
        [makeVariable('region'), 'confirmation_screen']
      )
    ),
    
    // Return composed screen data
    makeQuoted({
      formattedLoanDetails: makeVariable('formattedLoanDetails'),
      termsAndConditions: makeVariable('termsAndConditions'),
      processingTimeframe: makeVariable('processingTimeframe'),
      localizedText: makeVariable('localizedText'),
      regionSpecificDisclosure: makeApplication(
        makeVariable('getRegionDisclosure'),
        [makeVariable('region')]
      )
    })
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
          makeApplication(makeVariable('getRegionCode'), [makeVariable('region')])
        ]
      )
    ),
    
    // Get localized text
    makeAssignment(
      makeVariable('localizedText'),
      makeApplication(
        makeVariable('getLocalizedText'),
        [makeVariable('region'), 'success_screen']
      )
    ),
    
    // Return composed screen data
    makeQuoted({
      formattedLoanDetails: makeVariable('formattedLoanDetails'),
      processingTimeframe: makeVariable('processingTimeframe'),
      referenceNumber: makeVariable('referenceNumber'),
      followUpMessage: makeVariable('followUpMessage'),
      localizedText: makeVariable('localizedText'),
      disbursementTimeframe: makeApplication(
        makeVariable('getDisbursementTimeframe'),
        [makeVariable('currentDateTime'), makeVariable('region')]
      )
    })
  ])
);

// ===== Helper Functions for BDD Test Integration =====

// Create a global environment with primitive operations
const createGlobalEnvironment = () => {
  const env = { frame: {}, parent: null };
  
  // Add primitive operations
  defineVariable('getCustomerSegment', customerObj => {
    return customerObj.segment === 'Basic' ? basicSegment : wealthSegment;
  }, env);
  
  defineVariable('getCurrentLoan', customerObj => {
    return {
      id: 'LOAN123456',
      currentBalance: 10000,
      originalAmount: 15000,
      monthlyPayment: 250,
      remainingTerm: 48,
      interestRate: 5.9
    };
  }, env);
  
  defineVariable('formatLoanDetails', (loanDetails, region) => {
    const currencySymbol = getRegionCurrencySymbol(region);
    return {
      currentBalance: `${currencySymbol}${loanDetails.currentBalance.toLocaleString()}`,
      originalAmount: `${currencySymbol}${loanDetails.originalAmount.toLocaleString()}`,
      monthlyPayment: `${currencySymbol}${loanDetails.monthlyPayment.toLocaleString()}`,
      remainingTerm: `${loanDetails.remainingTerm} months`
    };
  }, env);
  
  defineVariable('getTimeBasedContent', (datetime, region) => {
    const hour = datetime.getHours();
    const regionCode = getRegionCode(region);
    
    if (hour >= 22 || hour < 8) {
      return { message: "Night owl banking active" };
    } else if (hour >= 17 && hour < 22) {
      return { message: "Evening processing confirmation" };
    } else {
      return { message: "Standard service hours" };
    }
  }, env);
  
  defineVariable('getApplicablePromotions', (datetime, region) => {
    const regionCode = getRegionCode(region);
    const allPromos = [springHomePromoUK, lunarNewYearPromoHK, nightOwlPromoUK, summerHolidayPromoUK, eveningBankingPromoHK];
    
    return allPromos.filter(promo => {
      // Only include promotions for this region
      if (getPromotionRegion(promo) !== regionCode) return false;
      
      // Check date range
      const startDate = new Date(getPromotionStartDate(promo));
      const endDate = new Date(getPromotionEndDate(promo));
      if (datetime < startDate || datetime > endDate) return false;
      
      // Check time restriction if any
      if (promo.properties.timeRestriction) {
        const hour = datetime.getHours();
        if (promo.properties.timeRestriction === 'night_hours' && (hour < 22 && hour >= 8)) return false;
        if (promo.properties.timeRestriction === 'evening_hours' && (hour < 17 || hour >= 22)) return false;
      }
      
      return true;
    }).map(promo => ({
      name: getPromotionName(promo),
      offer: getPromotionOffer(promo)
    }));
  }, env);
  
  defineVariable('getLocalizedText', (region, screenKey) => {
    const regionCode = getRegionCode(region);
    
    // Basic translations mapping
    const translations = {
      UK: {
        initial_offer_screen: {
          pre_approved_message: "You're Pre-approved for a Loan Top-up",
          cta_view_offer: "View Your Offer",
          cta_no_thanks: "No thanks, maybe later"
        },
        amount_selection_screen: {
          select_amount: "Select Amount",
          custom_amount_option: "Enter Custom Amount",
          continue: "Continue"
        },
        terms_review_screen: {
          continue: "Continue",
          accept: "Accept",
          go_back: "Go Back"
        },
        confirmation_screen: {
          confirm_topup: "Confirm Top-up",
          maybe_later: "Maybe Later",
          terms_agreement: "I have read and agree to the terms and conditions"
        },
        success_screen: {
          view_updated_loan: "View Updated Loan Details",
          return_to_account: "Return to Account Overview"
        }
      },
      HK: {
        initial_offer_screen: {
          pre_approved_message: "您已獲預先批核貸款增額",
          cta_view_offer: "查看優惠",
          cta_no_thanks: "暫時不需要，稍後再說"
        },
        amount_selection_screen: {
          select_amount: "選擇金額",
          custom_amount_option: "輸入自定金額",
          continue: "繼續"
        },
        terms_review_screen: {
          continue: "繼續",
          accept: "接受",
          go_back: "返回"
        },
        confirmation_screen: {
          confirm_topup: "確認增額",
          maybe_later: "稍後再說",
          terms_agreement: "我已閱讀並同意條款及細則"
        },
        success_screen: {
          view_updated_loan: "查看更新的貸款詳情",
          return_to_account: "返回戶口概覽"
        }
      }
    };
    
    return translations[regionCode][screenKey];
  }, env);
  
  defineVariable('generateAmountOptions', (minAmount, maxAmount, segment, region) => {
    const options = [];
    const step = (maxAmount - minAmount) / 4;
    
    for (let i = 0; i < 4; i++) {
      options.push(Math.round(minAmount + step * i));
    }
    options.push(maxAmount);
    
    return options;
  }, env);
  
  defineVariable('calculateNewLoanDetails', (currentLoan, topupAmount, interestRate) => {
    const newTotalBalance = currentLoan.currentBalance + topupAmount;
    const monthlyRate = interestRate / 100 / 12;
    const newMonthlyPayment = (newTotalBalance * monthlyRate) / (1 - Math.pow(1 + monthlyRate, -currentLoan.remainingTerm));
    const totalInterestPayable = (newMonthlyPayment * currentLoan.remainingTerm) - newTotalBalance;
    
    return {
      originalAmount: currentLoan.originalAmount,
      currentBalance: currentLoan.currentBalance,
      topupAmount: topupAmount,
      newTotalBalance: newTotalBalance,
      currentMonthlyPayment: currentLoan.monthlyPayment,
      newMonthlyPayment: newMonthlyPayment,
      remainingTerm: currentLoan.remainingTerm,
      interestRate: interestRate,
      totalInterestPayable: totalInterestPayable,
      totalRepayable: newTotalBalance + totalInterestPayable
    };
  }, env);
  
  defineVariable('getRegulatoryInformation', region => {
    const regionCode = getRegionCode(region);
    const regulatoryBody = getRegionRegulatoryBody(region);
    
    if (regionCode === 'UK') {
      return {
        regulatoryBody: "FCA",
        representativeAPR: "Representative 6.9% APR (variable)",
        coolingOffPeriod: "14 day cooling-off period",
        disclosure: "Credit reference agency notice and financial ombudsman details"
      };
    } else if (regionCode === 'HK') {
      return {
        regulatoryBody: "HKMA",
        representativeAPR: "Representative 7.2% APR (variable)",
        coolingOffPeriod: "14 day cooling-off period",
        disclosure: "HKMA regulatory notice and complaint procedure details"
      };
    }
  }, env);
  
  defineVariable('getTermsAndConditions', region => {
    const regionCode = getRegionCode(region);
    
    return {
      terms: `Standard terms and conditions for ${regionCode} region`,
      regulatoryDisclosures: `Regulatory disclosures for ${getRegionRegulatoryBody(region)}`
    };
  }, env);
  
  defineVariable('getProcessingTimeframe', (datetime, region) => {
    const regionCode = getRegionCode(region);
    const isHoliday = isHolidayDate(datetime, regionCode);
    
    if (isHoliday) {
      return {
        message: "Bank holiday processing notice",
        timeframe: "Next working day processing"
      };
    }
    
    const hour = datetime.getHours();
    
    return {
      message: "Immediate disbursement",
      timeframe: "Instant processing"
    };
  }, env);
  
  defineVariable('getFollowUpMessage', (segment, regionCode) => {
    const segmentCode = getSegmentCode(segment);
    
    if (segmentCode === 'Wealth' && regionCode === 'UK') {
      return "Your relationship manager will contact you within 3 days";
    } else if (segmentCode === 'Wealth' && regionCode === 'HK') {
      return "Your priority relationship manager will contact you within 2 days";
    } else {
      return "You'll receive a satisfaction survey in 7 days";
    }
  }, env);
  
  defineVariable('getRegionDisclosure', region => {
    const regionCode = getRegionCode(region);
    
    if (regionCode === 'UK') {
      return "Credit reference agency notice and financial ombudsman details";
    } else if (regionCode === 'HK') {
      return "HKMA regulatory notice and complaint procedure details";
    }
  }, env);
  
  defineVariable('getDisbursementTimeframe', (datetime, region) => {
    const regionCode = getRegionCode(region);
    const isHoliday = isHolidayDate(datetime, regionCode);
    
    if (isHoliday) {
      if (regionCode === 'UK') {
        return "Bank holiday processing notice";
      } else if (regionCode === 'HK' && isLunarNewYear(datetime)) {
        return "Lunar New Year processing notice";
      } else {
        return "Bank holiday processing notice";
      }
    } else {
      return "Immediate disbursement";
    }
  }, env);
  
  defineVariable('isHolidayDate', (datetime, regionCode) => {
    // Example holiday checks
    const date = datetime.getDate();
    const month = datetime.getMonth() + 1; // JavaScript months are 0-indexed
    
    if (regionCode === 'UK') {
      // UK Bank holidays (simplified for example)
      return (month === 12 && date === 25) || // Christmas
             (month === 12 && date === 26) || // Boxing Day
             (month === 1 && date === 1);     // New Year's Day
    } else if (regionCode === 'HK') {
      // HK holidays (simplified for example)
      return (month === 1 && date >= 25 && date <= 28) || // Lunar New Year (approximate)
             (month === 12 && date === 25);              // Christmas
    }
    
    return false;
  }, env);
  
  defineVariable('isLunarNewYear', datetime => {
    // Simplified check for Lunar New Year period (2025)
    const date = datetime.getDate();
    const month = datetime.getMonth() + 1;
    return (month === 1 && date >= 25 && date <= 28);
  }, env);
  
  defineVariable('parseDate', dateString => new Date(dateString), env);
  
  defineVariable('hasProperty', (obj, prop) => obj && obj.hasOwnProperty(prop), env);
  
  defineVariable('getProperty', (obj, prop) => obj && obj[prop], env);
  
  defineVariable('hasSegmentBenefits', (segment, regionCode) => {
    return segment && segment.properties && 
           segment.properties.benefits && 
           segment.properties.benefits[regionCode] && 
           segment.properties.benefits[regionCode].length > 0;
  }, env);
  
  defineVariable('getSpecialOffersForTime', (datetime, region) => {
    const regionCode = getRegionCode(region);
    const hour = datetime.getHours();
    
    if (regionCode === 'UK') {
      if (hour >= 22 || hour < 8) {
        return { special_offer: "Fee-free top-up processing" };
      } else if (month === 12 && date >= 24 && date <= 26) {
        return { special_offer: "Festive payment holiday option" };
      }
    } else if (regionCode === 'HK') {
      if (hour >= 17 && hour < 22) {
        return { special_offer: "Next-day processing guarantee" };
      }
    }
    
    return { special_offer: "None" };
  }, env);
  
  // Logical operators
  defineVariable('and', (...args) => args.every(Boolean), env);
  defineVariable('or', (...args) => args.some(Boolean), env);
  defineVariable('not', arg => !arg, env);
  
  // Comparison operators
  defineVariable('===', (a, b) => a === b, env);
  defineVariable('!==', (a, b) => a !== b, env);
  defineVariable('>', (a, b) => a > b, env);
  defineVariable('<', (a, b) => a < b, env);
  defineVariable('>=', (a, b) => a >= b, env);
  defineVariable('<=', (a, b) => a <= b, env);
  
  return env;
};

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
    interestRateRule, 
    promotionalOffersRule,
    processingTimeframeRule,
    segmentBenefitsRule
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
    lunarNewYearPromoHK,
    nightOwlPromoUK,
    summerHolidayPromoUK,
    eveningBankingPromoHK
  }
};

// Export for use in BDD tests
module.exports = {
  // Core DSL components
  makeVariable,
  makeApplication,
  makeLambda,
  makeIf,
  makeAssignment,
  makeSequence,
  makeQuoted,
  
  // Environment operations
  extendEnvironment,
  lookupVariableValue,
  defineVariable,
  setVariableValue,
  
  // Evaluation
  evaluate,
  createGlobalEnvironment,
  
  // Business domain
  regions: {
    UK: ukRegion,
    HK: hkRegion
  },
  
  customerSegments: {
    Basic: basicSegment,
    Wealth: wealthSegment
  },
  
  // Journey definition
  loanTopupJourney
};