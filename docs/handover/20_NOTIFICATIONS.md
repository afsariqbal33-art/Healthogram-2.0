# 20 — PUSH NOTIFICATIONS & LOCK-SCREEN PRIVACY

## 1. Notification Pipeline & FCM
* **High-Priority Data Payloads:** Push notifications are delivered via Firebase Cloud Messaging (FCM) using data-only payloads, allowing client-side background handlers to format and render alerts safely.
* **Notification Channels:**
  - `channel_appointments`: Appointment reminders and status updates.
  - `channel_messages`: Direct patient-doctor and peer-to-peer chat alerts.
  - `channel_orders`: Marketplace shipping, dispatch, and delivery updates.
  - `channel_security`: New device login, session expiration, and MFA alerts.

## 2. Lock-Screen Privacy Masking Invariant
* **Zero PHI on Lock Screen:** In compliance with patient confidentiality standards, push notifications rendered on the Android lock screen **never** display sensitive clinical terms, prescription names, or diagnosis details.
* **Masked Content Example:** Instead of *"Your Metformin prescription was renewed"*, the lock screen displays *"Healthogram: You have an update to your medical records"*.
