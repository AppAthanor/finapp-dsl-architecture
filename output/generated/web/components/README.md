# Web Components

This directory contains React components generated from `.finapp` DSL specifications for the web platform.

## Component Structure

Each component follows a consistent structure:

```
ComponentName/
├── ComponentName.tsx       # Main component implementation
├── ComponentName.module.css # Component-specific styles
├── ComponentName.test.tsx  # Component tests
└── index.ts               # Exports
```

## Generated Components

The components are organized by feature:

- **common/** - Reusable UI components
- **screens/** - Full screen components
- **forms/** - Form components
- **modals/** - Modal dialog components

## Example Usage

```tsx
import { TopupAmountSelection } from '@finapp/components';

function LoanTopupFlow() {
  return (
    <div className="loan-topup-container">
      <TopupAmountSelection 
        loanId="LOAN123" 
        offerId="OFFER456"
        onAmountSelected={(amount) => console.log(`Amount selected: ${amount}`)}
      />
    </div>
  );
}
```

## Development Notes

These components are automatically generated. Do not modify them directly.
If you need to change functionality, modify the source `.finapp` specification files instead. 