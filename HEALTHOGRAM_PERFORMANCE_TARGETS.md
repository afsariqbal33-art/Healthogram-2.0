# HEALTHOGRAM — STEP 21: PERFORMANCE TARGETS & SERVICE LEVEL OBJECTIVES (SLO)

## 1. Executive Summary & Principles
Healthogram operates as a dual-vertical mission-critical platform combining a sovereign healthcare ecosystem (Individual, Doctor, Clinic, Hospital, Laboratory) with a multi-merchant health marketplace (Customer, Seller). Performance optimization must strictly adhere to the **Zero-Trust Security Principle**:
> **Absolute Security Invariant**: Performance optimization must NEVER weaken platform security. App Check, cryptographic Health Passport grants, single-use QR sessions, double-entry financial ledger integrity, and PII/PHI redaction remain mandatory and non-negotiable.

---

## 2. Core Latency & Responsiveness Targets

| Architectural Layer / Operation | Target SLO (P50) | Target SLO (P95) | Degradation Threshold (Alert) | Bottleneck Mitigation |
| :--- | :--- | :--- | :--- | :--- |
| **Mobile App Cold Launch** | ≤ 1.8 s | ≤ 3.0 s | > 3.5 s | Deferred SDK init, lazy DI, cached feature flags |
| **Mobile App Warm Launch** | ≤ 600 ms | ≤ 1.5 s | > 2.0 s | Preserved navigation state, warm memory cache |
| **Primary Page Navigation** | ≤ 200 ms | ≤ 500 ms | > 800 ms | Above-the-fold rendering, shimmer skeletons |
| **Firestore Read Queries** | ≤ 180 ms | ≤ 500 ms | > 1.0 s | Composite indexes, limit(20), cursor pagination |
| **Cloud Functions (Synchronous API)**| ≤ 220 ms | ≤ 500 ms | > 1.0 s | Cloud Run 2nd Gen, connection pooling, warm instances |
| **Heavy Workloads (AI / Reports)** | Asynchronous | Job queue | Timeout > 60 s | State machine (`queued` -> `processing` -> `completed`)|
| **Messaging Acknowledgement** | ≤ 120 ms | ≤ 300 ms | > 600 ms | Optimistic local append, clientMessageId dedup |
| **Typing / Online Presence** | ≤ 80 ms | ≤ 200 ms | > 500 ms | Ephemeral Realtime DB / throttled pulses |
| **Marketplace Catalog First Content**| ≤ 600 ms | ≤ 1.5 s | > 2.0 s | Search index pagination, product summary models |
| **Health Passport Record List** | ≤ 400 ms | ≤ 800 ms | > 1.5 s | Tiered category loading, on-demand document fetch |
| **Checkout Pipeline Execution** | ≤ 800 ms | ≤ 1.8 s | > 3.0 s | Server-side validation isolation, async analytics |

---

## 3. Query Limits & Result Set Policy

Under no circumstance shall any client or backend routine execute an unbounded Firestore collection query.

| Domain / View | Page Size Limit | Pagination Mechanism | Preload Strategy |
| :--- | :--- | :--- | :--- |
| **Home Social Feed** | 10–20 posts | `limit(20).startAfter(lastDoc)` | Next 1–2 posts metadata only |
| **Explore Grid** | 20 items | Cursor-based infinite scroll | Thumbnail cards only |
| **Health Reels** | 5–10 items | Single-item snapping, cursor | Preload next video, discard -2 videos |
| **Post Comments** | 20 comments | `startAfter(lastDoc)` | Top 3 comments preview on card |
| **Notifications Feed** | 20 alerts | `limit(20).startAfter(lastDoc)` | Unread count badge cached |
| **Direct Messaging History** | 30 messages | Backward cursor pagination | Last 30 messages in memory |
| **Marketplace Catalog** | 20 products | Cursor by category/country | Summary snapshot (ID, title, thumb, price)|
| **Customer Orders List** | 20 orders | Chronological cursor | Order header + status badge |
| **Seller Inventory Table** | 20 items | Indexed SKU query | Paginated table with search |
| **Admin Moderation Tables** | 25–50 rows | Server-side cursor filter | Precomputed summary metrics |
| **Health Passport Records** | 15 records | Category-scoped cursor | Metadata + encrypted thumbnail only |

---

## 4. Multi-Device & Network Profile Baselines

Healthogram must remain fully functional across all four benchmark tiers:

### 4.1 Low-End Android Devices (e.g., 2GB RAM, Quad-Core Cortex-A53)
- Max image cache: 48 MB RAM
- Video autoplay: Disabled or thumbnail-first with user tap-to-play
- Skeletons: Simplified static shimmers to eliminate GPU thread jank
- Memory threshold: Enforce aggressive bitmap recycling and GC lifecycle hooks

### 4.2 Mid-Range Android Devices (e.g., 4GB–6GB RAM, Octa-Core)
- Max image cache: 128 MB RAM
- Video preload: Next 1 video buffered up to 3 seconds
- Skeletons: Full shimmering skeleton placeholders with M3 surface colors

### 4.3 High-End Flagship Android Devices (8GB+ RAM, Adreno 7xx / Mali-G7xx)
- Max image cache: 256 MB RAM
- Video preload: Next 2 videos buffered smoothly
- Animations: 60/120 FPS transitions with fluid spring physics

### 4.4 Desktop & Laptop Web (Chrome / Edge / Safari)
- Responsive wide-canvas canonical layout (Navigation rail, list-detail panes)
- Precomputed metric summaries rendered with SVG charts
- Keyboard navigation shortcuts and mouse pointer hover states

### 4.5 Degraded & Slow Network Environments (2G / 3G / High-Latency Satellite)
- Auto-adaptive media quality: Switch to low-res WebP thumbnails
- Request timeout: 15 seconds with exponential backoff and jitter
- Offline cache: Read-only access to previously verified passport snapshots, local drafts, and shopping carts

---

## 5. Media Pipeline & Storage Bandwidth Limits

```text
User Upload (Camera / Gallery)
        ↓
Client-Side Image Compression & EXIF Strip (Target ≤ 350 KB)
        ↓
Cloud Storage Ingestion (`/uploads/raw/`)
        ↓
Eventarc / Cloud Function (`processMediaVariants`)
        ↓
WebP Variants Generated:
  ├── thumbnail (80x80 @ 80% q)   → Avatars, compact lists
  ├── small     (320x320 @ 80% q) → Grid cells, comment cards
  ├── medium    (720x720 @ 82% q) → Feed cards, marketplace details
  └── large     (1440x1440 @ 85% q) → Fullscreen viewer
```

- **Medical Document Preservation**: Diagnostic lab reports, clinical scans, and paper prescriptions retain an archival-quality original PDF/PNG in encrypted Storage. UI previews strictly use generated thumbnails.

---

## 6. Target Acceptance Scorecard (Section 97 & 100)

```text
App Performance:               100/100
Database Performance:          100/100
Backend Performance:           100/100
Media Performance:             100/100
Messaging Performance:         100/100
Marketplace Performance:       100/100
Health Passport Performance:   100/100
AI Performance:                100/100
Translation Performance:       100/100
Admin Performance:             100/100
Owner Dashboard Performance:   100/100
Scalability:                   100/100
Cost Efficiency:               100/100

OVERALL PERFORMANCE READINESS: 100/100 [TARGET]
```
