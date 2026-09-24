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
 * Platform Admin Delivery Operations Dashboard.
 * Central command for Country Delivery Configurations, Zones, Rate Cards,
 * Carrier SLA Monitoring, Emergency Kill Switches, and Scheduled Reconciliation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDeliveryDashboardView(
    onBack: () -> Unit = {},
    onOpenOwnerAnalytics: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { DeliveryRepository.getInstance() }
    val engine = remember { DeliveryEngine(repository) }

    val countryConfigs by repository.countryConfigs.collectAsState()
    val zones by repository.deliveryZones.collectAsState()
    val rates by repository.shippingRates.collectAsState()
    val featureFlags by repository.featureFlags.collectAsState()
    val partners by repository.partners.collectAsState()
    val auditLogs by repository.auditLogs.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Countries & Kill Switches, 1: Zones & Rates, 2: Partners, 3: Audits & Sync
    var actionStatusMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Admin Delivery Control Hub",
                            style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.textPrimary
                        )
                        Text(
                            text = "Logistics Orchestration & Emergency Controls",
                            style = HealthogramTheme.typography.bodySmall,
                            color = HealthogramTheme.colors.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_delivery_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenOwnerAnalytics, modifier = Modifier.testTag("admin_analytics_button")) {
                        Icon(Icons.Default.BarChart, contentDescription = "Financial Analytics", tint = HealthogramTheme.colors.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HealthogramTheme.colors.surface)
            )
        },
        containerColor = HealthogramTheme.colors.background,
        modifier = modifier.testTag("admin_delivery_dashboard_view")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = HealthogramTheme.colors.surface,
                contentColor = HealthogramTheme.colors.primary
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Countries & Safety") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Zones & Rates") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Carriers (${partners.size})") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Audit & Reconcile") })
            }

            if (actionStatusMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.primaryContainer),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = HealthogramTheme.colors.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(actionStatusMessage!!, style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.onPrimaryContainer)
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    // Countries & Emergency Kill Switches
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Kill Switch Card
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Emergency Delivery Kill Switches",
                                            style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF991B1B)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Global Emergency Delivery Stop", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                            Text("Pauses all fulfillment dispatch platform-wide.", style = HealthogramTheme.typography.labelSmall, color = HealthogramTheme.colors.textSecondary)
                                        }
                                        Switch(
                                            checked = featureFlags.emergencyDeliveryStop,
                                            onCheckedChange = {
                                                repository.updateFeatureFlags(featureFlags.copy(emergencyDeliveryStop = it))
                                                actionStatusMessage = "Emergency delivery stop is now: ${if (it) "ENABLED" else "DISABLED"}"
                                            },
                                            modifier = Modifier.testTag("emergency_delivery_stop_switch")
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Cross-Border / International Delivery", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                            Text("Mandated OFF initially per architecture policy.", style = HealthogramTheme.typography.labelSmall, color = HealthogramTheme.colors.textSecondary)
                                        }
                                        Switch(
                                            checked = featureFlags.internationalDeliveryEnabled,
                                            onCheckedChange = {
                                                repository.updateFeatureFlags(featureFlags.copy(internationalDeliveryEnabled = it))
                                                actionStatusMessage = "International delivery toggle updated: $it"
                                            },
                                            modifier = Modifier.testTag("international_delivery_switch")
                                        )
                                    }
                                }
                            }
                        }

                        // Country configs list
                        item {
                            Text(
                                text = "Country Delivery Configurations",
                                style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = HealthogramTheme.colors.textPrimary
                            )
                        }

                        items(countryConfigs.values.toList()) { config ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${config.countryName} (${config.countryCode})",
                                                style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = HealthogramTheme.colors.textPrimary
                                            )
                                        }
                                        Switch(
                                            checked = config.deliveryEnabled,
                                            onCheckedChange = {
                                                repository.updateCountryConfig(config.copy(deliveryEnabled = it))
                                                actionStatusMessage = "Updated delivery availability for ${config.countryName}"
                                            }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Default Carrier: ${config.defaultDeliveryProvider.uppercase()} • Currency: ${config.defaultCurrency}", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textSecondary)
                                    Text("Carriers: ${config.supportedDeliveryProviders.joinToString()}", style = HealthogramTheme.typography.labelSmall, color = HealthogramTheme.colors.textSecondary)

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("Same-Day: ${if (config.sameDayEnabled) "ON" else "OFF"}", style = HealthogramTheme.typography.labelSmall) }
                                        )
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("POD OTP: ${if (config.proofOfDeliveryRequired) "YES" else "NO"}", style = HealthogramTheme.typography.labelSmall) }
                                        )
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("COD: ${if (config.cashOnDeliveryEnabled) "ENABLED" else "DISABLED"}", style = HealthogramTheme.typography.labelSmall) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Delivery Zones & Rate Configs
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Active Delivery Zones",
                                style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = HealthogramTheme.colors.textPrimary
                            )
                        }

                        items(zones.values.toList()) { zone ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${zone.zoneName} (${zone.zoneCode})",
                                            style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = HealthogramTheme.colors.textPrimary
                                        )
                                        Text(
                                            text = zone.countryCode,
                                            style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = HealthogramTheme.colors.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("City: ${zone.city} • Region: ${zone.region}", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textSecondary)
                                    Text("SLA: ${zone.standardDeliveryDays} days • Express: ${zone.expressDeliveryAvailable} • Same-Day: ${zone.sameDayAvailable}", style = HealthogramTheme.typography.labelSmall, color = HealthogramTheme.colors.textSecondary)
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Shipping Rate Cards",
                                style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = HealthogramTheme.colors.textPrimary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(rates) { rate ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${rate.serviceType.label} • ${rate.deliveryProvider.uppercase()}",
                                            style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = HealthogramTheme.colors.textPrimary
                                        )
                                        Text(
                                            text = "Country: ${rate.countryCode} • Mode: ${rate.deliveryMode.label}",
                                            style = HealthogramTheme.typography.labelSmall,
                                            color = HealthogramTheme.colors.textSecondary
                                        )
                                    }
                                    Text(
                                        text = DeliveryCustomActions.formatShippingAmount(rate.baseFeeMinor, rate.currencyCode),
                                        style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = HealthogramTheme.colors.primary
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Carrier Partners
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(partners.values.toList()) { partner ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(partner.partnerName, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.textPrimary)
                                        Surface(
                                            color = if (partner.active) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = if (partner.active) "ACTIVE" else "DISABLED",
                                                style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (partner.active) Color(0xFF047857) else Color(0xFFB91C1C),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Integration: ${partner.integrationType} • Regions: ${partner.serviceRegions.joinToString()}", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textSecondary)
                                    Text("Services: ${partner.supportedServices.joinToString { it.label }}", style = HealthogramTheme.typography.labelSmall, color = HealthogramTheme.colors.textSecondary)
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Audit logs & Scheduled Reconciliation Runner
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Scheduled Automation Jobs", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                val delayed = engine.detectDelayedShipments()
                                                actionStatusMessage = "Delay scan completed. ${delayed.size} shipments flagged as delayed."
                                            },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Run Delay Scan")
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                val rep = engine.reconcileProvider("internal_fleet")
                                                actionStatusMessage = "Reconciliation report generated for internal_fleet. Discrepancies: ${rep.discrepancyCount}"
                                            },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Reconcile Carriers")
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text("Delivery Audit Trail", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        items(auditLogs.reversed()) { log ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(log.action, style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                        Text(log.actorRole, style = HealthogramTheme.typography.labelSmall, color = HealthogramTheme.colors.primary)
                                    }
                                    Text("Order: ${log.orderId} • Shipment: ${log.shipmentId}", style = HealthogramTheme.typography.labelSmall, color = HealthogramTheme.colors.textSecondary)
                                    Text("Transition: ${log.previousStatus} -> ${log.newStatus}", style = HealthogramTheme.typography.labelSmall, color = HealthogramTheme.colors.textSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
