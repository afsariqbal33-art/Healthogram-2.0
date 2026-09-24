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
 * HEALTHOGRAM — STEP 08: MARKETPLACE CUSTOMER PAGES (PART 3)
 * 15. MarketplaceOrdersPage
 * 16. MarketplaceOrderDetailsPage
 * 17. MarketplaceOrderTrackingPage
 * 18. MarketplaceInvoicePage
 * 19. MarketplaceReviewsPage
 * 20. MarketplaceWriteReviewPage
 * 21. MarketplaceReturnsPage
 */

// 15. MarketplaceOrdersPage
@Composable
fun MarketplaceOrdersPage(
    repository: MarketplaceRepository,
    onOrderClick: (MarketplaceOrderSnapshot) -> Unit,
    onTrackOrder: (MarketplaceOrderSnapshot) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orders by repository.orders.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredOrders = remember(orders, selectedFilter) {
        when (selectedFilter) {
            "ACTIVE" -> orders.filter { it.orderStatus != MarketplaceOrderStatus.DELIVERED && it.orderStatus != MarketplaceOrderStatus.COMPLETED && it.orderStatus != MarketplaceOrderStatus.CANCELLED }
            "COMPLETED" -> orders.filter { it.orderStatus == MarketplaceOrderStatus.DELIVERED || it.orderStatus == MarketplaceOrderStatus.COMPLETED }
            else -> orders
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_orders_page")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Order History", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ALL" to "All Orders", "ACTIVE" to "In Progress", "COMPLETED" to "Completed").forEach { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key,
                    onClick = { selectedFilter = key },
                    label = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (filteredOrders.isEmpty()) {
            MarketplaceEmptyState(
                icon = Icons.Default.Inventory2,
                title = "No orders found",
                message = "You don't have any orders under this filter."
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredOrders) { order ->
                    MarketplaceOrderCard(
                        order = order,
                        onClick = { onOrderClick(order) },
                        onTrackClick = { onTrackOrder(order) }
                    )
                }
            }
        }
    }
}

// 16. MarketplaceOrderDetailsPage
@Composable
fun MarketplaceOrderDetailsPage(
    order: MarketplaceOrderSnapshot,
    onTrackOrder: () -> Unit,
    onViewInvoice: () -> Unit,
    onRequestReturn: () -> Unit,
    onWriteReview: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_order_details_page")
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
                Text("Order Details", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            IconButton(onClick = onViewInvoice, modifier = Modifier.testTag("details_view_invoice_icon")) {
                Icon(Icons.Default.Receipt, contentDescription = "Invoice", tint = HealthogramTheme.colors.primary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = HealthogramTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                    border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(order.orderNumber, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Surface(shape = HealthogramTheme.shapes.small, color = HealthogramTheme.colors.primary.copy(alpha = 0.15f)) {
                                Text(order.orderStatus.label, style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Payment: ${order.paymentStatus.label}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                        Text("Delivery Status: ${order.deliveryStatus}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                    }
                }
            }

            item {
                Text("Delivery Address", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))
                MarketplaceAddressCard(
                    address = order.shippingAddressSnapshot,
                    isSelected = true,
                    onSelect = {}
                )
            }

            item {
                Text("Progress Status", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                MarketplaceOrderTimeline(orderStatus = order.orderStatus)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onTrackOrder,
                        shape = HealthogramTheme.shapes.pill,
                        modifier = Modifier.weight(1f).testTag("details_track_btn")
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Track Shipment")
                    }

                    if (order.orderStatus == MarketplaceOrderStatus.DELIVERED) {
                        OutlinedButton(
                            onClick = onWriteReview,
                            shape = HealthogramTheme.shapes.pill,
                            modifier = Modifier.weight(1f).testTag("details_write_review_btn")
                        ) {
                            Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Write Review")
                        }
                    }
                }
            }

            if (order.orderStatus == MarketplaceOrderStatus.DELIVERED) {
                item {
                    OutlinedButton(
                        onClick = onRequestReturn,
                        shape = HealthogramTheme.shapes.pill,
                        modifier = Modifier.fillMaxWidth().testTag("details_return_btn")
                    ) {
                        Icon(Icons.Default.AssignmentReturn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Request Return / Exchange")
                    }
                }
            }
        }
    }
}

// 17. MarketplaceOrderTrackingPage
@Composable
fun MarketplaceOrderTrackingPage(
    order: MarketplaceOrderSnapshot,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deliveryProvider = remember { SaudiHealthcareDeliveryProvider() }
    var shipmentInfo by remember { mutableStateOf<ShipmentInfo?>(null) }

    LaunchedEffect(order.orderId) {
        shipmentInfo = deliveryProvider.trackShipment(order.orderNumber)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_order_tracking_page")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Shipment Tracking", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = HealthogramTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(46.dp).clip(CircleShape).background(HealthogramTheme.colors.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Saudi Healthcare Swift Delivery", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text("Tracking No: ${shipmentInfo?.trackingNumber ?: "HGM-EXP-SA"}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Surface(shape = HealthogramTheme.shapes.small, color = HealthogramTheme.colors.primary.copy(alpha = 0.1f)) {
                    Text(
                        text = "Status: ${shipmentInfo?.currentStatus ?: "In Transit"}",
                        style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Milestone Tracking History", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(12.dp))

        val timeline = shipmentInfo?.statusTimeline ?: listOf(
            "Order Registered at Pharmacy Warehouse" to System.currentTimeMillis() - 86400000L,
            "Quality & Cold Chain Check Passed" to System.currentTimeMillis() - 43200000L,
            "Departed Riyadh Sorting Facility" to System.currentTimeMillis() - 21600000L,
            "Out for Delivery with Courier" to System.currentTimeMillis()
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(timeline) { (event, time) ->
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(event, style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text("Recorded: Today", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                    }
                }
            }
        }
    }
}

// 18. MarketplaceInvoicePage
@Composable
fun MarketplaceInvoicePage(
    order: MarketplaceOrderSnapshot,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dummyItems = listOf(
        MarketplaceOrderItem(
            itemId = "item_inv_1",
            orderId = order.orderId,
            productId = "prod_bp_monitor",
            sellerUid = "seller_omron",
            productTitleSnapshot = "Smart Bluetooth Blood Pressure Monitor",
            unitPrice = order.subtotal,
            quantity = 1,
            lineTotal = order.subtotal
        )
    )

    val invoiceText = remember(order) {
        MarketplaceCustomActions.generateMarketplaceInvoice(order, dummyItems)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_invoice_page")
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
                Text("Official Tax Invoice", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Share, contentDescription = "Share Invoice")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = HealthogramTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
        ) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                item {
                    Text(
                        text = invoiceText,
                        style = HealthogramTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                        color = HealthogramTheme.colors.textPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onBack,
            shape = HealthogramTheme.shapes.pill,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
    }
}

// 19. MarketplaceReviewsPage
@Composable
fun MarketplaceReviewsPage(
    repository: MarketplaceRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reviews by repository.reviews.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_reviews_page")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Product Reviews (${reviews.size})", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(reviews) { rev ->
                MarketplaceReviewCard(review = rev)
            }
        }
    }
}

// 20. MarketplaceWriteReviewPage
@Composable
fun MarketplaceWriteReviewPage(
    order: MarketplaceOrderSnapshot,
    repository: MarketplaceRepository,
    onReviewSubmitted: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rating by remember { mutableIntStateOf(5) }
    var title by remember { mutableStateOf("") }
    var reviewText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_write_review_page")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Write a Verified Review", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = HealthogramTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Rate your product experience", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star }) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = "$star stars",
                                tint = if (star <= rating) Color(0xFFFFB300) else HealthogramTheme.colors.textMuted,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Headline / Summary") },
            modifier = Modifier.fillMaxWidth().testTag("review_title_input")
        )

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = reviewText,
            onValueChange = { reviewText = it },
            label = { Text("Detailed Review") },
            modifier = Modifier.fillMaxWidth().height(140.dp).testTag("review_text_input")
        )

        errorMessage?.let { err ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(err, color = HealthogramTheme.colors.error, style = HealthogramTheme.typography.caption)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Note: Under Healthogram Healthcare Policy, unsubstantiated medical claims (e.g. \"cures cancer\") are rejected during verification.",
            style = HealthogramTheme.typography.caption,
            color = HealthogramTheme.colors.textMuted
        )

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                try {
                    val review = MarketplaceCustomActions.addMarketplaceReview(
                        productId = "prod_bp_monitor",
                        orderId = order.orderId,
                        orderItemId = "item_1",
                        customerUid = repository.currentCustomerUid,
                        sellerUid = "seller_omron",
                        rating = rating,
                        title = title.ifBlank { "High Quality Medical Device" },
                        reviewText = reviewText.ifBlank { "Very satisfied with the clinical accuracy." }
                    )
                    repository.addReview(review)
                    onReviewSubmitted()
                } catch (e: Exception) {
                    errorMessage = e.message
                }
            },
            shape = HealthogramTheme.shapes.pill,
            modifier = Modifier.fillMaxWidth().testTag("submit_review_btn")
        ) {
            Text("Submit Review")
        }
    }
}

// 21. MarketplaceReturnsPage
@Composable
fun MarketplaceReturnsPage(
    repository: MarketplaceRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val returns by repository.returns.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_returns_page")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Returns & Exchanges", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (returns.isEmpty()) {
            MarketplaceEmptyState(
                icon = Icons.Default.AssignmentReturn,
                title = "No active returns",
                message = "Delivered orders eligible for the 14-day healthcare return policy will appear here."
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(returns) { ret ->
                    MarketplaceReturnCard(returnReq = ret)
                }
            }
        }
    }
}
