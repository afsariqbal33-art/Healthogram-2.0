# ADR-013: ASYNCHRONOUS JOB QUEUEING WITH CLOUD TASKS & DEAD-LETTER RECOVERY

**Status:** ACCEPTED  
**Date:** 2026-09-17  
**Deciders:** DevOps/SRE Lead, Backend Systems Architect  

---

## 1. Context
Resource-intensive or rate-limited operations (video transcoding, push notifications, AI generation, translation, analytics aggregation, search indexing) must not block synchronous HTTP requests.

## 2. Problem
How should asynchronous jobs be queued, dispatched, throttled, and retried to prevent system brownouts and handle third-party service degradation gracefully?

## 3. Options Considered
- **Option A: Unbounded Node.js Event Loop / Background Promises:** Spawn background promises inside Cloud Functions without awaiting.
- **Option B: Self-Hosted RabbitMQ / Celery Cluster:** Maintain dedicated compute instances for message queue workers.
- **Option C: Google Cloud Tasks with Configurable Rate Limits & Dead-Letter Storage (Selected):** Dispatch tasks to specialized Cloud Tasks queues with explicit concurrency limits, token-bucket dispatch rates, exponential backoff with jitter, and dead-letter persistence in `failed_jobs`.

## 4. Decision
Adopt **Option C: Google Cloud Tasks with Configurable Rate Limits & Dead-Letter Storage**. Implement 16 domain queues with strict retry policies.

## 5. Reason
Eliminates background execution drops caused by serverless function lifecycle termination. Provides fine-grained rate-limiting to protect downstream APIs.

## 6. Tradeoffs & Consequences
- All tasks must be stateless and idempotent.
- Operators can inspect and re-trigger jobs via the Admin Dead-Letter Console.
