# Healthogram Owner Earnings & Withdrawal 2.2 Validation

**Document Version:** 2.2.0  
**Phase:** Step 39 — Owner Finance, Revenue Aggregation & 2FA Withdrawal Governance  
**Classification:** Proprietary / Engineering Specification  

---

## 1. Owner Earnings Architecture

Healthogram Owner Earnings Engine tracks and reconciles the platform's proprietary financial health across all jurisdictions.

### 1.1 Mathematical Formula for Net Platform Earnings
$$\text{Net Owner Earnings} = (\text{Gross Commissions} + \text{Consumer Service Fees}) - (\text{Gateway Processing Costs} + \text{Refund Deductions} + \text{Chargebacks} + \text{Taxes Accrued})$$

### 1.2 Account Balance Segregation
The platform maintains distinct balance pools in `OwnerFinancialAccount`:
- **Available Balance**: Funds cleared through settlement windows and ready for withdrawal.
- **Pending Balance**: Commissions from orders currently within the customer return window (7 days).
- **Reserved Balance**: Risk reserve held for potential chargebacks, regulatory withholding, or pending disputes.
- **Withdrawn Balance**: Historical cumulative funds disbursed to verified owner corporate bank accounts.

---

## 2. Owner Withdrawal Governance & State Machine

Direct mutation of owner balance from the client is structurally impossible. All disbursements require strict multi-step governance:

```
[REQUESTED] (Owner initiates withdrawal with amount & destination IBAN)
   │
   ▼
[2FA_VERIFICATION] (Time-based One-Time Password / Biometric Reauth)
   ├─── (Invalid 2FA / Expired) ───► [CANCELLED]
   │ (Valid 2FA)
   ▼
[UNDER_REVIEW] (Automated risk & threshold checks: balance >= amount >= minThreshold)
   ├─── (Insufficient balance / risk alert) ───► [REJECTED]
   │ (Passes automated screening)
   ▼
[APPROVED]
   │ (Treasury batch dispatch to corporate banking rail)
   ▼
[PROCESSING]
   ├─── (Banking rail rejection / invalid IBAN) ───► [FAILED] ──► (Compensating credit)
   │ (Bank confirms transfer)
   ▼
[COMPLETED] ──► (Generates immutable ledger disbursement record)
```

---

## 3. Threshold & Risk Controls

1. **Minimum Withdrawal Limit**: Configurable per currency (e.g., 1,000.00 SAR / $500.00 USD). Requests below this are immediately rejected.
2. **Maximum Daily / Velocity Limit**: Daily cap prevents unauthorized bulk draining during account compromise.
3. **Dual-Approval for Large Transactions**: Withdrawals exceeding tier threshold (e.g., > 100,000.00 SAR) require second corporate owner signature.
4. **Audit Logging**: Every withdrawal request, approval, and execution logs IP address, device signature, admin UID, and timestamp in `owner_withdrawal_audit_logs`.

---

## 4. Automated Test Validation

Validated in `MarketplaceStep39ValidationSuite`:
- `test19_ownerEarningsCalculatesNetRevenueAccurately`: Verifies precision across revenue and deduction streams.
- `test20_ownerWithdrawalEnforces2FAThresholdsAndNoDirectClientBalanceMutation`: Confirms rejection without 2FA, rejection below minimum thresholds, and rejection on insufficient balance.
