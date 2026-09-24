# 04 — FIREBASE CLOUD INFRASTRUCTURE

## 1. Project Topology
* **Production Project:** `healthogram-prod-2026`
* **Staging Project:** `healthogram-staging-2026`
* **Region:** Multi-Region Europe (`eur3`) & US Central (`nam5`) for active redundancy.

## 2. Firestore Security Rules (v2.3)
Zero-trust enforcement:
* Medical vaults (`/health_passports/`) require `request.auth.uid == resource.data.patientId` or an unexpired, active record in `/consent_records/`.
* Write operations on financial ledgers (`/financial_ledger/`) are locked strictly to Cloud Functions backend contexts (`allow write: if false;` for client SDKs).
* Prohibited entity accounts are blocked at rule level during creation.

## 3. Cloud Storage Rules (v2.3)
* Medical uploads restricted to authenticated users.
* File sizes strictly limited: Maximum 25MB for diagnostic scans, 5MB for profile media.
* Strict MIME whitelisting: `application/pdf`, `image/jpeg`, `image/png`, `image/webp`. Executables and scripts permanently rejected.

## 4. Firebase App Check
* Attestation Provider: Google Play Integrity API.
* Blocks tampered APKs, untrusted emulators, and automated script attacks.
