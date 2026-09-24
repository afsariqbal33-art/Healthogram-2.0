# 11 — ANDROID HEALTH CONNECT ON-DEVICE INTEGRATION

## 1. Overview & Privacy Principles
Healthogram integrates with Android Health Connect to facilitate local on-device synchronization of fitness and wellness metrics without routing raw data through third-party servers.

## 2. Supported Record Types
* **Steps Record:** Daily step counts and hourly cadence.
* **Heart Rate Record:** Resting and active heart rate samples (bpm).
* **Sleep Session Record:** Sleep stages (Deep, Light, REM, Awake) and duration.
* **Active Calories Burned Record:** Daily active energy expenditure (kcal).

## 3. Runtime Permissions & User Control
* **Explicit Runtime Consent:** Permissions (`androidx.health.connect.client.permission`) are requested individually with in-app rationales.
* **Granular Toggles:** Users can toggle individual metric synchronization ON or OFF at any time in `Settings -> Health Connect`.
* **Zero Cloud Sync Without Authorization:** Metrics read from Health Connect remain on-device unless the user explicitly enables cloud vault backup.
