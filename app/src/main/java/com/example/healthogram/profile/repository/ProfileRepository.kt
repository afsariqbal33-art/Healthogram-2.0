package com.example.healthogram.profile.repository

import android.content.Context
import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.organization.HospitalDepartment
import com.example.healthogram.organization.LabTestItem
import com.example.healthogram.profile.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Healthogram Profile Repository.
 *
 * Implements clean architectural separation between:
 * 1. Public Profiles (`public_profiles/{uid}`)
 * 2. Private Profiles (`private_profiles/{uid}`)
 * 3. Professional Profiles (`professional_profiles/{uid}`)
 * 4. Healthcare Organizations (`organizations/{organizationId}`)
 * 5. Verification Profiles (`verification_profiles/{uid}`)
 * 6. Follow relationships (`users/{uid}/following/{targetUid}`)
 */
class ProfileRepository(
    private val context: Context? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        const val COLLECTION_PUBLIC_PROFILES = "public_profiles"
        const val COLLECTION_PRIVATE_PROFILES = "private_profiles"
        const val COLLECTION_PROFESSIONAL_PROFILES = "professional_profiles"
        const val COLLECTION_ORGANIZATIONS = "organizations"
        const val COLLECTION_VERIFICATIONS = "verification_profiles"
        const val COLLECTION_FOLLOWING = "following"
        const val COLLECTION_FOLLOWERS = "followers"
        const val COLLECTION_BLOCKED = "blocked"
        const val COLLECTION_REPORTS = "profile_reports"

        @Volatile
        private var instance: ProfileRepository? = null

        fun getInstance(context: Context? = null): ProfileRepository {
            return instance ?: synchronized(this) {
                instance ?: ProfileRepository(context?.applicationContext).also { instance = it }
            }
        }
    }

    // In-memory fallback and test store
    private val localPublicProfiles = mutableMapOf<String, PublicProfile>()
    private val localPrivateProfiles = mutableMapOf<String, PrivateProfile>()
    private val localProfessionalProfiles = mutableMapOf<String, ProfessionalProfile>()
    private val localOrganizations = mutableMapOf<String, OrganizationProfile>()
    private val localOrganizationMembers = mutableMapOf<String, MutableList<OrganizationMember>>() // orgId -> members
    private val localOrganizationDevices = mutableMapOf<String, MutableList<OrganizationDevice>>() // orgId -> devices
    private val localVerificationProfiles = mutableMapOf<String, VerificationProfile>()
    private val localFollowing = mutableMapOf<String, MutableSet<String>>() // uid -> set of targetUids
    private val localFollowers = mutableMapOf<String, MutableSet<String>>() // targetUid -> set of followerUids
    private val localBlocked = mutableMapOf<String, MutableSet<String>>() // uid -> set of blockedUids
    private val localReports = mutableListOf<ProfileReport>()

    private val isFirebaseAvailable: Boolean
        get() {
            return try {
                val ctx = context ?: return false
                FirebaseApp.getApps(ctx).isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }

    private val firestore: FirebaseFirestore?
        get() = if (isFirebaseAvailable) FirebaseFirestore.getInstance() else null

    init {
        seedDefaultTestData()
    }

    /**
     * Seeds realistic, professional demo profiles for each of the 5 allowed categories.
     */
    private fun seedDefaultTestData() {
        // 1. Individual: Alex Mercer
        val indUid = "user_individual_demo"
        localPublicProfiles[indUid] = PublicProfile(
            uid = indUid,
            username = "alex_mercer",
            normalizedUsername = "alex_mercer",
            displayName = "Alex Mercer",
            bio = "🏃‍♂️ Marathon runner & health tech enthusiast.\n📊 Tracking daily vitals & recovery with Healthogram.",
            accountType = AccountType.INDIVIDUAL,
            countryCode = "US",
            city = "San Francisco",
            isPrivateAccount = false,
            isProfessional = false,
            isVerified = true,
            followersCount = 2430L,
            followingCount = 318L,
            postsCount = 48L,
            reelsCount = 12L
        )
        localPrivateProfiles[indUid] = PrivateProfile(
            uid = indUid,
            dateOfBirth = "1994-06-15",
            gender = "Male",
            privateEmail = "alex.mercer@healthogram.test",
            privatePhone = "+14155550192",
            address = "742 Evergreen Terrace",
            postalCode = "94102",
            privacySettings = ProfilePrivacySettings(isPrivateAccount = false)
        )

        // 2. Doctor: Dr. Elena Rostova
        val docUid = "user_doctor_demo"
        localPublicProfiles[docUid] = PublicProfile(
            uid = docUid,
            username = "dr_elena_cardio",
            normalizedUsername = "dr_elena_cardio",
            displayName = "Dr. Elena Rostova, MD",
            bio = "Cardiothoracic Specialist & Preventive Heart Health.\nBoard Certified • Stanford Medical Alumni 🩺",
            accountType = AccountType.DOCTOR,
            countryCode = "US",
            city = "Palo Alto",
            website = "https://dr-elena-heart.healthogram.me",
            publicEmail = "contact@drelena.health",
            isPrivateAccount = false,
            isProfessional = true,
            isVerified = true,
            followersCount = 18400L,
            followingCount = 142L,
            postsCount = 96L
        )
        localProfessionalProfiles[docUid] = ProfessionalProfile(
            uid = docUid,
            accountType = AccountType.DOCTOR,
            professionalTitle = "Consultant Cardiologist",
            professionalBio = "Over 14 years specializing in non-invasive cardiovascular imaging, hypertension therapy, and lifestyle lipid optimization.",
            specializations = listOf("Cardiovascular Medicine", "Echocardiography", "Preventive Cardiology", "Lipidology"),
            education = listOf("MD - Stanford School of Medicine", "Fellowship - Mayo Clinic Cardiology", "BS - UC Berkeley"),
            experienceYears = 14,
            languages = listOf("English", "Spanish", "Russian"),
            defaultConsultationFee = 120.0,
            currency = "USD",
            businessHours = "Mon-Thu: 08:30 - 16:30",
            countryCode = "US",
            city = "Palo Alto",
            scannerPermission = true,
            services = listOf(
                ProfessionalServiceItem("srv_doc_1", "Comprehensive Heart Health Evaluation", "Full review of ECG, lipid panels, and lifestyle assessment", 45, 150.0, "USD", true),
                ProfessionalServiceItem("srv_doc_2", "Follow-up Cardiology Teleconsultation", "Review vitals and medication tolerance", 30, 95.0, "USD", true),
                ProfessionalServiceItem("srv_doc_3", "Second Opinion on Diagnostic Angiograms", "Detailed independent diagnostic assessment", 60, 220.0, "USD", false)
            )
        )
        localVerificationProfiles[docUid] = VerificationProfile(
            uid = docUid,
            accountType = AccountType.DOCTOR,
            countryCode = "US",
            status = VerificationStatus.APPROVED,
            scannerPermission = true,
            reviewedBy = "Healthogram Medical Review Board #410"
        )

        // 3. Clinic: Horizon Multispecialty Clinic
        val clinicId = "org_clinic_demo"
        localOrganizations[clinicId] = OrganizationProfile(
            organizationId = clinicId,
            ownerUid = "owner_clinic_01",
            organizationType = AccountType.CLINIC,
            name = "Horizon Multispecialty Clinic",
            username = "horizon_clinic",
            description = "State-of-the-art outpatient family and diagnostic care facility with 16 resident specialists.",
            countryCode = "US",
            city = "Austin",
            address = "1200 Congress Ave, Suite 400, Austin, TX",
            contactPhone = "+15125550188",
            contactEmail = "reception@horizonclinic.com",
            website = "https://horizonclinic.healthogram.me",
            workingHours = "Mon-Sat: 08:00 - 20:00",
            isVerified = true,
            verificationStatus = VerificationStatus.APPROVED,
            scannerPermission = true,
            followersCount = 5820L,
            services = listOf(
                ProfessionalServiceItem("cl_srv_1", "Pediatric Wellness Exam", "Routine childhood immunization and growth assessment", 30, 80.0, "USD", false),
                ProfessionalServiceItem("cl_srv_2", "General Internal Medicine Visit", "Comprehensive exam for acute and chronic conditions", 30, 90.0, "USD", true)
            )
        )
        localOrganizationMembers[clinicId] = mutableListOf(
            OrganizationMember("mem_1", clinicId, "owner_clinic_01", "Dr. Marcus Vance", "marcus@horizonclinic.com", OrganizationRole.OWNER),
            OrganizationMember("mem_2", clinicId, "admin_clinic_01", "Sarah Jenkins", "sarah@horizonclinic.com", OrganizationRole.ADMIN),
            OrganizationMember("mem_3", clinicId, docUid, "Dr. Elena Rostova", "contact@drelena.health", OrganizationRole.DOCTOR)
        )
        localOrganizationDevices[clinicId] = mutableListOf(
            OrganizationDevice("dev_cl_1", clinicId, "Reception Terminal iPad Pro", "iOS 17.4", "192.168.1.50", OrganizationDevicePermissions(management = true, appointments = true)),
            OrganizationDevice("dev_cl_2", clinicId, "Triage Room Tablet", "Android 14", "192.168.1.51", OrganizationDevicePermissions(appointments = true, textMessaging = true))
        )

        // 4. Hospital: St. Jude Metropolitan Hospital
        val hospId = "org_hospital_demo"
        localOrganizations[hospId] = OrganizationProfile(
            organizationId = hospId,
            ownerUid = "owner_hosp_01",
            organizationType = AccountType.HOSPITAL,
            name = "Metropolitan Central Hospital",
            username = "metropolitan_hospital",
            description = "Tertiary academic medical center featuring Level 1 Trauma Care, Robotic Cardiac Surgery, and Pediatric Intensive Care.",
            countryCode = "US",
            city = "Chicago",
            address = "500 Lake Shore Drive, Chicago, IL",
            contactPhone = "+13125550100",
            contactEmail = "admin@metropolitanhealth.org",
            website = "https://metropolitanhospital.org",
            workingHours = "24/7 Emergency & Inpatient Services",
            isVerified = true,
            verificationStatus = VerificationStatus.APPROVED,
            scannerPermission = true,
            maxDevices = 8,
            subscriptionPlan = "Enterprise 8-Device",
            followersCount = 34500L,
            departments = listOf(
                HospitalDepartment("dept_1", "Cardiovascular & Thoracic Surgery", "Dr. Anthony Weber", 18),
                HospitalDepartment("dept_2", "Emergency & Critical Trauma Center", "Dr. Lisa Chang", 28),
                HospitalDepartment("dept_3", "Neurology & Stroke Center", "Dr. Tariq Al-Mansoor", 14),
                HospitalDepartment("dept_4", "Oncology & Cellular Therapeutics", "Dr. Rebecca Miller", 22)
            ),
            services = listOf(
                ProfessionalServiceItem("hosp_srv_1", "Emergency Trauma Admission", "24/7 Rapid Response & Intensive Resuscitation", 0, 0.0, "USD", false),
                ProfessionalServiceItem("hosp_srv_2", "Robotic Cardiac Consultation", "Surgeon assessment for minimally invasive intervention", 60, 350.0, "USD", true)
            )
        )
        localOrganizationMembers[hospId] = mutableListOf(
            OrganizationMember("hosp_mem_1", hospId, "owner_hosp_01", "Arthur Sterling (CEO)", "ceo@metropolitanhealth.org", OrganizationRole.OWNER),
            OrganizationMember("hosp_mem_2", hospId, "admin_hosp_01", "Dr. Lisa Chang (Chief Medical Officer)", "cmo@metropolitanhealth.org", OrganizationRole.ADMIN)
        )
        localOrganizationDevices[hospId] = mutableListOf(
            OrganizationDevice("hosp_dev_1", hospId, "ER Triage Station A", "Android 14", "10.0.1.12", OrganizationDevicePermissions(management = true, appointments = true, textMessaging = true)),
            OrganizationDevice("hosp_dev_2", hospId, "Surgical ICU Terminal 4", "Android 14", "10.0.1.18", OrganizationDevicePermissions(management = true, consultations = true)),
            OrganizationDevice("dev_hosp_3", hospId, "Outpatient Booking Kiosk", "Android 14", "10.0.2.5", OrganizationDevicePermissions(appointments = true, socialMedia = false)),
            OrganizationDevice("dev_hosp_4", hospId, "Executive Medical Suite", "Android 14", "10.0.3.9", OrganizationDevicePermissions(management = true, aiStudio = true))
        )

        // 5. Laboratory: Apex Diagnostic & Pathology Laboratories
        val labId = "org_lab_demo"
        localOrganizations[labId] = OrganizationProfile(
            organizationId = labId,
            ownerUid = "owner_lab_01",
            organizationType = AccountType.LABORATORY,
            name = "Apex Precision Diagnostics",
            username = "apex_labs",
            description = "CAP-accredited clinical molecular pathology and diagnostic testing center.",
            countryCode = "US",
            city = "Boston",
            address = "750 Innovation Way, Cambridge, MA",
            contactPhone = "+16175550144",
            contactEmail = "specimens@apexlabs.com",
            website = "https://apexdiagnostics.healthogram.me",
            workingHours = "Mon-Sun: 07:00 - 21:00",
            isVerified = true,
            verificationStatus = VerificationStatus.APPROVED,
            scannerPermission = true,
            followersCount = 4210L,
            testCatalog = listOf(
                LabTestItem("LAB-CBC-01", "Complete Blood Count (CBC) with Differential", "Whole Blood (EDTA)", 4, 28.0, "USD", false),
                LabTestItem("LAB-CMP-02", "Comprehensive Metabolic Panel (CMP 14)", "Serum Gel", 6, 42.0, "USD", true),
                LabTestItem("LAB-LIP-03", "Lipid & Apolipoprotein Panel (ApoB/LDL-P)", "Serum", 8, 49.0, "USD", true),
                LabTestItem("LAB-HBA1C-04", "Glycated Hemoglobin (HbA1c)", "Whole Blood", 4, 32.0, "USD", false),
                LabTestItem("LAB-PCR-05", "Respiratory Pathogen Molecular Multiplex Panel", "Nasopharyngeal Swab", 12, 110.0, "USD", false)
            )
        )
        localOrganizationMembers[labId] = mutableListOf(
            OrganizationMember("lab_mem_1", labId, "owner_lab_01", "Dr. Kenneth Huang, PhD", "khuang@apexlabs.com", OrganizationRole.OWNER),
            OrganizationMember("lab_mem_2", labId, "staff_lab_01", "Maria Gomez (Lead Technologist)", "mgomez@apexlabs.com", OrganizationRole.STAFF)
        )
    }

    // =========================================================================
    // PUBLIC PROFILE OPERATIONS
    // =========================================================================

    suspend fun getPublicProfile(uid: String): PublicProfile? = withContext(ioDispatcher) {
        if (firestore != null) {
            try {
                val doc = firestore!!.collection(COLLECTION_PUBLIC_PROFILES).document(uid).get().await()
                if (doc.exists() && doc.data != null) {
                    return@withContext PublicProfile.fromFirestoreMap(doc.data!!)
                }
            } catch (_: Exception) {
                // Fallback to local store
            }
        }
        return@withContext localPublicProfiles[uid]
    }

    suspend fun savePublicProfile(profile: PublicProfile): Result<Unit> = withContext(ioDispatcher) {
        // Enforce account rules: PHARMACY is forbidden
        if (!AccountType.isAllowed(profile.accountType.name)) {
            return@withContext Result.failure(SecurityException("Disallowed account category ${profile.accountType.name}"))
        }

        val existing = localPublicProfiles[profile.uid]
        // Prevent unauthorized client-side tampering of verified status or followers count
        val sanitized = profile.copy(
            isVerified = existing?.isVerified ?: profile.isVerified,
            followersCount = existing?.followersCount ?: profile.followersCount,
            followingCount = existing?.followingCount ?: profile.followingCount,
            updatedAt = System.currentTimeMillis()
        )

        localPublicProfiles[sanitized.uid] = sanitized

        if (firestore != null) {
            try {
                firestore!!.collection(COLLECTION_PUBLIC_PROFILES)
                    .document(sanitized.uid)
                    .set(sanitized.toFirestoreMap(), SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
        Result.success(Unit)
    }

    // =========================================================================
    // PRIVATE PROFILE OPERATIONS
    // =========================================================================

    suspend fun getPrivateProfile(uid: String): PrivateProfile? = withContext(ioDispatcher) {
        if (firestore != null) {
            try {
                val doc = firestore!!.collection(COLLECTION_PRIVATE_PROFILES).document(uid).get().await()
                if (doc.exists() && doc.data != null) {
                    return@withContext PrivateProfile.fromFirestoreMap(doc.data!!)
                }
            } catch (_: Exception) {
                // Fallback
            }
        }
        return@withContext localPrivateProfiles[uid]
    }

    suspend fun savePrivateProfile(profile: PrivateProfile): Result<Unit> = withContext(ioDispatcher) {
        val updated = profile.copy(updatedAt = System.currentTimeMillis())
        localPrivateProfiles[updated.uid] = updated

        if (firestore != null) {
            try {
                firestore!!.collection(COLLECTION_PRIVATE_PROFILES)
                    .document(updated.uid)
                    .set(updated.toFirestoreMap(), SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
        Result.success(Unit)
    }

    // =========================================================================
    // PROFESSIONAL PROFILE OPERATIONS
    // =========================================================================

    suspend fun getProfessionalProfile(uid: String): ProfessionalProfile? = withContext(ioDispatcher) {
        if (firestore != null) {
            try {
                val doc = firestore!!.collection(COLLECTION_PROFESSIONAL_PROFILES).document(uid).get().await()
                if (doc.exists() && doc.data != null) {
                    return@withContext ProfessionalProfile.fromFirestoreMap(doc.data!!)
                }
            } catch (_: Exception) {
                // Fallback
            }
        }
        return@withContext localProfessionalProfiles[uid]
    }

    suspend fun saveProfessionalProfile(profile: ProfessionalProfile): Result<Unit> = withContext(ioDispatcher) {
        if (!AccountType.isAllowed(profile.accountType.name)) {
            return@withContext Result.failure(SecurityException("Disallowed account type"))
        }

        val existing = localProfessionalProfiles[profile.uid]
        // Client cannot self-activate scanner permission
        val sanitized = profile.copy(
            scannerPermission = existing?.scannerPermission ?: profile.scannerPermission,
            updatedAt = System.currentTimeMillis()
        )

        localProfessionalProfiles[sanitized.uid] = sanitized

        if (firestore != null) {
            try {
                firestore!!.collection(COLLECTION_PROFESSIONAL_PROFILES)
                    .document(sanitized.uid)
                    .set(sanitized.toFirestoreMap(), SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
        Result.success(Unit)
    }

    // =========================================================================
    // HEALTHCARE ORGANIZATION OPERATIONS
    // =========================================================================

    suspend fun getOrganizationProfile(orgId: String): OrganizationProfile? = withContext(ioDispatcher) {
        if (firestore != null) {
            try {
                val doc = firestore!!.collection(COLLECTION_ORGANIZATIONS).document(orgId).get().await()
                if (doc.exists() && doc.data != null) {
                    return@withContext OrganizationProfile.fromFirestoreMap(doc.data!!)
                }
            } catch (_: Exception) {
                // Fallback
            }
        }
        return@withContext localOrganizations[orgId]
    }

    suspend fun saveOrganizationProfile(profile: OrganizationProfile): Result<Unit> = withContext(ioDispatcher) {
        if (!profile.organizationType.isHealthcareOrganization) {
            return@withContext Result.failure(IllegalArgumentException("Not a healthcare organization"))
        }

        val existing = localOrganizations[profile.organizationId]
        // Clients cannot elevate their verification status, maxDevices, or subscriptionPlan arbitrarily
        val sanitized = profile.copy(
            isVerified = existing?.isVerified ?: profile.isVerified,
            verificationStatus = existing?.verificationStatus ?: profile.verificationStatus,
            scannerPermission = existing?.scannerPermission ?: profile.scannerPermission,
            maxDevices = existing?.maxDevices ?: profile.maxDevices,
            subscriptionPlan = existing?.subscriptionPlan ?: profile.subscriptionPlan,
            updatedAt = System.currentTimeMillis()
        )

        localOrganizations[sanitized.organizationId] = sanitized

        if (firestore != null) {
            try {
                firestore!!.collection(COLLECTION_ORGANIZATIONS)
                    .document(sanitized.organizationId)
                    .set(sanitized.toFirestoreMap(), SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
        Result.success(Unit)
    }

    suspend fun getOrganizationMembers(orgId: String): List<OrganizationMember> = withContext(ioDispatcher) {
        localOrganizationMembers[orgId]?.toList() ?: emptyList()
    }

    suspend fun addOrganizationMember(member: OrganizationMember): Result<Unit> = withContext(ioDispatcher) {
        val list = localOrganizationMembers.getOrPut(member.organizationId) { mutableListOf() }
        list.removeAll { it.uid == member.uid }
        list.add(member)
        Result.success(Unit)
    }

    suspend fun removeOrganizationMember(orgId: String, uid: String): Result<Unit> = withContext(ioDispatcher) {
        localOrganizationMembers[orgId]?.removeAll { it.uid == uid }
        Result.success(Unit)
    }

    suspend fun getOrganizationDevices(orgId: String): List<OrganizationDevice> = withContext(ioDispatcher) {
        localOrganizationDevices[orgId]?.toList() ?: emptyList()
    }

    suspend fun updateOrganizationDevicePermissions(
        orgId: String,
        deviceId: String,
        permissions: OrganizationDevicePermissions
    ): Result<Unit> = withContext(ioDispatcher) {
        val list = localOrganizationDevices[orgId] ?: return@withContext Result.failure(NoSuchElementException("No devices"))
        val index = list.indexOfFirst { it.deviceId == deviceId }
        if (index >= 0) {
            val updated = list[index].copy(devicePermissions = permissions, lastActiveAt = System.currentTimeMillis())
            list[index] = updated
            Result.success(Unit)
        } else {
            Result.failure(NoSuchElementException("Device not found"))
        }
    }

    // =========================================================================
    // VERIFICATION PROFILE OPERATIONS
    // =========================================================================

    suspend fun getVerificationProfile(uid: String): VerificationProfile? = withContext(ioDispatcher) {
        if (firestore != null) {
            try {
                val doc = firestore!!.collection(COLLECTION_VERIFICATIONS).document(uid).get().await()
                if (doc.exists() && doc.data != null) {
                    return@withContext VerificationProfile.fromFirestoreMap(doc.data!!)
                }
            } catch (_: Exception) {
                // Fallback
            }
        }
        return@withContext localVerificationProfiles[uid]
    }

    // =========================================================================
    // SOCIAL RELATIONSHIP OPERATIONS (FOLLOW / UNFOLLOW / BLOCK / REPORT)
    // =========================================================================

    suspend fun isFollowing(followerUid: String, targetUid: String): Boolean = withContext(ioDispatcher) {
        val set = localFollowing[followerUid]
        set?.contains(targetUid) == true
    }

    suspend fun followUser(followerUid: String, targetUid: String): Result<Boolean> = withContext(ioDispatcher) {
        if (followerUid == targetUid) {
            return@withContext Result.failure(IllegalArgumentException("Users cannot follow themselves"))
        }

        // Check if target has blocked follower
        if (localBlocked[targetUid]?.contains(followerUid) == true) {
            return@withContext Result.failure(SecurityException("Unable to follow account"))
        }

        val followingSet = localFollowing.getOrPut(followerUid) { mutableSetOf() }
        followingSet.add(targetUid)

        val followerSet = localFollowers.getOrPut(targetUid) { mutableSetOf() }
        followerSet.add(followerUid)

        // Increment target's followersCount
        localPublicProfiles[targetUid]?.let {
            localPublicProfiles[targetUid] = it.copy(followersCount = it.followersCount + 1)
        }
        // Increment follower's followingCount
        localPublicProfiles[followerUid]?.let {
            localPublicProfiles[followerUid] = it.copy(followingCount = it.followingCount + 1)
        }

        Result.success(true)
    }

    suspend fun unfollowUser(followerUid: String, targetUid: String): Result<Boolean> = withContext(ioDispatcher) {
        localFollowing[followerUid]?.remove(targetUid)
        localFollowers[targetUid]?.remove(followerUid)

        localPublicProfiles[targetUid]?.let {
            localPublicProfiles[targetUid] = it.copy(followersCount = (it.followersCount - 1).coerceAtLeast(0L))
        }
        localPublicProfiles[followerUid]?.let {
            localPublicProfiles[followerUid] = it.copy(followingCount = (it.followingCount - 1).coerceAtLeast(0L))
        }

        Result.success(true)
    }

    suspend fun blockUser(uid: String, targetUid: String): Result<Boolean> = withContext(ioDispatcher) {
        val set = localBlocked.getOrPut(uid) { mutableSetOf() }
        set.add(targetUid)
        // Automatically sever follow relationships
        unfollowUser(uid, targetUid)
        unfollowUser(targetUid, uid)
        Result.success(true)
    }

    suspend fun reportProfile(report: ProfileReport): Result<Unit> = withContext(ioDispatcher) {
        localReports.add(report)
        Result.success(Unit)
    }
}
