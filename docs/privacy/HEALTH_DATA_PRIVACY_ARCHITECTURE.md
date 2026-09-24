# Health Data Privacy & Sovereignty Architecture

**Version:** 2.2.0  
**Effective Date:** 2026-09-20  
**Compliance Standards:** HIPAA, GDPR (Articles 15, 17, 20), ISO 27701  

---

## 1. Core Principles of Patient Sovereignty

In Healthogram, health data privacy is enforced at the core database and operational level rather than through policy disclaimers alone:
1. **User Ownership:** Every clinical data point belongs to the patient identity.
2. **Explicit Opt-in Consent:** Healthcare organizations have zero visibility into any record without a cryptographic, audited consent grant.
3. **Zero Cross-Contamination:** Complete isolation between social/commerce engines and clinical health data.
4. **Permanent Right to Erasure & Portability:** Unconditional support for export and deletion without dark patterns.

---

## 2. Healthcare AI Privacy Airgap

Healthogram differentiates sharply between **Generic AI Studio** tools and **Healthcare AI Services**:

| Property | Generic AI Studio | Healthcare AI System |
| :--- | :--- | :--- |
| **Model Ingestion & Retraining** | Allowed for prompt optimization | **STRICTLY FORBIDDEN** (Zero data retention for training) |
| **Data Residency** | Global edge runtime | Sovereign healthcare data region |
| **Patient Identification** | Pseudonymous user ID | Fully anonymized / stripped of direct identifiers |
| **Audit Requirement** | Standard API telemetry | Permanent `HealthcareAIAuditRecord` with clinical justification |
| **Prompt Injection Defenses**| Standard safety filters | Medical boundary checks (never prescribes, never alters facts) |

---

## 3. Data Portability Workflow (GDPR Article 20)

Patients can initiate a self-service portability request at any time:
1. **Scope Selection:** Select specific categories (Conditions, Medications, Labs, Vitals, or Full Bundle).
2. **Standardized Formatting:** Export delivered as normative HL7 FHIR R4 Bundle alongside a human-readable PDF summary.
3. **Signed Download Package:** File generated with temporary signed URL expiring in 24 hours.
4. **Audit Record:** Generation and download logged with patient UID and timestamp.

---

## 4. Right to Erasure & Data Deletion Workflow (GDPR Article 17)

When an individual exercises their right to delete their health data:
1. **Authentication & Biometric Challenge:** Verification of requesting identity.
2. **Subcollection Cascade:** Immediate soft-deletion followed by cryptographic wiping of:
   - `users/{uid}/health_profile`
   - `users/{uid}/health_conditions`
   - `users/{uid}/health_allergies`
   - `users/{uid}/health_medications`
   - `users/{uid}/health_observations`
   - `users/{uid}/health_documents`
   - `users/{uid}/consents`
3. **Health Connect Revocation:** Android Health Connect linkage dissociated immediately.
4. **Emergency ICE Card Invalidation:** Tokens revoked; emergency profile purged.
5. **Preservation of Legal Minimums:** Audit logs of past clinical actions performed by external clinicians (e.g., signed prescriptions) are retained strictly in compliance with statutory medical record retention laws, stripped of patient non-clinical metadata.
