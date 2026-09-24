package com.example.healthogram

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.profile.model.*
import com.example.healthogram.profile.permissions.FeatureKey
import com.example.healthogram.profile.permissions.FeaturePermissionEngine
import com.example.healthogram.profile.repository.ProfileRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class HealthogramProfileAndPermissionsTest {

    private lateinit var repository: ProfileRepository

    @Before
    fun setUp() {
        repository = ProfileRepository.getInstance()
    }

    @Test
    fun testFeaturePermissions_individualAccountCapabilities() {
        val individual = User(
            uid = "test_user_indiv",
            email = "indiv@test.com",
            displayName = "Alex Mercer",
            username = "alexmercer",
            accountType = AccountType.INDIVIDUAL,
            isVerified = false,
            isMarketplaceEnabled = true
        )

        // Allowed
        assertTrue(FeaturePermissionEngine.hasPermission(individual, FeatureKey.CREATE_POST))
        assertTrue(FeaturePermissionEngine.hasPermission(individual, FeatureKey.MESSAGE))
        assertTrue(FeaturePermissionEngine.hasPermission(individual, FeatureKey.HEALTH_PASSPORT_OWN))
        assertTrue(FeaturePermissionEngine.hasPermission(individual, FeatureKey.MARKETPLACE))

        // Disallowed
        assertFalse(FeaturePermissionEngine.hasPermission(individual, FeatureKey.HEALTH_PASSPORT_SCAN))
        assertFalse(FeaturePermissionEngine.hasPermission(individual, FeatureKey.CLINIC_MANAGEMENT))
        assertFalse(FeaturePermissionEngine.hasPermission(individual, FeatureKey.HOSPITAL_MANAGEMENT))
        assertFalse(FeaturePermissionEngine.hasPermission(individual, FeatureKey.LABORATORY_MANAGEMENT))
        assertFalse(FeaturePermissionEngine.hasPermission(individual, FeatureKey.CONSULTATIONS))
    }

    @Test
    fun testFeaturePermissions_healthPassportScannerRestrictedToVerifiedClinicalAccounts() {
        val unverifiedDoctor = User(
            uid = "doc_unverified",
            email = "doc@test.com",
            displayName = "Dr. Unverified",
            username = "drunverified",
            accountType = AccountType.DOCTOR,
            isVerified = false,
            verificationStatus = VerificationStatus.UNDER_REVIEW
        )

        val verifiedDoctor = User(
            uid = "doc_verified",
            email = "doc_v@test.com",
            displayName = "Dr. Verified",
            username = "drverified",
            accountType = AccountType.DOCTOR,
            isVerified = true,
            verificationStatus = VerificationStatus.APPROVED
        )

        val verifiedIndividual = User(
            uid = "indiv_verified",
            email = "indiv_v@test.com",
            displayName = "Verified Individual",
            username = "indivv",
            accountType = AccountType.INDIVIDUAL,
            isVerified = true,
            verificationStatus = VerificationStatus.APPROVED
        )

        // Unverified doctor CANNOT scan passports
        assertFalse(FeaturePermissionEngine.hasPermission(unverifiedDoctor, FeatureKey.HEALTH_PASSPORT_SCAN, isVerified = false, scannerPermission = true))

        // Verified doctor CAN scan passports when scanner permission is granted
        assertTrue(FeaturePermissionEngine.hasPermission(verifiedDoctor, FeatureKey.HEALTH_PASSPORT_SCAN, isVerified = true, scannerPermission = true))

        // Verified individual still CANNOT scan passports (only licensed clinical practitioners can scan)
        assertFalse(FeaturePermissionEngine.hasPermission(verifiedIndividual, FeatureKey.HEALTH_PASSPORT_SCAN, isVerified = true, scannerPermission = true))
    }

    @Test
    fun testFeaturePermissions_laboratoryCapabilitiesAndNoPersonalPassport() {
        val labUser = User(
            uid = "lab_user_1",
            email = "lab@apex.com",
            displayName = "Apex Diagnostics",
            username = "apexlab",
            accountType = AccountType.LABORATORY,
            isVerified = true,
            verificationStatus = VerificationStatus.APPROVED
        )

        // Allowed
        assertTrue(FeaturePermissionEngine.hasPermission(labUser, FeatureKey.LABORATORY_MANAGEMENT))
        assertTrue(FeaturePermissionEngine.hasPermission(labUser, FeatureKey.TEST_MANAGEMENT))
        assertTrue(FeaturePermissionEngine.hasPermission(labUser, FeatureKey.HEALTH_PASSPORT_SCAN, isVerified = true, scannerPermission = true))

        // Strict rule: Laboratory accounts manage diagnostic test catalogs and do NOT own a personal patient passport
        assertFalse(FeaturePermissionEngine.hasPermission(labUser, FeatureKey.HEALTH_PASSPORT_OWN))
    }

    @Test
    fun testProfileRepository_dataSeparationAndAccess() = runBlocking {
        val uid = "user_separation_test"

        val publicProfile = PublicProfile(
            uid = uid,
            username = "sepuser",
            displayName = "Separation User",
            accountType = AccountType.INDIVIDUAL,
            bio = "Public bio content",
            isVerified = false
        )

        val privateProfile = PrivateProfile(
            uid = uid,
            privateEmail = "private_email@test.com",
            privatePhone = "+1999888777",
            securityPreferences = SecurityPreferences(twoFactorEnabled = true)
        )

        repository.savePublicProfile(publicProfile)
        repository.savePrivateProfile(privateProfile)

        val fetchedPublic = repository.getPublicProfile(uid)
        val fetchedPrivate = repository.getPrivateProfile(uid)

        assertNotNull(fetchedPublic)
        assertNotNull(fetchedPrivate)

        // Confirm fields are strictly separated: public document doesn't contain private email/phone
        assertEquals("sepuser", fetchedPublic!!.username)
        assertEquals("private_email@test.com", fetchedPrivate!!.privateEmail)
        assertEquals("+1999888777", fetchedPrivate.privatePhone)
        assertTrue(fetchedPrivate.securityPreferences.twoFactorEnabled)
    }

    @Test
    fun testProfileRepository_defensiveCopyingPreventsClientSelfVerification() = runBlocking {
        val uid = "user_tamper_test"

        // Initialize unverified profile
        val initialProfile = PublicProfile(
            uid = uid,
            username = "tamperuser",
            displayName = "Tamper User",
            accountType = AccountType.INDIVIDUAL,
            isVerified = false
        )
        repository.savePublicProfile(initialProfile)

        // Attempt client-side tampering: setting isVerified = true in savePublicProfile
        val maliciousAttempt = initialProfile.copy(isVerified = true, bio = "Updated Bio")
        repository.savePublicProfile(maliciousAttempt)

        val retrieved = repository.getPublicProfile(uid)
        assertNotNull(retrieved)
        assertEquals("Updated Bio", retrieved!!.bio)
        // Defensive check preserved original isVerified = false
        assertFalse(retrieved.isVerified)
    }

    @Test
    fun testProfileRepository_followAndUnfollowOperations() = runBlocking {
        val followerUid = "user_follower_1"
        val targetUid = "user_target_1"

        // Pre-save target profile so followers count can be incremented and verified
        val targetInitial = PublicProfile(
            uid = targetUid,
            username = "target_user",
            displayName = "Target User",
            accountType = AccountType.INDIVIDUAL,
            followersCount = 0L
        )
        repository.savePublicProfile(targetInitial)

        assertFalse(repository.isFollowing(followerUid, targetUid))

        repository.followUser(followerUid, targetUid)
        assertTrue(repository.isFollowing(followerUid, targetUid))

        val targetProfile = repository.getPublicProfile(targetUid)
        assertNotNull(targetProfile)
        assertTrue(targetProfile!!.followersCount > 0)

        repository.unfollowUser(followerUid, targetUid)
        assertFalse(repository.isFollowing(followerUid, targetUid))
    }

    @Test
    fun testProfileRepository_organizationMembersAndRoleValidation() = runBlocking {
        val orgId = "org_clinic_demo"
        val membersBefore = repository.getOrganizationMembers(orgId)
        assertTrue(membersBefore.isNotEmpty())

        val newStaff = OrganizationMember(
            membershipId = "mem_test_99",
            organizationId = orgId,
            uid = "user_test_staff",
            displayName = "Nurse Sarah Jenkins",
            email = "sarah.j@clinic.com",
            role = OrganizationRole.STAFF
        )

        repository.addOrganizationMember(newStaff)

        val membersAfter = repository.getOrganizationMembers(orgId)
        assertTrue(membersAfter.any { it.membershipId == "mem_test_99" })

        repository.removeOrganizationMember(orgId, "user_test_staff")
        val membersFinal = repository.getOrganizationMembers(orgId)
        assertFalse(membersFinal.any { it.membershipId == "mem_test_99" })
    }

    @Test
    fun testProfileRepository_organizationDevicePermissions() = runBlocking {
        val orgId = "org_clinic_demo"
        val devices = repository.getOrganizationDevices(orgId)
        assertTrue(devices.isNotEmpty())

        val targetDev = devices.first()
        val customPerms = targetDev.devicePermissions.copy(
            socialMedia = false,
            aiStudio = true,
            consultations = true
        )

        repository.updateOrganizationDevicePermissions(orgId, targetDev.deviceId, customPerms)

        val updatedDevices = repository.getOrganizationDevices(orgId)
        val updatedDev = updatedDevices.first { it.deviceId == targetDev.deviceId }
        assertFalse(updatedDev.devicePermissions.socialMedia)
        assertTrue(updatedDev.devicePermissions.aiStudio)
        assertTrue(updatedDev.devicePermissions.consultations)
    }
}
