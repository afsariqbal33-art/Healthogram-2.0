# HEALTHOGRAM FINAL SECURITY MATRIX (VERSION 20.0)

This matrix defines authorization across all eleven (11) platform actors across the entire Healthogram ecosystem.

## 1. ACTOR TAXONOMY & DEFINITIONS
1. **Individual**: Patient and citizen users owning a personal Health Passport.
2. **Doctor**: Licensed individual medical practitioner.
3. **Clinic**: Outpatient healthcare facility.
4. **Hospital**: Multi-department medical inpatient facility.
5. **Laboratory**: Certified medical testing and diagnostics organization.
6. **Customer**: Marketplace consumer purchasing products.
7. **Seller**: Marketplace merchant managing catalog, inventory, and fulfillment.
8. **Admin**: Platform staff operating under role-based access control (RBAC).
9. **Owner**: Platform master administrator (treasury, system configuration, kill-switches).
10. **Public**: Unauthenticated internet clients / unprivileged external users.
11. **Backend Service**: Trusted Cloud Functions, Cloud Run, and Firebase Admin SDK microservices.

---

## 2. ROLE-BASED ACCESS CONTROL (RBAC) CAPABILITY MAP

| Resource / Collection | Individual | Doctor | Clinic / Hospital | Laboratory | Customer | Seller | Admin | Owner | Public | Backend Service | Server-Only Actions | Audit Required |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **users** | CRU (Own) | CRU (Own) | CRU (Own) | CRU (Own) | CRU (Own) | CRU (Own) | RU (RBAC) | CRUD | R (Limited) | CRUD | Verified badge, role promotion, ban | YES |
| **user_profiles** | CRUD (Own) | CRUD (Own) | CRUD (Own) | CRUD (Own) | CRUD (Own) | CRUD (Own) | RU | CRUD | R | CRUD | Badge type assignment, strikes | NO |
| **health_passports** | CRUD (Own) | R (Grant) | R (Grant) | R (Grant) | DENIED | DENIED | DENIED | DENIED | DENIED | CRUD | Emergency access audit logs | YES |
| **health_conditions** | CRUD (Own) | CR (Grant) | CR (Grant) | DENIED | DENIED | DENIED | DENIED | DENIED | DENIED | CRUD | None | YES |
| **health_allergies** | CRUD (Own) | CR (Grant) | CR (Grant) | DENIED | DENIED | DENIED | DENIED | DENIED | DENIED | CRUD | None | YES |
| **health_medications** | CRUD (Own) | CR (Grant) | CR (Grant) | DENIED | DENIED | DENIED | DENIED | DENIED | DENIED | CRUD | None | YES |
| **health_lab_reports** | R (Own) | R (Grant) | R (Grant) | CR (Issued) | DENIED | DENIED | DENIED | DENIED | DENIED | CRUD | Signature validation | YES |
| **health_access_grants** | CRUD (Own) | R (Own) | R (Own) | R (Own) | DENIED | DENIED | DENIED | DENIED | DENIED | CRUD | TTL Expiration | YES |
| **health_qr_sessions** | CR (Own) | R (Scan) | R (Scan) | R (Scan) | DENIED | DENIED | DENIED | DENIED | DENIED | CRUD | Single-use consumption | YES |
| **posts** | CRUD (Own) | CRUD (Own) | CRUD (Own) | CRUD (Own) | CRUD (Own) | CRUD (Own) | RU (Mod) | CRUD | R | CRUD | Automated spam strike purge | NO |
| **marketplace_products** | R | R | R | R | R | CRUD (Own) | RU (Mod) | CRUD | R | CRUD | Compliance verification | YES |
| **shopping_carts** | CRUD (Own) | DENIED | DENIED | DENIED | CRUD (Own) | DENIED | DENIED | DENIED | DENIED | CRUD | Inventory reserve lock | NO |
| **marketplace_orders** | R (Own) | DENIED | DENIED | DENIED | R (Own) | R (Suborder) | R (Finance) | R | DENIED | CRUD | Creation, payment settlement | YES |
| **order_suborders** | R (Own) | DENIED | DENIED | DENIED | R (Own) | RU (Own) | RU (Finance) | R | DENIED | CRUD | Split calculation, payout hold | YES |
| **payment_transactions** | R (Own) | DENIED | DENIED | DENIED | R (Own) | R (Own) | R (Finance) | R | DENIED | CRUD | All writes, gateway reconciliation | YES |
| **financial_ledger_entries** | DENIED | DENIED | DENIED | DENIED | DENIED | DENIED | R (Finance) | R | DENIED | CRUD | All writes (immutable journal) | YES |
| **owner_earnings** | DENIED | DENIED | DENIED | DENIED | DENIED | DENIED | DENIED | CRUD | DENIED | CRUD | Revenue allocation, ledger sync | YES |
| **shipments** | R (Own) | DENIED | DENIED | DENIED | R (Own) | RU (Own) | RU (Ops) | R | DENIED | CRUD | Carrier tracking sync | YES |
| **conversations** | CRUD (Part) | CRUD (Part) | CRUD (Part) | CRUD (Part) | CRUD (Part) | CRUD (Part) | DENIED | DENIED | DENIED | CRUD | Membership updates | NO |
| **messages** | CRUD (Part) | CRUD (Part) | CRUD (Part) | CRUD (Part) | CRUD (Part) | CRUD (Part) | DENIED | DENIED | DENIED | CRUD | E2EE routing | NO |
| **ai_jobs** | CR (Own) | CR (Own) | CR (Own) | CR (Own) | CR (Own) | CR (Own) | R (Ops) | R | DENIED | CRUD | Token metering, billing calc | YES |
| **notifications** | R (Own) | R (Own) | R (Own) | R (Own) | R (Own) | R (Own) | RU (Ops) | R | DENIED | CRUD | FCM trigger & deduplication | NO |
| **verification_applications** | CR (Own) | CR (Own) | CR (Own) | CR (Own) | DENIED | CR (Own) | RU (Verif) | CRUD | DENIED | CRUD | Decision status updates | YES |
| **admin_audit_logs** | DENIED | DENIED | DENIED | DENIED | DENIED | DENIED | R (SecAud) | R | DENIED | CRUD | All log writes | YES |
| **platform_configuration** | R | R | R | R | R | R | R (Ops) | CRUD | R | CRUD | Precedence compilation | YES |

---

## 3. ABSOLUTE SECURITY CONSTRAINTS

1. **Healthcare Isolation Constraint**:
   Sellers, Customers, and unauthenticated Public users can NEVER query, read, or write any Health Passport record (`health_profiles`, `health_conditions`, `health_allergies`, `health_medications`, `health_lab_reports`, `health_visits`, `health_access_grants`).

2. **Admin Isolation Constraint**:
   Support and Moderation administrators have zero access to Health Passport records or raw financial ledgers. Even Super Admins cannot view medical content unless executing an explicitly audited emergency security incident response.

3. **Financial Immutability Constraint**:
   No client application (whether user, seller, doctor, or owner) can issue direct Firestore `write`, `update`, or `delete` requests against `financial_ledger_entries` or `payment_transactions`. All ledger manipulations are performed exclusively by server-side Cloud Functions using atomic transactions.
