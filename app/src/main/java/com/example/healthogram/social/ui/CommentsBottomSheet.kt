package com.example.healthogram.social.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.BadgeSize
import com.example.healthogram.designsystem.components.HealthogramVerifiedBadge
import com.example.healthogram.social.PostComment
import com.example.healthogram.social.SocialFeedEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    postId: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = remember { SocialFeedEngine.getInstance() }
    val allCommentsMap by engine.comments.collectAsState()
    val comments = allCommentsMap[postId] ?: emptyList()
    var commentText by remember { mutableStateOf("") }

    val quickEmojis = remember { listOf("❤️", "🔥", "👏", "💯", "🙌", "😍", "✨", "💪") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = HealthogramTheme.colors.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = HealthogramTheme.colors.border) },
        modifier = modifier.testTag("comments_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Comments (${comments.size})",
                    style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.textPrimary
                )
            }

            HorizontalDivider(color = HealthogramTheme.colors.borderLight)

            // Comments List
            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No comments yet",
                            style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Start the conversation!",
                            style = HealthogramTheme.typography.caption,
                            color = HealthogramTheme.colors.textMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(comments, key = { it.commentId }) { comment ->
                        CommentItemRow(
                            comment = comment,
                            onLikeClick = { engine.toggleLikeComment(postId, comment.commentId) }
                        )
                    }
                }
            }

            HorizontalDivider(color = HealthogramTheme.colors.borderLight)

            // Quick Emojis Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                items(quickEmojis) { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { commentText += emoji }
                            .padding(4.dp)
                    )
                }
            }

            // Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(HealthogramTheme.colors.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "U",
                        style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = {
                        Text(
                            "Add a comment...",
                            style = HealthogramTheme.typography.bodySmall,
                            color = HealthogramTheme.colors.textMuted
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("comment_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HealthogramTheme.colors.primary,
                        unfocusedBorderColor = HealthogramTheme.colors.borderLight,
                        focusedContainerColor = HealthogramTheme.colors.surfaceVariant,
                        unfocusedContainerColor = HealthogramTheme.colors.surfaceVariant
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    engine.addComment(
                                        postId = postId,
                                        authorUid = "current_user",
                                        authorName = "You",
                                        authorUsername = "current_user",
                                        text = commentText.trim()
                                    )
                                    commentText = ""
                                }
                            },
                            enabled = commentText.isNotBlank(),
                            modifier = Modifier.testTag("submit_comment_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (commentText.isNotBlank()) HealthogramTheme.colors.primary else HealthogramTheme.colors.textMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
fun CommentItemRow(
    comment: PostComment,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("comment_item_${comment.commentId}"),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(HealthogramTheme.colors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = comment.authorName.take(1),
                    style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.authorUsername,
                        style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.textPrimary
                    )
                    if (comment.isAuthorVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        HealthogramVerifiedBadge(status = VerificationStatus.APPROVED, size = BadgeSize.SMALL)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "just now",
                        style = HealthogramTheme.typography.caption,
                        color = HealthogramTheme.colors.textMuted
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = comment.text,
                    style = HealthogramTheme.typography.bodySmall,
                    color = HealthogramTheme.colors.textPrimary
                )
            }
        }

        // Like comment action
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            IconButton(
                onClick = onLikeClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (comment.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like Comment",
                    tint = if (comment.isLiked) HealthogramTheme.colors.error else HealthogramTheme.colors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (comment.likesCount > 0) {
                Text(
                    text = "${comment.likesCount}",
                    style = HealthogramTheme.typography.caption.copy(fontSize = 10.sp),
                    color = HealthogramTheme.colors.textMuted
                )
            }
        }
    }
}
