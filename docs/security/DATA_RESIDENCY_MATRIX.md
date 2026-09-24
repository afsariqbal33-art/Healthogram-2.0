# HEALTHOGRAM GLOBAL DATA RESIDENCY & PRIVACY CLASSIFICATION MATRIX

**Classification Policy:** Mandatory Architectural Law  
**Status:** ENFORCED  
**Version:** 3.0.0  

---

## 1. Data Classification Tiers

1. **TIER 1: Highly Sensitive Clinical (Health Passport, Prescriptions, Lab Panels, Diagnoses)**
   - *Residency Requirement:* **STRICTLY LOCAL/REGIONAL**. Under no circumstances may data be transferred, cached, or mirrored outside the sovereign regulatory boundary of the patient's domicile.
   - *Encryption:* AES-GCM-256 field-level client encryption before storage + TLS 1.3 in flight.
   - *Leakage Guard:* Banned from generic search indexes, AI prompt contexts, social algorithms, and ad engines.

2. **TIER 2: Sensitive Personal & Identity (Verification Documents, National IDs, Biometric Signatures)**
   - *Residency Requirement:* **REGIONAL SECURE VAULT**. Accessible only to authorized compliance reviewers.
   - *Storage:* Isolated private Cloud Storage bucket (`verification_private/{uid}/`). Expired/deleted upon verification completion.

3. **TIER 3: Financial & Transactions (Ledger Entries, Escrow Custody, Payouts, Bank Accounts)**
   - *Residency Requirement:* **REGIONAL BANKING DOMAIN**. Retained per local statutory financial accounting requirements (minimum 7 years in immutable double-entry format).

4. **TIER 4: Private Communications (1-on-1 Messages, Voice Memos, Call Signaling)**
   - *Residency Requirement:* End-to-End Encrypted (E2EE) channels. Ephemeral RTC signaling discarded immediately after session termination.

5. **TIER 5: Global Public (Social Posts, Public Reels, Creator Profiles, Product Catalogs)**
   - *Residency Requirement:* **GLOBAL MULTI-REGION**. Replicated across Cloud CDN edge nodes for lowest read latency.

---

## 2. Complete Domain Residency & Regulatory Mapping

| Domain / Collection | Data Tier | Storage Technology | Location Scope | Regulatory Framework | Cross-Border Transfer Permitted? |
|---|---|---|---|---|---|
| `health_passports` | TIER 1 | Firestore Sovereign Instance | National / GCC Region | Oman MOH, Saudi PDPL | **STRICTLY PROHIBITED** |
| `health_prescriptions` | TIER 1 | Firestore Sovereign Instance | National / GCC Region | Oman MOH, Saudi PDPL | **STRICTLY PROHIBITED** |
| `health_lab_reports` | TIER 1 | Cloud Storage Private Vault | National / GCC Region | Oman MOH, Saudi PDPL | **STRICTLY PROHIBITED** |
| `health_access_grants` | TIER 1 | Firestore Sovereign Instance | National / GCC Region | Oman MOH, Saudi PDPL | **STRICTLY PROHIBITED** |
| `verification_documents` | TIER 2 | Cloud Storage Private Vault | Regional Sovereign Bucket | National AML/KYC | Prohibited without court warrant |
| `financial_ledger_entries` | TIER 3 | Immutable Firestore Ledger | Regional Multi-Region | Central Bank Regulations | Audit mirroring permitted |
| `payout_accounts` | TIER 3 | Stripe / Regional PSP Vault | Regional Gateway | PCI-DSS Level 1 | Prohibited (Tokenized only) |
| `direct_messages` | TIER 4 | Firestore E2EE Messages | Multi-Region Encrypted | General Data Protection | Encrypted ciphertext only |
| `webrtc_signaling` | TIER 4 | Realtime Database Ephemeral | Edge Point of Presence | Local Telecom Regulations | Ephemeral (Zero retention) |
| `social_posts` | TIER 5 | Multi-Region Firestore + CDN | Global Edge Anycast | Terms of Service Public | **YES (Public CDN)** |
| `social_reels` | TIER 5 | Multi-Region Firestore + CDN | Global Edge Anycast | Terms of Service Public | **YES (Public CDN)** |
| `marketplace_products` | TIER 5 | Multi-Region Firestore + CDN | Global Edge Anycast | Consumer Protection Law | **YES (Public Catalog)** |
| `ai_prompts_public` | TIER 5 | Vertex AI / Gemini API | Regional Vertex Endpoint | AI Ethics Policy | Redacted metadata only (NO PHI) |
| `analytics_operational` | TIER 5 | Cloud Firestore Rollups | Regional Analytics Pool | Privacy Shield / Pseudonymized | Pseudonymized aggregated only |
