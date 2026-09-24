# HEALTHOGRAM 2.2.0 CONTROLLED PRODUCTION ROLLOUT POLICY

**Document ID:** HGM-PROD-POL-ROLLOUT  
**Effective Date:** 2026-09-21  
**Target Release:** Healthogram 2.2.0 (versionCode 20201)  
**Governance:** Architecture Board, Healthcare Compliance, Security & Operations  

---

## 1. Rollout Policy Principles
1. **Evidence-Driven Progression:** Rollout expansion is strictly gated by verified telemetry, zero P0/P1 incidents, and financial reconciliation. Download count increases alone NEVER justify progression.
2. **Deterministic Reversibility:** Any phase or feature must be capable of immediate rollback (< 5 minutes via Remote Config kill switch or Play Console halt).
3. **Multi-Dimensional Segmentation:** Rollouts are partitioned across:
   - **Rollout Tier / Percentage**
   - **Jurisdiction / Country Code**
   - **Account Category** (Individual, Doctor, Clinic, Hospital, Laboratory)
   - **Explicit User & Healthcare Partner Allowlists**
   - **Domain Feature Flags**

---

## 2. Progressive Rollout Stages Definition

| Stage | Target Population | Target Countries | Account Scope | Duration / Gate Evaluation | Entry Gate Requirements |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Stage A: Canary & Internal** | Allowlisted QA, Employees & Pilot Providers (0%) | Global Allowlist | All 5 Categories | Min 48h pre-prod staging | RC approval, 0 P0/P1, clean automated suites |
| **Stage B: Conservative Wave (CURRENT)** | **5% of eligible users** | Conservative Tier: `US, CA, GB, SA, AE, EG, IN` | All 5 Categories | Min 24h active live monitoring | Google Play ingestion, clean signature, baseline Vitals |
| **Stage C: Initial Expansion** | **15% of eligible users** | Conservative Tier | All 5 Categories | Min 48h active live monitoring | Crash rate < 0.1%, ANR < 0.05%, 0 ledger discrepancies |
| **Stage D: Broad Expansion** | **50% of eligible users** | Conservative Tier + Secondary Wave (`FR, DE, IT, ES, AU`) | All 5 Categories | Min 72h active live monitoring | P95 latency < 2.5s, 0 Health Passport security anomalies |
| **Stage E: High Scale** | **75% of eligible users** | All approved production countries | All 5 Categories | Min 48h active live monitoring | Cloud Function 5xx < 0.1%, Stripe webhook replay clean |
| **Stage F: Full Availability** | **100% General Availability** | All approved production countries | All 5 Categories | Permanent GA monitoring | Executive sign-off, statutory compliance audit clean |

*Note: Percentages are dynamic and managed in `PRODUCTION_REMOTE_CONFIG_v2.2.0.json` and Google Play Console Staged Rollout controls.*

---

## 3. Account Category Rollout Boundaries
Healthogram strictly recognizes only **FIVE** core account categories:
1. **Individual** (Patient / Health Consumer)
2. **Doctor** (Licensed Physician / Specialist)
3. **Clinic** (Outpatient Healthcare Facility)
4. **Hospital** (Inpatient / Medical Center Organization)
5. **Laboratory** (Diagnostic & Clinical Testing Center)

*Prohibition:* No "Pharmacy", "Medical Store", or unauthorized wholesale healthcare accounts exist as main categories. Marketplace activities are strictly bound to separate roles: **Customer** or **Seller** (Individual Seller / Business Seller).

---

## 4. International Marketplace Constraint Rule
* **Policy Rule:** `international_marketplace_enabled = false` is **STRICTLY ENFORCED** in production.
* **Jurisdictional Boundary:** Marketplace transactions are localized strictly country-by-country.
* **Operational Constraint:** A patient in Country A can only browse, cart, and purchase healthcare products dispatched by verified Sellers operating within Country A. Cross-border checkout, shipping, customs, or cross-currency fulfillment are blocked at the API, database rules, and UI layer.

---

## 5. Circuit Breaker & Rollback Triggers

| Trigger Event | Severity | Immediate Operational Action | Authority |
| :--- | :---: | :--- | :--- |
| **Health Passport Decryption Failure / PHI Exposure** | **P0** | Activate `emergency_maintenance_mode = true`, halt Play rollout | Security Lead / SRE |
| **Double-Entry Ledger Skew (`debits != credits`)** | **P0** | Activate `payments_processing_killswitch = true` | Finance Lead |
| **Crash Rate > 1.0% or ANR > 0.40%** | **P0** | Halt Google Play Staged Rollout immediately | Release Manager |
| **WebRTC Teleconsultation Failure > 5%** | **P1** | Activate `webrtc_calling_killswitch = true` | Ops Lead |
| **Marketplace Order Processing Glitch** | **P1** | Activate `marketplace_killswitch = true` | Ops Lead |
| **AI Studio Hallucination / Latency Spike** | **P2** | Activate `ai_generation_killswitch = true` | Ops Lead |
EOFThe action produced the following result:

The command exited with code 0.
Stdout:

Stderr: