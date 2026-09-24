package com.example.healthogram

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import com.example.healthogram.healthpassport.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class HealthPassportSecurityTest {

    private lateinit var engine: HealthPassportEngine

    private val patient = User(
        uid = "patient_alice",
        email = "alice@test.com",
        phoneNumber = "+1234567890",
        displayName = "Alice Patient",
        username = "alice",
        accountType = AccountType.INDIVIDUAL,
        isVerified = true
    )

    private val doctor = User(
        uid = "doctor_bob",
        email = "dr.bob@hospital.org",
        phoneNumber = "+1987654321",
        displayName = "Dr. Bob, MD",
        username = "drbob",
        accountType = AccountType.DOCTOR,
        isVerified = true
    )

    private val unverifiedDoctor = User(
        uid = "doctor_unverified",
        email = "fake.doc@test.com",
        phoneNumber = "+1122334455",
        displayName = "Fake Doctor",
        username = "fakedoc",
        accountType = AccountType.DOCTOR,
        isVerified = false
    )

    private val laboratory = User(
        uid = "lab_bioref",
        email = "lab@bioref.org",
        phoneNumber = "+1555444333",
        displayName = "BioReference Diagnostics Lab",
        username = "bioreflab",
        accountType = AccountType.LABORATORY,
        isVerified = true
    )

    private val individualUser = User(
        uid = "user_charlie",
        email = "charlie@test.com",
        phoneNumber = "+1444333222",
        displayName = "Charlie Social",
        username = "charlie",
        accountType = AccountType.INDIVIDUAL,
        isVerified = true
    )

    @Before
    fun setup() {
        engine = HealthPassportEngine.getInstance()
        engine.resetForTesting()
        // Initialize profile
        engine.getOrCreateProfile(patient.uid)
    }

    // =========================================================================
    // SCENARIO 1: User tries to view another user's Health Passport without consent -> DENIED
    // =========================================================================
    @Test
    fun testScenario01_unauthorizedUserCannotReadRecordsWithoutConsent() {
        val conditionsResult = engine.getConditionsForPatient(patient.uid, individualUser.uid)
        assertTrue(conditionsResult.isFailure)
        assertTrue(conditionsResult.exceptionOrNull() is SecurityException)

        val allergiesResult = engine.getAllergiesForPatient(patient.uid, individualUser.uid)
        assertTrue(allergiesResult.isFailure)

        val medsResult = engine.getMedicationsForPatient(patient.uid, individualUser.uid)
        assertTrue(medsResult.isFailure)
    }

    // =========================================================================
    // SCENARIO 2: Doctor tries to access records outside approved scopes -> DENIED
    // =========================================================================
    @Test
    fun testScenario02_doctorCannotAccessRecordsOutsideApprovedScopes() {
        // Patient grants DOCTOR access to 'allergies' and 'conditions' ONLY
        val req = engine.requestHealthAccess(
            requester = doctor,
            patientUid = patient.uid,
            requestedScopes = listOf("conditions", "allergies"),
            reason = "Consultation"
        ).getOrThrow()

        engine.approveHealthAccess(patient.uid, req.requestId, listOf("conditions", "allergies"), 24)

        // Doctor CAN read conditions
        val conds = engine.getConditionsForPatient(patient.uid, doctor.uid)
        assertTrue(conds.isSuccess)

        // Doctor CANNOT read medications or prescriptions (scope not granted)
        val meds = engine.getMedicationsForPatient(patient.uid, doctor.uid)
        assertTrue(meds.isFailure)
        assertTrue(meds.exceptionOrNull() is SecurityException)

        val notes = engine.getNotesForPatient(patient.uid, doctor.uid)
        assertTrue(notes.isFailure)
    }

    // =========================================================================
    // SCENARIO 3: Doctor tries to access records after expiration -> DENIED
    // =========================================================================
    @Test
    fun testScenario03_expiredGrantDeniesAccess() {
        val grant = HealthAccessGrant(
            grantId = "grt_expired_test",
            patientUid = patient.uid,
            requesterUid = doctor.uid,
            requesterRole = "DOCTOR",
            grantedScopes = listOf("conditions"),
            purpose = "Past consult",
            createdAt = System.currentTimeMillis() - 7200000,
            startsAt = System.currentTimeMillis() - 7200000,
            expiresAt = System.currentTimeMillis() - 1000, // expired 1 sec ago
            status = "active",
            approvedByUid = patient.uid
        )
        engine.addGrantDirectly(grant)
        assertFalse(grant.isCurrentlyActive)
        assertFalse(engine.hasActivePermission(patient.uid, doctor.uid, "conditions"))
        val conds = engine.getConditionsForPatient(patient.uid, doctor.uid)
        assertTrue(conds.isFailure)
        assertTrue(conds.exceptionOrNull() is SecurityException)
    }

    // =========================================================================
    // SCENARIO 4: Patient revokes access; Doctor tries to read immediately -> DENIED
    // =========================================================================
    @Test
    fun testScenario04_revokingGrantImmediatelyBlocksDoctor() {
        val req = engine.requestHealthAccess(doctor, patient.uid, listOf("conditions"), "Checkup").getOrThrow()
        val grant = engine.approveHealthAccess(patient.uid, req.requestId, listOf("conditions"), 24).getOrThrow()

        // Doctor has access
        assertTrue(engine.getConditionsForPatient(patient.uid, doctor.uid).isSuccess)

        // Patient revokes grant
        val revokeRes = engine.revokeHealthAccess(patient.uid, grant.grantId)
        assertTrue(revokeRes.isSuccess)

        // Doctor immediately blocked
        val postRevoke = engine.getConditionsForPatient(patient.uid, doctor.uid)
        assertTrue(postRevoke.isFailure)
        assertTrue(postRevoke.exceptionOrNull() is SecurityException)
    }

    // =========================================================================
    // SCENARIO 5, 6, 7, 8, 9: Scanner eligibility tests
    // =========================================================================
    @Test
    fun testScenario05_to_09_scannerEligibilityRejectsInvalidAccounts() {
        // Unverified doctor -> REJECTED
        val unverifiedCheck = engine.validateScannerEligibility(unverifiedDoctor)
        assertTrue(unverifiedCheck.isFailure)

        // Individual user -> REJECTED
        val individualCheck = engine.validateScannerEligibility(individualUser)
        assertTrue(individualCheck.isFailure)

        // Doctor and Laboratory -> ACCEPTED
        assertTrue(engine.validateScannerEligibility(doctor).isSuccess)
        assertTrue(engine.validateScannerEligibility(laboratory).isSuccess)
    }

    // =========================================================================
    // SCENARIO 10: QR Code contains zero medical records in QR payload
    // =========================================================================
    @Test
    fun testScenario10_qrPayloadContainsZeroMedicalRecords() {
        val session = engine.generateSecureHealthQRCode(patient.uid, "HG-749204829103")

        // Validate payload attributes
        assertNotNull(session.sessionId)
        assertTrue(session.sessionId.startsWith("qrs_"))
        assertNotNull(session.nonceHash)

        // Assert zero clinical strings in session ticket
        val serializedSession = "${session.sessionId}_${session.patientUid}_${session.nonceHash}"
        val forbiddenMedicalKeywords = listOf("Penicillin", "Rhinitis", "Cardiology", "Allergy", "ICD-10", "Medication", "Prescription", "Cancer", "Diabetes")
        for (kw in forbiddenMedicalKeywords) {
            assertFalse("QR payload must NOT contain medical term '$kw'", serializedSession.contains(kw, ignoreCase = true))
        }
    }

    // =========================================================================
    // SCENARIO 11, 12, 13: Laboratory restrictions
    // =========================================================================
    @Test
    fun testScenario11_to_13_laboratoryCannotAccessBroadHealthPassportOrNotes() {
        // Laboratory requests full passport -> DENIED
        val broadRequest = engine.requestHealthAccess(
            requester = laboratory,
            patientUid = patient.uid,
            requestedScopes = listOf("conditions", "allergies", "notes", "bills"),
            reason = "Test analysis"
        )
        assertTrue(broadRequest.isFailure)
        assertTrue(broadRequest.exceptionOrNull() is SecurityException)

        // Laboratory requests only test/lab scopes -> ALLOWED
        val labValidReq = engine.requestHealthAccess(
            requester = laboratory,
            patientUid = patient.uid,
            requestedScopes = listOf("tests", "lab_reports"),
            reason = "Blood test result upload"
        )
        assertTrue(labValidReq.isSuccess)
    }

    // =========================================================================
    // SCENARIO 14: Public profile contains zero medical records
    // =========================================================================
    @Test
    fun testScenario14_publicProfileZeroMedicalLeakage() {
        val publicProfileMap = mapOf(
            "uid" to "user_123",
            "username" to "johndoe",
            "displayName" to "John Doe",
            "bio" to "Photography & Travel",
            "isVerified" to false
        )
        // Should pass clean
        engine.assertZeroHealthPassportLeakage(publicProfileMap)

        // If medical key leaks, must throw SecurityException
        val leakyProfileMap = publicProfileMap + ("bloodGroup" to "O+")
        try {
            engine.assertZeroHealthPassportLeakage(leakyProfileMap)
            fail("Expected SecurityException on leaked medical key")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("CRITICAL INVARIANT VIOLATION"))
        }
    }

    // =========================================================================
    // SCENARIO 15, 16, 17: Post/Reel/Story cannot attach medical record
    // =========================================================================
    @Test
    fun testScenario15_to_17_socialFeedsProhibitMedicalRecordAttachment() {
        val socialPostWithLeak = mapOf(
            "postId" to "post_1",
            "authorUid" to "user_1",
            "caption" to "Feeling great today!",
            "prescriptionId" to "rx_9981"
        )
        try {
            engine.assertZeroHealthPassportLeakage(socialPostWithLeak)
            fail("Expected SecurityException when social post includes prescriptionId")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("CRITICAL INVARIANT VIOLATION"))
        }
    }

    // =========================================================================
    // SCENARIO 19: Secure document download requires active authorization
    // =========================================================================
    @Test
    fun testScenario19_secureDocumentDownloadEnforcesAuthorization() {
        val doc = engine.uploadPaperPrescription(
            patientUid = patient.uid,
            title = "Specialist Rx",
            fileName = "specialist_rx.pdf",
            fileBytesSize = 1048576L,
            mimeType = "application/pdf"
        ).getOrThrow()

        // Patient download -> SUCCESS
        val patientDownload = engine.secureDocumentDownload(patient.uid, patient.uid, doc.recordId)
        assertTrue(patientDownload.isSuccess)
        assertTrue(patientDownload.getOrThrow().contains("temp_sec_token_"))

        // Unauthorized user download -> DENIED
        val unauthorizedDownload = engine.secureDocumentDownload(patient.uid, individualUser.uid, doc.recordId)
        assertTrue(unauthorizedDownload.isFailure)
        assertTrue(unauthorizedDownload.exceptionOrNull() is SecurityException)
    }

    // =========================================================================
    // SCENARIO 20: Audit logging is immutable and comprehensive
    // =========================================================================
    @Test
    fun testScenario20_auditLoggingRecordsEventsAndContainsZeroClinicalPayload() {
        val initialLogs = engine.getAccessLogsForPatient(patient.uid)
        val initialCount = initialLogs.size

        // Trigger an action
        engine.uploadPaperPrescription(patient.uid, "Skin Consult", "rx.pdf", 500000L, "application/pdf")

        val updatedLogs = engine.getAccessLogsForPatient(patient.uid)
        assertTrue(updatedLogs.size > initialCount)

        // Verify zero clinical detail in logs
        for (log in updatedLogs) {
            assertNotNull(log.action)
            assertNotNull(log.timestamp)
            assertNotNull(log.result)
            // Scope & resource type are structural, not clinical diagnoses
            assertFalse(log.action.contains("Cancer", ignoreCase = true))
            assertFalse(log.scope.contains("penicillin", ignoreCase = true))
        }
    }

    // =========================================================================
    // SCENARIO 21: Health ID format: HG-XXXXXXXXXXXX
    // =========================================================================
    @Test
    fun testScenario21_healthIdFormatAndOpaqueGeneration() {
        val healthId = HealthID.generate("patient_uid_sample", "US")
        assertTrue(healthId.healthId.startsWith("HG-"))
        assertEquals(15, healthId.healthId.length) // "HG-" (3) + 12 alphanumeric = 15

        // Opaque: does NOT contain UID or phone
        assertFalse(healthId.healthId.contains("patient_uid_sample"))
        assertFalse(healthId.healthId.contains("@"))
    }

    // =========================================================================
    // SCENARIO 22: Paper prescription upload validates MIME types
    // =========================================================================
    @Test
    fun testScenario22_paperPrescriptionUploadRejectsDisallowedMimeAndExcessiveSize() {
        // Disallowed executable / archive
        val invalidMimeRes = engine.uploadPaperPrescription(
            patientUid = patient.uid,
            title = "Executable File",
            fileName = "payload.exe",
            fileBytesSize = 1000L,
            mimeType = "application/x-msdownload"
        )
        assertTrue(invalidMimeRes.isFailure)

        // Oversized file (> 20MB)
        val oversizedRes = engine.uploadPaperPrescription(
            patientUid = patient.uid,
            title = "Oversized File",
            fileName = "huge.pdf",
            fileBytesSize = 25 * 1024 * 1024L,
            mimeType = "application/pdf"
        )
        assertTrue(oversizedRes.isFailure)

        // Valid PDF (<= 20MB)
        val validRes = engine.uploadPaperPrescription(
            patientUid = patient.uid,
            title = "Valid Prescription",
            fileName = "rx_clean.pdf",
            fileBytesSize = 2 * 1024 * 1024L,
            mimeType = "application/pdf"
        )
        assertTrue(validRes.isSuccess)
        assertEquals("health_private/${patient.uid}/prescriptions/${validRes.getOrThrow().recordId}.pdf", validRes.getOrThrow().storagePath)
    }

    // =========================================================================
    // SCENARIO 23: Secure export requires strong authentication
    // =========================================================================
    @Test
    fun testScenario23_secureExportMandatesAuthChallenge() {
        // Without challenge passed -> DENIED
        val unauthExport = engine.secureHealthExport(patient.uid, listOf("conditions"), "ALL", false)
        assertTrue(unauthExport.isFailure)
        assertTrue(unauthExport.exceptionOrNull() is SecurityException)

        // With challenge passed -> SUCCESS
        val authExport = engine.secureHealthExport(patient.uid, listOf("conditions"), "ALL", true)
        assertTrue(authExport.isSuccess)
        assertTrue(authExport.getOrThrow().contains("expires="))
    }

    // =========================================================================
    // SCENARIO 24: Offline cache control & data purge
    // =========================================================================
    @Test
    fun testScenario24_offlineCacheDisabledByDefaultAndPurgeable() {
        val settings = engine.getSettings(patient.uid)
        assertFalse(settings.offlinePassportEnabled) // Default OFF

        // Enable and update
        val updated = engine.updateSettings(patient.uid, settings.copy(offlinePassportEnabled = true))
        assertTrue(updated.getOrThrow().offlinePassportEnabled)

        // Disable again -> triggers cache purge
        val disabled = engine.updateSettings(patient.uid, settings.copy(offlinePassportEnabled = false))
        assertFalse(disabled.getOrThrow().offlinePassportEnabled)
    }
}
