# 10 — HL7 FHIR R4 CLINICAL INTEROPERABILITY

## 1. Supported HL7 FHIR R4 Resource Types
The Healthogram FHIR ingestion and export engine supports 9 core standard clinical resources:
1. **Patient:** Demographics, identifiers, contact points (HIPAA / GDPR minimized).
2. **Condition:** Clinical status (active, relapse, remission), verification status, SNOMED CT / ICD-10 codings.
3. **Observation:** Quantitative vital signs (blood pressure, heart rate, BMI, glucose).
4. **AllergyIntolerance:** Clinical manifestations, criticality, allergen substance codes.
5. **Immunization:** Vaccine codes (CVX / ATC), occurrence dates, lot numbers.
6. **DiagnosticReport:** Laboratory reports, panel summaries, attached encrypted PDF references.
7. **Encounter:** Clinical consultation records, service provider, period, reason code.
8. **CarePlan:** Treatment regimens, follow-up instructions, provider contacts.
9. **MedicationRequest:** Prescription instructions, dosage timings, route of administration, prescriber reference.

## 2. Ingestion & Conflict Resolution Invariant
* **No Silent Overwrite:** External FHIR records imported into Healthogram never silently overwrite existing clinical records.
* **Patient Confirmation Gate:** Discrepancies between hospital records and local entries are flagged for patient or clinical review.
* **Audit Trail:** Every FHIR import event is logged with source EMR URI, timestamp, and signature validation status.
