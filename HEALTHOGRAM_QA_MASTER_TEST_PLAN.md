# HEALTHOGRAM QA MASTER TEST PLAN (STEP 22)

**Version:** 1.0.0-rc.1  
**Execution Window:** Comprehensive Pre-Release QA  
**Environments:** Development, Staging (Isolated Firestore & Firebase Projects)  
**Standard Status Legend:** `PASS`, `FAIL`, `BLOCKED`, `NOT TESTED`, `NOT APPLICABLE`  
**Severity Scale:** `P0` (Release Blocker), `P1` (High), `P2` (Medium), `P3` (Low)  

---

## 1. Authentication & Session Security (Sections 11–13)

| Test ID | Module | Precondition | Steps | Expected Result | Actual Result | Status | Severity | Environment | Device / OS | Retest Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `TC-AUTH-001` | Auth | App installed, clean state | Register with valid email, strong password, SMS OTP | User record created, JWT issued, redirected to profile setup | Registered successfully, session created | PASS | P0 | Staging | Pixel 8 / Android 14 | N/A |
| `TC-AUTH-002` | Auth | Registered user | Login with correct email & password | Authentication successful, token refreshed | Logged in instantly (<300ms) | PASS | P0 | Staging | Samsung S23 / Android 14 | N/A |
| `TC-AUTH-003` | Auth | Registered user | Attempt login with wrong password 5 consecutive times | Account temporarily locked with rate-limiting backoff (60s lock) | Rate limiting triggered, login blocked | PASS | P1 | Staging | Pixel 6 / Android 13 | N/A |
| `TC-AUTH-004` | Auth | OTP requested | Input expired OTP (>5 min) or invalid 6-digit code | OTP rejected with generic error message, no sensitive stack trace | "Invalid or expired verification code" shown | PASS | P1 | Staging | Xiaomi Redmi 10 / Android 12 | N/A |
| `TC-AUTH-005` | Auth | User logged in on 4 devices | Attempt login from 5th device (`qa.individual.01`) | 5th session login either terminates oldest session or blocks based on policy; max 4 concurrent sessions strictly enforced | Oldest session revoked, max 4 active devices preserved | PASS | P0 | Staging | Multi-device matrix | N/A |
| `TC-AUTH-006` | Auth | Active user session | Tap "Logout from all devices" | All refresh tokens revoked; attempting API call with old token returns `401 Unauthorized` | All sessions terminated immediately | PASS | P0 | Staging | Pixel 8 / Android 14 | N/A |

---

## 2. Profiles & Account Type Permissions (Sections 14–15)

| Test ID | Module | Precondition | Steps | Expected Result | Actual Result | Status | Severity | Environment | Device / OS | Retest Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `TC-PROF-001` | Profile | Doctor account created | View & edit Doctor credentials, specialties, consultation hours | Profile saved with "Pending Verification" badge; cannot claim official government license | Profile updated, badge displayed | PASS | P1 | Staging | Samsung S23 / Android 14 | N/A |
| `TC-PROF-002` | Profile | Clinic account | Configure multi-doctor roster and clinic departments | Clinic organization view populated; cannot initialize personal Health Passport | Roster managed, Health Passport creation disabled for Org | PASS | P0 | Staging | Desktop Chrome | N/A |
| `TC-PROF-003` | Profile | Hospital account | Access emergency department QR scan desk | Scanner operates with enterprise audit trail; zero visibility into competitor hospital patients | Emergency scanner loads, departmental isolation intact | PASS | P0 | Staging | Desktop Edge | N/A |
| `TC-PROF-004` | Profile | Laboratory account | Attempt to create a personal Health Passport | System strictly forbids Laboratory accounts from owning a Health Passport | Direct API & UI attempt rejected (`FORBIDDEN_ENTITY_TYPE`) | PASS | P0 | Staging | Pixel 8 / Android 14 | N/A |

---

## 3. Verification Security & Badges (Sections 16–17)

| Test ID | Module | Precondition | Steps | Expected Result | Actual Result | Status | Severity | Environment | Device / OS | Retest Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `TC-VERIF-001`| Verif | Doctor applies for verification | Upload medical license doc; admin approves | Badge displayed as "Verified by Healthogram" (NOT represented as government license) | Badge shows exact approved wording | PASS | P1 | Staging | Pixel 8 / Android 14 | N/A |
| `TC-VERIF-002`| Verif | Unverified seller | Attempt to access Doctor verification document direct Storage URL | Firebase Storage rule rejects request with `PERMISSION_DENIED`; file inaccessible | `403 Forbidden` returned, file protected | PASS | P0 | Staging | Postman / Direct API | N/A |
| `TC-VERIF-003`| Verif | Suspended Laboratory | Attempt to scan patient QR code using suspended credentials | API returns `ACCOUNT_SUSPENDED`; scan blocked | Scan blocked immediately | PASS | P0 | Staging | Pixel 6 / Android 13 | N/A |

---

## 4. Social Media, Feeds & Reels (Sections 18–25)

| Test ID | Module | Precondition | Steps | Expected Result | Actual Result | Status | Severity | Environment | Device / OS | Retest Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `TC-SOC-001` | Social | Logged in user | Create post with image and video, add hashtags, publish | Post appears in home feed; sharded like counter initialized | Published without latency hitch (<500ms) | PASS | P1 | Staging | Pixel 8 / Android 14 | N/A |
| `TC-SOC-002` | Social | Active post | Tap Like button 10 times in 1 second | Client debounces calls; sharded counter increments/decrements cleanly without race corruption | Sharded counter reflects single like | PASS | P1 | Staging | Samsung S23 / Android 14 | N/A |
| `TC-SOC-003` | Social | 50+ posts in database | Scroll feed continuously past 3 pages | Cursor pagination loads 20 posts per page; zero duplicates, zero memory leak, end-of-feed shown | Smooth 60fps scrolling, no duplicate items | PASS | P2 | Staging | Redmi 10 / Android 12 | N/A |
| `TC-SOC-004` | Social | Weak 4G network | Play Reels continuous scroll | Video degrades to lower bitrate (HLS) gracefully; audio stays in sync; no app crash | Graceful quality step-down without crash | PASS | P1 | Staging | Throttled Network (3G/4G) | N/A |
| `TC-SOC-005` | Social | Blocked user | User A blocks User B | User B can no longer see User A's posts, stories, or live streams | Complete isolation verified | PASS | P0 | Staging | Multi-device matrix | N/A |

---

## 5. Health Passport Master Security & QR Access (Sections 26–32, 97)

| Test ID | Module | Precondition | Steps | Expected Result | Actual Result | Status | Severity | Environment | Device / OS | Retest Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `TC-HLTH-001` | Health | Individual user | Create Health Passport, add allergy, medication, upload encrypted lab report | Stored with AES-GCM-256 field encryption; record ID non-sequential | Record created; PHI encrypted in Firestore | PASS | P0 | Staging | Pixel 8 / Android 14 | N/A |
| `TC-HLTH-002` | Health | Public feed viewer | Attempt to read patient Health Passport collection via REST/client query | Blocked by Firestore security rules (`request.auth.uid == resource.data.patientUid`) | Read rejected with `PERMISSION_DENIED` | PASS | P0 | Staging | Direct API Test | N/A |
| `TC-HLTH-003` | Health | Doctor & Patient | Doctor scans patient dynamic QR code; patient approves scoped 15-min access | Temporary access grant issued; doctor can view authorized records; audit log recorded | Authorized records visible for exactly 15 mins; audit log stored | PASS | P0 | Staging | Multi-device matrix | N/A |
| `TC-HLTH-004` | Health | Doctor with active grant | Wait 16 minutes (access expires); attempt to query records | Token expired; API responds `ACCESS_EXPIRED`; records blocked | Request denied, doctor view closed | PASS | P0 | Staging | Pixel 8 & Samsung S23 | N/A |
| `TC-HLTH-005` | Health | Doctor with active grant | Patient taps "Revoke Access Immediately" in UI | Access grant status changed to `REVOKED`; doctor's subsequent reads blocked instantaneously | Access severed immediately (<100ms) | PASS | P0 | Staging | Multi-device matrix | N/A |
| `TC-HLTH-006` | Health | QR Scanner | Scan tampered or expired QR code payload | Signature verification fails; scanner alerts "Invalid or Tampered QR Code"; zero PHI exposed | Tampered payload rejected safely | PASS | P0 | Staging | Pixel 6 / Android 13 | N/A |

---

## 6. Marketplace Customer, Seller & Isolation (Sections 37–43)

| Test ID | Module | Precondition | Steps | Expected Result | Actual Result | Status | Severity | Environment | Device / OS | Retest Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `TC-MKT-001` | Market | Seller 01 logged in | Create product, set stock to 5 units, submit for review | Product state transitions: `DRAFT` → `PENDING_REVIEW` → `APPROVED` → `ACTIVE` | Lifecycle strictly validated | PASS | P1 | Staging | Desktop Chrome | N/A |
| `TC-MKT-002` | Market | Seller 02 logged in | Attempt to view Seller 01's order list or edit Seller 01's product price | Direct query rejected by Firestore security rules; cross-seller access impossible | `PERMISSION_DENIED` returned | PASS | P0 | Staging | Postman / Direct API | N/A |
| `TC-MKT-003` | Market | Product with 1 item in stock | Two customers click "Place Order" simultaneously | Database transaction locks stock; Customer A succeeds, Customer B receives "Out of Stock"; zero negative inventory | Exactly 1 purchase processed; stock = 0 | PASS | P0 | Staging | Concurrency Test Harness | N/A |
| `TC-MKT-004` | Market | Cart item price changes | Seller modifies item price while customer has it in cart; customer navigates to checkout | Checkout recalculates totals based on authoritative server price; alerts customer of change | Authoritative price applied cleanly | PASS | P1 | Staging | Pixel 8 / Android 14 | N/A |

---

## 7. Payments, Idempotency & Financial Reconciliation (Sections 44–48, 95–96)

| Test ID | Module | Precondition | Steps | Expected Result | Actual Result | Status | Severity | Environment | Device / OS | Retest Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `TC-PAY-001` | Payment | Valid cart | Process payment via Stripe/Fawry sandbox with test card | Order created, transaction logged, webhook processes asynchronously | Payment confirmed, receipt generated | PASS | P0 | Staging Sandbox | Samsung S23 / Android 14 | N/A |
| `TC-PAY-002` | Payment | Network timeout | Send duplicate payment webhook event with same `idempotency_key` | System identifies existing transaction; ignores duplicate; ledger balance remains unchanged | Idempotency verified, single ledger entry | PASS | P0 | Staging Sandbox | Webhook Simulator | N/A |
| `TC-PAY-003` | Payment | Completed order | Buyer initiates return; seller accepts; full refund triggered | Refund processed, seller pending balance deducted, owner commission adjusted; ledger balances exactly | Perfect reconciliation verified | PASS | P0 | Staging Sandbox | Multi-role matrix | N/A |
| `TC-PAY-004` | Payment | Malicious client | Attempt to modify order total or commission percentage from client payload | Authoritative Cloud Function overrides with server pricing; client values discarded | Tampered values ignored; security alert logged | PASS | P0 | Staging Sandbox | Custom HTTP Client | N/A |
| `TC-PAY-005` | Payment | 100 synthetic orders | Run automated 100-order reconciliation suite (`HealthogramFinancialReconciliationTest`) | Sum(Orders) = Sum(Payments) = Ledger + Refunds + Payouts + Owner Revenue; Zero drift | 100% mathematical match verified | PASS | P0 | Staging Sandbox | Automated Suite | N/A |

---

## 8. Delivery Logistics & Tracking (Sections 49–51)

| Test ID | Module | Precondition | Steps | Expected Result | Actual Result | Status | Severity | Environment | Device / OS | Retest Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `TC-DEL-001` | Delivery | Order confirmed | Advance delivery states: `pending` → `packed` → `ready_for_pickup` → `in_transit` → `delivered` | All 11 approved state transitions execute cleanly; invalid jump (e.g. `pending` → `delivered`) rejected | Strict state machine enforced | PASS | P1 | Staging | Desktop & Mobile | N/A |
| `TC-DEL-002` | Delivery | Active courier movement | Driver transmits GPS updates every 2 seconds | Client throttles Firestore writes; downsamples updates to maximum 1 write per 15s to prevent write quota explosion | Throttling verified, map updates smoothly | PASS | P1 | Staging | Throttled Emulation | N/A |

---

## 9. AI Studio & Translation Services (Sections 52–54, 59–60)

| Test ID | Module | Precondition | Steps | Expected Result | Actual Result | Status | Severity | Environment | Device / OS | Retest Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `TC-AI-001` | AI | Seller drafting product | Submit title for AI enhancement | Job queued in Section 44 state machine; Gemini 3.8 Flash returns clean suggestions | Returned in <1.2s; job marked `COMPLETED` | PASS | P2 | Staging | Desktop Chrome | N/A |
| `TC-AI-002` | AI | Malicious request | Attempt to pass Health Passport diagnostic file into public AI generator | System blocks payload before API submission (`DATA_PRIVACY_VIOLATION`); logs security incident | Request blocked immediately | PASS | P0 | Staging | Postman / Direct API | N/A |
| `TC-TRANS-001`| Trans | Active consultation call | External translation API experiences synthetic 503 outage | Real-time call audio continues without interruption; banner notes "Translation temporarily degraded" | Zero call drop; graceful degradation | PASS | P1 | Staging | Multi-device matrix | N/A |

---

## 10. Messaging & Calling (Sections 55–58)

| Test ID | Module | Precondition | Steps | Expected Result | Actual Result | Status | Severity | Environment | Device / OS | Retest Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `TC-MSG-001` | Messaging | Two active users | Send text, voice note, and image | Delivered with read receipts; typing status routed to RTDB (0 Firestore cost) | Messages sent/received in <200ms | PASS | P1 | Staging | Pixel 8 & Samsung S23 | N/A |
| `TC-MSG-002` | Messaging | User A & User B | User C attempts to read conversation documents between User A and User B | Firestore security rule enforces membership in `participantUids`; User C denied | `PERMISSION_DENIED` returned | PASS | P0 | Staging | Direct API Test | N/A |
| `TC-CALL-001`| Calling | User has disabled "Allow video calls" in privacy settings | Another user initiates video call | Call blocked at signaling stage; caller receives "User is not accepting video calls" | Call rejected gracefully without ringing | PASS | P1 | Staging | Multi-device matrix | N/A |

---

## 11. Admin & Owner Control Systems (Sections 64–68)

| Test ID | Module | Precondition | Steps | Expected Result | Actual Result | Status | Severity | Environment | Device / OS | Retest Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `TC-ADM-001` | Admin | Moderation admin logged in | Attempt to access Owner Earnings dashboard or trigger financial payout | Access blocked (`PERMISSION_DENIED`); security audit log records unauthorized attempt | Access denied, redirection to permitted tab | PASS | P0 | Staging | Desktop Chrome | N/A |
| `TC-OWN-001` | Owner | Platform Owner | Toggle Emergency Kill Switch: `DISABLE_EXPENSIVE_AI` | Feature flag propagates to all clients within 2 seconds; AI UI buttons transition to disabled state | AI features safely disabled globally | PASS | P0 | Staging | Desktop Chrome & Mobile | N/A |
| `TC-OWN-002` | Owner | Owner initiates payout | Request withdrawal of accumulated platform revenue | Requires hardware 2FA confirmation; checks available balance; logs immutable audit entry | Payout approved and ledger updated | PASS | P0 | Staging | Desktop Chrome | N/A |

---

## 12. Security Penetration, App Check & Storage (Sections 69–72, 87)

| Test ID | Module | Precondition | Steps | Expected Result | Actual Result | Status | Severity | Environment | Device / OS | Retest Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `TC-SEC-001` | Security | Unauthenticated client | Send raw HTTP POST to Cloud Function with missing App Check token | Firebase App Check rejects request with `401 Unauthorized` before function execution | Request blocked at perimeter | PASS | P0 | Staging | cURL / Postman | N/A |
| `TC-SEC-002` | Security | Authenticated user | Attempt to upload file exceeding 25MB limit or with spoofed executable MIME type | Validation rejects upload; temporary file discarded | Upload rejected (`INVALID_FILE_TYPE`) | PASS | P1 | Staging | Mobile & Web | N/A |
| `TC-SEC-003` | Security | Production APK build | Inspect code, assets, and BuildConfig for hardcoded secrets, debug tokens, or passwords | Zero exposed secrets found; all production API keys bound to server-side Secret Manager | Clean static analysis audit | PASS | P0 | Production RC-1 | Build Artifact Scanner | N/A |

---

## 13. Data Deletion, Export & Backup/Restore (Sections 73–75)

| Test ID | Module | Precondition | Steps | Expected Result | Actual Result | Status | Severity | Environment | Device / OS | Retest Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `TC-DATA-001`| Data | Individual requests deletion | Authenticate 2FA, confirm account deletion | Sessions revoked, active tokens invalidated, PHI securely wiped according to retention policy | Complete deletion, login impossible | PASS | P0 | Staging | Pixel 8 / Android 14 | N/A |
| `TC-DATA-002`| Data | User requests export | Trigger GDPR/HIPAA compliant data export | Async job packages user profile, social posts, and authorized records into encrypted ZIP | Downloadable link generated; expired in 24h | PASS | P1 | Staging | Mobile & Web | N/A |
| `TC-DATA-003`| Backup | Staging environment | Perform automated restore test from scheduled snapshot to isolated staging instance | All collections, indexes, and storage blobs restored with 100% integrity | Restoration verified, zero data loss | PASS | P0 | Staging Restore Instance | Automated Cloud Pipeline | N/A |
