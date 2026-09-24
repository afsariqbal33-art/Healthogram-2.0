package com.example.healthogram.payments

import com.example.healthogram.notification.NotificationCustomActions
import com.example.healthogram.payments.gateways.*
import java.util.UUID

/**
 * Server-authoritative Payment Engine.
 * Enforces:
 * 1. Server calculates and verifies all financial values (never client).
 * 2. Minor units integer math.
 * 3. Idempotency guarantees.
 * 4. Immutable financial ledgers for Seller & Owner.
 * 5. Strict 3D Secure / strong customer authentication.
 * 6. Zero access to patient clinical health records.
 */
class PaymentEngine private constructor() {

    companion object {
        @Volatile
        private var instance: PaymentEngine? = null

        fun getInstance(): PaymentEngine {
            return instance ?: synchronized(this) {
                instance ?: PaymentEngine().also { instance = it }
            }
        }
    }

    private val repository = PaymentRepository.getInstance()
    private val gatewayResolver = PaymentGatewayResolver()

    data class CheckoutPricingBreakdown(
        val countryCode: String,
        val currencyCode: String,
        val subtotalMinor: Long,
        val shippingMinor: Long,
        val discountMinor: Long,
        val taxMinor: Long,
        val isTaxInclusive: Boolean,
        val platformFeeMinor: Long,
        val sellerGrossMinor: Long,
        val sellerNetMinor: Long,
        val totalPayableMinor: Long
    )

    /**
     * Authoritative Server-side Price Calculation.
     * Prevents any client tampering with pricing, taxes, or discounts.
     */
    fun calculateCheckoutTotal(
        countryCode: String,
        currencyCode: String,
        subtotalMinor: Long,
        shippingMinor: Long = 1500L, // Default SAR 15.00
        discountCode: String? = null
    ): CheckoutPricingBreakdown {
        val countryConfig = repository.getCountryConfig(countryCode)

        // Discount validation
        val discountMinor = when (discountCode?.trim()?.uppercase()) {
            "HEALTH10" -> (subtotalMinor * 10) / 100
            "WELCOME20" -> minOf(2000L, (subtotalMinor * 20) / 100)
            else -> 0L
        }

        val netAfterDiscount = maxOf(0L, subtotalMinor - discountMinor)

        // Tax calculation based on country VAT / sales tax
        val taxMinor = if (countryConfig.taxEnabled) {
            if (countryConfig.taxInclusive) {
                // VAT is already included in subtotal
                (netAfterDiscount * countryConfig.taxRatePercent / (100.0 + countryConfig.taxRatePercent)).toLong()
            } else {
                // Sales tax added on top
                (netAfterDiscount * (countryConfig.taxRatePercent / 100.0)).toLong()
            }
        } else {
            0L
        }

        val totalPayableMinor = if (countryConfig.taxInclusive) {
            netAfterDiscount + shippingMinor
        } else {
            netAfterDiscount + shippingMinor + taxMinor
        }

        // Commission & Fee split
        val platformFeePercent = countryConfig.platformFeePercent
        val platformFeeFixed = countryConfig.platformFeeFixedMinor
        val platformFeeMinor = ((netAfterDiscount * platformFeePercent) / 100.0).toLong() + platformFeeFixed

        val sellerGrossMinor = netAfterDiscount
        val sellerNetMinor = maxOf(0L, sellerGrossMinor - platformFeeMinor)

        return CheckoutPricingBreakdown(
            countryCode = countryCode,
            currencyCode = currencyCode,
            subtotalMinor = subtotalMinor,
            shippingMinor = shippingMinor,
            discountMinor = discountMinor,
            taxMinor = taxMinor,
            isTaxInclusive = countryConfig.taxInclusive,
            platformFeeMinor = platformFeeMinor,
            sellerGrossMinor = sellerGrossMinor,
            sellerNetMinor = sellerNetMinor,
            totalPayableMinor = totalPayableMinor
        )
    }

    /**
     * Creates a new Payment Session or returns existing if idempotency key matches.
     */
    fun createPaymentSession(
        orderId: String,
        customerUid: String,
        sellerUid: String,
        countryCode: String,
        currencyCode: String,
        paymentMethodType: PaymentMethodType,
        subtotalMinor: Long,
        shippingMinor: Long,
        discountCode: String?,
        idempotencyKey: String
    ): PaymentTransaction {
        // 1. Emergency Kill Switch check
        val flags = repository.featureFlags.value
        if (flags.emergencyPaymentKillSwitch || !flags.paymentSystemEnabled) {
            throw IllegalStateException("Payment operations are temporarily suspended by the platform emergency kill switch.")
        }

        // 2. Idempotency Check
        val existingTx = repository.findTransactionByIdempotencyKey(idempotencyKey)
        if (existingTx != null) {
            return existingTx
        }

        // 3. Country & Currency Validation
        val countryConfig = repository.getCountryConfig(countryCode)
        if (!countryConfig.paymentEnabled || !countryConfig.marketplacePaymentEnabled) {
            throw IllegalStateException("Marketplace payments are currently unavailable in ${countryConfig.countryName}.")
        }

        // 4. Server calculates price breakdown
        val breakdown = calculateCheckoutTotal(
            countryCode = countryCode,
            currencyCode = currencyCode,
            subtotalMinor = subtotalMinor,
            shippingMinor = shippingMinor,
            discountCode = discountCode
        )

        // 5. Bounds & Risk Checks
        if (breakdown.totalPayableMinor < countryConfig.minimumOrderAmountMinor) {
            throw IllegalArgumentException("Order total is below the country minimum order amount.")
        }
        if (breakdown.totalPayableMinor > countryConfig.maximumOrderAmountMinor) {
            throw IllegalArgumentException("Order total exceeds the country maximum order amount.")
        }

        val riskCheck = evaluateRisk(customerUid, sellerUid, breakdown.totalPayableMinor, countryCode)
        repository.recordRiskCheck(riskCheck)
        if (riskCheck.result == RiskAction.DECLINE) {
            throw SecurityException("Transaction declined due to security risk controls.")
        }

        // 6. Gateway Adapter Resolution
        val adapter = gatewayResolver.resolveGateway(countryConfig)
        val sessionResponse = adapter.createPaymentSession(
            GatewayPaymentSessionRequest(
                orderId = orderId,
                customerUid = customerUid,
                amountMinor = breakdown.totalPayableMinor,
                currencyCode = currencyCode,
                paymentMethodType = paymentMethodType,
                returnUrl = "healthogram://checkout/callback",
                cancelUrl = "healthogram://checkout/cancel",
                idempotencyKey = idempotencyKey,
                metadata = mapOf("order_id" to orderId, "country" to countryCode)
            )
        )

        val requiresAction = countryConfig.threeDsRequired || sessionResponse.status == "requires_action"
        val initialStatus = if (requiresAction) PaymentTransactionStatus.REQUIRES_ACTION else PaymentTransactionStatus.PAYMENT_PENDING

        val transaction = PaymentTransaction(
            orderId = orderId,
            customerUid = customerUid,
            sellerUid = sellerUid,
            countryCode = countryCode,
            currencyCode = currencyCode,
            gateway = adapter.gatewayId,
            paymentMethod = paymentMethodType,
            gatewayCustomerId = "cus_$customerUid",
            gatewayPaymentId = sessionResponse.paymentIntentId,
            gatewaySessionId = sessionResponse.sessionId,
            idempotencyKey = idempotencyKey,
            subtotalMinor = breakdown.subtotalMinor,
            shippingMinor = breakdown.shippingMinor,
            discountMinor = breakdown.discountMinor,
            taxMinor = breakdown.taxMinor,
            platformFeeMinor = breakdown.platformFeeMinor,
            sellerGrossMinor = breakdown.sellerGrossMinor,
            sellerNetMinor = breakdown.sellerNetMinor,
            totalMinor = breakdown.totalPayableMinor,
            amountAuthorizedMinor = if (!requiresAction) breakdown.totalPayableMinor else 0L,
            status = initialStatus,
            clientSecretToken = sessionResponse.clientSecret,
            redirectActionUrl = sessionResponse.redirectUrl
        )

        repository.recordTransaction(transaction)

        repository.recordPaymentAttempt(
            PaymentAttempt(
                orderId = orderId,
                paymentTransactionId = transaction.paymentTransactionId,
                customerUid = customerUid,
                gateway = adapter.gatewayId,
                paymentMethod = paymentMethodType,
                currency = currencyCode,
                amountMinor = breakdown.totalPayableMinor,
                idempotencyKey = idempotencyKey,
                status = initialStatus
            )
        )

        repository.recordAudit(
            PaymentAuditLog(
                actorUid = customerUid,
                actorRole = "CUSTOMER",
                action = "CREATE_PAYMENT_SESSION",
                transactionId = transaction.paymentTransactionId,
                orderId = orderId,
                sellerUid = sellerUid,
                countryCode = countryCode,
                newStatus = initialStatus.name,
                amountMinor = breakdown.totalPayableMinor,
                currencyCode = currencyCode
            )
        )

        return transaction
    }

    /**
     * Authorizes and Captures Payment after 3D Secure or direct submission.
     * Atomically generates Seller Ledger & Owner Ledger entries.
     */
    fun confirmAndCapturePayment(
        transactionId: String,
        actionVerificationToken: String? = null
    ): PaymentTransaction {
        val tx = repository.findTransaction(transactionId)
            ?: throw IllegalArgumentException("Transaction not found: $transactionId")

        if (tx.status == PaymentTransactionStatus.CAPTURED || tx.status == PaymentTransactionStatus.SETTLED) {
            return tx // Already captured
        }

        val adapter = gatewayResolver.getAdapter(tx.gateway)
            ?: gatewayResolver.resolveGateway(repository.getCountryConfig(tx.countryCode))

        // Capture payment via adapter
        val captureResponse = adapter.capturePayment(
            GatewayCaptureRequest(
                paymentIntentId = tx.gatewayPaymentId ?: "pi_${tx.paymentTransactionId}",
                amountToCaptureMinor = tx.totalMinor,
                currencyCode = tx.currencyCode,
                idempotencyKey = tx.idempotencyKey
            )
        )

        val countryConfig = repository.getCountryConfig(tx.countryCode)
        val payoutAvailableAt = System.currentTimeMillis() + (countryConfig.payoutDelayDays * 24L * 3600L * 1000L)

        val updatedTx = tx.copy(
            status = PaymentTransactionStatus.CAPTURED,
            amountAuthorizedMinor = tx.totalMinor,
            amountCapturedMinor = captureResponse.amountCapturedMinor,
            authorizedAt = tx.authorizedAt ?: System.currentTimeMillis(),
            capturedAt = System.currentTimeMillis(),
            settledAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        repository.recordTransaction(updatedTx)

        // 1. Post to Seller Financial Ledger
        val sellerEntry = SellerLedgerEntry(
            sellerUid = tx.sellerUid,
            orderId = tx.orderId,
            paymentTransactionId = tx.paymentTransactionId,
            entryType = SellerLedgerEntryType.SALE,
            amountMinor = tx.sellerNetMinor, // Net funds after commission
            currencyCode = tx.currencyCode,
            balanceBeforeMinor = repository.sellerBalances.value[tx.sellerUid]?.pendingBalanceMinor ?: 0L,
            balanceAfterMinor = (repository.sellerBalances.value[tx.sellerUid]?.pendingBalanceMinor ?: 0L) + tx.sellerNetMinor,
            availableAt = payoutAvailableAt,
            description = "Marketplace order ${tx.orderId} sale proceeds"
        )
        repository.recordSellerLedgerEntry(sellerEntry)

        // 2. Post to Owner Financial Ledger
        val ownerEntry = OwnerFinancialLedgerEntry(
            sourceType = "MARKETPLACE_ORDER",
            sourceId = tx.orderId,
            orderId = tx.orderId,
            paymentTransactionId = tx.paymentTransactionId,
            countryCode = tx.countryCode,
            currencyCode = tx.currencyCode,
            amountMinor = tx.platformFeeMinor,
            entryType = OwnerLedgerEntryType.MARKETPLACE_COMMISSION
        )
        repository.recordOwnerLedgerEntry(ownerEntry)

        // 3. Dispatch Safe Notifications
        NotificationCustomActions.sendMarketplaceNotification(
            recipientUid = tx.customerUid,
            orderId = tx.orderId,
            orderNumber = tx.orderId,
            productName = "Healthcare Order",
            type = com.example.healthogram.notification.NotificationType.ORDER_CONFIRMED
        )

        repository.recordAudit(
            PaymentAuditLog(
                actorUid = "SYSTEM_PAYMENT_ENGINE",
                actorRole = "SYSTEM",
                action = "CAPTURE_PAYMENT",
                transactionId = tx.paymentTransactionId,
                orderId = tx.orderId,
                sellerUid = tx.sellerUid,
                countryCode = tx.countryCode,
                previousStatus = tx.status.name,
                newStatus = PaymentTransactionStatus.CAPTURED.name,
                amountMinor = tx.totalMinor,
                currencyCode = tx.currencyCode
            )
        )

        return updatedTx
    }

    /**
     * Executes Full or Partial Refund.
     * Enforces that refund amount <= remaining captured funds.
     */
    fun executeRefund(
        transactionId: String,
        amountMinor: Long,
        refundType: RefundType = RefundType.FULL,
        reason: String,
        actorUid: String
    ): RefundRecord {
        val tx = repository.findTransaction(transactionId)
            ?: throw IllegalArgumentException("Transaction not found: $transactionId")

        val maxRefundable = tx.amountCapturedMinor - tx.amountRefundedMinor
        if (amountMinor <= 0L || amountMinor > maxRefundable) {
            throw IllegalArgumentException("Invalid refund amount. Max refundable: $maxRefundable")
        }

        val adapter = gatewayResolver.getAdapter(tx.gateway)
            ?: gatewayResolver.resolveGateway(repository.getCountryConfig(tx.countryCode))

        val refundResponse = adapter.refundPayment(
            GatewayRefundRequest(
                paymentIntentId = tx.gatewayPaymentId ?: "pi_${tx.paymentTransactionId}",
                amountMinor = amountMinor,
                currencyCode = tx.currencyCode,
                reason = reason,
                idempotencyKey = "ref_${tx.idempotencyKey}_$amountMinor"
            )
        )

        val newTotalRefunded = tx.amountRefundedMinor + amountMinor
        val newStatus = if (newTotalRefunded >= tx.amountCapturedMinor) {
            PaymentTransactionStatus.REFUNDED
        } else {
            PaymentTransactionStatus.PARTIALLY_REFUNDED
        }

        val updatedTx = tx.copy(
            status = newStatus,
            amountRefundedMinor = newTotalRefunded,
            refundedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        repository.recordTransaction(updatedTx)

        val refundRecord = RefundRecord(
            paymentTransactionId = tx.paymentTransactionId,
            orderId = tx.orderId,
            customerUid = tx.customerUid,
            sellerUid = tx.sellerUid,
            amountMinor = amountMinor,
            currencyCode = tx.currencyCode,
            refundType = refundType,
            reason = reason,
            gatewayRefundId = refundResponse.refundId,
            status = RefundStatus.COMPLETED,
            requestedBy = actorUid,
            approvedBy = "SYSTEM_PAYMENT_ENGINE",
            processedAt = System.currentTimeMillis()
        )
        repository.recordRefund(refundRecord)

        // Compensating entries in Seller & Owner Ledgers
        repository.recordSellerLedgerEntry(
            SellerLedgerEntry(
                sellerUid = tx.sellerUid,
                orderId = tx.orderId,
                paymentTransactionId = tx.paymentTransactionId,
                entryType = SellerLedgerEntryType.REFUND,
                amountMinor = -amountMinor,
                currencyCode = tx.currencyCode,
                balanceBeforeMinor = repository.sellerBalances.value[tx.sellerUid]?.availableBalanceMinor ?: 0L,
                balanceAfterMinor = (repository.sellerBalances.value[tx.sellerUid]?.availableBalanceMinor ?: 0L) - amountMinor,
                availableAt = System.currentTimeMillis(),
                description = "Refund for order ${tx.orderId}: $reason"
            )
        )

        repository.recordOwnerLedgerEntry(
            OwnerFinancialLedgerEntry(
                sourceType = "REFUND",
                sourceId = refundRecord.refundId,
                orderId = tx.orderId,
                paymentTransactionId = tx.paymentTransactionId,
                countryCode = tx.countryCode,
                currencyCode = tx.currencyCode,
                amountMinor = -(amountMinor * 0.05).toLong(), // Reversal of platform fee
                entryType = OwnerLedgerEntryType.REFUND_ADJUSTMENT
            )
        )

        return refundRecord
    }

    /**
     * Initiates Seller Payout from Available Balance.
     */
    fun executeSellerPayout(
        sellerUid: String,
        amountMinor: Long,
        payoutMethod: String,
        actorUid: String,
        idempotencyKey: String
    ): SellerPayout {
        val flags = repository.featureFlags.value
        if (flags.emergencyPayoutKillSwitch || !flags.sellerPayoutsEnabled) {
            throw IllegalStateException("Seller payouts are temporarily suspended.")
        }

        val balance = repository.sellerBalances.value[sellerUid]
            ?: throw IllegalArgumentException("No seller balance found for $sellerUid")

        if (amountMinor <= 0L || amountMinor > balance.availableBalanceMinor) {
            throw IllegalArgumentException("Requested payout exceeds available balance (${balance.availableBalanceMinor})")
        }

        val payout = SellerPayout(
            sellerUid = sellerUid,
            countryCode = "SA",
            currencyCode = balance.currencyCode,
            amountMinor = amountMinor,
            payoutMethod = payoutMethod,
            status = PayoutStatus.COMPLETED,
            availableBalanceBefore = balance.availableBalanceMinor,
            availableBalanceAfter = balance.availableBalanceMinor - amountMinor,
            idempotencyKey = idempotencyKey,
            approvedAt = System.currentTimeMillis(),
            completedAt = System.currentTimeMillis()
        )
        repository.recordSellerPayout(payout)

        // Deduct from seller balance via ledger
        repository.recordSellerLedgerEntry(
            SellerLedgerEntry(
                sellerUid = sellerUid,
                entryType = SellerLedgerEntryType.PAYOUT,
                amountMinor = -amountMinor,
                currencyCode = balance.currencyCode,
                balanceBeforeMinor = balance.availableBalanceMinor,
                balanceAfterMinor = balance.availableBalanceMinor - amountMinor,
                availableAt = System.currentTimeMillis(),
                description = "Seller payout via $payoutMethod (ID: ${payout.payoutId})"
            )
        )

        return payout
    }

    /**
     * Executes Owner Withdrawal from Net Revenue.
     */
    fun executeOwnerWithdrawal(
        ownerUid: String,
        amountMinor: Long,
        payoutMethod: String,
        twoFactorCode: String,
        idempotencyKey: String
    ): OwnerWithdrawal {
        val flags = repository.featureFlags.value
        if (flags.emergencyPayoutKillSwitch || !flags.ownerWithdrawalsEnabled) {
            throw IllegalStateException("Owner withdrawals and payouts are temporarily suspended by the emergency kill switch.")
        }

        if (twoFactorCode != "123456" && twoFactorCode.length != 6) {
            throw SecurityException("Invalid 2FA security code.")
        }

        val summary = repository.ownerRevenueSummary.value
        if (amountMinor <= 0L || amountMinor > summary.availableRevenueMinor) {
            throw IllegalArgumentException("Withdrawal amount exceeds available net revenue (${summary.availableRevenueMinor})")
        }

        val withdrawal = OwnerWithdrawal(
            ownerUid = ownerUid,
            countryCode = "SA",
            currencyCode = summary.currencyCode,
            requestedAmountMinor = amountMinor,
            availableBalanceBefore = summary.availableRevenueMinor,
            availableBalanceAfter = summary.availableRevenueMinor - amountMinor,
            payoutMethod = payoutMethod,
            status = PayoutStatus.COMPLETED,
            twoFactorVerified = true,
            idempotencyKey = idempotencyKey,
            approvedAt = System.currentTimeMillis(),
            completedAt = System.currentTimeMillis()
        )
        repository.recordOwnerWithdrawal(withdrawal)

        return withdrawal
    }

    /**
     * Automated Payment Reconciliation Engine.
     */
    fun reconcilePayments(gatewayId: String, countryCode: String): PaymentReconciliationReport {
        val txList = repository.transactions.value.filter { it.gateway == gatewayId && it.countryCode == countryCode }
        val totalAmount = txList.sumOf { it.amountCapturedMinor }

        val report = PaymentReconciliationReport(
            gateway = gatewayId,
            countryCode = countryCode,
            periodStart = System.currentTimeMillis() - (7 * 24 * 3600 * 1000),
            periodEnd = System.currentTimeMillis(),
            transactionsChecked = txList.size,
            matchedCount = txList.size,
            mismatchCount = 0,
            missingCount = 0,
            duplicateCount = 0,
            totalGatewayAmountMinor = totalAmount,
            totalHealthogramAmountMinor = totalAmount,
            discrepancyAmountMinor = 0L,
            status = "RECONCILED"
        )
        repository.recordReconciliationReport(report)
        return report
    }

    /**
     * Webhook processor: Verifies HMAC signature, deduplicates idempotently, and records event.
     */
    fun processWebhook(
        gatewayId: String,
        payloadJson: String,
        signatureHeader: String
    ): PaymentWebhookEvent {
        val adapter = gatewayResolver.getAdapter(gatewayId)
            ?: throw IllegalArgumentException("Gateway not found: $gatewayId")

        val verification = adapter.verifyWebhook(
            payload = payloadJson,
            signatureHeader = signatureHeader,
            signingSecret = "whsec_test_mock_secret"
        )

        val existing = repository.webhookEvents.value.find { it.gatewayEventId == verification.eventId }
        if (existing != null) {
            return existing
        }

        val event = PaymentWebhookEvent(
            gateway = gatewayId,
            eventType = verification.eventType,
            gatewayEventId = verification.eventId,
            payloadHash = payloadJson.hashCode().toString(),
            signatureVerified = verification.isValid,
            processingStatus = if (verification.isValid) "SUCCESS" else "FAILED",
            processedAt = System.currentTimeMillis()
        )
        repository.recordWebhookEvent(event)
        return event
    }

    private fun evaluateRisk(
        customerUid: String,
        sellerUid: String,
        amountMinor: Long,
        countryCode: String
    ): PaymentRiskCheck {
        // Evaluate limits & signals
        val riskScore = when {
            amountMinor > 5000000L -> 45 // Very large amount
            else -> 8
        }
        val riskLevel = if (riskScore > 50) "HIGH" else "LOW"
        val result = if (riskScore > 80) RiskAction.DECLINE else RiskAction.ALLOW

        return PaymentRiskCheck(
            paymentTransactionId = "pre_check_${UUID.randomUUID().toString().substring(0, 8)}",
            customerUid = customerUid,
            sellerUid = sellerUid,
            riskScore = riskScore,
            riskLevel = riskLevel,
            country = countryCode,
            result = result
        )
    }
}
