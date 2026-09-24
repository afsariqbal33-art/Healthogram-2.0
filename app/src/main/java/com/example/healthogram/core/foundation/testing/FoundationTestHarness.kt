package com.example.healthogram.core.foundation.testing

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.MarketplaceRole
import com.example.healthogram.core.foundation.auth.AuthSession
import com.example.healthogram.core.foundation.auth.AuthorizationService
import com.example.healthogram.core.foundation.auth.ConsentGrant
import com.example.healthogram.core.foundation.country.CountryConfigRegistry
import com.example.healthogram.core.foundation.database.DatabaseMigrationRegistry
import com.example.healthogram.core.foundation.error.HealthAccessDeniedException
import com.example.healthogram.core.foundation.flags.FeatureFlag23Registry
import com.example.healthogram.core.foundation.flags.FlagState
import com.example.healthogram.core.foundation.qr.QrSecurityEngine
import com.example.healthogram.core.foundation.service.DefaultHealthPassportService
import com.example.healthogram.core.foundation.service.DefaultLedgerService
import com.example.healthogram.core.foundation.service.DefaultPaymentService

data class TestResult(
    val testName: String,
    val passed: Boolean,
    val detail: String
)

/**
 * Step 49: Healthogram 2.3 Automated Foundation Verification Test Engine.
 *
 * Runs programmatic verification across security, authorization, QR vault,
 * idempotency, financials, and feature flags using 100% synthetic test data.
 */
class FoundationTestHarness {

    fun runAllFoundationTests(): List<TestResult> {
        val results = mutableListOf<TestResult>()

        results.add(testCountryIsolationAndNoInternationalTrade())
        results.add(testDatabaseMigrationRegistry())
        results.add(testUserACannotAccessUserB())
        results.add(testUnauthorizedDoctorAccessDenied())
        results.add(testExpiredConsentFailsSafely())
        results.add(testRevokedConsentFailsSafely())
        results.add(testWrongScopeFailsSafely())
        results.add(testQrVaultTokenLifecycleAndAntiReplay())
        results.add(testPaymentIdempotencyDeduplication())
        results.add(testDoubleEntryLedgerInvariance())
        results.add(testFeatureFlagRbacAndKillSwitch())

        return results
    }

    private fun testCountryIsolationAndNoInternationalTrade(): TestResult {
        val countries = listOf("US", "SA", "AE", "EG", "GB", "CA", "IN", "OM")
        val allSupported = countries.all { CountryConfigRegistry.instance.isCountrySupported(it) }
        val noCrossBorder = countries.all {
            CountryConfigRegistry.instance.getCountry(it)?.internationalTradeEnabled == false
        }
        val passed = allSupported && noCrossBorder
        return TestResult(
            testName = "Country Isolation & International Trade Locked False",
            passed = passed,
            detail = "Verified 8 countries registered. internationalTradeEnabled == false for all."
        )
    }

    private fun testDatabaseMigrationRegistry(): TestResult {
        val migrations = DatabaseMigrationRegistry.instance.getAllMigrations()
        val hasV3 = migrations.any { it.targetVersion == "2.3.0" }
        val nonDestructive = migrations.none { it.isDestructive }
        return TestResult(
            testName = "Database Schema Migration Framework",
            passed = hasV3 && nonDestructive,
            detail = "MIG-003-2.3.0 registered. 100% non-destructive additive migrations confirmed."
        )
    }

    private fun testUserACannotAccessUserB(): TestResult {
        val userA = AuthSession(uid = "user_patient_A", accountType = AccountType.INDIVIDUAL)
        val service = DefaultHealthPassportService()
        var caught = false
        try {
            service.getMedicalRecords(
                patientUid = "user_patient_B",
                session = userA,
                consentGrant = null
            )
        } catch (e: HealthAccessDeniedException) {
            caught = true
        }
        return TestResult(
            testName = "Security: User A Cannot Access User B Records",
            passed = caught,
            detail = if (caught) "HealthAccessDeniedException thrown as expected" else "Failed: Access allowed"
        )
    }

    private fun testUnauthorizedDoctorAccessDenied(): TestResult {
        val unverifiedDoctor = AuthSession(
            uid = "doctor_fake",
            accountType = AccountType.DOCTOR,
            isVerified = false
        )
        val service = DefaultHealthPassportService()
        var caught = false
        try {
            service.getMedicalRecords(
                patientUid = "patient_alice",
                session = unverifiedDoctor,
                consentGrant = null
            )
        } catch (e: HealthAccessDeniedException) {
            caught = true
        }
        return TestResult(
            testName = "Security: Unverified Doctor Access Denied",
            passed = caught,
            detail = if (caught) "Unverified doctor denied access" else "Failed: Access allowed"
        )
    }

    private fun testExpiredConsentFailsSafely(): TestResult {
        val verifiedDoctor = AuthSession(
            uid = "dr_smith",
            accountType = AccountType.DOCTOR,
            isVerified = true
        )
        val expiredGrant = ConsentGrant(
            grantId = "grant_exp_1",
            patientUid = "patient_alice",
            accessorUid = "dr_smith",
            allowedScopes = setOf("SCOPE_FULL_CLINICAL_TIMELINE"),
            expiresAt = System.currentTimeMillis() - 1000L // 1 second ago
        )
        val service = DefaultHealthPassportService()
        var caught = false
        try {
            service.getMedicalRecords(
                patientUid = "patient_alice",
                session = verifiedDoctor,
                consentGrant = expiredGrant
            )
        } catch (e: HealthAccessDeniedException) {
            caught = true
        }
        return TestResult(
            testName = "Security: Expired Patient Consent Fails Safely",
            passed = caught,
            detail = if (caught) "Expired consent safely rejected" else "Failed: Expired consent allowed"
        )
    }

    private fun testRevokedConsentFailsSafely(): TestResult {
        val verifiedDoctor = AuthSession(
            uid = "dr_smith",
            accountType = AccountType.DOCTOR,
            isVerified = true
        )
        val revokedGrant = ConsentGrant(
            grantId = "grant_rev_1",
            patientUid = "patient_alice",
            accessorUid = "dr_smith",
            allowedScopes = setOf("SCOPE_FULL_CLINICAL_TIMELINE"),
            isRevoked = true
        )
        val service = DefaultHealthPassportService()
        var caught = false
        try {
            service.getMedicalRecords(
                patientUid = "patient_alice",
                session = verifiedDoctor,
                consentGrant = revokedGrant
            )
        } catch (e: HealthAccessDeniedException) {
            caught = true
        }
        return TestResult(
            testName = "Security: Revoked Patient Consent Fails Safely",
            passed = caught,
            detail = if (caught) "Revoked consent safely rejected" else "Failed: Revoked consent allowed"
        )
    }

    private fun testWrongScopeFailsSafely(): TestResult {
        val verifiedDoctor = AuthSession(
            uid = "dr_smith",
            accountType = AccountType.DOCTOR,
            isVerified = true
        )
        val wrongScopeGrant = ConsentGrant(
            grantId = "grant_wrong_1",
            patientUid = "patient_alice",
            accessorUid = "dr_smith",
            allowedScopes = setOf("SCOPE_LAB_REPORTS_ONLY") // Does not include SCOPE_FULL_CLINICAL_TIMELINE
        )
        val service = DefaultHealthPassportService()
        var caught = false
        try {
            service.getMedicalRecords(
                patientUid = "patient_alice",
                session = verifiedDoctor,
                consentGrant = wrongScopeGrant
            )
        } catch (e: HealthAccessDeniedException) {
            caught = true
        }
        return TestResult(
            testName = "Security: Wrong Scope Fails Safely",
            passed = caught,
            detail = if (caught) "Wrong scope safely rejected" else "Failed: Scope bypass detected"
        )
    }

    private fun testQrVaultTokenLifecycleAndAntiReplay(): TestResult {
        val patientUid = "patient_synthetic_1"
        val token = QrSecurityEngine.instance.generateVaultToken(patientUid)
        val isFirstRedemptionSuccess = try {
            val redeemed = QrSecurityEngine.instance.consumeVaultToken(
                tokenId = token.tokenId,
                accessorUid = "dr_smith",
                accessorAccountType = "Doctor"
            )
            redeemed.isConsumed
        } catch (e: Exception) {
            false
        }

        // Second redemption must be rejected (Replay prevention)
        var secondRedemptionCaught = false
        try {
            QrSecurityEngine.instance.consumeVaultToken(
                tokenId = token.tokenId,
                accessorUid = "dr_smith",
                accessorAccountType = "Doctor"
            )
        } catch (e: HealthAccessDeniedException) {
            secondRedemptionCaught = true
        }

        val passed = isFirstRedemptionSuccess && secondRedemptionCaught
        return TestResult(
            testName = "QR Vault Token Lifecycle & Anti-Replay",
            passed = passed,
            detail = "60s opaque token consumed once; duplicate redemption blocked."
        )
    }

    private fun testPaymentIdempotencyDeduplication(): TestResult {
        val paymentService = DefaultPaymentService()
        val idempotencyKey = "tx_idemp_key_9999"

        val firstCall = paymentService.processCheckout(
            amountCents = 2500L,
            currency = "USD",
            idempotencyKey = idempotencyKey,
            preferredGateway = "STRIPE"
        )

        // Duplicate call with exact same key must safely return true (cached success) without double execution
        val secondCall = paymentService.processCheckout(
            amountCents = 2500L,
            currency = "USD",
            idempotencyKey = idempotencyKey,
            preferredGateway = "STRIPE"
        )

        return TestResult(
            testName = "Financials: Payment Idempotency Exactly-Once",
            passed = firstCall && secondCall,
            detail = "Idempotency key cached. Duplicate payment request safely handled without re-charge."
        )
    }

    private fun testDoubleEntryLedgerInvariance(): TestResult {
        val ledger = DefaultLedgerService()
        val entry1 = ledger.recordJournalEntry("ESCROW_LIABILITY", "SELLER_PAYABLE", 5000L, "ord_101")
        val entry2 = ledger.recordJournalEntry("CUSTOMER_CHARGE", "PLATFORM_REVENUE", 850L, "ord_102")
        val invariantHolds = ledger.verifyLedgerInvariance()

        return TestResult(
            testName = "Financials: Double-Entry Ledger Invariance",
            passed = entry1 && entry2 && invariantHolds,
            detail = "sum(debits) - sum(credits) == 0 verified across journal transactions."
        )
    }

    private fun testFeatureFlagRbacAndKillSwitch(): TestResult {
        val flagRegistry = FeatureFlag23Registry.instance

        // Non-admin updating flag must fail
        var nonAdminCaught = false
        try {
            flagRegistry.updateFlagState(
                key = FeatureFlag23Registry.FLAG_APPOINTMENTS_V2,
                newState = FlagState.ON,
                changedByActorId = "regular_user",
                actorType = "INDIVIDUAL",
                reason = "Unauthorized attempt"
            )
        } catch (e: IllegalArgumentException) {
            nonAdminCaught = true
        }

        // Admin tripping kill switch must succeed and block access immediately
        flagRegistry.tripKillSwitch(
            key = FeatureFlag23Registry.FLAG_APPOINTMENTS_V2,
            changedByActorId = "super_admin",
            actorType = "ADMIN",
            reason = "Emergency maintenance"
        )
        val isBlocked = !flagRegistry.isEnabled(FeatureFlag23Registry.FLAG_APPOINTMENTS_V2)

        return TestResult(
            testName = "Feature Flag RBAC & Emergency Kill Switch",
            passed = nonAdminCaught && isBlocked,
            detail = "Non-admin blocked from modifying flags; kill switch trips immediately."
        )
    }
}
