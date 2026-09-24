# HEALTHOGRAM PRODUCTION RELEASE PROCESS & ROLLBACK RUNBOOK

**Document:** `docs/releases/RELEASE_PROCESS.md`  
**Classification:** Production Engineering Standards  
**Version:** 1.0.0

---

## 1. End-to-End Release Pipeline Architecture (Section 34, 53)

```text
                  Gemini 3.8 Flash Visual Updates
                               │
                               ▼
                   'gemini-3.8-flash' Branch
                               │
                               ▼
                          Pull Request
                               │
                               ▼
                            develop
                               │
                   ┌───────────┴───────────┐
                   ▼                       ▼
                CI TESTS              SECURITY SCAN
                   │                       │
                   └───────────┬───────────┘
                               ▼
                             Full QA
                               │
                               ▼
                        release/X.Y.Z
                               │
                               ▼
                            STAGING
                               │
                               ▼
                     PRODUCTION ACCEPTANCE
                               │
                               ▼
                             main
                               │
                               ▼
                       Git Tag vX.Y.Z
                               │
                       ┌───────┴────────┐
                       ▼                ▼
                GitHub Release      Release Build
                                         │
                                         ▼
                                     Release AAB
                                         │
                                         ▼
                              Google Play Internal
                                    Testing
                                         │
                                         ▼
                                    Production
```

---

## 2. Release Stage Gates & Responsibilities

### Stage 1: Feature Integration (`develop`)
* All features originate from branch `feature/*` and merge into `develop` via PR.
* PR requires:
  1. Passing CI (`.github/workflows/ci.yml`).
  2. Passing Security Scan (`.github/workflows/security_scan.yml`).
  3. Minimum 1 Senior Engineer approval.

### Stage 2: Release Candidate Preparation (`release/X.Y.Z`)
* Once `develop` achieves feature milestone, branch `release/X.Y.Z` is cut.
* **Code Freeze**: Only P0/P1 bug fixes, security hardening, or documentation edits permitted.
* Deployed automatically to Firebase Staging environment (`healthogram-staging`).
* Automated QA test suite executed across device matrix.

### Stage 3: Production Acceptance Matrix Verification
* Complete verification against the 102+ criteria in `HEALTHOGRAM_PRODUCTION_ACCEPTANCE_MATRIX.md`.
* Mandatory criteria:
  - 0 Open P0 (Critical) Bugs.
  - 0 Open P1 (High) Bugs.
  - 100% Passing Financial Ledger Reconciliation (100-order drift-free).
  - 100% Passing Health Passport Zero-Trust Penetration Audit.

### Stage 4: Promotion to `main` & Tagging
* Fast-forward PR merged from `release/X.Y.Z` into `main`.
* Tag created with format `vMAJOR.MINOR.PATCH` (e.g., `v1.0.0`).
* Tag push automatically triggers `.github/workflows/release.yml` to compile signed release AAB, APK, and create GitHub Release with release notes.

### Stage 5: Google Play Internal Testing Track
1. Upload release AAB to Google Play Console under **Internal Testing**.
2. Run automated pre-launch report across test device pool.
3. Conduct 24-hour smoke testing by internal medical and operations personnel.
4. Promote to Closed Testing or Production upon Owner sign-off.

---

## 3. Emergency Rollback & Mitigation Strategy (Section 40)

If a critical flaw, zero-day security incident, or database schema incompatibility is discovered in production:

```text
Production Incident Detected
            │
            ├─► Fast-Mitigation: Trigger Owner Emergency Feature Flag / Kill Switch
            │   (Disable AI, Disable Live Streaming, Maintenance Mode)
            │
            ├─► Cloud Functions: Roll back deployment to previous function revision
            │   gcloud functions deploy --rollback
            │
            └─► Mobile Client: Halt Google Play staged rollout immediately
```

### Action Steps:
1. **Immediate Quarantine:**
   * If localized to a feature (e.g., Marketplace checkout or AI captioning), toggle the corresponding Kill Switch via Owner Control Panel or Firebase Remote Config.
   * If localized to data integrity, enable `MAINTENANCE_MODE` to halt client writes while keeping cached read-only access active.
2. **Halt Play Store Rollout:**
   * Navigate to Google Play Console -> Release Management -> Production.
   * Click **Halt Staged Rollout** to prevent further downloads of the affected version.
3. **Artifact & Evidence Preservation:**
   * Preserve server logs, client crash stack traces, and Firestore transaction audit entries. DO NOT delete the failed release tag or commit.
4. **Deploy Hotfix:**
   * Branch `hotfix/X.Y.Z+1` from `main`.
   * Apply targeted correction, retest, bump PATCH version, and execute automated emergency pipeline.
