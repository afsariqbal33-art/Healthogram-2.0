# HEALTHOGRAM — CONTINUOUS SECURITY MAINTENANCE RUNBOOK

**Classification:** Application Security, DevSecOps & Governance  
**Scope:** Continuous Threat Modeling, Rule Verification & Penetration Testing  

---

## 1. Continuous Threat Surfaces & Mitigation

Healthogram operates in a zero-trust model across 6 primary security boundaries:

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        SECURITY THREAT BOUNDARIES                      │
├──────────────────────┬──────────────────────┬──────────────────────────┤
│ 1. Identity & Auth   │ 2. Health Passport   │ 3. Financial Ledger      │
│ • Phone OTP Abuse    │ • Ephemeral QR Token │ • Double-Entry Escrow    │
│ • Max 4 Active Devs  │ • Ephemeral Scopes   │ • No Client Writes       │
│ • Biometrics / MFA   │ • Immutable Audit Log│ • Idempotent Webhooks    │
├──────────────────────┼──────────────────────┼──────────────────────────┤
│ 4. AI Studio         │ 5. Communications    │ 6. App Integrity         │
│ • Medical Air-Gap    │ • P2P WebRTC (No Rec)│ • Play Integrity / App   │
│ • Rate Limits        │ • E2EE Conversations │   Check Tokenization     │
│ • Claim Moderation   │ • Media Vault Sanit. │ • R8 Code Obfuscation    │
└──────────────────────┴──────────────────────┴──────────────────────────┘
```

---

## 2. Monthly Security Audit Checklist

Every 30 days, the Security Engineering Team executes this mandatory checklist:

- [ ] **Firestore Rule Audit:** Verify that all 1,544 lines of rules remain intact, default-deny is active, and no wildcard rules (`allow read, write: if true;`) exist.
- [ ] **Health Passport Access Review:** Run automated analysis on `health_access_logs` to detect abnormal access patterns, repetitive failed QR handshakes, or unauthorized provider scans.
- [ ] **App Check Certificate Fingerprint:** Verify that Google Play App Signing SHA-256 fingerprint matches the active configuration in Firebase App Check.
- [ ] **Admin Privilege Recertification:** Audit the `admins/{uid}` collection. Any inactive admin account (> 45 days idle) must be downgraded to standard user.
- [ ] **Storage Bucket Access Review:** Confirm that `gs://healthogram-vault/` retains private ACLs and signed-download token validation.
- [ ] **Cloud Function Secrets Rotation:** Verify that Stripe API keys and backend secrets in Google Cloud Secret Manager are rotated according to policy.
- [ ] **Dependency Vulnerability Scan:** Run static analysis (`./gradlew dependencyCheckAnalyze` / `npm audit` in `functions/`).
