package com.example.healthogram.healthpassport.fhir

import com.example.healthogram.healthpassport.*
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class FHIRValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

data class HealthRecordConflict(
    val conflictId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val recordType: String,
    val fieldName: String,
    val existingValue: String,
    val incomingValue: String,
    val sourceExisting: String,
    val sourceIncoming: String,
    val timestampExisting: Long,
    val timestampIncoming: Long,
    val conflictStatus: String = "UNRESOLVED", // UNRESOLVED, RESOLVED_USE_EXISTING, RESOLVED_USE_INCOMING, RESOLVED_KEEP_BOTH
    val resolutionNotes: String? = null,
    val detectedAt: Long = System.currentTimeMillis()
)

data class FHIRImportJob(
    val jobId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val sourceOrganization: String,
    val totalResources: Int,
    val importedCount: Int,
    val conflictCount: Int,
    val status: String = "COMPLETED", // PENDING, PROCESSING, COMPLETED, FAILED
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

/**
 * FHIRInteroperabilityService
 * Orchestrates FHIR R4 validation, conflict-aware importing, and bundle generation.
 */
class FHIRInteroperabilityService private constructor() {

    private val activeConflicts = ConcurrentHashMap<String, MutableList<HealthRecordConflict>>()
    private val importJobs = ConcurrentHashMap<String, MutableList<FHIRImportJob>>()

    companion object {
        @Volatile
        private var instance: FHIRInteroperabilityService? = null

        fun getInstance(): FHIRInteroperabilityService {
            return instance ?: synchronized(this) {
                instance ?: FHIRInteroperabilityService().also { instance = it }
            }
        }
    }

    /**
     * Validate FHIR Resource structure and compliance before export or ingestion
     */
    fun validateResource(resource: FHIRResource, expectedPatientUid: String): FHIRValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (resource.id.isBlank()) {
            errors.add("FHIR resource id must not be blank")
        }

        when (resource) {
            is FHIRPatient -> {
                if (resource.id != expectedPatientUid) {
                    errors.add("Patient ID (${resource.id}) does not match authorized subject ($expectedPatientUid)")
                }
                if (resource.identifier.isEmpty()) {
                    warnings.add("Patient resource does not contain an official identifier")
                }
            }
            is FHIRObservation -> {
                val subjectRef = resource.subject.reference
                if (!subjectRef.endsWith(expectedPatientUid)) {
                    errors.add("Observation subject ($subjectRef) does not match expected patient ($expectedPatientUid)")
                }
                if (resource.code.coding.isEmpty() && resource.code.text.isNullOrBlank()) {
                    errors.add("Observation must have valid terminology coding or display text")
                }
                if (resource.valueQuantity == null && resource.valueString == null) {
                    warnings.add("Observation has neither numeric quantity nor string value")
                }
            }
            is FHIRCondition -> {
                val subjectRef = resource.subject.reference
                if (!subjectRef.endsWith(expectedPatientUid)) {
                    errors.add("Condition subject ($subjectRef) does not match expected patient ($expectedPatientUid)")
                }
            }
            is FHIRAllergyIntolerance -> {
                val patientRef = resource.patient.reference
                if (!patientRef.endsWith(expectedPatientUid)) {
                    errors.add("Allergy patient ($patientRef) does not match expected patient ($expectedPatientUid)")
                }
            }
            is FHIRMedicationRequest -> {
                val subjectRef = resource.subject.reference
                if (!subjectRef.endsWith(expectedPatientUid)) {
                    errors.add("Medication subject ($subjectRef) does not match expected patient ($expectedPatientUid)")
                }
            }
            is FHIRDiagnosticReport -> {
                val subjectRef = resource.subject.reference
                if (!subjectRef.endsWith(expectedPatientUid)) {
                    errors.add("DiagnosticReport subject ($subjectRef) does not match expected patient ($expectedPatientUid)")
                }
            }
        }

        return FHIRValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }

    /**
     * Import an external FHIR Observation with reconciliation & conflict detection
     */
    fun importObservationWithConflictDetection(
        incoming: FHIRObservation,
        targetPatientUid: String,
        sourceOrg: String,
        existingObservations: List<HealthObservation>,
        timelineService: HealthTimelineService
    ): HealthObservation? {
        val validation = validateResource(incoming, targetPatientUid)
        require(validation.isValid) { "Validation failed: ${validation.errors.joinToString()}" }

        val incomingObs = FHIRResourceMapper.fromFHIRObservation(incoming, targetPatientUid, sourceOrg)

        // Check for conflicts: existing observation of same type within close timestamp with different value
        val potentialDuplicate = existingObservations.find { existing ->
            existing.observationType.equals(incomingObs.observationType, ignoreCase = true) &&
            Math.abs(existing.effectiveTimestamp - incomingObs.effectiveTimestamp) < (24 * 60 * 60 * 1000L)
        }

        if (potentialDuplicate != null && potentialDuplicate.valueNumeric != incomingObs.valueNumeric) {
            // Detected conflict! Do NOT silently overwrite. Log conflict for clinical/user reconciliation.
            val conflict = HealthRecordConflict(
                patientUid = targetPatientUid,
                recordType = "OBSERVATION",
                fieldName = "valueNumeric",
                existingValue = "${potentialDuplicate.valueNumeric} ${potentialDuplicate.unit}",
                incomingValue = "${incomingObs.valueNumeric} ${incomingObs.unit}",
                sourceExisting = potentialDuplicate.provenance.sourceSystem,
                sourceIncoming = incomingObs.provenance.sourceSystem,
                timestampExisting = potentialDuplicate.effectiveTimestamp,
                timestampIncoming = incomingObs.effectiveTimestamp
            )
            val conflictList = activeConflicts.computeIfAbsent(targetPatientUid) { mutableListOf() }
            synchronized(conflictList) {
                conflictList.add(conflict)
            }
        }

        // Add to timeline with imported provenance
        val timelineEntry = HealthTimelineEntry(
            patientUid = targetPatientUid,
            timestamp = incomingObs.effectiveTimestamp,
            recordType = "OBSERVATION",
            title = "Imported: ${incomingObs.observationType}",
            subtitle = "${incomingObs.valueNumeric ?: incomingObs.valueString ?: ""} ${incomingObs.unit}".trim(),
            summary = "Imported from $sourceOrg via FHIR R4. Provenance preserved.",
            recordId = incomingObs.recordId,
            provenance = incomingObs.provenance,
            verificationStatus = "IMPORTED_UNVERIFIED",
            category = "CLINICAL"
        )
        timelineService.addTimelineEntry(targetPatientUid, timelineEntry)

        return incomingObs
    }

    /**
     * Build standard FHIR R4 collection bundle for patient
     */
    fun exportFHIRBundle(
        patientUid: String,
        profile: HealthProfile,
        observations: List<HealthObservation> = emptyList(),
        conditions: List<HealthCondition> = emptyList(),
        allergies: List<HealthAllergy> = emptyList()
    ): FHIRBundle {
        val resources = mutableListOf<FHIRResource>()

        // 1. Patient
        resources.add(FHIRResourceMapper.toFHIRPatient(profile))

        // 2. Observations
        observations.forEach { obs ->
            resources.add(FHIRResourceMapper.toFHIRObservation(obs))
        }

        // 3. Conditions
        conditions.forEach { cond ->
            resources.add(FHIRResourceMapper.toFHIRCondition(cond))
        }

        // 4. Allergies
        allergies.forEach { allergy ->
            resources.add(FHIRResourceMapper.toFHIRAllergy(allergy))
        }

        return FHIRBundle(
            timestamp = java.time.Instant.now().toString(),
            total = resources.size,
            entry = resources
        )
    }

    fun getConflicts(patientUid: String): List<HealthRecordConflict> {
        return activeConflicts[patientUid]?.toList() ?: emptyList()
    }

    fun resolveConflict(
        patientUid: String,
        conflictId: String,
        resolution: String,
        notes: String
    ): Boolean {
        val list = activeConflicts[patientUid] ?: return false
        synchronized(list) {
            val idx = list.indexOfFirst { it.conflictId == conflictId }
            if (idx != -1) {
                val current = list[idx]
                list[idx] = current.copy(
                    conflictStatus = resolution,
                    resolutionNotes = notes
                )
                return true
            }
        }
        return false
    }

    fun clear() {
        activeConflicts.clear()
        importJobs.clear()
    }
}
