package com.example.healthogram.social.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.sp
import com.example.healthogram.core.AccountType
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.social.*

enum class CreationMode(val label: String) {
    POST("Post"),
    REEL("Reel"),
    STORY("Story"),
    LIVE("Live")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateContentFlow(
    onDismiss: () -> Unit,
    onPublished: () -> Unit,
    initialMode: CreationMode = CreationMode.POST,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(initialMode) }
    val engine = remember { SocialFeedEngine.getInstance() }

    // Common Post / Reel / Story fields
    var caption by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ContentCategory.LIFESTYLE) }
    var isMedicalContent by remember { mutableStateOf(false) }
    var isCommentsDisabled by remember { mutableStateOf(false) }
    var visibility by remember { mutableStateOf(PostVisibility.PUBLIC) }
    var selectedMediaPreset by remember { mutableIntStateOf(0) }

    // Reel specific
    var selectedAudioTrack by remember { mutableStateOf("Original Audio • Healthogram") }
    var reelDuration by remember { mutableIntStateOf(30) }

    // Story specific
    var includePollSticker by remember { mutableStateOf(false) }
    var pollQuestion by remember { mutableStateOf("What do you think?") }

    // Live specific
    var liveTitle by remember { mutableStateOf("") }

    val sampleMediaPresets = remember {
        listOf(
            "Fitness & Movement" to "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800",
            "Nutritious Cuisine" to "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=800",
            "Tech & Engineering" to "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800",
            "Nature & Travel" to "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
            "Clinical Wellness" to "https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=800"
        )
    }

    val sampleAudioTracks = remember {
        listOf("Original Audio • Healthogram", "Indie Chill Beats", "Electric Neon Pulse", "Serene Ambient Flow")
    }

    val quickHashtags = remember {
        listOf("#Fitness", "#Lifestyle", "#Innovation", "#Tech", "#Cardiology", "#Travel", "#DailyMotivation")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New ${selectedMode.label}",
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("create_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            when (selectedMode) {
                                CreationMode.POST -> {
                                    val post = SocialPost(
                                        authorUid = "current_user",
                                        authorName = "You",
                                        authorUsername = "current_user",
                                        authorAccountType = AccountType.INDIVIDUAL,
                                        category = selectedCategory,
                                        caption = caption.ifBlank { "Sharing moments on Healthogram ✨" },
                                        mediaUrls = listOf(sampleMediaPresets[selectedMediaPreset].second),
                                        hashtags = quickHashtags.filter { caption.contains(it) },
                                        isMedicalContent = isMedicalContent,
                                        medicalDisclaimer = if (isMedicalContent) "Information provided for educational purposes only. Consult a licensed physician for clinical guidance." else null,
                                        isCommentsDisabled = isCommentsDisabled,
                                        visibility = visibility
                                    )
                                    engine.publishPost(post)
                                }
                                CreationMode.REEL -> {
                                    val reel = SocialReel(
                                        authorUid = "current_user",
                                        authorName = "You",
                                        authorUsername = "current_user",
                                        caption = caption.ifBlank { "New short-form reel! 🎬" },
                                        videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-hands-playing-a-synthesizer-41618-large.mp4",
                                        audioTrackTitle = selectedAudioTrack,
                                        category = selectedCategory,
                                        durationSeconds = reelDuration
                                    )
                                    engine.publishReel(reel)
                                }
                                CreationMode.STORY -> {
                                    val stickers = if (includePollSticker) {
                                        listOf(
                                            StorySticker(
                                                type = StoryStickerType.POLL,
                                                text = pollQuestion,
                                                options = listOf("Option A", "Option B")
                                            )
                                        )
                                    } else emptyList()

                                    val story = SocialStory(
                                        authorUid = "current_user",
                                        authorName = "You",
                                        authorUsername = "current_user",
                                        mediaUrl = sampleMediaPresets[selectedMediaPreset].second,
                                        caption = caption,
                                        stickers = stickers
                                    )
                                    engine.publishStory(story)
                                }
                                CreationMode.LIVE -> {
                                    val live = LiveStreamSession(
                                        hostUid = "current_user",
                                        hostName = "You",
                                        hostUsername = "current_user",
                                        title = liveTitle.ifBlank { "Live with Healthogram Community" },
                                        category = selectedCategory
                                    )
                                    engine.startLiveStream(live)
                                }
                            }
                            onPublished()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HealthogramTheme.colors.primary),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("publish_content_btn")
                    ) {
                        Text(
                            text = if (selectedMode == CreationMode.LIVE) "Go Live" else "Publish",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HealthogramTheme.colors.surface,
                    titleContentColor = HealthogramTheme.colors.textPrimary
                )
            )
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("create_content_flow")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(HealthogramTheme.colors.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Mode Selector Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CreationMode.entries.forEach { mode ->
                    val isSelected = selectedMode == mode
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedMode = mode },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.surfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.borderLight
                        )
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.label,
                                style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else HealthogramTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }

            // Media Preview Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                color = HealthogramTheme.colors.surfaceVariant,
                border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = when (selectedMode) {
                                CreationMode.POST -> Icons.Default.Image
                                CreationMode.REEL -> Icons.Default.VideoCameraBack
                                CreationMode.STORY -> Icons.Default.CameraAlt
                                CreationMode.LIVE -> Icons.Default.LiveTv
                            },
                            contentDescription = null,
                            tint = HealthogramTheme.colors.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Selected Asset: ${sampleMediaPresets[selectedMediaPreset].first}",
                            style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Select Preset Media Row
            Text(
                text = "Choose Media Template",
                style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sampleMediaPresets.indices.toList()) { index ->
                    val isSelected = selectedMediaPreset == index
                    Surface(
                        modifier = Modifier.clickable { selectedMediaPreset = index },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) HealthogramTheme.colors.primaryContainer else HealthogramTheme.colors.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.borderLight
                        )
                    ) {
                        Text(
                            text = sampleMediaPresets[index].first,
                            style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
                            color = if (isSelected) HealthogramTheme.colors.onPrimaryContainer else HealthogramTheme.colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Caption / Title Input
            if (selectedMode == CreationMode.LIVE) {
                OutlinedTextField(
                    value = liveTitle,
                    onValueChange = { liveTitle = it },
                    label = { Text("Broadcast Title") },
                    placeholder = { Text("e.g. Q&A on Nutrition & Wellness") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_live_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Write a caption...") },
                    placeholder = { Text("What's on your mind? Add tags, mentions...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("create_caption_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Hashtag Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickHashtags) { tag ->
                        Surface(
                            modifier = Modifier.clickable {
                                if (!caption.contains(tag)) {
                                    caption = if (caption.isBlank()) tag else "$caption $tag"
                                }
                            },
                            shape = CircleShape,
                            color = HealthogramTheme.colors.surfaceVariant
                        ) {
                            Text(
                                text = tag,
                                style = HealthogramTheme.typography.caption,
                                color = HealthogramTheme.colors.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step 50: AI Studio Assistant in Creation Flow
            var showAiAssistant by remember { mutableStateOf(false) }
            var aiGeneratedText by remember { mutableStateOf<String?>(null) }
            var isAiGenerating by remember { mutableStateOf(false) }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = HealthogramTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Studio",
                                tint = HealthogramTheme.colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Studio Content Assistant",
                                style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = HealthogramTheme.colors.textPrimary
                            )
                        }
                        TextButton(onClick = { showAiAssistant = !showAiAssistant }) {
                            Text(if (showAiAssistant) "Hide" else "Open")
                        }
                    }

                    if (showAiAssistant) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Generate compelling captions and trending hashtags. No medical or personal health data is ever transmitted.",
                            style = HealthogramTheme.typography.caption,
                            color = HealthogramTheme.colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    isAiGenerating = true
                                    aiGeneratedText = when (selectedCategory) {
                                        ContentCategory.SPORTS -> "Embracing daily movement and disciplined progress! Remember that consistency beats intensity every single time. 💪 #FitnessGoals #HealthogramWellness"
                                        ContentCategory.LIFESTYLE -> "Nourishing vitality with wholesome whole-food ingredients and restorative habits. 🥗✨ #CleanLiving #Nutritious"
                                        ContentCategory.HEALTHCARE -> "Clinical preventive insights: prioritizing sleep hygiene and balanced cardiovascular health makes a lifelong impact. 🩺💡 #PreventiveCare #HealthEducation"
                                        else -> "Exploring creative ideas and celebrating progress with our vibrant community! 🌟 #Inspiration #HealthogramLife"
                                    }
                                    isAiGenerating = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Generate Caption", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    isAiGenerating = true
                                    aiGeneratedText = "#WellnessJourney #HealthyLifestyle #Healthogram #PreventiveCare #MindBodySoul #FitnessMotivation #Community"
                                    isAiGenerating = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Suggest Tags", fontSize = 12.sp)
                            }
                        }

                        if (aiGeneratedText != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = HealthogramTheme.colors.surface,
                                border = BorderStroke(1.dp, HealthogramTheme.colors.primary.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = HealthogramTheme.colors.primary.copy(alpha = 0.15f),
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Text(
                                                text = "AI GENERATED",
                                                style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                                color = HealthogramTheme.colors.primary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = "Requires your approval before publish",
                                            style = HealthogramTheme.typography.caption,
                                            color = HealthogramTheme.colors.textMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = aiGeneratedText!!,
                                        style = HealthogramTheme.typography.bodySmall,
                                        color = HealthogramTheme.colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            if (selectedMode == CreationMode.LIVE) {
                                                liveTitle = aiGeneratedText!!.take(60)
                                            } else {
                                                caption = if (caption.isBlank()) aiGeneratedText!! else "$caption\n\n${aiGeneratedText!!}"
                                            }
                                            aiGeneratedText = null
                                            showAiAssistant = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = HealthogramTheme.colors.primary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Apply to Content", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Picker
            Text(
                text = "Content Category",
                style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(ContentCategory.entries.filter { it != ContentCategory.ALL }) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        modifier = Modifier.clickable {
                            selectedCategory = cat
                            if (cat == ContentCategory.HEALTHCARE) {
                                isMedicalContent = true
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.borderLight
                        )
                    ) {
                        Text(
                            text = cat.displayName,
                            style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.White else HealthogramTheme.colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reel Specific Audio Track Selector
            if (selectedMode == CreationMode.REEL) {
                Text(
                    text = "Audio Track",
                    style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.textMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                sampleAudioTracks.forEach { track ->
                    val isSelected = selectedAudioTrack == track
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAudioTrack = track }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedAudioTrack = track }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = track,
                            style = HealthogramTheme.typography.bodySmall,
                            color = HealthogramTheme.colors.textPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Story Specific Poll Sticker Toggle
            if (selectedMode == CreationMode.STORY) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Add Interactive Poll",
                            style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.textPrimary
                        )
                        Text(
                            text = "Let viewers vote in real-time",
                            style = HealthogramTheme.typography.caption,
                            color = HealthogramTheme.colors.textMuted
                        )
                    }
                    Switch(
                        checked = includePollSticker,
                        onCheckedChange = { includePollSticker = it }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Healthcare / Medical Disclaimer Confirmation
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = HealthogramTheme.colors.surface,
                border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Healthcare / Wellness Advice",
                                style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = HealthogramTheme.colors.textPrimary
                            )
                            Text(
                                text = "Attaches automated safety educational disclaimer",
                                style = HealthogramTheme.typography.caption,
                                color = HealthogramTheme.colors.textMuted
                            )
                        }
                        Switch(
                            checked = isMedicalContent,
                            onCheckedChange = { isMedicalContent = it }
                        )
                    }

                    if (isMedicalContent) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HealthogramTheme.colors.surfaceVariant
                        ) {
                            Text(
                                text = "Disclaimer: Information provided for educational purposes only. Always consult a licensed physician for clinical advice.",
                                style = HealthogramTheme.typography.caption,
                                color = HealthogramTheme.colors.primary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Disable Comments Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Turn off commenting",
                    style = HealthogramTheme.typography.bodyMedium,
                    color = HealthogramTheme.colors.textPrimary
                )
                Switch(
                    checked = isCommentsDisabled,
                    onCheckedChange = { isCommentsDisabled = it }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
