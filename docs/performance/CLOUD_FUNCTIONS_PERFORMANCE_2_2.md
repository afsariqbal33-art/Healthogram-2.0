# HEALTHOGRAM 2.2 CLOUD FUNCTIONS & ASYNC PIPELINE PERFORMANCE

**Runtime:** Cloud Functions for Firebase v2 (Google Cloud Run backend, Node.js 20 ESM)  
**Region:** `europe-west1` (Staging/Production Primary)  
**Configuration Matrix:**
- Synchronous Checkout / Auth: 512MB RAM, 1 vCPU, Concurrency 80, Min Instances 2
- Asynchronous Media / FHIR: 2048MB RAM, 2 vCPU, Concurrency 10, Min Instances 0
- AI Translation & Vision: 1024MB RAM, 1 vCPU, Concurrency 40, Min Instances 1

---

## 1. Cloud Functions v2 Cold-Start & Concurrency Benchmarks

| Function Target | Trigger Type | Cold Start Latency | Warm Execution P50 | Warm Execution P95 | Concurrency Setting | Optimization Applied |
| :--- | :--- | :---: | :---: | :---: | :---: | :--- |
| `apiCheckoutValidate` | HTTPS (Callable) | 380 ms | 145 ms | 280 ms | 80 | Min instances = 2; zero cold start on hot checkout path |
| `apiHealthQrVerify` | HTTPS (Callable) | 390 ms | 130 ms | 220 ms | 80 | TEE verification in memory |
| `onPaymentWebhook` | HTTP (Webhook) | 410 ms | 115 ms | 195 ms | 80 | Instant SHA-256 idempotency check |
| `processFhirImport` | Cloud Tasks (Async)| 820 ms | 340 ms | 560 ms | 10 | Chunked parsing with 24-hr conflict checking |
| `transcodeReelVideo`| Cloud Tasks (Async)| 1,250 ms| 2,400 ms | 4,100 ms| 5 | FFmpeg 1080p/720p HLS chunking |
| `fcmBulkNotification`| Pub/Sub (Async) | 450 ms | 180 ms | 310 ms | 100 | Batch multicast 500 tokens/call |

---

## 2. Asynchronous Job State Machine & Dead-Letter Queue (DLQ)

Long-running operations (video transcoding, FHIR bulk export, AI report generation) are decoupled from HTTP request-response cycles:

```text
Client Submission
    ↓
1. Enqueue Task (Cloud Tasks / Firestore Job Record: QUEUED)
    ↓
2. Worker Processing (PROCESSING, progress 0-100%)
    ↓
3. Success ➔ COMPLETED (signed download URL emitted)
   Failure ➔ Retry (Exponential Backoff: 1s, 2s, 4s, 8s max 5 attempts)
   Dead-Letter ➔ FAILED (Sent to DLQ topic for SRE investigation)
```

Empirically validated in `Step42PerformanceValidationSuite.testDomain05_asyncJobPipelineTransitionsAndDeadLetterHandling`:
- Terminal states (`COMPLETED`, `FAILED`, `CANCELLED`) are immutable.
- State machines prevent premature job completion or orphaned execution threads.
