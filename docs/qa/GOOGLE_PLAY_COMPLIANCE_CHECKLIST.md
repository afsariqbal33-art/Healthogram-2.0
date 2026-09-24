# Google Play Console Launch & Compliance Checklist

## Healthogram Version 1.0.0

This checklist tracks mandatory Google Play policies, developer requirements, testing tracks, permissions disclosures, and country release strategies.

---

## 1. 2023+ Personal Developer Account Requirements (Mandatory 14-Day Testing)

For Google Play developer personal accounts created after November 13, 2023:
- [x] **Tester Recruitment**: Recruited 12+ qualified testers across diverse Android hardware.
- [x] **Continuous Opt-in**: All testers enrolled via Google Play Closed Testing track link.
- [x] **14 Consecutive Days**: Continuous testing period executed and monitored.
- [x] **Tester Engagement**: Recorded active sessions, bug reports, and UX feedback.
- [x] **Production Access Application**: Completed Play Console questionnaire outlining testing process, bugs addressed, and production readiness.

---

## 2. Google Play Testing Track Workflow

```text
[Internal Testing Track]
- Up to 100 testers (Engineers, QA, Security reviewers)
- Instant distribution, no Google Play review wait
       ↓
[Closed Testing Track]
- 12+ opt-in testers for 14 continuous days
- Validates real-world connectivity, push notifications, media codecs
       ↓
[Production Access Review]
- Play Console evaluates test metrics and grants production upload access
       ↓
[Controlled Staged Production Rollout]
- Initial release to selected Owner-approved launch countries
- Phased rollout (10% → 20% → 50% → 100%)
```

---

## 3. Android Runtime Permissions Justification Matrix

| Permission | Protection Level | Trigger / User Context | Fallback if Denied |
| :--- | :--- | :--- | :--- |
| `android.permission.INTERNET` | Normal | Core cloud synchronization, Firebase API, REST API | Offline banner with cached Room database |
| `android.permission.ACCESS_NETWORK_STATE` | Normal | Network reachability detection | Seamless auto-switch to offline sync queue |
| `android.permission.MODIFY_AUDIO_SETTINGS` | Normal | WebRTC voice/video call audio routing (Speaker vs Earpiece) | Standard default audio routing |
| `android.permission.VIBRATE` | Normal | Haptic feedback on notifications and barcode scans | Silent visual feedback only |
| `android.permission.RECORD_AUDIO` | Dangerous | Audio calls, voice messages, video reel recording | Text messaging only; call initiation blocked with explanatory rationale dialog |
| `android.permission.CAMERA` | Dangerous | Video calls, Health Passport QR scan, document capture | Manual code entry for QR; text-only communication |
| `android.permission.POST_NOTIFICATIONS` | Dangerous | FCM real-time alerts (incoming calls, appointment requests)| In-app notification center only |

*Note: Zero usage of broad storage permissions (`READ_EXTERNAL_STORAGE`). Android Photo Picker is utilized for zero-permission privacy compliance.*

---

## 4. Controlled Country Rollout Configuration

Healthogram enforces double-layer geo-fencing:
1. **Google Play Console Country Selection**: Store listing and downloads restricted to authorized countries initially.
2. **Backend Country Matrix (`country_configs`)**: Feature availability, currency processing, and delivery options bound to validated country jurisdictions.
