# HEALTHOGRAM — FINANCIAL LEDGER & DAILY RECONCILIATION RUNBOOK

**Target Version:** 1.0.0 (Production)  
**Classification:** Financial Operations, Escrow & Compliance  
**Audience:** Finance Admin, Platform Auditor, Financial Controller  

---

## 1. Accounting Formula & Balancing Equations

Healthogram implements strict double-entry ledger bookkeeping. Every transaction is governed by the invariant formula:

$$\text{Gross Customer Payment} = \text{Platform Commission} + \text{Seller Net Payout} + \text{Gateway Processing Fee} + \text{Sales/VAT Taxes}$$

### Balancing Checks
1. **Escrow Balance Invariant:**
   $$\text{Total Escrow Funds in Bank} = \sum \text{Undelivered Order Balances} + \sum \text{Pending Dispute Reserves}$$
2. **Owner Net Revenue:**
   $$\text{Owner Net Earnings} = \text{Gross Commissions} - \text{Refund Deductions} - \text{Chargeback Losses} - \text{Gateway Costs}$$
3. **Seller Payout Invariant:**
   $$\text{Seller Disbursable Balance} = \sum \text{Delivered Orders (Past Return Window)} - \sum \text{Previous Payouts}$$

---

## 2. Automated Daily Reconciliation Pipeline

At 00:05 UTC every night, the automated Cloud Function `reconcileDailyFinancialLedger` executes:
1. **Pulls Settlement Reports:** Fetches all successful charge and refund intents from Stripe API for the preceding 24-hour cycle.
2. **Cross-References Firestore Orders:** Matches each Stripe `payment_intent_id` against Firestore `orders/{orderId}`.
3. **Verifies Discrepancies:**
   - Detects orphan charges (charge succeeded on gateway but order missing in Firestore).
   - Detects orphan orders (order created in Firestore without matching gateway capture).
   - Detects fee calculation mismatches (> $0.01 tolerance).
4. **Writes Summary Record:** Writes an immutable summary document to `financial_reconciliation_daily/{date}`:
   ```json
   {
     "reconciliation_date": "2026-09-16",
     "total_orders_count": 1420,
     "gross_volume_cents": 4260000,
     "platform_fees_cents": 426000,
     "seller_payouts_cents": 3611000,
     "gateway_fees_cents": 127800,
     "taxes_cents": 95200,
     "discrepancy_cents": 0,
     "status": "BALANCED",
     "reconciled_at": "2026-09-17T00:05:12Z"
   }
   ```

---

## 3. Discrepancy Resolution Protocol

If `status == "UNBALANCED"`:
1. PagerDuty alert fires to `#financial-alerts`.
2. Finance Admin opens Google Cloud BigQuery / Firestore ledger logs.
3. Common Root Causes:
   - **Partial Refund Timing:** Customer initiated return at 23:59 UTC; gateway processed at 00:01 UTC. Resolution: Automatically resolves in next cycle.
   - **Chargeback Initiated:** Customer bank raised chargeback. Resolution: Escrow moves order amount from Seller Pending to Chargeback Reserve.
   - **Gateway Webhook Retrying:** Webhook arrived with exponential backoff delay. Resolution: Replay webhook via Stripe Dashboard.
4. **Sign-Off:** Finance Admin signs off on daily reconciliation report before seller payouts are released at 10:00 UTC.
