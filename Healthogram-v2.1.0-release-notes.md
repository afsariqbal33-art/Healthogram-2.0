# Healthogram 2.1.0 Release Notes

**Version:** 2.1.0 (Build 20100)  
**Release Date:** September 2026  
**Target Platform:** Android 16 (API 36) / Minimum Android 7.0 (API 24)  
**Distribution:** Google Play Production Staged Rollout  

---

## What's New in Healthogram 2.1

### 🏥 Health Passport 2.1 & Zero-Trust Consent
- **Granular Category Consent:** Patients now hold complete control over their medical history. Grant access selectively to Lab Reports, Vitals, Allergies, Medications, or Conditions for specific hours or consultation windows.
- **Ephemeral QR Consultations:** Generate dynamic 24-byte single-use QR codes that expire automatically after 15 minutes, allowing safe, in-person clinical consultations without sharing private passwords or IDs.
- **Instant Revocation:** Revoke practitioner or clinic access instantly with one tap from the Consent Center.
- **Full Clinical Provenance:** Every record clearly displays authoring clinic, timestamp, and verification hash.

### 🌐 HL7 FHIR R4 Interoperability
- **Self-Service Export:** Securely export your medical timeline in standardized HL7 FHIR R4 JSON bundles or formatted clinical PDF summaries.
- **Partner Interoperability Gateway:** Seamlessly receive verified diagnostic reports from accredited hospital and laboratory partners under active patient authorization.

### ⌚ Android Health Connect Synchronization
- **Opt-In Biometrics:** Securely connect your wearable devices through Android Health Connect to track steps, heart rate, and blood glucose directly in your personal health timeline.
- **Complete Privacy Airgap:** Your fitness and health telemetry is strictly private. It is never used for advertising, recommendations, or public sharing.

### 📅 Multi-Provider Clinical Scheduling
- **Universal Booking:** Schedule in-person and teleconsultation appointments with verified Doctors, Clinics, Hospitals, and Diagnostic Laboratories.
- **Privacy-Safe Reminders:** Push notifications and reminders are designed with zero diagnostic disclosure on lock screens.

### 🛡️ Enterprise Security & Architectural Hardening
- **Target Android 16 (API 36):** Built natively for the latest Android operating system security standards.
- **Firebase App Check with Play Integrity:** Strict backend verification ensuring all requests originate from untampered, genuine app instances.
- **Biometric Hardware Protection:** AES-256-GCM hardware keystore encryption for sensitive documents and credentials.
- **Device Session Management:** Concurrent session ceiling of 4 devices with real-time active session monitoring.

### 🌍 Multilingual & Regional Excellence
- **Native Arabic RTL:** Comprehensive right-to-left layout and localized terminology across all clinical and marketplace screens.
- **Multi-Currency Engine:** Native support for OMR, SAR, AED, and USD with minor-unit accounting.

---

## Important Notice
*Healthogram is a personal health record management and appointment booking platform. It does not provide medical diagnoses, treatment advice, or emergency response services. Always consult a qualified medical professional for health concerns.*
