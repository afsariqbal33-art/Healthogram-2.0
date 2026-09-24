package com.example.healthogram.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.designsystem.HealthogramTheme

enum class InputState {
    NORMAL,
    ERROR,
    SUCCESS,
    DISABLED
}

/**
 * Master Outlined Input Field for Healthogram
 */
@Composable
fun HealthogramTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    errorMessage: String? = null,
    successMessage: String? = null,
    inputState: InputState = if (errorMessage != null) InputState.ERROR else if (successMessage != null) InputState.SUCCESS else InputState.NORMAL,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    testTag: String = "healthogram_text_field"
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (inputState == InputState.ERROR) HealthogramTheme.colors.error else HealthogramTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(HealthogramTheme.spacing.xs))
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            placeholder = placeholder?.let {
                { Text(it, style = HealthogramTheme.typography.bodyMedium, color = HealthogramTheme.colors.textMuted) }
            },
            leadingIcon = leadingIcon?.let {
                { Icon(it, contentDescription = null, tint = HealthogramTheme.colors.textMuted, modifier = Modifier.size(20.dp)) }
            },
            trailingIcon = trailingIcon,
            singleLine = singleLine,
            maxLines = maxLines,
            enabled = inputState != InputState.DISABLED,
            isError = inputState == InputState.ERROR,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = HealthogramTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (inputState == InputState.SUCCESS) HealthogramTheme.colors.success else HealthogramTheme.colors.primary,
                unfocusedBorderColor = if (inputState == InputState.SUCCESS) HealthogramTheme.colors.success else HealthogramTheme.colors.border,
                errorBorderColor = HealthogramTheme.colors.error,
                focusedContainerColor = HealthogramTheme.colors.surface,
                unfocusedContainerColor = HealthogramTheme.colors.surface,
                focusedTextColor = HealthogramTheme.colors.textPrimary,
                unfocusedTextColor = HealthogramTheme.colors.textPrimary
            )
        )

        if (errorMessage != null && inputState == InputState.ERROR) {
            Spacer(modifier = Modifier.height(HealthogramTheme.spacing.xxs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = HealthogramTheme.colors.error, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = errorMessage,
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.error
                )
            }
        } else if (successMessage != null && inputState == InputState.SUCCESS) {
            Spacer(modifier = Modifier.height(HealthogramTheme.spacing.xxs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HealthogramTheme.colors.success, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = successMessage,
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.success
                )
            }
        }
    }
}

/**
 * Modern Search Bar Input
 */
@Composable
fun HealthogramSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search doctors, clinics, products, topics...",
    onSearch: () -> Unit = {},
    onClear: () -> Unit = { onQueryChange("") },
    testTag: String = "search_field"
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag(testTag),
        shape = HealthogramTheme.shapes.pill,
        color = HealthogramTheme.colors.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = HealthogramTheme.colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = HealthogramTheme.typography.bodyMedium,
                        color = HealthogramTheme.colors.textMuted
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = HealthogramTheme.typography.bodyMedium.copy(color = HealthogramTheme.colors.textPrimary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() })
                )
            }
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = HealthogramTheme.colors.textMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * Password Input with show/hide toggle
 */
@Composable
fun HealthogramPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Password",
    errorMessage: String? = null,
    testTag: String = "password_field"
) {
    var passwordVisible by remember { mutableStateOf(false) }

    HealthogramTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = "••••••••",
        leadingIcon = Icons.Default.Lock,
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    tint = HealthogramTheme.colors.textMuted
                )
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        errorMessage = errorMessage,
        testTag = testTag
    )
}

/**
 * Country Selector Dropdown
 */
@Composable
fun HealthogramCountrySelector(
    selectedCountry: String,
    onCountrySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    availableCountries: List<Pair<String, String>> = listOf(
        "US" to "United States (+1)",
        "GB" to "United Kingdom (+44)",
        "AE" to "United Arab Emirates (+971)",
        "SA" to "Saudi Arabia (+966)",
        "IN" to "India (+91)",
        "GLOBAL" to "International / Other"
    ),
    testTag: String = "country_selector"
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = "Country / Region",
            style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = HealthogramTheme.colors.textPrimary
        )
        Spacer(modifier = Modifier.height(HealthogramTheme.spacing.xs))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clickable { expanded = true }
                .testTag(testTag),
            shape = HealthogramTheme.shapes.medium,
            color = HealthogramTheme.colors.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, HealthogramTheme.colors.border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Public, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = availableCountries.find { it.first == selectedCountry }?.second ?: selectedCountry,
                        style = HealthogramTheme.typography.bodyMedium
                    )
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = HealthogramTheme.colors.textMuted)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableCountries.forEach { (code, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            onCountrySelected(code)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * 6-Digit Verification OTP Field
 */
@Composable
fun HealthogramOTPField(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    testTag: String = "otp_input_field"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 0 until length) {
            val char = if (i < otpValue.length) otpValue[i].toString() else ""
            val isFocused = i == otpValue.length

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(HealthogramTheme.shapes.medium)
                    .border(
                        width = if (isFocused) 2.dp else 1.dp,
                        color = if (isFocused) HealthogramTheme.colors.primary else HealthogramTheme.colors.border,
                        shape = HealthogramTheme.shapes.medium
                    )
                    .background(HealthogramTheme.colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char,
                    style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.textPrimary
                )
            }
        }
    }
}
