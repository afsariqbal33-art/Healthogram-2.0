package com.example.healthogram.owner

import com.example.healthogram.core.AccountType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * PlatformConfigurationService: Centralized state management and repository
 * for Step 17 Platform Owner configuration.
 */
class PlatformConfigurationService private constructor() {

    companion object {
        @Volatile
        private var instance: PlatformConfigurationService? = null

        fun getInstance(): PlatformConfigurationService {
            return instance ?: synchronized(this) {
                instance ?: PlatformConfigurationService().also { instance = it }
            }
        }
    }

    // Current Owner Persona
    private val _currentOwner = MutableStateFlow(
        OwnerProfile(
            ownerUid = "owner_root_001",
            displayName = "Dr. Tariq Al-Mansoor",
            email = "owner@healthogram.com",
            role = OwnerRole.PLATFORM_OWNER,
            status = OwnerStatus.ACTIVE,
            mfaEnabled = true,
            reauthRequired = false,
            lastReauthAt = System.currentTimeMillis()
        )
    )
    val currentOwner: StateFlow<OwnerProfile> = _currentOwner.asStateFlow()

    // Active Environment
    private val _activeEnvironment = MutableStateFlow(AppEnvironment.PRODUCTION)
    val activeEnvironment: StateFlow<AppEnvironment> = _activeEnvironment.asStateFlow()

    // Global App Configuration
    private val _globalConfig = MutableStateFlow(PlatformConfigGlobal())
    val globalConfig: StateFlow<PlatformConfigGlobal> = _globalConfig.asStateFlow()

    // Feature Flags Map (keyed by featureKey)
    private val _featureFlags = MutableStateFlow<Map<String, GlobalFeatureFlag>>(emptyMap())
    val featureFlags: StateFlow<Map<String, GlobalFeatureFlag>> = _featureFlags.asStateFlow()

    // Sovereign Country Configurations (keyed by countryCode)
    private val _countryConfigs = MutableStateFlow<Map<String, SovereignCountryConfig>>(emptyMap())
    val countryConfigs: StateFlow<Map<String, SovereignCountryConfig>> = _countryConfigs.asStateFlow()

    // Account Category Controls (strictly the 5 authorized categories)
    private val _accountCategoryControls = MutableStateFlow<Map<AccountType, AccountCategoryFeatureControls>>(emptyMap())
    val accountCategoryControls: StateFlow<Map<AccountType, AccountCategoryFeatureControls>> = _accountCategoryControls.asStateFlow()

    // Emergency Kill Switches
    private val _emergencySwitches = MutableStateFlow<Map<EmergencySwitchKey, EmergencyKillSwitchState>>(emptyMap())
    val emergencySwitches: StateFlow<Map<EmergencySwitchKey, EmergencyKillSwitchState>> = _emergencySwitches.asStateFlow()

    // Scheduled Configuration Changes
    private val _scheduledChanges = MutableStateFlow<List<ScheduledConfigurationChange>>(emptyList())
    val scheduledChanges: StateFlow<List<ScheduledConfigurationChange>> = _scheduledChanges.asStateFlow()

    // Configuration Version History
    private val _configurationVersions = MutableStateFlow<List<ConfigurationVersion>>(emptyList())
    val configurationVersions: StateFlow<List<ConfigurationVersion>> = _configurationVersions.asStateFlow()

    // Immutable Append-Only Audit Logs
    private val _auditLogs = MutableStateFlow<List<OwnerAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<OwnerAuditLog>> = _auditLogs.asStateFlow()

    // Platform Announcements
    private val _announcements = MutableStateFlow<List<PlatformAnnouncement>>(emptyList())
    val announcements: StateFlow<List<PlatformAnnouncement>> = _announcements.asStateFlow()

    // Service Providers
    private val _providers = MutableStateFlow<List<ServiceProviderConfig>>(emptyList())
    val providers: StateFlow<List<ServiceProviderConfig>> = _providers.asStateFlow()

    // Pricing & Commercial Configuration
    private val _pricingConfig = MutableStateFlow(PricingConfig())
    val pricingConfig: StateFlow<PricingConfig> = _pricingConfig.asStateFlow()

    // Owner Delegates
    private val _delegates = MutableStateFlow<List<OwnerDelegate>>(emptyList())
    val delegates: StateFlow<List<OwnerDelegate>> = _delegates.asStateFlow()

    init {
        seedInitialConfigurations()
    }

    private fun seedInitialConfigurations() {
        seedFeatureFlags()
        seedCountryConfigs()
        seedAccountCategories()
        seedEmergencySwitches()
        seedProviders()
        seedAnnouncements()
        seedInitialVersionAndAudit()
        seedInitialDelegates()
    }

    private fun seedFeatureFlags() {
        val flags = mutableMapOf<String, GlobalFeatureFlag>()

        fun addFlag(
            key: String,
            name: String,
            desc: String,
            category: FeatureCategory,
            status: FeatureFlagStatus = FeatureFlagStatus.ON,
            rollout: Int = 100
        ) {
            flags[key] = GlobalFeatureFlag(
                flagId = "flg_${key}",
                featureKey = key,
                displayName = name,
                description = desc,
                category = category,
                status = status,
                globalEnabled = (status == FeatureFlagStatus.ON || status == FeatureFlagStatus.BETA),
                rolloutPercentage = rollout
            )
        }

        // ACCOUNT
        addFlag("account_registration", "Account Registration", "Enable registration for new user accounts", FeatureCategory.ACCOUNT)
        addFlag("email_login", "Email/Password Login", "Authentication using email and credentials", FeatureCategory.ACCOUNT)
        addFlag("mobile_login", "Mobile Number Login", "SMS / Phone OTP authentication", FeatureCategory.ACCOUNT)
        addFlag("password_login", "Password Login", "Standard password challenge", FeatureCategory.ACCOUNT)
        addFlag("otp_login", "OTP Login", "One-time passcode authentication", FeatureCategory.ACCOUNT)
        addFlag("password_recovery", "Password Recovery", "Self-service password reset flows", FeatureCategory.ACCOUNT)
        addFlag("mfa", "Multi-Factor Authentication", "TOTP and SMS second factor verification", FeatureCategory.ACCOUNT)
        addFlag("account_deletion", "Account Deletion", "GDPR-compliant permanent account removal", FeatureCategory.ACCOUNT)
        addFlag("profile_editing", "Profile Editing", "Bio, avatar, and specialty profile updates", FeatureCategory.ACCOUNT)
        addFlag("profile_following", "Profile Following", "Following health creators and verified institutions", FeatureCategory.ACCOUNT)
        addFlag("profile_verification", "Profile Verification", "Submission of accreditation documents", FeatureCategory.ACCOUNT)

        // SOCIAL
        addFlag("social_feed", "Social Feed", "Core personalized health feed", FeatureCategory.SOCIAL)
        addFlag("posts", "Feed Posts", "Creation and rendering of social posts", FeatureCategory.SOCIAL)
        addFlag("photo_posts", "Photo Posts", "Image-based educational posts", FeatureCategory.SOCIAL)
        addFlag("video_posts", "Video Posts", "Clinical and health video uploads", FeatureCategory.SOCIAL)
        addFlag("reels", "Health Reels", "Vertical short-form health video engine", FeatureCategory.SOCIAL)
        addFlag("stories", "Daily Stories", "24-hour ephemeral visual updates", FeatureCategory.SOCIAL)
        addFlag("live_streaming", "Live Streaming", "Interactive live medical webinars and Q&A", FeatureCategory.SOCIAL)
        addFlag("likes", "Post Reactions", "Engagement likes and medical bookmarks", FeatureCategory.SOCIAL)
        addFlag("comments", "Post Comments", "Public commentary and discussions", FeatureCategory.SOCIAL)
        addFlag("shares", "Post Sharing", "Direct message and external social sharing", FeatureCategory.SOCIAL)
        addFlag("saves", "Saved Bookmarks", "Personal health library bookmarking", FeatureCategory.SOCIAL)
        addFlag("hashtags", "Hashtags", "Medical specialty and health taxonomy tags", FeatureCategory.SOCIAL)
        addFlag("mentions", "User Mentions", "@mentions linking verified profiles", FeatureCategory.SOCIAL)
        addFlag("creator_mode", "Creator Mode", "Professional tools for medical educators", FeatureCategory.SOCIAL)
        addFlag("creator_analytics", "Creator Analytics", "Impression and engagement telemetry", FeatureCategory.SOCIAL)
        addFlag("explore", "Explore Discover", "Trending medical topics and global search", FeatureCategory.SOCIAL)

        // HEALTH PASSPORT
        addFlag("health_passport", "Health Passport Core", "Sovereign patient medical record ledger", FeatureCategory.HEALTH_PASSPORT)
        addFlag("health_id", "Global Health ID", "Cryptographic alphanumeric patient identifier", FeatureCategory.HEALTH_PASSPORT)
        addFlag("health_qr", "Dynamic Health QR", "Encrypted, time-expiring clinical QR generator", FeatureCategory.HEALTH_PASSPORT)
        addFlag("doctor_access_request", "Doctor Access Requests", "Physician record viewing authorization requests", FeatureCategory.HEALTH_PASSPORT)
        addFlag("clinic_access_request", "Clinic Access Requests", "Outpatient clinic facility access requests", FeatureCategory.HEALTH_PASSPORT)
        addFlag("hospital_access_request", "Hospital Access Requests", "Inpatient hospital record access requests", FeatureCategory.HEALTH_PASSPORT)
        addFlag("laboratory_access_request", "Laboratory Access Requests", "Diagnostic center test result link requests", FeatureCategory.HEALTH_PASSPORT)
        addFlag("health_record_upload", "Clinical Record Upload", "Patient diagnostic report attachments", FeatureCategory.HEALTH_PASSPORT)
        addFlag("health_report_upload", "Lab Report Upload", "Direct laboratory test result attachments", FeatureCategory.HEALTH_PASSPORT)
        addFlag("paper_prescription_upload", "Prescription Upload", "Digitized paper prescription records", FeatureCategory.HEALTH_PASSPORT)
        addFlag("temporary_health_access", "Temporary Emergency Access", "Time-bounded emergency medical access", FeatureCategory.HEALTH_PASSPORT)
        addFlag("health_access_logs", "Patient Access Audit Logs", "Patient-visible clinical record access timeline", FeatureCategory.HEALTH_PASSPORT)

        // MARKETPLACE
        addFlag("marketplace", "Marketplace Engine", "Health & wellness catalog and checkout", FeatureCategory.MARKETPLACE)
        addFlag("marketplace_customer", "Customer Purchasing", "Browsing, ordering, and cart operations", FeatureCategory.MARKETPLACE)
        addFlag("marketplace_seller", "Seller Center", "Merchant merchant portal and inventory tools", FeatureCategory.MARKETPLACE)
        addFlag("seller_onboarding", "Seller Onboarding", "New vendor application and compliance intake", FeatureCategory.MARKETPLACE)
        addFlag("seller_verification", "Seller Verification", "Commercial registration review workflow", FeatureCategory.MARKETPLACE)
        addFlag("product_listing", "Product Listing", "Publishing health products and medical equipment", FeatureCategory.MARKETPLACE)
        addFlag("product_reviews", "Product Reviews", "Verified customer ratings and testimonials", FeatureCategory.MARKETPLACE)
        addFlag("wishlist", "Product Wishlist", "Saved item collections", FeatureCategory.MARKETPLACE)
        addFlag("cart", "Shopping Cart", "Multi-merchant consolidated checkout basket", FeatureCategory.MARKETPLACE)
        addFlag("checkout", "Order Checkout", "Payment processing and delivery dispatch initiation", FeatureCategory.MARKETPLACE)
        addFlag("flash_sales", "Flash Sales & Deals", "Time-limited promotional campaigns", FeatureCategory.MARKETPLACE)
        addFlag("offers", "Custom Merchant Offers", "Seller discount coupons and bundle pricing", FeatureCategory.MARKETPLACE)
        addFlag("returns", "Order Returns", "Customer return requests and inspection flows", FeatureCategory.MARKETPLACE)
        addFlag("refunds", "Marketplace Refunds", "Automated and customer-requested refunds", FeatureCategory.MARKETPLACE)
        addFlag("seller_payouts", "Seller Payouts", "Automated ledger disbursement to bank accounts", FeatureCategory.MARKETPLACE)
        // Strictly initially OFF per requirements:
        addFlag("international_marketplace", "International Marketplace", "Cross-border purchasing and shipping", FeatureCategory.MARKETPLACE, status = FeatureFlagStatus.OFF)

        // PAYMENT
        addFlag("payments", "Platform Payments", "Core digital payment gateway processing", FeatureCategory.PAYMENT)
        addFlag("card_payment", "Credit & Debit Cards", "Visa, Mastercard, American Express checkout", FeatureCategory.PAYMENT)
        addFlag("wallet_payment", "Digital Wallets", "In-app stored balance and regional e-wallets", FeatureCategory.PAYMENT)
        addFlag("apple_pay", "Apple Pay", "Native iOS Apple Pay authentication token", FeatureCategory.PAYMENT)
        addFlag("google_pay", "Google Pay", "Native Android Google Pay gateway processing", FeatureCategory.PAYMENT)
        addFlag("country_payment_methods", "Sovereign Domestic Gateways", "Mada, KNET, Benefit, UPI, Pix local rails", FeatureCategory.PAYMENT)
        addFlag("cash_on_delivery", "Cash on Delivery (COD)", "Physical cash settlement upon package handover", FeatureCategory.PAYMENT)
        addFlag("chargebacks", "Chargeback Dispute Processing", "Dispute response and evidence submission", FeatureCategory.PAYMENT)
        addFlag("owner_withdrawals", "Platform Owner Treasury Payouts", "Owner revenue disbursement to corporate treasury", FeatureCategory.PAYMENT)

        // DELIVERY
        addFlag("delivery", "Logistics & Delivery Engine", "End-to-end package dispatch and tracking", FeatureCategory.DELIVERY)
        addFlag("seller_delivery", "Seller-Managed Delivery", "Merchant direct fleet fulfillment", FeatureCategory.DELIVERY)
        addFlag("platform_delivery", "Platform-Managed Fulfillment", "Healthogram hub-and-spoke fulfillment center", FeatureCategory.DELIVERY)
        addFlag("third_party_delivery", "3PL Partner Logistics", "Aramex, DHL, FedEx, SMSA integrated tracking", FeatureCategory.DELIVERY)
        addFlag("customer_pickup", "Customer Click-and-Collect", "Direct customer pickup from pharmacy or warehouse", FeatureCategory.DELIVERY)
        addFlag("scheduled_delivery", "Scheduled Delivery Slots", "Customer calendar time-window fulfillment", FeatureCategory.DELIVERY)
        addFlag("live_tracking", "Live Telemetry GPS Tracking", "Real-time rider position mapping on active routes", FeatureCategory.DELIVERY)
        addFlag("delivery_otp", "Delivery Verification OTP", "Cryptographic handover confirmation pin code", FeatureCategory.DELIVERY)
        addFlag("proof_of_delivery", "Digital Proof of Delivery", "Signature capture and package photo confirmation", FeatureCategory.DELIVERY)
        addFlag("returns_delivery", "Reverse Logistics", "Return courier pickup from customer doorstep", FeatureCategory.DELIVERY)

        // AI
        addFlag("ai_studio", "Healthogram AI Studio", "Clinical and creator AI generation suite", FeatureCategory.AI)
        addFlag("ai_image_tools", "AI Image Enhancement", "Visual contrast and anatomical enhancement", FeatureCategory.AI)
        addFlag("ai_caption", "AI Caption Synthesizer", "Automated medical post caption generator", FeatureCategory.AI)
        addFlag("ai_hashtags", "AI Hashtags Generator", "Taxonomy-aligned health hashtag discovery", FeatureCategory.AI)
        addFlag("ai_video", "AI Video Summarizer", "Clinical video chaptering and highlight generation", FeatureCategory.AI)
        addFlag("ai_product_content", "AI Product Descriptions", "Automated compliance-checked catalog copy", FeatureCategory.AI)
        addFlag("ai_marketing", "AI Campaign Creative", "Health awareness campaign generation", FeatureCategory.AI)
        addFlag("ai_usage_limits", "AI Token Quota Enforcement", "Per-tier daily and monthly model token caps", FeatureCategory.AI)
        addFlag("ai_translation_ai", "AI Translation Integration", "Direct AI model translation", FeatureCategory.AI, status = FeatureFlagStatus.OFF)
        addFlag("ai_voice_tools", "AI Voice Synthesis & Cloning", "Neural audio generation", FeatureCategory.AI, status = FeatureFlagStatus.OFF)
        addFlag("ai_beta", "AI Beta Features", "Experimental model evaluation", FeatureCategory.AI, status = FeatureFlagStatus.OFF)
        addFlag("ai_studio_creator", "Creator AI Studio", "Content generation suite for creators", FeatureCategory.AI, status = FeatureFlagStatus.ON)
        addFlag("ai_studio_seller", "Seller AI Studio", "Catalog generation suite for sellers", FeatureCategory.AI, status = FeatureFlagStatus.ON)
        addFlag("ai_studio_healthcare", "Healthcare AI Studio", "Clinical generation suite", FeatureCategory.AI, status = FeatureFlagStatus.ON)

        // TRANSLATION
        addFlag("translation", "Universal Translation Engine", "Real-time multilingual communication", FeatureCategory.TRANSLATION)
        addFlag("text_translation", "Instant Text Translation", "Post, comment, and message instant translation", FeatureCategory.TRANSLATION)
        addFlag("voice_translation", "Voice Message Translation", "Audio translation with speaker dialect mapping", FeatureCategory.TRANSLATION)
        addFlag("live_captions", "Live Closed Captions", "Real-time audio-to-text during live webinars", FeatureCategory.TRANSLATION)
        addFlag("translated_audio", "Synthesized Audio Dubbing", "Translated audio stream overlay during calls", FeatureCategory.TRANSLATION, status = FeatureFlagStatus.BETA)
        addFlag("call_translation", "Real-Time Telehealth Translation", "Bilingual translation in audio/video calls", FeatureCategory.TRANSLATION, status = FeatureFlagStatus.BETA)
        addFlag("tts", "Text to Speech (TTS)", "Synthetic audio playback for accessibility", FeatureCategory.TRANSLATION)
        addFlag("speech_to_text", "Speech to Text (STT)", "Speech recognition for dictation and voice notes", FeatureCategory.TRANSLATION)

        // COMMUNICATION
        addFlag("messaging", "Direct Messaging", "Encrypted 1-on-1 and group chat communication", FeatureCategory.COMMUNICATION)
        addFlag("message_requests", "Message Requests", "Screening queue for non-followed incoming chats", FeatureCategory.COMMUNICATION)
        addFlag("voice_messages", "Voice Audio Notes", "Record and transmit asynchronous audio memos", FeatureCategory.COMMUNICATION)
        addFlag("audio_calls", "VoIP Audio Calls", "High-fidelity WebRTC audio consultations", FeatureCategory.COMMUNICATION)
        addFlag("video_calls", "HD Video Telehealth", "Encrypted multi-party video conferencing", FeatureCategory.COMMUNICATION)
        addFlag("group_calls", "Group Medical Conferences", "Multi-doctor clinical case review sessions", FeatureCategory.COMMUNICATION, status = FeatureFlagStatus.BETA)
        addFlag("presence", "Online Presence Indicators", "Real-time user availability status indicators", FeatureCategory.COMMUNICATION)
        addFlag("typing_indicator", "Typing Telemetry", "Real-time chat typing status broadcast", FeatureCategory.COMMUNICATION)
        addFlag("read_receipts", "Message Read Receipts", "Double-check delivery and read verification", FeatureCategory.COMMUNICATION)

        // NOTIFICATIONS
        addFlag("push_notifications", "FCM Push Notifications", "Real-time cloud push delivery to devices", FeatureCategory.NOTIFICATIONS)
        addFlag("in_app_notifications", "In-App Toast Banner Alerts", "Contextual real-time floating notifications", FeatureCategory.NOTIFICATIONS)
        addFlag("notification_center", "Notification Center Hub", "Historical notification feed and filtering", FeatureCategory.NOTIFICATIONS)
        addFlag("marketing_notifications", "Promotional Campaigns", "Health product discounts and wellness tips", FeatureCategory.NOTIFICATIONS)
        addFlag("security_notifications", "Account Security Alerts", "New device login and password change alerts", FeatureCategory.NOTIFICATIONS)
        addFlag("health_security_notifications", "Health Passport Security Alerts", "Instant alerts when QR is scanned by clinic", FeatureCategory.NOTIFICATIONS)

        // VERIFICATION
        addFlag("doctor_verification", "Doctor Credential Verification", "Medical license verification workflow", FeatureCategory.VERIFICATION)
        addFlag("clinic_verification", "Clinic Facility Verification", "Outpatient clinic accreditation review", FeatureCategory.VERIFICATION)
        addFlag("hospital_verification", "Hospital Facility Verification", "Inpatient healthcare facility audit", FeatureCategory.VERIFICATION)
        addFlag("laboratory_verification", "Diagnostic Lab Verification", "Accredited testing laboratory verification", FeatureCategory.VERIFICATION)
        addFlag("seller_verification", "Merchant Business Verification", "Commercial registration and tax certificate check", FeatureCategory.VERIFICATION)
        addFlag("verification_badge", "Public Verification Badges", "Display Verified by Healthogram credentials", FeatureCategory.VERIFICATION)

        // PLATFORM
        addFlag("new_registration", "New User Self-Registration", "Onboarding open to general public", FeatureCategory.PLATFORM)
        addFlag("maintenance_mode", "Maintenance Screen Mode", "Graceful maintenance lockout overlay", FeatureCategory.PLATFORM, status = FeatureFlagStatus.OFF)
        addFlag("system_announcements", "Global Announcements Banner", "Top-of-app operational announcements", FeatureCategory.PLATFORM)
        addFlag("support", "Support & Help Center", "Customer ticketing and clinical dispute desk", FeatureCategory.PLATFORM)
        addFlag("search", "Universal Search", "Search accounts, educational posts, and products", FeatureCategory.PLATFORM)
        addFlag("analytics", "Platform Analytics", "Usage telemetry and behavioral analytics", FeatureCategory.PLATFORM)
        addFlag("desktop_access", "Web & Desktop Access", "Responsive browser portal access", FeatureCategory.PLATFORM)
        addFlag("mobile_access", "Native Mobile Apps", "iOS and Android client access", FeatureCategory.PLATFORM)

        _featureFlags.value = flags
    }

    private fun seedCountryConfigs() {
        val configs = mutableMapOf<String, SovereignCountryConfig>()

        fun addCountry(
            code: String,
            name: String,
            curr: String,
            supportedCurrs: List<String>,
            lang: String,
            supportedLangs: List<String>,
            tz: String,
            intlMarketplace: Boolean = false
        ) {
            configs[code] = SovereignCountryConfig(
                countryCode = code,
                countryName = name,
                active = true,
                registrationEnabled = true,
                marketplaceEnabled = true,
                internationalMarketplaceEnabled = intlMarketplace, // default false
                paymentsEnabled = true,
                deliveryEnabled = true,
                aiEnabled = true,
                translationEnabled = true,
                messagingEnabled = true,
                callingEnabled = true,
                healthPassportEnabled = true,
                verificationEnabled = true,
                socialEnabled = true,
                notificationsEnabled = true,
                defaultCurrency = curr,
                supportedCurrencies = supportedCurrs,
                defaultLanguage = lang,
                supportedLanguages = supportedLangs,
                timezone = tz
            )
        }

        addCountry("SA", "Saudi Arabia", "SAR", listOf("SAR", "USD"), "ar", listOf("ar", "en"), "Asia/Riyadh")
        addCountry("AE", "United Arab Emirates", "AED", listOf("AED", "USD"), "ar", listOf("ar", "en"), "Asia/Dubai")
        addCountry("US", "United States", "USD", listOf("USD"), "en", listOf("en", "es"), "America/New_York")
        addCountry("GB", "United Kingdom", "GBP", listOf("GBP", "EUR", "USD"), "en", listOf("en"), "Europe/London")
        addCountry("IN", "India", "INR", listOf("INR", "USD"), "hi", listOf("hi", "en"), "Asia/Kolkata")
        addCountry("DE", "Germany", "EUR", listOf("EUR", "USD"), "de", listOf("de", "en"), "Europe/Berlin")
        addCountry("KW", "Kuwait", "KWD", listOf("KWD", "USD"), "ar", listOf("ar", "en"), "Asia/Kuwait")
        addCountry("QA", "Qatar", "QAR", listOf("QAR", "USD"), "ar", listOf("ar", "en"), "Asia/Qatar")
        addCountry("BH", "Bahrain", "BHD", listOf("BHD", "USD"), "ar", listOf("ar", "en"), "Asia/Bahrain")
        addCountry("OM", "Oman", "OMR", listOf("OMR", "USD"), "ar", listOf("ar", "en"), "Asia/Muscat")

        _countryConfigs.value = configs
    }

    private fun seedAccountCategories() {
        val controls = mutableMapOf<AccountType, AccountCategoryFeatureControls>()

        controls[AccountType.INDIVIDUAL] = AccountCategoryFeatureControls(
            category = AccountType.INDIVIDUAL,
            healthPassportAccessEnabled = true,
            qrScanningEnabled = false,
            appointmentsEnabled = true,
            messagingEnabled = true,
            callingEnabled = true,
            socialPostingEnabled = true,
            liveStreamingEnabled = true,
            verificationRequired = false,
            maxConcurrentDevices = 4,
            organizationManagementEnabled = false,
            labReportSubmissionEnabled = false
        )

        controls[AccountType.DOCTOR] = AccountCategoryFeatureControls(
            category = AccountType.DOCTOR,
            healthPassportAccessEnabled = true,
            qrScanningEnabled = true, // Authorized after patient authorization
            appointmentsEnabled = true,
            messagingEnabled = true,
            callingEnabled = true,
            socialPostingEnabled = true,
            liveStreamingEnabled = true,
            verificationRequired = true,
            maxConcurrentDevices = 4,
            organizationManagementEnabled = false,
            labReportSubmissionEnabled = false
        )

        controls[AccountType.CLINIC] = AccountCategoryFeatureControls(
            category = AccountType.CLINIC,
            healthPassportAccessEnabled = true,
            qrScanningEnabled = true,
            appointmentsEnabled = true,
            messagingEnabled = true,
            callingEnabled = true,
            socialPostingEnabled = true,
            liveStreamingEnabled = true,
            verificationRequired = true,
            maxConcurrentDevices = 4, // Up to 8 with tier 2 plan
            organizationManagementEnabled = true,
            labReportSubmissionEnabled = false
        )

        controls[AccountType.HOSPITAL] = AccountCategoryFeatureControls(
            category = AccountType.HOSPITAL,
            healthPassportAccessEnabled = true,
            qrScanningEnabled = true,
            appointmentsEnabled = true,
            messagingEnabled = true,
            callingEnabled = true,
            socialPostingEnabled = true,
            liveStreamingEnabled = true,
            verificationRequired = true,
            maxConcurrentDevices = 4,
            organizationManagementEnabled = true,
            labReportSubmissionEnabled = false
        )

        controls[AccountType.LABORATORY] = AccountCategoryFeatureControls(
            category = AccountType.LABORATORY,
            healthPassportAccessEnabled = true,
            qrScanningEnabled = true,
            appointmentsEnabled = true,
            messagingEnabled = true,
            callingEnabled = true,
            socialPostingEnabled = true,
            liveStreamingEnabled = false,
            verificationRequired = true,
            maxConcurrentDevices = 4,
            organizationManagementEnabled = true,
            labReportSubmissionEnabled = true
        )

        _accountCategoryControls.value = controls
    }

    private fun seedEmergencySwitches() {
        val switches = mutableMapOf<EmergencySwitchKey, EmergencyKillSwitchState>()
        EmergencySwitchKey.entries.forEach { key ->
            switches[key] = EmergencyKillSwitchState(key = key, isTriggered = false)
        }
        _emergencySwitches.value = switches
    }

    private fun seedProviders() {
        val list = listOf(
            ServiceProviderConfig("prov_stripe_us", "Stripe Global", "US", "PAYMENT", AppEnvironment.PRODUCTION, "ONLINE", 1, "prov_checkout_com"),
            ServiceProviderConfig("prov_mada_sa", "Mada Gateway (Saudi Payments)", "SA", "PAYMENT", AppEnvironment.PRODUCTION, "ONLINE", 1, "prov_stripe_us"),
            ServiceProviderConfig("prov_knet_kw", "KNET Payment Network", "KW", "PAYMENT", AppEnvironment.PRODUCTION, "ONLINE", 1),
            ServiceProviderConfig("prov_aramex_mena", "Aramex Express Logistics", "SA", "DELIVERY", AppEnvironment.PRODUCTION, "ONLINE", 1, "prov_smsa_sa"),
            ServiceProviderConfig("prov_smsa_sa", "SMSA Express", "SA", "DELIVERY", AppEnvironment.PRODUCTION, "ONLINE", 2),
            ServiceProviderConfig("prov_dhl_intl", "DHL Express Worldwide", "GLOBAL", "DELIVERY", AppEnvironment.PRODUCTION, "ONLINE", 1),
            ServiceProviderConfig("prov_gemini_vertex", "Google Cloud Vertex AI (Gemini 1.5 Pro)", "GLOBAL", "AI", AppEnvironment.PRODUCTION, "ONLINE", 1, "prov_gemini_flash"),
            ServiceProviderConfig("prov_gemini_flash", "Google Cloud Vertex AI (Gemini 1.5 Flash)", "GLOBAL", "AI", AppEnvironment.PRODUCTION, "ONLINE", 2),
            ServiceProviderConfig("prov_google_translate", "Google Cloud Translation API v3", "GLOBAL", "TRANSLATION", AppEnvironment.PRODUCTION, "ONLINE", 1),
            ServiceProviderConfig("prov_agora_rtc", "Agora Interactive Live RTC", "GLOBAL", "RTC_CALLING", AppEnvironment.PRODUCTION, "ONLINE", 1),
            ServiceProviderConfig("prov_fcm_push", "Firebase Cloud Messaging (FCM)", "GLOBAL", "PUSH_NOTIFICATION", AppEnvironment.PRODUCTION, "ONLINE", 1)
        )
        _providers.value = list
    }

    private fun seedAnnouncements() {
        val announcements = listOf(
            PlatformAnnouncement(
                announcementId = "anc_welcome_step17",
                title = "Healthogram Sovereign Platform Deployed",
                message = "Welcome to Healthogram. Digital Health Passport, verified clinical networks, and international teleconsultation are active.",
                priority = "NORMAL",
                type = AnnouncementType.SYSTEM,
                status = "ACTIVE"
            )
        )
        _announcements.value = announcements
    }

    private fun seedInitialVersionAndAudit() {
        val initialVersion = ConfigurationVersion(
            versionId = "ver_001_genesis",
            versionNumber = 1,
            configurationType = "GLOBAL_PLATFORM_CONFIG",
            previousVersion = 0,
            changedFields = listOf("all_initial_flags", "country_configs", "account_categories"),
            oldValues = emptyMap(),
            newValues = mapOf("status" to "GENESIS_INITIALIZED", "environment" to "PRODUCTION"),
            reason = "Genesis production deployment of Step 17 Owner Control Panel",
            createdBy = "owner_root_001",
            approvedBy = "owner_root_001",
            publishedAt = System.currentTimeMillis()
        )
        _configurationVersions.value = listOf(initialVersion)

        val initialAudit = OwnerAuditLog(
            ownerUid = "owner_root_001",
            ownerRole = "PLATFORM_OWNER",
            action = "GENESIS_INITIALIZE",
            actionCategory = "SECURITY",
            targetType = "PLATFORM_CONFIG",
            targetId = "global",
            reason = "Genesis platform configuration loaded successfully",
            configurationVersion = 1
        )
        _auditLogs.value = listOf(initialAudit)
    }

    private fun seedInitialDelegates() {
        val list = listOf(
            OwnerDelegate(
                delegateId = "del_ops_01",
                uid = "usr_del_operator",
                displayName = "Rashid Al-Khatib",
                email = "rashid.ops@healthogram.com",
                role = OwnerRole.PLATFORM_OPERATOR,
                permissions = setOf(
                    "owner.dashboard.view",
                    "owner.feature_flags.view",
                    "owner.country_config.view",
                    "owner.maintenance.execute"
                ),
                countryScope = emptySet(),
                assignedBy = "owner_root_001"
            ),
            OwnerDelegate(
                delegateId = "del_fin_01",
                uid = "usr_del_finance",
                displayName = "Sarah Jenkins, CPA",
                email = "sarah.finance@healthogram.com",
                role = OwnerRole.FINANCE_OPERATOR,
                permissions = setOf(
                    "owner.dashboard.view",
                    "owner.payment_config.view",
                    "owner.payment_config.edit",
                    "owner.financial.view",
                    "owner.financial.edit"
                ),
                countryScope = setOf("US", "GB"),
                assignedBy = "owner_root_001"
            )
        )
        _delegates.value = list
    }

    // =========================================================================
    // REPOSITORY MUTATIONS
    // =========================================================================

    fun updateFeatureFlag(flag: GlobalFeatureFlag) {
        val updated = _featureFlags.value.toMutableMap()
        updated[flag.featureKey] = flag
        _featureFlags.value = updated
    }

    fun updateCountryConfig(config: SovereignCountryConfig) {
        val updated = _countryConfigs.value.toMutableMap()
        updated[config.countryCode] = config
        _countryConfigs.value = updated
    }

    fun updateAccountCategoryControls(controls: AccountCategoryFeatureControls) {
        val updated = _accountCategoryControls.value.toMutableMap()
        updated[controls.category] = controls
        _accountCategoryControls.value = updated
    }

    fun updateGlobalConfig(config: PlatformConfigGlobal) {
        _globalConfig.value = config
    }

    fun updateEmergencySwitch(state: EmergencyKillSwitchState) {
        val updated = _emergencySwitches.value.toMutableMap()
        updated[state.key] = state
        _emergencySwitches.value = updated
    }

    fun updateEnvironment(env: AppEnvironment) {
        _activeEnvironment.value = env
    }

    fun appendAuditLog(log: OwnerAuditLog) {
        _auditLogs.value = listOf(log) + _auditLogs.value
    }

    fun appendConfigurationVersion(version: ConfigurationVersion) {
        _configurationVersions.value = listOf(version) + _configurationVersions.value
    }

    fun appendScheduledChange(change: ScheduledConfigurationChange) {
        _scheduledChanges.value = listOf(change) + _scheduledChanges.value
    }

    fun updateScheduledChange(change: ScheduledConfigurationChange) {
        _scheduledChanges.value = _scheduledChanges.value.map {
            if (it.changeId == change.changeId) change else it
        }
    }

    fun updateProvider(provider: ServiceProviderConfig) {
        _providers.value = _providers.value.map {
            if (it.providerId == provider.providerId) provider else it
        }
    }

    fun appendAnnouncement(announcement: PlatformAnnouncement) {
        _announcements.value = listOf(announcement) + _announcements.value
    }

    fun updateAnnouncement(announcement: PlatformAnnouncement) {
        _announcements.value = _announcements.value.map {
            if (it.announcementId == announcement.announcementId) announcement else it
        }
    }

    fun updatePricingConfig(pricing: PricingConfig) {
        _pricingConfig.value = pricing
    }

    fun updateOwnerProfile(profile: OwnerProfile) {
        _currentOwner.value = profile
    }

    fun appendDelegate(delegate: OwnerDelegate) {
        _delegates.value = listOf(delegate) + _delegates.value
    }

    fun updateDelegate(delegate: OwnerDelegate) {
        _delegates.value = _delegates.value.map {
            if (it.delegateId == delegate.delegateId) delegate else it
        }
    }

    fun resetForTesting() {
        seedInitialConfigurations()
    }
}
