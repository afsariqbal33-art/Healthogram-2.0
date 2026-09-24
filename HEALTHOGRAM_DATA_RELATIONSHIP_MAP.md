# HEALTHOGRAM DATA RELATIONSHIP MAP (VERSION 20.0)

This document visualizes the complete entity-relationship graph, hierarchical trees, foreign-key relationships, and application-level referential integrity rules across the Healthogram platform.

---

## 1. PRIMARY ACTOR HIERARCHIES

### 1.1 User Entity Hierarchy (`Individual`, `Doctor`, `Clinic`, `Hospital`, `Laboratory`)
```text
USER (users/{uid})
 │
 ├── PROFILE
 │    ├── public_profiles/{uid} (Display Name, Username, Verification Badge)
 │    ├── private_profiles/{uid} (Legal Name, Emergency Phone, Address)
 │    ├── user_settings/{uid} (Language, Timezone, Notification Preferences)
 │    ├── user_devices/{deviceId} (Bound Device Tokens, Max 4 active)
 │    └── user_sessions/{sessionId} (Active Token Fingerprints)
 │
 ├── HEALTH PASSPORT (Isolated Zero-Trust Domain)
 │    ├── health_profiles/{patientUid} (Emergency Info, Blood Group)
 │    ├── health_conditions/{recordId} (Chronic/Acute Diagnoses)
 │    ├── health_allergies/{recordId} (Allergens & Severity)
 │    ├── health_medications/{recordId} (Active/Past Prescriptions)
 │    ├── health_visits/{recordId} (Clinical Consultations)
 │    ├── health_lab_reports/{recordId} (Diagnostic Test Findings)
 │    ├── health_access_grants/{grantId} (Granular Doctor/Org Grants)
 │    ├── health_access_logs/{logId} (Immutable Access Audit Trail)
 │    └── health_qr_sessions/{sessionId} (Ephemeral Single-Use Handshake)
 │
 ├── SOCIAL GRAPH & CONTENT
 │    ├── posts/{postId}
 │    │    ├── post_media/{mediaId}
 │    │    ├── post_likes/{likeId} (uid_postId)
 │    │    └── post_comments/{commentId}
 │    │         └── comment_replies/{replyId}
 │    ├── stories/{storyId} (24-hr TTL)
 │    ├── follows/{followId} (followerUid_followingUid)
 │    └── content_reports/{reportId}
 │
 ├── REAL-TIME COMMUNICATION
 │    ├── conversations/{conversationId}
 │    │    ├── conversation_participants/{id}
 │    │    └── messages/{messageId}
 │    │         ├── message_reactions/{reactionId}
 │    │         └── message_translations/{translationId}
 │    └── call_sessions/{callId}
 │
 ├── MARKETPLACE (Customer Role)
 │    ├── shopping_carts/{cartId} -> marketplace_cart_items/{itemId}
 │    ├── wishlists/{wishlistId} -> wishlist_items/{itemId}
 │    └── marketplace_orders/{orderId} (Customer Order History)
 │
 └── AI STUDIO (Gemini 3.8 Flash)
      ├── ai_jobs/{jobId}
      ├── ai_generated_assets/{assetId}
      └── ai_usage/{usageId}
```

---

### 1.2 Seller Entity Hierarchy (`Seller` Role)
```text
SELLER (sellers/{sellerUid} or marketplace_seller_profiles/{sellerUid})
 │
 ├── CATALOG & INVENTORY
 │    ├── marketplace_products/{productId}
 │    │    ├── product_variants/{variantId}
 │    │    └── marketplace_reviews/{reviewId}
 │    ├── product_inventory/{inventoryId} (Available & Reserved Units)
 │    └── inventory_movements/{movementId} (Immutable Stock Audit)
 │
 ├── FULFILLMENT & ORDERS
 │    ├── order_suborders/{suborderId} (Seller Branch of Parent Order)
 │    │    ├── order_items/{itemId} (Line Items)
 │    │    └── shipments/{shipmentId} (Logistics Tracking & Waybill)
 │    └── marketplace_returns/{returnId} (RMA & Inspection)
 │
 ├── FINANCE & PAYOUTS
 │    ├── marketplace_seller_balances/{sellerUid} (Available / Escrow Pending)
 │    ├── marketplace_seller_ledger/{ledgerId} (Seller-Specific Journal)
 │    ├── marketplace_seller_payout_accounts/{accountId} (Bank/IBAN Info)
 │    └── marketplace_seller_payout_requests/{requestId} (Disbursement Queue)
 │
 └── VERIFICATION & COMPLIANCE
      ├── marketplace_seller_verification/{sellerUid}
      └── marketplace_seller_documents/{docId} (Business Tax ID / Licenses)
```

---

### 1.3 Owner & Platform Governance Hierarchy
```text
OWNER / PLATFORM GOVERNANCE
 │
 ├── CONFIGURATION & FEATURE FLAGS
 │    ├── platform_configuration/{configId} (Global, Country, Role Precedence)
 │    ├── feature_flags/{flagId} (Canary Rollouts & Kill-switches)
 │    └── emergency_controls/{controlId} (System-wide Panic Switches)
 │
 ├── FINANCIAL TREASURY & EARNINGS
 │    ├── owner_earnings/{earningsId} (Aggregated Platform Net Revenue)
 │    ├── owner_revenue_entries/{entryId} (Granular Commissions per Order)
 │    ├── financial_ledger_entries/{entryId} (Master Double-Entry Journal)
 │    ├── owner_withdrawal_requests/{withdrawalId} (Treasury Transfers)
 │    └── financial_reconciliations/{runId} (Daily Gateway Sync Checks)
 │
 └── SYSTEM HEALTH & AUDITING
      ├── data_integrity_reports/{reportId} (Scheduled DB Health Audits)
      ├── security_alerts/{alertId} (Threat Detection Feed)
      ├── security_incidents/{incidentId} (Automated Containment States)
      └── admin_audit_logs/{logId} (Privileged Staff Action Log)
```

---

## 2. CROSS-DOMAIN RELATIONSHIPS & FOREIGN KEY EQUIVALENTS

Because Firestore is a document database without foreign key constraints, the application layer and Cloud Functions enforce strict referential validation:

| Dependent Entity | Target Entity | Foreign Key Field | Validation Invariant |
| :--- | :--- | :--- | :--- |
| `order_suborders` | `marketplace_orders` | `parent_order_id` | Parent order must exist in `PENDING` or `PAID` state before suborders can be created. |
| `order_items` | `marketplace_products` | `product_id` | Product must exist, be `ACTIVE` and `APPROVED`, and have sufficient unreserved inventory. |
| `shipments` | `order_suborders` | `suborder_id` | Suborder must be paid and belonging to the fulfilling seller. |
| `financial_ledger_entries` | `payment_transactions` | `transaction_id` | Ledger entry must reference a verified payment transaction or approved refund. |
| `owner_revenue_entries` | `order_suborders` | `source_id` | Commission entry must equal the calculated percentage of the verified suborder. |
| `health_access_grants` | `users` | `patient_uid`, `requester_uid` | `patient_uid` must be an Individual; `requester_uid` must be a verified clinical account (`doctor`, `clinic`, `hospital`, `laboratory`). |
| `health_conditions` | `users` | `patient_uid` | Can only be created by the patient themselves or an authorized doctor holding an active grant. |
| `messages` | `conversations` | `conversation_id` | Sender must be present in `conversation.participant_uids`. |
| `user_devices` | `users` | `uid` | Total active devices for the user cannot exceed 4. |

---

## 3. APPLICATION-LEVEL INTEGRITY RULES & CASCADE BEHAVIORS

1. **User Account Deletion**:
   - Soft-delete initiated → 30-day grace period in `account_deletion_requests`.
   - Tokens revoked immediately; `user_sessions` and `user_devices` terminated.
   - Public profile hidden from search.
   - Social posts and comments scheduled for removal.
   - Personal health records deleted from `health_private` vaults and Firestore.
   - *Financial & Legal Exemption:* `payment_transactions`, `order_suborders`, and `financial_ledger_entries` are retained under legal hold with user identifiers pseudorandomly hashed for tax compliance.

2. **Health Access Grant Revocation**:
   - Immediate termination of clinical read access.
   - Active clinical sessions referencing the grant are invalidated instantly.
   - Audit event posted to `health_access_logs`.
