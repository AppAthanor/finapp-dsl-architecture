# RFC: Error Handling Framework for Banking App DSL

## 1. Summary

This RFC proposes a Centralised Error Hub approach for explicit error handling within our Banking App DSL architecture, addressing the current gap between the `.finapp` (WHAT) and Clojure implementation (HOW) files. The proposal provides a structured way to define errors, determine ownership, and specify handling behaviours while maintaining our architecture's separation of concerns.

## 2. Motivation

Our current DSL implementation lacks explicit error handling capabilities, which leads to several issues:

1. **Unclear Responsibility** - There's no formal way to specify whether the journey or platform handles specific errors
2. **Implicit Error Logic** - Error handling is often embedded within general flow logic, making it difficult to identify and validate
3. **Inconsistent Experience** - Without standardised error patterns, users experience inconsistent error handling across journeys
4. **Development Challenges** - Teams implement ad-hoc error handling solutions, increasing maintenance complexity

The proposed framework addresses these issues by providing explicit, declarative error handling capabilities within our existing DSL structure.

## 3. Detailed Design

### 3.1 Core Components

The proposed error handling framework consists of four key components:

#### 3.1.1 Error Definition

Errors are defined globally to ensure consistency across the application:

```
errors {
  // System-defined error categories
  category NetworkError {
    codes: [408, 502, 503, 504]
    severity: high
    retryable: true
  }
  
  category ValidationError {
    codes: [400, 422]
    severity: medium
    retryable: false
  }
  
  // Journey-specific error definitions
  error InsufficientFunds extends ValidationError {
    code: "PAYMENT_001"
    message: "Insufficient funds available"
  }
  
  error PaymentLimitExceeded extends ValidationError {
    code: "PAYMENT_002"
    message: "This payment exceeds your daily limit"
  }
}
```

#### 3.1.2 Centralised Error Hub

Each journey contains a centralised error hub that defines:
1. Which errors the journey owns vs delegates to the platform
2. How owned errors should be handled

```
journey PaymentJourney {
  // Journey definition
  // ...
  
  errorHub {
    // Define which errors this journey owns vs delegates to platform
    ownership {
      owns: [InsufficientFunds, PaymentLimitExceeded]
      delegates: [NetworkError, AuthenticationError]
    }
    
    // Define handlers for owned errors
    handlers {
      InsufficientFunds: {
        display: ErrorBanner
        location: "PaymentScreen"
        properties: {
          message: error.message
          actionText: "Check balance"
          action: navigate(AccountDetailsScreen)
        }
        recovery: {
          allowRetry: true
          alternativeFlow: navigate(AccountListScreen)
        }
      }
      
      PaymentLimitExceeded: {
        display: ModalDialog
        location: "PaymentScreen"
        properties: {
          title: "Daily limit reached"
          message: error.message
          primaryAction: "View limits"
          secondaryAction: "Go back"
        }
        recovery: {
          allowRetry: false
          alternativeFlow: navigate(LimitsScreen)
        }
      }
    }
  }
}
```

#### 3.1.3 Platform-Level Default Handlers

The platform defines default handlers for delegated errors:

```
platform {
  errorHub {
    defaultHandlers {
      NetworkError: {
        display: RetryDialog
        properties: {
          title: "Connection issue"
          message: "We're having trouble connecting. Please try again."
          retryAction: "Retry"
          cancelAction: "Cancel"
        }
        recovery: {
          retryStrategy: exponentialBackoff(3, 1000)
          cancelFlow: navigateBack()
        }
      }
      
      AuthenticationError: {
        display: SecurityDialog
        properties: {
          message: "Please log in again to continue"
          action: "Log in"
        }
        recovery: {
          preserveState: true
          flow: navigate(LoginScreen)
        }
      }
    }
  }
}
```

#### 3.1.4 Error Mapping from APIs

API definitions include error mapping to translate HTTP/backend errors to domain errors:

```
api PaymentService {
  endpoint: "/api/payments"
  
  operation createPayment {
    method: POST
    // Operation definition
    // ...
    
    errorMapping {
      400: {
        "INSUFFICIENT_BALANCE" -> InsufficientFunds
        "DAILY_LIMIT_EXCEEDED" -> PaymentLimitExceeded
      }
      401: AuthenticationError
      403: AuthorisationError
      5xx: NetworkError
    }
  }
}
```

### 3.2 Error Flow Resolution

When an error occurs, the system follows this resolution flow:

1. Map raw error (e.g., HTTP 400) to domain error (e.g., `InsufficientFunds`)
2. Determine ownership of the error:
   - If current journey owns the error, use journey's handler
   - If delegated to platform, use platform's default handler
   - If neither exists, fall back to system default
3. Render the appropriate error component in the specified location
4. Apply recovery strategy as defined in the handler

### 3.3 Component Library

To support this framework, we'll extend our component library with error-specific components:

```
components {
  // Error components
  component ErrorBanner {
    type: "banner"
    properties: {
      message: string
      actionText: string?
      action: action?
      variant: "error" | "warning" = "error"
    }
  }
  
  component ModalDialog {
    type: "modal"
    properties: {
      title: string
      message: string
      primaryAction: string
      secondaryAction: string?
      onPrimary: action
      onSecondary: action?
    }
  }
  
  component RetryDialog {
    type: "modal"
    properties: {
      title: string
      message: string
      retryAction: string
      cancelAction: string
      onRetry: action
      onCancel: action
    }
  }
}
```

## 4. Implementation Considerations

### 4.1 For Enterprise Context

The centralised error hub approach is designed with enterprise considerations in mind:

1. **Clear Structure for Offshore Teams**
   - Well-defined patterns for error handling
   - Reduced cognitive load through separation of normal flow and error handling
   - Easier to validate and review

2. **Tooling Opportunities**
   - Visual error hub builder tool for less technical team members
   - Validation rules to ensure all errors have appropriate handlers
   - Code generation for implementation teams

3. **Gradual Adoption**
   - Can be implemented incrementally across journeys
   - Compatible with existing journey implementations
   - Provides immediate value without requiring complete refactoring

### 4.2 Technical Implementation

The implementation in Clojure would involve:

1. **Error Registry**
   - Central registry of defined errors and their properties
   - Mapping functions for API responses to domain errors

2. **Error Resolution Engine**
   - Ownership resolution logic
   - Handler lookup and execution
   - Default fallback chain

3. **Component Rendering**
   - Error component instantiation
   - Context-aware error rendering
   - State preservation for recovery

4. **Recovery Strategies**
   - Retry mechanism implementation
   - State management for interrupted flows
   - Navigation handling for alternative flows

## 5. Alternative Approaches Considered

### 5.1 Annotation-based Approach

```
component PayButton {
  // Component definition
  type: "button"
  text: "Pay Now"
  
  // Inline error annotations
  @onError(InsufficientFunds, show: "ErrorBanner")
  @onError(NetworkError, retry: true)
  action: submitPayment()
}
```

**Pros**:
- Error handling is co-located with the affected component
- More explicit about where errors appear in the UI

**Cons**:
- Scattered error handling logic makes it harder to review
- Increases complexity of component definitions
- More difficult for less experienced developers to maintain consistency

### 5.2 Event-based Error Handling

```
events {
  ErrorOccurred: {
    payload: {
      code: string
      message: string
      context: object
    }
  }
}

screen PaymentScreen {
  // Screen definition
  // ...
  
  subscription(ErrorOccurred) {
    when: event.code == "PAYMENT_001"
    action: showErrorBanner(event.message)
  }
}
```

**Pros**:
- Leverages existing event system
- Flexible handling of errors across components

**Cons**:
- Less explicit ownership of error handling
- Can introduce timing and race condition issues
- Harder to follow error flow

### 5.3 Centralised Error Hub (Selected Approach)

As detailed in section 3.1.2.

**Pros**:
- Clear ownership and responsibility for error handling
- Single location to review all error cases for a journey
- Structured format suitable for enterprise development
- Easier to implement standardised tooling around

**Cons**:
- Separation between error definition and UI components that display them
- Requires careful documentation of location references
- Additional syntax to learn for developers

## 6. Implementation Plan

### 6.1 Phase 1: Foundation (2-3 months)

1. **DSL Extension**
   - Extend `.finapp` grammar to support error definitions
   - Implement centralised error hub syntax
   - Create platform-level error handler definitions

2. **Core Implementation**
   - Develop error registry and resolution engine
   - Implement basic error mapping from API responses
   - Create foundational error components

3. **Documentation & Standards**
   - Define error handling patterns and best practices
   - Document error definition process
   - Create error taxonomy for banking domain

### 6.2 Phase 2: Tooling & Migration (2-3 months)

1. **Development Tools**
   - Implement validation for error handling completeness
   - Create visual error hub builder
   - Develop testing framework for error scenarios

2. **Journey Migration**
   - Select pilot journey for implementation
   - Define migration process for existing journeys
   - Create migration tools for semi-automated conversion

3. **Error Analytics**
   - Implement error tracking and reporting
   - Create dashboards for error frequency and impact
   - Set up alerting for critical error patterns

### 6.3 Phase 3: Enterprise Scale (3-4 months)

1. **Advanced Features**
   - Implement contextual error handling
   - Add support for multi-step recovery strategies
   - Create market-specific error handling variations

2. **Training & Enablement**
   - Develop training materials for offshore teams
   - Create pattern library with examples
   - Establish error handling review process

3. **Optimisation**
   - Performance tuning for error resolution
   - Reduce runtime overhead
   - Implement efficient state preservation

## 7. Impact Assessment

### 7.1 Benefits

1. **Improved User Experience**
   - Consistent error handling across the application
   - More informative error messages
   - Clear recovery paths for users

2. **Development Efficiency**
   - Standardised approach reduces implementation time
   - Clearer responsibility between journey and platform
   - Better testability of error scenarios

3. **Operational Insights**
   - Better visibility into error frequency and patterns
   - Clearer understanding of user friction points
   - Data-driven improvements to error-prone areas

### 7.2 Risks

1. **Learning Curve**
   - New syntax and patterns for teams to learn
   - Initial productivity impact during adoption

2. **Migration Complexity**
   - Existing journeys require updates for consistency
   - Potential for mixed implementations during transition

3. **Performance Considerations**
   - Additional runtime resolution for error handling
   - State preservation overhead for recovery flows

## 8. Conclusion

The proposed Centralised Error Hub framework provides a structured, maintainable approach to error handling within our Banking App DSL. It addresses the current gap in explicitly handling errors while maintaining our architecture's separation of concerns between `.finapp` (WHAT) and Clojure (HOW) files.

The framework offers clear error ownership, standardised handling patterns, and recovery strategies while remaining feasible to implement in our enterprise context with offshore teams of varying skill levels.

By adopting this approach, we can improve user experience, increase development efficiency, and gain better operational insights while providing a path for incremental adoption across our application portfolio.

## 9. References

1. Banking App DSL Documentation: https://appathanor.github.io/finapp-dsl-architecture/doc/guides/architecture.html
2. Functional DSL Architecture Document
3. Clojure Implementation Guide
4. Financial Domain Error Patterns 