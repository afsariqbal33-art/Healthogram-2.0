package com.example.healthogram.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.communication.*
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.BadgeSize
import com.example.healthogram.designsystem.components.HealthogramVerifiedBadge
import java.text.SimpleDateFormat
import java.util.*

/**
 * HEALTHOGRAM STEP 11: MESSAGE REQUESTS PAGE
 *
 * Incoming message requests from accounts outside permitted circles:
 * - Recipient actions: Accept, Delete, Block, Report.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageRequestsPage(
    currentUid: String,
    repository: CommunicationRepository,
    onBack: () -> Unit,
    onOpenConversation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val requests by repository.messageRequests.collectAsState()
    val myRequests = remember(requests, currentUid) {
        requests.filter { it.recipientUid == currentUid && it.status == MessageRequestStatus.PENDING }
    }

    var selectedForReport by remember { mutableStateOf<MessageRequest?>(null) }
    var reportReason by remember { mutableStateOf("") }
    var reportDesc by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Message Requests", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("message_requests_page")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(HealthogramTheme.colors.background)
        ) {
            Surface(
                color = HealthogramTheme.colors.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "People you don't follow or haven't connected with can send requests. Opening a request won't let them know you've seen it until you accept.",
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.textSecondary,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (myRequests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No pending message requests", style = HealthogramTheme.typography.bodyMedium, color = HealthogramTheme.colors.textMuted)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(myRequests, key = { it.requestId }) { req ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(containerColor = HealthogramTheme.colors.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(HealthogramTheme.colors.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = req.senderUid.take(1).uppercase(),
                                                style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = HealthogramTheme.colors.onPrimaryContainer
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "User @${req.senderUid.take(8)}",
                                                    style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                HealthogramVerifiedBadge(status = VerificationStatus.APPROVED, size = BadgeSize.SMALL)
                                            }
                                            Text(
                                                text = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(req.createdAt)),
                                                style = HealthogramTheme.typography.caption,
                                                color = HealthogramTheme.colors.textMuted
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Hello, I would like to inquire about your consultation availability and service schedule.",
                                    style = HealthogramTheme.typography.bodySmall,
                                    color = HealthogramTheme.colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                // Actions: Accept, Delete, Block, Report
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            // Accept request
                                            onOpenConversation(req.conversationId)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = HealthogramTheme.colors.primary),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Accept")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            // Delete request
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Delete")
                                    }

                                    IconButton(
                                        onClick = {
                                            repository.blockUser(currentUid, req.senderUid, "Blocked from message request")
                                        }
                                    ) {
                                        Icon(Icons.Default.Block, contentDescription = "Block", tint = HealthogramTheme.colors.error)
                                    }

                                    IconButton(
                                        onClick = { selectedForReport = req }
                                    ) {
                                        Icon(Icons.Default.Report, contentDescription = "Report", tint = HealthogramTheme.colors.textMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Report Dialog
    if (selectedForReport != null) {
        val req = selectedForReport!!
        AlertDialog(
            onDismissRequest = { selectedForReport = null },
            title = { Text("Report Message Request") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Help us keep Healthogram safe. What is wrong with this request?", style = HealthogramTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        label = { Text("Reason (Spam, Harassment, Inappropriate)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reportDesc,
                        onValueChange = { reportDesc = it },
                        label = { Text("Additional Details") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.reportCommunication(
                            reporterUid = currentUid,
                            reportedUid = req.senderUid,
                            conversationId = req.conversationId,
                            reason = reportReason.ifBlank { "Unwanted request" },
                            description = reportDesc
                        )
                        selectedForReport = null
                    }
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedForReport = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
