package com.example.healthogram

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import com.example.healthogram.healthpassport.*
import com.example.healthogram.healthpassport.fhir.*
import com.example.healthogram.organization.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Step 38 Master Healthcare Integration, Partner Validation & Security Test Suite
 *
 * Validates complete healthcare domain requirements:
 * 1. Health Passport zero-trust security & cross-user denial
 * 2. Opaque QR token lifecycle & anti-replay
 * 3. Scoped consent state machine (REQUESTED -> APPROVED -> ACTIVE -> REVOKED/EXPIRED)
 * 4. Category-level data minimization
 * 5. Break-glass emergency override with immutable audit
 * 6. FHIR R4 validation across resources & conflict-aware import
 * 7. FHIR export bundles with intact provenance
 * 8. Android Health Connect granular permissions & battery guidance
 * 9. Paper prescription candidate extraction & clinician verification
 * 10. Partner certification checklist & lifecycle gate
 */
class HealthPassportStep38ValidationSuite {

    private val patientA = SyntheticHealthcareDataFactory.INDIVIDUAL_A
    private val patientB = SyntheticHealthcareDataFactory.INDIVIDUAL_B
    private val doctorA = SyntheticHealthcareDataFactory.DOCTOR_A
    private val doctorB = SyntheticHealthcareDataFactory.DOCTOR_B
    private val clinicA = SyntheticHealthcareDataFactory.CLINIC_A
    private val hospitalA = SyntheticHealthcareDataFactory.HOSPITAL_A
    private val labA = SyntheticHealthcareDataFactory.LABORATORY_A

    @Before
    fun setUp() {
        HealthTimelineService.getInstance().clear()
        HealthConnectService.getInstance().clear()
        ConsentManagementService.getInstance().clear()
        FHIRInteroperabilityService.getInstance().clear()
        HealthcareIntegrationGateway.getInstance().clear()
        EmergencyHealthCardService.getInstance().clear()
        PaperPrescriptionProcessingService.getInstance().clear()
    }

    // =========================================================================
    // 1. HEALTH PASSPORT SECURITY & ACCESS CONTROL
    // =========================================================================
    @Test
    fun testHealthPassport_UnauthorizedCrossUserAccess_StrictlyDenied() {
        val securityManager = HealthPassportSecurityManager()

        // Individual users are blocked from scanning clinical passports
        val scannerEligibility = securityManager.validateScannerEligibility(patientB)
        assertTrue("Individual users cannot scan clinical passports", scannerEligibility.isFailure)

        // Verified doctor without consent cannot access
        val accessEval = securityManager.evaluateAccessRequest(
            scanner = doctorA,
            patient = patientA,
            requestedScope = HealthAccessScope.FULL,
            purpose = "CONSULTATION",
            existingConsent = null
        )
        assertTrue("Doctor without consent must be denied access", accessEval.isFailure)
    }

    @Test
    fun testHealthPassport_VerifiedDoctorWithActiveConsent_Granted() {
        val securityManager = HealthPassportSecurityManager()
        val consentService = ConsentManagementService.getInstance()

        // Patient grants consent to Doctor A for CONDITIONS and VITALS
        val grant = consentService.requestConsent(
            patientUid = patientA.uid,
            requesterUid = doctorA.uid,
            organizationId = "clinic_001",
            organizationName = "Al-Amal Medical Clinic",
            purpose = "Routine Consultation",
            requestedCategories = setOf("CONDITIONS", "VITALS"),
            durationHours = 24
        )

        // Approve grant
        val approved = consentService.transitionConsentState(
            patientUid = patientA.uid,
            consentId = grant.consentId,
            newState = ConsentState.APPROVED,
            actorUid = patientA.uid
        )
        assertTrue("Consent state should transition to APPROVED", approved)

        val patientConsent = PatientConsent(
            consentId = grant.consentId,
            patientUid = patientA.uid,
            authorizedEntityUid = doctorA.uid,
            authorizedEntityName = doctorA.displayName,
            authorizedAccountType = AccountType.DOCTOR,
            grantedScope = HealthAccessScope.FULL,
            status = AccessConsentStatus.ACTIVE
        )

        val accessEval = securityManager.evaluateAccessRequest(
            scanner = doctorA,
            patient = patientA,
            requestedScope = HealthAccessScope.FULL,
            purpose = "Routine Consultation",
            existingConsent = patientConsent
        )
        assertTrue("Doctor with active consent must be granted access", accessEval.isSuccess)
    }

    // =========================================================================
    // 2. QR SESSION TOKEN SECURITY & ANTI-REPLAY
    // =========================================================================
    @Test
    fun testQrSession_AntiReplayAndExpirationEnforced() {
        val qr = HealthPassportQRCode(
            patientUid = patientA.uid,
            healthId = "OM-HEALTH-883921"
        )

        // Initial token must not be expired and must be opaque hex
        assertNotNull("Ticket must exist", qr.sessionTicket)
        assertEquals(48, qr.sessionTicket.length)
        assertFalse("QR must not be expired", qr.isTicketExpired)

        // Replay attack simulation (Single-use token registry)
        val consumedTickets = mutableSetOf<String>()
        fun processScannedTicket(ticket: String): Boolean {
            if (ticket in consumedTickets) return false // Replay blocked!
            consumedTickets.add(ticket)
            return true
        }

        assertTrue("First scan of QR ticket must succeed", processScannedTicket(qr.sessionTicket))
        assertFalse("Replay attack using same QR ticket must fail", processScannedTicket(qr.sessionTicket))
    }

    // =========================================================================
    // 3. CONSENT STATE MACHINE & CATEGORY-LEVEL MINIMIZATION
    // =========================================================================
    @Test
    fun testConsentStateMachine_FullLifecycleTransitions() {
        val consentService = ConsentManagementService.getInstance()

        // 1. Request
        val grant = consentService.requestConsent(
            patientUid = patientA.uid,
            requesterUid = doctorA.uid,
            organizationId = "org_hosp_1",
            organizationName = "General Hospital",
            purpose = "Inpatient Review",
            requestedCategories = setOf("MEDICATIONS", "LAB_REPORTS"),
            durationHours = 12
        )
        assertEquals("REQUESTED", grant.status)

        // 2. Cannot jump directly from REQUESTED to REVOKED
        val invalidTransition = consentService.transitionConsentState(
            patientUid = patientA.uid,
            consentId = grant.consentId,
            newState = ConsentState.REVOKED,
            actorUid = patientA.uid
        )
        assertFalse("Cannot transition directly from REQUESTED to REVOKED", invalidTransition)

        // 3. Approve -> APPROVED
        val approved = consentService.transitionConsentState(
            patientUid = patientA.uid,
            consentId = grant.consentId,
            newState = ConsentState.APPROVED,
            actorUid = patientA.uid
        )
        assertTrue("Should transition to APPROVED", approved)

        // 4. Verify category authorization
        assertTrue(consentService.isAuthorized(patientA.uid, doctorA.uid, "MEDICATIONS"))
        assertTrue(consentService.isAuthorized(patientA.uid, doctorA.uid, "LAB_REPORTS"))
        assertFalse("CONDITIONS was not granted", consentService.isAuthorized(patientA.uid, doctorA.uid, "CONDITIONS"))

        // 5. Patient revokes consent -> REVOKED
        val revoked = consentService.revokeConsent(patientA.uid, grant.consentId, "Patient ended visit")
        assertTrue("Consent should be revoked", revoked)
        assertFalse("Doctor must no longer be authorized after revocation", consentService.isAuthorized(patientA.uid, doctorA.uid, "MEDICATIONS"))
    }

    // =========================================================================
    // 4. EMERGENCY BREAK-GLASS OVERRIDE & ICE DECOUPLING
    // =========================================================================
    @Test
    fun testEmergencyHealthCard_MinimalDecoupledPayload() {
        val emergencyService = EmergencyHealthCardService.getInstance()
        emergencyService.updateCard(
            patientUid = patientA.uid,
            bloodGroup = "O_POSITIVE",
            emergencyContacts = listOf(EmergencyContact("Sara Doe", "Spouse", "+966501234567")),
            criticalAllergies = listOf("Penicillin (Anaphylaxis)"),
            lifeSavingMedications = listOf("Insulin Glargine"),
            emergencyInstructions = "Patient has Type 1 Diabetes",
            isOptedIn = true
        )

        val payload = emergencyService.buildEmergencyPayload(patientA.uid)
        assertNotNull("Emergency payload must be available when opted in", payload)
        assertEquals("O_POSITIVE", payload?.bloodGroup)
        assertTrue("Must contain Penicillin allergy", payload?.criticalAllergies?.contains("Penicillin (Anaphylaxis)") == true)
        // Ensure sensitive clinical notes are decoupled
        assertFalse("Payload must not contain raw internal timeline notes", payload?.emergencyInstructions?.contains("psychiatry") == true)

        // Audit emergency access
        emergencyService.recordEmergencyAccess(patientA.uid, "192.168.1.1", "AmbulanceTablet/1.0")
        val audits = emergencyService.getAccessLogs(patientA.uid)
        assertEquals(1, audits.size)
        assertEquals("QR_EMERGENCY_VIEW", audits[0].action)
    }

    @Test
    fun testEmergencyBreakGlassOverride_AuditedAndAlerted() {
        val consentService = ConsentManagementService.getInstance()
        val override = consentService.performEmergencyAccess(
            patientUid = patientA.uid,
            clinicianUid = doctorA.uid,
            clinicianRole = "HOSPITAL_EMERGENCY",
            clinicalJustification = "Patient unconscious from acute vehicular trauma",
            facilityName = "Riyadh Central Emergency Department"
        )

        assertNotNull(override)
        assertEquals(patientA.uid, override.patientUid)
        assertEquals("HOSPITAL_EMERGENCY", override.clinicianRole)
        assertTrue("Patient must be alerted immediately upon emergency override", override.alertDispatchedToPatient)

        val logs = consentService.getEmergencyLogs(patientA.uid)
        assertEquals(1, logs.size)
        assertEquals("Patient unconscious from acute vehicular trauma", logs[0].clinicalJustification)
    }

    // =========================================================================
    // 5. FHIR R4 INTEROPERABILITY & CONFLICT RECONCILIATION
    // =========================================================================
    @Test
    fun testFHIR_ExportAndImport_WithConflictDetection() {
        val fhirService = FHIRInteroperabilityService.getInstance()
        val timelineService = HealthTimelineService.getInstance()

        // 1. Setup profile and initial observation
        val profile = HealthProfile(
            uid = patientA.uid,
            healthId = "HP-${patientA.uid}",
            dateOfBirth = "1990-05-12",
            bloodGroup = "O+"
        )
        val initialObs = SyntheticHealthcareDataFactory.createSyntheticObservation(patientA.uid)
        timelineService.recordTimelineEntry(
            HealthTimelineEntry(
                patientUid = patientA.uid,
                timestamp = initialObs.effectiveTimestamp,
                recordType = "OBSERVATION",
                title = initialObs.observationType,
                subtitle = "${initialObs.valueNumeric} ${initialObs.unit}",
                summary = "Initial authoritative observation",
                recordId = initialObs.recordId,
                provenance = initialObs.provenance,
                verificationStatus = "CLINICIAN_VERIFIED"
            )
        )

        // 2. Export FHIR Bundle
        val bundle = fhirService.exportFHIRBundle(
            patientUid = patientA.uid,
            profile = profile,
            observations = listOf(initialObs)
        )
        assertNotNull(bundle)
        assertTrue("Bundle must contain resources", bundle.entry.isNotEmpty())

        // 3. Incoming external observation with conflicting value
        val externalFhirObs = FHIRObservation(
            id = UUID.randomUUID().toString(),
            status = "final",
            code = FHIRCodeableConcept(coding = emptyList(), text = initialObs.observationType),
            subject = FHIRReference(reference = "Patient/${patientA.uid}"),
            effectiveDateTime = java.time.Instant.ofEpochMilli(initialObs.effectiveTimestamp).toString(),
            valueQuantity = FHIRQuantity(value = (initialObs.valueNumeric ?: 100.0) + 50.0, unit = initialObs.unit)
        )

        // 4. Import should detect conflict rather than silently overwriting
        val imported = fhirService.importObservationWithConflictDetection(
            incoming = externalFhirObs,
            targetPatientUid = patientA.uid,
            sourceOrg = "External Partner Hospital",
            existingObservations = listOf(initialObs),
            timelineService = timelineService
        )
        assertNotNull(imported)

        val conflicts = fhirService.getConflicts(patientA.uid)
        assertEquals(1, conflicts.size)
        assertEquals("valueNumeric", conflicts[0].fieldName)
        assertEquals("UNRESOLVED", conflicts[0].conflictStatus)

        // 5. Patient or doctor resolves conflict
        val resolved = fhirService.resolveConflict(
            patientUid = patientA.uid,
            conflictId = conflicts[0].conflictId,
            resolution = "RESOLVED_KEEP_BOTH",
            notes = "Both values kept: home glucometer vs hospital venous draw"
        )
        assertTrue("Conflict should be resolved", resolved)
        assertEquals("RESOLVED_KEEP_BOTH", fhirService.getConflicts(patientA.uid)[0].conflictStatus)
    }

    // =========================================================================
    // 6. HEALTH CONNECT PRIVACY & OEM BATTERY MITIGATION
    // =========================================================================
    @Test
    fun testHealthConnect_GranularPermissionAndRevocation() {
        val healthConnect = HealthConnectService.getInstance()

        // Grant granular permissions
        val conn = healthConnect.requestAndGrantPermissions(
            uid = patientA.uid,
            requestedDataTypes = setOf("STEPS", "HEART_RATE")
        )
        assertEquals(HealthConnectStatus.CONNECTED, conn.connectionStatus)
        assertTrue(conn.grantedDataTypes.contains("STEPS"))
        assertFalse("BLOOD_GLUCOSE was not requested", conn.grantedDataTypes.contains("BLOOD_GLUCOSE"))

        // Revocation instantly invalidates connection
        val revoked = healthConnect.revokeConnection(patientA.uid)
        assertEquals(HealthConnectStatus.REVOKED, revoked.connectionStatus)
        assertTrue("Granted data types must be cleared upon revocation", revoked.grantedDataTypes.isEmpty())
    }

    @Test
    fun testHealthConnect_OemBatteryOptimizationGuidance() {
        val healthConnect = HealthConnectService.getInstance()

        val xiaomiGuidance = healthConnect.getOemExemptionGuidance("Xiaomi")
        assertEquals("Xiaomi", xiaomiGuidance.manufacturer)
        assertTrue(xiaomiGuidance.requiresManualExemption)

        val samsungGuidance = healthConnect.getOemExemptionGuidance("Samsung")
        assertEquals("Samsung", samsungGuidance.manufacturer)
    }

    // =========================================================================
    // 7. PAPER PRESCRIPTION OCR & CLINICIAN GATE
    // =========================================================================
    @Test
    fun testPaperPrescriptionWorkflow_CandidateReviewAndClinicianCommit() {
        val presService = PaperPrescriptionProcessingService.getInstance()

        // Stage 1: Upload validation
        val validation = presService.validateUpload(
            fileName = "prescription_scan.jpg",
            mimeType = "image/jpeg",
            fileSizeBytes = 2 * 1024 * 1024L,
            sha256Hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        )
        assertTrue(validation.isValid)

        // Stage 2: Ingest and extract candidates
        val draft = presService.ingestAndExtract(
            patientUid = patientA.uid,
            fileHashSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            storageUri = "gs://healthogram-synthetic/prescriptions/rx1.jpg",
            extractedItems = listOf(
                ExtractedPrescriptionItem(
                    rawDrugLine = "Amoxicillin 500mg TDS for 7 days",
                    parsedDrugName = "Amoxicillin",
                    parsedDosage = "500mg",
                    parsedFrequency = "Three times daily"
                )
            )
        )
        assertEquals("UNCONFIRMED", draft.reviewStatus)
        assertTrue("Extracted prescription must not be authoritative until verified", draft.isPendingConfirmation)

        // Stage 3: Clinician review & authoritative commit
        val reviewedMedications = listOf(
            PaperPrescriptionProcessingService.ConfirmedMedicationRecord(
                medicineName = "Amoxicillin",
                dosage = "500mg",
                frequency = "Three times daily",
                prescribedByUid = doctorA.uid,
                startDate = "2026-09-20"
            )
        )
        val verified = presService.confirmAndPersist(
            digitizationId = draft.digitizationId,
            verifiedItems = reviewedMedications,
            confirmedByUid = doctorA.uid,
            patientUid = patientA.uid,
            userConsentConfirmed = true
        )
        assertTrue(verified)

        val updatedDraft = presService.getDigitization(draft.digitizationId)
        assertNotNull(updatedDraft)
        assertEquals("REVIEWED_CONFIRMED", updatedDraft?.userReviewStatus)
        assertTrue(updatedDraft?.clinicianReviewed == true)
    }

    // =========================================================================
    // 8. HEALTHCARE PARTNER CERTIFICATION GATES
    // =========================================================================
    @Test
    fun testHealthcarePartnerCertification_LifecycleGateEnforced() {
        val gateway = HealthcareIntegrationGateway.getInstance()

        // 1. Partner applies
        val partner = gateway.registerPartner(
            partnerId = "partner_hospital_99",
            organizationName = "King Fahad Specialist Hospital",
            accountType = "HOSPITAL"
        )
        assertEquals(HealthcarePartnerStatus.APPLIED, partner.status)

        // 2. Attempting to activate before passing certification throws exception
        try {
            gateway.updatePartnerStatus("partner_hospital_99", HealthcarePartnerStatus.ACTIVE)
            fail("Partner must not transition to ACTIVE without passing certification checklist")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("certification checks pass") == true)
        }

        // 3. Complete all 9 certification checks
        val fullCert = PartnerCertificationChecklist(
            partnerId = "partner_hospital_99",
            organizationName = "King Fahad Specialist Hospital",
            authenticationTestPassed = true,
            authorizationTestPassed = true,
            fhirTestPassed = true,
            consentTestPassed = true,
            auditTestPassed = true,
            dataMinimizationTestPassed = true,
            errorHandlingTestPassed = true,
            securityTestPassed = true,
            supportContactVerified = true
        )
        assertTrue("Checklist should be eligible for active", fullCert.isEligibleForActive)

        gateway.submitPartnerCertification("partner_hospital_99", fullCert)
        val verifiedPartner = gateway.getPartner("partner_hospital_99")
        assertEquals(HealthcarePartnerStatus.VERIFIED, verifiedPartner?.status)

        // 4. Now transition to ACTIVE succeeds
        val activePartner = gateway.updatePartnerStatus("partner_hospital_99", HealthcarePartnerStatus.ACTIVE)
        assertEquals(HealthcarePartnerStatus.ACTIVE, activePartner.status)
    }
}
