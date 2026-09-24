package com.example.healthogram.organization

import com.example.healthogram.core.AccountType
import java.util.concurrent.ConcurrentHashMap

data class ProviderSearchResult(
    val providerUid: String,
    val name: String,
    val accountType: AccountType,
    val specialty: String,
    val organizationName: String?,
    val city: String,
    val countryCode: String,
    val languagesSpoken: List<String>,
    val consultationFee: Double,
    val currency: String = "OMR",
    val nextAvailableSlot: String? = "Tomorrow, 10:00 AM",
    val isVerified: Boolean = true,
    val isSponsored: Boolean = false
)

/**
 * HealthcareDiscoveryService 2.1
 * Discovery of verified healthcare providers, clinics, hospitals, and laboratories.
 * CRITICAL INVARIANT: Private patient medical records are NEVER indexed or searchable.
 */
class HealthcareDiscoveryService private constructor() {

    private val directory = ConcurrentHashMap<String, ProviderSearchResult>()

    init {
        // Seed standard verified directory entries
        directory["dr_sarah"] = ProviderSearchResult(
            providerUid = "dr_sarah",
            name = "Dr. Sarah Al-Busaidi, MD",
            accountType = AccountType.DOCTOR,
            specialty = "Cardiology",
            organizationName = "Royal Muscat Hospital",
            city = "Muscat",
            countryCode = "OM",
            languagesSpoken = listOf("Arabic", "English"),
            consultationFee = 25.0,
            currency = "OMR"
        )
        directory["clinic_al_amal"] = ProviderSearchResult(
            providerUid = "clinic_al_amal",
            name = "Al Amal Specialized Clinic",
            accountType = AccountType.CLINIC,
            specialty = "Multispecialty & Pediatrics",
            organizationName = null,
            city = "Salalah",
            countryCode = "OM",
            languagesSpoken = listOf("Arabic", "English"),
            consultationFee = 20.0,
            currency = "OMR"
        )
        directory["lab_precision"] = ProviderSearchResult(
            providerUid = "lab_precision",
            name = "Precision Diagnostic Lab",
            accountType = AccountType.LABORATORY,
            specialty = "Clinical Pathology & Genetics",
            organizationName = null,
            city = "Muscat",
            countryCode = "OM",
            languagesSpoken = listOf("Arabic", "English"),
            consultationFee = 15.0,
            currency = "OMR"
        )
    }

    companion object {
        @Volatile
        private var instance: HealthcareDiscoveryService? = null

        fun getInstance(): HealthcareDiscoveryService {
            return instance ?: synchronized(this) {
                instance ?: HealthcareDiscoveryService().also { instance = it }
            }
        }
    }

    fun searchProviders(
        countryCode: String? = null,
        city: String? = null,
        specialty: String? = null,
        accountType: AccountType? = null
    ): List<ProviderSearchResult> {
        return directory.values.filter { entry ->
            (countryCode == null || entry.countryCode.equals(countryCode, ignoreCase = true)) &&
            (city == null || entry.city.contains(city, ignoreCase = true)) &&
            (specialty == null || entry.specialty.contains(specialty, ignoreCase = true)) &&
            (accountType == null || entry.accountType == accountType)
        }.sortedWith(
            compareByDescending<ProviderSearchResult> { it.isSponsored }
                .thenByDescending { it.isVerified }
        )
    }

    fun registerProvider(result: ProviderSearchResult) {
        directory[result.providerUid] = result
    }

    fun clear() {
        directory.clear()
    }
}
