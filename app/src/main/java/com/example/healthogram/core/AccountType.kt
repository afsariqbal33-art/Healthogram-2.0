package com.example.healthogram.core

/**
 * Healthogram Primary Account Categories.
 *
 * CRITICAL ARCHITECTURAL CONSTRAINT:
 * Only exactly these five primary account categories are permitted:
 * 1. INDIVIDUAL
 * 2. DOCTOR
 * 3. CLINIC
 * 4. HOSPITAL
 * 5. LABORATORY
 *
 * PHARMACY MUST NOT EXIST as a primary Healthogram account category anywhere in the system.
 */
enum class AccountType(val displayName: String, val isHealthcareOrganization: Boolean) {
    INDIVIDUAL("Individual", false),
    DOCTOR("Doctor", false),
    CLINIC("Clinic", true),
    HOSPITAL("Hospital", true),
    LABORATORY("Laboratory", true);

    companion object {
        const val FORBIDDEN_PHARMACY_IDENTIFIER = "PHARMACY"

        /**
         * Validates whether a category name is an allowed Healthogram account type.
         * Explicitly rejects "PHARMACY" and any non-approved category.
         */
        fun isAllowed(name: String?): Boolean {
            if (name.isNullOrBlank()) return false
            if (name.trim().equals(FORBIDDEN_PHARMACY_IDENTIFIER, ignoreCase = true)) {
                return false
            }
            return entries.any { it.name.equals(name.trim(), ignoreCase = true) }
        }

        fun fromString(name: String?): AccountType {
            if (name != null && name.trim().equals(FORBIDDEN_PHARMACY_IDENTIFIER, ignoreCase = true)) {
                throw SecurityException("Architectural Violation: 'PHARMACY' is forbidden as a Healthogram account category.")
            }
            return entries.firstOrNull { it.name.equals(name?.trim(), ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown or disallowed account type: $name")
        }
    }
}

/**
 * Marketplace roles are completely separated from primary Healthogram account types.
 */
enum class MarketplaceRole {
    NONE,
    CUSTOMER,
    SELLER
}
