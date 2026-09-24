package com.example.healthogram.translation

import java.util.UUID

/**
 * HEALTHOGRAM STEP 12: MULTILINGUAL TRANSLATION DATA MODELS
 *
 * Implements strict schemas for:
 * - languages/{languageCode}
 * - translation_settings/{uid}
 * - message_translations/{translationId}
 * - translation_jobs/{jobId}
 * - voice_transcripts/{transcriptId}
 * - translation_call_sessions/{sessionId}
 * - translation_call_participants/{id}
 * - country_language_config/{countryCode}
 * - translation_history/{translationId}
 * - translation_usage/{usageId} & summary
 * - translation_audit_logs/{logId}
 */

/**
 * Language entity in languages/{languageCode}
 */
data class LanguageItem(
    val languageCode: String,
    val languageName: String,
    val nativeName: String,
    val countryCodes: List<String> = emptyList(),
    val isRtl: Boolean = false,
    val textTranslationEnabled: Boolean = true,
    val voiceTranslationEnabled: Boolean = true,
    val callTranslationEnabled: Boolean = true,
    val captionTranslationEnabled: Boolean = true,
    val ttsEnabled: Boolean = true,
    val sttEnabled: Boolean = true,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * User Translation Settings in translation_settings/{uid}
 */
data class UserTranslationSettings(
    val uid: String,
    val preferredLanguage: String = "en",
    val sourceLanguage: String = "auto",
    val targetLanguage: String = "en",
    val autoDetectLanguage: Boolean = true,
    val textTranslationEnabled: Boolean = true,
    val voiceTranslationEnabled: Boolean = true,
    val callTranslationEnabled: Boolean = false,
    val captionTranslationEnabled: Boolean = false,
    val translatedAudioEnabled: Boolean = false,
    val showOriginalText: Boolean = true,
    val showTranslatedText: Boolean = true,
    val translateIncomingMessages: Boolean = false,
    val translateOutgoingMessages: Boolean = false,
    val autoTranslateCalls: Boolean = false,
    val translationProvider: String = "gemini_3_8_flash",
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Message Translation Record in message_translations/{translationId}
 */
data class MessageTranslation(
    val translationId: String = UUID.randomUUID().toString(),
    val messageId: String,
    val conversationId: String,
    val requesterUid: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val sourceTextHash: String,
    val translatedText: String,
    val provider: String = "gemini_3_8_flash",
    val model: String = "gemini-3.8-flash",
    val translationVersion: String = "1.0",
    val status: String = "completed", // "completed", "failed", "processing"
    val isHealthcareContext: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000) // 30 days
)

enum class TranslationJobType {
    TEXT,
    VOICE,
    CAPTION,
    SPEECH_TO_TEXT,
    TEXT_TO_SPEECH,
    LIVE_TRANSLATION
}

enum class TranslationJobStatus {
    QUEUED,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    EXPIRED
}

/**
 * Translation Job entity in translation_jobs/{jobId}
 */
data class TranslationJob(
    val jobId: String = UUID.randomUUID().toString(),
    val uid: String,
    val conversationId: String? = null,
    val messageId: String? = null,
    val callId: String? = null,
    val jobType: TranslationJobType,
    val sourceLanguage: String,
    val targetLanguage: String,
    val status: TranslationJobStatus = TranslationJobStatus.QUEUED,
    val provider: String = "gemini_3_8_flash",
    val model: String = "gemini-3.8-flash",
    val inputReference: String,
    val outputReference: String? = null,
    val usageUnits: Long = 0,
    val estimatedCost: Double = 0.0,
    val actualCost: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val failedAt: Long? = null,
    val expiresAt: Long = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000)
)

/**
 * Voice Message Transcript in voice_transcripts/{transcriptId}
 */
data class VoiceTranscript(
    val transcriptId: String = UUID.randomUUID().toString(),
    val messageId: String,
    val uid: String,
    val sourceLanguage: String,
    val text: String,
    val confidence: Double = 0.95,
    val provider: String = "gemini_3_8_flash",
    val model: String = "gemini-3.8-flash",
    val status: String = "completed",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
)

/**
 * Translated Voice Audio Output reference (generic non-cloned approved voice)
 */
data class TranslatedVoiceAudio(
    val audioId: String = UUID.randomUUID().toString(),
    val messageId: String,
    val language: String,
    val audioUrl: String,
    val voiceConfiguration: String = "neutral_assistant",
    val durationSeconds: Int = 0,
    val provider: String = "gemini_tts",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000)
)

enum class TranslationCallSessionStatus {
    CREATED,
    ACTIVE,
    PAUSED,
    ENDED,
    FAILED
}

/**
 * Call Translation Session in translation_call_sessions/{sessionId}
 */
data class TranslationCallSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val callId: String,
    val initiatorUid: String,
    val participantIds: List<String> = emptyList(),
    val sourceLanguageMap: Map<String, String> = emptyMap(),
    val targetLanguageMap: Map<String, String> = emptyMap(),
    val captionEnabled: Boolean = false,
    val translationEnabled: Boolean = false,
    val translatedAudioEnabled: Boolean = false,
    val provider: String = "gemini_3_8_flash",
    val status: TranslationCallSessionStatus = TranslationCallSessionStatus.CREATED,
    val startedAt: Long? = null,
    val endedAt: Long? = null,
    val expiresAt: Long = System.currentTimeMillis() + (24L * 60 * 60 * 1000)
)

/**
 * Call Translation Participant in translation_call_participants/{id}
 */
data class TranslationCallParticipant(
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val uid: String,
    val preferredLanguage: String = "en",
    val sourceLanguage: String = "auto",
    val targetLanguage: String = "en",
    val captionEnabled: Boolean = false,
    val translationEnabled: Boolean = false,
    val translatedAudioEnabled: Boolean = false,
    val consentStatus: Boolean = false,
    val joinedAt: Long = System.currentTimeMillis(),
    val leftAt: Long? = null
)

/**
 * Country Language Configuration in country_language_config/{countryCode}
 */
data class CountryLanguageConfig(
    val countryCode: String,
    val defaultLanguage: String = "en",
    val supportedLanguages: List<String> = emptyList(),
    val textTranslationEnabled: Boolean = true,
    val voiceTranslationEnabled: Boolean = true,
    val callTranslationEnabled: Boolean = false,
    val liveCaptionEnabled: Boolean = false,
    val translatedAudioEnabled: Boolean = false,
    val provider: String = "gemini_3_8_flash",
    val legalNotice: String = "AI translation may contain errors. Not certified for critical medical situations.",
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Translation History item in translation_history/{translationId}
 */
data class TranslationHistoryItem(
    val translationId: String = UUID.randomUUID().toString(),
    val uid: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val sourceReference: String,
    val translationReference: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Translation Usage record in translation_usage/{usageId}
 */
data class TranslationUsageRecord(
    val usageId: String = UUID.randomUUID().toString(),
    val uid: String,
    val country: String = "US",
    val jobType: TranslationJobType,
    val sourceLanguage: String,
    val targetLanguage: String,
    val provider: String = "gemini_3_8_flash",
    val model: String = "gemini-3.8-flash",
    val units: Long = 0,
    val durationSeconds: Int = 0,
    val estimatedCost: Double = 0.0,
    val actualCost: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * User Monthly Translation Usage Summary in translation_usage_summary/{uid}
 */
data class TranslationUsageSummary(
    val uid: String,
    val monthlyTextUnits: Long = 0,
    val monthlyAudioSeconds: Long = 0,
    val monthlyCallMinutes: Long = 0,
    val monthlyTtsUnits: Long = 0,
    val monthlyEstimatedCost: Double = 0.0,
    val monthlyActualCost: Double = 0.0,
    val limitUnits: Long = 500_000,
    val remainingUnits: Long = 500_000,
    val resetDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
)

/**
 * Audit Log for Translation Security in translation_audit_logs/{logId}
 */
data class TranslationAuditLog(
    val logId: String = UUID.randomUUID().toString(),
    val actorUid: String,
    val action: String,
    val jobId: String? = null,
    val conversationId: String? = null,
    val callId: String? = null,
    val sourceLanguage: String? = null,
    val targetLanguage: String? = null,
    val provider: String? = null,
    val status: String,
    val timestamp: Long = System.currentTimeMillis(),
    val country: String = "US",
    val deviceId: String = "android_client"
)

enum class CaptionDisplayMode {
    OFF,
    ORIGINAL_ONLY,
    TRANSLATED_ONLY,
    BOTH
}

enum class CaptionFontSize {
    SMALL,
    MEDIUM,
    LARGE,
    EXTRA_LARGE
}

/**
 * Caption presentation styling settings
 */
data class CaptionSettings(
    val fontSize: CaptionFontSize = CaptionFontSize.MEDIUM,
    val highContrast: Boolean = true,
    val isTopPosition: Boolean = false,
    val backgroundColorAlpha: Float = 0.75f
)
