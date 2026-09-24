## What changed
<!-- Provide a concise description of code and architectural changes. -->

## Why
<!-- State the problem, feature requirement, or issue reference motivating this change. -->

## Healthogram modules affected
<!-- Check all applicable modules -->
- [ ] Authentication & Session Security (4-device limit)
- [ ] Account Profiles (Individual, Doctor, Clinic, Hospital, Laboratory)
- [ ] Healthcare Professional & Facility Verification
- [ ] Social Feed, Stories & Video Reels
- [ ] Health Passport & PHI Security (AES-GCM-256)
- [ ] Dynamic QR Cryptographic Access
- [ ] Marketplace (Customer & Seller)
- [ ] Payments & Idempotency
- [ ] Financial Ledger & Owner Payouts
- [ ] Delivery & Logistics (11-state machine)
- [ ] AI Studio & Clinical Summaries
- [ ] Messaging & WebRTC Calling
- [ ] Translation & GCC Localization
- [ ] Push Notifications (No clinical PHI)
- [ ] Admin Control (17 Isolated Roles)
- [ ] Owner Control & Emergency Switches

## Firebase changes
- [ ] Firestore Security Rules (`firestore.rules`)
- [ ] Firestore Indexes (`firestore.indexes.json`)
- [ ] Storage Rules (`storage.rules`)
- [ ] Cloud Functions (`functions/src/`)
- [ ] Firebase App Check configuration
- [ ] None

## Security impact
<!-- Describe threat model assessment, PHI handling, and authorization rules. -->

## Database changes
<!-- Describe any collection additions, field modifications, or indexing adjustments. -->

## Migration required?
- [ ] Yes (attach migration guide from `docs/database/migrations/`)
- [ ] No backward-incompatible schema changes

## QA performed
<!-- Detail automated test cases executed, synthetic accounts tested, and devices checked. -->

## Screenshots/videos if UI changed
<!-- Add visual proof of UI changes across light/dark themes and RTL if applicable. -->

## Rollback plan
<!-- Define emergency rollback or feature-flag disable procedure if this change fails. -->

## Release notes
<!-- Draft user-facing and admin-facing release notes. -->
