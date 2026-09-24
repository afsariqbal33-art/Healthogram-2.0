package com.example.healthogram.auth

import java.util.Locale
import java.util.regex.Pattern

data class UsernameValidationResult(
    val isValid: Boolean,
    val normalizedUsername: String,
    val errorMessage: String? = null
)

data class CountryMetadata(
    val countryCode: String,
    val countryName: String,
    val phoneDialCode: String,
    val currencyCode: String,
    val defaultLanguage: String,
    val flagEmoji: String
)

object AuthValidators {

    val RESERVED_USERNAMES = setOf(
        "admin",
        "administrator",
        "healthogram",
        "support",
        "official",
        "security",
        "help",
        "owner",
        "root",
        "system",
        "moderator",
        "verification",
        "billing",
        "compliance",
        "legal",
        "emergency"
    )

    val SUPPORTED_COUNTRIES = listOf(
        CountryMetadata("SA", "Saudi Arabia", "+966", "SAR", "ar", "🇸🇦"),
        CountryMetadata("AE", "United Arab Emirates", "+971", "AED", "ar", "🇦🇪"),
        CountryMetadata("US", "United States", "+1", "USD", "en", "🇺🇸"),
        CountryMetadata("GB", "United Kingdom", "+44", "GBP", "en", "🇬🇧"),
        CountryMetadata("IN", "India", "+91", "INR", "en", "🇮🇳"),
        CountryMetadata("DE", "Germany", "+49", "EUR", "de", "🇩🇪"),
        CountryMetadata("FR", "France", "+33", "EUR", "fr", "🇫🇷"),
        CountryMetadata("EG", "Egypt", "+20", "EGP", "ar", "🇪🇬"),
        CountryMetadata("PK", "Pakistan", "+92", "PKR", "ur", "🇵🇰"),
        CountryMetadata("CA", "Canada", "+1", "CAD", "en", "🇨🇦")
    )

    private val EMAIL_REGEX = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$"
    )

    private val USERNAME_REGEX = Pattern.compile(
        "^[a-zA-Z0-9._]{3,30}$"
    )

    fun getCountry(code: String): CountryMetadata {
        return SUPPORTED_COUNTRIES.firstOrNull { it.countryCode.equals(code, ignoreCase = true) }
            ?: SUPPORTED_COUNTRIES.first { it.countryCode == "US" }
    }

    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        return EMAIL_REGEX.matcher(email.trim()).matches()
    }

    fun normalizeUsername(input: String): String {
        return input.trim().lowercase(Locale.ROOT)
    }

    fun validateUsername(username: String): UsernameValidationResult {
        val trimmed = username.trim()
        val normalized = normalizeUsername(trimmed)

        if (trimmed.length < 3) {
            return UsernameValidationResult(false, normalized, "Username must be at least 3 characters.")
        }
        if (trimmed.length > 30) {
            return UsernameValidationResult(false, normalized, "Username must be 30 characters or fewer.")
        }
        if (!USERNAME_REGEX.matcher(trimmed).matches()) {
            return UsernameValidationResult(false, normalized, "Username can only contain letters, numbers, periods, and underscores.")
        }
        if (trimmed.startsWith(".") || trimmed.endsWith(".") || trimmed.startsWith("_") || trimmed.endsWith("_")) {
            return UsernameValidationResult(false, normalized, "Username cannot start or end with a period or underscore.")
        }
        if (trimmed.contains("..") || trimmed.contains("__")) {
            return UsernameValidationResult(false, normalized, "Username cannot contain consecutive periods or underscores.")
        }
        if (RESERVED_USERNAMES.contains(normalized)) {
            return UsernameValidationResult(false, normalized, "This username is reserved by Healthogram.")
        }

        return UsernameValidationResult(true, normalized, null)
    }

    fun validatePasswordStrength(password: String): PasswordStrength {
        val feedback = mutableListOf<String>()
        var score = 0

        if (password.length < 8) {
            feedback.add("Must be at least 8 characters long")
        } else {
            score++
        }

        if (password.any { it.isUpperCase() }) {
            score++
        } else {
            feedback.add("Add at least one uppercase letter")
        }

        if (password.any { it.isLowerCase() }) {
            score++
        } else {
            feedback.add("Add at least one lowercase letter")
        }

        if (password.any { it.isDigit() }) {
            score++
        } else {
            feedback.add("Add at least one number")
        }

        val hasSpecial = password.any { !it.isLetterOrDigit() }
        if (hasSpecial) {
            score++
        } else {
            feedback.add("Add at least one special character (!@#\$%^&*)")
        }

        val level = when {
            score <= 1 -> PasswordStrengthLevel.VERY_WEAK
            score == 2 -> PasswordStrengthLevel.WEAK
            score == 3 -> PasswordStrengthLevel.FAIR
            score == 4 -> PasswordStrengthLevel.STRONG
            else -> PasswordStrengthLevel.VERY_STRONG
        }

        val isAcceptable = password.length >= 8 && (score >= 3)

        return PasswordStrength(
            score = score.coerceIn(0, 4),
            level = level,
            feedback = feedback,
            isAcceptable = isAcceptable
        )
    }

    fun normalizePhoneNumber(phone: String, dialCode: String): String {
        val cleanPhone = phone.filter { it.isDigit() }
        val cleanDial = dialCode.filter { it.isDigit() }

        return if (cleanPhone.startsWith(cleanDial)) {
            "+$cleanPhone"
        } else {
            "+$cleanDial$cleanPhone"
        }
    }
}
