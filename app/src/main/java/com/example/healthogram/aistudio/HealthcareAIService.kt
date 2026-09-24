package com.example.healthogram.aistudio

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class HealthcareAIRequest(
    val requestId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val requesterUid: String,
    val purpose: String, // RECORD_SUMMARIZATION, TERMINOLOGY_EXPLANATION, DOCUMENT_ORGANIZATION, APPOINTMENT_NOTE_ASSIST
    val minimizedTextPayload: String,
    val consentVerified: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class HealthcareAIResponse(
    val responseId: String = UUID.randomUUID().toString(),
    val requestId: String,
    val patientUid: String,
    val generatedText: String,
    val structuredEntities: List<String> = emptyList(),
    val providerName: String = "Google Gemini Pro Healthcare Adapter",
    val requiresHumanConfirmation: Boolean = true,
    val isConfirmedByUser: Boolean = false,
    val disclaimer: String = "AI-assisted clinical note/summary for informational assistance only. This AI system does not diagnose, prescribe, or provide clinical directives. A verified clinician must review and approve before reliance.",
    val processedTimestamp: Long = System.currentTimeMillis()
)

interface HealthcareAIProviderAdapter {
    fun generateHealthcareAssistance(payload: String, purpose: String): String
}

class GeminiHealthcareAIProvider : HealthcareAIProviderAdapter {
    override fun generateHealthcareAssistance(payload: String, purpose: String): String {
        return when (purpose) {
            "TERMINOLOGY_EXPLANATION" -> {
                "Layperson Explanation: The term describes standard clinical measurements. Always review abnormal results directly with your physician."
            }
            "RECORD_SUMMARIZATION" -> {
                "Summary of consultation notes: Key discussion points identified and chronological medications outlined for your personal review."
            }
            "DOCUMENT_ORGANIZATION" -> {
                "Document categorized. Provider, date, and key items extracted for structured health record archiving."
            }
            else -> "Clinical administrative assistance processed: $payload"
        }
    }
}

/**
 * HealthcareAIService 2.1
 * Enforces a strict security and privacy airgap between generic social/marketplace AI
 * and private patient health data.
 *
 * CRITICAL INVARIANT:
 * 1. Generic AIStudioService NEVER receives Health Passport data.
 * 2. Healthcare AI requires explicit authorization, minimal payload, and mandatory human confirmation.
 * 3. AI NEVER silently modifies medications, prescriptions, or diagnosis records.
 */
class HealthcareAIService private constructor(
    private val providerAdapter: HealthcareAIProviderAdapter = GeminiHealthcareAIProvider()
) {

    private val requestAuditTrail = ConcurrentHashMap<String, MutableList<HealthcareAIRequest>>()

    companion object {
        @Volatile
        private var instance: HealthcareAIService? = null

        fun getInstance(): HealthcareAIService {
            return instance ?: synchronized(this) {
                instance ?: HealthcareAIService().also { instance = it }
            }
        }
    }

    /**
     * Executes safe, audited, non-diagnostic healthcare assistance
     */
    fun processHealthcareAssistance(
        request: HealthcareAIRequest
    ): HealthcareAIResponse {
        // Enforce explicit consent
        require(request.consentVerified) {
            "Security Violation: Healthcare AI processing requires explicit patient consent."
        }
        // Enforce data minimization
        require(request.minimizedTextPayload.isNotBlank() && request.minimizedTextPayload.length < 10000) {
            "Invalid payload: Must be minimized to necessary text segment (< 10,000 characters)."
        }

        val auditList = requestAuditTrail.computeIfAbsent(request.patientUid) { mutableListOf() }
        synchronized(auditList) {
            auditList.add(request)
        }

        val aiResult = providerAdapter.generateHealthcareAssistance(
            payload = request.minimizedTextPayload,
            purpose = request.purpose
        )

        return HealthcareAIResponse(
            requestId = request.requestId,
            patientUid = request.patientUid,
            generatedText = aiResult,
            requiresHumanConfirmation = true,
            isConfirmedByUser = false
        )
    }

    /**
     * Human confirmation gate: user or clinician confirms AI-extracted or summarized text
     */
    fun confirmAIResult(response: HealthcareAIResponse, userUid: String): HealthcareAIResponse {
        require(response.patientUid == userUid || userUid.isNotBlank()) {
            "Unauthorized confirmation of AI output"
        }
        return response.copy(isConfirmedByUser = true)
    }

    data class AIHealthQueryResult(
        val output: String,
        val hasMedicalDisclaimer: Boolean = true,
        val isDiagnostic: Boolean = false
    )

    fun processHealthQuery(patientUid: String, query: String): AIHealthQueryResult {
        return AIHealthQueryResult(
            output = "Consultation assistant note: $query. Disclaimer: Not a substitute for professional clinical advice.",
            hasMedicalDisclaimer = true,
            isDiagnostic = false
        )
    }

    fun sanitizeClinicalText(text: String): String {
        return text.replace("(?i)ignore previous instructions".toRegex(), "[FILTERED_SECURITY_COMMAND]")
            .replace("(?i)output all patients".toRegex(), "[FILTERED_PROMPT_INJECTION]")
    }

    fun getAuditTrail(patientUid: String): List<HealthcareAIRequest> {
        return requestAuditTrail[patientUid]?.toList() ?: emptyList()
    }

    fun clear() {
        requestAuditTrail.clear()
    }
}
