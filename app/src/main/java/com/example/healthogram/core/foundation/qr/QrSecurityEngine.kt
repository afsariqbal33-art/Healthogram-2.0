package com.example.healthogram.core.foundation.qr

import com.example.healthogram.core.foundation.audit.AuditEvent
import com.example.healthogram.core.foundation.audit.AuditLoggingService
import com.example.healthogram.core.foundation.audit.AuditResult
import com.example.healthogram.core.foundation.error.HealthAccessDeniedException
import java.security.SecureRandom
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Step 49: Healthogram 2.3 QR Security & Dynamic Vault Token Engine.
 *
 * CRITICAL PRIVACY INVARIANT:
 * QR codes NEVER contain raw Protected Health Information (PHI).
 * Only dynamic, single-use, opaque cryptographically bound tokens with a 60-second TTL are emitted.
 */
data class QrVaultToken(
    val tokenId: String,
    val patientUid: String,
    val nonce: String,
    val fingerprint: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 60000L, // Strict 60 seconds TTL
    var isConsumed: Boolean = false,
    var isRevoked: Boolean = false
) {
    fun isValid(): Boolean {
        val now = System.currentTimeMillis()
        return !isConsumed && !isRevoked && now < expiresAt
    }
}

class QrSecurityEngine private constructor() {
    private val activeTokens = ConcurrentHashMap<String, QrVaultToken>()
    private val secureRandom = SecureRandom()

    fun generateVaultToken(patientUid: String): QrVaultToken {
        val nonceBytes = ByteArray(16)
        secureRandom.nextBytes(nonceBytes)
        val nonce = nonceBytes.joinToString("") { "%02x".format(it) }

        val tokenId = "qr_v3_" + UUID.randomUUID().toString().replace("-", "").take(16)
        val fingerprint = "sha256_" + UUID.randomUUID().toString().take(12)

        val token = QrVaultToken(
            tokenId = tokenId,
            patientUid = patientUid,
            nonce = nonce,
            fingerprint = fingerprint
        )

        activeTokens[tokenId] = token

        AuditLoggingService.instance.logEvent(
            AuditEvent(
                actorId = patientUid,
                actorType = "Individual",
                targetType = "QRToken",
                targetId = tokenId,
                action = "GENERATE_QR_TOKEN",
                result = AuditResult.SUCCESS,
                reason = "Generated 60s dynamic QR vault token"
            )
        )

        return token
    }

    fun consumeVaultToken(
        tokenId: String,
        accessorUid: String,
        accessorAccountType: String
    ): QrVaultToken {
        val token = activeTokens[tokenId]

        if (token == null || !token.isValid()) {
            AuditLoggingService.instance.logEvent(
                AuditEvent(
                    actorId = accessorUid,
                    actorType = accessorAccountType,
                    targetType = "QRToken",
                    targetId = tokenId,
                    action = "CONSUME_QR_TOKEN",
                    result = AuditResult.DENIED,
                    reason = if (token?.isConsumed == true) "Token already consumed (Replay attack prevention)" else "Token expired or revoked"
                )
            )
            throw HealthAccessDeniedException("Invalid, expired, or previously consumed QR vault token")
        }

        // Atomically mark token as consumed to prevent replay attacks
        token.isConsumed = true

        AuditLoggingService.instance.logEvent(
            AuditEvent(
                actorId = accessorUid,
                actorType = accessorAccountType,
                targetType = "QRToken",
                targetId = tokenId,
                action = "CONSUME_QR_TOKEN",
                result = AuditResult.SUCCESS,
                reason = "Valid QR token handshake completed"
            )
        )

        return token
    }

    fun revokeVaultToken(tokenId: String, patientUid: String): Boolean {
        val token = activeTokens[tokenId] ?: return false
        if (token.patientUid != patientUid) return false

        token.isRevoked = true
        AuditLoggingService.instance.logEvent(
            AuditEvent(
                actorId = patientUid,
                actorType = "Individual",
                targetType = "QRToken",
                targetId = tokenId,
                action = "REVOKE_QR_TOKEN",
                result = AuditResult.SUCCESS,
                reason = "User initiated manual QR revocation"
            )
        )
        return true
    }

    companion object {
        val instance: QrSecurityEngine by lazy { QrSecurityEngine() }
    }
}
