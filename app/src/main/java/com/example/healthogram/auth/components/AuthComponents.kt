package com.example.healthogram.auth.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.auth.AuthValidators
import com.example.healthogram.auth.CountryMetadata
import com.example.healthogram.auth.DeviceSession
import com.example.healthogram.auth.PasswordStrength
import com.example.healthogram.auth.PasswordStrengthLevel
import com.example.healthogram.core.AccountType
import com.example.healthogram.designsystem.HealthogramTheme

/**
 * Standardized Healthogram Auth Text Field.
 */
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true,
    testTag: String = "auth_text_field"
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, style = HealthogramTheme.typography.bodyMedium) },
            placeholder = { if (placeholder.isNotEmpty()) Text(placeholder, style = HealthogramTheme.typography.bodyMedium, color = HealthogramTheme.colors.textMuted) },
            leadingIcon = leadingIcon?.let {
                { Icon(it, contentDescription = null, tint = if (isError) HealthogramTheme.colors.error else HealthogramTheme.colors.primary, modifier = Modifier.size(20.dp)) }
            },
            trailingIcon = trailingIcon,
            isError = isError,
            enabled = enabled,
            singleLine = true,
            shape = HealthogramTheme.shapes.medium,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HealthogramTheme.colors.primary,
                unfocusedBorderColor = HealthogramTheme.colors.border,
                focusedContainerColor = HealthogramTheme.colors.surface,
                unfocusedContainerColor = HealthogramTheme.colors.surface,
                errorBorderColor = HealthogramTheme.colors.error
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )
        if (isError && !errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = HealthogramTheme.typography.bodySmall,
                color = HealthogramTheme.colors.error,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

/**
 * Password Field with show/hide toggle.
 */
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Password",
    modifier: Modifier = Modifier,
    placeholder: String = "Enter your password",
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true,
    testTag: String = "password_field"
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, style = HealthogramTheme.typography.bodyMedium) },
            placeholder = { Text(placeholder, style = HealthogramTheme.typography.bodyMedium, color = HealthogramTheme.colors.textMuted) },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = if (isError) HealthogramTheme.colors.error else HealthogramTheme.colors.primary, modifier = Modifier.size(20.dp))
            },
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                        tint = HealthogramTheme.colors.textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = isError,
            enabled = enabled,
            singleLine = true,
            shape = HealthogramTheme.shapes.medium,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HealthogramTheme.colors.primary,
                unfocusedBorderColor = HealthogramTheme.colors.border,
                focusedContainerColor = HealthogramTheme.colors.surface,
                unfocusedContainerColor = HealthogramTheme.colors.surface,
                errorBorderColor = HealthogramTheme.colors.error
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )
        if (isError && !errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = HealthogramTheme.typography.bodySmall,
                color = HealthogramTheme.colors.error,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

/**
 * International Phone Number Input with Country Code Selector.
 */
@Composable
fun PhoneNumberField(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    selectedCountry: CountryMetadata,
    onCountrySelected: (CountryMetadata) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    testTag: String = "phone_number_field"
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Country dial selector chip
            Surface(
                shape = HealthogramTheme.shapes.medium,
                border = BorderStroke(1.dp, if (isError) HealthogramTheme.colors.error else HealthogramTheme.colors.border),
                color = HealthogramTheme.colors.surface,
                modifier = Modifier
                    .height(56.dp)
                    .clickable { expanded = true }
                    .testTag("country_code_selector")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedCountry.flagEmoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedCountry.phoneDialCode,
                        style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.textPrimary
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Select country",
                        tint = HealthogramTheme.colors.textMuted
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    AuthValidators.SUPPORTED_COUNTRIES.forEach { country ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(country.flagEmoji, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("${country.countryName} (${country.phoneDialCode})")
                                }
                            },
                            onClick = {
                                onCountrySelected(country)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Phone number text field
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { input ->
                    onPhoneNumberChange(input.filter { it.isDigit() })
                },
                label = { Text("Phone Number") },
                placeholder = { Text("1234567890", color = HealthogramTheme.colors.textMuted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = HealthogramTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HealthogramTheme.colors.primary,
                    unfocusedBorderColor = HealthogramTheme.colors.border,
                    focusedContainerColor = HealthogramTheme.colors.surface,
                    unfocusedContainerColor = HealthogramTheme.colors.surface,
                    errorBorderColor = HealthogramTheme.colors.error
                ),
                isError = isError,
                modifier = Modifier
                    .weight(1f)
                    .testTag(testTag)
            )
        }

        if (isError && !errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = HealthogramTheme.typography.bodySmall,
                color = HealthogramTheme.colors.error,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

/**
 * 6-Digit Interactive OTP Code Input.
 */
@Composable
fun OTPInput(
    otpCode: String,
    onOtpChange: (String) -> Unit,
    length: Int = 6,
    modifier: Modifier = Modifier,
    testTag: String = "otp_input"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (i in 0 until length) {
            val char = otpCode.getOrNull(i)?.toString() ?: ""
            val isFocused = otpCode.length == i

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(HealthogramTheme.colors.surface)
                    .border(
                        width = if (isFocused) 2.dp else 1.dp,
                        color = when {
                            isFocused -> HealthogramTheme.colors.primary
                            char.isNotEmpty() -> HealthogramTheme.colors.primaryDark
                            else -> HealthogramTheme.colors.border
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char,
                    style = HealthogramTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = HealthogramTheme.colors.textPrimary
                )
            }
        }
    }
}

/**
 * Password Strength Visual Progress Bar.
 */
@Composable
fun PasswordStrengthIndicator(
    strength: PasswordStrength,
    modifier: Modifier = Modifier
) {
    val barColor = when (strength.level) {
        PasswordStrengthLevel.VERY_WEAK -> HealthogramTheme.colors.error
        PasswordStrengthLevel.WEAK -> Color(0xFFFB8C00)
        PasswordStrengthLevel.FAIR -> HealthogramTheme.colors.warning
        PasswordStrengthLevel.STRONG -> Color(0xFF43A047)
        PasswordStrengthLevel.VERY_STRONG -> HealthogramTheme.colors.success
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Security: ${strength.level.label}",
                style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = barColor
            )
            if (strength.isAcceptable) {
                Text(
                    text = "Meets requirements",
                    style = HealthogramTheme.typography.labelSmall,
                    color = HealthogramTheme.colors.success
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 1..4) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (i <= strength.score) barColor else HealthogramTheme.colors.surfaceVariant)
                )
            }
        }

        if (strength.feedback.isNotEmpty() && !strength.isAcceptable) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "• ${strength.feedback.first()}",
                style = HealthogramTheme.typography.bodySmall,
                color = HealthogramTheme.colors.textMuted
            )
        }
    }
}

/**
 * Primary Call to Action Button with integrated loading spinner.
 */
@Composable
fun PrimaryAuthButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    testTag: String = "primary_auth_button"
) {
    Button(
        onClick = { if (!isLoading && enabled) onClick() },
        enabled = enabled && !isLoading,
        shape = HealthogramTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = HealthogramTheme.colors.primary,
            contentColor = Color.White,
            disabledContainerColor = HealthogramTheme.colors.surfaceVariant,
            disabledContentColor = HealthogramTheme.colors.textMuted
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = HealthogramTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/**
 * Secondary Outlined Button.
 */
@Composable
fun SecondaryAuthButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "secondary_auth_button"
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = HealthogramTheme.shapes.medium,
        border = BorderStroke(1.dp, HealthogramTheme.colors.border),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = HealthogramTheme.colors.textPrimary
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag)
    ) {
        Text(
            text = text,
            style = HealthogramTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
        )
    }
}

/**
 * Social Auth Sign-In Button (Google, Apple).
 */
@Composable
fun SocialLoginButton(
    text: String,
    provider: String, // "GOOGLE" or "APPLE"
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "social_login_button"
) {
    OutlinedButton(
        onClick = onClick,
        shape = HealthogramTheme.shapes.medium,
        border = BorderStroke(1.dp, HealthogramTheme.colors.border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = HealthogramTheme.colors.surface,
            contentColor = HealthogramTheme.colors.textPrimary
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (provider == "APPLE") Icons.Default.PhoneIphone else Icons.Default.AccountCircle,
                contentDescription = provider,
                tint = if (provider == "APPLE") HealthogramTheme.colors.textPrimary else Color(0xFFEA4335),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

/**
 * "OR" Divider between email and social login.
 */
@Composable
fun AuthDivider(text: String = "OR") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = HealthogramTheme.colors.border)
        Text(
            text = text,
            style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = HealthogramTheme.colors.textMuted,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = HealthogramTheme.colors.border)
    }
}

/**
 * Selectable Account Type Card (Exactly 5 categories: Individual, Doctor, Clinic, Hospital, Laboratory - strictly NO Pharmacy).
 */
@Composable
fun AccountTypeSelectionCard(
    accountType: AccountType,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (accountType) {
        AccountType.INDIVIDUAL -> Icons.Default.Person
        AccountType.DOCTOR -> Icons.Default.MedicalServices
        AccountType.CLINIC -> Icons.Default.LocalHospital
        AccountType.HOSPITAL -> Icons.Default.Domain
        AccountType.LABORATORY -> Icons.Default.Biotech
    }

    val description = when (accountType) {
        AccountType.INDIVIDUAL -> "Personal health passport, wellness tracking, creators & patient community"
        AccountType.DOCTOR -> "Verified physicians, clinical consultations, patient care & telehealth"
        AccountType.CLINIC -> "Outpatient centers, specialized practices & group medical care"
        AccountType.HOSPITAL -> "Multi-specialty inpatient medical institutions, emergency & clinical care"
        AccountType.LABORATORY -> "Certified diagnostic pathology & radiology diagnostic centers"
    }

    Card(
        shape = HealthogramTheme.shapes.large,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.border
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) HealthogramTheme.colors.primary.copy(alpha = 0.06f) else HealthogramTheme.colors.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("account_type_card_${accountType.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else HealthogramTheme.colors.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = accountType.displayName,
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.textPrimary
                    )
                    if (accountType.isHealthcareOrganization) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = HealthogramTheme.colors.surfaceVariant
                        ) {
                            Text(
                                text = "ORGANIZATION",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = HealthogramTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                                color = HealthogramTheme.colors.textSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = HealthogramTheme.typography.bodySmall,
                    color = HealthogramTheme.colors.textSecondary
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = { onSelect() },
                colors = RadioButtonDefaults.colors(selectedColor = HealthogramTheme.colors.primary)
            )
        }
    }
}

/**
 * User Friendly Error Message Banner.
 */
@Composable
fun AuthErrorMessage(message: String, modifier: Modifier = Modifier) {
    Surface(
        shape = HealthogramTheme.shapes.medium,
        color = HealthogramTheme.colors.error.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, HealthogramTheme.colors.error.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = HealthogramTheme.colors.error, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = HealthogramTheme.colors.error
            )
        }
    }
}

/**
 * Success Message Banner.
 */
@Composable
fun SuccessMessage(message: String, modifier: Modifier = Modifier) {
    Surface(
        shape = HealthogramTheme.shapes.medium,
        color = HealthogramTheme.colors.success.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, HealthogramTheme.colors.success.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = HealthogramTheme.colors.success, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = HealthogramTheme.colors.success
            )
        }
    }
}

/**
 * Device Session List Item with Revoke Action.
 */
@Composable
fun DeviceListItem(
    session: DeviceSession,
    onRevoke: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = HealthogramTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, HealthogramTheme.colors.border),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (session.isCurrentDevice) HealthogramTheme.colors.primary.copy(alpha = 0.12f) else HealthogramTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (session.platform.equals("iOS", true)) Icons.Default.PhoneIphone else Icons.Default.Smartphone,
                        contentDescription = null,
                        tint = if (session.isCurrentDevice) HealthogramTheme.colors.primary else HealthogramTheme.colors.textMuted
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = session.deviceName,
                            style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.textPrimary
                        )
                        if (session.isCurrentDevice) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = HealthogramTheme.colors.success.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "THIS DEVICE",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = HealthogramTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                                    color = HealthogramTheme.colors.success
                                )
                            }
                        }
                    }
                    Text(
                        text = "Platform: ${session.platform} • Active session",
                        style = HealthogramTheme.typography.bodySmall,
                        color = HealthogramTheme.colors.textSecondary
                    )
                }
            }

            if (!session.isCurrentDevice) {
                OutlinedButton(
                    onClick = onRevoke,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HealthogramTheme.colors.error),
                    border = BorderStroke(1.dp, HealthogramTheme.colors.error.copy(alpha = 0.5f)),
                    shape = HealthogramTheme.shapes.small,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("revoke_device_${session.deviceId}")
                ) {
                    Text("Revoke", style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

/**
 * Security Option Tile for Security Settings.
 */
@Composable
fun SecurityOptionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    isDanger: Boolean = false
) {
    Card(
        shape = HealthogramTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, HealthogramTheme.colors.border),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isDanger) HealthogramTheme.colors.error.copy(alpha = 0.1f) else HealthogramTheme.colors.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDanger) HealthogramTheme.colors.error else HealthogramTheme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isDanger) HealthogramTheme.colors.error else HealthogramTheme.colors.textPrimary
                    )
                    if (badgeText != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = HealthogramTheme.colors.surfaceVariant
                        ) {
                            Text(
                                text = badgeText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = HealthogramTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = HealthogramTheme.colors.textMuted
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = HealthogramTheme.typography.bodySmall,
                    color = HealthogramTheme.colors.textSecondary
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = HealthogramTheme.colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
