# HEALTHOGRAM — BACKUP & DISASTER RECOVERY

## 1. Five-Tier Backup Strategy
An APK alone is NEVER a backup. Healthogram maintains:
1. **Tier 1 (Source Code)**: Git repository with protected branch rules (`main`, `staging`, `develop`).
2. **Tier 2 (Source ZIP)**: Automated weekly zipped codebase snapshots.
3. **Tier 3 (Cloud Data)**: Automated daily Cloud Firestore managed exports to encrypted Cloud Storage buckets with versioning.
4. **Tier 4 (Infrastructure & Rules)**: Declarative Firestore security rules, Storage rules, and Cloud Functions code in version control.
5. **Tier 5 (Release Artifacts)**: Immutable signed AAB archives, Proguard mapping files, and Google Play release bundles.

---

## 2. Step Checkpoints

### Checkpoint: `step-07-verification-system-complete`
- **Date**: 2026-09-10
- **Scope**: Step 07 Healthcare Verification + Verified Badge System
- **Components Backed Up**:
  - FlutterFlow UI pages & Compose components:
    - `VerificationCenterPage`
    - `VerificationIntroductionPage`
    - `VerificationAccountTypePage`
    - `VerificationCountryPage`
    - `VerificationRequirementsPage`
    - `VerificationApplicationPage`
    - `VerificationDocumentUploadPage`
    - `VerificationDocumentPreviewPage`
    - `VerificationReviewPage`
    - `VerificationSubmittedPage`
    - `VerificationStatusPage`
    - `VerificationAdditionalInfoPage`
    - `VerificationSuccessPage`
    - `VerificationRejectedPage`
    - `VerificationHistoryPage`
    - `VerificationPrivacyPage`
    - `VerificationBadgeInfoPage`
    - `VerificationDashboardPage`
    - `VerifiedBadge`
    - `VerificationStatusCard`
    - `VerificationProgressCard`
    - `VerificationRequirementCard`
    - `VerificationDocumentCard`
    - `VerificationDocumentUploadCard`
    - `VerificationApplicationCard`
    - `VerificationReviewCard`
    - `VerificationDecisionCard`
    - `VerificationExpiryBanner`
    - `VerificationAdditionalInfoCard`
    - `VerificationBadgeInfoModal`
    - `VerificationCountryRequirementCard`
    - `HealthcareScannerEligibilityCard`
  - Firestore Collections Schema:
    - `verification_profiles/{uid}`
    - `verification_applications/{applicationId}`
    - `verification_documents/{documentId}`
    - `verification_audit_logs/{logId}`
    - `countries/{countryCode}`
    - `country_verification_requirements/{requirementId}`
    - `verification_queue/{queueId}`
  - Firestore Security Rules (`firestore.rules`):
    - Strict authorization with `verification_reviewer`, `verification_manager`, and client-mutation restrictions on verification status, badge, and reviewer fields.
  - Firebase Storage Rules (`storage.rules`):
    - Isolated `verification_private/{uid}/{applicationId}/{documentId}` path; public access strictly blocked.
  - Cloud Functions Backend:
    - `submitVerificationApplication()`, `validateVerificationApplication()`, `assignVerificationReviewer()`, `startVerificationReview()`, `requestAdditionalInformation()`, `approveVerificationApplication()`, `rejectVerificationApplication()`, `suspendVerification()`, `revokeVerification()`, `expireVerification()`, `renewVerification()`, `setVerifiedBadge()`, `removeVerifiedBadge()`, `validateHealthcareScannerEligibility()`, `processVerificationDocument()`, `createVerificationAuditLog()`, `sendVerificationNotification()`, `processExpiredVerifications()`.
  - Indexes: `firestore.indexes.json` updated with verification query indexes.
  - Security Test Suite: `HealthcareVerificationSecurityTest.kt` covering all 30 security attack and CUJ tests.

