# HEALTHOGRAM 2.3.0 GOOGLE PLAY DATA SAFETY DECLARATION FORM RESPONSES

**Application ID:** `com.aistudio.healthogram.hkqvpm`  
**Version:** `2.3.0` (versionCode `23000`)  
**Submission Date:** September 2026  

---

## Section 1: Overview Questions

* **Does your app collect or share any of the required user data types?**  
  `Yes`
* **Is all of the user data collected by your app encrypted in transit?**  
  `Yes` (Enforced TLS 1.3 with certificate pinning on backend endpoints)
* **Do you provide a way for users to request that their data be deleted?**  
  `Yes` (In-app deletion at Settings -> Privacy & Security -> Delete Account, plus web endpoint at https://healthogram.app/account/delete)

---

## Section 2: Data Collection & Sharing Detailed Breakdown

### 1. Health & Fitness
* **Health Info:**
  - Collected: `Yes`
  - Shared: `No`
  - Processed ephemerally: `No` (Stored in client-side AES-GCM-256 encrypted vault)
  - Is this data required or optional: `Optional` (Entered voluntarily by the user)
  - Purpose: `App functionality` (Health Passport personal health record management)
* **Fitness Info:**
  - Collected: `Yes` (Steps, active calories via Android Health Connect with explicit runtime permission)
  - Shared: `No`
  - Purpose: `App functionality`

### 2. Personal Information
* **Name:**
  - Collected: `Yes` | Shared: `No` | Required | Purpose: `App functionality, Account management`
* **Email Address:**
  - Collected: `Yes` | Shared: `No` | Required | Purpose: `App functionality, Account management`
* **Phone Number:**
  - Collected: `Yes` (Optional) | Shared: `No` | Purpose: `Account management, Teleconsultation contact`
* **User IDs:**
  - Collected: `Yes` (Firebase UID) | Shared: `No` | Required | Purpose: `Authentication & authorization`

### 3. Financial Information
* **Purchase History:**
  - Collected: `Yes` | Shared: `No` | Optional | Purpose: `App functionality` (Marketplace order management)
* **Payment Info (Card / Bank details):**
  - Collected: `No` (Healthogram never sees or stores full primary account numbers; all payment forms are securely hosted/tokenized via PCI-DSS compliant payment gateways)

### 4. Messages & Communications
* **In-App Messages:**
  - Collected: `Yes` | Shared: `No` | Optional | Purpose: `App functionality` (Patient-provider and peer communications)

### 5. Photos and Videos
* **Photos:**
  - Collected: `Yes` | Shared: `No` | Optional | Purpose: `App functionality` (Profile photos, medical document uploads selected via Photo Picker)
* **Videos:**
  - Collected: `Yes` | Shared: `No` | Optional | Purpose: `App functionality` (Health education reels)

### 6. Audio Files
* **Voice / Sound Recordings:**
  - Collected: `Yes` (Ephemeral in WebRTC calls or voice note messages) | Shared: `No` | Optional | Purpose: `App functionality`

### 7. App Info and Performance
* **Crash Logs & Diagnostics:**
  - Collected: `Yes` | Shared: `No` | Required | Purpose: `Analytics, App functionality` (Zero PII/PHI logged)
