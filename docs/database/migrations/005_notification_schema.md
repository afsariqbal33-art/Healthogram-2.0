# DATABASE MIGRATION 005: PRIVACY-PRESERVING NOTIFICATIONS SCHEMA

**Migration ID:** `005_notification_schema`  
**Target Environment:** Development, Staging, Production  
**Author:** Healthogram Infrastructure & Privacy Architect  

---

## 1. Overview
Introduces notification dispatch architecture enforcing strict PHI omission from push payloads, deduplication keys, and ephemeral device token rotation.

## 2. Collections Created
* `/notifications/{notificationId}`: In-app user notifications (masked preview text).
* `/device_tokens/{uid_tokenId}`: Active FCM / APNs registration tokens with last active timestamp.
* `/notification_preferences/{uid}`: Granular user notification settings (quiet hours, channels).

## 3. Privacy Rule Enforcement
* Push notification messages must NEVER contain clinical diagnoses, laboratory values, or medication names.
* Only generic alert messages (e.g. *"You have a new update in your Health Passport"*) are dispatched through external push gateways.
