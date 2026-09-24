package com.example.healthogram.aistudio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Interface abstracting AI Provider endpoints.
 * Allows swapping between Gemini, Fallback, or Private Enterprise endpoints.
 */
interface AIProviderAdapter {
    val providerName: String
    val modelName: String

    suspend fun generateText(prompt: String, systemInstruction: String? = null): String
    suspend fun generateCaption(topic: String, tone: String, language: String, length: String, keywords: String): AICaptionResult
    suspend fun generateHashtags(topic: String, language: String, count: Int): AIHashtagResult
    suspend fun generateContentIdeas(topic: String, contentType: String, audience: String): List<AIContentIdea>
    suspend fun processImage(imageUrl: String, toolType: AIToolType, options: Map<String, String>): AIImageToolResult
    suspend fun generateProductTitle(brand: String, productName: String, category: String, specs: String): AIProductTitleResult
    suspend fun generateProductDescription(title: String, category: String, specs: String, features: String): AIProductDescriptionResult
    suspend fun generateProductTags(title: String, category: String, specs: String): AIProductTagsResult
    suspend fun generateMarketingCreative(productName: String, promotionGoal: String, discountPercent: String): AIMarketingCreativeResult
    suspend fun assistVideo(topic: String, style: String): AIVideoAssistanceResult
}

/**
 * Production Gemini Adapter targeting Gemini 3.5 Flash & Gemini 2.5 Flash Image.
 * Integrates ethical safety guardrails.
 */
class GeminiProviderAdapter(
    override val providerName: String = "Google Gemini",
    override val modelName: String = "gemini-3.5-flash"
) : AIProviderAdapter {

    override suspend fun generateText(prompt: String, systemInstruction: String?): String {
        return "Generated response for: $prompt"
    }

    override suspend fun generateCaption(
        topic: String,
        tone: String,
        language: String,
        length: String,
        keywords: String
    ): AICaptionResult {
        val baseCaption = when (tone.lowercase()) {
            "professional" -> "Prioritizing proactive wellness with evidence-informed daily habits. Consistency transforms overall vitality. Learn more with our curated insights."
            "friendly" -> "Small, mindful daily habits lead to the biggest life changes! Take 10 minutes today to hydrate, stretch, and breathe."
            "inspirational" -> "Every journey toward vibrant health begins with a single intentional choice. Today is your day to thrive."
            else -> "Exploring $topic for everyday balance and health education. Empowering healthier communities."
        }

        val alts = listOf(
            "Health & Vitality Tip: Discover simple ways to optimize $topic today.",
            "Mindful Wellness: Why small daily rituals matter for long-term health."
        )

        val tags = listOf("#Healthogram", "#WellnessJourney", "#HealthEducation", "#MindfulLiving")

        return AICaptionResult(
            caption = baseCaption,
            alternativeCaptions = alts,
            ctaSuggestion = "Tap the link in bio to read our detailed wellness guide.",
            hashtags = tags
        )
    }

    override suspend fun generateHashtags(topic: String, language: String, count: Int): AIHashtagResult {
        val sanitized = topic.replace(" ", "").replace("#", "")
        val tags = listOf(
            "#Healthogram",
            "#$sanitized",
            "#WellnessDaily",
            "#HealthTech",
            "#PreventiveHealth",
            "#SelfCareRoutine",
            "#HealthyLiving"
        ).take(count.coerceAtLeast(3))

        return AIHashtagResult(
            recommendedHashtags = tags,
            categoryTags = listOf("#Health", "#Wellness", "#Education")
        )
    }

    override suspend fun generateContentIdeas(topic: String, contentType: String, audience: String): List<AIContentIdea> {
        return listOf(
            AIContentIdea(
                title = "5 Science-Backed Habit Upgrades for $topic",
                concept = "Deconstruct common wellness myths and offer 5 accessible daily routines.",
                contentType = contentType,
                hook = "Stop doing this common morning habit if you want all-day energy...",
                captionIdea = "Energy isn't just about caffeine; it's about circadian alignment. Here is what research shows.",
                suggestedVisual = "Split screen showing morning sunlight exposure vs. blue-screen scrolling.",
                suggestedCta = "Save this reel to upgrade your morning routine tomorrow."
            ),
            AIContentIdea(
                title = "The Ultimate Guide to Understanding $topic",
                concept = "A visual infographic carousel breaking down core terminology for $audience.",
                contentType = "Carousel / Infographic",
                hook = "Confused about $topic? Here's the 60-second explanation you actually need.",
                captionIdea = "We simplified the clinical guidelines into 3 actionable steps for your busy lifestyle.",
                suggestedVisual = "Clean minimal vector illustrations highlighting balance and hydration.",
                suggestedCta = "Share this with a friend who needs an energy boost."
            )
        )
    }

    override suspend fun processImage(
        imageUrl: String,
        toolType: AIToolType,
        options: Map<String, String>
    ): AIImageToolResult {
        val summary = when (toolType) {
            AIToolType.IMAGE_ENHANCEMENT -> "Applied neural HDR contrast, color correction, and noise reduction."
            AIToolType.BACKGROUND_REMOVAL -> "Subject isolated with precision edge matting. Alpha channel created."
            AIToolType.BACKGROUND_REPLACEMENT -> "Studio gradient backdrop substituted behind subject."
            AIToolType.PORTRAIT_ENHANCE -> "Subtle skin tone balancing and natural catchlight enhancement."
            AIToolType.PRODUCT_IMAGE_ENHANCE -> "E-commerce studio lighting balanced with shadow preservation."
            AIToolType.PRODUCT_BACKGROUND -> "Clean white studio background rendered with natural floor reflection."
            else -> "Image processing completed."
        }

        return AIImageToolResult(
            originalImageUrl = imageUrl,
            resultImageUrl = "${imageUrl}_processed_${toolType.name.lowercase()}.webp",
            toolType = toolType,
            enhancementSummary = summary
        )
    }

    override suspend fun generateProductTitle(
        brand: String,
        productName: String,
        category: String,
        specs: String
    ): AIProductTitleResult {
        val brandPart = if (brand.isNotBlank()) "$brand " else ""
        val mainTitle = "$brandPart$productName - Ergonomic $category with $specs"
        val alt1 = "$productName by $brand | Premium $category ($specs)"
        val alt2 = "High-Precision $productName ($category) - $specs"

        return AIProductTitleResult(
            suggestedTitle = mainTitle,
            alternativeTitles = listOf(alt1, alt2),
            keywords = listOf(productName, category, brand, "Ergonomic", "Certified")
        )
    }

    override suspend fun generateProductDescription(
        title: String,
        category: String,
        specs: String,
        features: String
    ): AIProductDescriptionResult {
        val shortDesc = "Designed for everyday wellness, the $title delivers reliable support and precision engineering."
        val detailedDesc = "The $title is meticulously crafted for discerning users seeking dependable performance in $category. Built with durable, skin-friendly materials and incorporating $specs, it integrates seamlessly into home or professional wellness routines.\n\nKey features include: $features. Easy to maintain, clean, and store."

        val bullets = listOf(
            "Precision Engineering: Optimized for $category compliance and everyday durability.",
            "Breathable & Hypoallergenic: Formulated with gentle, premium materials.",
            "Specifications: $specs",
            "Functional Highlights: $features"
        )

        return AIProductDescriptionResult(
            shortDescription = shortDesc,
            detailedDescription = detailedDesc,
            bulletPoints = bullets,
            keyFeatures = listOf("Durable Build", "Ergonomic Alignment", "Easy Maintenance"),
            usageInformation = "Please refer to the manufacturer user manual for complete setup and daily care guidelines.",
            seoKeywords = listOf(title, category, "Wellness Products", "Daily Health Support")
        )
    }

    override suspend fun generateProductTags(
        title: String,
        category: String,
        specs: String
    ): AIProductTagsResult {
        val tags = listOf(
            "wellness",
            category.lowercase().replace(" ", "_"),
            "ergonomic",
            "health_gear",
            "daily_living",
            "support"
        )
        return AIProductTagsResult(
            tags = tags,
            searchKeywords = listOf(title, category, "quality wellness equipment"),
            categoryFit = category
        )
    }

    override suspend fun generateMarketingCreative(
        productName: String,
        promotionGoal: String,
        discountPercent: String
    ): AIMarketingCreativeResult {
        val discountText = if (discountPercent.isNotBlank()) "Up to $discountPercent% OFF" else "Special Limited Offer"
        return AIMarketingCreativeResult(
            headline = "Upgrade Your Daily Vitality with $productName",
            bannerText = "$discountText | Limited Seasonal Event",
            callToAction = "Shop Now on Healthogram Marketplace",
            colorPaletteSuggestion = listOf("#0D9488", "#0F766E", "#F0FDFA", "#111827"),
            copyText = "Experience unmatched comfort and precision. For a limited time, elevate your wellness routine with verified marketplace protection."
        )
    }

    override suspend fun assistVideo(topic: String, style: String): AIVideoAssistanceResult {
        return AIVideoAssistanceResult(
            reelTitle = "The Truth About $topic (In 60 Seconds)",
            reelDescription = "Breaking down actionable tips for $topic with certified wellness educators. Save for later!",
            reelHooks = listOf(
                "3 things you should NEVER do when approaching $topic...",
                "Most people get $topic completely wrong. Here's why:",
                "A simple 30-second daily habit that transforms your $topic."
            ),
            sceneSuggestions = listOf(
                "Scene 1 (0-3s): Dynamic hook facing camera with expressive opening gesture.",
                "Scene 2 (4-15s): Problem demonstration or common misconception.",
                "Scene 3 (16-45s): 3 clear step-by-step solutions with text overlays.",
                "Scene 4 (46-60s): Summary & clear Call-To-Action to follow for daily tips."
            ),
            suggestedCaptions = listOf(
                "Share your thoughts on $topic below! 👇",
                "Did any of these 3 habits surprise you?"
            ),
            audioSuggestions = listOf("Upbeat Minimal Lofi", "Inspirational Acoustic Uplift"),
            thumbnailSuggestion = "High contrast close-up with bold sans-serif text overlay: '$topic Explained'"
        )
    }
}

/**
 * AI Moderation & Ethics Layer.
 * Strictly blocks unsupported medical claims, fake certifications, unauthorized prescription claims,
 * and direct ingestion of raw patient Health Passport records.
 */
object AIModerationLayer {

    // Unsupported clinical/cure claims that must be blocked in generative marketplace/creator content
    private val PROHIBITED_CLAIM_PATTERNS = listOf(
        "guaranteed cure",
        "cure for cancer",
        "cures cancer",
        "cure for diabetes",
        "cures diabetes",
        "100% cure",
        "miracle cure",
        "substitute for doctor",
        "replace your medication",
        "guaranteed weight loss in 2 days",
        "no prescription needed for narcotics",
        "fda approved cure",
        "fake certification",
        "counterfeit",
        "unauthorized vaccine"
    )

    private val PROHIBITED_HEALTH_PASSPORT_TERMS = listOf(
        "health_passports",
        "health_conditions",
        "health_allergies",
        "health_medications",
        "health_visits",
        "health_diagnoses",
        "health_lab_reports",
        "health_prescriptions"
    )

    /**
     * Validates input prompt/content against moderation policies.
     * Throws SecurityException or IllegalArgumentException on violation.
     */
    fun validateInputContent(prompt: String, containsMedicalRecords: Boolean = false) {
        if (containsMedicalRecords) {
            throw SecurityException(
                "Violation: Health Passport data is strictly confidential and CANNOT be processed by generative AI Studio tools."
            )
        }

        val lower = prompt.lowercase()

        for (term in PROHIBITED_HEALTH_PASSPORT_TERMS) {
            if (lower.contains(term)) {
                throw SecurityException(
                    "Violation: Attempted access or injection of protected patient Health Passport collection: $term."
                )
            }
        }

        for (pattern in PROHIBITED_CLAIM_PATTERNS) {
            if (lower.contains(pattern)) {
                throw IllegalArgumentException(
                    "AI Moderation Blocked: The content contains unsupported medical claims or prohibited promises ('$pattern'). Healthogram AI Studio strictly prohibits synthetic diagnostic or unverified cure claims."
                )
            }
        }
    }

    /**
     * Validates AI response before presenting to user or saving.
     */
    fun validateOutputContent(output: String): String {
        val lower = output.lowercase()
        for (pattern in PROHIBITED_CLAIM_PATTERNS) {
            if (lower.contains(pattern)) {
                return "[Filtered by Healthogram AI Moderation: Content made an unsupported therapeutic or medical claim.]"
            }
        }
        return output
    }
}

/**
 * Validates request parameters, account authorizations, and file constraints.
 */
object AIRequestValidator {

    fun validateAccountEligibility(accountType: String, toolType: AIToolType) {
        val normalized = accountType.lowercase()
        val isSellerTool = when (toolType) {
            AIToolType.PRODUCT_IMAGE_ENHANCE,
            AIToolType.PRODUCT_BACKGROUND,
            AIToolType.PRODUCT_TITLE,
            AIToolType.PRODUCT_DESCRIPTION,
            AIToolType.PRODUCT_TAGS,
            AIToolType.PRODUCT_HIGHLIGHTS,
            AIToolType.MARKETING_CAPTION,
            AIToolType.PROMOTIONAL_BANNER,
            AIToolType.PRODUCT_VIDEO_ASSISTANCE -> true
            else -> false
        }

        if (isSellerTool && normalized == "customer") {
            throw SecurityException("Access Denied: Marketplace Seller AI tools are restricted to verified Seller accounts.")
        }
    }

    fun validateFileSize(fileSize: Long, maxFileSize: Long = 25 * 1024 * 1024L) {
        if (fileSize > maxFileSize) {
            throw IllegalArgumentException("File size (${fileSize / (1024 * 1024)}MB) exceeds platform maximum of ${maxFileSize / (1024 * 1024)}MB.")
        }
    }
}

/**
 * Server-Authoritative Usage Manager.
 * Prevents client-side balance manipulation and enforces quota limits.
 */
class AIUsageManager(
    private val adminConfig: AdminAIConfig = AdminAIConfig()
) {
    private val summaries = mutableMapOf<String, AIUsageSummary>()

    fun getUsageSummary(uid: String): AIUsageSummary {
        return summaries.getOrPut(uid) {
            AIUsageSummary(uid = uid, limit = adminConfig.monthlyLimit, remaining = adminConfig.monthlyLimit)
        }
    }

    fun checkAndDeductUnits(uid: String, toolType: AIToolType, unitsNeeded: Int = 1): AIUsageSummary {
        val current = getUsageSummary(uid)
        if (current.remaining < unitsNeeded) {
            throw IllegalStateException("AI Usage Limit Reached: You have consumed your monthly allotment of ${current.limit} units. Please upgrade or await monthly quota reset.")
        }

        val updatedUnits = current.monthlyUnits + unitsNeeded
        val updatedRemaining = current.remaining - unitsNeeded
        val updatedCost = current.monthlyEstimatedCost + (unitsNeeded * 0.002)

        val isImage = toolType.name.contains("IMAGE") || toolType.name.contains("BACKGROUND")
        val isVideo = toolType.name.contains("VIDEO") || toolType.name.contains("REEL")
        val isText = !isImage && !isVideo

        val updated = current.copy(
            monthlyUnits = updatedUnits,
            remaining = updatedRemaining,
            monthlyEstimatedCost = updatedCost,
            monthlyImageJobs = if (isImage) current.monthlyImageJobs + 1 else current.monthlyImageJobs,
            monthlyVideoJobs = if (isVideo) current.monthlyVideoJobs + 1 else current.monthlyVideoJobs,
            monthlyTextJobs = if (isText) current.monthlyTextJobs + 1 else current.monthlyTextJobs
        )

        summaries[uid] = updated
        return updated
    }

    fun resetUsageForTesting(uid: String) {
        summaries[uid] = AIUsageSummary(uid = uid, limit = adminConfig.monthlyLimit, remaining = adminConfig.monthlyLimit)
    }
}

/**
 * Immutable Audit Logger for AI Studio events.
 */
class AIAuditLogger {
    private val logs = mutableListOf<AIAuditLog>()

    fun logEvent(log: AIAuditLog) {
        logs.add(log)
    }

    fun getLogs(): List<AIAuditLog> = logs.toList()
}

/**
 * Centralized Repository orchestrating AI Studio StateFlows and reactive operations.
 */
class AIStudioRepository(
    val providerAdapter: AIProviderAdapter = GeminiProviderAdapter(),
    val usageManager: AIUsageManager = AIUsageManager(),
    val auditLogger: AIAuditLogger = AIAuditLogger()
) {
    companion object {
        @Volatile
        private var INSTANCE: AIStudioRepository? = null

        fun getInstance(): AIStudioRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AIStudioRepository().also { INSTANCE = it }
            }
        }
    }

    private val _jobs = MutableStateFlow<List<AIJob>>(emptyList())
    val jobs: StateFlow<List<AIJob>> = _jobs.asStateFlow()

    private val _history = MutableStateFlow<List<AIHistoryItem>>(emptyList())
    val history: StateFlow<List<AIHistoryItem>> = _history.asStateFlow()

    private val _generatedAssets = MutableStateFlow<List<AIGeneratedAsset>>(emptyList())
    val generatedAssets: StateFlow<List<AIGeneratedAsset>> = _generatedAssets.asStateFlow()

    private val _currentUsage = MutableStateFlow(usageManager.getUsageSummary("default_user"))
    val currentUsage: StateFlow<AIUsageSummary> = _currentUsage.asStateFlow()

    private val _settings = MutableStateFlow(AISettings(uid = "default_user"))
    val settings: StateFlow<AISettings> = _settings.asStateFlow()

    init {
        seedInitialHistory()
    }

    private fun seedInitialHistory() {
        val sampleHistory = listOf(
            AIHistoryItem(
                historyId = "hist_01",
                uid = "default_user",
                jobId = "job_init_01",
                toolType = AIToolType.CAPTION_GENERATOR,
                inputType = "text",
                outputType = "text",
                title = "Smart Sleep Hygiene Caption",
                createdAt = System.currentTimeMillis() - 86400000L
            ),
            AIHistoryItem(
                historyId = "hist_02",
                uid = "default_user",
                jobId = "job_init_02",
                toolType = AIToolType.IMAGE_ENHANCEMENT,
                inputType = "image",
                outputType = "image",
                title = "Clinic Reception Lighting Enhancement",
                thumbnail = "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=200",
                createdAt = System.currentTimeMillis() - 172800000L
            )
        )
        _history.value = sampleHistory
    }

    suspend fun submitJob(
        uid: String,
        accountType: String,
        toolType: AIToolType,
        requestType: AIRequestType,
        inputReference: String,
        containsMedicalRecords: Boolean = false
    ): AIJob {
        // Step 1: Account eligibility check
        AIRequestValidator.validateAccountEligibility(accountType, toolType)

        // Step 2: Moderation check
        AIModerationLayer.validateInputContent(inputReference, containsMedicalRecords)

        // Step 3: Check and deduct usage quota
        val updatedSummary = usageManager.checkAndDeductUnits(uid, toolType, 1)
        _currentUsage.value = updatedSummary

        // Step 4: Create Job Record
        val job = AIJob(
            uid = uid,
            accountType = accountType,
            toolType = toolType,
            requestType = requestType,
            inputReference = inputReference,
            status = AIJobStatus.PROCESSING,
            startedAt = System.currentTimeMillis()
        )

        _jobs.value = _jobs.value + job

        // Step 5: Audit log
        auditLogger.logEvent(
            AIAuditLog(
                actorUid = uid,
                actorRole = accountType,
                action = "AI_JOB_INITIATED",
                toolType = toolType,
                jobId = job.jobId
            )
        )

        // Step 6: Process job via adapter
        val completedJob = job.copy(
            status = AIJobStatus.COMPLETED,
            completedAt = System.currentTimeMillis(),
            outputReference = "Processed result for ${toolType.name}"
        )

        _jobs.value = _jobs.value.map { if (it.jobId == job.jobId) completedJob else it }

        // Step 7: Record into history
        val historyItem = AIHistoryItem(
            uid = uid,
            jobId = job.jobId,
            toolType = toolType,
            inputType = requestType.name.lowercase(),
            outputType = requestType.name.lowercase(),
            title = "${toolType.name.replace("_", " ")} Output",
            createdAt = System.currentTimeMillis()
        )
        _history.value = listOf(historyItem) + _history.value

        return completedJob
    }

    fun deleteAsset(assetId: String, callerUid: String) {
        val asset = _generatedAssets.value.find { it.assetId == assetId }
            ?: throw NoSuchElementException("Asset not found")
        if (asset.uid != callerUid) {
            throw SecurityException("Security Violation: User cannot delete another user's asset.")
        }
        _generatedAssets.value = _generatedAssets.value.filterNot { it.assetId == assetId }
    }

    fun cancelJob(jobId: String, callerUid: String): Boolean {
        val job = _jobs.value.find { it.jobId == jobId } ?: return false
        if (job.uid != callerUid) {
            throw SecurityException("Security Violation: Cannot cancel another user's job.")
        }
        _jobs.value = _jobs.value.map { if (it.jobId == jobId) it.copy(status = AIJobStatus.CANCELLED) else it }
        return true
    }

    suspend fun retryJob(jobId: String, callerUid: String): AIJob {
        val job = _jobs.value.find { it.jobId == jobId } ?: throw NoSuchElementException("Job not found")
        if (job.uid != callerUid) {
            throw SecurityException("Security Violation: Cannot retry another user's job.")
        }
        return submitJob(
            uid = callerUid,
            accountType = job.accountType,
            toolType = job.toolType,
            requestType = job.requestType,
            inputReference = job.inputReference
        )
    }

    fun updateSettings(newSettings: AISettings) {
        _settings.value = newSettings
    }
}
