# HEALTHOGRAM — VERSION 1.1.0 RELEASE READINESS CHECKLIST

**Milestone:** v1.1.0  
**Target:** Google Play Staged Rollout Gate  

---

## 1. Pre-Flight Verification Checklist

### A. Code & Build Verification
- [ ] Version code bumped to `10` and `versionName` set to `"1.1.0"` in `app/build.gradle.kts`.
- [ ] Target SDK verified at `36` (Android 16).
- [ ] Dependency catalog (`libs.versions.toml`) verified free of critical CVEs.
- [ ] Clean build successful: `gradle :app:bundleRelease` produces valid AAB.
- [ ] ProGuard / R8 mapping file archived securely for Crashlytics de-obfuscation.

### B. Security & Compliance
- [ ] Security Review (`docs/releases/v1.1.0/SECURITY_REVIEW.md`) signed off.
- [ ] Health Passport medical records air-gapped from all new feature analytics.
- [ ] Play Integrity / App Check tokens validated against staging endpoints.
- [ ] Public Privacy Policy and Data Safety declaration reviewed for any scope changes.

### C. Quality Assurance Sign-Off
- [ ] Automated regression tests passing: `gradle :app:testDebugUnitTest`.
- [ ] Continuous production smoke tests passing: `ProductionSmokeTestSuiteTest`.
- [ ] Adaptive UI verified across phone, tablet, and foldable display classes.
- [ ] Escrow double-entry ledger verified for all coupon and discount calculations.

### D. Operations & Deployment Strategy
- [ ] Firebase Remote Config default templates uploaded and verified.
- [ ] Rollout plan configured: 5% -> 20% -> 50% -> 100%.
- [ ] Emergency kill switches confirmed responsive (`emergency_disable_marketplace`, etc.).
- [ ] On-call SRE rotation assigned for 48 hours post-deployment.
