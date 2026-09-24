# HEALTHOGRAM PRIVACY POLICY

**Last Updated:** September 16, 2026  
**Effective Date:** September 16, 2026  
**Published Location:** `https://healthogram.app/privacy-policy` and In-App Settings  

---

## 1. Introduction

Healthogram ("we", "our", or "us") provides a global healthcare, Health Passport, social, marketplace, and communication platform designed to empower individuals with sovereign control over their health information. We are committed to protecting your privacy and upholding the highest standards of data security and confidentiality in accordance with global regulations, including HIPAA, GDPR, and Google Play Developer Policies.

---

## 2. Information We Collect

### A. Account Information
When you create an account, we collect your name, email address, phone number, hashed credentials, and designated account category (`Individual`, `Doctor`, `Clinic`, `Hospital`, or `Laboratory`).

### B. Health Passport & Medical Records
You may voluntarily store health information in your personal Health Passport, including allergies, chronic medical conditions, medications, clinical visit notes, diagnostic test results, lab reports, and uploaded medical documents. This data is private by default.

### C. Communications & Social Content
We collect content you create, share, or upload, including social posts, reels, stories, direct messages, and comments. Audio and video calls are transmitted peer-to-peer via WebRTC and are **never automatically recorded**.

### D. Marketplace & Payment Information
When purchasing or selling wellness products on the marketplace, we collect order details, delivery addresses, and payment transaction metadata. Full credit card numbers and banking credentials are tokenized by our PCI-DSS certified payment processors (e.g., Stripe) and are **never stored on our servers**.

### E. Device & Diagnostic Telemetry
We collect technical information regarding your device model, operating system version, app version, network state, and anonymized crash diagnostics through Firebase Crashlytics to maintain application stability.

---

## 3. How We Use Your Information

- To provide, maintain, and improve our services.
- To facilitate secure, patient-authorized sharing of Health Passport records with verified healthcare providers.
- To verify the credentials of healthcare professionals and medical facilities.
- To process marketplace transactions, manage escrow, and calculate seller commissions.
- To enforce our 4-device simultaneous session limit and detect fraudulent account activity.
- To deliver essential service notifications and security alerts.

---

## 4. How We Share Your Information

- **Healthcare Providers:** Health records are shared **only when you explicitly authorize access** via an in-app prompt or QR code session scan. Healthcare access is time-limited and subject to an immutable audit log.
- **Delivery Partners:** Delivery drivers receive only your designated shipping address and recipient name to fulfill marketplace orders.
- **Service Providers:** We engage trusted infrastructure providers (e.g., Google Cloud Platform / Firebase) to host our databases and execute server-side microservices under strict data processing agreements.
- **Zero Advertising Sales:** **We do not sell, rent, or trade your personal or health data to third-party advertisers or data brokers.**

---

## 5. Data Security & Storage

- All data in transit is encrypted using Transport Layer Security (TLS 1.3).
- Stored medical documents and database records are encrypted at rest using AES-256.
- Health records are isolated in private Cloud Storage buckets accessible only through short-lived signed tokens.
- Application integrity is attested via Google Play Integrity and Firebase App Check.

---

## 6. Your Rights & Choices

You have full sovereign rights over your data:
- **Access & Portability:** You can export your full profile and medical records at any time.
- **Correction:** You can update or correct inaccuracies in your health profile.
- **Consent Revocation:** You can instantly revoke healthcare provider access grants with a single tap.
- **Account Deletion:** You can delete your account and associated data directly in the app or via `https://healthogram.app/delete-account`.

---

## 7. Contact Us

If you have questions, concerns, or requests regarding this Privacy Policy or our data practices, please contact our Data Protection Officer:

**Healthogram Privacy & Security Office**  
Email: `privacy@healthogram.app`  
Security Team: `security@healthogram.app`  
Website: `https://healthogram.app`
