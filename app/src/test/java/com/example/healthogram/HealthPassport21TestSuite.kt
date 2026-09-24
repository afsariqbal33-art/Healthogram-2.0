package com.example.healthogram

import com.example.healthogram.aistudio.*
import com.example.healthogram.core.AccountType
import com.example.healthogram.finance.*
import com.example.healthogram.healthpassport.*
import com.example.healthogram.healthpassport.fhir.*
import com.example.healthogram.organization.*
import com.example.healthogram.payments.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class HealthPassport21TestSuite {

    private val patientUid = "patient_salim"
    private val doctorUid = "dr_khalfan"
    private val labUid = "lab_royal_pathology"

    @Before
    fun setup() {
        HealthTimelineService.getInstance().clear()
        HealthConnectService.getInstance().clear()
        ConsentManagementService.getInstance().clear()
        FHIRInteroperabilityService.getInstance().clear()
        HealthcareInteropGateway.getInstance().clear()
        HealthcareAIService.getInstance().clear()
        CreatorMonetizationService.getInstance().clear()
        SellerMonetizationService.getInstance().clear()
        GlobalPaymentService.getInstance().clear()
        AppointmentService.getInstance().clear()
    }

    // --- TEST 1: Health Passport 2.1 Provenance & Timeline ---
    @Test
    fun testHealthRecordProvenancePreservation() {
        val timelineService = HealthTimelineService.getInstance()
        val provenance = HealthRecordProvenance(
            recordId = "rec_001",
            recordType = "OBSERVATION",
            patientUid = patientUid,
            createdByUid = doctorUid,
            createdByAccountType = "DOCTOR",
            organizationName = "Sultan Qaboos Comprehensive Cancer Center",
            sourceSystem = "EHR_EPIC_R4",
            sourceStatus = RecordSourceStatus.PROVIDER_ENTERED,
            verificationStatus = "CLINICIAN_VERIFIED"
        )

        val entry = HealthTimelineEntry(
            patientUid = patientUid,
            timestamp = System.currentTimeMillis(),
            recordType = "OBSERVATION",
            title = "Blood Glucose Fasting",
            subtitle = "5.4 mmol/L",
            summary = "Normal fasting plasma glucose level.",
            recordId = "rec_001",
            provenance = provenance,
            verificationStatus = "CLINICIAN_VERIFIED",
            category = "CLINICAL"
        )

        timelineService.addTimelineEntry(patientUid, entry)
        val timeline = timelineService.getTimeline(patientUid)

        assertEquals(1, timeline.size)
        assertEquals("CLINICIAN_VERIFIED", timeline[0].verificationStatus)
        assertEquals("Sultan Qaboos Comprehensive Cancer Center", timeline[0].provenance.organizationName)
        assertEquals(RecordSourceStatus.PROVIDER_ENTERED, timeline[0].provenance.sourceStatus)
    }

    // --- TEST 2: Health Connect Sync & Permission Boundaries ---
    @Test
    fun testHealthConnectGranularSync() {
        val healthConnect = HealthConnectService.getInstance()
        val requestedDataTypes = setOf("STEPS", "HEART_RATE", "SLEEP", "WEIGHT")

        val connection = healthConnect.requestAndGrantPermissions(patientUid, requestedDataTypes)
        assertEquals(HealthConnectStatus.CONNECTED, connection.connectionStatus)
        assertTrue(connection.grantedDataTypes.contains("STEPS"))

        val now = System.currentTimeMillis()
        val records = listOf(
            HealthConnectRecordData(type = "STEPS", numericValue = 8450.0, unit = "steps", timestamp = now),
            HealthConnectRecordData(type = "HEART_RATE", numericValue = 72.0, unit = "bpm", timestamp = now),
            HealthConnectRecordData(type = "SLEEP", numericValue = 450.0, unit = "minutes", timestamp = now),
            HealthConnectRecordData(type = "WEIGHT", numericValue = 74.5, unit = "kg", timestamp = now)
        )

        val syncJob = healthConnect.syncHealthConnectRecords(
            uid = patientUid,
            records = records
        )

        assertEquals("COMPLETED", syncJob.status)
        val timeline = HealthTimelineService.getInstance().getTimeline(patientUid)
        assertEquals(4, timeline.size)
    }

    // --- TEST 3: FHIR R4 Interoperability & Terminology Mapping ---
    @Test
    fun testFHIRResourceMappingAndTerminology() {
        val condition = HealthCondition(
            recordId = "cond_123",
            patientUid = patientUid,
            conditionName = "Type 2 Diabetes Mellitus",
            conditionCode = "E11.9",
            diagnosedDate = "2023-05-10",
            status = "ACTIVE",
            createdByUid = doctorUid
        )

        val fhirCondition = FHIRResourceMapper.toFHIRCondition(condition)
        assertEquals("Condition", fhirCondition.resourceType)
        assertEquals("active", fhirCondition.clinicalStatus)
        assertEquals("Patient/$patientUid", fhirCondition.subject.reference)
        assertEquals("E11.9", fhirCondition.code.coding[0].code)

        // LOINC and ICD-10 Terminology checks
        val heartRateCoding = TerminologyService.getObservationCoding("HEART_RATE")
        assertEquals("8867-4", heartRateCoding.code)

        val conditionCoding = TerminologyService.getConditionCoding("Type 2 Diabetes", "E11.9")
        assertEquals("E11.9", conditionCoding.coding[0].code)
    }

    // --- TEST 4: FHIR Interoperability Validation & Conflict Detection ---
    @Test
    fun testFHIRConflictDetectionOnImport() {
        val fhirService = FHIRInteroperabilityService.getInstance()
        val timelineService = HealthTimelineService.getInstance()

        val existingObs = HealthObservation(
            recordId = "obs_local_1",
            patientUid = patientUid,
            observationType = "Blood Glucose",
            valueNumeric = 5.2,
            unit = "mmol/L",
            effectiveTimestamp = System.currentTimeMillis() - 3600000L,
            provenance = HealthRecordProvenance(
                recordId = "obs_local_1",
                recordType = "OBSERVATION",
                patientUid = patientUid,
                createdByUid = "local_device",
                createdByAccountType = "INDIVIDUAL",
                organizationName = "Home Glucometer",
                sourceSystem = "HealthConnect",
                sourceStatus = RecordSourceStatus.PROVIDER_ENTERED
            )
        )

        val incomingFhir = FHIRObservation(
            id = "fhir_obs_ext_99",
            subject = FHIRReference(reference = "Patient/$patientUid"),
            code = FHIRCodeableConcept(
                coding = listOf(FHIRCoding("http://loinc.org", "2339-0", "Blood Glucose")),
                text = "Blood Glucose"
            ),
            valueQuantity = FHIRQuantity(value = 8.9, unit = "mmol/L"),
            effectiveDateTime = java.time.Instant.now().toString()
        )

        // Ingesting should detect conflict without overwriting
        fhirService.importObservationWithConflictDetection(
            incoming = incomingFhir,
            targetPatientUid = patientUid,
            sourceOrg = "National Diabetes Clinic",
            existingObservations = listOf(existingObs),
            timelineService = timelineService
        )

        val conflicts = fhirService.getConflicts(patientUid)
        assertEquals(1, conflicts.size)
        assertEquals("UNRESOLVED", conflicts[0].conflictStatus)
        assertEquals("5.2 mmol/L", conflicts[0].existingValue)
        assertEquals("8.9 mmol/L", conflicts[0].incomingValue)

        // Resolve conflict
        val resolved = fhirService.resolveConflict(
            patientUid = patientUid,
            conflictId = conflicts[0].conflictId,
            resolution = "RESOLVED_KEEP_BOTH",
            notes = "Different test times, keeping both readings for clinic review."
        )
        assertTrue(resolved)
        assertEquals("RESOLVED_KEEP_BOTH", fhirService.getConflicts(patientUid)[0].conflictStatus)
    }

    // --- TEST 5: Health Data Portability Export Service ---
    @Test
    fun testHealthDataExportSecurityAndFormats() {
        val exportService = HealthDataExportService.getInstance()
        val exportRecord = exportService.createExportRecord(
            patientUid = patientUid,
            requestedByUid = patientUid,
            format = ExportFormat.FHIR_R4_BUNDLE,
            categories = listOf("ALLERGIES", "CONDITIONS", "OBSERVATIONS")
        )

        assertNotNull(exportRecord.downloadUrl)
        assertTrue(exportRecord.downloadUrl.endsWith(".fhir.json"))
        assertTrue(exportRecord.expiresAt > System.currentTimeMillis())

        // Unauthorized export attempt for different user must fail
        assertThrows(IllegalArgumentException::class.java) {
            exportService.createExportRecord(
                patientUid = "another_patient_456",
                requestedByUid = patientUid,
                format = ExportFormat.PDF_SUMMARY,
                categories = listOf("ALL")
            )
        }
    }

    // --- TEST 6: Scoped Consent Management & Emergency Access ---
    @Test
    fun testScopedConsentAndEmergencyAccess() {
        val consentService = ConsentManagementService.getInstance()

        // Patient grants doctor access to ALLERGIES and MEDICATIONS only
        val grant = consentService.grantConsent(
            patientUid = patientUid,
            recipientUid = doctorUid,
            organizationId = "org_hospital_01",
            organizationName = "Al Nahdha Hospital",
            purpose = "Cardiology Consultation",
            allowedCategories = setOf("ALLERGIES", "MEDICATIONS"),
            durationHours = 48
        )

        assertTrue(consentService.isAuthorized(patientUid, doctorUid, "ALLERGIES"))
        assertTrue(consentService.isAuthorized(patientUid, doctorUid, "MEDICATIONS"))
        assertFalse(consentService.isAuthorized(patientUid, doctorUid, "LAB_REPORTS"))

        // Emergency Access Override test
        val emergencyLog = consentService.performEmergencyAccess(
            patientUid = patientUid,
            clinicianUid = doctorUid,
            clinicianRole = "HOSPITAL_EMERGENCY",
            facilityName = "Khoula Hospital Trauma Center",
            clinicalJustification = "Unresponsive patient presenting with multiple trauma in emergency bay"
        )

        assertNotNull(emergencyLog)
        assertTrue(emergencyLog.alertDispatchedToPatient)
        val audits = consentService.getAuditTrail(patientUid)
        assertTrue(audits.any { it.action == "EMERGENCY_OVERRIDE" })
    }

    // --- TEST 7: HealthPassportGateway Filtering & Access Rules ---
    @Test
    fun testHealthPassportGatewayEnforcement() {
        val consentService = ConsentManagementService.getInstance()
        val gateway = HealthPassportGateway.getInstance()

        consentService.grantConsent(
            patientUid = patientUid,
            recipientUid = doctorUid,
            organizationId = "org_01",
            organizationName = "Specialty Clinic",
            purpose = "Treatment",
            allowedCategories = setOf("ALLERGIES")
        )

        val allergies = listOf(
            HealthAllergy(
                patientUid = patientUid,
                allergen = "Aspirin",
                reaction = "Bronchospasm",
                severity = "HIGH",
                createdByUid = doctorUid
            )
        )
        val medications = listOf(
            HealthMedication(
                patientUid = patientUid,
                medicineName = "Metformin",
                dosage = "500mg",
                frequency = "BID",
                prescribedByUid = doctorUid
            )
        )

        val dataPackage = gateway.getScopedPatientData(
            patientUid = patientUid,
            requesterUid = doctorUid,
            requesterAccountType = AccountType.DOCTOR,
            organizationId = "org_01",
            requestedCategories = setOf("ALLERGIES", "MEDICATIONS"),
            allAllergies = allergies,
            allMedications = medications
        )

        // Gateway should strictly filter: allergies included, medications stripped because not in consent
        assertEquals(1, dataPackage.allergies.size)
        assertEquals(0, dataPackage.medications.size)
    }

    // --- TEST 8: Laboratory 2.1 Workflow ---
    @Test
    fun testLaboratoryWorkflowAndStructuredResults() {
        val interopGateway = HealthcareInteropGateway.getInstance()

        // 1. Doctor creates lab order
        val order = interopGateway.createLabOrder(
            patientUid = patientUid,
            doctorUid = doctorUid,
            doctorName = "Dr. Khalfan",
            laboratoryUid = labUid,
            laboratoryName = "Precision Diagnostic Lab",
            testCode = "L-001",
            testName = "Lipid Panel"
        )
        assertEquals(LabOrderStatus.ORDERED, order.status)

        // 2. Lab updates sample
        val collectedOrder = interopGateway.updateSampleStatus(order.orderId, labUid, LabOrderStatus.SAMPLE_COLLECTED)
        assertEquals(LabOrderStatus.SAMPLE_COLLECTED, collectedOrder.status)

        // 3. Lab completes test and uploads structured results
        val results = listOf(
            StructuredLabResultItem("Total Cholesterol", "2093-3", 4.5, "mmol/L", 3.0, 5.2, "NORMAL"),
            StructuredLabResultItem("HDL Cholesterol", "2085-9", 1.4, "mmol/L", 1.0, 2.0, "NORMAL")
        )
        val (completedOrder, report) = interopGateway.completeLabOrderWithResults(
            orderId = order.orderId,
            laboratoryUid = labUid,
            reportSummary = "Lipid panel within normal limits.",
            storagePath = "labs/orders/${order.orderId}.pdf",
            structuredResults = results
        )

        assertEquals(LabOrderStatus.COMPLETED, completedOrder.status)
        assertEquals("FINAL", report.reportStatus)
        assertNotNull(completedOrder.resultingLabReportId)
    }

    // --- TEST 9: Healthcare AI Privacy Boundary ---
    @Test
    fun testHealthcareAIPrivacyBoundaryAndHumanGate() {
        val aiService = HealthcareAIService.getInstance()

        val req = HealthcareAIRequest(
            patientUid = patientUid,
            requesterUid = patientUid,
            purpose = "TERMINOLOGY_EXPLANATION",
            minimizedTextPayload = "What does HDL Cholesterol 1.4 mmol/L mean?",
            consentVerified = true
        )

        val response = aiService.processHealthcareAssistance(req)
        assertTrue(response.requiresHumanConfirmation)
        assertFalse(response.isConfirmedByUser)
        assertTrue(response.disclaimer.contains("does not diagnose"))

        // User confirms result
        val confirmed = aiService.confirmAIResult(response, patientUid)
        assertTrue(confirmed.isConfirmedByUser)
    }

    // --- TEST 10: Creator Monetization 2.1 & Double-Entry Math ---
    @Test
    fun testCreatorMonetizationCalculation() {
        val monetization = CreatorMonetizationService.getInstance()
        // 100.000 OMR = 100,000 minor units
        val grossMinor = 100000L

        val tx = monetization.processCreatorEarning(
            creatorUid = "creator_salma",
            supporterUid = patientUid,
            type = CreatorMonetizationType.SUBSCRIPTION,
            grossAmountMinorUnits = grossMinor,
            currency = "OMR",
            countryCode = "OM"
        )

        // 10% platform fee = 10,000 minor units
        assertEquals(10000L, tx.platformFeeMinorUnits)
        // 2.5% payment fee = 2,500 minor units
        assertEquals(2500L, tx.paymentProcessingFeeMinorUnits)
        // 5% Oman VAT on platform fee = 500 minor units
        assertEquals(500L, tx.taxMinorUnits)
        // Net = 100,000 - 10,000 - 2,500 - 500 = 87,000 minor units
        assertEquals(87000L, tx.netCreatorAmountMinorUnits)

        val balance = monetization.getCreatorBalance("creator_salma")
        assertEquals(87000L, balance.availableBalanceMinorUnits)

        // Payout request
        val payout = monetization.requestPayout(
            creatorUid = "creator_salma",
            amountMinorUnits = 50000L,
            iban = "OM62BOMN000012345678"
        )
        assertEquals("PROCESSING", payout.status)
        assertEquals(37000L, monetization.getCreatorBalance("creator_salma").availableBalanceMinorUnits)
    }

    // --- TEST 11: Global Payments & Idempotency ---
    @Test
    fun testGlobalPaymentIdempotencyAndRefund() {
        val paymentService = GlobalPaymentService.getInstance()
        val idempotencyKey = UUID.randomUUID().toString()

        val tx1 = paymentService.processPayment(
            idempotencyKey = idempotencyKey,
            payerUid = patientUid,
            payeeUid = doctorUid,
            amountMinorUnits = 25000L, // 25.000 OMR
            currency = "OMR",
            countryCode = "OM",
            paymentMethod = "OMAN_NET_DEBIT"
        )
        assertEquals("SUCCESS", tx1.status)
        assertEquals("THAWANI", tx1.gatewayUsed)

        // Duplicate call with same idempotency key must return exact same transaction
        val tx2 = paymentService.processPayment(
            idempotencyKey = idempotencyKey,
            payerUid = patientUid,
            payeeUid = doctorUid,
            amountMinorUnits = 25000L,
            currency = "OMR",
            countryCode = "OM",
            paymentMethod = "OMAN_NET_DEBIT"
        )
        assertEquals(tx1.transactionId, tx2.transactionId)

        // Refund
        val refund = paymentService.processRefund(tx1.transactionId, "Doctor rescheduled")
        assertEquals("COMPLETED", refund.status)
        assertEquals("REFUNDED", paymentService.getTransaction(tx1.transactionId)?.status)
    }

    // --- TEST 12: Health Data Quality Evaluator ---
    @Test
    fun testHealthDataQualityScoring() {
        val qualityService = HealthDataQualityService.getInstance()
        val provenance = HealthRecordProvenance(
            recordId = "rec_q1",
            recordType = "OBSERVATION",
            patientUid = patientUid,
            createdByUid = doctorUid,
            createdByAccountType = "DOCTOR",
            organizationName = "City Clinic",
            sourceSystem = "EHR",
            sourceStatus = RecordSourceStatus.PROVIDER_ENTERED,
            verificationStatus = "VERIFIED"
        )

        val entries = listOf(
            HealthTimelineEntry(
                patientUid = patientUid,
                timestamp = System.currentTimeMillis(),
                recordType = "OBSERVATION",
                title = "HbA1c",
                subtitle = "5.7%",
                summary = "Normal",
                recordId = "rec_q1",
                provenance = provenance,
                verificationStatus = "VERIFIED",
                category = "CLINICAL"
            )
        )

        val report = qualityService.evaluateDataQuality(patientUid, entries)
        assertTrue(report.completenessScorePercent > 0)
        assertEquals(1, report.verifiedRecordsCount)
        assertEquals(0, report.potentialDuplicateCount)
    }

    // --- TEST 13: Paper Prescription OCR Workflow with Mandatory Confirmation Gate ---
    @Test
    fun testPaperPrescriptionOcrAndConfirmation() {
        val ocrService = PaperPrescriptionProcessingService.getInstance()
        ocrService.clear()

        // 1. Validate file
        val validation = ocrService.validateUpload(
            fileName = "rx_september.pdf",
            mimeType = "application/pdf",
            fileSizeBytes = 1024 * 500,
            sha256Hash = "a3f5b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6"
        )
        assertTrue(validation.isValid)

        // 2. Candidate extraction (non-authoritative)
        val extractedItem = ExtractedPrescriptionItem(
            rawDrugLine = "Metformin 500mg PO BID with meals #60",
            parsedDrugName = "Metformin",
            parsedDosage = "500mg",
            parsedFrequency = "Twice daily"
        )
        val digitization = ocrService.ingestAndExtract(
            patientUid = patientUid,
            uploaderUid = patientUid,
            storageUri = "gs://healthogram-vault/rx/doc_001.pdf",
            validation = validation,
            rawOcrText = "Metformin 500mg PO BID with meals",
            doctorCandidate = "Dr. Salim Al-Harthy",
            clinicCandidate = "Al Noor Clinic",
            extractedItems = listOf(extractedItem),
            confidenceScore = 0.92f
        )
        assertEquals("PENDING_REVIEW", digitization.userReviewStatus)
        assertFalse(digitization.clinicianReviewed)

        // 3. User Review and Confirmation
        val confirmedMed = HealthMedication(
            recordId = "med_metformin_01",
            patientUid = patientUid,
            medicineName = "Metformin",
            dosage = "500mg",
            frequency = "Twice daily",
            prescribedByUid = "dr_salim",
            startDate = "2026-09-18"
        )
        val confirmed = ocrService.confirmAndPersist(
            digitizationId = digitization.digitizationId,
            reviewerUid = patientUid,
            reviewerAccountType = "INDIVIDUAL",
            confirmedMedications = listOf(confirmedMed),
            disclaimerAcknowledged = true
        )
        assertEquals("REVIEWED_CONFIRMED", confirmed.userReviewStatus)
        assertTrue(confirmed.userVerificationDisclaimerAcknowledged)

        // Verify provenance recorded in timeline
        val timeline = HealthTimelineService.getInstance().getTimeline(patientUid)
        val createdEntry = timeline.find { it.recordId == "med_metformin_01" }
        assertNotNull(createdEntry)
        assertEquals(RecordSourceStatus.DIGITIZED_FROM_PAPER, createdEntry?.provenance?.sourceStatus)
    }

    // --- TEST 14: FHIR R4 Validation & Bidirectional Mapping ---
    @Test
    fun testFHIRValidationAndMapping() {
        val validationService = FHIRValidationService.getInstance()
        val mappingService = FHIRMappingService.getInstance()

        // Test FHIR Observation validation
        val obs = FHIRObservation(
            id = "obs_glucose_01",
            status = "final",
            code = FHIRCodeableConcept(
                coding = listOf(FHIRCoding(system = "http://loinc.org", code = "15074-8", display = "Glucose [Moles/volume] in Blood")),
                text = "Fasting Blood Glucose"
            ),
            subject = FHIRReference(reference = "Patient/$patientUid"),
            effectiveDateTime = "2026-09-18T08:00:00Z",
            valueQuantity = FHIRQuantity(value = 5.6, unit = "mmol/L")
        )
        val report = validationService.validateResource(obs)
        assertTrue(report.isValid)
        assertFalse(report.hasErrors)

        // Test Reverse Mapping from FHIR to internal Observation
        val mapResult = mappingService.mapFHIRToObservation(obs, patientUid, "Muscat Central Lab")
        assertTrue(mapResult.isSuccess)
        assertNotNull(mapResult.mappedOutput)
        assertEquals(5.6, mapResult.mappedOutput?.valueNumeric ?: 0.0, 0.01)
        assertEquals(RecordSourceStatus.IMPORTED, mapResult.mappedOutput?.provenance?.sourceStatus)
    }

    // --- TEST 15: Healthcare Integration Gateway & Partner Adapter Decoupling ---
    @Test
    fun testHealthcareIntegrationGateway() {
        val gateway = HealthcareIntegrationGateway.getInstance()
        gateway.clear()

        // Mock FHIR Adapter
        val mockAdapter = object : FHIRPartnerAdapter {
            override val partnerId: String = "partner_royal_hospital"
            override val fhirVersion: String = "4.0.1"
            override fun fetchResource(resourceType: String, resourceId: String): FHIRResource? = null
            override fun postResource(resource: FHIRResource): Boolean = true
            override fun ping(): Boolean = true
        }
        gateway.registerFHIRAdapter(mockAdapter)
        assertNotNull(gateway.getFHIRAdapter("partner_royal_hospital"))
        assertTrue(gateway.getFHIRAdapter("partner_royal_hospital")?.ping() == true)

        gateway.logIntegrationEvent(
            HealthcareIntegrationGateway.IntegrationEvent(
                adapterType = "FHIR_PARTNER",
                partnerId = "partner_royal_hospital",
                action = "PING",
                status = "SUCCESS",
                latencyMs = 45L
            )
        )
        assertEquals(1, gateway.getIntegrationEvents("partner_royal_hospital").size)
    }

    // --- TEST 16: Appointment 2.1 State Lifecycle & Privacy-Safe Notifications ---
    @Test
    fun testAppointmentLifecycleAndSafeNotification() {
        val appointmentService = AppointmentService.getInstance()
        appointmentService.clear()

        appointmentService.setProviderSlots(
            doctorUid,
            listOf(
                AppointmentSlot(
                    slotId = "slot_100",
                    providerUid = doctorUid,
                    startTime = 1758189600000L,
                    endTime = 1758191400000L,
                    isAvailable = true
                )
            )
        )

        val booking = appointmentService.bookAppointment(
            patientUid = patientUid,
            providerUid = doctorUid,
            organizationId = "clinic_muscat",
            appointmentType = AppointmentType.DOCTOR_IN_PERSON,
            slotId = "slot_100",
            location = "Consultation Room 3"
        )
        assertEquals(AppointmentStatus.CONFIRMED, booking.status)

        // Notification must NOT contain medical conditions
        val safeText = appointmentService.buildSafeNotificationText(booking)
        assertTrue(safeText.contains("confirmed"))
        assertFalse(safeText.contains("Diabetes"))
        assertFalse(safeText.contains("Cancer"))

        // Update status to COMPLETED
        val completed = appointmentService.updateAppointmentStatus(
            appointmentId = booking.appointmentId,
            newStatus = AppointmentStatus.COMPLETED,
            actorUid = doctorUid
        )
        assertEquals(AppointmentStatus.COMPLETED, completed.status)
    }

    // --- TEST 17: Health Consent Center with Granular Scoping & Revocation ---
    @Test
    fun testHealthConsentCenterFlow() {
        val consentCenter = HealthConsentCenter.getInstance()
        consentCenter.clear()

        // 1. Doctor requests access
        val request = consentCenter.submitAccessRequest(
            patientUid = patientUid,
            requesterUid = doctorUid,
            requesterName = "Dr. Khalfan",
            organizationId = "org_royal",
            organizationName = "Royal Hospital",
            requesterAccountType = "DOCTOR",
            requestedCategories = setOf("ALLERGIES", "MEDICATIONS", "CONDITIONS", "LAB_REPORTS"),
            purpose = "TREATMENT"
        )
        assertEquals(HealthConsentCenter.RequestStatus.PENDING, request.status)

        // 2. Patient approves with limited categories (only ALLERGIES and MEDICATIONS)
        val grant = consentCenter.approveRequest(
            patientUid = patientUid,
            requestId = request.requestId,
            approvedCategories = setOf("ALLERGIES", "MEDICATIONS")
        )
        assertEquals("ACTIVE", grant.status)
        assertTrue(grant.allowedRecordCategories.contains("ALLERGIES"))
        assertFalse(grant.allowedRecordCategories.contains("LAB_REPORTS"))

        // 3. Patient revokes access
        val revoked = consentCenter.revokeActiveConsent(patientUid, grant.consentId, "Consultation concluded")
        assertTrue(revoked)
    }

    // --- TEST 18: Emergency Health Card Opt-in & Minimal QR Payload ---
    @Test
    fun testEmergencyHealthCardPrivacy() {
        val emergencyService = EmergencyHealthCardService.getInstance()
        emergencyService.clear()

        // Verify unconfigured card returns null payload
        assertNull(emergencyService.buildEmergencyPayload(patientUid))

        // Configure minimal card
        val config = EmergencyHealthCardService.EmergencyCardConfig(
            patientUid = patientUid,
            isOptedIn = true,
            emergencyContactName = "Ahmed Al-Balushi",
            emergencyContactPhone = "+968 9123 4567",
            emergencyContactRelation = "Brother",
            includeBloodGroup = true,
            bloodGroup = "O+",
            criticalAllergies = listOf("Penicillin (Anaphylaxis)"),
            criticalMedications = listOf("EpiPen 0.3mg Auto-Injector"),
            importantConditions = listOf("Severe Peanut Allergy"),
            emergencyInstructions = "Call ambulance immediately upon exposure"
        )
        emergencyService.configureCard(config)

        val payload = emergencyService.buildEmergencyPayload(patientUid)
        assertNotNull(payload)
        assertEquals("O+", payload?.bloodGroup)
        assertEquals(1, payload?.criticalAllergies?.size)
        // Ensure non-diagnostic disclaimer
        assertTrue(payload?.disclaimer?.contains("Non-comprehensive") == true)

        // Revoke card
        emergencyService.revokeCard(patientUid)
        assertNull(emergencyService.buildEmergencyPayload(patientUid))
    }

    // --- TEST 19: CountryConfigService & CurrencyService ---
    @Test
    fun testCountryConfigAndMinorUnits() {
        val countryService = com.example.healthogram.core.CountryConfigService.getInstance()
        val currencyService = CurrencyService.getInstance()

        val omanConfig = countryService.getCountryConfig("OM")
        assertEquals("OMR", omanConfig.currency)
        assertTrue(omanConfig.rtlEnabled)

        // Minor units calculation: 1 OMR = 1000 Baiza
        val baiza = currencyService.toMinorUnits(15.500, "OMR")
        assertEquals(15500L, baiza)
        assertEquals(15.5, currencyService.toMajorUnits(baiza, "OMR"), 0.001)

        val formattedOman = currencyService.formatAmount(baiza, "OMR", isArabic = true)
        assertTrue(formattedOman.contains("ر.ع."))
    }

    // --- TEST 20: Unified Search & Personalization Safety Boundary ---
    @Test
    fun testSearchSafetyAndPersonalizationGuard() {
        val searchService = com.example.healthogram.core.UnifiedSearchService.getInstance()
        val personalization = com.example.healthogram.core.PersonalizationSafetyService.getInstance()

        // 1. Search must reject health records indexing
        try {
            searchService.indexPublicContent(
                domain = com.example.healthogram.core.SearchDomain.POSTS,
                result = com.example.healthogram.core.UnifiedSearchResult(
                    id = "res_1",
                    domain = com.example.healthogram.core.SearchDomain.POSTS,
                    title = "Test",
                    subtitle = "Test",
                    summary = "Test"
                ),
                sourceCollection = "health_passport"
            )
            fail("Expected SecurityException when indexing health_passport")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("PROHIBITED") == true)
        }

        // 2. Personalization safety guard: default MUST be false
        assertFalse(personalization.canUseHealthDataForRecommendations(patientUid))
    }

    // --- TEST 21: Feature Flag Phased Rollout Engine ---
    @Test
    fun testFeatureFlagPhasedRollout() {
        val flagService = com.example.healthogram.core.FeatureFlagService.getInstance()

        // Check health passport v2 is active for Oman Individual
        val isHpEnabled = flagService.isFeatureEnabled(
            key = com.example.healthogram.core.FeatureFlagService.FLAG_HEALTH_PASSPORT_V2,
            userUid = patientUid,
            countryCode = "OM",
            accountType = AccountType.INDIVIDUAL
        )
        assertTrue(isHpEnabled)

        // Check kill switch
        flagService.activateKillSwitch(com.example.healthogram.core.FeatureFlagService.FLAG_HEALTH_PASSPORT_V2)
        val isKilled = flagService.isFeatureEnabled(
            key = com.example.healthogram.core.FeatureFlagService.FLAG_HEALTH_PASSPORT_V2,
            userUid = patientUid,
            countryCode = "OM",
            accountType = AccountType.INDIVIDUAL
        )
        assertFalse(isKilled)
        flagService.deactivateKillSwitch(com.example.healthogram.core.FeatureFlagService.FLAG_HEALTH_PASSPORT_V2)
    }
}
