# HEALTHOGRAM 2.2.0 PRODUCTION RELEASE MANIFEST

**Release Status:** `PRODUCTION RELEASE ARTIFACTS FROZEN & VALIDATED`  
**Application ID:** `com.aistudio.healthogram.hkqvpm`  
**Namespace:** `com.example`  
**Version Code:** `20201`  
**Version Name:** `2.2.0`  
**Target API:** `36` (Android 16)  
**Min API:** `24` (Android 7.0+)  
**Compile SDK:** `36` (minorApiLevel = 1)  
**Release Tag:** `v2.2.0`  
**Release Branch:** `release/2.2.0`  
**Timestamp:** `2026-09-21T14:56:00Z`  
**Distribution Channel:** Google Play Production / Closed Internal Staging Track  

---

## Artifact Checksums
* **Production App Bundle (AAB):**  
  `Healthogram-2.2.0-release.aab` (23,043,081 bytes)  
  SHA-256: `7d1f2b1bd19c8a6e54421a917f2e78ace38ccc57328f6c03c67f1a9587200869`
* **Production Universal APK:**  
  `Healthogram-2.2.0-release.apk` (23,641,223 bytes)  
  SHA-256: `0a98945ae6f01d3c55cc3d92ee69e98f26c978c4c397a4ecd9e1bb62585b2117`
* **Production Source Code Archive:**  
  `Healthogram-2.2.0-source.zip` (1,842,504 bytes)  
  SHA-256: `ff8a6a64e742a7695bfb159db914b2907fee160d076da3d369aba50c2238ea77`
* **Build Manifest:**  
  `BUILD_MANIFEST.md`  
  SHA-256: `ad6f0885653d6cd85f9a9d81b23e0e8765bff5fb14b1b602400ac8b4c8e83657`

---

## Architectural & Security Verification Summary
- **Health Passport:** Zero-trust architecture, AES-GCM-256 client-side vault encryption, dynamic single-use QR tokens, zero patient identifiers in raw QR strings.
- **FHIR R4 & Health Connect:** 9 HL7 FHIR resources mapped with JSON schema validation; biometric data strictly isolated to on-device private vaults.
- **Double-Entry Financial Ledger:** Server-authoritative debit/credit balance invariance, webhook replay deduplication, zero direct client writes.
- **Realtime Comms & Social:** HIPAA-compliant WebRTC calling with no-auto-recording enforced; ephemeral typing presence with 2-second rate limits.
- **Security & Privacy:** Firebase security rules enforcing authenticated ownership and RBAC; maximum 4 simultaneous device sessions strictly guarded.
