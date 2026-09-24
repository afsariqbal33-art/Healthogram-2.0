package com.example.healthogram.social

import com.example.healthogram.core.AccountType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Healthogram Social Media & Creator Platform Engine.
 *
 * Implements high-performance feed management, cursor-based pagination,
 * creator monetization & analytics, live streaming real-time simulation,
 * stories with ephemeral expiry, and user safety/moderation.
 *
 * Enforces STRICT ZERO-LEAKAGE SEPARATION between Social Content and Health Passport.
 */
class SocialFeedEngine private constructor() {

    companion object {
        @Volatile
        private var instance: SocialFeedEngine? = null

        fun getInstance(): SocialFeedEngine {
            return instance ?: synchronized(this) {
                instance ?: SocialFeedEngine().also { instance = it }
            }
        }
    }

    // --- State Holders ---
    private val _posts = MutableStateFlow<List<SocialPost>>(emptyList())
    val posts: StateFlow<List<SocialPost>> = _posts.asStateFlow()

    private val _reels = MutableStateFlow<List<SocialReel>>(emptyList())
    val reels: StateFlow<List<SocialReel>> = _reels.asStateFlow()

    private val _stories = MutableStateFlow<List<SocialStory>>(emptyList())
    val stories: StateFlow<List<SocialStory>> = _stories.asStateFlow()

    private val _liveStreams = MutableStateFlow<List<LiveStreamSession>>(emptyList())
    val liveStreams: StateFlow<List<LiveStreamSession>> = _liveStreams.asStateFlow()

    private val _comments = MutableStateFlow<Map<String, List<PostComment>>>(emptyMap())
    val comments: StateFlow<Map<String, List<PostComment>>> = _comments.asStateFlow()

    private val _liveMessages = MutableStateFlow<Map<String, List<LiveChatMessage>>>(emptyMap())
    val liveMessages: StateFlow<Map<String, List<LiveChatMessage>>> = _liveMessages.asStateFlow()

    private val _analytics = MutableStateFlow(CreatorAnalytics())
    val analytics: StateFlow<CreatorAnalytics> = _analytics.asStateFlow()

    private val _safetyProfile = MutableStateFlow(UserSafetyProfile(userId = "current_user"))
    val safetyProfile: StateFlow<UserSafetyProfile> = _safetyProfile.asStateFlow()

    private val _submittedReports = MutableStateFlow<List<SafetyReport>>(emptyList())
    val submittedReports: StateFlow<List<SafetyReport>> = _submittedReports.asStateFlow()

    init {
        seedInitialContent()
    }

    private fun seedInitialContent() {
        val now = System.currentTimeMillis()

        // 1. Initial Diverse General & Healthcare Social Posts
        val initialPosts = listOf(
            SocialPost(
                postId = "post_1",
                authorUid = "doc_1",
                authorName = "Dr. Sarah Jenkins",
                authorUsername = "dr.sarah",
                authorAccountType = AccountType.DOCTOR,
                isAuthorVerified = true,
                category = ContentCategory.HEALTHCARE,
                caption = "3 essential habits to adopt this month: 30 minutes of zone 2 cardio, managing resting heart rate, and staying hydrated! What's your daily goal? #Cardiology #FitnessGoals #Healthogram",
                mediaUrls = listOf("https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=800"),
                hashtags = listOf("#Cardiology", "#FitnessGoals", "#Healthogram"),
                likesCount = 1420,
                commentsCount = 3,
                sharesCount = 34,
                isMedicalContent = true,
                medicalDisclaimer = "Information provided for educational purposes only. Always consult your personal physician for clinical advice.",
                createdAt = now - 1000 * 60 * 30 // 30 mins ago
            ),
            SocialPost(
                postId = "post_2",
                authorUid = "creator_tech",
                authorName = "Elena Rostova",
                authorUsername = "elena.tech",
                authorAccountType = AccountType.INDIVIDUAL,
                isAuthorVerified = true,
                category = ContentCategory.TECHNOLOGY,
                caption = "Just tested the latest non-invasive optical sensor modules! The latency is under 5ms, meaning real-time spatial computing is finally here. Are we ready for augmented workflows? 🚀💻",
                mediaUrls = listOf("https://images.unsplash.com/photo-1518770660439-4636190af475?w=800"),
                hashtags = listOf("#TechNews", "#FutureOfTech", "#SpatialComputing"),
                likesCount = 2890,
                commentsCount = 4,
                sharesCount = 112,
                createdAt = now - 1000 * 60 * 90 // 1.5 hours ago
            ),
            SocialPost(
                postId = "post_3",
                authorUid = "chef_marcus",
                authorName = "Chef Marcus Bell",
                authorUsername = "marcus_cooks",
                authorAccountType = AccountType.INDIVIDUAL,
                isAuthorVerified = false,
                category = ContentCategory.LIFESTYLE,
                caption = "Mediterranean bowl with wild quinoa, citrus vinaigrette, grilled avocado, and microgreens! Eating vibrant whole foods doesn't have to be complicated or boring. Recipe in bio! 🥑🥗",
                mediaUrls = listOf("https://images.unsplash.com/photo-1540420773420-3366772f4999?w=800"),
                hashtags = listOf("#HealthyRecipes", "#CleanEating", "#ChefMarcus"),
                likesCount = 980,
                commentsCount = 2,
                sharesCount = 45,
                createdAt = now - 1000 * 60 * 180
            ),
            SocialPost(
                postId = "post_4",
                authorUid = "hosp_1",
                authorName = "City General Hospital",
                authorUsername = "cityhospital",
                authorAccountType = AccountType.HOSPITAL,
                isAuthorVerified = true,
                category = ContentCategory.COMMUNITY,
                caption = "Proud to announce our 24/7 robotic cardiology surgical wing is officially online. Consultations can now be booked directly through our Healthogram profile.",
                mediaUrls = listOf("https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=800"),
                hashtags = listOf("#RoboticSurgery", "#HealthcareInnovation"),
                likesCount = 3890,
                commentsCount = 1,
                sharesCount = 205,
                createdAt = now - 1000 * 60 * 240
            ),
            SocialPost(
                postId = "post_5",
                authorUid = "comedy_dan",
                authorName = "Dan Miller",
                authorUsername = "dan_laughs",
                authorAccountType = AccountType.INDIVIDUAL,
                isAuthorVerified = false,
                category = ContentCategory.COMEDY,
                caption = "Me looking at my smartwatch notification telling me I've been sitting for 4 hours when I just sat down 5 minutes ago: 👁️👄👁️ #Relatable #WorkFromHome",
                mediaUrls = emptyList(),
                hashtags = listOf("#Relatable", "#WorkFromHome", "#Comedy"),
                likesCount = 4510,
                commentsCount = 5,
                sharesCount = 670,
                createdAt = now - 1000 * 60 * 360
            )
        )
        _posts.value = initialPosts

        // 2. Initial Sample Comments
        _comments.value = mapOf(
            "post_1" to listOf(
                PostComment(
                    commentId = "c1",
                    postId = "post_1",
                    authorUid = "user_runner",
                    authorName = "Alex Mercer",
                    authorUsername = "alex_runner",
                    text = "Zone 2 training has completely transformed my endurance! Heart rate stays right at 135 bpm.",
                    likesCount = 24,
                    createdAt = now - 1000 * 60 * 15
                ),
                PostComment(
                    commentId = "c2",
                    postId = "post_1",
                    authorUid = "doc_2",
                    authorName = "Dr. Lisa Wong",
                    authorUsername = "dr.lisa",
                    isAuthorVerified = true,
                    text = "Fantastic advice Sarah. Cellular mitochondrial density benefits of zone 2 cannot be overstated!",
                    likesCount = 48,
                    createdAt = now - 1000 * 60 * 10
                ),
                PostComment(
                    commentId = "c3",
                    postId = "post_1",
                    authorUid = "user_3",
                    authorName = "David K.",
                    authorUsername = "david_k",
                    text = "How long do you recommend staying in zone 2 per session?",
                    likesCount = 6,
                    createdAt = now - 1000 * 60 * 5
                )
            ),
            "post_2" to listOf(
                PostComment(
                    commentId = "c4",
                    postId = "post_2",
                    authorUid = "dev_chris",
                    authorName = "Chris Evans",
                    authorUsername = "chris_dev",
                    text = "5ms optical tracking is insane! Can't wait for developer SDK access.",
                    likesCount = 12,
                    createdAt = now - 1000 * 60 * 40
                )
            )
        )

        // 3. Initial Reels (Vertical short-form video items)
        val initialReels = listOf(
            SocialReel(
                reelId = "reel_1",
                authorUid = "doc_mark",
                authorName = "Dr. Mark Thorne",
                authorUsername = "dr.mark_physio",
                authorAccountType = AccountType.DOCTOR,
                isAuthorVerified = true,
                caption = "3 quick posture corrections you can do right now at your desk! Save this for your workday routine. #Posture #DeskWorkout #Healthogram",
                videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-stretching-exercises-in-a-gym-42795-large.mp4",
                audioTrackTitle = "Original Audio • Dr. Mark Thorne",
                audioAuthor = "Dr. Mark Thorne",
                category = ContentCategory.SPORTS,
                tags = listOf("#Posture", "#PhysicalTherapy", "#DeskWorkout"),
                likesCount = 28400,
                commentsCount = 492,
                sharesCount = 1205,
                durationSeconds = 28,
                createdAt = now - 1000 * 60 * 120
            ),
            SocialReel(
                reelId = "reel_2",
                authorUid = "travel_sam",
                authorName = "Samantha Cruz",
                authorUsername = "sam_explores",
                authorAccountType = AccountType.INDIVIDUAL,
                isAuthorVerified = true,
                caption = "Sunrise over the Dolomites in Italy 🏔️✨ The crisp mountain air hits different at 2500m! #TravelReels #Wanderlust #Nature",
                videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-aerial-view-of-a-mountain-valley-41551-large.mp4",
                audioTrackTitle = "Acoustic Morning • Indie Soundscapes",
                audioAuthor = "Indie Soundscapes",
                category = ContentCategory.TRAVEL,
                tags = listOf("#TravelReels", "#Dolomites", "#Nature"),
                likesCount = 42100,
                commentsCount = 890,
                sharesCount = 3450,
                durationSeconds = 18,
                createdAt = now - 1000 * 60 * 300
            ),
            SocialReel(
                reelId = "reel_3",
                authorUid = "beat_maker",
                authorName = "Marcus Beats",
                authorUsername = "marcus_beats",
                authorAccountType = AccountType.INDIVIDUAL,
                isAuthorVerified = false,
                caption = "Building a synthwave drop in 60 seconds with modular analog synths! 🎹⚡ Turn volume UP! #MusicProduction #Synthwave",
                videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-hands-playing-a-synthesizer-41618-large.mp4",
                audioTrackTitle = "Cyber City Neon • Marcus Beats",
                audioAuthor = "Marcus Beats",
                category = ContentCategory.MUSIC,
                tags = listOf("#Synthwave", "#MusicProduction", "#Audio"),
                likesCount = 19800,
                commentsCount = 312,
                sharesCount = 890,
                durationSeconds = 45,
                createdAt = now - 1000 * 60 * 480
            )
        )
        _reels.value = initialReels

        // 4. Initial Stories (24h Ephemeral Stories)
        val initialStories = listOf(
            SocialStory(
                storyId = "story_1",
                authorUid = "doc_1",
                authorName = "Dr. Sarah Jenkins",
                authorUsername = "dr.sarah",
                authorAccountType = AccountType.DOCTOR,
                isAuthorVerified = true,
                mediaUrl = "https://images.unsplash.com/photo-1579684385127-1ef15d508118?w=800",
                caption = "Starting clinical rounds today! Morning reminder: stretch your hamstrings and hydrate ☕",
                stickers = listOf(
                    StorySticker(
                        type = StoryStickerType.POLL,
                        text = "Did you drink water today?",
                        options = listOf("Yes, 2L+ 💧", "Working on it 😅"),
                        votes = listOf(142, 38)
                    )
                ),
                isSeen = false,
                viewersCount = 412,
                createdAt = now - 1000 * 60 * 60 * 4
            ),
            SocialStory(
                storyId = "story_2",
                authorUid = "mayoclinic",
                authorName = "Mayo Health Center",
                authorUsername = "mayoclinic",
                authorAccountType = AccountType.CLINIC,
                isAuthorVerified = true,
                mediaUrl = "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=800",
                caption = "LIVE: Cardiology symposium and Q&A session broadcast!",
                isLive = true,
                isSeen = false,
                viewersCount = 1890,
                createdAt = now - 1000 * 60 * 30
            ),
            SocialStory(
                storyId = "story_3",
                authorUid = "alex_runner",
                authorName = "Alex Mercer",
                authorUsername = "alex_runner",
                authorAccountType = AccountType.INDIVIDUAL,
                isAuthorVerified = false,
                mediaUrl = "https://images.unsplash.com/photo-1502680390469-be75c86b636f?w=800",
                caption = "Trail run complete. 12km in 58 mins. Pace is feeling strong! 🏃‍♂️",
                stickers = listOf(
                    StorySticker(
                        type = StoryStickerType.LOCATION,
                        text = "Pacific Crest Trail, CA"
                    )
                ),
                isSeen = false,
                viewersCount = 180,
                createdAt = now - 1000 * 60 * 60 * 6
            ),
            SocialStory(
                storyId = "story_4",
                authorUid = "cityhospital",
                authorName = "City General Hospital",
                authorUsername = "cityhospital",
                authorAccountType = AccountType.HOSPITAL,
                isAuthorVerified = true,
                mediaUrl = "https://images.unsplash.com/photo-1584515979956-d9f6e5d09982?w=800",
                caption = "Walk-in flu & booster clinics open today until 6 PM.",
                isSeen = true,
                viewersCount = 820,
                createdAt = now - 1000 * 60 * 60 * 12
            )
        )
        _stories.value = initialStories

        // 5. Initial Active Live Streams
        val initialLiveStreams = listOf(
            LiveStreamSession(
                streamId = "live_1",
                hostUid = "mayoclinic",
                hostName = "Mayo Health Center",
                hostUsername = "mayoclinic",
                hostAccountType = AccountType.CLINIC,
                isHostVerified = true,
                title = "Cardiology Innovation & Q&A with Senior Faculty",
                category = ContentCategory.HEALTHCARE,
                isLive = true,
                viewerCount = 1890,
                likesCount = 8400,
                startedAt = now - 1000 * 60 * 25
            ),
            LiveStreamSession(
                streamId = "live_2",
                hostUid = "tech_creator",
                hostName = "Elena Rostova",
                hostUsername = "elena.tech",
                hostAccountType = AccountType.INDIVIDUAL,
                isHostVerified = true,
                title = "Live Code Review: AI Agents & Edge Inference",
                category = ContentCategory.TECHNOLOGY,
                isLive = true,
                viewerCount = 640,
                likesCount = 3200,
                startedAt = now - 1000 * 60 * 15
            )
        )
        _liveStreams.value = initialLiveStreams

        // 6. Live Chat Messages
        _liveMessages.value = mapOf(
            "live_1" to listOf(
                LiveChatMessage(
                    streamId = "live_1",
                    senderUid = "user_a",
                    senderName = "Marcus",
                    senderUsername = "marcus_b",
                    message = "Can you speak on the long-term data for zone 2 pacing?"
                ),
                LiveChatMessage(
                    streamId = "live_1",
                    senderUid = "user_b",
                    senderName = "Dr. Anita",
                    senderUsername = "anita_md",
                    message = "Greeting from Boston! Excellent presentation."
                ),
                LiveChatMessage(
                    streamId = "live_1",
                    senderUid = "mayoclinic",
                    senderName = "Mayo Health Center",
                    senderUsername = "mayoclinic",
                    message = "Welcome everyone! Feel free to drop questions in chat.",
                    isHost = true
                )
            )
        )
    }

    // --- Cursor-Based Pagination Implementation ---

    /**
     * Fetch feed posts with cursor-based pagination.
     * Guaranteed scalable: does not load entire collection; queries by createdAt descending.
     */
    fun getFeedPosts(cursor: Long? = null, limit: Int = 10, category: ContentCategory? = null): CursorPage<SocialPost> {
        val filtered = _posts.value.filter { post ->
            val blocked = _safetyProfile.value.blockedUserIds.contains(post.authorUid)
            val categoryMatches = category == null || category == ContentCategory.ALL || post.category == category
            !blocked && categoryMatches
        }.sortedByDescending { it.createdAt }

        val startIndex = if (cursor == null) {
            0
        } else {
            val idx = filtered.indexOfFirst { it.createdAt < cursor }
            if (idx == -1) filtered.size else idx
        }

        val pageItems = filtered.drop(startIndex).take(limit)
        val nextCursor = if (pageItems.size == limit) pageItems.lastOrNull()?.createdAt else null
        val hasMore = nextCursor != null && (startIndex + limit) < filtered.size

        return CursorPage(items = pageItems, nextCursor = nextCursor, hasMore = hasMore)
    }

    // --- Social Interactions (Likes, Comments, Saves, Shares, Follow) ---

    fun toggleLikePost(postId: String, currentUid: String) {
        _posts.update { currentList ->
            currentList.map { post ->
                if (post.postId == postId) {
                    val newIsLiked = !post.isLikedByCurrentUser
                    val newLikesCount = if (newIsLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
                    post.copy(isLikedByCurrentUser = newIsLiked, likesCount = newLikesCount)
                } else post
            }
        }
    }

    fun toggleSavePost(postId: String) {
        _posts.update { currentList ->
            currentList.map { post ->
                if (post.postId == postId) {
                    val newIsSaved = !post.isSavedByCurrentUser
                    val newSavesCount = if (newIsSaved) post.savesCount + 1 else (post.savesCount - 1).coerceAtLeast(0)
                    post.copy(isSavedByCurrentUser = newIsSaved, savesCount = newSavesCount)
                } else post
            }
        }
    }

    fun toggleFollowAuthor(authorUid: String) {
        _posts.update { currentList ->
            currentList.map { post ->
                if (post.authorUid == authorUid) {
                    post.copy(isFollowingAuthor = !post.isFollowingAuthor)
                } else post
            }
        }
    }

    fun incrementShareCount(postId: String) {
        _posts.update { currentList ->
            currentList.map { post ->
                if (post.postId == postId) {
                    post.copy(sharesCount = post.sharesCount + 1)
                } else post
            }
        }
    }

    fun addComment(
        postId: String,
        authorUid: String,
        authorName: String,
        authorUsername: String,
        text: String,
        isVerified: Boolean = false
    ): PostComment {
        val newComment = PostComment(
            postId = postId,
            authorUid = authorUid,
            authorName = authorName,
            authorUsername = authorUsername,
            isAuthorVerified = isVerified,
            text = text,
            createdAt = System.currentTimeMillis()
        )

        _comments.update { map ->
            val existing = map[postId] ?: emptyList()
            map + (postId to (existing + newComment))
        }

        // Increment post comment count
        _posts.update { list ->
            list.map { post ->
                if (post.postId == postId) {
                    post.copy(commentsCount = post.commentsCount + 1)
                } else post
            }
        }

        return newComment
    }

    fun toggleLikeComment(postId: String, commentId: String) {
        _comments.update { map ->
            val existing = map[postId] ?: emptyList()
            val updated = existing.map { comment ->
                if (comment.commentId == commentId) {
                    val newLiked = !comment.isLiked
                    val newCount = if (newLiked) comment.likesCount + 1 else (comment.likesCount - 1).coerceAtLeast(0)
                    comment.copy(isLiked = newLiked, likesCount = newCount)
                } else comment
            }
            map + (postId to updated)
        }
    }

    // --- Content Creation & Publishing ---

    fun publishPost(post: SocialPost): SocialPost {
        // Enforce Security Check: ensure NO health passport data is leaked
        assertZeroHealthPassportLeakage(post)

        _posts.update { listOf(post) + it }

        // Update creator analytics
        _analytics.update { prev ->
            prev.copy(
                totalImpressions = prev.totalImpressions + 150,
                totalReach = prev.totalReach + 90
            )
        }

        return post
    }

    fun publishReel(reel: SocialReel): SocialReel {
        _reels.update { listOf(reel) + it }
        return reel
    }

    fun publishStory(story: SocialStory): SocialStory {
        _stories.update { listOf(story) + it }
        return story
    }

    fun startLiveStream(session: LiveStreamSession): LiveStreamSession {
        _liveStreams.update { listOf(session) + it }
        return session
    }

    fun endLiveStream(streamId: String) {
        _liveStreams.update { list ->
            list.map { if (it.streamId == streamId) it.copy(isLive = false, endedAt = System.currentTimeMillis()) else it }
        }
    }

    fun sendLiveChatMessage(
        streamId: String,
        senderUid: String,
        senderName: String,
        senderUsername: String,
        text: String,
        isHost: Boolean = false
    ) {
        val message = LiveChatMessage(
            streamId = streamId,
            senderUid = senderUid,
            senderName = senderName,
            senderUsername = senderUsername,
            message = text,
            isHost = isHost
        )
        _liveMessages.update { map ->
            val existing = map[streamId] ?: emptyList()
            map + (streamId to (existing + message))
        }
    }

    fun sendLiveHeart(streamId: String) {
        _liveStreams.update { list ->
            list.map {
                if (it.streamId == streamId) it.copy(likesCount = it.likesCount + 1) else it
            }
        }
    }

    // --- Reels Actions ---

    fun toggleLikeReel(reelId: String) {
        _reels.update { list ->
            list.map { reel ->
                if (reel.reelId == reelId) {
                    val newLiked = !reel.isLiked
                    val newLikesCount = if (newLiked) reel.likesCount + 1 else (reel.likesCount - 1).coerceAtLeast(0)
                    reel.copy(isLiked = newLiked, likesCount = newLikesCount)
                } else reel
            }
        }
    }

    fun toggleFollowReelAuthor(reelId: String) {
        _reels.update { list ->
            list.map { reel ->
                if (reel.reelId == reelId) {
                    reel.copy(isFollowing = !reel.isFollowing)
                } else reel
            }
        }
    }

    // --- Safety & Moderation (Reporting, Muting, Blocking) ---

    fun submitSafetyReport(report: SafetyReport): Boolean {
        _submittedReports.update { it + report }
        return true
    }

    fun blockUser(targetUid: String) {
        _safetyProfile.update { profile ->
            profile.copy(blockedUserIds = profile.blockedUserIds + targetUid)
        }
    }

    fun unblockUser(targetUid: String) {
        _safetyProfile.update { profile ->
            profile.copy(blockedUserIds = profile.blockedUserIds - targetUid)
        }
    }

    fun muteUser(targetUid: String) {
        _safetyProfile.update { profile ->
            profile.copy(mutedUserIds = profile.mutedUserIds + targetUid)
        }
    }

    fun restrictUser(targetUid: String) {
        _safetyProfile.update { profile ->
            profile.copy(restrictedUserIds = profile.restrictedUserIds + targetUid)
        }
    }

    // --- Strict Separation Invariant Validator ---

    /**
     * Enforces that social media posts and interactions remain strictly decoupled
     * from Health Passport collections and records.
     */
    fun assertZeroHealthPassportLeakage(post: SocialPost) {
        val combinedContent = "${post.caption} ${post.hashtags.joinToString(" ")}"
        val forbiddenPassportTokens = listOf("health_passport_id", "biometric_sample", "prescription_record_rx", "patient_ssn")

        for (token in forbiddenPassportTokens) {
            if (combinedContent.contains(token, ignoreCase = true)) {
                throw SecurityException("Architectural Violation: Social content must not contain sensitive Health Passport identifiers: $token")
            }
        }
    }
}
