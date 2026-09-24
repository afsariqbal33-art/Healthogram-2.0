# 05 — AUTHENTICATION & SESSION MANAGEMENT

## 1. Authentication Modalities
* **Primary Identity:** Email & Password with sovereign MFA PIN.
* **Mobile OTP:** Supported via Firebase Phone Authentication with rate-limiting.
* **Biometric Step-Up:** Android BiometricPrompt (Class 3 Strong Biometrics) for Health Passport unlocking.

## 2. Hardened 4-Device Session Ceiling
Enforced by `SecurityHardeningEngine`:
* A maximum of 4 concurrent active device sessions are permitted per account.
* Attempting to log into a 5th device triggers an explicit authorization prompt requiring the user to revoke an existing device session or reject the login.
* The session list displays device model, OS version, approximate location, and last active timestamp.

## 3. Session Security Policies
* **Idle Timeout:** Automatic session lock after 15 minutes of inactivity; biometric or sovereign PIN required to resume.
* **Global Revocation:** "Logout from all devices" invalidates all Firebase refresh tokens immediately.
* **Password Change Invalidation:** Updating password automatically revokes all sessions on other devices.
