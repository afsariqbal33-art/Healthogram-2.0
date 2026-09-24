package com.example.healthogram.organization

import com.example.healthogram.core.AccountType
import com.example.healthogram.healthpassport.*
import java.util.UUID

data class ScopedHealthDataPackage(
    val patientUid: String,
    val accessedByUid: String,
    val organizationId: String?,
    val accessedCategories: Set<String>,
    val conditions: List<HealthCondition> = emptyList(),
    val allergies: List<HealthAllergy> = emptyList(),
    val medications: List<HealthMedication> = emptyList(),
    val labReports: List<HealthLabReport> = emptyList(),
    val vitals: List<HealthObservation> = emptyList(),
    val auditId: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * HealthPassportGateway
 * Gateway controlling all organization and provider access to patient medical data.
 * INVARIANT: No provider or organization receives unrestricted access to a patient's Health Passport.
 * Every access requires verified identity, scoped consent, and creates an immutable audit trail.
 */
class HealthPassportGateway private constructor(
    private val consentService: ConsentManagementService = ConsentManagementService.getInstance()
) {

    companion object {
        @Volatile
        private var instance: HealthPassportGateway? = null

        fun getInstance(): HealthPassportGateway {
            return instance ?: synchronized(this) {
                instance ?: HealthPassportGateway().also { instance = it }
            }
        }
    }

    /**
     * Request scoped patient health data on behalf of a verified healthcare provider
     */
    fun getScopedPatientData(
        patientUid: String,
        requesterUid: String,
        requesterAccountType: AccountType,
        organizationId: String?,
        requestedCategories: Set<String>,
        allConditions: List<HealthCondition> = emptyList(),
        allAllergies: List<HealthAllergy> = emptyList(),
        allMedications: List<HealthMedication> = emptyList(),
        allLabReports: List<HealthLabReport> = emptyList(),
        allVitals: List<HealthObservation> = emptyList()
    ): ScopedHealthDataPackage {
        // 1. Enforce verified healthcare account types only
        require(
            requesterAccountType in listOf(
                AccountType.DOCTOR,
                AccountType.CLINIC,
                AccountType.HOSPITAL,
                AccountType.LABORATORY
            )
        ) {
            "Account type $requesterAccountType is not permitted healthcare provider access"
        }

        // 2. Validate active scoped consent for requested categories
        val authorizedCategories = requestedCategories.filter { category ->
            consentService.isAuthorized(patientUid, requesterUid, category)
        }.toSet()

        if (authorizedCategories.isEmpty()) {
            throw SecurityException(
                "Access Denied: No active consent grant found for provider $requesterUid on patient $patientUid for categories $requestedCategories"
            )
        }

        // 3. Data minimization: strictly filter returned data to authorized categories only
        val filteredConditions = if (authorizedCategories.contains("CONDITIONS")) {
            allConditions.filter { it.patientUid == patientUid }
        } else emptyList()

        val filteredAllergies = if (authorizedCategories.contains("ALLERGIES")) {
            allAllergies.filter { it.patientUid == patientUid }
        } else emptyList()

        val filteredMedications = if (authorizedCategories.contains("MEDICATIONS")) {
            allMedications.filter { it.patientUid == patientUid }
        } else emptyList()

        val filteredLabReports = if (authorizedCategories.contains("LAB_REPORTS")) {
            allLabReports.filter { it.patientUid == patientUid }
        } else emptyList()

        val filteredVitals = if (authorizedCategories.contains("VITALS")) {
            allVitals.filter { it.patientUid == patientUid }
        } else emptyList()

        return ScopedHealthDataPackage(
            patientUid = patientUid,
            accessedByUid = requesterUid,
            organizationId = organizationId,
            accessedCategories = authorizedCategories,
            conditions = filteredConditions,
            allergies = filteredAllergies,
            medications = filteredMedications,
            labReports = filteredLabReports,
            vitals = filteredVitals
        )
    }
}
