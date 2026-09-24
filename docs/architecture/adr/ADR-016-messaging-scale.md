# ADR-016: HIGH-SCALE MESSAGING, CURSOR PAGINATION & EPHEMERAL PRESENCE

**Status:** ACCEPTED  
**Date:** 2026-09-17  
**Deciders:** Communications Platform Architect, Distributed Systems Engineer  

---

## 1. Context
Direct 1-on-1 and group messaging generate high message volumes. Loading entire chat threads or writing typing indicators and presence heartbeats to Firestore creates severe document write contention and ballooning costs.

## 2. Problem
How do we scale chat threads to millions of messages while keeping presence indicators real-time, low latency, and cost-effective?

## 3. Options Considered
- **Option A: Pure Firestore Messaging & Presence:** Store all messages, unread counts, and presence heartbeats in Firestore documents.
- **Option B: Dedicated WebSocket Cluster:** Deploy customized socket server fleets on Compute Engine.
- **Option C: Hybrid Architecture (Firestore Messages + Realtime Presence) (Selected):** Messages and E2EE conversation metadata reside in Firestore using strict cursor pagination (last 30 messages per page). Ephemeral typing indicators, user online presence, and WebRTC signaling are routed through Firebase Realtime Database with automatic disconnection hooks.

## 4. Decision
Adopt **Option C: Hybrid Architecture**. Never write presence heartbeats to Firestore.

## 5. Reason
Eliminates high-frequency Firestore writes, keeping messaging costs near zero for presence while leveraging Firestore's robust security rules and offline persistence for actual conversation histories.

## 6. Tradeoffs & Consequences
- Client manages two connection lifecycles (Firestore listener for messages, Realtime listener for active typing/presence).
- Realtime presence listeners must be cleanly unregistered when screens are disposed.
