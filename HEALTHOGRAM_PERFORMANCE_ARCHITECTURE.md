# HEALTHOGRAM — STEP 21: PERFORMANCE ARCHITECTURE & SCALABILITY SPECIFICATION

## 1. High-Level Scalability Architecture

```text
[ CLIENT APPLICATIONS (Android Mobile / Tablet / Desktop Web) ]
       │
       ├── App Check Attestation & TLS 1.3
       ├── Local Cache (Encrypted Room DB + LRU Memory Cache)
       ├── Controlled Infinite Scroll State (Cursor Pagination)
       └── Shimmer Skeleton Render Pipeline
       │
       ▼
[ EDGE ROUTING & CDN ACCELERATION ]
       ├── Cloud CDN for Static Assets & Compressed Media Variants
       ├── Sovereign Edge Gateways (Saudi Arabia, GCC, Global)
       └── Distributed DDoS Mitigation & Rate Limiting
       │
       ▼
[ SERVERLESS COMPUTE LAYER (Google Cloud Run / Cloud Functions 2nd Gen) ]
       ├── Min Instances (Warm Pool) for Critical Endpoints (Checkout, Auth, QR)
       ├── Concurrency = 80 per Container (Node.js 20 / Kotlin JVM 21)
       ├── Global Connection Pooling (PostgreSQL / Redis / Stripe / Gemini SDK)
       └── Asynchronous Task Queues (Cloud Tasks / Eventarc) for Heavy Jobs
       │
       ▼
[ STATE & DATA STORAGE LAYER ]
       ├── Firestore: Sharded Counters, Subcollection Isolation, Cursor Pagination
       ├── Realtime Database: Ephemeral Presence, Fast Signaling (Shallow listeners)
       ├── Cloud Storage: Tiered Lifecycle Rules (Raw -> WebP Variants -> Coldline Archive)
       └── Redis Memorystore: Distributed Session Cache & Rate Limit Buckets
```

---

## 2. Client-Side Rendering & Mobile Optimization

### 2.1 Shimmer Skeleton Pipeline
To eliminate visual jarring, layout shift, and full-screen blocking spinners:
- Every major UI surface displays a semantic skeleton placeholder:
  - `PostSkeleton`: Avatar circle, username rect, timestamp, square media rect, action bar icons.
  - `ReelSkeleton`: Fullscreen aspect-ratio box with simulated side action buttons.
  - `ProductSkeleton`: 1:1 image box, badge pill, price text, merchant tag.
  - `OrderSkeleton`: Order ID pill, product thumbnail, price, status tracker bar.
  - `MessageSkeleton`: Left/right staggered bubbles with varying widths.
  - `ProfileSkeleton`: Hero header, avatar, stats row, tabs bar, 3-column grid.
  - `HealthRecordSkeleton`: Lock badge, category pill, date, doctor summary box.
  - `DashboardSkeleton`: KPI metric cards, mini sparkline chart placeholder.
  - `NotificationSkeleton`: Staggered avatar, title line, relative time chip.

### 2.2 Recomposition Control & Memory Leak Defense
- Use `remember` and `derivedStateOf` for list derived states (e.g. scroll offset > threshold).
- Provide stable keys to `LazyColumn` and `LazyRow` items (`key = { it.id }`).
- Limit in-memory image caches to a maximum percentage of device RAM (15% for Low-End, 25% for High-End).
- Unregister sensor and RTC listeners in Composable `DisposableEffect` cleanups.

---

## 3. Firestore Query & Storage Optimization

### 3.1 Distributed Sharded Counters
To prevent write contention on high-frequency documents (e.g. viral post likes, merchant sales counters, product review tallies):
- High-velocity counters use a subcollection of 10–20 random shards:
  `posts/{postId}/counter_shards/{shardId}`
- A write increments a random shard: `shardId = floor(random() * NUM_SHARDS)`.
- A read aggregates the shards using Firestore `count()` / `sum()` aggregation queries or reads a background-amalgamated rollup field updated every 60 seconds.

### 3.2 Non-Sequential ID Generation
- Auto-generated document IDs utilize distributed random hashes (Base64 / UUIDv4 / Firestore auto-id) rather than monotonic timestamps to prevent write hotspots on Firestore partition keys.

### 3.3 Strict Composite Indexing Catalog
All queries with multiple equality/inequality filters or sorting order must have explicit composite indexes:
- `posts`: `authorId ASC, createdAt DESC`
- `posts`: `tags ARRAY_CONTAINS, createdAt DESC`
- `marketplace_products`: `countryCode ASC, category ASC, price ASC, createdAt DESC`
- `orders`: `customerId ASC, createdAt DESC`
- `orders`: `sellerId ASC, status ASC, createdAt DESC`
- `health_records`: `patientUid ASC, category ASC, recordDate DESC`

---

## 4. Cloud Functions Cold-Start & Concurrency Strategy

1. **Cloud Run 2nd Generation**: Run on Cloud Functions v2 with Cloud Run infrastructure.
2. **Min Instances Warm Pool**: Set `minInstances: 2` for critical latency paths:
   - `auth-token-mint`
   - `qr-verify-grant`
   - `checkout-create-payment`
   - `agora-rtc-token`
3. **Concurrency**: Set `concurrency: 80` to process concurrent I/O-bound requests within the same container, reducing container spin-up overhead by 75%.
4. **Global Singleton Initialization**: SDK clients (Stripe, Twilio, Gemini Vertex AI, SendGrid) are instantiated in module scope outside request handlers to reuse TCP connections.

---

## 5. Health Passport Performance & Tiered Data Loading

1. **Tier 1 (Instant Profile Header)**: Patient emergency blood group, allergy flags, and sovereign verification badge cached locally.
2. **Tier 2 (Category Summary List)**: Diagnoses, prescriptions, immunizations list summaries fetched with limit(15).
3. **Tier 3 (Record Detail Metadata)**: Specific clinical record clicked, metadata decrypted in memory.
4. **Tier 4 (On-Demand Document Retrieval)**: Full resolution radiology scan / lab report PDF is ONLY downloaded when explicitly requested by user or authorized clinician. Medical documents are never pre-downloaded in bulk.

---

## 6. Asynchronous Long-Running Job State Machine

All workloads exceeding 500ms (e.g., AI generation, bulk CSV export, PDF compilation, video compression) execute as background jobs:

```text
[Client POST /jobs] ──► [HTTP Function: validate & insert Firestore job]
                                 │
                                 ▼ status: 'queued'
                         [Cloud Tasks Enqueue]
                                 │
                                 ▼
                         [Worker Function]
                                 │
                                 ▼ status: 'processing', progress: 25%
                         [Gemini / FFmpeg / PDF Engine]
                                 │
                                 ▼ status: 'completed', resultUrl: '...'
                         [FCM Push Notification to Client]
```

---

## 7. Emergency Performance & Degradation Controls

The Platform Owner can execute granular emergency degradation controls via the Owner Control Panel:
- `DISABLE_LIVE_STREAMING`: Halts real-time video broadcasts under network saturation.
- `DISABLE_EXPENSIVE_AI`: Switches Gemini 1.5 Pro jobs to Gemini 1.5 Flash or pauses non-essential AI tools.
- `REDUCE_FEED_REFRESH_RATE`: Extends client feed cache TTL from 2 minutes to 15 minutes.
- `PAUSE_FLASH_SALES`: Caps marketplace concurrent checkout locks during extreme spikes.
- `THROTTLE_BACKGROUND_ANALYTICS`: Suppresses non-essential client telemetry events.
