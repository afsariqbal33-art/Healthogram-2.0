# HEALTHOGRAM — DATABASE STRATEGY & FIRESTORE SCHEMA

## 1. Multi-Domain Isolation Architecture

Data domains are strictly partitioned to ensure medical records never leak into social or public query spaces:

```
/users/{uid}                           -> Public/Authenticated profile metadata
/health_passports/{patientUid}        -> High-security private health passport
/health_passports/{uid}/records/{id}  -> Granular medical records
/health_passports/{uid}/consents/{doc}-> Active patient permissions
/health_audit_logs/{logId}            -> Immutable access audit records
/verifications/{appId}                -> Private verification submissions
/user_devices/{deviceId}              -> Registered device sessions & permissions
/social_posts/{postId}                -> Social media feed, reels & comments
/marketplace_products/{productId}     -> Marketplace items & inventory
/marketplace_orders/{orderId}         -> Customer orders & fulfillment status
/payment_transactions/{txnId}         -> Secure payment ledger (Server write-only)
/feature_flags/{flagId}               -> Owner controlled feature toggles
/owner_ledger/{entryId}               -> Owner platform financial ledger
/admins/{adminUid}                    -> Granular administrative authorizations
```

## 2. Core Collections Detail

### `users/{uid}`
- `uid`: string (Firebase Auth UID)
- `email`: string
- `phoneNumber`: string
- `displayName`: string
- `username`: string (unique indexed)
- `photoUrl`: string?
- `accountType`: string (`INDIVIDUAL`, `DOCTOR`, `CLINIC`, `HOSPITAL`, `LABORATORY`)
  - **CONSTRAINT**: Value `PHARMACY` is strictly rejected by Firestore rules and client parsers.
- `countryCode`: string (ISO 3166-1 alpha-2)
- `languageCode`: string (ISO 639-1)
- `currencyCode`: string (ISO 4217)
- `bio`: string
- `isActive`: boolean
- `isSuspended`: boolean
- `isVerified`: boolean
- `verificationStatus`: string (`NOT_STARTED`, `PENDING`, `UNDER_REVIEW`, `APPROVED`, `REJECTED`, `EXPIRED`, `SUSPENDED`)
- `isProfessional`: boolean
- `marketplaceRole`: string (`NONE`, `CUSTOMER`, `SELLER`)
- `createdAt`: timestamp
- `updatedAt`: timestamp
- `lastLoginAt`: timestamp

### `health_passports/{patientUid}`
- `healthId`: string (unique masked ID, e.g. `HG-8924-US`)
- `patientUid`: string
- `bloodGroup`: string
- `knownAllergies`: array<string>
- `chronicConditions`: array<string>
- `emergencyContactName`: string
- `emergencyContactPhone`: string
- `isLocked`: boolean
- `createdAt`: timestamp
- `updatedAt`: timestamp

### `health_audit_logs/{logId}`
- `auditId`: string
- `patientUid`: string
- `accessedByUid`: string
- `accessedByName`: string
- `organizationName`: string
- `requesterAccountType`: string
- `requestedScope`: string
- `purpose`: string
- `timestamp`: timestamp
- `isGranted`: boolean
- `denialReason`: string?
