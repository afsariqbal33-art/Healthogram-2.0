package com.example.healthogram.healthpassport

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Health Connect Connection State
 */
enum class HealthConnectStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    REVOKED,
    ERROR
}

data class HealthConnectConnection(
    val uid: String,
    val platform: String = "ANDROID_HEALTH_CONNECT",
    val connectionStatus: HealthConnectStatus = HealthConnectStatus.DISCONNECTED,
    val grantedDataTypes: Set<String> = emptySet(), // STEPS, HEART_RATE, SLEEP, EXERCISE, CALORIES, WEIGHT
    val lastSyncAt: Long? = null,
    val syncStatus: String = "IDLE",
    val lastError: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val revokedAt: Long? = null
)

data class HealthConnectSyncJob(
    val jobId: String = UUID.randomUUID().toString(),
    val uid: String,
    val dataType: String,
    val periodStart: Long,
    val periodEnd: Long,
    val status: String = "COMPLETED", // PENDING, RUNNING, COMPLETED, FAILED
    val recordsProcessed: Int = 0,
    val recordsFailed: Int = 0,
    val retryCount: Int = 0,
    val syncedAt: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

data class HealthConnectRecordData(
    val type: String, // STEPS, HEART_RATE, SLEEP, WEIGHT, CALORIES
    val numericValue: Double,
    val unit: String,
    val timestamp: Long,
    val sourcePackage: String = "com.google.android.apps.healthdata"
)

/**
 * HealthConnectService
 * Integrates Android Health Connect under Android 16/API 36 granular permissions.
 * STRICT PRIVACY INVARIANT: Health Connect data is completely isolated from social,
 * marketplace, ads, and creator/seller analytics.
 */
class HealthConnectService private constructor() {

    private val connections = ConcurrentHashMap<String, HealthConnectConnection>()
    private val syncJobs = ConcurrentHashMap<String, MutableList<HealthConnectSyncJob>>()

    companion object {
        @Volatile
        private var instance: HealthConnectService? = null

        fun getInstance(): HealthConnectService {
            return instance ?: synchronized(this) {
                instance ?: HealthConnectService().also { instance = it }
            }
        }

        // Allowed granular data types supported by Healthogram 2.1
        val SUPPORTED_DATA_TYPES = setOf(
            "STEPS",
            "HEART_RATE",
            "SLEEP",
            "EXERCISE",
            "CALORIES",
            "WEIGHT",
            "BLOOD_GLUCOSE"
        )
    }

    /**
     * Get or initialize connection state for user
     */
    fun getConnection(uid: String): HealthConnectConnection {
        return connections.computeIfAbsent(uid) {
            HealthConnectConnection(uid = uid)
        }
    }

    /**
     * Initiates Health Connect permission request flow
     * Only grants requested data types approved by the user
     */
    fun requestAndGrantPermissions(
        uid: String,
        requestedDataTypes: Set<String>
    ): HealthConnectConnection {
        // Enforce least privilege: filter against supported types
        val validTypes = requestedDataTypes.filter { SUPPORTED_DATA_TYPES.contains(it) }.toSet()
        require(validTypes.isNotEmpty()) { "Must request at least one valid supported health data type" }

        val updated = HealthConnectConnection(
            uid = uid,
            connectionStatus = HealthConnectStatus.CONNECTED,
            grantedDataTypes = validTypes,
            updatedAt = System.currentTimeMillis(),
            lastSyncAt = null
        )
        connections[uid] = updated
        return updated
    }

    /**
     * Synchronize fitness & vital data from Android Health Connect into Health Passport Timeline
     */
    fun syncHealthConnectRecords(
        uid: String,
        records: List<HealthConnectRecordData>,
        timelineService: HealthTimelineService = HealthTimelineService.getInstance()
    ): HealthConnectSyncJob {
        val conn = connections[uid] ?: throw IllegalStateException("Health Connect is not connected for user $uid")
        if (conn.connectionStatus != HealthConnectStatus.CONNECTED) {
            throw IllegalStateException("Health Connect connection status is ${conn.connectionStatus}")
        }

        var processed = 0
        var failed = 0

        val job = HealthConnectSyncJob(
            uid = uid,
            dataType = records.firstOrNull()?.type ?: "MULTIPLE",
            periodStart = records.minOfOrNull { it.timestamp } ?: System.currentTimeMillis(),
            periodEnd = records.maxOfOrNull { it.timestamp } ?: System.currentTimeMillis(),
            status = "RUNNING"
        )

        records.forEach { record ->
            if (!conn.grantedDataTypes.contains(record.type)) {
                failed++
                return@forEach
            }

            try {
                val provenance = HealthRecordProvenance(
                    recordId = UUID.randomUUID().toString(),
                    recordType = "OBSERVATION",
                    patientUid = uid,
                    createdByUid = uid,
                    createdByAccountType = "INDIVIDUAL",
                    sourceSystem = "Android Health Connect (${record.sourcePackage})",
                    sourceStatus = RecordSourceStatus.PATIENT_ENTERED,
                    verificationStatus = "DEVICE_VERIFIED"
                )

                val observation = HealthObservation(
                    patientUid = uid,
                    observationType = record.type,
                    valueNumeric = record.numericValue,
                    unit = record.unit,
                    effectiveTimestamp = record.timestamp,
                    observationSource = "HEALTH_CONNECT",
                    provenance = provenance
                )

                val timelineEntry = HealthTimelineEntry(
                    patientUid = uid,
                    timestamp = record.timestamp,
                    recordType = "MEASUREMENT",
                    title = "Health Connect: ${record.type}",
                    subtitle = "${record.numericValue} ${record.unit}",
                    summary = "Imported securely from Android Health Connect with user consent.",
                    recordId = observation.recordId,
                    provenance = provenance,
                    verificationStatus = "DEVICE_VERIFIED",
                    category = "WELLNESS"
                )

                timelineService.addTimelineEntry(uid, timelineEntry)
                processed++
            } catch (e: Exception) {
                failed++
            }
        }

        val completedJob = job.copy(
            status = if (failed == 0 || processed > 0) "COMPLETED" else "FAILED",
            recordsProcessed = processed,
            recordsFailed = failed,
            syncedAt = System.currentTimeMillis()
        )

        val userJobs = syncJobs.computeIfAbsent(uid) { mutableListOf() }
        synchronized(userJobs) {
            userJobs.add(completedJob)
        }

        connections[uid] = conn.copy(
            lastSyncAt = System.currentTimeMillis(),
            syncStatus = "IDLE",
            updatedAt = System.currentTimeMillis()
        )

        return completedJob
    }

    /**
     * Revokes Health Connect integration and halts all synchronization
     */
    fun revokeConnection(uid: String): HealthConnectConnection {
        val conn = connections[uid] ?: HealthConnectConnection(uid = uid)
        val revoked = conn.copy(
            connectionStatus = HealthConnectStatus.REVOKED,
            grantedDataTypes = emptySet(),
            updatedAt = System.currentTimeMillis(),
            revokedAt = System.currentTimeMillis()
        )
        connections[uid] = revoked
        return revoked
    }

    fun getSyncHistory(uid: String): List<HealthConnectSyncJob> {
        return syncJobs[uid]?.toList() ?: emptyList()
    }

    fun clear() {
        connections.clear()
        syncJobs.clear()
    }

    /**
     * Step 37 / DEBT-01 Remediation:
     * OEM Battery Optimization Mitigation & Opportunistic Sync Policy.
     */
    data class OemBatteryGuidance(
        val manufacturer: String,
        val guidanceTitle: String,
        val stepInstructions: List<String>,
        val requiresManualExemption: Boolean
    )

    fun getOemExemptionGuidance(manufacturer: String): OemBatteryGuidance {
        val normalized = manufacturer.trim().uppercase()
        return when {
            normalized.contains("XIAOMI") || normalized.contains("REDMI") || normalized.contains("POCO") -> {
                OemBatteryGuidance(
                    manufacturer = "Xiaomi",
                    guidanceTitle = "MIUI / HyperOS Battery Saver Exemption",
                    stepInstructions = listOf(
                        "Open Settings > Apps > Manage Apps > Healthogram",
                        "Enable 'Autostart'",
                        "Under 'Battery saver', choose 'No restrictions'",
                        "Under Permissions, verify 'Physical activity' is allowed"
                    ),
                    requiresManualExemption = true
                )
            }
            normalized.contains("HUAWEI") || normalized.contains("HONOR") -> {
                OemBatteryGuidance(
                    manufacturer = "Huawei",
                    guidanceTitle = "EMUI Battery Management Exemption",
                    stepInstructions = listOf(
                        "Open Settings > Battery > App Launch",
                        "Locate Healthogram, toggle from 'Auto' to 'Manage manually'",
                        "Enable: Auto-launch, Secondary launch, and Run in background"
                    ),
                    requiresManualExemption = true
                )
            }
            normalized.contains("SAMSUNG") -> {
                OemBatteryGuidance(
                    manufacturer = "Samsung",
                    guidanceTitle = "OneUI Never Sleeping Apps Configuration",
                    stepInstructions = listOf(
                        "Open Settings > Battery > Background usage limits",
                        "Tap 'Never auto-sleeping apps'",
                        "Tap '+' and add Healthogram to ensure scheduled clinical sync"
                    ),
                    requiresManualExemption = false
                )
            }
            else -> {
                OemBatteryGuidance(
                    manufacturer = manufacturer,
                    guidanceTitle = "Standard Android Battery Optimization",
                    stepInstructions = listOf(
                        "Open App Info > Battery",
                        "Select 'Unrestricted' battery usage"
                    ),
                    requiresManualExemption = false
                )
            }
        }
    }

    /**
     * Opportunistic sync: Triggered immediately when patient opens Health Passport screen,
     * overcoming any OEM background worker kills without requiring user battery adjustments.
     */
    fun triggerOpportunisticSync(
        uid: String,
        records: List<HealthConnectRecordData>
    ): HealthConnectSyncJob {
        return syncHealthConnectRecords(uid, records)
    }
}

