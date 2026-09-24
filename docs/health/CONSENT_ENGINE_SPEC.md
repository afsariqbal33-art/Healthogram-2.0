# Consent Engine & Health Consent Center Specification

## 1. Zero-Trust Healthcare Consent Architecture

Healthogram treats health data with zero-trust principles:
1. **Explicit Granular Consent**: Clinicians, clinics, and laboratories do NOT receive global account access. Access must be granted for specific categories:
   - `ALLERGIES`
   - `MEDICATIONS`
   - `CONDITIONS`
   - `LAB_REPORTS`
   - `VITALS`
   - `PROCEDURES`
2. **Time-to-Live (TTL)**: Grants expire automatically after a predefined duration (e.g. 1 hour, 24 hours, 7 days).
3. **Immediate Revocation**: Patients can revoke active grants with zero delay.
4. **Scope Narrowing**: Patients can dynamically remove individual categories from an active grant without invalidating the remaining session.

---

## 2. Inbound Request & Approval Lifecycle

```
[Clinician / Facility]
          │
          ▼ (Requests access for specific purpose e.g. "TREATMENT")
[Health Consent Center]
          │
          ▼ (Push notification + In-app prompt to patient)
[Patient Review]
   ├── [Reject] ───► Terminal status REJECTED (audit logged)
   └── [Approve with Granular Scope] 
             │
             ▼
[ScopedConsentGrant Issued] ─── (Active status, cryptographic TTL)
             │
             ▼
[Gateway Enforcement] ─── (Filters clinical queries by allowed categories)
             │
             ▼
[Patient Revocation / Expiration] ─── (Immediate access termination)
```

---

## 3. Emergency Break-Glass Override Protocol

In acute, life-threatening emergency trauma scenarios where an unconscious patient cannot confirm access:
1. **Eligible Roles Only**: Only licensed `DOCTOR` or `HOSPITAL_EMERGENCY` accounts with verified identity can initiate break-glass override.
2. **Mandatory Clinical Justification**: Clinician must enter an explicit clinical rationale (minimum 20 characters).
3. **Immutable Auditing**: `ConsentManagementService.performEmergencyAccess()` creates an indelible record in `emergency_access_logs`.
4. **Patient & Guardian Alerts**: High-priority push notifications and SMS alerts are immediately dispatched to the patient and their designated emergency contacts.
