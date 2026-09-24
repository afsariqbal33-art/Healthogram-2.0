# ADR-2.2-005: Search Architecture & Domain Query Separation

**Status:** ACCEPTED  
**Date:** 2026-09-20  
**Context:**  
Healthogram requires search across multiple distinct domains: Health Passport records, verified doctors/clinics, marketplace products, and public social feeds. Integrating a heavy external search cluster (such as Elasticsearch or Algolia) would introduce substantial operational complexity, licensing costs, and data synchronization hazards.

**Decision:**  
We implement a domain-separated, tiered search architecture:
1. **Health Passport Clinical Search**: Executed **entirely on-device** using SQLite FTS (Full-Text Search) within the encrypted Room database. Health records are never indexed in public cloud search clusters.
2. **Doctor & Clinic Search**: Utilizes Firestore composite indexes querying structured attributes (`specialty`, `countryCode`, `verificationStatus`) with client-side geohash distance filtering.
3. **Marketplace Catalog Search**: Uses Firestore prefix queries (`titleSearchTokens`) with consolidated composite indexes (`category + status + createdAt`), deferring minor multi-facet sorting to client-side memory for paginated batches.
4. **Social Post Search**: Indexed by hashtag arrays and author usernames using standard Firestore array-contains queries.

**Consequences:**  
- **Positive**: Zero clinical data exposure; zero external search cluster hosting costs; sub-30ms local query latency for patient records.
- **Negative**: Marketplace text search lacks complex fuzzy typo tolerance, requiring clean product title tokenization.
