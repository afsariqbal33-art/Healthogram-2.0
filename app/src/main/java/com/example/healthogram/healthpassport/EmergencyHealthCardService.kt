package com.example.healthogram.healthpassport

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class EmergencyContact(
    val name: String,
    val relationship: String,
    val phoneNumber: String
)

/**
 * Step 32: EmergencyHealthCardService
 *
 * Provides opt-in, minimal, revocable, and auditable emergency card profiles.
 * Invariant: Never exposes complete clinical health passport via emergency QR payloads.
 */
class EmergencyHealthCardService private constructor() {

    data class EmergencyCardConfig(
        val patientUid: String,
        val isOptedIn: Boolean = false,
        val emergencyContactName: String = "",
        val emergencyContactPhone: String = "",
        val emergencyContactRelation: String = "",
        val includeBloodGroup: Boolean = true,
        val bloodGroup: String? = null,
        val criticalAllergies: List<String> = emptyList(),
        val criticalMedications: List<String> = emptyList(),
        val importantConditions: List<String> = emptyList(),
        val emergencyInstructions: String = "",
        val qrSessionToken: String = UUID.randomUUID().toString(),
        val lastUpdatedAt: Long = System.currentTimeMillis()
    )

    data class EmergencyQRPayload(
        val type: String = "HEALTHOGRAM_EMERGENCY_CARD_V1",
        val emergencyToken: String,
        val emergencyContact: String,
        val bloodGroup: String?,
        val criticalAllergies: List<String>,
        val criticalMedications: List<String>,
        val importantConditions: List<String>,
        val emergencyInstructions: String,
        val disclaimer: String = "For Emergency Use Only. Non-comprehensive clinical summary."
    )

    data class EmergencyAccessAudit(
        val auditId: String = UUID.randomUUID().toString(),
        val patientUid: String,
        val accessedAt: Long = System.currentTimeMillis(),
        val ipAddress: String? = null,
        val userAgent: String? = null,
        val action: String = "QR_EMERGENCY_VIEW"
    )

    private val cardConfigs = ConcurrentHashMap<String, EmergencyCardConfig>()
    private val emergencyAccessLogs = ConcurrentHashMap<String, MutableList<EmergencyAccessAudit>>()

    companion object {
        @Volatile
        private var instance: EmergencyHealthCardService? = null

        fun getInstance(): EmergencyHealthCardService {
            return instance ?: synchronized(this) {
                instance ?: EmergencyHealthCardService().also { instance = it }
            }
        }
    }

    /**
     * Opt-in and configure minimal emergency profile
     */
    fun configureCard(config: EmergencyCardConfig): EmergencyCardConfig {
        val updated = config.copy(
            qrSessionToken = UUID.randomUUID().toString(),
            lastUpdatedAt = System.currentTimeMillis()
        )
        cardConfigs[config.patientUid] = updated
        return updated
    }

    fun updateCard(
        patientUid: String,
        bloodGroup: String?,
        emergencyContacts: List<EmergencyContact>,
        criticalAllergies: List<String>,
        lifeSavingMedications: List<String>,
        emergencyInstructions: String,
        isOptedIn: Boolean = true
    ): EmergencyCardConfig {
        val primary = emergencyContacts.firstOrNull()
        val config = EmergencyCardConfig(
            patientUid = patientUid,
            isOptedIn = isOptedIn,
            emergencyContactName = primary?.name ?: "",
            emergencyContactPhone = primary?.phoneNumber ?: "",
            emergencyContactRelation = primary?.relationship ?: "",
            bloodGroup = bloodGroup,
            criticalAllergies = criticalAllergies,
            criticalMedications = lifeSavingMedications,
            emergencyInstructions = emergencyInstructions
        )
        return configureCard(config)
    }

    fun getPublicEmergencyPayload(patientUid: String): EmergencyQRPayload? = buildEmergencyPayload(patientUid)

    /**
     * Revoke emergency card and invalidate existing QR tokens immediately
     */
    fun revokeCard(patientUid: String): Boolean {
        val existing = cardConfigs[patientUid] ?: return false
        cardConfigs[patientUid] = existing.copy(
            isOptedIn = false,
            qrSessionToken = UUID.randomUUID().toString(),
            lastUpdatedAt = System.currentTimeMillis()
        )
        return true
    }

    /**
     * Build minimal emergency QR payload. Does NOT expose full clinical history.
     */
    fun buildEmergencyPayload(patientUid: String): EmergencyQRPayload? {
        val config = cardConfigs[patientUid] ?: return null
        if (!config.isOptedIn) return null

        return EmergencyQRPayload(
            emergencyToken = config.qrSessionToken,
            emergencyContact = "${config.emergencyContactName} (${config.emergencyContactRelation}) - ${config.emergencyContactPhone}",
            bloodGroup = if (config.includeBloodGroup) config.bloodGroup else null,
            criticalAllergies = config.criticalAllergies,
            criticalMedications = config.criticalMedications,
            importantConditions = config.importantConditions,
            emergencyInstructions = config.emergencyInstructions
        )
    }

    /**
     * Audit access to emergency profile
     */
    fun recordEmergencyAccess(patientUid: String, ip: String? = null, ua: String? = null) {
        val log = EmergencyAccessAudit(
            patientUid = patientUid,
            ipAddress = ip,
            userAgent = ua
        )
        emergencyAccessLogs.computeIfAbsent(patientUid) { mutableListOf() }.add(log)
    }

    fun getAccessLogs(patientUid: String): List<EmergencyAccessAudit> {
        return emergencyAccessLogs[patientUid] ?: emptyList()
    }

    fun getCardConfig(patientUid: String): EmergencyCardConfig? {
        return cardConfigs[patientUid]
    }

    fun clear() {
        cardConfigs.clear()
        emergencyAccessLogs.clear()
    }
}
