package com.example.healthogram.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.healthogram.auth.FirebaseAuthManager
import com.example.healthogram.auth.components.*
import com.example.healthogram.designsystem.HealthogramTheme
import kotlinx.coroutines.launch

/**
 * Forgot Password Page.
 *
 * Enforces Section 14 Privacy-Preserving Rule:
 * Does NOT reveal whether a specific email address exists in the system.
 */
@Composable
fun ForgotPasswordPage(
    authManager: FirebaseAuthManager,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var emailInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var successNotice by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 24.dp)
            .testTag("forgot_password_page"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackToLogin) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back to login", tint = HealthogramTheme.colors.textPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Reset Password",
                style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Icon(
            imageVector = Icons.Default.LockReset,
            contentDescription = null,
            tint = HealthogramTheme.colors.primary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enter your registered email address. We will send you secure instructions to reset your password.",
            style = HealthogramTheme.typography.bodyMedium,
            color = HealthogramTheme.colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (successNotice != null) {
            SuccessMessage(message = successNotice!!)
            Spacer(modifier = Modifier.height(20.dp))
            SecondaryAuthButton(
                text = "Back to Login",
                onClick = onBackToLogin
            )
        } else {
            if (errorMessage != null) {
                AuthErrorMessage(message = errorMessage!!)
                Spacer(modifier = Modifier.height(14.dp))
            }

            AuthTextField(
                value = emailInput,
                onValueChange = {
                    emailInput = it
                    errorMessage = null
                },
                label = "Email address",
                placeholder = "name@domain.com",
                leadingIcon = Icons.Default.Email,
                testTag = "reset_email_input"
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryAuthButton(
                text = "Send Password Reset Email",
                onClick = {
                    if (emailInput.isBlank()) {
                        errorMessage = "Please enter your email."
                        return@PrimaryAuthButton
                    }
                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        val result = authManager.sendPasswordResetEmail(emailInput)
                        isLoading = false
                        successNotice = result.getOrNull()
                    }
                },
                isLoading = isLoading,
                testTag = "send_reset_button"
            )
        }
    }
}
