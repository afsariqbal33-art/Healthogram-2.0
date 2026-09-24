# HEALTHOGRAM BACKUP & DISASTER RECOVERY POLICY

**Document:** `docs/backups/BACKUP_POLICY.md`  
**Classification:** Confidential DevOps & Engineering Operations  
**Compliance Standard:** HIPAA Security Rule, GDPR Disaster Recovery, ISO 27001

---

## 1. Primary Objectives & Recovery Guarantees
* **Recovery Point Objective (RPO):** < 1 hour for transactional ledgers and Health Passport audit trails; < 24 hours for non-critical social posts.
* **Recovery Time Objective (RTO):** < 4 hours for total platform restore to operational status.
* **Geographic Redundancy:** Primary GCP region `me-central1` (Doha/UAE sovereign access) with continuous encrypted multi-region replica in `europe-west1`.

---

## 2. Six-Pillar Backup Architecture (Section 41)

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        HEALTHOGRAM 6-PILLAR BACKUP                     │
├─────────────────┬──────────────────┬─────────────────┬─────────────────┤
│ Pillar A        │ Pillar B         │ Pillar C        │ Pillar D        │
│ Gemini 3.8 Flash│ GitHub Private   │ Source Archive  │ Firebase Cloud  │
│ Visual Snapshot │ Repository (Git) │ Encrypted ZIP   │ Rules, Indexes, │
│ & Branches      │ Commits & Tags   │ & AAB Builds    │ Functions, Data │
├─────────────────┴──────────────────┴─────────────────┴─────────────────┤
│ Pillar E                           │ Pillar F                          │
│ Offline Keystore & Signing Vault   │ Complete Documentation & Schemas  │
│ Hardware Security Module (HSM)     │ Architectural & Disaster Playbooks│
└────────────────────────────────────┴───────────────────────────────────┘
```

### Pillar A: Gemini 3.8 Flash Version Checkpoints
* **Scope:** Generated UI layouts, action graphs, theme tokens, and component mappings.
* **Frequency:** Named commit and checkpoint saved prior to each GitHub sync or major milestone.
* **Tracking:** Recorded in `docs/backups/VERSION_CHECKPOINTS.md`.

### Pillar B: GitHub Private Repository (`healthogram-app`)
* **Scope:** Complete Kotlin Android application, Jetpack Compose UI, Cloud Functions, tests, and CI/CD pipelines.
* **Branches:** `main` (production), `develop` (staging integration), `gemini-3.8-flash` (raw generator synchronization), `release/*`.
* **Frequency:** Continuous push upon PR merge; daily backup clone to secondary self-hosted mirror.

### Pillar C: Source ZIP & Build Artifacts
* **Scope:** Source code archive (`Healthogram-vX.Y.Z-source.zip`), signed release APK, and signed release AAB.
* **Storage:** GitHub Releases and immutable Google Cloud Storage bucket with Object Versioning enabled.

### Pillar D: Firebase Infrastructure Backup
* **Scope:** Firestore database daily automated export, `firestore.rules`, `storage.rules`, `firestore.indexes.json`, `functions/src/`.
* **Database Snapshot:** Nightly scheduled Cloud Function invoking `gcloud firestore export` to encrypted Coldline bucket.
* **Retention:** 30 daily snapshots, 12 monthly archives, 7-year audit retention for financial ledgers.

### Pillar E: Offline Keystore & Signing Security
* **Scope:** Production Android release keystore (`release.keystore`), upload keys, and key properties.
* **Storage:** Air-gapped Hardware Security Module (HSM) and encrypted 1Password / Google Secret Manager enterprise vault.
* **Policy:** NEVER committed to Git or accessible in unencrypted CI environments.

### Pillar F: Documentation & Architectural Runbooks
* **Scope:** Disaster recovery runbooks, database migration logs, API specifications, and role matrices.
* **Storage:** Version-controlled inside `/docs/` and synced to internal engineering wiki.

---

## 3. Disaster Recovery Execution Runbook (Section 42)

In the event of total developer workstation loss or primary host catastrophic failure:

1. **Step 1: Authenticate Infrastructure Access**
   * Authenticate authorized engineer using hardware YubiKey MFA.
   * Access GitHub organization: `https://github.com/healthogram-enterprise/healthogram-app`.

2. **Step 2: Clone Repository**
   ```bash
   git clone git@github.com:healthogram-enterprise/healthogram-app.git
   cd healthogram-app
   git checkout main
   ```

3. **Step 3: Restore Local Toolchain**
   * Verify JDK 17, Android SDK Platform 34, Node.js 20, and Firebase CLI.
   * Run toolchain validation script: `./scripts/verify_environment.sh`.

4. **Step 4: Inject Environment Configuration**
   * Retrieve encrypted environment config from Google Secret Manager:
     ```bash
     gcloud secrets versions access latest --secret="healthogram-dev-env" > .env
     ```

5. **Step 5: Execute Test Verification**
   ```bash
   gradle :app:testDebugUnitTest
   ```

6. **Step 6: Build Verification Artifacts**
   ```bash
   gradle :app:assembleDebug
   ```

7. **Step 7: Production Release Recovery**
   * Pull production signing key from HSM vault into build agent.
   * Execute release build pipeline:
     ```bash
     gradle :app:bundleRelease
     ```
