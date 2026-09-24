package com.example.healthogram.social.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.BadgeSize
import com.example.healthogram.designsystem.components.HealthogramVerifiedBadge
import com.example.healthogram.social.SocialStory
import com.example.healthogram.social.StoryStickerType
import kotlinx.coroutines.delay

@Composable
fun StoryViewerOverlay(
    stories: List<SocialStory>,
    initialIndex: Int = 0,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (stories.isEmpty()) {
        LaunchedEffect(Unit) { onClose() }
        return
    }

    var currentIndex by remember { mutableIntStateOf(initialIndex.coerceIn(0, stories.size - 1)) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var isLiked by remember { mutableStateOf(false) }
    var selectedPollOption by remember { mutableStateOf<Int?>(null) }

    val currentStory = stories[currentIndex]

    // Story progress auto-advance timer
    LaunchedEffect(currentIndex, isPaused) {
        progress = 0f
        val stepMs = 50L
        val totalDurationMs = 5000L
        val increment = stepMs.toFloat() / totalDurationMs.toFloat()

        while (progress < 1f) {
            if (!isPaused) {
                delay(stepMs)
                progress = (progress + increment).coerceAtMost(1f)
            } else {
                delay(100L)
            }
        }

        if (currentIndex < stories.size - 1) {
            currentIndex++
            selectedPollOption = null
        } else {
            onClose()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("story_viewer_overlay")
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPaused = true
                        tryAwaitRelease()
                        isPaused = false
                    },
                    onTap = { offset ->
                        val screenWidth = size.width
                        if (offset.x < screenWidth * 0.35f) {
                            // Previous
                            if (currentIndex > 0) {
                                currentIndex--
                                selectedPollOption = null
                            }
                        } else {
                            // Next
                            if (currentIndex < stories.size - 1) {
                                currentIndex++
                                selectedPollOption = null
                            } else {
                                onClose()
                            }
                        }
                    }
                )
            }
    ) {
        // Story Visual Backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E1B4B),
                            Color(0xFF312E81),
                            Color(0xFF0F172A)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Caption in center
            if (currentStory.caption.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = currentStory.caption,
                        style = HealthogramTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 24.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // Interactive Stickers (Poll, Location, etc.)
        currentStory.stickers.forEach { sticker ->
            when (sticker.type) {
                StoryStickerType.POLL -> {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 180.dp)
                            .width(280.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = sticker.text,
                                style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            sticker.options.forEachIndexed { optIndex, optionText ->
                                val isSelected = selectedPollOption == optIndex
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { selectedPollOption = optIndex },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) HealthogramTheme.colors.primary else Color(0xFFF1F5F9)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = optionText,
                                            style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) Color.White else Color(0xFF1E293B)
                                        )
                                        if (selectedPollOption != null) {
                                            Text(
                                                text = if (optIndex == 0) "78%" else "22%",
                                                style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) Color.White else Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                StoryStickerType.LOCATION -> {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 220.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = HealthogramTheme.colors.primary
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = sticker.text,
                                style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
                else -> {}
            }
        }

        // Top Progress Bars & Author Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 12.dp, end = 12.dp)
        ) {
            // Segmented Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                stories.forEachIndexed { index, _ ->
                    val segmentProgress = when {
                        index < currentIndex -> 1f
                        index == currentIndex -> progress
                        else -> 0f
                    }
                    val animatedProgress by animateFloatAsState(targetValue = segmentProgress, label = "segment_anim")

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.35f)
                    )
                }
            }

            // Author Profile Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(HealthogramTheme.colors.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentStory.authorName.take(1),
                            style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentStory.authorUsername,
                                style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            if (currentStory.isAuthorVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                HealthogramVerifiedBadge(status = VerificationStatus.APPROVED, size = BadgeSize.SMALL)
                            }
                        }
                        Text(
                            text = "2h ago",
                            style = HealthogramTheme.typography.caption,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_story_viewer_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // Bottom Quick Reply Bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = replyText,
                onValueChange = { replyText = it },
                placeholder = {
                    Text(
                        text = "Reply to ${currentStory.authorUsername}...",
                        color = Color.White.copy(alpha = 0.6f),
                        style = HealthogramTheme.typography.bodySmall
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("story_reply_input"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White.copy(alpha = 0.7f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                    focusedContainerColor = Color.Black.copy(alpha = 0.35f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.35f)
                ),
                trailingIcon = {
                    if (replyText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                replyText = ""
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Reply",
                                tint = Color.White
                            )
                        }
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = { isLiked = !isLiked },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
                    .testTag("story_like_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Like Story",
                    tint = if (isLiked) HealthogramTheme.colors.error else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
