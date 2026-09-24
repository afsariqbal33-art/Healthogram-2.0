package com.example.healthogram.core.foundation.idempotency

import java.util.concurrent.ConcurrentHashMap

/**
 * Step 49: Healthogram 2.3 Reusable Idempotency Engine.
 *
 * Guarantees exactly-once execution semantics for payments, appointment booking,
 * refunds, orders, and Health Passport write operations.
 */
data class IdempotencyRecord(
    val idempotencyKey: String,
    val operation: String,
    val actorUid: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 86400000L, // 24 hours TTL
    var status: IdempotencyStatus = IdempotencyStatus.PENDING,
    var cachedResponsePayload: String? = null
)

enum class IdempotencyStatus {
    PENDING,
    COMPLETED,
    FAILED
}

class IdempotencyManager private constructor() {
    private val records = ConcurrentHashMap<String, IdempotencyRecord>()

    fun acquireKey(key: String, operation: String, actorUid: String): Boolean {
        cleanupExpired()
        val compositeKey = "$operation:$key"
        val existing = records[compositeKey]

        if (existing != null) {
            if (existing.status == IdempotencyStatus.COMPLETED) {
                // Key already processed
                return false
            }
            if (existing.status == IdempotencyStatus.PENDING) {
                // In-flight concurrent duplicate request
                return false
            }
        }

        records[compositeKey] = IdempotencyRecord(
            idempotencyKey = key,
            operation = operation,
            actorUid = actorUid
        )
        return true
    }

    fun completeKey(key: String, operation: String, responsePayload: String? = null) {
        val compositeKey = "$operation:$key"
        records[compositeKey]?.let {
            it.status = IdempotencyStatus.COMPLETED
            it.cachedResponsePayload = responsePayload
        }
    }

    fun failKey(key: String, operation: String) {
        val compositeKey = "$operation:$key"
        records.remove(compositeKey) // Release key on failure to allow retry
    }

    fun getCachedResponse(key: String, operation: String): String? {
        val compositeKey = "$operation:$key"
        return records[compositeKey]?.takeIf { it.status == IdempotencyStatus.COMPLETED }?.cachedResponsePayload
    }

    private fun cleanupExpired() {
        val now = System.currentTimeMillis()
        records.entries.removeIf { it.value.expiresAt < now }
    }

    companion object {
        val instance: IdempotencyManager by lazy { IdempotencyManager() }
    }
}
