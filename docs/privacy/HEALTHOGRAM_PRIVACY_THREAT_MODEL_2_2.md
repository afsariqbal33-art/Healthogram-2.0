# HEALTHOGRAM — PRIVACY THREAT MODEL & RISK ASSESSMENT 2.2

**Document Version:** 2.2.0  
**Classification:** Enterprise Threat Modeling & Privacy Engineering Standard  
**Effective Date:** September 20, 2026  
**Methodology:** LINDDUN Privacy Threat Framework & NIST Privacy Framework (NIST IR 8062)  
**Owner:** Privacy Engineering Lead & Chief Information Security Architect

---

## 1. Overview & Scope

The Healthogram 2.2 Privacy Threat Model systematically analyzes privacy risks across all seven core system domains:
1. User Authentication & Multi-Device Sessions
2. Social Feed, Creator Media & Ephemeral Stories
3. Real-Time Messaging, Calling & Presence
4. AI Studio Generative Assistance & Translation
5. Health Passport, Clinical Encounters & Health Connect
6. Marketplace, Seller Center, Payments & Logistics
7. Owner Earnings & Financial Ledger Administration

---

## 2. LINDDUN Threat Analysis & Mitigations

The LINDDUN privacy methodology identifies seven high-level privacy threat categories:
- **L**inking
- **I**dentifying
- **N**on-repudiation (Privacy context: unwanted traceability)
- **D**etecting
- **D**ata Disclosure
- **U**nawareness
- **N**on-compliance

| Threat ID | LINDDUN Category | Threat Description | Attack Vector / Scenario | Severity | Applied Technical Safeguards (Healthogram 2.2) | Residual Risk |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **PRV-01** | Linking | Correlating a patient's public creator profile with their confidential medical conditions | Malicious observer correlates social post timestamps or topics with medical visits | **HIGH** | Database logical separation; zero foreign keys linking social posts to Health Passport tables; medical collections store patient UID without exposing public handle. | **LOW** |
| **PRV-02** | Identifying | Re-identifying patient identity via Health Passport QR code scanning | Attacker captures photo of patient's QR code displayed on screen or printed card | **CRITICAL** | QR codes encode strictly opaque, single-use, 10-minute ephemeral tokens. Zero clinical or demographic data in QR payload. Multi-factor clinician authentication required to resolve token. | **NEGLIGIBLE** |
| **PRV-03** | Data Disclosure | Doctor accessing medical records after clinical encounter has concluded | Attending clinician retains long-term access to patient history without active relationship | **HIGH** | Default 2-hour grant expiration; patient receives real-time access alerts; one-tap instant revocation; clinical scopes strictly limited to approved encounter needs. | **LOW** |
| **PRV-04** | Detecting | Network observer or device sniffer detecting user presence and real-time calling activity | Eavesdropper monitors signaling traffic to deduce sensitive patient-doctor teleconsultation times | **MEDIUM** | DTLS-SRTP end-to-end media encryption; WebRTC signaling docs purged immediately upon call hangup; obfuscated Firestore signaling channels. | **LOW** |
| **PRV-05** | Data Disclosure | Accidental PHI ingestion into generative AI prompts | Creator pastes medical report or prescription text into AI Studio caption generator | **CRITICAL** | Client and server pre-processing sanitizers detect and reject clinical terminology, ICD-10 codes, and drug dosage patterns. Explicit sandbox isolation. | **NEGLIGIBLE** |
| **PRV-06** | Unawareness | User unaware of third-party delivery courier accessing residential address | Marketplace seller or delivery courier exfiltrating customer home address | **MEDIUM** | Addresses stored in restricted `marketplace_addresses` collection; couriers receive access exclusively to active delivery shipments; addresses masked post-delivery completion. | **LOW** |
| **PRV-07** | Data Disclosure | Android OS or rogue third-party app capturing screenshot of Health Passport | Screen-recording malware or task switcher capturing sensitive diagnostic scans | **HIGH** | Mandatory `FLAG_SECURE` enabled on all Health Passport Compose activities and dialogs. Android Photo Picker utilized (zero broad storage permissions). | **LOW** |
| **PRV-08** | Non-compliance | Failure to purge medical records following statutory right-to-be-forgotten request | Account deletion leaves orphan medical records in secondary storage buckets | **CRITICAL** | Automated 30-day deletion worker cascades hard-delete across all user collections and Cloud Storage paths with cryptographic verification receipts. | **NEGLIGIBLE** |

---

## 3. Privacy-by-Design Architectural Guarantees

1. **Private by Default:** All user accounts, Health Passport records, and messaging settings initialize in the most private, least-permissive configuration upon creation.
2. **Explicit Granular Consent:** Data sharing requires affirmative, opt-in consent detailing exact scopes, intended purposes, and expiration durations.
3. **Data Minimization:** APIs, network payloads, and database queries request and return only the minimum data elements necessary to execute the immediate function.
4. **End-to-End Auditability:** The patient possesses full, unfiltered visibility into every system, doctor, or organization that has accessed their health data.
