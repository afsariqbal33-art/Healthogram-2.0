# HEALTHOGRAM 2.2 REGRESSION MATRIX

**Test Run Identifier:** `REG-2.2.0-STG-043`  
**Execution Timestamp:** 2026-09-21  
**Target Build:** Healthogram Android Client v2.2.0-alpha / Firebase Cloud Infrastructure v2.2  
**Test Categories:** Functional, Security, Zero-Trust Privacy, Financial Double-Entry, Concurrency, Emergency Controls  

---

| Test ID | Domain | Feature | Account Type | Preconditions | Test Steps | Expected Result | Actual Result | Status | Severity | Environment | Build Version | Retest Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---: | :---: | :---: | :---: | :---: |
| `REG-001` | Auth | 4-Device Ceiling | Individual | Active user account | Register 4 devices; attempt 5th device login | 5th login enforces max limit policy (evicts oldest or rejects) | Enforced strictly; device count capped at 4 | `VERIFIED` | P0 | Staging / Local | 2.2.0 | `PASSED` |
| `REG-002` | Health | QR Raw Data Check | Individual | Active Health Passport | Generate single-use QR token | Token must not leak patient UID, diagnosis, or lab values | Token is cryptographic hash; 0 raw PHI | `VERIFIED` | P0 | Staging / Local | 2.2.0 | `PASSED` |
| `REG-003` | Health | QR Replay Prevention | Doctor | Valid QR token generated | Doctor 1 scans token; Doctor 2 replays same token | First scan succeeds; second replay fails with `ALREADY_CONSUMED` | Replay rejected; session is single-use | `VERIFIED` | P0 | Staging / Local | 2.2.0 | `PASSED` |
| `REG-004` | Health | Degradation Safety | System | Maintenance Mode ON | Trigger emergency degradation | Zero-Trust App Check and AES-GCM-256 remain active | Security invariants 100% preserved | `VERIFIED` | P0 | Staging / Local | 2.2.0 | `PASSED` |
| `REG-005` | Finance | Webhook Idempotency | Finance | Stripe Webhook endpoint | Post identical payment intent webhook twice | First marks ledger; duplicate discarded without double-credit | Double-entry balance preserved; 0 duplicate | `VERIFIED` | P0 | Staging / Local | 2.2.0 | `PASSED` |
| `REG-006` | Market | Price Authority | Customer | Active cart with 3 items | Client submits spoofed cart total ($10.00 vs $52.25) | Server overrides client input with authoritative price | Spoofed total rejected; exact $52.25 charged | `VERIFIED` | P0 | Staging / Local | 2.2.0 | `PASSED` |
| `REG-007` | Social | PHI Air-Gap | Individual | Social Feed engine | Inspect recommendation schema & query fields | 0 access to clinical diagnoses, labs, or Health IDs | Schema disjoint verified; 0 PHI leakage | `VERIFIED` | P0 | Staging / Local | 2.2.0 | `PASSED` |
| `REG-008` | Chat | Typing Throttle | Individual | Active chat room | Rapidly type 20 characters in 2 seconds | Typing indicators throttled to <= 2 dispatches; 0 Firestore writes | 0 Firestore writes; throttled to RTDB | `VERIFIED` | P1 | Staging / Local | 2.2.0 | `PASSED` |
| `REG-009` | Calling | No-Auto-Record | Doctor | Active teleconsultation | Establish WebRTC session | Recording must remain OFF unless explicit consent given | Recording disabled by default | `VERIFIED` | P0 | Staging / Local | 2.2.0 | `PASSED` |
| `REG-010` | AI | AI Studio Air-Gap | Individual | Creation flow | Generate promotional image and caption | Generative prompt validated against medical dictionaries | Sanitized prompt sent; 0 health data | `VERIFIED` | P1 | Staging / Local | 2.2.0 | `PASSED` |
| `REG-011` | Trans | Translation Fallback| Individual | Multilingual chat | Simulate translation service outage | Communication fails open to original text without crashing | Message delivered in original language | `VERIFIED` | P2 | Staging / Local | 2.2.0 | `PASSED` |
| `REG-012` | Push | PHI Preview Masking | Individual | Lab report generated | Dispatch push notification to patient device | Preview states generic notification; excludes lab numbers | Notification displays "New clinical document" | `VERIFIED` | P0 | Staging / Local | 2.2.0 | `PASSED` |
| `REG-013` | Owner | Global Kill Switch | Owner | Owner Control Panel | Trigger `GLOBAL_APP_DISABLE` | Feature flags and clients evaluate status as OFF | Immediate global shutdown of client actions | `VERIFIED` | P0 | Staging / Local | 2.2.0 | `PASSED` |
| `REG-014` | Journey | Master E2E Flow | Synthetic | Synthetic Individual & Doctor | Execute complete QR generation, scan, and data audit | End-to-end flow executes cleanly; data audit PASS | Audit status: PASS; 0 orphan records | `VERIFIED` | P0 | Staging / Local | 2.2.0 | `PASSED` |
