# Healthogram 2.2 — Financial Architecture & Ledger Specification

**Document:** `docs/finance/HEALTHOGRAM_2_2_FINANCIAL_ARCHITECTURE.md`  
**System:** Healthogram Financial Ledger Subsystem 2.2  
**Target Release:** Healthogram 2.2.0 (Build 20200)  
**Authority:** Chief Financial Officer (CFO), Lead Financial Engineer & Principal Architect  
**Classification:** RESTRICTED FINANCIAL SPECIFICATION  

---

## 1. Core Financial Principles & Invariants

Healthogram 2.2 operates under four non-negotiable financial mandates:
1. **Zero Client Balance Mutation**: Mobile clients have zero authority to modify account balances or commission rates. All balance updates occur strictly within server-side Cloud Functions.
2. **Strict Minor-Unit Integer Arithmetic**: No floating-point math is ever used. All transactions are calculated in integer minor units (e.g. 1 OMR = 1000 Baisa; 1 SAR = 100 Halalas; 1 USD = 100 Cents).
3. **Double-Entry Bookkeeping**: Every debit must have an equal and opposing credit across system accounts. The global ledger balance across all accounts must strictly equal zero:
   $$\sum \text{Debits} + \sum \text{Credits} = 0$$
4. **Idempotent Webhook Processing**: Payment gateway webhooks must include an idempotency key and HMAC-SHA256 signature verification. Replayed webhooks are acknowledged without duplicate ledger postings.

---

## 2. Multi-Currency Support Matrix

| Currency Code | Country | Minor Unit Name | Minor Unit Multiplier | Primary Payment Gateway |
| :--- | :--- | :--- | :--- | :--- |
| **`OMR`** | Sultanate of Oman | Baisa | 1,000 ($10^3$) | Thawani / Bank Muscat PG |
| **`SAR`** | Kingdom of Saudi Arabia | Halala | 100 ($10^2$) | PayTabs / Mada |
| **`AED`** | United Arab Emirates | Fils | 100 ($10^2$) | Stripe UAE / Network Intl |
| **`USD`** | United States / Global | Cent | 100 ($10^2$) | Stripe US |

---

## 3. Double-Entry Journal Chart of Accounts

```
  ASSETS:
  ├── 1010: Cash at Payment Gateway (Escrow / Pending Settlement)
  └── 1020: Cash at Operating Bank Account

  LIABILITIES:
  ├── 2010: Customer Escrow Balances (Pending Delivery)
  ├── 2020: Verified Seller Pending Payout Balances
  └── 2030: Doctor / Clinic Consultation Escrow

  EQUITY & REVENUE:
  ├── 3010: Healthogram Platform Marketplace Commission Revenue
  ├── 3020: Healthogram Telehealth Booking Commission Revenue
  └── 3030: Payment Gateway Processing Fee Expense
```

---

## 4. Transaction State Machines & Lifecycle

### 4.1. Marketplace Order Flow
1. **Checkout**: Customer authorizes payment -> Gateway webhook fires -> Healthogram posts journal entry:
   - `DEBIT 1010 (Gateway Cash)`
   - `CREDIT 2010 (Customer Escrow)`
2. **Fulfillment**: Seller ships goods -> Courier delivers with customer OTP code -> Order status `DELIVERED`.
3. **Escrow Hold**: 7-day return inspection window begins.
4. **Settlement**: 7-day window expires cleanly -> Healthogram releases escrow:
   - `DEBIT 2010 (Customer Escrow)`
   - `CREDIT 2020 (Seller Payout Balance)` [Order Total - 5% Platform Fee]
   - `CREDIT 3010 (Platform Commission)` [5% Platform Fee]

### 4.2. Telehealth Consultation Flow
1. **Booking**: Patient books consultation slot -> Payment authorized:
   - `DEBIT 1010 (Gateway Cash)`
   - `CREDIT 2030 (Doctor Consultation Escrow)`
2. **Encounter Completed**: Doctor and patient complete call -> Doctor submits intake note:
   - `DEBIT 2030 (Doctor Escrow)`
   - `CREDIT 2020 (Doctor Payout Balance)` [Consultation Fee - 10% Platform Fee]
   - `CREDIT 3020 (Telehealth Commission)` [10% Platform Fee]
3. **Cancellation by Patient (> 24h notice)**: Full refund returned to patient card via gateway API; zero fee charged.

---

## 5. Owner Earnings Governance & Payout Authorization

- **Platform Earnings Separation**: Platform fee revenue (`3010`, `3020`) accumulates in a dedicated corporate account isolated from user escrow funds.
- **Hardware-Backed Payout Authorization**:
  - Payout batches to sellers and owner dividend distributions require the Master Owner to enter a hardware-backed physical PIN and confirm biometric authentication.
  - Payouts above $5,000 USD equivalent require dual-authorization (Owner + CFO).
- **Anti-Money Laundering (AML) Velocity Safeguards**:
  - Any single account attempting to withdraw > $10,000 USD in a 24-hour window triggers an automated compliance hold pending manual bank statement verification.
