# HEALTHOGRAM 2.2.0 GOOGLE PLAY REVIEWER ACCESS CREDENTIALS & TEST GUIDE

**Effective Date:** 2026-09-21  
**Target Release:** 2.2.0 (versionCode 20201)  
**Security Notice:** All reviewer accounts are provisioned exclusively with synthetic test data. Zero real patient identifiable information (PII/PHI) or real financial payment credentials exist in these sandbox accounts.

---

## 1. Google Play Reviewer Test Account
* **Role:** Synthetic Patient / Health Consumer
* **Login Email:** `play-reviewer-patient@healthogram-demo.internal`
* **Password:** `PlayDemo2026!Health`
* **Two-Factor / OTP Bypass Code:** `739201` (Active in closed test / reviewer sandbox only)
* **Associated Data:**
  - Synthetic Health ID: `HID-REV-2026-091`
  - Synthetic Conditions: Mild Seasonal Allergy (Synthetic LOINC/SNOMED CT test record)
  - Synthetic Immunization: Seasonal Influenza 2025 (Demo record)
  - Synthetic QR Token Demo: Can be generated in-app; refreshes every 60s
  - Synthetic Marketplace History: 1 completed demo order of Hypoallergenic Bandages (ID: `ORD-TEST-9921`)

---

## 2. Healthcare Provider Reviewer Test Account
* **Role:** Verified Doctor (Clinician Teleconsultation Demo)
* **Login Email:** `play-reviewer-doctor@healthogram-demo.internal`
* **Password:** `DoctorPlay2026!Review`
* **Demo License ID:** `MED-LIC-SYNTH-5501`
* **Associated Data:**
  - Mock Clinic: "Metro Health Demonstration Center"
  - Teleconsultation Sandbox: WebRTC loopback test room `room_play_demo_01` (Strictly non-recording)
  - QR Scanner Demonstration: Can scan the patient's single-use QR token to demonstrate consent-gated review

---

## 3. Marketplace Seller Reviewer Test Account
* **Role:** Approved Health Store Merchant
* **Login Email:** `play-reviewer-seller@healthogram-demo.internal`
* **Password:** `SellerPlay2026!Demo`
* **Associated Data:**
  - Store: "Apex Wellness Supplies (Demo)"
  - Catalog: 4 demonstration wellness products
  - Payout Dashboard: Synthetic ledger entries showing ledger credit/debit double-entry logs
