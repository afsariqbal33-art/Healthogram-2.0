package com.example.healthogram

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.MarketplaceRole
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.devices.DeviceManager
import com.example.healthogram.devices.DevicePermissions
import com.example.healthogram.devices.DeviceStatus
import com.example.healthogram.devices.SubscriptionPackage
import com.example.healthogram.healthpassport.HealthAccessScope
import com.example.healthogram.healthpassport.HealthPassportQRCode
import com.example.healthogram.healthpassport.HealthPassportSecurityManager
import com.example.healthogram.healthpassport.PatientConsent
import com.example.healthogram.owner.LedgerType
import com.example.healthogram.owner.OwnerControlEngine
import com.example.healthogram.owner.PlatformFeature
import com.example.healthogram.payments.PaymentAbstractionService
import com.example.healthogram.verification.CountryVerificationEngine
import com.example.healthogram.verification.DocumentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class HealthogramFoundationTest {

    @Test
    fun testAccountTypes_onlyAllowedFiveExistAndPharmacyIsStrictlyForbidden() {
        val allowedNames = AccountType.entries.map { it.name }
        assertEquals(5, allowedNames.size)
        assertTrue(allowedNames.contains("INDIVIDUAL"))
        assertTrue(allowedNames.contains("DOCTOR"))
        assertTrue(allowedNames.contains("CLINIC"))
        assertTrue(allowedNames.contains("HOSPITAL"))
        assertTrue(allowedNames.contains("LABORATORY"))

        // Critical architectural constraint: PHARMACY must not exist
        assertFalse(AccountType.isAllowed("PHARMACY"))
        assertFalse(AccountType.isAllowed("pharmacy"))
        assertFalse(AccountType.isAllowed("Medicine Company"))

        try {
            AccountType.fromString("PHARMACY")
            fail("Expected SecurityException when querying PHARMACY")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("Architectural Violation"))
        }
    }

    @Test
    fun testHealthPassportEligibility_laboratoryHasNoPersonalPassport() {
        val patient = User(
            uid = "user_patient_1",
            email = "patient@test.com",
            phoneNumber = "+1234567890",
            displayName = "Jane Doe",
            username = "janedoe",
            accountType = AccountType.INDIVIDUAL
        )
        assertTrue(patient.isEligibleForPersonalHealthPassport)

        val lab = User(
            uid = "user_lab_1",
            email = "lab@test.com",
            phoneNumber = "+1987654321",
            displayName = "Apex Diagnostics Lab",
            username = "apexlab",
            accountType = AccountType.LABORATORY
        )
        // Rule: Laboratory must NOT receive a personal Health Passport
        assertFalse(lab.isEligibleForPersonalHealthPassport)
    }

    @Test
    fun testScannerEligibility_unverifiedAccountsCannotScan() {
        val unverifiedDoctor = User(
            uid = "doc_1",
            email = "doc@test.com",
            phoneNumber = "+111222333",
            displayName = "Dr. Smith",
            username = "drsmith",
            accountType = AccountType.DOCTOR,
            isVerified = false
        )
        assertFalse(unverifiedDoctor.isEligibleToScanHealthPassport)

        val verifiedDoctor = unverifiedDoctor.copy(
            isVerified = true,
            verificationStatus = VerificationStatus.APPROVED
        )
        assertTrue(verifiedDoctor.isEligibleToScanHealthPassport)

        val verifiedIndividual = User(
            uid = "ind_1",
            email = "ind@test.com",
            phoneNumber = "+111222334",
            displayName = "Alice",
            username = "alice",
            accountType = AccountType.INDIVIDUAL,
            isVerified = true
        )
        // Normal individual cannot scan clinical health passports
        assertFalse(verifiedIndividual.isEligibleToScanHealthPassport)
    }

    @Test
    fun testHealthPassportSecurity_accessRequiresConsentAndAuditsAllAttempts() {
        val securityManager = HealthPassportSecurityManager()
        val patient = User(
            uid = "patient_100",
            email = "patient@test.com",
            phoneNumber = "+100",
            displayName = "Patient Alpha",
            username = "p_alpha",
            accountType = AccountType.INDIVIDUAL
        )
        val verifiedDoctor = User(
            uid = "doc_200",
            email = "doc@test.com",
            phoneNumber = "+200",
            displayName = "Dr. House",
            username = "drhouse",
            accountType = AccountType.DOCTOR,
            isVerified = true,
            verificationStatus = VerificationStatus.APPROVED
        )

        // Attempt without consent
        val unauthorizedAttempt = securityManager.evaluateAccessRequest(
            scanner = verifiedDoctor,
            patient = patient,
            requestedScope = HealthAccessScope.FULL,
            purpose = "Routine Consultation",
            existingConsent = null
        )
        assertTrue(unauthorizedAttempt.isFailure)

        // Verify audit log generated for denied attempt
        val logs = securityManager.getAuditLogsForPatient(patient.uid)
        assertEquals(1, logs.size)
        assertFalse(logs.first().isGranted)

        // Valid consent granted by patient
        val validConsent = PatientConsent(
            patientUid = patient.uid,
            authorizedEntityUid = verifiedDoctor.uid,
            authorizedEntityName = verifiedDoctor.displayName,
            authorizedAccountType = verifiedDoctor.accountType,
            grantedScope = HealthAccessScope.FULL
        )

        val authorizedAttempt = securityManager.evaluateAccessRequest(
            scanner = verifiedDoctor,
            patient = patient,
            requestedScope = HealthAccessScope.FULL,
            purpose = "Routine Consultation",
            existingConsent = validConsent
        )
        assertTrue(authorizedAttempt.isSuccess)

        val updatedLogs = securityManager.getAuditLogsForPatient(patient.uid)
        assertEquals(2, updatedLogs.size)
        assertTrue(updatedLogs.last().isGranted)
    }

    @Test
    fun testHealthPassportQRCode_neverExposesRawDataAndGeneratesDynamicTicket() {
        val qr = HealthPassportQRCode(
            patientUid = "patient_1",
            healthId = "HG-8921-US"
        )
        assertNotNull(qr.sessionTicket)
        assertTrue(qr.sessionTicket.length >= 32)
        assertFalse(qr.isTicketExpired)
    }

    @Test
    fun testDeviceLimitManager_enforcesFourDeviceLimitForNormalAccounts() {
        val deviceManager = DeviceManager()
        val userId = "user_normal_1"

        // Register 4 devices (allowed)
        for (i in 1..4) {
            val result = deviceManager.registerDevice(
                userId = userId,
                accountType = AccountType.INDIVIDUAL,
                deviceName = "Android Device $i"
            )
            assertTrue("Device $i should succeed", result.isSuccess)
        }

        // 5th device must fail
        val fifthDevice = deviceManager.registerDevice(
            userId = userId,
            accountType = AccountType.INDIVIDUAL,
            deviceName = "Tablet Device 5"
        )
        assertTrue(fifthDevice.isFailure)
        assertTrue(fifthDevice.exceptionOrNull()!!.message!!.contains("Device Limit Exceeded"))

        // Revoking 1 device frees up a slot
        val active = deviceManager.getActiveDevices(userId)
        assertEquals(4, active.size)
        val revoked = deviceManager.revokeDevice(userId, active.first().deviceId)
        assertTrue(revoked)

        val newFifthDevice = deviceManager.registerDevice(
            userId = userId,
            accountType = AccountType.INDIVIDUAL,
            deviceName = "New Replacement Phone"
        )
        assertTrue("New device after revocation should succeed", newFifthDevice.isSuccess)
    }

    @Test
    fun testDevicePermissions_healthcareOrgAdminCanConfigureDeviceFeatures() {
        val deviceManager = DeviceManager()
        val orgUserId = "hospital_org_1"

        val device = deviceManager.registerDevice(
            userId = orgUserId,
            accountType = AccountType.HOSPITAL,
            deviceName = "Nurse Station Terminal 1",
            packageTier = SubscriptionPackage.PREMIUM_ORGANIZATION,
            initialPermissions = DevicePermissions(
                organizationManagement = true,
                socialContent = false,
                marketplace = false,
                messages = false,
                calls = false
            )
        ).getOrThrow()

        assertFalse(device.permissions.socialContent)
        assertFalse(device.permissions.marketplace)
        assertTrue(device.permissions.organizationManagement)

        // Update permissions
        val updated = deviceManager.updateDevicePermissions(
            userId = orgUserId,
            deviceId = device.deviceId,
            newPermissions = device.permissions.copy(messages = true)
        ).getOrThrow()

        assertTrue(updated.permissions.messages)
        assertFalse(updated.permissions.socialContent)
    }

    @Test
    fun testVerificationEngine_requiresMandatoryDocsPerCategory() {
        val engine = CountryVerificationEngine()

        val doctorDocs = engine.getRequirements(AccountType.DOCTOR, "US")
        assertTrue(doctorDocs.any { it.documentType == DocumentType.MEDICAL_LICENSE })
        assertTrue(doctorDocs.any { it.documentType == DocumentType.NATIONAL_ID })

        val validDocs = setOf(DocumentType.NATIONAL_ID, DocumentType.MEDICAL_LICENSE)
        assertTrue(engine.validateSubmission(AccountType.DOCTOR, "US", validDocs))

        val incompleteDocs = setOf(DocumentType.NATIONAL_ID)
        assertFalse(engine.validateSubmission(AccountType.DOCTOR, "US", incompleteDocs))
    }

    @Test
    fun testPaymentAbstraction_calculatesSplitAndRoutesCountry() {
        val paymentService = PaymentAbstractionService()
        val split = paymentService.calculateSplit(amount = 100.0, commissionRate = 0.05)
        assertEquals(5.0, split.first, 0.01) // Platform fee
        assertEquals(3.20, split.second, 0.01) // 2.9% + 0.30 provider fee
        assertEquals(91.80, split.third, 0.01) // Net merchant payout

        val saudiConfig = paymentService.getCountryConfiguration("SA")
        assertEquals("SAR", saudiConfig.defaultCurrency)
        assertEquals(15.0, saudiConfig.vatRatePercentage, 0.01)
    }

    @Test
    fun testOwnerControl_emergencyKillSwitchOverridesEverything() {
        val ownerEngine = OwnerControlEngine()
        assertTrue(ownerEngine.isFeatureAvailable(PlatformFeature.SOCIAL_FEED, "US", AccountType.INDIVIDUAL))

        ownerEngine.setEmergencyKillSwitch(true)
        assertFalse(ownerEngine.isFeatureAvailable(PlatformFeature.SOCIAL_FEED, "US", AccountType.INDIVIDUAL))
        assertFalse(ownerEngine.isFeatureAvailable(PlatformFeature.HEALTH_PASSPORT_CORE, "US", AccountType.INDIVIDUAL))

        ownerEngine.setEmergencyKillSwitch(false)
        assertTrue(ownerEngine.isFeatureAvailable(PlatformFeature.SOCIAL_FEED, "US", AccountType.INDIVIDUAL))
    }

    @Test
    fun testOwnerLedger_recordsTransactionsAndDerivesBalances() {
        val ownerEngine = OwnerControlEngine()
        ownerEngine.recordLedgerTransaction(
            type = LedgerType.MARKETPLACE_SALE,
            gross = 200.0,
            commission = 10.0,
            serviceFee = 2.0,
            processingFee = 6.10,
            tax = 0.0,
            country = "US",
            reference = "order_1234"
        )

        val summary = ownerEngine.calculateEarningsSummary()
        assertEquals(200.0, summary.grossPlatformVolume, 0.01)
        assertEquals(10.0, summary.totalCommissionsEarned, 0.01)
        assertEquals(2.0, summary.totalServiceFeesEarned, 0.01)
        assertTrue(summary.netAvailableBalance > 0.0)
    }
}
