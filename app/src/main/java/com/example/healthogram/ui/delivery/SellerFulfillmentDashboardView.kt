package com.example.healthogram.ui.delivery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.delivery.*
import com.example.healthogram.designsystem.HealthogramTheme

/**
 * Seller Fulfillment Pipeline Dashboard.
 * Allows sellers to monitor incoming orders, track packaging state,
 * print manifests, and coordinate with courier partners.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerFulfillmentDashboardView(
    sellerUid: String = "seller_alnoor_pharmacy",
    onBack: () -> Unit = {},
    onOpenFulfillment: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { DeliveryRepository.getInstance() }
    val fulfillmentsMap by repository.fulfillments.collectAsState()
    val shipmentsMap by repository.shipments.collectAsState()

    val myFulfillments = remember(fulfillmentsMap, sellerUid) {
        fulfillmentsMap.values.filter { it.sellerUid == sellerUid }
    }

    var selectedFilter by remember { mutableStateOf<FulfillmentStatus?>(null) }
    val filteredList = remember(myFulfillments, selectedFilter) {
        if (selectedFilter == null) myFulfillments else myFulfillments.filter { it.fulfillmentStatus == selectedFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Seller Fulfillment Hub",
                            style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.textPrimary
                        )
                        Text(
                            text = "Al-Noor Central Pharmacy • Active Pipeline",
                            style = HealthogramTheme.typography.bodySmall,
                            color = HealthogramTheme.colors.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("seller_hub_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HealthogramTheme.colors.surface)
            )
        },
        containerColor = HealthogramTheme.colors.background,
        modifier = modifier.testTag("seller_fulfillment_dashboard_view")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Pipeline Metrics Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBadge(
                        label = "To Pack",
                        count = myFulfillments.count { it.fulfillmentStatus == FulfillmentStatus.CONFIRMED || it.fulfillmentStatus == FulfillmentStatus.PROCESSING },
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                    MetricBadge(
                        label = "Ready Pickup",
                        count = myFulfillments.count { it.fulfillmentStatus == FulfillmentStatus.READY_FOR_PICKUP },
                        color = HealthogramTheme.colors.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBadge(
                        label = "In Transit",
                        count = myFulfillments.count { it.fulfillmentStatus == FulfillmentStatus.IN_TRANSIT || it.fulfillmentStatus == FulfillmentStatus.OUT_FOR_DELIVERY },
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                    MetricBadge(
                        label = "Delivered",
                        count = myFulfillments.count { it.fulfillmentStatus == FulfillmentStatus.DELIVERED },
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Status Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All (${myFulfillments.size})") }
                    )
                    FilterChip(
                        selected = selectedFilter == FulfillmentStatus.OUT_FOR_DELIVERY,
                        onClick = { selectedFilter = FulfillmentStatus.OUT_FOR_DELIVERY },
                        label = { Text("Out for Delivery") }
                    )
                    FilterChip(
                        selected = selectedFilter == FulfillmentStatus.DELIVERED,
                        onClick = { selectedFilter = FulfillmentStatus.DELIVERED },
                        label = { Text("Completed") }
                    )
                }
            }

            // 3. Suborder Fulfillments List
            items(filteredList) { fulfillment ->
                val shipment = fulfillment.shipmentId?.let { shipmentsMap[it] }

                Card(
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Order: ${fulfillment.orderId}",
                                style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = HealthogramTheme.colors.textPrimary
                            )
                            Surface(
                                color = HealthogramTheme.colors.primary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = fulfillment.fulfillmentStatus.label,
                                    style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = HealthogramTheme.colors.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Customer: Dr. Sarah Al-Ahmad • Riyadh",
                            style = HealthogramTheme.typography.bodySmall,
                            color = HealthogramTheme.colors.textSecondary
                        )
                        Text(
                            text = "Destination: ${fulfillment.deliveryAddressReference}",
                            style = HealthogramTheme.typography.bodySmall,
                            color = HealthogramTheme.colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Provider: ${fulfillment.deliveryProvider.replace("_", " ").uppercase()}",
                                style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = HealthogramTheme.colors.textSecondary
                            )
                            if (shipment != null) {
                                Text(
                                    text = "Tracking: ${shipment.trackingNumber}",
                                    style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = HealthogramTheme.colors.textPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onOpenFulfillment(fulfillment.fulfillmentId) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Manage & Label")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBadge(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = label,
                style = HealthogramTheme.typography.labelSmall,
                color = HealthogramTheme.colors.textSecondary
            )
        }
    }
}
