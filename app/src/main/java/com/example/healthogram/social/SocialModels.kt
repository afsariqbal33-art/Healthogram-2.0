package com.example.healthogram.social

import com.example.healthogram.core.AccountType
import java.util.UUID

/**
 * Healthogram General & Creator Content Categories.
 * Healthogram is a general-purpose entertainment, lifestyle, and creator platform
 * that also securely accommodates healthcare topics.
 */
enum class ContentCategory(val displayName: String, val tag: String) {
    ALL("All", "#All"),
    ENTERTAINMENT("Entertainment", "#Entertainment"),
    LIFESTYLE("Lifestyle", "#Lifestyle"),
    TRAVEL("Travel", "#Travel"),
    EDUCATION("Education", "#Education"),
    TECHNOLOGY("Technology", "#Tech"),
    BUSINESS("Business", "#Business"),
    COMEDY("Comedy", "#Comedy"),
    SPORTS("Sports", "#Fitness"),
    MUSIC("Music", "#Music"),
    CREATIVE("Creative", "#Art"),
    HEALTHCARE("Healthcare", "#Health"),
    PERSONAL("Personal", "#Life"),
    COMMUNITY("Community", "#Community")
}

enum class ContentType {
    POST,
    REEL,
    CAROUSEL,
    VIDEO,
    STORY,
    LIVE
}

enum class PostVisibility(val label: String) {
    PUBLIC("Public"),
    FOLLOWERS_ONLY("Followers Only"),
    CLOSE_FRIENDS("Close Friends")
}

data class SocialPost(
    val postId: String = UUID.randomUUID().toString(),
    val authorUid: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatarUrl: String? = null,
    val authorAccountType: AccountType = AccountType.INDIVIDUAL,
    val isAuthorVerified: Boolean = false,
    val contentType: ContentType = ContentType.POST,
    val category: ContentCategory = ContentCategory.LIFESTYLE,
    val caption: String,
    val mediaUrls: List<String> = emptyList(),
    val hashtags: List<String> = emptyList(),
    val taggedUsers: List<String> = emptyList(),
    val locationName: String? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val savesCount: Int = 0,
    val isLikedByCurrentUser: Boolean = false,
    val isSavedByCurrentUser: Boolean = false,
    val isFollowingAuthor: Boolean = false,
    val isMedicalContent: Boolean = false,
    val medicalDisclaimer: String? = null,
    val isCommentsDisabled: Boolean = false,
    val visibility: PostVisibility = PostVisibility.PUBLIC,
    val createdAt: Long = System.currentTimeMillis()
)

data class PostComment(
    val commentId: String = UUID.randomUUID().toString(),
    val postId: String,
    val authorUid: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatarUrl: String? = null,
    val isAuthorVerified: Boolean = false,
    val text: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val replies: List<PostComment> = emptyList()
)

data class SocialReel(
    val reelId: String = UUID.randomUUID().toString(),
    val authorUid: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatarUrl: String? = null,
    val authorAccountType: AccountType = AccountType.INDIVIDUAL,
    val isAuthorVerified: Boolean = false,
    val caption: String,
    val videoUrl: String,
    val thumbnailUrl: String? = null,
    val audioTrackTitle: String = "Original Audio",
    val audioAuthor: String = "Healthogram Creator",
    val category: ContentCategory = ContentCategory.ENTERTAINMENT,
    val tags: List<String> = emptyList(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val savesCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val isFollowing: Boolean = false,
    val durationSeconds: Int = 30,
    val createdAt: Long = System.currentTimeMillis()
)

enum class StoryStickerType {
    POLL,
    QUESTION,
    LINK,
    MENTION,
    LOCATION
}

data class StorySticker(
    val type: StoryStickerType,
    val text: String,
    val options: List<String> = emptyList(),
    val votes: List<Int> = emptyList()
)

data class SocialStory(
    val storyId: String = UUID.randomUUID().toString(),
    val authorUid: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatarUrl: String? = null,
    val authorAccountType: AccountType = AccountType.INDIVIDUAL,
    val isAuthorVerified: Boolean = false,
    val mediaUrl: String,
    val isVideo: Boolean = false,
    val caption: String = "",
    val stickers: List<StorySticker> = emptyList(),
    val isSeen: Boolean = false,
    val isLive: Boolean = false,
    val viewersCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
)

data class LiveStreamSession(
    val streamId: String = UUID.randomUUID().toString(),
    val hostUid: String,
    val hostName: String,
    val hostUsername: String,
    val hostAvatarUrl: String? = null,
    val hostAccountType: AccountType = AccountType.INDIVIDUAL,
    val isHostVerified: Boolean = false,
    val title: String,
    val category: ContentCategory = ContentCategory.COMMUNITY,
    val isLive: Boolean = true,
    val viewerCount: Int = 1,
    val likesCount: Int = 0,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null
)

data class LiveChatMessage(
    val messageId: String = UUID.randomUUID().toString(),
    val streamId: String,
    val senderUid: String,
    val senderName: String,
    val senderUsername: String,
    val message: String,
    val isHost: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class CreatorAnalytics(
    val totalImpressions: Long = 145200,
    val totalReach: Long = 98400,
    val engagementRatePercentage: Double = 6.8,
    val followerGrowthRate: Double = 12.4,
    val profileViewsThisMonth: Long = 18200,
    val totalLikes: Long = 34200,
    val totalShares: Long = 4850,
    val topPerformingPostId: String? = "post_1",
    val categoryBreakdown: Map<ContentCategory, Int> = mapOf(
        ContentCategory.LIFESTYLE to 35,
        ContentCategory.HEALTHCARE to 30,
        ContentCategory.TECHNOLOGY to 20,
        ContentCategory.ENTERTAINMENT to 15
    ),
    val monthlyViews: List<Pair<String, Long>> = listOf(
        "Jan" to 82000L,
        "Feb" to 96000L,
        "Mar" to 118000L,
        "Apr" to 145200L
    )
)

enum class SafetyReportReason(val label: String) {
    SPAM("Spam or Bot Activity"),
    HARASSMENT("Harassment or Bullying"),
    HATE_SPEECH("Hate Speech or Discrimination"),
    MISINFORMATION("False Information / Scams"),
    MEDICAL_MISINFORMATION("Dangerous or Unverified Medical Claims"),
    INAPPROPRIATE_CONTENT("Inappropriate or Explicit Content"),
    VIOLENCE("Violence or Dangerous Organizations"),
    COPYRIGHT("Intellectual Property Violation")
}

data class SafetyReport(
    val reportId: String = UUID.randomUUID().toString(),
    val reporterUid: String,
    val reportedEntityId: String,
    val entityType: String, // "post", "reel", "story", "user", "comment"
    val reason: SafetyReportReason,
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "submitted"
)

data class UserSafetyProfile(
    val userId: String,
    val blockedUserIds: Set<String> = emptySet(),
    val mutedUserIds: Set<String> = emptySet(),
    val restrictedUserIds: Set<String> = emptySet()
)

data class CursorPage<T>(
    val items: List<T>,
    val nextCursor: Long?,
    val hasMore: Boolean
)
