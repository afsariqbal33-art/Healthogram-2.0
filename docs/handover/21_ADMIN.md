# 21 — ADMINISTRATIVE CONTROL PANEL

## 1. Role-Based Access Control (RBAC)
Administrative privileges are governed strictly on the principle of least privilege:
* **Tier 1 (Moderator):** Social content moderation, reported posts, community disputes.
* **Tier 2 (Verification Officer):** Review of doctor medical licenses, clinic registration documents, seller KYC.
* **Tier 3 (Support Specialist):** Order tracking disputes, user account recovery assistance.
* **Tier 4 (System Administrator):** System health metrics, error logs, user ban/unban enforcement.

## 2. Inviolable Governance: No Casual Browsing of Health Passports
* **Strict Privacy Shield:** Administrators **do not have access** to browse, view, or export patient Health Passports.
* **Cryptographic Barricade:** Because vaults are client-side encrypted with patient keys, even a compromised administrator account cannot decrypt clinical records.
* **Audit Trail:** Every administrative action (verification, ban, dispute resolution) generates an immutable log entry in `/audit_logs/`.
