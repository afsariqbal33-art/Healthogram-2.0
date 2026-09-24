# 08 — SOVEREIGN QR CONSENT & CLINICAL ACCESS WORKFLOW

## 1. Absolute Invariant: Zero Raw PHI in QR Payloads
**The QR matrix displayed on a user's screen NEVER contains raw medical records, patient names, diagnoses, or prescriptions.**
A bystander scanning or photographing the QR code captures only an opaque, cryptographically signed session ticket.

## 2. Dynamic QR Lifecycle & Security Parameters
* **Token Structure:** `HGM-CONSENT-v2.{ticketId}.{timestamp}.{hmacSignature}`
* **Time-to-Live (TTL):** Exactly 60 seconds. A real-time countdown ring is rendered in the UI.
* **Single-Use Enforced:** The backend consumes and invalidates the ticket upon first verified scan.
* **Scoped Permissions:** Patient chooses what to share prior to generating the QR code:
  - All Records
  - Medications Only
  - Allergies & Emergency Vitals Only
  - Specific Lab Report

## 3. Provider Scan & Authorization Handshake
1. **Patient:** Generates single-use QR token.
2. **Provider (Doctor/Clinic/Hospital):** Scans QR using verified practitioner account.
3. **Backend Validation:** Validates provider license, verifies token signature, checks 60s TTL, checks ticket hasn't been redeemed.
4. **Consent Record Creation:** Creates immutable entry in `/consent_records/` with access window (default: 30 minutes).
5. **Decryption Session:** Patient device issues an ephemeral session key wrapped with provider's public key.
6. **Patient Revocation:** Patient can tap "Revoke Access Immediately" from their device at any time during the active session.
