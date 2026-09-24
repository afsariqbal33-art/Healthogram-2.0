# Healthogram 2.2 — Technical Debt Catalog & Remediation Strategy

**Document:** `docs/engineering/HEALTHOGRAM_2_2_TECHNICAL_DEBT.md`  
**Evaluation Baseline:** `TECHNICAL_DEBT_2_1.md` & Step 35 Post-Launch Audit  
**Target Release:** Healthogram 2.2.0 (Build 20200)  
**Authority:** Principal Software Architect, Engineering Director & QA Lead  
**Classification:** ENGINEERING GOVERNANCE SPECIFICATION  

---

## 1. Technical Debt Governance Policy

To guarantee long-term stability and maintain a sub-0.05% crash rate:
- **Mandatory 20% Capacity Allocation**: Every two-week development sprint in the 2.2 lifecycle must dedicate exactly 20% of engineering bandwidth strictly to debt burndown and refactoring.
- **Zero Debt Ingress on Critical Path**: No new PR introducing unmanaged technical debt or undocumented hacks will be approved for merge.

---

## 2. Technical Debt Catalog (Prioritized for Version 2.2)

| Debt ID | Domain | Description & Root Cause | Severity | Effort | Risk If Unresolved | Remediation Strategy in 2.2 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **DEBT-01** | Mobile / WorkManager | Aggressive OEM battery managers killing periodic Health Connect sync on Xiaomi/Huawei devices. | **HIGH** | Medium (3d) | Biometric data gaps for users on affected Android devices. | Implement expedited WorkManager requests, foreground opportunistic sync, and OEM guide. |
| **DEBT-02** | Database / Firestore | Approaching Firestore index quota (168/200 composite indexes). | **HIGH** | Medium (3d) | Index exhaustion blocking deployment of new feature queries. | Consolidate overlapping marketplace and appointment composite indexes; reduce to 142. |
| **DEBT-03** | Backend / FHIR | Monolithic FHIR JSON serialization in memory causing Cloud Function memory spikes (> 512MB) on 500+ record histories. | **MEDIUM** | Medium (4d) | Cloud Function OOM errors during large medical history exports. | Implement NDJSON chunked streaming directly to Cloud Storage buckets. |
| **DEBT-04** | Mobile / UI | Health Passport timeline recompositions triggering redraws of offscreen items. | **MEDIUM** | Small (2d) | UI stutter (dropped frames) on budget devices during rapid scrolling. | Wrap timeline items in `remember` blocks and apply `@Immutable` annotations to data models. |
| **DEBT-05** | Security / IAM | Session eviction relies on client trigger during sign-in rather than automated scheduled cloud sweep. | **MEDIUM** | Small (2d) | Inactive zombie sessions lingering in database for > 90 days. | Deploy daily scheduled Cloud Function (`purgeZombieSessions`) to clean up inactive tokens. |
| **DEBT-06** | Mobile / Network | Hardcoded HTTP timeout values across heterogeneous network environments (3G vs Wi-Fi). | **LOW** | Small (1d) | Unnecessary connection timeouts in rural clinic areas. | Implement dynamic timeout configuration based on active network transport quality. |
| **DEBT-07** | DevOps / CI | Robolectric unit tests take > 4 minutes due to non-parallelized execution in GitHub Actions runner. | **LOW** | Small (1d) | Developer friction and slower PR verification turnaround. | Configure Gradle test worker forks (`maxParallelForks = 4`) in CI pipeline. |
| **DEBT-08** | i18n / Arabic | Minor pluralization edge cases in Arabic string resources for numbers between 11 and 99. | **LOW** | Small (1d) | Grammatical imperfections in Arabic order quantity displays. | Refine Android `<plurals>` resources using standard CLDR Arabic pluralization rules. |

---

## 3. Sprint Allocation & Burndown Schedule

```
  Sprint 1 (Weeks 1-2):
  ├── DEBT-01: Health Connect WorkManager OEM Resiliency (Lead: Android Lead)
  └── DEBT-02: Firestore Composite Index Consolidation (Lead: Database Architect)

  Sprint 2 (Weeks 3-4):
  ├── DEBT-03: Chunked NDJSON FHIR Streaming Pipeline (Lead: Backend Lead)
  └── DEBT-05: Scheduled Daily Zombie Session Eviction (Lead: Security Lead)

  Sprint 3 (Weeks 5-6):
  ├── DEBT-04: Compose Timeline Canvas & Recomposition Optimization (Lead: Mobile UI Lead)
  ├── DEBT-06: Dynamic Network Connection Timeouts (Lead: Android Core)
  ├── DEBT-07: CI/CD Parallel Test Execution Optimization (Lead: DevOps)
  └── DEBT-08: Arabic Pluralization Localization Refinement (Lead: i18n Lead)
```

By the completion of Sprint 3, all 8 cataloged debt items from Healthogram 2.1 will be fully resolved and verified.
