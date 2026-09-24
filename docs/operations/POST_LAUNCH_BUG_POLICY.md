# HEALTHOGRAM — POST-LAUNCH DEFECT CLASSIFICATION & BUG POLICY

**Classification:** Quality Assurance & Production Governance  
**Target Versions:** v1.0.x Maintenance & v1.1.x Feature Updates  

---

## 1. Severity Definitions & SLAs

| Severity Tier | Definition | Response SLA | Target Fix SLA | Release Pipeline |
| :--- | :--- | :--- | :--- | :--- |
| **P0 (Blocker / Emergency)** | Uncontrolled PHI data leak, credential breach, double billing, universal crash on launch, core authentication failure. | < 15 minutes | < 4 hours | Emergency Hotfix (`hotfix/*`), Expedited Review. |
| **P1 (Critical / Major)** | Core workflow broken for significant user cohort (> 5%), checkout failure, teleconsultation connection failure, camera permission crash. | < 1 hour | < 24 hours | Fast-track patch release (`release/1.0.x`). |
| **P2 (Normal / Moderate)** | Non-blocking defect, edge-case UI rendering bug on specific device, minor translation inaccuracy, slow image thumbnail loading. | < 1 business day | Next bi-weekly patch | Standard scheduled patch release. |
| **P3 (Minor / Cosmetic)** | Minor text truncation, non-standard padding, cosmetic dark mode contrast quirk, subtle animation stutter. | < 3 business days | Next minor version (v1.1.0) | Scheduled milestone backlog. |

---

## 2. Defect Ingestion & Triage Rules

1. **Intake Channels:**
   - User Support Tickets (`support@healthogram.app`)
   - Firebase Crashlytics automated issue clustering
   - Google Play Console Android Vitals (ANR & Crash alerts)
   - Security Vulnerability Submissions (`security@healthogram.app`)
2. **Triage Invariants:**
   - Any bug report touching `Health Passport`, `Encryption`, `Payment Ledger`, or `Account Roles` automatically defaults to **P1** or **P0** until formally de-escalated by the Security Lead.
   - Bugs cannot be marked as `RESOLVED` without an accompanying automated test verifying the fix.
