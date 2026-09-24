# ADR-010: SCALE-OUT ROADMAP, PARTITIONING & MULTI-REGION READ REPLICAS

**Status:** ACCEPTED  
**Date:** 2026-09-17  
**Deciders:** Principal Systems Architect, Database Architect, Site Reliability Lead  

---

## 1. Context
Healthogram is transitioning from its v1.0 launch footprint to Healthogram 2.0, targeting millions of concurrent active users across multiple geographical regions with heavy mixed workloads (write-heavy financial ledgering, read-heavy social feed browsing, and low-latency real-time video teleconsultation).

## 2. Problem
How do we ensure linear scalability, sub-200ms API response latencies, and high availability (99.99%) across global regions without incurring uncontrollable database contention, Firestore write hotspots, or explosive cloud infrastructure bills?

## 3. Options Considered
- **Option A: Vertical Scaling & Sharded SQL:** Migrate the entire database layer to Google Cloud Spanner or sharded PostgreSQL clusters.
- **Option B: Unconstrained NoSQL Document Expansion:** Continue with unpartitioned Firestore collections relying purely on default auto-indexing.
- **Option C: Tiered Scale-Out Strategy with Distributed Sharding, Edge Caching & Analytical Offloading (Selected):** Retain Multi-Region Cloud Firestore (nam5 / regional replicas) as the primary transactional document store, implement distributed counter sharding for high-frequency writes (likes, views), deploy Cloud CDN edge caching for public media and static feeds, offload cold analytics to BigQuery, and use Cloud Tasks for queue-based write smoothing.

## 4. Decision
Adopt **Option C: Tiered Scale-Out Strategy with Distributed Sharding, Edge Caching & Analytical Offloading**. High-velocity counters (social likes, inventory reservation nonces) are sharded across 10–20 sub-documents. Read-heavy content is cached at the edge.

## 5. Reason
Firestore scales horizontally to millions of concurrent connections out of the box, provided individual document write limits (1 write/second/document) are respected via distributed counter sharding. Avoids the multimillion-dollar baseline operational overhead of Cloud Spanner while delivering predictable performance and costs.

## 6. Tradeoffs
- Aggregating sharded counters requires sum operations across shard subcollections.
- Analytics queries cannot run directly against the live transactional database and must query BigQuery replicas.

## 7. Consequences
- Zero write hotspot contention even during viral creator posts or flash marketplace sales.
- Platform throughput scales linearly with user growth.
- Database operational costs remain directly proportional to verified active user engagement.
