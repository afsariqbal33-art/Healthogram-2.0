# ADR-2.2-002: HL7 FHIR R4 Integration & Lab ServiceRequest Pipeline

**Status:** ACCEPTED  
**Date:** 2026-09-20  
**Context:**  
Healthogram 2.1 validated 16 normative HL7 FHIR Release 4 models with high export/import success rates. In Healthogram 2.2, clinical partners require automated diagnostic lab ordering, where a clinic doctor can dispatch a diagnostic order to an external accredited laboratory, and the laboratory can push signed diagnostic reports directly into the patient's Health Passport.

**Decision:**  
1. Adopt the normative HL7 FHIR R4 `ServiceRequest` resource as the standard format for clinical diagnostic and imaging orders.
2. The ordering flow requires:
   - Doctor creates `ServiceRequest` specifying LOINC test code and patient identifier.
   - Lab partner retrieves pending orders via mTLS-secured REST endpoint.
   - Upon test completion, lab partner pushes a `DiagnosticReport` FHIR bundle containing test observations and verified clinician signature.
   - Healthogram ingestion gateway validates schema, verifies active patient consent, and commits the result to the patient's Health Passport.
3. Healthcare partners must attain Level 6 certification before gaining access to live `ServiceRequest` endpoints.

**Consequences:**  
- **Positive**: Standards-compliant bi-directional clinical workflows; eliminates paper lab reports; ensures full audit provenance.
- **Negative**: Requires strict partner conformance testing in our FHIR sandbox environment before production activation.
