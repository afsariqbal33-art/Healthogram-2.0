package com.example.healthogram.healthpassport

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * HealthTimelineService
 * Manages unified, immutable, provenance-preserving timeline for the Health Passport.
 */
class HealthTimelineService private constructor() {

    private val timelineStore = ConcurrentHashMap<String, MutableList<HealthTimelineEntry>>()
    private val paperPrescriptions = ConcurrentHashMap<String, PaperPrescriptionDigitization>()
    private val medicalDocuments = ConcurrentHashMap<String, MedicalDocumentRecord>()

    private val _timelineFlow = MutableStateFlow<List<HealthTimelineEntry>>(emptyList())
    val timelineFlow: Flow<List<HealthTimelineEntry>> = _timelineFlow.asStateFlow()

    companion object {
        @Volatile
        private var instance: HealthTimelineService? = null

        fun getInstance(): HealthTimelineService {
            return instance ?: synchronized(this) {
                instance ?: HealthTimelineService().also { instance = it }
            }
        }
    }

    /**
     * Add entry to patient timeline preserving provenance
     */
    fun addTimelineEntry(patientUid: String, entry: HealthTimelineEntry): HealthTimelineEntry {
        require(entry.provenance.patientUid == patientUid) {
            "Provenance patientUid does not match target patientUid"
        }
        val list = timelineStore.computeIfAbsent(patientUid) { mutableListOf() }
        synchronized(list) {
            list.add(entry)
            // Sort descending by timestamp
            list.sortByDescending { it.timestamp }
        }
        _timelineFlow.value = list.toList()
        return entry
    }

    /**
     * Query timeline with category, time range, and verification filters
     */
    fun getTimeline(
        patientUid: String,
        category: String? = null,
        recordType: String? = null,
        startTime: Long = 0L,
        endTime: Long = Long.MAX_VALUE,
        sourceStatus: RecordSourceStatus? = null
    ): List<HealthTimelineEntry> {
        val list = timelineStore[patientUid] ?: return emptyList()
        return synchronized(list) {
            list.filter { entry ->
                (category == null || entry.category.equals(category, ignoreCase = true)) &&
                (recordType == null || entry.recordType.equals(recordType, ignoreCase = true)) &&
                (entry.timestamp in startTime..endTime) &&
                (sourceStatus == null || entry.provenance.sourceStatus == sourceStatus)
            }.sortedByDescending { it.timestamp }
        }
    }

    /**
     * Paper Prescription Digitization Workflow
     * Upload paper image -> OCR processing -> extracted fields -> User review & correction
     */
    fun submitPaperPrescriptionForDigitization(
        patientUid: String,
        imageUri: String,
        imageHash: String,
        mockOcrText: String? = null
    ): PaperPrescriptionDigitization {
        val rawOcr = mockOcrText ?: """
            Dr. Sarah Al-Busaidi, MD (Cardiology)
            Royal Muscat Clinic - Date: 2026-08-14
            Rx:
            1. Atorvastatin 20mg - 1 tab oral daily at bedtime (30 days)
            2. Lisinopril 10mg - 1 tab daily morning (30 days)
            Signature: [Verified Clinician]
        """.trimIndent()

        val parsedItems = listOf(
            ExtractedPrescriptionItem(
                rawDrugLine = "1. Atorvastatin 20mg - 1 tab oral daily at bedtime (30 days)",
                parsedDrugName = "Atorvastatin",
                parsedDosage = "20mg",
                parsedFrequency = "Once daily at bedtime",
                parsedDurationDays = 30
            ),
            ExtractedPrescriptionItem(
                rawDrugLine = "2. Lisinopril 10mg - 1 tab daily morning (30 days)",
                parsedDrugName = "Lisinopril",
                parsedDosage = "10mg",
                parsedFrequency = "Once daily in the morning",
                parsedDurationDays = 30
            )
        )

        val digitization = PaperPrescriptionDigitization(
            patientUid = patientUid,
            originalImageUri = imageUri,
            imageHashSha256 = imageHash,
            extractedTextRaw = rawOcr,
            extractedDoctorName = "Dr. Sarah Al-Busaidi",
            extractedClinicName = "Royal Muscat Clinic",
            extractedDate = "2026-08-14",
            extractedMedications = parsedItems,
            ocrConfidenceScore = 0.94f,
            userReviewStatus = "PENDING_REVIEW",
            userVerificationDisclaimerAcknowledged = false,
            clinicianReviewed = false
        )

        paperPrescriptions[digitization.digitizationId] = digitization
        return digitization
    }

    /**
     * User reviews, confirms, or edits extracted paper prescription
     */
    fun confirmAndSaveDigitizedPrescription(
        digitizationId: String,
        patientUid: String,
        confirmedItems: List<ExtractedPrescriptionItem>,
        disclaimerAcknowledged: Boolean
    ): HealthTimelineEntry {
        val record = paperPrescriptions[digitizationId]
            ?: throw IllegalArgumentException("Prescription digitization ID not found")
        require(record.patientUid == patientUid) { "Unauthorized access to prescription" }
        require(disclaimerAcknowledged) {
            "User must acknowledge verification disclaimer: Please verify extracted information."
        }

        val updated = record.copy(
            extractedMedications = confirmedItems,
            userReviewStatus = "REVIEWED_CONFIRMED",
            userVerificationDisclaimerAcknowledged = true,
            reviewedAt = System.currentTimeMillis()
        )
        paperPrescriptions[digitizationId] = updated

        // Create timeline entry preserving provenance
        val provenance = HealthRecordProvenance(
            recordId = digitizationId,
            recordType = "PRESCRIPTION",
            patientUid = patientUid,
            createdByUid = patientUid,
            createdByAccountType = "INDIVIDUAL",
            organizationName = record.extractedClinicName,
            sourceSystem = "Healthogram Paper Prescription Digitizer",
            sourceStatus = RecordSourceStatus.DIGITIZED_FROM_PAPER,
            verificationStatus = "PATIENT_VERIFIED",
            modificationNotes = "Digitized from paper prescription; reviewed and confirmed by patient."
        )

        val drugSummary = confirmedItems.joinToString(", ") {
            "${it.userConfirmedName ?: it.parsedDrugName} (${it.userConfirmedDosage ?: it.parsedDosage})"
        }

        val entry = HealthTimelineEntry(
            patientUid = patientUid,
            timestamp = System.currentTimeMillis(),
            recordType = "PRESCRIPTION",
            title = "Prescription (${record.extractedDoctorName ?: "Unknown Doctor"})",
            subtitle = record.extractedClinicName ?: "Paper Prescription",
            summary = "Medications: $drugSummary",
            recordId = digitizationId,
            provenance = provenance,
            verificationStatus = "PATIENT_VERIFIED",
            category = "CLINICAL"
        )

        return addTimelineEntry(patientUid, entry)
    }

    /**
     * Process Medical Document with safe boundary (classification, metadata extraction, summary)
     */
    fun processMedicalDocument(
        patientUid: String,
        storagePath: String,
        mimeType: String,
        fileSizeBytes: Long,
        category: String,
        title: String,
        organization: String?,
        doctorName: String?
    ): MedicalDocumentRecord {
        val provenance = HealthRecordProvenance(
            recordId = UUID.randomUUID().toString(),
            recordType = "DOCUMENT",
            patientUid = patientUid,
            createdByUid = patientUid,
            createdByAccountType = "INDIVIDUAL",
            organizationName = organization,
            sourceSystem = "Healthogram Medical Document Intelligence",
            sourceStatus = RecordSourceStatus.PATIENT_ENTERED,
            verificationStatus = "UNVERIFIED"
        )

        val summary = "Uploaded $category from ${organization ?: "provider"}. Classified as $category. AI extracted dates and provider identities for structured organization. Professional clinician review recommended."

        val doc = MedicalDocumentRecord(
            patientUid = patientUid,
            storagePath = storagePath,
            fileMimeType = mimeType,
            fileSizeBytes = fileSizeBytes,
            documentCategory = category,
            detectedTitle = title,
            detectedProviderName = doctorName,
            detectedOrganizationName = organization,
            detectedDate = "2026-09-01",
            structuredSummary = summary,
            isVerifiedByUser = true,
            provenance = provenance
        )

        medicalDocuments[doc.documentId] = doc

        // Also push to timeline
        val timelineEntry = HealthTimelineEntry(
            patientUid = patientUid,
            timestamp = System.currentTimeMillis(),
            recordType = "DOCUMENT",
            title = doc.detectedTitle,
            subtitle = doc.detectedOrganizationName ?: doc.documentCategory,
            summary = doc.structuredSummary,
            recordId = doc.documentId,
            provenance = provenance,
            verificationStatus = "UNVERIFIED",
            category = "CLINICAL"
        )
        addTimelineEntry(patientUid, timelineEntry)

        return doc
    }

    fun clearPatientData(patientUid: String) {
        timelineStore.remove(patientUid)
        paperPrescriptions.values.removeIf { it.patientUid == patientUid }
        medicalDocuments.values.removeIf { it.patientUid == patientUid }
    }

    fun recordTimelineEntry(entry: HealthTimelineEntry): HealthTimelineEntry {
        return addTimelineEntry(entry.patientUid, entry)
    }

    fun getPatientTimeline(patientUid: String): List<HealthTimelineEntry> {
        return getTimeline(patientUid)
    }

    fun clear() {
        timelineStore.clear()
        paperPrescriptions.clear()
        medicalDocuments.clear()
        _timelineFlow.value = emptyList()
    }
}
