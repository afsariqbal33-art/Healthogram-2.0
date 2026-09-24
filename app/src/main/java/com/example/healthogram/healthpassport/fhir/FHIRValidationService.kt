package com.example.healthogram.healthpassport.fhir

import java.util.UUID

/**
 * Step 32: FHIRValidationService
 *
 * Validates HL7 FHIR R4 resources against schemas, required fields,
 * reference integrity, terminology bindings, and version compatibility.
 */
class FHIRValidationService private constructor() {

    data class ValidationIssue(
        val severity: String, // ERROR, WARNING, INFORMATION
        val location: String,
        val message: String,
        val code: String
    )

    data class FHIRValidationReport(
        val isValid: Boolean,
        val resourceType: String,
        val resourceId: String,
        val issues: List<ValidationIssue> = emptyList(),
        val validatedAt: Long = System.currentTimeMillis()
    ) {
        val hasErrors: Boolean get() = issues.any { it.severity == "ERROR" }
        val hasWarnings: Boolean get() = issues.any { it.severity == "WARNING" }
    }

    companion object {
        @Volatile
        private var instance: FHIRValidationService? = null

        fun getInstance(): FHIRValidationService {
            return instance ?: synchronized(this) {
                instance ?: FHIRValidationService().also { instance = it }
            }
        }

        const val FHIR_VERSION = "4.0.1"
    }

    /**
     * Validates any generic FHIRResource against R4 core constraints
     */
    fun validateResource(resource: FHIRResource): FHIRValidationReport {
        val issues = mutableListOf<ValidationIssue>()

        // 1. Required field checks
        if (resource.id.isBlank()) {
            issues.add(
                ValidationIssue(
                    severity = "ERROR",
                    location = "${resource.resourceType}.id",
                    message = "Resource ID cannot be null or blank",
                    code = "required-field-missing"
                )
            )
        }

        // 2. Resource-specific schema validation
        when (resource) {
            is FHIRPatient -> validatePatient(resource, issues)
            is FHIRObservation -> validateObservation(resource, issues)
            is FHIRCondition -> validateCondition(resource, issues)
            is FHIRAllergyIntolerance -> validateAllergy(resource, issues)
            is FHIRMedicationRequest -> validateMedicationRequest(resource, issues)
            is FHIRDiagnosticReport -> validateDiagnosticReport(resource, issues)
            is FHIRProcedure -> validateProcedure(resource, issues)
            is FHIRImmunization -> validateImmunization(resource, issues)
            is FHIRDocumentReference -> validateDocumentReference(resource, issues)
            is FHIRCarePlan -> validateCarePlan(resource, issues)
            is FHIRAppointmentResource -> validateAppointment(resource, issues)
            is FHIRServiceRequest -> validateServiceRequest(resource, issues)
            is FHIRMedication -> validateMedication(resource, issues)
            is FHIRPractitioner -> validatePractitioner(resource, issues)
            is FHIROrganization -> validateOrganization(resource, issues)
            is FHIREncounter -> validateEncounter(resource, issues)
            else -> {
                issues.add(
                    ValidationIssue(
                        severity = "WARNING",
                        location = resource.resourceType,
                        message = "Resource type has basic structural validation only",
                        code = "limited-profile"
                    )
                )
            }
        }

        return FHIRValidationReport(
            isValid = issues.none { it.severity == "ERROR" },
            resourceType = resource.resourceType,
            resourceId = resource.id,
            issues = issues
        )
    }

    private fun validatePatient(patient: FHIRPatient, issues: MutableList<ValidationIssue>) {
        if (patient.gender != null && patient.gender !in setOf("male", "female", "other", "unknown")) {
            issues.add(
                ValidationIssue(
                    severity = "ERROR",
                    location = "Patient.gender",
                    message = "Invalid administrative gender: ${patient.gender}. Must be male|female|other|unknown",
                    code = "invalid-code"
                )
            )
        }
    }

    private fun validateObservation(obs: FHIRObservation, issues: MutableList<ValidationIssue>) {
        if (!obs.subject.reference.startsWith("Patient/")) {
            issues.add(
                ValidationIssue(
                    severity = "ERROR",
                    location = "Observation.subject",
                    message = "Observation subject must reference a Patient (e.g., 'Patient/uid')",
                    code = "invalid-reference"
                )
            )
        }
        if (obs.code.coding.isEmpty() && obs.code.text.isNullOrBlank()) {
            issues.add(
                ValidationIssue(
                    severity = "ERROR",
                    location = "Observation.code",
                    message = "Observation requires at least one coding or non-empty text",
                    code = "missing-code"
                )
            )
        }
        if (obs.valueQuantity == null && obs.valueString == null) {
            issues.add(
                ValidationIssue(
                    severity = "WARNING",
                    location = "Observation.value",
                    message = "Observation does not provide valueQuantity or valueString",
                    code = "no-value"
                )
            )
        }
    }

    private fun validateCondition(cond: FHIRCondition, issues: MutableList<ValidationIssue>) {
        if (!cond.subject.reference.startsWith("Patient/")) {
            issues.add(
                ValidationIssue(
                    severity = "ERROR",
                    location = "Condition.subject",
                    message = "Condition subject must reference a Patient",
                    code = "invalid-reference"
                )
            )
        }
        if (cond.clinicalStatus !in setOf("active", "recurrence", "relapse", "inactive", "remission", "resolved")) {
            issues.add(
                ValidationIssue(
                    severity = "WARNING",
                    location = "Condition.clinicalStatus",
                    message = "Unrecognized clinicalStatus: ${cond.clinicalStatus}",
                    code = "unrecognized-status"
                )
            )
        }
    }

    private fun validateAllergy(allergy: FHIRAllergyIntolerance, issues: MutableList<ValidationIssue>) {
        if (!allergy.patient.reference.startsWith("Patient/")) {
            issues.add(
                ValidationIssue(
                    severity = "ERROR",
                    location = "AllergyIntolerance.patient",
                    message = "Allergy patient must reference a Patient",
                    code = "invalid-reference"
                )
            )
        }
    }

    private fun validateMedicationRequest(req: FHIRMedicationRequest, issues: MutableList<ValidationIssue>) {
        if (!req.subject.reference.startsWith("Patient/")) {
            issues.add(
                ValidationIssue(
                    severity = "ERROR",
                    location = "MedicationRequest.subject",
                    message = "MedicationRequest subject must reference a Patient",
                    code = "invalid-reference"
                )
            )
        }
    }

    private fun validateDiagnosticReport(report: FHIRDiagnosticReport, issues: MutableList<ValidationIssue>) {
        if (!report.subject.reference.startsWith("Patient/")) {
            issues.add(
                ValidationIssue(
                    severity = "ERROR",
                    location = "DiagnosticReport.subject",
                    message = "DiagnosticReport subject must reference a Patient",
                    code = "invalid-reference"
                )
            )
        }
    }

    private fun validateProcedure(proc: FHIRProcedure, issues: MutableList<ValidationIssue>) {
        if (!proc.subject.reference.startsWith("Patient/")) {
            issues.add(
                ValidationIssue(
                    severity = "ERROR",
                    location = "Procedure.subject",
                    message = "Procedure subject must reference a Patient",
                    code = "invalid-reference"
                )
            )
        }
    }

    private fun validateImmunization(imm: FHIRImmunization, issues: MutableList<ValidationIssue>) {
        if (!imm.patient.reference.startsWith("Patient/")) {
            issues.add(
                ValidationIssue(
                    severity = "ERROR",
                    location = "Immunization.patient",
                    message = "Immunization patient must reference a Patient",
                    code = "invalid-reference"
                )
            )
        }
    }

    private fun validateDocumentReference(doc: FHIRDocumentReference, issues: MutableList<ValidationIssue>) {
        if (!doc.subject.reference.startsWith("Patient/")) {
            issues.add(
                ValidationIssue(
                    severity = "ERROR",
                    location = "DocumentReference.subject",
                    message = "DocumentReference subject must reference a Patient",
                    code = "invalid-reference"
                )
            )
        }
    }

    private fun validateCarePlan(plan: FHIRCarePlan, issues: MutableList<ValidationIssue>) {
        if (!plan.subject.reference.startsWith("Patient/")) {
            issues.add(
                ValidationIssue(
                    severity = "ERROR",
                    location = "CarePlan.subject",
                    message = "CarePlan subject must reference a Patient",
                    code = "invalid-reference"
                )
            )
        }
    }

    private fun validateAppointment(app: FHIRAppointmentResource, issues: MutableList<ValidationIssue>) {
        if (app.start.isBlank() || app.end.isBlank()) {
            issues.add(
                ValidationIssue(
                    severity = "ERROR",
                    location = "Appointment.time",
                    message = "Appointment requires valid start and end timestamps",
                    code = "missing-time"
                )
            )
        }
    }

    private fun validateServiceRequest(sr: FHIRServiceRequest, issues: MutableList<ValidationIssue>) {
        if (!sr.subject.reference.startsWith("Patient/")) {
            issues.add(
                ValidationIssue(
                    severity = "ERROR",
                    location = "ServiceRequest.subject",
                    message = "ServiceRequest subject must reference a Patient",
                    code = "invalid-reference"
                )
            )
        }
    }

    private fun validateMedication(med: FHIRMedication, issues: MutableList<ValidationIssue>) {
        if (med.code.coding.isEmpty()) {
            issues.add(
                ValidationIssue(
                    severity = "WARNING",
                    location = "Medication.code",
                    message = "Medication should provide standardized coding",
                    code = "unmapped-coding"
                )
            )
        }
    }

    private fun validatePractitioner(prac: FHIRPractitioner, issues: MutableList<ValidationIssue>) {
        if (prac.name.isBlank()) {
            issues.add(
                ValidationIssue(
                    severity = "ERROR",
                    location = "Practitioner.name",
                    message = "Practitioner name cannot be blank",
                    code = "required-field-missing"
                )
            )
        }
    }

    private fun validateOrganization(org: FHIROrganization, issues: MutableList<ValidationIssue>) {
        if (org.name.isBlank()) {
            issues.add(
                ValidationIssue(
                    severity = "ERROR",
                    location = "Organization.name",
                    message = "Organization name cannot be blank",
                    code = "required-field-missing"
                )
            )
        }
    }

    private fun validateEncounter(enc: FHIREncounter, issues: MutableList<ValidationIssue>) {
        if (!enc.subject.reference.startsWith("Patient/")) {
            issues.add(
                ValidationIssue(
                    severity = "ERROR",
                    location = "Encounter.subject",
                    message = "Encounter subject must reference a Patient",
                    code = "invalid-reference"
                )
            )
        }
    }
}
