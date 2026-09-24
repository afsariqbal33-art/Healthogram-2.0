# ADR-2.2-004: Atomic Appointment Scheduling & Notification Privacy

**Status:** ACCEPTED  
**Date:** 2026-09-20  
**Context:**  
Telehealth and in-person consultations require absolute scheduling consistency (zero double-bookings) and patient privacy. In Step 35, 34,200 appointments were completed with zero double-bookings. As appointment volume increases in 2.2, scheduling must remain atomic across multi-region Firestore deployments, and push notifications must protect clinical confidentiality.

**Decision:**  
1. **Atomic Booking Invariant**: Appointment slot reservations are executed exclusively via Cloud Firestore atomic transactions. If two patients attempt to book the same doctor slot simultaneously, the second transaction fails immediately and triggers an intuitive UI retry.
2. **Timezone Standardization**: All schedule slots are stored in UTC ISO 8601 strings, converted client-side to the local timezone of the clinic or patient.
3. **Notification Privacy**: Outgoing Firebase Cloud Messaging (FCM) payloads must pass through an automated privacy sanitizer. Push alerts displayed on mobile lockscreens must never reveal medical diagnoses, specialist types, or clinical notes (e.g. "You have an appointment with Dr. Al-Balushi tomorrow at 10:00 AM").
4. **Calendar Integration**: Export appointment reminders via standard `.ics` format or the Android Calendar Provider API upon explicit user opt-in.

**Consequences:**  
- **Positive**: Zero double-bookings; complete patient confidentiality; seamless integration with external calendar apps.
- **Negative**: Requires patients to unlock the app to view consultation intake notes and medical pre-requisites.
