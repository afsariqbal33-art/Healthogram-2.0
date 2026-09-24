package com.example.healthogram.ui.marketplace.seller.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.marketplace.seller.*
import com.example.healthogram.ui.marketplace.seller.components.*

/**
 * HEALTHOGRAM — STEP 09: SELLER ORDER FULFILLMENT & AUTHORITATIVE FINANCIAL PAGES
 * Implements strict separation: No health passport access, authoritative ledger entries,
 * and controlled operational order state transitions.
 */

// 1. SellerOrdersPage
@Composable
fun SellerOrdersPage(
    orders: List<SellerOrderView>,
    onOrderClick: (SellerOrderView) -> Unit = {},
    onAdvanceOrderStatus: (String, String) -> Unit = { _, _ -> }
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Order Fulfillment", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
            Text("Process incoming customer purchases and update dispatch readiness.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (orders.isEmpty()) {
            item {
                SellerEmptyState(
                    title = "No Orders Pending",
                    subtitle = "All orders have been packed and handed over to logistics.",
                    icon = Icons.Default.CheckCircle
                )
            }
        } else {
            items(orders) { order ->
                SellerOrderCard(
                    order = order,
                    onClick = { onOrderClick(order) },
                    onAdvanceStatus = {
                        val next = when (order.orderStatus) {
                            "CONFIRMED" -> "PROCESSING"
                            "PROCESSING" -> "PACKED"
                            "PACKED" -> "READY_FOR_SHIPMENT"
                            else -> order.orderStatus
                        }
                        onAdvanceOrderStatus(order.orderId, next)
                    }
                )
            }
        }
    }
}

// 2. SellerOrderDetailsPage
@Composable
fun SellerOrderDetailsPage(
    order: SellerOrderView,
    onBack: () -> Unit = {},
    onAdvanceStatus: (String) -> Unit = {}
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Order ${order.orderNumber}", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Fulfillment Progress", style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(12.dp))
                    SellerOrderTimeline(currentStatus = order.orderStatus)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Recipient & Delivery Destination", style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(order.recipientName, style = HealthogramTheme.typography.body2.copy(fontWeight = FontWeight.Bold))
                    Text("${order.deliveryAddressLine}, ${order.deliveryCity}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
                    if (order.customerNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Delivery Instructions: ${order.customerNotes}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.primary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text("Ordered Items", style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(order.items) { item ->
            Surface(
                color = HealthogramTheme.colors.surface,
                border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.productTitle, style = HealthogramTheme.typography.body2.copy(fontWeight = FontWeight.Bold))
                        Text("SKU: ${item.productSku} • Qty: ${item.quantity}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
                    }
                    Text("${item.totalPrice} SAR", style = HealthogramTheme.typography.body2.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gross Subtotal", style = HealthogramTheme.typography.caption)
                        Text("${order.subtotal} ${order.currency}", style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Platform Fee / Commission", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
                        Text("-${order.platformCommission} ${order.currency}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.error)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Divider(color = HealthogramTheme.colors.borderLight)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Authoritative Net Payout", style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold))
                        Text("${order.sellerNetEarnings} ${order.currency}", style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// 3. SellerReturnsPage
@Composable
fun SellerReturnsPage(onBack: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            Text("Returns & Replacement Requests", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(16.dp))
        SellerReturnCard(
            orderNumber = "HG-ORD-88410",
            productTitle = "Omron Evolv Wireless Blood Pressure Monitor",
            reason = "Customer ordered wrong cuff size (requires Large). Device seal intact.",
            status = "Pending Inspection",
            onRespond = {}
        )
    }
}

// 4. SellerReviewsPage
@Composable
fun SellerReviewsPage(onBack: () -> Unit = {}) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Customer Product Feedback", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            SellerReviewCard(
                reviewerName = "Dr. Tariq Al-Ghamdi",
                rating = 5.0,
                comment = "Clinical grade precision. Fast delivery in Riyadh within 24 hours. Certified device.",
                date = "2 days ago"
            )
        }
        item {
            SellerReviewCard(
                reviewerName = "Sarah M.",
                rating = 4.5,
                comment = "Original packaging and valid warranty card included. Very satisfied.",
                date = "1 week ago"
            )
        }
    }
}

// 5. SellerFinancialDashboardPage
@Composable
fun SellerFinancialDashboardPage(
    balance: MarketplaceSellerBalance,
    ledger: List<MarketplaceSellerLedgerEntry>,
    payoutRequests: List<MarketplaceSellerPayoutRequest>,
    onRequestPayoutClick: () -> Unit = {},
    onLedgerClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Financial Ledger & Balances", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
            Text("Authoritative append-only financial accounting and payout status.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
            Spacer(modifier = Modifier.height(6.dp))
        }

        item {
            SellerRevenueCard(
                balance = balance,
                onPayoutClick = onRequestPayoutClick
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Ledger Entries", style = HealthogramTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold))
                TextButton(onClick = onLedgerClick) {
                    Text("View Full Ledger")
                }
            }
        }

        items(ledger.take(3)) { entry ->
            SellerLedgerRow(entry = entry)
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Payout History", style = HealthogramTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold))
        }

        items(payoutRequests) { req ->
            SellerPayoutCard(request = req)
        }
    }
}

// 6. SellerRequestPayoutPage
@Composable
fun SellerRequestPayoutPage(
    balance: MarketplaceSellerBalance,
    payoutAccount: MarketplaceSellerPayoutAccount?,
    onSubmitPayout: (Double) -> Unit,
    onBack: () -> Unit = {}
) {
    var amountStr by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            Text("Request Bank Transfer Payout", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Available For Payout", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
                Text("${balance.availableBalance} ${balance.currency}", style = HealthogramTheme.typography.h5.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = HealthogramTheme.colors.borderLight)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Destination Account", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
                Text(payoutAccount?.provider ?: "Saudi National Bank (SNB)", style = HealthogramTheme.typography.body2.copy(fontWeight = FontWeight.Bold))
                Text(payoutAccount?.maskedDestination ?: "SA82 1000 0001 •••• •••• 4912", style = HealthogramTheme.typography.caption)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = amountStr,
            onValueChange = {
                amountStr = it
                errorMsg = null
            },
            label = { Text("Payout Amount (${balance.currency})") },
            placeholder = { Text("Minimum 100 SAR") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (errorMsg != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMsg!!, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.error)
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                val amt = amountStr.toDoubleOrNull() ?: 0.0
                if (amt < 100.0) {
                    errorMsg = "Minimum payout threshold is 100 SAR."
                    return@Button
                }
                if (amt > balance.availableBalance) {
                    errorMsg = "Amount exceeds available balance."
                    return@Button
                }
                onSubmitPayout(amt)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = HealthogramTheme.shapes.pill
        ) {
            Text("Confirm Payout Request", fontWeight = FontWeight.Bold)
        }
    }
}

// 7. SellerLedgerPage
@Composable
fun SellerLedgerPage(
    ledger: List<MarketplaceSellerLedgerEntry>,
    onBack: () -> Unit = {}
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Authoritative Accounting Ledger", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(ledger) { entry ->
            SellerLedgerRow(entry = entry)
        }
    }
}

// 8. SellerSupportPage
@Composable
fun SellerSupportPage(
    tickets: List<SellerSupportTicket>,
    onCreateTicket: (subject: String, desc: String, category: SellerSupportCategory) -> Unit,
    onBack: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SellerSupportCategory.COMPLIANCE) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = HealthogramTheme.colors.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Ticket")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                    Text("Seller Support & Compliance", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(tickets) { ticket ->
                SellerSupportCard(ticket = ticket)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Open Seller Support Ticket") },
            text = {
                Column {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Details") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subject.isNotBlank()) {
                            onCreateTicket(subject, description, selectedCategory)
                            showDialog = false
                            subject = ""
                            description = ""
                        }
                    }
                ) {
                    Text("Submit Ticket")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// 9. SellerSettingsPage
@Composable
fun SellerSettingsPage(
    profile: MarketplaceSellerProfile,
    onSaveProfile: (MarketplaceSellerProfile) -> Unit,
    onBack: () -> Unit = {}
) {
    var storeName by remember { mutableStateOf(profile.storeName) }
    var description by remember { mutableStateOf(profile.description) }
    var email by remember { mutableStateOf(profile.email) }
    var phone by remember { mutableStateOf(profile.phone) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            Text("Seller Account Settings", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = storeName,
            onValueChange = { storeName = it },
            label = { Text("Store Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Store Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Contact Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                onSaveProfile(profile.copy(storeName = storeName, description = description, email = email, phone = phone))
                onBack()
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = HealthogramTheme.shapes.pill
        ) {
            Text("Save Store Settings")
        }
    }
}
