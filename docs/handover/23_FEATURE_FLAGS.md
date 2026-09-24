# 23 — CENTRALIZED FEATURE FLAGS & EMERGENCY KILL SWITCHES

## 1. Feature Flag Architecture
Feature toggles are managed through Firebase Remote Config with cached fallback values bundled in the APK:
* **Scopes Supported:**
  - Global (All users)
  - Country-specific (e.g. `allowed_countries: ["SA", "AE", "US", "GB", "EG", "IN"]`)
  - Account Category (`INDIVIDUAL`, `DOCTOR`, `CLINIC`, `HOSPITAL`, `LABORATORY`)
  - Role-specific (`CUSTOMER`, `SELLER`)

## 2. Emergency Kill-Switches (Remote Config Manifest)
In the event of an upstream provider outage or security anomaly, individual platform subsystems can be isolated in real-time:
* `marketplace_killswitch`: Disables new cart additions and checkout while leaving past orders viewable.
* `payments_processing_killswitch`: Suspends payment gateway tokenization.
* `webrtc_calling_killswitch`: Disables incoming and outgoing audio/video calls with a user-friendly maintenance banner.
* `ai_generation_killswitch`: Hides AI studio creative tools, reverting to standard manual editing.
* `fhir_sync_killswitch`: Halts automated EMR synchronization.
* `delivery_dispatch_killswitch`: Freezes real-time courier dispatching.
* `emergency_maintenance_mode`: Overlays a full-screen maintenance message across all application flows.
