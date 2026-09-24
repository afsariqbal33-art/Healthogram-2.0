package com.example.healthogram.core

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class RoadmapFeatureCandidate(
    val candidateId: String = UUID.randomUUID().toString(),
    val featureName: String,
    val problemStatement: String,
    val targetUserSegment: String, // PATIENTS, DOCTORS, CLINICS, HOSPITALS, LABS, CREATORS, SELLERS
    val empiricalEvidence: String,
    val estimatedUserBenefit: String,
    val infrastructureCostImpact: String,
    val engineeringComplexity: String, // LOW, MEDIUM, HIGH, COMPLEX
    val privacyRiskScore: String, // LOW, MEDIUM, HIGH (Requires DPA)
    val securityRiskScore: String,
    val regulatoryPrerequisites: List<String>,
    val monetizationPotential: String,
    val rolloutStrategy: String,
    val status: String = "EVALUATING" // EVALUATING, APPROVED_FOR_SPIKE, PROTOTYPING, SCHEDULED, REJECTED
)

/**
 * ProductEvidenceService 2.1
 * Gathers empirical signals (adoption, crashes, support tickets, revenue, cost)
 * to formulate evidence-based product roadmap decisions.
 */
class ProductEvidenceService private constructor() {

    private val candidates = ConcurrentHashMap<String, RoadmapFeatureCandidate>()

    companion object {
        @Volatile
        private var instance: ProductEvidenceService? = null

        fun getInstance(): ProductEvidenceService {
            return instance ?: synchronized(this) {
                instance ?: ProductEvidenceService().also { instance = it }
            }
        }
    }

    fun submitCandidate(candidate: RoadmapFeatureCandidate): RoadmapFeatureCandidate {
        candidates[candidate.candidateId] = candidate
        return candidate
    }

    fun getCandidates(): List<RoadmapFeatureCandidate> = candidates.values.toList()

    fun updateCandidateStatus(candidateId: String, newStatus: String): Boolean {
        val existing = candidates[candidateId] ?: return false
        candidates[candidateId] = existing.copy(status = newStatus)
        return true
    }
}
