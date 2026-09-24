package com.example.healthogram.healthpassport.fhir

import com.example.healthogram.healthpassport.*
import java.util.UUID

enum class ExportFormat {
    PDF_SUMMARY,
    STRUCTURED_JSON,
    FHIR_R4_BUNDLE,
    PORTABILITY_ZIP
}

data class HealthDataExportRecord(
    val exportId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val requestedByUid: String,
    val exportFormat: ExportFormat,
    val fileSizeBytes: Long,
    val downloadUrl: String,
    val expiresAt: Long,
    val exportedCategories: List<String>,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * HealthDataExportService
 * Provides human-readable, JSON, FHIR R4, and ZIP export bundles preserving provenance.
 */
class HealthDataExportService private constructor() {

    private val exportAuditLog = mutableListOf<HealthDataExportRecord>()

    companion object {
        @Volatile
        private var instance: HealthDataExportService? = null

        fun getInstance(): HealthDataExportService {
            return instance ?: synchronized(this) {
                instance ?: HealthDataExportService().also { instance = it }
            }
        }
    }

    /**
     * Generate human-readable clinical summary (Text / PDF simulation)
     */
    fun generateHumanReadableSummary(
        profile: HealthProfile,
        conditions: List<HealthCondition>,
        allergies: List<HealthAllergy>,
        medications: List<HealthMedication>,
        recentObservations: List<HealthObservation>
    ): String {
        val sb = StringBuilder()
        sb.append("====================================================\n")
        sb.append("HEALTHOGRAM HEALTH PASSPORT 2.1 - CLINICAL SUMMARY\n")
        sb.append("====================================================\n")
        sb.append("Patient Health ID : ${profile.healthId}\n")
        sb.append("Date of Birth     : ${profile.dateOfBirth}\n")
        sb.append("Blood Group       : ${profile.bloodGroup}\n")
        sb.append("Export Timestamp  : ${java.time.Instant.now()}\n\n")

        sb.append("--- ACTIVE CONDITIONS ---\n")
        if (conditions.isEmpty()) sb.append("No active conditions recorded.\n")
        conditions.forEach { cond ->
            sb.append("• ${cond.conditionName} (${cond.conditionCode}) - Diagnosed: ${cond.diagnosedDate}\n")
        }
        sb.append("\n")

        sb.append("--- KNOWN ALLERGIES & ADVERSE REACTIONS ---\n")
        if (allergies.isEmpty()) sb.append("No allergies on file.\n")
        allergies.forEach { a ->
            sb.append("• ${a.allergen} [Severity: ${a.severity}] - Reaction: ${a.reaction}\n")
        }
        sb.append("\n")

        sb.append("--- CURRENT ACTIVE MEDICATIONS ---\n")
        if (medications.isEmpty()) sb.append("No active medications.\n")
        medications.forEach { m ->
            sb.append("• ${m.medicineName} (${m.dosage}) - ${m.frequency} [Route: ${m.route}]\n")
        }
        sb.append("\n")

        sb.append("--- RECENT OBSERVATIONS & VITALS ---\n")
        if (recentObservations.isEmpty()) sb.append("No observations available.\n")
        recentObservations.forEach { obs ->
            sb.append("• ${obs.observationType}: ${obs.valueNumeric ?: obs.valueString} ${obs.unit} (${obs.provenance.sourceSystem})\n")
        }
        sb.append("\n====================================================\n")
        sb.append("NOTICE: This summary is generated at the explicit request of the patient for portable healthcare continuity.\n")
        return sb.toString()
    }

    /**
     * Creates an export bundle record with 24-hour expiration
     */
    fun createExportRecord(
        patientUid: String,
        requestedByUid: String,
        format: ExportFormat,
        categories: List<String>,
        simulatedSizeBytes: Long = 48500L
    ): HealthDataExportRecord {
        require(patientUid == requestedByUid) { "Unauthorized: cannot export data of another patient" }

        val expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000L) // 24 hours
        val record = HealthDataExportRecord(
            patientUid = patientUid,
            requestedByUid = requestedByUid,
            exportFormat = format,
            fileSizeBytes = simulatedSizeBytes,
            downloadUrl = "https://health-export.healthogram.io/secure/exp_${UUID.randomUUID()}.${when (format) {
                ExportFormat.PDF_SUMMARY -> "pdf"
                ExportFormat.STRUCTURED_JSON -> "json"
                ExportFormat.FHIR_R4_BUNDLE -> "fhir.json"
                ExportFormat.PORTABILITY_ZIP -> "zip"
            }}",
            expiresAt = expiresAt,
            exportedCategories = categories
        )

        synchronized(exportAuditLog) {
            exportAuditLog.add(record)
        }
        return record
    }

    fun getExportHistory(patientUid: String): List<HealthDataExportRecord> {
        synchronized(exportAuditLog) {
            return exportAuditLog.filter { it.patientUid == patientUid }
        }
    }
}
