package com.example.healthogram.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.designsystem.HealthogramTheme

enum class ButtonState {
    NORMAL,
    DISABLED,
    LOADING,
    SUCCESS
}

enum class ButtonSize(
    val height: Dp,
    val horizontalPadding: Dp,
    val iconSize: Dp,
    val fontSize: Float
) {
    SMALL(36.dp, 12.dp, 16.dp, 12f),
    MEDIUM(46.dp, 16.dp, 18.dp, 14f),
    LARGE(54.dp, 24.dp, 22.dp, 16f)
}

/**
 * Master Primary Button
 */
@Composable
fun HealthogramPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: ButtonState = ButtonState.NORMAL,
    size: ButtonSize = ButtonSize.MEDIUM,
    icon: ImageVector? = null,
    shape: Shape = HealthogramTheme.shapes.medium,
    testTag: String = "primary_button"
) {
    val isInteractive = state == ButtonState.NORMAL
    val bgColor = when (state) {
        ButtonState.SUCCESS -> HealthogramTheme.colors.success
        ButtonState.DISABLED -> HealthogramTheme.colors.surfaceVariant
        else -> HealthogramTheme.colors.primary
    }

    Button(
        onClick = onClick,
        enabled = isInteractive,
        modifier = modifier
            .height(size.height)
            .testTag(testTag),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = Color.White,
            disabledContainerColor = HealthogramTheme.colors.surfaceVariant,
            disabledContentColor = HealthogramTheme.colors.textMuted
        ),
        contentPadding = PaddingValues(horizontal = size.horizontalPadding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (state == ButtonState.LOADING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(size.iconSize),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(HealthogramTheme.spacing.sm))
            } else if (state == ButtonState.SUCCESS) {
                Icon(Icons.Default.Check, contentDescription = "Success", modifier = Modifier.size(size.iconSize))
                Spacer(modifier = Modifier.width(HealthogramTheme.spacing.sm))
            } else if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(size.iconSize))
                Spacer(modifier = Modifier.width(HealthogramTheme.spacing.sm))
            }

            Text(
                text = if (state == ButtonState.SUCCESS) "Done" else text,
                style = HealthogramTheme.typography.buttonText.copy(fontSize = size.fontSize.sp)
            )
        }
    }
}

/**
 * Secondary Button (Filled with light container or secondary brand color)
 */
@Composable
fun HealthogramSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: ButtonState = ButtonState.NORMAL,
    size: ButtonSize = ButtonSize.MEDIUM,
    icon: ImageVector? = null,
    shape: Shape = HealthogramTheme.shapes.medium,
    testTag: String = "secondary_button"
) {
    Button(
        onClick = onClick,
        enabled = state == ButtonState.NORMAL,
        modifier = modifier
            .height(size.height)
            .testTag(testTag),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = HealthogramTheme.colors.secondary,
            contentColor = Color.White,
            disabledContainerColor = HealthogramTheme.colors.surfaceVariant,
            disabledContentColor = HealthogramTheme.colors.textMuted
        ),
        contentPadding = PaddingValues(horizontal = size.horizontalPadding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(size.iconSize))
                Spacer(modifier = Modifier.width(HealthogramTheme.spacing.sm))
            }
            Text(
                text = text,
                style = HealthogramTheme.typography.buttonText.copy(fontSize = size.fontSize.sp)
            )
        }
    }
}

/**
 * Outline Button
 */
@Composable
fun HealthogramOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: ButtonState = ButtonState.NORMAL,
    size: ButtonSize = ButtonSize.MEDIUM,
    icon: ImageVector? = null,
    borderColor: Color = HealthogramTheme.colors.border,
    contentColor: Color = HealthogramTheme.colors.textPrimary,
    shape: Shape = HealthogramTheme.shapes.medium,
    testTag: String = "outline_button"
) {
    OutlinedButton(
        onClick = onClick,
        enabled = state == ButtonState.NORMAL,
        modifier = modifier
            .height(size.height)
            .testTag(testTag),
        shape = shape,
        border = BorderStroke(1.dp, if (state == ButtonState.DISABLED) HealthogramTheme.colors.borderLight else borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor,
            disabledContentColor = HealthogramTheme.colors.textMuted
        ),
        contentPadding = PaddingValues(horizontal = size.horizontalPadding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(size.iconSize))
                Spacer(modifier = Modifier.width(HealthogramTheme.spacing.sm))
            }
            Text(
                text = text,
                style = HealthogramTheme.typography.buttonText.copy(fontSize = size.fontSize.sp)
            )
        }
    }
}

/**
 * Text Action Button
 */
@Composable
fun HealthogramTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = HealthogramTheme.colors.primary,
    testTag: String = "text_button"
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.testTag(testTag)
    ) {
        Text(
            text = text,
            style = HealthogramTheme.typography.buttonText,
            color = color
        )
    }
}

/**
 * Danger / Destructive Button
 */
@Composable
fun HealthogramDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = Icons.Default.Delete,
    testTag: String = "danger_button"
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp).testTag(testTag),
        shape = HealthogramTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = HealthogramTheme.colors.error,
            contentColor = Color.White
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(HealthogramTheme.spacing.sm))
        }
        Text(text, style = HealthogramTheme.typography.buttonText)
    }
}

/**
 * Follow / Following Adaptive Button (Social Instagram UX standard)
 */
@Composable
fun HealthogramFollowButton(
    isFollowing: Boolean,
    onToggleFollow: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "follow_toggle_button"
) {
    if (isFollowing) {
        OutlinedButton(
            onClick = onToggleFollow,
            modifier = modifier.height(34.dp).testTag(testTag),
            shape = HealthogramTheme.shapes.medium,
            border = BorderStroke(1.dp, HealthogramTheme.colors.border),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = HealthogramTheme.colors.textPrimary
            ),
            contentPadding = PaddingValues(horizontal = 14.dp)
        ) {
            Text(
                text = "Following",
                style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    } else {
        Button(
            onClick = onToggleFollow,
            modifier = modifier.height(34.dp).testTag(testTag),
            shape = HealthogramTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = HealthogramTheme.colors.primary,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Text(
                text = "Follow",
                style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

/**
 * Book Appointment Button
 */
@Composable
fun HealthogramBookAppointmentButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "book_appointment_button"
) {
    HealthogramPrimaryButton(
        text = "Book Appointment",
        onClick = onClick,
        icon = Icons.Default.CalendarMonth,
        modifier = modifier,
        testTag = testTag
    )
}

/**
 * Scan QR Code Button
 */
@Composable
fun HealthogramScanQRButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "scan_qr_button"
) {
    HealthogramSecondaryButton(
        text = "Scan Health Passport",
        onClick = onClick,
        icon = Icons.Default.QrCodeScanner,
        modifier = modifier,
        testTag = testTag
    )
}

/**
 * Marketplace E-Commerce Buttons
 */
@Composable
fun HealthogramAddToCartButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "add_to_cart_button"
) {
    HealthogramOutlineButton(
        text = "Add to Cart",
        onClick = onClick,
        icon = Icons.Default.AddShoppingCart,
        borderColor = HealthogramTheme.colors.primary,
        contentColor = HealthogramTheme.colors.primary,
        modifier = modifier,
        testTag = testTag
    )
}

@Composable
fun HealthogramBuyNowButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "buy_now_button"
) {
    HealthogramPrimaryButton(
        text = "Buy Now",
        onClick = onClick,
        icon = Icons.Default.ShoppingBag,
        modifier = modifier,
        testTag = testTag
    )
}

@Composable
fun HealthogramCheckoutButton(
    amountText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "checkout_button"
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp).testTag(testTag),
        shape = HealthogramTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = HealthogramTheme.colors.primary,
            contentColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Proceed to Checkout", style = HealthogramTheme.typography.buttonText)
            Text(amountText, style = HealthogramTheme.typography.titleMedium.copy(color = Color.White))
        }
    }
}
