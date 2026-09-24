package com.example.healthogram.ui.calls

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.*
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
import com.example.healthogram.communication.*
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.BadgeSize
import com.example.healthogram.designsystem.components.HealthogramVerifiedBadge
import com.example.healthogram.translation.TranslationCustomActions
import com.example.healthogram.translation.TranslationCustomFunctions
import com.example.healthogram.ui.translation.CallTranslationControlBar
import com.example.healthogram.ui.translation.LiveCaptionOverlay
import com.example.healthogram.ui.translation.TranslationConsentDialog
import kotlinx.coroutines.delay

/**
 * HEALTHOGRAM STEP 11: CALL PAGES & REUSABLE CALL COMPONENTS
 * 
 * Includes:
 * 1. IncomingCallPage
 * 2. OutgoingCallPage
 * 3. ActiveAudioCallPage
 * 4. ActiveVideoCallPage
 * 5. CallHistoryPage
 * 6. Reusable Call Controls & Indicators
 */

@Composable
fun CallAvatar(
    name: String,
    size: Int = 100,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF2563EB), Color(0xFF0D9488))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1).uppercase(),
            style = HealthogramTheme.typography.displayMedium.copy(
                fontSize = (size * 0.4).sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )
    }
}

@Composable
fun CallTimer(
    startTimeMs: Long,
    modifier: Modifier = Modifier
) {
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(startTimeMs) {
        while (true) {
            val seconds = ((System.currentTimeMillis() - startTimeMs) / 1000).toInt()
            elapsedSeconds = if (seconds > 0) seconds else 0
            delay(1000)
        }
    }

    Text(
        text = CommunicationCustomFunctions.getCallDurationLabel(elapsedSeconds),
        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = Color.White.copy(alpha = 0.9f),
        modifier = modifier.testTag("call_timer")
    )
}

@Composable
fun ConnectionQualityIndicator(
    quality: RTCConnectionQuality,
    modifier: Modifier = Modifier
) {
    val (color, label) = when (quality) {
        RTCConnectionQuality.EXCELLENT -> Color(0xFF10B981) to "HD Quality"
        RTCConnectionQuality.GOOD -> Color(0xFF3B82F6) to "Good"
        RTCConnectionQuality.POOR -> Color(0xFFF59E0B) to "Weak Signal"
        RTCConnectionQuality.RECONNECTING -> Color(0xFFEF4444) to "Reconnecting..."
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
            color = Color.White
        )
    }
}

/**
 * 1. INCOMING CALL PAGE
 */
@Composable
fun IncomingCallPage(
    callerName: String,
    callerTitle: String = "Consultant",
    isVerified: Boolean = true,
    callType: CallType = CallType.AUDIO,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))
                )
            )
            .padding(24.dp)
            .testTag("incoming_call_page"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (callType == CallType.AUDIO) "Incoming Audio Call" else "Incoming Video Call",
                style = HealthogramTheme.typography.titleSmall,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            CallAvatar(name = callerName, size = 120)

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = callerName,
                    style = HealthogramTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                if (isVerified) {
                    Spacer(modifier = Modifier.width(6.dp))
                    HealthogramVerifiedBadge(status = VerificationStatus.APPROVED, size = BadgeSize.MEDIUM)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = callerTitle,
                style = HealthogramTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Text(
                text = "Ringing • End-to-End Encrypted",
                style = HealthogramTheme.typography.caption,
                color = Color(0xFF10B981)
            )

            Spacer(modifier = Modifier.height(80.dp))

            // Accept / Decline Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decline (Red)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onDecline,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .testTag("decline_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Decline Call",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Decline", style = HealthogramTheme.typography.caption, color = Color.White.copy(alpha = 0.8f))
                }

                // Accept (Green)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onAccept,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                            .testTag("accept_call_button")
                    ) {
                        Icon(
                            imageVector = if (callType == CallType.AUDIO) Icons.Default.Call else Icons.Default.Videocam,
                            contentDescription = "Accept Call",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Accept", style = HealthogramTheme.typography.caption, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}

/**
 * 2. OUTGOING CALL PAGE
 */
@Composable
fun OutgoingCallPage(
    receiverName: String,
    receiverTitle: String = "Consultant",
    isVerified: Boolean = true,
    callType: CallType = CallType.AUDIO,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp)
            .testTag("outgoing_call_page"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (callType == CallType.AUDIO) "Calling Audio..." else "Calling Video...",
                style = HealthogramTheme.typography.titleSmall,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            CallAvatar(name = receiverName, size = 120)

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = receiverName,
                    style = HealthogramTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                if (isVerified) {
                    Spacer(modifier = Modifier.width(6.dp))
                    HealthogramVerifiedBadge(status = VerificationStatus.APPROVED, size = BadgeSize.MEDIUM)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = receiverTitle,
                style = HealthogramTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(80.dp))

            // Cancel Button
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
                    .testTag("cancel_call_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "Cancel Call",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Cancel", style = HealthogramTheme.typography.caption, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

/**
 * 3. ACTIVE AUDIO CALL PAGE
 */
@Composable
fun ActiveAudioCallPage(
    participantName: String,
    startTimeMs: Long = System.currentTimeMillis(),
    webRTCService: WebRTCCallService,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mediaState by webRTCService.mediaState.collectAsState()
    var isTranslationActive by remember { mutableStateOf(false) }
    var showConsentDialog by remember { mutableStateOf(false) }
    var hasConsented by remember { mutableStateOf(false) }
    var targetLanguage by remember { mutableStateOf("ar") }
    var liveCaptions by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    LaunchedEffect(isTranslationActive) {
        if (isTranslationActive) {
            delay(1500)
            liveCaptions = listOf(
                "Hello, how are you feeling today?" to "مرحباً، كيف تشعر اليوم؟"
            )
            delay(3500)
            liveCaptions = liveCaptions + (
                "I am monitoring your blood pressure closely." to "أنا أراقب ضغط دمك بعناية."
            )
        } else {
            liveCaptions = emptyList()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp)
            .testTag("active_audio_call_page"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            ConnectionQualityIndicator(quality = mediaState.connectionQuality)

            Spacer(modifier = Modifier.height(24.dp))

            CallAvatar(name = participantName, size = 100)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = participantName,
                style = HealthogramTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            CallTimer(startTimeMs = startTimeMs)

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "End-to-End Encrypted HD Voice",
                style = HealthogramTheme.typography.caption,
                color = Color(0xFF10B981)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 12: Call Translation Control
            CallTranslationControlBar(
                isTranslationActive = isTranslationActive,
                targetLanguage = targetLanguage,
                onToggleTranslation = {
                    if (!isTranslationActive) {
                        if (hasConsented) isTranslationActive = true
                        else showConsentDialog = true
                    } else {
                        isTranslationActive = false
                    }
                },
                onOpenLanguagePicker = {
                    targetLanguage = if (targetLanguage == "ar") "es" else if (targetLanguage == "es") "hi" else "ar"
                }
            )

            // Step 12: Live Caption Overlay
            if (isTranslationActive && liveCaptions.isNotEmpty()) {
                LiveCaptionOverlay(
                    captions = liveCaptions,
                    onDismiss = { liveCaptions = emptyList() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Controls: Mute, End Call, Speaker
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Mute
                IconButton(
                    onClick = { webRTCService.toggleMicrophone() },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (mediaState.isMicMuted) Color.White else Color.White.copy(alpha = 0.18f))
                        .testTag("mute_mic_button")
                ) {
                    Icon(
                        imageVector = if (mediaState.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Toggle Microphone",
                        tint = if (mediaState.isMicMuted) Color.Black else Color.White
                    )
                }

                // Hang Up
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .testTag("end_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Speaker
                IconButton(
                    onClick = { webRTCService.toggleSpeaker() },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (mediaState.isSpeakerOn) Color.White else Color.White.copy(alpha = 0.18f))
                        .testTag("speaker_button")
                ) {
                    Icon(
                        imageVector = if (mediaState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Toggle Speaker",
                        tint = if (mediaState.isSpeakerOn) Color.Black else Color.White
                    )
                }
            }
        }
    }

    if (showConsentDialog) {
        TranslationConsentDialog(
            onConfirm = {
                hasConsented = true
                isTranslationActive = true
                showConsentDialog = false
            },
            onDismiss = { showConsentDialog = false }
        )
    }
}

/**
 * 4. ACTIVE VIDEO CALL PAGE
 */
@Composable
fun ActiveVideoCallPage(
    participantName: String,
    startTimeMs: Long = System.currentTimeMillis(),
    webRTCService: WebRTCCallService,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mediaState by webRTCService.mediaState.collectAsState()
    var isTranslationActive by remember { mutableStateOf(false) }
    var showConsentDialog by remember { mutableStateOf(false) }
    var hasConsented by remember { mutableStateOf(false) }
    var targetLanguage by remember { mutableStateOf("ar") }
    var liveCaptions by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    LaunchedEffect(isTranslationActive) {
        if (isTranslationActive) {
            delay(1500)
            liveCaptions = listOf(
                "Good afternoon, can you see the test results on my screen?" to "مساء الخير، هل يمكنك رؤية نتائج الفحص على شاشتي؟"
            )
            delay(3500)
            liveCaptions = liveCaptions + (
                "Yes, the heart rate values are normal." to "نعم، مؤشرات نبضات القلب طبيعية وممتازة."
            )
        } else {
            liveCaptions = emptyList()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
            .testTag("active_video_call_page")
    ) {
        // Remote Video Surface Simulated Feed
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CallAvatar(name = participantName, size = 90)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    participantName,
                    style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                CallTimer(startTimeMs = startTimeMs)
            }
        }

        // Top Status Header (Quality, timer)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConnectionQualityIndicator(quality = mediaState.connectionQuality)

            IconButton(
                onClick = { webRTCService.switchCamera() },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .testTag("switch_camera_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Switch Camera",
                    tint = Color.White
                )
            }
        }

        // Local Floating Camera PIP Preview
        if (mediaState.isCameraEnabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 16.dp)
                    .size(width = 110.dp, height = 150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .background(Color(0xFF334155)),
                contentAlignment = Alignment.Center
            ) {
                Text("Self Preview", style = HealthogramTheme.typography.caption, color = Color.White)
            }
        }

        // Live Caption Overlay (positioned above toolbar)
        if (isTranslationActive && liveCaptions.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
            ) {
                LiveCaptionOverlay(
                    captions = liveCaptions,
                    onDismiss = { liveCaptions = emptyList() }
                )
            }
        }

        // Bottom Controls Toolbar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.Black.copy(alpha = 0.75f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Mute
                IconButton(
                    onClick = { webRTCService.toggleMicrophone() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (mediaState.isMicMuted) Color.White else Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = if (mediaState.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = if (mediaState.isMicMuted) Color.Black else Color.White
                    )
                }

                // Camera Toggle
                IconButton(
                    onClick = { webRTCService.toggleCamera() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (!mediaState.isCameraEnabled) Color.White else Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = if (!mediaState.isCameraEnabled) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        contentDescription = "Camera Toggle",
                        tint = if (!mediaState.isCameraEnabled) Color.Black else Color.White
                    )
                }

                // Translate Button
                IconButton(
                    onClick = {
                        if (!isTranslationActive) {
                            if (hasConsented) isTranslationActive = true
                            else showConsentDialog = true
                        } else {
                            isTranslationActive = false
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isTranslationActive) Color(0xFF2563EB) else Color.White.copy(alpha = 0.2f))
                        .testTag("call_translate_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Translate Call",
                        tint = Color.White
                    )
                }

                // End Call (Hang Up)
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .testTag("end_video_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Speaker Toggle
                IconButton(
                    onClick = { webRTCService.toggleSpeaker() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (mediaState.isSpeakerOn) Color.White else Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = if (mediaState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Speaker",
                        tint = if (mediaState.isSpeakerOn) Color.Black else Color.White
                    )
                }
            }
        }
    }

    if (showConsentDialog) {
        TranslationConsentDialog(
            onConfirm = {
                hasConsented = true
                isTranslationActive = true
                showConsentDialog = false
            },
            onDismiss = { showConsentDialog = false }
        )
    }
}

/**
 * 5. CALL HISTORY PAGE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryPage(
    history: List<CallHistoryEntry>,
    onBack: () -> Unit,
    onCallUser: (String, CallType) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredList = remember(history, selectedFilter) {
        when (selectedFilter) {
            "Missed" -> history.filter { it.direction == CallDirection.MISSED }
            "Audio" -> history.filter { it.callType == CallType.AUDIO }
            "Video" -> history.filter { it.callType == CallType.VIDEO }
            else -> history
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Call History", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("call_history_page")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(HealthogramTheme.colors.background)
        ) {
            // Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Missed", "Audio", "Video").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }

            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = HealthogramTheme.colors.textMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No call records found", style = HealthogramTheme.typography.bodyMedium, color = HealthogramTheme.colors.textMuted)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList) { entry ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(containerColor = HealthogramTheme.colors.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (entry.direction) {
                                                    CallDirection.MISSED -> Color(0xFFFEE2E2)
                                                    CallDirection.INCOMING -> Color(0xFFDCFCE7)
                                                    CallDirection.OUTGOING -> Color(0xFFE0E7FF)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (entry.direction) {
                                                CallDirection.MISSED -> Icons.AutoMirrored.Filled.CallMissed
                                                CallDirection.INCOMING -> Icons.AutoMirrored.Filled.CallReceived
                                                CallDirection.OUTGOING -> Icons.AutoMirrored.Filled.CallMade
                                            },
                                            contentDescription = null,
                                            tint = when (entry.direction) {
                                                CallDirection.MISSED -> Color(0xFFEF4444)
                                                CallDirection.INCOMING -> Color(0xFF10B981)
                                                CallDirection.OUTGOING -> Color(0xFF4F46E5)
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = entry.otherPartyName,
                                            style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = if (entry.callType == CallType.AUDIO) "Audio" else "Video",
                                                style = HealthogramTheme.typography.caption,
                                                color = HealthogramTheme.colors.primary
                                            )
                                            Text("•", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                            Text(
                                                text = if (entry.durationSeconds > 0) CommunicationCustomFunctions.getCallDurationLabel(entry.durationSeconds) else "Unanswered",
                                                style = HealthogramTheme.typography.caption,
                                                color = HealthogramTheme.colors.textMuted
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { onCallUser(entry.otherPartyUid, entry.callType) }
                                ) {
                                    Icon(
                                        imageVector = if (entry.callType == CallType.AUDIO) Icons.Default.Call else Icons.Default.Videocam,
                                        contentDescription = "Call Back",
                                        tint = HealthogramTheme.colors.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
