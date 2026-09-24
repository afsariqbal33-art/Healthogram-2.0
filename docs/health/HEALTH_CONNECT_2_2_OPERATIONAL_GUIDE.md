# Health Connect 2.2 Operational & Privacy Architecture

**Version:** 2.2.0  
**Domain:** Android Health Connect & Sensor Telemetry  
**Target API:** Android 16 / API 36  

---

## 1. Overview & Privacy Isolation

Health Connect allows Healthogram patients to import daily physical activity, vitals, sleep cycles, and wellness telemetry from supported smart wearables and digital medical hardware.

### 1.1 Strict Isolation Invariant
Under no circumstances is Health Connect telemetry permitted to cross into:
- Social networking feeds or user stories
- Targeted advertising profiles or marketing attribution engines
- Creator or seller analytics dashboards
- Marketplace search ranking algorithms

All telemetry flows into the sovereign Health Passport timeline with status `DEVICE_VERIFIED` and category `WELLNESS`.

---

## 2. Granular Permission Management (Android 16 Compliant)

Healthogram 2.2 adheres to least-privilege scoping. Users can individually grant or deny specific data types:
- `STEPS`
- `HEART_RATE`
- `SLEEP`
- `EXERCISE`
- `CALORIES`
- `WEIGHT`
- `BLOOD_GLUCOSE`

### 2.1 One-Touch Revocation
Patients can instantly revoke Health Connect access at any time directly from **Health Settings -> Connected Devices**. Revocation:
- Immediately terminates background sync jobs.
- Clears the granted scope set.
- Records an audited revocation event.
- Leaves existing timeline history intact under patient control, with option for full purge.

---

## 3. OEM Battery Resilience & Mitigation Policy

Certain OEM Android skins (e.g., Xiaomi MIUI/HyperOS, Huawei EMUI, Samsung OneUI, OnePlus OxygenOS) aggressively terminate background synchronization services, causing telemetry gaps.

Healthogram implements a two-fold mitigation strategy:
1. **Opportunistic Foreground Synchronization:** Telemetry is proactively synchronized whenever the patient actively opens or views their Health Passport timeline.
2. **Context-Aware In-App Guidance:** The app detects aggressive OEM manufacturers and guides the user step-by-step through granting battery optimization exemptions without using deceptive or intrusive techniques.

| Manufacturer | Known Aggressive Behavior | In-App User Guidance |
| :--- | :--- | :--- |
| **Xiaomi / Poco** | Kills background sync within 5-10 minutes | Guide to: Settings -> Apps -> Healthogram -> Battery Saver -> No Restrictions |
| **Huawei / Honor**| Auto-launch disabled by default | Guide to: Settings -> Battery -> App Launch -> Healthogram -> Manage Manually |
| **Samsung** | Puts apps to sleep after 3 days of no direct launch | Guide to: Settings -> Battery -> Background usage limits -> Never sleeping apps |
| **Generic Android**| Standard Doze Mode | Standard `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` system dialog |

---

## 4. Telemetry Deduplication & Data Quality

Health Connect telemetry records are deduplicated based on `(dataType, timestamp, sourcePackage)`. If multiple fitness trackers record overlapping step counts for identical 15-minute windows:
- Records with device-verified priority take precedence.
- Raw observation values are not double-counted in daily summary aggregates.
- Provenance attributes record both the source package and the timestamp of ingestion.
