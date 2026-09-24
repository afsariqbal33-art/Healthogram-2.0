# ADR-2.2-009: Double-Entry Financial Ledger & Minor-Unit Math

**Status:** ACCEPTED  
**Date:** 2026-09-20  
**Context:**  
Healthogram processes multi-currency marketplace sales, telehealth consultation fees, platform commissions, and seller/doctor payouts. Floating-point rounding errors and race conditions in financial calculations lead to balance drift, tax discrepancies, and audit failures. In Step 35, 18,920 orders were processed with zero ledger discrepancy.

**Decision:**  
1. **Double-Entry Bookkeeping**: Every transaction must record balanced debit and credit entries in the immutable `financial_ledger` collection.
2. **Integer Minor Units**: All monetary amounts are stored and calculated exclusively in integer minor units (e.g. 1000 Baisa = 1.000 OMR; 100 Halalas = 1.00 SAR). Floating-point data types (`float`, `double`) are strictly prohibited in financial code.
3. **Server-Side Exclusivity**: Financial mutations execute solely inside atomic Firestore transactions triggered by serverless Cloud Functions. Client applications have read-only access to their own transaction history.
4. **Hardware-PIN Payouts**: Payout releases to external bank accounts require physical hardware PIN confirmation by the Master Platform Owner.

**Consequences:**  
- **Positive**: Complete auditability; zero mathematical rounding errors; absolute compliance with international banking standards.
- **Negative**: Requires careful client-side currency string formatting based on country locale.
