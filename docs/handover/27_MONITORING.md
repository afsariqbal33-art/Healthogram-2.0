# 27 — PRODUCTION MONITORING & SITE RELIABILITY

## 1. Monitoring Telemetry Matrix
* **Android Vitals (Play Console):**
  - User-perceived crash rate (Target: < 0.20%, Threshold: 1.09%)
  - User-perceived ANR rate (Target: < 0.10%, Threshold: 0.47%)
  - Slow rendering sessions (Target: < 5%)
* **Firebase Crashlytics:**
  - Real-time fatal crash alerts (P0 triggered if > 0.5% crash rate in 15 minutes)
  - Stack trace grouping and device model distribution
* **Firebase Performance Monitoring:**
  - Network request success rate (Target: > 99.5%)
  - App cold startup latency (Target: < 2.0s 95th percentile)
* **Cloud Firestore & Functions Monitoring:**
  - Read/write quotas, execution durations, active memory usage
  - Permission denial alert (triggers immediate security triage)

## 2. Structured Operational Cadence
* **First Hour:** Real-time crash monitoring and auth flow validation.
* **Daily Standup:** Review 24-hour vitals, crash trends, and support ticket queues.
* **Weekly Health Review:** Financial ledger audit, escrow balance verification, cost baseline evaluation.
