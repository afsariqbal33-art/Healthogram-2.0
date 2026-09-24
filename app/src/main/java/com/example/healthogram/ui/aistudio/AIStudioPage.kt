package com.example.healthogram.ui.aistudio

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.aistudio.*
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.ui.aistudio.components.*
import com.example.healthogram.ui.aistudio.pages.*

enum class AIStudioSubScreen {
    HOME,
    CREATOR_SUITE,
    SELLER_SUITE,
    CAPTION_TOOL,
    HASHTAG_TOOL,
    CONTENT_IDEAS_TOOL,
    IMAGE_EDITOR_TOOL,
    VIDEO_EDITOR_TOOL,
    PRODUCT_TITLE_TOOL,
    PRODUCT_DESCRIPTION_TOOL,
    PRODUCT_TAGS_TOOL,
    MARKETING_CREATIVE_TOOL,
    HISTORY,
    USAGE,
    SETTINGS
}

@Composable
fun AIStudioPage(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    var currentSubScreen by remember { mutableStateOf(AIStudioSubScreen.HOME) }
    val repository = remember { AIStudioRepository.getInstance() }
    val usageSummary by repository.currentUsage.collectAsState()
    val historyItems by repository.history.collectAsState()

    when (currentSubScreen) {
        AIStudioSubScreen.HOME -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(HealthogramTheme.colors.background)
                    .testTag("ai_studio_home_page"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header
                item {
                    AIStudioHeader(
                        title = "Healthogram AI Studio",
                        subtitle = "Generative suite for creators, doctors & sellers",
                        onBack = onBack,
                        actions = {
                            IconButton(onClick = { currentSubScreen = AIStudioSubScreen.SETTINGS }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = HealthogramTheme.colors.textMuted)
                            }
                        }
                    )
                }

                // Medical Guardrail Banner
                item {
                    AIConsentCard()
                }

                // Usage Meter Card
                item {
                    AIUsageCard(
                        usageSummary = usageSummary,
                        onUpgradeClick = { currentSubScreen = AIStudioSubScreen.USAGE }
                    )
                }

                // Studio Modes Selector
                item {
                    Text(
                        text = "Studio Suites",
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Creator Studio Button Card
                        HealthogramBasicCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { currentSubScreen = AIStudioSubScreen.CREATOR_SUITE }
                                .testTag("btn_open_creator_suite")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(HealthogramTheme.colors.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Palette, contentDescription = null, tint = HealthogramTheme.colors.primary)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Creator Suite", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text("Posts, Captions & Video", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            }
                        }

                        // Seller Studio Button Card
                        HealthogramBasicCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { currentSubScreen = AIStudioSubScreen.SELLER_SUITE }
                                .testTag("btn_open_seller_suite")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(HealthogramTheme.colors.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = HealthogramTheme.colors.secondary)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Seller Suite", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text("Specs, Titles & Banners", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            }
                        }
                    }
                }

                // Quick Tools
                item {
                    Text(
                        text = "Quick Tools",
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                item {
                    AIToolCard(
                        title = "Smart Caption & Hook Writer",
                        description = "Draft engaging educational health copy with verified disclaimers.",
                        icon = Icons.Default.ChatBubble,
                        categoryBadge = "Popular",
                        onClick = { currentSubScreen = AIStudioSubScreen.CAPTION_TOOL }
                    )
                }

                item {
                    AIToolCard(
                        title = "Neural Image & Portrait Retouch",
                        description = "One-tap lighting correction, noise reduction & studio backdrops.",
                        icon = Icons.Default.Image,
                        categoryBadge = "Media",
                        onClick = { currentSubScreen = AIStudioSubScreen.IMAGE_EDITOR_TOOL }
                    )
                }

                item {
                    AIToolCard(
                        title = "Marketplace Product Description Writer",
                        description = "Produce compliant e-commerce specs and user instructions.",
                        icon = Icons.Default.Description,
                        categoryBadge = "E-Commerce",
                        onClick = { currentSubScreen = AIStudioSubScreen.PRODUCT_DESCRIPTION_TOOL }
                    )
                }

                // Recent Generations History
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Generations",
                            style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        TextButton(onClick = { currentSubScreen = AIStudioSubScreen.HISTORY }) {
                            Text("See All", style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                items(historyItems.take(3)) { hist ->
                    AIHistoryCard(item = hist, onClick = { /* view */ })
                }
            }
        }

        AIStudioSubScreen.CREATOR_SUITE -> {
            AIStudioCreatorPage(
                onNavigateToTool = { tool ->
                    when (tool) {
                        AIToolType.CAPTION_GENERATOR -> currentSubScreen = AIStudioSubScreen.CAPTION_TOOL
                        AIToolType.HASHTAG_GENERATOR -> currentSubScreen = AIStudioSubScreen.HASHTAG_TOOL
                        AIToolType.CONTENT_IDEAS -> currentSubScreen = AIStudioSubScreen.CONTENT_IDEAS_TOOL
                        AIToolType.IMAGE_ENHANCEMENT -> currentSubScreen = AIStudioSubScreen.IMAGE_EDITOR_TOOL
                        AIToolType.BACKGROUND_REMOVAL -> currentSubScreen = AIStudioSubScreen.IMAGE_EDITOR_TOOL
                        AIToolType.REEL_HOOKS -> currentSubScreen = AIStudioSubScreen.VIDEO_EDITOR_TOOL
                        else -> {}
                    }
                },
                onBack = { currentSubScreen = AIStudioSubScreen.HOME }
            )
        }

        AIStudioSubScreen.SELLER_SUITE -> {
            SellerAIStudioPage(
                onNavigateToSellerTool = { tool ->
                    when (tool) {
                        AIToolType.PRODUCT_TITLE -> currentSubScreen = AIStudioSubScreen.PRODUCT_TITLE_TOOL
                        AIToolType.PRODUCT_DESCRIPTION -> currentSubScreen = AIStudioSubScreen.PRODUCT_DESCRIPTION_TOOL
                        AIToolType.PRODUCT_TAGS -> currentSubScreen = AIStudioSubScreen.PRODUCT_TAGS_TOOL
                        AIToolType.PROMOTIONAL_BANNER -> currentSubScreen = AIStudioSubScreen.MARKETING_CREATIVE_TOOL
                        AIToolType.PRODUCT_IMAGE_ENHANCE -> currentSubScreen = AIStudioSubScreen.IMAGE_EDITOR_TOOL
                        AIToolType.PRODUCT_VIDEO_ASSISTANCE -> currentSubScreen = AIStudioSubScreen.VIDEO_EDITOR_TOOL
                        else -> {}
                    }
                },
                onBack = { currentSubScreen = AIStudioSubScreen.HOME }
            )
        }

        AIStudioSubScreen.CAPTION_TOOL -> AIStudioCaptionPage(onBack = { currentSubScreen = AIStudioSubScreen.HOME })
        AIStudioSubScreen.HASHTAG_TOOL -> AIStudioHashtagPage(onBack = { currentSubScreen = AIStudioSubScreen.HOME })
        AIStudioSubScreen.CONTENT_IDEAS_TOOL -> AIStudioContentIdeasPage(onBack = { currentSubScreen = AIStudioSubScreen.HOME })
        AIStudioSubScreen.IMAGE_EDITOR_TOOL -> AIStudioImageEditorPage(onBack = { currentSubScreen = AIStudioSubScreen.HOME })
        AIStudioSubScreen.VIDEO_EDITOR_TOOL -> AIStudioVideoEditorPage(onBack = { currentSubScreen = AIStudioSubScreen.HOME })
        AIStudioSubScreen.PRODUCT_TITLE_TOOL -> SellerAIProductTitlePage(onBack = { currentSubScreen = AIStudioSubScreen.SELLER_SUITE })
        AIStudioSubScreen.PRODUCT_DESCRIPTION_TOOL -> SellerAIProductDescriptionPage(onBack = { currentSubScreen = AIStudioSubScreen.SELLER_SUITE })
        AIStudioSubScreen.PRODUCT_TAGS_TOOL -> SellerAIProductTagsPage(onBack = { currentSubScreen = AIStudioSubScreen.SELLER_SUITE })
        AIStudioSubScreen.MARKETING_CREATIVE_TOOL -> SellerAIMarketingCreativePage(onBack = { currentSubScreen = AIStudioSubScreen.SELLER_SUITE })
        AIStudioSubScreen.HISTORY -> AIStudioHistoryPage(onBack = { currentSubScreen = AIStudioSubScreen.HOME })
        AIStudioSubScreen.USAGE -> AIStudioUsagePage(onBack = { currentSubScreen = AIStudioSubScreen.HOME })
        AIStudioSubScreen.SETTINGS -> AIStudioSettingsPage(onBack = { currentSubScreen = AIStudioSubScreen.HOME })
    }
}
