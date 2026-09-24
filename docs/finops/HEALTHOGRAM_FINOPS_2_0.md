# HEALTHOGRAM FINOPS 2.0: INFRASTRUCTURE COST ENGINEERING & CAPACITY ECONOMICS

**Document Version:** 2.0.0-FINOPS  
**Classification:** Strategic Financial Engineering  
**Reporting Currency:** USD ($) & OMR (ر.ع.)  

---

## 1. Cost Allocation by Infrastructure Subsystem

| Cloud Service / Provider | Monthly Fixed ($) | Variable Rate Unit | Variable Unit Cost ($) | Primary Cost Driver |
|---|---|---|---|---|
| **Cloud Firestore (nam5 / regional)** | $0.00 | Per 100,000 Reads / Writes | $0.06 / read, $0.18 / write | Social Feed fan-out & messaging |
| **Cloud Storage (Standard + Coldline)**| $0.00 | Per GB / Month + Bandwidth | $0.02 / GB-month, $0.08 / GB egress | Reels video & high-res product photos |
| **Cloud Functions v2 (Serverless)** | $0.00 | Per 1M invocations + GB-sec | $0.40 / 1M inv, $0.0000165 / GB-sec | Image/video resizing, event dispatch |
| **Cloud Tasks (16 Domain Queues)** | $0.00 | Per 1M dispatched tasks | $0.40 / 1M tasks | Asynchronous event processing |
| **Firebase App Check & Auth** | $0.00 | SMS OTP verifications | $0.01 – $0.06 per verification | Phone number onboarding |
| **Google Cloud CDN & Anycast Routing**| $0.00 | Per GB egress from edge | $0.02 – $0.05 per GB | Public video streaming |
| **Vertex AI / Gemini API** | $0.00 | Per 1M prompt / response tokens | $0.15 / 1M input, $0.60 / 1M output | AI Studio medical assistant |
| **Cloud Translation API** | $0.00 | Per 1M translated characters | $20.00 / 1M characters | Arabization & cross-lingual chat |

---

## 2. Modeled Unit Cost per Active User (MAU)

*Note: All values below are **MODELED** based on representative 100,000 MAU activity profile.*

| Workload Category | Consumption per User/Month | Modeled Monthly Cost / User | Optimization Lever |
|---|---|---|---|
| **Firestore Operational Reads/Writes** | 1,200 reads, 150 writes | **$0.00099** | Hybrid fan-out, client SQLCipher cache |
| **Cloud Storage & CDN Bandwidth** | 80 MB transferred | **$0.00400** | H.265 / WebP compression, local caching |
| **Serverless Compute (Functions + Tasks)**| 40 invocations | **$0.00030** | Batch event processing, ephemeral cold starts |
| **Third-Party AI & Translation** | 2 AI interactions, 15 chat translations | **$0.00350** | Local device dictionary, server prompt caching |
| **Authentication & App Check** | 0.2 SMS verifications (sessions cached) | **$0.00500** | 4-device session persistence (minimize SMS) |
| **Total Modeled Cost per Active User** | — | **~$0.01379 / user / month** | Target: Under $0.02 / active user |

---

## 3. Cost Anomaly Detection & Circuit Trip Thresholds

To prevent runaway infrastructure bills during viral events or distributed denial-of-wallet attacks:
1. **Firestore Write Spikes:** Automated alert fires if write rate exceeds $300\%$ of baseline 30-day moving average.
2. **AI Quota Guardrails:** Individual user free AI prompts capped at 10 requests / day.
3. **Storage Ingest Limits:** Single video uploads capped at 150MB; video reels automatically transcoded to multi-bitrate HLS.
4. **Owner Circuit Breaker:** The Platform Owner can toggle `ai_studio_enabled = false` or `translation_enabled = false` from the Owner Control Panel with zero impact on core checkout or consultations.
