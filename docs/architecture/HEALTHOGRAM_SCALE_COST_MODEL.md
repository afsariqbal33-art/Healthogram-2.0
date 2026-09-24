# HEALTHOGRAM SCALE COST MODEL & UNIT ECONOMICS (2.0)

**Document Version:** 2.0.0  
**Classification:** Financial Engineering, Cloud Economics & Infrastructure Budgeting  
**Base Cloud Environment:** Google Cloud Platform (GCP) & Firebase Enterprise  

---

## 1. Unit Economics & Variable Cost Breakdown

| Workload Activity | Cloud Services Consumed | Unit Cost (USD) | Scaling Behavior |
| :--- | :--- | :--- | :--- |
| **Active User / Month** | Firebase Auth (Phone OTP amortized), Firestore session reads, FCM token sync | **$0.0120** | Linear with Monthly Active Users (MAU). |
| **Marketplace Order** | Firestore double-entry writes, Cloud Function execution, Stripe webhook verification | **$0.0085** | Linear with completed orders (offset by 10% platform fee). |
| **AI Studio Job (Text)** | Gemini 1.5/2.0 Flash prompt tokens (~500 tokens in, ~300 tokens out) | **$0.00015** | Metered per generative creation; governed by monthly user quota. |
| **AI Studio Job (Media)**| Multimodal image generation / editing | **$0.0200** | Metered per creation; capped per creator account tier. |
| **Translation Request** | Cloud Translation API (~200 characters cached locally) | **$0.0004** | Cached in `translation_cache` (85% cache hit rate). |
| **GB Media Stored** | Cloud Storage Multi-Region (Dual-Region nam5) | **$0.0260 / GB / mo** | Storage lifecycle policies transition stale reels to Coldline. |
| **GB Media Streamed** | Cloud CDN network egress | **$0.0400 / GB** | Optimized via adaptive HLS streaming & client Coil cache. |
| **Teleconsultation (min)**| WebRTC STUN/TURN bandwidth relay (only required on symmetric NATs ~15%) | **$0.0020 / min** | Direct peer-to-peer P2P is $0.00; TURN relay costs minimal. |
| **Push Notification** | Firebase Cloud Messaging (FCM) high priority | **$0.0000** | Free tier infrastructure; zero per-message cost. |

---

## 2. Monthly Fixed Baseline Costs

| Infrastructure Component | Purpose | Monthly Cost (USD) |
| :--- | :--- | :--- |
| **Cloud Armor WAF Policy** | DDoS protection, geo-rate limiting, IP reputation rules | $45.00 |
| **Cloud KMS Keys** | Envelope encryption keys for field-level Health Passport protection | $12.00 |
| **Google Secret Manager** | Production credential, API key & payment secret storage | $6.00 |
| **Cloud Logging & Monitoring**| 30-day retention for audit logs and error budgets | $85.00 |
| **Total Fixed Infrastructure**| Baseline platform readiness reservation | **$148.00 / month** |

---

## 3. Projected Scale Scenarios

### Scenario A: 100,000 MAU (Post-Launch Phase)
- **Active Users:** $1,200 / month
- **Orders (20,000 orders/mo):** $170 / month
- **Media Storage & CDN (10 TB stored, 25 TB egress):** $1,260 / month
- **AI & Translation Jobs:** $210 / month
- **Fixed Infrastructure:** $148 / month
- **Total Cloud Spend:** **$2,988 / month**
- **Estimated Platform Gross Revenue (at $25 AOV, 10% take-rate):** **$50,000 / month**
- **Gross Infrastructure Margin:** **94.0%**

### Scenario B: 1,000,000 MAU (Growth Phase)
- **Active Users:** $10,500 / month (volume discount tier)
- **Orders (250,000 orders/mo):** $1,875 / month
- **Media Storage & CDN (100 TB stored, 300 TB egress):** $13,200 / month
- **AI & Translation Jobs:** $1,900 / month
- **Fixed Infrastructure + Cloud Armor Advanced:** $450 / month
- **Total Cloud Spend:** **$27,925 / month**
- **Estimated Platform Gross Revenue (at $25 AOV, 10% take-rate):** **$625,000 / month**
- **Gross Infrastructure Margin:** **95.5%**

---

## 4. Cost Governance & Anomaly Detection Rules

1. **AI Studio Spend Guard:** Automated circuit breaker immediately suspends non-essential AI generation if daily token burn exceeds 150% of the 7-day rolling average.
2. **Firestore Read Alert:** Trigger P1 SRE alert if any single Cloud Function execution performs > 500 Firestore document reads.
3. **Storage Coldline Archival:** All video files unviewed for > 90 days automatically transition from Standard to Coldline storage ($0.007/GB), reducing long-term archival costs by 73%.
