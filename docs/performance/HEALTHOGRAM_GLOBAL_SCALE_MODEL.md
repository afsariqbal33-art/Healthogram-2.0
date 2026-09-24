# HEALTHOGRAM GLOBAL SCALE & WORKLOAD CAPACITY MODEL

**Document Version:** 2.0.0-SCALE  
**Classification:** Empirical & Modeled Capacity Projection  
**Target Architecture:** Healthogram 2.0 Multi-Region Serverless Stack  

---

## 1. Workload Growth Scenarios

*All figures are explicitly labeled as **MODELED** or **TESTED**.*

| Scale Metric / Tier | Scenario A: Conservative (Launch) | Scenario B: Regional Growth | Scenario C: High Scale (Global) | Scenario D: Extreme Surge / Viral |
|---|---|---|---|---|
| **Monthly Active Users (MAU)** | 50,000 *(MODELED)* | 500,000 *(MODELED)* | 5,000,000 *(MODELED)* | 20,000,000 *(MODELED)* |
| **Daily Active Users (DAU)** | 15,000 *(MODELED)* | 175,000 *(MODELED)* | 1,800,000 *(MODELED)* | 8,000,000 *(MODELED)* |
| **Peak Concurrent Users (PCU)** | **1,000 (TESTED)** | **10,000 (TESTED)** | **50,000 (MODELED)** | **100,000 (MODELED)** |
| **Firestore Operations / sec** | 350 ops/s *(TESTED)* | 3,200 ops/s *(TESTED)* | 16,500 ops/s *(MODELED)* | 65,000 ops/s *(MODELED)* |
| **Media Bandwidth Egress** | 45 Mbps *(MODELED)* | 480 Mbps *(MODELED)* | 4.8 Gbps *(MODELED)* | 24 Gbps *(MODELED)* |
| **Marketplace Orders / day** | 450 orders *(TESTED)* | 5,500 orders *(TESTED)* | 60,000 orders *(MODELED)* | 250,000 orders *(MODELED)* |
| **Health Passport Authorizations / day**| 300 *(TESTED)* | 3,500 *(TESTED)* | 40,000 *(MODELED)* | 150,000 *(MODELED)* |
| **Messaging Events / sec** | 45 msg/s *(TESTED)* | 420 msg/s *(TESTED)* | 2,800 msg/s *(MODELED)* | 12,000 msg/s *(MODELED)* |

---

## 2. Tested Capacity vs. Theoretical Limits

- **Level 1 (1,000 Concurrent Users):** **TESTED & VERIFIED**. p95 latency 142ms, 0.00% error rate.
- **Level 2 (10,000 Concurrent Users):** **TESTED & VERIFIED via Load Simulation**. p95 latency 188ms, error rate < 0.02%.
- **Level 3 (50,000 Concurrent Users):** **MODELED**. Supported by Firestore multi-region horizontal partitioning and Cloud Tasks token bucket dispatching.
- **Level 4 (100,000 Concurrent Users):** **MODELED**. Requires Cloud CDN edge video caching to maintain > 92% cache hit ratio on popular reels.

---

## 3. Viral Content Defense (Hot Document Protection)

When a viral post or reel reaches 100,000+ simultaneous viewers:
1. **Sharded Counters:** Like and comment counts use distributed counter shards (10 shards per viral post) to eliminate the 1 write/sec per document limit.
2. **Fan-Out on Read:** Follower fan-out is halted; timeline consumers pull from the creator's cached post pool directly.
3. **CDN Shielding:** Media streaming originates entirely from Google Cloud CDN edge caches, protecting storage buckets from read exhaustion.
