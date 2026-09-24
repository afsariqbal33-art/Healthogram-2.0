package com.example.healthogram.core.foundation.env

/**
 * Healthogram 2.3 Environment Separation & Configuration Architecture.
 *
 * Enforces strict boundaries between DEVELOPMENT, STAGING, and PRODUCTION.
 * Production credentials, Firebase projects, and storage buckets must remain isolated.
 */
enum class AppEnvironment(val identifier: String) {
    DEVELOPMENT("dev"),
    STAGING("staging"),
    PRODUCTION("prod");

    val isProduction: Boolean get() = this == PRODUCTION
}

data class EnvironmentConfig(
    val environment: AppEnvironment,
    val firebaseProjectId: String,
    val firestoreDatabaseId: String,
    val storageBucket: String,
    val apiBaseUrl: String,
    val isAppCheckEnforced: Boolean,
    val isPlayIntegrityEnforced: Boolean,
    val isMockDataAllowed: Boolean,
    val isLoggingVerbose: Boolean,
    val cmekKmsKeyUri: String? = null
) {
    init {
        // Enforce production security constraints
        if (environment.isProduction) {
            require(isAppCheckEnforced) { "Security Violation: App Check MUST be enforced in PRODUCTION." }
            require(isPlayIntegrityEnforced) { "Security Violation: Play Integrity MUST be enforced in PRODUCTION." }
            require(!isMockDataAllowed) { "Security Violation: Mock data is strictly FORBIDDEN in PRODUCTION." }
            require(!isLoggingVerbose) { "Security Violation: Verbose logging must be disabled in PRODUCTION to prevent PHI leakage." }
        }
    }

    companion object {
        fun createDevelopment(): EnvironmentConfig = EnvironmentConfig(
            environment = AppEnvironment.DEVELOPMENT,
            firebaseProjectId = "healthogram-dev-sandbox",
            firestoreDatabaseId = "(default)",
            storageBucket = "healthogram-dev-assets.appspot.com",
            apiBaseUrl = "https://dev-api.healthogram.internal/api/v2",
            isAppCheckEnforced = false,
            isPlayIntegrityEnforced = false,
            isMockDataAllowed = true,
            isLoggingVerbose = true
        )

        fun createStaging(): EnvironmentConfig = EnvironmentConfig(
            environment = AppEnvironment.STAGING,
            firebaseProjectId = "healthogram-staging-verify",
            firestoreDatabaseId = "(default)",
            storageBucket = "healthogram-staging-assets.appspot.com",
            apiBaseUrl = "https://staging-api.healthogram.internal/api/v2",
            isAppCheckEnforced = true,
            isPlayIntegrityEnforced = true,
            isMockDataAllowed = false,
            isLoggingVerbose = true
        )

        fun createProduction(): EnvironmentConfig = EnvironmentConfig(
            environment = AppEnvironment.PRODUCTION,
            firebaseProjectId = "healthogram-prod-secure",
            firestoreDatabaseId = "(default)",
            storageBucket = "healthogram-prod-cmek-vault.appspot.com",
            apiBaseUrl = "https://api.healthogram.com/api/v2",
            isAppCheckEnforced = true,
            isPlayIntegrityEnforced = true,
            isMockDataAllowed = false,
            isLoggingVerbose = false,
            cmekKmsKeyUri = "projects/healthogram-prod-secure/locations/global/keyRings/hgm-cmek-ring/cryptoKeys/hgm-vault-key"
        )
    }
}

/**
 * Global Environment Manager singleton.
 */
object EnvironmentManager {
    @Volatile
    private var currentConfig: EnvironmentConfig = EnvironmentConfig.createProduction()

    fun initialize(config: EnvironmentConfig) {
        currentConfig = config
    }

    fun get(): EnvironmentConfig = currentConfig

    fun isProduction(): Boolean = currentConfig.environment.isProduction
}
