# ADR-004: ASYNCHRONOUS DOMAIN EVENT BUS & CLOUD TASKS QUEUEING

**Status:** ACCEPTED  
**Date:** 2026-09-17  
**Deciders:** Distributed Systems Architect, DevOps/SRE Lead  

---

## 1. Context
Direct synchronous HTTP/RPC chains between subsystems (e.g., checkout triggering push notifications, inventory sync, analytics logging, and email receipts within a single request) introduce cascading latency, timeout risks, and tight coupling.

## 2. Problem
How do we decouple synchronous user-facing API responses from heavy asynchronous side-effects while guaranteeing at-least-once processing, retry backoff, and idempotency?

## 3. Options Considered
- **Option A: Synchronous Function Chaining:** Execute all side-effects sequentially inside the primary Cloud Function.
- **Option B: Self-Hosted Message Broker (RabbitMQ / Apache Kafka):** Deploy dedicated message broker clusters on Compute Engine or Kubernetes.
- **Option C: Google Cloud Tasks & Pub/Sub Eventarc (Selected):** Use Cloud Pub/Sub for broadcast domain events and Cloud Tasks for point-to-point worker dispatching with configurable rate limits, token buckets, and exponential backoff.

## 4. Decision
Adopt **Option C: Google Cloud Tasks & Pub/Sub Eventarc**. Synchronous endpoints emit versioned domain events to Pub/Sub and dispatch asynchronous work items to dedicated Cloud Tasks queues.

## 5. Reason
Cloud Tasks provides native serverless execution with fine-grained rate-limiting, deduplication keys, scheduled future execution, and dead-letter queue (DLQ) alerts without the cluster maintenance burden of Kafka or RabbitMQ.

## 6. Tradeoffs
- Side-effects are eventually consistent rather than immediately visible.
- Consumers must be designed to be strictly idempotent to handle at-least-once delivery.

## 7. Consequences
- User checkout latency drops from ~1200ms to < 200ms.
- Downstream failures (e.g., FCM push network glitch) do not abort or fail the primary financial transaction.
- Dead-letter queues alert the on-call SRE if an asynchronous task fails after 5 retries.
