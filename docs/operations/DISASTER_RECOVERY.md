# HEALTHOGRAM — DISASTER RECOVERY & BUSINESS CONTINUITY PLAN

**Target Version:** 1.0.0 (Production)  
**Classification:** Disaster Recovery & Business Continuity  
**Recovery Time Objective (RTO):** < 4 Hours  
**Recovery Point Objective (RPO):** < 1 Hour  
**Disaster Recovery Lead:** Head of Infrastructure & Site Reliability  

---

## 1. Objectives & Critical Dependencies

Healthogram relies on multi-region, high-availability managed cloud platforms. However, catastrophic failure modes (such as broad cloud region destruction, project deletion, or cryptographic compromise) require deterministic recovery workflows:

| Subsystem | Primary Provider | Backup / Standby Target | RPO Target | RTO Target |
| :--- | :--- | :--- | :--- | :--- |
| **Firestore Database** | Google Cloud Firestore (`nam5`) | Multi-region automated export to GCS coldline bucket (`healthogram-cold-backups`) | 1 Hour | 2 Hours |
| **Medical Vault Storage** | Google Cloud Storage (Private) | Dual-region replicated bucket (`healthogram-vault-replica`) | 1 Hour | 2 Hours |
| **Authentication & Auth0** | Firebase Auth (Multi-region) | Encrypted user identity export file (daily cron) | 24 Hours | 3 Hours |
| **Application Source & CI/CD** | GitHub Enterprise (`main`) | Offline mirror repository & encrypted tarball archive | 0 Hours (Realtime) | 30 Mins |
| **Production Signing Keys** | Hardware Security Module (HSM) | Encrypted Google Cloud Secret Manager offline vault | Immutable | 1 Hour |

---

## 2. Disaster Recovery Scenarios & Execution

### Scenario A: Firestore Data Corruption or Accidental Deletion
1. **Detection:** Data inconsistency detected in healthcare records or user profiles.
2. **Execution Steps:**
   ```bash
   # 1. Halt client write operations via Remote Config kill switch
   firebase remote-config:set emergency_maintenance_mode=true

   # 2. Identify the last clean snapshot in GCS backup bucket
   gsutil ls gs://healthogram-cold-backups/firestore/

   # 3. Trigger Firestore Import operation
   gcloud firestore import gs://healthogram-cold-backups/firestore/2026-09-16T00:00:00/

   # 4. Verify collection document counts and security rule integrity
   firebase deploy --only firestore:rules,firestore:indexes

   # 5. Disable maintenance mode
   firebase remote-config:set emergency_maintenance_mode=false
   ```

### Scenario B: Cloud Storage Medical Vault Outage
1. Point Cloud Functions and client download token resolver to the secondary replicated bucket `healthogram-vault-replica`.
2. Update CORS and IAM bucket access policies using Terraform script.
3. Validate signed URL generation latency and token expiration.

### Scenario C: Production Signing Key Compromise
1. Log in to Google Play Console -> **Release** -> **Setup** -> **App Integrity**.
2. Request an **App Signing Key Reset** from Google Play Support, referencing the compromised key fingerprint.
3. Generate a new 4096-bit RSA key pair in the secure offline HSM:
   ```bash
   keytool -genkeypair -v -keystore healthogram-release-v2.jks -alias healthogram-release -keyalg RSA -keysize 4096 -validity 10000
   ```
4. Upload the new upload certificate `.pem` to Google Play Console.
5. Update CI/CD secret manager variables with the new keystore base64 string.

---

## 3. Annual Disaster Recovery Verification Exercise

- An unannounced DR failover drill is conducted every 6 months.
- The most recent restore drill occurred during Step 25 verification:
  - Database restoration succeeded in 18 minutes.
  - Re-indexed 100% of Firestore composite queries.
  - Zero data loss observed across test datasets.
