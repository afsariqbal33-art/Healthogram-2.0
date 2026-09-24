# HEALTHOGRAM — PRODUCTION SECURITY MONITORING & THREAT DETECTION

**Target Version:** 1.0.0 (Production)  
**Classification:** Application Security & SOC Operations  
**Monitoring Stack:** Google Cloud Security Command Center + Firebase App Check + Cloud Armor + Firestore Audit Trails  

---

## 1. Security Alert Detection Vectors

Healthogram monitors high-risk events across all application surfaces:

| Threat Vector | Monitoring Logic / Metric | Severity | Automated Action |
| :--- | :--- | :--- | :--- |
| **Credential Stuffing / Brute Force** | > 5 failed logins within 10 minutes for single account. | High | Temporarily locks login for 15 minutes; requires email OTP to unlock. |
| **Play Integrity Failure Burst** | Client requests failing App Check attestation (> 50/min). | High | Blocks requests at API gateway; logs device hashes. |
| **Health Passport QR Brute Force** | > 3 invalid QR session token handshakes from single UID. | Critical (SEV-0) | Suspends requesting account; sends alert to patient and Health Security Admin. |
| **Simultaneous Session Abuse** | Attempt to maintain > 4 active device sessions. | Medium | Enforces `MAX_DEVICES=4` policy; rejects 5th login attempt. |
| **Payment Card Testing** | > 3 declined card attempts in 5 minutes from same IP/UID. | High | Blocks checkout IP for 1 hour; flags account for manual review. |
| **Seller Inventory Tampering** | Client attempts direct write to `sellers/{sellerId}/balance`. | Critical (SEV-0) | Firestore Security Rule rejects with `permission-denied`; logs privilege escalation attempt. |
| **Admin Privilege Escalation** | Direct modification attempt on `admins/{uid}` or `roles/{uid}`. | Critical (SEV-0) | Rejected by immutable rules; sends urgent alert to Platform Owner. |
| **Medical Record Public URL Leak** | Direct unauthenticated HTTP GET to `gs://healthogram-vault`. | Critical (SEV-0) | Cloud Storage default deny-all rejects; alerts Security Ops. |

---

## 2. Health Passport Access Audit Invariants

Every read or write access to patient medical records is logged immutably to `health_access_logs`:
- Logs are strictly **append-only** via trusted Cloud Functions.
- Direct client creation, update, or deletion of audit logs is forbidden by Firestore rules.
- Patients can view their complete access history in real time under **Health Passport** -> **Access History**.
- If an unauthorized query is detected:
  1. The Firestore rule immediately denies the query.
  2. The failed attempt is logged with timestamp, requesting UID, target patient UID, and IP address.
  3. Real-time Cloud Function alerts the on-duty Security Engineer.

---

## 3. Data Protection & Privacy Auditing

- **No PHI in Push Notifications:** FCM messages are audited to verify they only contain generic notification titles (e.g., `"New Health Passport Access Request"`), never clinical diagnoses, lab names, or medication details.
- **No PHI in Analytics or Crashlytics:** Crashlytics logging filters out all sensitive user data, passwords, OTPs, credit cards, and medical fields.
- **Data Export & Deletion Compliance:** Automated background jobs process GDPR/HIPAA export and deletion requests with zero manual developer intervention.
