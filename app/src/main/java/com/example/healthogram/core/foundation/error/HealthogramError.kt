package com.example.healthogram.core.foundation.error

/**
 * Step 49: Healthogram 2.3 Standardized Error Framework.
 *
 * Ensures error responses never expose internal stack traces, system paths,
 * or raw Protected Health Information (PHI) to end clients.
 */
enum class ErrorCategory {
    AUTHENTICATION_ERROR,
    AUTHORIZATION_ERROR,
    VALIDATION_ERROR,
    NOT_FOUND,
    RATE_LIMITED,
    CONFLICT,
    PAYMENT_ERROR,
    HEALTH_ACCESS_DENIED,
    CONSENT_EXPIRED,
    CONSENT_REVOKED,
    PROVIDER_ERROR,
    INTERNAL_ERROR
}

data class ClientSafeError(
    val category: ErrorCategory,
    val errorCode: String,
    val userMessage: String,
    val requestId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

open class HealthException(
    val category: ErrorCategory,
    val errorCode: String,
    override val message: String,
    val userSafeMessage: String,
    val requestId: String? = null,
    cause: Throwable? = null
) : Exception(message, cause) {

    fun toClientSafeError(): ClientSafeError = ClientSafeError(
        category = category,
        errorCode = errorCode,
        userMessage = userSafeMessage,
        requestId = requestId
    )
}

class ConsentExpiredException(
    requestId: String? = null
) : HealthException(
    category = ErrorCategory.CONSENT_EXPIRED,
    errorCode = "ERR_HGM_CONSENT_EXPIRED",
    message = "Patient consent grant has expired",
    userSafeMessage = "Your medical access session has expired. Please request a new QR authorization.",
    requestId = requestId
)

class ConsentRevokedException(
    requestId: String? = null
) : HealthException(
    category = ErrorCategory.CONSENT_REVOKED,
    errorCode = "ERR_HGM_CONSENT_REVOKED",
    message = "Patient explicitly revoked consent for this session",
    userSafeMessage = "Access to this medical record was revoked by the patient.",
    requestId = requestId
)

class HealthAccessDeniedException(
    reason: String,
    requestId: String? = null
) : HealthException(
    category = ErrorCategory.HEALTH_ACCESS_DENIED,
    errorCode = "ERR_HGM_ACCESS_DENIED",
    message = "Access to Health Passport denied: $reason",
    userSafeMessage = "You do not have authorized permission to view this medical record.",
    requestId = requestId
)

class PaymentConflictException(
    reason: String,
    requestId: String? = null
) : HealthException(
    category = ErrorCategory.PAYMENT_ERROR,
    errorCode = "ERR_HGM_PAYMENT_FAILED",
    message = "Payment processing error: $reason",
    userSafeMessage = "Unable to process payment. Please verify your payment details and retry.",
    requestId = requestId
)
