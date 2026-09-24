# HEALTHOGRAM — RELEASE MANAGEMENT & GOOGLE PLAY STRATEGY

**Classification:** Release Engineering & Google Play Operations  
**Framework:** Semantic Versioning + Staged Rollout Management  

---

## 1. Versioning Architecture

Healthogram follows Semantic Versioning (`MAJOR.MINOR.PATCH`):
- **MAJOR (e.g., 2.0.0):** Revolutionary platform paradigm shifts, major architectural redesigns, breaking protocol upgrades.
- **MINOR (e.g., 1.1.0):** Backward-compatible new features, new product modules, major UI enhancements, new country activations.
- **PATCH (e.g., 1.0.1, 1.0.2):** Backward-compatible defect fixes, security patches, performance tuning.

*Version Code Rule:* Every uploaded artifact MUST increment `versionCode` by at least +1:
- `v1.0.0` = `versionCode 1`
- `v1.0.1` = `versionCode 2`
- `v1.1.0` = `versionCode 10`

---

## 2. Git Branching & Promotion Pipeline

```text
feature/* ──┐
bugfix/*  ──┼──> develop ──> release/1.1.0 ──> main (v1.1.0 Tag)
hotfix/*  ───────────────────────────────────> main (v1.0.1 Tag)
```

1. **`main`:** Contains strictly production-ready code matching the current live Google Play build.
2. **`develop`:** Integration branch where completed and reviewed feature branches merge.
3. **`feature/<name>`:** Branch created for developing individual capabilities (e.g., `feature/seller-coupons`).
4. **`release/<version>`:** Release candidate branch for final stabilization, documentation, and QA sign-off.
5. **`hotfix/<version>`:** Emergency branch branched directly from `main` to address critical P0/P1 production defects.

---

## 3. Staged Rollout Operational Schedule

For minor and major production releases, Healthogram applies a staged rollout across launch countries:

| Rollout Stage | Percentage of Users | Monitoring Window | Gate Verification |
| :--- | :--- | :--- | :--- |
| **Stage 1 (Internal)** | 100 Internal Employees | 24 Hours | Zero crashes, manual smoke tests pass 100%. |
| **Stage 2 (Closed Test)** | 500 Trusted Beta Users | 48 Hours | Crash-free sessions > 99.8%, no P0/P1 reports. |
| **Stage 3 (Staged 5%)** | 5% Production Users | 24 Hours | Crashlytics stable, ANRs < 0.1%, server latency normal. |
| **Stage 4 (Staged 20%)** | 20% Production Users | 48 Hours | Payment conversion steady, no webhook queue lag. |
| **Stage 5 (Staged 50%)** | 50% Production Users | 48 Hours | Provider verification steady, support volume normal. |
| **Stage 6 (Full 100%)** | 100% Global Rollout | Ongoing | Full production operations active. |

*Halt Condition:* If at any stage crash-free sessions fall below 99.5% or a critical payment/Health Passport anomaly occurs, the Release Manager immediately clicks **Halt Rollout** in Google Play Console.
