# HEALTHOGRAM QA TEST ACCOUNT MATRIX

This document outlines the standard synthetic test accounts used across Development, Staging, and Pre-production QA environments. **Zero real patient or production financial credentials are used.**

---

| Test ID | Account Type | Email | Phone | Verification Status | Country | Permissions / Roles | Primary QA Purpose | Expected Access Scope |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `qa.individual.01` | Individual | `qa.individual.01@test.healthogram.com` | `+971501112233` | Verified Individual | UAE | `USER_BASIC`, `HEALTH_PASSPORT_OWNER`, `MARKETPLACE_BUYER` | Core patient flows, Health Passport records, Social, Buyer checkout | Own Health Passport, own orders, public social feed, peer chat |
| `qa.individual.02` | Individual | `qa.individual.02@test.healthogram.com` | `+971501112234` | Unverified | UAE | `USER_BASIC`, `HEALTH_PASSPORT_OWNER` | Negative security tests, cross-user isolation, peer blocking | Cannot view or modify `qa.individual.01` Health records or orders |
| `qa.doctor.01` | Doctor | `qa.doctor.01@test.healthogram.com` | `+971502223344` | Verified by Healthogram | UAE | `DOCTOR_PRO`, `QR_SCANNER`, `AUTHORIZED_HEALTH_ACCESS` | QR scanning, temporary patient access, consultation calling | Authorized patient records only during active session window; no direct marketplace seller tools |
| `qa.doctor.02` | Doctor | `qa.doctor.02@test.healthogram.com` | `+971502223345` | Pending Verification | UAE | `DOCTOR_UNVERIFIED` | Verification workflow, unapproved scanner lockout tests | Scanner disabled; Health Passport access requests strictly rejected |
| `qa.clinic.01` | Clinic | `qa.clinic.01@test.healthogram.com` | `+971503334455` | Verified by Healthogram | UAE | `CLINIC_ORG`, `MULTI_DOCTOR_MGMT`, `QR_DESK_SCANNER` | Clinic department management, appointment scheduling, device locks | Clinic practitioner association, check-in desk scanner; cannot own personal Health Passport |
| `qa.hospital.01` | Hospital | `qa.hospital.01@test.healthogram.com` | `+971504445566` | Verified by Healthogram | UAE | `HOSPITAL_ENTERPRISE`, `ER_SCANNER`, `DEPT_ADMIN` | Inpatient admission, ER patient authorization, multi-device enterprise | Departmental access logs, emergency QR scans; zero access to unrelated clinic/hospital data |
| `qa.laboratory.01` | Laboratory | `qa.laboratory.01@test.healthogram.com` | `+971505556677` | Verified by Healthogram | UAE | `LAB_PROVIDER`, `REPORT_INGESTION`, `QR_AUTHORIZED_ONLY` | Lab test upload, patient lab report ingestion, barcode verification | Upload lab results to authorized patient record; CANNOT own personal Health Passport or view medical history |
| `qa.laboratory.02` | Laboratory | `qa.laboratory.02@test.healthogram.com` | `+971505556678` | Suspended | UAE | `LAB_SUSPENDED` | Sanctions & suspension enforcement | All upload and scanner APIs blocked with `PERMISSION_DENIED` |
| `qa.customer.01` | Marketplace Buyer | `qa.customer.01@test.healthogram.com` | `+971506667788` | Active | UAE | `CUSTOMER_ROLE`, `CART_CHECKOUT`, `REVIEWS` | E-commerce browse, cart revalidation, payment sandbox, delivery tracking | Own cart, own order history, own shipping addresses; cannot see seller backend |
| `qa.seller.01` | Marketplace Seller | `qa.seller.01@test.healthogram.com` | `+971507778899` | Verified Seller | UAE | `SELLER_ROLE`, `CATALOG_MGMT`, `INVENTORY_MGMT`, `PAYOUT_VIEW` | Product lifecycle, inventory decrement, order fulfillment, payout tracking | Own inventory, own products, own earnings; CANNOT see Seller 02 or patient medical data |
| `qa.seller.02` | Marketplace Seller | `qa.seller.02@test.healthogram.com` | `+971507778800` | Verified Seller | Saudi Arabia | `SELLER_ROLE`, `CATALOG_MGMT`, `INVENTORY_MGMT` | Cross-seller data isolation, multi-currency pricing | Scoped strictly to Seller 02 products & orders; zero access to Seller 01 records |
| `qa.admin.01` | Operations Admin | `qa.admin.01@test.healthogram.com` | `+971508889900` | Admin Verified | UAE | `ADMIN_OPERATIONS`, `USER_SUPPORT` | User account support, verification ticket triage | Operations views; strictly DENIED access to Owner Earnings and Health Passports |
| `qa.moderator.01`| Moderation Admin | `qa.mod.01@test.healthogram.com` | `+971508889901` | Admin Verified | Global | `ADMIN_MODERATION` | Social feed report triage, comment hide/delete | Social moderation queue; strictly DENIED financial ledger or Health Passport access |
| `qa.finance.01` | Finance Admin | `qa.finance.01@test.healthogram.com` | `+971508889902` | Admin 2FA Verified | UAE | `ADMIN_FINANCE`, `SETTLEMENT_AUDIT` | Settlement review, payment reconciliation, chargeback tracking | Financial dispute data; strictly DENIED medical health records and Owner root actions |
| `qa.healthsec.01`| Health Security Admin | `qa.sec.01@test.healthogram.com` | `+971508889903` | Admin 2FA Verified | UAE | `ADMIN_HEALTH_SECURITY`, `AUDIT_INSPECTION` | Access grant audit log inspection, emergency breach containment | Cryptographic access logs only (zero raw PHI plaintext access) |
| `qa.owner.01` | Owner / Platform Master | `qa.owner.01@test.healthogram.com` | `+971509990011` | Owner Hardware Token | Global | `PLATFORM_OWNER`, `ALL_OWNER_GATES` | Platform configuration, emergency kill switches, earnings withdrawal | Full owner control panel, commission rates; zero direct raw clinical health record snooping |

---

### Non-Negotiable Constraint Compliance
- **Account Categories Present**: Individual, Doctor, Clinic, Hospital, Laboratory.
- **Marketplace Roles Present**: Customer, Seller.
- **Forbidden Account Types Excluded**: Pharmacy, Medical Store, Medicine Company, Wholesale/Supplier, Equipment Manufacturer are NOT present.
- **Data Safety**: All emails use `@test.healthogram.com`; all phone numbers use reserved mock series.
