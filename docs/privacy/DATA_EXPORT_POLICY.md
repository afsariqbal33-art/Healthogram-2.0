# HEALTHOGRAM — USER DATA EXPORT POLICY & PROCEDURES

**Scope:** GDPR Article 20 / CCPA / HIPAA Patient Right of Access  
**Effective Date:** September 16, 2026  

---

## 1. Right to Data Portability

Every registered user of Healthogram has the right to receive a comprehensive, structured, commonly used, and machine-readable copy of their personal data and medical history.

---

## 2. Exportable Data Packages

Users can generate export archives directly within the app settings or via formal support request:

### A. Core Profile & Social Archive (`healthogram_profile_export.json`)
- Account information and profile metadata.
- Published social posts, reels metadata, comments, and public interactions.
- Device login session history.

### B. Health Passport & Medical Records Archive (`healthogram_health_vault.zip`)
- Complete Health Profile (Conditions, Allergies, Medications, Precautions).
- Historic Medical Encounters (Diagnoses, Doctor Visits, Clinical Notes).
- Diagnostic Laboratory Reports and Prescriptions.
- Medical Document Vault attachments (downloaded PDFs, clinical scans).
- Comprehensive Health Access Logs (detailed history of who accessed records).

### C. Commercial & Order Archive (`healthogram_orders_export.json`)
- Order history, purchased items, invoices, and shipping details.
- Seller sales summary and payout records (for registered sellers).

---

## 3. Security & Anti-Exfiltration Safeguards

1. **Re-Authentication Requirement:** Requesting a data export triggers mandatory password re-entry or 2FA verification.
2. **Encrypted Delivery:** Generated ZIP archives are encrypted using AES-256 with a user-specified export passphrase.
3. **Short-Lived Download Links:** Cloud Storage signed URLs for export packages expire automatically after 24 hours.
4. **Third-Party Record Isolation:** Data exports contain exclusively the requesting user's records. Attending physician notes regarding other patients are strictly filtered out.

---

## 4. Export Generation Flow

```text
User Request (Settings -> Privacy -> Export Data)
       ↓
Re-authenticate via 2FA
       ↓
Cloud Function Job Queued (asynchronous bundling)
       ↓
Export Package Assembled & Encrypted with User Key
       ↓
Email Notification sent with 24-hour expiring download link
       ↓
User downloads archive and verifies checksum
```
