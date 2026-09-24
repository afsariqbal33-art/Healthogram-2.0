# HEALTHOGRAM GLOBAL REGION & MULTI-REGION ARCHITECTURE STRATEGY

**Document Version:** 1.0.0-PROD  
**Status:** APPROVED BY PRINCIPAL ARCHITECT & GLOBAL CLOUD ARCHITECT  
**Date:** 2026-09-17  

---

## 1. Context & Business Constraints

Healthogram operates across healthcare, social media, fintech, and logistics. Expanding from its initial launch base (Oman & GCC) toward global availability requires a region strategy that balances:
1. **Clinical Data Sovereignty & Regulations:** (e.g. Oman MOH, Saudi PDPL, UAE Health Data Law, GDPR, HIPAA).
2. **End-to-End Latency:** < 150ms p95 for social feed, < 200ms p95 for checkout, < 80ms for RTC signaling.
3. **Operational Cost & Complexity:** Avoiding premature multi-master database federation where single-region or multi-region managed Firestore suffices.

---

## 2. Evaluation of Architectural Options

### Option A: Pure Single-Region Architecture
- *Description:* Deploy all services and databases in a single GCP region (e.g., `me-central1` Doha or `me-west1` Tel Aviv).
- *Evaluation:* Lowest complexity, but introduces single-region blast radius and cross-continental latency for international users.

### Option B: Multi-Region Cloud Firestore (Global Read / Quorum Write)
- *Description:* Utilize Firestore multi-region locations (e.g., `eur3` or `nam5`) providing cross-zone and cross-region high availability with automatic failover.
- *Evaluation:* Excellent for social media, creator feeds, and marketplace catalogs. However, clinical data cannot be replicated outside national borders in regulated jurisdictions.

### Option C: Multi-Region Application Compute + Local Data Stores
- *Description:* Cloud Functions v2 and Cloud Run services deployed in multiple edge regions (`me-central1`, `europe-west1`, `asia-south1`), routing to local database instances.
- *Evaluation:* Minimizes network transport latency, but requires complex distributed routing.

### Option D: Country/Region-Specific Data Domains (SELECTED ARCHITECTURE)
- *Description:* **Hybrid Domain Sovereignty Architecture**:
  - **Clinical Domain (Health Passport, Lab Reports, Doctor Grantees):** Stored in regional/country-specific Firestore databases (e.g., GCC Sovereign Vault in `me-central1`). Never replicated internationally.
  - **Global Public Domain (Social Feeds, Creator Reels, Marketplace Catalog, Public Profiles):** Stored in Multi-Region Firestore with Global Cloud CDN edge caching.
  - **Financial Domain:** Multi-region ACID ledger with strict regional tax and banking connectors.
- *Evaluation:* **OPTIMAL**. Fully compliant with strict healthcare privacy statutes while allowing social and commerce content to scale globally with minimal latency.

### Option E: Full Regional Isolation (Silo per Country)
- *Description:* Completely independent app stack per country.
- *Evaluation:* Impairs global creator following and cross-border discovery. Rejected for social/commerce, adopted only for sovereign health data.

---

## 3. Decision & Phased Implementation Plan

| Phase | Target Region | Workloads Deployed | Residency Compliance |
|---|---|---|---|
| **Phase 1: Home Base (Active)** | GCC (`me-central1` / `europe-west1`) | Full Platform Stack (Clinical, Social, Commerce, FinTech) | Oman MOH / GCC PDPL Compliant |
| **Phase 2: Regional Expansion** | MENA & South Asia (`asia-south1`) | Edge Compute + Regional Media Caching + Local Clinical Vault | Saudi PDPL / India DPDPA Compliant |
| **Phase 3: Global Expansion** | Europe (`europe-west1`) & Americas (`us-central1`) | Sovereign Clinical Nodes + Global Edge Social Mesh | GDPR / HIPAA Compliant |
