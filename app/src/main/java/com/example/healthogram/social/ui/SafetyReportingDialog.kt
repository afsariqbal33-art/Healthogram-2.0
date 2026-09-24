package com.example.healthogram.social.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.social.SafetyReport
import com.example.healthogram.social.SafetyReportReason
import com.example.healthogram.social.SocialFeedEngine

@Composable
fun SafetyReportingDialog(
    entityId: String,
    entityType: String,
    authorUid: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = remember { SocialFeedEngine.getInstance() }
    var selectedReason by remember { mutableStateOf(SafetyReportReason.SPAM) }
    var additionalNotes by remember { mutableStateOf("") }
    var alsoBlockUser by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("safety_reporting_dialog"),
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Safety Alert",
                tint = HealthogramTheme.colors.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = if (isSubmitted) "Report Received" else "Report Content",
                style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            if (isSubmitted) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Thank you for keeping Healthogram safe. Our automated safety engine and trust & safety team have logged this report for immediate moderation review.",
                        style = HealthogramTheme.typography.bodySmall,
                        color = HealthogramTheme.colors.textPrimary
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Why are you reporting this $entityType?",
                        style = HealthogramTheme.typography.bodySmall,
                        color = HealthogramTheme.colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(SafetyReportReason.entries) { reason ->
                            val isSelected = selectedReason == reason
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedReason = reason },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) HealthogramTheme.colors.primaryContainer else HealthogramTheme.colors.surfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.borderLight
                                )
                            ) {
                                Text(
                                    text = reason.label,
                                    style = HealthogramTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) HealthogramTheme.colors.onPrimaryContainer else HealthogramTheme.colors.textPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = additionalNotes,
                        onValueChange = { additionalNotes = it },
                        placeholder = { Text("Additional context (optional)...", style = HealthogramTheme.typography.bodySmall) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = alsoBlockUser,
                            onCheckedChange = { alsoBlockUser = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Also block this account",
                            style = HealthogramTheme.typography.caption,
                            color = HealthogramTheme.colors.textPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (isSubmitted) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = HealthogramTheme.colors.primary)
                ) {
                    Text("Done", color = Color.White)
                }
            } else {
                Button(
                    onClick = {
                        val report = SafetyReport(
                            reporterUid = "current_user",
                            reportedEntityId = entityId,
                            entityType = entityType,
                            reason = selectedReason,
                            details = additionalNotes.trim()
                        )
                        engine.submitSafetyReport(report)
                        if (alsoBlockUser) {
                            engine.blockUser(authorUid)
                        }
                        isSubmitted = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthogramTheme.colors.error),
                    modifier = Modifier.testTag("submit_safety_report_btn")
                ) {
                    Text("Submit Report", color = Color.White)
                }
            }
        },
        dismissButton = {
            if (!isSubmitted) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = HealthogramTheme.colors.textMuted)
                }
            }
        }
    )
}
