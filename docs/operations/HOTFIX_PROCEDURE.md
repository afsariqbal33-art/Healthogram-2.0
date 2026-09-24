# HEALTHOGRAM — PRODUCTION HOTFIX PROCEDURE

**Target Version:** 1.0.0 (Production)  
**Classification:** DevOps & Release Management  

---

## 1. Hotfix Qualification Criteria

A hotfix must ONLY be created to address:
- **P0 / SEV-0:** Data corruption, security vulnerability, PHI exposure, authentication lockout.
- **P1 / SEV-1:** Critical crash loop (> 1% crash rate), broken payment funnel, blocked healthcare provider verification.

Cosmetic improvements, minor copy changes, and non-blocking feature requests MUST wait for the next scheduled sprint release (e.g., `v1.1.0`).

---

## 2. Step-by-Step Hotfix Workflow

### Step 1: Branch Creation
Always branch from the production release tag:
```bash
git checkout -b hotfix/v1.0.1 v1.0.0
```

### Step 2: Version Bump in `app/build.gradle.kts`
Modify the version configuration:
```kotlin
defaultConfig {
    applicationId = "com.aistudio.healthogram.hkqvpm"
    minSdk = 24
    targetSdk = 36
    versionCode = 2        // Increment by 1
    versionName = "1.0.1"  // Patch version bump
}
```

### Step 3: Implement Minimal Surgical Fix
- Only touch lines of code strictly required to resolve the issue.
- Never bundle unrelated refactors, dependency version bumps, or feature changes in a hotfix.

### Step 4: Write Regression Test
Add a targeted Robolectric or unit test in `app/src/test/` asserting that the defect is permanently resolved.

### Step 5: Test Execution & Clean Build
```bash
# Run unit and regression tests
gradle :app:testDebugUnitTest

# Compile production App Bundle
gradle :app:bundleRelease
```

### Step 6: Code Review & Sign-Off
- Requires minimum 2 approvals: Lead Architect + Security Engineer.
- Reviewer checks: No PHI leakage, no broad permissions added, no hardcoded secrets, no regression in test suite.

### Step 7: Commit, Tag, and Merge
```bash
git add .
git commit -m "fix(hotfix): resolve critical production crash in [module] (v1.0.1)"
git tag -a v1.0.1 -m "Healthogram Production Hotfix v1.0.1"
git checkout main
git merge hotfix/v1.0.1
git push origin main --tags
```

### Step 8: Play Console Expedited Deployment
1. Upload `app-release.aab` to Play Console under **Production**.
2. Note release notes: "Fixes an issue affecting app stability on certain devices."
3. Request **Expedited Review** via Play Console Developer Support citing SEV-1 crash mitigation.
