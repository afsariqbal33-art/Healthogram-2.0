# HEALTHOGRAM — SYSTEM ARCHITECTURE

## 1. Modular High-Level Topology

Healthogram adheres to a modern, decoupled Clean Architecture pattern on Android:

```
+---------------------------------------------------------------+
|                       Presentation Layer                     |
|  (Jetpack Compose M3 UI, ViewModels, StateFlow, Navigation)   |
+---------------------------------------------------------------+
                                |
                                v
+---------------------------------------------------------------+
|                         Domain Layer                          |
| (Use Cases, Authorization Guards, Security Rules, Validations)|
+---------------------------------------------------------------+
                                |
                                v
+---------------------------------------------------------------+
|                          Data Layer                           |
| (Repositories, Firestore Clients, Room Cache, Remote APIs)    |
+---------------------------------------------------------------+
```

## 2. Core Subsystems

1. **Authentication & Identity (`/auth`, `/core`)**
   - Firebase Auth + Credential Manager.
   - Centralized `AccountType`: INDIVIDUAL, DOCTOR, CLINIC, HOSPITAL, LABORATORY.
   - Prohibited category assertion: Any input containing "PHARMACY" is rejected at parsing.

2. **Health Passport Subsystem (`/healthpassport`)**
   - Private, isolated medical record vault.
   - Dynamic QR session ticket generation (15 min expiry, zero raw data).
   - Patient consent engine with revocable, time-bound grants.
   - Append-only immutable audit trail.

3. **Verification Engine (`/verification`)**
   - Country-aware rules: US (NPI/Board), GB (GMC/CQC), AE (DHA/MOHAP), SA (SCFHS), Global.
   - Verification badge required to unlock clinical QR scanning.

4. **Multi-Device Session Guard (`/devices`)**
   - Normal users: Hard maximum of 4 active devices.
   - Healthcare organizations: 4 (Basic), 8 (Premium), Configurable (Enterprise).
   - Device permission policies (Management, Social, Marketplace, Messages, Calls).

5. **Ecommerce Marketplace (`/marketplace`)**
   - Strict separation of Customer and Seller roles.
   - Regulated medical device validation flags.

6. **Payment Abstraction Layer (`/payments`)**
   - Country-aware routing (Stripe, Apple Pay, Google Pay, Mada, Aramex).
   - Automated fee splitting, VAT handling, and chargeback protection.

7. **Universal Translation Subsystem (`/translation`)**
   - Consent-based translation for text, voice, and live video captions.
   - Mandatory disclaimer: Automated translation is not certified clinical interpretation.

8. **AI Studio (`/aistudio`)**
   - Modes: Creator, Seller, Healthcare Org.
   - Hard ethical boundary: Rejects any attempt to process raw Health Passport data or fabricate treatments.

9. **Owner Control & Ledger (`/owner`)**
   - Global emergency kill switch and maintenance controls.
   - Granular feature flags (by country and account type).
   - Immutable financial transaction ledger.
