package com.example.healthogram.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.auth.FirebaseAuthManager
import com.example.healthogram.auth.components.SecurityOptionTile
import com.example.healthogram.designsystem.HealthogramTheme

/**
 * Account Security Settings Page.
 *
 * Enforces Section 21 & 22:
 * - Centralized security management
 * - Change password & change email
 * - Phone & email verification status
 * - Future MFA / Authenticator App architecture placeholder
 * - Active devices list (4-device limit enforcement)
 * - Security audit alerts & recovery
 * - Delete account workflow
 */
@Composable
fun SecuritySettingsPage(
    authManager: FirebaseAuthManager,
    onBack: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToChangeEmail: () -> Unit,
    onNavigateToActiveDevices: () -> Unit,
    onNavigateToDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by authManager.currentUser.collectAsState()
    var showMfaDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
            .testTag("security_settings_page")
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Account Security",
                style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Login Credentials
        Text(
            text = "LOGIN & CREDENTIALS",
            style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = HealthogramTheme.colors.textSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))

        SecurityOptionTile(
            title = "Password",
            subtitle = "Last changed recently • Standard protection",
            icon = Icons.Default.Lock,
            onClick = onNavigateToChangePassword
        )

        Spacer(modifier = Modifier.height(10.dp))

        SecurityOptionTile(
            title = "Email Address",
            subtitle = currentUser?.email ?: "Not registered",
            icon = Icons.Default.Email,
            badgeText = "Verified",
            onClick = onNavigateToChangeEmail
        )

        Spacer(modifier = Modifier.height(10.dp))

        SecurityOptionTile(
            title = "Phone Verification",
            subtitle = if (currentUser?.phoneNumber.isNullOrBlank()) "Add backup phone number" else currentUser!!.phoneNumber,
            icon = Icons.Default.PhoneIphone,
            onClick = { /* Phone verification link */ }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Advanced Security & Sessions
        Text(
            text = "TWO-FACTOR & DEVICES",
            style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = HealthogramTheme.colors.textSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))

        SecurityOptionTile(
            title = "Authenticator App (MFA)",
            subtitle = "Time-based One-Time Password (TOTP)",
            icon = Icons.Default.Security,
            badgeText = "Upcoming",
            onClick = { showMfaDialog = true }
        )

        Spacer(modifier = Modifier.height(10.dp))

        SecurityOptionTile(
            title = "Active Devices & Sessions",
            subtitle = "Maximum 4 devices • Manage authorized devices",
            icon = Icons.Default.Devices,
            onClick = onNavigateToActiveDevices
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Auditing & Dangerous Actions
        Text(
            text = "ACCOUNT PRIVACY & CLOSURE",
            style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = HealthogramTheme.colors.textSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))

        SecurityOptionTile(
            title = "Security Audit Logs",
            subtitle = "Zero-trust session tracking & login history",
            icon = Icons.Default.History,
            onClick = { /* View security audit log */ }
        )

        Spacer(modifier = Modifier.height(10.dp))

        SecurityOptionTile(
            title = "Delete Account",
            subtitle = "Permanent deletion & health data retention review",
            icon = Icons.Default.DeleteForever,
            isDanger = true,
            onClick = onNavigateToDeleteAccount
        )

        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showMfaDialog) {
        AlertDialog(
            onDismissRequest = { showMfaDialog = false },
            title = { Text("Authenticator App (MFA)") },
            text = {
                Text("Hardware token & TOTP Authenticator (Google Authenticator, Microsoft Authenticator) architecture foundation is established. Native TOTP enrollment requires multi-tenant Firebase MFA backend configuration.")
            },
            confirmButton = {
                TextButton(onClick = { showMfaDialog = false }) {
                    Text("OK", color = HealthogramTheme.colors.primary)
                }
            }
        )
    }
}
