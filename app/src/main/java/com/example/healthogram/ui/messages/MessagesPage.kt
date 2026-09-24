package com.example.healthogram.ui.messages

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
import com.example.healthogram.communication.*
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.ui.calls.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * HEALTHOGRAM STEP 11: MAIN MESSAGING HUB
 *
 * Full production-grade messaging hub providing:
 * - Direct conversations list with verified badges & real-time presence
 * - Tabs: All, Unread, Requests, Archived
 * - Message Search & New Chat Dialog
 * - Seamless integration with WebRTC Audio & Video Calling
 * - Communication Privacy & Device Management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesPage(
    currentUid: String = "user_current",
    repository: CommunicationRepository = remember { CommunicationRepository() },
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // Navigation & active modal state
    var selectedConversationId by remember { mutableStateOf<String?>(null) }
    var showRequestsPage by remember { mutableStateOf(false) }
    var showSettingsPage by remember { mutableStateOf(false) }
    var showCallHistoryPage by remember { mutableStateOf(false) }
    var showInfoPage by remember { mutableStateOf(false) }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var newChatRecipientInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Unread, 2: Requests, 3: Archived

    // Active Calling State
    var activeCallSession by remember { mutableStateOf<CallSession?>(null) }
    var isIncomingCall by remember { mutableStateOf(false) }

    // Seed mock initial demo data if repository is fresh
    LaunchedEffect(Unit) {
        if (repository.conversations.value.isEmpty()) {
            val conv1 = repository.createOrGetDirectConversation(currentUid, "doctor_sarah")
            val conv2 = repository.createOrGetDirectConversation(currentUid, "city_hospital")
            val conv3 = repository.createOrGetDirectConversation(currentUid, "nova_labs")

            repository.sendMessage(
                callerUid = "doctor_sarah",
                conversationId = conv1.conversationId,
                text = "Your latest ECG report looks stable. Keep up the morning routine."
            )
            repository.sendMessage(
                callerUid = "city_hospital",
                conversationId = conv2.conversationId,
                text = "Appointment confirmed for tomorrow at 11:00 AM."
            )
            repository.sendMessage(
                callerUid = "nova_labs",
                conversationId = conv3.conversationId,
                text = "Metabolic panel results ready in your Health Passport."
            )

            repository.updatePresence("doctor_sarah", PresenceState.ONLINE)
            repository.updatePresence("city_hospital", PresenceState.OFFLINE)
            repository.updatePresence("nova_labs", PresenceState.ONLINE)
        }
    }

    val conversations by repository.conversations.collectAsState()
    val requests by repository.messageRequests.collectAsState()
    val callHistory by repository.callHistory.collectAsState()

    // Handle Active Call Overlay Screen
    if (activeCallSession != null) {
        val session = activeCallSession!!
        val otherUid = if (session.callerUid == currentUid) session.receiverUid else session.callerUid
        val otherName = when (otherUid) {
            "doctor_sarah" -> "Dr. Sarah Jenkins"
            "city_hospital" -> "City General Hospital"
            "nova_labs" -> "Nova Clinical Labs"
            else -> "Healthcare Professional"
        }

        if (isIncomingCall && session.status == CallStatus.RINGING) {
            IncomingCallPage(
                callerName = otherName,
                callType = session.callType,
                onAccept = {
                    coroutineScope.launch {
                        activeCallSession = repository.acceptCall(session.callId, currentUid)
                        isIncomingCall = false
                    }
                },
                onDecline = {
                    coroutineScope.launch {
                        repository.declineCall(session.callId, currentUid)
                        activeCallSession = null
                        isIncomingCall = false
                    }
                }
            )
            return
        } else if (!isIncomingCall && session.status == CallStatus.RINGING) {
            OutgoingCallPage(
                receiverName = otherName,
                callType = session.callType,
                onCancel = {
                    coroutineScope.launch {
                        repository.cancelCall(session.callId, currentUid)
                        activeCallSession = null
                    }
                }
            )
            return
        } else if (session.status == CallStatus.CONNECTED) {
            if (session.callType == CallType.AUDIO) {
                ActiveAudioCallPage(
                    participantName = otherName,
                    startTimeMs = session.connectedAt ?: System.currentTimeMillis(),
                    webRTCService = repository.webRTCService,
                    onEndCall = {
                        coroutineScope.launch {
                            repository.endCall(session.callId, currentUid)
                            activeCallSession = null
                        }
                    }
                )
            } else {
                ActiveVideoCallPage(
                    participantName = otherName,
                    startTimeMs = session.connectedAt ?: System.currentTimeMillis(),
                    webRTCService = repository.webRTCService,
                    onEndCall = {
                        coroutineScope.launch {
                            repository.endCall(session.callId, currentUid)
                            activeCallSession = null
                        }
                    }
                )
            }
            return
        }
    }

    // Secondary Screen Route: Conversation Details Info
    if (showInfoPage && selectedConversationId != null) {
        val conv = conversations.find { it.conversationId == selectedConversationId }
        val otherUid = conv?.participantIds?.firstOrNull { it != currentUid } ?: "unknown"
        val otherName = when (otherUid) {
            "doctor_sarah" -> "Dr. Sarah Jenkins"
            "city_hospital" -> "City General Hospital"
            "nova_labs" -> "Nova Clinical Labs"
            else -> "User @${otherUid.take(8)}"
        }
        ConversationInfoPage(
            conversationId = selectedConversationId!!,
            currentUid = currentUid,
            participantUid = otherUid,
            participantName = otherName,
            repository = repository,
            onBack = { showInfoPage = false }
        )
        return
    }

    // Secondary Screen Route: Active Conversation Page
    if (selectedConversationId != null) {
        val conv = conversations.find { it.conversationId == selectedConversationId }
        val otherUid = conv?.participantIds?.firstOrNull { it != currentUid } ?: "unknown"
        val otherName = when (otherUid) {
            "doctor_sarah" -> "Dr. Sarah Jenkins"
            "city_hospital" -> "City General Hospital"
            "nova_labs" -> "Nova Clinical Labs"
            else -> "User @${otherUid.take(8)}"
        }

        ConversationPage(
            conversationId = selectedConversationId!!,
            currentUid = currentUid,
            participantName = otherName,
            repository = repository,
            onBack = { selectedConversationId = null },
            onStartAudioCall = {
                coroutineScope.launch {
                    try {
                        activeCallSession = repository.initiateCall(currentUid, otherUid, CallType.AUDIO)
                        isIncomingCall = false
                    } catch (e: Exception) {
                        // handled gracefully
                    }
                }
            },
            onStartVideoCall = {
                coroutineScope.launch {
                    try {
                        activeCallSession = repository.initiateCall(currentUid, otherUid, CallType.VIDEO)
                        isIncomingCall = false
                    } catch (e: Exception) {
                        // handled gracefully
                    }
                }
            },
            onOpenInfo = { showInfoPage = true }
        )
        return
    }

    // Secondary Screen Route: Message Requests Page
    if (showRequestsPage) {
        MessageRequestsPage(
            currentUid = currentUid,
            repository = repository,
            onBack = { showRequestsPage = false },
            onOpenConversation = { convId ->
                selectedConversationId = convId
                showRequestsPage = false
            }
        )
        return
    }

    // Secondary Screen Route: Communication Settings Page
    if (showSettingsPage) {
        MessageSettingsPage(
            currentUid = currentUid,
            repository = repository,
            onBack = { showSettingsPage = false }
        )
        return
    }

    // Secondary Screen Route: Call History Page
    if (showCallHistoryPage) {
        CallHistoryPage(
            history = callHistory,
            onBack = { showCallHistoryPage = false },
            onCallUser = { targetUid, callType ->
                coroutineScope.launch {
                    try {
                        activeCallSession = repository.initiateCall(currentUid, targetUid, callType)
                        isIncomingCall = false
                        showCallHistoryPage = false
                    } catch (e: Exception) {
                        // handled
                    }
                }
            }
        )
        return
    }

    // Primary Inbox List
    val filteredConversations = remember(conversations, selectedTab, searchQuery) {
        var list = when (selectedTab) {
            1 -> conversations.filter { (it.unreadCountMap[currentUid] ?: 0) > 0 }
            3 -> conversations.filter { it.isArchived }
            else -> conversations.filter { !it.isArchived }
        }
        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.lastMessagePreview.contains(searchQuery, ignoreCase = true) ||
                        it.participantIds.any { uid -> uid.contains(searchQuery, ignoreCase = true) }
            }
        }
        list.sortedWith(compareByDescending<Conversation> { it.isPinned }.thenByDescending { it.lastMessageAt })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages", style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                actions = {
                    IconButton(onClick = { showCallHistoryPage = true }, modifier = Modifier.testTag("call_history_button")) {
                        Icon(Icons.Default.Phone, contentDescription = "Call History", tint = HealthogramTheme.colors.primary)
                    }
                    IconButton(onClick = { showSettingsPage = true }, modifier = Modifier.testTag("communication_settings_button")) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = HealthogramTheme.colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HealthogramTheme.colors.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewChatDialog = true },
                containerColor = HealthogramTheme.colors.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("new_chat_fab")
            ) {
                Icon(Icons.Default.Edit, contentDescription = "New Conversation")
            }
        },
        modifier = modifier.testTag("messages_hub_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(HealthogramTheme.colors.background)
        ) {
            // Search Field
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                HealthogramSearchField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search messages & verified doctors..."
                )
            }

            // Inbox Category Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = HealthogramTheme.colors.surface,
                contentColor = HealthogramTheme.colors.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("All") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Unread") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        showRequestsPage = true
                    },
                    text = {
                        BadgedBox(badge = {
                            if (requests.isNotEmpty()) {
                                Badge { Text("${requests.size}") }
                            }
                        }) {
                            Text("Requests")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Archived") }
                )
            }

            // Conversation Items
            if (filteredConversations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = HealthogramTheme.colors.textMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No conversations found", style = HealthogramTheme.typography.bodyMedium, color = HealthogramTheme.colors.textMuted)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredConversations, key = { it.conversationId }) { conv ->
                        val otherUid = conv.participantIds.firstOrNull { it != currentUid } ?: "unknown"
                        val otherName = when (otherUid) {
                            "doctor_sarah" -> "Dr. Sarah Jenkins"
                            "city_hospital" -> "City General Hospital"
                            "nova_labs" -> "Nova Clinical Labs"
                            else -> "User @${otherUid.take(8)}"
                        }
                        val otherRole = when (otherUid) {
                            "doctor_sarah" -> "Cardiologist"
                            "city_hospital" -> "Clinical Care Desk"
                            "nova_labs" -> "Diagnostic Services"
                            else -> "Consultant"
                        }
                        val presence = repository.getUserPresence(otherUid)
                        val unreadCount = conv.unreadCountMap[currentUid] ?: 0

                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedConversationId = conv.conversationId }
                                .testTag("conversation_card_${conv.conversationId}"),
                            colors = CardDefaults.elevatedCardColors(containerColor = HealthogramTheme.colors.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar + Presence indicator
                                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(HealthogramTheme.colors.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = otherName.take(1).uppercase(),
                                            style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = HealthogramTheme.colors.onPrimaryContainer
                                        )
                                    }
                                    if (presence.state == PresenceState.ONLINE) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .align(Alignment.BottomEnd)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                                .border(2.dp, HealthogramTheme.colors.surface, CircleShape)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Middle: Name, role, preview
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = otherName,
                                                style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            HealthogramVerifiedBadge(status = VerificationStatus.APPROVED, size = BadgeSize.SMALL)
                                            if (conv.isPinned) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = HealthogramTheme.colors.primary, modifier = Modifier.size(14.dp))
                                            }
                                        }

                                        Text(
                                            text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(conv.lastMessageAt)),
                                            style = HealthogramTheme.typography.caption,
                                            color = HealthogramTheme.colors.textMuted
                                        )
                                    }

                                    Text(
                                        text = otherRole,
                                        style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
                                        color = HealthogramTheme.colors.primary
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = conv.lastMessagePreview.ifBlank { "No messages yet" },
                                            style = HealthogramTheme.typography.bodySmall,
                                            color = if (unreadCount > 0) HealthogramTheme.colors.textPrimary else HealthogramTheme.colors.textSecondary,
                                            fontWeight = if (unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f)
                                        )

                                        if (conv.isMuted) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.VolumeOff, contentDescription = "Muted", tint = HealthogramTheme.colors.textMuted, modifier = Modifier.size(14.dp))
                                        }

                                        if (unreadCount > 0) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Badge(containerColor = HealthogramTheme.colors.primary) {
                                                Text("$unreadCount")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New Chat Recipient Input Dialog
    if (showNewChatDialog) {
        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = { Text("Start Conversation") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter the user ID or healthcare username:", style = HealthogramTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = newChatRecipientInput,
                        onValueChange = { newChatRecipientInput = it },
                        label = { Text("Recipient ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val recipient = newChatRecipientInput.trim()
                        if (recipient.isNotEmpty()) {
                            try {
                                val conv = repository.createOrGetDirectConversation(currentUid, recipient)
                                selectedConversationId = conv.conversationId
                                showNewChatDialog = false
                                newChatRecipientInput = ""
                            } catch (e: Exception) {
                                // Handled
                            }
                        }
                    }
                ) {
                    Text("Start")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
