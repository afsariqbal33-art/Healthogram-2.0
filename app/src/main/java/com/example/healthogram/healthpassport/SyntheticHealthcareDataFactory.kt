package com.example.healthogram.healthpassport

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import com.example.healthogram.healthpassport.fhir.*
import java.util.UUID

/**
 * Step 33: SyntheticHealthcareDataFactory
 *
 * Generates clearly designated synthetic healthcare test fixtures for automated QA,
 * conformance validation, and security testing.
 *
 * CRITICAL INVARIANT:
 * Never uses real patient information. All synthetic entities are explicitly prefixed.
 */
object SyntheticHealthcareDataFactory {

    // --- Synthetic Accounts ---
    val INDIVIDUAL_A = User(
        uid = "qa.individual.01",
        email = "qa.individual.01@synthetic.healthogram.test",
        displayName = "Synthetic Patient Salim",
        username = "qa_individual_01",
        accountType = AccountType.INDIVIDUAL,
        isVerified = true
    )

    val INDIVIDUAL_B = User(
        uid = "qa.individual.02",
        email = "qa.individual.02@synthetic.healthogram.test",
        displayName = "Synthetic Patient Fatima",
        username = "qa_individual_02",
        accountType = AccountType.INDIVIDUAL,
        isVerified = true
    )

    val DOCTOR_A = User(
        uid = "qa.doctor.01",
        email = "qa.doctor.01@synthetic.healthogram.test",
        displayName = "Dr. Synthetic Khalfan (Cardiology)",
        username = "qa_doctor_01",
        accountType = AccountType.DOCTOR,
        isVerified = true
    )

    val DOCTOR_B = User(
        uid = "qa.doctor.02",
        email = "qa.doctor.02@synthetic.healthogram.test",
        displayName = "Dr. Synthetic Laila (Endocrinology)",
        username = "qa_doctor_02",
        accountType = AccountType.DOCTOR,
        isVerified = true
    )

    val CLINIC_A = User(
        uid = "qa.clinic.01",
        email = "qa.clinic.01@synthetic.healthogram.test",
        displayName = "Synthetic Al-Bustan Medical Clinic",
        username = "qa_clinic_01",
        accountType = AccountType.CLINIC,
        isVerified = true
    )

    val HOSPITAL_A = User(
        uid = "qa.hospital.01",
        email = "qa.hospital.01@synthetic.healthogram.test",
        displayName = "Synthetic Royal Muscat Hospital",
        username = "qa_hospital_01",
        accountType = AccountType.HOSPITAL,
        isVerified = true
    )

    val LABORATORY_A = User(
        uid = "qa.lab.01",
        email = "qa.lab.01@synthetic.healthogram.test",
        displayName = "Synthetic Gulf Diagnostics Laboratory",
        username = "qa_lab_01",
        accountType = AccountType.LABORATORY,
        isVerified = true
    )

    // --- Synthetic Clinical Records for Individual A ---

    fun createSyntheticObservation(
        patientUid: String = INDIVIDUAL_A.uid,
        obsType: String = "FASTING_BLOOD_GLUCOSE",
        valueNumeric: Double = 5.4,
        unit: String = "mmol/L"
    ): HealthObservation {
        val provenance = HealthRecordProvenance(
            recordId = "synth_obs_${UUID.randomUUID().toString().take(8)}",
            recordType = "OBSERVATION",
            patientUid = patientUid,
            createdByUid = LABORATORY_A.uid,
            createdByAccountType = "LABORATORY",
            organizationName = LABORATORY_A.displayName,
            sourceSystem = "Synthetic LIS R4",
            sourceStatus = RecordSourceStatus.LABORATORY_GENERATED,
            verificationStatus = "CLINICIAN_VERIFIED"
        )
        return HealthObservation(
            recordId = provenance.recordId,
            patientUid = patientUid,
            observationType = obsType,
            valueNumeric = valueNumeric,
            valueString = null,
            unit = unit,
            interpretation = "NORMAL",
            effectiveTimestamp = System.currentTimeMillis() - 86400000L,
            observationSource = "LAB_INTEGRATION",
            provenance = provenance
        )
    }

    fun createSyntheticCondition(
        patientUid: String = INDIVIDUAL_A.uid,
        conditionName: String = "Essential Hypertension",
        icd10: String = "I10"
    ): HealthCondition {
        return HealthCondition(
            recordId = "synth_cond_${UUID.randomUUID().toString().take(8)}",
            patientUid = patientUid,
            conditionName = conditionName,
            conditionCode = icd10,
            status = "ACTIVE",
            severity = "MODERATE",
            diagnosedDate = "2024-03-15",
            createdByUid = DOCTOR_A.uid,
            createdByRole = "DOCTOR",
            sourceProviderId = CLINIC_A.uid
        )
    }

    fun createSyntheticAllergy(
        patientUid: String = INDIVIDUAL_A.uid,
        allergen: String = "Penicillin",
        reaction: String = "Anaphylaxis / Hives"
    ): HealthAllergy {
        return HealthAllergy(
            recordId = "synth_alg_${UUID.randomUUID().toString().take(8)}",
            patientUid = patientUid,
            allergen = allergen,
            reaction = reaction,
            severity = "HIGH",
            createdByUid = DOCTOR_A.uid
        )
    }

    fun createSyntheticMedication(
        patientUid: String = INDIVIDUAL_A.uid,
        medicineName: String = "Amlodipine 5mg",
        dosage: String = "5mg Once Daily"
    ): HealthMedication {
        return HealthMedication(
            recordId = "synth_med_${UUID.randomUUID().toString().take(8)}",
            patientUid = patientUid,
            medicineName = medicineName,
            dosage = dosage,
            frequency = "Once daily morning",
            prescribedByUid = DOCTOR_A.uid,
            startDate = "2024-03-15",
            status = "ACTIVE"
        )
    }

    // --- FHIR Round Trip Analysis Models ---
    enum class RoundTripDifferenceType {
        EQUIVALENT,
        TRANSFORMED,
        LOSSY,
        UNSUPPORTED,
        REQUIRES_REVIEW
    }

    data class RoundTripComparisonResult(
        val entityType: String,
        val originalRecordId: String,
        val fhirResourceId: String,
        val roundTripRecordId: String,
        val differenceType: RoundTripDifferenceType,
        val fieldDiffs: Map<String, Pair<Any?, Any?>> = emptyMap(),
        val remarks: String
    )

    /**
     * Compares an original HealthObservation with one reconstituted from FHIR R4
     */
    fun evaluateObservationRoundTrip(
        original: HealthObservation,
        reconstituted: HealthObservation
    ): RoundTripComparisonResult {
        val diffs = mutableMapOf<String, Pair<Any?, Any?>>()

        if (original.observationType != reconstituted.observationType) {
            diffs["observationType"] = Pair(original.observationType, reconstituted.observationType)
        }
        if (original.valueNumeric != reconstituted.valueNumeric) {
            diffs["valueNumeric"] = Pair(original.valueNumeric, reconstituted.valueNumeric)
        }
        if (original.unit != reconstituted.unit) {
            diffs["unit"] = Pair(original.unit, reconstituted.unit)
        }
        if (original.patientUid != reconstituted.patientUid) {
            diffs["patientUid"] = Pair(original.patientUid, reconstituted.patientUid)
        }

        val diffType = when {
            diffs.isEmpty() && original.provenance.sourceStatus != reconstituted.provenance.sourceStatus ->
                RoundTripDifferenceType.TRANSFORMED // Provenance indicates re-imported state
            diffs.isEmpty() -> RoundTripDifferenceType.EQUIVALENT
            diffs.containsKey("valueNumeric") -> RoundTripDifferenceType.LOSSY
            else -> RoundTripDifferenceType.REQUIRES_REVIEW
        }

        return RoundTripComparisonResult(
            entityType = "HealthObservation",
            originalRecordId = original.recordId,
            fhirResourceId = reconstituted.provenance.externalRecordId ?: "N/A",
            roundTripRecordId = reconstituted.recordId,
            differenceType = diffType,
            fieldDiffs = diffs,
            remarks = if (diffType == RoundTripDifferenceType.EQUIVALENT || diffType == RoundTripDifferenceType.TRANSFORMED) {
                "Clinical fidelity strictly preserved across FHIR R4 transformation"
            } else {
                "Discrepancies detected: ${diffs.keys}"
            }
        )
    }
}
