# ADR-020: MODULAR DOMAIN BOUNDARIES VS. MICROSERVICE DEPLOYMENT READINESS

**Status:** ACCEPTED  
**Date:** 2026-09-17  
**Deciders:** Principal Software Architect, Platform Lead, DevOps/SRE Lead  

---

## 1. Context
Healthogram encompasses 23 logical domains. External architectural proposals frequently advocate immediately fracturing systems into dozens of independent microservices running on Kubernetes with gRPC service meshes.

## 2. Problem
Is Healthogram 2.0 best served by an immediate microservice split, or does a Modular Domain Architecture provide superior velocity, lower latency, and higher security at our target scale?

## 3. Options Considered
- **Option A: Immediate Microservice Fleet:** Break the application into 23 separate deployable services with independent databases, Kubernetes clusters, and API gateways.
- **Option B: Modular Domain Architecture on Managed Cloud Serverless Primitives (Selected):** Maintain strictly defined domain packages and service boundaries in Kotlin and Cloud Functions v2. Enforce clean separation of models, repositories, validators, and events. Extract an independent microservice ONLY when measurable performance or compliance isolation justifies it.

## 4. Decision
Adopt **Option B: Modular Domain Architecture on Managed Cloud Serverless Primitives**. Scale hot paths (video transcoding, search, event queues), not everything.

## 5. Reason
Microservice sprawl introduces severe distributed systems failure modes (split-brain states, network latency hops, complex distributed transactions, multi-cluster management overhead) that degrade reliability. Modular domain architecture achieves identical boundary isolation with 10x lower operational complexity and near-zero idle compute cost.

## 6. Tradeoffs & Consequences
- Rigorous linting and code architecture tests are required to enforce package boundaries and prevent circular imports.
- Any future extraction of a hot-path service (e.g., dedicated media transcoding workers) is straightforward because domain boundaries are already cleanly encapsulated.
