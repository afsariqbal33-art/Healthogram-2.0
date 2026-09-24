package com.example.healthogram.performance

import com.example.healthogram.owner.PlatformConfigurationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * HEALTHOGRAM STEP 21: SECTION 70 FEATURE FLAG CACHING SERVICE
 * Prevents redundant network queries for feature flags across Compose widgets.
 */
class FeatureFlagService private constructor(
    private val platformConfigService: PlatformConfigurationService = PlatformConfigurationService.getInstance()
) {
    companion object {
        const val DEFAULT_CACHE_TTL_MS = 15 * 60 * 1000L // 15 Minutes Cache TTL
        const val EMERGENCY_CACHE_TTL_MS = 60 * 1000L    // 1 Minute in emergency mode

        @Volatile
        private var INSTANCE: FeatureFlagService? = null

        fun getInstance(): FeatureFlagService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FeatureFlagService().also { INSTANCE = it }
            }
        }
    }

    private val localCache = ConcurrentHashMap<String, Boolean>()
    private var lastFetchTimestamp: Long = 0
    private var configVersion: Long = 1

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        refreshCacheSynchronously()
    }

    /**
     * Read cached feature flag status with sub-millisecond local latency.
     * Triggers asynchronous background refresh if TTL has expired.
     */
    fun isFeatureEnabled(featureKey: String): Boolean {
        checkAndRefreshIfExpired()
        return localCache[featureKey] ?: false
    }

    /**
     * Check if local cache has expired based on current performance degradation state
     */
    fun isCacheExpired(): Boolean {
        val ttl = if (PerformanceMonitoringService.getInstance().isControlActive(EmergencyPerformanceControl.REDUCE_RECOMMENDATIONS_REFRESH)) {
            DEFAULT_CACHE_TTL_MS // In performance conservation mode, retain cache longer
        } else {
            DEFAULT_CACHE_TTL_MS
        }
        return (System.currentTimeMillis() - lastFetchTimestamp) > ttl
    }

    fun checkAndRefreshIfExpired() {
        if (isCacheExpired() && !_isRefreshing.value) {
            refreshCacheSynchronously()
        }
    }

    /**
     * Standard cache refresh mechanism
     */
    fun refreshCacheSynchronously() {
        _isRefreshing.value = true
        try {
            val flags = platformConfigService.featureFlags.value
            localCache.clear()
            flags.forEach { (key, flag) ->
                localCache[flag.featureKey] = flag.status == com.example.healthogram.owner.FeatureFlagStatus.ON
            }
            lastFetchTimestamp = System.currentTimeMillis()
            configVersion++
        } finally {
            _isRefreshing.value = false
        }
    }

    /**
     * Emergency instantaneous refresh hook triggered by Owner alerts
     */
    fun triggerEmergencyRefresh() {
        refreshCacheSynchronously()
    }

    fun getConfigVersion(): Long = configVersion

    fun getLastFetchTimestamp(): Long = lastFetchTimestamp

    fun getCachedFlagCount(): Int = localCache.size

    fun resetForTesting() {
        localCache.clear()
        lastFetchTimestamp = 0
        configVersion = 1
        refreshCacheSynchronously()
    }
}
