package com.example.healthogram.ui.marketplace.seller.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.marketplace.MarketplaceProduct
import com.example.healthogram.marketplace.ProductStatus
import com.example.healthogram.marketplace.seller.*

/**
 * HEALTHOGRAM — STEP 09: SELLER CENTER MATERIAL 3 UI COMPONENTS
 * 24 Standardized, responsive UI components for the Seller Center.
 */

// 1. SellerDashboardHeader
@Composable
fun SellerDashboardHeader(
    profile: MarketplaceSellerProfile,
    onStoreClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Surface(
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(HealthogramTheme.colors.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Storefront,
                    contentDescription = null,
                    tint = HealthogramTheme.colors.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile.storeName.ifBlank { "Health Store" },
                        style = HealthogramTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    SellerVerificationBadge(status = profile.verificationStatus)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${profile.sellerType.displayName} • ${profile.countryCode} (${profile.currency})",
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.textSecondary
                )
            }
            IconButton(onClick = onStoreClick) {
                Icon(Icons.Default.Visibility, contentDescription = "View Storefront", tint = HealthogramTheme.colors.textSecondary)
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Seller Settings", tint = HealthogramTheme.colors.textSecondary)
            }
        }
    }
}

// 2. SellerRevenueCard
@Composable
fun SellerRevenueCard(
    balance: MarketplaceSellerBalance,
    onPayoutClick: () -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.primary),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Available For Payout",
                        style = HealthogramTheme.typography.caption,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = SellerCustomFunctions.formatSellerBalance(balance.availableBalance, balance.currency),
                        style = HealthogramTheme.typography.h4.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                }
                Button(
                    onClick = onPayoutClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = HealthogramTheme.colors.primary),
                    shape = HealthogramTheme.shapes.pill,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Payout", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.White.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Gross Sales", style = HealthogramTheme.typography.caption, color = Color.White.copy(alpha = 0.75f))
                    Text(
                        SellerCustomFunctions.formatSellerBalance(balance.grossSales, balance.currency),
                        style = HealthogramTheme.typography.body2.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }
                Column {
                    Text("Pending Funds", style = HealthogramTheme.typography.caption, color = Color.White.copy(alpha = 0.75f))
                    Text(
                        SellerCustomFunctions.formatSellerBalance(balance.pendingBalance, balance.currency),
                        style = HealthogramTheme.typography.body2.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }
                Column {
                    Text("Total Settled", style = HealthogramTheme.typography.caption, color = Color.White.copy(alpha = 0.75f))
                    Text(
                        SellerCustomFunctions.formatSellerBalance(balance.paidOut, balance.currency),
                        style = HealthogramTheme.typography.body2.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

// 3. SellerOrdersCard
@Composable
fun SellerOrdersCard(
    totalOrders: Int,
    pendingOrders: Int,
    onClick: () -> Unit = {}
) {
    Surface(
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(HealthogramTheme.colors.info.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = HealthogramTheme.colors.info)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Order Processing", style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold))
                Text("$totalOrders total orders • $pendingOrders pending fulfillment", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthogramTheme.colors.textSecondary)
        }
    }
}

// 4. SellerProductCard
@Composable
fun SellerProductCard(
    product: MarketplaceProduct,
    onClick: () -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    Surface(
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(HealthogramTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Medication, contentDescription = null, tint = HealthogramTheme.colors.primary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.title,
                    style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${product.price} ${product.currency}",
                        style = HealthogramTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("• Stock: ${product.stock}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                SellerProductStatusBadge(status = product.status.name)
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Product", tint = HealthogramTheme.colors.textSecondary)
            }
        }
    }
}

// 5. SellerInventoryCard
@Composable
fun SellerInventoryCard(
    inventory: MarketplaceInventory,
    onAdjustStock: (Int) -> Unit = {}
) {
    Surface(
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SKU: ${inventory.sku}", style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.textSecondary)
                    Text(
                        "${inventory.availableQuantity} Units Available",
                        style = HealthogramTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold),
                        color = if (inventory.availableQuantity <= inventory.lowStockThreshold) HealthogramTheme.colors.error else HealthogramTheme.colors.textPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { onAdjustStock(-1) },
                        modifier = Modifier.size(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("-")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { onAdjustStock(5) },
                        modifier = Modifier.size(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("+5")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = SellerCustomFunctions.calculateStockPercentage(inventory.availableQuantity, inventory.lowStockThreshold),
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = if (inventory.availableQuantity <= inventory.lowStockThreshold) HealthogramTheme.colors.error else HealthogramTheme.colors.primary,
                trackColor = HealthogramTheme.colors.surfaceVariant
            )
        }
    }
}

// 6. SellerLowStockCard
@Composable
fun SellerLowStockCard(
    lowStockCount: Int,
    onClick: () -> Unit = {}
) {
    if (lowStockCount <= 0) return
    Card(
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.warning.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, HealthogramTheme.colors.warning.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = HealthogramTheme.colors.warning)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Low Stock Warning", style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.warning)
                Text("$lowStockCount products have fallen below their re-order threshold.", style = HealthogramTheme.typography.caption)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthogramTheme.colors.warning)
        }
    }
}

// 7. SellerProductStatusBadge
@Composable
fun SellerProductStatusBadge(status: String) {
    val (bgColor, textColor, text) = when (status.uppercase()) {
        "ACTIVE" -> Triple(HealthogramTheme.colors.success.copy(alpha = 0.15f), HealthogramTheme.colors.success, "Live")
        "PENDING_REVIEW" -> Triple(HealthogramTheme.colors.warning.copy(alpha = 0.15f), HealthogramTheme.colors.warning, "In Review")
        "DRAFT" -> Triple(HealthogramTheme.colors.surfaceVariant, HealthogramTheme.colors.textSecondary, "Draft")
        "REJECTED" -> Triple(HealthogramTheme.colors.error.copy(alpha = 0.15f), HealthogramTheme.colors.error, "Rejected")
        else -> Triple(HealthogramTheme.colors.surfaceVariant, HealthogramTheme.colors.textSecondary, status)
    }

    Surface(
        shape = HealthogramTheme.shapes.pill,
        color = bgColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
    }
}

// 8. SellerVerificationBadge
@Composable
fun SellerVerificationBadge(status: SellerVerificationStatus) {
    val isVerified = status == SellerVerificationStatus.VERIFIED
    Surface(
        shape = HealthogramTheme.shapes.pill,
        color = if (isVerified) HealthogramTheme.colors.success.copy(alpha = 0.15f) else HealthogramTheme.colors.warning.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isVerified) Icons.Default.Verified else Icons.Default.Pending,
                contentDescription = null,
                tint = if (isVerified) HealthogramTheme.colors.success else HealthogramTheme.colors.warning,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isVerified) "Verified Seller" else "Pending Verification",
                style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                color = if (isVerified) HealthogramTheme.colors.success else HealthogramTheme.colors.warning
            )
        }
    }
}

// 9. SellerOrderCard
@Composable
fun SellerOrderCard(
    order: SellerOrderView,
    onClick: () -> Unit = {},
    onAdvanceStatus: () -> Unit = {}
) {
    Surface(
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(order.orderNumber, style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold))
                Surface(
                    shape = HealthogramTheme.shapes.pill,
                    color = HealthogramTheme.colors.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        SellerCustomFunctions.getOrderStatusLabel(order.orderStatus),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("Recipient: ${order.recipientName}", style = HealthogramTheme.typography.body2.copy(fontWeight = FontWeight.SemiBold))
            Text("${order.deliveryCity} • ${order.deliveryAddressLine}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = HealthogramTheme.colors.borderLight)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Your Net Earnings", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
                    Text(
                        "${order.sellerNetEarnings} ${order.currency}",
                        style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.primary
                    )
                }
                if (order.orderStatus == "CONFIRMED" || order.orderStatus == "PROCESSING" || order.orderStatus == "PACKED") {
                    Button(
                        onClick = onAdvanceStatus,
                        shape = HealthogramTheme.shapes.pill,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            when (order.orderStatus) {
                                "CONFIRMED" -> "Start Processing"
                                "PROCESSING" -> "Mark Packed"
                                "PACKED" -> "Ready for Carrier"
                                else -> "Update"
                            },
                            style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

// 10. SellerOrderTimeline
@Composable
fun SellerOrderTimeline(currentStatus: String) {
    val steps = listOf("Confirmed", "Processing", "Packed", "Ready for Shipment", "In Transit", "Delivered")
    val activeIndex = when (currentStatus.uppercase()) {
        "CONFIRMED" -> 0
        "PROCESSING" -> 1
        "PACKED" -> 2
        "READY_FOR_SHIPMENT" -> 3
        "SHIPPED" -> 4
        "DELIVERED" -> 5
        else -> 0
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, step ->
            val isPassed = index <= activeIndex
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isPassed) HealthogramTheme.colors.primary else HealthogramTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPassed) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    } else {
                        Text("${index + 1}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = step,
                    style = HealthogramTheme.typography.body2.copy(fontWeight = if (index == activeIndex) FontWeight.Bold else FontWeight.Normal),
                    color = if (isPassed) HealthogramTheme.colors.textPrimary else HealthogramTheme.colors.textSecondary
                )
            }
        }
    }
}

// 11. SellerReturnCard
@Composable
fun SellerReturnCard(
    orderNumber: String,
    productTitle: String,
    reason: String,
    status: String,
    onRespond: () -> Unit = {}
) {
    Surface(
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Return Request • $orderNumber", style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold))
                Surface(
                    shape = HealthogramTheme.shapes.pill,
                    color = HealthogramTheme.colors.warning.copy(alpha = 0.15f)
                ) {
                    Text(status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.warning)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(productTitle, style = HealthogramTheme.typography.body2)
            Text("Reason: $reason", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onRespond, shape = HealthogramTheme.shapes.pill) {
                    Text("Review Return Policy")
                }
            }
        }
    }
}

// 12. SellerReviewCard
@Composable
fun SellerReviewCard(
    reviewerName: String,
    rating: Double,
    comment: String,
    date: String,
    onReply: () -> Unit = {}
) {
    Surface(
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(reviewerName, style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold))
                Row {
                    repeat(5) { i ->
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = if (i < rating) Color(0xFFFFB300) else HealthogramTheme.colors.surfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(date, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(comment, style = HealthogramTheme.typography.body2)
        }
    }
}

// 13. SellerAnalyticsCard
@Composable
fun SellerAnalyticsCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color = HealthogramTheme.colors.primary
) {
    Surface(
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(title, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
                Text(value, style = HealthogramTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold))
                Text(subtitle, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.success)
            }
        }
    }
}

// 14. SellerPayoutCard
@Composable
fun SellerPayoutCard(
    request: MarketplaceSellerPayoutRequest
) {
    Surface(
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("${request.amount} ${request.currency}", style = HealthogramTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold))
                Text(request.providerPayoutId ?: "Direct Bank Transfer", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
            }
            Surface(
                shape = HealthogramTheme.shapes.pill,
                color = when (request.status) {
                    PayoutStatus.PAID -> HealthogramTheme.colors.success.copy(alpha = 0.15f)
                    PayoutStatus.REQUESTED, PayoutStatus.PROCESSING -> HealthogramTheme.colors.info.copy(alpha = 0.15f)
                    else -> HealthogramTheme.colors.surfaceVariant
                }
            ) {
                Text(
                    SellerCustomFunctions.getPayoutStatusLabel(request.status),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                    color = when (request.status) {
                        PayoutStatus.PAID -> HealthogramTheme.colors.success
                        PayoutStatus.REQUESTED, PayoutStatus.PROCESSING -> HealthogramTheme.colors.info
                        else -> HealthogramTheme.colors.textSecondary
                    }
                )
            }
        }
    }
}

// 15. SellerLedgerRow
@Composable
fun SellerLedgerRow(entry: MarketplaceSellerLedgerEntry) {
    Surface(
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    entry.transactionType.value.replace('_', ' ').capitalize(),
                    style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "Order: ${entry.orderId ?: "N/A"} • Commission: -${entry.commission} ${entry.currency}",
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.textSecondary
                )
            }
            Text(
                "${if (entry.netAmount >= 0) "+" else ""}${entry.netAmount} ${entry.currency}",
                style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold),
                color = if (entry.netAmount >= 0) HealthogramTheme.colors.success else HealthogramTheme.colors.error
            )
        }
    }
}

// 16. SellerProductImageUploader
@Composable
fun SellerProductImageUploader(
    images: List<String>,
    onUploadClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Product Images", style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(HealthogramTheme.colors.surfaceVariant)
                    .clickable { onUploadClick() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = HealthogramTheme.colors.primary)
                    Text("Upload", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.primary)
                }
            }
            images.take(3).forEach { img ->
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(HealthogramTheme.colors.surface)
                        .border(1.dp, HealthogramTheme.colors.borderLight, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = HealthogramTheme.colors.textSecondary)
                }
            }
        }
    }
}

// 17. SellerPriceEditor
@Composable
fun SellerPriceEditor(
    price: Double,
    compareAtPrice: Double?,
    onPriceChange: (Double) -> Unit,
    currency: String = "SAR"
) {
    Surface(
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Pricing & Valuation", style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Selling Price ($currency)", style = HealthogramTheme.typography.caption)
                    OutlinedTextField(
                        value = price.toString(),
                        onValueChange = { str -> str.toDoubleOrNull()?.let { onPriceChange(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Original Compare-At Price", style = HealthogramTheme.typography.caption)
                    OutlinedTextField(
                        value = compareAtPrice?.toString() ?: "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = true
                    )
                }
            }
        }
    }
}

// 18. SellerInventoryEditor
@Composable
fun SellerInventoryEditor(
    availableStock: Int,
    lowStockThreshold: Int,
    onStockChange: (Int) -> Unit
) {
    Surface(
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Inventory Management", style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Available Units: $availableStock", style = HealthogramTheme.typography.body2.copy(fontWeight = FontWeight.Bold))
                    Text("Low stock threshold: $lowStockThreshold units", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
                }
                Row {
                    Button(onClick = { onStockChange(10) }, shape = HealthogramTheme.shapes.pill) {
                        Text("+10 Units")
                    }
                }
            }
        }
    }
}

// 19. SellerOfferCard
@Composable
fun SellerOfferCard(offer: MarketplaceSellerOffer) {
    Surface(
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    offer.offerType.value.replace('_', ' ').capitalize(),
                    style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "Discount Value: ${offer.value}% • Min Qty: ${offer.minimumQuantity}",
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.textSecondary
                )
            }
            Surface(
                shape = HealthogramTheme.shapes.pill,
                color = HealthogramTheme.colors.success.copy(alpha = 0.15f)
            ) {
                Text(
                    offer.status.name,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.success
                )
            }
        }
    }
}

// 20. SellerFlashSaleCard
@Composable
fun SellerFlashSaleCard(
    saleTitle: String,
    discountPercent: Double,
    onParticipate: () -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.secondary),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Flash Sale Campaign", style = HealthogramTheme.typography.caption, color = Color.White.copy(alpha = 0.8f))
                Text(saleTitle, style = HealthogramTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold), color = Color.White)
                Text("Up to $discountPercent% seller subsidy eligible", style = HealthogramTheme.typography.caption, color = Color.White.copy(alpha = 0.9f))
            }
            Button(
                onClick = onParticipate,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = HealthogramTheme.colors.secondary),
                shape = HealthogramTheme.shapes.pill
            ) {
                Text("Enlist")
            }
        }
    }
}

// 21. SellerSupportCard
@Composable
fun SellerSupportCard(ticket: SellerSupportTicket) {
    Surface(
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(ticket.subject, style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Surface(
                    shape = HealthogramTheme.shapes.pill,
                    color = HealthogramTheme.colors.primary.copy(alpha = 0.12f)
                ) {
                    Text(ticket.status.capitalize(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(ticket.description, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

// 22. SellerEmptyState
@Composable
fun SellerEmptyState(
    title: String,
    subtitle: String,
    icon: ImageVector = Icons.Default.Inbox,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = HealthogramTheme.colors.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(14.dp))
        Text(title, style = HealthogramTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.textPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(subtitle, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
        if (actionLabel != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAction, shape = HealthogramTheme.shapes.pill) {
                Text(actionLabel)
            }
        }
    }
}

// 23. SellerLoadingState
@Composable
fun SellerLoadingState(message: String = "Loading Seller Data...") {
    Box(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = HealthogramTheme.colors.primary)
            Spacer(modifier = Modifier.height(12.dp))
            Text(message, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
        }
    }
}

// 24. SellerErrorState
@Composable
fun SellerErrorState(
    errorMessage: String,
    onRetry: () -> Unit = {}
) {
    Surface(
        color = HealthogramTheme.colors.error.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, HealthogramTheme.colors.error.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = HealthogramTheme.colors.error)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Attention Required", style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.error)
                Text(errorMessage, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
            }
            TextButton(onClick = onRetry) {
                Text("Retry", color = HealthogramTheme.colors.error, fontWeight = FontWeight.Bold)
            }
        }
    }
}
