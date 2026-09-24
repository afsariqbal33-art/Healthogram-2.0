# HEALTHOGRAM 2.2.0 PRODUCTION MONITORING & OBSERVABILITY PLAN

**Target Release:** 2.2.0 (versionCode 20201)  
**Lead Roles:** Production Operations Engineer, SRE, Firebase Production Engineer  

---

## 1. Key Observability Metrics & SLIs
| Domain | Metric / SLI | Target Objective | Alert Threshold (P1/P0) |
| :--- | :--- | :--- | :--- |
| **Android Vitals** | User-perceived crash rate | < 1.09% | > 0.50% (P1), > 1.0% (P0) |
| **Android Vitals** | User-perceived ANR rate | < 0.47% | > 0.20% (P1), > 0.40% (P0) |
| **Authentication** | Auth failure / lockout rate | < 0.5% | > 2.0% of attempts |
| **Health Passport** | QR Token decryption failure | 0% | > 0.1% of QR scans |
| **Ledger / Checkout** | Double-entry balance skew | 0.00 | Any imbalance > 0 (P0) |
| **Messaging / Signaling**| WebRTC connection setup time| < 2,500 ms | P95 > 5,000 ms |
| **Functions** | Cloud Function 5xx / Error rate | < 0.1% | > 1.0% of calls |

---

## 2. Telemetry Channels & Dashboards
* **Firebase Crashlytics:** Real-time crash alerting integrated with On-Call PagerDuty.
* **Google Play Console Vitals:** Daily and hourly crash/ANR tracking against bad behavior thresholds.
* **Cloud Monitoring:** Cloud Function latency, Firestore read/write quotas, and egress bandwidth.
* **Financial Ledger Reconciliation Daemon:** Hourly double-entry automated integrity checks.
