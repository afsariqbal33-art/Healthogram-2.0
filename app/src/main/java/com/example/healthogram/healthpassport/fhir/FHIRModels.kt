package com.example.healthogram.healthpassport.fhir

import java.util.UUID

/**
 * Standard HL7 FHIR R4 Resource representation classes
 */
data class FHIRCoding(
    val system: String,
    val code: String,
    val display: String
)

data class FHIRCodeableConcept(
    val coding: List<FHIRCoding>,
    val text: String? = null
)

data class FHIRIdentifier(
    val system: String,
    val value: String,
    val use: String = "official"
)

data class FHIRReference(
    val reference: String,
    val display: String? = null
)

data class FHIRPeriod(
    val start: String? = null,
    val end: String? = null
)

data class FHIRQuantity(
    val value: Double,
    val unit: String,
    val system: String = "http://unitsofmeasure.org",
    val code: String? = null
)

/**
 * Base FHIR Resource definition
 */
interface FHIRResource {
    val resourceType: String
    val id: String
}

data class FHIRPatient(
    override val resourceType: String = "Patient",
    override val id: String,
    val identifier: List<FHIRIdentifier> = emptyList(),
    val active: Boolean = true,
    val name: List<Map<String, Any>> = emptyList(),
    val gender: String? = null,
    val birthDate: String? = null
) : FHIRResource

data class FHIRPractitioner(
    override val resourceType: String = "Practitioner",
    override val id: String,
    val identifier: List<FHIRIdentifier> = emptyList(),
    val name: String,
    val specialty: String? = null
) : FHIRResource

data class FHIROrganization(
    override val resourceType: String = "Organization",
    override val id: String,
    val identifier: List<FHIRIdentifier> = emptyList(),
    val name: String,
    val type: String? = null // clinic, hospital, lab
) : FHIRResource

data class FHIREncounter(
    override val resourceType: String = "Encounter",
    override val id: String,
    val status: String = "finished",
    val subject: FHIRReference,
    val serviceProvider: FHIRReference? = null,
    val period: FHIRPeriod? = null,
    val reasonCode: List<FHIRCodeableConcept> = emptyList()
) : FHIRResource

data class FHIRObservation(
    override val resourceType: String = "Observation",
    override val id: String,
    val status: String = "final",
    val code: FHIRCodeableConcept,
    val subject: FHIRReference,
    val effectiveDateTime: String,
    val valueQuantity: FHIRQuantity? = null,
    val valueString: String? = null
) : FHIRResource

data class FHIRCondition(
    override val resourceType: String = "Condition",
    override val id: String,
    val clinicalStatus: String = "active",
    val code: FHIRCodeableConcept,
    val subject: FHIRReference,
    val recordedDate: String? = null
) : FHIRResource

data class FHIRAllergyIntolerance(
    override val resourceType: String = "AllergyIntolerance",
    override val id: String,
    val clinicalStatus: String = "active",
    val code: FHIRCodeableConcept,
    val patient: FHIRReference,
    val criticality: String = "high"
) : FHIRResource

data class FHIRMedicationRequest(
    override val resourceType: String = "MedicationRequest",
    override val id: String,
    val status: String = "active",
    val medicationCodeableConcept: FHIRCodeableConcept,
    val subject: FHIRReference,
    val authoredOn: String? = null,
    val dosageInstruction: String? = null
) : FHIRResource

data class FHIRDiagnosticReport(
    override val resourceType: String = "DiagnosticReport",
    override val id: String,
    val status: String = "final",
    val code: FHIRCodeableConcept,
    val subject: FHIRReference,
    val effectiveDateTime: String,
    val performer: List<FHIRReference> = emptyList(),
    val conclusion: String? = null
) : FHIRResource

data class FHIRProcedure(
    override val resourceType: String = "Procedure",
    override val id: String,
    val status: String = "completed",
    val code: FHIRCodeableConcept,
    val subject: FHIRReference,
    val performedDateTime: String,
    val performer: List<FHIRReference> = emptyList()
) : FHIRResource

data class FHIRImmunization(
    override val resourceType: String = "Immunization",
    override val id: String,
    val status: String = "completed",
    val vaccineCode: FHIRCodeableConcept,
    val patient: FHIRReference,
    val occurrenceDateTime: String,
    val primarySource: Boolean = true
) : FHIRResource

data class FHIRDocumentReference(
    override val resourceType: String = "DocumentReference",
    override val id: String,
    val status: String = "current",
    val type: FHIRCodeableConcept,
    val subject: FHIRReference,
    val date: String,
    val description: String? = null
) : FHIRResource

data class FHIRAppointmentResource(
    override val resourceType: String = "Appointment",
    override val id: String,
    val status: String = "booked",
    val description: String? = null,
    val start: String,
    val end: String,
    val participant: List<FHIRReference> = emptyList()
) : FHIRResource

data class FHIRMedication(
    override val resourceType: String = "Medication",
    override val id: String,
    val code: FHIRCodeableConcept,
    val status: String = "active"
) : FHIRResource

data class FHIRCarePlan(
    override val resourceType: String = "CarePlan",
    override val id: String,
    val status: String = "active",
    val intent: String = "plan",
    val subject: FHIRReference,
    val title: String? = null,
    val description: String? = null,
    val period: FHIRPeriod? = null
) : FHIRResource

data class FHIRServiceRequest(
    override val resourceType: String = "ServiceRequest",
    override val id: String,
    val status: String = "active",
    val intent: String = "order",
    val code: FHIRCodeableConcept,
    val subject: FHIRReference,
    val authoredOn: String? = null,
    val requester: FHIRReference? = null
) : FHIRResource

data class FHIRBundle(
    val resourceType: String = "Bundle",
    val id: String = UUID.randomUUID().toString(),
    val type: String = "collection",
    val timestamp: String,
    val total: Int,
    val entry: List<FHIRResource> = emptyList()
)
