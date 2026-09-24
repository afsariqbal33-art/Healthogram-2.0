package com.example.healthogram.core.foundation.flags

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.MarketplaceRole
import com.example.healthogram.core.foundation.audit.AuditEvent
import com.example.healthogram.core.foundation.audit.AuditLoggingService
import com.example.healthogram.core.foundation.audit.AuditResult
import com.example.healthogram.core.foundation.env.AppEnvironment
import com.example.healthogram.core.foundation.env.EnvironmentManager
import java.util.concurrent.ConcurrentHashMap

/**
 * Step 49: Healthogram 2.3 Centralized Feature Flag & Dynamic Governance Architecture.
 */
enum class FlagState {
    OFF,
    BETA,
    ON,
    MAINTENANCE,
    COMING_SOON
}

data class FeatureFlag23(
    val key: String,
    var state: FlagState = FlagState.OFF,
    val targetCountries: Set<String> = emptySet(),
    val targetAccountTypes: Set<AccountType> = emptySet(),
    val targetMarketplaceRoles: Set<MarketplaceRole> = emptySet(),
    val allowedUserUids: Set<String> = emptySet(),
    val allowedOrgUids: Set<String> = emptySet(),
    val targetEnvironments: Set<AppEnvironment> = setOf(AppEnvironment.DEVELOPMENT, AppEnvironment.STAGING, AppEnvironment.PRODUCTION),
    val isKillSwitchActive: Boolean = false,
    val description: String = ""
) {
    fun isAccessible(
        countryCode: String? = null,
        accountType: AccountType? = null,
        marketplaceRole: MarketplaceRole? = null,
        userUid: String? = null,
        orgUid: String? = null,
        env: AppEnvironment = EnvironmentManager.get().environment
    ): Boolean {
        if (isKillSwitchActive) return false
        if (!targetEnvironments.contains(env)) return false

        return when (state) {
            FlagState.OFF -> false
            FlagState.MAINTENANCE -> false
            FlagState.COMING_SOON -> false
            FlagState.ON -> {
                val countryMatch = targetCountries.isEmpty() || (countryCode != null && targetCountries.contains(countryCode.uppercase()))
                val accountMatch = targetAccountTypes.isEmpty() || (accountType != null && targetAccountTypes.contains(accountType))
                val roleMatch = targetMarketplaceRoles.isEmpty() || (marketplaceRole != null && targetMarketplaceRoles.contains(marketplaceRole))
                countryMatch && accountMatch && roleMatch
            }
            FlagState.BETA -> {
                val userMatch = userUid != null && allowedUserUids.contains(userUid)
                val orgMatch = orgUid != null && allowedOrgUids.contains(orgUid)
                userMatch || orgMatch
            }
        }
    }
}

class FeatureFlag23Registry private constructor() {
    private val flags = ConcurrentHashMap<String, FeatureFlag23>()

    companion object {
        const val FLAG_HEALTHOGRAM_23_ENABLED = "healthogram_23_enabled"
        const val FLAG_HEALTH_PASSPORT_23_ENABLED = "health_passport_23_enabled"
        const val FLAG_HEALTH_PASSPORT_CONSENT_V2 = "health_passport_consent_v2"
        const val FLAG_FHIR_V2 = "fhir_v2"
        const val FLAG_HEALTH_CONNECT_V2 = "health_connect_v2"
        const val FLAG_APPOINTMENTS_V2 = "appointments_v2"
        const val FLAG_MARKETPLACE_V2 = "marketplace_v2"
        const val FLAG_PAYMENTS_V2 = "payments_v2"
        const val FLAG_DELIVERY_V2 = "delivery_v2"
        const val FLAG_SOCIAL_V2 = "social_v2"
        const val FLAG_MESSAGING_V2 = "messaging_v2"
        const val FLAG_CALLING_V2 = "calling_v2"
        const val FLAG_TRANSLATION_V2 = "translation_v2"
        const val FLAG_AI_STUDIO_V2 = "ai_studio_v2"
        const val FLAG_NOTIFICATIONS_V2 = "notifications_v2"
        const val FLAG_ADMIN_V2 = "admin_v2"
        const val FLAG_OWNER_V2 = "owner_v2"

        val instance: FeatureFlag23Registry by lazy { FeatureFlag23Registry() }
    }

    init {
        // Register all standard 2.3 flags with default safe states
        register(FeatureFlag23(FLAG_HEALTHOGRAM_23_ENABLED, FlagState.ON, description = "Master switch for 2.3 foundation"))
        register(FeatureFlag23(FLAG_HEALTH_PASSPORT_23_ENABLED, FlagState.ON, description = "Health Passport 2.3 enhancements"))
        register(FeatureFlag23(FLAG_HEALTH_PASSPORT_CONSENT_V2, FlagState.ON, description = "Granular clinical consent presets"))
        register(FeatureFlag23(FLAG_FHIR_V2, FlagState.BETA, targetAccountTypes = setOf(AccountType.INDIVIDUAL, AccountType.DOCTOR), description = "HL7 FHIR R4 clinical export"))
        register(FeatureFlag23(FLAG_HEALTH_CONNECT_V2, FlagState.BETA, targetAccountTypes = setOf(AccountType.INDIVIDUAL), description = "Android Health Connect sync"))
        register(FeatureFlag23(FLAG_APPOINTMENTS_V2, FlagState.BETA, targetCountries = setOf("US", "SA", "AE", "OM"), description = "Multi-specialty appointment booking"))
        register(FeatureFlag23(FLAG_MARKETPLACE_V2, FlagState.ON, targetMarketplaceRoles = setOf(MarketplaceRole.CUSTOMER, MarketplaceRole.SELLER), description = "Healthcare domestic marketplace"))
        register(FeatureFlag23(FLAG_PAYMENTS_V2, FlagState.ON, description = "Unified payments adapter"))
        register(FeatureFlag23(FLAG_DELIVERY_V2, FlagState.ON, description = "Cold chain logistics and OTP handover"))
        register(FeatureFlag23(FLAG_SOCIAL_V2, FlagState.ON, description = "Unified + creator tray and social feed"))
        register(FeatureFlag23(FLAG_MESSAGING_V2, FlagState.ON, description = "Signal protocol E2EE direct chat"))
        register(FeatureFlag23(FLAG_CALLING_V2, FlagState.ON, description = "WebRTC telehealth audio/video calling"))
        register(FeatureFlag23(FLAG_TRANSLATION_V2, FlagState.ON, description = "Side-by-side bilingual medical safety"))
        register(FeatureFlag23(FLAG_AI_STUDIO_V2, FlagState.ON, description = "Contextual creator tools with zero PHI"))
        register(FeatureFlag23(FLAG_NOTIFICATIONS_V2, FlagState.ON, description = "Privacy-preserving notification channels"))
        register(FeatureFlag23(FLAG_ADMIN_V2, FlagState.ON, description = "Admin control panel v2"))
        register(FeatureFlag23(FLAG_OWNER_V2, FlagState.ON, description = "Owner sovereign control panel and kill switches"))
    }

    fun register(flag: FeatureFlag23) {
        flags[flag.key] = flag
    }

    fun getFlag(key: String): FeatureFlag23? = flags[key]

    fun isEnabled(
        key: String,
        countryCode: String? = null,
        accountType: AccountType? = null,
        marketplaceRole: MarketplaceRole? = null,
        userUid: String? = null,
        orgUid: String? = null
    ): Boolean {
        val flag = flags[key] ?: return false
        return flag.isAccessible(countryCode, accountType, marketplaceRole, userUid, orgUid)
    }

    fun updateFlagState(
        key: String,
        newState: FlagState,
        changedByActorId: String,
        actorType: String,
        reason: String
    ): Boolean {
        // Enforce RBAC: Only Admin or Owner may update feature flags
        require(actorType == "ADMIN" || actorType == "OWNER") {
            "Security Violation: Only Admin or Owner may alter feature flag configurations."
        }

        val flag = flags[key] ?: return false
        val oldState = flag.state
        flag.state = newState

        // Audit the change
        AuditLoggingService.instance.logEvent(
            AuditEvent(
                actorId = changedByActorId,
                actorType = actorType,
                targetType = "FeatureFlag",
                targetId = key,
                action = "UPDATE_FLAG_STATE",
                result = AuditResult.SUCCESS,
                reason = "Changed from $oldState to $newState. Reason: $reason"
            )
        )
        return true
    }

    fun tripKillSwitch(key: String, changedByActorId: String, actorType: String, reason: String): Boolean {
        require(actorType == "OWNER" || actorType == "ADMIN") { "Security Violation: Kill switches require Admin/Owner authorization." }
        val flag = flags[key] ?: return false
        flags[key] = flag.copy(isKillSwitchActive = true)

        AuditLoggingService.instance.logEvent(
            AuditEvent(
                actorId = changedByActorId,
                actorType = actorType,
                targetType = "FeatureFlag",
                targetId = key,
                action = "KILL_SWITCH_TRIPPED",
                result = AuditResult.SUCCESS,
                reason = reason
            )
        )
        return true
    }
}
