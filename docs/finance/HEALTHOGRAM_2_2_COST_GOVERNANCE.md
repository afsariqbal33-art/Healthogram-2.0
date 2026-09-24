# Healthogram 2.2 — Cloud Cost Governance & FinOps Architecture

**Document:** `docs/finance/HEALTHOGRAM_2_2_COST_GOVERNANCE.md`  
**Evaluation Scope:** GCP & Firebase Production Cost Controls  
**Target Release:** Healthogram 2.2.0 (Build 20200)  
**Authority:** FinOps Lead, SRE Lead & Engineering Director  
**Classification:** OPERATIONAL FINOPS POLICY  

---

## 1. Production Cost Baseline & Budget Ceilings

In Step 35 production telemetry:
- **Total Monthly Cloud Spend**: **$475.84 USD** across 412,850 registered users.
- **Approved Budget Ceiling**: **$980.00 USD / month**.
- **Current Headroom**: **$504.16 USD (51.4% unused capacity)**.

### Monthly Expense Breakdown (Step 35 Baseline)
```
  ├── Cloud Firestore (Reads/Writes/Storage):  $142.20 (29.9%)
  ├── Cloud Storage & Network Egress:          $ 98.40 (20.7%)
  ├── Cloud Functions (Node 20 Invocations):   $ 74.10 (15.6%)
  ├── WebRTC TURN Relay (Bandwidth Egress):    $ 68.30 (14.4%)
  ├── Vertex AI & Cloud Translation APIs:      $ 46.84 ( 9.8%)
  ├── Firebase Auth & App Check Attestation:   $ 28.00 ( 5.9%)
  └── Cloud Logging & Monitoring:              $ 18.00 ( 3.8%)
  Total:                                       $475.84 USD
```

---

## 2. Unit Economic Metrics & Efficiency Safeguards

To prevent uncontrolled cost spikes as user volume scales toward 1,000,000 users in 2.2, the following unit cost safeguards are strictly enforced:

### 2.1. Cloud Firestore Optimization
- **Local Room Caching**: Mobile clients cache health records, catalog products, and doctor profiles. Re-fetching queries pass `last_modified_at` timestamps, preventing redundant reads. This cached over 12,000,000 reads in Step 35, saving an estimated $72.00/mo.
- **Index Hygiene**: Consolidating composite indexes from 168 to 142 avoids write amplification fees on indexing documents.

### 2.2. Cloud Storage & Egress Optimization
- **Client-Side WebP Transcoding**: The Android client compresses all images to WebP format (80% quality, max 1080p resolution) before uploading. This reduces average file size from 4.2 MB to 280 KB (93% bandwidth saving).
- **Short-Lived Signed URLs**: Media access utilizes Cloud Storage signed URLs with 15-minute TTL, avoiding open public CDNs or continuous streaming egress loops.

### 2.3. Vertex AI Token Governance (`checkAndDeductAiQuota`)
- **Server-Side Quota Gate**: AI requests execute through `functions/src/ai/index.js`, which checks the user's daily token balance before dispatching calls to Gemini 3.8 Flash.
- **Daily Token Limits**:
  - `Individual`: 10,000 tokens/day (Free tier)
  - `Doctor`: 100,000 tokens/day (Clinical summary tier)
  - `Clinic / Hospital`: 500,000 tokens/day (Institutional tier)
- **Zero-Streaming Prompt Hygiene**: Health record summarization prompts use compact structured JSON schemas without verbose chain-of-thought tokens, keeping input tokens $< 600$ per invocation.

### 2.4. WebRTC TURN Relay Cost Containment
- **P2P STUN First Policy**: WebRTC signaling attempts direct peer-to-peer UDP connections for 5 seconds. Only when NAT traversal fails does the client fall back to Google Cloud TURN relay.
- **Consultation Call Cap**: Audio/video consultation sessions are hard-capped at 60 minutes, terminating the TURN relay channel automatically if practitioners forget to hang up.

---

## 3. Automated Budget Alerting & Circuit Breaker Protocol

```
  Monthly GCP Spend Progression:
  
  [0% ─────── 50% ($490) ─────── 80% ($784) ─────── 100% ($980) ─────── 110% ($1,078)]
                 │                     │                    │                   │
                 ▼                     ▼                    ▼                   ▼
           [Stage 1 Alert]       [Stage 2 Alert]      [Stage 3 Alert]     [Stage 4 Circuit Breaker]
           Slack to FinOps       Page on-call SRE     Executive Escalation Non-essential throttling:
           & Architecture        Freeze non-crit PRs  Freeze staging envs  • Pause AI image filters
                                                                           • Throttle media uploads
                                                                           • Restrict to cached feeds
```

- **Stage 1 (50% - $490 USD)**: Informational notification dispatched to Slack `#finops-alerts`.
- **Stage 2 (80% - $784 USD)**: High-priority pager notification to SRE on-call; non-critical staging workloads scaled to zero.
- **Stage 3 (100% - $980 USD)**: Critical alert to CTO/CFO; emergency architectural review initiated.
- **Stage 4 (110% - $1,078 USD - Automated Circuit Breaker)**: Automated Cloud Function temporarily disables non-essential capabilities (AI image generation, heavy video feed preloading) via Remote Config flags while preserving 100% availability for core clinical Health Passport, appointments, and emergency ICE services.
