# 06 — PLATFORM SECURITY & THREAT MITIGATION

## 1. Zero-Trust Security Posture
* **Envelope Encryption:** Client-side AES-GCM-256 for all Protected Health Information (PHI).
* **Hardware Keystore Integration:** Master cryptographic keys never leave Android Keystore / StrongBox.
* **Certificate Pinning & TLS 1.3:** Enforced on all outgoing network connections to backend endpoints.

## 2. In-App Security Engine Audits
* **Root / Tamper Detection:** Checks for root binaries, unlocked bootloaders, and debugger attachments.
* **Screen Privacy:** `FLAG_SECURE` enabled on Health Passport and teleconsultation screens to prevent screenshot leaks and OS recents thumbnail caching.
* **Zero Hardcoded Secrets:** Static analysis verifies zero embedded private keys, webhook secrets, or service account tokens in source code.

## 3. Defense Against OWASP Mobile Top 10
* **M1 (Improper Credential Usage):** OAuth tokens stored exclusively in Android EncryptedSharedPreferences.
* **M2 (Inadequate Supply Chain):** Dependencies pinned and verified via Gradle verification metadata.
* **M3 (Insecure Authentication):** Enforced 4-device ceiling, rate-limited auth attempts, brute-force lockouts.
* **M4 (Insufficient Input Validation):** Strict JSON schema and FHIR resource validation before ingestion.
* **M9 (Insecure Data Storage):** AES-GCM-256 encryption at rest; Room DB encrypted with SQLCipher.
