# FHIR Production Operations & Gateway Runbook

**Document:** `docs/health/FHIR_PRODUCTION_OPERATIONS.md`  
**Standard:** HL7 FHIR Release 4 (R4)  
**Gateway Engine:** Healthogram Healthcare Interoperability Gateway 2.1  
**Target Environments:** `FHIR_SANDBOX` vs. `FHIR_PRODUCTION`  
**Status:** PRODUCTION STANDARD  

---

## 1. Environment Partitioning & Isolation

| Parameter | `FHIR_SANDBOX` | `FHIR_PRODUCTION` |
| :--- | :--- | :--- |
| **Network Endpoint** | `https://fhir-sandbox.healthogram.com/r4` | `https://fhir-api.healthogram.com/r4` |
| **Data Policy** | 100% Synthetic Deterministic Patients Only | Live Encrypted Patient Clinical Data |
| **Real Patient Ingestion** | **STRICTLY FORBIDDEN** (Automated rejection) | Permitted solely under active patient consent |
| **Credentials & Keys** | Generated from Test Key Vault (Rotated monthly) | GCP Secret Manager; mTLS Client Certificates |
| **Credential Reuse** | **ZERO TOLERANCE**: Sandbox keys blocked in Prod | Production keys immediately revoked if seen in Sandbox |
| **Rate Limits** | 60 requests / minute | 300 requests / minute (Dynamic burst allowed) |

---

## 2. Invariant Data Integrity Control & Versioning

To preserve clinical and legal defensibility, **no historical FHIR mapping is ever silently updated**.

### Transaction Ledger Schema
Every FHIR exchange event (Import or Export) creates an immutable transaction record:
```json
{
  "transactionId": "tx_fhir_8839210948",
  "timestamp": "2026-09-20T01:15:30.120Z",
  "direction": "IMPORT",
  "mappingVersion": "2.1.0-fhir-r4-v3",
  "sourceSystem": "org_hospital_royal_muscat",
  "destinationSystem": "healthogram_patient_timeline",
  "patientUid": "usr_patient_om_7741",
  "consentReference": "cns_grant_9918234",
  "authorizationActorUid": "doc_khalfan_4401",
  "resourceType": "Observation",
  "validationResult": "PASS",
  "deltaStatus": "NEW_RECORD",
  "hashSha256": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
}
```

### Mapping Version Rules
1. Current active mapping version: `2.1.0-fhir-r4-v3`.
2. Any adjustment to terminologies (LOINC, SNOMED-CT, RxNorm, ICD-10) increments the mapping version to `v4`.
3. Historical records maintain their original `mappingVersion` and hash digest for clinical audit integrity.

---

## 3. Production Monitoring Metrics & Thresholds

| Metric Name | Prometheus / Cloud Monitoring Key | Warning Level | Critical Incident Trigger |
| :--- | :--- | :--- | :--- |
| **FHIR Request Volume** | `fhir_requests_total` | > 10,000 req/min | > 25,000 req/min |
| **Export Failure Rate** | `fhir_export_failures_ratio` | > 0.5% | > 2.0% (P1 Incident) |
| **Import Validation Failures**| `fhir_import_validation_failures_ratio` | > 1.0% | > 3.0% (Partner Warning) |
| **Mapping Translation Errors**| `fhir_mapping_error_count` | > 5 errors / hr | > 20 errors / hr (P1) |
| **Auth / Consent Failures** | `fhir_unauthorized_attempts_total` | > 15 / hr | > 50 / hr (Possible Attack) |
| **P95 Processing Latency** | `fhir_processing_duration_seconds` | > 150ms | > 400ms (SRE Throttle) |
| **Partner Availability** | `fhir_partner_endpoint_up` | < 99.5% | < 98.0% (Auto Circuit Break) |

---

## 4. External Provider Circuit Breakers & Backoff

External EHR endpoints (Epic/Cerner/Lab) are wrapped by the resilient `HealthcareInteropGateway`:
- **Circuit Breaker States:** `CLOSED` (Normal), `OPEN` (Tripped after 5 consecutive failures), `HALF-OPEN` (Probing after 60s cooldown).
- **Retry Schedule:** Exponential backoff with jitter: 1s, 2s, 4s, 8s, 16s (Max 5 attempts).
- **Idempotency:** HTTP Header `Idempotency-Key` enforced on all write and order operations.
