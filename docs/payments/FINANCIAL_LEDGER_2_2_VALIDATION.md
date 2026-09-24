# Healthogram Financial Ledger 2.2 Validation Specification

**Document Version:** 2.2.0  
**Phase:** Step 39 — Double-Entry Accounting, Immutability & Audit Trails  
**Classification:** Proprietary / Engineering Specification  

---

## 1. Core Double-Entry Architecture

Healthogram maintains an immutable, double-entry financial ledger (`FinancialLedgerEngine`) ensuring mathematical consistency and regulatory auditability across all marketplace transactions.

### 1.1 Fundamental Double-Entry Invariance
For every commercial event:
$$\sum \text{Debits} = \sum \text{Credits}$$

Floating-point imprecision is strictly avoided by either minor unit integers (halalas/cents) or standardized double precision rounding to 4 decimal places with exact balancing.

---

## 2. Standard Transaction Posting Matrix

When a customer pays for an order ($Total = \text{ProductSubtotal} + \text{DeliveryFee} + \text{PlatformFee} + \text{VAT}$):

| Account | Account Type | Direction | Amount | Purpose |
|---|---|---|---|---|
| `acc_cust_pmt_{country}` | `CUSTOMER_PAYMENT` | **DEBIT** | $Total$ | Cash / Gateway receivable received |
| `acc_seller_payable_{sellerId}` | `SELLER_PAYABLE` | **CREDIT** | $ProductSubtotal - Commission$ | Obligation owed to merchant |
| `acc_platform_revenue_{country}`| `PLATFORM_REVENUE` | **CREDIT** | $Commission$ | Healthogram marketplace commission |
| `acc_platform_fees_{country}`   | `PLATFORM_FEES` | **CREDIT** | $PlatformFee$ | Consumer service fee |
| `acc_delivery_payable_{country}`| `DELIVERY_PAYABLE` | **CREDIT** | $DeliveryFee$ | Obligation owed to delivery carrier |
| `acc_tax_payable_{country}`     | `TAX_PAYABLE` | **CREDIT** | $VAT$ | Accrued VAT/sales tax owed to government |

---

## 3. Immutability & Compensating Corrections

1. **Non-Destructive Ledger**:
   - Ledger entries (`FinancialLedgerEntry`) are strictly append-only.
   - Updates (`UPDATE`) and deletions (`DELETE`) on ledger tables and collections are permanently prohibited at database and Firestore rule levels.
2. **Compensating Transactions**:
   - Any financial adjustment (refund, return, dispute settlement, bank reversal) is posted as a new balancing compensating entry.
   - Example Refund: Debits `acc_seller_payable` and `acc_platform_revenue`, and Credits `acc_cust_pmt` (or customer wallet).

---

## 4. Multi-Tier Commission Engine

The ledger engine supports dynamic, country-specific, and category-specific commission calculations:
- **Percentage**: Standard rate (e.g., 8% for medical equipment, 12% for diagnostic supplies).
- **Fixed Fee**: Flat charge per transaction (e.g., 2.00 SAR).
- **Hybrid**: Percentage plus fixed fee with configurable minimum and maximum caps.

```kotlin
val rawCommission = when (rule.commissionType) {
    CommissionType.FIXED -> fixedFee
    CommissionType.PERCENTAGE_PLUS_FIXED -> (subtotal * rate) + fixedFee
    else -> subtotal * rate
}
val effectiveCommission = rawCommission.coerceIn(minFee, maxFee)
```

---

## 5. Automated Validation & Test Coverage

Validated in `MarketplaceStep39ValidationSuite`:
- `test17_financialLedgerEnforcesDoubleEntryBalanceAndExactAccounting`: Confirms perfect debit/credit equality ($Debits == Credits$).
- `test18_compensatingTransactionForRefundsMaintainsLedgerAuditability`: Confirms non-destructive refund compensation without mutating prior records.
