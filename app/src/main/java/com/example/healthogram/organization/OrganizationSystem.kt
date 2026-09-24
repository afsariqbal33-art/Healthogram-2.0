package com.example.healthogram.organization

import com.example.healthogram.core.AccountType

/**
 * Doctor Profile Model.
 */
data class DoctorProfile(
    val uid: String,
    val specialization: String,
    val medicalLicenseNumber: String,
    val yearsOfExperience: Int,
    val affiliatedHospitalOrClinic: String? = null,
    val consultationFee: Double = 50.0,
    val currency: String = "USD",
    val isAvailableForTelehealth: Boolean = true
)

/**
 * Hospital Department.
 */
data class HospitalDepartment(
    val departmentId: String,
    val name: String,
    val headDoctorName: String,
    val activeDoctorCount: Int
)

/**
 * Clinical Laboratory Diagnostic Test.
 */
data class LabTestItem(
    val testCode: String,
    val testName: String,
    val sampleType: String, // Blood, Urine, Swab, Tissue
    val standardTurnaroundHours: Int,
    val price: Double,
    val currency: String = "USD",
    val requiresFasting: Boolean = false
)

/**
 * Healthcare Organization Profile.
 * Supports Clinic, Hospital, and Laboratory (Pharmacy is strictly forbidden).
 */
data class HealthcareOrganizationProfile(
    val organizationUid: String,
    val name: String,
    val accountType: AccountType, // Must be CLINIC, HOSPITAL, or LABORATORY
    val registrationNumber: String,
    val facilityLicenseNumber: String,
    val physicalAddress: String,
    val countryCode: String,
    val departments: List<HospitalDepartment> = emptyList(),
    val labCatalog: List<LabTestItem> = emptyList(),
    val isVerified: Boolean = false
) {
    init {
        require(accountType.isHealthcareOrganization) {
            "HealthcareOrganizationProfile can only be applied to CLINIC, HOSPITAL, or LABORATORY"
        }
    }
}
