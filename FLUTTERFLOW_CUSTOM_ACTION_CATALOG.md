# FLUTTERFLOW CUSTOM ACTION & COMPONENT CATALOG (VERSION 20.0)

This catalog details all audited FlutterFlow client actions, custom logic blocks, and reusable UI components across Healthogram.

---

## 1. REUSABLE UI COMPONENT INVENTORY & DATA CONTRACTS

| Component Name | Primary Data Source | Allowed Account Types | Security Rule Compatibility | Error & Empty State Handling |
| :--- | :--- | :--- | :--- | :--- |
| `AppHeader` | `user_profiles/{uid}` | All Authenticated | Reads public profile; zero sensitive PII. | Shimmer placeholder; fallback initials avatar. |
| `NotificationBell` | `notifications` (unread count) | All Authenticated | Stream limited to unread; badge count < 99. | Zero badge if 0; offline cache. |
| `ProfileHeader` | `public_profiles/{targetUid}` | All Accounts | Public read permitted; private profile isolated. | Skeleton loader; retry button on failure. |
| `VerifiedBadge` | `is_verified`, `badge_type` | Doctor, Clinic, Hospital, Lab, Seller | Strictly read-only; server-controlled badge color. | Hidden if `is_verified == false`. |
| `PostCard` | `posts/{postId}` | All Accounts | Cursor-paginated (20 per page). | Image blurhash preview; moderation strike placeholder. |
| `ReelCard` | `reels/{reelId}` | All Accounts | Direct video CDN stream with auto-loop. | Black canvas + retry icon if video stream fails. |
| `StoryViewer` | `stories/{storyId}` | All Accounts | Excludes expired items (`expires_at > now()`). | Automatic skip to next story if load exceeds 5s. |
| `ProductCard` | `marketplace_products/{id}` | All Accounts | Shows approved items only (`approval_status == 'APPROVED'`). | "Out of Stock" overlay when inventory is 0. |
| `SellerCard` | `marketplace_seller_profiles` | All Accounts | Public business name, rating, country flag. | Generic merchant icon if banner missing. |
| `OrderCard` | `marketplace_orders/{orderId}` | Customer only | Customer can read own orders only. | Status chip (`PENDING`, `SHIPPED`, `DELIVERED`). |
| `MessageBubble` | `messages/{messageId}` | Conversation Participants | Encrypted message text; participant check enforced. | Clock icon (sending) -> Single tick -> Double tick. |
| `CallControls` | `call_sessions/{callId}` | Call Participants | Ephemeral WebRTC signaling controls. | Audio/Video mute toggles with local state mirror. |
| `HealthAccessDialog`| `health_access_grants` | Patient & Doctor | Requires explicit patient confirmation before grant. | "Grant Expired" or "Declined" visual notification. |
| `QRScanner` | Camera Feed | Doctor, Clinic, Hospital, Lab | Scans opaque token only; zero medical text in QR. | Visual viewfinder with torch toggle and sound chirp. |
| `ConsentDialog` | Granular Scope Checkboxes | Individual only | Formulates `health_access_grants` creation. | "Confirm Scopes" disabled until at least 1 scope picked. |
| `PaymentWidget` | `createPaymentIntent` | Customer only | Stripe Elements / SDK integration; zero raw card inputs. | Card decline toast with specific bank decline reason. |
| `DeliveryTracker` | `shipments/{shipmentId}` | Customer & Seller | Timeline showing carrier milestones and GPS checkpoint. | "Awaiting Pickup" state if carrier hasn't scanned. |
| `AIStudioPanel` | `ai_jobs` | All Authenticated | Direct Gemini 3.8 Flash task initiator. | Character counter; token estimate; loading spinner. |
| `TranslationControl`| `translateTextMessage` | All Authenticated | Toggles translated vs original message bubble text. | "Original" / "Translated" tab toggle with language pill. |
| `AdminSidebar` | `admin_roles` | Admin Staff Only | Enforces RBAC permissions per navigation item. | Unauthorized tabs completely hidden from DOM. |
| `OwnerDashboard` | `owner_earnings`, `emergency`| Platform Owner Only | Owner PIN required for any configuration modification. | Real-time stream with audit event ticker. |

---

## 2. AUDITED CUSTOM ACTIONS CATALOG

### Action 1: `initializeHealthQrHandshake`
- **Purpose**: Generates an ephemeral single-use QR token for clinical admission.
- **Inputs**: `patientUid: String`, `expiryMinutes: Integer (default 15)`
- **Outputs**: `qrSessionToken: String`, `expiresAt: DateTime`
- **Authentication**: Individual Account holder only.
- **Error Handling**: Graceful network error toast; prevents generation if account suspended.
- **Backend Function**: `callable:generateHealthQrSession`

### Action 2: `confirmPatientClinicalGrant`
- **Purpose**: Patient grants specific scopes (e.g. Allergies + Medications) to a requesting doctor.
- **Inputs**: `grantId: String`, `approvedScopes: List<String>`, `durationHours: Integer`
- **Outputs**: `isSuccess: Boolean`, `grantStatus: String`
- **Authentication**: Patient (`patientUid == auth.uid`)
- **Error Handling**: Fails with `PERMISSION_DENIED` if doctor is unverified or blocked.
- **Backend Function**: `callable:processPatientConsent`

### Action 3: `executeMarketplaceCheckout`
- **Purpose**: Processes customer cart into multi-seller orders and returns payment intent client secret.
- **Inputs**: `cartItems: List<CartItem>`, `shippingAddressId: String`, `idempotencyKey: String`
- **Outputs**: `orderId: String`, `clientSecret: String`, `totalAmountCents: Integer`
- **Authentication**: Customer role.
- **Error Handling**: Returns `PRODUCT_NOT_AVAILABLE` if stock sold out during review.
- **Backend Function**: `callable:createMarketplaceOrder`

### Action 4: `submitSellerPayoutRequest`
- **Purpose**: Seller requests disbursement of cleared escrow funds to linked bank account.
- **Inputs**: `amountCents: Integer`, `payoutAccountId: String`
- **Outputs**: `payoutRequestId: String`, `remainingBalanceCents: Integer`
- **Authentication**: Verified Seller.
- **Error Handling**: Blocks request if amount > available balance or pending disputes exist.
- **Backend Function**: `callable:processSellerPayout`

### Action 5: `executeEmergencyKillSwitch`
- **Purpose**: Owner triggers or clears instant platform lockdown.
- **Inputs**: `toggleKey: String`, `state: Boolean`, `ownerPin: String`
- **Outputs**: `isSuccess: Boolean`, `auditLogId: String`
- **Authentication**: Platform Owner with active re-auth token.
- **Error Handling**: Locks control for 30 minutes after 3 invalid PIN attempts.
- **Backend Function**: `callable:updateEmergencyControls`

### Action 6: `requestAccountDeletionWorkflow`
- **Purpose**: User initiates complete account erasure under GDPR / right-to-be-forgotten.
- **Inputs**: `reason: String`, `userPasswordProof: String`
- **Outputs**: `scheduledPurgeDate: DateTime`
- **Authentication**: Owner of account.
- **Error Handling**: Rejects if active seller orders are pending fulfillment.
- **Backend Function**: `callable:requestAccountDeletion`
