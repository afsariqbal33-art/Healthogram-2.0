# HEALTHOGRAM — PRODUCTION OPERATIONAL RUNBOOK

**Target Version:** 1.0.0 (Build 1)  
**Environment:** Production (`healthogram-prod`)  
**Package:** `com.aistudio.healthogram.hkqvpm`  
**Platform:** Native Android (Kotlin / Jetpack Compose / Target SDK 36)  
**Classification:** Internal Site Reliability & Production Engineering  

---

## 1. System Architecture Overview

Healthogram operates on a zero-trust, serverless architecture running Native Android clients connected to Google Cloud / Firebase Enterprise infrastructure:
- **Client Layer:** Native Android Kotlin 2.1, Jetpack Compose, Material 3, Edge-to-Edge window insets, targetSdkVersion 36.
- **Identity & Access Management:** Firebase Authentication with phone OTP, email/password, MFA, and strict maximum 4 simultaneous device sessions.
- **Database Layer:** Cloud Firestore multi-region (`nam5`) with 1,544 lines of granular security rules and default deny-all posture.
- **Storage Layer:** Cloud Storage with private medical vaults (`health_vault/{uid}`), signed URL downloads, and isolated public asset buckets.
- **Application Security:** Google Play Integrity API integrated via Firebase App Check to attest genuine client binaries.
- **Asynchronous Compute:** Cloud Functions (Node.js 20) for payment escrow, order fulfillment, verification approvals, and immutable audit logging.

---

## 2. Daily Operational Cadence

### Morning Routine (08:00 UTC)
1. **Crashlytics Health Check:** Verify crash-free users >= 99.8% and crash-free sessions >= 99.9%. Inspect any new fatal clusters.
2. **App Check Attestation Metrics:** Verify Play Integrity attestation rate is > 99.5% legitimate requests.
3. **Firestore Read/Write Volume:** Check quota utilization in Google Cloud Console to ensure queries remain within provisioned budgets.
4. **Payment Gateway Settlement:** Validate Stripe webhook processing queue and verify zero unhandled charge events.
5. **Healthcare Verification Queue:** Review pending verification requests for Doctor, Clinic, Hospital, and Laboratory accounts.

### Mid-Day Routine (14:00 UTC)
1. **Health Passport Access Audit:** Inspect `health_access_logs` for anomaly spikes or failed QR session handshake bursts.
2. **Marketplace Escrow Balancing:** Run automated escrow balance reconciliation to verify pending seller funds match active delivery pipelines.
3. **Delivery Status Sync:** Check carrier webhook health and ensure 6-digit OTP completion rate is within expected normal thresholds (> 98%).
4. **AI & Translation Quotas:** Verify rate-limiting tokens and model latency metrics for generative assistance.

### Evening Routine (20:00 UTC)
1. **Daily Financial Reconciliation:** Execute automated ledger balancing: Gross Volume = Platform Fees + Seller Net Payouts + Gateway Fees + Taxes.
2. **Database Snapshot Verification:** Confirm Cloud Firestore automated daily backup completed successfully.
3. **Support Ticket Escalations:** Triage P0/P1 customer, healthcare provider, and seller tickets.
4. **On-Call Handoff:** Review alerts, ongoing investigations, and active Remote Config kill switches.

---

## 3. Core Component Runbooks

### A. Authentication & Session Management
- **Monitoring Metric:** Login success rate (> 99.0%), OTP delivery latency (< 15 seconds).
- **Enforcement:** `MAX_ACTIVE_DEVICES = 4`. When a user attempts to authenticate on a 5th device, the registration rejects with a descriptive prompt advising the user to revoke an existing device from their security settings.
- **Intervention:** In case of credential stuffing attacks, trip Remote Config flag `require_mfa_all_logins = true` and enforce re-captcha / phone verification.

### B. Health Passport & Medical Records
- **Monitoring Metric:** QR scan-to-grant latency (< 800ms), zero unauthorized access attempts.
- **Security Boundary:** Raw medical records must NEVER be placed in QR payloads or push notifications.
- **Incident Trigger:** Any detected query to `health_profiles/*` without an active session grant immediately raises a SEV-0 security incident.

### C. Marketplace & Seller Ledger
- **Monitoring Metric:** Order conversion rate, checkout idempotency cache hits, dispute frequency (< 0.5%).
- **Roles:** Strict isolation between `Customer` and `Seller`. Sellers cannot view customer Health Passports or platform administrative accounts.
- **Ledger Invariant:** Client balance modifications are strictly prohibited. All financial transactions run via Cloud Functions.

### D. Teleconsultation & Communications
- **WebRTC Signalling:** Audio and video consultations are peer-to-peer and strictly unrecorded.
- **Camera/Mic Lifecycle:** Hardware streams must release cleanly when calls terminate or when the app moves to background.
- **Fallback:** Translation service outages must never drop the core WebRTC or messaging channel.

---

## 4. Operational Dashboard Coordinates

| Subsystem | Metric Tracked | Target Baseline | Alerting Threshold |
| :--- | :--- | :--- | :--- |
| **Crashlytics** | Crash-Free Sessions | >= 99.9% | < 99.5% |
| **ANR Rate** | Application Not Responding | <= 0.20% | > 0.40% |
| **App Check** | Verified Client Requests | >= 99.5% | < 97.0% |
| **Firestore** | Latency p95 | <= 120ms | > 350ms |
| **Functions** | Error Rate | <= 0.10% | > 1.0% |
| **Payments** | Checkout Success Rate | >= 98.5% | < 95.0% |
| **OTP Delivery** | SMS / Email Handshake | >= 98.0% | < 90.0% |
