# HEALTHOGRAM BACKEND API CATALOG (VERSION 20.0)

This catalog specifies all external-facing and client-accessible backend APIs, remote procedures, error taxonomies, and logging formats across Healthogram.

---

## 1. STANDARDIZED ERROR ARCHITECTURE

Healthogram enforces a unified, strongly typed error response format. Stack traces and internal server details are never returned to clients.

```json
{
  "error": {
    "code": "HEALTH_ACCESS_EXPIRED",
    "message": "The clinical access authorization for this patient has expired.",
    "request_id": "req_a9f82d1c7e6b",
    "timestamp": "2026-09-15T15:30:00Z",
    "retryable": false
  }
}
```

### Standardized Error Codes Catalog
- `AUTH_REQUIRED`: Authentication token is missing, malformed, or expired.
- `PERMISSION_DENIED`: Caller does not possess sufficient RBAC permissions.
- `ACCOUNT_SUSPENDED`: User account has been deactivated or penalized.
- `VERIFICATION_REQUIRED`: Feature requires verified professional credentials (e.g., Doctor badge).
- `HEALTH_ACCESS_REQUIRED`: Clinical data access requested without a valid patient grant.
- `HEALTH_ACCESS_EXPIRED`: The requested health access grant has passed its expiration time.
- `INVALID_REQUEST`: Malformed payload or missing mandatory parameters.
- `VALIDATION_FAILED`: Business logic validation failure (e.g., negative money amount, invalid country code).
- `RATE_LIMITED`: Request ceiling exceeded; client must wait before retrying (`Retry-After` header returned).
- `PAYMENT_FAILED`: Payment processor declined transaction.
- `PAYMENT_PENDING`: Transaction awaiting asynchronous 3D-Secure or bank clearance.
- `PAYMENT_ALREADY_PROCESSED`: Idempotency collision; payment already captured for this transaction.
- `ORDER_NOT_FOUND`: Referenced order identifier does not exist.
- `SELLER_NOT_FOUND`: Referenced marketplace seller identifier does not exist.
- `PRODUCT_NOT_AVAILABLE`: Insufficient inventory or product unapproved.
- `DELIVERY_UNAVAILABLE`: Destination address is outside serviceable logistics zones.
- `AI_LIMIT_REACHED`: User has exhausted allotted Gemini API token quota.
- `TRANSLATION_FAILED`: Translation provider unavailable or target language unsupported.
- `SERVICE_UNAVAILABLE`: Temporary upstream service outage; client may retry with exponential backoff.
- `INTERNAL_ERROR`: Unexpected internal exception logged to server telemetry.

---

## 2. STRUCTURED AUDIT LOGGING SPECIFICATION

All backend operations produce structured logs adhering to strict PII/PHI sanitization rules. Sensitive medical content, plain passwords, card details, and private chats are strictly redacted prior to logging.

```json
{
  "timestamp": "2026-09-15T15:30:00.123Z",
  "service": "healthogram-backend",
  "function": "consumeHealthQrSession",
  "request_id": "req_88f910ab3c",
  "actor_uid_hash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
  "actor_role": "doctor",
  "country_code": "US",
  "resource_id": "hqr_98124a",
  "status": "SUCCESS",
  "duration_ms": 185,
  "error_code": null
}
```

---

## 3. CALLABLE & HTTP API ENDPOINTS

### 3.1 Health Passport Service
- **Endpoint**: `callable:requestHealthAccess`
  - **Purpose**: Verified clinician requests patient consent.
  - **Auth**: Authenticated (`doctor`, `clinic`, `hospital`, `laboratory`).
  - **Input**: `{ patient_uid: string, scopes: string[], purpose: string }`
  - **Output**: `{ grant_id: string, status: "REQUESTED" }`
  - **Rate Limit**: 30 requests / hour / clinician.
  - **Idempotency**: Keyed by `(patient_uid, requester_uid, day)`.

- **Endpoint**: `callable:consumeHealthQrSession`
  - **Purpose**: Exchange single-use QR token for active session.
  - **Auth**: Authenticated clinician.
  - **Input**: `{ opaque_token: string }`
  - **Output**: `{ grant_id: string, patient_summary: object, expires_at: timestamp }`
  - **Rate Limit**: 10 attempts / minute / IP.

### 3.2 Marketplace & Checkout Service
- **Endpoint**: `callable:createMarketplaceOrder`
  - **Purpose**: Atomic checkout across multi-seller cart.
  - **Auth**: Authenticated (`customer`).
  - **Input**: `{ cart_items: array, shipping_address_id: string, idempotency_key: string }`
  - **Output**: `{ order_id: string, suborders: array, total_amount: number, client_secret: string }`
  - **Rate Limit**: 15 checkouts / hour.
  - **Idempotency**: Enforced by `idempotency_key`.

### 3.3 Payments & Webhooks Service
- **Endpoint**: `http:handlePaymentWebhook`
  - **Purpose**: Asynchronous capture confirmation from Stripe/PayPal/M-Pesa.
  - **Auth**: HMAC-SHA256 signature verification in HTTP header (`Stripe-Signature`).
  - **Input**: Raw provider JSON payload.
  - **Output**: `{ received: true }`
  - **Idempotency**: Strict deduplication against `payment_webhook_events/{event_id}`.

### 3.4 AI Studio Service (Gemini 3.8 Flash)
- **Endpoint**: `callable:submitAiJob`
  - **Purpose**: Generate product copy, image prompts, or translation with Gemini.
  - **Auth**: Authenticated user.
  - **Input**: `{ prompt: string, task_type: string, asset_reference_id?: string }`
  - **Output**: `{ job_id: string, status: "QUEUED" }`
  - **Rate Limit**: 20 requests / hour / user tier.
  - **Safety Check**: Automated PII/PHI scrubber rejects any medical records.

### 3.5 Owner Governance Service
- **Endpoint**: `callable:updateEmergencyControls`
  - **Purpose**: Trigger or release platform kill-switches.
  - **Auth**: Platform Owner with active re-authentication challenge.
  - **Input**: `{ emergency_toggles: object, owner_pin: string }`
  - **Output**: `{ success: true, updated_at: timestamp }`
  - **Audit**: High-severity audit event generated immediately.
