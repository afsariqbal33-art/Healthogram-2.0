package com.example.healthogram.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.auth.AuthValidators
import com.example.healthogram.auth.FirebaseAuthManager
import com.example.healthogram.auth.components.*
import com.example.healthogram.designsystem.HealthogramTheme
import kotlinx.coroutines.launch

/**
 * Change Password Page with re-authentication and strength meter.
 */
@Composable
fun ChangePasswordPage(
    authManager: FirebaseAuthManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successNotice by remember { mutableStateOf<String?>(null) }

    val strength = remember(newPassword) {
        AuthValidators.validatePasswordStrength(newPassword)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 20.dp)
            .testTag("change_password_page")
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
                text = "Change Password",
                style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (successNotice != null) {
            SuccessMessage(message = successNotice!!)
            Spacer(modifier = Modifier.height(16.dp))
            SecondaryAuthButton(text = "Done", onClick = onBack)
        } else {
            if (errorMessage != null) {
                AuthErrorMessage(message = errorMessage!!)
                Spacer(modifier = Modifier.height(14.dp))
            }

            PasswordField(
                value = currentPassword,
                onValueChange = {
                    currentPassword = it
                    errorMessage = null
                },
                label = "Current Password",
                placeholder = "Enter current password"
            )

            Spacer(modifier = Modifier.height(14.dp))

            PasswordField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    errorMessage = null
                },
                label = "New Password",
                placeholder = "At least 8 characters"
            )

            Spacer(modifier = Modifier.height(6.dp))
            PasswordStrengthIndicator(strength = strength)

            Spacer(modifier = Modifier.height(14.dp))

            PasswordField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                label = "Confirm New Password",
                placeholder = "Re-enter new password"
            )

            Spacer(modifier = Modifier.height(28.dp))

            PrimaryAuthButton(
                text = "Update Password",
                onClick = {
                    if (currentPassword.isBlank() || newPassword.isBlank()) {
                        errorMessage = "Please fill in all password fields."
                        return@PrimaryAuthButton
                    }
                    if (!strength.isAcceptable) {
                        errorMessage = "New password does not meet security requirements."
                        return@PrimaryAuthButton
                    }
                    if (newPassword != confirmPassword) {
                        errorMessage = "Passwords do not match."
                        return@PrimaryAuthButton
                    }
                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        val result = authManager.changePassword(currentPassword, newPassword)
                        isLoading = false
                        if (result.isSuccess) {
                            successNotice = "Password updated successfully. Your active sessions remain secured."
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to update password."
                        }
                    }
                },
                isLoading = isLoading,
                testTag = "submit_change_password"
            )
        }
    }
}
