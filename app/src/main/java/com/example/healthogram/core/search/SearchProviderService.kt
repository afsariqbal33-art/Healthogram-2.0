package com.example.healthogram.core.search

/**
 * Healthogram 2.0 Search Provider Abstraction & Decoupled Indexing Architecture.
 *
 * Implements ADR-015:
 * - Decouples search queries from primary transactional Firestore tables.
 * - Pluggable search adapter (In-Memory, OpenSearch, Algolia, Cloud Search).
 * - Multi-field filtering (entity type, country, city, verification, price range).
 * - STRICT SECURITY INVARIANT: Health Passport medical records are permanently barred from indexing.
 */
enum class SearchEntityType {
    USER,
    DOCTOR,
    CLINIC,
    HOSPITAL,
    LABORATORY,
    PRODUCT,
    HASHTAG
}

data class SearchDocument(
    val documentId: String,
    val entityType: SearchEntityType,
    val title: String,
    val subtitle: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val country: String = "OM",
    val city: String = "",
    val isVerified: Boolean = false,
    val priceInCents: Long? = null,
    val rating: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis()
)

data class SearchQuery(
    val term: String,
    val entityTypes: Set<SearchEntityType> = emptySet(),
    val country: String? = null,
    val city: String? = null,
    val onlyVerified: Boolean = false,
    val minPriceInCents: Long? = null,
    val maxPriceInCents: Long? = null,
    val limit: Int = 20,
    val offset: Int = 0
)

data class SearchResultPage(
    val documents: List<SearchDocument>,
    val totalHits: Int,
    val executionTimeMillis: Long
)

interface SearchProviderAdapter {
    suspend fun indexDocument(document: SearchDocument): Result<Unit>
    suspend fun updateDocument(document: SearchDocument): Result<Unit>
    suspend fun deleteDocument(documentId: String): Result<Unit>
    suspend fun search(query: SearchQuery): SearchResultPage
}

/**
 * High-performance In-Memory Search Engine for local caching and development testing.
 */
class InMemorySearchProvider : SearchProviderAdapter {
    private val index = mutableMapOf<String, SearchDocument>()

    override suspend fun indexDocument(document: SearchDocument): Result<Unit> {
        // Enforce zero-leakage invariant: No clinical records allowed
        validateIndexingSafety(document)
        synchronized(index) {
            index[document.documentId] = document
        }
        return Result.success(Unit)
    }

    override suspend fun updateDocument(document: SearchDocument): Result<Unit> {
        return indexDocument(document)
    }

    override suspend fun deleteDocument(documentId: String): Result<Unit> {
        synchronized(index) {
            index.remove(documentId)
        }
        return Result.success(Unit)
    }

    override suspend fun search(query: SearchQuery): SearchResultPage {
        val start = System.currentTimeMillis()
        val tokens = query.term.trim().lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }

        val candidates = synchronized(index) {
            index.values.filter { doc ->
                // Filter by entity type if specified
                if (query.entityTypes.isNotEmpty() && doc.entityType !in query.entityTypes) {
                    return@filter false
                }
                // Filter by country
                if (query.country != null && !doc.country.equals(query.country, ignoreCase = true)) {
                    return@filter false
                }
                // Filter by city
                if (query.city != null && !doc.city.equals(query.city, ignoreCase = true)) {
                    return@filter false
                }
                // Filter by verification
                if (query.onlyVerified && !doc.isVerified) {
                    return@filter false
                }
                // Filter by price range
                if (query.minPriceInCents != null && (doc.priceInCents == null || doc.priceInCents < query.minPriceInCents)) {
                    return@filter false
                }
                if (query.maxPriceInCents != null && (doc.priceInCents == null || doc.priceInCents > query.maxPriceInCents)) {
                    return@filter false
                }

                // Text matching across title, subtitle, description, tags
                if (tokens.isEmpty()) {
                    true
                } else {
                    val searchableBlob = "${doc.title} ${doc.subtitle} ${doc.description} ${doc.tags.joinToString(" ")}".lowercase()
                    tokens.all { searchableBlob.contains(it) }
                }
            }
        }

        // Rank results: Verified first, then by rating, then by recent updates
        val sorted = candidates.sortedWith(
            compareByDescending<SearchDocument> { it.isVerified }
                .thenByDescending { it.rating }
                .thenByDescending { it.updatedAt }
        )

        val paged = sorted.drop(query.offset).take(query.limit)
        val elapsed = System.currentTimeMillis() - start

        return SearchResultPage(
            documents = paged,
            totalHits = candidates.size,
            executionTimeMillis = elapsed
        )
    }

    private fun validateIndexingSafety(doc: SearchDocument) {
        val lowerTitle = doc.title.lowercase()
        val lowerDesc = doc.description.lowercase()
        val forbiddenKeywords = listOf("health_passport", "diagnosis", "prescription", "lab_result", "allergy_history")
        for (kw in forbiddenKeywords) {
            if (lowerTitle.contains(kw) || lowerDesc.contains(kw)) {
                throw SecurityException("Architectural Violation: Health Passport clinical records MUST NOT be indexed in SearchProvider.")
            }
        }
    }
}
