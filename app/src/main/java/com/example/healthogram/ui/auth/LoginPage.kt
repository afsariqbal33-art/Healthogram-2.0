package com.example.healthogram.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.auth.FirebaseAuthManager
import com.example.healthogram.auth.components.*
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.HealthogramLogo
import com.example.healthogram.designsystem.components.HealthogramLogoSize
import kotlinx.coroutines.launch

/**
 * Modern Instagram-style premium login experience.
 *
 * Designed in strict accordance with Section 4 of Master Architecture:
 * - Healthogram logo and premium branding
 * - Email / phone input
 * - Password input with show/hide toggle
 * - Forgot password link
 * - Login button with asynchronous loading state
 * - Phone / OTP login option
 * - Google & Apple social sign-in buttons
 * - Create Account navigation
 * - Terms & Privacy links
 */
@Composable
fun LoginPage(
    authManager: FirebaseAuthManager,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToPhoneLogin: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var socialAuthNotice by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
            .testTag("login_page"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Branding & Logo
        HealthogramLogo(
            size = HealthogramLogoSize.LARGE,
            showWordmark = true
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Global Healthcare & Creator Super-Platform",
            style = HealthogramTheme.typography.bodySmall,
            color = HealthogramTheme.colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Error Banner
        if (errorMessage != null) {
            AuthErrorMessage(message = errorMessage!!)
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Social Notice Banner
        if (socialAuthNotice != null) {
            Surface(
                shape = HealthogramTheme.shapes.medium,
                color = HealthogramTheme.colors.primary.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = socialAuthNotice!!,
                    style = HealthogramTheme.typography.bodySmall,
                    color = HealthogramTheme.colors.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Email Input
        AuthTextField(
            value = emailInput,
            onValueChange = {
                emailInput = it
                errorMessage = null
            },
            label = "Email address",
            placeholder = "name@domain.com",
            leadingIcon = Icons.Default.Email,
            isError = errorMessage != null && emailInput.isBlank(),
            testTag = "login_email_input"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Password Input
        PasswordField(
            value = passwordInput,
            onValueChange = {
                passwordInput = it
                errorMessage = null
            },
            label = "Password",
            isError = errorMessage != null && passwordInput.isBlank(),
            testTag = "login_password_input"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Forgot Password Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Forgot password?",
                style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = HealthogramTheme.colors.primary,
                modifier = Modifier
                    .clickable { onNavigateToForgotPassword() }
                    .padding(vertical = 4.dp)
                    .testTag("forgot_password_link")
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Primary Login Button
        PrimaryAuthButton(
            text = "Log In",
            onClick = {
                if (emailInput.isBlank() || passwordInput.isBlank()) {
                    errorMessage = "Please enter both your email and password."
                    return@PrimaryAuthButton
                }
                isLoading = true
                errorMessage = null
                coroutineScope.launch {
                    val result = authManager.signInWithEmail(emailInput, passwordInput)
                    isLoading = false
                    if (result.isSuccess) {
                        onLoginSuccess()
                    } else {
                        errorMessage = result.exceptionOrNull()?.message ?: "Login failed. Please check your credentials."
                    }
                }
            },
            isLoading = isLoading,
            testTag = "login_submit_button"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Phone OTP Option
        SecondaryAuthButton(
            text = "Continue with Phone Number",
            onClick = onNavigateToPhoneLogin,
            testTag = "login_phone_otp_button"
        )

        AuthDivider()

        // Social Authentication
        SocialLoginButton(
            text = "Continue with Google",
            provider = "GOOGLE",
            onClick = {
                socialAuthNotice = "Google Sign-In ready in architecture. Requires client OAuth Web Client ID configuration in Google Cloud Console."
            },
            testTag = "login_google_button"
        )

        Spacer(modifier = Modifier.height(10.dp))

        SocialLoginButton(
            text = "Continue with Apple",
            provider = "APPLE",
            onClick = {
                socialAuthNotice = "Apple Sign-In architecture ready. Requires Apple Developer Services ID association."
            },
            testTag = "login_apple_button"
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Sign Up Link
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Don't have an account?",
                style = HealthogramTheme.typography.bodyMedium,
                color = HealthogramTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Sign Up",
                style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.primary,
                modifier = Modifier
                    .clickable { onNavigateToSignUp() }
                    .testTag("navigate_signup_link")
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Terms and Privacy Notice
        Text(
            text = "By continuing, you agree to Healthogram's Terms of Service, Privacy Policy, and Health Data Protection Standards.",
            style = HealthogramTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = HealthogramTheme.colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
