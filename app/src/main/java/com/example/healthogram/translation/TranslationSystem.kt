package com.example.healthogram.translation

/**
 * Supported Languages for Universal Translation.
 */
enum class SupportedLanguage(val code: String, val displayName: String, val isRTL: Boolean = false) {
    ENGLISH("en", "English", false),
    ARABIC("ar", "العربية", true),
    SPANISH("es", "Español", false),
    FRENCH("fr", "Français", false),
    GERMAN("de", "Deutsch", false),
    HINDI("hi", "हिन्दी", false),
    CHINESE("zh", "中文", false);

    companion object {
        fun fromCode(code: String): SupportedLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

/**
 * Translation configuration preferences per user session.
 */
data class TranslationPreferences(
    val isTranslationEnabled: Boolean = true,
    val autoDetectSource: Boolean = true,
    val preferredTargetLanguage: SupportedLanguage = SupportedLanguage.ENGLISH,
    val showOriginalTextAlongside: Boolean = true,
    val showLiveCaptionsInCalls: Boolean = true,
    val enableTranslatedAudioVoice: Boolean = false,
    val userConsentAcknowledged: Boolean = true
) {
    companion object {
        /**
         * Mandatory regulatory & legal medical translation notice.
         */
        const val MEDICAL_INTERPRETATION_DISCLAIMER =
            "NOTICE: Healthogram universal translation is an automated accessibility aid. It is NOT a certified medical interpreter. For critical clinical consultations, utilize certified human medical interpreters."
    }
}

/**
 * Result of translating a communication payload.
 */
data class TranslatedMessage(
    val originalText: String,
    val translatedText: String,
    val detectedSourceLanguage: SupportedLanguage,
    val targetLanguage: SupportedLanguage,
    val confidence: Float = 0.98f,
    val isHealthcareContext: Boolean = false,
    val legalNotice: String? = if (isHealthcareContext) TranslationPreferences.MEDICAL_INTERPRETATION_DISCLAIMER else null
)

/**
 * Service orchestrating real-time translation with safety safeguards.
 */
class UniversalTranslationEngine {

    fun translateTextMessage(
        rawText: String,
        targetLanguage: SupportedLanguage,
        isHealthcareContext: Boolean = false
    ): TranslatedMessage {
        // High quality translation pipeline simulation (in production uses Cloud Translation API / On-Device MLKit)
        val sampleTranslation = when (targetLanguage) {
            SupportedLanguage.ARABIC -> "مرحباً بكم في منصة هيلثوجرام: $rawText"
            SupportedLanguage.SPANISH -> "Bienvenido a Healthogram: $rawText"
            SupportedLanguage.FRENCH -> "Bienvenue sur Healthogram: $rawText"
            else -> rawText
        }

        return TranslatedMessage(
            originalText = rawText,
            translatedText = sampleTranslation,
            detectedSourceLanguage = SupportedLanguage.ENGLISH,
            targetLanguage = targetLanguage,
            isHealthcareContext = isHealthcareContext
        )
    }
}
