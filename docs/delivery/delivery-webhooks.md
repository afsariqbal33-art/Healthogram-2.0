# Carrier Delivery Webhooks

## 1. Webhook Ingestion & Signature Verification
Third-party carriers publish real-time milestone events via HTTPS POST webhooks:
- Each webhook request is verified against HMAC-SHA256 signatures using provider-specific webhook signing secrets stored in AI Studio secrets.
- Webhook payloads are hashed with SHA-256 (`payloadHash`) to ensure idempotent processing and prevent duplicate event insertion.

## 2. Webhook Event Schema (`delivery_webhook_events/{eventId}`)
```kotlin
data class DeliveryWebhookEvent(
    val eventId: String,
    val provider: String,
    val providerEventId: String,
    val shipmentId: String,
    val eventType: String,
    val signatureVerified: Boolean,
    val payloadHash: String,
    val receivedAt: Long,
    val processedAt: Long?,
    val processingStatus: String,
    val retryCount: Int,
    val errorCode: String?
)
```

## 3. Resilience & Retry Strategy
1. **Idempotency Check**: If `payloadHash` has already been processed within the last 7 days, the event is acknowledged immediately with HTTP 200 and logged as `DUPLICATE_SKIPPED`.
2. **Exponential Backoff**: Transient database or network timeouts trigger up to 5 retries with exponential delay before escalating to admin notification alerts.
