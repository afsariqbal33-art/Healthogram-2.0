# 07 — SOVEREIGN HEALTH PASSPORT

## 1. Architectural Philosophy: Sovereign Patient Ownership
The Health Passport is built on the invariant that personal medical data belongs solely to the individual user. The platform operators cannot view, sell, or analyze clinical records.

## 2. Vault Contents & Data Taxonomy
* **Conditions & Diagnoses:** Chronic and acute conditions (ICD-10 / SNOMED mapped).
* **Allergies & Intolerances:** Environmental, pharmaceutical, and dietary sensitivities with severity levels.
* **Medications & Prescriptions:** Active prescriptions, dosages, prescribing physician, and refill dates.
* **Longitudinal Vitals:** Blood pressure, heart rate, blood glucose, body temperature, oxygen saturation.
* **Diagnostic & Lab Reports:** PDF laboratory reports and doctor consultation summaries.
* **Immunization Records:** Vaccines, lot numbers, administration dates, and healthcare provider IDs.

## 3. Cryptographic Storage Pipeline
```text
Plaintext Medical Record
        │
        ▼ (AES-GCM-256 with 96-bit random IV)
  Ciphertext + 128-bit Authentication Tag
        │
        ▼ (TLS 1.3 Transport)
Cloud Firestore Vault Document (/health_passports/{uid})
```
* **Decryption:** Decryption keys are derived from the patient's sovereign PIN + hardware Keystore master key. Server never holds plaintext or keys.
* **Zero PHI in Logs:** Cloud Function logs, Crashlytics reports, and analytics pipelines are completely scrubbed of clinical text.
