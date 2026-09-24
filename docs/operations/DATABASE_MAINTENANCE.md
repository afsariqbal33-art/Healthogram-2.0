# HEALTHOGRAM — DATABASE & FIRESTORE MAINTENANCE RUNBOOK

**Classification:** Database Architecture, Performance Tuning & Hygiene  
**Target:** Google Cloud Firestore (`nam5` Multi-Region)  

---

## 1. Firestore Hygiene & Optimization Principles

To sustain sub-100ms query performance and optimize cloud operational costs:
1. **Index Governance:** Every composite query must be backed by a declared index in `firestore.indexes.json`. Never allow ad-hoc unindexed queries in production.
2. **Listener Lifecycle:** Jetpack Compose viewmodels must bind Firestore listeners to UI lifecycle flows (`collectAsStateWithLifecycle`), automatically unsubscribing when screens leave composition.
3. **Pagination & Query Limits:** Feed queries, marketplace catalogs, and transaction histories MUST enforce explicit limits (default 20 items per page) via `startAfter` cursors.
4. **Denormalization vs Consistency:** Denormalize display fields (e.g., author display name, product thumbnail URL) for read-heavy feeds, but maintain authoritative double-entry documents for payments, inventory, and health access grants.

---

## 2. Schema Migration Protocol

Schema changes must follow the structured migration process:

```text
[Migration Design Document] (e.g., migration-006-seller-coupons.md)
        ↓
[Dual-Write Phase] (Backend writes to both old and new schema fields)
        ↓
[Client Backfill / Migration Cloud Function Execution]
        ↓
[Verify Data Consistency & Integrity Across 100% of Documents]
        ↓
[Switch Reads to New Schema]
        ↓
[Deprecate Old Fields in Subsequent Minor Release]
```

*Golden Rule:* Never delete production collections or fields without a formal, approved migration rollback strategy.

---

## 3. Monthly Database Review Checklist

- [ ] Inspect Firestore usage analytics for unexpected read/write spikes.
- [ ] Review slow queries (p95 latency > 200ms) in Google Cloud Trace.
- [ ] Clean up expired ephemeral records (`health_qr_sessions` where `expiresAt < now() - 7 days`).
- [ ] Confirm automated nightly GCS backup snapshots completed without failures.
- [ ] Verify point-in-time recovery (PITR) window is active.
