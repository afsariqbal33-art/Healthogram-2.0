# 26 — BACKUP STRATEGY & DISASTER RECOVERY

## 1. Recovery Objectives
* **Recovery Point Objective (RPO):** < 1 Hour (Automated continuous Firestore point-in-time recovery + hourly delta snapshots).
* **Recovery Time Objective (RTO):** < 4 Hours (Full regional failover and restoration to healthy multi-region cluster).

## 2. Backup Topology & Immutability
* **Cold Storage Bucket:** `gs://healthogram-prod-backups-2026/`
* **Retention Policy:** 90-day WORM (Write Once, Read Many) retention lock. Deletion or overwrite is cryptographically prohibited by Cloud Storage bucket policies.
* **Customer-Managed Encryption Keys (CMEK):** Backup archives are encrypted with independent Google Cloud KMS keys rotated annually.

## 3. Disaster Recovery Drill Runbook
1. **Declare Incident:** Alert DR team on `#incident-healthogram-prod`.
2. **Isolate Compromised Region:** Route global traffic to secondary healthy region (`nam5`).
3. **Restore Data Collections:** Execute Cloud Storage to Firestore restore script using verified snapshot.
4. **Cryptographic Validation:** Validate vault ciphertext hashes and double-entry ledger balance invariants.
5. **Resume Traffic:** Remove maintenance overlays and reopen client traffic.
