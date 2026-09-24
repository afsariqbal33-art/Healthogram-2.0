package com.example.healthogram.ui.messages

import androidx.compose.foundation.background
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
import com.example.healthogram.communication.*
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.BadgeSize
import com.example.healthogram.designsystem.components.HealthogramVerifiedBadge

/**
 * HEALTHOGRAM STEP 11: CONVERSATION INFO & MEDIA GALLERY PAGE
 *
 * Provides participant details, media tabs (Images, Videos, Audio, Documents, Shared Products, Shared Posts),
 * and security actions (Mute, Pin, Archive, Block, Report).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationInfoPage(
    conversationId: String,
    currentUid: String,
    participantUid: String,
    participantName: String,
    participantRole: String = "Doctor",
    isVerified: Boolean = true,
    repository: CommunicationRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by repository.messages.collectAsState()
    val convMessages = remember(messages, conversationId) {
        messages.filter { it.conversationId == conversationId }
    }

    var selectedMediaTab by remember { mutableIntStateOf(0) }
    val mediaTabs = listOf("Photos", "Videos", "Audio", "Docs", "Products")

    var isMuted by remember { mutableStateOf(false) }
    var isPinned by remember { mutableStateOf(false) }
    var isArchived by remember { mutableStateOf(false) }

    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }
    var reportDesc by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("conversation_info_page")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(HealthogramTheme.colors.background)
        ) {
            // Profile Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(HealthogramTheme.colors.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = participantName.take(1).uppercase(),
                            style = HealthogramTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = participantName,
                            style = HealthogramTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        if (isVerified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            HealthogramVerifiedBadge(status = VerificationStatus.APPROVED, size = BadgeSize.MEDIUM)
                        }
                    }

                    Text(
                        text = participantRole,
                        style = HealthogramTheme.typography.bodyMedium,
                        color = HealthogramTheme.colors.primary
                    )
                }
            }

            // Media Gallery Tabs
            item {
                Text(
                    text = "SHARED MEDIA & FILES",
                    style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                PrimaryTabRow(
                    selectedTabIndex = selectedMediaTab,
                    containerColor = HealthogramTheme.colors.surface
                ) {
                    mediaTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedMediaTab == index,
                            onClick = { selectedMediaTab = index },
                            text = { Text(title, style = HealthogramTheme.typography.bodySmall) }
                        )
                    }
                }
            }

            // Shared Items List
            val itemsForTab = when (selectedMediaTab) {
                0 -> convMessages.filter { it.messageType == MessageType.IMAGE }
                1 -> convMessages.filter { it.messageType == MessageType.VIDEO }
                2 -> convMessages.filter { it.messageType == MessageType.AUDIO || it.messageType == MessageType.VOICE }
                3 -> convMessages.filter { it.messageType == MessageType.DOCUMENT }
                else -> convMessages.filter { it.messageType == MessageType.SHARED_PRODUCT }
            }

            if (itemsForTab.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No shared ${mediaTabs[selectedMediaTab].lowercase()} yet", style = HealthogramTheme.typography.bodyMedium, color = HealthogramTheme.colors.textMuted)
                    }
                }
            } else {
                items(itemsForTab) { msg ->
                    ListItem(
                        headlineContent = { Text(msg.fileName ?: msg.text) },
                        supportingContent = { Text(CommunicationCustomFunctions.getMessageTypeLabel(msg.messageType)) },
                        leadingContent = {
                            Icon(
                                imageVector = when (msg.messageType) {
                                    MessageType.IMAGE -> Icons.Default.Image
                                    MessageType.VIDEO -> Icons.Default.Videocam
                                    MessageType.DOCUMENT -> Icons.Default.Description
                                    MessageType.SHARED_PRODUCT -> Icons.Default.ShoppingBag
                                    else -> Icons.Default.Audiotrack
                                },
                                contentDescription = null,
                                tint = HealthogramTheme.colors.primary
                            )
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Actions & Security Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "ACTIONS & PRIVACY",
                    style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = HealthogramTheme.colors.surface)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        // Mute
                        SettingSwitchRow(
                            title = "Mute Notifications",
                            subtitle = "Silence message alerts for this conversation",
                            checked = isMuted,
                            onCheckedChange = {
                                isMuted = it
                                repository.setConversationMuted(conversationId, currentUid, it)
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Pin
                        SettingSwitchRow(
                            title = "Pin to Top",
                            subtitle = "Keep this chat pinned at the top of your inbox",
                            checked = isPinned,
                            onCheckedChange = {
                                isPinned = it
                                repository.setConversationPinned(conversationId, currentUid, it)
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Archive
                        SettingSwitchRow(
                            title = "Archive Conversation",
                            subtitle = "Move conversation out of the primary inbox",
                            checked = isArchived,
                            onCheckedChange = {
                                isArchived = it
                                repository.setConversationArchived(conversationId, currentUid, it)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Block and Report
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = HealthogramTheme.colors.surface)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        ListItem(
                            headlineContent = { Text("Block User", color = HealthogramTheme.colors.error) },
                            leadingContent = { Icon(Icons.Default.Block, contentDescription = null, tint = HealthogramTheme.colors.error) },
                            modifier = Modifier.clickable {
                                repository.blockUser(currentUid, participantUid, "Blocked from conversation info")
                            }
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("Report Conversation", color = HealthogramTheme.colors.error) },
                            leadingContent = { Icon(Icons.Default.Report, contentDescription = null, tint = HealthogramTheme.colors.error) },
                            modifier = Modifier.clickable { showReportDialog = true }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Report Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Conversation") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select reason and provide details:", style = HealthogramTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        label = { Text("Reason (Spam, Harassment, Medical Misinformation)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reportDesc,
                        onValueChange = { reportDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.reportCommunication(
                            reporterUid = currentUid,
                            reportedUid = participantUid,
                            conversationId = conversationId,
                            reason = reportReason.ifBlank { "Policy violation" },
                            description = reportDesc
                        )
                        showReportDialog = false
                    }
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
