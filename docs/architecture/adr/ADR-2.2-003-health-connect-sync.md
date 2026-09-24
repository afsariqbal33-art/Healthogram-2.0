# ADR-2.2-003: Android Health Connect Scoped Sync & Battery Resiliency

**Status:** ACCEPTED  
**Date:** 2026-09-20  
**Context:**  
Wearable integration via Android Health Connect is vital for chronic care monitoring. However, aggressive battery management on OEM devices (Xiaomi, Huawei) terminates background WorkManager sync jobs, causing data gaps. Furthermore, over-requesting permissions damages patient trust.

**Decision:**  
1. **Scoped Data Scopes**: Restrict requested Health Connect permissions strictly to `StepsRecord`, `HeartRateRecord`, and `BloodGlucoseRecord`.
2. **Sync Frequency & Constraints**: Background periodic sync set to 6 hours with `BatteryNotLow` and `NetworkType.CONNECTED` constraints.
3. **Resilience Strategy**:
   - Utilize AndroidX `WorkManager` expedited work requests where supported.
   - Execute an opportunistic non-blocking synchronization pass whenever the patient actively opens the Health Passport screen.
   - Present a gentle one-time Material 3 guidance dialogue on OEM devices to advise users to whitelist Healthogram from battery killers.
4. **Deduplication**: Ingested records compute a deterministic client-side hash `SHA-256(type + timestamp + value)` to eliminate duplicate insertions.

**Consequences:**  
- **Positive**: High sync reliability across all device manufacturers; minimal battery consumption; transparent user permissions.
- **Negative**: Biometric data updates in the cloud are near-real-time (6-hour delay) rather than instant streaming.
