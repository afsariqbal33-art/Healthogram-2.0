# ADR-018: ZERO-TRUST HEALTH PASSPORT SERVICE BOUNDARY & ACCESS GOVERNANCE

**Status:** ACCEPTED  
**Date:** 2026-09-17  
**Deciders:** Healthcare Security Architect, Privacy Officer, Principal Architect  

---

## 1. Context
Patient Health Passport records contain sensitive personal health information (diagnoses, prescriptions, lab results, allergies). Any leakage into social algorithms, ad networks, or unauthenticated endpoints constitutes a catastrophic breach.

## 2. Problem
How do we ensure that no generic application service can ever access, query, or infer sensitive clinical records without an explicit, cryptographically verifiable patient grant?

## 3. Options Considered
- **Option A: Convention-Based Separation:** Developer guidelines instructing engineers not to query health collections from social or marketplace controllers.
- **Option B: Dedicated Health Passport Service Boundary with Mandatory Policy Enforcement (Selected):** Encapsulate all clinical operations behind `HealthPassportService`. Enforce strict Firestore security rules (`allow read, write: if false;` for general callers). Require single-use ephemeral QR tokens (15-min TTL), patient approval handshakes, and immutable audit logging.

## 4. Decision
Adopt **Option B: Dedicated Health Passport Service Boundary with Mandatory Policy Enforcement**. Bar Health Passport data from feeds, search, generic notifications, and third-party AI models.

## 5. Reason
Architectural constraints must be enforced programmatically by the runtime and database layers, not merely left to developer discipline.

## 6. Tradeoffs & Consequences
- Cross-domain workflows requiring health data must initiate a formal consent handshake.
- Ephemeral QR tokens expire automatically; consultations cannot reuse expired tokens.
