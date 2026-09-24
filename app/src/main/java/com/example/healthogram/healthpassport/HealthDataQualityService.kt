package com.example.healthogram.healthpassport

data class HealthDataQualityReport(
    val patientUid: String,
    val completenessScorePercent: Int, // 0-100%
    val totalRecords: Int,
    val verifiedRecordsCount: Int,
    val unverifiedRecordsCount: Int,
    val potentialDuplicateCount: Int,
    val staleRecordsCount: Int, // Records older than 2 years without review
    val dataQualitySummary: String
)

/**
 * HealthDataQualityService 2.1
 * Evaluates completeness, provenance coverage, duplicate detection, and recency.
 * Invariant: Never declares medical data as clinically "correct" without verified clinician authority.
 */
class HealthDataQualityService private constructor() {

    companion object {
        @Volatile
        private var instance: HealthDataQualityService? = null

        fun getInstance(): HealthDataQualityService {
            return instance ?: synchronized(this) {
                instance ?: HealthDataQualityService().also { instance = it }
            }
        }
    }

    fun evaluateDataQuality(
        patientUid: String,
        timelineEntries: List<HealthTimelineEntry>
    ): HealthDataQualityReport {
        if (timelineEntries.isEmpty()) {
            return HealthDataQualityReport(
                patientUid = patientUid,
                completenessScorePercent = 0,
                totalRecords = 0,
                verifiedRecordsCount = 0,
                unverifiedRecordsCount = 0,
                potentialDuplicateCount = 0,
                staleRecordsCount = 0,
                dataQualitySummary = "No health records found in passport."
            )
        }

        var verifiedCount = 0
        var unverifiedCount = 0
        var staleCount = 0
        val twoYearsAgo = System.currentTimeMillis() - (2 * 365 * 24 * 60 * 60 * 1000L)

        val titleSet = mutableSetOf<String>()
        var duplicates = 0

        timelineEntries.forEach { entry ->
            if (entry.provenance.verificationStatus.contains("VERIFIED", ignoreCase = true)) {
                verifiedCount++
            } else {
                unverifiedCount++
            }

            if (entry.timestamp < twoYearsAgo) {
                staleCount++
            }

            val key = "${entry.recordType}:${entry.title}"
            if (!titleSet.add(key)) {
                duplicates++
            }
        }

        // Completeness heuristics
        var score = 30 // Base score for having records
        if (verifiedCount > 0) score += 40
        if (duplicates == 0) score += 20
        if (timelineEntries.size >= 5) score += 10
        val finalScore = score.coerceIn(0, 100)

        val summary = "Health Passport data quality score: $finalScore%. " +
                "$verifiedCount verified records, $unverifiedCount patient/unverified entries. " +
                "$duplicates potential duplicates identified for review."

        return HealthDataQualityReport(
            patientUid = patientUid,
            completenessScorePercent = finalScore,
            totalRecords = timelineEntries.size,
            verifiedRecordsCount = verifiedCount,
            unverifiedRecordsCount = unverifiedCount,
            potentialDuplicateCount = duplicates,
            staleRecordsCount = staleCount,
            dataQualitySummary = summary
        )
    }
}
