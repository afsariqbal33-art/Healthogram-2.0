package com.example.healthogram.verification

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.VerificationStatus

/**
 * Types of documents used during platform verification.
 */
enum class DocumentType(val displayName: String) {
    NATIONAL_ID("National Identity Card / Passport"),
    MEDICAL_LICENSE("Professional Medical / Doctor License"),
    BUSINESS_REGISTRATION("Commercial / Business Registration Certificate"),
    HEALTHCARE_FACILITY_LICENSE("Government Healthcare Facility License"),
    LABORATORY_LICENSE("Clinical Laboratory Operation Accreditation"),
    TAX_IDENTIFICATION("Taxpayer Identification / VAT Certificate")
}

/**
 * Requirement entry for verification.
 */
data class VerificationRequirement(
    val id: String,
    val documentType: DocumentType,
    val isMandatory: Boolean = true,
    val description: String,
    val countrySpecificAuthority: String? = null
)

/**
 * Country-aware Verification Rules Engine.
 * Dynamically configures mandatory compliance documentation per country.
 */
class CountryVerificationEngine {

    companion object {
        private val GLOBAL_BASE_REQUIREMENTS = mapOf(
            AccountType.INDIVIDUAL to listOf(
                VerificationRequirement(
                    id = "ind_national_id",
                    documentType = DocumentType.NATIONAL_ID,
                    description = "Government issued National ID or Passport"
                )
            ),
            AccountType.DOCTOR to listOf(
                VerificationRequirement(
                    id = "doc_national_id",
                    documentType = DocumentType.NATIONAL_ID,
                    description = "Government issued National ID"
                ),
                VerificationRequirement(
                    id = "doc_med_license",
                    documentType = DocumentType.MEDICAL_LICENSE,
                    description = "Official Doctor Medical Practice License"
                )
            ),
            AccountType.CLINIC to listOf(
                VerificationRequirement(
                    id = "cli_national_id",
                    documentType = DocumentType.NATIONAL_ID,
                    description = "Authorized Representative National ID"
                ),
                VerificationRequirement(
                    id = "cli_biz_reg",
                    documentType = DocumentType.BUSINESS_REGISTRATION,
                    description = "Clinic Commercial Registration"
                ),
                VerificationRequirement(
                    id = "cli_health_lic",
                    documentType = DocumentType.HEALTHCARE_FACILITY_LICENSE,
                    description = "Outpatient Clinic Operating License"
                )
            ),
            AccountType.HOSPITAL to listOf(
                VerificationRequirement(
                    id = "hosp_national_id",
                    documentType = DocumentType.NATIONAL_ID,
                    description = "Authorized Administrator National ID"
                ),
                VerificationRequirement(
                    id = "hosp_biz_reg",
                    documentType = DocumentType.BUSINESS_REGISTRATION,
                    description = "Hospital Enterprise Commercial Registration"
                ),
                VerificationRequirement(
                    id = "hosp_lic",
                    documentType = DocumentType.HEALTHCARE_FACILITY_LICENSE,
                    description = "Hospital Operating Accreditation / License"
                )
            ),
            AccountType.LABORATORY to listOf(
                VerificationRequirement(
                    id = "lab_national_id",
                    documentType = DocumentType.NATIONAL_ID,
                    description = "Authorized Director National ID"
                ),
                VerificationRequirement(
                    id = "lab_biz_reg",
                    documentType = DocumentType.BUSINESS_REGISTRATION,
                    description = "Laboratory Business Registration"
                ),
                VerificationRequirement(
                    id = "lab_lic",
                    documentType = DocumentType.LABORATORY_LICENSE,
                    description = "Clinical Diagnostic Laboratory Accreditation"
                )
            )
        )
    }

    /**
     * Retrieves verification requirements for a given account type and country.
     * Enforces that PHARMACY is completely forbidden.
     */
    fun getRequirements(accountType: AccountType, countryCode: String): List<VerificationRequirement> {
        val base = GLOBAL_BASE_REQUIREMENTS[accountType] ?: emptyList()

        // Country-specific augmentations
        return when (countryCode.uppercase()) {
            "US" -> base.map { req ->
                if (req.documentType == DocumentType.MEDICAL_LICENSE) {
                    req.copy(countrySpecificAuthority = "State Medical Board & NPI Registry")
                } else req
            }
            "GB" -> base.map { req ->
                if (req.documentType == DocumentType.MEDICAL_LICENSE) {
                    req.copy(countrySpecificAuthority = "General Medical Council (GMC)")
                } else req
            }
            "AE" -> base.map { req ->
                if (req.documentType == DocumentType.MEDICAL_LICENSE) {
                    req.copy(countrySpecificAuthority = "DHA / MOHAP / DoH Healthcare Licensing")
                } else req
            }
            "SA" -> base.map { req ->
                if (req.documentType == DocumentType.MEDICAL_LICENSE) {
                    req.copy(countrySpecificAuthority = "Saudi Commission for Health Specialties (SCFHS)")
                } else req
            }
            else -> base
        }
    }

    /**
     * Validates if a submission satisfies all mandatory documents for the country and account type.
     */
    fun validateSubmission(
        accountType: AccountType,
        countryCode: String,
        providedDocs: Set<DocumentType>
    ): Boolean {
        val requirements = getRequirements(accountType, countryCode)
        val mandatoryTypes = requirements.filter { it.isMandatory }.map { it.documentType }.toSet()
        return providedDocs.containsAll(mandatoryTypes)
    }
}
