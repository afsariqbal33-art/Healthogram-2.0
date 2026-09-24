# Healthogram Payment 2.2 Architecture & Security Validation

**Document Version:** 2.2.0  
**Phase:** Step 39 — Financial Security, Provider Abstraction & Idempotency  
**Classification:** Proprietary / Engineering Specification  

---

## 1. Gateway Architecture & Provider Abstraction

Healthogram Payment Architecture decouples high-level commercial transactions from underlying payment gateways using the `PaymentProvider` interface adapter pattern.

```
+-------------------------------------------------------------+
|               Marketplace Checkout Engine                   |
+-------------------------------------------------------------+
                              │
                              ▼
+-------------------------------------------------------------+
|                   PaymentProvider Interface                 |
| - createPayment(PaymentIntentRequest): PaymentIntentResult  |
| - confirmPayment(paymentId, token): PaymentStatus           |
| - handleWebhook(PaymentWebhookPayload): PaymentWebhookResult|
| - refundPayment(RefundRequest): RefundResult                |
| - getPaymentStatus(paymentId): PaymentStatus                |
+-------------------------------------------------------------+
                              │
              ┌───────────────┴───────────────┐
              ▼                               ▼
+-----------------------------+ +-----------------------------+
| SaudiNationalPaymentGateway | | Stripe / International      |
| (Mada, Apple Pay, STC Pay)  | | Gateway (Disabled by default|
+-----------------------------+ +-----------------------------+
```

---

## 2. Payment State Machine

```
[INITIATED]
   │ (Payment Intent created on server)
   ▼
[PENDING / REQUIRES_ACTION] (Customer enters 3DS OTP challenge)
   ├─── (Customer fails challenge / timeout) ───► [FAILED]
   │ (Bank verifies and customer authenticates)
   ▼
[AUTHORIZED]
   │ (Immediate server capture)
   ▼
[PAID / CAPTURED] ◄─────────────────────────────────────────────┐
   │                                                            │
   ├── (Customer requests return / cancel) ──► [REFUND_PENDING] │
   │                                                │           │
   │                                                ▼           │
   ├───► [PARTIALLY_REFUNDED] / [REFUNDED] ─────────┘           │
   │                                                            │
   └── (Customer files dispute with issuing bank)               │
           │                                                    │
           ▼                                                    │
     [CHARGEBACK] ──► (Evidence submitted & won) ───────────────┘
```

---

## 3. Financial Security Mandates

### 3.1 Zero Raw Card Storage (PCI-DSS Level 1 Compliance)
- Healthogram mobile clients and servers **never handle, store, or log raw Primary Account Numbers (PAN), CVVs, or card expiration dates**.
- All card capture is performed within gateway-hosted SDK fields or web views. The server receives only temporary single-use payment tokens (`tok_...`).

### 3.2 Server-Side Authorization
- The client cannot unilaterally mark an order or payment as `PAID`.
- Transitions to `PAID` require signed server-to-server webhook confirmation or server-authenticated capture responses.

### 3.3 Webhook Verification & Replay Protection
1. **HMAC-SHA256 Signature Verification**: Every incoming webhook is checked against the provider webhook secret before parsing.
2. **Amount & Currency Cross-Validation**: Webhooks asserting successful payment must match the exact `orderId`, `amount`, and `currency` recorded during payment intent creation.
3. **Event Idempotency**: Processed `eventId` / `paymentId` tokens are stored in an append-only processed events log (`marketplace_processed_webhook_events`), preventing duplicate capture or credit upon webhook replay.

### 3.4 Duplicate Click & Network Retry Protection
- Client checkout requests include an `idempotencyKey` (UUIDv4).
- If the user double-taps "Pay" or a network retry resends the request, the backend detects the active key and returns the existing payment intent without creating duplicate authorizations.
