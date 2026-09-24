package com.example.healthogram.owner

import com.example.healthogram.core.AccountType
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive Unit Test Suite for Healthogram Step 17:
 * Owner Control Panel + Global Feature Flags + Platform Configuration Engine.
 */
class OwnerControlEngineTest {

    private lateinit var service: PlatformConfigurationService
    private lateinit var engine: OwnerControlEngine

    private val rootOwnerUid = "owner_root_001"
    private val delegateUid = "usr_del_finance"
    private val standardUserUid = "usr_regular_alex"

    @Before
    fun setup() {
        service = PlatformConfigurationService.getInstance()
        service.resetForTesting()
        engine = OwnerControlEngine(service)

        // Reset reauth on root owner
        val owner = service.currentOwner.value
        service.updateOwnerProfile(
            owner.copy(
                lastReauthAt = System.currentTimeMillis(),
                reauthRequired = false
            )
        )
    }

    @After
    fun tearDown() {
        service.resetForTesting()
    }

    @Test
    fun testPlatformOwnerAuthorizationAndDelegation() {
        // Root owner has all permissions
        assertTrue(engine.isPlatformOwner(rootOwnerUid))
        assertTrue(engine.hasOwnerPermission(rootOwnerUid, "owner.emergency_controls.execute"))
        assertTrue(engine.hasOwnerPermission(rootOwnerUid, "owner.feature_flags.publish"))
        assertTrue(engine.hasOwnerPermission(rootOwnerUid, "owner.financial.edit"))

        // Standard user has no owner permissions
        assertFalse(engine.isPlatformOwner(standardUserUid))
        assertFalse(engine.hasOwnerPermission(standardUserUid, "owner.feature_flags.publish"))

        // Delegate has specified permissions only
        assertTrue(engine.hasOwnerPermission(delegateUid, "owner.financial.view"))
        assertTrue(engine.hasOwnerPermission(delegateUid, "owner.financial.edit"))
        assertFalse(engine.hasOwnerPermission(delegateUid, "owner.emergency_controls.execute"))
    }

    @Test
    fun testReauthVerificationAndExpiry() {
        // Correct root PIN succeeds
        assertTrue(engine.verifyOwnerReauth(rootOwnerUid, "9900"))

        // Incorrect PIN fails
        assertFalse(engine.verifyOwnerReauth(rootOwnerUid, "1234"))

        // Verify reauth freshness
        assertTrue(engine.isReauthFresh(rootOwnerUid))

        // When reauth is older than window, it is no longer fresh
        val staleOwner = service.currentOwner.value.copy(
            lastReauthAt = System.currentTimeMillis() - (20 * 60 * 1000L) // 20 mins ago
        )
        service.updateOwnerProfile(staleOwner)
        assertFalse(engine.isReauthFresh(rootOwnerUid))
    }

    @Test
    fun testSevenTierFeatureFlagPrecedenceHierarchy() {
        // Base test context: US, Individual user
        val baseContext = FeatureEvaluationContext(
            uid = "user_test_123",
            country = "US",
            accountCategory = AccountType.INDIVIDUAL
        )

        // Ensure baseline social feed is ON
        val baseline = engine.evaluateFeature("social_feed", baseContext)
        assertEquals(FeatureFlagStatus.ON, baseline.effectiveStatus)
        assertTrue(baseline.isEnabled)

        // -----------------------------------------------------------------
        // TIER 1: EMERGENCY KILL SWITCH OVERRIDES EVERYTHING
        // -----------------------------------------------------------------
        // Trigger emergency kill switch for Live Streaming Subsystem
        engine.triggerEmergencyKillSwitch(
            actorUid = rootOwnerUid,
            key = EmergencySwitchKey.LIVE_DISABLE,
            typedConfirmation = "DISABLE LIVE STREAMING",
            reason = "Live stream safety test",
            pin = "9900"
        )

        val liveEmergencyEval = engine.evaluateFeature("live_streaming", baseContext)
        assertEquals(FeatureFlagStatus.OFF, liveEmergencyEval.effectiveStatus)
        assertFalse(liveEmergencyEval.isEnabled)
        assertTrue(liveEmergencyEval.precedenceReason.contains("1. Emergency Kill Switch"))

        // Restore emergency switch
        engine.deactivateEmergencyKillSwitch(
            actorUid = rootOwnerUid,
            key = EmergencySwitchKey.LIVE_DISABLE,
            reason = "Test completed",
            pin = "9900"
        )

        // -----------------------------------------------------------------
        // TIER 2: EXPLICIT USER OVERRIDE
        // -----------------------------------------------------------------
        val flaggedFlag = service.featureFlags.value["live_streaming"]!!
        service.updateFeatureFlag(
            flaggedFlag.copy(
                status = FeatureFlagStatus.OFF,
                globalEnabled = false,
                userTargetingOverrides = mapOf("vip_broadcaster_99" to FeatureFlagStatus.ON)
            )
        )

        // Regular user should see OFF
        val regularUserEval = engine.evaluateFeature(
            "live_streaming",
            baseContext.copy(uid = "regular_user")
        )
        assertEquals(FeatureFlagStatus.OFF, regularUserEval.effectiveStatus)
        assertFalse(regularUserEval.isEnabled)

        // Targeted user should see ON
        val vipUserEval = engine.evaluateFeature(
            "live_streaming",
            baseContext.copy(uid = "vip_broadcaster_99")
        )
        assertEquals(FeatureFlagStatus.ON, vipUserEval.effectiveStatus)
        assertTrue(vipUserEval.isEnabled)
        assertTrue(vipUserEval.precedenceReason.contains("2. Explicit User Override"))

        // -----------------------------------------------------------------
        // TIER 3: ACCOUNT CATEGORY OVERRIDE
        // -----------------------------------------------------------------
        // Video calls enabled for Doctor, Hospital, Clinic; disabled for Individual
        val videoFlag = service.featureFlags.value["video_calls"]!!
        service.updateFeatureFlag(
            videoFlag.copy(
                accountTypeOverrides = mapOf(
                    AccountType.INDIVIDUAL to FeatureFlagStatus.OFF,
                    AccountType.DOCTOR to FeatureFlagStatus.ON,
                    AccountType.HOSPITAL to FeatureFlagStatus.ON
                )
            )
        )

        val individualVideo = engine.evaluateFeature(
            "video_calls",
            baseContext.copy(accountCategory = AccountType.INDIVIDUAL)
        )
        assertEquals(FeatureFlagStatus.OFF, individualVideo.effectiveStatus)
        assertFalse(individualVideo.isEnabled)
        assertTrue(individualVideo.precedenceReason.contains("3. Account Category Override"))

        val doctorVideo = engine.evaluateFeature(
            "video_calls",
            baseContext.copy(accountCategory = AccountType.DOCTOR)
        )
        assertEquals(FeatureFlagStatus.ON, doctorVideo.effectiveStatus)
        assertTrue(doctorVideo.isEnabled)

        // -----------------------------------------------------------------
        // TIER 4: COUNTRY OVERRIDE
        // -----------------------------------------------------------------
        // Deactivate country "KW"
        engine.updateCountryActiveStatus(
            actorUid = rootOwnerUid,
            countryCode = "KW",
            active = false,
            reason = "Regulatory review test",
            pin = "9900"
        )

        val kwEval = engine.evaluateFeature(
            "social_feed",
            baseContext.copy(country = "KW")
        )
        assertEquals(FeatureFlagStatus.OFF, kwEval.effectiveStatus)
        assertFalse(kwEval.isEnabled)
        assertTrue(kwEval.precedenceReason.contains("4. Country Override"))

        // Restore country KW
        engine.updateCountryActiveStatus(
            actorUid = rootOwnerUid,
            countryCode = "KW",
            active = true,
            reason = "Restored",
            pin = "9900"
        )

        // -----------------------------------------------------------------
        // TIER 6: GLOBAL FEATURE FLAG
        // -----------------------------------------------------------------
        val aiFlag = service.featureFlags.value["ai_studio"]!!
        service.updateFeatureFlag(
            aiFlag.copy(
                status = FeatureFlagStatus.MAINTENANCE,
                maintenanceMessage = "Vertex AI upgrade underway"
            )
        )

        val aiEval = engine.evaluateFeature("ai_studio", baseContext)
        assertEquals(FeatureFlagStatus.MAINTENANCE, aiEval.effectiveStatus)
        assertFalse(aiEval.isEnabled)
        assertEquals("Vertex AI upgrade underway", aiEval.displayMessage)
    }

    @Test
    fun testEmergencyKillSwitchRequiresTypedConfirmationAndPin() {
        // Fails with invalid confirmation string
        assertThrows(IllegalArgumentException::class.java) {
            engine.triggerEmergencyKillSwitch(
                actorUid = rootOwnerUid,
                key = EmergencySwitchKey.GLOBAL_APP_DISABLE,
                typedConfirmation = "WRONG CONFIRMATION",
                reason = "Testing invalid confirmation",
                pin = "9900"
            )
        }

        // Fails with wrong PIN
        assertThrows(SecurityException::class.java) {
            engine.triggerEmergencyKillSwitch(
                actorUid = rootOwnerUid,
                key = EmergencySwitchKey.GLOBAL_APP_DISABLE,
                typedConfirmation = "DISABLE ALL SERVICES",
                reason = "Testing invalid PIN",
                pin = "0000"
            )
        }

        // Succeeds with exact confirmation and correct PIN
        val result = engine.triggerEmergencyKillSwitch(
            actorUid = rootOwnerUid,
            key = EmergencySwitchKey.GLOBAL_APP_DISABLE,
            typedConfirmation = "DISABLE ALL SERVICES",
            reason = "Zero-day containment simulation",
            pin = "9900"
        )
        assertTrue(result.isTriggered)
        assertTrue(engine.isEmergencyKillSwitchActive())

        // Restore switch
        engine.deactivateEmergencyKillSwitch(
            actorUid = rootOwnerUid,
            key = EmergencySwitchKey.GLOBAL_APP_DISABLE,
            reason = "Simulation concluded",
            pin = "9900"
        )
        assertFalse(engine.isEmergencyKillSwitchActive())
    }

    @Test
    fun testAccountCategoryGovernanceExcludesPharmacy() {
        val categories = service.accountCategoryControls.value
        assertEquals(5, categories.size)
        assertTrue(categories.containsKey(AccountType.INDIVIDUAL))
        assertTrue(categories.containsKey(AccountType.DOCTOR))
        assertTrue(categories.containsKey(AccountType.CLINIC))
        assertTrue(categories.containsKey(AccountType.HOSPITAL))
        assertTrue(categories.containsKey(AccountType.LABORATORY))

        // Verify that only the 5 valid categories exist and no custom "pharmacy" exists
        val keys = categories.keys.map { it.name }
        assertFalse("Pharmacy must NOT be present as an account category", keys.contains("PHARMACY"))
    }

    @Test
    fun testImmutableAuditLogsAndSnapshotVersioning() {
        val initialLogCount = service.auditLogs.value.size
        val initialVersionCount = service.configurationVersions.value.size

        // Publish a feature flag update
        engine.updateFeatureFlagStatus(
            actorUid = rootOwnerUid,
            featureKey = "audio_calls",
            newStatus = FeatureFlagStatus.BETA,
            reason = "Beta rollout for voice calling v2",
            pin = "9900"
        )

        // Check that an audit log was appended (newest log is at index 0)
        val updatedLogs = service.auditLogs.value
        assertEquals(initialLogCount + 1, updatedLogs.size)
        val latestLog = updatedLogs.first()
        assertEquals("FEATURE_FLAG_UPDATED", latestLog.action)
        assertEquals("audio_calls", latestLog.targetId)
        assertEquals("Beta rollout for voice calling v2", latestLog.reason)

        // Check that a version snapshot was recorded
        val updatedVersions = service.configurationVersions.value
        assertEquals(initialVersionCount + 1, updatedVersions.size)
    }

    @Test
    fun testFinancialLedgerAndEarningsCalculations() {
        engine.recordLedgerTransaction(
            type = LedgerType.ORGANIZATION_SUBSCRIPTION,
            gross = 500.0,
            commission = 500.0,
            serviceFee = 0.0,
            processingFee = 15.0,
            tax = 0.0,
            country = "US",
            reference = "sub_annual_01"
        )

        engine.recordLedgerTransaction(
            type = LedgerType.MARKETPLACE_SALE,
            gross = 200.0,
            commission = 16.0,
            serviceFee = 3.0,
            processingFee = 6.0,
            tax = 0.0,
            country = "US",
            reference = "ord_test_01"
        )

        val summary = engine.calculateEarningsSummary()
        assertEquals(700.0, summary.grossPlatformVolume, 0.01)
        assertEquals(516.0, summary.totalCommissionsEarned, 0.01)
        assertEquals(3.0, summary.totalServiceFeesEarned, 0.01)
        assertEquals(21.0, summary.totalProcessingCostDeductions, 0.01)
        assertTrue(summary.netAvailableBalance > 0)
    }
}
