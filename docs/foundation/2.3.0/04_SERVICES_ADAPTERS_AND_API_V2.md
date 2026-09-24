# HEALTHOGRAM 2.3: SERVICE ABSTRACTIONS, PROVIDER ADAPTERS & API V2

**Document ID:** HGM-2.3-FOUNDATION-04-API  
**Phase:** Step 49 Foundation Implementation  
**Target Release:** Healthogram Version `2.3.0`  

---

## 1. Provider Adapter Architecture

Third-party dependencies are decoupled via explicit adapter interfaces:

```
[BUSINESS SERVICE] 
        │
        ▼
[PROVIDER ADAPTER INTERFACE]
        │
        ├─► [PROVIDER ADAPTER A] (e.g. Stripe, HyperPay)
        ├─► [PROVIDER ADAPTER B] (e.g. Google Pay, Apple Pay)
        └─► [MOCK ADAPTER]       (Local unit tests only)
```

### Decoupled Subsystems
* `PaymentService` $\longrightarrow$ `PaymentProviderAdapter` (`Stripe`, `HyperPay`, `GooglePay`)
* `TranslationService` $\longrightarrow$ `TranslationProviderAdapter` (`OnDeviceMLKit`, `CloudTranslation`)
* `AIService` $\longrightarrow$ `AIProviderAdapter` (`VertexAI`, `OnDeviceGemini`)
* `DeliveryService` $\longrightarrow$ `DeliveryProviderAdapter` (`Aramex`, `SMSA`, `FedEx`)
* `CallingService` $\longrightarrow$ `CallingProviderAdapter` (`WebRTCCoturn`, `EnterpriseRelay`)

---

## 2. API Versioning (`/api/v2`) & Envelopes

To prevent breaking 2.2 clients while exposing enhanced features:
* Version 2.2 clients query `/api/v1/*` routes.
* Version 2.3 clients query `/api/v2/*` routes.
* Both request and response payloads use standardized envelopes (`ApiRequestEnvelope<T>` and `ApiResponseEnvelope<T>`) containing client version, request ID, timestamp, and client-safe error details.

---

## 3. Idempotency Foundation

* Reusable `IdempotencyManager` tracks client-supplied UUID idempotency keys across payments, appointments, and medical writes.
* Duplicate submissions within 24 hours safely return the cached response without double-charging or creating duplicate appointments.
