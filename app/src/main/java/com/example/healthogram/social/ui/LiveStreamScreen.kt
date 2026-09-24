package com.example.healthogram.social.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.BadgeSize
import com.example.healthogram.designsystem.components.HealthogramVerifiedBadge
import com.example.healthogram.social.LiveStreamSession
import com.example.healthogram.social.SocialFeedEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LiveStreamScreen(
    stream: LiveStreamSession,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = remember { SocialFeedEngine.getInstance() }
    val allMessagesMap by engine.liveMessages.collectAsState()
    val chatMessages = allMessagesMap[stream.streamId] ?: emptyList()

    var chatInput by remember { mutableStateOf("") }
    var likesCount by remember { mutableIntStateOf(stream.likesCount) }
    var isFollowing by remember { mutableStateOf(false) }

    // Floating heart reaction trigger
    var heartCount by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("live_stream_screen")
    ) {
        // Video Studio / Camera Feed Backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF1E293B),
                            Color(0xFF020617)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(HealthogramTheme.colors.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stream.hostName.take(1),
                        style = HealthogramTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stream.title,
                    style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HealthogramTheme.colors.primary.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = stream.category.displayName,
                        style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Top Navigation & Stats Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 42.dp, start = 14.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Host Information
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(HealthogramTheme.colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stream.hostName.take(1),
                        style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stream.hostUsername,
                            style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        if (stream.isHostVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            HealthogramVerifiedBadge(status = VerificationStatus.APPROVED, size = BadgeSize.SMALL)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isFollowing) Color.White.copy(alpha = 0.2f) else HealthogramTheme.colors.primary,
                    modifier = Modifier.clickable { isFollowing = !isFollowing }
                ) {
                    Text(
                        text = if (isFollowing) "Following" else "Follow",
                        style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Right Badges: LIVE indicator + Viewer Count + Close
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = HealthogramTheme.colors.error
                ) {
                    Text(
                        text = "LIVE",
                        style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.45f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${stream.viewerCount}",
                            style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .testTag("close_live_stream_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Live Chat Feed (Bottom Overlay)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(bottom = 84.dp, start = 14.dp, end = 80.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .heightIn(max = 240.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(chatMessages, key = { it.messageId }) { msg ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.45f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${msg.senderUsername}: ",
                                style = HealthogramTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (msg.isHost) HealthogramTheme.colors.primary else Color(0xFF38BDF8)
                                )
                            )
                            Text(
                                text = msg.message,
                                style = HealthogramTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Floating Hearts Animation Visual Indicator
        if (heartCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 140.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = HealthogramTheme.colors.error,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Bottom Chat Input Bar & Reaction Button
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { chatInput = it },
                placeholder = {
                    Text(
                        "Comment in live chat...",
                        color = Color.White.copy(alpha = 0.6f),
                        style = HealthogramTheme.typography.bodySmall
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("live_chat_input"),
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
                    if (chatInput.isNotBlank()) {
                        IconButton(
                            onClick = {
                                engine.sendLiveChatMessage(
                                    streamId = stream.streamId,
                                    senderUid = "current_user",
                                    senderName = "You",
                                    senderUsername = "you",
                                    text = chatInput.trim()
                                )
                                chatInput = ""
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Floating Heart Tap Button
            IconButton(
                onClick = {
                    likesCount++
                    heartCount++
                    engine.sendLiveHeart(stream.streamId)
                    coroutineScope.launch {
                        delay(600)
                        heartCount = (heartCount - 1).coerceAtLeast(0)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(HealthogramTheme.colors.error.copy(alpha = 0.85f))
                    .testTag("live_stream_heart_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Send Heart",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
