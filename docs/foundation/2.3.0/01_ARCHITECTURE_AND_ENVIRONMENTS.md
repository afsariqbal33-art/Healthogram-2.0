# HEALTHOGRAM 2.3: ARCHITECTURE & ENVIRONMENT SEPARATION BLUEPRINT

**Document ID:** HGM-2.3-FOUNDATION-01-ENV  
**Phase:** Step 49 Foundation Implementation  
**Target Release:** Healthogram Version `2.3.0`  
**Git Branch:** `develop/healthogram-2-3`  
**Timestamp:** 2026-09-22T08:40:00Z  

---

## 1. Environment Isolation & Separation

Healthogram 2.3 enforces total architectural isolation across three environments:

| Attribute | DEVELOPMENT | STAGING | PRODUCTION |
| :--- | :--- | :--- | :--- |
| **Identifier** | `dev` | `staging` | `prod` |
| **Firebase Project** | `healthogram-dev-sandbox` | `healthogram-staging-verify` | `healthogram-prod-secure` |
| **Firestore Database**| `(default)` | `(default)` | `(default)` CMEK Encrypted |
| **Storage Bucket** | `healthogram-dev-assets` | `healthogram-staging-assets` | `healthogram-prod-cmek-vault` |
| **API Base URL** | `https://dev-api.healthogram.internal/api/v2` | `https://staging-api.healthogram.internal/api/v2` | `https://api.healthogram.com/api/v2` |
| **App Check** | Optional (Disabled in dev) | Enforced | Enforced |
| **Play Integrity** | Optional (Disabled in dev) | Enforced | Enforced |
| **Mock Data Allowed** | Yes (Synthetic only) | No | **Strictly Forbidden** |
| **Verbose Logging** | Enabled | Enabled | **Disabled (Zero PHI leakage)** |
| **CMEK Key Ring** | Not configured | Regional GCP KMS | Global Multi-Region KMS |

---

## 2. Repository Architecture & Loose Coupling

Code modules follow a clean separation of concerns:
```
com.example.healthogram/
├── core/
│   ├── foundation/
│   │   ├── env/            # Environment manager and separation
│   │   ├── database/       # Schema versioning & migration registry
│   │   ├── api/            # API versioning & gateway envelopes
│   │   ├── service/        # Service abstractions & provider adapters
│   │   ├── flags/          # Dynamic feature flags & kill switches
│   │   ├── country/        # Country parameterization (8 countries)
│   │   ├── auth/           # Authorization pipeline & session models
│   │   ├── qr/             # Dynamic QR vault token engine (60s TTL)
│   │   ├── audit/          # Immutable write-only audit trail
│   │   ├── idempotency/    # Reusable idempotency manager
│   │   ├── observability/  # Telemetry service & span tracking
│   │   ├── error/          # Standardized client-safe errors
│   │   └── testing/        # Automated test harness & synthetic factories
│   ├── AccountType.kt      # Strict 5-category platform hierarchy
│   ├── CountryConfigService.kt
│   └── FeatureFlagService.kt
├── healthpassport/         # Isolated sovereign clinical vault
├── marketplace/            # Domestic healthcare commerce
├── payments/               # Unified payment processing & Apple/Google Pay
├── finance/                # Double-entry ledger journal
├── social/                 # Social feed & creator tools
└── communication/          # Signal E2EE messaging & WebRTC calling
```
*Health Passport Isolation Rule:* The Health Passport module operates independently of social feeds, advertising networks, and marketplace inventory. It communicates exclusively via authorized consent tokens.
