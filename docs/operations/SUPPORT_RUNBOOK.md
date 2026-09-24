# HEALTHOGRAM — USER & HEALTHCARE PROVIDER SUPPORT RUNBOOK

**Target Version:** 1.0.0 (Production)  
**Classification:** Tier 1 / Tier 2 / Tier 3 Customer & Provider Support  
**Support Email:** `support@healthogram.app`  

---

## 1. Golden Rules of Healthogram Support

1. **NEVER Request Sensitive Data:** Support agents must NEVER ask users for passwords, OTP codes, credit card numbers, private encryption keys, or raw medical records/diagnoses.
2. **Health Passport Confidentiality:** Support agents cannot view or modify a patient's Health Passport records under any circumstances. There is no "backdoor" in the Admin console for patient medical profiles.
3. **Immutability of Financial Transactions:** Support agents cannot directly edit account balances. All refunds and adjustments must proceed through the verified payment dispute pipeline.
4. **Professionalism:** Healthcare providers (Doctors, Clinics, Hospitals, Laboratories) and Patients must be handled with compassionate, professional, and clear communication.

---

## 2. Ticket Classification & SLA Matrix

| Ticket Category | Examples | Target First Response | Target Resolution |
| :--- | :--- | :--- | :--- |
| **Account & Login** | OTP not received, password reset email missing, session limit prompt. | < 2 hours | < 12 hours |
| **Healthcare Verification** | Doctor license upload inquiry, clinic registration review status. | < 4 hours | < 24 hours |
| **Health Passport Sharing** | QR code scanning issue, expired access grant inquiry. | < 1 hour | < 8 hours |
| **Marketplace & Orders** | Defective product received, courier delivery delayed, return request. | < 2 hours | < 24 hours |
| **Payment & Billing** | Duplicate charge, refund status, payment failed at checkout. | < 1 hour | < 12 hours |
| **Privacy & GDPR/HIPAA** | Request for data export, request for account & data deletion. | < 24 hours | < 30 days (per law) |

---

## 3. Standard Troubleshooting Procedures

### Procedure A: User reports "Device Limit Reached (Max 4 Devices)"
- **Explanation:** Healthogram enforces a maximum of 4 concurrent active device sessions per account for account security.
- **Resolution:**
  1. Advise user to open **Settings** -> **Security** -> **Active Sessions** on one of their logged-in devices.
  2. Select and tap **Revoke** on any older or unused device session.
  3. If user has lost access to all previous devices: verify user identity via registered government ID and secondary email; escalate to `Tier 2 Auth Support` to trigger server-side session revocation.

### Procedure B: Doctor reports "Unable to view Patient Health Passport after scan"
- **Resolution:**
  1. Check Doctor's verification status in Admin Portal (`accounts/{doctorId}`). If `verification_status != "VERIFIED"`, explain that unverified accounts cannot view patient records.
  2. Verify patient authorized the temporary access request on their screen. Access requests expire after 30 minutes if not approved.
  3. Ask Doctor to request a fresh QR scan from the patient.

### Procedure C: Customer requests Account & Data Deletion
- **Self-Service Path:** User can navigate to **Settings** -> **Account** -> **Delete Account**.
- **Agent Assistance Path:**
  1. Direct user to our self-service deletion flow or public deletion portal (`https://healthogram.app/account/delete`).
  2. Note that account enters a 30-day grace period, after which all profile data, media, and chat histories are permanently purged.
  3. Financial ledger records are retained in an anonymized state for 7 years as required by statutory tax laws.
