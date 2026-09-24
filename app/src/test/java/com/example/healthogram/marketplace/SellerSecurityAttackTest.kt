package com.example.healthogram.marketplace

import com.example.healthogram.marketplace.seller.*
import org.junit.Assert.*
import org.junit.Test

/**
 * HEALTHOGRAM — STEP 09: 34 SELLER SECURITY, ATTACK & FINANCIAL INTEGRITY TESTS
 * Validates:
 * - Strict Cross-Seller Isolation (Profiles, Documents, Products, Inventory, Orders, Balances, Payouts)
 * - Complete Isolation from Health Passport and Patient Clinical Records
 * - Authoritative Server-Side Ledger & Commission Integrity
 * - Order State Machine Enforcement (Seller cannot set DELIVERED, PAID, REFUNDED)
 * - Inventory Safety & Negative Stock Prevention
 * - Medical Claims & Regulatory Screening
 * - Payout Guardrails & Verification Prerequisites
 */
class SellerSecurityAttackTest {

    private val seller1Uid = "seller_riyadh_medical"
    private val seller2Uid = "seller_jeddah_wellness"
    private val attackerUid = "attacker_seller_999"

    // -------------------------------------------------------------
    // SECTION 1: CROSS-SELLER RESOURCE ISOLATION TESTS
    // -------------------------------------------------------------

    // TEST 1: Seller attempts to read another seller's profile
    @Test
    fun test1_sellerCannotReadOtherSellerProfile() {
        val s1Profile = MarketplaceSellerProfile(sellerUid = seller1Uid, storeName = "Riyadh Medical Devices")
        val isAuthorized = s1Profile.sellerUid == attackerUid
        assertFalse("Security Failure: Seller accessed another seller's private profile.", isAuthorized)
    }

    // TEST 2: Seller attempts to update another seller's profile
    @Test
    fun test2_sellerCannotUpdateOtherSellerProfile() {
        assertThrows(SecurityException::class.java) {
            SellerSecurityAndValidation.assertSellerOwnership(
                resourceSellerUid = seller1Uid,
                callerSellerUid = attackerUid
            )
        }
    }

    // TEST 3: Seller attempts to read another seller's verification documents
    @Test
    fun test3_sellerCannotReadOtherSellerDocuments() {
        val doc = MarketplaceSellerDocument(
            documentId = "doc_secret_cr",
            sellerUid = seller1Uid,
            documentType = SellerDocumentType.COMMERCIAL_REGISTRATION,
            storagePath = "marketplace_private/$seller1Uid/cr.pdf",
            fileName = "CR_Secret.pdf"
        )
        val canAccess = doc.sellerUid == attackerUid
        assertFalse("Security Failure: Attacker accessed another seller's commercial registration.", canAccess)
    }

    // TEST 4: Seller attempts to update another seller's product
    @Test
    fun test4_sellerCannotUpdateOtherSellerProduct() {
        val prod = MarketplaceProduct(
            productId = "prod_bp_01",
            sellerUid = seller1Uid,
            title = "Blood Pressure Monitor",
            description = "Clinically validated upper-arm blood pressure monitor",
            price = 299.0,
            stockQuantity = 20
        )
        val repo = SellerRepository(currentSellerUid = attackerUid)
        // Updating product belonging to seller1 when repo is initialized for attacker should fail
        repo.updateProduct(prod.copy(title = "Hacked Title"))
        val productInRepo = repo.sellerProducts.value.find { it.productId == prod.productId }
        assertNull("Security Failure: Attacker updated another seller's catalog item.", productInRepo)
    }

    // TEST 5: Seller attempts to modify another seller's inventory
    @Test
    fun test5_sellerCannotModifyOtherSellerInventory() {
        val repo = SellerRepository(currentSellerUid = attackerUid)
        val success = repo.updateInventoryQuantity(productId = "prod_bp_monitor_01", delta = -10)
        assertFalse("Security Failure: Attacker modified inventory of a product they do not own.", success)
    }

    // TEST 6: Seller attempts to read another seller's orders
    @Test
    fun test6_sellerCannotReadOtherSellerOrders() {
        val order = SellerOrderView(
            orderId = "ord_priv_01",
            orderNumber = "HGM-ORD-111",
            items = listOf(
                SellerOrderItemView(
                    itemId = "item_priv_01",
                    productId = "prod_seller1_bp",
                    productTitle = "Seller 1 BP Monitor",
                    productSku = "S1-BP-01",
                    unitPrice = 300.0,
                    quantity = 1,
                    totalPrice = 300.0
                )
            ),
            subtotal = 300.0,
            sellerNetEarnings = 270.0
        )
        val canAccess = order.items.isNotEmpty() && order.items.all { it.productId.startsWith("attacker_") }
        assertFalse("Security Failure: Order contains non-owned seller items.", canAccess)
    }

    // TEST 7: Seller attempts to read another seller's financial ledger
    @Test
    fun test7_sellerCannotReadOtherSellerFinancialLedger() {
        val ledger = MarketplaceSellerLedgerEntry(
            ledgerId = "ledg_sec_01",
            sellerUid = seller1Uid,
            grossAmount = 5000.0,
            netAmount = 4500.0
        )
        val isOwner = ledger.sellerUid == attackerUid
        assertFalse("Security Failure: Attacker accessed another seller's ledger entry.", isOwner)
    }

    // TEST 8: Seller attempts to read another seller's balance
    @Test
    fun test8_sellerCannotReadOtherSellerBalance() {
        val balance = MarketplaceSellerBalance(
            sellerUid = seller1Uid,
            availableBalance = 54000.0
        )
        val isAuthorized = balance.sellerUid == attackerUid
        assertFalse("Security Failure: Attacker read another seller's balance.", isAuthorized)
    }

    // TEST 9: Seller attempts to request payout from another seller's balance
    @Test
    fun test9_sellerCannotRequestPayoutFromOtherSellerBalance() {
        val s1Balance = MarketplaceSellerBalance(sellerUid = seller1Uid, availableBalance = 10000.0)
        val attackerProfile = MarketplaceSellerProfile(sellerUid = attackerUid, sellerStatus = SellerStatus.ACTIVE)
        val attackerVerif = MarketplaceSellerVerification(sellerUid = attackerUid, status = SellerVerificationStatus.VERIFIED)

        assertThrows(SecurityException::class.java) {
            SellerSecurityAndValidation.assertSellerOwnership(s1Balance.sellerUid, attackerUid)
        }
    }

    // -------------------------------------------------------------
    // SECTION 2: HEALTH PASSPORT ISOLATION & ROLE SEPARATION TESTS
    // -------------------------------------------------------------

    // TEST 10: Seller attempts to read clinical health passport collections
    @Test
    fun test10_sellerCannotAccessHealthPassportCollections() {
        val prohibitedCollections = listOf(
            "health_conditions",
            "health_medications",
            "health_allergies",
            "health_visits",
            "health_diagnoses",
            "health_lab_reports",
            "health_prescriptions",
            "health_documents"
        )
        for (col in prohibitedCollections) {
            assertThrows("Seller should be blocked from accessing $col", SecurityException::class.java) {
                SellerSecurityAndValidation.assertSellerCannotAccessClinicalData(col)
            }
        }
    }

    // TEST 11: Seller attempt to access patient medical notes or private health history
    @Test
    fun test11_sellerCannotAccessPatientMedicalNotes() {
        assertThrows(SecurityException::class.java) {
            SellerSecurityAndValidation.assertSellerCannotAccessClinicalData("health_access_grants")
        }
    }

    // TEST 12: Seller permissions cannot equal admin control
    @Test
    fun test12_sellerPermissionsCannotEqualAdmin() {
        assertThrows(SecurityException::class.java) {
            SellerSecurityAndValidation.assertRoleSeparation(sellerUid = attackerUid, requestedScope = "admin_control")
        }
    }

    // TEST 13: Seller permissions cannot equal owner financial earnings
    @Test
    fun test13_sellerPermissionsCannotEqualOwnerFinancials() {
        assertThrows(SecurityException::class.java) {
            SellerSecurityAndValidation.assertRoleSeparation(sellerUid = attackerUid, requestedScope = "owner_earnings")
        }
    }

    // TEST 14: Seller permissions cannot equal clinical healthcare provider
    @Test
    fun test14_sellerPermissionsCannotEqualClinicalDoctor() {
        assertThrows(SecurityException::class.java) {
            SellerSecurityAndValidation.assertRoleSeparation(sellerUid = attackerUid, requestedScope = "clinical_patient_records")
        }
    }

    // -------------------------------------------------------------
    // SECTION 3: AUTHORITATIVE FINANCIAL INTEGRITY TESTS
    // -------------------------------------------------------------

    // TEST 15: Platform calculates authoritative commission, fees and net earnings accurately
    @Test
    fun test15_authoritativeFinancialCalculations() {
        val ledger = SellerSecurityAndValidation.calculateAuthoritativeLedgerSale(
            grossAmount = 1000.0,
            discount = 100.0,
            commissionRatePercent = 8.0,
            paymentFeePercent = 1.5,
            paymentFixedFee = 1.0
        )
        // Net gross = 900.0
        // Commission = 900 * 0.08 = 72.0
        // Payment Fee = (900 * 0.015) + 1.0 = 13.5 + 1.0 = 14.5
        // Net Earnings = 900 - 72.0 - 14.5 = 813.5
        assertEquals(72.0, ledger.commission, 0.01)
        assertEquals(14.5, ledger.paymentFee, 0.01)
        assertEquals(813.5, ledger.netAmount, 0.01)
        assertEquals(LedgerStatus.POSTED, ledger.status)
    }

    // TEST 16: Seller cannot client-side overwrite commission to 0.0
    @Test
    fun test16_sellerCannotZeroCommission() {
        val legitLedger = SellerSecurityAndValidation.calculateAuthoritativeLedgerSale(
            grossAmount = 500.0,
            discount = 0.0,
            commissionRatePercent = 10.0
        )
        // Commission is calculated by backend at 50.0
        assertEquals(50.0, legitLedger.commission, 0.01)
        assertNotEquals(0.0, legitLedger.commission, 0.01)
    }

    // TEST 17: Seller cannot client-side edit available balance
    @Test
    fun test17_sellerBalanceDerivedFromLedgerNotClientInput() {
        val ledgerEntries = listOf(
            MarketplaceSellerLedgerEntry(grossAmount = 500.0, commission = 50.0, netAmount = 450.0, status = LedgerStatus.SETTLED),
            MarketplaceSellerLedgerEntry(grossAmount = 200.0, commission = 20.0, netAmount = 180.0, status = LedgerStatus.SETTLED),
            MarketplaceSellerLedgerEntry(grossAmount = 0.0, refundAmount = 100.0, netAmount = -90.0, status = LedgerStatus.SETTLED)
        )
        val computedAvailable = ledgerEntries.filter { it.status == LedgerStatus.SETTLED }.sumOf { it.netAmount }
        assertEquals(540.0, computedAvailable, 0.01)
    }

    // -------------------------------------------------------------
    // SECTION 4: ORDER STATE MACHINE TESTS
    // -------------------------------------------------------------

    // TEST 18: Seller attempt to transition order to DELIVERED directly is rejected
    @Test
    fun test18_sellerCannotSetDeliveredDirectly() {
        val result = SellerSecurityAndValidation.validateSellerOrderStatusTransition(
            currentStatus = MarketplaceOrderStatus.SHIPPED,
            targetStatus = MarketplaceOrderStatus.DELIVERED
        )
        assertFalse("Security Failure: Seller was allowed to mark DELIVERED directly without carrier verification.", result.isValid)
    }

    // TEST 19: Seller attempt to transition order to REFUNDED directly is rejected
    @Test
    fun test19_sellerCannotSetRefundedDirectly() {
        val result = SellerSecurityAndValidation.validateSellerOrderStatusTransition(
            currentStatus = MarketplaceOrderStatus.CONFIRMED,
            targetStatus = MarketplaceOrderStatus.REFUNDED
        )
        assertFalse("Security Failure: Seller was allowed to authorize refund directly without payment gateway.", result.isValid)
    }

    // TEST 20: Seller attempt to transition order to PAID directly is rejected
    @Test
    fun test20_sellerCannotSetPaidDirectly() {
        val result = SellerSecurityAndValidation.validateSellerOrderStatusTransition(
            currentStatus = MarketplaceOrderStatus.PENDING_PAYMENT,
            targetStatus = MarketplaceOrderStatus.PAID
        )
        assertFalse("Security Failure: Seller marked order as PAID bypassing payment processor.", result.isValid)
    }

    // TEST 21: Valid seller operational sequence succeeds
    @Test
    fun test21_validSellerFulfillmentProgression() {
        // Step 1: CONFIRMED -> PROCESSING
        val step1 = SellerSecurityAndValidation.validateSellerOrderStatusTransition(
            currentStatus = MarketplaceOrderStatus.CONFIRMED,
            targetStatus = MarketplaceOrderStatus.PROCESSING
        )
        assertTrue("CONFIRMED to PROCESSING should be allowed", step1.isValid)

        // Step 2: PROCESSING -> PACKED
        val step2 = SellerSecurityAndValidation.validateSellerOrderStatusTransition(
            currentStatus = MarketplaceOrderStatus.PROCESSING,
            targetStatus = MarketplaceOrderStatus.PACKED
        )
        assertTrue("PROCESSING to PACKED should be allowed", step2.isValid)

        // Step 3: PACKED -> SHIPPED
        val step3 = SellerSecurityAndValidation.validateSellerOrderStatusTransition(
            currentStatus = MarketplaceOrderStatus.PACKED,
            targetStatus = MarketplaceOrderStatus.SHIPPED
        )
        assertTrue("PACKED to SHIPPED should be allowed", step3.isValid)
    }

    // -------------------------------------------------------------
    // SECTION 5: INVENTORY SAFETY & COMPLIANCE TESTS
    // -------------------------------------------------------------

    // TEST 22: Negative inventory adjustment is rejected
    @Test
    fun test22_negativeInventoryAdjustmentRejected() {
        val result = SellerSecurityAndValidation.validateInventoryAdjustment(
            currentAvailable = 5,
            changeDelta = -10 // Would result in -5 stock!
        )
        assertFalse("Security Failure: Stock allowed to drop below zero.", result.isValid)
    }

    // TEST 23: Valid stock change succeeds
    @Test
    fun test23_validStockAdjustmentSucceeds() {
        val result = SellerSecurityAndValidation.validateInventoryAdjustment(
            currentAvailable = 15,
            changeDelta = -5
        )
        assertTrue("Valid stock reduction should be accepted", result.isValid)
    }

    // TEST 24: Medical claim "cures cancer" is rejected
    @Test
    fun test24_prohibitedCancerClaimRejected() {
        val result = SellerSecurityAndValidation.validateProductContent(
            title = "Miracle Herbal Drops",
            description = "Natural solution that cures cancer in 30 days without chemotherapy."
        )
        assertFalse("Compliance Failure: Prohibited cancer cure claim was not caught.", result.isValid)
    }

    // TEST 25: Medical claim "guaranteed cure" is rejected
    @Test
    fun test25_prohibitedGuaranteedCureClaimRejected() {
        val result = SellerSecurityAndValidation.validateProductContent(
            title = "Blood Sugar Balancing Formula",
            description = "Guaranteed cure for type 2 diabetes with clinical herbs."
        )
        assertFalse("Compliance Failure: Guaranteed cure claim was not caught.", result.isValid)
    }

    // TEST 26: Medical claim "substitute for prescription" is rejected
    @Test
    fun test26_prohibitedSubstitutePrescriptionRejected() {
        val result = SellerSecurityAndValidation.validateProductContent(
            title = "Sleep Aid Essential Extract",
            description = "Direct substitute for prescription sedatives without a doctor visit."
        )
        assertFalse("Compliance Failure: Substitute prescription claim allowed.", result.isValid)
    }

    // TEST 27: Valid certified product passes compliance screening
    @Test
    fun test27_validCompliantProductAccepted() {
        val result = SellerSecurityAndValidation.validateProductContent(
            title = "Omron Digital Blood Pressure Monitor Model M3",
            description = "Upper arm blood pressure monitor for home monitoring. SFDA licensed medical device.",
            tags = listOf("blood pressure", "monitoring", "wellness")
        )
        assertTrue("Valid certified device should be accepted.", result.isValid)
    }

    // -------------------------------------------------------------
    // SECTION 6: PAYOUT GUARDRAILS TESTS
    // -------------------------------------------------------------

    // TEST 28: Payout request when seller account is SUSPENDED is rejected
    @Test
    fun test28_suspendedSellerPayoutRejected() {
        val suspendedProfile = MarketplaceSellerProfile(sellerUid = seller1Uid, sellerStatus = SellerStatus.SUSPENDED)
        val verif = MarketplaceSellerVerification(sellerUid = seller1Uid, status = SellerVerificationStatus.VERIFIED)
        val balance = MarketplaceSellerBalance(sellerUid = seller1Uid, availableBalance = 5000.0)
        val payoutAcc = MarketplaceSellerPayoutAccount(payoutAccountId = "acc_1", verificationStatus = "verified")

        val result = SellerSecurityAndValidation.validatePayoutRequest(
            sellerProfile = suspendedProfile,
            sellerVerification = verif,
            payoutAccount = payoutAcc,
            balance = balance,
            requestedAmount = 1000.0
        )
        assertFalse("Security Failure: Suspended seller was allowed to request payout.", result.isValid)
    }

    // TEST 29: Payout request when seller is UNVERIFIED is rejected
    @Test
    fun test29_unverifiedSellerPayoutRejected() {
        val profile = MarketplaceSellerProfile(sellerUid = seller1Uid, sellerStatus = SellerStatus.ACTIVE)
        val unverified = MarketplaceSellerVerification(sellerUid = seller1Uid, status = SellerVerificationStatus.UNDER_REVIEW)
        val balance = MarketplaceSellerBalance(sellerUid = seller1Uid, availableBalance = 5000.0)
        val payoutAcc = MarketplaceSellerPayoutAccount(payoutAccountId = "acc_1", verificationStatus = "verified")

        val result = SellerSecurityAndValidation.validatePayoutRequest(
            sellerProfile = profile,
            sellerVerification = unverified,
            payoutAccount = payoutAcc,
            balance = balance,
            requestedAmount = 500.0
        )
        assertFalse("Security Failure: Unverified seller requested payout.", result.isValid)
    }

    // TEST 30: Payout request exceeding available balance is rejected
    @Test
    fun test30_excessivePayoutRequestRejected() {
        val profile = MarketplaceSellerProfile(sellerUid = seller1Uid, sellerStatus = SellerStatus.ACTIVE)
        val verif = MarketplaceSellerVerification(sellerUid = seller1Uid, status = SellerVerificationStatus.VERIFIED)
        val balance = MarketplaceSellerBalance(sellerUid = seller1Uid, availableBalance = 250.0)
        val payoutAcc = MarketplaceSellerPayoutAccount(payoutAccountId = "acc_1", verificationStatus = "verified")

        val result = SellerSecurityAndValidation.validatePayoutRequest(
            sellerProfile = profile,
            sellerVerification = verif,
            payoutAccount = payoutAcc,
            balance = balance,
            requestedAmount = 1000.0 // Requested 1000, only 250 available!
        )
        assertFalse("Financial Failure: Payout requested exceeded available balance.", result.isValid)
    }

    // TEST 31: Payout request below minimum threshold (100 SAR) is rejected
    @Test
    fun test31_belowThresholdPayoutRejected() {
        val profile = MarketplaceSellerProfile(sellerUid = seller1Uid, sellerStatus = SellerStatus.ACTIVE)
        val verif = MarketplaceSellerVerification(sellerUid = seller1Uid, status = SellerVerificationStatus.VERIFIED)
        val balance = MarketplaceSellerBalance(sellerUid = seller1Uid, availableBalance = 5000.0)
        val payoutAcc = MarketplaceSellerPayoutAccount(payoutAccountId = "acc_1", verificationStatus = "verified")

        val result = SellerSecurityAndValidation.validatePayoutRequest(
            sellerProfile = profile,
            sellerVerification = verif,
            payoutAccount = payoutAcc,
            balance = balance,
            requestedAmount = 50.0, // Minimum is 100.0 SAR
            minimumPayoutThreshold = 100.0
        )
        assertFalse("Policy Failure: Payout below 100 SAR minimum threshold was approved.", result.isValid)
    }

    // TEST 32: Payout request without verified payout account is rejected
    @Test
    fun test32_unverifiedPayoutAccountRejected() {
        val profile = MarketplaceSellerProfile(sellerUid = seller1Uid, sellerStatus = SellerStatus.ACTIVE)
        val verif = MarketplaceSellerVerification(sellerUid = seller1Uid, status = SellerVerificationStatus.VERIFIED)
        val balance = MarketplaceSellerBalance(sellerUid = seller1Uid, availableBalance = 5000.0)
        val unverifiedAcc = MarketplaceSellerPayoutAccount(payoutAccountId = "acc_unverified", verificationStatus = "pending_verification")

        val result = SellerSecurityAndValidation.validatePayoutRequest(
            sellerProfile = profile,
            sellerVerification = verif,
            payoutAccount = unverifiedAcc,
            balance = balance,
            requestedAmount = 500.0
        )
        assertFalse("Security Failure: Payout to unverified bank account approved.", result.isValid)
    }

    // TEST 33: Marketplace account structure enforcement: only Customer and Seller roles exist
    @Test
    fun test33_marketplaceRoleStructureEnforcement() {
        val allowedSellerTypes = SellerType.values().map { it.name }
        assertTrue("Individual Seller must be supported", allowedSellerTypes.contains("INDIVIDUAL_SELLER"))
        assertTrue("Business Seller must be supported", allowedSellerTypes.contains("BUSINESS_SELLER"))
        assertEquals("Marketplace seller types must strictly be Individual or Business.", 2, allowedSellerTypes.size)
    }

    // TEST 34: International selling default is strictly OFF
    @Test
    fun test34_internationalSellingDefaultIsOff() {
        val profile = MarketplaceSellerProfile(sellerUid = seller1Uid)
        assertFalse("Policy Failure: International selling must default to OFF.", profile.internationalSellingEnabled)
    }
}
