package com.example.healthogram.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.auth.FirebaseAuthManager
import com.example.healthogram.auth.components.AuthErrorMessage
import com.example.healthogram.auth.components.PasswordField
import com.example.healthogram.auth.components.PrimaryAuthButton
import com.example.healthogram.auth.components.SecondaryAuthButton
import com.example.healthogram.designsystem.HealthogramTheme
import kotlinx.coroutines.launch

/**
 * Delete Account Page.
 *
 * Enforces Section 23:
 * - Explains data retention requirements for regulated healthcare and financial audit data.
 * - Multi-step confirmation.
 * - Re-authentication with password.
 * - Permanent deletion workflow.
 */
@Composable
fun DeleteAccountPage(
    authManager: FirebaseAuthManager,
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var passwordConfirmation by remember { mutableStateOf("") }
    var confirmedRisk by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
            .testTag("delete_account_page")
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
                text = "Delete Account",
                style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.error
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Warning card
        Card(
            shape = HealthogramTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.error.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, HealthogramTheme.colors.error.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = HealthogramTheme.colors.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Permanent Action Warning",
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.error
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Deleting your account is irreversible. Your profile, social posts, reels, and messaging history will be permanently deleted.",
                    style = HealthogramTheme.typography.bodyMedium,
                    color = HealthogramTheme.colors.textPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Regulated Healthcare & Financial Retention Notice: In accordance with international healthcare regulations (such as HIPAA/GDPR) and taxation laws, official clinical consultations, laboratory diagnostic reports, and marketplace financial transactions may be anonymized and retained for legal statutory retention periods.",
                    style = HealthogramTheme.typography.bodySmall,
                    color = HealthogramTheme.colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = confirmedRisk,
                onCheckedChange = { confirmedRisk = it },
                colors = CheckboxDefaults.colors(checkedColor = HealthogramTheme.colors.error)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "I understand the consequences and wish to permanently delete my account.",
                style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = HealthogramTheme.colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            AuthErrorMessage(message = errorMessage!!)
            Spacer(modifier = Modifier.height(14.dp))
        }

        PasswordField(
            value = passwordConfirmation,
            onValueChange = {
                passwordConfirmation = it
                errorMessage = null
            },
            label = "Confirm Password",
            placeholder = "Enter your password to authorize deletion"
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                if (!confirmedRisk) {
                    errorMessage = "Please check the confirmation box to proceed."
                    return@Button
                }
                if (passwordConfirmation.isBlank()) {
                    errorMessage = "Please enter your password to confirm deletion."
                    return@Button
                }
                isLoading = true
                errorMessage = null
                coroutineScope.launch {
                    val result = authManager.deleteAccount(passwordConfirmation)
                    isLoading = false
                    if (result.isSuccess) {
                        onAccountDeleted()
                    } else {
                        errorMessage = result.exceptionOrNull()?.message ?: "Account deletion failed."
                    }
                }
            },
            enabled = confirmedRisk && !isLoading,
            shape = HealthogramTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = HealthogramTheme.colors.error,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("confirm_delete_account_button")
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            } else {
                Text("Permanently Delete My Account", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SecondaryAuthButton(
            text = "Cancel and Keep Account",
            onClick = onBack
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
