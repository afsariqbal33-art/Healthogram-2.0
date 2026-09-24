package com.example.healthogram.performance

import java.util.UUID

/**
 * HEALTHOGRAM STEP 21: PERFORMANCE MODELS & METRICS ARCHITECTURE
 */

enum class PerformanceStatus {
    GREEN,   // Within target SLO
    YELLOW,  // Approaching degradation threshold, requires monitoring
    RED      // Production-impacting performance degradation / alert triggered
}

enum class PerformanceMetricType(
    val displayName: String,
    val targetP50Ms: Long,
    val targetP95Ms: Long,
    val alertThresholdMs: Long
) {
    APP_COLD_STARTUP("App Cold Launch", 1800, 3000, 3500),
    APP_WARM_STARTUP("App Warm Launch", 600, 1500, 2000),
    PAGE_LOAD("Page Transition", 200, 500, 800),
    FIRESTORE_QUERY("Firestore Read Query", 180, 500, 1000),
    CLOUD_FUNCTION_SYNC("Cloud Function Synchronous", 220, 500, 1000),
    STORAGE_UPLOAD("Media Upload", 800, 2000, 4000),
    STORAGE_DOWNLOAD("Media Download", 300, 800, 1500),
    VIDEO_START("Video Start Latency", 400, 1000, 2000),
    CALL_SETUP("WebRTC Call Setup", 600, 1500, 2500),
    MESSAGING_ACK("Message Send Ack", 120, 300, 600),
    PAYMENT_PROCESSING("Payment Intent & Verification", 800, 1800, 3000),
    TRANSLATION_SYNC("Text Translation Sync", 250, 600, 1200),
    AI_INFERENCE("AI Generation Latency", 1200, 3500, 7000),
    HEALTH_PASSPORT_FETCH("Health Passport Record Fetch", 350, 800, 1500),
    MARKETPLACE_CHECKOUT("Marketplace Checkout Total Calc", 300, 750, 1500),
    FHIR_BUNDLE_PARSE("FHIR R4 Bundle Validation", 400, 900, 1800),
    ADMIN_DASHBOARD_LOAD("Admin Multi-Domain Dashboard Fetch", 450, 1100, 2200),
    OWNER_EARNINGS_CALC("Owner Ledger Aggregation", 380, 950, 2000)
}

data class MetricSample(
    val metricType: PerformanceMetricType,
    val durationMs: Long,
    val isSuccess: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String = "",
    val errorMessage: String? = null
)

data class PercentileSummary(
    val metricType: PerformanceMetricType,
    val sampleCount: Int,
    val p50Ms: Long,
    val p90Ms: Long,
    val p95Ms: Long,
    val p99Ms: Long,
    val errorRatePercent: Double,
    val status: PerformanceStatus
)

data class DomainPerformanceSummary(
    val domainName: String,
    val p50Ms: Long,
    val p95Ms: Long,
    val errorRatePercent: Double,
    val status: PerformanceStatus,
    val trafficRps: Int,
    val dailyCostEstimateUsd: Double
)

/**
 * Asynchronous Long-Running Job State Machine (Section 44)
 */
enum class JobState {
    QUEUED,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    EXPIRED
}

data class AsyncJob(
    val jobId: String = UUID.randomUUID().toString(),
    val jobType: String,
    val state: JobState = JobState.QUEUED,
    val progressPercent: Int = 0,
    val requestedByUid: String,
    val payloadJson: String = "",
    val resultUrl: String? = null,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Emergency Performance Controls for Owner Dashboard (Section 93)
 */
enum class EmergencyPerformanceControl(
    val controlKey: String,
    val title: String,
    val description: String,
    val defaultEnabled: Boolean = false
) {
    DISABLE_LIVE_STREAMING(
        "disable_live_streaming",
        "Disable Live Streaming",
        "Pauses real-time video streaming broadcast under high network stress",
        false
    ),
    DISABLE_EXPENSIVE_AI(
        "disable_expensive_ai",
        "Throttle Expensive AI Tools",
        "Switches generative heavy models to Flash / pauses heavy AI pipelines",
        false
    ),
    REDUCE_RECOMMENDATIONS_REFRESH(
        "reduce_recommendations_refresh",
        "Extend Feed Cache TTL",
        "Lengthens client cache TTL from 2m to 15m to cut Firestore reads by 80%",
        false
    ),
    DISABLE_FLASH_SALES(
        "disable_flash_sales",
        "Pause Flash Sale Bursts",
        "Caps concurrent flash checkout queue to protect payment processing",
        false
    ),
    PAUSE_MARKETPLACE_CHECKOUT(
        "pause_marketplace_checkout",
        "Pause Marketplace Checkout",
        "Temporarily queues new order creation during catastrophic gateway stress",
        false
    ),
    PAUSE_SELLER_ONBOARDING(
        "pause_seller_onboarding",
        "Pause Seller Onboarding",
        "Defers heavy seller document verification during peak traffic",
        false
    ),
    PAUSE_LARGE_EXPORTS(
        "pause_large_exports",
        "Pause Large Data Exports",
        "Queues bulk CSV/PDF generation jobs for off-peak execution",
        false
    ),
    PAUSE_NON_ESSENTIAL_NOTIFICATIONS(
        "pause_non_essential_notifications",
        "Pause Bulk Push Fanout",
        "Suppresses marketing and non-critical push notifications",
        false
    ),
    ENABLE_MAINTENANCE_MODE(
        "enable_maintenance_mode",
        "Platform Maintenance Safeguard",
        "Puts client into read-only graceful degradation mode",
        false
    )
}
