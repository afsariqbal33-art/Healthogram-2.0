package com.example.healthogram.core.sre

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Healthogram 2.0 Observability 2.0 & SRE Engine.
 *
 * Implements Section 36, 37, 38 & 39:
 * - Collects real-time telemetry across Application, Database, Payments, and Health Passport.
 * - Enforces Service Level Objectives (SLOs) and calculates Error Budget depletion.
 * - Determines Severity Levels (SEV-0 through SEV-3) for incident escalation.
 * - Deployment gatekeeper: Freezes non-essential deployments when error budget is exhausted.
 */
enum class AlertSeverity {
    SEV_0_CRITICAL,    // Platform-wide outage or clinical/financial security breach
    SEV_1_MAJOR,       // Major customer or checkout outage
    SEV_2_DEGRADED,    // AI, translation, or notification latency
    SEV_3_LIMITED      // Non-critical background task delay
}

data class ServiceSlo(
    val serviceName: String,
    val targetAvailability: Double, // e.g. 99.95%
    val totalRequests: Long = 0,
    val failedRequests: Long = 0
) {
    val currentAvailability: Double
        get() = if (totalRequests == 0L) 100.0 else ((totalRequests - failedRequests).toDouble() / totalRequests.toDouble()) * 100.0

    val isBudgetExhausted: Boolean
        get() = currentAvailability < targetAvailability
}

class HealthogramObservabilityService {

    private val requestCounters = ConcurrentHashMap<String, AtomicLong>()
    private val failureCounters = ConcurrentHashMap<String, AtomicLong>()
    private val securityAlerts = ConcurrentHashMap<String, AtomicLong>()

    // Predefined SLO Targets
    private val targetSlos = mapOf(
        "health_passport" to 99.95,
        "payments" to 99.95,
        "auth" to 99.90,
        "messaging" to 99.80,
        "social_feed" to 99.50,
        "ai_studio" to 99.00
    )

    fun recordRequest(service: String, isSuccess: Boolean) {
        requestCounters.getOrPut(service) { AtomicLong(0) }.incrementAndGet()
        if (!isSuccess) {
            failureCounters.getOrPut(service) { AtomicLong(0) }.incrementAndGet()
        }
    }

    fun recordSecurityIncident(incidentType: String) {
        securityAlerts.getOrPut(incidentType) { AtomicLong(0) }.incrementAndGet()
    }

    fun getSloStatus(service: String): ServiceSlo {
        val total = requestCounters[service]?.get() ?: 0L
        val failed = failureCounters[service]?.get() ?: 0L
        val target = targetSlos[service] ?: 99.0

        return ServiceSlo(
            serviceName = service,
            targetAvailability = target,
            totalRequests = total,
            failedRequests = failed
        )
    }

    /**
     * Determines whether a risky release/migration is permitted based on remaining error budgets.
     */
    fun canDeployRiskyRelease(): Boolean {
        for (service in listOf("health_passport", "payments", "auth")) {
            val slo = getSloStatus(service)
            if (slo.totalRequests > 100 && slo.isBudgetExhausted) {
                return false // Error budget exhausted; freeze risky releases
            }
        }
        return true
    }

    /**
     * Evaluates severity of an incoming alert.
     */
    fun evaluateSeverity(
        isHealthPassportBreach: Boolean,
        isLedgerCorrupted: Boolean,
        isAuthCompletelyDown: Boolean,
        affectedUserPercentage: Double
    ): AlertSeverity {
        return when {
            isHealthPassportBreach || isLedgerCorrupted || (isAuthCompletelyDown && affectedUserPercentage > 50.0) -> {
                AlertSeverity.SEV_0_CRITICAL
            }
            affectedUserPercentage > 20.0 || isAuthCompletelyDown -> {
                AlertSeverity.SEV_1_MAJOR
            }
            affectedUserPercentage > 5.0 -> {
                AlertSeverity.SEV_2_DEGRADED
            }
            else -> {
                AlertSeverity.SEV_3_LIMITED
            }
        }
    }
}
