package com.example.healthogram.profile.model

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.organization.HospitalDepartment
import com.example.healthogram.organization.LabTestItem

/**
 * Public Profile Data Model.
 * Mapped to Firestore collection: `public_profiles/{uid}`.
 * Only data safe and intended for public social discovery belongs here.
 * Sensitive documents, national IDs, medical histories, and passwords MUST NOT exist here.
 */
data class PublicProfile(
    val uid: String,
    val username: String = "",
    val normalizedUsername: String = "",
    val displayName: String = "",
    val profilePhotoUrl: String? = null,
    val coverPhotoUrl: String? = null,
    val bio: String = "",
    val accountType: AccountType = AccountType.INDIVIDUAL,
    val countryCode: String = "US",
    val city: String = "",
    val website: String = "",
    val publicEmail: String = "",
    val publicPhone: String = "",
    val isPrivateAccount: Boolean = false,
    val isProfessional: Boolean = false,
    val isVerified: Boolean = false,
    val verificationType: String = "standard",
    val followersCount: Long = 0L,
    val followingCount: Long = 0L,
    val postsCount: Long = 0L,
    val reelsCount: Long = 0L,
    val videosCount: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    init {
        require(AccountType.isAllowed(accountType.name)) {
            "Architectural Violation: Disallowed account type ${accountType.name}"
        }
    }

    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "username" to username,
        "normalizedUsername" to normalizedUsername.ifBlank { username.lowercase().trim() },
        "displayName" to displayName,
        "profilePhotoUrl" to profilePhotoUrl,
        "coverPhotoUrl" to coverPhotoUrl,
        "bio" to bio,
        "accountType" to accountType.name.lowercase(),
        "countryCode" to countryCode,
        "city" to city,
        "website" to website,
        "publicEmail" to publicEmail,
        "publicPhone" to publicPhone,
        "isPrivateAccount" to isPrivateAccount,
        "isProfessional" to isProfessional,
        "isVerified" to isVerified,
        "verificationType" to verificationType,
        "followersCount" to followersCount,
        "followingCount" to followingCount,
        "postsCount" to postsCount,
        "reelsCount" to reelsCount,
        "videosCount" to videosCount,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>): PublicProfile {
            val accountTypeStr = data["accountType"] as? String ?: "individual"
            val accountType = try {
                AccountType.fromString(accountTypeStr)
            } catch (_: Exception) {
                AccountType.INDIVIDUAL
            }

            return PublicProfile(
                uid = data["uid"] as? String ?: "",
                username = data["username"] as? String ?: "",
                normalizedUsername = data["normalizedUsername"] as? String ?: "",
                displayName = data["displayName"] as? String ?: "",
                profilePhotoUrl = data["profilePhotoUrl"] as? String,
                coverPhotoUrl = data["coverPhotoUrl"] as? String,
                bio = data["bio"] as? String ?: "",
                accountType = accountType,
                countryCode = data["countryCode"] as? String ?: "US",
                city = data["city"] as? String ?: "",
                website = data["website"] as? String ?: "",
                publicEmail = data["publicEmail"] as? String ?: "",
                publicPhone = data["publicPhone"] as? String ?: "",
                isPrivateAccount = data["isPrivateAccount"] as? Boolean ?: false,
                isProfessional = data["isProfessional"] as? Boolean ?: false,
                isVerified = data["isVerified"] as? Boolean ?: false,
                verificationType = data["verificationType"] as? String ?: "standard",
                followersCount = (data["followersCount"] as? Number)?.toLong() ?: 0L,
                followingCount = (data["followingCount"] as? Number)?.toLong() ?: 0L,
                postsCount = (data["postsCount"] as? Number)?.toLong() ?: 0L,
                reelsCount = (data["reelsCount"] as? Number)?.toLong() ?: 0L,
                videosCount = (data["videosCount"] as? Number)?.toLong() ?: 0L,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * Audience levels for privacy controls.
 */
enum class PrivacyAudience(val displayName: String) {
    EVERYONE("Everyone"),
    FOLLOWERS_ONLY("Followers Only"),
    VERIFIED_ONLY("Verified Accounts Only"),
    NOBODY("No One")
}

/**
 * Privacy Settings Model.
 */
data class ProfilePrivacySettings(
    val isPrivateAccount: Boolean = false,
    val whoCanMessage: PrivacyAudience = PrivacyAudience.EVERYONE,
    val whoCanCall: PrivacyAudience = PrivacyAudience.EVERYONE,
    val whoCanVideoCall: PrivacyAudience = PrivacyAudience.EVERYONE,
    val whoCanFollow: PrivacyAudience = PrivacyAudience.EVERYONE,
    val showActivityStatus: Boolean = true,
    val allowTagging: Boolean = true
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "isPrivateAccount" to isPrivateAccount,
        "whoCanMessage" to whoCanMessage.name,
        "whoCanCall" to whoCanCall.name,
        "whoCanVideoCall" to whoCanVideoCall.name,
        "whoCanFollow" to whoCanFollow.name,
        "showActivityStatus" to showActivityStatus,
        "allowTagging" to allowTagging
    )

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>?): ProfilePrivacySettings {
            if (data == null) return ProfilePrivacySettings()
            return ProfilePrivacySettings(
                isPrivateAccount = data["isPrivateAccount"] as? Boolean ?: false,
                whoCanMessage = try { PrivacyAudience.valueOf(data["whoCanMessage"] as? String ?: "EVERYONE") } catch (_: Exception) { PrivacyAudience.EVERYONE },
                whoCanCall = try { PrivacyAudience.valueOf(data["whoCanCall"] as? String ?: "EVERYONE") } catch (_: Exception) { PrivacyAudience.EVERYONE },
                whoCanVideoCall = try { PrivacyAudience.valueOf(data["whoCanVideoCall"] as? String ?: "EVERYONE") } catch (_: Exception) { PrivacyAudience.EVERYONE },
                whoCanFollow = try { PrivacyAudience.valueOf(data["whoCanFollow"] as? String ?: "EVERYONE") } catch (_: Exception) { PrivacyAudience.EVERYONE },
                showActivityStatus = data["showActivityStatus"] as? Boolean ?: true,
                allowTagging = data["allowTagging"] as? Boolean ?: true
            )
        }
    }
}

/**
 * Communication Settings Model.
 */
data class CommunicationSettings(
    val audioCallsEnabled: Boolean = true,
    val videoCallsEnabled: Boolean = true,
    val messagesEnabled: Boolean = true,
    val callQualityMode: String = "HD",
    val allowEmergencyBypass: Boolean = true,
    val pushNotificationsCalls: Boolean = true,
    val pushNotificationsMessages: Boolean = true
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "audioCallsEnabled" to audioCallsEnabled,
        "videoCallsEnabled" to videoCallsEnabled,
        "messagesEnabled" to messagesEnabled,
        "callQualityMode" to callQualityMode,
        "allowEmergencyBypass" to allowEmergencyBypass,
        "pushNotificationsCalls" to pushNotificationsCalls,
        "pushNotificationsMessages" to pushNotificationsMessages
    )

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>?): CommunicationSettings {
            if (data == null) return CommunicationSettings()
            return CommunicationSettings(
                audioCallsEnabled = data["audioCallsEnabled"] as? Boolean ?: true,
                videoCallsEnabled = data["videoCallsEnabled"] as? Boolean ?: true,
                messagesEnabled = data["messagesEnabled"] as? Boolean ?: true,
                callQualityMode = data["callQualityMode"] as? String ?: "HD",
                allowEmergencyBypass = data["allowEmergencyBypass"] as? Boolean ?: true,
                pushNotificationsCalls = data["pushNotificationsCalls"] as? Boolean ?: true,
                pushNotificationsMessages = data["pushNotificationsMessages"] as? Boolean ?: true
            )
        }
    }
}

/**
 * Security Preferences Model.
 */
data class SecurityPreferences(
    val twoFactorEnabled: Boolean = false,
    val loginAlertsEnabled: Boolean = true,
    val biometricUnlockEnabled: Boolean = false,
    val autoRevokeInactiveDevicesDays: Int = 30
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "twoFactorEnabled" to twoFactorEnabled,
        "loginAlertsEnabled" to loginAlertsEnabled,
        "biometricUnlockEnabled" to biometricUnlockEnabled,
        "autoRevokeInactiveDevicesDays" to autoRevokeInactiveDevicesDays
    )

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>?): SecurityPreferences {
            if (data == null) return SecurityPreferences()
            return SecurityPreferences(
                twoFactorEnabled = data["twoFactorEnabled"] as? Boolean ?: false,
                loginAlertsEnabled = data["loginAlertsEnabled"] as? Boolean ?: true,
                biometricUnlockEnabled = data["biometricUnlockEnabled"] as? Boolean ?: false,
                autoRevokeInactiveDevicesDays = (data["autoRevokeInactiveDevicesDays"] as? Number)?.toInt() ?: 30
            )
        }
    }
}

/**
 * Emergency Contact Model.
 */
data class EmergencyContactInfo(
    val name: String = "",
    val relationship: String = "",
    val phoneNumber: String = ""
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "relationship" to relationship,
        "phoneNumber" to phoneNumber
    )

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>?): EmergencyContactInfo? {
            if (data == null) return null
            return EmergencyContactInfo(
                name = data["name"] as? String ?: "",
                relationship = data["relationship"] as? String ?: "",
                phoneNumber = data["phoneNumber"] as? String ?: ""
            )
        }
    }
}

/**
 * Private Profile Data Model.
 * Mapped to Firestore collection: `private_profiles/{uid}`.
 * Strictly confidential. Accessible ONLY by the authenticated account owner or authorized workflows.
 */
data class PrivateProfile(
    val uid: String,
    val dateOfBirth: String = "",
    val gender: String = "",
    val privatePhone: String = "",
    val privateEmail: String = "",
    val address: String = "",
    val postalCode: String = "",
    val emergencyContact: EmergencyContactInfo? = null,
    val privacySettings: ProfilePrivacySettings = ProfilePrivacySettings(),
    val communicationSettings: CommunicationSettings = CommunicationSettings(),
    val securityPreferences: SecurityPreferences = SecurityPreferences(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "dateOfBirth" to dateOfBirth,
        "gender" to gender,
        "privatePhone" to privatePhone,
        "privateEmail" to privateEmail,
        "address" to address,
        "postalCode" to postalCode,
        "emergencyContact" to emergencyContact?.toFirestoreMap(),
        "privacySettings" to privacySettings.toFirestoreMap(),
        "communicationSettings" to communicationSettings.toFirestoreMap(),
        "securityPreferences" to securityPreferences.toFirestoreMap(),
        "updatedAt" to updatedAt
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestoreMap(data: Map<String, Any?>): PrivateProfile {
            return PrivateProfile(
                uid = data["uid"] as? String ?: "",
                dateOfBirth = data["dateOfBirth"] as? String ?: "",
                gender = data["gender"] as? String ?: "",
                privatePhone = data["privatePhone"] as? String ?: "",
                privateEmail = data["privateEmail"] as? String ?: "",
                address = data["address"] as? String ?: "",
                postalCode = data["postalCode"] as? String ?: "",
                emergencyContact = EmergencyContactInfo.fromFirestoreMap(data["emergencyContact"] as? Map<String, Any?>),
                privacySettings = ProfilePrivacySettings.fromFirestoreMap(data["privacySettings"] as? Map<String, Any?>),
                communicationSettings = CommunicationSettings.fromFirestoreMap(data["communicationSettings"] as? Map<String, Any?>),
                securityPreferences = SecurityPreferences.fromFirestoreMap(data["securityPreferences"] as? Map<String, Any?>),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * Service Item provided by Doctors, Clinics, Hospitals, or Labs.
 */
data class ProfessionalServiceItem(
    val id: String,
    val title: String,
    val description: String = "",
    val durationMinutes: Int = 30,
    val fee: Double = 0.0,
    val currency: String = "USD",
    val isTelehealthAvailable: Boolean = true,
    val category: String = "General Consultation"
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "description" to description,
        "durationMinutes" to durationMinutes,
        "fee" to fee,
        "currency" to currency,
        "isTelehealthAvailable" to isTelehealthAvailable,
        "category" to category
    )

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>): ProfessionalServiceItem = ProfessionalServiceItem(
            id = data["id"] as? String ?: "",
            title = data["title"] as? String ?: "",
            description = data["description"] as? String ?: "",
            durationMinutes = (data["durationMinutes"] as? Number)?.toInt() ?: 30,
            fee = (data["fee"] as? Number)?.toDouble() ?: 0.0,
            currency = data["currency"] as? String ?: "USD",
            isTelehealthAvailable = data["isTelehealthAvailable"] as? Boolean ?: true,
            category = data["category"] as? String ?: "General Consultation"
        )
    }
}

/**
 * Professional Profile Data Model.
 * Mapped to Firestore collection: `professional_profiles/{uid}`.
 * Holds professional credentials, specializations, business hours, and consultation capability.
 */
data class ProfessionalProfile(
    val uid: String,
    val accountType: AccountType = AccountType.DOCTOR,
    val professionalTitle: String = "",
    val professionalBio: String = "",
    val specializations: List<String> = emptyList(),
    val education: List<String> = emptyList(),
    val experienceYears: Int = 0,
    val languages: List<String> = listOf("English"),
    val services: List<ProfessionalServiceItem> = emptyList(),
    val consultationEnabled: Boolean = true,
    val onlineConsultationEnabled: Boolean = true,
    val appointmentEnabled: Boolean = true,
    val defaultConsultationFee: Double = 50.0,
    val currency: String = "USD",
    val professionalWebsite: String = "",
    val professionalEmail: String = "",
    val professionalPhone: String = "",
    val businessHours: String = "Mon-Fri: 09:00 - 17:00",
    val countryCode: String = "US",
    val city: String = "",
    val scannerPermission: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    init {
        require(AccountType.isAllowed(accountType.name)) {
            "Architectural Violation: Disallowed account type ${accountType.name}"
        }
    }

    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "accountType" to accountType.name.lowercase(),
        "professionalTitle" to professionalTitle,
        "professionalBio" to professionalBio,
        "specializations" to specializations,
        "education" to education,
        "experienceYears" to experienceYears,
        "languages" to languages,
        "services" to services.map { it.toFirestoreMap() },
        "consultationEnabled" to consultationEnabled,
        "onlineConsultationEnabled" to onlineConsultationEnabled,
        "appointmentEnabled" to appointmentEnabled,
        "defaultConsultationFee" to defaultConsultationFee,
        "currency" to currency,
        "professionalWebsite" to professionalWebsite,
        "professionalEmail" to professionalEmail,
        "professionalPhone" to professionalPhone,
        "businessHours" to businessHours,
        "countryCode" to countryCode,
        "city" to city,
        "scannerPermission" to scannerPermission,
        "updatedAt" to updatedAt
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestoreMap(data: Map<String, Any?>): ProfessionalProfile {
            val accountTypeStr = data["accountType"] as? String ?: "doctor"
            val accountType = try {
                AccountType.fromString(accountTypeStr)
            } catch (_: Exception) {
                AccountType.DOCTOR
            }

            val servicesList = (data["services"] as? List<Map<String, Any?>>)
                ?.map { ProfessionalServiceItem.fromFirestoreMap(it) }
                ?: emptyList()

            return ProfessionalProfile(
                uid = data["uid"] as? String ?: "",
                accountType = accountType,
                professionalTitle = data["professionalTitle"] as? String ?: "",
                professionalBio = data["professionalBio"] as? String ?: "",
                specializations = (data["specializations"] as? List<String>) ?: emptyList(),
                education = (data["education"] as? List<String>) ?: emptyList(),
                experienceYears = (data["experienceYears"] as? Number)?.toInt() ?: 0,
                languages = (data["languages"] as? List<String>) ?: listOf("English"),
                services = servicesList,
                consultationEnabled = data["consultationEnabled"] as? Boolean ?: true,
                onlineConsultationEnabled = data["onlineConsultationEnabled"] as? Boolean ?: true,
                appointmentEnabled = data["appointmentEnabled"] as? Boolean ?: true,
                defaultConsultationFee = (data["defaultConsultationFee"] as? Number)?.toDouble() ?: 50.0,
                currency = data["currency"] as? String ?: "USD",
                professionalWebsite = data["professionalWebsite"] as? String ?: "",
                professionalEmail = data["professionalEmail"] as? String ?: "",
                professionalPhone = data["professionalPhone"] as? String ?: "",
                businessHours = data["businessHours"] as? String ?: "Mon-Fri: 09:00 - 17:00",
                countryCode = data["countryCode"] as? String ?: "US",
                city = data["city"] as? String ?: "",
                scannerPermission = data["scannerPermission"] as? Boolean ?: false,
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * Roles inside Clinic, Hospital, or Laboratory organization.
 */
enum class OrganizationRole(val displayName: String, val level: Int) {
    OWNER("Organization Owner", 100),
    ADMIN("Clinic/Hospital Admin", 80),
    MANAGER("Practice Manager", 60),
    DOCTOR("Staff Doctor", 50),
    STAFF("Clinical Staff", 30),
    RECEPTION("Reception & Booking", 20),
    CONTENT_MANAGER("Social & Content Manager", 20)
}

/**
 * Organization Member Model.
 * Mapped to Firestore: `organizations/{orgId}/members/{uid}`.
 */
data class OrganizationMember(
    val membershipId: String,
    val organizationId: String,
    val uid: String,
    val displayName: String,
    val email: String,
    val role: OrganizationRole = OrganizationRole.STAFF,
    val permissions: Set<String> = emptySet(),
    val status: String = "active",
    val addedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "membershipId" to membershipId,
        "organizationId" to organizationId,
        "uid" to uid,
        "displayName" to displayName,
        "email" to email,
        "role" to role.name,
        "permissions" to permissions.toList(),
        "status" to status,
        "addedAt" to addedAt,
        "updatedAt" to updatedAt
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestoreMap(data: Map<String, Any?>): OrganizationMember {
            val roleStr = data["role"] as? String ?: "STAFF"
            val role = try { OrganizationRole.valueOf(roleStr) } catch (_: Exception) { OrganizationRole.STAFF }
            val perms = (data["permissions"] as? List<String>)?.toSet() ?: emptySet()

            return OrganizationMember(
                membershipId = data["membershipId"] as? String ?: "",
                organizationId = data["organizationId"] as? String ?: "",
                uid = data["uid"] as? String ?: "",
                displayName = data["displayName"] as? String ?: "",
                email = data["email"] as? String ?: "",
                role = role,
                permissions = perms,
                status = data["status"] as? String ?: "active",
                addedAt = (data["addedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * Controlled Organization Device Feature Permissions.
 */
data class OrganizationDevicePermissions(
    val socialMedia: Boolean = true,
    val marketplace: Boolean = false,
    val textMessaging: Boolean = true,
    val audioCalling: Boolean = true,
    val videoCalling: Boolean = true,
    val appointments: Boolean = true,
    val consultations: Boolean = true,
    val management: Boolean = false,
    val contentCreation: Boolean = true,
    val aiStudio: Boolean = false
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "socialMedia" to socialMedia,
        "marketplace" to marketplace,
        "textMessaging" to textMessaging,
        "audioCalling" to audioCalling,
        "videoCalling" to videoCalling,
        "appointments" to appointments,
        "consultations" to consultations,
        "management" to management,
        "contentCreation" to contentCreation,
        "aiStudio" to aiStudio
    )

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>?): OrganizationDevicePermissions {
            if (data == null) return OrganizationDevicePermissions()
            return OrganizationDevicePermissions(
                socialMedia = data["socialMedia"] as? Boolean ?: true,
                marketplace = data["marketplace"] as? Boolean ?: false,
                textMessaging = data["textMessaging"] as? Boolean ?: true,
                audioCalling = data["audioCalling"] as? Boolean ?: true,
                videoCalling = data["videoCalling"] as? Boolean ?: true,
                appointments = data["appointments"] as? Boolean ?: true,
                consultations = data["consultations"] as? Boolean ?: true,
                management = data["management"] as? Boolean ?: false,
                contentCreation = data["contentCreation"] as? Boolean ?: true,
                aiStudio = data["aiStudio"] as? Boolean ?: false
            )
        }
    }
}

/**
 * Organization Device Model.
 * Mapped to Firestore: `organizations/{orgId}/devices/{deviceId}`.
 */
data class OrganizationDevice(
    val deviceId: String,
    val organizationId: String,
    val deviceName: String,
    val osVersion: String = "Android",
    val ipAddress: String = "192.168.1.1",
    val devicePermissions: OrganizationDevicePermissions = OrganizationDevicePermissions(),
    val status: String = "active",
    val registeredAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis()
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "deviceId" to deviceId,
        "organizationId" to organizationId,
        "deviceName" to deviceName,
        "osVersion" to osVersion,
        "ipAddress" to ipAddress,
        "devicePermissions" to devicePermissions.toFirestoreMap(),
        "status" to status,
        "registeredAt" to registeredAt,
        "lastActiveAt" to lastActiveAt
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestoreMap(data: Map<String, Any?>): OrganizationDevice = OrganizationDevice(
            deviceId = data["deviceId"] as? String ?: "",
            organizationId = data["organizationId"] as? String ?: "",
            deviceName = data["deviceName"] as? String ?: "Device",
            osVersion = data["osVersion"] as? String ?: "Android",
            ipAddress = data["ipAddress"] as? String ?: "",
            devicePermissions = OrganizationDevicePermissions.fromFirestoreMap(data["devicePermissions"] as? Map<String, Any?>),
            status = data["status"] as? String ?: "active",
            registeredAt = (data["registeredAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            lastActiveAt = (data["lastActiveAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}

/**
 * Healthcare Organization Profile Model.
 * Mapped to Firestore collection: `organizations/{organizationId}`.
 * Only CLINIC, HOSPITAL, or LABORATORY allowed. (Pharmacy strictly forbidden).
 */
data class OrganizationProfile(
    val organizationId: String,
    val ownerUid: String,
    val organizationType: AccountType = AccountType.CLINIC,
    val name: String,
    val username: String = "",
    val logoUrl: String? = null,
    val coverUrl: String? = null,
    val description: String = "",
    val countryCode: String = "US",
    val city: String = "",
    val address: String = "",
    val contactPhone: String = "",
    val contactEmail: String = "",
    val website: String = "",
    val workingHours: String = "24/7",
    val isActive: Boolean = true,
    val isVerified: Boolean = false,
    val verificationStatus: VerificationStatus = VerificationStatus.NOT_STARTED,
    val scannerPermission: Boolean = false,
    val subscriptionPlan: String = "Standard 4-Device",
    val maxDevices: Int = 4,
    val departments: List<HospitalDepartment> = emptyList(),
    val services: List<ProfessionalServiceItem> = emptyList(),
    val testCatalog: List<LabTestItem> = emptyList(),
    val followersCount: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    init {
        require(organizationType.isHealthcareOrganization) {
            "Architectural Violation: OrganizationProfile can only be CLINIC, HOSPITAL, or LABORATORY. Received: ${organizationType.name}"
        }
    }

    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "organizationId" to organizationId,
        "ownerUid" to ownerUid,
        "organizationType" to organizationType.name.lowercase(),
        "name" to name,
        "username" to username,
        "logoUrl" to logoUrl,
        "coverUrl" to coverUrl,
        "description" to description,
        "countryCode" to countryCode,
        "city" to city,
        "address" to address,
        "contactPhone" to contactPhone,
        "contactEmail" to contactEmail,
        "website" to website,
        "workingHours" to workingHours,
        "isActive" to isActive,
        "isVerified" to isVerified,
        "verificationStatus" to verificationStatus.name.lowercase(),
        "scannerPermission" to scannerPermission,
        "subscriptionPlan" to subscriptionPlan,
        "maxDevices" to maxDevices,
        "departments" to departments.map {
            mapOf("departmentId" to it.departmentId, "name" to it.name, "headDoctorName" to it.headDoctorName, "activeDoctorCount" to it.activeDoctorCount)
        },
        "services" to services.map { it.toFirestoreMap() },
        "testCatalog" to testCatalog.map {
            mapOf("testCode" to it.testCode, "testName" to it.testName, "sampleType" to it.sampleType, "standardTurnaroundHours" to it.standardTurnaroundHours, "price" to it.price, "currency" to it.currency, "requiresFasting" to it.requiresFasting)
        },
        "followersCount" to followersCount,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestoreMap(data: Map<String, Any?>): OrganizationProfile {
            val typeStr = data["organizationType"] as? String ?: "clinic"
            val orgType = try { AccountType.fromString(typeStr) } catch (_: Exception) { AccountType.CLINIC }
            val vStatusStr = data["verificationStatus"] as? String ?: "not_started"
            val vStatus = try { VerificationStatus.valueOf(vStatusStr.uppercase()) } catch (_: Exception) { VerificationStatus.NOT_STARTED }

            val depts = (data["departments"] as? List<Map<String, Any?>>)?.map {
                HospitalDepartment(
                    departmentId = it["departmentId"] as? String ?: "",
                    name = it["name"] as? String ?: "",
                    headDoctorName = it["headDoctorName"] as? String ?: "",
                    activeDoctorCount = (it["activeDoctorCount"] as? Number)?.toInt() ?: 0
                )
            } ?: emptyList()

            val servs = (data["services"] as? List<Map<String, Any?>>)?.map {
                ProfessionalServiceItem.fromFirestoreMap(it)
            } ?: emptyList()

            val tests = (data["testCatalog"] as? List<Map<String, Any?>>)?.map {
                LabTestItem(
                    testCode = it["testCode"] as? String ?: "",
                    testName = it["testName"] as? String ?: "",
                    sampleType = it["sampleType"] as? String ?: "Blood",
                    standardTurnaroundHours = (it["standardTurnaroundHours"] as? Number)?.toInt() ?: 24,
                    price = (it["price"] as? Number)?.toDouble() ?: 0.0,
                    currency = it["currency"] as? String ?: "USD",
                    requiresFasting = it["requiresFasting"] as? Boolean ?: false
                )
            } ?: emptyList()

            return OrganizationProfile(
                organizationId = data["organizationId"] as? String ?: "",
                ownerUid = data["ownerUid"] as? String ?: "",
                organizationType = orgType,
                name = data["name"] as? String ?: "",
                username = data["username"] as? String ?: "",
                logoUrl = data["logoUrl"] as? String,
                coverUrl = data["coverUrl"] as? String,
                description = data["description"] as? String ?: "",
                countryCode = data["countryCode"] as? String ?: "US",
                city = data["city"] as? String ?: "",
                address = data["address"] as? String ?: "",
                contactPhone = data["contactPhone"] as? String ?: "",
                contactEmail = data["contactEmail"] as? String ?: "",
                website = data["website"] as? String ?: "",
                workingHours = data["workingHours"] as? String ?: "24/7",
                isActive = data["isActive"] as? Boolean ?: true,
                isVerified = data["isVerified"] as? Boolean ?: false,
                verificationStatus = vStatus,
                scannerPermission = data["scannerPermission"] as? Boolean ?: false,
                subscriptionPlan = data["subscriptionPlan"] as? String ?: "Standard 4-Device",
                maxDevices = (data["maxDevices"] as? Number)?.toInt() ?: 4,
                departments = depts,
                services = servs,
                testCatalog = tests,
                followersCount = (data["followersCount"] as? Number)?.toLong() ?: 0L,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * Metadata reference for a submitted verification document.
 * (No sensitive content or public URLs are stored here).
 */
data class VerificationDocumentReference(
    val documentId: String,
    val documentType: String,
    val storagePath: String, // e.g., "verification_documents/{uid}/{id}"
    val status: String = "uploaded",
    val uploadedAt: Long = System.currentTimeMillis()
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "documentId" to documentId,
        "documentType" to documentType,
        "storagePath" to storagePath,
        "status" to status,
        "uploadedAt" to uploadedAt
    )

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>): VerificationDocumentReference = VerificationDocumentReference(
            documentId = data["documentId"] as? String ?: "",
            documentType = data["documentType"] as? String ?: "",
            storagePath = data["storagePath"] as? String ?: "",
            status = data["status"] as? String ?: "uploaded",
            uploadedAt = (data["uploadedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}

/**
 * Verification Profile Model.
 * Mapped to Firestore: `verification_profiles/{uid}`.
 */
data class VerificationProfile(
    val uid: String,
    val accountType: AccountType = AccountType.INDIVIDUAL,
    val countryCode: String = "US",
    val status: VerificationStatus = VerificationStatus.NOT_STARTED,
    val verificationType: String = "standard",
    val requiredDocuments: List<String> = emptyList(),
    val submittedDocuments: List<VerificationDocumentReference> = emptyList(),
    val reviewNotes: String? = null,
    val reviewedBy: String? = null,
    val submittedAt: Long? = null,
    val reviewedAt: Long? = null,
    val expiresAt: Long? = null,
    val rejectionReason: String? = null,
    val scannerPermission: Boolean = false
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "accountType" to accountType.name.lowercase(),
        "countryCode" to countryCode,
        "status" to status.name.lowercase(),
        "verificationType" to verificationType,
        "requiredDocuments" to requiredDocuments,
        "submittedDocuments" to submittedDocuments.map { it.toFirestoreMap() },
        "reviewNotes" to reviewNotes,
        "reviewedBy" to reviewedBy,
        "submittedAt" to submittedAt,
        "reviewedAt" to reviewedAt,
        "expiresAt" to expiresAt,
        "rejectionReason" to rejectionReason,
        "scannerPermission" to scannerPermission
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestoreMap(data: Map<String, Any?>): VerificationProfile {
            val typeStr = data["accountType"] as? String ?: "individual"
            val aType = try { AccountType.fromString(typeStr) } catch (_: Exception) { AccountType.INDIVIDUAL }
            val stStr = data["status"] as? String ?: "not_started"
            val st = try { VerificationStatus.valueOf(stStr.uppercase()) } catch (_: Exception) { VerificationStatus.NOT_STARTED }

            val docs = (data["submittedDocuments"] as? List<Map<String, Any?>>)?.map {
                VerificationDocumentReference.fromFirestoreMap(it)
            } ?: emptyList()

            return VerificationProfile(
                uid = data["uid"] as? String ?: "",
                accountType = aType,
                countryCode = data["countryCode"] as? String ?: "US",
                status = st,
                verificationType = data["verificationType"] as? String ?: "standard",
                requiredDocuments = (data["requiredDocuments"] as? List<String>) ?: emptyList(),
                submittedDocuments = docs,
                reviewNotes = data["reviewNotes"] as? String,
                reviewedBy = data["reviewedBy"] as? String,
                submittedAt = (data["submittedAt"] as? Number)?.toLong(),
                reviewedAt = (data["reviewedAt"] as? Number)?.toLong(),
                expiresAt = (data["expiresAt"] as? Number)?.toLong(),
                rejectionReason = data["rejectionReason"] as? String,
                scannerPermission = data["scannerPermission"] as? Boolean ?: false
            )
        }
    }
}

/**
 * Country-Specific Verification Requirements.
 * Mapped to Firestore: `verification_requirements/{countryCode}`.
 */
data class CountryVerificationRequirement(
    val countryCode: String,
    val accountType: AccountType,
    val requiredDocuments: List<String>,
    val optionalDocuments: List<String> = emptyList(),
    val documentExpiryRequired: Boolean = true,
    val manualReviewRequired: Boolean = true,
    val automatedVerificationAllowed: Boolean = false,
    val scannerAllowedAfterVerification: Boolean = true,
    val verificationProvider: String = "healthogram_trust_network",
    val rulesVersion: String = "1.0",
    val isActive: Boolean = true
)

/**
 * Follow Relationship.
 * Mapped to Firestore:
 * - `users/{uid}/following/{targetUid}`
 * - `users/{uid}/followers/{followerUid}`
 */
data class FollowRelationship(
    val uid: String,
    val targetUid: String,
    val status: String = "active", // "active", "pending", "blocked"
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Profile Report for Moderation.
 * Mapped to Firestore: `profile_reports/{reportId}`.
 */
data class ProfileReport(
    val reportId: String,
    val reporterUid: String,
    val targetUid: String,
    val reason: String,
    val description: String = "",
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis()
)
