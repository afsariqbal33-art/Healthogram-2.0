# HEALTHOGRAM BACKEND SOURCE OF TRUTH (VERSION 20.0)

## 1. EXECUTIVE ARCHITECTURE OVERVIEW

This document constitutes the authoritative, final backend and data architecture specification for Healthogram, unifying and harmonizing all systems developed across Steps 1 through 19.

### 1.1 Controlled Account Categories (Strict Limit: 5)
Healthogram strictly enforces five (5) core platform account categories across all identity, profile, and authorization models:
1. `individual`: Patient and citizen users owning a personal Health Passport.
2. `doctor`: Licensed medical practitioners providing clinical consultations and authoring medical records.
3. `clinic`: Outpatient healthcare centers and group practices.
4. `hospital`: Comprehensive inpatient and emergency medical centers.
5. `laboratory`: Diagnostic facilities issuing pathology, radiology, and clinical test reports.

*Policy Invariant:* **PHARMACY IS STRICTLY EXCLUDED.** Any legacy attempt to register or query a pharmacy account category is permanently rejected by security rules and backend validation.

### 1.2 Marketplace Role Model (Strict Separation)
Marketplace commerce roles are strictly orthogonal to healthcare account types:
1. `customer`: Consumers purchasing medical supplies, wellness goods, and health devices.
2. `seller`: Registered and verified merchants listing products and fulfilling orders.

*Role Separation Rule:* `Customer ≠ Seller` and `Seller ≠ Doctor / Clinic / Hospital / Laboratory`. A clinical account holder does NOT automatically inherit seller permissions, and sellers have zero access to Health Passport records.

---

## 2. RESOLVED ARCHITECTURAL CONFLICTS MATRIX

| Domain | Historical / Conflicting Models | Final Authoritative Resolution (Step 20) | Rationale & Safety Justification |
| :--- | :--- | :--- | :--- |
| **Profile Storage** | Single monolithic profile vs Split public/private | **Split Architecture**: `public_profiles` (public discovery) and `private_profiles` (PII/compliance). Canonical alias `user_profiles` resolves to safe sanitization view. | Minimizes data exposure; prevents PII leakage during feed/social exploration; zero document-level leakage. |
| **Health Records** | Subcollections under `health_profiles/{uid}/...` vs Root collections with `patientUid` | **Root Collections with `patientUid` reference**: (`health_conditions`, `health_allergies`, `health_medications`, etc.). | Enables cross-collection security queries, targeted audit logging, fine-grained access grants, and avoids hotspot write limits on parent profile docs. |
| **Counters (Social)** | Direct client increment (`FieldValue.increment`) vs Server-authoritative sync | **Server-side Cloud Functions + Atomic Batched Write Validation**. Direct client manipulation blocked by rules on sensitive financial/social counters. | Eliminates client-side count spoofing, replay manipulation, and concurrent race condition desynchronization. |
| **Marketplace Orders** | Flat order document vs Hierarchical Suborder fulfillment | **Multi-Tier Order Model**: Parent `orders` split into per-seller `order_suborders` with separate `order_items` and `shipments`. | Single customer checkout cart can span multiple sellers with independent shipping, delivery tracking, cancellations, and escrow payouts. |
| **Financial Ledger** | Mutable account balance documents vs Immutable Double-Entry Ledger | **Strictly Immutable Double-Entry Ledger** (`financial_ledger_entries`). Account balances (`marketplace_seller_balances`, `owner_earnings`) are strictly computed projections. | Prevents silent ledger manipulation; fully reconcilable against Stripe/PayPal/M-Pesa/Bank transfer webhooks. |
| **Health QR Codes** | Encoded raw medical data in QR vs Ephemeral Opaque Session Token | **Opaque Ephemeral Single-Use Token** (`health_qr_sessions`). Raw medical data is NEVER encoded in QR images. | Scanning only resolves to a temporary handshake requiring doctor verification and real-time patient consent grant approval. |
| **Configuration** | Ad-hoc remote config flags vs Hierarchical Precedence Engine | **5-Tier Precedence**: `Specific User` → `Account Type` → `Country Code` → `Environment` → `Global Default`. | Enables granular regional compliance, safe canary rollouts, and targeted emergency kill-switches. |

---

## 3. CANONICAL FIRESTORE DOMAINS & COLLECTION INVENTORY

Cloud Firestore is the primary application database. Documents are compartmentalized into distinct security domains:

### 3.1 Domain A: Identity, Authentication & Security
- `users/{uid}`: Core auth sync, email/phone verification status, base account category (`individual`, `doctor`, `clinic`, `hospital`, `laboratory`), suspension flags.
- `user_profiles/{uid}` & `public_profiles/{uid}`: Public discovery, avatar, bio, city, country, specialty (for doctors), verified badge.
- `private_profiles/{uid}`: Legal name, national ID reference, emergency phone, residential address.
- `user_settings/{uid}`: Preferences, language, timezone, privacy toggles, marketing consent.
- `user_devices/{deviceId}`: Bound mobile/tablet devices (max 4 per user), FCM token, platform, last active.
- `user_sessions/{sessionId}`: Active login sessions, refresh token fingerprints, IP address, revocation status.
- `user_blocks/{blockId}`: Bidirectional block relationships between user pairs.
- `user_restrictions/{restrictionId}`: Account penalties, temporary mutes, shadow-bans issued by admin/security.
- `account_actions/{actionId}`: Moderation actions (warnings, suspensions, credential resets).
- `account_deletion_requests/{requestId}`: Right-to-be-forgotten lifecycle tracker (30-day grace period, asset purge queue).
- `account_export_requests/{requestId}`: GDPR/HIPAA machine-readable data export requests and download token lifecycle.
- `login_security_events/{eventId}`: Failed login telemetry, brute-force anomalies, new device challenges.
- `authentication_events/{eventId}`: MFA challenges, password resets, biometrics handshakes.

### 3.2 Domain B: Health Passport (Zero-Trust Isolation)
*Critical Invariant:* Health Passport data is strictly quarantined from social, marketing, marketplace, AI, and public search.
- `health_profiles/{patientUid}`: Emergency summary, blood type, organ donor status, emergency contacts.
- `health_passports/{passportId}`: Master passport metadata, issuance date, verification status.
- `health_conditions/{recordId}`: Chronic and acute conditions, ICD-10 code, diagnosis date, treating doctor.
- `health_allergies/{recordId}`: Allergen name, reaction severity (`MILD`, `MODERATE`, `SEVERE`, `ANAPHYLAXIS`), verified date.
- `health_medications/{recordId}`: Drug name, dosage, frequency, start/end dates, prescribing doctor.
- `health_visits/{recordId}`: Clinical encounter logs, facility reference, reason for visit, clinical notes.
- `health_diagnoses/{recordId}`: Formal diagnostic assessments, clinical staging.
- `health_tests/{recordId}`: Diagnostic test orders, sample collection status, completion dates.
- `health_lab_reports/{recordId}`: Structured laboratory results, reference ranges, specimen metadata, signed PDF vault reference.
- `health_prescriptions/{recordId}`: Electronic prescriptions, dosage instructions, electronic signature.
- `health_documents/{recordId}`: Scanned clinical records, discharge summaries, imaging links (private Storage only).
- `health_bills/{recordId}`: Medical invoices, insurance co-pay references.
- `health_access_grants/{grantId}`: Granular patient-approved access delegations (scopes: `CONDITIONS`, `ALLERGIES`, `MEDICATIONS`, `LABS`, `VISITS`, `DOCUMENTS`; expiry timestamp).
- `health_access_logs/{logId}`: Immutable, tamper-evident audit log of every read/export of medical data.
- `health_qr_sessions/{sessionId}`: Single-use, time-limited (max 15 min) QR exchange tokens.
- `health_sharing_links/{linkId}`: Password-protected, time-bounded emergency sharing links with explicit revocation.

### 3.3 Domain C: Social & Content
- `posts/{postId}`: Text, media pointers, tags, author uid, visibility (`PUBLIC`, `FOLLOWERS_ONLY`, `PRIVATE`), moderation status.
- `post_media/{mediaId}`: Storage references, aspect ratios, thumbnails, alt text.
- `post_likes/{likeId}`: Unique pairs `(postId_uid)` preventing duplicate likes.
- `post_comments/{commentId}`: Hierarchical comment threads, author uid, content, moderation flags.
- `comment_replies/{replyId}`: Nested sub-replies.
- `post_saves/{saveId}`: User bookmark reference `(uid_postId)`.
- `post_shares/{shareId}`: Amplification and cross-post references.
- `follows/{followId}`: Active follower relationship `(followerUid_followingUid)`.
- `follow_requests/{requestId}`: Pending approval requests for private accounts.
- `stories/{storyId}`: Ephemeral 24-hour visual posts with automatic TTL expiration.
- `story_views/{viewId}`: Deduped story view events.
- `reels/{reelId}`: Short-form vertical videos with sound metadata.
- `hashtags/{tag}`: Normalized topic indexing and aggregate engagement counts.
- `content_reports/{reportId}`: User-submitted abuse/misinformation flags.
- `content_moderation_events/{eventId}`: Automated AI/human content triage decisions.

### 3.4 Domain D: Marketplace (E-Commerce)
- `marketplace_products/{productId}`: Product listing, seller UID, SKU, price, currency, country code, inventory quantity, compliance status (`PENDING_REVIEW`, `APPROVED`, `REJECTED`).
- `product_variants/{variantId}`: Specific size, color, dosage, package configurations.
- `product_inventory/{inventoryId}`: Real-time stock counts, reserved stock, safety buffer.
- `inventory_movements/{movementId}`: Immutable inventory audit (inward, reserved, dispatched, returned).
- `shopping_carts/{cartId}` & `marketplace_cart_items/{itemId}`: Active buyer items, quantity, snapshot price.
- `wishlists/{wishlistId}` & `wishlist_items/{itemId}`: Saved products for future purchase.
- `marketplace_orders/{orderId}`: Master checkout order (multi-seller umbrella, payment status, total amount).
- `order_suborders/{suborderId}`: Seller-specific fulfillment order branch.
- `order_items/{itemId}`: Granular line items with price, product, and seller snapshot.
- `order_status_history/{historyId}`: Audit trail of transitions (`PLACED` → `CONFIRMED` → `PACKED` → `SHIPPED` → `DELIVERED`).
- `marketplace_returns/{returnId}` & `marketplace_refunds/{refundId}`: Return claims, RMA generation, refund processing.
- `marketplace_reviews/{reviewId}`: Verified-buyer product and seller reviews.

### 3.5 Domain E: Delivery & Fulfillment
- `delivery_providers/{providerId}`: Integrated logistics couriers (DHL, FedEx, Local 3PL).
- `delivery_zones/{zoneId}`: Geo-fenced polygons and postal codes with flat/dynamic pricing.
- `delivery_rates/{rateId}`: Base rate cards by distance and weight tier.
- `delivery_assignments/{assignmentId}`: Driver/courier assignment to package.
- `shipments/{shipmentId}`: Package tracking ID, carrier, origin, destination, tracking timeline.
- `delivery_tracking/{trackingId}`: GPS checkpoints, status updates.
- `delivery_proof/{proofId}`: Signature, recipient photo, geotagged confirmation.
- `delivery_otp_sessions/{sessionId}`: 6-digit delivery confirmation code validation.

### 3.6 Domain F: Payments, Financial Ledger & Owner Earnings
- `payment_transactions/{txId}`: Authorized payment records, gateway references, idempotency key.
- `payment_attempts/{attemptId}`: Ephemeral payment initiation events (fraud checks, 3D-Secure).
- `payment_webhook_events/{eventId}`: Deduplicated, cryptographically signed gateway webhooks.
- `financial_ledger_entries/{entryId}`: Immutable double-entry financial journal.
- `marketplace_seller_balances/{sellerUid}`: Computed available, pending, and reserved payout balances.
- `marketplace_seller_payout_requests/{requestId}`: Seller bank payout requests and remittance tracking.
- `owner_earnings/{earningsId}`: Platform fee, commission, and subscription revenue ledgers.
- `owner_revenue_entries/{entryId}`: Granular commission capture attached to order items.
- `owner_withdrawal_requests/{withdrawalId}`: Owner profit distribution records to treasury accounts.
- `financial_reconciliations/{runId}`: Daily automated ledger vs bank/stripe reconciliation runs.

### 3.7 Domain G: Real-Time Communication & Calling
- `conversations/{conversationId}`: Direct and group message rooms, participant array, last message snapshot.
- `conversation_participants/{id}`: Membership roles, mute toggles, unread counters.
- `messages/{messageId}`: Text content, encrypted media pointers, delivery status (`SENT`, `DELIVERED`, `READ`).
- `message_reactions/{reactionId}`: Emoji reactions attached to message.
- `call_sessions/{callId}`: WebRTC signaling, call type (`VOICE`, `VIDEO`), room state, participants.
- `call_history/{historyId}`: Call duration, start/end timestamps, quality metrics (zero media stored).

### 3.8 Domain H: Translation System
- `languages/{code}`: Supported ISO language definitions, dialect models.
- `message_translations/{translationId}`: Cached translations keyed by `(messageId_targetLang)`.
- `translation_jobs/{jobId}`: Asynchronous batch translation workloads.
- `voice_transcripts/{transcriptId}`: Temporary clinical or call transcripts (governed by consent retention).
- `translation_usage/{usageId}`: Character/second metering for quota and cost accounting.

### 3.9 Domain I: AI Studio (Gemini 3.8 Flash Backend)
- `ai_jobs/{jobId}`: AI generation task, model ID (`gemini-3.8-flash`), task type, status.
- `ai_generated_assets/{assetId}`: Generated copy, summaries, artwork, social captions.
- `ai_usage/{usageId}`: Token consumption (input tokens, output tokens, cost calculation).
- `ai_settings/{uid}`: Personal AI model preferences, tone controls, privacy constraints.
- `ai_audit_logs/{logId}`: Audit log ensuring no Health Passport data was ingested into AI tasks.

### 3.10 Domain J: Notifications & Verification
- `notifications/{notificationId}`: In-app user notifications (type, title, message preview, deeplink, read status).
- `notification_preferences/{uid}`: Granular opt-ins for push, SMS, email, in-app channels.
- `verification_applications/{appId}`: Formal verification dossiers for Doctors, Clinics, Hospitals, Labs, and Sellers.
- `verification_documents/{docId}`: Medical licenses, accreditation certificates stored in private storage.
- `verification_reviews/{reviewId}`: Internal compliance officer audit records and decision log.

### 3.11 Domain K: Platform Governance & Admin
- `admin_users/{uid}`: Internal staff with RBAC roles (`SUPPORT`, `VERIFICATION`, `MODERATION`, `FINANCE`, `SUPER_ADMIN`).
- `admin_roles/{roleId}`: Explicit permission sets mapped to admin capabilities.
- `admin_audit_logs/{logId}`: Tamper-evident record of all administrative operations.
- `platform_configuration/{configId}`: Global, regional, and versioned configuration parameters.
- `feature_flags/{flagId}`: Dynamic feature toggles with percentage rollouts and environment targeting.
- `emergency_controls/{controlId}`: Instant system-wide kill-switches (registrations, payouts, marketplace, QR).
- `data_integrity_reports/{reportId}`: Output of scheduled system-wide database integrity audits.

---

## 4. FIELD-LEVEL DATA TYPES & METADATA CONTRACTS

Every Firestore document must strictly adhere to standard typing rules:
- **Timestamps**: Firestore `Timestamp` objects (never raw local strings or client epoch timestamps).
- **UIDs & References**: String identifiers corresponding to authenticated Firebase UIDs or document IDs.
- **Amounts & Money**: Stored as 64-bit integer minor currency units (e.g., USD cents `$25.00` = `2500`) to completely eliminate floating-point rounding errors.
- **Audit Fields**: Every mutable document must maintain `created_at`, `updated_at`, `created_by`, and `version`.

---

## 5. ENVIRONMENT SEPARATION & ISOLATION

Healthogram mandates three completely isolated environments:

| Environment | Firebase Project ID | Purpose & Access Rules |
| :--- | :--- | :--- |
| **Development** | `healthogram-dev` | Feature branch prototyping, mock medical data, sandbox payments, synthetic AI loads. |
| **Staging** | `healthogram-staging` | Pre-production testing, QA automated verification, end-to-end load testing. |
| **Production** | `healthogram-prod` | Live production traffic, strict HIPAA/GDPR compliance, real payment gateways, hardened audit logs. |

*Production Boundary Rule:* Development and Staging environments NEVER point to or read from the Production Firestore database or Cloud Storage buckets.

---

## 6. MIGRATION & BACKWARD COMPATIBILITY SYSTEM

The platform uses a formal database migration tracker (`backend_migrations`) and a global schema indicator:
- **Current Database Schema Version**: `20`
- **Non-Destructive Field Migration Pattern**:
  When introducing a new schema representation:
  1. Add `new_field` alongside `old_field`.
  2. Dual-write in application services and Cloud Functions.
  3. Execute a idempotent migration job updating historical records.
  4. Deprecate `old_field` reads.
  5. Remove `old_field` in a subsequent scheduled maintenance release.
