# HEALTHOGRAM 2.3: COMMUNICATIONS, CREATOR, AI & GOVERNANCE ARCHITECTURE

**Document ID:** HGM-2.3-ARCH-05-COMM-OPS  
**Phase:** Step 48 Architecture & Planning  
**Target Release:** Healthogram Version `2.3.0`  
**Git Branch:** `develop/healthogram-2-3`  
**Timestamp:** 2026-09-22T06:35:00Z  
**Governing Standards:** WebRTC 1.0 (W3C), Signal Protocol Specification, WCAG 2.2 Level AA  

---

## 1. Social Platform & Creator Architecture 2.3

The Healthogram social network provides an open, modern creator ecosystem supporting general social sharing as well as accredited medical thought-leadership.

### A. Core Architectural Principle
* **General Social Content Permitted:** Social content is **not restricted to healthcare topics**. Creators can publish lifestyle, fitness, cultural, educational, and creative media.
* **Unified "+" Creation Entry:** All social creation originates from a single prominent floating action entry (`+`) that expands into:
  1. `Post` (Photo & rich text)
  2. `Reel` (Short-form vertical video)
  3. `Video` (Long-form horizontal video)
  4. `Story` (24-hour ephemeral media)
  5. `Live` (Interactive real-time broadcast)
  *Competing or fragmented creation buttons across other app bars are strictly forbidden.*

### B. Creator System Tiers
* **Normal Creator:** Standard platform user sharing personal stories, photos, and public updates.
* **Professional Creator:** Verified public figures, wellness educators, and accredited medical professionals:
  * Access to **Professional Dashboard** displaying reach, views, engagement rate, follower demographics, and content performance.
  * **Privacy Guarantee:** Audience analytics are aggregated and anonymized; individual follower viewing histories are never exposed.

---

## 2. Secure Communications: Messaging & Calling 2.3

Confidentiality, low latency, and offline resilience govern direct communication between users and healthcare providers.

### A. E2EE Messaging Architecture
* **Encryption Standard:** Signal Protocol (Double Ratchet + Curve25519 + AES-256-CBC).
* **Attachment Security:** Images, audio memos, and clinical documents are encrypted locally on the device using ephemeral symmetric keys. Encrypted blobs are uploaded to Cloud Storage; decryption keys are passed exclusively inside the E2EE message payload.
* **Offline Resilience:** Messages composed while offline are stored in Room (`PendingMessageEntity`) and synchronized sequentially upon network reconnection with automatic deduplication.

### B. Telehealth Calling 2.3 (Calling Provider Abstraction)
* **Provider Abstraction Layer (`TelehealthCallingService`):**
  * Decouples the UI from the underlying WebRTC signaling and media transport engines.
  * Facilitates seamless switching or load-balancing between Google Cloud Coturn TURN servers and enterprise WebRTC gateways.
* **Call Privacy Mandate:** Audio and video calls are **not recorded by default**. If a clinician initiates clinical recording for the patient's medical record, both parties must provide explicit on-screen consent, accompanied by a persistent flashing red indicator and audio chime.

---

## 3. Translation 2.3 Subsystem (Bilingual Medical Safety)

Healthogram bridges English and Arabic communications while guaranteeing clinical accuracy.

### A. Clinical Meaning Protection & Side-by-Side Rendering
* **Non-Certification Disclaimer:** Automated translations are strictly assistive and never represented as certified legal medical interpretations.
* **Dual-Pane Rendering:** In clinical contexts (prescriptions, diagnostic summaries), the original clinician-authored text is displayed prominently alongside the translated text with the mandatory disclaimer:
  *"Automated translation for convenience. Always refer to original clinical text for treatment decisions."*
* **On-Device Offline Translation:** Pre-packaged Google ML Kit translation models for English and Arabic operate entirely offline without network latency or cloud API fees.

---

## 4. AI Studio 2.3 Architecture (Contextual Creation Assist)

AI Studio delivers generative AI assistance tailored strictly to creative and commercial productivity.

### A. Contextual Workflow Integration
* **Placement Rule:** AI Studio tools reside **strictly inside creation and listing trays** (e.g., inside the Post Composer or Seller Product Editor).
* **Navigation Constraint:** AI Studio is **forbidden from occupying top-level app headers or primary navigation tabs**, preserving a clean and uncluttered user interface.

### B. Zero PHI Guarantee
* **Guardrail Enforcement:** Middleware interceptor verifies incoming AI prompt context. Any request originating from `/health_passport/`, clinical consultation threads, or medical bill collections is dropped with HTTP 403.
* **Capabilities Supported:**
  * Contextual post captions and hashtag suggestions.
  * Product photo background removal and lighting enhancement for sellers.
  * Multi-language translation of general seller catalog descriptions.

---

## 5. Notification Subsystem 2.3

Notifications deliver timely, context-sensitive updates while protecting patient confidentiality.

### Privacy-Preserving Channel Design
| Notification Channel ID | Channel Name | Importance | Lock-Screen Privacy Rule |
| :--- | :--- | :---: | :--- |
| `channel_health_emergency` | Emergency Medical Alerts | `HIGH` | Generic text: "Emergency alert received. Tap to unlock." |
| `channel_appointments` | Appointment Reminders | `HIGH` | Generic text: "Upcoming consultation reminder." |
| `channel_orders` | Order & Delivery Status | `DEFAULT` | Displays order status and courier arrival OTP prompt |
| `channel_messages` | E2EE Direct Messages | `HIGH` | Displays sender name; message body hidden if privacy mode active |
| `channel_social` | Social Likes & Comments | `LOW` | Standard batched social notifications |

---

## 6. Admin & Owner Control Panel Architecture

Privileged operations are partitioned to ensure accountability and auditability.

### A. Admin Control Panel (Operational Management)
* **RBAC Scopes:** `VerificationOfficer`, `ClinicalModerator`, `MarketplaceAuditor`, `SupportManager`.
* **Capabilities:** Healthcare practitioner license verification, reported content moderation, dispute resolution, ticket triage.
* **Audit Trail:** Every administrative read and update emits an immutable write-only audit log with administrator UID, IP address, and timestamp.

### B. Owner Control Panel (Strategic Governance & Kill Switches)
* **Owner Sovereignty:** Accessible only to verified platform owners via hardware MFA (FIDO2 / YubiKey).
* **Platform Governance Controls:**
  * Platform commission fee adjustments (default 8.5%).
  * International marketplace toggle (locked to `false` by default).
  * Country rollout enablement and jurisdictional feature policy matrix.
  * **Granular Emergency Kill Switches:** 1-click instantaneous shutdown of any subsystem (Health Passport QR, Marketplace Checkout, Calling, AI Studio, Push Notifications).
