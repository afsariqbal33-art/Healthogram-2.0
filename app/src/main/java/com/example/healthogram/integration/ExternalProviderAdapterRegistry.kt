package com.example.healthogram.integration

import com.example.healthogram.aistudio.AIProviderAdapter
import com.example.healthogram.aistudio.GeminiProviderAdapter
import com.example.healthogram.delivery.adapters.DeliveryProviderAdapter
import com.example.healthogram.delivery.adapters.InternalDeliveryAdapter
import com.example.healthogram.payments.gateways.LocalGatewayAdapter
import com.example.healthogram.payments.gateways.PaymentGatewayAdapter
import com.example.healthogram.payments.gateways.StripeGatewayAdapter
import com.example.healthogram.translation.GeminiTranslationProvider
import com.example.healthogram.translation.TranslationProviderAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * HEALTHOGRAM 2.3 — STEP 51: EXTERNAL PROVIDER ADAPTER REGISTRY
 * Implements Section 39, 40, 41, 78:
 * Dynamic provider registry with health monitoring, sandbox flags, explicit failover,
 * and strict 'REQUIRES EXTERNAL PROVIDER' status for unconnected production services.
 */
class ExternalProviderAdapterRegistry private constructor() {

    private val providerHealthMap = ConcurrentHashMap<String, ProviderHealthInfo>()
    private val _providerHealthFlow = MutableStateFlow<Map<String, ProviderHealthInfo>>(emptyMap())
    val providerHealthFlow: StateFlow<Map<String, ProviderHealthInfo>> = _providerHealthFlow.asStateFlow()

    // 1. Payment Adapters
    private val paymentAdapters = ConcurrentHashMap<String, PaymentGatewayAdapter>()

    // 2. Delivery Adapters
    private val deliveryAdapters = ConcurrentHashMap<String, DeliveryProviderAdapter>()

    // 3. AI Adapters
    private val aiAdapters = ConcurrentHashMap<String, AIProviderAdapter>()

    // 4. Translation Adapters
    private val translationAdapters = ConcurrentHashMap<String, TranslationProviderAdapter>()

    init {
        initializeStandardAdapters()
    }

    companion object {
        @Volatile
        private var instance: ExternalProviderAdapterRegistry? = null

        fun getInstance(): ExternalProviderAdapterRegistry {
            return instance ?: synchronized(this) {
                instance ?: ExternalProviderAdapterRegistry().also { instance = it }
            }
        }
    }

    private fun initializeStandardAdapters() {
        // --- PAYMENT ADAPTERS ---
        val stripeAdapter = StripeGatewayAdapter(isLiveMode = false)
        val madaAdapter = LocalGatewayAdapter()
        paymentAdapters[stripeAdapter.gatewayId] = stripeAdapter
        paymentAdapters[madaAdapter.gatewayId] = madaAdapter

        providerHealthMap["stripe"] = ProviderHealthInfo(
            providerId = "stripe",
            providerName = "Stripe Gateway (International & Cards)",
            serviceType = IntegrationServiceType.PAYMENT_GATEWAY,
            countryCode = "GLOBAL",
            status = ProviderConnectionStatus.SANDBOX_ACTIVE,
            isSandbox = true,
            latencyMs = 78L
        )

        providerHealthMap["local_mada"] = ProviderHealthInfo(
            providerId = "local_mada",
            providerName = "Mada Saudi Gateway (Local Scheme)",
            serviceType = IntegrationServiceType.PAYMENT_GATEWAY,
            countryCode = "SA",
            status = ProviderConnectionStatus.SANDBOX_ACTIVE,
            isSandbox = true,
            latencyMs = 42L
        )

        providerHealthMap["future_international_payment"] = ProviderHealthInfo(
            providerId = "future_international_payment",
            providerName = "Cross-Border Settlement Gateway",
            serviceType = IntegrationServiceType.PAYMENT_GATEWAY,
            countryCode = "GLOBAL",
            status = ProviderConnectionStatus.REQUIRES_EXTERNAL_PROVIDER,
            requiresExternalConfiguration = true,
            externalProviderNotes = "Requires live banking license & cross-border merchant credentials before activation."
        )

        // --- DELIVERY ADAPTERS ---
        val localDelivery = InternalDeliveryAdapter()
        deliveryAdapters[localDelivery.providerId] = localDelivery

        providerHealthMap["local_express"] = ProviderHealthInfo(
            providerId = "local_express",
            providerName = "Healthogram Express Fleet (Urban Logistics)",
            serviceType = IntegrationServiceType.DELIVERY_PROVIDER,
            countryCode = "SA",
            status = ProviderConnectionStatus.HEALTHY,
            isSandbox = true,
            latencyMs = 35L
        )

        providerHealthMap["aramex"] = ProviderHealthInfo(
            providerId = "aramex",
            providerName = "Aramex Logistics Adapter",
            serviceType = IntegrationServiceType.DELIVERY_PROVIDER,
            countryCode = "SA",
            status = ProviderConnectionStatus.SANDBOX_ACTIVE,
            isSandbox = true,
            latencyMs = 95L
        )

        providerHealthMap["dhl_express"] = ProviderHealthInfo(
            providerId = "dhl_express",
            providerName = "DHL Medical Express",
            serviceType = IntegrationServiceType.DELIVERY_PROVIDER,
            countryCode = "GLOBAL",
            status = ProviderConnectionStatus.REQUIRES_EXTERNAL_PROVIDER,
            requiresExternalConfiguration = true,
            externalProviderNotes = "Requires DHL Express Enterprise Web Services API key and corporate billing account."
        )

        // --- HEALTHCARE INTEROPERABILITY ADAPTERS ---
        providerHealthMap["hl7_fhir_r4"] = ProviderHealthInfo(
            providerId = "hl7_fhir_r4",
            providerName = "HL7 FHIR R4 Core Engine",
            serviceType = IntegrationServiceType.FHIR_ENDPOINT,
            countryCode = "GLOBAL",
            status = ProviderConnectionStatus.HEALTHY,
            isSandbox = true,
            latencyMs = 18L,
            externalProviderNotes = "FHIR R4 resource mapper and validator active with Patient-Scoped Consent."
        )

        providerHealthMap["smart_on_fhir_external"] = ProviderHealthInfo(
            providerId = "smart_on_fhir_external",
            providerName = "SMART on FHIR Hospital Gateway (Epic/Cerner)",
            serviceType = IntegrationServiceType.FHIR_ENDPOINT,
            countryCode = "GLOBAL",
            status = ProviderConnectionStatus.REQUIRES_EXTERNAL_PROVIDER,
            requiresExternalConfiguration = true,
            externalProviderNotes = "Requires Hospital EHR OAuth2 client credentials and client mutual TLS certificate."
        )

        providerHealthMap["health_connect"] = ProviderHealthInfo(
            providerId = "health_connect",
            providerName = "Android Health Connect Bridge (API 34+)",
            serviceType = IntegrationServiceType.HEALTH_CONNECT,
            countryCode = "DEVICE_LOCAL",
            status = ProviderConnectionStatus.HEALTHY,
            isSandbox = false,
            latencyMs = 12L,
            externalProviderNotes = "Local Android on-device Health Connect manager active with granular permissions."
        )

        // --- AI STUDIO ADAPTERS ---
        val geminiAi = GeminiProviderAdapter()
        aiAdapters[geminiAi.providerName] = geminiAi

        providerHealthMap["gemini_ai_studio"] = ProviderHealthInfo(
            providerId = "gemini_ai_studio",
            providerName = "Google Gemini 3.5 & 3.8 Flash Engine",
            serviceType = IntegrationServiceType.AI_STUDIO,
            countryCode = "GLOBAL",
            status = ProviderConnectionStatus.HEALTHY,
            isSandbox = true,
            latencyMs = 120L,
            externalProviderNotes = "Isolated to social/creator/seller workflows. Strict Zero-PHI barrier enforced."
        )

        // --- TRANSLATION ADAPTERS ---
        val geminiTranslation = GeminiTranslationProvider()
        translationAdapters[geminiTranslation.providerName] = geminiTranslation

        providerHealthMap["gemini_translation"] = ProviderHealthInfo(
            providerId = "gemini_translation",
            providerName = "Gemini Multilingual Neural Translation",
            serviceType = IntegrationServiceType.TRANSLATION,
            countryCode = "GLOBAL",
            status = ProviderConnectionStatus.HEALTHY,
            isSandbox = true,
            latencyMs = 85L,
            externalProviderNotes = "Supports 16 languages. Medical disclaimer displayed on healthcare content translations."
        )

        _providerHealthFlow.value = providerHealthMap.toMap()
    }

    fun getPaymentAdapter(gatewayId: String): PaymentGatewayAdapter? = paymentAdapters[gatewayId]

    fun getDeliveryAdapter(providerId: String): DeliveryProviderAdapter? = deliveryAdapters[providerId]

    fun recordHeartbeat(providerId: String, latencyMs: Long, isError: Boolean) {
        val current = providerHealthMap[providerId] ?: return
        val newErrorRate = if (isError) (current.errorRatePercent * 0.9 + 10.0) else (current.errorRatePercent * 0.9)
        val newStatus = when {
            newErrorRate > 25.0 -> ProviderConnectionStatus.DEGRADED
            current.status == ProviderConnectionStatus.REQUIRES_EXTERNAL_PROVIDER -> ProviderConnectionStatus.REQUIRES_EXTERNAL_PROVIDER
            current.isSandbox -> ProviderConnectionStatus.SANDBOX_ACTIVE
            else -> ProviderConnectionStatus.HEALTHY
        }
        providerHealthMap[providerId] = current.copy(
            lastHeartbeat = System.currentTimeMillis(),
            latencyMs = latencyMs,
            errorRatePercent = newErrorRate,
            status = newStatus
        )
        _providerHealthFlow.value = providerHealthMap.toMap()
    }
}
