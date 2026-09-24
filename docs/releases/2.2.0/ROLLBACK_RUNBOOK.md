# HEALTHOGRAM 2.2.0 PRODUCTION ROLLBACK RUNBOOK

**Target Release:** 2.2.0 (versionCode 20201)  
**Previous Stable:** 2.1.0 (versionCode 20100)  

---

## 1. Immediate Staged Rollout Halting (Play Console)
1. Log into Google Play Console.
2. Select **Healthogram** -> **Production** -> **Releases**.
3. Under **Active Releases**, locate `2.2.0 (20201)`.
4. Click **Halt Rollout**.
5. Confirm action. The rollout stops immediately; no further users receive update 2.2.0.

---

## 2. Remote Config Instant Feature Deprecation
If an issue is isolated to a specific subsystem:
```json
{
  "parameters": {
    "emergency_switches": {
      "marketplace_killswitch": true,
      "webrtc_calling_killswitch": true,
      "ai_generation_killswitch": true
    }
  }
}
```
Deploy instantly via Firebase Console or CLI (`firebase remoteconfig:rollback` / publish).

---

## 3. Emergency Binary Rollback / Hotfix Forward Roll
Because Google Play does not support downgrading versionCode for users who already updated to `20201`, the approved emergency procedure is:
1. Promote hotfix branch `hotfix/2.2.1` based on stable source.
2. Bump `versionCode = 20202` and `versionName = "2.2.1"`.
3. Fast-track build via automated CI/CD pipeline.
4. Upload `Healthogram-2.2.1-release.aab` to Play Console Production with 100% rollout.
