# HEALTHOGRAM SUBSYSTEM CAPACITY PLAN & BOTTLENECK THRESHOLDS

**Document Version:** 2.0.0-CAPACITY  
**Status:** ENFORCED IN PRODUCTION OPS  
**Classification:** Infrastructure Sizing & Scaling Rules  

---

## 1. Subsystem Capacity Matrix

| Subsystem | Tested Capacity | Modeled Capacity | Known Bottleneck | Scaling Strategy | Warning Threshold | Emergency Threshold |
|---|---|---|---|---|---|---|
| **Health Passport QR** | 500 scans/sec | 5,000 scans/sec | Cryptographic grant handshake | Sharded token verification | > 350 scans/sec | > 450 scans/sec |
| **Financial Ledger** | 250 tx/sec | 3,000 tx/sec | ACID serializable balance lock | Partitioned seller balance nonces | > 180 tx/sec | > 220 tx/sec |
| **Social Feed Engine** | 1,500 reads/sec| 25,000 reads/sec| High-follower fan-out | Hybrid fan-out (read merge) | > 1,000 reads/sec| > 1,350 reads/sec|
| **Marketplace Inventory**| 400 res/sec | 4,000 res/sec | Single SKU stock contention | Fractional inventory buckets | > 280 res/sec | > 350 res/sec |
| **Video Transcoding** | 20 jobs/sec | 150 jobs/sec | Cloud Run worker CPU | Dynamic Cloud Tasks throttle | > 15 jobs/sec | > 18 jobs/sec |
| **1-on-1 Chat Presence**| 5,000 conns | 100,000 conns | Realtime Database connections | Ephemeral disconnect pruning | > 3,500 conns | > 4,500 conns |
| **FCM Push Dispatch** | 1,000 msg/sec| 50,000 msg/sec | Third-party FCM rate limits | Batch message fan-out queues | > 750 msg/sec | > 900 msg/sec |

---

## 2. Infrastructure Scaling Triggers

1. **Cloud Functions v2:** Set to scale up to 1,000 concurrent instances with a minimum of 2 idle warm instances in primary regions to eliminate cold starts on checkout hot paths.
2. **Cloud Tasks Rate Limiting:** Enforces maximum concurrent dispatches (e.g. 15 for AI, 20 for Video Transcoding, 200 for Notifications) to protect downstream services from degradation.
3. **Firestore Index Explosion Guard:** Restricts composite index generation to strictly verified query patterns, preventing Firestore write amplification.
