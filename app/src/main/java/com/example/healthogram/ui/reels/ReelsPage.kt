package com.example.healthogram.ui.reels

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.BadgeSize
import com.example.healthogram.designsystem.components.HealthogramVerifiedBadge
import com.example.healthogram.social.SocialFeedEngine
import com.example.healthogram.social.SocialReel
import com.example.healthogram.social.ui.CommentsBottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReelsPage(
    modifier: Modifier = Modifier,
    onCommentsClick: (String) -> Unit = {},
    onShareClick: (String) -> Unit = {}
) {
    val engine = remember { SocialFeedEngine.getInstance() }
    val reels by engine.reels.collectAsState()

    var activeReelIndex by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var activeCommentsReelId by remember { mutableStateOf<String?>(null) }
    var showHeartBurst by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val currentReel = remember(reels, activeReelIndex) {
        if (reels.isNotEmpty()) reels[activeReelIndex.coerceIn(0, reels.size - 1)] else null
    }

    // Music vinyl rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "music_disc")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_anim"
    )

    val heartScale by animateFloatAsState(
        targetValue = if (showHeartBurst) 1.25f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "burst_anim"
    )

    if (currentReel == null) {
        Box(modifier = modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("reels_page")
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (!currentReel.isLiked) {
                            engine.toggleLikeReel(currentReel.reelId)
                        }
                        showHeartBurst = true
                        coroutineScope.launch {
                            delay(700)
                            showHeartBurst = false
                        }
                    },
                    onTap = {
                        isPlaying = !isPlaying
                    }
                )
            }
    ) {
        // Video Mock Backdrop with Rich Vertical Atmospheric Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF1E1B4B),
                            Color(0xFF020617)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!isPlaying) {
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = "Paused",
                    tint = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(76.dp)
                )
            }
        }

        // Heart Burst on Double Tap
        if (showHeartBurst) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.95f),
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.Center)
                    .scale(heartScale)
            )
        }

        // Top Navigation Controls (Header, Switch Reel, Mute)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 42.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Reels",
                    style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${activeReelIndex + 1}/${reels.size}",
                        style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Next Reel switcher button
                if (reels.size > 1) {
                    IconButton(
                        onClick = {
                            activeReelIndex = (activeReelIndex + 1) % reels.size
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                            .testTag("next_reel_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Reel",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Sound Toggle
                IconButton(
                    onClick = { isMuted = !isMuted },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Mute Toggle",
                        tint = Color.White
                    )
                }
            }
        }

        // Right Floating Action Sidebar (Like, Comment, Share, Save, Music Disc)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { engine.toggleLikeReel(currentReel.reelId) },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .testTag("reel_like_btn")
                ) {
                    Icon(
                        imageVector = if (currentReel.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like Reel",
                        tint = if (currentReel.isLiked) HealthogramTheme.colors.error else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = "${currentReel.likesCount}",
                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            // Comment Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        activeCommentsReelId = currentReel.reelId
                        onCommentsClick(currentReel.reelId)
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .testTag("reel_comment_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment Reel",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = "${currentReel.commentsCount}",
                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            // Share Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        onShareClick(currentReel.reelId)
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .testTag("reel_share_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share Reel",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = "${currentReel.sharesCount}",
                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            // Spinning Vinyl Music Disc
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
                    .rotate(if (isPlaying) discRotation else 0f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Bottom Creator Overlay (Author, Verification, Follow, Caption, Audio Track)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.82f)
                .padding(start = 16.dp, bottom = 80.dp)
        ) {
            // Category Badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = HealthogramTheme.colors.primary.copy(alpha = 0.85f),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Text(
                    text = currentReel.category.displayName,
                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Author Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(HealthogramTheme.colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentReel.authorName.take(1),
                        style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = currentReel.authorName,
                    style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                if (currentReel.isAuthorVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    HealthogramVerifiedBadge(status = VerificationStatus.APPROVED, size = BadgeSize.SMALL)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (currentReel.isFollowing) Color.White.copy(alpha = 0.25f) else HealthogramTheme.colors.primary,
                    modifier = Modifier.clickable { engine.toggleFollowReelAuthor(currentReel.reelId) }
                ) {
                    Text(
                        text = if (currentReel.isFollowing) "Following" else "Follow",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Caption
            Text(
                text = currentReel.caption,
                style = HealthogramTheme.typography.bodySmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Audio Track Marquee Indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = currentReel.audioTrackTitle,
                    style = HealthogramTheme.typography.caption,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Active Comments Sheet
        activeCommentsReelId?.let { reelId ->
            CommentsBottomSheet(
                postId = reelId,
                onDismiss = { activeCommentsReelId = null }
            )
        }
    }
}
