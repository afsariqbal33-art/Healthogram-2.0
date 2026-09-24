# Android Health Connect Architecture & Integration Spec

## 1. Overview & Android 16 Alignment
Healthogram integrates with Android Health Connect as an optional, patient-controlled telemetry bridge. Designed for Android 14+ (API 34) and tested on Android 16 (API 36), it synchronizes fitness and physiological vitals without ever exposing clinical diagnoses back into third-party fitness apps.

---

## 2. Granular Permission Gating

In accordance with Android Health Connect guidelines, granular read permissions are declared in `AndroidManifest.xml` and requested via runtime contracts:
- `androidx.health.permission.Steps:read`
- `androidx.health.permission.HeartRate:read`
- `androidx.health.permission.SleepSession:read`
- `androidx.health.permission.Weight:read`

### Privacy Policy Invariant:
Healthogram explicitly forbids writing private clinical diagnosis records (`Condition`, `MedicationRequest`, `DiagnosticReport`) back into Health Connect's shared datastore. Integration is strictly read-only for physiological telemetry.

---

## 3. Synchronization & Deduplication Lifecycle

```
[Health Connect Provider]
          │
          ▼
 [Permission Discovery] ─── (Verify SDK availability)
          │
          ▼
 [Differential Sync] ─── (Using ChangesToken cursors)
          │
          ▼
 [Deduplication Engine] ─── (Matching clientRecordId & timestamp ± 60s)
          │
          ▼
 [Timeline Ingestion] ─── (Attributed with sourceStatus = WEARABLE_SYNC)
```

1. **Permission Check**: `HealthConnectService.checkPermissions()` evaluates user-granted privileges.
2. **Changes Token Cursor**: Sync operations track an opaque `changesToken` to fetch only differential records created since the last synchronization epoch.
3. **Deduplication**: When ingesting vitals (e.g. Heart Rate or Steps), incoming records are compared against existing `HealthTimelineEntry` items within a 60-second window to prevent duplicate records.
4. **Source Attribution**: All ingested vitals are labeled with:
   - `sourceSystem`: `"Android Health Connect"`
   - `sourceStatus`: `RecordSourceStatus.WEARABLE_SYNC`
   - `verificationStatus`: `"AUTOMATED_WEARABLE"`

---

## 4. Disconnect & Data Erasure Protocol
Users maintain total control over Health Connect integration:
- Disconnecting revokes background synchronization workers.
- The user can select between:
  1. *Retain existing synced records in Health Timeline* (marked as archived telemetry).
  2. *Purge all wearable-synced records* from private Health Passport storage.
