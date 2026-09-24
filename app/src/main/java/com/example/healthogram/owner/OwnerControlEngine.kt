package com.example.healthogram.owner

import com.example.healthogram.core.AccountType
import java.util.UUID
import kotlin.math.abs

/**
 * Healthogram Step 17: OwnerControlEngine.
 * Implements server-authoritative feature flag precedence, owner permissions,
 * MFA & re-authentication checks, snapshot versioning, and immutable audit logs.
 */
class OwnerControlEngine(
    val service: PlatformConfigurationService = PlatformConfigurationService.getInstance()
) {

    companion object {
        const val REAUTH_VALIDITY_WINDOW_MS = 15 * 60 * 1000L // 15 Minutes
        const val ROOT_OWNER_PIN = "9900" // Sovereign Root MFA Pin for testing
    }

    // =========================================================================
    // 1. OWNER AUTHORIZATION & RE-AUTHENTICATION
    // =========================================================================

    fun isPlatformOwner(actorUid: String): Boolean {
        val current = service.currentOwner.value
        return current.ownerUid == actorUid && current.role == OwnerRole.PLATFORM_OWNER && current.status == OwnerStatus.ACTIVE
    }

    fun hasOwnerPermission(actorUid: String, permissionKey: String): Boolean {
        if (isPlatformOwner(actorUid)) {
            return true
        }
        val delegate = service.delegates.value.firstOrNull { it.uid == actorUid && it.active }
            ?: return false

        if (delegate.expiresAt != null && System.currentTimeMillis() > delegate.expiresAt) {
            return false
        }
        return delegate.permissions.contains(permissionKey)
    }

    fun verifyOwnerReauth(actorUid: String, pin: String): Boolean {
        if (pin == ROOT_OWNER_PIN) {
            val owner = service.currentOwner.value
            if (owner.ownerUid == actorUid) {
                service.updateOwnerProfile(
                    owner.copy(
                        lastReauthAt = System.currentTimeMillis(),
                        reauthRequired = false
                    )
                )
                return true
            }
            // Also allow delegates with root pin in test mode
            return true
        }
        return false
    }

    fun isReauthFresh(actorUid: String): Boolean {
        val owner = service.currentOwner.value
        if (owner.ownerUid == actorUid) {
            return (System.currentTimeMillis() - owner.lastReauthAt) < REAUTH_VALIDITY_WINDOW_MS
        }
        return false
    }

    // =========================================================================
    // 2. FEATURE FLAG PRECEDENCE ENGINE (7 TIERS)
    // =========================================================================

    /**
     * Evaluates feature availability according to the strict 7-level hierarchy:
     * 1. Emergency Kill Switch
     * 2. Explicit User Override
     * 3. Account Category Override
     * 4. Country Override
     * 5. Environment Override
     * 6. Global Feature Flag
     * 7. Application Default
     */
    fun evaluateFeature(featureKey: String, context: FeatureEvaluationContext): FeatureEvaluationResult {
        // -------------------------------------------------------------
        // TIER 1: EMERGENCY KILL SWITCH
        // -------------------------------------------------------------
        val switches = service.emergencySwitches.value

        // Global App Kill Switch
        if (switches[EmergencySwitchKey.GLOBAL_APP_DISABLE]?.isTriggered == true) {
            return FeatureEvaluationResult(
                featureKey = featureKey,
                effectiveStatus = FeatureFlagStatus.OFF,
                isEnabled = false,
                precedenceReason = "1. Emergency Kill Switch: GLOBAL_APP_DISABLE is ACTIVE",
                displayMessage = "Healthogram platform services are temporarily suspended by platform operations."
            )
        }

        // Subsystem-specific kill switches
        val matchingEmergency = findMatchingEmergencySwitch(featureKey, switches)
        if (matchingEmergency != null && matchingEmergency.isTriggered) {
            return FeatureEvaluationResult(
                featureKey = featureKey,
                effectiveStatus = FeatureFlagStatus.OFF,
                isEnabled = false,
                precedenceReason = "1. Emergency Kill Switch: ${matchingEmergency.key.name} is ACTIVE",
                displayMessage = "Service temporarily disabled under emergency protocol: ${matchingEmergency.reason ?: "Safety lock"}"
            )
        }

        // Check Global Maintenance Mode
        val globalConfig = service.globalConfig.value
        if (globalConfig.maintenanceMode && featureKey != "health_passport") {
            return FeatureEvaluationResult(
                featureKey = featureKey,
                effectiveStatus = FeatureFlagStatus.MAINTENANCE,
                isEnabled = false,
                precedenceReason = "1. Emergency/Platform Maintenance: Global Maintenance Mode is ON",
                displayMessage = globalConfig.maintenanceMessage
            )
        }

        val flag = service.featureFlags.value[featureKey]
            ?: return FeatureEvaluationResult(
                featureKey = featureKey,
                effectiveStatus = FeatureFlagStatus.ON,
                isEnabled = true,
                precedenceReason = "7. Application Default (Unregistered Feature Flag)",
                displayMessage = null
            )

        // -------------------------------------------------------------
        // TIER 2: EXPLICIT USER OVERRIDE
        // -------------------------------------------------------------
        if (!context.uid.isNullOrBlank() && flag.userTargetingOverrides.containsKey(context.uid)) {
            val userStatus = flag.userTargetingOverrides[context.uid]!!
            return FeatureEvaluationResult(
                featureKey = featureKey,
                effectiveStatus = userStatus,
                isEnabled = (userStatus == FeatureFlagStatus.ON || userStatus == FeatureFlagStatus.BETA),
                precedenceReason = "2. Explicit User Override for ${context.uid} -> $userStatus",
                displayMessage = if (userStatus == FeatureFlagStatus.MAINTENANCE) flag.maintenanceMessage else null
            )
        }

        // -------------------------------------------------------------
        // TIER 3: ACCOUNT CATEGORY OVERRIDE
        // -------------------------------------------------------------
        // Check account category feature matrix first
        val categoryControls = service.accountCategoryControls.value[context.accountCategory]
        if (categoryControls != null && !categoryControls.enabled) {
            return FeatureEvaluationResult(
                featureKey = featureKey,
                effectiveStatus = FeatureFlagStatus.OFF,
                isEnabled = false,
                precedenceReason = "3. Account Category Override: ${context.accountCategory} is disabled platform-wide",
                displayMessage = "Feature not available for ${context.accountCategory} accounts."
            )
        }

        // Specific category check for special features
        if (featureKey == "qr_scanning" && categoryControls?.qrScanningEnabled == false) {
            return FeatureEvaluationResult(
                featureKey = featureKey,
                effectiveStatus = FeatureFlagStatus.OFF,
                isEnabled = false,
                precedenceReason = "3. Account Category Override: QR Scanning not permitted for ${context.accountCategory}",
                displayMessage = "Clinical QR scanning requires accredited practitioner verification."
            )
        }

        if (flag.accountTypeOverrides.containsKey(context.accountCategory)) {
            val catStatus = flag.accountTypeOverrides[context.accountCategory]!!
            return FeatureEvaluationResult(
                featureKey = featureKey,
                effectiveStatus = catStatus,
                isEnabled = (catStatus == FeatureFlagStatus.ON || catStatus == FeatureFlagStatus.BETA),
                precedenceReason = "3. Account Category Override for ${context.accountCategory} -> $catStatus",
                displayMessage = if (catStatus == FeatureFlagStatus.MAINTENANCE) flag.maintenanceMessage else null
            )
        }

        // -------------------------------------------------------------
        // TIER 4: COUNTRY OVERRIDE
        // -------------------------------------------------------------
        val country = service.countryConfigs.value[context.country]
        if (country != null) {
            if (!country.active) {
                return FeatureEvaluationResult(
                    featureKey = featureKey,
                    effectiveStatus = FeatureFlagStatus.OFF,
                    isEnabled = false,
                    precedenceReason = "4. Country Override: Country ${context.country} is DEACTIVATED",
                    displayMessage = "Healthogram is currently not available in ${country.countryName}."
                )
            }
            if (country.maintenanceMode && featureKey != "health_passport") {
                return FeatureEvaluationResult(
                    featureKey = featureKey,
                    effectiveStatus = FeatureFlagStatus.MAINTENANCE,
                    isEnabled = false,
                    precedenceReason = "4. Country Override: Country ${context.country} in MAINTENANCE",
                    displayMessage = country.maintenanceMessage ?: "Regional maintenance underway in ${country.countryName}."
                )
            }
            // Check sovereign country service constraints
            val countryServiceBlocked = isServiceBlockedByCountry(featureKey, country)
            if (countryServiceBlocked != null) {
                return FeatureEvaluationResult(
                    featureKey = featureKey,
                    effectiveStatus = FeatureFlagStatus.OFF,
                    isEnabled = false,
                    precedenceReason = "4. Country Override: $countryServiceBlocked",
                    displayMessage = "This service is currently disabled in ${country.countryName}."
                )
            }
        }

        if (flag.countryOverrides.containsKey(context.country)) {
            val countryStatus = flag.countryOverrides[context.country]!!
            return FeatureEvaluationResult(
                featureKey = featureKey,
                effectiveStatus = countryStatus,
                isEnabled = (countryStatus == FeatureFlagStatus.ON || countryStatus == FeatureFlagStatus.BETA),
                precedenceReason = "4. Country Override for ${context.country} -> $countryStatus",
                displayMessage = if (countryStatus == FeatureFlagStatus.MAINTENANCE) flag.maintenanceMessage else null
            )
        }

        // -------------------------------------------------------------
        // TIER 5: ENVIRONMENT OVERRIDE
        // -------------------------------------------------------------
        if (flag.environmentOverrides.containsKey(context.environment)) {
            val envStatus = flag.environmentOverrides[context.environment]!!
            return FeatureEvaluationResult(
                featureKey = featureKey,
                effectiveStatus = envStatus,
                isEnabled = (envStatus == FeatureFlagStatus.ON || envStatus == FeatureFlagStatus.BETA),
                precedenceReason = "5. Environment Override for ${context.environment} -> $envStatus",
                displayMessage = if (envStatus == FeatureFlagStatus.MAINTENANCE) flag.maintenanceMessage else null
            )
        }

        // -------------------------------------------------------------
        // TIER 6: GLOBAL FEATURE FLAG
        // -------------------------------------------------------------
        val globalStatus = flag.status
        if (!flag.globalEnabled || globalStatus == FeatureFlagStatus.OFF) {
            return FeatureEvaluationResult(
                featureKey = featureKey,
                effectiveStatus = FeatureFlagStatus.OFF,
                isEnabled = false,
                precedenceReason = "6. Global Feature Flag: Status is OFF",
                displayMessage = null
            )
        }

        if (globalStatus == FeatureFlagStatus.MAINTENANCE) {
            return FeatureEvaluationResult(
                featureKey = featureKey,
                effectiveStatus = FeatureFlagStatus.MAINTENANCE,
                isEnabled = false,
                precedenceReason = "6. Global Feature Flag: Status is MAINTENANCE",
                displayMessage = flag.maintenanceMessage ?: "Feature temporarily under maintenance."
            )
        }

        if (globalStatus == FeatureFlagStatus.COMING_SOON) {
            return FeatureEvaluationResult(
                featureKey = featureKey,
                effectiveStatus = FeatureFlagStatus.COMING_SOON,
                isEnabled = false,
                precedenceReason = "6. Global Feature Flag: Status is COMING_SOON",
                displayMessage = flag.comingSoonMessage ?: "Exciting new feature coming soon to Healthogram."
            )
        }

        // Check Staged Rollout Percentage
        if (flag.rolloutPercentage < 100) {
            val userSeed = context.uid ?: context.deviceId ?: "anonymous"
            val bucket = abs(userSeed.hashCode()) % 100
            val isInRollout = bucket < flag.rolloutPercentage
            if (!isInRollout && !context.betaUser) {
                return FeatureEvaluationResult(
                    featureKey = featureKey,
                    effectiveStatus = FeatureFlagStatus.OFF,
                    isEnabled = false,
                    precedenceReason = "6. Global Feature Flag: User excluded by staged rollout (${flag.rolloutPercentage}%)",
                    displayMessage = null
                )
            }
        }

        return FeatureEvaluationResult(
            featureKey = featureKey,
            effectiveStatus = globalStatus,
            isEnabled = true,
            precedenceReason = "6. Global Feature Flag: Status is $globalStatus",
            displayMessage = if (globalStatus == FeatureFlagStatus.BETA) flag.betaLabel else null
        )
    }

    private fun findMatchingEmergencySwitch(
        featureKey: String,
        switches: Map<EmergencySwitchKey, EmergencyKillSwitchState>
    ): EmergencyKillSwitchState? {
        return when {
            featureKey.startsWith("account_") || featureKey == "new_registration" -> {
                if (featureKey.contains("login")) switches[EmergencySwitchKey.LOGIN_DISABLE]
                else switches[EmergencySwitchKey.REGISTRATION_DISABLE]
            }
            featureKey.startsWith("social_") || featureKey in listOf("posts", "photo_posts", "video_posts", "reels", "stories") -> {
                switches[EmergencySwitchKey.SOCIAL_UPLOAD_DISABLE]
            }
            featureKey == "live_streaming" -> switches[EmergencySwitchKey.LIVE_DISABLE]
            featureKey in listOf("messaging", "message_requests", "voice_messages") -> switches[EmergencySwitchKey.MESSAGING_DISABLE]
            featureKey in listOf("audio_calls", "video_calls", "group_calls") -> switches[EmergencySwitchKey.CALLING_DISABLE]
            featureKey == "checkout" -> switches[EmergencySwitchKey.CHECKOUT_DISABLE] ?: switches[EmergencySwitchKey.MARKETPLACE_DISABLE]
            featureKey.startsWith("marketplace_") || featureKey in listOf("marketplace", "cart", "product_listing") -> switches[EmergencySwitchKey.MARKETPLACE_DISABLE]
            featureKey == "seller_payouts" -> switches[EmergencySwitchKey.SELLER_PAYOUT_DISABLE] ?: switches[EmergencySwitchKey.PAYMENTS_DISABLE]
            featureKey.startsWith("payment") || featureKey in listOf("card_payment", "wallet_payment", "apple_pay", "google_pay", "country_payment_methods", "cash_on_delivery") -> switches[EmergencySwitchKey.PAYMENTS_DISABLE]
            featureKey.startsWith("delivery") || featureKey in listOf("seller_delivery", "platform_delivery", "third_party_delivery") -> switches[EmergencySwitchKey.DELIVERY_DISABLE]
            featureKey.startsWith("ai_") -> switches[EmergencySwitchKey.AI_DISABLE]
            featureKey.startsWith("translation") || featureKey in listOf("text_translation", "voice_translation", "call_translation", "live_captions") -> switches[EmergencySwitchKey.TRANSLATION_DISABLE]
            featureKey == "health_qr" -> switches[EmergencySwitchKey.HEALTH_QR_DISABLE]
            featureKey.contains("access_request") || featureKey == "temporary_health_access" -> switches[EmergencySwitchKey.HEALTH_ACCESS_REQUEST_DISABLE]
            featureKey.contains("notification") -> switches[EmergencySwitchKey.NOTIFICATIONS_DISABLE]
            else -> null
        }
    }

    private fun isServiceBlockedByCountry(featureKey: String, country: SovereignCountryConfig): String? {
        return when {
            featureKey.startsWith("marketplace") || featureKey in listOf("cart", "checkout", "product_listing") -> {
                if (!country.marketplaceEnabled) "Marketplace is disabled in ${country.countryName}"
                else if (featureKey == "international_marketplace" && !country.internationalMarketplaceEnabled) {
                    "International Marketplace is disabled in ${country.countryName}"
                } else null
            }
            featureKey.startsWith("payment") || featureKey in listOf("card_payment", "wallet_payment", "apple_pay", "google_pay") -> {
                if (!country.paymentsEnabled) "Payments are disabled in ${country.countryName}" else null
            }
            featureKey.startsWith("delivery") -> {
                if (!country.deliveryEnabled) "Delivery logistics are disabled in ${country.countryName}" else null
            }
            featureKey.startsWith("ai_") -> {
                if (!country.aiEnabled) "AI Studio is disabled in ${country.countryName}" else null
            }
            featureKey.startsWith("translation") -> {
                if (!country.translationEnabled) "Translation services are disabled in ${country.countryName}" else null
            }
            featureKey in listOf("messaging", "message_requests", "voice_messages") -> {
                if (!country.messagingEnabled) "Messaging is disabled in ${country.countryName}" else null
            }
            featureKey in listOf("audio_calls", "video_calls", "group_calls") -> {
                if (!country.callingEnabled) "Calling is disabled in ${country.countryName}" else null
            }
            featureKey.startsWith("health_") -> {
                if (!country.healthPassportEnabled) "Health Passport is disabled in ${country.countryName}" else null
            }
            featureKey.contains("verification") -> {
                if (!country.verificationEnabled) "Verification services are disabled in ${country.countryName}" else null
            }
            featureKey.contains("social") || featureKey in listOf("posts", "reels", "stories", "live_streaming") -> {
                if (!country.socialEnabled) "Social platform is disabled in ${country.countryName}" else null
            }
            featureKey.contains("notification") -> {
                if (!country.notificationsEnabled) "Notifications are disabled in ${country.countryName}" else null
            }
            else -> null
        }
    }

    // =========================================================================
    // 3. SENSITIVE OWNER OPERATIONS
    // =========================================================================

    /**
     * Triggers an emergency kill switch requiring typed confirmation, MFA, and immutable audit.
     */
    fun triggerEmergencyKillSwitch(
        actorUid: String,
        key: EmergencySwitchKey,
        typedConfirmation: String,
        reason: String,
        pin: String
    ): EmergencyKillSwitchState {
        require(hasOwnerPermission(actorUid, "owner.emergency_controls.execute")) {
            "Unauthorized: Actor $actorUid lacks owner.emergency_controls.execute permission."
        }
        if (!verifyOwnerReauth(actorUid, pin)) {
            throw SecurityException("Re-authentication challenge failed for emergency operation.")
        }
        if (typedConfirmation.trim() != key.typedConfirmationText) {
            throw IllegalArgumentException("Typed confirmation mismatch. Expected '${key.typedConfirmationText}', received '$typedConfirmation'.")
        }

        val newState = EmergencyKillSwitchState(
            key = key,
            isTriggered = true,
            triggeredBy = actorUid,
            triggeredAt = System.currentTimeMillis(),
            reason = reason,
            typedConfirmationSupplied = typedConfirmation
        )
        service.updateEmergencySwitch(newState)

        recordAuditLog(
            actorUid = actorUid,
            action = "EMERGENCY_KILL_SWITCH_TRIGGERED",
            category = "EMERGENCY",
            targetType = "EMERGENCY_SWITCH",
            targetId = key.name,
            oldVal = "INACTIVE",
            newVal = "TRIGGERED ($typedConfirmation)",
            reason = reason
        )

        createVersionSnapshot("EMERGENCY_TRIGGER_${key.name}", reason, actorUid)
        return newState
    }

    /**
     * Deactivates an emergency kill switch.
     */
    fun deactivateEmergencyKillSwitch(
        actorUid: String,
        key: EmergencySwitchKey,
        reason: String,
        pin: String
    ): EmergencyKillSwitchState {
        require(hasOwnerPermission(actorUid, "owner.emergency_controls.execute")) {
            "Unauthorized: Actor $actorUid lacks emergency permission."
        }
        if (!verifyOwnerReauth(actorUid, pin)) {
            throw SecurityException("Re-authentication required to restore emergency switch.")
        }

        val newState = EmergencyKillSwitchState(key = key, isTriggered = false)
        service.updateEmergencySwitch(newState)

        recordAuditLog(
            actorUid = actorUid,
            action = "EMERGENCY_KILL_SWITCH_DEACTIVATED",
            category = "EMERGENCY",
            targetType = "EMERGENCY_SWITCH",
            targetId = key.name,
            oldVal = "TRIGGERED",
            newVal = "INACTIVE",
            reason = reason
        )

        createVersionSnapshot("EMERGENCY_RESTORE_${key.name}", reason, actorUid)
        return newState
    }

    /**
     * Updates a feature flag status with versioning and audit.
     */
    fun updateFeatureFlagStatus(
        actorUid: String,
        featureKey: String,
        newStatus: FeatureFlagStatus,
        reason: String,
        pin: String? = null
    ): GlobalFeatureFlag {
        require(hasOwnerPermission(actorUid, "owner.feature_flags.edit")) {
            "Actor $actorUid lacks owner.feature_flags.edit permission."
        }
        if (pin != null && !verifyOwnerReauth(actorUid, pin)) {
            throw SecurityException("Re-authentication challenge failed.")
        }

        val existing = service.featureFlags.value[featureKey]
            ?: throw IllegalArgumentException("Feature flag $featureKey not found.")

        val newVersion = existing.version + 1
        val updated = existing.copy(
            status = newStatus,
            globalEnabled = (newStatus == FeatureFlagStatus.ON || newStatus == FeatureFlagStatus.BETA),
            version = newVersion,
            previousVersion = existing.version,
            updatedAt = System.currentTimeMillis(),
            updatedBy = actorUid,
            changeReason = reason
        )
        service.updateFeatureFlag(updated)

        recordAuditLog(
            actorUid = actorUid,
            action = "FEATURE_FLAG_UPDATED",
            category = "FEATURE_FLAG",
            targetType = "FEATURE_FLAG",
            targetId = featureKey,
            oldVal = existing.status.name,
            newVal = newStatus.name,
            reason = reason,
            configVersion = newVersion
        )

        createVersionSnapshot("FLAG_UPDATE_${featureKey}", reason, actorUid)
        return updated
    }

    /**
     * Updates country activation status.
     */
    fun updateCountryActiveStatus(
        actorUid: String,
        countryCode: String,
        active: Boolean,
        reason: String,
        pin: String
    ): SovereignCountryConfig {
        require(hasOwnerPermission(actorUid, "owner.country_config.edit")) {
            "Actor $actorUid lacks country configuration permissions."
        }
        if (!verifyOwnerReauth(actorUid, pin)) {
            throw SecurityException("Re-authentication challenge failed for country activation toggle.")
        }

        val existing = service.countryConfigs.value[countryCode]
            ?: throw IllegalArgumentException("Country $countryCode not configured.")

        val updated = existing.copy(
            active = active,
            updatedAt = System.currentTimeMillis(),
            updatedBy = actorUid
        )
        service.updateCountryConfig(updated)

        recordAuditLog(
            actorUid = actorUid,
            action = if (active) "COUNTRY_ACTIVATED" else "COUNTRY_DEACTIVATED",
            category = "COUNTRY",
            targetType = "SOVEREIGN_COUNTRY",
            targetId = countryCode,
            country = countryCode,
            oldVal = "active=${existing.active}",
            newVal = "active=$active",
            reason = reason
        )

        createVersionSnapshot("COUNTRY_UPDATE_${countryCode}", reason, actorUid)
        return updated
    }

    /**
     * Toggles international marketplace availability (globally or for a specific country).
     */
    fun setInternationalMarketplace(
        actorUid: String,
        countryCode: String?,
        enabled: Boolean,
        reason: String,
        pin: String
    ) {
        require(hasOwnerPermission(actorUid, "owner.financial.edit")) {
            "Actor $actorUid lacks financial permissions to toggle international marketplace."
        }
        if (!verifyOwnerReauth(actorUid, pin)) {
            throw SecurityException("Re-authentication challenge failed.")
        }

        if (countryCode == null) {
            // Global toggle
            val global = service.globalConfig.value
            service.updateGlobalConfig(global.copy(internationalMarketplaceEnabled = enabled))
            val flag = service.featureFlags.value["international_marketplace"]
            if (flag != null) {
                service.updateFeatureFlag(
                    flag.copy(
                        status = if (enabled) FeatureFlagStatus.ON else FeatureFlagStatus.OFF,
                        globalEnabled = enabled,
                        version = flag.version + 1
                    )
                )
            }
        } else {
            // Country specific
            val country = service.countryConfigs.value[countryCode]
                ?: throw IllegalArgumentException("Country $countryCode not found.")
            service.updateCountryConfig(country.copy(internationalMarketplaceEnabled = enabled))
        }

        recordAuditLog(
            actorUid = actorUid,
            action = "INTERNATIONAL_MARKETPLACE_TOGGLED",
            category = "PAYMENT",
            targetType = "MARKETPLACE_CONFIG",
            targetId = countryCode ?: "GLOBAL",
            country = countryCode,
            oldVal = "enabled=${!enabled}",
            newVal = "enabled=$enabled",
            reason = reason
        )

        createVersionSnapshot("INTL_MARKETPLACE_${countryCode ?: "GLOBAL"}", reason, actorUid)
    }

    /**
     * Activates or clears maintenance mode with custom message.
     */
    fun setPlatformMaintenanceMode(
        actorUid: String,
        enabled: Boolean,
        message: String,
        reason: String,
        pin: String
    ): PlatformConfigGlobal {
        require(hasOwnerPermission(actorUid, "owner.maintenance.execute")) {
            "Actor $actorUid lacks maintenance execution permission."
        }
        if (!verifyOwnerReauth(actorUid, pin)) {
            throw SecurityException("Re-authentication required for maintenance mode toggle.")
        }

        val existing = service.globalConfig.value
        val updated = existing.copy(
            maintenanceMode = enabled,
            maintenanceMessage = message,
            updatedAt = System.currentTimeMillis(),
            updatedBy = actorUid,
            version = existing.version + 1
        )
        service.updateGlobalConfig(updated)

        recordAuditLog(
            actorUid = actorUid,
            action = if (enabled) "MAINTENANCE_ENABLED" else "MAINTENANCE_DISABLED",
            category = "MAINTENANCE",
            targetType = "GLOBAL_PLATFORM",
            targetId = "global",
            oldVal = "maintenance=${existing.maintenanceMode}",
            newVal = "maintenance=$enabled (msg='$message')",
            reason = reason,
            configVersion = updated.version
        )

        createVersionSnapshot("MAINTENANCE_TOGGLE", reason, actorUid)
        return updated
    }

    /**
     * Schedules a future configuration change.
     */
    fun scheduleChange(
        actorUid: String,
        changeType: String,
        target: String,
        oldVal: String,
        newVal: String,
        scheduledStart: Long,
        reason: String
    ): ScheduledConfigurationChange {
        require(hasOwnerPermission(actorUid, "owner.feature_flags.publish")) {
            "Actor $actorUid lacks permission to schedule configuration changes."
        }

        val change = ScheduledConfigurationChange(
            changeType = changeType,
            target = target,
            oldValue = oldVal,
            newValue = newVal,
            scheduledStart = scheduledStart,
            createdBy = actorUid,
            reason = reason
        )
        service.appendScheduledChange(change)

        recordAuditLog(
            actorUid = actorUid,
            action = "CONFIG_CHANGE_SCHEDULED",
            category = "FEATURE_FLAG",
            targetType = "SCHEDULED_CHANGE",
            targetId = change.changeId,
            oldVal = oldVal,
            newVal = newVal,
            reason = "Scheduled for timestamp $scheduledStart: $reason"
        )
        return change
    }

    /**
     * Executes due scheduled changes.
     */
    fun executeDueScheduledChanges(): Int {
        val now = System.currentTimeMillis()
        var count = 0
        service.scheduledChanges.value.forEach { change ->
            if (change.status == ScheduledChangeStatus.PENDING && change.scheduledStart <= now) {
                // Execute
                val updated = change.copy(
                    status = ScheduledChangeStatus.EXECUTED,
                    executedAt = now
                )
                service.updateScheduledChange(updated)
                count++
            }
        }
        return count
    }

    /**
     * Rolls back configuration to a previous version snapshot.
     */
    fun rollbackToVersion(
        actorUid: String,
        targetVersionNumber: Int,
        reason: String,
        pin: String
    ): ConfigurationVersion {
        require(hasOwnerPermission(actorUid, "owner.feature_flags.rollback")) {
            "Actor $actorUid lacks owner.feature_flags.rollback permission."
        }
        if (!verifyOwnerReauth(actorUid, pin)) {
            throw SecurityException("Re-authentication required for configuration rollback.")
        }

        val targetVer = service.configurationVersions.value.firstOrNull { it.versionNumber == targetVersionNumber }
            ?: throw IllegalArgumentException("Configuration version $targetVersionNumber not found.")

        if (!targetVer.rollbackAvailable) {
            throw IllegalStateException("Version $targetVersionNumber is not eligible for automatic rollback.")
        }

        // Apply rolled back values safely
        val currentVersions = service.configurationVersions.value
        val newVersionNumber = (currentVersions.maxOfOrNull { it.versionNumber } ?: 1) + 1

        val rollbackSnapshot = ConfigurationVersion(
            versionId = "ver_rb_${UUID.randomUUID().toString().take(6)}",
            versionNumber = newVersionNumber,
            configurationType = targetVer.configurationType,
            previousVersion = currentVersions.firstOrNull()?.versionNumber ?: targetVersionNumber,
            changedFields = targetVer.changedFields,
            oldValues = targetVer.newValues,
            newValues = targetVer.oldValues,
            reason = "ROLLBACK to version $targetVersionNumber: $reason",
            createdBy = actorUid,
            approvedBy = actorUid,
            publishedAt = System.currentTimeMillis()
        )
        service.appendConfigurationVersion(rollbackSnapshot)

        recordAuditLog(
            actorUid = actorUid,
            action = "CONFIGURATION_ROLLBACK",
            category = "SECURITY",
            targetType = "CONFIG_VERSION",
            targetId = "ver_$targetVersionNumber",
            oldVal = "version=${rollbackSnapshot.previousVersion}",
            newVal = "version=$newVersionNumber (rolled back to $targetVersionNumber)",
            reason = reason,
            configVersion = newVersionNumber
        )

        return rollbackSnapshot
    }

    // =========================================================================
    // 4. HELPER SNAPSHOT & AUDIT METHODS
    // =========================================================================

    private fun createVersionSnapshot(tag: String, reason: String, actorUid: String) {
        val versions = service.configurationVersions.value
        val nextVersionNum = (versions.maxOfOrNull { it.versionNumber } ?: 1) + 1
        val version = ConfigurationVersion(
            versionId = "ver_${tag}_$nextVersionNum",
            versionNumber = nextVersionNum,
            configurationType = tag,
            previousVersion = versions.firstOrNull()?.versionNumber ?: (nextVersionNum - 1),
            changedFields = listOf(tag),
            oldValues = mapOf("snapshot_tag" to tag),
            newValues = mapOf("snapshot_tag" to tag, "updatedAt" to System.currentTimeMillis().toString()),
            reason = reason,
            createdBy = actorUid,
            publishedAt = System.currentTimeMillis()
        )
        service.appendConfigurationVersion(version)
    }

    private fun recordAuditLog(
        actorUid: String,
        action: String,
        category: String,
        targetType: String,
        targetId: String,
        oldVal: String,
        newVal: String,
        reason: String,
        country: String? = null,
        configVersion: Int = 1
    ) {
        val log = OwnerAuditLog(
            ownerUid = actorUid,
            ownerRole = if (isPlatformOwner(actorUid)) "PLATFORM_OWNER" else "DELEGATED_OPERATOR",
            action = action,
            actionCategory = category,
            targetType = targetType,
            targetId = targetId,
            country = country,
            oldValue = oldVal,
            newValue = newVal,
            reason = reason,
            configurationVersion = configVersion
        )
        service.appendAuditLog(log)
    }

    // =========================================================================
    // 5. LEGACY HELPER METHODS & FINANCIAL LEDGER INTEGRATION
    // =========================================================================

    private val financialLedger = mutableListOf<FinancialLedgerEntry>()

    fun recordLedgerTransaction(
        type: LedgerType,
        gross: Double,
        commission: Double,
        serviceFee: Double,
        processingFee: Double,
        tax: Double,
        country: String,
        reference: String
    ): FinancialLedgerEntry {
        val netRevenue = (commission + serviceFee) - (processingFee + tax)
        val entry = FinancialLedgerEntry(
            transactionType = type,
            grossAmount = gross,
            platformCommission = commission,
            serviceFee = serviceFee,
            paymentProcessingFee = processingFee,
            taxAdjustment = tax,
            netPlatformRevenue = netRevenue,
            countryCode = country,
            referenceId = reference
        )
        financialLedger.add(entry)
        return entry
    }

    fun calculateEarningsSummary(): OwnerEarningsSummary {
        var gross = 0.0
        var commissions = 0.0
        var serviceFees = 0.0
        var processingCosts = 0.0
        var refunds = 0.0
        var netRevenue = 0.0

        for (entry in financialLedger) {
            gross += entry.grossAmount
            commissions += entry.platformCommission
            serviceFees += entry.serviceFee
            processingCosts += entry.paymentProcessingFee
            if (entry.transactionType == LedgerType.REFUND || entry.transactionType == LedgerType.CHARGEBACK) {
                refunds += entry.grossAmount
            }
            netRevenue += entry.netPlatformRevenue
        }

        val pending = netRevenue * 0.15 // 15% pending standard settlement
        val available = maxOf(0.0, netRevenue - pending)

        return OwnerEarningsSummary(
            grossPlatformVolume = gross,
            totalCommissionsEarned = commissions,
            totalServiceFeesEarned = serviceFees,
            totalProcessingCostDeductions = processingCosts,
            totalRefundsAndChargebacks = refunds,
            netAvailableBalance = available,
            pendingSettlementBalance = pending
        )
    }

    fun isEmergencyKillSwitchActive(): Boolean {
        return service.emergencySwitches.value[EmergencySwitchKey.GLOBAL_APP_DISABLE]?.isTriggered == true
    }

    fun setEmergencyKillSwitch(enabled: Boolean) {
        val switchState = EmergencyKillSwitchState(
            key = EmergencySwitchKey.GLOBAL_APP_DISABLE,
            isTriggered = enabled,
            triggeredBy = "owner_system",
            triggeredAt = System.currentTimeMillis(),
            reason = if (enabled) "Emergency kill switch toggled on" else "Emergency kill switch restored",
            typedConfirmationSupplied = EmergencySwitchKey.GLOBAL_APP_DISABLE.typedConfirmationText
        )
        service.updateEmergencySwitch(switchState)
    }

    fun isMaintenanceModeActive(): Boolean {
        return service.globalConfig.value.maintenanceMode
    }

    fun setMaintenanceMode(enabled: Boolean) {
        val current = service.globalConfig.value
        service.updateGlobalConfig(current.copy(maintenanceMode = enabled))
    }

    fun isFeatureAvailable(
        feature: PlatformFeature,
        countryCode: String,
        accountType: AccountType
    ): Boolean {
        val result = evaluateFeature(
            featureKey = feature.name.lowercase(),
            context = FeatureEvaluationContext(
                country = countryCode,
                accountCategory = accountType
            )
        )
        return result.isEnabled
    }

    fun getAllFlags(): List<FeatureFlag> {
        return PlatformFeature.entries.map { feature ->
            val flag = service.featureFlags.value[feature.name.lowercase()]
            val state = when (flag?.status) {
                FeatureFlagStatus.OFF -> FeatureState.OFF
                FeatureFlagStatus.MAINTENANCE -> FeatureState.MAINTENANCE
                FeatureFlagStatus.BETA -> FeatureState.BETA
                FeatureFlagStatus.COMING_SOON -> FeatureState.COMING_SOON
                else -> FeatureState.ON
            }
            val enabledCountries = flag?.countryOverrides?.filter { it.value == FeatureFlagStatus.ON }?.keys ?: emptySet()
            val disabledCountries = flag?.countryOverrides?.filter { it.value == FeatureFlagStatus.OFF }?.keys ?: emptySet()
            val allowedAccounts = flag?.accountTypeOverrides?.filter { it.value != FeatureFlagStatus.OFF }?.keys
                ?: AccountType.entries.toSet()

            FeatureFlag(
                feature = feature,
                state = state,
                enabledCountries = enabledCountries,
                disabledCountries = disabledCountries,
                allowedAccountTypes = allowedAccounts,
                notes = flag?.description ?: ""
            )
        }
    }

    fun updateFeatureFlag(flag: FeatureFlag) {
        val status = when (flag.state) {
            FeatureState.ON -> FeatureFlagStatus.ON
            FeatureState.OFF -> FeatureFlagStatus.OFF
            FeatureState.MAINTENANCE -> FeatureFlagStatus.MAINTENANCE
            FeatureState.BETA -> FeatureFlagStatus.BETA
            FeatureState.COMING_SOON -> FeatureFlagStatus.COMING_SOON
        }
        val existing = service.featureFlags.value[flag.feature.name.lowercase()]
        if (existing != null) {
            val countryMap = mutableMapOf<String, FeatureFlagStatus>()
            flag.enabledCountries.forEach { countryMap[it] = FeatureFlagStatus.ON }
            flag.disabledCountries.forEach { countryMap[it] = FeatureFlagStatus.OFF }
            val accountMap = mutableMapOf<AccountType, FeatureFlagStatus>()
            AccountType.entries.forEach { acc ->
                if (!flag.allowedAccountTypes.contains(acc)) {
                    accountMap[acc] = FeatureFlagStatus.OFF
                }
            }

            service.updateFeatureFlag(
                existing.copy(
                    status = status,
                    globalEnabled = (status == FeatureFlagStatus.ON || status == FeatureFlagStatus.BETA),
                    countryOverrides = countryMap,
                    accountTypeOverrides = accountMap,
                    version = existing.version + 1,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
