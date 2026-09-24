package com.example.healthogram.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.core.AccountType
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.social.*
import com.example.healthogram.social.ui.CommentsBottomSheet
import com.example.healthogram.social.ui.SafetyReportingDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class FeedFilterTab(val label: String) {
    FOR_YOU("For You"),
    FOLLOWING("Following"),
    TRENDING("Trending")
}

@Composable
fun HomePage(
    onOpenStory: (SocialStory) -> Unit = {},
    onOpenLive: (LiveStreamSession) -> Unit = {},
    onUserClick: (String) -> Unit = {},
    onCommentsClick: (String) -> Unit = {},
    onShareClick: (String) -> Unit = {},
    onCreateClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val engine = remember { SocialFeedEngine.getInstance() }
    val posts by engine.posts.collectAsState()
    val stories by engine.stories.collectAsState()
    val liveStreams by engine.liveStreams.collectAsState()

    var selectedTab by remember { mutableStateOf(FeedFilterTab.FOR_YOU) }
    var activeCommentsPostId by remember { mutableStateOf<String?>(null) }
    var reportTargetPost by remember { mutableStateOf<SocialPost?>(null) }

    val filteredPosts = remember(posts, selectedTab) {
        when (selectedTab) {
            FeedFilterTab.FOR_YOU -> posts
            FeedFilterTab.FOLLOWING -> posts.filter { it.isFollowingAuthor }
            FeedFilterTab.TRENDING -> posts.sortedByDescending { it.likesCount + it.sharesCount * 2 }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(HealthogramTheme.colors.background)
                .testTag("home_page_feed"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Feed Tabs Bar ("For You", "Following", "Trending")
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = HealthogramTheme.colors.surface,
                    border = BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FeedFilterTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .clickable { selectedTab = tab }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = tab.label,
                                        style = HealthogramTheme.typography.titleSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.textMuted
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .height(2.5.dp)
                                            .width(28.dp)
                                            .background(
                                                if (isSelected) HealthogramTheme.colors.primary else Color.Transparent,
                                                RoundedCornerShape(2.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Stories Horizontal Tray
            item {
                StoriesRow(
                    stories = stories,
                    onStoryClick = { story ->
                        if (story.isLive) {
                            val live = liveStreams.firstOrNull { it.hostUid == story.authorUid }
                                ?: LiveStreamSession(
                                    streamId = "live_stream_${story.authorUid}",
                                    hostUid = story.authorUid,
                                    hostName = story.authorName,
                                    hostUsername = story.authorUsername,
                                    title = story.caption.ifBlank { "Live Broadcast" }
                                )
                            onOpenLive(live)
                        } else {
                            onOpenStory(story)
                        }
                    },
                    onCreateStoryClick = onCreateClick
                )
            }

            // Feed Posts
            if (filteredPosts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.DynamicFeed,
                                contentDescription = null,
                                tint = HealthogramTheme.colors.textMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (selectedTab == FeedFilterTab.FOLLOWING) "No posts from accounts you follow yet" else "No posts found",
                                style = HealthogramTheme.typography.bodyMedium,
                                color = HealthogramTheme.colors.textMuted
                            )
                            if (selectedTab == FeedFilterTab.FOLLOWING) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { selectedTab = FeedFilterTab.FOR_YOU }) {
                                    Text("Explore 'For You' Feed", color = HealthogramTheme.colors.primary)
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredPosts, key = { it.postId }) { post ->
                    ModernPostCard(
                        post = post,
                        onUserClick = { onUserClick(post.authorUid) },
                        onLikeClick = { engine.toggleLikePost(post.postId, "current_user") },
                        onSaveClick = { engine.toggleSavePost(post.postId) },
                        onFollowClick = { engine.toggleFollowAuthor(post.authorUid) },
                        onCommentsClick = { activeCommentsPostId = post.postId },
                        onShareClick = {
                            engine.incrementShareCount(post.postId)
                            onShareClick(post.postId)
                        },
                        onReportClick = { reportTargetPost = post }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Active Comments Bottom Sheet
        activeCommentsPostId?.let { postId ->
            CommentsBottomSheet(
                postId = postId,
                onDismiss = { activeCommentsPostId = null }
            )
        }

        // Safety Reporting Dialog
        reportTargetPost?.let { post ->
            SafetyReportingDialog(
                entityId = post.postId,
                entityType = "post",
                authorUid = post.authorUid,
                onDismiss = { reportTargetPost = null }
            )
        }
    }
}

/**
 * Modern Stories Row with Instagram-inspired gradient rings & LIVE badge
 */
@Composable
fun StoriesRow(
    stories: List<SocialStory>,
    onStoryClick: (SocialStory) -> Unit,
    onCreateStoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight)
    ) {
        LazyRow(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Your Story" Add Item
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onCreateStoryClick() }
                        .testTag("add_story_btn")
                ) {
                    Box(modifier = Modifier.size(66.dp), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(HealthogramTheme.colors.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = HealthogramTheme.colors.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(HealthogramTheme.colors.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Story",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your Story",
                        style = HealthogramTheme.typography.caption,
                        color = HealthogramTheme.colors.textMuted
                    )
                }
            }

            // Active Stories
            items(stories, key = { it.storyId }) { story ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onStoryClick(story) }
                        .testTag("story_${story.authorUsername}")
                ) {
                    Box(modifier = Modifier.size(66.dp), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .then(
                                    if (!story.isSeen) {
                                        Modifier.background(HealthogramTheme.colors.storyGradient)
                                    } else {
                                        Modifier.background(HealthogramTheme.colors.border)
                                    }
                                )
                                .padding(2.5.dp)
                                .clip(CircleShape)
                                .background(HealthogramTheme.colors.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(HealthogramTheme.colors.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = story.authorName.take(1),
                                    style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = HealthogramTheme.colors.onPrimaryContainer
                                )
                            }
                        }

                        if (story.isLive) {
                            Surface(
                                modifier = Modifier.align(Alignment.BottomCenter),
                                shape = RoundedCornerShape(4.dp),
                                color = HealthogramTheme.colors.error
                            ) {
                                Text(
                                    text = "LIVE",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    style = HealthogramTheme.typography.overline,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = story.authorUsername,
                        style = HealthogramTheme.typography.caption,
                        color = HealthogramTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Modern High-Engagement Social Post Card
 */
@Composable
fun ModernPostCard(
    post: SocialPost,
    onUserClick: () -> Unit,
    onLikeClick: () -> Unit,
    onSaveClick: () -> Unit,
    onFollowClick: () -> Unit,
    onCommentsClick: () -> Unit,
    onShareClick: () -> Unit,
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showHeartAnimation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val heartScale by animateFloatAsState(
        targetValue = if (showHeartAnimation) 1.2f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "heart_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("post_card_${post.postId}"),
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight)
    ) {
        Column {
            // Post Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onUserClick() }
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(HealthogramTheme.colors.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.authorName.take(1),
                            style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.authorName,
                                style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = HealthogramTheme.colors.textPrimary
                            )
                            if (post.isAuthorVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                HealthogramVerifiedBadge(status = VerificationStatus.APPROVED, size = BadgeSize.SMALL)
                            }
                        }
                        Text(
                            text = "@${post.authorUsername} • ${post.category.displayName}",
                            style = HealthogramTheme.typography.caption,
                            color = HealthogramTheme.colors.textMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    HealthogramFollowButton(
                        isFollowing = post.isFollowingAuthor,
                        onToggleFollow = onFollowClick
                    )

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = HealthogramTheme.colors.textMuted)
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Report Post") },
                                onClick = {
                                    showMenu = false
                                    onReportClick()
                                },
                                leadingIcon = { Icon(Icons.Default.Report, contentDescription = null, tint = HealthogramTheme.colors.error) }
                            )
                            DropdownMenuItem(
                                text = { Text("Share Link") },
                                onClick = {
                                    showMenu = false
                                    onShareClick()
                                },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Mute @${post.authorUsername}") },
                                onClick = {
                                    showMenu = false
                                    SocialFeedEngine.getInstance().muteUser(post.authorUid)
                                },
                                leadingIcon = { Icon(Icons.Default.VolumeOff, contentDescription = null) }
                            )
                        }
                    }
                }
            }

            // Media Visual Area with Double-Tap Like Burst
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(HealthogramTheme.colors.surfaceVariant)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (!post.isLikedByCurrentUser) {
                                    onLikeClick()
                                }
                                showHeartAnimation = true
                                coroutineScope.launch {
                                    delay(750)
                                    showHeartAnimation = false
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Background visual icon indicator
                Icon(
                    imageVector = when (post.category) {
                        ContentCategory.HEALTHCARE -> Icons.Default.MedicalServices
                        ContentCategory.TECHNOLOGY -> Icons.Default.Computer
                        ContentCategory.SPORTS -> Icons.Default.FitnessCenter
                        ContentCategory.MUSIC -> Icons.Default.MusicNote
                        ContentCategory.TRAVEL -> Icons.Default.Flight
                        else -> Icons.Default.Spa
                    },
                    contentDescription = null,
                    tint = HealthogramTheme.colors.primary.copy(alpha = 0.45f),
                    modifier = Modifier.size(72.dp)
                )

                // Category pill on bottom-right of media
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.55f)
                ) {
                    Text(
                        text = post.category.displayName,
                        style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Heart Burst Animation on Double Tap
                if (showHeartAnimation) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.95f),
                        modifier = Modifier
                            .size(90.dp)
                            .scale(heartScale)
                    )
                }
            }

            // Action Buttons Bar (Like, Comment, Share, Save)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onLikeClick,
                        modifier = Modifier.testTag("like_btn_${post.postId}")
                    ) {
                        Icon(
                            imageVector = if (post.isLikedByCurrentUser) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.isLikedByCurrentUser) HealthogramTheme.colors.error else HealthogramTheme.colors.textPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    if (!post.isCommentsDisabled) {
                        IconButton(
                            onClick = onCommentsClick,
                            modifier = Modifier.testTag("comment_btn_${post.postId}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Comment",
                                tint = HealthogramTheme.colors.textPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.testTag("share_btn_${post.postId}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = HealthogramTheme.colors.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onSaveClick,
                    modifier = Modifier.testTag("save_btn_${post.postId}")
                ) {
                    Icon(
                        imageVector = if (post.isSavedByCurrentUser) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (post.isSavedByCurrentUser) HealthogramTheme.colors.primary else HealthogramTheme.colors.textPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Engagement & Caption Details
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)) {
                Text(
                    text = "${post.likesCount} likes",
                    style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.textPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row {
                    Text(
                        text = "${post.authorUsername} ",
                        style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.textPrimary
                    )
                    Text(
                        text = post.caption,
                        style = HealthogramTheme.typography.bodySmall,
                        color = HealthogramTheme.colors.textPrimary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Healthcare Verified Educational Disclaimer Card
                if (post.isMedicalContent && post.medicalDisclaimer != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HealthogramTheme.colors.surfaceVariant,
                        border = BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = HealthogramTheme.colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = post.medicalDisclaimer,
                                style = HealthogramTheme.typography.caption.copy(fontSize = 11.sp),
                                color = HealthogramTheme.colors.textMuted
                            )
                        }
                    }
                }

                // Comments link
                if (!post.isCommentsDisabled && post.commentsCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "View all ${post.commentsCount} comments",
                        style = HealthogramTheme.typography.caption,
                        color = HealthogramTheme.colors.textMuted,
                        modifier = Modifier.clickable { onCommentsClick() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
