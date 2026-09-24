package com.example.healthogram.ui.aistudio.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.aistudio.*
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.ui.aistudio.components.*
import kotlinx.coroutines.launch

@Composable
fun AIStudioCreatorPage(
    onNavigateToTool: (AIToolType) -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("ai_studio_creator_page"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AIStudioHeader(
                title = "Creator AI Studio",
                subtitle = "Health education, social media copy & media suite",
                onBack = onBack
            )
        }

        item {
            AIConsentCard()
        }

        item {
            Text(
                text = "Writing & Content Generators",
                style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            AICreatorToolCard(
                title = "Caption & Hook Generator",
                description = "Generate verified educational post copy, trending hooks & compliance notices.",
                icon = Icons.Default.ChatBubble,
                onClick = { onNavigateToTool(AIToolType.CAPTION_GENERATOR) }
            )
        }

        item {
            AICreatorToolCard(
                title = "Smart Hashtag Engine",
                description = "Discover high-reach medical awareness and wellness hashtags.",
                icon = Icons.Default.Tag,
                onClick = { onNavigateToTool(AIToolType.HASHTAG_GENERATOR) }
            )
        }

        item {
            AICreatorToolCard(
                title = "Educational Content Ideas",
                description = "Turn clinical topics into engaging carousels, reels, and infographics.",
                icon = Icons.Default.Lightbulb,
                onClick = { onNavigateToTool(AIToolType.CONTENT_IDEAS) }
            )
        }

        item {
            Text(
                text = "Visual & Media Enhancers",
                style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            AIImageToolCard(
                title = "Clinical Photo & Portrait Enhancer",
                description = "Correct studio lighting, remove digital grain, and balance contrast.",
                onClick = { onNavigateToTool(AIToolType.IMAGE_ENHANCEMENT) }
            )
        }

        item {
            AIImageToolCard(
                title = "Background Isolator & Replacement",
                description = "Extract subject cleanly and place in modern clinical or nature scenes.",
                onClick = { onNavigateToTool(AIToolType.BACKGROUND_REMOVAL) }
            )
        }

        item {
            AIVideoToolCard(
                title = "Reel Script & Scene Assistant",
                description = "Plan reel hooks, 60-second scene pacing, and audio recommendations.",
                onClick = { onNavigateToTool(AIToolType.REEL_HOOKS) }
            )
        }
    }
}

@Composable
fun AIStudioCaptionPage(
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var topic by remember { mutableStateOf("") }
    var selectedTone by remember { mutableStateOf("Professional") }
    var selectedLanguage by remember { mutableStateOf("English") }
    var selectedLength by remember { mutableStateOf("Medium") }
    var keywords by remember { mutableStateOf("") }

    var isGenerating by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<AICaptionResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("ai_studio_caption_page"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AIStudioHeader(
                title = "Smart Caption Generator",
                subtitle = "Evidence-informed health awareness copywriter",
                onBack = onBack
            )
        }

        item {
            AIConsentCard()
        }

        item {
            AIPromptInput(
                value = topic,
                onValueChange = { topic = it },
                placeholder = "e.g. Benefits of staying hydrated during physical endurance training..."
            )
        }

        item {
            AIToneSelector(
                selectedTone = selectedTone,
                onToneSelected = { selectedTone = it }
            )
        }

        item {
            AILanguageSelector(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { selectedLanguage = it }
            )
        }

        item {
            AILengthSelector(
                selectedLength = selectedLength,
                onLengthSelected = { selectedLength = it }
            )
        }

        item {
            HealthogramPrimaryButton(
                text = "Generate Caption",
                icon = Icons.Default.AutoAwesome,
                onClick = {
                    if (topic.isBlank()) return@HealthogramPrimaryButton
                    coroutineScope.launch {
                        isGenerating = true
                        errorMessage = null
                        try {
                            val res = AIStudioCustomActions.generateAICaption(
                                topic = topic,
                                tone = selectedTone,
                                language = selectedLanguage,
                                length = selectedLength,
                                keywords = keywords
                            )
                            result = res
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to generate caption"
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                state = if (isGenerating) ButtonState.LOADING else ButtonState.NORMAL,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (errorMessage != null) {
            item {
                AIErrorCard(message = errorMessage!!, onRetry = { errorMessage = null })
            }
        }

        if (result != null) {
            item {
                AIResultPreview(
                    title = "Primary Caption",
                    content = result!!.caption,
                    hashtags = result!!.hashtags
                )
            }

            if (result!!.ctaSuggestion.isNotBlank()) {
                item {
                    Text(
                        text = "Call-To-Action: ${result!!.ctaSuggestion}",
                        style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                        color = HealthogramTheme.colors.primary
                    )
                }
            }

            item {
                AIResultActionBar(
                    onCopy = { /* copied */ },
                    onUseInPost = { /* applied to composer */ },
                    onRegenerate = {
                        // Re-trigger
                    }
                )
            }
        }
    }
}

@Composable
fun AIStudioHashtagPage(
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var topic by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<AIHashtagResult?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("ai_studio_hashtag_page"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AIStudioHeader(
                title = "Hashtag Generator",
                subtitle = "Discover high-reach health awareness tags",
                onBack = onBack
            )
        }

        item {
            AIPromptInput(
                value = topic,
                onValueChange = { topic = it },
                placeholder = "Enter post topic (e.g., Cardiology Tips, Nutrition Prep)..."
            )
        }

        item {
            HealthogramPrimaryButton(
                text = "Curate Hashtags",
                icon = Icons.Default.Tag,
                onClick = {
                    if (topic.isBlank()) return@HealthogramPrimaryButton
                    coroutineScope.launch {
                        isGenerating = true
                        errorMessage = null
                        try {
                            result = AIStudioCustomActions.generateAIHashtags(topic = topic)
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to generate hashtags"
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                state = if (isGenerating) ButtonState.LOADING else ButtonState.NORMAL,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (errorMessage != null) {
            item {
                AIErrorCard(message = errorMessage!!, onRetry = { errorMessage = null })
            }
        }

        if (result != null) {
            item {
                AIResultPreview(
                    title = "Recommended Tags",
                    content = result!!.recommendedHashtags.joinToString(" "),
                    hashtags = result!!.categoryTags
                )
            }
            item {
                AIResultActionBar(
                    onCopy = {},
                    onUseInPost = {},
                    onRegenerate = {}
                )
            }
        }
    }
}

@Composable
fun AIStudioContentIdeasPage(
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var topic by remember { mutableStateOf("") }
    var ideas by remember { mutableStateOf<List<AIContentIdea>>(emptyList()) }
    var isGenerating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("ai_studio_content_ideas_page"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AIStudioHeader(
                title = "Content Ideas Generator",
                subtitle = "Brainstorm educational hooks, carousels & video prompts",
                onBack = onBack
            )
        }

        item {
            AIPromptInput(
                value = topic,
                onValueChange = { topic = it },
                placeholder = "Topic or theme (e.g. Ergonomics for Remote Workers)..."
            )
        }

        item {
            HealthogramPrimaryButton(
                text = "Generate Content Plan",
                icon = Icons.Default.Lightbulb,
                onClick = {
                    if (topic.isBlank()) return@HealthogramPrimaryButton
                    coroutineScope.launch {
                        isGenerating = true
                        errorMessage = null
                        try {
                            ideas = AIStudioCustomActions.generateContentIdeas(topic = topic)
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to generate ideas"
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                state = if (isGenerating) ButtonState.LOADING else ButtonState.NORMAL,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (errorMessage != null) {
            item {
                AIErrorCard(message = errorMessage!!, onRetry = { errorMessage = null })
            }
        }

        items(ideas) { idea ->
            HealthogramBasicCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = idea.title,
                            style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.primary
                        )
                        Surface(
                            shape = HealthogramTheme.shapes.pill,
                            color = HealthogramTheme.colors.primaryContainer
                        ) {
                            Text(
                                text = idea.contentType,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                                color = HealthogramTheme.colors.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("💡 Hook: \"${idea.hook}\"", style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(idea.concept, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("🎬 Visual Concept: ${idea.suggestedVisual}", style = HealthogramTheme.typography.caption)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("📣 Call to Action: ${idea.suggestedCta}", style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Medium), color = HealthogramTheme.colors.secondary)
                }
            }
        }
    }
}

@Composable
fun AIStudioImageEditorPage(
    onBack: () -> Unit
) {
    var hasUploaded by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var processedResult by remember { mutableStateOf<AIImageToolResult?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("ai_studio_image_editor_page"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AIStudioHeader(
                title = "AI Image Editor",
                subtitle = "Enhance, retouch & studio backdrop replacement",
                onBack = onBack
            )
        }

        item {
            AIUploadCard(
                label = if (hasUploaded) "Photo Selected: clinic_facility_01.jpg" else "Tap to upload image for neural enhancement",
                onUploadClick = { hasUploaded = true }
            )
        }

        if (hasUploaded) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                processedResult = AIStudioCustomActions.enhanceAIImage("demo_clinic_image")
                                isProcessing = false
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Enhance HDR")
                    }
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                processedResult = AIStudioCustomActions.removeAIBackground("demo_clinic_image")
                                isProcessing = false
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Remove BG")
                    }
                }
            }
        }

        if (isProcessing) {
            item {
                AILoadingState(label = "Applying neural imaging filters...")
            }
        }

        if (processedResult != null) {
            item {
                AIImageBeforeAfter(
                    enhancementSummary = processedResult!!.enhancementSummary
                )
            }
            item {
                AIResultActionBar(
                    onCopy = {},
                    onUseInPost = {},
                    onRegenerate = {}
                )
            }
        }
    }
}

@Composable
fun AIStudioVideoEditorPage(
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var topic by remember { mutableStateOf("") }
    var videoResult by remember { mutableStateOf<AIVideoAssistanceResult?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("ai_studio_video_editor_page"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AIStudioHeader(
                title = "Video & Reel Assistant",
                subtitle = "Hook writing, 60s structure & scene planning",
                onBack = onBack
            )
        }

        item {
            AIPromptInput(
                value = topic,
                onValueChange = { topic = it },
                placeholder = "Video topic (e.g. Quick Stretches for Neck Pain)..."
            )
        }

        item {
            HealthogramPrimaryButton(
                text = "Structure Reel",
                icon = Icons.Default.VideoCameraFront,
                onClick = {
                    if (topic.isBlank()) return@HealthogramPrimaryButton
                    coroutineScope.launch {
                        isGenerating = true
                        videoResult = AIStudioCustomActions.startAIVideoJob(topic = topic)
                        isGenerating = false
                    }
                },
                state = if (isGenerating) ButtonState.LOADING else ButtonState.NORMAL,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (videoResult != null) {
            item {
                HealthogramBasicCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = videoResult!!.reelTitle,
                            style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(videoResult!!.reelDescription, style = HealthogramTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("🎯 Viral Hooks:", style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        videoResult!!.reelHooks.forEach { hook ->
                            Text("• $hook", style = HealthogramTheme.typography.caption)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("🎬 Scene-by-Scene Pacing:", style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        videoResult!!.sceneSuggestions.forEach { scene ->
                            Text(scene, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AIStudioHistoryPage(
    onBack: () -> Unit
) {
    val repository = remember { AIStudioRepository.getInstance() }
    val history by repository.history.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("ai_studio_history_page"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AIStudioHeader(
                title = "AI Generation History",
                subtitle = "Past jobs, captions, banners & assets",
                onBack = onBack
            )
        }

        if (history.isEmpty()) {
            item {
                AIEmptyState(
                    title = "No History Found",
                    description = "Your saved AI creations will appear here."
                )
            }
        } else {
            items(history) { item ->
                AIHistoryCard(item = item, onClick = {})
            }
        }
    }
}

@Composable
fun AIStudioUsagePage(
    onBack: () -> Unit
) {
    val repository = remember { AIStudioRepository.getInstance() }
    val usage by repository.currentUsage.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("ai_studio_usage_page"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AIStudioHeader(
                title = "AI Usage & Limits",
                subtitle = "Authoritative quotas and tier allocation",
                onBack = onBack
            )
        }

        item {
            AIUsageCard(usageSummary = usage, onUpgradeClick = {})
        }

        item {
            HealthogramBasicCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Monthly Allocation Breakdown", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Text Generations", style = HealthogramTheme.typography.bodySmall)
                        Text("${usage.monthlyTextJobs} jobs", style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Image Processing", style = HealthogramTheme.typography.bodySmall)
                        Text("${usage.monthlyImageJobs} jobs", style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Video Assistance", style = HealthogramTheme.typography.bodySmall)
                        Text("${usage.monthlyVideoJobs} jobs", style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Plan Status", style = HealthogramTheme.typography.bodySmall)
                        Text(usage.currentPlan.replace("_", " ").uppercase(), style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                    }
                }
            }
        }

        item {
            AIUpgradeCard(onUpgradeClick = {})
        }
    }
}

@Composable
fun AIStudioSettingsPage(
    onBack: () -> Unit
) {
    val repository = remember { AIStudioRepository.getInstance() }
    val settings by repository.settings.collectAsState()
    var aiEnabled by remember { mutableStateOf(settings.aiEnabled) }
    var saveHistory by remember { mutableStateOf(settings.saveHistory) }
    var allowPersonalization by remember { mutableStateOf(settings.allowPersonalization) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("ai_studio_settings_page"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AIStudioHeader(
                title = "AI Studio Settings",
                subtitle = "Preferences, privacy & guardrail controls",
                onBack = onBack
            )
        }

        item {
            HealthogramBasicCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable Generative AI", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Access AI copywriting and image tools", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                        }
                        Switch(checked = aiEnabled, onCheckedChange = { aiEnabled = it })
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Save Generation History", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Retain outputs for 30 days in your vault", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                        }
                        Switch(checked = saveHistory, onCheckedChange = { saveHistory = it })
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Brand Tone Personalization", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Adapt tone to your previous post styles", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                        }
                        Switch(checked = allowPersonalization, onCheckedChange = { allowPersonalization = it })
                    }
                }
            }
        }

        item {
            AIConsentCard()
        }
    }
}
