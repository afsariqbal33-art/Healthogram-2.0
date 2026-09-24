# Healthogram 2.2 — Database Architecture & Evolution Plan

**Document:** `docs/database/HEALTHOGRAM_2_2_DATABASE_EVOLUTION.md`  
**System:** Cloud Firestore Multi-Region & Android Room Database  
**Target Release:** Healthogram 2.2.0 (Build 20200)  
**Authority:** Database Architect, Backend Lead & Android Data Engineer  
**Classification:** ARCHITECTURAL SPECIFICATION & DATA SCHEMAS  

---

## 1. Production Context & Database Invariants

In Step 35 production telemetry:
- **Cloud Firestore Multi-Region (`eur3`)** processed over 45,000,000 document reads and 8,900,000 document writes with 99.97% uptime.
- **168 composite indexes** are currently deployed in `firestore.indexes.json`.
- **Room Database** on Android clients cached over 12,000,000 clinical reads locally, reducing Firestore cloud costs by 48%.

### Core Database Invariants
1. **No Unnecessary Migration**: The database remains Cloud Firestore for distributed cloud storage and Room with SQLCipher for local mobile caching. No foreign database transitions are authorized.
2. **Double-Entry Financial Immutability**: All monetary calculations utilize minor-unit integers (Baisa/Cents) within atomic Firestore transactions. No balance record may be updated without a corresponding ledger entry.
3. **Clinical Path Isolation**: Health records are segregated into dedicated subcollections with strictly isolated Firestore security rules.

---

## 2. Remediation of Technical Debt: Firestore Composite Index Consolidation (DEBT-02)

### The Constraint
- Firestore enforces a hard quota limit of **200 composite indexes** per database project. Healthogram 2.1 currently utilizes **168 composite indexes**, leaving an unacceptably narrow safety buffer of only 32 indexes for new feature growth.

### The 2.2 Consolidation Strategy
1. **Consolidate Marketplace Search Indexes**:
   - Merge overlapping marketplace filter indexes (`category + status + createdAt` and `category + status + price`) into a single composite index `category + status + createdAt`, deferring secondary price sorting to in-memory client-side sorting for paginated batches (batches limited to 20 items).
   - This eliminates **18 redundant composite indexes**.
2. **Consolidate Appointment Query Indexes**:
   - Standardize appointment queries on `(patientUid, scheduledStartTime, status)` and `(doctorUid, scheduledStartTime, status)`.
   - This eliminates **8 redundant composite indexes**.
3. **Net Reduction**:
   - Total composite indexes reduced from **168 to 142**, creating a resilient 58-index safety margin.

---

## 3. Core Cloud Firestore Collection Schemas (Version 2.2)

```
  /users/{uid}
    ├── /device_sessions/{deviceId}      [Session governance: max 4 devices]
    ├── /consent_grants/{grantId}        [Patient sovereign consent records]
    ├── /shipping_addresses/{addressId}  [NEW 2.2: Customer multi-address book]
    └── /health_records/{recordId}       [Encrypted clinical documents & vitals]

  /appointments/{appointmentId}          [Atomic booking records & slot state]
  
  /healthcare_organizations/{orgId}      [Accredited clinics, hospitals, labs]
    ├── /roster/{practitionerUid}        [Affiliated verified doctors]
    └── /service_requests/{orderId}      [NEW 2.2: Lab test diagnostic orders]

  /marketplace_products/{productId}      [Approved wellness & fitness catalog]
  
  /marketplace_orders/{orderId}          [Order lifecycle & OTP delivery proofs]
  
  /financial_ledger/{transactionId}      [Immutable double-entry journal]
  
  /audit_logs/{auditEntryId}             [Immutable WORM compliance logs]
```

---

## 4. Android Client Local Database Evolution (Room 2.6+)

### Client-Side Data Architecture
- **Room Engine**: Room 2.6.1 with Kotlin Symbol Processing (KSP).
- **Encryption**: Integrated SQLCipher 4.5+ utilizing 256-bit AES-GCM encryption with keys derived securely from the Android Keystore hardware module.
- **Delta Synchronization**:
  - Entities record `lastModifiedAt` timestamps.
  - Sync requests pass `If-Modified-Since` headers to fetch only records updated since the local cache timestamp, eliminating redundant reads.
- **Data Pruning**: Local social and marketplace cache entries older than 14 days are automatically cleared on application launch during low-memory conditions. Health records are retained indefinitely until explicit patient revocation or account erasure.
