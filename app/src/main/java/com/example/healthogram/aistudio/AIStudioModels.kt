package com.example.healthogram.aistudio

import com.example.healthogram.core.AccountType
import java.util.UUID

/**
 * Status lifecycle of an AI Job.
 */
enum class AIJobStatus {
    QUEUED,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    EXPIRED
}

/**
 * All supported AI tool types across Creator and Marketplace Seller suites.
 */
enum class AIToolType {
    // Creator Image Tools
    IMAGE_ENHANCEMENT,
    IMAGE_RETOUCH,
    BACKGROUND_REMOVAL,
    BACKGROUND_REPLACEMENT,
    IMAGE_RESIZE,
    IMAGE_QUALITY,
    LIGHTING_ENHANCE,
    PORTRAIT_ENHANCE,
    OBJECT_CLEANUP,

    // Creator Text & Content Tools
    CAPTION_GENERATOR,
    HASHTAG_GENERATOR,
    CONTENT_IDEAS,
    WRITING_IMPROVEMENT,
    PROMOTIONAL_TEXT,

    // Creator Video & Reel Tools
    VIDEO_ENHANCE,
    VIDEO_TRIM,
    REEL_HOOKS,
    REEL_TITLE_DESCRIPTION,

    // Seller Marketplace Specific Tools
    PRODUCT_IMAGE_ENHANCE,
    PRODUCT_BACKGROUND,
    PRODUCT_TITLE,
    PRODUCT_DESCRIPTION,
    PRODUCT_TAGS,
    PRODUCT_HIGHLIGHTS,
    MARKETING_CAPTION,
    PROMOTIONAL_BANNER,
    PRODUCT_VIDEO_ASSISTANCE
}

/**
 * Request category categorization.
 */
enum class AIRequestType {
    TEXT,
    IMAGE,
    VIDEO,
    MULTIMODAL
}

/**
 * Authoritative AI Job entity in Firestore: ai_jobs/{jobId}
 */
data class AIJob(
    val jobId: String = "job_${UUID.randomUUID().toString().take(12)}",
    val uid: String,
    val accountType: String, // individual, doctor, clinic, hospital, laboratory, seller
    val toolType: AIToolType,
    val requestType: AIRequestType,
    val status: AIJobStatus = AIJobStatus.QUEUED,
    val inputReference: String = "", // Storage path or sanitized text
    val outputReference: String = "", // Storage path or generated output text
    val provider: String = "gemini-3.5-flash",
    val model: String = "gemini-3.5-flash",
    val promptVersion: String = "v1.2",
    val language: String = "en",
    val country: String = "SA",
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val failedAt: Long? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val usageUnits: Int = 1,
    val estimatedCost: Double = 0.002,
    val actualCost: Double = 0.002,
    val isBillable: Boolean = true,
    val expiresAt: Long = System.currentTimeMillis() + 86400000L * 7 // 7 days retention
)

/**
 * AI Generated Asset entity in Firestore: ai_generated_assets/{assetId}
 */
data class AIGeneratedAsset(
    val assetId: String = "asset_${UUID.randomUUID().toString().take(12)}",
    val uid: String,
    val sourceJobId: String,
    val assetType: String, // image, video, banner, text_bundle
    val sourceType: String = "ai_generation",
    val sourceReference: String = "",
    val storagePath: String = "", // ai_private/{uid}/output/{jobId}/
    val thumbnailPath: String = "", // ai_private/{uid}/thumbnail/{jobId}/
    val mimeType: String = "image/webp",
    val fileSize: Long = 0L,
    val provider: String = "gemini-2.5-flash-image",
    val model: String = "gemini-2.5-flash-image",
    val promptVersion: String = "v1.0",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 86400000L * 30, // 30 days retention
    val status: String = "active", // active, deleted, expired
    val deletedAt: Long? = null
)

/**
 * AI Transaction Usage record in Firestore: ai_usage/{usageId}
 */
data class AIUsage(
    val usageId: String = "use_${UUID.randomUUID().toString().take(12)}",
    val uid: String,
    val accountType: String,
    val country: String = "SA",
    val toolType: AIToolType,
    val units: Int = 1,
    val provider: String = "gemini-3.5-flash",
    val model: String = "gemini-3.5-flash",
    val estimatedCost: Double = 0.002,
    val actualCost: Double = 0.002,
    val createdAt: Long = System.currentTimeMillis(),
    val billingPeriod: String = "2026-09"
)

/**
 * Authoritative User-Level Aggregate in Firestore: ai_usage_summary/{uid}
 * Client cannot write to this document directly.
 */
data class AIUsageSummary(
    val uid: String,
    val monthlyUnits: Int = 0,
    val monthlyImageJobs: Int = 0,
    val monthlyVideoJobs: Int = 0,
    val monthlyTextJobs: Int = 0,
    val monthlyEstimatedCost: Double = 0.0,
    val currentPlan: String = "standard_tier",
    val limit: Int = 200, // Monthly quota
    val remaining: Int = 200,
    val resetDate: Long = System.currentTimeMillis() + 86400000L * 30
)

/**
 * User AI History item in Firestore: ai_history/{historyId}
 */
data class AIHistoryItem(
    val historyId: String = "hist_${UUID.randomUUID().toString().take(12)}",
    val uid: String,
    val jobId: String,
    val toolType: AIToolType,
    val inputType: String, // text, image, video
    val outputType: String, // text, image, video
    val title: String,
    val thumbnail: String = "",
    val status: AIJobStatus = AIJobStatus.COMPLETED,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * User AI Settings in Firestore: ai_settings/{uid}
 */
data class AISettings(
    val uid: String,
    val aiEnabled: Boolean = true,
    val preferredLanguage: String = "en",
    val defaultTone: String = "Professional",
    val defaultCaptionLength: String = "Medium",
    val saveHistory: Boolean = true,
    val allowPersonalization: Boolean = true,
    val marketingAiEnabled: Boolean = true,
    val sellerAiEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Immutable Audit Log: ai_audit_logs/{logId}
 */
data class AIAuditLog(
    val logId: String = "ailog_${UUID.randomUUID().toString().take(12)}",
    val actorUid: String,
    val actorRole: String,
    val action: String,
    val toolType: AIToolType,
    val jobId: String = "",
    val targetUid: String = "",
    val provider: String = "gemini-3.5-flash",
    val result: String = "SUCCESS",
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val country: String = "SA",
    val deviceId: String = "android-device",
    val ipHash: String = "sha256_client_ip"
)

/**
 * Global Admin AI Configuration: admin_ai_config
 */
data class AdminAIConfig(
    val aiEnabled: Boolean = true,
    val creatorAiEnabled: Boolean = true,
    val sellerAiEnabled: Boolean = true,
    val imageAiEnabled: Boolean = true,
    val videoAiEnabled: Boolean = true,
    val textAiEnabled: Boolean = true,
    val translationAiEnabled: Boolean = false,
    val voiceAiEnabled: Boolean = false,
    val defaultProvider: String = "gemini-3.5-flash",
    val defaultModel: String = "gemini-3.5-flash",
    val fallbackProvider: String = "gemini-3.1-flash-lite-preview",
    val dailyLimit: Int = 50,
    val monthlyLimit: Int = 500,
    val maxFileSize: Long = 25 * 1024 * 1024L, // 25MB
    val maxVideoDuration: Int = 180, // 3 minutes
    val countryRules: Map<String, Boolean> = mapOf("SA" to true, "AE" to true, "US" to true),
    val maintenanceMode: Boolean = false,
    val updatedBy: String = "platform_owner",
    val updatedAt: Long = System.currentTimeMillis()
)

// -------------------------------------------------------------------------
// Typed Results from AI Studio operations
// -------------------------------------------------------------------------

data class AICaptionResult(
    val caption: String,
    val alternativeCaptions: List<String> = emptyList(),
    val ctaSuggestion: String = "",
    val hashtags: List<String> = emptyList()
)

data class AIHashtagResult(
    val recommendedHashtags: List<String> = emptyList(),
    val categoryTags: List<String> = emptyList()
)

data class AIContentIdea(
    val title: String,
    val concept: String,
    val contentType: String, // Post, Reel, Story, Live topic
    val hook: String,
    val captionIdea: String,
    val suggestedVisual: String,
    val suggestedCta: String
)

data class AIProductTitleResult(
    val suggestedTitle: String,
    val alternativeTitles: List<String> = emptyList(),
    val keywords: List<String> = emptyList()
)

data class AIProductDescriptionResult(
    val shortDescription: String,
    val detailedDescription: String,
    val bulletPoints: List<String> = emptyList(),
    val keyFeatures: List<String> = emptyList(),
    val usageInformation: String = "",
    val seoKeywords: List<String> = emptyList()
)

data class AIProductTagsResult(
    val tags: List<String> = emptyList(),
    val searchKeywords: List<String> = emptyList(),
    val categoryFit: String = ""
)

data class AIMarketingCreativeResult(
    val headline: String,
    val bannerText: String,
    val callToAction: String,
    val colorPaletteSuggestion: List<String> = emptyList(),
    val copyText: String
)

data class AIVideoAssistanceResult(
    val reelTitle: String,
    val reelDescription: String,
    val reelHooks: List<String> = emptyList(),
    val sceneSuggestions: List<String> = emptyList(),
    val suggestedCaptions: List<String> = emptyList(),
    val audioSuggestions: List<String> = emptyList(),
    val thumbnailSuggestion: String = ""
)

data class AIImageToolResult(
    val originalImageUrl: String,
    val resultImageUrl: String,
    val toolType: AIToolType,
    val enhancementSummary: String
)
