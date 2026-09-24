# ADR-003: IMMUTABLE DOUBLE-ENTRY FINANCIAL ACCOUNTING & ESCROW CUSTODY

**Status:** ACCEPTED  
**Date:** 2026-09-17  
**Deciders:** FinTech Systems Architect, Backend Architect, Platform Owner  

---

## 1. Context
Healthogram processes multi-seller e-commerce orders, courier delivery fees, payment gateway interchange costs, dispute refunds, and platform commissions. Financial discrepancies, balance race conditions, or lost funds directly threaten platform solvency and legal compliance.

## 2. Problem
How should monetary transactions, escrow holdings, and payouts be represented and executed across multiple parties (Customer, Seller, Courier, Platform Owner) to ensure absolute auditability and zero drift?

## 3. Options Considered
- **Option A: Simple Balance Mutation:** Maintain a mutable `balance` field on seller and user accounts and mutate via `balance += amount`.
- **Option B: Event-Sourced Accounting Engine:** Complex event sourcing with dedicated Kafka event log and CQRS projections.
- **Option C: Server-Side Double-Entry Ledger with Idempotency Locks (Selected):** Every monetary event creates two offsetting immutable ledger rows (Debit and Credit). Balances are calculated by aggregating ledger entries. All mutations occur strictly within Cloud Functions with unique idempotency keys.

## 4. Decision
Adopt **Option C: Server-Side Double-Entry Ledger with Idempotency Locks**. Direct client writes to balances or order totals are strictly prohibited (`allow write: if false;`).

## 5. Reason
Double-entry bookkeeping is the universal standard for financial integrity. It guarantees that money cannot be created or destroyed within the platform. If a bug occurs, the full audit trail remains intact, allowing exact point-in-time reconciliation.

## 6. Tradeoffs
- Requires more write operations per transaction (minimum 2 ledger entries per order).
- Calculating account balances requires either summing entries or maintaining audited periodic checkpoint snapshots.

## 7. Consequences
- Client applications can NEVER directly modify account balances.
- Stripe payment webhooks are protected against duplicate execution via cryptographic idempotency locks.
- Automated daily reconciliation runs verify that platform gross debits minus credits equals exactly $0.00.
