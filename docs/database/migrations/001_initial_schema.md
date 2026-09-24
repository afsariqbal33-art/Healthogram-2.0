# DATABASE MIGRATION 001: INITIAL CORE SCHEMA

**Migration ID:** `001_initial_schema`  
**Target Environment:** Development, Staging, Production  
**Author:** Healthogram Database Architect  

---

## 1. Overview
Establishes the foundational Firestore collections for the 5 approved Healthogram account categories:
1. Individual
2. Doctor
3. Clinic
4. Hospital
5. Laboratory

## 2. Collections Created
* `/users/{uid}`: Core authentication, email, role, phone, and metadata.
* `/user_sessions/{sessionId}`: Track active device sessions (max 4 per user).
* `/profiles/{uid}`: Public professional and individual profile attributes.
* `/verification_requests/{requestId}`: Professional and facility credential verification.

## 3. Backward Compatibility & Rollback Plan
* **Backward Compatibility**: Fully additive; no existing collections.
* **Rollback Procedure**: Drop collections using Firebase CLI emulator or script `scripts/rollback_migration_001.js`.
