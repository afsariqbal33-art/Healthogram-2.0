# HEALTHOGRAM — STEP 21: PRODUCTION PERFORMANCE RUNBOOK & INCIDENT RESPONSE

## 1. Scope & Objective
This runbook provides on-call Site Reliability Engineers (SRE), Platform DevOps, and Platform Owners with standardized procedures to diagnose, mitigate, and resolve performance degradations across the Healthogram infrastructure.

---

## 2. Real-Time Alert Triage Matrix

| Metric Alert | Severity | Diagnostic Steps | Immediate Mitigation Action |
| :--- | :--- | :--- | :--- |
| **Firestore Query Latency > 1.5s** | HIGH (P1) | 1. Check Cloud Monitoring for missing composite index warnings.<br>2. Inspect slow query logs for unindexed filters. | Deploy missing composite index via `firestore.indexes.json`. Enable client cache fallback. |
| **Cloud Functions Error Rate > 2%** | CRITICAL (P0) | 1. Review Cloud Logging error traces.<br>2. Check for third-party gateway timeouts (Stripe/SMSA/Agora). | 1. Trigger Circuit Breaker for failing upstream provider.<br>2. Switch to backup provider in Platform Config. |
| **Cloud Storage Egress Spike** | MEDIUM (P2) | 1. Inspect CDN cache-hit ratio in Cloud CDN console.<br>2. Identify hot uncompressed media files. | Purge CDN cache, verify `Cache-Control` headers, enable strict client WebP variant serving. |
| **Realtime DB Concurrent Connections > 80%** | HIGH (P1) | 1. Verify client presence disconnect hooks.<br>2. Check for zombie socket connections. | Force client reconnect with exponential jitter; prune stale presence nodes. |
| **Hot Document Contention Warning** | CRITICAL (P0) | 1. Identify document ID experiencing write lock timeouts.<br>2. Check shard distribution. | Increase counter shard count from 10 to 50 via Owner Control Panel. |

---

## 3. Emergency Owner Control Panel Actions

In catastrophic traffic spikes or systemic platform stress, Platform Owners can invoke Emergency Controls from the Owner Dashboard (`/owner/performance`):

### 3.1 Step 1: Engage Graceful Degradation Mode
- Click `ENABLE_PERFORMANCE_SAFEGUARD`.
- Automatically raises client cache TTL to 15 minutes.
- Enforces low-resolution thumbnail delivery across social feeds.
- Deactivates non-essential UI animations and video autoplay.

### 3.2 Step 2: Throttle Heavy Compute Workloads
- Click `THROTTLE_AI_STUDIO`: Pauses non-critical generative AI tasks; queues requests for off-peak execution.
- Click `DISABLE_LIVE_STREAMING`: Halts real-time video streaming broadcasts while preserving essential 1-on-1 telehealth video appointments.

### 3.3 Step 3: Flash Sale Queue Throttling
- Click `ENGAGE_MARKETPLACE_RATE_LIMITER`: Activates virtual waiting room for high-demand product drops to protect payment processing and inventory locks.

---

## 4. Post-Incident Review (PIR) Checklist
1. Export Cloud Monitoring latency charts (P50, P90, P99).
2. Quantify impact (affected user sessions, delayed orders, failed calls).
3. Document root cause analysis (RCA) and identify missing synthetic test coverage.
4. Update `HEALTHOGRAM_PERFORMANCE_TARGETS.md` with revised capacity thresholds.
