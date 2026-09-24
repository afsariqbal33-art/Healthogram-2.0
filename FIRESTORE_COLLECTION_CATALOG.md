# FIRESTORE COLLECTION CATALOG (VERSION 20.0)

This catalog details all authoritative Firestore collections in the Healthogram platform, specifying their data fields, data types, security classification, ownership, and relationship hierarchy.

---

## 1. IDENTITY, PROFILES & SECURITY

### `users`
- **Path**: `/users/{uid}`
- **Security Classification**: `CONFIDENTIAL`
- **Ownership**: User (`auth.uid == uid`)
- **Key Fields**:
  - `uid`: string (Firestore doc ID matches Firebase Auth UID)
  - `email`: string (Auth synchronized)
  - `phone_number`: string (E.164 format)
  - `account_type`: string (`individual`, `doctor`, `clinic`, `hospital`, `laboratory`)
  - `marketplace_role`: string (`customer`, `seller`)
  - `is_verified`: boolean (Controlled server-side)
  - `verification_status`: string (`not_started`, `pending`, `in_review`, `approved`, `rejected`)
  - `account_status`: string (`active`, `suspended`, `pending_verification`, `deactivated`)
  - `mfa_enabled`: boolean
  - `created_at`: timestamp
  - `updated_at`: timestamp

### `user_profiles` (also mirrored in `public_profiles`)
- **Path**: `/user_profiles/{uid}`
- **Security Classification**: `PUBLIC`
- **Ownership**: User (`auth.uid == uid`)
- **Key Fields**:
  - `uid`: string
  - `account_type`: string (`individual`, `doctor`, `clinic`, `hospital`, `laboratory`)
  - `display_name`: string
  - `username`: string (unique indexed)
  - `profile_photo_url`: string
  - `cover_photo_url`: string
  - `bio`: string
  - `country_code`: string (ISO-3166 2-letter)
  - `city`: string
  - `language`: string (ISO 639-1)
  - `timezone`: string (IANA format)
  - `is_private`: boolean
  - `is_verified`: boolean
  - `verification_badge_type`: string (`BLUE_DOCTOR`, `GREEN_CLINIC`, `PURPLE_HOSPITAL`, `ORANGE_LAB`, `GOLD_SELLER`, `NONE`)
  - `follower_count`: number (server-authoritative)
  - `following_count`: number (server-authoritative)
  - `post_count`: number (server-authoritative)
  - `created_at`: timestamp
  - `updated_at`: timestamp
  - `last_active_at`: timestamp

### `user_devices`
- **Path**: `/user_devices/{deviceId}`
- **Security Classification**: `CONFIDENTIAL`
- **Ownership**: User (`auth.uid == uid`)
- **Key Fields**:
  - `device_id`: string
  - `uid`: string
  - `fcm_token`: string
  - `device_model`: string
  - `os_version`: string
  - `app_version`: string
  - `is_active`: boolean
  - `last_seen_at`: timestamp
  - `created_at`: timestamp

### `user_sessions`
- **Path**: `/user_sessions/{sessionId}`
- **Security Classification**: `CONFIDENTIAL`
- **Ownership**: User (`auth.uid == uid`)
- **Key Fields**:
  - `session_id`: string
  - `uid`: string
  - `device_id`: string
  - `ip_address_hash`: string
  - `user_agent`: string
  - `is_active`: boolean
  - `revoked_at`: timestamp (optional)
  - `created_at`: timestamp
  - `expires_at`: timestamp

### `user_blocks`
- **Path**: `/user_blocks/{blockId}`
- **Security Classification**: `CONFIDENTIAL`
- **Ownership**: User (`auth.uid == blockerUid`)
- **Key Fields**:
  - `block_id`: string (`{blockerUid}_{blockedUid}`)
  - `blocker_uid`: string
  - `blocked_uid`: string
  - `reason`: string
  - `created_at`: timestamp

### `account_deletion_requests`
- **Path**: `/account_deletion_requests/{requestId}`
- **Security Classification**: `CRITICAL`
- **Ownership**: User (`auth.uid == uid`) / Server
- **Key Fields**:
  - `request_id`: string
  - `uid`: string
  - `status`: string (`PENDING_GRACE_PERIOD`, `PROCESSING`, `COMPLETED`, `CANCELLED`)
  - `scheduled_deletion_at`: timestamp (30 days from creation)
  - `created_at`: timestamp
  - `completed_at`: timestamp (optional)

---

## 2. HEALTH PASSPORT (ZERO-TRUST ISOLATED DOMAIN)

*Safety Mandate:* Health Passport data is never mixed with public social feeds, marketplace listings, or unauthenticated queries.

### `health_profiles`
- **Path**: `/health_profiles/{patientUid}`
- **Security Classification**: `RESTRICTED_HEALTH`
- **Ownership**: Patient (`auth.uid == patientUid`)
- **Key Fields**:
  - `patient_uid`: string
  - `blood_type`: string (`A_POS`, `A_NEG`, `B_POS`, `B_NEG`, `AB_POS`, `AB_NEG`, `O_POS`, `O_NEG`, `UNKNOWN`)
  - `organ_donor`: boolean
  - `emergency_contacts`: array of maps (`name`, `relationship`, `phone_number`)
  - `vital_height_cm`: number
  - `vital_weight_kg`: number
  - `updated_at`: timestamp

### `health_conditions`
- **Path**: `/health_conditions/{recordId}`
- **Security Classification**: `RESTRICTED_HEALTH`
- **Ownership**: Patient (`patientUid`)
- **Key Fields**:
  - `record_id`: string
  - `patient_uid`: string
  - `condition_name`: string
  - `icd10_code`: string (optional)
  - `status`: string (`ACTIVE`, `IN_REMISSION`, `RESOLVED`)
  - `diagnosed_date`: timestamp
  - `diagnosed_by_uid`: string
  - `diagnosed_by_name`: string
  - `created_at`: timestamp

### `health_allergies`
- **Path**: `/health_allergies/{recordId}`
- **Security Classification**: `RESTRICTED_HEALTH`
- **Ownership**: Patient (`patientUid`)
- **Key Fields**:
  - `record_id`: string
  - `patient_uid`: string
  - `allergen`: string
  - `severity`: string (`MILD`, `MODERATE`, `SEVERE`, `ANAPHYLAXIS`)
  - `reaction_description`: string
  - `created_at`: timestamp

### `health_medications`
- **Path**: `/health_medications/{recordId}`
- **Security Classification**: `RESTRICTED_HEALTH`
- **Ownership**: Patient (`patientUid`)
- **Key Fields**:
  - `record_id`: string
  - `patient_uid`: string
  - `medication_name`: string
  - `dosage`: string
  - `frequency`: string
  - `start_date`: timestamp
  - `end_date`: timestamp (optional)
  - `status`: string (`CURRENT`, `COMPLETED`, `DISCONTINUED`)
  - `prescribed_by_uid`: string

### `health_lab_reports`
- **Path**: `/health_lab_reports/{recordId}`
- **Security Classification**: `RESTRICTED_HEALTH`
- **Ownership**: Patient (`patientUid`)
- **Key Fields**:
  - `record_id`: string
  - `patient_uid`: string
  - `lab_uid`: string (Laboratory account category)
  - `test_name`: string
  - `report_date`: timestamp
  - `status`: string (`FINAL`, `PRELIMINARY`, `AMENDED`)
  - `storage_path`: string (Private storage URI: `health_private/{patientUid}/reports/...`)
  - `results_summary`: map
  - `created_at`: timestamp

### `health_access_grants`
- **Path**: `/health_access_grants/{grantId}`
- **Security Classification**: `RESTRICTED_HEALTH`
- **Ownership**: Patient (`patientUid`)
- **Key Fields**:
  - `grant_id`: string
  - `patient_uid`: string
  - `requester_uid`: string (Doctor, Clinic, Hospital, Lab)
  - `requester_account_type`: string (`doctor`, `clinic`, `hospital`, `laboratory`)
  - `purpose`: string
  - `scopes`: array of string (`CONDITIONS`, `ALLERGIES`, `MEDICATIONS`, `LABS`, `VISITS`, `DOCUMENTS`)
  - `status`: string (`REQUESTED`, `APPROVED`, `REVOKED`, `EXPIRED`)
  - `granted_at`: timestamp
  - `expires_at`: timestamp (Strict max 24 hours)

### `health_qr_sessions`
- **Path**: `/health_qr_sessions/{sessionId}`
- **Security Classification**: `RESTRICTED_HEALTH`
- **Ownership**: Patient (`patientUid`)
- **Key Fields**:
  - `session_id`: string
  - `patient_uid`: string
  - `opaque_token`: string (Cryptographically random, max 15-minute TTL)
  - `is_single_use`: boolean
  - `is_consumed`: boolean
  - `consumed_by_uid`: string (optional)
  - `consumed_at`: timestamp (optional)
  - `expires_at`: timestamp

---

## 3. SOCIAL MEDIA & CONTENT

### `posts`
- **Path**: `/posts/{postId}`
- **Security Classification**: `PUBLIC`
- **Ownership**: Author (`author_uid`)
- **Key Fields**:
  - `post_id`: string
  - `author_uid`: string
  - `author_account_type`: string (`individual`, `doctor`, `clinic`, `hospital`, `laboratory`)
  - `content_text`: string
  - `media_urls`: array of strings
  - `visibility`: string (`PUBLIC`, `FOLLOWERS_ONLY`, `PRIVATE`)
  - `like_count`: number
  - `comment_count`: number
  - `share_count`: number
  - `status`: string (`PUBLISHED`, `HIDDEN`, `REMOVED_BY_MODERATOR`)
  - `created_at`: timestamp
  - `updated_at`: timestamp

### `post_comments`
- **Path**: `/post_comments/{commentId}`
- **Security Classification**: `PUBLIC`
- **Ownership**: Commenter (`author_uid`)
- **Key Fields**:
  - `comment_id`: string
  - `post_id`: string
  - `author_uid`: string
  - `comment_text`: string
  - `status`: string (`ACTIVE`, `FLAGGED`, `REMOVED`)
  - `created_at`: timestamp

### `stories`
- **Path**: `/stories/{storyId}`
- **Security Classification**: `PUBLIC`
- **Ownership**: Author (`author_uid`)
- **Key Fields**:
  - `story_id`: string
  - `author_uid`: string
  - `media_url`: string
  - `view_count`: number
  - `expires_at`: timestamp (24-hour TTL)
  - `created_at`: timestamp

---

## 4. MARKETPLACE & E-COMMERCE

### `marketplace_products`
- **Path**: `/marketplace_products/{productId}`
- **Security Classification**: `PUBLIC`
- **Ownership**: Seller (`seller_uid`)
- **Key Fields**:
  - `product_id`: string
  - `seller_uid`: string
  - `title`: string
  - `description`: string
  - `sku`: string
  - `category`: string
  - `price`: number (Minor currency units, e.g., cents)
  - `currency`: string (ISO-4217, e.g., `USD`)
  - `country_code`: string (ISO 3166-1)
  - `inventory_quantity`: number
  - `status`: string (`ACTIVE`, `OUT_OF_STOCK`, `DRAFT`, `SUSPENDED`)
  - `approval_status`: string (`PENDING`, `APPROVED`, `REJECTED`)
  - `created_at`: timestamp
  - `updated_at`: timestamp

### `marketplace_orders` (Parent Order)
- **Path**: `/marketplace_orders/{orderId}`
- **Security Classification**: `CONFIDENTIAL`
- **Ownership**: Customer (`customer_uid`)
- **Key Fields**:
  - `order_id`: string
  - `customer_uid`: string
  - `total_amount`: number (Cents)
  - `currency`: string
  - `country_code`: string
  - `payment_status`: string (`PENDING`, `PAID`, `FAILED`, `REFUNDED`)
  - `fulfillment_status`: string (`UNFULFILLED`, `PARTIALLY_FULFILLED`, `FULFILLED`)
  - `suborder_ids`: array of strings
  - `shipping_address`: map
  - `created_at`: timestamp
  - `updated_at`: timestamp

### `order_suborders`
- **Path**: `/order_suborders/{suborderId}`
- **Security Classification**: `CONFIDENTIAL`
- **Ownership**: Seller (`seller_uid`) and Customer (`customer_uid`)
- **Key Fields**:
  - `suborder_id`: string
  - `parent_order_id`: string
  - `seller_uid`: string
  - `customer_uid`: string
  - `subtotal_amount`: number
  - `shipping_amount`: number
  - `commission_amount`: number
  - `seller_payout_amount`: number
  - `status`: string (`PLACED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`)
  - `shipment_id`: string (optional)
  - `created_at`: timestamp

---

## 5. PAYMENTS & FINANCIAL LEDGER

### `payment_transactions`
- **Path**: `/payment_transactions/{txId}`
- **Security Classification**: `CRITICAL`
- **Ownership**: Server-Only (Readable by payer customer and payee seller)
- **Key Fields**:
  - `transaction_id`: string
  - `order_id`: string
  - `customer_uid`: string
  - `seller_id`: string
  - `amount`: number (Cents)
  - `currency`: string
  - `payment_provider`: string (`STRIPE`, `PAYPAL`, `MPESA`, `LOCAL_GATEWAY`)
  - `provider_transaction_id`: string
  - `idempotency_key`: string (Unique constraint)
  - `status`: string (`REQUIRES_ACTION`, `SUCCEEDED`, `FAILED`, `REFUNDED`)
  - `created_at`: timestamp

### `financial_ledger_entries`
- **Path**: `/financial_ledger_entries/{entryId}`
- **Security Classification**: `CRITICAL`
- **Ownership**: Server-Only / Financial Auditors (Strictly Read-Only)
- **Key Fields**:
  - `ledger_entry_id`: string
  - `transaction_id`: string
  - `entry_type`: string (`ESCROW_CREDIT`, `SELLER_PAYABLE`, `PLATFORM_COMMISSION`, `DELIVERY_FEE`, `REFUND_DEBIT`)
  - `direction`: string (`DEBIT`, `CREDIT`)
  - `amount`: number (Cents)
  - `currency`: string
  - `account_type`: string (`ESCROW`, `SELLER`, `OWNER_REVENUE`, `CARRIER`)
  - `account_id`: string
  - `status`: string (`POSTED`, `REVERSED`)
  - `created_at`: timestamp

### `owner_earnings`
- **Path**: `/owner_earnings/{earningsId}`
- **Security Classification**: `CRITICAL`
- **Ownership**: Owner / System Service
- **Key Fields**:
  - `earnings_id`: string
  - `total_platform_revenue`: number (Cents)
  - `marketplace_commissions`: number
  - `delivery_margins`: number
  - `ai_subscription_revenue`: number
  - `pending_clearance`: number
  - `available_for_withdrawal`: number
  - `withdrawn_to_date`: number
  - `currency`: string
  - `last_updated_at`: timestamp

---

## 6. LOGISTICS & DELIVERY

### `shipments`
- **Path**: `/shipments/{shipmentId}`
- **Security Classification**: `CONFIDENTIAL`
- **Ownership**: Customer (`customer_uid`), Seller (`seller_uid`), Carrier
- **Key Fields**:
  - `shipment_id`: string
  - `order_id`: string
  - `suborder_id`: string
  - `seller_uid`: string
  - `customer_uid`: string
  - `carrier_code`: string
  - `tracking_number`: string
  - `status`: string (`LABEL_CREATED`, `PICKED_UP`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`, `DELIVERED`, `EXCEPTION`)
  - `origin_address`: map
  - `destination_address`: map
  - `estimated_delivery`: timestamp
  - `delivered_at`: timestamp (optional)

---

## 7. REAL-TIME MESSAGING & CALLING

### `conversations`
- **Path**: `/conversations/{conversationId}`
- **Security Classification**: `CONFIDENTIAL`
- **Ownership**: Participants (`participant_uids`)
- **Key Fields**:
  - `conversation_id`: string
  - `participant_uids`: array of strings
  - `last_message_text`: string (Encrypted preview)
  - `last_message_sender_uid`: string
  - `last_message_at`: timestamp
  - `is_group`: boolean
  - `created_at`: timestamp

### `messages`
- **Path**: `/messages/{messageId}`
- **Security Classification**: `CONFIDENTIAL`
- **Ownership**: Sender (`sender_uid`) and Conversation Participants
- **Key Fields**:
  - `message_id`: string
  - `conversation_id`: string
  - `sender_uid`: string
  - `text`: string
  - `media_type`: string (`TEXT`, `IMAGE`, `AUDIO`, `DOCUMENT`)
  - `media_url`: string (optional)
  - `delivery_status`: string (`SENT`, `DELIVERED`, `READ`)
  - `created_at`: timestamp

---

## 8. AI STUDIO (GEMINI 3.8 FLASH BACKEND)

### `ai_jobs`
- **Path**: `/ai_jobs/{jobId}`
- **Security Classification**: `CONFIDENTIAL`
- **Ownership**: Requester (`uid`)
- **Key Fields**:
  - `job_id`: string
  - `uid`: string
  - `model`: string (`gemini-3.8-flash`)
  - `task_type`: string (`TRANSLATION`, `PRODUCT_COPY`, `IMAGE_GENERATION`, `HEALTH_RECORD_OCR`)
  - `status`: string (`QUEUED`, `PROCESSING`, `COMPLETED`, `FAILED`)
  - `input_tokens`: number
  - `output_tokens`: number
  - `estimated_cost_usd`: number
  - `created_at`: timestamp
  - `completed_at`: timestamp

---

## 9. PLATFORM GOVERNANCE & INTEGRITY

### `data_integrity_reports`
- **Path**: `/data_integrity_reports/{reportId}`
- **Security Classification**: `CRITICAL`
- **Ownership**: System / Super Admin
- **Key Fields**:
  - `report_id`: string
  - `scanned_collections_count`: number
  - `orphan_orders_found`: number
  - `expired_qr_sessions_purged`: number
  - `expired_health_grants_purged`: number
  - `unreconciled_ledger_entries`: number
  - `integrity_status`: string (`PASS`, `WARNING`, `FAILED`)
  - `executed_at`: timestamp
