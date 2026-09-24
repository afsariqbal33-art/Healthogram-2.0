package com.example.healthogram.core.foundation.observability

import com.example.healthogram.core.foundation.env.AppEnvironment
import com.example.healthogram.core.foundation.env.EnvironmentManager
import com.example.healthogram.core.foundation.error.ErrorCategory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Step 49: Healthogram 2.3 Observability & Standardized Telemetry Engine.
 *
 * Privacy Invariant: Telemetry captures performance, operational counts, and latency.
 * Protected Health Information (PHI) is strictly forbidden in telemetry payloads.
 */
data class TelemetrySpan(
    val requestId: String,
    val operationId: String,
    val serviceName: String,
    val environment: AppEnvironment = EnvironmentManager.get().environment,
    val startTimeMs: Long = System.currentTimeMillis(),
    var endTimeMs: Long? = null,
    var status: String = "SUCCESS",
    var latencyMs: Long = 0,
    var errorCategory: ErrorCategory? = null
) {
    fun finish(success: Boolean, errorCategory: ErrorCategory? = null) {
        val end = System.currentTimeMillis()
        endTimeMs = end
        latencyMs = end - startTimeMs
        this.status = if (success) "SUCCESS" else "FAILURE"
        this.errorCategory = errorCategory
    }
}

class TelemetryService private constructor() {
    private val serviceCounters = ConcurrentHashMap<String, AtomicLong>()
    private val errorCounters = ConcurrentHashMap<String, AtomicLong>()

    fun startSpan(requestId: String, operationId: String, serviceName: String): TelemetrySpan {
        serviceCounters.computeIfAbsent(serviceName) { AtomicLong(0) }.incrementAndGet()
        return TelemetrySpan(
            requestId = requestId,
            operationId = operationId,
            serviceName = serviceName
        )
    }

    fun recordError(serviceName: String, category: ErrorCategory) {
        val key = "$serviceName:${category.name}"
        errorCounters.computeIfAbsent(key) { AtomicLong(0) }.incrementAndGet()
    }

    fun getInvocationCount(serviceName: String): Long {
        return serviceCounters[serviceName]?.get() ?: 0L
    }

    fun getErrorCount(serviceName: String, category: ErrorCategory): Long {
        return errorCounters["$serviceName:${category.name}"]?.get() ?: 0L
    }

    companion object {
        val instance: TelemetryService by lazy { TelemetryService() }
    }
}
