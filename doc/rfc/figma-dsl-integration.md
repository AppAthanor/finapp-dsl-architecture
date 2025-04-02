# RFC: Integrating Figma Designs into Mobile DSL

## 1. Summary

This RFC proposes integrating Figma designs directly into our Mobile DSL through an automated pipeline that transforms Figma design files into Mobile DSL components and screens. This creates a streamlined design-to-development workflow, reduces manual coding errors, and ensures UI implementation matches designer intent across all 22 markets.

## 2. Motivation

Our current workflow has several friction points between design and implementation:

1. **Manual Translation** - Developers must manually interpret Figma designs and translate them into Mobile DSL code
2. **Design Drift** - Implemented UI often diverges from designer intent as changes are made
3. **Inefficient Iterations** - Design changes require manual updates to Mobile DSL files
4. **Limited Designer Input** - Designers have limited visibility into how their designs are implemented

By creating a direct Figma-to-DSL pipeline, we can address these challenges while improving development velocity and UI consistency.

## 3. Detailed Design

### 3.1 Overall Architecture

The proposed integration follows this workflow:

1. **Design in Figma** - Designers create UI components and screens using our design system components
2. **Export to JSON** - Figma designs are exported through Figma API as structured JSON 
3. **Transform to Mobile DSL** - A transformation tool converts Figma JSON to Mobile DSL syntax
4. **Enhance with Logic** - Developers add data bindings, validations, and event handlers
5. **Generate Code** - Final Mobile DSL is processed by our code generators

### 3.2 Figma Export Format

The Figma API provides a structured representation of design files:

```json
{
  "name": "AmountSelectionScreen",
  "type": "FRAME",
  "children": [
    {
      "name": "Header",
      "type": "FRAME",
      "children": [
        {
          "name": "Title",
          "type": "TEXT",
          "characters": "Select Top-up Amount",
          "style": {
            "fontFamily": "Roboto",
            "fontWeight": 600,
            "fontSize": 20
          }
        }
      ]
    },
    {
      "name": "AmountOptions",
      "type": "FRAME",
      "layoutMode": "VERTICAL",
      "children": [
        {
          "name": "AmountOptionCard1",
          "type": "INSTANCE",
          "componentId": "amount-card-component-id",
          "componentProperties": {
            "amount": "£5,000",
            "selected": "false"
          }
        }
      ]
    }
  ]
}
```

### 3.3 Transformed Mobile DSL Output

The exported Figma JSON would be transformed into Mobile DSL:

```
// Generated from Figma export of "AmountSelectionScreen"
screen AmountSelectionScreen {
  title: "Select Top-up Amount"
  
  layout: {
    type: "stack",
    spacing: 16,
    components: [
      {
        type: "header",
        title: bindText("select_topup_amount"),
        showBackButton: true
      },
      {
        type: "container",
        id: "amountOptions",
        layout: "vertical",
        spacing: 12,
        components: [
          {
            type: "amountOptionCard",
            id: "option1",
            amount: "£5,000", // Will be replaced with data binding
            selected: false
          }
        ]
      }
    ]
  }
  
  // Placeholders for developers to complete
  dataBindings: {
    // TODO: Add data bindings here
  }
}
```

### 3.4 Component Mapping System

A key part of this system is mapping Figma components to Mobile DSL components:

```
// Component mapping definition
componentMappings: {
  // Figma component ID to Mobile DSL component type
  "amount-card-component-id": {
    mobileComponent: "amountOptionCard",
    propertyMappings: {
      "amount": "amount",
      "selected": "selected"
    },
    requiredBindings: [
      {
        property: "amount",
        suggestedBinding: "formatCurrency(amount)"
      },
      {
        property: "selected",
        suggestedBinding: "selectedAmount === amount"
      }
    ]
  }
}
```

### 3.5 Visual Integration Tool

To make this workflow practical, we'll create a visual integration tool that:

1. **Displays Figma Designs** - Shows the original design for reference
2. **Presents Generated DSL** - Shows the auto-generated Mobile DSL code
3. **Provides Binding UI** - Allows adding data bindings through a visual interface
4. **Handles Logic Integration** - Connects UI elements to business rules
5. **Shows Live Preview** - Displays how the UI will look with sample data

### 3.6 Design System Enforcement

The integration will enforce our design system by:

1. Only allowing mapped components from our design system library
2. Validating design tokens (colors, spacing, typography)
3. Flagging components that don't match design system guidelines
4. Ensuring accessibility guidelines are followed

## 4. Example Workflow

### 4.1 Figma Component

Designers create a loan top-up amount selection screen in Figma using design system components.

### 4.2 Generated Mobile DSL (Initial)

```
screen AmountSelectionScreen {
  title: "Select Top-up Amount"
  
  layout: {
    type: "stack",
    spacing: 16,
    components: [
      {
        type: "amountOptionList",
        id: "amountOptions",
        options: [
          { amount: "£5,000", selected: false },
          { amount: "£10,000", selected: false },
          { amount: "£15,000", selected: false }
        ]
      },
      {
        type: "loanSummary",
        id: "summary",
        currentBalance: "£10,000",
        newBalance: "£20,000", // Static values from Figma
        monthlyPayment: "£250"
      }
    ]
  }
}
```

### 4.3 Developer-Enhanced Mobile DSL (Final)

```
screen AmountSelectionScreen {
  title: "Select Top-up Amount"
  
  // Import business rules from Business DSL
  import { 
    EligibilityRules,
    calculateNewMonthlyPayment 
  } from "./loan-topup.bizapp"
  
  dataBindings: {
    loan: "api.getLoan({ loanId: route.params.loanId })",
    offer: "api.getTopupOffer({ loanId: route.params.loanId })",
    suggestedAmounts: "computed((offer) => generateAmountOptions(offer.minAmount, offer.maxAmount))",
    selectedAmount: "state(null)",
    calculatedPayment: "computed((selectedAmount, loan) => calculatePayment(selectedAmount, loan))"
  }
  
  layout: {
    type: "stack",
    spacing: 16,
    components: [
      {
        type: "amountOptionList",
        id: "amountOptions",
        options: "bind:suggestedAmounts",
        selectedOption: "bind:selectedAmount",
        onSelect: "selectAmount"
      },
      {
        type: "loanSummary",
        id: "summary",
        currentBalance: "bind:formatCurrency(loan.currentBalance)",
        newBalance: "bind:formatCurrency(loan.currentBalance + selectedAmount)",
        monthlyPayment: "bind:formatCurrency(calculatedPayment.monthlyPayment)"
      }
    ]
  }
  
  validation: {
    selectedAmount: {
      rule: EligibilityRules.amountWithinLimits,
      onError: showErrorBanner(message)
    }
  }
}
```

## 5. Benefits

### 5.1 For Designers
- Direct path from design to implementation
- Confidence that UI will match design intent
- Ability to iterate quickly with immediate feedback
- Less time spent reviewing developer implementations

### 5.2 For Developers
- Less manual UI coding
- Focus on logic rather than layout
- Faster implementation of design changes
- Reduced risk of misinterpreting designs

### 5.3 For Product Quality
- Consistent UI across platforms
- Better adherence to design system
- Reduced UI bugs and inconsistencies
- Improved accessibility compliance

## 6. Technical Considerations

### 6.1 Figma API Limitations
- The Figma API has rate limits that need to be managed
- Some Figma features may not be directly mappable to Mobile DSL
- Plugin security needs careful consideration

### 6.2 Design System Requirements
- All Figma components must follow our design system guidelines
- Component naming conventions must be consistent
- Design tokens need to be properly defined

### 6.3 Change Management
- Need to handle conflicts between design and development changes
- Version tracking between Figma and Mobile DSL
- Clear ownership of different parts of the system

## 7. Conclusion

Integrating Figma designs directly into our Mobile DSL will significantly improve our design-to-development workflow, reduce manual errors, and ensure UI implementation matches designer intent.

We recommend approving this RFC to begin implementing the Figma-to-DSL integration pipeline.
