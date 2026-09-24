# HEALTHOGRAM — VERSION 1.1.0 MASTER QA & TEST PLAN

**Milestone:** v1.1.0  
**Target:** Staging & Release Candidate Verification  
**QA Lead:** Quality Assurance & Automated Testing Lead  

---

## 1. Test Scope & Matrix

| Test Domain | Target Verifications | Automation Method | Pass Criteria |
| :--- | :--- | :--- | :--- |
| **Biometric Auth for QR** | Android 16 BiometricPrompt enrollment, prompt cancel, prompt success, fallback to device PIN. | Robolectric + Hardware Lab Device | Zero bypass, cryptographic token generated only on `BIOMETRIC_SUCCESS`. |
| **Marketplace Coupons** | Valid coupon, expired coupon, minimum cart threshold, coupon stacking attempt, negative cart total attempt. | Unit + Cloud Functions Integration | Zero negative total, double-entry ledger balances to $0.00. |
| **Creator Analytics v2** | Aggregated calculations, zero UID leaks, empty state handling for new creators. | Unit Tests (`AnalyticsEngineTest`) | Aggregated values verified, zero PHI contamination. |
| **Tablet Multi-Pane** | Resizable layout, folding postures, split-screen mode, memory leak on orientation change. | Roborazzi Screenshot + JVM Robolectric | Zero UI clipping, responsive layout transitions smoothly. |
| **Regression Suite** | Full execution of all 150+ existing unit, acceptance, security, and smoke tests. | `gradle :app:testDebugUnitTest` | 100% Green, 0 Failures, 0 Regressions. |
| **Continuous Smoke Suite**| Execution of `ProductionSmokeTestSuiteTest`. | `gradle :app:testDebugUnitTest --tests com.example.healthogram.qa.ProductionSmokeTestSuiteTest` | 100% Green. |

---

## 2. Release Acceptance Criteria

- [ ] Zero P0 (Blocker) or P1 (Critical) defects.
- [ ] Maximum allowable P2 issues: <= 2 (non-blocking, documented with mitigation).
- [ ] Crash-free sessions in staging >= 99.9%.
- [ ] Full sign-off by Security Lead and Platform Architect.
