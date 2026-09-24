# HEALTHOGRAM — DATA RETENTION & LIFECYCLE MANAGEMENT POLICY

**Document Version:** 2.2.0  
**Classification:** Enterprise Legal & Compliance Standard  
**Effective Date:** September 20, 2026  
**Audience:** All Engineering, Legal, Operations, and Compliance Staff  
**Owner:** Compliance Architect & Legal Counsel (privacy@healthogram.app)

---

## 1. Purpose & Scope

This Data Retention Policy establishes mandatory retention windows, automated archiving schedules, and certified cryptographic destruction protocols for all data categories managed within the Healthogram platform. 

It satisfies statutory obligations under:
- **HIPAA** (Health Insurance Portability and Accountability Act - 45 CFR § 164.316)
- **GDPR** (General Data Protection Regulation - Art. 5(1)(e), Art. 17)
- **CCPA / CPRA** (California Consumer Privacy Act)
- **PCI-DSS v4.0** (Payment Card Industry Data Security Standard)
- **Google Play Developer Program Policies** (User Data & Account Deletion)

---

## 2. Retention Schedule Master Table

| Data Category | Specific Collections / Assets | Minimum Retention Period | Maximum Retention Period | Statutory / Business Justification | Automated Disposal Action |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Health Passport Records** | `health_profiles`, `health_conditions`, `health_allergies`, `health_medications`, `health_diagnoses`, `health_lab_reports`, `health_prescriptions`, `health_documents`, `health_notes` | Duration of active user account | 30 days post account deletion request | Patient care continuity & HIPAA access right. Overridden immediately upon verified user deletion request. | Cryptographic erasure of records and storage files upon completion of 30-day deletion grace period. |
| **Clinical Access Audit Logs** | `health_access_logs`, `health_access_grants` | **7 years** from generation date | **7 years + 90 days** | HIPAA 45 CFR § 164.312(b) & statutory medical audit defense mandate. | Automated Cloud Function cron purges log records older than 2,555 days. User identity pseudonymized. |
| **Account Identity & PII** | `users`, `public_profiles`, `private_profiles`, `devices` | Duration of active user account | 30 days post account deletion request | User authentication, identity verification, account management. | Hard delete from Firebase Auth, Firestore documents purged, cached tokens revoked. |
| **Professional Healthcare Verifications** | `verifications`, `verification_applications`, `verification_documents`, `verification_private/**` | Active accreditation period + **5 years** post-expiration | **7 years** post-rejection or expiration | Medical malpractice defense, licensing board audit requirements, regulatory accreditation history. | Verification documents purged from Cloud Storage; cryptographic audit hash preserved. |
| **Social Media Posts & Reels** | `posts`, `reels`, `comments`, `public_social/**` | Until author deletion or account closure | 30 days post account deletion | Creator content hosting, community interaction. | Hard delete from Firestore, video/image media removed from Cloud Storage bucket immediately. |
| **Ephemeral Stories** | `stories`, `public_social/{uid}/stories/**` | Immediate public view: **24 hours** | **30 days** in soft-deleted backup buffer | Short-lived social communication standard. | Firestore TTL policy expires documents at `createdAt + 24h`; storage asset hard-deleted at Day 30. |
| **Direct Messages & Chat History** | `messages`, `conversations`, `messages_private/**` | Active conversation duration | 1 year rolling window or until user deletion | Direct user communication. Users may select 30-day, 90-day, or 1-year auto-delete. | Scheduled purge job deletes messages beyond user-configured conversation retention threshold. |
| **Teleconsultation Signaling & WebRTC** | `call_sessions`, `translation_call_sessions` | Duration of active call session | **5 minutes** post call termination | WebRTC session negotiation and ICE candidate exchange. Zero audio/video persisted. | Signaling docs deleted immediately upon call hangup. Stale sessions purged after 15 minutes. |
| **AI Studio Job Inputs & Outputs** | `ai_jobs`, `ai_generated_assets`, `ai_private/**` | Active user session / job execution | **24 hours** for temporary job assets; permanent assets saved to user gallery | Stateless generative assistant processing. | Automatic Cloud Storage lifecycle rule purges temporary job cache at 24 hours. |
| **Marketplace Orders & Invoices** | `marketplace_orders`, `marketplace_payments`, `marketplace_invoices` | **7 years** | **10 years** | Commercial tax law, VAT compliance, audit defense, warranty claim handling. | Records moved to cold archive storage at Year 3; permanent purge at Year 10. |
| **Double-Entry Financial Ledger** | `financial_ledger_entries`, `owner_financial_accounts`, `marketplace_seller_ledger` | **10 years** | **10 years** | Anti-Money Laundering (AML), FinCEN regulations, corporate accounting audit defense. | Immutable append-only ledger; archived after 10 years per tax regulatory guidelines. |
| **Security & Login Audit Logs** | `security_events`, `security_alerts`, `login_security_events`, `admin_audit_logs` | **2 years** | **3 years** | SOC 2 Type II, ISO 27001, security incident forensics and threat hunting. | Automated partition expiration deletes logs older than 730 days. |
| **Crashlytics & Telemetry** | Firebase Crashlytics crash traces, performance metrics | Rolling **90 days** | **90 days** | Application reliability monitoring, bug triage. | Automatically purged by Google Cloud / Firebase telemetry lifecycle. |

---

## 3. Account Deletion Lifecycle & Grace Period

Healthogram enforces a 30-day statutory grace period for account deletion to prevent catastrophic data loss resulting from unauthorized account compromise or accidental submission:

```
[Day 0: Deletion Requested]
   ├── User re-authenticates (MFA/Biometrics)
   ├── Active sessions terminated immediately (`user_devices` revoked)
   ├── Firebase Auth disabled (login blocked)
   ├── Public profile and social content hidden from search & feeds
   └── Health Passport access tokens and active grants revoked
   
[Days 1–30: Grace Period]
   ├── Account remains in 'pending_deletion' soft state
   └── User may restore account via authenticated challenge
   
[Day 31: Automated Permanent Deletion]
   ├── Firestore hard delete worker cleans all personal collections
   ├── Cloud Storage secure file destruction in `health_private`, `ai_private`, `public_social`
   ├── Medical access audit logs anonymized (patient UID -> cryptographic hash)
   └── Financial ledger entries detached from personal identifiable markers
```

---

## 4. Cryptographic Erasure & Destruction Standards

When records reach their maximum retention limit or are purged following an account deletion request:
1. **Firestore Documents:** Executed via batched delete operations removing all document properties and subcollections.
2. **Cloud Storage Objects:** Objects are deleted and their storage buckets enforce zero versioning retention beyond the lifecycle window.
3. **Envelope Encryption Keys:** In encrypted health partitions, the user-specific Data Encryption Key (DEK) is destroyed from the Key Management Service, rendering any residual block cache mathematically unrecoverable.
4. **Disposal Verification:** Every automated deletion run generates an immutable, non-identifying completion receipt logged in `data_integrity_reports`.
