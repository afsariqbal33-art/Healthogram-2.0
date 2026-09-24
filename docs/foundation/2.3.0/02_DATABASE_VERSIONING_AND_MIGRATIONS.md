# HEALTHOGRAM 2.3: DATABASE VERSIONING & ZERO-DOWNTIME MIGRATIONS

**Document ID:** HGM-2.3-FOUNDATION-02-DB  
**Phase:** Step 49 Foundation Implementation  
**Target Release:** Healthogram Version `2.3.0`  

---

## 1. Schema Versioning Architecture

The database schema version is formally tracked across both Firestore and Room SQLCipher stores:
* **Current Version:** `schema_version = 2.3.0` (`VERSION_CODE = 3`)
* **Minimum Supported Client Schema:** `schema_version = 2` (Healthogram 2.2 clients remain functional without breaking changes)

---

## 2. Migration Registry (`DatabaseMigrationRegistry`)

| Migration ID | Target Version | Description | Checksum | Status | Rollback Strategy |
| :--- | :---: | :--- | :--- | :---: | :--- |
| `MIG-001-2.1.0` | `2.1.0` | Initial baseline collections (users, posts, orders) | `sha256:4a8b...` | `VERIFIED` | Restore GCP snapshot |
| `MIG-002-2.2.0` | `2.2.0` | Vault isolation, double-entry ledger, 4-device limit | `sha256:7c9e...` | `VERIFIED` | Drop unindexed subcollections |
| `MIG-003-2.3.0` | `2.3.0` | Additive collections: `/appointments`, `/doctor_schedules`, `/fhir_exports`, `/health_connect_sync` | `sha256:e3b0...` | `PLANNED` | Additive evolution: 2.2 clients ignore new fields; disable 2.3 feature flags |

---

## 3. Migration Lifecycle & Governance Rules

1. **Non-Destructive Guarantee:** Destructive migrations (`isDestructive = true`) throw fatal `IllegalArgumentException` at compilation and registration time.
2. **Zero-Downtime Multi-Phase Rollout:**
   * Step 1: Deploy Firestore Security Rules supporting both `v2` and `v3`.
   * Step 2: Deploy Cloud Functions handling dual-version data models.
   * Step 3: Run Cloud Run asynchronous cursor backfill worker on historical records.
   * Step 4: Promote mobile client APKs/AABs via Google Play Staged Rollout.
3. **Repeat-Safe:** Re-running a migration that is `COMPLETED` or `VERIFIED` is idempotent and safe.
