package com.example.healthogram.core.events

import java.util.UUID

/**
 * Healthogram 2.0 Standard Domain Event Specification.
 *
 * All asynchronous domain events across the platform MUST implement this contract.
 * Strictly decoupled: Payloads refer to aggregate references or non-PHI data.
 * Under NO circumstances may unencrypted clinical Health Passport records be emitted in event payloads.
 */
data class DomainEvent(
    val eventId: String = "evt_" + UUID.randomUUID().toString().replace("-", ""),
    val eventType: String,
    val eventVersion: Int = 1,
    val aggregateType: String,
    val aggregateId: String,
    val actorUid: String,
    val country: String = "OM",
    val occurredAt: Long = System.currentTimeMillis(),
    val correlationId: String = "corr_" + UUID.randomUUID().toString().substring(0, 12),
    val causationId: String = "caus_" + UUID.randomUUID().toString().substring(0, 12),
    val idempotencyKey: String = "idemp_" + UUID.randomUUID().toString(),
    val payloadReference: String = "",
    val schemaVersion: String = "2.0",
    val metadata: Map<String, String> = emptyMap()
) {
    companion object {
        // Event Type Constants
        const val ORDER_PLACED = "marketplace.order.placed"
        const val ORDER_SUBORDER_CREATED = "marketplace.suborder.created"
        const val PAYMENT_SUCCEEDED = "payments.intent.succeeded"
        const val PAYMENT_FAILED = "payments.intent.failed"
        const val ESCROW_CREDITED = "finance.escrow.credited"
        const val SELLER_PAYOUT_INITIATED = "finance.seller.payout_initiated"
        const val POST_CREATED = "social.post.created"
        const val POST_LIKED = "social.post.liked"
        const val REEL_PUBLISHED = "social.reel.published"
        const val HEALTH_QR_CONSUMED = "health.qr.consumed"
        const val HEALTH_ACCESS_GRANTED = "health.access.granted"
        const val HEALTH_ACCESS_REVOKED = "health.access.revoked"
        const val DELIVERY_STATE_CHANGED = "delivery.status.changed"
        const val VERIFICATION_SUBMITTED = "verification.request.submitted"
        const val VERIFICATION_APPROVED = "verification.request.approved"
        const val SEARCH_INDEX_REQUESTED = "search.index.requested"
        const val MEDIA_TRANSCODE_REQUESTED = "media.transcode.requested"
        const val NOTIFICATION_DISPATCH_REQUESTED = "notification.dispatch.requested"
    }
}

/**
 * Interface for consuming Domain Events.
 */
fun interface DomainEventListener {
    suspend fun onEvent(event: DomainEvent): Result<Unit>
}

/**
 * Durable Idempotency Store interface.
 */
interface IdempotencyStore {
    suspend fun hasProcessed(idempotencyKey: String): Boolean
    suspend fun markProcessed(idempotencyKey: String, eventId: String, ttlSeconds: Long = 86400)
}

/**
 * In-memory / local implementation of IdempotencyStore.
 */
class InMemoryIdempotencyStore : IdempotencyStore {
    private val processedKeys = mutableMapOf<String, Long>()

    override suspend fun hasProcessed(idempotencyKey: String): Boolean {
        synchronized(processedKeys) {
            val expiry = processedKeys[idempotencyKey] ?: return false
            if (System.currentTimeMillis() > expiry) {
                processedKeys.remove(idempotencyKey)
                return false
            }
            return true
        }
    }

    override suspend fun markProcessed(idempotencyKey: String, eventId: String, ttlSeconds: Long) {
        synchronized(processedKeys) {
            processedKeys[idempotencyKey] = System.currentTimeMillis() + (ttlSeconds * 1000)
        }
    }

    fun clear() {
        synchronized(processedKeys) {
            processedKeys.clear()
        }
    }
}

/**
 * Domain Event Dispatcher for Healthogram 2.0.
 */
class DomainEventDispatcher(
    private val idempotencyStore: IdempotencyStore = InMemoryIdempotencyStore()
) {
    private val listeners = mutableMapOf<String, MutableList<DomainEventListener>>()

    fun subscribe(eventType: String, listener: DomainEventListener) {
        synchronized(listeners) {
            listeners.getOrPut(eventType) { mutableListOf() }.add(listener)
        }
    }

    suspend fun publish(event: DomainEvent): Boolean {
        // Enforce Idempotency Check
        if (idempotencyStore.hasProcessed(event.idempotencyKey)) {
            return false // Duplicate skipped
        }

        val targetListeners = synchronized(listeners) {
            val direct = listeners[event.eventType] ?: emptyList()
            val wildcard = listeners["*"] ?: emptyList()
            direct + wildcard
        }

        var allSucceeded = true
        for (listener in targetListeners) {
            val result = listener.onEvent(event)
            if (result.isFailure) {
                allSucceeded = false
            }
        }

        if (allSucceeded) {
            idempotencyStore.markProcessed(event.idempotencyKey, event.eventId)
        }

        return allSucceeded
    }
}
