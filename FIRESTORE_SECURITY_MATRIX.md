# FIRESTORE SECURITY MATRIX (VERSION 20.0)

This matrix defines the exact read, write, administrative, and server-side rules for all Cloud Firestore collections in Healthogram.

| Collection | User Read | User Write | Admin Read/Write | Server-Only Actions | App Check | MFA / Re-auth | Audited |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `users` | Authenticated (Public fields) | Owner (`uid`) unprivileged | Yes (Admin RBAC) | Verified badge, role assignment | Yes | Yes (Password/MFA) | Yes |
| `user_profiles` / `public_profiles` | Authenticated | Owner (`uid`) | Yes (Moderation) | Verified badge assignment | Yes | No | No |
| `private_profiles` | Owner only | Owner (`uid`) | Super Admin only | Legal status flags | Yes | Yes | Yes |
| `user_devices` | Owner only | Owner (`uid`) | Yes (Security) | Token invalidation | Yes | No | Yes |
| `user_sessions` | Owner only | No (Server only) | Yes (Security) | All writes | Yes | No | Yes |
| `user_blocks` | Owner only | Owner (`uid`) | Yes (Moderation) | Policy enforcement | Yes | No | Yes |
| `account_deletion_requests` | Owner only | Owner (`uid`) | Yes (Compliance) | Deletion execution | Yes | Yes (MFA required) | Yes |
| `health_profiles` | Owner / Authorized Grant | Owner (`patientUid`) | No (Forbidden) | None | Yes | Yes | Yes |
| `health_conditions` | Owner / Authorized Grant | Owner / Verified Clinician | No (Forbidden) | None | Yes | Yes | Yes |
| `health_allergies` | Owner / Authorized Grant | Owner / Verified Clinician | No (Forbidden) | None | Yes | Yes | Yes |
| `health_medications` | Owner / Authorized Grant | Owner / Prescribing Doctor | No (Forbidden) | None | Yes | Yes | Yes |
| `health_visits` | Owner / Authorized Grant | Verified Clinical Org | No (Forbidden) | None | Yes | Yes | Yes |
| `health_lab_reports` | Owner / Authorized Grant | Verified Laboratory Org | No (Forbidden) | None | Yes | Yes | Yes |
| `health_access_grants` | Patient / Requester | Patient (Approve/Revoke) | No (Forbidden) | Automated expiration | Yes | Yes (Consent approval) | Yes |
| `health_access_logs` | Patient (`patientUid`) | No (Server only) | Restricted Security | All log entries | Yes | No | Yes |
| `health_qr_sessions` | Patient / Scanner (Single) | Patient (Generate) | No (Forbidden) | Single-use consumption | Yes | No | Yes |
| `posts` | Public (or Followers) | Author (`author_uid`) | Moderation Admin | Feed scoring, strikes | Yes | No | On Strike |
| `post_comments` | Public | Commenter | Moderation Admin | Toxic comment purge | Yes | No | No |
| `stories` | Public / Followers | Author (`author_uid`) | Moderation Admin | 24-hr TTL purge | Yes | No | No |
| `marketplace_products` | Public | Verified Seller (`seller_uid`) | Marketplace Admin | Product compliance approval | Yes | No | Yes |
| `shopping_carts` | Owner (`customer_uid`) | Owner (`customer_uid`) | No | Cart expiration | Yes | No | No |
| `marketplace_orders` | Customer / Suborder Sellers | No (Server only) | Finance Admin | Order creation, payment link | Yes | No | Yes |
| `order_suborders` | Customer / Assigned Seller | No (Server only) | Finance Admin | Payout hold, splits | Yes | No | Yes |
| `marketplace_returns` | Customer / Seller | Customer (Request) | Support Admin | Escrow release | Yes | No | Yes |
| `payment_transactions` | Payer / Payee Seller (Limited) | No (Server only) | Finance Admin | All payment states | Yes | No | Yes |
| `financial_ledger_entries` | No (Forbidden) | No (Forbidden) | Financial Auditors | All ledger posts | Yes | No | Strict |
| `owner_earnings` | Owner only | No (Forbidden) | Owner Only | All ledger increments | Yes | Yes (Owner PIN) | Strict |
| `owner_revenue_entries` | Owner only | No (Forbidden) | Owner Only | All entries | Yes | No | Strict |
| `shipments` | Customer / Seller / Carrier | Carrier / Server | Operations Admin | Geotag updates | Yes | No | Yes |
| `delivery_otp_sessions` | Customer / Carrier | Carrier (Verify OTP) | Operations Admin | OTP generation & check | Yes | No | Yes |
| `conversations` | Participants only | Participants only | No (E2E Encrypted) | Room creation | Yes | No | No |
| `messages` | Participants only | Sender only | No (Private) | Message delivery tracking | Yes | No | On Report |
| `call_sessions` | Call participants | Call participants | No (Private) | WebRTC token creation | Yes | No | No |
| `ai_jobs` | Requester (`uid`) | Requester (Create) | AI Systems Admin | Token metering, cost calc | Yes | No | Yes |
| `notifications` | Owner (`uid`) | No (Server only) | System Admin | FCM dispatch | Yes | No | No |
| `verification_applications` | Applicant | Applicant (Submit) | Verification Officer | Approval / Rejection | Yes | Yes | Yes |
| `verification_documents` | Applicant / Verification | Applicant (Upload) | Verification Officer | Document validation | Yes | No | Yes |
| `admin_audit_logs` | No (Forbidden) | No (Forbidden) | Super Admin / Audit | All admin writes | Yes | No | Strict |
| `platform_configuration` | Authenticated | No (Forbidden) | Owner / Super Admin | Precedence calculations | Yes | Yes (Owner PIN) | Strict |
| `data_integrity_reports` | No (Forbidden) | No (Forbidden) | Super Admin | Scheduled audit runner | Yes | No | Yes |
