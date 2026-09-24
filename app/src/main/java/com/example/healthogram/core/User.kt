package com.example.healthogram.core

/**
 * Platform verification lifecycle status.
 *
 * Supported Statuses (Step 07):
 * - NOT_STARTED
 * - DRAFT
 * - SUBMITTED
 * - UNDER_REVIEW
 * - ADDITIONAL_INFORMATION_REQUIRED
 * - VERIFIED (APPROVED)
 * - REJECTED
 * - SUSPENDED
 * - EXPIRED
 * - REVOKED
 */
enum class VerificationStatus(val displayName: String) {
    NOT_STARTED("Not Started"),
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    ADDITIONAL_INFORMATION_REQUIRED("Additional Information Required"),
    VERIFIED("Verified"),
    APPROVED("Verified"), // Backwards compatibility alias for VERIFIED
    REJECTED("Rejected"),
    SUSPENDED("Suspended"),
    EXPIRED("Expired"),
    REVOKED("Revoked");

    val isVerifiedState: Boolean
        get() = this == VERIFIED || this == APPROVED
}

/**
 * Controlled Account Lifecycle Status.
 */
enum class AccountStatus(val displayName: String) {
    ACTIVE("Active"),
    PENDING("Pending Verification"),
    SUSPENDED("Suspended"),
    RESTRICTED("Restricted"),
    DEACTIVATED("Deactivated"),
    DELETED("Deleted")
}

/**
 * Verification badge visuals.
 */
enum class VerificationBadgeType {
    NONE,
    CYAN_CHECK,      // Verified individual or doctor
    GOLD_SHIELD,     // Certified clinical institution / hospital
    GREEN_CROSS      // Accredited diagnostic laboratory
}

/**
 * Healthogram User Profile Data Model.
 *
 * Mapped to Firestore collection: `users/{uid}`.
 *
 * Designed in strict accordance with Master Architecture & Step 03:
 * - Does NOT contain passwords or raw authentication credentials.
 * - Does NOT contain full Health Passport records or raw medical history.
 * - Does NOT contain sensitive national ID or verification license documents.
 */
data class User(
    // IDENTITY
    val uid: String,
    val email: String = "",
    val phoneNumber: String = "",

    // PROFILE
    val displayName: String = "",
    val username: String = "",
    val photoUrl: String? = null,
    val bio: String = "",

    // ACCOUNT
    val accountType: AccountType = AccountType.INDIVIDUAL,
    val isProfessional: Boolean = false,
    val accountStatus: AccountStatus = AccountStatus.ACTIVE,

    // LOCATION
    val countryCode: String = "US",
    val countryName: String = "United States",
    val city: String = "",
    val languageCode: String = "en",
    val currencyCode: String = "USD",
    val timezone: String = "UTC",

    // VERIFICATION
    val verificationStatus: VerificationStatus = VerificationStatus.NOT_STARTED,
    val isVerified: Boolean = false,
    val verificationBadgeType: VerificationBadgeType = VerificationBadgeType.NONE,
    val verificationSubmittedAt: Long? = null,
    val verificationApprovedAt: Long? = null,
    val verificationExpiresAt: Long? = null,

    // SECURITY
    val isActive: Boolean = true,
    val isSuspended: Boolean = false,
    val suspensionReason: String? = null,
    val lastLoginAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // PREFERENCES
    val notificationsEnabled: Boolean = true,
    val languagePreference: String = "en",
    val themePreference: String = "system",

    // MARKETPLACE (Completely separated from primary account categories)
    val marketplaceRole: MarketplaceRole = MarketplaceRole.NONE,
    val isMarketplaceEnabled: Boolean = false,

    // DEVICE
    val activeDeviceCount: Int = 1,

    // TERMS & PRIVACY
    val termsAccepted: Boolean = true,
    val termsVersion: String = "1.0",
    val privacyAccepted: Boolean = true,
    val privacyVersion: String = "1.0",
    val acceptedAt: Long = System.currentTimeMillis(),
    val isOnboardingComplete: Boolean = true
) {
    init {
        // Enforce account category integrity
        require(AccountType.isAllowed(accountType.name)) {
            "Invalid account type for user $uid"
        }
    }

    /**
     * Converts User to Firestore document data.
     * Guaranteed never to include passwords, credentials, or private health data.
     */
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "email" to email,
        "phoneNumber" to phoneNumber,
        "displayName" to displayName,
        "username" to username,
        "photoUrl" to photoUrl,
        "bio" to bio,
        "accountType" to accountType.name.lowercase(),
        "isProfessional" to isProfessional,
        "accountStatus" to accountStatus.name.lowercase(),
        "countryCode" to countryCode,
        "countryName" to countryName,
        "city" to city,
        "languageCode" to languageCode,
        "currencyCode" to currencyCode,
        "timezone" to timezone,
        "verificationStatus" to when (verificationStatus) {
            VerificationStatus.APPROVED, VerificationStatus.VERIFIED -> "verified"
            else -> verificationStatus.name.lowercase()
        },
        "isVerified" to (isVerified && verificationStatus.isVerifiedState),
        "verificationBadgeType" to verificationBadgeType.name.lowercase(),
        "verificationSubmittedAt" to verificationSubmittedAt,
        "verificationApprovedAt" to verificationApprovedAt,
        "verificationExpiresAt" to verificationExpiresAt,
        "isActive" to isActive,
        "isSuspended" to isSuspended,
        "suspensionReason" to suspensionReason,
        "lastLoginAt" to lastLoginAt,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "notificationsEnabled" to notificationsEnabled,
        "languagePreference" to languagePreference,
        "themePreference" to themePreference,
        "marketplaceRole" to marketplaceRole.name.lowercase(),
        "isMarketplaceEnabled" to isMarketplaceEnabled,
        "activeDeviceCount" to activeDeviceCount,
        "termsAccepted" to termsAccepted,
        "termsVersion" to termsVersion,
        "privacyAccepted" to privacyAccepted,
        "privacyVersion" to privacyVersion,
        "acceptedAt" to acceptedAt,
        "isOnboardingComplete" to isOnboardingComplete
    )

    /**
     * Determines if user is eligible to scan patient Health Passport QR codes.
     * Section 34 Rule:
     * Scanner can open ONLY if:
     * 1. Account type is: Doctor, Clinic, Hospital, Laboratory
     * 2. verification_status == verified
     * 3. verification has not expired
     * 4. account is not suspended / revoked
     */
    val isEligibleToScanHealthPassport: Boolean
        get() {
            val isVerifiedState = (isVerified || verificationStatus.isVerifiedState) &&
                    verificationStatus != VerificationStatus.REJECTED &&
                    verificationStatus != VerificationStatus.REVOKED
            val notExpired = verificationExpiresAt == null || System.currentTimeMillis() < verificationExpiresAt
            val notSuspendedOrRevoked = !isSuspended &&
                    verificationStatus != VerificationStatus.SUSPENDED &&
                    verificationStatus != VerificationStatus.REVOKED &&
                    accountStatus == AccountStatus.ACTIVE
            val allowedType = accountType in setOf(
                AccountType.DOCTOR,
                AccountType.CLINIC,
                AccountType.HOSPITAL,
                AccountType.LABORATORY
            )
            return isVerifiedState && notExpired && notSuspendedOrRevoked && allowedType
        }

    /**
     * Determines if user is eligible to own a personal Health Passport.
     * Rule: Individuals, Doctors, Clinics, and Hospitals have health records.
     * Section 27 Rule: Laboratory must NOT receive a personal Health Passport.
     */
    val isEligibleForPersonalHealthPassport: Boolean
        get() = accountType != AccountType.LABORATORY

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>): User {
            val accountTypeStr = data["accountType"] as? String ?: "individual"
            val accountType = try {
                AccountType.fromString(accountTypeStr)
            } catch (_: Exception) {
                AccountType.INDIVIDUAL
            }

            val accountStatusStr = data["accountStatus"] as? String ?: "active"
            val accountStatus = try {
                AccountStatus.valueOf(accountStatusStr.uppercase())
            } catch (_: Exception) {
                AccountStatus.ACTIVE
            }

            val verificationStatusStr = data["verificationStatus"] as? String ?: "not_started"
            val verificationStatus = when (verificationStatusStr.lowercase()) {
                "verified", "approved" -> VerificationStatus.VERIFIED
                "additional_information_required" -> VerificationStatus.ADDITIONAL_INFORMATION_REQUIRED
                "under_review" -> VerificationStatus.UNDER_REVIEW
                "submitted" -> VerificationStatus.SUBMITTED
                "draft" -> VerificationStatus.DRAFT
                "rejected" -> VerificationStatus.REJECTED
                "suspended" -> VerificationStatus.SUSPENDED
                "expired" -> VerificationStatus.EXPIRED
                "revoked" -> VerificationStatus.REVOKED
                else -> VerificationStatus.NOT_STARTED
            }

            val marketplaceRoleStr = data["marketplaceRole"] as? String ?: "none"
            val marketplaceRole = try {
                MarketplaceRole.valueOf(marketplaceRoleStr.uppercase())
            } catch (_: Exception) {
                MarketplaceRole.NONE
            }

            return User(
                uid = data["uid"] as? String ?: "",
                email = data["email"] as? String ?: "",
                phoneNumber = data["phoneNumber"] as? String ?: "",
                displayName = data["displayName"] as? String ?: "",
                username = data["username"] as? String ?: "",
                photoUrl = data["photoUrl"] as? String,
                bio = data["bio"] as? String ?: "",
                accountType = accountType,
                isProfessional = data["isProfessional"] as? Boolean ?: false,
                accountStatus = accountStatus,
                countryCode = data["countryCode"] as? String ?: "US",
                countryName = data["countryName"] as? String ?: "United States",
                city = data["city"] as? String ?: "",
                languageCode = data["languageCode"] as? String ?: "en",
                currencyCode = data["currencyCode"] as? String ?: "USD",
                timezone = data["timezone"] as? String ?: "UTC",
                verificationStatus = verificationStatus,
                isVerified = data["isVerified"] as? Boolean ?: false,
                verificationBadgeType = try {
                    VerificationBadgeType.valueOf((data["verificationBadgeType"] as? String ?: "NONE").uppercase())
                } catch (_: Exception) {
                    VerificationBadgeType.NONE
                },
                verificationSubmittedAt = (data["verificationSubmittedAt"] as? Number)?.toLong(),
                verificationApprovedAt = (data["verificationApprovedAt"] as? Number)?.toLong(),
                verificationExpiresAt = (data["verificationExpiresAt"] as? Number)?.toLong(),
                isActive = data["isActive"] as? Boolean ?: true,
                isSuspended = data["isSuspended"] as? Boolean ?: false,
                suspensionReason = data["suspensionReason"] as? String,
                lastLoginAt = (data["lastLoginAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                notificationsEnabled = data["notificationsEnabled"] as? Boolean ?: true,
                languagePreference = data["languagePreference"] as? String ?: "en",
                themePreference = data["themePreference"] as? String ?: "system",
                marketplaceRole = marketplaceRole,
                isMarketplaceEnabled = data["isMarketplaceEnabled"] as? Boolean ?: false,
                activeDeviceCount = (data["activeDeviceCount"] as? Number)?.toInt() ?: 1,
                termsAccepted = data["termsAccepted"] as? Boolean ?: true,
                termsVersion = data["termsVersion"] as? String ?: "1.0",
                privacyAccepted = data["privacyAccepted"] as? Boolean ?: true,
                privacyVersion = data["privacyVersion"] as? String ?: "1.0",
                acceptedAt = (data["acceptedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                isOnboardingComplete = data["isOnboardingComplete"] as? Boolean ?: true
            )
        }
    }
}
