# ADR-011: HYBRID SOCIAL FEED ARCHITECTURE & HIGH-FOLLOWER FAN-OUT

**Status:** ACCEPTED  
**Date:** 2026-09-17  
**Deciders:** Principal Architect, Social Media Lead, Database Architect  

---

## 1. Context
Healthogram features rich social feeds, video reels, and stories. Creators and verified healthcare organizations can accumulate hundreds of thousands or millions of followers. Writing every new post into every follower's personal feed collection (fan-out on write) causes severe write latency, Firestore write contention, and massive database costs.

## 2. Problem
How do we support instant feed delivery for both regular users (< 1,000 followers) and high-volume creators (> 50,000 followers) without write hotspot degradation or query bottlenecks?

## 3. Options Considered
- **Option A: Pure Fan-Out on Write:** Write post references into every follower's inbox collection at publish time.
- **Option B: Pure Fan-Out on Read:** Never write to followers; on feed fetch, query recent posts of every user the account follows and merge client-side or server-side.
- **Option C: Hybrid Fan-Out Architecture (Selected):** Regular creators use asynchronous fan-out on write into follower timelines. High-volume creators (> 25,000 followers) bypass write fan-out; their posts are indexed in a primary creator feed pool and merged with the user's personal timeline at read time using cursor pagination and Redis/MemoryStore caching.

## 4. Decision
Adopt **Option C: Hybrid Fan-Out Architecture**. Decouple feed generation into `FeedService` with configurable thresholding and cursor-based pagination.

## 5. Reason
Eliminates write spikes when viral creators publish content while keeping feed read latency under 150ms for normal timelines.

## 6. Tradeoffs & Consequences
- Read queries require a two-source merge (user inbox + high-follower creator posts).
- Health Passport clinical records remain strictly barred from feed ranking algorithms.
