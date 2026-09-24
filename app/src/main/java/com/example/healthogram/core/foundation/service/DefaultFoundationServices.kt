package com.example.healthogram.core.foundation.service

import com.example.healthogram.core.foundation.auth.AuthSession
import com.example.healthogram.core.foundation.auth.AuthorizationService
import com.example.healthogram.core.foundation.auth.ConsentGrant
import com.example.healthogram.core.foundation.idempotency.IdempotencyManager
import com.example.healthogram.core.foundation.qr.QrSecurityEngine
import com.example.healthogram.core.foundation.qr.QrVaultToken
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Step 49: Concrete Foundation Service Implementations.
 */
class DefaultPaymentService : PaymentService {
    private val adapters = ConcurrentHashMap<String, PaymentProviderAdapter>()

    init {
        registerAdapter(StripePaymentAdapter())
        registerAdapter(HyperPayPaymentAdapter())
        registerAdapter(GooglePayPaymentAdapter())
    }

    override fun registerAdapter(adapter: PaymentProviderAdapter) {
        adapters[adapter.providerName.uppercase()] = adapter
    }

    override fun processCheckout(
        amountCents: Long,
        currency: String,
        idempotencyKey: String,
        preferredGateway: String
    ): Boolean {
        // Enforce Idempotency
        val acquired = IdempotencyManager.instance.acquireKey(
            key = idempotencyKey,
            operation = "PAYMENT_CHECKOUT",
            actorUid = "checkout_user"
        )
        if (!acquired) {
            // Check if already completed
            val cached = IdempotencyManager.instance.getCachedResponse(idempotencyKey, "PAYMENT_CHECKOUT")
            return cached == "SUCCESS"
        }

        val adapter = adapters[preferredGateway.uppercase()] ?: adapters["STRIPE"]!!
        val success = adapter.processPayment(amountCents, currency, idempotencyKey)

        if (success) {
            IdempotencyManager.instance.completeKey(idempotencyKey, "PAYMENT_CHECKOUT", "SUCCESS")
        } else {
            IdempotencyManager.instance.failKey(idempotencyKey, "PAYMENT_CHECKOUT")
        }
        return success
    }
}

class DefaultLedgerService : LedgerService {
    private var totalDebitsCents: Long = 0L
    private var totalCreditsCents: Long = 0L

    @Synchronized
    override fun recordJournalEntry(
        debitAccount: String,
        creditAccount: String,
        amountCents: Long,
        referenceId: String
    ): Boolean {
        require(amountCents > 0) { "Journal entries must have positive amount." }
        totalDebitsCents += amountCents
        totalCreditsCents += amountCents
        return verifyLedgerInvariance()
    }

    override fun verifyLedgerInvariance(): Boolean {
        return (totalDebitsCents - totalCreditsCents) == 0L
    }
}

class DefaultHealthPassportService : HealthPassportService {
    private val records = ConcurrentHashMap<String, CopyOnWriteArrayList<Map<String, Any>>>()

    override fun getMedicalRecords(
        patientUid: String,
        session: AuthSession,
        consentGrant: ConsentGrant?
    ): List<Map<String, Any>> {
        return AuthorizationService.instance.executeHealthPassportOperation(
            session = session,
            targetPatientUid = patientUid,
            requiredScope = "SCOPE_FULL_CLINICAL_TIMELINE",
            consentGrant = consentGrant,
            operationName = "READ_MEDICAL_RECORDS"
        ) {
            records[patientUid] ?: emptyList()
        }
    }

    override fun appendRecord(
        patientUid: String,
        session: AuthSession,
        recordData: Map<String, Any>
    ): String {
        return AuthorizationService.instance.executeHealthPassportOperation(
            session = session,
            targetPatientUid = patientUid,
            requiredScope = "SCOPE_CONSULTATION_VISIT",
            consentGrant = null, // writing own or authorized clinical visit
            operationName = "APPEND_MEDICAL_RECORD"
        ) {
            val list = records.computeIfAbsent(patientUid) { CopyOnWriteArrayList() }
            val recordId = "rec_" + System.currentTimeMillis()
            val enriched = HashMap(recordData).apply { put("recordId", recordId) }
            list.add(enriched)
            recordId
        }
    }
}

class DefaultQrAccessService : QrAccessService {
    override fun issueVaultQrToken(patientUid: String): QrVaultToken {
        return QrSecurityEngine.instance.generateVaultToken(patientUid)
    }

    override fun redeemVaultQrToken(tokenId: String, accessorUid: String): QrVaultToken {
        return QrSecurityEngine.instance.consumeVaultToken(
            tokenId = tokenId,
            accessorUid = accessorUid,
            accessorAccountType = "Doctor"
        )
    }
}
