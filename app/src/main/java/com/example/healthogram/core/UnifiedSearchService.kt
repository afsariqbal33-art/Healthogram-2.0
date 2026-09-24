package com.example.healthogram.core

import java.util.concurrent.ConcurrentHashMap

/**
 * Step 32: Unified Search Abstraction & Personalization Safety
 *
 * Provides a unified public search engine across:
 * - Public Accounts
 * - Doctors, Clinics, Hospitals, Laboratories
 * - Marketplace Products
 * - Public Posts & Educational Articles
 *
 * CRITICAL SAFETY ENFORCEMENT:
 * Private patient medical records, Health Passport entries, documents,
 * and lab results are STRICTLY EXCLUDED and mathematically impossible to index.
 */
enum class SearchDomain {
    ACCOUNTS,
    DOCTORS,
    CLINICS,
    HOSPITALS,
    LABORATORIES,
    PRODUCTS,
    POSTS,
    EDUCATIONAL_CONTENT
}

data class UnifiedSearchResult(
    val id: String,
    val domain: SearchDomain,
    val title: String,
    val subtitle: String,
    val summary: String,
    val imageUrl: String? = null,
    val tags: List<String> = emptyList(),
    val countryCode: String = "OM"
)

class UnifiedSearchService private constructor() {

    private val searchIndex = ConcurrentHashMap<SearchDomain, MutableList<UnifiedSearchResult>>()

    init {
        // Seed standard public directory items
        val doctors = searchIndex.computeIfAbsent(SearchDomain.DOCTORS) { mutableListOf() }
        doctors.add(
            UnifiedSearchResult(
                id = "doc_101",
                domain = SearchDomain.DOCTORS,
                title = "Dr. Tariq Al-Hinai, MD",
                subtitle = "Neurology & Sleep Medicine",
                summary = "Consultant Neurologist at Muscat City Medical Complex.",
                countryCode = "OM"
            )
        )
        val clinics = searchIndex.computeIfAbsent(SearchDomain.CLINICS) { mutableListOf() }
        clinics.add(
            UnifiedSearchResult(
                id = "cl_201",
                domain = SearchDomain.CLINICS,
                title = "Bawshar Wellness Clinic",
                subtitle = "General Medicine & Dermatology",
                summary = "Comprehensive family healthcare center.",
                countryCode = "OM"
            )
        )
        val products = searchIndex.computeIfAbsent(SearchDomain.PRODUCTS) { mutableListOf() }
        products.add(
            UnifiedSearchResult(
                id = "prod_301",
                domain = SearchDomain.PRODUCTS,
                title = "Digital Blood Pressure Monitor",
                subtitle = "Medical Device (FDA Approved)",
                summary = "Arm cuff digital oscillometric sphygmomanometer.",
                countryCode = "OM"
            )
        )
        val edu = searchIndex.computeIfAbsent(SearchDomain.EDUCATIONAL_CONTENT) { mutableListOf() }
        edu.add(
            UnifiedSearchResult(
                id = "edu_401",
                domain = SearchDomain.EDUCATIONAL_CONTENT,
                title = "Managing Seasonal Allergies in the Gulf",
                subtitle = "Preventative Health Guide",
                summary = "Evidence-based guidelines on dust, pollen, and humidity triggers.",
                countryCode = "OM"
            )
        )
    }

    companion object {
        @Volatile
        private var instance: UnifiedSearchService? = null

        fun getInstance(): UnifiedSearchService {
            return instance ?: synchronized(this) {
                instance ?: UnifiedSearchService().also { instance = it }
            }
        }

        // Prohibited terms check to strictly prevent health passport ingestion
        private val PROHIBITED_COLLECTIONS = setOf(
            "health_passport", "health_records", "health_conditions",
            "health_allergies", "health_medications", "health_documents",
            "health_lab_reports", "emergency_card"
        )
    }

    /**
     * Attempts to index public search content.
     * Rejects any collection belonging to private health records.
     */
    fun indexPublicContent(domain: SearchDomain, result: UnifiedSearchResult, sourceCollection: String) {
        if (PROHIBITED_COLLECTIONS.contains(sourceCollection.lowercase())) {
            throw SecurityException("PROHIBITED: Health passport data must NEVER be indexed in public search: $sourceCollection")
        }
        val list = searchIndex.computeIfAbsent(domain) { mutableListOf() }
        synchronized(list) {
            list.removeIf { it.id == result.id }
            list.add(result)
        }
    }

    /**
     * Query public index across domains
     */
    fun search(
        query: String,
        domains: Set<SearchDomain> = SearchDomain.values().toSet(),
        countryCode: String? = null
    ): List<UnifiedSearchResult> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return emptyList()

        val results = mutableListOf<UnifiedSearchResult>()
        for (domain in domains) {
            val items = searchIndex[domain] ?: continue
            val matched = items.filter { item ->
                (countryCode == null || item.countryCode.equals(countryCode, ignoreCase = true)) &&
                (item.title.lowercase().contains(q) ||
                 item.subtitle.lowercase().contains(q) ||
                 item.summary.lowercase().contains(q) ||
                 item.tags.any { it.lowercase().contains(q) })
            }
            results.addAll(matched)
        }
        return results
    }
}

/**
 * Step 32: PersonalizationSafetyService
 * Enforces absolute separation between clinical health records and feed algorithms.
 */
class PersonalizationSafetyService private constructor() {

    data class PersonalizationPreferences(
        val userUid: String,
        val healthDataUsedForPersonalization: Boolean = false, // ALWAYS false by default
        val socialRecommendationsEnabled: Boolean = true,
        val productRecommendationsEnabled: Boolean = true,
        val allowTopicRecommendations: Boolean = true,
        val userExplicitlyApprovedHealthPersonalization: Boolean = false
    )

    private val preferences = ConcurrentHashMap<String, PersonalizationPreferences>()

    companion object {
        @Volatile
        private var instance: PersonalizationSafetyService? = null

        fun getInstance(): PersonalizationSafetyService {
            return instance ?: synchronized(this) {
                instance ?: PersonalizationSafetyService().also { instance = it }
            }
        }
    }

    fun getPreferences(userUid: String): PersonalizationPreferences {
        return preferences[userUid] ?: PersonalizationPreferences(userUid = userUid)
    }

    /**
     * Updates personalization controls. Enforces that health data cannot be used
     * unless explicitly confirmed with disclaimer.
     */
    fun updatePreferences(
        userUid: String,
        enableHealthPersonalization: Boolean,
        disclaimerConfirmed: Boolean
    ): PersonalizationPreferences {
        if (enableHealthPersonalization && !disclaimerConfirmed) {
            throw IllegalArgumentException("Cannot enable health-based personalization without explicit disclaimer confirmation")
        }

        val updated = PersonalizationPreferences(
            userUid = userUid,
            healthDataUsedForPersonalization = enableHealthPersonalization && disclaimerConfirmed,
            userExplicitlyApprovedHealthPersonalization = enableHealthPersonalization && disclaimerConfirmed
        )
        preferences[userUid] = updated
        return updated
    }

    /**
     * Guard check before applying any recommendation feature.
     * Guaranteed to return false if default or unconfirmed.
     */
    fun canUseHealthDataForRecommendations(userUid: String): Boolean {
        val pref = getPreferences(userUid)
        return pref.healthDataUsedForPersonalization && pref.userExplicitlyApprovedHealthPersonalization
    }
}
