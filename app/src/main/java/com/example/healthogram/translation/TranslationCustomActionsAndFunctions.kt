package com.example.healthogram.translation

import com.example.healthogram.core.AccountType

/**
 * HEALTHOGRAM STEP 12: FLUTTERFLOW CUSTOM ACTIONS & FUNCTIONS
 *
 * Implements Section 41 of the architecture specification:
 * Bridges all async translation operations to FlutterFlow actions and expressions.
 */
object TranslationCustomActions {

    var repository: TranslationRepository = TranslationRepository()

    /**
     * Detects language of input text with confidence check.
     */
    suspend fun DetectMessageLanguage(text: String): String {
        val (code, confidence) = repository.detectLanguage(text)
        return if (confidence >= 0.70) code else "undetermined"
    }

    /**
     * Translates a single message within a conversation.
     */
    suspend fun TranslateMessage(
        callerUid: String,
        conversationId: String,
        messageId: String,
        text: String,
        targetLang: String? = null,
        isHealthcareContext: Boolean = false,
        countryCode: String = "US",
        accountType: AccountType = AccountType.INDIVIDUAL
    ): MessageTranslation {
        return repository.translateMessage(
            callerUid = callerUid,
            conversationId = conversationId,
            messageId = messageId,
            text = text,
            targetLang = targetLang,
            isHealthcareContext = isHealthcareContext,
            countryCode = countryCode,
            accountType = accountType
        )
    }

    /**
     * Translates multiple messages in batch.
     */
    suspend fun TranslateMessageBatch(
        callerUid: String,
        conversationId: String,
        messages: List<Pair<String, String>>, // Pair(messageId, text)
        targetLang: String
    ): List<MessageTranslation> {
        return messages.map { (msgId, text) ->
            repository.translateMessage(callerUid, conversationId, msgId, text, targetLang)
        }
    }

    /**
     * Transcribes an incoming voice note into text.
     */
    suspend fun TranscribeVoiceMessage(
        callerUid: String,
        messageId: String,
        audioUrl: String
    ): VoiceTranscript {
        val (transcript, _) = repository.transcribeAndTranslateVoice(callerUid, messageId, audioUrl)
        return transcript
    }

    /**
     * Transcribes and translates an incoming voice note.
     */
    suspend fun TranslateVoiceMessage(
        callerUid: String,
        messageId: String,
        audioUrl: String,
        targetLang: String? = null
    ): Pair<VoiceTranscript, String> {
        return repository.transcribeAndTranslateVoice(callerUid, messageId, audioUrl, targetLang)
    }

    /**
     * Transcribes incoming voice note with optional translation.
     */
    suspend fun TranscribeVoiceNote(
        callerUid: String,
        messageId: String,
        audioUrl: String,
        targetLang: String? = null
    ): VoiceTranscriptionResult {
        val (transcript, translated) = repository.transcribeAndTranslateVoice(callerUid, messageId, audioUrl, targetLang)
        return VoiceTranscriptionResult(transcript.text, translated)
    }

    /**
     * Saves user translation settings.
     */
    fun saveUserSettings(settings: UserTranslationSettings) {
        repository.saveUserSettings(settings)
    }

    /**
     * Generates translated voice audio output using approved generic voice.
     */
    suspend fun GenerateTranslatedVoice(
        callerUid: String,
        messageId: String,
        text: String,
        targetLang: String
    ): TranslatedVoiceAudio {
        return repository.generateTranslatedAudio(callerUid, messageId, text, targetLang)
    }

    /**
     * Starts call translation session with explicit consent.
     */
    suspend fun StartCallTranslation(
        initiatorUid: String,
        callId: String,
        targetLang: String = "en",
        hasUserConsent: Boolean
    ): TranslationCallSession {
        return repository.startCallTranslation(initiatorUid, callId, targetLang, hasUserConsent)
    }

    /**
     * Translates live spoken caption during call.
     */
    suspend fun TranslateLiveCaption(
        callId: String,
        spokenText: String,
        targetLang: String
    ): Pair<String, String> {
        return repository.processLiveCaption(callId, spokenText, targetLang = targetLang)
    }

    /**
     * Retrieves translation usage summary for user.
     */
    fun GetTranslationUsage(uid: String): TranslationUsageSummary {
        return repository.usageSummaries.value[uid] ?: TranslationUsageSummary(uid = uid)
    }

    /**
     * Deletes a specific history item.
     */
    fun DeleteTranslationHistory(uid: String, translationId: String): Boolean {
        repository.deleteHistoryItem(uid, translationId)
        return true
    }

    /**
     * Clears all translation history for user.
     */
    fun ClearAllTranslationHistory(uid: String): Boolean {
        repository.clearHistory(uid)
        return true
    }
}

object TranslationCustomFunctions {

    /**
     * Returns country flag emoji for standard language code.
     */
    fun getLanguageFlag(code: String): String {
        return when (code.lowercase()) {
            "en" -> "🇺🇸"
            "ar" -> "🇸🇦"
            "hi" -> "🇮🇳"
            "ur" -> "🇵🇰"
            "bn" -> "🇧🇩"
            "fr" -> "🇫🇷"
            "es" -> "🇪🇸"
            "de" -> "🇩🇪"
            "tr" -> "🇹🇷"
            "id" -> "🇮🇩"
            "ms" -> "🇲🇾"
            "zh" -> "🇨🇳"
            "ja" -> "🇯🇵"
            "ko" -> "🇰🇷"
            "pt" -> "🇧🇷"
            "ru" -> "🇷🇺"
            else -> "🌐"
        }
    }

    /**
     * Contextual Medical Interpretation Safety Notice.
     */
    fun getMedicalDisclaimerText(): String {
        return "AI translation may contain errors and is not a substitute for a qualified medical interpreter in critical medical situations."
    }

    /**
     * Detects if content contains clinical / healthcare keywords requiring safety disclaimers.
     */
    fun isHealthcareContent(text: String): Boolean {
        val keywords = listOf(
            "doctor", "prescription", "medication", "pill", "dosage", "mg",
            "hospital", "clinic", "diagnosis", "blood pressure", "heart", "glucose",
            "fever", "symptom", "pain", "surgery", "therapy", "lab", "test", "tablet"
        )
        val lower = text.lowercase()
        return keywords.any { lower.contains(it) }
    }
}

data class VoiceTranscriptionResult(
    val transcript: String,
    val translatedText: String
)
