package com.example.healthogram.social

import com.example.healthogram.core.AccountType
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * Healthogram 2.0 Scalable Feed Engine & Hybrid Fan-Out Architecture.
 *
 * Implements ADR-011:
 * - Hybrid fan-out: Fan-out on write for regular users (< 25,000 followers).
 * - Fan-out on read with caching for high-volume creators (>= 25,000 followers).
 * - Multi-strategy ranking (Chronological, Engagement-Weighted, Healthcare Boost).
 * - Cursor-based pagination.
 * - Zero-leakage constraint: Clinical Health Passport records NEVER enter feed ranking.
 */
enum class FanOutStrategy {
    FAN_OUT_ON_WRITE,
    FAN_OUT_ON_READ
}

enum class RankingStrategy {
    CHRONOLOGICAL,
    ENGAGEMENT_WEIGHTED,
    HEALTHCARE_VERIFIED_BOOST
}

data class ScaledFeedItem(
    val postId: String,
    val authorUid: String,
    val authorAccountType: AccountType,
    val authorVerified: Boolean,
    val publishedAtMillis: Long,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val rankingScore: Double = 0.0
)

data class FeedPage(
    val items: List<ScaledFeedItem>,
    val nextCursor: String?,
    val hasMore: Boolean
)

class FeedScalingService(
    val highVolumeThreshold: Int = 25_000
) {
    /**
     * Determines the optimal fan-out strategy based on follower count.
     */
    fun determineFanOutStrategy(followerCount: Int): FanOutStrategy {
        return if (followerCount >= highVolumeThreshold) {
            FanOutStrategy.FAN_OUT_ON_READ
        } else {
            FanOutStrategy.FAN_OUT_ON_WRITE
        }
    }

    /**
     * Calculates engagement-weighted score with exponential time decay.
     * Score = (Likes * 2 + Comments * 5 + Shares * 10) / ((AgeHours + 2)^1.5)
     */
    fun calculateRankingScore(
        item: ScaledFeedItem,
        strategy: RankingStrategy = RankingStrategy.HEALTHCARE_VERIFIED_BOOST,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Double {
        if (strategy == RankingStrategy.CHRONOLOGICAL) {
            return item.publishedAtMillis.toDouble()
        }

        val ageMillis = max(0L, currentTimeMillis - item.publishedAtMillis)
        val ageHours = TimeUnit.MILLISECONDS.toHours(ageMillis).toDouble()

        val rawEngagement = (item.likeCount * 2.0) + (item.commentCount * 5.0) + (item.shareCount * 10.0)
        val baseScore = (rawEngagement + 10.0) / Math.pow(ageHours + 2.0, 1.5)

        val finalScore = if (strategy == RankingStrategy.HEALTHCARE_VERIFIED_BOOST && item.authorVerified) {
            when (item.authorAccountType) {
                AccountType.DOCTOR,
                AccountType.CLINIC,
                AccountType.HOSPITAL,
                AccountType.LABORATORY -> baseScore * 1.35 // 35% clinical authority boost
                else -> baseScore
            }
        } else {
            baseScore
        }

        return finalScore
    }

    /**
     * Merges personal follower timelines with high-volume creator posts using cursor pagination.
     */
    fun mergeAndRankTimeline(
        userInboxItems: List<ScaledFeedItem>,
        creatorItems: List<ScaledFeedItem>,
        strategy: RankingStrategy = RankingStrategy.HEALTHCARE_VERIFIED_BOOST,
        pageSize: Int = 20,
        cursor: String? = null,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): FeedPage {
        val combined = (userInboxItems + creatorItems)
            .distinctBy { it.postId }
            .map { item ->
                item.copy(rankingScore = calculateRankingScore(item, strategy, currentTimeMillis))
            }
            .sortedByDescending { it.rankingScore }

        val startIndex = if (cursor != null) {
            val idx = combined.indexOfFirst { it.postId == cursor }
            if (idx >= 0) idx + 1 else 0
        } else {
            0
        }

        val pageItems = combined.drop(startIndex).take(pageSize)
        val hasMore = (startIndex + pageItems.size) < combined.size
        val nextCursor = if (hasMore && pageItems.isNotEmpty()) pageItems.last().postId else null

        return FeedPage(
            items = pageItems,
            nextCursor = nextCursor,
            hasMore = hasMore
        )
    }
}
