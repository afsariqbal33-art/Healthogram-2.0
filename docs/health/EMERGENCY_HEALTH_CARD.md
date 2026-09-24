# Emergency Health Card Specification

## 1. Principles & Threat Model

The **Emergency Health Card** is an opt-in, life-saving convenience tool designed for emergency medical responders (EMTs, paramedics, emergency physicians). 

### Security Threat Model & Defense:
- **Threat**: Anyone who photographs an emergency QR code or views the lock screen could inspect the patient's entire medical record (psychiatric evaluations, sensitive diagnoses, prescription history).
- **Defense**: **The emergency QR code NEVER exposes the complete Health Passport.** It transmits only a minimalist, strictly filtered emergency card payload with an explicit non-diagnostic disclaimer.

---

## 2. Allowed Emergency Attributes

Only the following attributes may appear on the Emergency Health Card and within the Emergency QR payload:
1. **Emergency Contacts**: Name, relationship, and telephone number of designated primary contacts.
2. **Critical Allergies**: Anaphylactic or life-threatening drug/food allergies (e.g. Penicillin, Peanuts, Latex).
3. **Critical Medications**: Time-sensitive medications (e.g. Insulin, Anticoagulants, EpiPen).
4. **Blood Group**: Optional, displayed only if user explicitly toggles `includeBloodGroup = true`.
5. **Critical Conditions**: Conditions directly affecting resuscitation or acute triage (e.g. Severe Asthma, Epilepsy, Pacemaker).
6. **Emergency Instructions**: Free-text user directives (e.g. "Diabetic shock protocol", "Deaf/Hard of hearing").

---

## 3. Dynamic Revocation & Access Auditing

1. **Instant Revocation**: Toggling opt-in off immediately rotates and invalidates the `qrSessionToken`. Scanners reading the previous QR code will receive an HTTP 410 Gone / Revoked response.
2. **Access Auditing**: Every scan of an emergency card is logged in `EmergencyHealthCardService` recording timestamp, IP address, and user agent to detect unauthorized access.
