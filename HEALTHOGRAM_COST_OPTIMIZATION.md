# HEALTHOGRAM — STEP 21: CLOUD COST OPTIMIZATION & RESOURCE EFFICIENCY

## 1. Cost Optimization Objectives & Principles
As Healthogram scales to millions of users across sovereign jurisdictions, infrastructure costs must scale sub-linearly with user growth. This is achieved by amortizing expensive database reads, minimizing cloud compute duration, and aggressively compressing media transfer.

---

## 2. Firestore Cost Reduction Strategies

### 2.1 Aggressive Client-Side Read Caching
- **Profile & Metadata Caching**: User profiles, seller details, and country configurations are cached locally in memory and persistent storage with a 15-minute TTL.
- **Unread Counters**: Instead of running continuous real-time queries for total unread notifications or messages, aggregate counts are stored directly on the `users/{uid}` document and updated via atomic increments.

### 2.2 Bundled & Aggregate Queries
- Use Firestore Count Aggregation (`count()`) which charges 1 read per 1,000 index entries, rather than reading 1,000 full documents.
- Admin dashboards read precomputed daily rollup documents (`daily_metrics/YYYY-MM-DD`) instead of scanning raw order and post collections.

### 2.3 Sharded Write Distribution
- Distributing high-frequency writes (likes, views, sales) across 10–20 shards eliminates lock contention and failed retry write costs.

---

## 3. Cloud Functions & Compute Optimization

1. **Right-Sizing Memory & CPU**:
   - Lightweight webhook & auth validators: Allocated 256MB RAM / 0.5 vCPU.
   - Standard API endpoints: Allocated 512MB RAM / 1 vCPU.
   - Media & PDF generators: Allocated 2GB RAM / 2 vCPU with auto-shutdown.
2. **Short Timeouts**:
   - Synchronous APIs capped at 10 seconds.
   - Webhook processors capped at 15 seconds.
   - Background tasks capped at 120 seconds.
3. **Execution Concurrency**:
   - Utilizing Cloud Run concurrency (80 requests per instance) reduces the total container instance footprint by up to 80% compared to Cloud Functions v1 (1 request per container).

---

## 4. Cloud Storage & Network Egress Optimization

1. **Format Standardization**: Convert all uploaded raster images to WebP format, achieving a 65–80% size reduction compared to raw JPEG/PNG without diagnostic quality loss.
2. **Cloud CDN Caching**:
   - All public media (avatars, product images, reel video segments) served through Cloud CDN with `Cache-Control: public, max-age=31536000, immutable`.
   - Edge cache hits reduce Cloud Storage egress bandwidth by over 90%.
3. **Lifecycle Rules for Ephemeral Storage**:
   - Temporary upload staging (`/uploads/tmp/*`) auto-deleted after 24 hours.
   - Realtime RTC recording chunks moved to Coldline Storage after 30 days.

---

## 5. Third-Party API & AI Token Budgeting

1. **Gemini AI Model Tiering**:
   - Routine text tasks (captions, hashtags, product descriptions) utilize **Gemini 1.5 Flash** ($0.075 / 1M input tokens).
   - High-complexity clinical tasks (medical summary verification, doctor clinical note drafting) selectively use **Gemini 1.5 Pro**.
2. **AI Rate Limiting & Monthly Quotas**:
   - Individual accounts capped at 25 AI requests per month.
   - Doctors capped at 150 AI requests per month.
   - Sellers capped at 100 catalog requests per month.
   - Excessive requests rejected client-side before invoking the Gemini API.
3. **Translation Memory Cache**:
   - Frequently translated phrases, UI strings, and common medical terms are hashed and cached in Redis/Firestore, avoiding redundant Google Cloud Translation API calls.
