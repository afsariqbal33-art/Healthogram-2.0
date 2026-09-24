package com.example.healthogram.aistudio

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * FlutterFlow Custom Actions for Healthogram AI Studio.
 * Bridges UI interactions to the reactive repository and Cloud Functions backend.
 */
object AIStudioCustomActions {

    private val repository = AIStudioRepository.getInstance()

    suspend fun createAIJob(
        uid: String,
        accountType: String,
        toolType: AIToolType,
        requestType: AIRequestType,
        inputReference: String
    ): AIJob = withContext(Dispatchers.IO) {
        repository.submitJob(
            uid = uid,
            accountType = accountType,
            toolType = toolType,
            requestType = requestType,
            inputReference = inputReference
        )
    }

    suspend fun generateAICaption(
        topic: String,
        tone: String = "Professional",
        language: String = "en",
        length: String = "Medium",
        keywords: String = ""
    ): AICaptionResult = withContext(Dispatchers.IO) {
        AIModerationLayer.validateInputContent(topic)
        repository.providerAdapter.generateCaption(topic, tone, language, length, keywords)
    }

    suspend fun generateAIHashtags(
        topic: String,
        language: String = "en",
        count: Int = 10
    ): AIHashtagResult = withContext(Dispatchers.IO) {
        AIModerationLayer.validateInputContent(topic)
        repository.providerAdapter.generateHashtags(topic, language, count)
    }

    suspend fun generateContentIdeas(
        topic: String,
        contentType: String = "Reel",
        audience: String = "Patients & Wellness Seekers"
    ): List<AIContentIdea> = withContext(Dispatchers.IO) {
        AIModerationLayer.validateInputContent(topic)
        repository.providerAdapter.generateContentIdeas(topic, contentType, audience)
    }

    suspend fun enhanceAIImage(
        imageUrl: String,
        contrastOption: String = "standard",
        hdrOption: String = "high"
    ): AIImageToolResult = withContext(Dispatchers.IO) {
        repository.providerAdapter.processImage(
            imageUrl = imageUrl,
            toolType = AIToolType.IMAGE_ENHANCEMENT,
            options = mapOf("contrast" to contrastOption, "hdr" to hdrOption)
        )
    }

    suspend fun removeAIBackground(
        imageUrl: String
    ): AIImageToolResult = withContext(Dispatchers.IO) {
        repository.providerAdapter.processImage(
            imageUrl = imageUrl,
            toolType = AIToolType.BACKGROUND_REMOVAL,
            options = emptyMap()
        )
    }

    suspend fun generateProductTitle(
        brand: String,
        productName: String,
        category: String,
        specs: String
    ): AIProductTitleResult = withContext(Dispatchers.IO) {
        AIModerationLayer.validateInputContent("$productName $specs")
        repository.providerAdapter.generateProductTitle(brand, productName, category, specs)
    }

    suspend fun generateProductDescription(
        title: String,
        category: String,
        specs: String,
        features: String
    ): AIProductDescriptionResult = withContext(Dispatchers.IO) {
        AIModerationLayer.validateInputContent("$title $specs $features")
        repository.providerAdapter.generateProductDescription(title, category, specs, features)
    }

    suspend fun generateProductTags(
        title: String,
        category: String,
        specs: String
    ): AIProductTagsResult = withContext(Dispatchers.IO) {
        AIModerationLayer.validateInputContent("$title $specs")
        repository.providerAdapter.generateProductTags(title, category, specs)
    }

    suspend fun generateMarketingCreative(
        productName: String,
        promotionGoal: String,
        discountPercent: String
    ): AIMarketingCreativeResult = withContext(Dispatchers.IO) {
        AIModerationLayer.validateInputContent(productName)
        repository.providerAdapter.generateMarketingCreative(productName, promotionGoal, discountPercent)
    }

    suspend fun startAIVideoJob(
        topic: String,
        style: String = "Educational Reel"
    ): AIVideoAssistanceResult = withContext(Dispatchers.IO) {
        AIModerationLayer.validateInputContent(topic)
        repository.providerAdapter.assistVideo(topic, style)
    }

    suspend fun getAIJobStatus(jobId: String): AIJobStatus = withContext(Dispatchers.IO) {
        repository.jobs.value.find { it.jobId == jobId }?.status ?: AIJobStatus.FAILED
    }

    suspend fun cancelAIJob(jobId: String, callerUid: String): Boolean = withContext(Dispatchers.IO) {
        val job = repository.jobs.value.find { it.jobId == jobId } ?: return@withContext false
        if (job.uid != callerUid) {
            throw SecurityException("Security Violation: Cannot cancel another user's job.")
        }
        true
    }

    suspend fun retryAIJob(jobId: String, callerUid: String): AIJob = withContext(Dispatchers.IO) {
        val job = repository.jobs.value.find { it.jobId == jobId }
            ?: throw NoSuchElementException("Job not found")
        if (job.uid != callerUid) {
            throw SecurityException("Security Violation: Cannot retry another user's job.")
        }
        repository.submitJob(
            uid = callerUid,
            accountType = job.accountType,
            toolType = job.toolType,
            requestType = job.requestType,
            inputReference = job.inputReference
        )
    }

    suspend fun getAIHistory(uid: String): List<AIHistoryItem> = withContext(Dispatchers.IO) {
        repository.history.value.filter { it.uid == uid }
    }

    suspend fun getAIUsage(uid: String): AIUsageSummary = withContext(Dispatchers.IO) {
        repository.usageManager.getUsageSummary(uid)
    }

    suspend fun deleteAIGeneratedAsset(assetId: String, callerUid: String): Unit = withContext(Dispatchers.IO) {
        repository.deleteAsset(assetId, callerUid)
    }
}

/**
 * FlutterFlow Custom Functions for formatting and status presentation.
 */
object AIStudioCustomFunctions {

    fun getAIJobStatusLabel(status: AIJobStatus): String = when (status) {
        AIJobStatus.QUEUED -> "In Queue"
        AIJobStatus.PROCESSING -> "Processing..."
        AIJobStatus.COMPLETED -> "Ready & Completed"
        AIJobStatus.FAILED -> "Failed"
        AIJobStatus.CANCELLED -> "Cancelled"
        AIJobStatus.EXPIRED -> "Expired"
    }

    fun getAIJobStatusIcon(status: AIJobStatus): String = when (status) {
        AIJobStatus.QUEUED -> "hourglass_empty"
        AIJobStatus.PROCESSING -> "sync"
        AIJobStatus.COMPLETED -> "check_circle"
        AIJobStatus.FAILED -> "error"
        AIJobStatus.CANCELLED -> "cancel"
        AIJobStatus.EXPIRED -> "history"
    }

    fun getAIUsagePercentage(summary: AIUsageSummary): Float {
        if (summary.limit <= 0) return 0f
        return (summary.monthlyUnits.toFloat() / summary.limit.toFloat()).coerceIn(0f, 1f)
    }

    fun getAIUsageRemaining(summary: AIUsageSummary): Int {
        return summary.remaining.coerceAtLeast(0)
    }

    fun getAIToolLabel(toolType: AIToolType): String = when (toolType) {
        AIToolType.IMAGE_ENHANCEMENT -> "Image Neural Enhancer"
        AIToolType.IMAGE_RETOUCH -> "Smart Retouch"
        AIToolType.BACKGROUND_REMOVAL -> "Background Eraser"
        AIToolType.BACKGROUND_REPLACEMENT -> "Studio Backdrop"
        AIToolType.IMAGE_RESIZE -> "Smart Aspect Ratio"
        AIToolType.IMAGE_QUALITY -> "Resolution Upscaler"
        AIToolType.LIGHTING_ENHANCE -> "Studio Lighting Optimizer"
        AIToolType.PORTRAIT_ENHANCE -> "Clinical Portrait Tuning"
        AIToolType.OBJECT_CLEANUP -> "Object Remover"
        AIToolType.CAPTION_GENERATOR -> "Healthcare Caption Writer"
        AIToolType.HASHTAG_GENERATOR -> "Smart Hashtag Engine"
        AIToolType.CONTENT_IDEAS -> "Educational Content Ideas"
        AIToolType.WRITING_IMPROVEMENT -> "Copy Polisher & Tone Shifter"
        AIToolType.PROMOTIONAL_TEXT -> "Marketing Text Generator"
        AIToolType.VIDEO_ENHANCE -> "Video Clarity Optimizer"
        AIToolType.VIDEO_TRIM -> "Smart Reel Trimmer"
        AIToolType.REEL_HOOKS -> "Viral Hook Formulator"
        AIToolType.REEL_TITLE_DESCRIPTION -> "Reel Metadata Formatter"
        AIToolType.PRODUCT_IMAGE_ENHANCE -> "Product Photo Enhancer"
        AIToolType.PRODUCT_BACKGROUND -> "Marketplace Studio Backdrop"
        AIToolType.PRODUCT_TITLE -> "Compliant Product Title Writer"
        AIToolType.PRODUCT_DESCRIPTION -> "E-commerce Spec & Description"
        AIToolType.PRODUCT_TAGS -> "Search Tags & SEO Formatter"
        AIToolType.PRODUCT_HIGHLIGHTS -> "Clinical Highlight Bullets"
        AIToolType.MARKETING_CAPTION -> "Promo Copy & Campaign Creator"
        AIToolType.PROMOTIONAL_BANNER -> "Deal Banner Designer"
        AIToolType.PRODUCT_VIDEO_ASSISTANCE -> "Marketplace Reel Assistant"
    }

    fun getAIToolDescription(toolType: AIToolType): String = when (toolType) {
        AIToolType.IMAGE_ENHANCEMENT -> "Auto-balance brightness, shadow, and sharpness with neural filters."
        AIToolType.BACKGROUND_REMOVAL -> "Isolate subjects and products cleanly with one tap."
        AIToolType.CAPTION_GENERATOR -> "Generate engaging, certified educational copy tailored to your audience."
        AIToolType.HASHTAG_GENERATOR -> "Curate optimal medical and wellness discoverability tags."
        AIToolType.CONTENT_IDEAS -> "Brainstorm high-converting health awareness topics and hooks."
        AIToolType.PRODUCT_DESCRIPTION -> "Craft professional medical supply specs and compliant descriptions."
        AIToolType.PRODUCT_TITLE -> "Draft high-converting, policy-compliant e-commerce product titles."
        AIToolType.PROMOTIONAL_BANNER -> "Generate banners and promotional text for seasonal storefront campaigns."
        else -> "AI-powered assistant designed specifically for Healthogram creators and sellers."
    }

    fun getAICostLabel(cost: Double): String {
        return "$${String.format("%.3f", cost)} USD"
    }

    fun getAIFailureMessage(errorCode: String?): String = when (errorCode) {
        "QUOTA_EXCEEDED" -> "Monthly AI usage limit reached. Please wait for the cycle reset or upgrade."
        "CONTENT_MODERATION_FAILED" -> "Content flagged by Healthogram safety filters for prohibited health claims."
        "INVALID_FILE_FORMAT" -> "Unsupported file format. Please upload JPG, PNG, or WEBP."
        "FILE_TOO_LARGE" -> "Uploaded file exceeds the 25MB maximum limit."
        else -> "Generation failed. Please retry or adjust prompt."
    }

    fun getAIGenerationTimeLabel(startedAt: Long?, completedAt: Long?): String {
        if (startedAt == null || completedAt == null) return "Instant"
        val seconds = ((completedAt - startedAt) / 1000).coerceAtLeast(1)
        return "${seconds}s"
    }

    fun isAIToolEnabled(toolType: AIToolType, config: AdminAIConfig): Boolean {
        if (!config.aiEnabled) return false
        val isSeller = toolType.name.startsWith("PRODUCT") || toolType == AIToolType.PROMOTIONAL_BANNER
        if (isSeller && !config.sellerAiEnabled) return false
        if (!isSeller && !config.creatorAiEnabled) return false
        return true
    }

    fun isAIUsageLimitReached(summary: AIUsageSummary): Boolean {
        return summary.remaining <= 0
    }

    fun getAIAccountAccess(accountType: String, toolType: AIToolType): Boolean {
        val isSellerTool = toolType.name.startsWith("PRODUCT_") || toolType == AIToolType.PROMOTIONAL_BANNER
        if (isSellerTool && accountType.lowercase() == "customer") {
            return false
        }
        return true
    }

    fun getAIProviderLabel(provider: String): String = when {
        provider.contains("gemini", ignoreCase = true) -> "Google Gemini AI"
        provider.contains("veo", ignoreCase = true) -> "Google Veo"
        else -> "Healthogram AI Engine"
    }
}
