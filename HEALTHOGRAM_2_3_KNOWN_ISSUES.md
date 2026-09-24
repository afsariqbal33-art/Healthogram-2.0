# HEALTHOGRAM 2.3 — KNOWN ISSUES & LIMITATIONS REGISTER

**Document ID:** HGM-PROD-KNOWN-ISSUES-230  
**Effective Release:** 2.3.0 (versionCode 23000)  
**Status Date:** 2026-09-23  

---

## 1. Register Overview
In accordance with zero-defect engineering governance, Healthogram maintains a transparent known-issues register. There are **zero unresolved P0 or P1 release-blocking defects** in the 2.3.0 release candidate. All items listed below are non-blocking external provider dependencies or future minor enhancements.

---

## 2. Issues Log

### ISSUE-230-01: Live Hospital EMR Direct Sync Requires External Hospital Mutual TLS
* **ID:** `ISSUE-230-01`
* **Title:** Live Hospital EMR Integration Requires External Mutual TLS Certificate Exchange
* **Severity:** `P2 (External Dependency)`
* **Area:** Healthcare Interoperability (FHIR R4)
* **Description:** While all 9 HL7 FHIR R4 resource models (Patient, Condition, Observation, AllergyIntolerance, Immunization, DiagnosticReport, Encounter, CarePlan, MedicationRequest) are fully implemented and validated against HL7 JSON schemas, connecting to a live hospital's Epic or Cerner server requires organization-specific mutual TLS (mTLS) client certificates and vendor network whitelisting.
* **Impact:** In the absence of an external hospital integration agreement, clinical records must be imported via user document upload or entered directly by licensed doctors on the platform.
* **Workaround:** Patients can manually input or upload diagnostic reports and prescriptions via the Health Passport camera/file scanner.
* **Status:** `REQUIRES EXTERNAL PROVIDER`
* **Owner:** Healthcare Integration Lead
* **Target Version:** `2.4.0` (First hospital enterprise pilot)

---

### ISSUE-230-02: Commercial Carrier Live Webhooks Depend on Carrier API Contract
* **ID:** `ISSUE-230-02`
* **Title:** Commercial Courier Real-Time Vehicle Telemetry Awaits Third-Party Fleet API Provisioning
* **Severity:** `P2 (External Dependency)`
* **Area:** Delivery & Logistics
* **Description:** The courier tracking engine supports simulated and internal driver apps with 5-second location throttling and delivery PIN verification. Integration with commercial nationwide carriers (e.g. DHL, Aramex, FedEx) requires production API keys and registered webhook endpoints in each destination country.
* **Impact:** For orders fulfilled by external commercial couriers without native API keys, tracking is updated via milestone status events (Dispatched, In Transit, Delivered) rather than live GPS map breadcrumbs.
* **Workaround:** Milestone-based tracking and courier tracking URL redirection are active.
* **Status:** `REQUIRES EXTERNAL PROVIDER`
* **Owner:** Delivery Operations Lead
* **Target Version:** `2.3.1`

---

### ISSUE-230-03: Google Play Android Vitals Baseline Initial Data Delay
* **ID:** `ISSUE-230-03`
* **Title:** Android Vitals Dashboard Metric Gathering Period
* **Severity:** `P3 (Informational)`
* **Area:** Google Play Quality Metrics
* **Description:** Android Vitals metrics (user-perceived crash rate, ANR rate) in the Google Play Developer Console require an initial threshold of active user sessions before statistical aggregations appear in the dashboard.
* **Impact:** Metric shows as `DATA NOT AVAILABLE` during the first 24 to 48 hours of initial 5% canary deployment.
* **Workaround:** Client-side Firebase Crashlytics and Performance Monitoring provide real-time crash and ANR visibility from minute zero.
* **Status:** `DATA NOT AVAILABLE (Normal Cold Start)`
* **Owner:** Senior Android Release Engineer
* **Target Version:** `2.3.0` (Will auto-resolve as sessions accumulate)

---

### ISSUE-230-04: Offline Health Passport Cache Size Cap on Low-Storage Devices
* **ID:** `ISSUE-230-04`
* **Title:** Automatic Pruning of High-Resolution Scanned Document Cache on Low-Storage Devices
* **Severity:** `P3 (Minor Optimization)`
* **Area:** Storage & Caching
* **Description:** On devices with less than 500MB available free disk space, cached local copies of encrypted high-resolution MRI/CT scan PDFs may be evicted to preserve device stability. Metadata and thumbnail summaries remain permanently cached.
* **Impact:** Viewing large multi-megabyte PDF attachments while fully offline may require pre-downloading or re-connecting to network.
* **Workaround:** A clear in-app prompt indicates "Document available in cloud vault — tap to download for offline access".
* **Status:** `VERIFIED (Intended Behavior)`
* **Owner:** Mobile Lead
* **Target Version:** `2.4.0` (User-configurable offline storage quotas)
