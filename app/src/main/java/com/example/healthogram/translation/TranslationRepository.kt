package com.example.healthogram.translation

import com.example.healthogram.core.AccountType
import com.example.healthogram.owner.OwnerControlEngine
import com.example.healthogram.owner.PlatformFeature
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * HEALTHOGRAM STEP 12: TRANSLATION REPOSITORY
 *
 * Central orchestrator for all multilingual translation services:
 * - Text message translation & scoped caching
 * - Voice message transcription & translation
 * - Generic approved voice synthesis (No voice cloning)
 * - Live RTC Call Captions & Translated audio stream
 * - Strict Health Passport Isolation enforcement
 * - Usage, Rate Limits, and Audit Logging
 */
class TranslationRepository(
    private val ownerControlEngine: OwnerControlEngine = OwnerControlEngine(),
    private val providerRegistry: TranslationProviderRegistry = TranslationProviderRegistry()
) {
    // Standard supported languages
    private val _languages = MutableStateFlow<List<LanguageItem>>(
        listOf(
            LanguageItem("en", "English", "English", listOf("US", "GB", "CA", "AU"), isRtl = false),
            LanguageItem("ar", "Arabic", "العربية", listOf("SA", "AE", "EG", "QA"), isRtl = true),
            LanguageItem("hi", "Hindi", "हिन्दी", listOf("IN"), isRtl = false),
            LanguageItem("ur", "Urdu", "اردو", listOf("PK", "IN"), isRtl = true),
            LanguageItem("bn", "Bengali", "বাংলা", listOf("BD", "IN"), isRtl = false),
            LanguageItem("fr", "French", "Français", listOf("FR", "CA", "BE", "CH"), isRtl = false),
            LanguageItem("es", "Spanish", "Español", listOf("ES", "MX", "CO", "AR"), isRtl = false),
            LanguageItem("de", "German", "Deutsch", listOf("DE", "AT", "CH"), isRtl = false),
            LanguageItem("tr", "Turkish", "Türkçe", listOf("TR"), isRtl = false),
            LanguageItem("id", "Indonesian", "Bahasa Indonesia", listOf("ID"), isRtl = false),
            LanguageItem("ms", "Malay", "Bahasa Melayu", listOf("MY"), isRtl = false),
            LanguageItem("zh", "Chinese", "中文", listOf("CN", "TW", "SG"), isRtl = false),
            LanguageItem("ja", "Japanese", "日本語", listOf("JP"), isRtl = false),
            LanguageItem("ko", "Korean", "한국어", listOf("KR"), isRtl = false),
            LanguageItem("pt", "Portuguese", "Português", listOf("BR", "PT"), isRtl = false),
            LanguageItem("ru", "Russian", "Русский", listOf("RU", "KZ"), isRtl = false)
        )
    )
    val languages: StateFlow<List<LanguageItem>> = _languages.asStateFlow()

    // User settings store
    private val _userSettings = MutableStateFlow<Map<String, UserTranslationSettings>>(emptyMap())
    val userSettings: StateFlow<Map<String, UserTranslationSettings>> = _userSettings.asStateFlow()

    // Cached message translations: (conversationId:messageId:targetLang) -> MessageTranslation
    private val _translations = MutableStateFlow<Map<String, MessageTranslation>>(emptyMap())
    val translations: StateFlow<Map<String, MessageTranslation>> = _translations.asStateFlow()

    // Voice transcripts
    private val _voiceTranscripts = MutableStateFlow<Map<String, VoiceTranscript>>(emptyMap())
    val voiceTranscripts: StateFlow<Map<String, VoiceTranscript>> = _voiceTranscripts.asStateFlow()

    // Synthesized translated audio
    private val _translatedAudios = MutableStateFlow<Map<String, TranslatedVoiceAudio>>(emptyMap())
    val translatedAudios: StateFlow<Map<String, TranslatedVoiceAudio>> = _translatedAudios.asStateFlow()

    // Call translation sessions
    private val _callSessions = MutableStateFlow<Map<String, TranslationCallSession>>(emptyMap())
    val callSessions: StateFlow<Map<String, TranslationCallSession>> = _callSessions.asStateFlow()

    // Call live captions: callId -> List of Pair(original, translated)
    private val _liveCaptions = MutableStateFlow<Map<String, List<Pair<String, String>>>>(emptyMap())
    val liveCaptions: StateFlow<Map<String, List<Pair<String, String>>>> = _liveCaptions.asStateFlow()

    // Translation history
    private val _history = MutableStateFlow<List<TranslationHistoryItem>>(emptyList())
    val history: StateFlow<List<TranslationHistoryItem>> = _history.asStateFlow()

    // Usage Summaries
    private val _usageSummaries = MutableStateFlow<Map<String, TranslationUsageSummary>>(emptyMap())
    val usageSummaries: StateFlow<Map<String, TranslationUsageSummary>> = _usageSummaries.asStateFlow()

    // Audit logs
    private val _auditLogs = MutableStateFlow<List<TranslationAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<TranslationAuditLog>> = _auditLogs.asStateFlow()

    // Protected Health Passport collection keys that must NEVER be accessed through translation
    private val protectedHealthKeys = setOf(
        "health_passports", "health_conditions", "health_allergies",
        "health_medications", "health_visits", "health_diagnoses",
        "health_tests", "health_lab_reports", "health_prescriptions",
        "health_documents", "health_bills"
    )

    // -------------------------------------------------------------------------
    // User Settings Management
    // -------------------------------------------------------------------------

    fun getUserSettings(uid: String): UserTranslationSettings {
        return _userSettings.value[uid] ?: UserTranslationSettings(uid = uid)
    }

    fun updateUserSettings(settings: UserTranslationSettings, callerUid: String) {
        if (settings.uid != callerUid) {
            throw SecurityException("Cannot update another user's translation settings")
        }
        val current = _userSettings.value.toMutableMap()
        current[settings.uid] = settings.copy(updatedAt = System.currentTimeMillis())
        _userSettings.value = current
    }

    fun saveUserSettings(settings: UserTranslationSettings) {
        val current = _userSettings.value.toMutableMap()
        current[settings.uid] = settings.copy(updatedAt = System.currentTimeMillis())
        _userSettings.value = current
    }

    fun updateUserSettings(settings: UserTranslationSettings) {
        saveUserSettings(settings)
    }

    // -------------------------------------------------------------------------
    // Language Detection
    // -------------------------------------------------------------------------

    suspend fun detectLanguage(text: String): Pair<String, Double> {
        val provider = providerRegistry.getActiveProvider()
        return provider.detectLanguage(text)
    }

    // -------------------------------------------------------------------------
    // Text Message Translation
    // -------------------------------------------------------------------------

    suspend fun translateMessage(
        callerUid: String,
        conversationId: String,
        messageId: String,
        text: String,
        targetLang: String? = null,
        isHealthcareContext: Boolean = false,
        countryCode: String = "US",
        accountType: AccountType = AccountType.INDIVIDUAL
    ): MessageTranslation {
        // 1. Check Owner Control Feature Availability
        if (ownerControlEngine.isEmergencyKillSwitchActive()) {
            throw IllegalStateException("Translation services are temporarily suspended by platform administrator.")
        }
        if (!ownerControlEngine.isFeatureAvailable(PlatformFeature.TRANSLATION_CORE, countryCode, accountType)) {
            throw IllegalStateException("Translation feature is currently disabled.")
        }
        if (!ownerControlEngine.isFeatureAvailable(PlatformFeature.TEXT_TRANSLATION, countryCode, accountType)) {
            throw IllegalStateException("Text translation is currently disabled.")
        }

        // 2. Critical Health Passport Isolation Check
        assertNoHealthPassportViolation(text, callerUid, conversationId)

        // 3. Resolve target language
        val settings = getUserSettings(callerUid)
        val resolvedTarget = targetLang ?: settings.targetLanguage

        val cacheKey = "$conversationId:$messageId:$resolvedTarget"
        val existing = _translations.value[cacheKey]
        if (existing != null) {
            return existing
        }

        // 4. Rate and Usage Check
        checkAndDeductUsage(callerUid, text.length.toLong(), TranslationJobType.TEXT)

        // 5. Execute Translation via active provider (with fallback safety)
        val provider = providerRegistry.getActiveProvider()
        val translation = provider.translateText(
            text = text,
            sourceLang = if (settings.autoDetectLanguage) "auto" else settings.sourceLanguage,
            targetLang = resolvedTarget,
            isHealthcareContext = isHealthcareContext
        ).copy(
            conversationId = conversationId,
            messageId = messageId,
            requesterUid = callerUid
        )

        // Store scoped in cache
        val currentTranslations = _translations.value.toMutableMap()
        currentTranslations[cacheKey] = translation
        _translations.value = currentTranslations

        // Save history if enabled
        if (ownerControlEngine.isFeatureAvailable(PlatformFeature.TRANSLATION_HISTORY, countryCode, accountType)) {
            saveHistoryItem(callerUid, translation.sourceLanguage, resolvedTarget, text, translation.translatedText)
        }

        // Log audit
        logAudit(
            actorUid = callerUid,
            action = "TRANSLATE_TEXT",
            conversationId = conversationId,
            sourceLang = translation.sourceLanguage,
            targetLang = resolvedTarget,
            provider = provider.providerName,
            status = "SUCCESS"
        )

        return translation
    }

    // -------------------------------------------------------------------------
    // Voice Message Transcription & Translation
    // -------------------------------------------------------------------------

    suspend fun transcribeAndTranslateVoice(
        callerUid: String,
        messageId: String,
        audioUrl: String,
        targetLang: String? = null,
        countryCode: String = "US",
        accountType: AccountType = AccountType.INDIVIDUAL
    ): Pair<VoiceTranscript, String> {
        if (!ownerControlEngine.isFeatureAvailable(PlatformFeature.VOICE_TRANSLATION, countryCode, accountType)) {
            throw IllegalStateException("Voice translation is currently disabled.")
        }

        val settings = getUserSettings(callerUid)
        val resolvedTarget = targetLang ?: settings.targetLanguage

        val provider = providerRegistry.getActiveProvider()

        // 1. Transcription (STT)
        val transcript = provider.transcribeAudio(audioUrl, settings.sourceLanguage)
        val transcriptsMap = _voiceTranscripts.value.toMutableMap()
        transcriptsMap[messageId] = transcript
        _voiceTranscripts.value = transcriptsMap

        // 2. Health Passport Isolation Check on Transcribed Text
        assertNoHealthPassportViolation(transcript.text, callerUid, null)

        // 3. Translation
        val translated = provider.translateTranscript(transcript.text, transcript.sourceLanguage, resolvedTarget)

        checkAndDeductUsage(callerUid, 15L, TranslationJobType.VOICE)

        logAudit(
            actorUid = callerUid,
            action = "TRANSLATE_VOICE",
            sourceLang = transcript.sourceLanguage,
            targetLang = resolvedTarget,
            provider = provider.providerName,
            status = "SUCCESS"
        )

        return Pair(transcript, translated)
    }

    // -------------------------------------------------------------------------
    // Generic Speech Synthesis (TTS) - Approved Generic Voices Only
    // -------------------------------------------------------------------------

    suspend fun generateTranslatedAudio(
        callerUid: String,
        messageId: String,
        text: String,
        targetLang: String
    ): TranslatedVoiceAudio {
        val provider = providerRegistry.getActiveProvider()
        val audio = provider.synthesizeSpeech(text, targetLang)

        val audiosMap = _translatedAudios.value.toMutableMap()
        audiosMap[messageId] = audio
        _translatedAudios.value = audiosMap

        return audio
    }

    // -------------------------------------------------------------------------
    // Live Call Translation & Captions
    // -------------------------------------------------------------------------

    suspend fun startCallTranslation(
        initiatorUid: String,
        callId: String,
        targetLang: String = "en",
        hasUserConsent: Boolean
    ): TranslationCallSession {
        if (!hasUserConsent) {
            throw IllegalStateException("Call translation requires explicit user consent.")
        }
        if (!ownerControlEngine.isFeatureAvailable(PlatformFeature.CALL_TRANSLATION, "US", AccountType.INDIVIDUAL)) {
            // If feature is disabled by owner, throw safe exception
            throw IllegalStateException("Call translation is currently disabled in your region.")
        }

        val session = TranslationCallSession(
            sessionId = UUID.randomUUID().toString(),
            callId = callId,
            initiatorUid = initiatorUid,
            participantIds = listOf(initiatorUid),
            captionEnabled = true,
            translationEnabled = true,
            status = TranslationCallSessionStatus.ACTIVE,
            startedAt = System.currentTimeMillis()
        )

        val sessions = _callSessions.value.toMutableMap()
        sessions[callId] = session
        _callSessions.value = sessions

        logAudit(
            actorUid = initiatorUid,
            action = "START_CALL_TRANSLATION",
            callId = callId,
            status = "ACTIVE"
        )

        return session
    }

    suspend fun processLiveCaption(
        callId: String,
        spokenText: String,
        sourceLang: String = "auto",
        targetLang: String = "en"
    ): Pair<String, String> {
        val provider = providerRegistry.getActiveProvider()
        val result = provider.translateLiveCaption(spokenText, sourceLang, targetLang)

        val captionsMap = _liveCaptions.value.toMutableMap()
        val existing = captionsMap[callId]?.toMutableList() ?: mutableListOf()
        existing.add(result)
        captionsMap[callId] = existing
        _liveCaptions.value = captionsMap

        return result
    }

    // -------------------------------------------------------------------------
    // History & Usage Management
    // -------------------------------------------------------------------------

    private fun saveHistoryItem(uid: String, sourceLang: String, targetLang: String, original: String, translated: String) {
        val item = TranslationHistoryItem(
            uid = uid,
            sourceLanguage = sourceLang,
            targetLanguage = targetLang,
            sourceReference = original,
            translationReference = translated
        )
        val current = _history.value.toMutableList()
        current.add(0, item)
        _history.value = current
    }

    fun deleteHistoryItem(uid: String, translationId: String) {
        _history.value = _history.value.filterNot { it.translationId == translationId && it.uid == uid }
    }

    fun clearHistory(uid: String) {
        _history.value = _history.value.filterNot { it.uid == uid }
    }

    private fun checkAndDeductUsage(uid: String, units: Long, jobType: TranslationJobType) {
        val summary = _usageSummaries.value[uid] ?: TranslationUsageSummary(uid = uid)
        if (summary.remainingUnits < units) {
            throw IllegalStateException("Monthly translation usage limit reached. Please upgrade quota.")
        }

        val updated = summary.copy(
            monthlyTextUnits = summary.monthlyTextUnits + if (jobType == TranslationJobType.TEXT) units else 0,
            monthlyAudioSeconds = summary.monthlyAudioSeconds + if (jobType == TranslationJobType.VOICE) units else 0,
            remainingUnits = summary.remainingUnits - units
        )

        val map = _usageSummaries.value.toMutableMap()
        map[uid] = updated
        _usageSummaries.value = map
    }

    // -------------------------------------------------------------------------
    // Security Invariants & Isolation Guards
    // -------------------------------------------------------------------------

    private fun assertNoHealthPassportViolation(text: String, actorUid: String, conversationId: String?) {
        for (protectedKey in protectedHealthKeys) {
            if (text.contains(protectedKey, ignoreCase = true)) {
                logAudit(
                    actorUid = actorUid,
                    action = "HEALTH_PASSPORT_INJECTION_BLOCKED",
                    conversationId = conversationId,
                    status = "BLOCKED_SECURITY_VIOLATION"
                )
                throw SecurityException(
                    "Health Passport Isolation Violation: Translation engine strictly prohibits access to protected health collection '$protectedKey' without explicit patient authorization grant."
                )
            }
        }
    }

    private fun logAudit(
        actorUid: String,
        action: String,
        jobId: String? = null,
        conversationId: String? = null,
        callId: String? = null,
        sourceLang: String? = null,
        targetLang: String? = null,
        provider: String? = null,
        status: String
    ) {
        val entry = TranslationAuditLog(
            actorUid = actorUid,
            action = action,
            jobId = jobId,
            conversationId = conversationId,
            callId = callId,
            sourceLanguage = sourceLang,
            targetLanguage = targetLang,
            provider = provider,
            status = status
        )
        val logs = _auditLogs.value.toMutableList()
        logs.add(0, entry)
        _auditLogs.value = logs
    }
}
