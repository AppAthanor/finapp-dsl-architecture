# iOS View Controllers

This directory contains Swift view controllers generated from `.finapp` DSL specifications for the iOS platform.

## Controller Structure

Each view controller follows a consistent structure:

```
ViewControllerName/
├── ViewControllerName.swift        # Main controller implementation
├── ViewControllerName.storyboard   # UI layout (if applicable)
├── ViewControllerNameTests.swift   # Controller tests
└── Views/                          # Custom views for this controller
```

## Generated View Controllers

The view controllers are organized by feature:

- **Common/** - Reusable controllers
- **Screens/** - Main screen controllers
- **Modals/** - Modal presentation controllers
- **Flows/** - Navigation controllers for multi-step workflows

## Example Usage

```swift
import UIKit
import FinAppCore

class LoanDashboardViewController: UIViewController {
    
    @IBOutlet weak var topupButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
    }
    
    @IBAction func onTopupButtonTapped(_ sender: Any) {
        let topupVC = TopupAmountSelectionViewController(loanId: "LOAN123", offerId: "OFFER456")
        topupVC.delegate = self
        navigationController?.pushViewController(topupVC, animated: true)
    }
}

extension LoanDashboardViewController: TopupAmountSelectionDelegate {
    func didSelectAmount(_ amount: Decimal) {
        print("Amount selected: \(amount)")
    }
}
```

## Development Notes

These view controllers are automatically generated. Do not modify them directly.
If you need to change functionality, modify the source `.finapp` specification files instead. 