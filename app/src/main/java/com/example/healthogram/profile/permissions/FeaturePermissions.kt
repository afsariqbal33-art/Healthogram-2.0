package com.example.healthogram.profile.permissions

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.profile.model.OrganizationDevicePermissions
import com.example.healthogram.profile.model.OrganizationRole

/**
 * Healthogram Core Platform Feature Permission Keys.
 * Strictly enforced across UI, ViewModels, and backend security guards.
 */
enum class FeatureKey {
    VIEW_PROFILE,
    EDIT_PROFILE,
    CREATE_POST,
    CREATE_REEL,
    CREATE_VIDEO,
    CREATE_STORY,
    GO_LIVE,
    MESSAGE,
    AUDIO_CALL,
    VIDEO_CALL,
    FOLLOW,
    HEALTH_PASSPORT_OWN,
    HEALTH_PASSPORT_SCAN,
    HEALTH_PASSPORT_REQUEST_ACCESS,
    APPOINTMENTS,
    CONSULTATIONS,
    SERVICES,
    TEST_MANAGEMENT,
    DOCTOR_MANAGEMENT,
    HOSPITAL_MANAGEMENT,
    CLINIC_MANAGEMENT,
    LABORATORY_MANAGEMENT,
    MARKETPLACE,
    AI_STUDIO
}

/**
 * Result of a permission check with reason for denial.
 */
data class PermissionResult(
    val isGranted: Boolean,
    val reason: String? = null
) {
    companion object {
        fun granted(): PermissionResult = PermissionResult(true)
        fun denied(reason: String): PermissionResult = PermissionResult(false, reason)
    }
}

/**
 * Feature Permission Authorization Engine.
 *
 * Implements strict, server-aligned business logic:
 * - Scanner access requires: accountType in (doctor, clinic, hospital, laboratory) AND verified AND scannerPermission enabled.
 * - Laboratory accounts strictly do NOT own personal health passports.
 * - Organization management requires authorized organization role.
 * - Device-level permission restrictions applied when device profile is passed.
 */
object FeaturePermissionEngine {

    fun hasPermission(
        user: User?,
        feature: FeatureKey,
        isVerified: Boolean = user?.isVerified ?: false,
        scannerPermission: Boolean = false,
        orgRole: OrganizationRole? = null,
        devicePermissions: OrganizationDevicePermissions? = null
    ): Boolean {
        return checkPermission(
            feature = feature,
            user = user,
            isVerified = isVerified,
            scannerPermission = scannerPermission,
            orgRole = orgRole,
            devicePermissions = devicePermissions
        ).isGranted
    }

    fun checkPermission(
        feature: FeatureKey,
        user: User?,
        isVerified: Boolean = user?.isVerified ?: false,
        scannerPermission: Boolean = false,
        orgRole: OrganizationRole? = null,
        devicePermissions: OrganizationDevicePermissions? = null
    ): PermissionResult {
        if (user == null) {
            return PermissionResult.denied("Authentication required")
        }

        if (user.isSuspended || !user.isActive) {
            return PermissionResult.denied("Account is suspended or deactivated")
        }

        // 1. Device-level gating (if device policies are active)
        if (devicePermissions != null) {
            val deviceAllowed = when (feature) {
                FeatureKey.MESSAGE -> devicePermissions.textMessaging
                FeatureKey.AUDIO_CALL -> devicePermissions.audioCalling
                FeatureKey.VIDEO_CALL -> devicePermissions.videoCalling
                FeatureKey.APPOINTMENTS -> devicePermissions.appointments
                FeatureKey.CONSULTATIONS -> devicePermissions.consultations
                FeatureKey.MARKETPLACE -> devicePermissions.marketplace
                FeatureKey.AI_STUDIO -> devicePermissions.aiStudio
                FeatureKey.CLINIC_MANAGEMENT,
                FeatureKey.HOSPITAL_MANAGEMENT,
                FeatureKey.LABORATORY_MANAGEMENT -> devicePermissions.management
                FeatureKey.CREATE_POST,
                FeatureKey.CREATE_REEL,
                FeatureKey.CREATE_STORY,
                FeatureKey.CREATE_VIDEO -> devicePermissions.contentCreation
                else -> true
            }
            if (!deviceAllowed) {
                return PermissionResult.denied("Feature is disabled on this registered hardware device")
            }
        }

        // 2. Feature-specific business rules
        return when (feature) {
            FeatureKey.VIEW_PROFILE -> PermissionResult.granted()
            FeatureKey.EDIT_PROFILE -> PermissionResult.granted()

            FeatureKey.CREATE_POST,
            FeatureKey.CREATE_REEL,
            FeatureKey.CREATE_VIDEO,
            FeatureKey.CREATE_STORY -> PermissionResult.granted()

            FeatureKey.GO_LIVE -> {
                if (isVerified || user.isProfessional) {
                    PermissionResult.granted()
                } else {
                    PermissionResult.denied("Live streaming requires verified or professional creator status")
                }
            }

            FeatureKey.MESSAGE -> PermissionResult.granted()
            FeatureKey.AUDIO_CALL -> PermissionResult.granted()
            FeatureKey.VIDEO_CALL -> PermissionResult.granted()
            FeatureKey.FOLLOW -> PermissionResult.granted()

            // CRITICAL RULE: Laboratory accounts strictly do NOT receive a personal Health Passport.
            FeatureKey.HEALTH_PASSPORT_OWN -> {
                if (user.accountType == AccountType.LABORATORY) {
                    PermissionResult.denied("Diagnostic Laboratories manage test catalogs and report delivery; they do not hold personal patient passports.")
                } else {
                    PermissionResult.granted()
                }
            }

            // CRITICAL RULE: Health Passport Scanner requires:
            // accountType in (DOCTOR, CLINIC, HOSPITAL, LABORATORY) AND isVerified == true AND scannerPermission == true
            FeatureKey.HEALTH_PASSPORT_SCAN -> {
                val eligibleAccount = user.accountType in setOf(
                    AccountType.DOCTOR,
                    AccountType.CLINIC,
                    AccountType.HOSPITAL,
                    AccountType.LABORATORY
                )
                if (!eligibleAccount) {
                    return PermissionResult.denied("Health Passport Scanner is restricted to verified healthcare professionals and organizations.")
                }
                if (!isVerified) {
                    return PermissionResult.denied("Professional verification is required before Health Passport Scanner access is granted.")
                }
                if (!scannerPermission) {
                    return PermissionResult.denied("Health Passport Scanner permission is currently not activated on this account.")
                }
                PermissionResult.granted()
            }

            FeatureKey.HEALTH_PASSPORT_REQUEST_ACCESS -> {
                if (user.accountType in setOf(AccountType.DOCTOR, AccountType.CLINIC, AccountType.HOSPITAL, AccountType.LABORATORY) && isVerified) {
                    PermissionResult.granted()
                } else {
                    PermissionResult.denied("Only verified practitioners and healthcare establishments can request patient medical record access.")
                }
            }

            FeatureKey.APPOINTMENTS -> {
                if (user.accountType in setOf(AccountType.DOCTOR, AccountType.CLINIC, AccountType.HOSPITAL)) {
                    PermissionResult.granted()
                } else {
                    PermissionResult.denied("Appointment scheduling is available for clinical practitioners and facilities.")
                }
            }

            FeatureKey.CONSULTATIONS -> {
                if (user.accountType in setOf(AccountType.DOCTOR, AccountType.CLINIC, AccountType.HOSPITAL)) {
                    PermissionResult.granted()
                } else {
                    PermissionResult.denied("Telehealth consultations are available for doctors, clinics, and hospitals.")
                }
            }

            FeatureKey.SERVICES -> {
                if (user.accountType != AccountType.INDIVIDUAL || user.isProfessional) {
                    PermissionResult.granted()
                } else {
                    PermissionResult.denied("Service catalogs are available for professional and organization profiles.")
                }
            }

            FeatureKey.TEST_MANAGEMENT,
            FeatureKey.LABORATORY_MANAGEMENT -> {
                if (user.accountType == AccountType.LABORATORY) {
                    PermissionResult.granted()
                } else {
                    PermissionResult.denied("Laboratory test management is restricted to laboratory accounts.")
                }
            }

            FeatureKey.DOCTOR_MANAGEMENT -> {
                if (user.accountType in setOf(AccountType.CLINIC, AccountType.HOSPITAL) &&
                    (orgRole == null || orgRole in setOf(OrganizationRole.OWNER, OrganizationRole.ADMIN, OrganizationRole.MANAGER))) {
                    PermissionResult.granted()
                } else {
                    PermissionResult.denied("Doctor roster management is restricted to clinic and hospital administrators.")
                }
            }

            FeatureKey.CLINIC_MANAGEMENT -> {
                if (user.accountType == AccountType.CLINIC &&
                    (orgRole == null || orgRole in setOf(OrganizationRole.OWNER, OrganizationRole.ADMIN, OrganizationRole.MANAGER))) {
                    PermissionResult.granted()
                } else {
                    PermissionResult.denied("Clinic management requires clinic administrative permissions.")
                }
            }

            FeatureKey.HOSPITAL_MANAGEMENT -> {
                if (user.accountType == AccountType.HOSPITAL &&
                    (orgRole == null || orgRole in setOf(OrganizationRole.OWNER, OrganizationRole.ADMIN, OrganizationRole.MANAGER))) {
                    PermissionResult.granted()
                } else {
                    PermissionResult.denied("Hospital management requires hospital administrative permissions.")
                }
            }

            FeatureKey.MARKETPLACE -> {
                if (user.isMarketplaceEnabled) {
                    PermissionResult.granted()
                } else {
                    PermissionResult.denied("Marketplace access is currently inactive for this region or profile.")
                }
            }

            FeatureKey.AI_STUDIO -> PermissionResult.granted()
        }
    }
}
