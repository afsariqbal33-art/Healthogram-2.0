package com.example.healthogram.healthpassport

import java.util.UUID

/**
 * Healthogram 2.1 Health Record Provenance & Source Status
 * Tracks lineage, origin, actor, organization, and verification status.
 */
enum class RecordSourceStatus {
    PATIENT_ENTERED,
    PROVIDER_ENTERED,
    LABORATORY_GENERATED,
    IMPORTED,
    DIGITIZED_FROM_PAPER,
    VERIFIED_SOURCE,
    UNVERIFIED_SOURCE
}

data class HealthRecordProvenance(
    val provenanceId: String = UUID.randomUUID().toString(),
    val recordId: String,
    val recordType: String,
    val patientUid: String,
    val createdByUid: String,
    val createdByAccountType: String, // INDIVIDUAL, DOCTOR, CLINIC, HOSPITAL, LABORATORY
    val organizationId: String? = null,
    val organizationName: String? = null,
    val sourceSystem: String = "Healthogram Core", // e.g. "Health Connect", "Epic FHIR R4", "Manual Entry", "Paper Digitizer"
    val sourceStatus: RecordSourceStatus = RecordSourceStatus.PATIENT_ENTERED,
    val isImported: Boolean = false,
    val isExported: Boolean = false,
    val externalRecordId: String? = null,
    val verificationStatus: String = "UNVERIFIED", // UNVERIFIED, VERIFIED, CLINICIAN_CONFIRMED
    val verifiedByUid: String? = null,
    val verifiedTimestamp: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val modificationNotes: String? = null,
    val digitalSignature: String? = null
)

/**
 * Health Timeline Entry 2.1
 * Unified chronological event representation across all clinical and personal categories.
 */
data class HealthTimelineEntry(
    val timelineId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val timestamp: Long,
    val recordType: String, // VISIT, ENCOUNTER, LAB_TEST, PRESCRIPTION, DIAGNOSIS, PROCEDURE, SURGERY, VACCINATION, MEASUREMENT, HEALTH_CONNECT, FHIR_IMPORT, DOCUMENT, CARE_PLAN
    val title: String,
    val subtitle: String,
    val summary: String,
    val recordId: String,
    val provenance: HealthRecordProvenance,
    val verificationStatus: String,
    val consentContextId: String? = null,
    val category: String = "CLINICAL", // CLINICAL, WELLNESS, ADMINISTRATIVE, EMERGENCY
    val isSensitive: Boolean = false,
    val flags: List<String> = emptyList()
)

/**
 * Immunization / Vaccination record
 */
data class HealthImmunization(
    val recordId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val vaccineName: String,
    val vaccineCode: String = "CVX",
    val doseNumber: Int = 1,
    val seriesDoses: Int = 2,
    val administeredDate: String,
    val lotNumber: String? = null,
    val expirationDate: String? = null,
    val site: String? = "Deltoid muscle, Left arm",
    val route: String? = "Intramuscular",
    val administeringOrganization: String,
    val administeringClinician: String,
    val reactions: String? = null,
    val provenance: HealthRecordProvenance,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Surgical & Clinical Procedures
 */
data class HealthProcedure(
    val recordId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val procedureName: String,
    val procedureCode: String = "CPT/SNOMED",
    val status: String = "COMPLETED", // PREPARATION, IN_PROGRESS, COMPLETED, CANCELLED
    val category: String = "SURGICAL", // SURGICAL, DIAGNOSTIC, THERAPEUTIC
    val performedDate: String,
    val bodySite: String,
    val performingPractitioner: String,
    val performingOrganization: String,
    val outcome: String,
    val complicationNotes: String? = null,
    val followupInstructions: String? = null,
    val provenance: HealthRecordProvenance,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Observations, Measurements & Vital Signs
 */
data class HealthObservation(
    val recordId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val observationType: String, // BLOOD_PRESSURE, HEART_RATE, GLUCOSE, WEIGHT, HEIGHT, BMI, SPO2, TEMPERATURE, LAB_ANALYTE
    val code: String = "LOINC",
    val valueNumeric: Double? = null,
    val valueString: String? = null,
    val unit: String,
    val referenceRangeLow: Double? = null,
    val referenceRangeHigh: Double? = null,
    val interpretation: String? = "NORMAL", // NORMAL, HIGH, LOW, CRITICAL
    val effectiveTimestamp: Long = System.currentTimeMillis(),
    val observationSource: String = "PATIENT_MANUAL", // HEALTH_CONNECT, CLINICAL_MONITOR, LAB, PATIENT_MANUAL
    val provenance: HealthRecordProvenance,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Care Plan
 */
data class HealthCarePlan(
    val recordId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val planTitle: String,
    val intent: String = "PLAN", // PROPOSAL, PLAN, ORDER
    val status: String = "ACTIVE", // DRAFT, ACTIVE, ON_HOLD, COMPLETED, REVOKED
    val primaryCondition: String,
    val goals: List<String> = emptyList(),
    val activities: List<String> = emptyList(),
    val authorClinician: String,
    val authorOrganization: String,
    val startDate: String,
    val targetEndDate: String? = null,
    val notes: String = "",
    val provenance: HealthRecordProvenance,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Clinical Referral
 */
data class HealthReferral(
    val recordId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val referralReason: String,
    val specialty: String,
    val referringDoctorUid: String,
    val referringDoctorName: String,
    val referringClinicName: String,
    val targetProviderUid: String? = null,
    val targetProviderName: String? = null,
    val targetOrganizationName: String? = null,
    val priority: String = "ROUTINE", // ROUTINE, URGENT, STAT
    val status: String = "PENDING", // PENDING, ACCEPTED, SCHEDULED, COMPLETED, DECLINED
    val clinicalSummary: String,
    val validityExpiresAt: Long,
    val provenance: HealthRecordProvenance,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Encounter Summary
 */
data class HealthEncounterSummary(
    val encounterId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val encounterType: String, // OUTPATIENT, INPATIENT, EMERGENCY, VIRTUAL, HOME
    val organizationId: String,
    val organizationName: String,
    val clinicianName: String,
    val startTimestamp: Long,
    val endTimestamp: Long?,
    val chiefComplaint: String,
    val assessmentAndPlan: String,
    val dischargeDisposition: String? = null,
    val followUpInstructions: String? = null,
    val provenance: HealthRecordProvenance,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Family Medical History
 */
data class HealthFamilyHistory(
    val recordId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val relativeRelationship: String, // MOTHER, FATHER, SIBLING, GRANDPARENT, CHILD
    val conditionName: String,
    val approximateOnsetAge: Int? = null,
    val isDeceased: Boolean = false,
    val causeOfDeath: String? = null,
    val notes: String = "",
    val provenance: HealthRecordProvenance,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Emergency Health Card
 * Strictly patient-controlled card containing critical survival metadata.
 */
data class EmergencyHealthCard(
    val cardId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val fullName: String,
    val dateOfBirth: String,
    val bloodType: String, // A+, O-, etc.
    val criticalAllergies: List<String> = emptyList(),
    val lifeSustainingMedications: List<String> = emptyList(),
    val majorConditions: List<String> = emptyList(),
    val emergencyContacts: List<EmergencyContactEntry> = emptyList(),
    val specialDirectives: String = "",
    val organDonor: Boolean = false,
    val languagePreference: String = "en",
    val qrAccessEnabled: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class EmergencyContactEntry(
    val name: String,
    val relationship: String,
    val phone: String,
    val isPrimary: Boolean = false
)

/**
 * Paper Prescription Digitization record
 * Upload paper prescription -> OCR extraction -> Human review -> Structured record
 */
data class PaperPrescriptionDigitization(
    val digitizationId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val originalImageUri: String,
    val imageHashSha256: String,
    val extractedTextRaw: String,
    val extractedDoctorName: String? = null,
    val extractedClinicName: String? = null,
    val extractedDate: String? = null,
    val extractedMedications: List<ExtractedPrescriptionItem> = emptyList(),
    val ocrConfidenceScore: Float = 0.0f,
    val userReviewStatus: String = "PENDING_REVIEW", // PENDING_REVIEW, REVIEWED_CONFIRMED, EDITED_BY_USER, REJECTED
    val userVerificationDisclaimerAcknowledged: Boolean = false,
    val clinicianReviewed: Boolean = false,
    val finalizedRecordId: String? = null,
    val uploadedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null
)

data class ExtractedPrescriptionItem(
    val rawDrugLine: String,
    val parsedDrugName: String,
    val parsedDosage: String,
    val parsedFrequency: String,
    val parsedDurationDays: Int? = null,
    val userConfirmedName: String? = null,
    val userConfirmedDosage: String? = null,
    val userConfirmedFrequency: String? = null
)

/**
 * Medical Document Intelligence Metadata
 */
data class MedicalDocumentRecord(
    val documentId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val storagePath: String,
    val fileMimeType: String,
    val fileSizeBytes: Long,
    val documentCategory: String, // PRESCRIPTION, LAB_REPORT, DISCHARGE_SUMMARY, REFERRAL, VACCINATION, BILL, CONSULTATION_NOTE
    val detectedTitle: String,
    val detectedProviderName: String? = null,
    val detectedOrganizationName: String? = null,
    val detectedDate: String? = null,
    val structuredSummary: String,
    val aiModelUsed: String = "Gemini Health Doc Intelligence v2.1",
    val aiConfidenceScore: Float = 0.92f,
    val disclaimerText: String = "Extracted by automated document intelligence for organization purposes only. Please verify extracted information with your healthcare provider. This tool does not diagnose, prescribe, or substitute for professional medical care.",
    val isVerifiedByUser: Boolean = false,
    val provenance: HealthRecordProvenance,
    val createdAt: Long = System.currentTimeMillis()
)
