# HEALTHOGRAM PRODUCTION DATABASE BACKUP & POINT-IN-TIME RECOVERY POLICY

**Document Version:** 2.0.0-DR  
**Policy Owner:** Head of Site Reliability Engineering & Database Reliability Architect  
**Classification:** Enterprise Critical Policy  

---

## 1. Objectives & Metrics

- **Recovery Point Objective (RPO):**
  - Clinical Health Passport & Verification: **< 1 minute** (via Firestore Continuous PITR).
  - Financial Ledger & Payout Balances: **0 seconds** (Dual-written immutable audit log).
  - Social Media Posts, Comments & Reels: **< 1 hour**.
- **Recovery Time Objective (RTO):**
  - SEV-0 Clinical / Financial Outage: **< 30 minutes** to full operational restoration.
  - Social / Content Discovery Restoration: **< 2 hours**.

---

## 2. Backup Schedules & Retention Matrix

| Tier / Data Domain | Backup Mechanism | Frequency | Retention Window | Storage Location | Encryption Standard |
|---|---|---|---|---|---|
| **Firestore Production (All Collections)** | Continuous Point-in-Time Recovery (PITR) | Real-time write log streaming | 7 Days Rolling Continuous | Multi-Region GCP Vault | Customer-Managed Cloud KMS |
| **Firestore Scheduled Snapshots** | Automated Managed Firestore Backup | Daily (02:00 UTC) + Weekly Full | Daily: 30 Days; Weekly: 365 Days | Isolated Secondary GCP Region | Cloud KMS AES-256 |
| **Private Medical Object Storage** | Cloud Storage Object Versioning + Dual-Region Replication | Real-time object versioning | 7 Years (Statutory medical retention) | Geo-redundant Vault (`me-central1` + `europe-west1`) | AES-256 + Signed KMS Keys |
| **Verification Documents Vault** | Cloud Storage Object Versioning | Real-time | 90 Days post-decision | Sovereign Regional Bucket | AES-256 Restricted IAM |
| **Financial Double-Entry Ledger** | Continuous PITR + Nightly Export to BigQuery Cold Storage | Real-time + Daily Snapshot | Permanent (7 Years statutory) | Multi-Region Encrypted Coldline | Cloud KMS AES-256 |

---

## 3. Pre-Migration & Pre-Release Backup Protocols

Before executing any database schema update, bulk backfill, or major version release:
1. **Mandatory Manual Snapshot:** Trigger a point-in-time snapshot with label `pre_migration_<version>_<timestamp>`.
2. **Backward-Compatibility Verification:** Ensure new application code reads both legacy and modernized document schemas before activating writes.
3. **Dry-Run Rollback Test:** Verify that rollback migration scripts execute without data loss in the staging environment before production deployment.

---

## 4. Access Control & Authorization

- Backups are stored in dedicated backup storage projects with **Object Lock** and **WORM (Write Once, Read Many)** protection enabled.
- Restoration operations require **Multi-Party Approval (Dual-Custody)**: Platform Owner authorization + Lead SRE cryptographic token.
- No developer or administrator has permissions to unilaterally overwrite or delete active backup snapshots.
