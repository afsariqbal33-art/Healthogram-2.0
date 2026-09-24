# ADR-017: ASYNCHRONOUS FINANCIAL RECONCILIATION & DISPUTE AUDITING

**Status:** ACCEPTED  
**Date:** 2026-09-17  
**Deciders:** FinTech Systems Architect, Platform Owner, Senior Security Engineer  

---

## 1. Context
Financial integrity across customer orders, marketplace seller disbursements, platform commissions, courier fees, and payment gateway chargebacks requires continuous automated verification against bank and gateway settlements.

## 2. Problem
How do we ensure that ledger discrepancies, missing webhook captures, or rounding drifts are detected and escalated without halting active real-time checkout processing?

## 3. Options Considered
- **Option A: Synchronous Multi-Party Cross-Verification:** Run full ledger reconciliation checks synchronously on every checkout.
- **Option B: Manual Monthly Spreadsheet Audit:** Export CSV reports and reconcile transactions manually.
- **Option C: Scheduled Asynchronous Reconciliation Workers (Selected):** Automated scheduled Cloud Task workers run periodic comparisons between internal `financial_ledger_entries`, Stripe gateway events, order fulfillment statuses, and seller payout ledgers. Discrepancies generate immutable records in `payment_reconciliation_cases`.

## 4. Decision
Adopt **Option C: Scheduled Asynchronous Reconciliation Workers**. Owner revenue calculations derive exclusively from posted, reconciled double-entry records.

## 5. Reason
Decouples checkout throughput from complex financial audits while maintaining 100% financial observability and automated alert triggers.

## 6. Tradeoffs & Consequences
- Owner revenue metrics reflect settled/posted transactions rather than raw UI intent states.
- Detected anomalies generate high-priority alerts for the platform Owner.
