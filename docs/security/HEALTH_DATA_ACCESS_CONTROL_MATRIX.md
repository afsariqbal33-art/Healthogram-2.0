# Health Data Access Control Matrix

**Version:** 2.2.0  
**Effective Date:** 2026-09-20  
**Security Domain:** Access Control & Data Minimization  

---

## 1. Overview & Role Definitions

Healthogram partitions users into distinct account archetypes with cryptographic and Firestore security rule enforcement:
- **INDIVIDUAL (Patient):** Sovereign owner of their personal Health Passport.
- **DOCTOR:** Licensed individual healthcare practitioner.
- **CLINIC:** Ambulatory care or outpatient clinic organization.
- **HOSPITAL:** Inpatient and emergency medical health system.
- **LABORATORY:** Diagnostic pathology, radiology, or testing facility.
- **PHARMACY:** Prescription dispensation provider (isolated from direct clinical charts).
- **CUSTOMER / SELLER:** Commerce & marketplace participants (zero health passport access).
- **ADMIN / OWNER:** Infrastructure operations and governance.

---

## 2. Resource Access Control Matrix

| Healthcare Resource | INDIVIDUAL (Owner) | DOCTOR (Verified) | CLINIC / HOSPITAL | LABORATORY | PHARMACY | SELLER / CUSTOMER |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Health Profile (Demographics)** | Read / Write | Read (Consent) | Read (Consent) | Read (Consent) | Read (Dispense only) | **DENIED** |
| **Active Conditions** | Read / Write | Read / Write (Consent) | Read / Write (Consent) | Read (Consent) | **DENIED** | **DENIED** |
| **Allergies & Reactions** | Read / Write | Read / Write (Consent) | Read / Write (Consent) | Read (Consent) | Read (Dispense safety) | **DENIED** |
| **Active Medications** | Read / Write | Read / Prescribe (Consent) | Read / Prescribe (Consent) | **DENIED** | Read (Fulfillment) | **DENIED** |
| **Diagnostic Lab Results** | Read / Write (Self) | Read (Consent) | Read (Consent) | Read / Ingest (Order) | **DENIED** | **DENIED** |
| **Vitals & Observations** | Read / Write (Self) | Read (Consent) | Read (Consent) | **DENIED** | **DENIED** | **DENIED** |
| **Health Connect Devices** | Read / Sync / Revoke | Read (Consent) | Read (Consent) | **DENIED** | **DENIED** | **DENIED** |
| **Emergency ICE Card** | Read / Configure | Read (Public / Emergency) | Read (Emergency Break-Glass) | **DENIED** | **DENIED** | **DENIED** |
| **Paper Prescription OCR** | Upload / Confirm | Review / Certify | Review / Certify | **DENIED** | Read (Post-certified) | **DENIED** |
| **Audit Logs** | Read (Own access log) | Append-only (System) | Append-only (System) | Append-only (System) | **DENIED** | **DENIED** |

---

## 3. Scoped Consent Matrix (Category-Level Minimization)

When consent is requested by a clinician or organization, access is restricted strictly to granted categories:

```
[Requested Grant]
  ├── ALLERGIES      --> Grants access ONLY to allergies & adverse reactions
  ├── MEDICATIONS    --> Grants access ONLY to prescription history & regimens
  ├── CONDITIONS     --> Grants access ONLY to active/past clinical diagnoses
  ├── LAB_REPORTS    --> Grants access ONLY to structured diagnostic reports
  ├── VITALS         --> Grants access ONLY to physiological time-series observations
  └── PROCEDURES     --> Grants access ONLY to surgical and intervention histories
```

**Cross-Category Enforcement:**
- A grant for `LAB_REPORTS` explicitly denies queries against `/users/{id}/health_conditions` or `/users/{id}/health_medications`.
- The rule engine evaluates:
  ```
  request.auth.uid in get(/databases/$(database)/documents/users/$(patientUid)/consents/$(grantId)).data.allowedUids
  && category in get(...).data.allowedRecordCategories
  ```

---

## 4. Break-Glass Override Matrix

| Triggering Condition | Authorized Role | Target Data | Mandatory Logging | Alert Dispatched |
| :--- | :--- | :--- | :--- | :--- |
| **Life-Threatening Trauma** | `HOSPITAL_EMERGENCY` | Full Emergency ICE + Critical Labs/Meds | Permanent `EmergencyAccessLog` | Instant SMS & Push to Patient & Next of Kin |
| **Unconscious / Incapacitated** | Verified `DOCTOR` | Blood Group, Allergies, Active Meds | Permanent Audit Justification | Instant Notification + Compliance Flag |
| **Routine Consultation** | Any Provider | **BREAK-GLASS FORBIDDEN** | Violation yields license suspension | N/A (Standard Consent Required) |
