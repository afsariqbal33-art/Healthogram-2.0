# CLOUD FUNCTIONS SOURCE OF TRUTH (VERSION 20.0)

This document serves as the authoritative inventory and specification for all serverless Cloud Functions, scheduled workers, and background task queues powering Healthogram.

---

## 1. FUNCTION INVENTORY BY DOMAIN

### 1.1 Authentication & Security Domain
1. `validateUserSession`: HTTPS Callable. Validates device session fingerprint, enforces max-4 device rule, and revokes compromised tokens.
2. `onUserCreated`: Auth Trigger (`auth.user().onCreate`). Synchronizes baseline user document, assigns default unprivileged roles, creates default notification settings.
3. `onUserDeleted`: Auth Trigger (`auth.user().onDelete`). Triggers automated compliance data purge pipeline.
4. `issueReauthChallenge`: HTTPS Callable. Issues 15-minute cryptographically signed MFA challenges for sensitive operations (password changes, payout accounts).

### 1.2 Health Passport Domain (Zero-Trust Isolation)
5. `requestHealthAccess`: HTTPS Callable. Healthcare provider requests clinical access to specific patient records with required medical justification.
6. `processPatientConsent`: HTTPS Callable. Patient approves or rejects pending access grant with granular scope constraints and maximum 24-hour expiration.
7. `generateHealthQrSession`: HTTPS Callable. Generates a 15-minute, single-use opaque handshake token for in-person clinical admission.
8. `consumeHealthQrSession`: HTTPS Callable. Verified healthcare provider exchanges scanned QR token for an active access grant; token is marked consumed immediately.
9. `expireHealthGrantsScheduler`: Scheduled (Every 15 min). Identifies and revokes expired health access grants and unconsumed QR tokens.

### 1.3 Social & Content Domain
10. `onPostLikeChanged`: Firestore Trigger (`post_likes/{likeId}`). Atomically increments/decrements `posts/{postId}.like_count`.
11. `onCommentCreated`: Firestore Trigger (`post_comments/{commentId}`). Updates comment counts, dispatches notifications, triggers automated toxicity screening.
12. `expireStoriesScheduler`: Scheduled (Every hour). Queries stories where `expires_at < now()` and moves them to archival storage.

### 1.4 Marketplace & Order Management Domain
13. `createMarketplaceOrder`: HTTPS Callable. Validates product availability, reserves stock, creates parent order and seller suborders in a single atomic transaction.
14. `cancelMarketplaceOrder`: HTTPS Callable. Handles buyer/seller cancellations, restores reserved inventory, initiates escrow refund.
15. `onProductInventoryUpdated`: Firestore Trigger (`product_inventory/{id}`). Flags out-of-stock products and updates catalog search status.

### 1.5 Payments & Financial Ledger Domain
16. `createPaymentIntent`: HTTPS Callable. Initializes Stripe/PayPal/M-Pesa session with idempotency key and country-specific pricing rules.
17. `handlePaymentWebhook`: HTTP Webhook. Validates cryptographic provider signature, deduplicates event ID, posts double-entry ledger entries, marks order as `PAID`.
18. `processSellerPayout`: HTTPS Callable. Validates seller available balance, enforces 14-day escrow clearance, dispatches bank transfer, records ledger debit.
19. `processOwnerWithdrawal`: HTTPS Callable. Owner-only withdrawal with mandatory MFA/PIN validation and immutable platform revenue debit.
20. `runDailyFinancialReconciliation`: Scheduled (Daily at 02:00 UTC). Reconciles gateway settlement logs against `financial_ledger_entries` and flags discrepancies.

### 1.6 Delivery & Logistics Domain
21. `calculateDeliveryQuote`: HTTPS Callable. Computes dynamic courier pricing based on pickup/dropoff zones, weight, and delivery SLA.
22. `createShipment`: HTTPS Callable. Generates carrier waybill label, assigns tracking number, attaches to seller suborder.
23. `handleDeliveryWebhook`: HTTP Webhook. Ingests carrier milestone updates (In Transit, Out for Delivery, Delivered) and notifies recipient.
24. `verifyDeliveryOtp`: HTTPS Callable. Validates 6-digit one-time password provided by recipient to courier before marking package as delivered.

### 1.7 AI Studio Domain (Gemini 3.8 Flash)
25. `submitAiJob`: HTTPS Callable. Validates user quotas, strips any sensitive medical data, queues task with Gemini 3.8 Flash model.
26. `processAiQueue`: Background Task Queue. Dispatches batch generation requests to Gemini API, meters token usage, writes output asset.

### 1.8 Translation System Domain
27. `translateTextMessage`: HTTPS Callable. Translates chat messages on demand, caches result in `message_translations`.
28. `processVoiceTranscription`: Storage Trigger. Converts uploaded audio voice messages into text transcripts under strict user consent policies.

### 1.9 Notifications Domain
29. `dispatchPushNotification`: Firestore Trigger (`notifications/{id}`). Formats FCM payload (zero sensitive medical information included) and delivers to active user devices.

### 1.10 Platform Integrity & Governance Domain
30. `runDataIntegrityAudit`: Scheduled (Daily at 04:00 UTC). Comprehensive consistency check across all collections, identifying orphaned documents, stale carts, and unconsumed sessions. Outputs `data_integrity_reports`.
31. `updateEmergencyControls`: HTTPS Callable. Owner-only immediate activation of system kill-switches.

---

## 2. DETAILED FUNCTION SPECIFICATION MATRIX

| Function Name | Trigger Type | Input Params | Auth Required | Idempotent | Expected Runtime | Secret Keys Required |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `validateUserSession` | HTTPS Callable | `deviceId`, `platform` | Any Auth | Yes | 150ms | None |
| `issueReauthChallenge` | HTTPS Callable | `operationType` | Any Auth | Yes | 200ms | JWT Signing Secret |
| `requestHealthAccess` | HTTPS Callable | `patientUid`, `scopes`, `purpose` | Doctor/Clinical | Yes | 300ms | None |
| `processPatientConsent` | HTTPS Callable | `grantId`, `status` | Patient Only | Yes | 350ms | None |
| `generateHealthQrSession` | HTTPS Callable | `singleUse: true` | Patient Only | Yes | 200ms | Token Salt |
| `consumeHealthQrSession` | HTTPS Callable | `opaqueToken` | Doctor/Clinical | Yes | 400ms | None |
| `createMarketplaceOrder` | HTTPS Callable | `cartItems`, `shippingAddress`, `idempotencyKey` | Customer | Yes | 800ms | None |
| `createPaymentIntent` | HTTPS Callable | `orderId`, `gateway`, `idempotencyKey` | Customer | Yes | 600ms | Stripe / PayPal Secret |
| `handlePaymentWebhook` | HTTP Endpoint | Raw Webhook Payload & Signature | Webhook Sig | Yes | 500ms | Stripe / PayPal Webhook Secret |
| `processSellerPayout` | HTTPS Callable | `amount`, `accountId` | Verified Seller | Yes | 1200ms | Gateway Payout API Key |
| `processOwnerWithdrawal`| HTTPS Callable | `amount`, `targetBankId`, `ownerPin` | Platform Owner | Yes | 1000ms | Treasury Master Key |
| `runDailyFinancialReconciliation` | Pub/Sub Cron | None | System | Yes | 15s | Gateway Reporting API |
| `submitAiJob` | HTTPS Callable | `taskType`, `prompt`, `referenceAssetId` | Authenticated | Yes | 500ms | Gemini API Key |
| `dispatchPushNotification` | Firestore Write | New Notification Doc | System | Yes | 250ms | FCM Service Account |
| `runDataIntegrityAudit`| Pub/Sub Cron | None | System | Yes | 25s | None |
| `updateEmergencyControls` | HTTPS Callable | `emergencyToggles`, `ownerPin` | Platform Owner | Yes | 300ms | Owner Auth Secret |
