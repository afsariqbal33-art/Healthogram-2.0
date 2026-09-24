# HEALTHOGRAM — SECURITY SPECIFICATION

## 1. Security Foundations
- **Platform Infrastructure Protection**: Encryption in transit (TLS 1.3) and encryption at rest (AES-256 via Cloud Firestore and Cloud KMS).
- **Zero Raw Data in QR**: Patient QR codes contain a 15-minute rotating cryptographic session ticket. Scanning initiates mutual verification rather than raw data transmission.
- **Audit Logging**: Every single access attempt to medical records produces an immutable `health_audit_logs` record, capturing requester ID, organization, requested scope, purpose, and timestamp.

## 2. Hard Architectural Invariants
1. **Pharmacy Exclusion**: `AccountType.isValid("PHARMACY") == false`. Any creation attempt in Firestore triggers rule rejection.
2. **Device Limits Enforcement**: 4 devices maximum for normal accounts; 4 or 8 devices for healthcare organizations based on subscription package tier.
3. **Owner Earnings Integrity**: Balance updates cannot be triggered by clients; only Cloud Functions processing webhook settlements write to `owner_ledger`.
4. **AI Safety**: Raw patient records are never sent to external or multimodal generative AI models.
