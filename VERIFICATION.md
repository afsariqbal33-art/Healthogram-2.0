# HEALTHOGRAM — VERIFICATION FRAMEWORK (STEP 07)

## 1. Multi-Tier Compliance Model
Verification is country-aware, role-governed, and mandatory before clinical QR scanning is enabled:

| Account Type | Mandatory Verification Documents | Badge Display | Scanner Authorization |
| :--- | :--- | :--- | :--- |
| **INDIVIDUAL** | National ID / Government Passport | Cyan Checkmark | **Denied** (Individuals cannot scan) |
| **DOCTOR** | National ID + Medical Practice License | Cyan Checkmark | **Authorized** (Active & unexpired) |
| **CLINIC** | Authorized Rep National ID + Commercial Registration + Clinic Operating License | Gold Shield | **Authorized** (Active & unexpired) |
| **HOSPITAL** | Administrator National ID + Enterprise Commercial Registration + Hospital License | Gold Shield | **Authorized** (Active & unexpired) |
| **LABORATORY** | Director National ID + Business Registration + Clinical Lab Accreditation | Green Cross | **Authorized** (Active & unexpired) |
| **PHARMACY** | **STRICTLY PROHIBITED** (Cannot register or submit verification) | N/A | N/A |

Marketplace accounts (Customer and Seller) remain completely distinct and are never verified through clinical healthcare pathways.

---

## 2. Dynamic Country Requirements Engine
Requirements are dynamically resolved per jurisdiction rather than hard-coded:
- **United States (US)**: State Medical Board License + NPI Registry validation + CLIA / Joint Commission accreditation.
- **United Kingdom (GB)**: GMC registry verification + CQC accreditation + Companies House filing.
- **United Arab Emirates (AE)**: DHA / MOHAP / DoH Healthcare Facility Licensing + Emirates ID.
- **Saudi Arabia (SA)**: SCFHS (Saudi Commission for Health Specialties) verification + CBAHI / MOH licenses + Commercial Registration.

---

## 3. Storage Isolation & Zero-Trust Security
- **Private Storage Path**: `verification_private/{uid}/{applicationId}/{documentId}`
- **Storage Rules**: 
  - Strictly non-public (`/public/` usage is strictly forbidden).
  - Client reads are strictly limited to the applicant owner or authorized verification reviewers / managers (`request.auth.token.verificationRole`).
  - Supported formats: PDF, JPG, JPEG, PNG, HEIC (Max 25MB).
- **Document Number Masking**: Full national ID numbers or license numbers are never persisted in plaintext. Only the last 4 digits (`documentNumberLast4`) are stored.

---

## 4. Verification Lifecycle States
Applications progress through controlled zero-trust states:
1. `NOT_STARTED` — Initial unverified baseline state.
2. `DRAFT` — Application created; applicant uploading required credentials.
3. `SUBMITTED` — Mandatory documents validated; entered into verification review queue.
4. `UNDER_REVIEW` — Locked and under review by authorized compliance reviewer.
5. `ADDITIONAL_INFORMATION_REQUIRED` — Clarification or clearer scan requested from applicant.
6. `VERIFIED` (`APPROVED`) — Verified badge activated; 365-day validity timestamp set; scanner unlocked for clinical roles.
7. `REJECTED` — Application denied with standardized reason code; internal notes remain private.
8. `SUSPENDED` — Temporary administrative hold pending audit.
9. `EXPIRED` — License expired; automated cron removes badge and locks scanner.
10. `REVOKED` — Permanent administrative invalidation of credentials.

---

## 5. Healthcare Scanner Gating (Section 34)
The Health Passport QR scanner is strictly gated by the verification engine:
- **Eligibility Criteria**:
  1. Account Type is `DOCTOR`, `CLINIC`, `HOSPITAL`, or `LABORATORY`.
  2. `verification_status == VERIFIED` and `verified_badge == true`.
  3. `verification_expires_at > currentTime`.
  4. Account is **not** suspended, revoked, or restricted.
- **Strict Separation from Step 06**: Verification grants scanner eligibility, but does **not** grant unilateral Health Passport data access. Patient consent via dynamic QR session grant remains mandatory for every clinical access event.

---

## 6. Immutable Audit Trail
All verification lifecycle events (`APPLICATION_CREATED`, `DOCUMENT_UPLOADED`, `APPLICATION_SUBMITTED`, `REVIEW_STARTED`, `APPLICATION_APPROVED`, `APPLICATION_REJECTED`, `BADGE_ACTIVATED`, `BADGE_REMOVED`, `VERIFICATION_SUSPENDED`, `VERIFICATION_REVOKED`) are logged to `verification_audit_logs/{logId}`. Clients are strictly forbidden from writing or altering audit logs.

