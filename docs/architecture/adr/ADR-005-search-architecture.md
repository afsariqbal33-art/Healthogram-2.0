# ADR-005: DEDICATED SEARCH INDEXING & DISCOVERY ABSTRACTION

**Status:** ACCEPTED  
**Date:** 2026-09-17  
**Deciders:** Database Architect, Marketplace Lead, Principal Architect  

---

## 1. Context
As the marketplace catalog grows from hundreds of products to tens of thousands of items across multiple healthcare categories, user search requirements evolve to include multi-attribute filtering (price, category, ratings, seller location), typo tolerance, and phonetic matching.

## 2. Problem
Cloud Firestore is an ACID document database optimized for primary key lookups and shallow range scans; it does not natively support full-text search, tokenization, stemming, or relevance scoring. Attempting to force full-text search into Firestore leads to massive read costs and complex composite index bloat.

## 3. Options Considered
- **Option A: Emulated Full-Text Search in Firestore:** Generate prefix token arrays (`search_tokens: ["vit", "vita", "vitamin"]`) on documents.
- **Option B: Self-Hosted Elasticsearch Cluster:** Deploy and maintain an open-source Elasticsearch/OpenSearch cluster on Google Cloud.
- **Option C: Managed Event-Driven Search Index via Adapter Pattern (Selected):** Synchronize Firestore catalog changes (`products/{productId}`) asynchronously to a managed search engine (such as Algolia or Google Cloud Search) via Cloud Functions. The Android client queries a dedicated search adapter endpoint.

## 4. Decision
Adopt **Option C: Managed Event-Driven Search Index via Adapter Pattern**. The client queries a decoupled search interface (`SearchProviderAdapter`), allowing seamless backend switching between Algolia, Cloud Search, or Elasticsearch.

## 5. Reason
Decoupling search through an adapter ensures the client application never hardcodes provider-specific SDKs or query languages. Search indexes remain eventually consistent, while primary transactional reads and stock reservations remain strictly on authoritative Firestore documents.

## 6. Tradeoffs
- Eventual consistency delay (typically 200ms - 1000ms) between product creation/update and search index availability.
- Additional SaaS subscription or infrastructure cost for the search provider.

## 7. Consequences
- Zero negative impact on transactional order processing or inventory reservation.
- Sub-50ms search response times with full typo tolerance and faceted filtering.
- Client applications interact with a unified search API.
