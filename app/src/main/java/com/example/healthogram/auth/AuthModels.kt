package com.example.healthogram.auth

import com.example.healthogram.core.AccountType
import java.util.UUID

/**
 * Username reservation record stored in Firestore collection: `usernames/{normalizedUsername}`.
 */
data class UsernameReservation(
    val username: String,
    val normalizedUsername: String,
    val uid: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "username" to username,
        "normalizedUsername" to normalizedUsername,
        "uid" to uid,
        "createdAt" to createdAt
    )
}

/**
 * Device session tracking model for Firestore collection: `users/{uid}/devices/{deviceId}`.
 * Enforces strict maximum 4 active devices per user.
 */
data class DeviceSession(
    val deviceId: String,
    val uid: String,
    val deviceName: String,
    val platform: String = "Android",
    val appVersion: String = "1.0.0",
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val isRevoked: Boolean = false,
    val revokedAt: Long? = null,
    val isCurrentDevice: Boolean = false
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "deviceId" to deviceId,
        "uid" to uid,
        "deviceName" to deviceName,
        "platform" to platform,
        "appVersion" to appVersion,
        "createdAt" to createdAt,
        "lastActiveAt" to lastActiveAt,
        "lastLoginAt" to lastLoginAt,
        "isActive" to isActive,
        "isRevoked" to isRevoked,
        "revokedAt" to revokedAt
    )

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>, currentDeviceId: String): DeviceSession {
            val id = data["deviceId"] as? String ?: UUID.randomUUID().toString()
            return DeviceSession(
                deviceId = id,
                uid = data["uid"] as? String ?: "",
                deviceName = data["deviceName"] as? String ?: "Android Device",
                platform = data["platform"] as? String ?: "Android",
                appVersion = data["appVersion"] as? String ?: "1.0.0",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                lastActiveAt = (data["lastActiveAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                lastLoginAt = (data["lastLoginAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                isActive = data["isActive"] as? Boolean ?: true,
                isRevoked = data["isRevoked"] as? Boolean ?: false,
                revokedAt = (data["revokedAt"] as? Number)?.toLong(),
                isCurrentDevice = id == currentDeviceId
            )
        }
    }
}

/**
 * Security audit event types.
 */
enum class SecurityEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    LOGOUT,
    SIGNUP_STARTED,
    SIGNUP_COMPLETED,
    PASSWORD_CHANGED,
    EMAIL_CHANGED,
    DEVICE_ADDED,
    DEVICE_REVOKED,
    ACCOUNT_DELETED,
    VERIFICATION_SUBMITTED,
    VERIFICATION_APPROVED,
    VERIFICATION_REJECTED,
    ACCOUNT_RESTRICTED,
    SUSPICIOUS_LOGIN_BLOCKED
}

/**
 * Security audit log record stored in Firestore collection: `security_audit_logs/{eventId}`.
 * Strictly never stores passwords, OTPs, or private medical data.
 */
data class SecurityAuditLog(
    val eventId: String = UUID.randomUUID().toString(),
    val uid: String,
    val eventType: SecurityEventType,
    val timestamp: Long = System.currentTimeMillis(),
    val platform: String = "Android",
    val deviceId: String,
    val success: Boolean = true,
    val reasonCode: String? = null,
    val countryCode: String = "US",
    val metadata: Map<String, String> = emptyMap()
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "eventId" to eventId,
        "uid" to uid,
        "eventType" to eventType.name,
        "timestamp" to timestamp,
        "platform" to platform,
        "deviceId" to deviceId,
        "success" to success,
        "reasonCode" to reasonCode,
        "countryCode" to countryCode,
        "metadata" to metadata
    )
}

/**
 * Registration Draft State held during Multi-Step Sign-up (Steps A to H).
 */
data class RegistrationDraft(
    val registrationMethod: String = "EMAIL", // EMAIL, PHONE, GOOGLE, APPLE
    val accountType: AccountType = AccountType.INDIVIDUAL,
    val email: String = "",
    val phoneNumber: String = "",
    val displayName: String = "",
    val username: String = "",
    val countryCode: String = "US",
    val countryName: String = "United States",
    val city: String = "",
    val languageCode: String = "en",
    val currencyCode: String = "USD",
    val bio: String = "",
    val organizationName: String = "",
    val specializationOrCategory: String = "",
    val password: String = "",
    val termsAccepted: Boolean = false,
    val privacyAccepted: Boolean = false,
    val communityGuidelinesAccepted: Boolean = false,
    val healthDataNoticeAccepted: Boolean = false
)

/**
 * Password Strength level.
 */
enum class PasswordStrengthLevel(val label: String) {
    VERY_WEAK("Very Weak"),
    WEAK("Weak"),
    FAIR("Fair"),
    STRONG("Strong"),
    VERY_STRONG("Very Strong")
}

data class PasswordStrength(
    val score: Int, // 0 to 4
    val level: PasswordStrengthLevel,
    val feedback: List<String>,
    val isAcceptable: Boolean
)

/**
 * High-level Authentication State.
 */
sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val user: com.example.healthogram.core.User) : AuthState()
    data class OnboardingRequired(val user: com.example.healthogram.core.User) : AuthState()
    data class AccountRestricted(val user: com.example.healthogram.core.User, val reason: String) : AuthState()
    data class DeviceLimitExceeded(val activeDevices: List<DeviceSession>, val pendingUid: String) : AuthState()
}
