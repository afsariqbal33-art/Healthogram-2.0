package com.example.healthogram.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
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
 * Change Email Page with re-authentication and authoritative Firebase Auth update.
 */
@Composable
fun ChangeEmailPage(
    authManager: FirebaseAuthManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val currentUser by authManager.currentUser.collectAsState()
    var newEmail by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successNotice by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 20.dp)
            .testTag("change_email_page")
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
                text = "Change Email Address",
                style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Current registered email: ${currentUser?.email ?: "Not set"}",
            style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = HealthogramTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (successNotice != null) {
            SuccessMessage(message = successNotice!!)
            Spacer(modifier = Modifier.height(16.dp))
            SecondaryAuthButton(text = "Done", onClick = onBack)
        } else {
            if (errorMessage != null) {
                AuthErrorMessage(message = errorMessage!!)
                Spacer(modifier = Modifier.height(14.dp))
            }

            AuthTextField(
                value = newEmail,
                onValueChange = {
                    newEmail = it
                    errorMessage = null
                },
                label = "New Email Address",
                placeholder = "new.email@domain.com",
                leadingIcon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(14.dp))

            PasswordField(
                value = passwordConfirmation,
                onValueChange = {
                    passwordConfirmation = it
                    errorMessage = null
                },
                label = "Current Password",
                placeholder = "Confirm password for security"
            )

            Spacer(modifier = Modifier.height(28.dp))

            PrimaryAuthButton(
                text = "Send Verification to New Email",
                onClick = {
                    if (!AuthValidators.isValidEmail(newEmail)) {
                        errorMessage = "Please enter a valid email address."
                        return@PrimaryAuthButton
                    }
                    if (passwordConfirmation.isBlank()) {
                        errorMessage = "Password confirmation is required."
                        return@PrimaryAuthButton
                    }
                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        val result = authManager.changeEmail(newEmail, passwordConfirmation)
                        isLoading = false
                        if (result.isSuccess) {
                            successNotice = "A confirmation link has been sent to $newEmail. Please click the link to verify your new email."
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to update email."
                        }
                    }
                },
                isLoading = isLoading,
                testTag = "submit_change_email"
            )
        }
    }
}
