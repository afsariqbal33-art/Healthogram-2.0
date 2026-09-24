package com.example.healthogram.ui.auth

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthogram.auth.AuthState
import com.example.healthogram.auth.FirebaseAuthManager
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.HealthogramLogo
import com.example.healthogram.designsystem.components.HealthogramLogoSize

enum class AuthFlowScreen {
    LOGIN,
    SIGN_UP,
    PHONE_LOGIN,
    FORGOT_PASSWORD,
    CHANGE_PASSWORD,
    CHANGE_EMAIL,
    ACTIVE_DEVICES,
    SECURITY_SETTINGS,
    DELETE_ACCOUNT,
    RESTRICTED
}

/**
 * Centralized Authentication & Navigation Decision Router.
 *
 * Enforces Section 30 & 31:
 * Determines:
 * - Is user authenticated?
 * - Is account active?
 * - Is onboarding complete?
 * - Is account restricted?
 */
@Composable
fun AuthenticationRouter(
    authManager: FirebaseAuthManager,
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by authManager.authState.collectAsState()
    var currentFlowScreen by remember { mutableStateOf(AuthFlowScreen.LOGIN) }

    when (val state = authState) {
        is AuthState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(HealthogramTheme.colors.background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HealthogramLogo(size = HealthogramLogoSize.LARGE, showWordmark = true)
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator(color = HealthogramTheme.colors.primary, strokeWidth = 2.dp)
                }
            }
        }

        is AuthState.AccountRestricted -> {
            AccountRestrictedPage(
                authManager = authManager,
                reason = state.reason,
                onSignOut = { currentFlowScreen = AuthFlowScreen.LOGIN }
            )
        }

        is AuthState.DeviceLimitExceeded -> {
            ActiveDevicesPage(
                authManager = authManager,
                onBack = { authManager.signOut() }
            )
        }

        is AuthState.OnboardingRequired -> {
            SignUpWizardPage(
                authManager = authManager,
                onNavigateToLogin = { authManager.signOut() },
                onSignUpComplete = onAuthenticated
            )
        }

        is AuthState.Authenticated -> {
            onAuthenticated()
        }

        is AuthState.Unauthenticated -> {
            Crossfade(targetState = currentFlowScreen, label = "auth_screen_crossfade") { screen ->
                when (screen) {
                    AuthFlowScreen.LOGIN -> {
                        LoginPage(
                            authManager = authManager,
                            onNavigateToSignUp = { currentFlowScreen = AuthFlowScreen.SIGN_UP },
                            onNavigateToForgotPassword = { currentFlowScreen = AuthFlowScreen.FORGOT_PASSWORD },
                            onNavigateToPhoneLogin = { currentFlowScreen = AuthFlowScreen.PHONE_LOGIN },
                            onLoginSuccess = onAuthenticated
                        )
                    }

                    AuthFlowScreen.SIGN_UP -> {
                        SignUpWizardPage(
                            authManager = authManager,
                            onNavigateToLogin = { currentFlowScreen = AuthFlowScreen.LOGIN },
                            onSignUpComplete = onAuthenticated
                        )
                    }

                    AuthFlowScreen.PHONE_LOGIN -> {
                        PhoneLoginPage(
                            authManager = authManager,
                            onBackToEmailLogin = { currentFlowScreen = AuthFlowScreen.LOGIN },
                            onLoginSuccess = onAuthenticated
                        )
                    }

                    AuthFlowScreen.FORGOT_PASSWORD -> {
                        ForgotPasswordPage(
                            authManager = authManager,
                            onBackToLogin = { currentFlowScreen = AuthFlowScreen.LOGIN }
                        )
                    }

                    AuthFlowScreen.CHANGE_PASSWORD -> {
                        ChangePasswordPage(
                            authManager = authManager,
                            onBack = { currentFlowScreen = AuthFlowScreen.SECURITY_SETTINGS }
                        )
                    }

                    AuthFlowScreen.CHANGE_EMAIL -> {
                        ChangeEmailPage(
                            authManager = authManager,
                            onBack = { currentFlowScreen = AuthFlowScreen.SECURITY_SETTINGS }
                        )
                    }

                    AuthFlowScreen.ACTIVE_DEVICES -> {
                        ActiveDevicesPage(
                            authManager = authManager,
                            onBack = { currentFlowScreen = AuthFlowScreen.SECURITY_SETTINGS }
                        )
                    }

                    AuthFlowScreen.SECURITY_SETTINGS -> {
                        SecuritySettingsPage(
                            authManager = authManager,
                            onBack = { currentFlowScreen = AuthFlowScreen.LOGIN },
                            onNavigateToChangePassword = { currentFlowScreen = AuthFlowScreen.CHANGE_PASSWORD },
                            onNavigateToChangeEmail = { currentFlowScreen = AuthFlowScreen.CHANGE_EMAIL },
                            onNavigateToActiveDevices = { currentFlowScreen = AuthFlowScreen.ACTIVE_DEVICES },
                            onNavigateToDeleteAccount = { currentFlowScreen = AuthFlowScreen.DELETE_ACCOUNT }
                        )
                    }

                    AuthFlowScreen.DELETE_ACCOUNT -> {
                        DeleteAccountPage(
                            authManager = authManager,
                            onBack = { currentFlowScreen = AuthFlowScreen.SECURITY_SETTINGS },
                            onAccountDeleted = { currentFlowScreen = AuthFlowScreen.LOGIN }
                        )
                    }

                    AuthFlowScreen.RESTRICTED -> {
                        AccountRestrictedPage(
                            authManager = authManager,
                            onSignOut = { currentFlowScreen = AuthFlowScreen.LOGIN }
                        )
                    }
                }
            }
        }
    }
}
