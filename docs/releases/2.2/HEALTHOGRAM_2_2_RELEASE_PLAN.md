# Healthogram 2.2 — Production Release Plan & Staged Rollout Strategy

**Document:** `docs/releases/2.2/HEALTHOGRAM_2_2_RELEASE_PLAN.md`  
**Target Release:** Healthogram 2.2.0 (Build 20200)  
**Authority:** Release Manager, Principal Software Architect & QA Lead  
**Classification:** RELEASE GOVERNANCE SPECIFICATION  

---

## 1. Release Invariants & Guiding Principles

1. **Reversibility**: Every new feature deployed in Healthogram 2.2 must be controllable via Firebase Remote Config feature flags with sub-second remote disablement capabilities.
2. **Zero Inconvenience to Clinical Care**: Critical Health Passport, teleconsultation, and emergency ICE services must maintain 100% availability during all rollout phases.
3. **Automated Rollback Tripwires**: If telemetry exceeds predefined safety thresholds, the canary rollout is automatically halted and rolled back without requiring manual engineering intervention.

---

## 2. Release Gates (The 5 Mandatory Production Gates)

```
  [Gate 1: Code Freeze & Build] ──► [Gate 2: Security Sign-off] ──► [Gate 3: Interoperability]
                 │                                │                               │
                 ▼                                ▼                               ▼
  100% Tests Pass; No Comp Errors   Zero High/Crit CVEs; App Check    FHIR R4 Validation 100%;
  Lint Clean; Version Code Bumped   Generic Push Alerts Verified      Level 6 Partner Sandbox Pass
                                                                                  │
  ┌───────────────────────────────────────────────────────────────────────────────┘
  │
  ▼
  [Gate 4: Financial Ledger Integrity] ──► [Gate 5: Canary Soak & Full Production]
                 │                                        │
                 ▼                                        ▼
  Minor-Unit Arithmetic Verified;            24h Soak at 5% (Crash-Free > 99.9%);
  0.00 OMR Ledger Drift Reconciled           Promote to 25% -> 50% -> 100%
```

### Gate Criteria Checklist
- **Gate 1 (Build & Architecture)**:
  - All Gradle builds succeed with target SDK 36 (Android 16).
  - Robolectric test suite and unit tests report 100% pass rate.
  - Zero critical lint violations.
- **Gate 2 (Security & Privacy)**:
  - STRIDE threat model mitigations verified.
  - Google Play Integrity App Check enforced on 100% of mutating Cloud Functions.
  - Push notification sanitization verified (zero clinical disclosures).
- **Gate 3 (Interoperability & Clinical Safety)**:
  - HL7 FHIR R4 schema conformance validated against all 16 normative models.
  - Bi-directional `ServiceRequest` pipeline certified in partner sandbox.
  - Non-diagnostic AI disclaimers verified on all assistive screens.
- **Gate 4 (Financial & Operational Integrity)**:
  - Double-entry ledger reconciliation reports 0.00 minor unit variance.
  - Webhook signature verification verified for Stripe, Thawani, and PayTabs.
  - International marketplace flag confirmed disabled (`enabled = false`).
- **Gate 5 (Canary & Telemetry Soak)**:
  - 24-hour canary soak at 5% user cohort confirms crash-free users $\ge 99.90\%$ and ANR rate $\le 0.02\%$.

---

## 3. Staged Rollout Schedule & Canary Progression

```
  Rollout Stage Progression:
  
  [Phase 0: Staging Internal] ──► [Phase 1: Canary 1%] ──► [Phase 2: Canary 5%]
           (Day 0 - 100 Users)         (Day 1 - 4k Users)          (Day 2 - 20k Users)
                                                                            │
  ┌─────────────────────────────────────────────────────────────────────────┘
  │
  ▼
  [Phase 3: Expanded 25%] ────► [Phase 4: Broad 50%] ───► [Phase 5: Global 100%]
        (Day 4 - 100k Users)        (Day 6 - 200k Users)        (Day 8 - All 412k+ Users)
```

| Phase | Duration | Target Cohort | Key Metrics Monitored | Gate Exit Requirement |
| :--- | :--- | :--- | :--- | :--- |
| **Phase 0: Internal** | 24 Hours | Internal team & beta doctors (100 devices)| End-to-end smoke test; crash logs | Zero crashes; 100% booking success |
| **Phase 1: Canary 1%** | 24 Hours | 1% of random active Android users (~4,100) | Crashlytics, Firestore read rates, P95 latency| Crash-free $\ge 99.90\%$; P95 $\le 50\text{ms}$ |
| **Phase 2: Canary 5%** | 48 Hours | 5% of active users (~20,600 devices) | Health Connect sync success, DB connections | Error budget consumption $\le 5\%$ |
| **Phase 3: Canary 25%**| 48 Hours | 25% of active users (~103,000 devices) | Payment webhooks, ledger drift, FCM latency | Zero double-bookings; zero ledger drift |
| **Phase 4: Canary 50%**| 48 Hours | 50% of active users (~206,000 devices) | Cloud storage egress, TURN relay bandwidth | Spend trajectory within budget ceiling |
| **Phase 5: Full 100%** | Permanent | 100% of global active users (412,850+) | Global platform SLIs / SLOs | Stable production baseline established |

---

## 4. Automated Rollback Criteria & Circuit Breakers

The canary rollout is **immediately frozen and rolled back to version 2.1.0** if any of the following automated conditions are triggered:
1. **Crash-Free Degradation**: Crash-free user rate drops below **99.80%** over a rolling 1-hour window.
2. **ANR Spike**: Application Not Responding (ANR) rate exceeds **0.05%**.
3. **Latency Degradation**: Health Passport P95 read latency exceeds **150ms** for $> 15$ continuous minutes.
4. **Payment Discrepancy**: Any un-reconciled minor-unit variance detected in the financial ledger ($> 0.00$).
5. **Clinical Interoperability Failure**: FHIR import or export schema rejection rate exceeds **1.0%**.
6. **Security Alert**: Triggering of high-severity App Check token invalidation anomalies.
