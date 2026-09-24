# HEALTHOGRAM — ON-CALL RUNBOOK

**Target Version:** 1.0.0 (Production)  
**Audience:** Primary and Secondary On-Call Site Reliability Engineers  
**Rotation:** 7-Day Shifts (Handoff Mondays 09:00 UTC)  

---

## 1. On-Call Prerequisites & Access Checklist

Before assuming on-call duty, the engineer must verify:
- [ ] PagerDuty / Opsgenie app installed with critical alert sound override enabled.
- [ ] Google Cloud Console access with `Editor` role on `healthogram-prod`.
- [ ] Firebase Console access with `Firebase Admin` permissions on `healthogram-prod`.
- [ ] Google Play Console access with `Release Manager` permissions.
- [ ] Stripe Dashboard access with `Support Specialist / Fraud Analyst` role.
- [ ] SSH access to build bastion and signing token enclave.
- [ ] Verified local development environment with JDK 17, Android SDK 36, and Gradle 8.11.1.

---

## 2. Common Alert Playbooks

### Alert: `HighCrashRateDetected` (Crashlytics > 0.5%)
1. Open Firebase Crashlytics dashboard for `com.aistudio.healthogram.hkqvpm`.
2. Filter by `versionName: 1.0.0`.
3. Identify the root stack trace:
   - If crash is caused by a bad Remote Config payload: immediately revert Remote Config parameter to default value.
   - If crash occurs on specific Android API level or vendor device: evaluate if mitigation can be pushed via Remote Config or requires emergency hotfix.
4. If crash-free sessions drop below 99.0%, escalate to SEV-1 and notify Release Manager.

### Alert: `PlayIntegrityAttestationFailureSpike` (> 5% rejection)
1. Inspect Firebase App Check telemetry in Google Cloud Console.
2. Determine if failure is due to:
   - Modified/pirated client APKs distributing malware (legitimate blocking).
   - Google Play Integrity quota exhaustion (request emergency quota increase via GCP support).
   - Certificate fingerprint mismatch after an uncoordinated signing key update.
3. If legitimate users are affected, temporarily set App Check enforcement mode to `Unenforced (Monitoring)` while investigating.

### Alert: `HealthPassportUnauthorizedAccessSpike`
1. Navigate to Firestore collection `health_access_logs`.
2. Query recent entries where `status == "REJECTED"` or `status == "UNAUTHORIZED"`.
3. Identify IP address or originating User UID:
   - If an account is brute-forcing QR tokens: immediately suspend account UID via Firebase Authentication Admin.
   - If an unverified provider account is querying records: verify security rules blocked the request (confirm `permission-denied` status code).
4. File a security audit ticket and notify the Health Security Admin.

### Alert: `PaymentWebhookProcessingLag`
1. Check Stripe Webhook Events log for HTTP 500 or timeout occurrences.
2. Check Cloud Function `onStripeWebhook` logs in Google Cloud Logging.
3. If Cloud Function is crashing or timing out:
   - Check Firestore write quota limits.
   - Verify idempotency collection `processed_transactions` is responding.
   - Stripe retries webhooks automatically for up to 72 hours; once Cloud Function is patched or scaled, unprocessed events will replay cleanly.

---

## 3. Communication Procedures

When a SEV-0 or SEV-1 incident is active:
1. Create a dedicated incident Slack/Teams channel: `#incident-YYYYMMDD-<name>`.
2. Post an initial status update to internal leadership within 15 minutes.
3. Post status updates every 30 minutes until mitigation is achieved.
4. Once mitigated, announce transition to monitoring mode for 2 hours before closing.
