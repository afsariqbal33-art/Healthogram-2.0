# HEALTHOGRAM 2.2.0: PRODUCTION BUG & INCIDENT REGISTER

**Document ID:** HGM-STAB-BUG-REGISTER-2.2.0  
**Release Under Audit:** Healthogram Version `2.2.0` (`versionCode 20201`)  
**Standard Compliance:** ISO/IEC 25010, ITIL v4 Incident Management, HIPAA Security Rule § 164.308  
**Audit Date:** 2026-09-22T06:10:00Z  

---

## 1. Production Bug Intake Architecture & Taxonomy

Every defect discovered during post-launch operations is ingested via a standardized schema designed to capture healthcare compliance, financial risk, and platform stability attributes.

### Standardized Bug Schema
* **Bug ID:** Unique sequential identifier (`BUG-2.2.0-XXXX`)
* **Title:** Concise, descriptive summary of failure mode
* **Component:** Affected architectural subsystem (`HealthPassport`, `Auth`, `Marketplace`, `Payments`, `Ledger`, `Delivery`, `Social`, `Messaging`, `Calling`, `AIStudio`, `Translation`, `Notifications`, `Admin`)
* **Environment:** (`Production-Live`, `Staging-PreProd`, `Canary-Wave`)
* **Version / Code:** `2.2.0` / `20201`
* **Device / OS:** Target OEM model, Android API level (e.g., Pixel 8, Samsung S24, API 34/35/36)
* **Country:** Jurisdiction identifier (`US`, `CA`, `GB`, `SA`, `AE`, `EG`, `IN`)
* **Account Category:** Exactly 1 of the 5 authorized categories (`Individual`, `Doctor`, `Clinic`, `Hospital`, `Laboratory`) or Marketplace Actor (`Customer`, `Seller`)
* **Steps to Reproduce:** Exact deterministic sequence to recreate behavior
* **Expected Result:** Compliant functional behavior
* **Actual Result:** Observed anomalous behavior
* **Severity:** `P0` (Critical), `P1` (Major), `P2` (Significant), `P3` (Minor)
* **Frequency:** `Always (100%)`, `Intermittent (>20%)`, `Rare (<5%)`, `Edge-case (<1%)`
* **Evidence:** Telemetry links, Crashlytics trace ID, Play Vitals trace
* **Logs:** Sanitized log snippet (strictly zero PHI or unhashed secrets)
* **Screenshots:** Reference asset or null
* **Affected Users:** Quantified cohort or `DATA NOT AVAILABLE`
* **Security Impact:** Confidentiality / Integrity / Availability risk rating
* **Financial Impact:** Direct or indirect ledger skew, refund liability, or null
* **Health Data Impact:** PHI exposure risk, incorrect medical record association, or zero
* **Workaround:** Temporary operational or client mitigation
* **Root Cause:** Architectural, logic, concurrency, or network root origin
* **Fix:** Proposed code modification or configuration change
* **Test:** Deterministic automated test or manual verification procedure
* **Deployment:** Target patch release or Remote Config push
* **Verification:** Production validation outcome
* **Status:** `NEW` | `TRIAGED` | `IN_PROGRESS` | `BLOCKED` | `FIX_READY` | `QA` | `STAGING` | `PRODUCTION` | `VERIFIED` | `CLOSED` | `WONT_FIX`

---

## 2. Priority Definition & Severity SLA Matrix

Bug priority is not driven solely by complaint volume. Prioritization weights privacy, healthcare regulatory compliance, financial integrity, and user safety above general cosmetic or UX issues.

| Priority | Definition | Health / Security / Financial Criteria | Triage SLA | Resolution SLA | Escalation Target |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **P0** | **Critical Emergency** | Any unauthorized PHI access; Health Passport key leakage; Double-entry ledger imbalance; App-wide crash looping on launch; Play Store malware/policy flag. | < 15 mins | < 4 hours | CTO, Chief Security Officer, Release Manager |
| **P1** | **Major Functional Failure** | Core feature blocked without workaround; Checkout/payment flow blocked in an active country; Inability to complete 2FA login; Delivery OTP validation broken. | < 30 mins | < 24 hours | Principal Domain Engineer, QA Lead |
| **P2** | **Significant Defect with Workaround** | Subsystem degraded but functional; Non-critical UI glitch in RTL layout; Translation cache misses; Excessive Firestore read loops; Video call drop on network transition. | < 4 hours | < 72 hours | Senior Engineer, Sprint Backlog |
| **P3** | **Minor Defect / Cosmetic** | Minor typo in localized string; Asset alignment in secondary screen; Low-frequency animation stutter on legacy Android versions. | < 24 hours | Next Planned Release (2.2.1 / 2.3) | Assigned Developer |

---

## 3. Active Production Bug Register (Stage B Handover)

Below are the audited production issues recorded during initial Stage B rollout.

### Item 1: BUG-2.2.0-001 (RTL Mirroring Alignment in Medical Bill Viewer)
* **Title:** Medical Bill currency symbol overlapping amount in Arabic locale on Android 14
* **Component:** `HealthPassport` / `MedicalBills`
* **Environment:** `Production-Live`
* **Version:** `2.2.0` (`20201`)
* **Device / OS:** Samsung Galaxy S23, Android 14 (OneUI 6.0)
* **Country:** `SA` (Saudi Arabia), `AE` (United Arab Emirates)
* **Account Category:** `Individual`
* **Steps to Reproduce:**
  1. Set system language to Arabic (`ar-SA`).
  2. Open Health Passport → Medical Bills tab.
  3. Inspect bill amount containing currency symbol (SAR / AED).
* **Expected Result:** Currency symbol aligns cleanly to the right of the numerical value according to Arabic typography standards.
* **Actual Result:** Text overlaps by 4dp due to missing `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)` bounding constraint.
* **Severity:** `P2`
* **Frequency:** `Always (100% on Arabic locale)`
* **Affected Users:** `DATA NOT AVAILABLE` (Cohort: active Arabic users viewing bills)
* **Security Impact:** None.
* **Financial Impact:** Zero (Display only, underlying ledger balances unaltered).
* **Health Data Impact:** Zero.
* **Workaround:** Switch app locale to English in app settings or rotate to landscape mode.
* **Root Cause:** Jetpack Compose `Row` in `MedicalBillCard.kt` used fixed horizontal spacing without RTL directional awareness.
* **Fix:** Wrap currency text composable with directional layout padding and auto-mirrored typography layout.
* **Test:** Localized Robolectric screenshot test `MedicalBillRtlScreenshotTest.kt`.
* **Deployment:** Scheduled for patch `2.2.1`.
* **Verification:** `REQUIRES VALIDATION` (Verified in emulator sandbox).
* **Status:** `TRIAGED`

---

### Item 2: BUG-2.2.0-002 (Repeated Cloud Translation Invocation for Static Medical Precautions)
* **Title:** Redundant Cloud Translation API calls on repeatedly viewed prescription precautions
* **Component:** `Translation`
* **Environment:** `Production-Live`
* **Version:** `2.2.0` (`20201`)
* **Device / OS:** Various (Pixel 7/8, Xiaomi 13)
* **Country:** `EG`, `SA`, `IN`
* **Account Category:** `Individual`, `Doctor`
* **Steps to Reproduce:**
  1. Open a prescription containing standardized medical precaution instructions (e.g., "Take after meals").
  2. Request Arabic translation.
  3. Close screen and reopen the same prescription 5 minutes later.
* **Expected Result:** The translated text loads immediately from local Room encrypted cache without network egress.
* **Actual Result:** Cache key omitted the standardized medical term ID, causing fallback to remote Cloud Translation API on each view.
* **Severity:** `P2`
* **Frequency:** `Always`
* **Affected Users:** `DATA NOT AVAILABLE`
* **Security Impact:** None (Payload contains standardized clinical phrases, zero patient identifiers).
* **Financial Impact:** Zero direct user impact; induces unnecessary Google Cloud Translation API consumption costs.
* **Health Data Impact:** Zero.
* **Workaround:** Local memory cache holds string during single active session.
* **Root Cause:** Cache query checked only `sha256(text)` instead of composite key `(termId, targetLanguage, hash)`.
* **Fix:** Update `TranslationRepository.kt` to persist standardized medical glossary terms permanently in Room database.
* **Test:** Unit test `TranslationCacheTest.kt` verifying 0 network calls on repeat query.
* **Deployment:** Scheduled for patch `2.2.1`.
* **Verification:** `REQUIRES VALIDATION`
* **Status:** `IN_PROGRESS`

---

### Item 3: BUG-2.2.0-003 (Carrier SMS Latency for Phone OTP in Egypt)
* **Title:** Elevated SMS OTP delivery delays during peak carrier traffic in Egypt (Vodafone/Orange)
* **Component:** `Auth`
* **Environment:** `Production-Live`
* **Version:** `2.2.0` (`20201`)
* **Device / OS:** All devices
* **Country:** `EG`
* **Account Category:** All 5 categories
* **Steps to Reproduce:**
  1. Initiate phone verification with Egyptian MSISDN (+20 1X XXXXXXXX).
  2. Observe arrival time of SMS containing verification code.
* **Expected Result:** OTP received within < 30 seconds.
* **Actual Result:** Intermittent delays up to 120 seconds during peak local network hours (18:00 - 21:00 EET).
* **Severity:** `P2`
* **Frequency:** `Intermittent (>20% in EG evening window)`
* **Affected Users:** `DATA NOT AVAILABLE`
* **Security Impact:** None. Expired codes (>300s) are rejected correctly by Firebase Auth.
* **Financial Impact:** Zero.
* **Health Data Impact:** Zero.
* **Workaround:** Users can request code resend after 60s cooldown timer.
* **Root Cause:** Upstream regional carrier gateway routing congestion.
* **Fix:** Configure Firebase Auth Multi-Carrier fallback routing and introduce WhatsApp OTP verification fallback channel.
* **Test:** Automated gateway latency monitoring probe.
* **Deployment:** Provider-side configuration update in progress.
* **Verification:** `REQUIRES EXTERNAL PROVIDER`
* **Status:** `TRIAGED`

---

### Item 4: BUG-2.2.0-004 (Infinite Scroll Pagination Prefetch Threshold in Social Reels)
* **Title:** Reel player memory footprint climbs after scrolling through > 40 consecutive video reels
* **Component:** `Social` / `Reels`
* **Environment:** `Production-Live`
* **Version:** `2.2.0` (`20201`)
* **Device / OS:** Mid-tier Android devices (4GB RAM)
* **Country:** All active countries
* **Account Category:** `Individual`
* **Steps to Reproduce:**
  1. Open Social Reels feed.
  2. Continuously scroll through 45+ video items.
* **Expected Result:** ExoPlayer instance releases off-screen video buffers, capping RAM usage under 250MB.
* **Actual Result:** Cached ExoPlayer surface views retain decoded frames up to 10 items backward, causing GC thrashing on 4GB devices.
* **Severity:** `P3`
* **Frequency:** `Intermittent`
* **Affected Users:** `DATA NOT AVAILABLE`
* **Security Impact:** None.
* **Financial Impact:** Zero.
* **Health Data Impact:** Zero.
* **Workaround:** Exiting and re-entering the Reels feed resets the media pool.
* **Root Cause:** Media player pool max retain count was configured at 10 instead of 3.
* **Fix:** Clamp `EXOPLAYER_POOL_SIZE` to 3 active instances and call `player.clearMediaItems()` on item detaching from viewport.
* **Test:** Memory profile regression test in Android Studio profiler.
* **Deployment:** Scheduled for patch `2.2.1`.
* **Verification:** `REQUIRES VALIDATION`
* **Status:** `FIX_READY`

---

## 4. Production Incident Log (P0 / P1 Severity)

| Incident ID | Severity | Subsystem | Trigger Description | Detection | Containment Action | Root Cause | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **INC-2026-000** | N/A | None | No P0 or P1 incidents have occurred in Step 46 or Stage B. | Continuous SRE Alerts | Pre-configured emergency circuit breakers active | Clean operational state | `CLOSED` |

*Incident Management Policy:* If a P0 or P1 incident occurs, the **Health Passport Incident Protocol** (Section 6 of Step 47 directives) is immediately triggered, logging the exact forensic timeline without overwriting evidence.
