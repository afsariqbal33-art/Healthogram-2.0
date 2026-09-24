package com.example.healthogram.ui.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.designsystem.HealthogramTheme

/**
 * Notification Privacy & Patient Confidentiality Policy Page.
 * Outlines the architectural guarantees of Healthogram's Zero-Clinical-Exposure notification infrastructure.
 */
@Composable
fun NotificationPrivacyPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var hideSenderOnLockScreen by remember { mutableStateOf(false) }
    var sanitizePreviewText by remember { mutableStateOf(true) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("notification_privacy_page"),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = HealthogramTheme.colors.surface,
                border = BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Notification Privacy & Safety",
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(HealthogramTheme.colors.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Assurance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.dp, Color(0xFF86EFAC))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Zero-Clinical-Data in Push Notifications",
                            style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF15803D)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "To protect your HIPAA and GDPR health privacy, Healthogram enforces strict server-side redaction. Push payloads never contain diagnoses, prescriptions, lab values, or medical reports.",
                            style = HealthogramTheme.typography.bodySmall,
                            color = Color(0xFF166534)
                        )
                    }
                }
            }

            // Lock Screen Confidentiality Controls
            Text(
                text = "LOCK SCREEN PRIVACY",
                style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textMuted
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sanitize Lock Screen Previews", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("Displays 'You have a new message' without revealing message content.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                        }
                        Switch(checked = sanitizePreviewText, onCheckedChange = { sanitizePreviewText = it })
                    }
                    Divider(color = HealthogramTheme.colors.borderLight, modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hide Sender Name on Lock Screen", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("Shows only 'Healthogram' as sender until phone is unlocked.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                        }
                        Switch(checked = hideSenderOnLockScreen, onCheckedChange = { hideSenderOnLockScreen = it })
                    }
                }
            }

            // Legal & Technical Invariants
            Text(
                text = "ARCHITECTURAL GUARANTEES",
                style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textMuted
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GuaranteeItem(
                        title = "Health Passport Access ≠ Push Approval",
                        desc = "Receiving an access request notification does not grant permission. You must unlock the app and authenticate to authorize a doctor or clinic."
                    )
                    GuaranteeItem(
                        title = "Marketing Separation",
                        desc = "Promotional campaigns are completely decoupled from clinical alerts. Disabling marketing notifications will never suppress appointment or security alerts."
                    )
                    GuaranteeItem(
                        title = "Immutable Security Retention",
                        desc = "Security notifications (e.g. login attempts, Health Passport QR scans) are stored in an immutable 90-day compliance audit trail."
                    )
                    GuaranteeItem(
                        title = "End-to-End Key Insulation",
                        desc = "Encryption keys, WebRTC tokens, and session credentials are never embedded in FCM payloads."
                    )
                }
            }
        }
    }
}

@Composable
private fun GuaranteeItem(title: String, desc: String) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(desc, style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textSecondary, modifier = Modifier.padding(start = 24.dp))
    }
}
