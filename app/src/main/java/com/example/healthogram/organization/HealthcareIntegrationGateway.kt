package com.example.healthogram.organization

import com.example.healthogram.healthpassport.fhir.FHIRResource
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

enum class HealthcarePartnerStatus {
    APPLIED,
    UNDER_REVIEW,
    VERIFIED,
    ACTIVE,
    SUSPENDED,
    REVOKED,
    EXPIRED
}

data class PartnerCertificationChecklist(
    val partnerId: String,
    val organizationName: String,
    val authenticationTestPassed: Boolean = false,
    val authorizationTestPassed: Boolean = false,
    val fhirTestPassed: Boolean = false,
    val consentTestPassed: Boolean = false,
    val auditTestPassed: Boolean = false,
    val dataMinimizationTestPassed: Boolean = false,
    val errorHandlingTestPassed: Boolean = false,
    val securityTestPassed: Boolean = false,
    val supportContactVerified: Boolean = false
) {
    val isEligibleForActive: Boolean
        get() = authenticationTestPassed &&
                authorizationTestPassed &&
                fhirTestPassed &&
                consentTestPassed &&
                auditTestPassed &&
                dataMinimizationTestPassed &&
                errorHandlingTestPassed &&
                securityTestPassed &&
                supportContactVerified
}

data class RegisteredPartnerRecord(
    val partnerId: String,
    val organizationName: String,
    val accountType: String, // CLINIC, HOSPITAL, LABORATORY
    val status: HealthcarePartnerStatus = HealthcarePartnerStatus.APPLIED,
    val certification: PartnerCertificationChecklist = PartnerCertificationChecklist(partnerId, organizationName),
    val registeredAt: Long = System.currentTimeMillis(),
    val statusUpdatedAt: Long = System.currentTimeMillis(),
    val rejectionOrSuspensionReason: String? = null
)

/**
 * Step 32: Healthcare Integration Gateway & Provider-Independent Adapters
 *
 * Enforces decoupling: Core business logic does NOT hardcode specific external
 * healthcare vendors. All communications pass through standardized adapter contracts.
 */

interface FHIRPartnerAdapter {
    val partnerId: String
    val fhirVersion: String
    fun fetchResource(resourceType: String, resourceId: String): FHIRResource?
    fun postResource(resource: FHIRResource): Boolean
    fun ping(): Boolean
}

interface HospitalSystemAdapter {
    val hospitalId: String
    val systemName: String // e.g. "Epic", "Cerner", "Local HL7"
    fun queryPatientEncounters(patientHealthId: String): List<String>
    fun notifyAdmission(patientHealthId: String, department: String): Boolean
}

interface LaboratoryAdapter {
    val laboratoryId: String
    fun transmitOrder(order: LabOrderRecord): Boolean
    fun fetchAnalyteResults(orderId: String): List<StructuredLabResultItem>
}

interface AppointmentProviderAdapter {
    val providerId: String
    fun syncAvailability(providerUid: String): List<AppointmentSlot>
    fun reserveSlot(slotId: String, patientUid: String): Boolean
}

interface IdentityVerificationAdapter {
    val countryCode: String
    fun verifyClinicianLicense(licenseNumber: String, country: String): Boolean
}

interface HealthDataImportAdapter {
    val sourceFormat: String // FHIR_R4, CDA, DICOM_METADATA, CSV
    fun parse(payload: ByteArray): List<FHIRResource>
}

/**
 * HealthcareIntegrationGateway
 * Manages external partner adapters and dispatches integration events.
 */
class HealthcareIntegrationGateway private constructor() {

    private val fhirAdapters = ConcurrentHashMap<String, FHIRPartnerAdapter>()
    private val hospitalAdapters = ConcurrentHashMap<String, HospitalSystemAdapter>()
    private val labAdapters = ConcurrentHashMap<String, LaboratoryAdapter>()
    private val appointmentAdapters = ConcurrentHashMap<String, AppointmentProviderAdapter>()
    private val identityAdapters = ConcurrentHashMap<String, IdentityVerificationAdapter>()
    private val importAdapters = ConcurrentHashMap<String, HealthDataImportAdapter>()

    data class IntegrationEvent(
        val eventId: String = UUID.randomUUID().toString(),
        val adapterType: String,
        val partnerId: String,
        val action: String,
        val status: String, // SUCCESS, FAILURE, TIMEOUT
        val timestamp: Long = System.currentTimeMillis(),
        val latencyMs: Long = 0L,
        val errorMessage: String? = null
    )

    private val eventLogs = ConcurrentHashMap<String, MutableList<IntegrationEvent>>()

    companion object {
        @Volatile
        private var instance: HealthcareIntegrationGateway? = null

        fun getInstance(): HealthcareIntegrationGateway {
            return instance ?: synchronized(this) {
                instance ?: HealthcareIntegrationGateway().also { instance = it }
            }
        }
    }

    fun registerFHIRAdapter(adapter: FHIRPartnerAdapter) {
        fhirAdapters[adapter.partnerId] = adapter
    }

    fun registerHospitalAdapter(adapter: HospitalSystemAdapter) {
        hospitalAdapters[adapter.hospitalId] = adapter
    }

    fun registerLaboratoryAdapter(adapter: LaboratoryAdapter) {
        labAdapters[adapter.laboratoryId] = adapter
    }

    fun registerAppointmentAdapter(adapter: AppointmentProviderAdapter) {
        appointmentAdapters[adapter.providerId] = adapter
    }

    fun registerIdentityAdapter(adapter: IdentityVerificationAdapter) {
        identityAdapters[adapter.countryCode] = adapter
    }

    fun registerImportAdapter(adapter: HealthDataImportAdapter) {
        importAdapters[adapter.sourceFormat] = adapter
    }

    fun getFHIRAdapter(partnerId: String): FHIRPartnerAdapter? = fhirAdapters[partnerId]
    fun getHospitalAdapter(hospitalId: String): HospitalSystemAdapter? = hospitalAdapters[hospitalId]
    fun getLaboratoryAdapter(laboratoryId: String): LaboratoryAdapter? = labAdapters[laboratoryId]

    fun logIntegrationEvent(event: IntegrationEvent) {
        eventLogs.computeIfAbsent(event.partnerId) { mutableListOf() }.add(event)
    }

    fun getIntegrationEvents(partnerId: String): List<IntegrationEvent> {
        return eventLogs[partnerId] ?: emptyList()
    }

    private val registeredPartners = ConcurrentHashMap<String, RegisteredPartnerRecord>()

    fun registerPartner(
        partnerId: String,
        organizationName: String,
        accountType: String
    ): RegisteredPartnerRecord {
        val record = RegisteredPartnerRecord(
            partnerId = partnerId,
            organizationName = organizationName,
            accountType = accountType,
            status = HealthcarePartnerStatus.APPLIED
        )
        registeredPartners[partnerId] = record
        return record
    }

    fun updatePartnerStatus(
        partnerId: String,
        newStatus: HealthcarePartnerStatus,
        reason: String? = null
    ): RegisteredPartnerRecord {
        val partner = registeredPartners[partnerId] ?: throw IllegalArgumentException("Partner not registered")
        
        // Cannot activate without full certification pass
        if (newStatus == HealthcarePartnerStatus.ACTIVE && !partner.certification.isEligibleForActive) {
            throw IllegalStateException("Partner cannot transition to ACTIVE until all certification checks pass")
        }

        val updated = partner.copy(
            status = newStatus,
            statusUpdatedAt = System.currentTimeMillis(),
            rejectionOrSuspensionReason = reason
        )
        registeredPartners[partnerId] = updated
        return updated
    }

    fun submitPartnerCertification(
        partnerId: String,
        certification: PartnerCertificationChecklist
    ): RegisteredPartnerRecord {
        val partner = registeredPartners[partnerId] ?: throw IllegalArgumentException("Partner not registered")
        val updated = partner.copy(
            certification = certification,
            status = if (certification.isEligibleForActive) HealthcarePartnerStatus.VERIFIED else partner.status,
            statusUpdatedAt = System.currentTimeMillis()
        )
        registeredPartners[partnerId] = updated
        return updated
    }

    fun getPartner(partnerId: String): RegisteredPartnerRecord? = registeredPartners[partnerId]

    fun clear() {
        registeredPartners.clear()
        fhirAdapters.clear()
        hospitalAdapters.clear()
        labAdapters.clear()
        appointmentAdapters.clear()
        identityAdapters.clear()
        importAdapters.clear()
        eventLogs.clear()
    }
}
