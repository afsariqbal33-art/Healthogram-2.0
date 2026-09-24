# ADR-019: SEPARATION OF OPERATIONAL TRANSACTIONS FROM ANALYTICAL DATA WAREHOUSING

**Status:** ACCEPTED  
**Date:** 2026-09-17  
**Deciders:** Data Platform Architect, Performance Engineer, Cloud Infrastructure Lead  

---

## 1. Context
Running complex analytical aggregations (DAU/WAU/MAU, gross merchandise value across quarters, historical cohort churn) directly against the operational Cloud Firestore database consumes massive read quotas, creates lock contention, and slows user transactions.

## 2. Problem
How do we provide rich, real-time Owner and Admin executive dashboards while insulating operational Firestore transactional latency?

## 3. Options Considered
- **Option A: Real-Time On-Demand Firestore Aggregations:** Execute `count()`, `sum()`, and wide collection scans whenever dashboards load.
- **Option B: Distributed Operational Checkpoints + Asynchronous BigQuery Export (Selected):** Maintain pre-aggregated operational counters (`analytics_daily_rollups`, `owner_metrics_summary`) updated via asynchronous event consumers. Export cold operational logs to BigQuery for long-term historical cohort modeling.

## 4. Decision
Adopt **Option B: Distributed Operational Checkpoints + Asynchronous BigQuery Export**. Operational Firestore stores only current state and rolling 30-day summaries.

## 5. Reason
Reduces executive dashboard load times from 8+ seconds to < 120ms while saving over 90% of analytics-related Firestore read costs.

## 6. Tradeoffs & Consequences
- Historical reports (> 30 days) are queried through data warehouse connectors rather than direct Firestore queries.
- Health Passport sensitive clinical data is permanently excluded from analytical export pipelines.
