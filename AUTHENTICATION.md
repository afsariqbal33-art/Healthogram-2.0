# HEALTHOGRAM — AUTHENTICATION ARCHITECTURE

## 1. Identity Providers & Credentials
- **Firebase Authentication**: Primary secure identity broker.
- **Android Credential Manager**: Integrated Google Sign-In, Passkeys, and Phone Auth.
- **Multi-Factor Authentication (MFA)**: Enforced for Healthcare Organization Administrators and Platform Owners.

## 2. Session Lifecycle & Token Claims
- Custom Claims:
  - `account_type`: INDIVIDUAL | DOCTOR | CLINIC | HOSPITAL | LABORATORY
  - `is_verified`: boolean
  - `is_owner`: boolean
  - `is_admin`: boolean
  - `marketplace_role`: NONE | CUSTOMER | SELLER
- Token refresh handles role promotions seamlessly without client credentials exposure.
- Zero-trust token validation across all backend entrypoints.

---

# HEALTHOGRAM — SECURITY & COMPLIANCE

## 1. Zero Trust Architecture
- **Private By Default**: Medical records cannot be read by any user except the patient unless active, non-expired consent exists.
- **No Client Trust for Balances & Privileges**: Balances, payments, refunds, verifications, and admin status are guarded server-side.
- **Restrictive Rules Posture**: No wildcards (`allow read, write: if true;` is permanently prohibited).
- **Prohibited Entity Filtering**: "PHARMACY" is rejected at the Firestore security rule layer and domain model constructors.

## 2. App Check & Safety
- Firebase App Check enabled with Play Integrity on Android.
- Secret keys managed securely through BuildConfig, Secrets Gradle Plugin, and Google Cloud Secret Manager (never hardcoded).
