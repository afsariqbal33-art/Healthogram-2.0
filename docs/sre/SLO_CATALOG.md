# HEALTHOGRAM SERVICE LEVEL OBJECTIVE (SLO) CATALOG

**Document Version:** 2.0.0-SRE  
**Status:** ENFORCED IN OBSERVABILITY 2.0  
**Measurement Window:** 30-Day Rolling Window  

---

## 1. Core Service Level Objectives

| Service Name | Availability SLO (%) | Latency SLO (p95) | Latency SLO (p99) | Error Budget (Monthly) | Critical Failure Definition |
|---|---|---|---|---|---|
| **Health Passport Vault** | **99.95%** | < 250ms | < 600ms | 21.6 minutes | Unauthorized read or data unavailability |
| **Payments & Financial Ledger** | **99.95%** | < 300ms | < 800ms | 21.6 minutes | Checkout failure or balance drift |
| **Authentication & App Check** | **99.90%** | < 200ms | < 500ms | 43.2 minutes | Login failure or token rejection |
| **Emergency RTC Calling** | **99.85%** | < 100ms (signaling) | < 250ms | 64.8 minutes | Signaling failure or dropped call setup |
| **1-on-1 Messaging Channels** | **99.80%** | < 200ms | < 500ms | 86.4 minutes | Message delivery failure |
| **Marketplace Order Checkout** | **99.80%** | < 400ms | < 1000ms | 86.4 minutes | Cart lock or inventory failure |
| **Social Feed & Reels Timeline**| **99.50%** | < 150ms | < 400ms | 216.0 minutes | Feed blanking or pagination failure |
| **Search & Discovery Engine** | **99.50%** | < 200ms | < 500ms | 216.0 minutes | Search timeout or 500 error |
| **AI Studio & Smart Tools** | **99.00%** | < 2000ms | < 4000ms | 432.0 minutes | Generation timeout (graceful fallback) |
| **Cloud Translation** | **99.00%** | < 500ms | < 1500ms | 432.0 minutes | Translation drop (graceful fallback) |

---

## 2. Client-Side Android Application Performance Objectives

- **Crash-Free User Sessions:** $\ge 99.85\%$ of daily active user sessions.
- **Application Not Responding (ANR) Rate:** $\le 0.05\%$ (strictly below Google Play bad behavior threshold of 0.47%).
- **Cold App Launch Time:** $\le 1200\text{ms}$ on median reference devices (Android 14–16).
- **Screen Rendering Latency:** $\le 16.6\text{ms}$ (60 FPS smooth Compose recomposition, < 1% jank frames).
