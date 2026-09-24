package com.example.healthogram.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme

enum class BadgeSize(val iconSize: Dp, val checkSize: Dp) {
    SMALL(16.dp, 10.dp),
    MEDIUM(20.dp, 12.dp),
    LARGE(24.dp, 16.dp)
}

/**
 * Healthogram Verified Badge Component.
 * Supports verified, pending, and rejected states with accessibility and consistent styling.
 */
@Composable
fun HealthogramVerifiedBadge(
    status: VerificationStatus,
    modifier: Modifier = Modifier,
    size: BadgeSize = BadgeSize.MEDIUM,
    showLabel: Boolean = false,
    testTag: String = "verified_badge"
) {
    if (status == VerificationStatus.NOT_STARTED) return

    Row(
        modifier = modifier.testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (status) {
            VerificationStatus.VERIFIED,
            VerificationStatus.APPROVED -> {
                Box(
                    modifier = Modifier
                        .size(size.iconSize)
                        .clip(CircleShape)
                        .background(HealthogramTheme.colors.verifiedBadgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Verified Account",
                        tint = Color.White,
                        modifier = Modifier.size(size.checkSize)
                    )
                }
                if (showLabel) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Verified",
                        style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.primary
                    )
                }
            }

            VerificationStatus.SUBMITTED,
            VerificationStatus.DRAFT,
            VerificationStatus.UNDER_REVIEW,
            VerificationStatus.ADDITIONAL_INFORMATION_REQUIRED -> {
                Box(
                    modifier = Modifier
                        .size(size.iconSize)
                        .clip(CircleShape)
                        .background(HealthogramTheme.colors.warning),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = "Verification Under Review",
                        tint = Color.White,
                        modifier = Modifier.size(size.checkSize)
                    )
                }
                if (showLabel) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (status == VerificationStatus.ADDITIONAL_INFORMATION_REQUIRED) "Info Required" else "Under Review",
                        style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.warning
                    )
                }
            }

            VerificationStatus.REJECTED,
            VerificationStatus.SUSPENDED,
            VerificationStatus.EXPIRED,
            VerificationStatus.REVOKED -> {
                Box(
                    modifier = Modifier
                        .size(size.iconSize)
                        .clip(CircleShape)
                        .background(HealthogramTheme.colors.error),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Verification Expired, Suspended, or Rejected",
                        tint = Color.White,
                        modifier = Modifier.size(size.checkSize)
                    )
                }
                if (showLabel) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Action Needed",
                        style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.error
                    )
                }
            }

            VerificationStatus.NOT_STARTED -> {
                // Do not display badge if verification is not started
            }
        }
    }
}
