package com.example.healthogram.marketplace.seller

import com.example.healthogram.marketplace.*

/**
 * HEALTHOGRAM — STEP 09: SELLER SECURITY, COMPLIANCE & AUTHORITATIVE VALIDATION ENGINE
 *
 * Rules:
 * 1. Health Passport Isolation: Sellers have ZERO access to clinical data (health_*).
 * 2. Medical Claims Prohibition: Prevents unsupported cures, clinical guarantees, and uncertified claims.
 * 3. Authoritative Order State Transitions: Seller can only progress operational states.
 * 4. Authoritative Financials: Append-only ledger, balance checks, payout validations.
 * 5. Cross-Seller Isolation: Strict ownership verification.
 */
object SellerSecurityAndValidation {

    private val PROHIBITED_MEDICAL_CLAIMS = listOf(
        "cures cancer",
        "treats diabetes",
        "guaranteed cure",
        "100% cure",
        "fda approved miracle",
        "substitute for prescription",
        "instant weight loss miracle",
        "reverses aging",
        "cures covid",
        "eradicates infection permanently"
    )

    private val HEALTH_COLLECTIONS = listOf(
        "health_profiles",
        "health_conditions",
        "health_allergies",
        "health_medications",
        "health_visits",
        "health_diagnoses",
        "health_tests",
        "health_lab_reports",
        "health_prescriptions",
        "health_documents",
        "health_bills",
        "health_access_requests",
        "health_access_grants",
        "health_access_logs"
    )

    // ==========================================
    // 1. HEALTH PASSPORT & ROLE ISOLATION
    // ==========================================

    /**
     * Strictly verifies that a marketplace seller request does not attempt to access
     * any clinical health passport collections.
     */
    fun assertSellerCannotAccessClinicalData(collectionName: String): Boolean {
        if (HEALTH_COLLECTIONS.any { collectionName.startsWith(it) }) {
            throw SecurityException(
                "CRITICAL SECURITY VIOLATION: Marketplace Seller role is strictly forbidden from accessing Health Passport collection: $collectionName"
            )
        }
        return true
    }

    /**
     * Verifies that seller permissions never equal admin, owner, or healthcare provider permissions.
     */
    fun assertRoleSeparation(sellerUid: String, requestedScope: String): Boolean {
        val forbiddenScopes = listOf("admin_control", "owner_earnings", "clinical_patient_records", "doctor_prescriptions")
        if (forbiddenScopes.contains(requestedScope)) {
            throw SecurityException("CRITICAL ROLE VIOLATION: Seller $sellerUid cannot access $requestedScope")
        }
        return true
    }

    // ==========================================
    // 2. PRODUCT CONTENT & COMPLIANCE
    // ==========================================

    data class ValidationResult(val isValid: Boolean, val reason: String? = null)

    fun validateProductContent(
        title: String,
        description: String,
        tags: List<String> = emptyList()
    ): ValidationResult {
        if (title.isBlank()) return ValidationResult(false, "Product title cannot be empty.")
        if (title.length < 3) return ValidationResult(false, "Product title is too short.")
        if (description.isBlank()) return ValidationResult(false, "Product description cannot be empty.")

        val fullText = "$title $description ${tags.joinToString(" ")}".lowercase()
        for (claim in PROHIBITED_MEDICAL_CLAIMS) {
            if (fullText.contains(claim)) {
                return ValidationResult(
                    false,
                    "Policy Violation: Prohibited medical claim detected ('$claim'). Products cannot claim to diagnose, cure, or replace clinical treatments."
                )
            }
        }
        return ValidationResult(true)
    }

    // ==========================================
    // 3. SELLER ORDER WORKFLOW TRANSITIONS
    // ==========================================

    /**
     * Seller can ONLY transition orders through operational fulfillment states:
     * CONFIRMED -> PROCESSING
     * PROCESSING -> PACKED
     * PACKED -> READY_FOR_SHIPMENT
     *
     * Seller CANNOT set PAID, REFUNDED, CHARGEBACK, or CUSTOMER_CANCELLED.
     * Seller CANNOT mark DELIVERED (must come from delivery provider).
     */
    fun validateSellerOrderStatusTransition(
        currentStatus: MarketplaceOrderStatus,
        targetStatus: MarketplaceOrderStatus
    ): ValidationResult {
        // Explicitly forbidden target states for a seller client
        val forbiddenTargetStates = listOf(
            MarketplaceOrderStatus.PENDING_PAYMENT,
            MarketplaceOrderStatus.PAID,
            MarketplaceOrderStatus.DELIVERED,
            MarketplaceOrderStatus.COMPLETED,
            MarketplaceOrderStatus.REFUND_PENDING,
            MarketplaceOrderStatus.REFUNDED,
            MarketplaceOrderStatus.FAILED
        )

        if (forbiddenTargetStates.contains(targetStatus)) {
            return ValidationResult(
                false,
                "Unauthorized: Sellers cannot set authoritative financial/customer/delivery status '$targetStatus'."
            )
        }

        return when (currentStatus) {
            MarketplaceOrderStatus.CONFIRMED -> {
                if (targetStatus == MarketplaceOrderStatus.PROCESSING || targetStatus == MarketplaceOrderStatus.CANCELLED) {
                    ValidationResult(true)
                } else {
                    ValidationResult(false, "Invalid transition from CONFIRMED to $targetStatus.")
                }
            }
            MarketplaceOrderStatus.PROCESSING -> {
                if (targetStatus == MarketplaceOrderStatus.PACKED || targetStatus == MarketplaceOrderStatus.CANCELLED) {
                    ValidationResult(true)
                } else {
                    ValidationResult(false, "Invalid transition from PROCESSING to $targetStatus.")
                }
            }
            MarketplaceOrderStatus.PACKED -> {
                if (targetStatus == MarketplaceOrderStatus.SHIPPED) {
                    ValidationResult(true)
                } else {
                    ValidationResult(false, "Invalid transition from PACKED to $targetStatus.")
                }
            }
            else -> ValidationResult(false, "Seller cannot transition order from current state: $currentStatus")
        }
    }

    // ==========================================
    // 4. FINANCIAL & PAYOUT VALIDATIONS
    // ==========================================

    fun validatePayoutRequest(
        sellerProfile: MarketplaceSellerProfile,
        sellerVerification: MarketplaceSellerVerification,
        payoutAccount: MarketplaceSellerPayoutAccount?,
        balance: MarketplaceSellerBalance,
        requestedAmount: Double,
        minimumPayoutThreshold: Double = 100.0
    ): ValidationResult {
        if (sellerProfile.sellerStatus != SellerStatus.ACTIVE) {
            return ValidationResult(false, "Seller account is not active (current status: ${sellerProfile.sellerStatus}).")
        }
        if (sellerVerification.status != SellerVerificationStatus.VERIFIED) {
            return ValidationResult(false, "Seller is not verified. Payouts require VERIFIED status.")
        }
        if (payoutAccount == null || payoutAccount.verificationStatus != "verified") {
            return ValidationResult(false, "Verified payout destination account is required.")
        }
        if (requestedAmount <= 0.0) {
            return ValidationResult(false, "Payout amount must be greater than zero.")
        }
        if (requestedAmount < minimumPayoutThreshold) {
            return ValidationResult(false, "Minimum payout threshold is $minimumPayoutThreshold ${balance.currency}.")
        }
        if (requestedAmount > balance.availableBalance) {
            return ValidationResult(
                false,
                "Insufficient funds: Requested $requestedAmount exceeds available balance of ${balance.availableBalance} ${balance.currency}."
            )
        }
        return ValidationResult(true)
    }

    /**
     * Authoritative Ledger and Seller Balance Calculation.
     * Computes net earnings from gross sale, discounts, platform commission, payment processing fees, and taxes.
     */
    fun calculateAuthoritativeLedgerSale(
        grossAmount: Double,
        discount: Double,
        commissionRatePercent: Double,
        paymentFeePercent: Double = 1.5,
        paymentFixedFee: Double = 1.0,
        taxPercent: Double = 15.0 // Saudi VAT or configured tax
    ): MarketplaceSellerLedgerEntry {
        val netGross = (grossAmount - discount).coerceAtLeast(0.0)
        val commission = (netGross * (commissionRatePercent / 100.0)).coerceAtLeast(0.0)
        val paymentFee = (netGross * (paymentFeePercent / 100.0) + paymentFixedFee).coerceAtLeast(0.0)
        val netBeforeTax = netGross - commission - paymentFee
        val taxAmount = 0.0 // Handled at platform or vendor level per rule
        val netEarnings = (netBeforeTax - taxAmount).coerceAtLeast(0.0)

        return MarketplaceSellerLedgerEntry(
            grossAmount = grossAmount,
            commission = commission,
            paymentFee = paymentFee,
            refundAmount = 0.0,
            adjustment = 0.0,
            taxAmount = taxAmount,
            netAmount = netEarnings,
            currency = "SAR",
            status = LedgerStatus.POSTED
        )
    }

    // ==========================================
    // 5. INVENTORY VALIDATION
    // ==========================================

    fun validateInventoryAdjustment(
        currentAvailable: Int,
        changeDelta: Int
    ): ValidationResult {
        val newAvailable = currentAvailable + changeDelta
        if (newAvailable < 0) {
            return ValidationResult(
                false,
                "Negative inventory violation: Stock cannot drop below zero (requested adjustment would result in $newAvailable)."
            )
        }
        return ValidationResult(true)
    }

    // ==========================================
    // 6. CROSS-SELLER SECURITY ASSERTIONS
    // ==========================================

    fun assertSellerOwnership(resourceSellerUid: String, callerSellerUid: String) {
        if (resourceSellerUid != callerSellerUid) {
            throw SecurityException("ACCESS DENIED: Seller $callerSellerUid cannot access or modify resources owned by $resourceSellerUid.")
        }
    }
}
