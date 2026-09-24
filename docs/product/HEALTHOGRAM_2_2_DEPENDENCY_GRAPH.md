# Healthogram 2.2 — Product Feature & Milestone Dependency Graph

**Document:** `docs/product/HEALTHOGRAM_2_2_DEPENDENCY_GRAPH.md`  
**Scope:** Product Strategy & Milestone Execution  
**Authority:** Product Strategy Lead & Principal Software Architect  
**Classification:** PRODUCT SPECIFICATION  

---

## 1. Feature Milestone Dependency Chain

```
[Phase 1: Architecture & Technical Debt]
  • Resolve Health Connect OEM battery kills (DEBT-01)
  • Consolidate marketplace Firestore indexes (DEBT-02)
         │
         ▼
[Phase 2: Security & Data Governance Hardening]
  • Periodic automated session eviction sweeps
  • GDPR Art 17 hard-deletion scheduler verification
         │
         ▼
[Phase 3: Core Healthcare Enhancements]
  • Health Passport 2.2 Longitudinal Trend Charts (Compose Canvas)
  • Bi-directional FHIR ServiceRequest order fulfillment
  • Chunked streaming for large patient FHIR history exports
         │
         ▼
[Phase 4: Platform & Usability Refinement]
  • Appointment calendar sync (.ics / Google Calendar)
  • Customer multi-address book for domestic delivery
  • On-device ML Kit emergency translation fallback
         │
         ▼
[Phase 5: Validation, Staging & Controlled Rollout]
  • Partner sandbox integration validation
  • Performance budget verification (P95 < 50ms)
  • Remote Config canary release (1% -> 5% -> 25% -> 100%)
```

---

## 2. Cross-Functional Dependencies & Prerequisites

| Planned Capability | Prerequisite Infrastructure | Critical Path Blocker | Primary Owner |
| :--- | :--- | :--- | :--- |
| **Longitudinal Vitals Trends** | Health Passport 2.1 schema & local Room cache | None (Client-side Canvas work) | Android Lead |
| **Lab ServiceRequest Orders** | Level 6 certified laboratory partner sandbox | Third-party EHR mTLS handshake | Interoperability Lead |
| **Chunked FHIR Streaming** | Cloud Storage NDJSON streaming adapter | None (Cloud Functions work) | Backend Lead |
| **Appointment Calendar Sync** | ICS specification & deep link URI scheme | None (Client-side Intent work) | Mobile Lead |
| **Customer Address Book** | Firestore user sub-collection schema update | None | Full-Stack Lead |
| **Offline Translation Fallback** | ML Kit Translation on-device models | Model size vs APK size budget | i18n Lead |
| **International Marketplace** | Cross-border customs, VAT, & bilateral treaties | **BLOCKED**: Held OFF in v2.2 | Legal & FinOps Lead |
