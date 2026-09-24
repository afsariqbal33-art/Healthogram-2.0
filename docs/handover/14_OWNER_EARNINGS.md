# 14 — DOUBLE-ENTRY FINANCIAL LEDGER & OWNER EARNINGS

## 1. Mathematical Double-Entry Accounting
Every financial movement on the platform is logged as a balanced journal entry satisfying the invariant:
$$\sum \text{Debits} - \sum \text{Credits} = 0.00$$

### Standard Transaction Journal Schema
* **Customer Payment:**
  - `DEBIT:` Payment Processor Asset Account
  - `CREDIT:` Marketplace Escrow Liability Account
* **Commission & Fee Realization (Order Completion):**
  - `DEBIT:` Marketplace Escrow Liability Account
  - `CREDIT:` Seller Payable Account (e.g. 90%)
  - `CREDIT:` Owner Platform Revenue Account (e.g. 10%)

## 2. 7-Day Escrow Hold Policy
* **Settlement Protection:** Customer funds remain in the Escrow Liability account until order delivery confirmation + 7-day return inspection window expires.
* **Dispute Freezes:** Filing an order dispute or return request automatically freezes the escrow funds, preventing payout until resolution.

## 3. Owner Earnings Formula & Reconciliation
$$\text{Net Platform Revenue} = \text{Gross Sales} - \text{Seller Payouts} - \text{Gateway Processing Fees} - \text{Refunds} - \text{Statutory Taxes}$$
* **Audit Trail:** Manual adjustments to balances require 2FA, authorized Owner privileges, and mandatory audit log entries recording before/after states.
