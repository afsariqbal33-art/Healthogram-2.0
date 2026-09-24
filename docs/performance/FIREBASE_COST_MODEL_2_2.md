# HEALTHOGRAM 2.2 FIREBASE COST MODEL & FINOPS GOVERNANCE

**Financial Model:** 100,000 Daily Active Users (DAU) / 3,000,000 Monthly Active Users (MAU)  
**Cost Discipline:** Every query, function invocation, and media byte is budgeted and guarded.

---

## 1. Subsystem Monthly Cost Breakdown (100,000 DAU)

| Infrastructure Service | Monthly Usage Volume | Unit Cost Basis | Monthly Expense (USD) | FinOps Control |
| :--- | :--- | :--- | :---: | :--- |
| **Cloud Firestore** | 251M reads, 24.5M writes, 120GB storage | $0.06 / 100k reads, $0.18 / 100k writes | $194.96 | Cursor pagination + 10-shard counters |
| **Cloud Functions v2** | 45M invocations, 18M vCPU-seconds | $0.40 / M calls, $0.000024 / vCPU-s | $182.40 | Concurrency 80 per instance |
| **Cloud Storage & CDN** | 1.8 TB storage, 6.5 TB edge egress | $0.026 / GB, $0.08 / GB egress | $246.80 | WebP 82% compression + aggressive edge caching |
| **Firebase Realtime DB** | 4.2 TB GB-downloaded, 15k peak conns | $1.00 / GB transfer | $168.00 | Payload compaction (< 512B) + 2s typing throttle |
| **Gemini AI Studio** | 250k summary calls (Gemini 1.5 Flash) | $0.075 / M input tokens | $45.00 | In-memory phrase caching + async queues |
| **Cloud Tasks & Pub/Sub** | 8.5M jobs dispatched | $0.40 / M operations | $3.40 | Batch multicast processing |
| **Firebase App Check** | 85M device attestations | Free tier / Play Integrity API | $0.00 | Included within Google Cloud tier |
| **MONTHLY TOTAL** | — | — | **$840.56** | **Cost per DAU: $0.0084 / month** |

---

## 2. FinOps Alerts & Automated Circuit Breakers

* **Budget Thresholds:** Configured at 50% ($500), 80% ($800), and 100% ($1,000) of target monthly burn.
* **Cost Spike Circuit Breaker:** If daily Cloud Function invocations exceed 300% of the moving 7-day average, non-essential background tasks are queued and the SRE on-call is paged automatically.
