package com.example.healthogram.performance

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * HEALTHOGRAM STEP 21: SECTION 71 APP VERSION PERFORMANCE & RETIREMENT CONTROL
 */
data class AppVersionConfig(
    val platform: String = "android",
    val minimumVersionCode: Int = 100, // E.g. v1.0.0
    val recommendedVersionCode: Int = 110, // E.g. v1.1.0
    val latestVersionCode: Int = 120, // E.g. v1.2.0
    val minimumVersionName: String = "1.0.0",
    val latestVersionName: String = "1.2.0",
    val forceUpdate: Boolean = false,
    val maintenanceMode: Boolean = false,
    val releaseNotes: String = "Performance optimizations and critical security updates."
)

enum class VersionVerificationStatus {
    UP_TO_DATE,
    RECOMMENDED_UPDATE,
    FORCE_UPDATE_REQUIRED,
    MAINTENANCE_MODE
}

class AppVersionConfigService private constructor() {

    private val _config = MutableStateFlow(AppVersionConfig())
    val config: StateFlow<AppVersionConfig> = _config.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: AppVersionConfigService? = null

        fun getInstance(): AppVersionConfigService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppVersionConfigService().also { INSTANCE = it }
            }
        }
    }

    /**
     * Verify running client version against sovereign owner rules
     */
    fun verifyClientVersion(currentVersionCode: Int): VersionVerificationStatus {
        val currentConfig = _config.value
        if (currentConfig.maintenanceMode) {
            return VersionVerificationStatus.MAINTENANCE_MODE
        }
        if (currentConfig.forceUpdate || currentVersionCode < currentConfig.minimumVersionCode) {
            return VersionVerificationStatus.FORCE_UPDATE_REQUIRED
        }
        if (currentVersionCode < currentConfig.recommendedVersionCode) {
            return VersionVerificationStatus.RECOMMENDED_UPDATE
        }
        return VersionVerificationStatus.UP_TO_DATE
    }

    fun updateConfig(newConfig: AppVersionConfig) {
        _config.value = newConfig
    }

    fun setMaintenanceMode(enabled: Boolean) {
        _config.value = _config.value.copy(maintenanceMode = enabled)
    }

    fun setForceUpdate(enabled: Boolean, minimumCode: Int = _config.value.minimumVersionCode) {
        _config.value = _config.value.copy(
            forceUpdate = enabled,
            minimumVersionCode = minimumCode
        )
    }

    fun resetForTesting() {
        _config.value = AppVersionConfig()
    }
}
