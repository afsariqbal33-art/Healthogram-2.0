# HEALTH PASSPORT — HIGH SECURITY MEDICAL SUBSYSTEM

## 1. Threat Model & Privacy Controls
- **Problem**: Traditional healthcare applications expose static QR codes that contain plaintext health records, allowing unauthorized eavesdropping or persistent leaks.
- **Healthogram Defense**:
  - **Dynamic Rotating Tickets**: QR code encodes a short-lived token (`sessionTicket`) that expires in 15 minutes.
  - **Mutual Authentication**: Scanning provider must be logged in, verified, and hold an eligible account type (`DOCTOR`, `CLINIC`, `HOSPITAL`, `LABORATORY`).
  - **Granular Scopes**: Patients grant access to specific scopes only:
    - `FULL`
    - `DIAGNOSTIC_ONLY`
    - `PRESCRIPTIONS_ONLY`
    - `LAB_ONLY`
    - `EMERGENCY_ONLY`
  - **Revocability**: Patients can revoke consent at any moment via their dashboard.
  - **Audit Records**: Full immutable ledger of every access.

## 2. Laboratory Specific Rule
- Laboratories are authorized to scan and upload diagnostic reports for a patient under active consent.
- Laboratories **do NOT receive a personal Health Passport** for their organization profile.
