# Step 34 — Production Rollback Runbook for Healthogram 2.1

**Document:** `docs/operations/STEP_34_ROLLBACK_RUNBOOK.md`  
**Execution Lead:** Release Engineer, Site Reliability Engineer, Lead Architect  
**Version:** 2.1.0-RC1  
**Status:** ACTIVE VERIFIED RUNBOOK  

---

## 1. Rollback Taxonomy: Dynamic vs. Code Rollback

Healthogram 2.1 utilizes a dual-tier rollback architecture:
1. **Tier 1 (Instant Dynamic Rollback):** Executed via Firebase Remote Config and Server Kill Switches (< 60 seconds). Used for operational anomalies, partner downstream outages, or latency regressions.
2. **Tier 2 (Binary / Code Rollback):** Executed via Google Play Console track halt and deployment of hotfix APK/AAB (< 2 hours). Used for fundamental client-side crash loops, native compilation errors, or local Room schema incompatibilities.

---

## 2. Tier 1: Step-by-Step Remote Config Rollback Protocol

When an anomalous behavior is observed in a specific feature domain:

### Step 1: Freeze Staged Rollout
Immediately issue a Remote Config update to halt percentage expansion:
```bash
# Example via Firebase Admin CLI / API
firebase remoteconfig:rollback --version <previous_stable_version>
```
*Note: In Remote Config, rollback creates a new version containing the exact configuration of the target past version. Historical records are preserved.*

### Step 2: Target Feature Isolation & Kill Switch Activation
Set the specific feature flag to `enabled: false` and activate the server kill switch in `emergency_controls/current`:
- `disable_health_connect: true`
- `disable_fhir_import: true`
- `disable_fhir_export: true`
- `disable_healthcare_integrations: true`
- `disable_appointments: true`
- `disable_healthcare_ai: true`

### Step 3: Verify Client State & Fail-Safe Fallbacks
- Clients instantly fall back to the safe baseline:
  - If `health_connect` is disabled, the UI displays "Health Connect telemetry sync is temporarily under scheduled maintenance".
  - If `fhir_import` is disabled, bundle uploads are queued or rejected with clear retry guidance.
  - Zero crashes occur; UI remains fully responsive.

### Step 4: Verify Healthcare Data Integrity
Execute the database integrity script:
```bash
node scripts/verify_health_integrity.js --dry-run
```
Confirm zero uncommitted or orphaned FHIR records exist.

---

## 3. Tier 2: Google Play Production Code Rollback & Hotfix Procedure

If a bug resides within the compiled native code or Jetpack Compose UI:

1. **Halt Google Play Staged Rollout:**
   - Log into Google Play Console -> Release -> Production -> Edit release -> "Halt rollout".
   - This immediately restricts the affected release code from propagating to any further users.

2. **Prepare Hotfix Branch:**
   ```bash
   git checkout -b hotfix/2.1.1 release/2.1.0
   ```
   Apply the targeted fix. **Never use a feature flag to conceal a fundamental server or client authorization vulnerability.**

3. **Verify Regression Suite:**
   Run the full unit and integration test suite:
   ```bash
   gradle :app:testDebugUnitTest
   ```

4. **Bump Version Code & Build Clean Artifact:**
   - Increment `versionCode` (e.g., `20101`) and `versionName` (`2.1.1`).
   - Generate production AAB and upload to Play Console Closed Testing track first.

5. **Executive Approval Gate:**
   Release requires formal sign-off from Principal Software Architect and QA Lead before resuming rollout.
