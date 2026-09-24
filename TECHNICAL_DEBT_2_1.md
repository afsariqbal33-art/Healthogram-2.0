# Healthogram 2.1 — Technical Debt Backlog & Engineering Remediation Register

**Document:** `TECHNICAL_DEBT_2_1.md`  
**System Version:** Healthogram 2.1.0 (Build 20100)  
**Governance:** Architecture Review Board (ARB) & Engineering Leads  
**Classification:** Critical, High, Medium, Low  
**Status:** ACTIVE REGISTER  

---

## 1. Technical Debt Inventory

| Item ID | Classification | Priority | Component / Module | Description | Effort | Dependency | Target Release | Owner |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **DEBT-01** | **High** | P1 | `HealthConnectService` | Background battery optimization kills WorkManager job on aggressive OEM Android builds (Xiaomi/Huawei). | Medium (3d) | None | v2.2.0 | Mobile Lead |
| **DEBT-02** | **High** | P1 | `Firestore Indexes` | Composite index count for multi-field marketplace filtering approaching 180 of 200 limit. | Medium (4d) | None | v2.2.0 | Database Lead |
| **DEBT-03** | **Medium** | P2 | `FHIRMappingService` | Manual JSON serialization for custom hospital extensions instead of code-generated Kotlinx models. | Large (5d) | FHIR R4 Spec | v2.2.0 | Interop Lead |
| **DEBT-04** | **Medium** | P2 | `TranslationService` | Lack of on-device offline translation fallback for critical clinical terminology when connectivity drops. | Large (6d) | ML Kit Translation | v2.2.0 | i18n Lead |
| **DEBT-05** | **Medium** | P2 | `Cloud Functions` | Node.js CommonJS legacy files mixed with ES Modules in `functions/src/`. | Medium (2d) | Node 20 LTS | v2.2.0 | Backend Lead |
| **DEBT-06** | **Low** | P3 | `Room Migrations` | Legacy manual SQL migration scripts in `001_initial_schema` could utilize Room AutoMigration. | Small (1d) | Room 2.6+ | v2.3.0 | Android Lead |
| **DEBT-07** | **Low** | P3 | `Strings & Localization`| Some Arabic strings in niche appointment cancellation dialogues lack gender-neutral variants. | Small (1d) | Arabic Copy Review | v2.1.1 | QA Lead |
| **DEBT-08** | **Low** | P3 | `Test Suites` | Redundant mock test fixtures across `Step32` and `Step33` can be consolidated into shared test harness. | Small (2d) | JUnit 5 | v2.2.0 | QA Lead |

---

## 2. Debt Invariant & Zero-Tolerance Policy

- **Critical Invariant:** Any debt item classified as **Critical (Security or Medical Data Integrity Risk)** must be triaged as a P0 blocker and remediated immediately within the current sprint before any new feature work proceeds. Currently, **0 Critical debt items exist**.
- **Sprint Debt Allocation:** Every development cycle commits 20% of engineering bandwidth strictly to debt burndown and infrastructure modernization.
