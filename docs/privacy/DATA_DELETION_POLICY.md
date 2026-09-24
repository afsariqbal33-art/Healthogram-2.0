# HEALTHOGRAM — USER DATA DELETION & ACCOUNT PURGE POLICY

**Scope:** Right to be Forgotten (GDPR Art. 17) & Google Play Account Deletion Policy  
**Effective Date:** September 16, 2026  
**In-App Access:** Profile -> Settings -> Account -> Delete Account  
**Web Deletion Request URL:** `https://healthogram.app/delete-account`  

---

## 1. Overview

Healthogram complies with Google Play's Account Deletion Requirement by offering both in-app and web-based self-service account deletion workflows. When a user requests account deletion, all personal, social, and medical records are purged, subject to statutory medical and financial audit retention laws.

---

## 2. Deletion Cascade Matrix

| Data Category | Immediate Action | 30-Day Grace Period | Post-Grace Permanent Action | Statutory Exception / Retention Reason |
| :--- | :--- | :--- | :--- | :--- |
| **Authentication Credential** | Disabled immediately | Account deactivated; recovery possible | Permanently erased from Firebase Auth | None |
| **Public Profile & Avatar** | Hidden from public search and feeds | Retained in soft-deleted state | Permanently deleted from Firestore & Storage | None |
| **Social Posts & Reels** | Hidden from feeds and explore | Retained in soft-deleted state | Storage assets and Firestore docs purged | None |
| **Direct Messages** | Message history marked as deleted for user | Retained in soft-deleted state | Content deleted; recipient keeps their copy | None |
| **Health Passport Records** | Access grants revoked immediately | Retained in soft-deleted state | All conditions, medications, allergies purged | Patient request overrides default retention |
| **Medical Documents Vault** | Encrypted files marked for deletion | Retained in soft-deleted state | Secure overwrite & deletion from Cloud Storage | None |
| **Health Access Audit Logs** | Anonymized user pointer | Unchanged | Retained with pseudonymous hash | **Retained for 7 years** per statutory clinical compliance |
| **Marketplace Order Invoices**| Removed from customer order history | Unchanged | Transaction records archived | **Retained for 7-10 years** per tax/financial regulations |
| **Financial Ledger Entries** | Anonymized buyer/seller references | Unchanged | Double-entry financial records preserved | **Retained for 10 years** to prevent financial fraud |
| **Crash & Diagnostic Logs** | User identifier dissociated | Unchanged | Automatically purged on 90-day rolling cycle | None |

---

## 3. Account Deletion Execution Procedure

1. **User Request:** User initiates deletion in Android app via Settings or online portal.
2. **Re-Authentication:** User confirms identity via password / biometric / 2FA.
3. **Immediate Lockout:**
   - Active device sessions are revoked (`users/{uid}/devices/*` cleared).
   - Firebase Auth user disabled.
   - All active Health Passport QR session tokens are invalidated.
4. **30-Day Cooling-off Period:** If user logs in during this period, an "Account Pending Deletion" recovery screen is shown, allowing the user to cancel deletion.
5. **Permanent Deletion Worker (Cloud Function):**
   - Batched deletion runs on Day 31.
   - All Firestore documents across user subcollections are hard-deleted.
   - Storage files in `health_vault/{uid}` and `users/{uid}` are permanently removed.
   - Confirmation email sent to user.
