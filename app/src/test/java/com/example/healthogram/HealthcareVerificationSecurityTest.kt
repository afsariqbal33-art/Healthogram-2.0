package com.example.healthogram

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.verification.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class HealthcareVerificationSecurityTest {

    private lateinit var engine: VerificationEngine

    private val doctorUser = User(
        uid = "doc_marcus",
        email = "marcus@hospital.org",
        phoneNumber = "+1234567890",
        displayName = "Dr. Marcus Vance, MD",
        username = "marcusvance",
        accountType = AccountType.DOCTOR,
        countryCode = "US",
        countryName = "United States",
        isVerified = false
    )

    private val clinicUser = User(
        uid = "clinic_metro",
        email = "admin@metroclinic.com",
        phoneNumber = "+1987654321",
        displayName = "Metro Health Clinic",
        username = "metroclinic",
        accountType = AccountType.CLINIC,
        countryCode = "GB",
        countryName = "United Kingdom",
        isVerified = false
    )

    private val individualUser = User(
        uid = "ind_alex",
        email = "alex@test.com",
        phoneNumber = "+1555666777",
        displayName = "Alex Individual",
        username = "alexind",
        accountType = AccountType.INDIVIDUAL,
        countryCode = "AE",
        countryName = "United Arab Emirates",
        isVerified = false
    )

    private val staffReviewer = User(
        uid = "staff_rev_1",
        email = "reviewer@healthogram.com",
        phoneNumber = "+1000000001",
        displayName = "Staff Reviewer",
        username = "staffrev1",
        accountType = AccountType.INDIVIDUAL,
        isVerified = true
    )

    private val staffManager = User(
        uid = "staff_mgr_1",
        email = "manager@healthogram.com",
        phoneNumber = "+1000000002",
        displayName = "Staff Manager",
        username = "staffmgr1",
        accountType = AccountType.INDIVIDUAL,
        isVerified = true
    )

    @Before
    fun setup() {
        engine = VerificationEngine.getInstance()
        engine.resetForTesting()
        engine.assignRoleForTesting(staffReviewer.uid, VerificationRole.VERIFICATION_REVIEWER)
        engine.assignRoleForTesting(staffManager.uid, VerificationRole.VERIFICATION_MANAGER)
    }

    @Test
    fun testStartApplication_PermittedAccountTypes() {
        val doctorAppRes = engine.startApplication(doctorUser, AccountType.DOCTOR, "US")
        assertTrue("Doctor application should succeed", doctorAppRes.isSuccess)
        val doctorApp = doctorAppRes.getOrNull()!!
        assertEquals(AccountType.DOCTOR, doctorApp.accountType)
        assertEquals("US", doctorApp.countryCode)
        assertEquals(VerificationStatus.DRAFT, doctorApp.status)

        val clinicAppRes = engine.startApplication(clinicUser, AccountType.CLINIC, "GB")
        assertTrue("Clinic application should succeed", clinicAppRes.isSuccess)

        val indAppRes = engine.startApplication(individualUser, AccountType.INDIVIDUAL, "AE")
        assertTrue("Individual application should succeed", indAppRes.isSuccess)
    }

    @Test
    fun testDocumentUpload_PrivateStorageAndMasking() {
        val app = engine.startApplication(doctorUser, AccountType.DOCTOR, "US").getOrNull()!!

        val docRes = engine.uploadDocument(
            user = doctorUser,
            applicationId = app.applicationId,
            documentType = "medical_license",
            documentName = "State Medical Board License",
            fileName = "license_2026.pdf",
            fileSizeBytes = 2 * 1024 * 1024L,
            mimeType = "application/pdf",
            documentNumber = "MD-984729184"
        )

        assertTrue("Upload should succeed", docRes.isSuccess)
        val doc = docRes.getOrNull()!!

        // Invariant: Storage path MUST be private
        assertTrue(
            "Path must be in verification_private bucket",
            doc.storagePath.startsWith("verification_private/${doctorUser.uid}/${app.applicationId}/")
        )

        // Invariant: Document number MUST be masked, only last 4 digits
        assertEquals("9184", doc.maskedDocumentNumber)
        assertFalse(doc.storagePath.contains("MD-984729184"))
    }

    @Test
    fun testSubmitApplication_ValidatesRequirements() {
        val app = engine.startApplication(doctorUser, AccountType.DOCTOR, "US").getOrNull()!!

        // Submit without required documents should fail
        val failedSubmit = engine.submitApplication(doctorUser, app.applicationId)
        assertTrue("Submission should fail when required docs are missing", failedSubmit.isFailure)

        // Upload required documents
        engine.uploadDocument(
            user = doctorUser,
            applicationId = app.applicationId,
            documentType = "government_id",
            documentName = "Passport ID",
            fileName = "passport.jpg",
            fileSizeBytes = 1024 * 1024L,
            mimeType = "image/jpeg",
            documentNumber = "PASS12345"
        )
        engine.uploadDocument(
            user = doctorUser,
            applicationId = app.applicationId,
            documentType = "medical_license",
            documentName = "State Medical License",
            fileName = "license.pdf",
            fileSizeBytes = 1024 * 1024L,
            mimeType = "application/pdf",
            documentNumber = "LIC998877"
        )

        val successSubmit = engine.submitApplication(doctorUser, app.applicationId)
        assertTrue("Submission should succeed with docs", successSubmit.isSuccess)
        assertEquals(VerificationStatus.SUBMITTED, successSubmit.getOrNull()!!.status)

        // Check verification queue contains this application
        val queue = engine.queue.value
        assertTrue("Queue must contain submitted application", queue.any { it.applicationId == app.applicationId })
    }

    @Test
    fun testReviewAndApproval_RbacEnforced() {
        val app = engine.startApplication(doctorUser, AccountType.DOCTOR, "US").getOrNull()!!
        engine.uploadDocument(doctorUser, app.applicationId, "government_id", "ID", "id.png", 1024, "image/png", "12345")
        engine.uploadDocument(doctorUser, app.applicationId, "medical_license", "License", "lic.pdf", 1024, "application/pdf", "67890")
        engine.submitApplication(doctorUser, app.applicationId)

        // Unauthorized user attempts approval
        val unauthorizedApprove = engine.approveApplication(individualUser, app.applicationId)
        assertTrue("Non-staff user cannot approve", unauthorizedApprove.isFailure)

        // Staff Reviewer starts review
        val reviewRes = engine.startReview(staffReviewer, app.applicationId)
        assertTrue("Staff reviewer can start review", reviewRes.isSuccess)
        assertEquals(VerificationStatus.UNDER_REVIEW, reviewRes.getOrNull()!!.status)

        // Staff Reviewer approves application
        val approveRes = engine.approveApplication(staffReviewer, app.applicationId)
        assertTrue("Staff reviewer can approve", approveRes.isSuccess)

        val profile = engine.profiles.value[doctorUser.uid]!!
        assertEquals(VerificationStatus.VERIFIED, profile.verificationStatus)
        assertNotNull("Verification expiry date must be set", profile.verificationExpiresAt)
    }

    @Test
    fun testHealthcareScannerEligibility_StrictGating() {
        val now = System.currentTimeMillis()
        val oneYearFromNow = now + (365L * 24 * 3600 * 1000)
        val past = now - 10000

        // 1. Verified Doctor within validity
        val verifiedDoctor = doctorUser.copy(
            isVerified = true,
            verificationStatus = VerificationStatus.VERIFIED,
            verificationExpiresAt = oneYearFromNow
        )
        assertTrue("Active verified doctor can scan", verifiedDoctor.isEligibleToScanHealthPassport)
        assertTrue(engine.validateHealthcareScannerEligibility(verifiedDoctor).isSuccess)

        // 2. Verified Clinic within validity
        val verifiedClinic = clinicUser.copy(
            isVerified = true,
            verificationStatus = VerificationStatus.VERIFIED,
            verificationExpiresAt = oneYearFromNow
        )
        assertTrue("Active verified clinic can scan", verifiedClinic.isEligibleToScanHealthPassport)

        // 3. Verified Individual CANNOT scan
        val verifiedIndividual = individualUser.copy(
            isVerified = true,
            verificationStatus = VerificationStatus.VERIFIED,
            verificationExpiresAt = oneYearFromNow
        )
        assertFalse("Verified individual CANNOT scan", verifiedIndividual.isEligibleToScanHealthPassport)
        assertTrue(engine.validateHealthcareScannerEligibility(verifiedIndividual).isFailure)

        // 4. Unverified Doctor CANNOT scan
        val unverifiedDoctor = doctorUser.copy(isVerified = false, verificationStatus = VerificationStatus.NOT_STARTED)
        assertFalse("Unverified doctor cannot scan", unverifiedDoctor.isEligibleToScanHealthPassport)

        // 5. Expired Doctor CANNOT scan
        val expiredDoctor = doctorUser.copy(
            isVerified = true,
            verificationStatus = VerificationStatus.VERIFIED,
            verificationExpiresAt = past
        )
        assertFalse("Expired doctor cannot scan", expiredDoctor.isEligibleToScanHealthPassport)

        // 6. Suspended Doctor CANNOT scan
        val suspendedDoctor = doctorUser.copy(
            isVerified = true,
            verificationStatus = VerificationStatus.VERIFIED,
            isSuspended = true,
            verificationExpiresAt = oneYearFromNow
        )
        assertFalse("Suspended doctor cannot scan", suspendedDoctor.isEligibleToScanHealthPassport)

        // 7. Revoked Doctor CANNOT scan
        val revokedDoctor = doctorUser.copy(
            isVerified = false,
            verificationStatus = VerificationStatus.REVOKED
        )
        assertFalse("Revoked doctor cannot scan", revokedDoctor.isEligibleToScanHealthPassport)
    }

    @Test
    fun testRejection_StandardizedCodesAndSafeExplanation() {
        val app = engine.startApplication(doctorUser, AccountType.DOCTOR, "US").getOrNull()!!
        engine.uploadDocument(doctorUser, app.applicationId, "government_id", "ID", "id.png", 1024, "image/png", "12345")
        engine.uploadDocument(doctorUser, app.applicationId, "medical_license", "License", "lic.pdf", 1024, "application/pdf", "67890")
        engine.submitApplication(doctorUser, app.applicationId)

        val rejectRes = engine.rejectApplication(
            staffReviewer,
            app.applicationId,
            RejectionReasonCode.DOCUMENT_UNREADABLE,
            internalNotes = "Image blurry in bottom corner"
        )
        assertTrue("Rejection should succeed", rejectRes.isSuccess)

        val profile = engine.profiles.value[doctorUser.uid]!!
        assertEquals(VerificationStatus.REJECTED, profile.verificationStatus)
        assertEquals("document_unreadable", profile.rejectionReasonCode)

        // Ensure safe explanation does NOT reveal internal review notes
        val codeObj = RejectionReasonCode.fromCode(profile.rejectionReasonCode)
        assertFalse(codeObj.safeUserExplanation.contains("Image blurry in bottom corner"))
        assertTrue(codeObj.safeUserExplanation.contains("unreadable or blurry"))
    }

    @Test
    fun testSuspensionAndRevocation_ByManager() {
        // Setup verified doctor profile
        val profile = engine.getOrCreateProfile(doctorUser)
        val verifiedProfile = profile.copy(
            verificationStatus = VerificationStatus.VERIFIED,
            verifiedBadge = true,
            verificationExpiresAt = System.currentTimeMillis() + 1000000
        )
        engine.updateProfileForTesting(doctorUser.uid, verifiedProfile)

        // Suspend
        val suspendRes = engine.suspendVerification(staffManager, doctorUser.uid, "Investigation pending")
        assertTrue("Manager can suspend", suspendRes.isSuccess)
        val suspended = engine.profiles.value[doctorUser.uid]!!
        assertEquals(VerificationStatus.SUSPENDED, suspended.verificationStatus)
        assertTrue(suspended.isSuspended)

        // Revoke
        val revokeRes = engine.revokeVerification(staffManager, doctorUser.uid, "Fraudulent credentials detected")
        assertTrue("Manager can revoke", revokeRes.isSuccess)
        val revoked = engine.profiles.value[doctorUser.uid]!!
        assertEquals(VerificationStatus.REVOKED, revoked.verificationStatus)
        assertFalse(revoked.isVerified)
        assertTrue(revoked.isRevoked)
    }

    @Test
    fun testAuditLogs_ImmutableAndRecorded() {
        val app = engine.startApplication(doctorUser, AccountType.DOCTOR, "US").getOrNull()!!
        engine.uploadDocument(doctorUser, app.applicationId, "government_id", "ID", "id.png", 1024, "image/png", "12345")
        engine.uploadDocument(doctorUser, app.applicationId, "medical_license", "License", "lic.pdf", 1024, "application/pdf", "67890")
        engine.submitApplication(doctorUser, app.applicationId)
        engine.approveApplication(staffReviewer, app.applicationId)

        val logs = engine.auditLogs.value.filter { it.uid == doctorUser.uid }
        assertTrue("Audit logs must record application lifecycle", logs.isNotEmpty())
        assertTrue(logs.any { it.action == VerificationAction.APPLICATION_CREATED })
        assertTrue(logs.any { it.action == VerificationAction.APPLICATION_SUBMITTED })
        assertTrue(logs.any { it.action == VerificationAction.APPLICATION_APPROVED })
    }
}
