# 03 — DATABASE ARCHITECTURE & MIGRATION GOVERNANCE

## 1. Cloud Firestore Collection Taxonomy
* `/users/{userId}`: Identity, account category enum, verification level, sovereign key metadata.
* `/profiles/{profileId}`: Public and social profile details, avatar URLs, creator statistics.
* `/health_passports/{passportId}`: Encrypted envelope documents (`ciphertext`, `iv`, `authTag`, `keyVersion`).
  - `/conditions/{conditionId}`: Encrypted clinical conditions.
  - `/allergies/{allergyId}`: Encrypted allergen sensitivities.
  - `/medications/{medicationId}`: Encrypted prescription dosages.
  - `/vitals/{vitalId}`: Encrypted longitudinal vitals (BP, HR, SpO2).
  - `/lab_reports/{reportId}`: Encrypted lab documents & doctor notes.
* `/consent_records/{consentId}`: Audit trail of QR scan authorizations (`patientId`, `providerId`, `grantTimestamp`, `expiryTimestamp`, `revoked`).
* `/appointments/{appointmentId}`: Booking slots, status state machine (`PENDING`, `CONFIRMED`, `COMPLETED`, `CANCELLED`).
* `/products/{productId}`: Wellness marketplace items, stock reservations, pricing.
* `/orders/{orderId}`: Multi-item purchase records, escrow references.
* `/financial_ledger/{ledgerEntryId}`: Double-entry immutable accounting journals (`debitAccount`, `creditAccount`, `amount`, `currency`).
* `/owner_earnings/{earningsId}`: Platform fee splits, reconciliation ledger.

## 2. Migration Invariant: Expand-Migrate-Validate-Contract
1. **Expand:** New fields added as optional in client/server models. Legacy fields preserved.
2. **Migrate:** Cloud Functions transform records lazily on read/write.
3. **Validate:** Regression tests verify both legacy and new client versions function in parallel.
4. **Contract:** Deprecated fields removed only after 100% active client migration and 90-day grace period.
