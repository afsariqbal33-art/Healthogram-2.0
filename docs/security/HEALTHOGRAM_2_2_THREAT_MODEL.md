# Healthogram 2.2 — Comprehensive STRIDE Threat Model & Security Architecture

**Document:** `docs/security/HEALTHOGRAM_2_2_THREAT_MODEL.md`  
**System:** Healthogram Platform 2.2  
**Target Release:** Healthogram 2.2.0 (Build 20200)  
**Authority:** Chief Information Security Officer (CISO) & Lead Application Security Architect  
**Classification:** RESTRICTED SECURITY SPECIFICATION  

---

## 1. System Scope & Trust Boundaries

```
[Trust Zone 0: Untrusted Public Internet]
  • Mobile client running on rooted/modified consumer Android devices
  • Public network transit (Wi-Fi, cellular)
         │
═════════╪══════════════════════════════════════════════════════════════ [Boundary 1: App Check / mTLS]
         ▼
[Trust Zone 1: Verified Edge Gateways]
  • Cloud Load Balancer & Cloud Armor (DDoS mitigation)
  • Firebase App Check / Play Integrity hardware attestation
  • Partner Gateway (mTLS 1.3 certificate validation)
         │
═════════╪══════════════════════════════════════════════════════════════ [Boundary 2: IAM & Custom Claims]
         ▼
[Trust Zone 2: Application Cloud Functions & Logic]
  • Node 20 Serverless execution environment
  • Session Concurrency Manager (Max 4 active devices)
  • Role-Based Access Control (Individual, Doctor, Clinic, Hospital, Laboratory)
         │
═════════╪══════════════════════════════════════════════════════════════ [Boundary 3: Firestore Rules]
         ▼
[Trust Zone 3: Persistence & Enclave Stores]
  • Cloud Firestore Multi-Region (`eur3`)
  • Private Cloud Storage buckets (AES-256 encrypted blobs)
  • Hardware Security Module (HSM) / Cloud KMS keys
```

---

## 2. STRIDE Threat Analysis & Concrete Mitigations

### 2.1. Spoofing (Identity & Client Impersonation)
- **Threat S-01: Unauthorized App Binary Tampering / Bot Emulators**
  - *Attack*: Threat actor decompiles APK, bypasses UI checks, and issues automated API requests via emulator or script.
  - *Mitigation*: Mandatory Firebase App Check enforced with Google Play Integrity API. Mutating requests without a valid hardware attestation token are rejected with HTTP 401 at the edge.
- **Threat S-02: Doctor & Clinical Partner Impersonation**
  - *Attack*: Malicious individual creates an account claiming to be a licensed physician.
  - *Mitigation*: Healthcare accounts (`Doctor`, `Clinic`, `Hospital`, `Laboratory`) remain disabled (`VERIFICATION_PENDING`) until manual validation against national registries (e.g. Oman MOH, Saudi SCFHS) with verified government credentials.

### 2.2. Tampering (Unauthorized Modification of Data)
- **Threat T-01: Patient Health Record Alteration**
  - *Attack*: Patient or rogue user attempts to alter diagnostic lab results or doctor notes in Firestore.
  - *Mitigation*: Firestore Security Rules prohibit client-side updates to `health_records`. Records can only be committed via Cloud Functions verifying cryptographic signatures and practitioner role claims.
- **Threat T-02: Payment Webhook Replay / Amount Tampering**
  - *Attack*: Attacker intercepts payment gateway webhook and replays payload with modified order amounts or currency.
  - *Mitigation*: HMAC-SHA256 signature verification on all incoming webhooks using secrets stored in Cloud Secret Manager. Webhook processing is idempotent, recording transaction IDs in a processed-event ledger.

### 2.3. Repudiation (Denial of Actions)
- **Threat R-01: Clinician Denying Teleconsultation / Prescription Issuance**
  - *Attack*: Practitioner claims they did not prescribe a medication or issue an intake note.
  - *Mitigation*: Immutable provenance audit logs storing practitioner UID, digital certificate fingerprint, client IP, and UTC timestamp in append-only Firestore audit collections.
- **Threat R-02: Seller Denying Customer Order Fulfillment**
  - *Attack*: Marketplace seller claims customer received goods without delivering them.
  - *Mitigation*: Fulfillment release requires courier OTP verification code entered by customer upon physical package handover.

### 2.4. Information Disclosure (Unauthorized PHI / PII Exposure)
- **Threat I-01: Clinical Diagnoses Leaked on Mobile Lockscreen Notifications**
  - *Attack*: Push notification displays sensitive illness (e.g. "Oncology consult booked") on a visible lockscreen.
  - *Mitigation*: Strict notification payload sanitization in Cloud Functions. Outgoing FCM alerts carry strictly generic copy: *"You have an appointment update from Healthogram"*. Clinical data is never included in push notification text.
- **Threat I-02: Local Device Clinical Cache Compromise**
  - *Attack*: Unauthorized third party inspects SQLite database on stolen or rooted Android device.
  - *Mitigation*: Local Room database encrypted using SQLCipher with 256-bit AES keys securely derived from the Android Keystore hardware-backed security module (TEE/StrongBox).

### 2.5. Denial of Service (Resource Exhaustion)
- **Threat D-01: Firestore Read/Write Exhaustion via Rapid Polling**
  - *Attack*: Compromised client repeatedly subscribes to large Firestore collections, exhausting monthly read quotas.
  - *Mitigation*: Server-side token bucket rate limiting on Cloud Functions (100 req/min per IP/UID); client-side Room database caching using `last_modified_at` conditional fetch headers.
- **Threat D-02: WebRTC TURN Relay Bandwidth Flooding**
  - *Attack*: Attacker initiates continuous dummy video streams via TURN servers to drive up cloud egress costs.
  - *Mitigation*: Ephemeral TURN credentials with 15-minute TTL; mandatory P2P STUN handshake prioritization; hard limit of 60 minutes per consultation call session.

### 2.6. Elevation of Privilege (Unauthorized Privilege Escalation)
- **Threat E-01: User Elevating Claims to Administrator / Doctor**
  - *Attack*: Attacker crafts a token attempting to inject `admin: true` or `role: "doctor"` custom claims.
  - *Mitigation*: Firebase Custom Claims can only be set by server-side Cloud Functions running under privileged service accounts protected by Cloud IAM. The client SDK cannot write to `auth.token`.
- **Threat E-02: Healthcare Partner Cross-Patient Scope Crawling**
  - *Attack*: Accredited hospital attempts to query records of patients who have not registered an active consent grant with that facility.
  - *Mitigation*: FHIR controller evaluates active consent grants in Firestore before serializing any clinical resource. If no valid, unexpired grant exists matching `(patientUid, partnerOrgUid)`, the request is terminated with HTTP 403 Forbidden.

---

## 3. Threat Mitigation Summary Matrix

| Threat Code | Vector | Risk Severity | Primary Defense Mechanism | Residual Risk |
| :--- | :--- | :--- | :--- | :--- |
| **S-01** | App Tampering | Critical | Firebase App Check + Play Integrity | Low |
| **S-02** | Doctor Spoofing | Critical | Multi-stage Government License Registry Verification | Minimal |
| **T-01** | Clinical Tampering | Critical | Server-Only Firestore Rules + Cryptographic Provenance | Negligible |
| **T-02** | Webhook Tampering | High | HMAC-SHA256 Signatures + Idempotency Tables | Minimal |
| **R-01** | Consultation Repudiation | Medium | WORM Cloud Logging + Append-Only Audit Entries | Negligible |
| **I-01** | Push Notification Leak | High | Generic Push Notification Payload Sanitizer | Negligible |
| **I-02** | Device Local Stealing | High | SQLCipher AES-256 + Android Keystore TEE | Low |
| **D-01** | Quota Exhaustion | Medium | Local Room Caching + Cloud Function Rate Limiters | Low |
| **E-01** | Admin Privilege Escalation| Critical | Server-Side Cloud IAM + Admin FIDO2 Hardware MFA | Negligible |
| **E-02** | Partner Consent Breach | Critical | Granular Token Introspection + Scoped Firestore Rules | Negligible |
