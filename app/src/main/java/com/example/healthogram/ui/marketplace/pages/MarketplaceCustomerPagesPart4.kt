package com.example.healthogram.ui.marketplace.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.marketplace.*
import com.example.healthogram.ui.marketplace.components.*

/**
 * HEALTHOGRAM — STEP 08: MARKETPLACE CUSTOMER PAGES (PART 4)
 * 22. MarketplaceRefundStatusPage
 * 23. MarketplaceDealsPage
 * 24. MarketplaceFlashSalesPage
 * 25. MarketplaceRecentlyViewedPage
 * 26. MarketplaceNotificationsPage
 * 27. MarketplaceCustomerSupportPage
 * 28. MarketplaceSettingsPage
 */

// 22. MarketplaceRefundStatusPage
@Composable
fun MarketplaceRefundStatusPage(
    repository: MarketplaceRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val refunds = remember {
        listOf(
            MarketplaceRefund(
                refundId = "ref_sa_90182",
                orderId = "ord_past_2",
                paymentId = "pay_sa_1289",
                customerUid = repository.currentCustomerUid,
                amount = 211.60,
                currency = "SAR",
                reason = "Damaged packaging in transit",
                status = RefundStatus.COMPLETED
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_refund_status_page")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Refund Status", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(refunds) { ref ->
                MarketplaceRefundCard(refund = ref)
            }
        }
    }
}

// 23. MarketplaceDealsPage
@Composable
fun MarketplaceDealsPage(
    repository: MarketplaceRepository,
    onProductClick: (MarketplaceProduct) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val products by repository.products.collectAsState()
    val wishlist by repository.wishlist.collectAsState()
    val deals = products.filter { it.discountPrice != null }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_deals_page")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Healthcare Deals & Promotions", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))
        repository.availableCoupons.forEach { coupon ->
            MarketplaceDealCard(
                dealTitle = "${coupon.code} • ${coupon.discountValue}${if (coupon.discountType == DiscountType.PERCENTAGE) "%" else " SAR"} Discount",
                subtitle = "Valid on orders over ${coupon.minimumOrderValue} SAR",
                code = coupon.code,
                onClick = {}
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Discounted Healthcare Items", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                MarketplaceProductGrid(
                    products = deals,
                    wishlist = wishlist,
                    onWishlistToggle = { repository.toggleWishlist(it) },
                    onAddToCart = { repository.addToCart(it) },
                    onProductClick = onProductClick
                )
            }
        }
    }
}

// 24. MarketplaceFlashSalesPage
@Composable
fun MarketplaceFlashSalesPage(
    repository: MarketplaceRepository,
    onProductClick: (MarketplaceProduct) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val products by repository.products.collectAsState()
    val wishlist by repository.wishlist.collectAsState()
    val flashSaleProducts = products.filter { it.isFlashSale }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_flash_sales_page")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Flash Sales", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(12.dp))
        MarketplaceFlashSaleCard(onClick = {})

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                MarketplaceProductGrid(
                    products = flashSaleProducts,
                    wishlist = wishlist,
                    onWishlistToggle = { repository.toggleWishlist(it) },
                    onAddToCart = { repository.addToCart(it) },
                    onProductClick = onProductClick
                )
            }
        }
    }
}

// 25. MarketplaceRecentlyViewedPage
@Composable
fun MarketplaceRecentlyViewedPage(
    repository: MarketplaceRepository,
    onProductClick: (MarketplaceProduct) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recentlyViewedIds by repository.recentlyViewed.collectAsState()
    val products by repository.products.collectAsState()
    val wishlist by repository.wishlist.collectAsState()

    val viewedProducts = remember(recentlyViewedIds, products) {
        recentlyViewedIds.mapNotNull { id -> products.find { it.productId == id } }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_recently_viewed_page")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text("Recently Viewed", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            if (viewedProducts.isNotEmpty()) {
                TextButton(onClick = { repository.clearRecentlyViewed() }) {
                    Text("Clear", color = HealthogramTheme.colors.textMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (viewedProducts.isEmpty()) {
            MarketplaceEmptyState(
                icon = Icons.Default.Visibility,
                title = "No recently viewed products",
                message = "Products you browse in the catalog will appear here for fast access."
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    MarketplaceProductGrid(
                        products = viewedProducts,
                        wishlist = wishlist,
                        onWishlistToggle = { repository.toggleWishlist(it) },
                        onAddToCart = { repository.addToCart(it) },
                        onProductClick = onProductClick
                    )
                }
            }
        }
    }
}

// 26. MarketplaceNotificationsPage
@Composable
fun MarketplaceNotificationsPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notifications = listOf(
        Triple("Shipment Update", "Your order #HGM-83920194 is out for delivery with Saudi Healthcare Swift Delivery.", "15 mins ago"),
        Triple("Price Drop Alert", "Smart Bluetooth Blood Pressure Monitor is now available at 249 SAR.", "2 hours ago"),
        Triple("Flash Sale Live", "Up to 40% discount on clinical nebulizers and diagnostic monitors.", "1 day ago")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_notifications_page")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Marketplace Notifications", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(notifications) { (title, desc, time) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = HealthogramTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                    border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(HealthogramTheme.colors.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(title, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text(time, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(desc, style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textPrimary)
                        }
                    }
                }
            }
        }
    }
}

// 27. MarketplaceCustomerSupportPage
@Composable
fun MarketplaceCustomerSupportPage(
    repository: MarketplaceRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val supportTickets by repository.supportTickets.collectAsState()
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_customer_support_page")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Customer Support & Inquiries", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = HealthogramTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                    border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Open a Support Ticket", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("Subject / Issue") },
                            modifier = Modifier.fillMaxWidth().testTag("ticket_subject_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Describe your inquiry") },
                            modifier = Modifier.fillMaxWidth().height(100.dp).testTag("ticket_desc_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (subject.isNotBlank() && description.isNotBlank()) {
                                    val tkt = MarketplaceSupportTicket(
                                        customerUid = repository.currentCustomerUid,
                                        category = SupportCategory.ORDER,
                                        subject = subject,
                                        description = description
                                    )
                                    repository.addSupportTicket(tkt)
                                    subject = ""
                                    description = ""
                                }
                            },
                            shape = HealthogramTheme.shapes.pill,
                            modifier = Modifier.fillMaxWidth().testTag("submit_ticket_btn")
                        ) {
                            Text("Submit Support Request")
                        }
                    }
                }
            }

            item {
                Text("Your Support Tickets", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }

            items(supportTickets) { ticket ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = HealthogramTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                    border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(ticket.subject, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Surface(shape = HealthogramTheme.shapes.small, color = HealthogramTheme.colors.secondary.copy(alpha = 0.15f)) {
                                Text(ticket.status.label, style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.secondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(ticket.description, style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
                    }
                }
            }
        }
    }
}

// 28. MarketplaceSettingsPage
@Composable
fun MarketplaceSettingsPage(
    repository: MarketplaceRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var country by remember { mutableStateOf("Saudi Arabia (SAR)") }
    var currency by remember { mutableStateOf("SAR") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoTrackEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_settings_page")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Marketplace Settings", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = HealthogramTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Regional & Compliance Settings", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Active Country", style = HealthogramTheme.typography.bodyMedium)
                        Text(country, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                    }
                    Surface(shape = HealthogramTheme.shapes.small, color = HealthogramTheme.colors.primary.copy(alpha = 0.1f)) {
                        Text("Saudi Arabia", style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = HealthogramTheme.colors.borderLight)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Order Tracking Alerts", style = HealthogramTheme.typography.bodyMedium)
                        Text("Push notifications on courier updates", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                    }
                    Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = HealthogramTheme.colors.borderLight)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Cross-Border Trade", style = HealthogramTheme.typography.bodyMedium)
                        Text("International cross-border medical imports", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                    }
                    Surface(shape = HealthogramTheme.shapes.small, color = HealthogramTheme.colors.surfaceVariant) {
                        Text("Disabled (Local SA Only)", style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = HealthogramTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Health Passport Privacy Guarantee", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Healthogram strictly isolates clinical Health Passport documents, lab tests, and diagnoses from all marketplace recommendations and advertising. Your medical records are never shared with merchants.",
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.textMuted
                )
            }
        }
    }
}
