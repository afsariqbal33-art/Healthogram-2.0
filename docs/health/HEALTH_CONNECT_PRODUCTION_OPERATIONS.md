# Android Health Connect Production Operations Runbook

**Document:** `docs/health/HEALTH_CONNECT_PRODUCTION_OPERATIONS.md`  
**API Layer:** AndroidX Health Connect 1.1.0 Client Library  
**Target Platform:** Android 14+ (Built-in Framework) & Android 9-13 (Health Connect APK)  
**Status:** PRODUCTION STANDARD  

---

## 1. Compliance Principles & Mandatory Airgaps

1. **Strictly Opt-In:** Health Connect integration is entirely optional. The app never prompts for permissions until the user explicitly navigates to "Connect Devices" and taps "Link Health Connect".
2. **Runtime Granular Scopes:** Users grant or deny permissions per biometric data type (`STEPS`, `HEART_RATE`, `BLOOD_GLUCOSE`, `SLEEP_SESSION`).
3. **Absolute Commercial & Social Airgap:**
   - **Zero Advertising Use:** Telemetry is completely excluded from user profiling, ad networks, and audience segmentation.
   - **Zero Recommendation Ingestion:** The algorithmic feed and seller recommendation engines cannot query or access Health Connect tables.
   - **Zero Social Exposure:** Health Connect metrics cannot be automatically shared in public social posts or community challenges without separate explicit user composition.

---

## 2. Telemetry Ingestion Architecture

```
[Android Health Connect Storage Enclave]
                     │
                     ▼ (Explicit Runtime Permission Check)
        [HealthConnectService]
          - Composite Key Deduplication Cache
          - Scoped Record Mapper
                     │
          ┌──────────┴──────────┐
          ▼                     ▼
[Local Encrypted Room DB]   [Health Timeline Feed]
(Device-Only At-Rest Enc)   (Private Clinician-Consented View)
          │
          ✕ (STRICT ARCHITECTURAL AIRGAP)
┌─────────┴───────────────────────┐
▼                                 ▼
[Unified Search Engine]   [Social & Ads Pipeline]
(Denied Indexing)         (Hardcoded Block)
```

---

## 3. Disconnect & Revocation Protocol

1. **Instant Revocation:** When a user taps "Disconnect Health Connect" or toggles permissions off in Android system settings:
   - `HealthConnectService.revokeConnection(patientUid)` fires immediately.
   - Connection status transitions to `REVOKED`.
   - Active sync jobs in Android WorkManager are canceled immediately.
2. **Data Retention & Purge Options:**
   - User is presented with two clear choices:
     - *Keep Existing Records:* Synced historical entries remain in the private Health Passport.
     - *Purge All Synced Records:* All historical entries tagged `RecordSourceStatus.HEALTH_CONNECT` are permanently erased from the local and remote stores.

---

## 4. Production Operational Monitoring Metrics

| Metric Key | Target SLO | Warning Trigger | Action on Alert |
| :--- | :--- | :--- | :--- |
| `health_connect_connected_users` | Monitored volume | Sudden 20% drop | Investigate system update compatibility |
| `health_connect_sync_success_rate`| >= 99.8% | < 99.0% | Review device WorkManager execution logs |
| `health_connect_duplicate_rate` | < 0.1% | > 0.5% | Inspect deduplication hash composite key |
| `health_connect_revocation_latency`| < 500ms | > 2000ms | Verify local StateFlow broadcast queue |
| `health_connect_unsupported_records`| 0 | > 5 / day | Check for new Android OS health record types |
| `health_connect_airgap_violations`| **0 (Zero)** | **> 0 (CRITICAL P0)**| **Immediate feature kill switch + Forensic audit** |
