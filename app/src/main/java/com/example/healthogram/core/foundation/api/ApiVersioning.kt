package com.example.healthogram.core.foundation.api

import com.example.healthogram.core.foundation.error.ClientSafeError
import com.example.healthogram.core.foundation.error.ErrorCategory
import java.util.UUID

/**
 * Step 49: Healthogram 2.3 API Versioning & Gateway Dispatch Architecture.
 *
 * Guarantees backward compatibility for Version 2.2 clients while exposing enhanced /api/v2 endpoints.
 */
enum class ApiVersion(val pathPrefix: String) {
    V1("/api/v1"),
    V2("/api/v2")
}

data class ApiRequestEnvelope<T>(
    val apiVersion: ApiVersion = ApiVersion.V2,
    val requestId: String = UUID.randomUUID().toString(),
    val idempotencyKey: String? = null,
    val clientAppVersion: Int = 20202,
    val timestamp: Long = System.currentTimeMillis(),
    val data: T
)

data class ApiResponseEnvelope<T>(
    val apiVersion: ApiVersion,
    val requestId: String,
    val isSuccess: Boolean,
    val data: T? = null,
    val error: ClientSafeError? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun <T> success(apiVersion: ApiVersion, requestId: String, data: T): ApiResponseEnvelope<T> {
            return ApiResponseEnvelope(
                apiVersion = apiVersion,
                requestId = requestId,
                isSuccess = true,
                data = data
            )
        }

        fun <T> failure(
            apiVersion: ApiVersion,
            requestId: String,
            category: ErrorCategory,
            errorCode: String,
            userMessage: String
        ): ApiResponseEnvelope<T> {
            return ApiResponseEnvelope(
                apiVersion = apiVersion,
                requestId = requestId,
                isSuccess = false,
                error = ClientSafeError(
                    category = category,
                    errorCode = errorCode,
                    userMessage = userMessage,
                    requestId = requestId
                )
            )
        }
    }
}
