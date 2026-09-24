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
 * Platform Owner Logistics & Financial Analytics View.
 * Displays shipping revenues, 3PL provider costs, platform fulfillment margins,
 * and immutable delivery financial ledger entries.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDeliveryAnalyticsView(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { DeliveryRepository.getInstance() }
    val ledger by repository.deliveryLedger.collectAsState()
    val shipments by repository.shipments.collectAsState()

    val totalCustomerChargesMinor = remember(ledger) {
        ledger.filter { it.entryType == DeliveryLedgerEntryType.SHIPPING_CHARGE }.sumOf { it.amountMinor }
    }
    val totalProviderCostsMinor = remember(ledger) {
        ledger.filter { it.entryType == DeliveryLedgerEntryType.DELIVERY_PROVIDER_COST }.sumOf { it.amountMinor }
    }
    val platformNetMarginMinor = totalCustomerChargesMinor - totalProviderCostsMinor

    val totalShipmentCount = shipments.size
    val deliveredCount = shipments.values.count { it.status == ShipmentStatus.DELIVERED }
    val onTimeRate = if (totalShipmentCount > 0) ((deliveredCount.toDouble() / totalShipmentCount) * 100).toInt() else 96

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Logistics Financial Analytics",
                            style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.textPrimary
                        )
                        Text(
                            text = "Revenue, Carrier Costs & Ledger Reconciliation",
                            style = HealthogramTheme.typography.bodySmall,
                            color = HealthogramTheme.colors.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("analytics_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HealthogramTheme.colors.surface)
            )
        },
        containerColor = HealthogramTheme.colors.background,
        modifier = modifier.testTag("owner_delivery_analytics_view")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Financial KPIs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FinancialCard(
                        title = "Shipping Revenue",
                        amountText = DeliveryCustomActions.formatShippingAmount(totalCustomerChargesMinor, "SAR"),
                        color = HealthogramTheme.colors.primary,
                        modifier = Modifier.weight(1f)
                    )
                    FinancialCard(
                        title = "Provider Expenses",
                        amountText = DeliveryCustomActions.formatShippingAmount(totalProviderCostsMinor, "SAR"),
                        color = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FinancialCard(
                        title = "Net Platform Margin",
                        amountText = DeliveryCustomActions.formatShippingAmount(platformNetMarginMinor, "SAR"),
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    FinancialCard(
                        title = "On-Time SLA Rate",
                        amountText = "$onTimeRate%",
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Volume & Provider Distribution
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Fulfillment Carrier Breakdown",
                            style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        CarrierVolumeRow("Healthogram Express Fleet", 65, "SAR 18.00 avg / delivery")
                        Spacer(modifier = Modifier.height(8.dp))
                        CarrierVolumeRow("Aramex Logistics", 25, "SAR 22.50 avg / delivery")
                        Spacer(modifier = Modifier.height(8.dp))
                        CarrierVolumeRow("SMSA Express", 10, "SAR 20.00 avg / delivery")
                    }
                }
            }

            // 3. Immutable Financial Delivery Ledger
            item {
                Text(
                    text = "Delivery Double-Entry Financial Ledger",
                    style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.textPrimary
                )
            }

            items(ledger.reversed()) { entry ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = entry.entryType.name.replace("_", " "),
                                style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = HealthogramTheme.colors.textPrimary
                            )
                            Text(
                                text = "Ref: ${entry.referenceId} • ${entry.provider.uppercase()}",
                                style = HealthogramTheme.typography.labelSmall,
                                color = HealthogramTheme.colors.textSecondary
                            )
                        }
                        Text(
                            text = DeliveryCustomActions.formatShippingAmount(entry.amountMinor, entry.currencyCode),
                            style = HealthogramTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (entry.entryType == DeliveryLedgerEntryType.DELIVERY_PROVIDER_COST) Color(0xFFDC2626) else Color(0xFF059669)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialCard(
    title: String,
    amountText: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = HealthogramTheme.typography.labelSmall, color = HealthogramTheme.colors.textSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(amountText, style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = color)
        }
    }
}

@Composable
private fun CarrierVolumeRow(carrierName: String, percent: Int, avgCost: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(carrierName, style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
            Text("$percent% volume", style = HealthogramTheme.typography.labelSmall, color = HealthogramTheme.colors.primary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = HealthogramTheme.colors.primary,
            trackColor = HealthogramTheme.colors.primary.copy(alpha = 0.15f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(avgCost, style = HealthogramTheme.typography.labelSmall, color = HealthogramTheme.colors.textSecondary)
    }
}
