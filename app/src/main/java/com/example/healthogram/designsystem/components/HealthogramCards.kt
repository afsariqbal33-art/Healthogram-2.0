package com.example.healthogram.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.core.AccountType
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.marketplace.MarketplaceProduct

/**
 * Reusable Foundation Card for Healthogram
 */
@Composable
fun HealthogramBasicCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: androidx.compose.ui.unit.Dp = HealthogramTheme.elevation.small,
    border: BorderStroke? = BorderStroke(1.dp, HealthogramTheme.colors.border),
    containerColor: Color = HealthogramTheme.colors.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = HealthogramTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = border
    ) {
        Column(
            modifier = Modifier.padding(HealthogramTheme.spacing.cardPadding),
            content = content
        )
    }
}

/**
 * User / Creator Profile Card
 */
@Composable
fun HealthogramUserCard(
    displayName: String,
    username: String,
    accountType: AccountType,
    verificationStatus: VerificationStatus,
    subtitle: String? = null,
    isFollowing: Boolean = false,
    onFollowToggle: () -> Unit = {},
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    testTag: String = "user_card"
) {
    HealthogramBasicCard(
        modifier = modifier.testTag(testTag),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Profile Avatar Initial or Icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(HealthogramTheme.colors.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.take(1).uppercase(),
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(HealthogramTheme.spacing.md))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displayName,
                            style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        HealthogramVerifiedBadge(status = verificationStatus, size = BadgeSize.SMALL)
                    }

                    Text(
                        text = "@$username • ${accountType.displayName}",
                        style = HealthogramTheme.typography.bodySmall,
                        color = HealthogramTheme.colors.textMuted
                    )

                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = HealthogramTheme.typography.caption,
                            color = HealthogramTheme.colors.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(HealthogramTheme.spacing.sm))
            HealthogramFollowButton(
                isFollowing = isFollowing,
                onToggleFollow = onFollowToggle
            )
        }
    }
}

/**
 * Healthcare Doctor Card
 */
@Composable
fun HealthogramDoctorCard(
    doctorName: String,
    specialization: String,
    hospitalOrClinic: String,
    rating: Float = 4.9f,
    fee: Double = 60.0,
    currency: String = "USD",
    isVerified: Boolean = true,
    onBookClick: () -> Unit = {},
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    testTag: String = "doctor_card"
) {
    HealthogramBasicCard(
        modifier = modifier.testTag(testTag),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(HealthogramTheme.colors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = HealthogramTheme.colors.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(HealthogramTheme.spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = doctorName,
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        HealthogramVerifiedBadge(status = VerificationStatus.APPROVED, size = BadgeSize.SMALL)
                    }
                }

                Text(
                    text = specialization,
                    style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = HealthogramTheme.colors.primary
                )

                Text(
                    text = hospitalOrClinic,
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(HealthogramTheme.spacing.md))
        HorizontalDivider(color = HealthogramTheme.colors.borderLight)
        Spacer(modifier = Modifier.height(HealthogramTheme.spacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Consultation Fee",
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.textMuted
                )
                Text(
                    text = "$currency $fee",
                    style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.textPrimary
                )
            }

            HealthogramPrimaryButton(
                text = "Book",
                onClick = onBookClick,
                size = ButtonSize.SMALL,
                icon = Icons.Default.CalendarToday
            )
        }
    }
}

/**
 * Healthcare Facility Card (Clinic, Hospital, Laboratory)
 */
@Composable
fun HealthogramFacilityCard(
    facilityName: String,
    accountType: AccountType,
    address: String,
    highlightMetric: String,
    isVerified: Boolean = true,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    testTag: String = "facility_card"
) {
    HealthogramBasicCard(
        modifier = modifier.testTag(testTag),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (accountType) {
                            AccountType.HOSPITAL -> HealthogramTheme.colors.primaryContainer
                            AccountType.CLINIC -> HealthogramTheme.colors.secondaryContainer
                            AccountType.LABORATORY -> HealthogramTheme.colors.infoContainer
                            else -> HealthogramTheme.colors.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (accountType) {
                        AccountType.HOSPITAL -> Icons.Default.LocalHospital
                        AccountType.CLINIC -> Icons.Default.Apartment
                        AccountType.LABORATORY -> Icons.Default.Biotech
                        else -> Icons.Default.Domain
                    },
                    contentDescription = null,
                    tint = when (accountType) {
                        AccountType.HOSPITAL -> HealthogramTheme.colors.primary
                        AccountType.CLINIC -> HealthogramTheme.colors.secondary
                        AccountType.LABORATORY -> HealthogramTheme.colors.info
                        else -> HealthogramTheme.colors.textPrimary
                    }
                )
            }

            Spacer(modifier = Modifier.width(HealthogramTheme.spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = facilityName,
                        style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    if (isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        HealthogramVerifiedBadge(status = VerificationStatus.APPROVED, size = BadgeSize.SMALL)
                    }
                }
                Text(
                    text = "${accountType.displayName} • $address",
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.textMuted
                )
                Text(
                    text = highlightMetric,
                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                    color = HealthogramTheme.colors.secondary
                )
            }
        }
    }
}

/**
 * Ecommerce Product Card
 */
@Composable
fun HealthogramProductCard(
    product: MarketplaceProduct,
    onAddToCart: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "product_card"
) {
    Card(
        modifier = modifier
            .width(180.dp)
            .clickable { onClick() }
            .testTag(testTag),
        shape = HealthogramTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, HealthogramTheme.colors.border)
    ) {
        Column {
            // Product Image Thumbnail Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(HealthogramTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MedicalInformation,
                    contentDescription = null,
                    tint = HealthogramTheme.colors.primary,
                    modifier = Modifier.size(48.dp)
                )

                if (product.isFlashSale) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        shape = HealthogramTheme.shapes.small,
                        color = HealthogramTheme.colors.error
                    ) {
                        Text(
                            text = "FLASH SALE",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = HealthogramTheme.typography.overline,
                            color = Color.White
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.title,
                    style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.sellerStoreName,
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.textMuted,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "$${"%.2f".format(product.price)}",
                            style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = HealthogramTheme.colors.textPrimary
                        )
                        if (product.discountPrice != null) {
                            Text(
                                text = "$${"%.2f".format(product.discountPrice)}",
                                style = HealthogramTheme.typography.caption,
                                color = HealthogramTheme.colors.textMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(HealthogramTheme.colors.primaryContainer)
                    ) {
                        Icon(
                            Icons.Default.AddShoppingCart,
                            contentDescription = "Add to Cart",
                            tint = HealthogramTheme.colors.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Health Record Card (Private, High Security Visual styling)
 */
@Composable
fun HealthogramHealthRecordCard(
    recordTitle: String,
    recordCategory: String,
    facilityOrDoctor: String,
    dateString: String,
    isConfidential: Boolean = true,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    testTag: String = "health_record_card"
) {
    HealthogramBasicCard(
        modifier = modifier.testTag(testTag),
        onClick = onClick,
        containerColor = HealthogramTheme.colors.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(HealthogramTheme.colors.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = HealthogramTheme.colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(HealthogramTheme.spacing.md))
                Column {
                    Text(
                        text = recordTitle,
                        style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "$recordCategory • $facilityOrDoctor",
                        style = HealthogramTheme.typography.bodySmall,
                        color = HealthogramTheme.colors.textMuted
                    )
                }
            }

            Surface(
                shape = HealthogramTheme.shapes.small,
                color = HealthogramTheme.colors.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Private", modifier = Modifier.size(12.dp), tint = HealthogramTheme.colors.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Encrypted",
                        style = HealthogramTheme.typography.overline,
                        color = HealthogramTheme.colors.primary
                    )
                }
            }
        }
    }
}

/**
 * Professional Analytics Card
 */
@Composable
fun HealthogramAnalyticsCard(
    title: String,
    value: String,
    trendPercentage: String,
    isPositiveTrend: Boolean = true,
    icon: ImageVector = Icons.Default.TrendingUp,
    modifier: Modifier = Modifier,
    testTag: String = "analytics_card"
) {
    HealthogramBasicCard(modifier = modifier.testTag(testTag)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = title,
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.textMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = HealthogramTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                    color = HealthogramTheme.colors.textPrimary
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(HealthogramTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(HealthogramTheme.spacing.sm))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isPositiveTrend) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = if (isPositiveTrend) HealthogramTheme.colors.success else HealthogramTheme.colors.error,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = trendPercentage,
                style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                color = if (isPositiveTrend) HealthogramTheme.colors.success else HealthogramTheme.colors.error
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "vs last month",
                style = HealthogramTheme.typography.caption,
                color = HealthogramTheme.colors.textMuted
            )
        }
    }
}
