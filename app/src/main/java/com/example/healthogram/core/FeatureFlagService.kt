package com.example.healthogram.core

import java.util.concurrent.ConcurrentHashMap

/**
 * Step 32: FeatureFlagService & Controlled Clinical Rollout Engine
 *
 * Enforces controlled phased rollouts:
 * Clinical features never jump to 100% unconstrained global activation.
 * Supports:
 * - rollout percentage (0-100)
 * - allowed countries
 * - allowed account types
 * - minimum app version
 * - beta user list
 * - emergency kill switch
 * - read-only maintenance mode
 */
data class FeatureFlag(
    val key: String,
    val enabled: Boolean,
    val rolloutPercentage: Int = 0, // 0 to 100
    val allowedCountries: Set<String> = emptySet(),
    val allowedAccountTypes: Set<AccountType> = emptySet(),
    val minAppVersion: Int = 1,
    val betaUserUids: Set<String> = emptySet(),
    val killSwitchActive: Boolean = false,
    val readOnlyMaintenanceMode: Boolean = false,
    val rolloutPhase: String = "PHASE_1_INTERNAL_QA" // PHASE_1_INTERNAL_QA, PHASE_2_ALPHA, PHASE_3_PILOT_COUNTRY, PHASE_4_REGIONAL, PHASE_5_GA
)

class FeatureFlagService private constructor() {

    private val flags = ConcurrentHashMap<String, FeatureFlag>()

    companion object {
        @Volatile
        private var instance: FeatureFlagService? = null

        fun getInstance(): FeatureFlagService {
            return instance ?: synchronized(this) {
                instance ?: FeatureFlagService().also { instance = it }
            }
        }

        // Standard Step 32 & Step 34 Feature Keys
        const val FLAG_HEALTH_PASSPORT_V2 = "health_passport_v2_enabled"
        const val FLAG_HEALTH_PASSPORT_2_1 = "health_passport_2_1_enabled"
        const val FLAG_HEALTH_CONNECT = "health_connect_enabled"
        const val FLAG_FHIR_EXPORT = "fhir_export_enabled"
        const val FLAG_FHIR_IMPORT = "fhir_import_enabled"
        const val FLAG_HEALTHCARE_INTEGRATION = "healthcare_integration_enabled"
        const val FLAG_PAPER_PRESCRIPTION_OCR = "paper_prescription_ocr_enabled"
        const val FLAG_CREATOR_MONETIZATION = "creator_monetization_enabled"
        const val FLAG_SELLER_MONETIZATION = "seller_monetization_enabled"
        const val FLAG_ORGANIZATION_MONETIZATION = "organization_monetization_enabled"
        const val FLAG_ORGANIZATION_PLANS = "organization_plans_enabled"
        const val FLAG_EMERGENCY_CARD = "emergency_card_enabled"
        const val FLAG_EMERGENCY_HEALTH_CARD = "emergency_health_card_enabled"
        const val FLAG_APPOINTMENT_SYSTEM_V2 = "appointment_system_v2_enabled"
        const val FLAG_APPOINTMENTS_2_1 = "appointments_2_1_enabled"
        const val FLAG_HEALTHCARE_AI_ASSISTANCE = "healthcare_ai_assistance_enabled"
        const val FLAG_HEALTHCARE_AI = "healthcare_ai_enabled"
        const val FLAG_HEALTH_DATA_PORTABILITY = "health_data_portability_enabled"
        const val FLAG_MULTILINGUAL_ARABIC = "multilingual_arabic_enabled"
        const val FLAG_ARABIC_RTL = "arabic_rtl_enabled"
        const val FLAG_INTERNATIONAL_MARKETPLACE = "international_marketplace_enabled"

        // Healthogram 2.2 Feature Flags (Step 37)
        const val FLAG_HEALTH_PASSPORT_2_2 = "health_passport_2_2_enabled"
        const val FLAG_FHIR_2_2 = "fhir_2_2_enabled"
        const val FLAG_HEALTH_CONNECT_2_2 = "health_connect_2_2_enabled"
        const val FLAG_APPOINTMENTS_2_2 = "appointments_2_2_enabled"
        const val FLAG_MARKETPLACE_2_2 = "marketplace_2_2_enabled"
        const val FLAG_AI_2_2 = "ai_2_2_enabled"
        const val FLAG_TRANSLATION_2_2 = "translation_2_2_enabled"

        // Step 34 Emergency Kill Switches (Server & Owner Controlled)
        const val KILL_SWITCH_HEALTH_CONNECT = "disable_health_connect"
        const val KILL_SWITCH_FHIR_IMPORT = "disable_fhir_import"
        const val KILL_SWITCH_FHIR_EXPORT = "disable_fhir_export"
        const val KILL_SWITCH_HEALTHCARE_INTEGRATIONS = "disable_healthcare_integrations"
        const val KILL_SWITCH_APPOINTMENTS = "disable_appointments"
        const val KILL_SWITCH_HEALTHCARE_AI = "disable_healthcare_ai"
        const val KILL_SWITCH_HEALTH_DATA_EXPORTS = "disable_health_data_exports"
        const val KILL_SWITCH_EMERGENCY_HEALTH_CARD = "disable_emergency_health_card"
    }

    init {
        // Initialize default safe feature flags for Healthogram 2.1
        registerFlag(
            FeatureFlag(
                key = FLAG_HEALTH_PASSPORT_V2,
                enabled = true,
                rolloutPercentage = 100,
                allowedCountries = setOf("OM", "SA", "AE", "US"),
                rolloutPhase = "PHASE_5_GA"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_HEALTH_CONNECT,
                enabled = true,
                rolloutPercentage = 50,
                allowedCountries = setOf("OM", "SA", "AE", "US"),
                rolloutPhase = "PHASE_3_PILOT_COUNTRY"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_FHIR_EXPORT,
                enabled = true,
                rolloutPercentage = 100,
                allowedCountries = setOf("OM", "SA", "AE", "US"),
                rolloutPhase = "PHASE_5_GA"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_FHIR_IMPORT,
                enabled = true,
                rolloutPercentage = 30,
                allowedCountries = setOf("OM", "SA"),
                allowedAccountTypes = setOf(AccountType.INDIVIDUAL, AccountType.DOCTOR),
                rolloutPhase = "PHASE_2_ALPHA"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_PAPER_PRESCRIPTION_OCR,
                enabled = true,
                rolloutPercentage = 40,
                allowedCountries = setOf("OM", "SA", "AE"),
                rolloutPhase = "PHASE_3_PILOT_COUNTRY"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_CREATOR_MONETIZATION,
                enabled = true,
                rolloutPercentage = 100,
                allowedCountries = setOf("OM", "SA", "AE", "US"),
                rolloutPhase = "PHASE_5_GA"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_SELLER_MONETIZATION,
                enabled = true,
                rolloutPercentage = 100,
                allowedCountries = setOf("OM", "SA", "AE", "US"),
                rolloutPhase = "PHASE_5_GA"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_ORGANIZATION_MONETIZATION,
                enabled = true,
                rolloutPercentage = 25,
                allowedCountries = setOf("OM"),
                allowedAccountTypes = setOf(AccountType.CLINIC, AccountType.HOSPITAL, AccountType.LABORATORY),
                rolloutPhase = "PHASE_2_ALPHA"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_EMERGENCY_CARD,
                enabled = true,
                rolloutPercentage = 100,
                allowedCountries = setOf("OM", "SA", "AE", "US"),
                rolloutPhase = "PHASE_5_GA"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_APPOINTMENT_SYSTEM_V2,
                enabled = true,
                rolloutPercentage = 100,
                allowedCountries = setOf("OM", "SA", "AE", "US"),
                rolloutPhase = "PHASE_5_GA"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_HEALTHCARE_AI_ASSISTANCE,
                enabled = true,
                rolloutPercentage = 50,
                allowedCountries = setOf("OM", "SA", "AE"),
                rolloutPhase = "PHASE_3_PILOT_COUNTRY"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_MULTILINGUAL_ARABIC,
                enabled = true,
                rolloutPercentage = 100,
                allowedCountries = setOf("OM", "SA", "AE", "US"),
                rolloutPhase = "PHASE_5_GA"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_INTERNATIONAL_MARKETPLACE,
                enabled = false,
                rolloutPercentage = 0,
                allowedCountries = setOf("OM"),
                rolloutPhase = "PHASE_0_INTERNAL"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_HEALTH_PASSPORT_2_1,
                enabled = true,
                rolloutPercentage = 100,
                allowedCountries = setOf("OM", "SA", "AE", "US"),
                rolloutPhase = "PHASE_5_GA"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_HEALTHCARE_INTEGRATION,
                enabled = true,
                rolloutPercentage = 30,
                allowedCountries = setOf("OM", "SA"),
                allowedAccountTypes = setOf(AccountType.CLINIC, AccountType.HOSPITAL, AccountType.LABORATORY),
                rolloutPhase = "PHASE_3_PILOT_COUNTRY"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_ORGANIZATION_PLANS,
                enabled = true,
                rolloutPercentage = 50,
                allowedCountries = setOf("OM", "SA", "AE"),
                allowedAccountTypes = setOf(AccountType.CLINIC, AccountType.HOSPITAL, AccountType.LABORATORY),
                rolloutPhase = "PHASE_3_PILOT_COUNTRY"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_EMERGENCY_HEALTH_CARD,
                enabled = true,
                rolloutPercentage = 100,
                allowedCountries = setOf("OM", "SA", "AE", "US"),
                rolloutPhase = "PHASE_5_GA"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_APPOINTMENTS_2_1,
                enabled = true,
                rolloutPercentage = 100,
                allowedCountries = setOf("OM", "SA", "AE", "US"),
                rolloutPhase = "PHASE_5_GA"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_HEALTHCARE_AI,
                enabled = true,
                rolloutPercentage = 50,
                allowedCountries = setOf("OM", "SA", "AE"),
                rolloutPhase = "PHASE_3_PILOT_COUNTRY"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_HEALTH_DATA_PORTABILITY,
                enabled = true,
                rolloutPercentage = 100,
                allowedCountries = setOf("OM", "SA", "AE", "US"),
                rolloutPhase = "PHASE_5_GA"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_ARABIC_RTL,
                enabled = true,
                rolloutPercentage = 100,
                allowedCountries = setOf("OM", "SA", "AE", "US"),
                rolloutPhase = "PHASE_5_GA"
            )
        )

        // Healthogram 2.2 Controlled Feature Flag Registrations (Disabled by default, canary staged)
        registerFlag(
            FeatureFlag(
                key = FLAG_HEALTH_PASSPORT_2_2,
                enabled = false,
                rolloutPercentage = 10,
                allowedCountries = setOf("OM", "SA"),
                rolloutPhase = "PHASE_2_ALPHA"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_FHIR_2_2,
                enabled = false,
                rolloutPercentage = 10,
                allowedCountries = setOf("OM", "SA"),
                allowedAccountTypes = setOf(AccountType.CLINIC, AccountType.HOSPITAL, AccountType.LABORATORY),
                rolloutPhase = "PHASE_2_ALPHA"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_HEALTH_CONNECT_2_2,
                enabled = false,
                rolloutPercentage = 15,
                allowedCountries = setOf("OM", "SA", "AE"),
                rolloutPhase = "PHASE_2_ALPHA"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_APPOINTMENTS_2_2,
                enabled = false,
                rolloutPercentage = 10,
                allowedCountries = setOf("OM", "SA"),
                rolloutPhase = "PHASE_2_ALPHA"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_MARKETPLACE_2_2,
                enabled = false,
                rolloutPercentage = 10,
                allowedCountries = setOf("OM", "SA"),
                rolloutPhase = "PHASE_2_ALPHA"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_AI_2_2,
                enabled = false,
                rolloutPercentage = 10,
                allowedCountries = setOf("OM", "SA", "AE"),
                rolloutPhase = "PHASE_2_ALPHA"
            )
        )
        registerFlag(
            FeatureFlag(
                key = FLAG_TRANSLATION_2_2,
                enabled = false,
                rolloutPercentage = 15,
                allowedCountries = setOf("OM", "SA", "AE", "US"),
                rolloutPhase = "PHASE_2_ALPHA"
            )
        )
    }

    fun registerFlag(flag: FeatureFlag) {
        flags[flag.key] = flag
    }

    fun getFlag(key: String): FeatureFlag? = flags[key]

    /**
     * Evaluates whether a feature is active for the specific user context
     */
    fun isFeatureEnabled(
        key: String,
        userUid: String,
        countryCode: String,
        accountType: AccountType,
        appVersion: Int = 1
    ): Boolean {
        val flag = flags[key] ?: return false

        // 1. Kill Switch Check
        if (flag.killSwitchActive || !flag.enabled) {
            return false
        }

        // 2. Minimum Version Check
        if (appVersion < flag.minAppVersion) {
            return false
        }

        // 3. Country Check
        if (flag.allowedCountries.isNotEmpty() && !flag.allowedCountries.contains(countryCode.uppercase())) {
            return false
        }

        // 4. Account Type Check
        if (flag.allowedAccountTypes.isNotEmpty() && !flag.allowedAccountTypes.contains(accountType)) {
            return false
        }

        // 5. Beta Whitelist Bypass
        if (flag.betaUserUids.contains(userUid)) {
            return true
        }

        // 6. Rollout Percentage Calculation (Deterministic hash of uid + key)
        if (flag.rolloutPercentage <= 0) return false
        if (flag.rolloutPercentage >= 100) return true

        val hashVal = Math.abs((userUid + key).hashCode()) % 100
        return hashVal < flag.rolloutPercentage
    }

    fun isReadOnlyMaintenance(key: String): Boolean {
        return flags[key]?.readOnlyMaintenanceMode ?: false
    }

    fun isKillSwitchActive(key: String): Boolean {
        return flags[key]?.killSwitchActive ?: false
    }

    fun activateKillSwitch(key: String): Boolean {
        val existing = flags[key] ?: return false
        flags[key] = existing.copy(killSwitchActive = true)
        return true
    }

    fun deactivateKillSwitch(key: String): Boolean {
        val existing = flags[key] ?: return false
        flags[key] = existing.copy(killSwitchActive = false)
        return true
    }

    fun setRolloutPercentage(key: String, percentage: Int): Boolean {
        require(percentage in 0..100) { "Percentage must be between 0 and 100" }
        val existing = flags[key] ?: return false
        flags[key] = existing.copy(rolloutPercentage = percentage)
        return true
    }
}
