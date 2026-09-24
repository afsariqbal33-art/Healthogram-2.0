# Healthogram 2.2 — Operational Security Plan & Implementation Guide

**Document:** `docs/security/HEALTHOGRAM_2_2_SECURITY_PLAN.md`  
**System:** Healthogram Platform 2.2  
**Target Release:** Healthogram 2.2.0 (Build 20200)  
**Authority:** Chief Information Security Officer (CISO) & Security Engineering Lead  
**Classification:** RESTRICTED SECURITY PLAN  

---

## 1. Security Architecture Foundations

The Healthogram 2.2 security program enforces defense-in-depth across six distinct layers:
1. **Edge & Perimeter**: Google Cloud Armor DDoS mitigation, Firebase App Check, and Cloud Load Balancing.
2. **Transport**: Strict TLS 1.3 encryption for mobile clients; mutual TLS (mTLS 1.3) for healthcare partner EHR gateways.
3. **Identity & Session Governance**: Firebase Authentication with custom security claims, multi-factor authentication (MFA) for clinical/admin staff, and a hard 4-device concurrent session ceiling.
4. **Data Isolation & Storage**: Granular Firestore Security Rules, private Cloud Storage buckets with signed URL tokens (15-minute max TTL), and AES-256 SQLCipher local on-device database encryption.
5. **Key Management**: Google Cloud KMS for cloud secrets; Android Keystore TEE / StrongBox for mobile device cryptographic keys.
6. **Continuous Monitoring**: Google Cloud Audit Logs, Security Command Center, and real-time SIEM alerting.

---

## 2. Device Session Governance (Max 4 Devices)

### Operational Policy
To eliminate unauthorized account sharing, zombie session persistence, and credential stuffing:
- Every user account is constrained to a maximum of **4 concurrent active device sessions**.
- Session registrations are stored in `users/{uid}/device_sessions/{deviceId}` containing:
  - `deviceId`, `fcmToken`, `deviceModel`, `osVersion`, `registeredAt`, and `lastActiveAt`.
- If a user signs into a 5th device, Cloud Functions execute an automated eviction sweep, invalidating the session with the oldest `lastActiveAt` timestamp and revoking its refresh token.

---

## 3. Play Integrity & Firebase App Check Hardening

- **Mandatory Enforcement**: 100% of mutating Cloud Functions (`createAppointment`, `issueConsentGrant`, `updateHealthRecord`, `initiateOrderCheckout`) require a verified Play Integrity App Check attestation token.
- **Hardware Attestation**: Evaluates device verdict (`MEETS_STRONG_INTEGRITY` or `MEETS_DEVICE_INTEGRITY`). Untrusted emulators or uncertified custom ROMs are blocked at the Cloud Function entry point.
- **Replay Protection**: Nonce verification ensures App Check tokens cannot be captured and replayed across distinct network sessions.

---

## 4. Encryption & Key Management Architecture

| Layer | Technology | Key Management | Lifecycle & Rotation |
| :--- | :--- | :--- | :--- |
| **Mobile Database** | SQLCipher AES-256 | Android Keystore (StrongBox / TEE) | Ephemeral per-device key; invalidated on app wipe |
| **Mobile Keystore Blobs** | AES-256-GCM | MasterKey generated via AndroidX Security Crypto| Non-exportable hardware-backed key |
| **Cloud Storage Blobs** | Server-Side AES-256 | Google Cloud KMS (Customer-Managed Key CMEK) | Automated annual rotation |
| **Firestore Persistence**| AES-256 at Rest | Google Cloud Default Storage Keys | Managed Google Cloud lifecycle |
| **EHR Partner Gateways** | Mutual TLS 1.3 | X.509 RSA-4096 / ECDSA Certificates | Strict 90-day automated rotation cycle |
| **Payment Webhooks** | HMAC-SHA256 | Google Cloud Secret Manager | Rotated bi-annually or upon breach alert |

---

## 5. Security Incident Response & Audit Standards

### 5.1. Automated Threat Containment
- **Repeated Auth Failures**: 5 consecutive failed authentication attempts on a practitioner or admin account triggers a 30-minute automated account soft-lock and sends an alert to the Security Operations Center (SOC).
- **Abnormal Data Egress**: If a practitioner account attempts to export > 50 patient records within a 10-minute window, the session is suspended and requires manual administrator review.

### 5.2. Audit Logging Invariant
- Every read, write, export, or deletion of clinical data records an immutable audit log entry in `audit_logs` storing:
  `{ timestamp, actorUid, actorRole, patientUid, actionType, resourceType, ipAddress, userAgent, consentGrantId }`.
- Audit logs are written to write-once-read-many (WORM) storage with a 7-year retention policy meeting international healthcare regulatory standards.
