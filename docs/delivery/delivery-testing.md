# Delivery Infrastructure & Integration Testing

## 1. Automated Verification Suite
All delivery operations are continuously verified via `DeliveryInfrastructureTest.kt`:

1. **Country Configuration Resolution**: Validates that sovereign country configs (SA, AE, US) load valid defaults and enforce local currency.
2. **Zone Resolution**: Verifies metropolitan and postal code matching against active zones.
3. **Address Validation**: Ensures missing contact numbers, missing street addresses, or out-of-zone destinations are rejected with descriptive errors.
4. **Server Shipping Rate Calculation**: Validates base fees, weight surcharge tiers, tax computation, and free shipping threshold qualification.
5. **International Delivery Boundary**: Asserts that cross-border deliveries between different countries are blocked while `internationalDeliveryEnabled = false`.
6. **Product Restriction Enforcement**: Verifies prohibited medications or controlled substances trigger legal blockages before shipment creation.
7. **End-to-End Shipment Creation**: Validates generation of tracking numbers, barcodes, packages, initial tracking events, and financial ledger entries.
8. **Contactless OTP Proof of Delivery**: Verifies that matching OTP successfully transitions fulfillment and shipment states to `DELIVERED`.
9. **Return to Seller (RTS)**: Tests automated carrier reversal and failure logging.
10. **Customer Returns & Step 14 Integration**: Tests return request workflow and inspection approval.
11. **Emergency Kill Switches**: Tests that `emergencyDeliveryStop` instantly halts rate calculation and dispatch.
12. **Double-Entry Financial Ledger Reconciliation**: Asserts that customer shipping charges, provider expenses, and net platform revenues balance to zero unallocated difference.
