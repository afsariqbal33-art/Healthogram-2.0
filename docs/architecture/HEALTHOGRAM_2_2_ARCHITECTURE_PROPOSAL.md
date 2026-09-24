# Healthogram 2.2 — Architecture Evolution Proposal

**Document:** `docs/architecture/HEALTHOGRAM_2_2_ARCHITECTURE_PROPOSAL.md`  
**Governing Lead:** Principal Software Architect, Senior Full-Stack Engineer, Lead Cloud Architect  
**Review Target:** Architecture Review Board (ARB)  
**Status:** PROPOSED & EVIDENCE-DRIVEN  

---

## 1. Architectural Stability Assessment & Principle of Non-Rewrite

> **MANDATORY ARCHITECTURAL DIRECTIVE:** Based on comprehensive production evidence collected in Step 35 (99.94% crash-free rate, 22ms P95 Health Passport latency, $475.84 monthly cloud spend), the core Healthogram 2.1 architecture is **exceptionally robust and scalable**. There is **ZERO technical or commercial justification for a fundamental architectural rewrite**. Version 2.2 will execute as an evolutionary, modular enhancement of existing microservices and client models.

---

## 2. Production Bottleneck Analysis & Targeted Enhancements

```
[Current State: v2.1 Architecture]
  Mobile Client (Android 16 Compose + Room Local Delta Cache)
         │
         ▼ (Firebase App Check / Play Integrity)
  Cloud API Gateway & Cloud Functions (Node 20 Modular)
         │
         ├───────────────────────┬───────────────────────┐
         ▼                       ▼                       ▼
  Cloud Firestore         Cloud Storage           External Gateways
  (Multi-Region eur3/us)  (AES-256 Blobs)         (mTLS FHIR R4 & Stripe)
```

### Proposed Targeted 2.2 Enhancements:

### A. Real-Time Laboratory FHIR Pipeline (`FHIRServiceRequestEngine`)
- **Current Pattern:** External laboratories upload diagnostic reports as standalone static documents.
- **2.2 Enhancement:** Introduce asynchronous pub/sub messaging via Cloud Tasks for bi-directional `ServiceRequest` fulfillment. Diagnostic orders automatically progress from `PLACED` -> `SAMPLE_COLLECTED` -> `ANALYZING` -> `RESULT_ATTACHED` -> `PATIENT_NOTIFIED`.

### B. Scalable FHIR Bundle Streaming
- **Current Pattern:** Client requests full FHIR bundle; Cloud Function generates full JSON in-memory before returning.
- **2.2 Enhancement:** For records exceeding 200 clinical events, implement NDJSON (Newline Delimited JSON) chunked streaming directly from Cloud Storage, reducing Cloud Function memory footprint from 512MB to 128MB.

### C. Database Index & Query Optimization
- **Current Pattern:** Multiple composite indexes per marketplace filter combination.
- **2.2 Enhancement:** Consolidate sparse marketplace query indexes by utilizing client-side memory filtering on small result sets (< 50 items), preventing Firestore composite index limits from being breached.

### D. Offline Clinical Viewing Cache (Local Room Engine)
- **Current Pattern:** Diagnostic PDF viewing requires an active network connection.
- **2.2 Enhancement:** Implement an encrypted on-device SQLite blob store for the patient's 5 most recent diagnostic reports, enabling emergency clinical viewing during total network outages.

---

## 3. Account Hierarchy & Invariant Protection

The Version 2.2 architecture maintains absolute fidelity to our core identity model:
1. **Core Account Types:** `Individual`, `Doctor`, `Clinic`, `Hospital`, `Laboratory`.
2. **Marketplace Roles:** `Customer`, `Seller`.
3. **Prohibited Entities:** Zero pharmacy, medicine store, medicine distributor, or medical equipment manufacturer accounts will ever be permitted into the schema.
