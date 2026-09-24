# ADR-2.2-010: Observability, Error Budgets & SLA Monitoring

**Status:** ACCEPTED  
**Date:** 2026-09-20  
**Context:**  
Healthogram operates 24/7 across critical clinical and commercial workflows. In Step 35, the platform achieved 99.94% crash-free sessions and 99.97% service availability. As adoption grows, SRE teams require unified visibility into client crashes, serverless latency, FHIR gateway health, and error budget burn rates.

**Decision:**  
1. **Four Golden Signals**: We track Latency (P50, P95, P99), Traffic (requests/sec), Errors (HTTP 5xx, schema rejections), and Saturation (Cloud Function concurrency, Firestore quotas).
2. **Standardized Telemetry Stack**:
   - Client Crashes & Vitals: Firebase Crashlytics + Google Play Vitals.
   - Cloud Infrastructure & Logging: Google Cloud Logging & Cloud Monitoring.
   - SRE Alerting: Prometheus metrics and Slack/PagerDuty escalation channels.
3. **Error Budget Governance**:
   - Monthly Service Level Objective (SLO): 99.90% availability for clinical endpoints.
   - If error budget consumption exceeds 20% in a single week, non-critical feature deployments are frozen to prioritize reliability remediation.

**Consequences:**  
- **Positive**: Proactive incident detection; rapid mean time to resolution (MTTR $< 15\text{m}$); clear criteria for production rollout gating.
- **Negative**: Adds minor observability logging overhead, budgeted within standard cloud costs.
