# HEALTHOGRAM 2.3.0 RELEASE CANDIDATE (v2.3.0-rc1) RELEASE NOTES

**Version Name:** `2.3.0`  
**Version Code:** `23000`  
**Release Candidate:** `v2.3.0-rc1`  
**Target SDK:** `36` (Android 16)  
**Distribution Track:** Google Play Internal Testing Track  
**Submission Date:** September 2026  

---

## Highlights of Healthogram 2.3.0

### 1. Refined Healthcare Identity & Governance
* **Strict 5 Healthcare Account Types:** The system strictly standardizes on five licensed healthcare account categories: `INDIVIDUAL`, `DOCTOR`, `CLINIC`, `HOSPITAL`, and `LABORATORY`.
* **Independent Marketplace Governance:** Pure separation between healthcare provider accounts and e-commerce marketplace accounts (`CUSTOMER` and `SELLER`).
* **Session Hardening & Security Ceiling:** Hard limit of 4 active concurrent device sessions per account, automatic token invalidation upon password reset or security revocation, and 15-minute idle session timeout.

### 2. Sovereign Health Passport v2.3
* **Client-Side Envelope Encryption:** Medical records, conditions, medications, allergies, and vitals protected by AES-GCM-256 encryption.
* **Single-Use Scoped QR Consent:** Share clinical records with verified practitioners via time-limited QR codes (60s TTL) with instant, one-tap patient revocation.
* **Zero Medical Data in QR:** QR tokens contain only cryptographic session identifiers, preventing visual eavesdropping or unauthorized camera capture of clinical data.

### 3. Healthcare Interoperability & Android Health Connect
* **HL7 FHIR R4 Ingestion Engine:** Production validation across 9 critical FHIR resource types (Patient, Condition, Observation, AllergyIntolerance, Immunization, DiagnosticReport, Encounter, CarePlan, MedicationRequest).
* **Android Health Connect Integration:** Local on-device synchronization for daily steps, heart rate, sleep cycles, and active calories with strict runtime permission gating.

### 4. Marketplace & Financial Ledger Integrity
* **Double-Entry Financial Accounting:** Every purchase, platform commission split, escrow hold, and seller payout is recorded in an immutable double-entry ledger with zero unbacked currency creation.
* **Strict Domestic Isolation:** International cross-border trade is disabled by default to comply with regional healthcare and pharmaceutical import regulations.
* **Server-Authoritative Stock Locking:** Real-time atomic inventory reservations eliminate overselling during high-concurrency checkouts.

### 5. Verified Teleconsultation & Secure Messaging
* **Encrypted WebRTC Consultations:** Direct peer-to-peer audio and video calling with strictly enforced privacy defaults (zero automated call recording).
* **Ephemeral Chat Indicators:** Ephemeral typing presence and delivery status without database write thrashing.
* **Masked Push Notifications:** Notifications never expose clinical terminology or sensitive medical identifiers on the lock screen.

### 6. Android 16 (API 36) & Modern Compose Experience
* **Adaptive Window Sizes:** Fluid responsive layout dynamically adjusting between phone, foldable, and tablet screens.
* **Full Arabic RTL Support:** Native Right-to-Left bidirectional layout rendering across every screen.
* **Accessibility Certified:** 100% compliant with 48dp touch targets, TalkBack content descriptions, and high-contrast color schemes.
