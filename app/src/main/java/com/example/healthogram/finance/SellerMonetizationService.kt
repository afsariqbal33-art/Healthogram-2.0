package com.example.healthogram.finance

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

enum class SellerSubscriptionTier {
    STANDARD,
    PRO_SELLER,
    ENTERPRISE_MERCHANT
}

data class SellerSubscriptionPlan(
    val tier: SellerSubscriptionTier,
    val monthlyPriceMinorUnits: Long,
    val currency: String = "OMR",
    val maxActiveListings: Int,
    val includesAdvancedAnalytics: Boolean,
    val includesAIMarketingAssistant: Boolean,
    val lowerCommissionRateBps: Int // Discount on marketplace commission
)

data class PromotedListingCampaign(
    val campaignId: String = UUID.randomUUID().toString(),
    val sellerUid: String,
    val productId: String,
    val budgetMinorUnits: Long,
    val costPerClickMinorUnits: Long = 50L,
    val status: String = "ACTIVE", // ACTIVE, PAUSED, COMPLETED
    val clicksDelivered: Int = 0,
    val impressionsDelivered: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * SellerMonetizationService 2.1
 * Manages seller subscriptions, promoted campaigns, and premium analytics tiers.
 */
class SellerMonetizationService private constructor() {

    private val sellerSubscriptions = ConcurrentHashMap<String, SellerSubscriptionTier>()
    private val activeCampaigns = ConcurrentHashMap<String, MutableList<PromotedListingCampaign>>()

    companion object {
        @Volatile
        private var instance: SellerMonetizationService? = null

        fun getInstance(): SellerMonetizationService {
            return instance ?: synchronized(this) {
                instance ?: SellerMonetizationService().also { instance = it }
            }
        }

        val PLANS = mapOf(
            SellerSubscriptionTier.STANDARD to SellerSubscriptionPlan(
                tier = SellerSubscriptionTier.STANDARD,
                monthlyPriceMinorUnits = 0L,
                maxActiveListings = 20,
                includesAdvancedAnalytics = false,
                includesAIMarketingAssistant = false,
                lowerCommissionRateBps = 0
            ),
            SellerSubscriptionTier.PRO_SELLER to SellerSubscriptionPlan(
                tier = SellerSubscriptionTier.PRO_SELLER,
                monthlyPriceMinorUnits = 15000L, // 15.000 OMR
                maxActiveListings = 200,
                includesAdvancedAnalytics = true,
                includesAIMarketingAssistant = true,
                lowerCommissionRateBps = 200 // 2% discount
            ),
            SellerSubscriptionTier.ENTERPRISE_MERCHANT to SellerSubscriptionPlan(
                tier = SellerSubscriptionTier.ENTERPRISE_MERCHANT,
                monthlyPriceMinorUnits = 45000L, // 45.000 OMR
                maxActiveListings = 5000,
                includesAdvancedAnalytics = true,
                includesAIMarketingAssistant = true,
                lowerCommissionRateBps = 400 // 4% discount
            )
        )
    }

    fun upgradeSellerSubscription(sellerUid: String, tier: SellerSubscriptionTier): SellerSubscriptionPlan {
        sellerSubscriptions[sellerUid] = tier
        return PLANS[tier] ?: PLANS[SellerSubscriptionTier.STANDARD]!!
    }

    fun getSellerSubscription(sellerUid: String): SellerSubscriptionPlan {
        val tier = sellerSubscriptions[sellerUid] ?: SellerSubscriptionTier.STANDARD
        return PLANS[tier] ?: PLANS[SellerSubscriptionTier.STANDARD]!!
    }

    fun createPromotedCampaign(sellerUid: String, productId: String, budgetMinorUnits: Long): PromotedListingCampaign {
        require(budgetMinorUnits >= 5000L) { "Minimum campaign budget is 5.000 OMR" }
        val campaign = PromotedListingCampaign(
            sellerUid = sellerUid,
            productId = productId,
            budgetMinorUnits = budgetMinorUnits
        )
        val list = activeCampaigns.computeIfAbsent(sellerUid) { mutableListOf() }
        synchronized(list) {
            list.add(campaign)
        }
        return campaign
    }

    fun getCampaigns(sellerUid: String): List<PromotedListingCampaign> {
        return activeCampaigns[sellerUid]?.toList() ?: emptyList()
    }

    fun clear() {
        sellerSubscriptions.clear()
        activeCampaigns.clear()
    }
}
