# ADR-012: DURABLE DOMAIN EVENT ARCHITECTURE & IDEMPOTENCY ENGINE

**Status:** ACCEPTED  
**Date:** 2026-09-17  
**Deciders:** Distributed Systems Architect, Senior Backend Engineer  

---

## 1. Context
Direct synchronous coupling between domains (e.g., payment completion directly invoking inventory decrement, email sending, search reindexing, and push notifications) creates brittle failure chains and cascading latency.

## 2. Problem
How do we decouple domain communications while guaranteeing event delivery, strict causal ordering where needed, and absolute protection against duplicate processing?

## 3. Options Considered
- **Option A: Synchronous In-Process Invocations:** Direct function calls across modules.
- **Option B: Heavy Message Broker (Apache Kafka):** Deploy Kafka clusters on Compute Engine.
- **Option C: Serverless Domain Event Engine with Durable Idempotency (Selected):** Formal `DomainEvent` envelope with unique `event_id`, `causation_id`, and `idempotency_key`. Events are written to transactional outbox collections and processed with `processed_events/{eventId}` verification.

## 4. Decision
Adopt **Option C: Serverless Domain Event Engine with Durable Idempotency**. All event consumers must verify idempotency before executing state mutations.

## 5. Reason
Guarantees at-least-once processing without duplicate side-effects (duplicate payouts, duplicate notifications, double inventory release).

## 6. Tradeoffs & Consequences
- Requires storage overhead for idempotency tracking documents.
- Events must NEVER contain sensitive Health Passport clinical diagnoses or unencrypted PHI in payloads.
