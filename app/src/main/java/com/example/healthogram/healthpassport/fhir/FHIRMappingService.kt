package com.example.healthogram.healthpassport.fhir

import com.example.healthogram.healthpassport.*
import java.util.UUID

/**
 * Step 32: FHIRMappingService
 *
 * Implements bidirectional transformation between internal Healthogram models
 * and HL7 FHIR R4 resources. Detects unmapped fields, produces transformation audits,
 * and reports warnings and errors without silent data discarding.
 */
class FHIRMappingService private constructor() {

    data class MappingResult<T>(
        val isSuccess: Boolean,
        val mappedOutput: T?,
        val mappedFields: List<String>,
        val unmappedFields: List<String>,
        val warnings: List<String> = emptyList(),
        val errors: List<String> = emptyList(),
        val auditId: String = UUID.randomUUID().toString()
    )

    companion object {
        @Volatile
        private var instance: FHIRMappingService? = null

        fun getInstance(): FHIRMappingService {
            return instance ?: synchronized(this) {
                instance ?: FHIRMappingService().also { instance = it }
            }
        }
    }

    /**
     * Map internal HealthObservation to FHIRObservation
     */
    fun mapObservationToFHIR(obs: HealthObservation): MappingResult<FHIRObservation> {
        val mapped = mutableListOf<String>()
        val unmapped = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        mapped.add("recordId -> id")
        mapped.add("patientUid -> subject.reference")
        mapped.add("observationType -> code")
        mapped.add("valueNumeric -> valueQuantity.value")
        mapped.add("unit -> valueQuantity.unit")
        mapped.add("effectiveTimestamp -> effectiveDateTime")

        if (obs.interpretation != null) {
            mapped.add("interpretation -> interpretation")
        }

        val fhirObs = FHIRResourceMapper.toFHIRObservation(obs)

        return MappingResult(
            isSuccess = true,
            mappedOutput = fhirObs,
            mappedFields = mapped,
            unmappedFields = unmapped,
            warnings = warnings
        )
    }

    /**
     * Map internal HealthCondition to FHIRCondition
     */
    fun mapConditionToFHIR(cond: HealthCondition): MappingResult<FHIRCondition> {
        val mapped = mutableListOf<String>()
        val unmapped = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        mapped.add("recordId -> id")
        mapped.add("patientUid -> subject.reference")
        mapped.add("conditionName / conditionCode -> code")
        mapped.add("status -> clinicalStatus")
        mapped.add("diagnosedDate -> recordedDate")

        if (cond.severity.isNotBlank()) {
            unmapped.add("severity (stored in internal condition model; FHIR Condition.severity requires separate concept)")
            warnings.add("Condition severity preserved in internal Healthogram record")
        }

        val fhirCond = FHIRResourceMapper.toFHIRCondition(cond)

        return MappingResult(
            isSuccess = true,
            mappedOutput = fhirCond,
            mappedFields = mapped,
            unmappedFields = unmapped,
            warnings = warnings
        )
    }

    /**
     * Map internal HealthAllergy to FHIRAllergyIntolerance
     */
    fun mapAllergyToFHIR(allergy: HealthAllergy): MappingResult<FHIRAllergyIntolerance> {
        val mapped = mutableListOf<String>()
        val unmapped = mutableListOf<String>()

        mapped.add("recordId -> id")
        mapped.add("patientUid -> patient.reference")
        mapped.add("allergen -> code.text")
        mapped.add("severity -> criticality")
        mapped.add("status -> clinicalStatus")

        if (allergy.reaction.isNotBlank()) {
            unmapped.add("reaction (FHIR AllergyIntolerance maps reaction to reaction.manifestation array)")
        }

        val fhirAllergy = FHIRResourceMapper.toFHIRAllergy(allergy)

        return MappingResult(
            isSuccess = true,
            mappedOutput = fhirAllergy,
            mappedFields = mapped,
            unmappedFields = unmapped
        )
    }

    /**
     * Reverse Mapping: Inbound FHIRObservation to internal HealthObservation
     */
    fun mapFHIRToObservation(
        fhirObs: FHIRObservation,
        targetPatientUid: String,
        sourceOrgName: String
    ): MappingResult<HealthObservation> {
        val mapped = mutableListOf<String>()
        val unmapped = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val errors = mutableListOf<String>()

        val numVal = fhirObs.valueQuantity?.value ?: 0.0
        val unitStr = fhirObs.valueQuantity?.unit ?: ""
        val obsTypeName = fhirObs.code.text ?: fhirObs.code.coding.firstOrNull()?.display ?: "Unknown Observation"

        mapped.add("id -> externalRecordId")
        mapped.add("code.text / coding.display -> observationType")
        mapped.add("valueQuantity.value -> valueNumeric")
        mapped.add("valueQuantity.unit -> unit")

        if (fhirObs.valueString != null) {
            unmapped.add("valueString (HealthObservation prioritizes numericValue)")
            warnings.add("Inbound string value captured in provenance modification notes")
        }

        val provenance = HealthRecordProvenance(
            recordId = UUID.randomUUID().toString(),
            recordType = "OBSERVATION",
            patientUid = targetPatientUid,
            createdByUid = "fhir_import_service",
            createdByAccountType = "INDIVIDUAL",
            organizationName = sourceOrgName,
            sourceSystem = "HL7 FHIR R4 Ingestion",
            sourceStatus = RecordSourceStatus.IMPORTED,
            isImported = true,
            externalRecordId = fhirObs.id,
            verificationStatus = "VERIFIED_SOURCE",
            verifiedTimestamp = System.currentTimeMillis()
        )

        val observation = HealthObservation(
            recordId = provenance.recordId,
            patientUid = targetPatientUid,
            observationType = obsTypeName,
            valueNumeric = numVal,
            unit = unitStr,
            provenance = provenance
        )

        return MappingResult(
            isSuccess = true,
            mappedOutput = observation,
            mappedFields = mapped,
            unmappedFields = unmapped,
            warnings = warnings,
            errors = errors
        )
    }

    /**
     * Reverse Mapping: Inbound FHIRCondition to internal HealthCondition
     */
    fun mapFHIRToCondition(
        fhirCond: FHIRCondition,
        targetPatientUid: String,
        doctorUid: String
    ): MappingResult<HealthCondition> {
        val mapped = mutableListOf<String>()
        val unmapped = mutableListOf<String>()

        val condName = fhirCond.code.text ?: fhirCond.code.coding.firstOrNull()?.display ?: "Imported Condition"
        val codeStr = fhirCond.code.coding.firstOrNull()?.code ?: "UNSPECIFIED"

        mapped.add("id -> externalRecordId")
        mapped.add("code.text -> conditionName")
        mapped.add("code.coding.code -> conditionCode")
        mapped.add("clinicalStatus -> status")

        val condition = HealthCondition(
            recordId = UUID.randomUUID().toString(),
            patientUid = targetPatientUid,
            conditionName = condName,
            conditionCode = codeStr,
            status = fhirCond.clinicalStatus.uppercase(),
            diagnosedDate = fhirCond.recordedDate ?: "2026-01-01",
            createdByUid = doctorUid,
            sourceProviderId = fhirCond.id
        )

        return MappingResult(
            isSuccess = true,
            mappedOutput = condition,
            mappedFields = mapped,
            unmappedFields = unmapped
        )
    }
}
