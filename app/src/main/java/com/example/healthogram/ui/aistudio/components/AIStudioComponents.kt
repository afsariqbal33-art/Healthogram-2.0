package com.example.healthogram.ui.aistudio.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.aistudio.*
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*

@Composable
fun AIStudioHeader(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_studio_header"),
        color = HealthogramTheme.colors.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("ai_header_back_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = HealthogramTheme.colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(HealthogramTheme.colors.brandGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.textPrimary
                )
                Text(
                    text = subtitle,
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.textMuted
                )
            }

            actions()
        }
    }
}

@Composable
fun AIToolCard(
    title: String,
    description: String,
    icon: ImageVector,
    categoryBadge: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "ai_tool_card"
) {
    HealthogramBasicCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(HealthogramTheme.colors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = HealthogramTheme.shapes.pill,
                        color = HealthogramTheme.colors.surfaceVariant
                    ) {
                        Text(
                            text = categoryBadge,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = HealthogramTheme.typography.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                            color = HealthogramTheme.colors.secondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.textMuted,
                    maxLines = 2
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = HealthogramTheme.colors.textMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun AIImageToolCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    AIToolCard(
        title = title,
        description = description,
        icon = Icons.Default.Image,
        categoryBadge = "Image AI",
        onClick = onClick,
        testTag = "ai_image_tool_card"
    )
}

@Composable
fun AIVideoToolCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    AIToolCard(
        title = title,
        description = description,
        icon = Icons.Default.VideoLibrary,
        categoryBadge = "Video AI",
        onClick = onClick,
        testTag = "ai_video_tool_card"
    )
}

@Composable
fun AICreatorToolCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    AIToolCard(
        title = title,
        description = description,
        icon = icon,
        categoryBadge = "Creator Suite",
        onClick = onClick,
        testTag = "ai_creator_tool_card"
    )
}

@Composable
fun AISellerToolCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    AIToolCard(
        title = title,
        description = description,
        icon = icon,
        categoryBadge = "Seller Suite",
        onClick = onClick,
        testTag = "ai_seller_tool_card"
    )
}

@Composable
fun AIUsageCard(
    usageSummary: AIUsageSummary,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HealthogramBasicCard(modifier = modifier.testTag("ai_usage_card")) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = null,
                        tint = HealthogramTheme.colors.warning,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI Usage Quota",
                        style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                TextButton(onClick = onUpgradeClick) {
                    Text("Plans & Quota", style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            val percent = (usageSummary.monthlyUnits.toFloat() / usageSummary.limit.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { percent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (percent > 0.85f) HealthogramTheme.colors.error else HealthogramTheme.colors.primary,
                trackColor = HealthogramTheme.colors.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${usageSummary.monthlyUnits} / ${usageSummary.limit} units used",
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.textMuted
                )
                Text(
                    text = "${usageSummary.remaining} units remaining",
                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                    color = if (usageSummary.remaining <= 10) HealthogramTheme.colors.error else HealthogramTheme.colors.primary
                )
            }
        }
    }
}

@Composable
fun AIHistoryCard(
    item: AIHistoryItem,
    onClick: () -> Unit
) {
    HealthogramBasicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("ai_history_card_${item.historyId}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(HealthogramTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (item.outputType) {
                    "image" -> Icons.Default.Image
                    "video" -> Icons.Default.Videocam
                    else -> Icons.Default.Notes
                }
                Icon(icon, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    text = "${item.toolType.name.replace("_", " ")} • ${item.status.name}",
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.textMuted
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = HealthogramTheme.colors.textMuted, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun AIJobProgressCard(
    job: AIJob,
    onCancel: () -> Unit
) {
    HealthogramBasicCard(modifier = Modifier.testTag("ai_job_progress_card")) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Job: ${job.toolType.name}",
                    style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Surface(
                    shape = HealthogramTheme.shapes.pill,
                    color = HealthogramTheme.colors.primaryContainer
                ) {
                    Text(
                        text = job.status.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel Job", color = HealthogramTheme.colors.error)
                }
            }
        }
    }
}

@Composable
fun AIResultPreview(
    title: String = "Generated Result",
    content: String,
    hashtags: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_result_preview"),
        shape = HealthogramTheme.shapes.medium,
        color = HealthogramTheme.colors.surfaceVariant,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.primary
                )
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = HealthogramTheme.colors.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = HealthogramTheme.typography.bodyMedium,
                color = HealthogramTheme.colors.textPrimary
            )
            if (hashtags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = hashtags.joinToString(" "),
                    style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = HealthogramTheme.colors.secondary
                )
            }
        }
    }
}

@Composable
fun AIResultActionBar(
    onCopy: () -> Unit,
    onUseInPost: () -> Unit,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_result_action_bar"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onCopy,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Copy")
        }
        OutlinedButton(
            onClick = onRegenerate,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Retry")
        }
        Button(
            onClick = onUseInPost,
            modifier = Modifier.weight(1.2f)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Apply")
        }
    }
}

@Composable
fun AIImageBeforeAfter(
    originalLabel: String = "Original",
    enhancedLabel: String = "AI Enhanced",
    enhancementSummary: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_image_before_after"),
        shape = HealthogramTheme.shapes.medium,
        color = HealthogramTheme.colors.surfaceVariant,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Original Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                        .clip(HealthogramTheme.shapes.small)
                        .background(HealthogramTheme.colors.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = HealthogramTheme.colors.textMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(originalLabel, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                    }
                }
                // Enhanced Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                        .clip(HealthogramTheme.shapes.small)
                        .background(HealthogramTheme.colors.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = HealthogramTheme.colors.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(enhancedLabel, style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = enhancementSummary,
                style = HealthogramTheme.typography.caption,
                color = HealthogramTheme.colors.textSecondary
            )
        }
    }
}

@Composable
fun AIUploadCard(
    label: String = "Tap to upload or take a photo",
    onUploadClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUploadClick)
            .testTag("ai_upload_card"),
        shape = HealthogramTheme.shapes.medium,
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.5.dp, HealthogramTheme.colors.primary.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(HealthogramTheme.colors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = HealthogramTheme.colors.primary)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(label, style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
            Text("Max 25MB • JPG, PNG, WEBP", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
        }
    }
}

@Composable
fun AIPromptInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Describe what you want to generate...",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HealthogramTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            singleLine = false,
            maxLines = 4,
            modifier = Modifier.testTag("ai_prompt_input")
        )
    }
}

@Composable
fun AIToneSelector(
    selectedTone: String,
    onToneSelected: (String) -> Unit,
    tones: List<String> = listOf("Professional", "Friendly", "Educational", "Scientific", "Inspirational")
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Tone of Voice", style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tones.forEach { tone ->
                val isSelected = selectedTone == tone
                Surface(
                    shape = HealthogramTheme.shapes.pill,
                    color = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.surfaceVariant,
                    modifier = Modifier.clickable { onToneSelected(tone) }
                ) {
                    Text(
                        text = tone,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = HealthogramTheme.typography.caption.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                        color = if (isSelected) Color.White else HealthogramTheme.colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun AILanguageSelector(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf("English", "Arabic (العربية)", "French", "Spanish")
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Language", style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            languages.forEach { lang ->
                val isSelected = selectedLanguage == lang
                Surface(
                    shape = HealthogramTheme.shapes.pill,
                    color = if (isSelected) HealthogramTheme.colors.secondary else HealthogramTheme.colors.surfaceVariant,
                    modifier = Modifier.clickable { onLanguageSelected(lang) }
                ) {
                    Text(
                        text = lang,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = HealthogramTheme.typography.caption.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                        color = if (isSelected) Color.White else HealthogramTheme.colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun AILengthSelector(
    selectedLength: String,
    onLengthSelected: (String) -> Unit
) {
    val lengths = listOf("Short (1-2 lines)", "Medium (1 paragraph)", "Comprehensive (Full article)")
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Length", style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            lengths.forEach { len ->
                val isSelected = selectedLength.startsWith(len.take(5))
                Surface(
                    shape = HealthogramTheme.shapes.pill,
                    color = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.surfaceVariant,
                    modifier = Modifier.clickable { onLengthSelected(len) }
                ) {
                    Text(
                        text = len.split(" ")[0],
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = HealthogramTheme.typography.caption.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                        color = if (isSelected) Color.White else HealthogramTheme.colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun AIConsentCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_consent_card"),
        shape = HealthogramTheme.shapes.medium,
        color = HealthogramTheme.colors.surfaceVariant,
        border = BorderStroke(1.dp, HealthogramTheme.colors.infoContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = HealthogramTheme.colors.info, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ethical Policy: AI Studio assists with creative drafting and media styling. It must NEVER be used for automated clinical diagnosis, fake certifications, or unverified cures. All generated content requires your review prior to publishing.",
                style = HealthogramTheme.typography.caption,
                color = HealthogramTheme.colors.textSecondary
            )
        }
    }
}

@Composable
fun AIErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_error_card"),
        shape = HealthogramTheme.shapes.medium,
        color = HealthogramTheme.colors.errorContainer,
        border = BorderStroke(1.dp, HealthogramTheme.colors.error)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = HealthogramTheme.colors.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generation Alert", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.error)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(message, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textPrimary)
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = HealthogramTheme.colors.error)) {
                Text("Retry", color = Color.White)
            }
        }
    }
}

@Composable
fun AIEmptyState(
    title: String = "No AI Generations Yet",
    description: String = "Select any tool above to draft captions, enhance photos, or write compliant product specs.",
    icon: ImageVector = Icons.Default.AutoAwesome
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
            .testTag("ai_empty_state"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(HealthogramTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(4.dp))
        Text(description, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted, textAlign = TextAlign.Center)
    }
}

@Composable
fun AILoadingState(
    label: String = "Generating with Google Gemini..."
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .testTag("ai_loading_state"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(36.dp),
            color = HealthogramTheme.colors.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(label, style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold), color = HealthogramTheme.colors.primary)
    }
}

@Composable
fun AIUpgradeCard(
    onUpgradeClick: () -> Unit
) {
    HealthogramBasicCard(modifier = Modifier.testTag("ai_upgrade_card")) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = HealthogramTheme.colors.warning)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Healthogram AI Pro Tier", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Unlock unlimited 4K product retouching, batch caption creation, and instant multi-language localizations.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
            Spacer(modifier = Modifier.height(10.dp))
            HealthogramPrimaryButton(text = "View Pro Plans", onClick = onUpgradeClick, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun AIUsageLimitCard(
    onUpgradeClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_usage_limit_card"),
        shape = HealthogramTheme.shapes.medium,
        color = HealthogramTheme.colors.errorContainer,
        border = BorderStroke(1.dp, HealthogramTheme.colors.error)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Monthly Limit Reached", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.error)
            Spacer(modifier = Modifier.height(4.dp))
            Text("You have reached your free monthly quota. Upgrade your plan to continue using AI tools immediately.", style = HealthogramTheme.typography.caption)
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = onUpgradeClick, colors = ButtonDefaults.buttonColors(containerColor = HealthogramTheme.colors.error)) {
                Text("Upgrade Quota", color = Color.White)
            }
        }
    }
}
