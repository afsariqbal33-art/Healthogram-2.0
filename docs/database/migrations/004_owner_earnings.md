# DATABASE MIGRATION 004: FINANCIAL LEDGER & OWNER EARNINGS SCHEMA

**Migration ID:** `004_owner_earnings`  
**Target Environment:** Development, Staging, Production  
**Author:** Healthogram Financial Systems Architect  

---

## 1. Overview
Establishes double-entry immutable financial ledger tracking for marketplace commissions, seller settlements, owner payouts, and gateway reconciliation with zero drift.

## 2. Collections Created
* `/financial_ledger/{entryId}`: Immutable debit/credit entries for all monetary events (`order_payment`, `seller_payout`, `platform_commission`, `refund`).
* `/settlement_records/{settlementId}`: Monthly seller payout calculation records.
* `/payout_requests/{payoutId}`: Owner withdrawal requests with mandatory 2FA confirmation flags.
* `/idempotency_locks/{idempotencyKey}`: Unique transaction locks preventing duplicate webhook processing.

## 3. Backward Compatibility & Rollback Plan
* **Backward Compatibility**: Client write is strictly forbidden (`allow write: if false`). Server-side function updates only.
* **Rollback Procedure**: Ledger is append-only; reversals must be posted as offsetting adjustment entries.
