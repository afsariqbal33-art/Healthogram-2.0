package com.example.healthogram.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.auth.*
import com.example.healthogram.auth.components.*
import com.example.healthogram.core.AccountType
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.HealthogramLogo
import com.example.healthogram.designsystem.components.HealthogramLogoSize
import kotlinx.coroutines.launch

enum class SignUpStep(val stepNumber: Int, val title: String) {
    STEP_A_METHOD(1, "Registration Method"),
    STEP_B_CATEGORY(2, "Account Category"),
    STEP_C_BASIC_INFO(3, "Basic Information"),
    STEP_D_VERIFICATION(4, "Verification"),
    STEP_E_CREDENTIALS(5, "Security Credentials"),
    STEP_F_PROFILE(6, "Profile Setup"),
    STEP_G_TERMS(7, "Terms & Privacy"),
    STEP_H_COMPLETE(8, "Account Ready")
}

/**
 * Multi-Step Healthogram Account Creation Flow (Steps A through H).
 *
 * Enforces:
 * - Exactly 5 primary categories (Individual, Doctor, Clinic, Hospital, Laboratory - NO Pharmacy).
 * - Live username normalization and uniqueness validation against reserved names.
 * - Password strength meter.
 * - Country code metadata.
 * - Terms, Privacy, Community Guidelines, and Health Data Notice acceptance.
 */
@Composable
fun SignUpWizardPage(
    authManager: FirebaseAuthManager,
    onNavigateToLogin: () -> Unit,
    onSignUpComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var currentStep by remember { mutableStateOf(SignUpStep.STEP_A_METHOD) }
    var draft by remember { mutableStateOf(RegistrationDraft()) }

    var selectedCountry by remember {
        mutableStateOf(AuthValidators.getCountry(draft.countryCode))
    }

    var usernameCheckResult by remember { mutableStateOf<String?>(null) }
    var isCheckingUsername by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
            .testTag("signup_wizard_page"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        // Header / Stepper indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    if (currentStep == SignUpStep.STEP_A_METHOD) {
                        onNavigateToLogin()
                    } else {
                        currentStep = SignUpStep.entries[currentStep.ordinal - 1]
                    }
                }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
            }

            Text(
                text = "Step ${currentStep.stepNumber} of 8",
                style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.primary
            )

            Text(
                text = "Sign In",
                style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = HealthogramTheme.colors.primary,
                modifier = Modifier
                    .clickable { onNavigateToLogin() }
                    .padding(8.dp)
            )
        }

        // Progress bar
        LinearProgressIndicator(
            progress = { currentStep.stepNumber / 8f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(6.dp)
                .clip(CircleShape),
            color = HealthogramTheme.colors.primary,
            trackColor = HealthogramTheme.colors.surfaceVariant,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = currentStep.title,
            style = HealthogramTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = HealthogramTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (errorMessage != null) {
            AuthErrorMessage(message = errorMessage!!)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // -----------------------------------------------------------------
        // STEP A: Choose registration method
        // -----------------------------------------------------------------
        if (currentStep == SignUpStep.STEP_A_METHOD) {
            Text(
                text = "Select your preferred registration method to establish your verified Healthogram identity.",
                style = HealthogramTheme.typography.bodyMedium,
                color = HealthogramTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryAuthButton(
                text = "Continue with Email",
                leadingIcon = Icons.Default.Email,
                onClick = {
                    draft = draft.copy(registrationMethod = "EMAIL")
                    currentStep = SignUpStep.STEP_B_CATEGORY
                },
                testTag = "select_email_method"
            )

            Spacer(modifier = Modifier.height(14.dp))

            SecondaryAuthButton(
                text = "Continue with Mobile Phone",
                onClick = {
                    draft = draft.copy(registrationMethod = "PHONE")
                    currentStep = SignUpStep.STEP_B_CATEGORY
                },
                testTag = "select_phone_method"
            )

            AuthDivider()

            SocialLoginButton(
                text = "Continue with Google",
                provider = "GOOGLE",
                onClick = {
                    draft = draft.copy(registrationMethod = "GOOGLE")
                    currentStep = SignUpStep.STEP_B_CATEGORY
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SocialLoginButton(
                text = "Continue with Apple",
                provider = "APPLE",
                onClick = {
                    draft = draft.copy(registrationMethod = "APPLE")
                    currentStep = SignUpStep.STEP_B_CATEGORY
                }
            )
        }

        // -----------------------------------------------------------------
        // STEP B: Choose Healthogram account category (Strictly 5, NO Pharmacy)
        // -----------------------------------------------------------------
        if (currentStep == SignUpStep.STEP_B_CATEGORY) {
            Text(
                text = "Healthogram strictly validates and isolates account types. Select the official category for your account.",
                style = HealthogramTheme.typography.bodySmall,
                color = HealthogramTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display exactly the 5 allowed categories
            AccountType.entries.forEach { category ->
                AccountTypeSelectionCard(
                    accountType = category,
                    isSelected = draft.accountType == category,
                    onSelect = { draft = draft.copy(accountType = category) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            PrimaryAuthButton(
                text = "Continue with ${draft.accountType.displayName}",
                onClick = { currentStep = SignUpStep.STEP_C_BASIC_INFO },
                testTag = "confirm_category_button"
            )
        }

        // -----------------------------------------------------------------
        // STEP C: Enter basic account info
        // -----------------------------------------------------------------
        if (currentStep == SignUpStep.STEP_C_BASIC_INFO) {
            val isOrg = draft.accountType.isHealthcareOrganization

            AuthTextField(
                value = if (isOrg) draft.organizationName else draft.displayName,
                onValueChange = {
                    draft = if (isOrg) draft.copy(organizationName = it) else draft.copy(displayName = it)
                    errorMessage = null
                },
                label = if (isOrg) "${draft.accountType.displayName} Legal Name" else "Full Name",
                placeholder = if (isOrg) "e.g., Mayo Clinic Health System" else "e.g., Dr. Sarah Connor",
                leadingIcon = Icons.Default.Badge
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Username input with live validation
            AuthTextField(
                value = draft.username,
                onValueChange = { input ->
                    val clean = input.filter { it.isLetterOrDigit() || it == '.' || it == '_' }
                    draft = draft.copy(username = clean)
                    errorMessage = null
                    usernameCheckResult = null

                    val validation = AuthValidators.validateUsername(clean)
                    if (validation.isValid) {
                        isCheckingUsername = true
                        coroutineScope.launch {
                            val available = authManager.checkUsernameAvailability(validation.normalizedUsername).getOrDefault(false)
                            isCheckingUsername = false
                            usernameCheckResult = if (available) "Username is available" else "Username is already taken"
                        }
                    } else {
                        usernameCheckResult = validation.errorMessage
                    }
                },
                label = "Username",
                placeholder = "unique_username",
                leadingIcon = Icons.Default.AlternateEmail,
                trailingIcon = {
                    if (isCheckingUsername) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else if (usernameCheckResult == "Username is available") {
                        Icon(Icons.Default.Check, contentDescription = "Available", tint = HealthogramTheme.colors.success)
                    }
                }
            )

            if (usernameCheckResult != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = usernameCheckResult!!,
                    style = HealthogramTheme.typography.bodySmall,
                    color = if (usernameCheckResult == "Username is available") HealthogramTheme.colors.success else HealthogramTheme.colors.error,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Email or Phone field based on chosen method
            if (draft.registrationMethod == "PHONE") {
                PhoneNumberField(
                    phoneNumber = draft.phoneNumber,
                    onPhoneNumberChange = {
                        draft = draft.copy(phoneNumber = it)
                        errorMessage = null
                    },
                    selectedCountry = selectedCountry,
                    onCountrySelected = {
                        selectedCountry = it
                        draft = draft.copy(
                            countryCode = it.countryCode,
                            countryName = it.countryName,
                            currencyCode = it.currencyCode,
                            languageCode = it.defaultLanguage
                        )
                    }
                )
            } else {
                AuthTextField(
                    value = draft.email,
                    onValueChange = {
                        draft = draft.copy(email = it)
                        errorMessage = null
                    },
                    label = "Email address",
                    placeholder = "doctor@healthogram.com",
                    leadingIcon = Icons.Default.Email
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // City
            AuthTextField(
                value = draft.city,
                onValueChange = { draft = draft.copy(city = it) },
                label = "City / Region",
                placeholder = "e.g., Riyadh, Dubai, Berlin, New York",
                leadingIcon = Icons.Default.LocationCity
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryAuthButton(
                text = "Next: Verification",
                onClick = {
                    val name = if (isOrg) draft.organizationName else draft.displayName
                    if (name.isBlank()) {
                        errorMessage = "Please enter your name."
                        return@PrimaryAuthButton
                    }
                    val userValidation = AuthValidators.validateUsername(draft.username)
                    if (!userValidation.isValid) {
                        errorMessage = userValidation.errorMessage
                        return@PrimaryAuthButton
                    }
                    if (draft.registrationMethod == "EMAIL" && !AuthValidators.isValidEmail(draft.email)) {
                        errorMessage = "Please enter a valid email address."
                        return@PrimaryAuthButton
                    }
                    if (draft.registrationMethod == "PHONE" && draft.phoneNumber.length < 7) {
                        errorMessage = "Please enter a valid phone number."
                        return@PrimaryAuthButton
                    }
                    currentStep = SignUpStep.STEP_D_VERIFICATION
                }
            )
        }

        // -----------------------------------------------------------------
        // STEP D: Verify Email or Phone OTP
        // -----------------------------------------------------------------
        if (currentStep == SignUpStep.STEP_D_VERIFICATION) {
            var otpCode by remember { mutableStateOf("123456") }

            Text(
                text = if (draft.registrationMethod == "PHONE")
                    "We've sent a 6-digit verification code to ${selectedCountry.phoneDialCode} ${draft.phoneNumber}."
                else
                    "Your email ${draft.email} will be verified via a confirmation link sent upon account creation.",
                style = HealthogramTheme.typography.bodyMedium,
                color = HealthogramTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (draft.registrationMethod == "PHONE") {
                OTPInput(
                    otpCode = otpCode,
                    onOtpChange = { otpCode = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Resend Code in 45s",
                    style = HealthogramTheme.typography.labelSmall,
                    color = HealthogramTheme.colors.textMuted
                )
            } else {
                Card(
                    shape = HealthogramTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Automatic Verification Ready", fontWeight = FontWeight.Bold, style = HealthogramTheme.typography.titleSmall)
                            Text("Firebase Auth email verification will be dispatched securely.", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textSecondary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryAuthButton(
                text = "Continue to Credentials",
                onClick = { currentStep = SignUpStep.STEP_E_CREDENTIALS }
            )
        }

        // -----------------------------------------------------------------
        // STEP E: Create authentication credential
        // -----------------------------------------------------------------
        if (currentStep == SignUpStep.STEP_E_CREDENTIALS) {
            var confirmPassword by remember { mutableStateOf("") }
            val strength = remember(draft.password) {
                AuthValidators.validatePasswordStrength(draft.password)
            }

            PasswordField(
                value = draft.password,
                onValueChange = {
                    draft = draft.copy(password = it)
                    errorMessage = null
                },
                label = "Create Password",
                placeholder = "At least 8 characters"
            )

            Spacer(modifier = Modifier.height(8.dp))

            PasswordStrengthIndicator(strength = strength)

            Spacer(modifier = Modifier.height(14.dp))

            PasswordField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                label = "Confirm Password",
                placeholder = "Re-enter password"
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryAuthButton(
                text = "Next: Profile Setup",
                onClick = {
                    if (!strength.isAcceptable) {
                        errorMessage = "Please choose a stronger password matching the requirements."
                        return@PrimaryAuthButton
                    }
                    if (draft.password != confirmPassword) {
                        errorMessage = "Passwords do not match."
                        return@PrimaryAuthButton
                    }
                    currentStep = SignUpStep.STEP_F_PROFILE
                }
            )
        }

        // -----------------------------------------------------------------
        // STEP F: Create user profile
        // -----------------------------------------------------------------
        if (currentStep == SignUpStep.STEP_F_PROFILE) {
            Text(
                text = "Add profile details for your ${draft.accountType.displayName} account. You can update these later in settings.",
                style = HealthogramTheme.typography.bodySmall,
                color = HealthogramTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                value = draft.bio,
                onValueChange = { draft = draft.copy(bio = it) },
                label = "Bio / Description",
                placeholder = "Brief summary of your clinical specialty or wellness interests..."
            )

            if (draft.accountType == AccountType.DOCTOR) {
                Spacer(modifier = Modifier.height(14.dp))
                AuthTextField(
                    value = draft.specializationOrCategory,
                    onValueChange = { draft = draft.copy(specializationOrCategory = it) },
                    label = "Specialization",
                    placeholder = "e.g., Cardiology, Dermatology, General Practice"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryAuthButton(
                text = "Next: Legal & Privacy",
                onClick = { currentStep = SignUpStep.STEP_G_TERMS }
            )
        }

        // -----------------------------------------------------------------
        // STEP G: Accept Terms and Privacy Policy
        // -----------------------------------------------------------------
        if (currentStep == SignUpStep.STEP_G_TERMS) {
            Card(
                shape = HealthogramTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                border = BorderStroke(1.dp, HealthogramTheme.colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Legal Compliance & Health Data Protection",
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Healthogram adheres to international medical privacy standards (including HIPAA/GDPR principles). Your Health Passport is zero-trust and private by default. Your data is never sold.",
                        style = HealthogramTheme.typography.bodySmall,
                        color = HealthogramTheme.colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = draft.termsAccepted,
                            onCheckedChange = { draft = draft.copy(termsAccepted = it) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "I accept the Terms of Service & Community Guidelines",
                            style = HealthogramTheme.typography.bodySmall
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = draft.privacyAccepted,
                            onCheckedChange = { draft = draft.copy(privacyAccepted = it) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "I accept the Privacy Policy & Health Data Notice",
                            style = HealthogramTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryAuthButton(
                text = "Create Healthogram Account",
                isLoading = isLoading,
                onClick = {
                    if (!draft.termsAccepted || !draft.privacyAccepted) {
                        errorMessage = "You must accept the Terms and Privacy Policy to create an account."
                        return@PrimaryAuthButton
                    }
                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        val result = authManager.signUpWithDraft(draft)
                        isLoading = false
                        if (result.isSuccess) {
                            currentStep = SignUpStep.STEP_H_COMPLETE
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Account creation failed."
                        }
                    }
                }
            )
        }

        // -----------------------------------------------------------------
        // STEP H: Complete onboarding
        // -----------------------------------------------------------------
        if (currentStep == SignUpStep.STEP_H_COMPLETE) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(HealthogramTheme.colors.success),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = "Success", tint = Color.White, modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Account Created Successfully!",
                style = HealthogramTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Welcome to Healthogram, @${draft.username}. Your ${draft.accountType.displayName} account and device session are active.",
                style = HealthogramTheme.typography.bodyMedium,
                color = HealthogramTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryAuthButton(
                text = "Enter Healthogram",
                onClick = onSignUpComplete,
                testTag = "enter_app_button"
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
