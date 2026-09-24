# Delivery Financial Ledger & 3PL Carrier Reconciliation

## 1. Double-Entry Delivery Ledger
Every physical movement incurring financial value generates immutable ledger entries:

| Entry Type | Debit / Credit | Description |
|---|---|---|
| `SHIPPING_CHARGE` | Customer Receivable | What the customer paid for fulfillment |
| `DELIVERY_PROVIDER_COST` | Provider Payable | What Healthogram owes Aramex, SMSA, or fleet |
| `SELLER_DELIVERY_FEE` | Seller Payable | Payout to seller if fulfillment was seller-managed |
| `DELIVERY_ADJUSTMENT` | Adjustment | Re-weighting or oversized package surcharges |
| `DELIVERY_REFUND` | Customer Refund | Refunded shipping on damaged/cancelled fulfillment |
| `FAILED_DELIVERY_FEE` | Logistics Cost | Attempt fee charged by 3PL carrier on failure |
| `RETURN_SHIPPING` | Logistics Cost | Return leg transit costs |
| `PLATFORM_DELIVERY_REVENUE` | Net Retained Margin | Positive margin earned between retail and wholesale rates |

## 2. Carrier Invoice Reconciliation
1. At weekly settlement cycles, carrier electronic billing invoices are matched against `Shipment` records.
2. `DeliveryEngine.reconcileProvider()` checks:
   - Matched delivered packages count
   - Discrepancies in billable weight (actual vs declared)
   - Unaccounted shipment tracking numbers
3. Reconciliation reports (`delivery_reconciliation_reports/{reportId}`) are exported for audit and finance review.
