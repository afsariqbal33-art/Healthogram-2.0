package com.example.healthogram.owner

import com.example.healthogram.core.AccountType
import java.util.UUID

/**
 * Feature availability states controlled by Platform Owner.
 */
enum class FeatureState {
    ON,
    OFF,
    MAINTENANCE,
    BETA,
    COMING_SOON
}

enum class PlatformFeature {
    SOCIAL_FEED,
    REELS_AND_VIDEOS,
    LIVE_STREAMING,
    MARKETPLACE_DOMESTIC,
    MARKETPLACE_INTERNATIONAL,
    HEALTH_PASSPORT_CORE,
    HEALTH_PASSPORT_QR_SHARING,
    AI_STUDIO_CREATOR,
    AI_STUDIO_SELLER,
    AI_STUDIO_HEALTHCARE,
    AI_IMAGE_TOOLS,
    AI_VIDEO_TOOLS,
    AI_CAPTION_TOOLS,
    AI_HASHTAG_TOOLS,
    AI_PRODUCT_CONTENT,
    AI_PRODUCT_IMAGE_TOOLS,
    AI_PRODUCT_VIDEO_TOOLS,
    AI_TRANSLATION_AI,
    AI_VOICE_TOOLS,
    AI_BETA,
    AUDIO_CALLS,
    VIDEO_CALLS,
    MESSAGING_CORE,
    MESSAGE_REQUESTS,
    VOICE_MESSAGES,
    MEDIA_MESSAGES,
    CALL_RECORDING,
    GROUP_CALLS,
    INTERNATIONAL_CALLS,
    COMMUNICATION_TRANSLATION,
    UNIVERSAL_TRANSLATION,
    TRANSLATION_CORE,
    TEXT_TRANSLATION,
    VOICE_TRANSLATION,
    CALL_TRANSLATION,
    LIVE_CAPTIONS,
    TRANSLATED_CAPTIONS,
    TRANSLATED_AUDIO,
    AUTO_LANGUAGE_DETECTION,
    TRANSLATION_HISTORY,
    TRANSLATION_BETA,
    PROFESSIONAL_ACCOUNTS,
    ONLINE_PAYMENTS,
    DELIVERY_DISPATCH
}

/**
 * Global and contextual feature flags.
 */
data class FeatureFlag(
    val feature: PlatformFeature,
    val state: FeatureState = FeatureState.ON,
    val enabledCountries: Set<String> = emptySet(), // Empty means all countries
    val disabledCountries: Set<String> = emptySet(),
    val allowedAccountTypes: Set<AccountType> = AccountType.entries.toSet(),
    val notes: String = ""
)

/**
 * Owner Financial Transaction Ledger Entry.
 */
data class FinancialLedgerEntry(
    val entryId: String = UUID.randomUUID().toString(),
    val transactionType: LedgerType,
    val grossAmount: Double,
    val platformCommission: Double,
    val serviceFee: Double,
    val paymentProcessingFee: Double,
    val taxAdjustment: Double,
    val netPlatformRevenue: Double,
    val currency: String = "USD",
    val countryCode: String = "US",
    val timestamp: Long = System.currentTimeMillis(),
    val referenceId: String
)

enum class LedgerType {
    MARKETPLACE_SALE,
    TELEHEALTH_CONSULTATION,
    ORGANIZATION_SUBSCRIPTION,
    REFUND,
    CHARGEBACK,
    OWNER_PAYOUT
}

/**
 * Owner Earnings Summary.
 */
data class OwnerEarningsSummary(
    val grossPlatformVolume: Double,
    val totalCommissionsEarned: Double,
    val totalServiceFeesEarned: Double,
    val totalProcessingCostDeductions: Double,
    val totalRefundsAndChargebacks: Double,
    val netAvailableBalance: Double,
    val pendingSettlementBalance: Double,
    val currency: String = "USD"
)
