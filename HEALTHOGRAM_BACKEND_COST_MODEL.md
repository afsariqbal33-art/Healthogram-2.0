# HEALTHOGRAM BACKEND COST MODEL & SCALABILITY ANALYSIS (VERSION 20.0)

This document provides a quantitative unit economics model and scalability analysis for Healthogram's serverless infrastructure on Google Cloud and Firebase.

---

## 1. INFRASTRUCTURE PRICING BASELINE

- **Cloud Firestore**:
  - Document Reads: `$0.06` per 100,000 reads
  - Document Writes: `$0.18` per 100,000 writes
  - Document Deletes: `$0.02` per 100,000 deletes
  - Stored Data: `$0.18` per GB/month
- **Firebase Cloud Storage**:
  - Storage: `$0.026` per GB/month
  - Bandwidth Out: `$0.12` per GB
- **Cloud Functions (2nd Gen)**:
  - Invocations: `$0.40` per million
  - Compute (256MB, 200ms): `$0.0000008` per call
- **Gemini 3.8 Flash**:
  - Input: `$0.075` per million tokens
  - Output: `$0.30` per million tokens
- **Cloud Translation & Speech-to-Text**:
  - Text Translation: `$20.00` per million characters
  - Voice Transcription: `$0.016` per audio minute

---

## 2. ESTIMATED UNIT ECONOMICS TABLE

| Activity / Entity | Firestore Ops per Unit | Compute / Storage per Unit | External API Cost | Total Estimated Unit Cost |
| :--- | :--- | :--- | :--- | :--- |
| **Monthly Active User (MAU)** | 450 reads, 40 writes | 2MB storage | FCM push ($0.00) | **$0.00054 / user / mo** |
| **Social Active User (Feed/Posts)**| 1,200 reads, 95 writes | 50MB CDN bandwidth | None | **$0.0068 / user / mo** |
| **Active Marketplace Seller** | 3,500 reads, 420 writes | 150MB asset storage | None | **$0.024 / seller / mo** |
| **Completed E-Commerce Order** | 45 reads, 28 writes | Minimal storage | Webhook processing ($0.00001) | **$0.00008 / order** |
| **AI Studio Job (Gemini 3.8 Flash)**| 5 reads, 3 writes | Output asset storage | 2,500 tokens ($0.00045) | **$0.00047 / job** |
| **Translation Minute (Live Voice)** | 10 reads, 6 writes | 1 min audio buffer | STT + Translation ($0.018) | **$0.0185 / minute** |
| **WebRTC Video Call Minute** | 4 reads, 2 writes | Signaling only (P2P mesh) | STUN/TURN bandwidth ($0.001) | **$0.0011 / minute** |
| **Health Passport QR Access Grant**| 12 reads, 8 writes | Zero medical data in QR | Cryptographic signature ($0.00) | **$0.00002 / access** |
| **Delivery Shipment Fulfillment** | 35 reads, 22 writes | Proof-of-delivery photo | Geocoding API ($0.005) | **$0.0051 / shipment** |

---

## 3. MULTI-TIER SCALABILITY LOAD PROJECTIONS

| Metric / Dimension | 1,000 Users | 10,000 Users | 100,000 Users | 1,000,000 Users | Architectural Mitigations |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Daily Firestore Reads** | 15,000 | 150,000 | 1,500,000 | 15,000,000 | Cursor-based pagination (`startAfter`), client local caching, CDN caching for public assets. |
| **Daily Firestore Writes** | 1,200 | 12,000 | 120,000 | 1,200,000 | Batched writes, distributed counter shards for high-traffic posts, write rate limits. |
| **Peak Concurrent Calls** | 5 | 50 | 500 | 5,000 | Direct peer-to-peer WebRTC signaling; media relayed through distributed TURN servers only on symmetric NAT. |
| **Daily Order Throughput** | 10 | 100 | 1,000 | 10,000 | Idempotency token deduplication; asynchronous webhook queues prevent write locks. |
| **Daily AI Studio Queries** | 25 | 250 | 2,500 | 25,000 | Gemini 3.8 Flash high throughput quotas; task queues throttle bursts; client rate limits. |
| **Estimated Monthly Cloud Cost**| **$12.50** | **$78.00** | **$680.00** | **$5,950.00** | Highly efficient serverless pricing curve ensures sustainable profit margins from seller commissions. |

---

## 4. ARCHITECTURAL BOTTLENECK ELIMINATION

1. **Hotspot Elimination on Counters**:
   High-velocity social posts do not write to a single document counter on every like. Likes are recorded in isolated `post_likes/{likeId}` documents, and counters are consolidated asynchronously or sharded across 10 distributed counter buckets.

2. **Zero-Read Offset Pagination**:
   Strict avoidance of `offset()` eliminates the O(N) read amplification penalty in Firestore. All UI list queries use `limit(20).startAfter(lastDocument)`.

3. **Storage Tiering**:
   Temporary files (AI audio buffers, ephemeral story videos, temporary QR session caches) are assigned automatic lifecycle rules (24-hour / 7-day TTL deletion) to prevent unbounded storage billing growth.
