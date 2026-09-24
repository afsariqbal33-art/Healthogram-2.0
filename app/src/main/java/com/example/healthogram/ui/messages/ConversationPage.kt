package com.example.healthogram.ui.messages

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.healthogram.designsystem.components.BadgeSize
import com.example.healthogram.designsystem.components.HealthogramVerifiedBadge
import com.example.healthogram.translation.*
import com.example.healthogram.ui.translation.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * HEALTHOGRAM STEP 11: ACTIVE CONVERSATION SCREEN
 *
 * Full-featured 1-to-1 conversation view with:
 * - Direct text, media, voice messages
 * - Message replies & inline quoted banner
 * - Reactions (❤️, 👍, 😂, 😮, 😢, 😡)
 * - Message editing within 15m window
 * - Message deletion ("Delete for me" vs "Delete for everyone")
 * - Audio & Video calling triggers
 * - Typing indicators & presence
 * - Strict Health Passport isolation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationPage(
    conversationId: String,
    currentUid: String,
    participantName: String,
    participantRole: String = "Healthcare Professional",
    isVerified: Boolean = true,
    repository: CommunicationRepository,
    onBack: () -> Unit,
    onStartAudioCall: () -> Unit,
    onStartVideoCall: () -> Unit,
    onOpenInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<Message?>(null) }
    var selectedMessageForMenu by remember { mutableStateOf<Message?>(null) }
    var messageToEdit by remember { mutableStateOf<Message?>(null) }
    var editTextValue by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<Message?>(null) }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var voiceRecordSeconds by remember { mutableIntStateOf(0) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Translation Step 12 state hooks
    val clipboardManager = LocalClipboardManager.current
    var activeTranslations by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var translatingMessageId by remember { mutableStateOf<String?>(null) }
    var voiceTranscripts by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var userTranslationSettings by remember { mutableStateOf(TranslationCustomActions.repository.getUserSettings(currentUid)) }
    var showLanguageSheet by remember { mutableStateOf(false) }

    val allMessages by repository.messages.collectAsState()
    val conversationMessages = remember(allMessages, conversationId) {
        allMessages.filter { it.conversationId == conversationId }.sortedBy { it.createdAt }
    }

    val isPeerTyping = repository.isUserTyping(conversationId, "peer_user")
    val presence = repository.getUserPresence(currentUid)

    // Automatically mark read on entry
    LaunchedEffect(conversationId) {
        repository.markConversationRead(conversationId, currentUid)
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(conversationMessages.size) {
        if (conversationMessages.isNotEmpty()) {
            listState.animateScrollToItem(conversationMessages.size - 1)
        }
    }

    // Voice record timer
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            voiceRecordSeconds = 0
            while (isRecordingVoice) {
                kotlinx.coroutines.delay(1000)
                voiceRecordSeconds++
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onOpenInfo() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(HealthogramTheme.colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = participantName.take(1).uppercase(),
                                style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = HealthogramTheme.colors.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = participantName,
                                    style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = HealthogramTheme.colors.textPrimary
                                )
                                if (isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    HealthogramVerifiedBadge(status = VerificationStatus.APPROVED, size = BadgeSize.SMALL)
                                }
                            }
                            Text(
                                text = if (isPeerTyping) "typing..." else CommunicationCustomFunctions.getPresenceLabel(presence),
                                style = HealthogramTheme.typography.caption,
                                color = if (isPeerTyping) HealthogramTheme.colors.primary else HealthogramTheme.colors.textMuted
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showLanguageSheet = true }, modifier = Modifier.testTag("conversation_translate_icon")) {
                        Icon(
                            Icons.Default.Translate,
                            contentDescription = "Translate Settings",
                            tint = if (userTranslationSettings.textTranslationEnabled) HealthogramTheme.colors.primary else HealthogramTheme.colors.textMuted
                        )
                    }
                    IconButton(onClick = onStartAudioCall, modifier = Modifier.testTag("audio_call_icon")) {
                        Icon(Icons.Default.Call, contentDescription = "Audio Call", tint = HealthogramTheme.colors.primary)
                    }
                    IconButton(onClick = onStartVideoCall, modifier = Modifier.testTag("video_call_icon")) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = HealthogramTheme.colors.primary)
                    }
                    IconButton(onClick = onOpenInfo) {
                        Icon(Icons.Default.Info, contentDescription = "Conversation Info", tint = HealthogramTheme.colors.textMuted)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HealthogramTheme.colors.surface
                )
            )
        },
        modifier = modifier.testTag("conversation_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(HealthogramTheme.colors.background)
        ) {
            // Security Notice Banner (Health Passport Isolation Reminder)
            Surface(
                color = Color(0xFFF8FAFC),
                border = BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Encrypted Chat. Health Passport records require separate patient consent grant.",
                        style = HealthogramTheme.typography.caption,
                        color = HealthogramTheme.colors.textSecondary
                    )
                }
            }

            // Error banner if any
            if (errorMessage != null) {
                Surface(
                    color = HealthogramTheme.colors.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            style = HealthogramTheme.typography.bodySmall,
                            color = HealthogramTheme.colors.error,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { errorMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = HealthogramTheme.colors.error)
                        }
                    }
                }
            }

            // Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(conversationMessages, key = { it.messageId }) { msg ->
                    val isMine = msg.senderUid == currentUid
                    val isTranslated = activeTranslations.containsKey(msg.messageId)
                    MessageBubble(
                        message = msg,
                        isMine = isMine,
                        translatedText = activeTranslations[msg.messageId],
                        voiceTranscript = voiceTranscripts[msg.messageId],
                        isTranslating = translatingMessageId == msg.messageId,
                        targetLanguage = userTranslationSettings.targetLanguage,
                        isTranslationEnabled = userTranslationSettings.textTranslationEnabled,
                        onLongClick = { selectedMessageForMenu = msg },
                        onReplyClick = { replyingToMessage = msg },
                        onReactionClick = { reaction ->
                            repository.reactToMessage(msg.messageId, currentUid, reaction)
                        },
                        onTranslateClick = {
                            coroutineScope.launch {
                                try {
                                    translatingMessageId = msg.messageId
                                    val res = TranslationCustomActions.TranslateMessage(
                                        callerUid = currentUid,
                                        conversationId = conversationId,
                                        messageId = msg.messageId,
                                        text = msg.text,
                                        targetLang = userTranslationSettings.targetLanguage
                                    )
                                    activeTranslations = activeTranslations + (msg.messageId to res.translatedText)
                                } catch (e: Exception) {
                                    errorMessage = e.message
                                } finally {
                                    translatingMessageId = null
                                }
                            }
                        },
                        onHideTranslation = {
                            activeTranslations = activeTranslations - msg.messageId
                        },
                        onCopyTranslation = { txt ->
                            clipboardManager.setText(AnnotatedString(txt))
                        },
                        onTranscribeVoice = {
                            coroutineScope.launch {
                                try {
                                    translatingMessageId = msg.messageId
                                    val res = TranslationCustomActions.TranscribeVoiceNote(
                                        callerUid = currentUid,
                                        messageId = msg.messageId,
                                        audioUrl = msg.mediaReference ?: "",
                                        targetLang = userTranslationSettings.targetLanguage
                                    )
                                    voiceTranscripts = voiceTranscripts + (msg.messageId to "${res.transcript}\n↳ [${userTranslationSettings.targetLanguage.uppercase()}]: ${res.translatedText}")
                                } catch (e: Exception) {
                                    errorMessage = e.message
                                } finally {
                                    translatingMessageId = null
                                }
                            }
                        }
                    )
                }
            }

            // Inline Reply Quoted Banner
            if (replyingToMessage != null) {
                Surface(
                    color = HealthogramTheme.colors.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Replying to message", style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                                Text(replyingToMessage?.text?.take(40) ?: "", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary, maxLines = 1)
                            }
                        }
                        IconButton(onClick = { replyingToMessage = null }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel reply", tint = HealthogramTheme.colors.textMuted)
                        }
                    }
                }
            }

            // Bottom Input Bar & Voice Recorder
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = HealthogramTheme.colors.surface,
                border = BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight)
            ) {
                if (isRecordingVoice) {
                    // Active Voice Recording UI
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recording: ${CommunicationCustomFunctions.getCallDurationLabel(voiceRecordSeconds)}",
                                style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFFEF4444)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { isRecordingVoice = false }) {
                                Text("Cancel", color = HealthogramTheme.colors.textMuted)
                            }
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        try {
                                            CommunicationCustomActions.SendVoiceMessage(
                                                callerUid = currentUid,
                                                conversationId = conversationId,
                                                audioUrl = "voice_${System.currentTimeMillis()}.m4a",
                                                durationSeconds = voiceRecordSeconds
                                            )
                                        } catch (e: Exception) {
                                            errorMessage = e.message
                                        } finally {
                                            isRecordingVoice = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = HealthogramTheme.colors.primary)
                            ) {
                                Text("Send Voice")
                            }
                        }
                    }
                } else {
                    // Standard Text & Media Input Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showAttachmentSheet = true }) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Attach media", tint = HealthogramTheme.colors.primary)
                        }

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = {
                                inputText = it
                                repository.setTyping(conversationId, currentUid, it.isNotBlank())
                            },
                            placeholder = { Text("Message...", style = HealthogramTheme.typography.bodyMedium) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                                .testTag("message_input_field"),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HealthogramTheme.colors.primary,
                                unfocusedBorderColor = HealthogramTheme.colors.borderLight
                            )
                        )

                        if (inputText.isBlank()) {
                            IconButton(
                                onClick = { isRecordingVoice = true },
                                modifier = Modifier.testTag("record_voice_button")
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Record voice note", tint = HealthogramTheme.colors.primary)
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    val textToSend = inputText.trim()
                                    if (textToSend.isNotEmpty()) {
                                        coroutineScope.launch {
                                            try {
                                                if (replyingToMessage != null) {
                                                    CommunicationCustomActions.ReplyToMessage(
                                                        callerUid = currentUid,
                                                        conversationId = conversationId,
                                                        replyToMessageId = replyingToMessage!!.messageId,
                                                        text = textToSend
                                                    )
                                                    replyingToMessage = null
                                                } else {
                                                    CommunicationCustomActions.SendMessage(
                                                        callerUid = currentUid,
                                                        conversationId = conversationId,
                                                        text = textToSend
                                                    )
                                                }
                                                inputText = ""
                                                repository.setTyping(conversationId, currentUid, false)
                                            } catch (e: Exception) {
                                                errorMessage = e.message
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(HealthogramTheme.colors.primary)
                                    .size(40.dp)
                                    .testTag("send_message_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Message Options Bottom Sheet
    if (selectedMessageForMenu != null) {
        val selected = selectedMessageForMenu!!
        val isMine = selected.senderUid == currentUid

        ModalBottomSheet(onDismissRequest = { selectedMessageForMenu = null }) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Quick Reaction Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("❤️", "👍", "😂", "😮", "😢", "😡").forEach { emoji ->
                        Text(
                            text = emoji,
                            style = HealthogramTheme.typography.titleLarge,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    repository.reactToMessage(selected.messageId, currentUid, emoji)
                                    selectedMessageForMenu = null
                                }
                                .padding(8.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Action: Reply
                ListItem(
                    headlineContent = { Text("Reply") },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null) },
                    modifier = Modifier.clickable {
                        replyingToMessage = selected
                        selectedMessageForMenu = null
                    }
                )

                // Action: Edit (Only own message and within window)
                if (isMine && !selected.isDeleted) {
                    ListItem(
                        headlineContent = { Text("Edit Message") },
                        leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                        modifier = Modifier.clickable {
                            messageToEdit = selected
                            editTextValue = selected.text
                            selectedMessageForMenu = null
                        }
                    )
                }

                // Action: Translation Controls
                if (!selected.isDeleted && selected.text.isNotBlank()) {
                    val isTranslated = activeTranslations.containsKey(selected.messageId)
                    ListItem(
                        headlineContent = {
                            Text(if (isTranslated) "Hide Translation" else "Translate (${userTranslationSettings.targetLanguage.uppercase()})")
                        },
                        leadingContent = {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = HealthogramTheme.colors.primary)
                        },
                        modifier = Modifier.clickable {
                            if (isTranslated) {
                                activeTranslations = activeTranslations - selected.messageId
                            } else {
                                coroutineScope.launch {
                                    try {
                                        translatingMessageId = selected.messageId
                                        val res = TranslationCustomActions.TranslateMessage(
                                            callerUid = currentUid,
                                            conversationId = conversationId,
                                            messageId = selected.messageId,
                                            text = selected.text,
                                            targetLang = userTranslationSettings.targetLanguage
                                        )
                                        activeTranslations = activeTranslations + (selected.messageId to res.translatedText)
                                    } catch (e: Exception) {
                                        errorMessage = e.message
                                    } finally {
                                        translatingMessageId = null
                                    }
                                }
                            }
                            selectedMessageForMenu = null
                        }
                    )

                    if (isTranslated) {
                        ListItem(
                            headlineContent = { Text("Copy Translation") },
                            leadingContent = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            modifier = Modifier.clickable {
                                activeTranslations[selected.messageId]?.let {
                                    clipboardManager.setText(AnnotatedString(it))
                                }
                                selectedMessageForMenu = null
                            }
                        )
                    }
                }

                // Action: Delete
                ListItem(
                    headlineContent = { Text("Delete", color = HealthogramTheme.colors.error) },
                    leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = HealthogramTheme.colors.error) },
                    modifier = Modifier.clickable {
                        showDeleteDialog = selected
                        selectedMessageForMenu = null
                    }
                )
            }
        }
    }

    // Quick Language Selection Bottom Sheet
    if (showLanguageSheet) {
        ModalBottomSheet(onDismissRequest = { showLanguageSheet = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Translation Settings", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Switch(
                        checked = userTranslationSettings.textTranslationEnabled,
                        onCheckedChange = { enabled ->
                            val updated = userTranslationSettings.copy(textTranslationEnabled = enabled)
                            userTranslationSettings = updated
                            TranslationCustomActions.repository.saveUserSettings(updated)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Preferred Target Language:", style = HealthogramTheme.typography.caption)
                Spacer(modifier = Modifier.height(8.dp))

                val availableLangs = listOf("en", "ar", "es", "fr", "de", "hi", "ur", "bn", "zh", "ja", "tr")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableLangs.take(5).forEach { code ->
                        val isSelected = userTranslationSettings.targetLanguage.equals(code, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val updated = userTranslationSettings.copy(targetLanguage = code)
                                userTranslationSettings = updated
                                TranslationCustomActions.repository.saveUserSettings(updated)
                            },
                            label = { Text("${TranslationCustomFunctions.getLanguageFlag(code)} ${code.uppercase()}") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableLangs.drop(5).forEach { code ->
                        val isSelected = userTranslationSettings.targetLanguage.equals(code, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val updated = userTranslationSettings.copy(targetLanguage = code)
                                userTranslationSettings = updated
                                TranslationCustomActions.repository.saveUserSettings(updated)
                            },
                            label = { Text("${TranslationCustomFunctions.getLanguageFlag(code)} ${code.uppercase()}") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Edit Message Dialog
    if (messageToEdit != null) {
        AlertDialog(
            onDismissRequest = { messageToEdit = null },
            title = { Text("Edit Message") },
            text = {
                OutlinedTextField(
                    value = editTextValue,
                    onValueChange = { editTextValue = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            repository.editMessage(messageToEdit!!.messageId, currentUid, editTextValue.trim())
                            messageToEdit = null
                        } catch (e: Exception) {
                            errorMessage = e.message
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Message Dialog (Delete for me vs Delete for everyone)
    if (showDeleteDialog != null) {
        val target = showDeleteDialog!!
        val isMine = target.senderUid == currentUid

        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Message?") },
            text = { Text("Choose how you want to delete this message.") },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isMine) {
                        Button(
                            onClick = {
                                repository.deleteMessage(target.messageId, currentUid, deleteForEveryone = true)
                                showDeleteDialog = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HealthogramTheme.colors.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Delete for Everyone")
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            repository.deleteMessage(target.messageId, currentUid, deleteForEveryone = false)
                            showDeleteDialog = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete for Me")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Media Attachments Picker Sheet
    if (showAttachmentSheet) {
        ModalBottomSheet(onDismissRequest = { showAttachmentSheet = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Send Attachment", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AttachmentOption(icon = Icons.Default.Image, label = "Photo", color = Color(0xFF3B82F6)) {
                        coroutineScope.launch {
                            CommunicationCustomActions.SendMediaMessage(
                                callerUid = currentUid,
                                conversationId = conversationId,
                                mediaUrl = "https://example.com/photo.jpg",
                                messageType = MessageType.IMAGE
                            )
                            showAttachmentSheet = false
                        }
                    }
                    AttachmentOption(icon = Icons.Default.Videocam, label = "Video", color = Color(0xFF8B5CF6)) {
                        coroutineScope.launch {
                            CommunicationCustomActions.SendMediaMessage(
                                callerUid = currentUid,
                                conversationId = conversationId,
                                mediaUrl = "https://example.com/video.mp4",
                                messageType = MessageType.VIDEO
                            )
                            showAttachmentSheet = false
                        }
                    }
                    AttachmentOption(icon = Icons.Default.Description, label = "Document", color = Color(0xFF10B981)) {
                        coroutineScope.launch {
                            CommunicationCustomActions.SendMediaMessage(
                                callerUid = currentUid,
                                conversationId = conversationId,
                                mediaUrl = "https://example.com/report.pdf",
                                messageType = MessageType.DOCUMENT,
                                fileName = "Lab_Summary_Report.pdf",
                                fileSizeBytes = 1024 * 350
                            )
                            showAttachmentSheet = false
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, style = HealthogramTheme.typography.caption)
    }
}

/**
 * Modern Message Bubble Component
 */
@Composable
fun MessageBubble(
    message: Message,
    isMine: Boolean,
    translatedText: String? = null,
    voiceTranscript: String? = null,
    isTranslating: Boolean = false,
    targetLanguage: String = "en",
    isTranslationEnabled: Boolean = true,
    onLongClick: () -> Unit,
    onReplyClick: () -> Unit,
    onReactionClick: (String) -> Unit,
    onTranslateClick: () -> Unit = {},
    onHideTranslation: () -> Unit = {},
    onCopyTranslation: (String) -> Unit = {},
    onTranscribeVoice: () -> Unit = {}
) {
    val bubbleColor = if (isMine) HealthogramTheme.colors.primary else HealthogramTheme.colors.surface
    val textColor = if (isMine) Color.White else HealthogramTheme.colors.textPrimary
    val alignment = if (isMine) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 16.dp
            ),
            color = bubbleColor,
            border = if (!isMine) BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight) else null,
            shadowElevation = 1.dp,
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clickable { onLongClick() }
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Reply Quote Preview
                if (message.replyToMessageId != null && message.replyPreview != null) {
                    Surface(
                        color = if (isMine) Color.White.copy(alpha = 0.15f) else HealthogramTheme.colors.surfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = message.replyPreview,
                            style = HealthogramTheme.typography.caption,
                            color = textColor.copy(alpha = 0.85f),
                            modifier = Modifier.padding(6.dp),
                            maxLines = 2
                        )
                    }
                }

                // Deleted Message Display
                if (message.isDeleted) {
                    Text(
                        text = "This message was deleted.",
                        style = HealthogramTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                        color = textColor.copy(alpha = 0.7f)
                    )
                } else {
                    // Render by MessageType
                    when (message.messageType) {
                        MessageType.TEXT -> {
                            Text(
                                text = message.text,
                                style = HealthogramTheme.typography.bodyMedium,
                                color = textColor
                            )
                        }
                        MessageType.IMAGE -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.DarkGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Image, contentDescription = "Image preview", tint = Color.White)
                            }
                        }
                        MessageType.VOICE -> {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play voice note", tint = textColor)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    LinearProgressIndicator(
                                        progress = { 0.35f },
                                        modifier = Modifier.weight(1f),
                                        color = if (isMine) Color.White else HealthogramTheme.colors.primary,
                                        trackColor = textColor.copy(alpha = 0.2f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${message.mediaDurationSeconds ?: 0}s",
                                        style = HealthogramTheme.typography.caption,
                                        color = textColor
                                    )
                                }

                                if (isTranslationEnabled && voiceTranscript == null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isMine) Color.White.copy(alpha = 0.15f) else HealthogramTheme.colors.surfaceVariant)
                                            .clickable { onTranscribeVoice() }
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Translate,
                                            contentDescription = "Transcribe & Translate",
                                            tint = textColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isTranslating) "Transcribing..." else "Transcribe & Translate",
                                            style = HealthogramTheme.typography.caption.copy(fontSize = 11.sp),
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }
                        MessageType.DOCUMENT -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = textColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = message.fileName ?: "Document.pdf",
                                        style = HealthogramTheme.typography.titleSmall,
                                        color = textColor
                                    )
                                    Text(
                                        text = "${(message.fileSizeBytes ?: 0) / 1024} KB",
                                        style = HealthogramTheme.typography.caption,
                                        color = textColor.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                        else -> {
                            Text(text = message.text, style = HealthogramTheme.typography.bodyMedium, color = textColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Footer: Translate button if text not translated yet
                if (isTranslationEnabled && !isMine && !message.isDeleted && message.messageType == MessageType.TEXT && message.text.isNotBlank() && translatedText == null) {
                    TranslateButton(
                        isTranslating = isTranslating,
                        isTranslated = false,
                        onClick = onTranslateClick,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Footer: Timestamp, Edited flag, Delivery tick
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (message.isEdited) {
                        Text(
                            text = "edited",
                            style = HealthogramTheme.typography.caption.copy(fontSize = 10.sp),
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.createdAt)),
                        style = HealthogramTheme.typography.caption.copy(fontSize = 10.sp),
                        color = textColor.copy(alpha = 0.75f)
                    )
                    if (isMine) {
                        Text(
                            text = CommunicationCustomFunctions.getMessageStatusIcon(message.deliveryStatus),
                            style = HealthogramTheme.typography.caption.copy(fontSize = 11.sp),
                            color = textColor.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // Translated Message Card below original message bubble
        if (translatedText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.widthIn(max = 290.dp)) {
                TranslatedMessageCard(
                    originalText = message.text,
                    translatedText = translatedText,
                    sourceLanguage = "auto",
                    targetLanguage = targetLanguage,
                    isHealthcareContext = TranslationCustomFunctions.isHealthcareContent(message.text),
                    onCopyTranslation = { onCopyTranslation(translatedText) },
                    onHideTranslation = onHideTranslation
                )
            }
        }

        // Voice Transcript Card if transcribed
        if (voiceTranscript != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.widthIn(max = 290.dp)) {
                TranslatedMessageCard(
                    originalText = "Voice Message",
                    translatedText = voiceTranscript,
                    sourceLanguage = "auto",
                    targetLanguage = targetLanguage,
                    isHealthcareContext = false,
                    onCopyTranslation = { onCopyTranslation(voiceTranscript) },
                    onHideTranslation = onHideTranslation
                )
            }
        }

        // Reaction Pills under Bubble
        if (message.reactionSummary.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                message.reactionSummary.forEach { (emoji, count) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = HealthogramTheme.colors.surface,
                        border = BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight),
                        shadowElevation = 1.dp
                    ) {
                        Text(
                            text = "$emoji $count",
                            style = HealthogramTheme.typography.caption,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
