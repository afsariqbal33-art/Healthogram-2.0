# ADR-015: PLUGGABLE SEARCH PROVIDER ABSTRACTION & INDEXING ENGINE

**Status:** ACCEPTED  
**Date:** 2026-09-17  
**Deciders:** Database Architect, Senior Backend Engineer  

---

## 1. Context
Search is critical across users, verified doctors, clinics, hospitals, laboratories, marketplace products, and hashtags. Direct Firestore queries cannot support fuzzy matching, stemming, or composite faceted sorting without index explosion.

## 2. Problem
How do we support rich, typo-tolerant search across millions of records without locking the system into a single search vendor or prematurely provisioning expensive search clusters?

## 3. Options Considered
- **Option A: Emulated Firestore In-Memory Search:** Fetch large result sets and filter in application memory.
- **Option B: Mandatory External SaaS Lock-in:** Directly embed vendor SDKs (e.g., Algolia) across all client components.
- **Option C: Decoupled SearchProviderAdapter Architecture (Selected):** Implement an abstract `SearchProviderAdapter` interface supporting indexing, updating, deleting, and searching. In v2.0, start with an optimized Firestore indexer adapter, with immediate pluggability for OpenSearch, Algolia, or Google Cloud Search.

## 4. Decision
Adopt **Option C: Decoupled SearchProviderAdapter Architecture**. Search indexing occurs asynchronously via domain events (`ProductSearchIndexRequested`, `ProfileSearchIndexRequested`).

## 5. Reason
Prevents client vendor lock-in. Guarantees that search index degradation never blocks transactional updates. Ensures Health Passport medical records NEVER enter search indexes.

## 6. Tradeoffs & Consequences
- Search indexes are eventually consistent (typically 200–500ms delay).
- Authoritative transactional verification remains strictly on primary Firestore documents.
