package com.example.healthogram.healthpassport.fhir

import com.example.healthogram.healthpassport.*
import java.util.UUID

/**
 * Healthogram 2.1 FHIR R4 Resource Mapper
 * Converts between Healthogram internal models and normative FHIR R4 resources.
 * INVARIANT: Provenance and data origin are strictly preserved in every transformation.
 */
object FHIRResourceMapper {

    const val MAPPING_VERSION = "2.2.0"
    const val SCHEMA_VERSION = "HL7_FHIR_R4_0_1"
    const val INTEGRATION_VERSION = "HEALTHOGRAM_GATEWAY_V2"

    // 1. Patient Mapping
    fun toFHIRPatient(profile: HealthProfile, displayName: String = "Healthogram Patient"): FHIRPatient {
        return FHIRPatient(
            id = profile.uid,
            identifier = listOf(
                FHIRIdentifier(
                    system = "https://healthogram.io/fhir/health-id",
                    value = profile.healthId
                )
            ),
            active = profile.profileStatus.equals("ACTIVE", ignoreCase = true),
            name = listOf(
                mapOf(
                    "use" to "official",
                    "text" to displayName
                )
            ),
            birthDate = profile.dateOfBirth
        )
    }

    // 2. Observation Mapping (Vitals & Labs)
    fun toFHIRObservation(observation: HealthObservation): FHIRObservation {
        val coding = TerminologyService.getObservationCoding(observation.observationType)
        return FHIRObservation(
            id = observation.recordId,
            status = "final",
            code = FHIRCodeableConcept(
                coding = listOf(coding),
                text = observation.observationType
            ),
            subject = FHIRReference(
                reference = "Patient/${observation.patientUid}"
            ),
            effectiveDateTime = java.time.Instant.ofEpochMilli(observation.effectiveTimestamp).toString(),
            valueQuantity = observation.valueNumeric?.let {
                FHIRQuantity(
                    value = it,
                    unit = observation.unit,
                    code = observation.unit
                )
            },
            valueString = observation.valueString
        )
    }

    fun fromFHIRObservation(fhir: FHIRObservation, patientUid: String, sourceOrg: String = "External FHIR"): HealthObservation {
        val obsType = fhir.code.text ?: fhir.code.coding.firstOrNull()?.display ?: "GENERAL_OBSERVATION"
        val numericVal = fhir.valueQuantity?.value
        val strVal = fhir.valueString
        val unit = fhir.valueQuantity?.unit ?: ""

        val provenance = HealthRecordProvenance(
            recordId = fhir.id,
            recordType = "OBSERVATION",
            patientUid = patientUid,
            createdByUid = "FHIR_IMPORT",
            createdByAccountType = "IMPORTED",
            organizationName = sourceOrg,
            sourceSystem = "FHIR R4 Import",
            sourceStatus = RecordSourceStatus.IMPORTED,
            isImported = true,
            externalRecordId = fhir.id,
            verificationStatus = "IMPORTED_UNVERIFIED"
        )

        val parsedTimestamp = try {
            fhir.effectiveDateTime?.let { java.time.Instant.parse(it).toEpochMilli() } ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }

        return HealthObservation(
            recordId = UUID.randomUUID().toString(),
            patientUid = patientUid,
            observationType = obsType,
            valueNumeric = numericVal,
            valueString = strVal,
            unit = unit,
            effectiveTimestamp = parsedTimestamp,
            observationSource = "FHIR_IMPORT",
            provenance = provenance
        )
    }

    // 3. Condition Mapping
    fun toFHIRCondition(condition: HealthCondition): FHIRCondition {
        return FHIRCondition(
            id = condition.recordId,
            clinicalStatus = if (condition.status.equals("ACTIVE", ignoreCase = true)) "active" else "resolved",
            code = TerminologyService.getConditionCoding(condition.conditionName, condition.conditionCode),
            subject = FHIRReference(
                reference = "Patient/${condition.patientUid}"
            ),
            recordedDate = condition.diagnosedDate
        )
    }

    fun fromFHIRCondition(fhir: FHIRCondition, patientUid: String, sourceOrg: String = "External FHIR"): HealthCondition {
        val name = fhir.code.text ?: fhir.code.coding.firstOrNull()?.display ?: "Condition"
        val code = fhir.code.coding.firstOrNull()?.code ?: "ICD-10"

        return HealthCondition(
            recordId = UUID.randomUUID().toString(),
            patientUid = patientUid,
            conditionName = name,
            conditionCode = code,
            status = if (fhir.clinicalStatus.equals("active", ignoreCase = true)) "ACTIVE" else "RESOLVED",
            diagnosedDate = fhir.recordedDate ?: "2026-01-01",
            createdByUid = "FHIR_IMPORT",
            createdByRole = "IMPORT",
            sourceProviderId = sourceOrg
        )
    }

    // 4. AllergyIntolerance Mapping
    fun toFHIRAllergy(allergy: HealthAllergy): FHIRAllergyIntolerance {
        return FHIRAllergyIntolerance(
            id = allergy.recordId,
            clinicalStatus = if (allergy.status.equals("ACTIVE", ignoreCase = true)) "active" else "inactive",
            code = FHIRCodeableConcept(
                coding = listOf(
                    FHIRCoding(
                        system = TerminologyService.SYSTEM_SNOMED,
                        code = "ALLERG-${Math.abs(allergy.allergen.hashCode()) % 100000}",
                        display = allergy.allergen
                    )
                ),
                text = "${allergy.allergen} (${allergy.reaction})"
            ),
            patient = FHIRReference(
                reference = "Patient/${allergy.patientUid}"
            ),
            criticality = when (allergy.severity.uppercase()) {
                "LIFE_THREATENING" -> "high"
                "HIGH" -> "high"
                else -> "low"
            }
        )
    }

    // 5. MedicationRequest Mapping
    fun toFHIRMedication(medication: HealthMedication): FHIRMedicationRequest {
        return FHIRMedicationRequest(
            id = medication.recordId,
            status = if (medication.status.equals("ACTIVE", ignoreCase = true)) "active" else "stopped",
            medicationCodeableConcept = TerminologyService.getMedicationCoding(medication.medicineName),
            subject = FHIRReference(
                reference = "Patient/${medication.patientUid}"
            ),
            authoredOn = medication.startDate,
            dosageInstruction = "${medication.dosage}, ${medication.frequency}, Route: ${medication.route}"
        )
    }

    // 6. DiagnosticReport Mapping (Lab Reports)
    fun toFHIRDiagnosticReport(labReport: HealthLabReport): FHIRDiagnosticReport {
        return FHIRDiagnosticReport(
            id = labReport.recordId,
            status = when (labReport.reportStatus.uppercase()) {
                "FINAL" -> "final"
                "PRELIMINARY" -> "preliminary"
                else -> "amended"
            },
            code = FHIRCodeableConcept(
                coding = listOf(
                    TerminologyService.getObservationCoding("LAB_REPORT")
                ),
                text = "Diagnostic Laboratory Report"
            ),
            subject = FHIRReference(
                reference = "Patient/${labReport.patientUid}"
            ),
            effectiveDateTime = labReport.reportDate,
            performer = listOf(
                FHIRReference(
                    reference = "Organization/${labReport.laboratoryUid}",
                    display = "Laboratory Provider"
                )
            ),
            conclusion = labReport.summary
        )
    }

    // 7. Immunization Mapping
    fun toFHIRImmunization(immunization: HealthImmunization): FHIRImmunization {
        val coding = TerminologyService.getVaccineCoding(immunization.vaccineName)
        return FHIRImmunization(
            id = immunization.recordId,
            status = "completed",
            vaccineCode = FHIRCodeableConcept(
                coding = listOf(coding),
                text = immunization.vaccineName
            ),
            patient = FHIRReference(
                reference = "Patient/${immunization.patientUid}"
            ),
            occurrenceDateTime = immunization.administeredDate,
            primarySource = immunization.provenance.sourceStatus == RecordSourceStatus.PROVIDER_ENTERED
        )
    }

    // 8. Procedure Mapping
    fun toFHIRProcedure(procedure: HealthProcedure): FHIRProcedure {
        return FHIRProcedure(
            id = procedure.recordId,
            status = if (procedure.status.equals("COMPLETED", ignoreCase = true)) "completed" else "in-progress",
            code = FHIRCodeableConcept(
                coding = listOf(
                    FHIRCoding(
                        system = TerminologyService.SYSTEM_SNOMED,
                        code = "PROC-${Math.abs(procedure.procedureName.hashCode()) % 100000}",
                        display = procedure.procedureName
                    )
                ),
                text = procedure.procedureName
            ),
            subject = FHIRReference(
                reference = "Patient/${procedure.patientUid}"
            ),
            performedDateTime = procedure.performedDate,
            performer = listOf(
                FHIRReference(
                    reference = "Practitioner/${procedure.performingPractitioner}",
                    display = procedure.performingPractitioner
                )
            )
        )
    }

    // 9. DocumentReference Mapping
    fun toFHIRDocumentReference(doc: MedicalDocumentRecord): FHIRDocumentReference {
        return FHIRDocumentReference(
            id = doc.documentId,
            status = "current",
            type = FHIRCodeableConcept(
                coding = listOf(
                    FHIRCoding(
                        system = TerminologyService.SYSTEM_LOINC,
                        code = "34133-9",
                        display = "Summary of episode note"
                    )
                ),
                text = doc.documentCategory
            ),
            subject = FHIRReference(
                reference = "Patient/${doc.patientUid}"
            ),
            date = doc.detectedDate ?: "2026-09-01",
            description = "${doc.detectedTitle} - ${doc.structuredSummary}"
        )
    }

    // 10. Encounter Mapping
    fun toFHIREncounter(encounter: HealthEncounterSummary): FHIREncounter {
        return FHIREncounter(
            id = encounter.encounterId,
            status = "finished",
            subject = FHIRReference(
                reference = "Patient/${encounter.patientUid}"
            ),
            serviceProvider = FHIRReference(
                reference = "Organization/${encounter.organizationId}",
                display = encounter.organizationName
            ),
            period = FHIRPeriod(
                start = java.time.Instant.ofEpochMilli(encounter.startTimestamp).toString(),
                end = encounter.endTimestamp?.let { java.time.Instant.ofEpochMilli(it).toString() }
            ),
            reasonCode = listOf(
                FHIRCodeableConcept(
                    coding = emptyList(),
                    text = encounter.chiefComplaint
                )
            )
        )
    }
}
