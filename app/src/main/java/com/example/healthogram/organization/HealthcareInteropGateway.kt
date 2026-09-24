package com.example.healthogram.organization

import com.example.healthogram.healthpassport.*
import com.example.healthogram.healthpassport.fhir.*
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

enum class LabOrderStatus {
    ORDERED,
    SAMPLE_COLLECTED,
    ANALYZING,
    COMPLETED,
    CANCELLED
}

data class LabOrderRecord(
    val orderId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val orderingDoctorUid: String,
    val orderingDoctorName: String,
    val laboratoryUid: String,
    val laboratoryName: String,
    val testCode: String,
    val testName: String,
    val sampleType: String = "Blood", // Blood, Urine, Swab, Tissue
    val status: LabOrderStatus = LabOrderStatus.ORDERED,
    val clinicalNotes: String = "",
    val sampleCollectedTimestamp: Long? = null,
    val completedTimestamp: Long? = null,
    val resultingLabReportId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class StructuredLabResultItem(
    val analyteName: String,
    val code: String,
    val numericValue: Double,
    val unit: String,
    val referenceRangeLow: Double,
    val referenceRangeHigh: Double,
    val flag: String = "NORMAL" // NORMAL, HIGH, LOW, CRITICAL
)

/**
 * Healthcare Integration Adapter
 * Contract for integrating hospital EHRs, lab analyzers, and clinic systems.
 */
interface HealthcareIntegrationAdapter {
    fun authenticate(credentials: Map<String, String>): Boolean
    fun discoverEndpoints(): List<String>
    fun fetchExternalRecords(patientRef: String, resourceType: String): List<String>
    fun transformToFHIR(rawRecord: String): FHIRResource?
    fun validate(resource: FHIRResource): Boolean
    fun importRecords(patientUid: String, resources: List<FHIRResource>): Int
    fun revoke(): Boolean
}

/**
 * HealthcareInteropGateway
 * Handles external integrations, laboratory digital test orders, and result dispatches.
 */
class HealthcareInteropGateway private constructor(
    private val fhirInterop: FHIRInteroperabilityService = FHIRInteroperabilityService.getInstance(),
    private val timelineService: HealthTimelineService = HealthTimelineService.getInstance()
) {

    private val labOrders = ConcurrentHashMap<String, LabOrderRecord>()

    companion object {
        @Volatile
        private var instance: HealthcareInteropGateway? = null

        fun getInstance(): HealthcareInteropGateway {
            return instance ?: synchronized(this) {
                instance ?: HealthcareInteropGateway().also { instance = it }
            }
        }
    }

    /**
     * Doctor creates digital lab test order for a laboratory
     */
    fun createLabOrder(
        patientUid: String,
        doctorUid: String,
        doctorName: String,
        laboratoryUid: String,
        laboratoryName: String,
        testCode: String,
        testName: String,
        notes: String = ""
    ): LabOrderRecord {
        val order = LabOrderRecord(
            patientUid = patientUid,
            orderingDoctorUid = doctorUid,
            orderingDoctorName = doctorName,
            laboratoryUid = laboratoryUid,
            laboratoryName = laboratoryName,
            testCode = testCode,
            testName = testName,
            clinicalNotes = notes
        )
        labOrders[order.orderId] = order
        return order
    }

    /**
     * Laboratory updates sample status
     */
    fun updateSampleStatus(
        orderId: String,
        laboratoryUid: String,
        newStatus: LabOrderStatus
    ): LabOrderRecord {
        val order = labOrders[orderId] ?: throw IllegalArgumentException("Lab order not found")
        require(order.laboratoryUid == laboratoryUid) { "Unauthorized laboratory for this order" }

        val updated = order.copy(
            status = newStatus,
            sampleCollectedTimestamp = if (newStatus == LabOrderStatus.SAMPLE_COLLECTED) System.currentTimeMillis() else order.sampleCollectedTimestamp
        )
        labOrders[orderId] = updated
        return updated
    }

    /**
     * Laboratory uploads structured test results, generates HealthLabReport and updates patient timeline
     */
    fun completeLabOrderWithResults(
        orderId: String,
        laboratoryUid: String,
        reportSummary: String,
        storagePath: String,
        structuredResults: List<StructuredLabResultItem>
    ): Pair<LabOrderRecord, HealthLabReport> {
        val order = labOrders[orderId] ?: throw IllegalArgumentException("Lab order not found")
        require(order.laboratoryUid == laboratoryUid) { "Unauthorized laboratory for this order" }

        val labReportId = UUID.randomUUID().toString()

        val provenance = HealthRecordProvenance(
            recordId = labReportId,
            recordType = "LAB_REPORT",
            patientUid = order.patientUid,
            createdByUid = laboratoryUid,
            createdByAccountType = "LABORATORY",
            organizationName = order.laboratoryName,
            sourceSystem = "Healthogram Laboratory 2.1",
            sourceStatus = RecordSourceStatus.LABORATORY_GENERATED,
            verificationStatus = "LAB_VERIFIED"
        )

        val report = HealthLabReport(
            recordId = labReportId,
            patientUid = order.patientUid,
            laboratoryUid = laboratoryUid,
            testId = order.orderId,
            reportStatus = "FINAL",
            summary = "$reportSummary [Analyzed: ${structuredResults.size} analytes]",
            reportFilePath = storagePath
        )

        val updatedOrder = order.copy(
            status = LabOrderStatus.COMPLETED,
            completedTimestamp = System.currentTimeMillis(),
            resultingLabReportId = labReportId
        )
        labOrders[orderId] = updatedOrder

        // Add to timeline
        val timelineEntry = HealthTimelineEntry(
            patientUid = order.patientUid,
            timestamp = System.currentTimeMillis(),
            recordType = "LAB_TEST",
            title = order.testName,
            subtitle = order.laboratoryName,
            summary = report.summary,
            recordId = labReportId,
            provenance = provenance,
            verificationStatus = "LAB_VERIFIED",
            category = "CLINICAL"
        )
        timelineService.addTimelineEntry(order.patientUid, timelineEntry)

        return Pair(updatedOrder, report)
    }

    fun getOrdersForPatient(patientUid: String): List<LabOrderRecord> {
        return labOrders.values.filter { it.patientUid == patientUid }.sortedByDescending { it.createdAt }
    }

    fun getOrdersForLab(laboratoryUid: String): List<LabOrderRecord> {
        return labOrders.values.filter { it.laboratoryUid == laboratoryUid }.sortedByDescending { it.createdAt }
    }

    interface PartnerAdapter {
        val partnerId: String
        fun fetchPatientRecords(patientId: String): List<FHIRObservation>
        fun sendLabOrder(order: LabOrderRequest): Boolean
    }

    data class LabOrderRequest(
        val patientId: String,
        val testName: String
    )

    private val partnerAdapters = ConcurrentHashMap<String, PartnerAdapter>()

    fun registerAdapter(adapter: PartnerAdapter) {
        partnerAdapters[adapter.partnerId] = adapter
    }

    fun getAdapter(partnerId: String): PartnerAdapter? {
        return partnerAdapters[partnerId]
    }

    fun clear() {
        labOrders.clear()
        partnerAdapters.clear()
    }
}
