package com.example.healthogram.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.integration.*

/**
 * HEALTHOGRAM 2.3 — STEP 51: ADMIN INTEGRATIONS MANAGEMENT CONSOLE
 * Implements Section 54: Real-time provider health dashboard covering
 * Payment Gateways, Delivery Fleets, FHIR R4, Health Connect, AI Studio, and Translation.
 */
@Composable
fun AdminIntegrationsManagementView(
    modifier: Modifier = Modifier
) {
    val registry = remember { ExternalProviderAdapterRegistry.getInstance() }
    val countryEngine = remember { CountryConfigurationEngine.getInstance() }
    val reconciliationService = remember { FinancialReconciliationService.getInstance() }

    val providerHealthMap by registry.providerHealthFlow.collectAsState()
    val reconciliationReports by reconciliationService.reportsFlow.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Providers, 1 = Financial Reconciliation, 2 = Country Config

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Platform Integrations & Providers",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Live telemetry, adapter statuses, and country-level governance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "STEP 51 ACTIVE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf("External Providers (${providerHealthMap.size})", "Financial Reconciliation", "Country Controls")
            tabs.forEachIndexed { index, label ->
                val isSelected = selectedTab == index
                Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .clickable { selectedTab = index }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> {
                // External Providers Telemetry List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(providerHealthMap.values.toList(), key = { it.providerId }) { provider ->
                        ProviderTelemetryCard(provider)
                    }
                }
            }
            1 -> {
                // Financial Reconciliation Hub
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Automated Double-Entry Reconciliation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Button(
                            onClick = { reconciliationService.runFullReconciliation("admin_manual") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Run Audit Check", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (reconciliationReports.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Financial Ledger Balanced", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Orders, payments, refunds, and double-entry ledger in equilibrium.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(reconciliationReports) { report ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "Report: ${report.reportId}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Surface(
                                                color = if (report.isBalanced) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = if (report.isBalanced) "BALANCED" else "DISCREPANCIES DETECTED",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (report.isBalanced) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Orders Checked: ${report.totalOrdersChecked} · Payments: ${report.totalPaymentsChecked} · Ledger Entries: ${report.totalLedgerEntriesChecked}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Sovereign Country Controls View
                val saConfig = countryEngine.getMarketplaceConfig("SA")
                val usConfig = countryEngine.getMarketplaceConfig("US")

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        CountryGovernanceCard(
                            config = saConfig,
                            onToggleIntl = {
                                countryEngine.setInternationalMarketplace("SA", !saConfig.internationalMarketplaceEnabled, "admin")
                            }
                        )
                    }
                    item {
                        CountryGovernanceCard(
                            config = usConfig,
                            onToggleIntl = {
                                countryEngine.setInternationalMarketplace("US", !usConfig.internationalMarketplaceEnabled, "admin")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderTelemetryCard(provider: ProviderHealthInfo) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (provider.serviceType) {
                            IntegrationServiceType.PAYMENT_GATEWAY -> Icons.Default.Payments
                            IntegrationServiceType.DELIVERY_PROVIDER -> Icons.Default.LocalShipping
                            IntegrationServiceType.FHIR_ENDPOINT -> Icons.Default.MedicalServices
                            IntegrationServiceType.HEALTH_CONNECT -> Icons.Default.Favorite
                            IntegrationServiceType.AI_STUDIO -> Icons.Default.Psychology
                            IntegrationServiceType.TRANSLATION -> Icons.Default.Translate
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = provider.providerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${provider.serviceType.label} · Market: ${provider.countryCode}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = when (provider.status) {
                        ProviderConnectionStatus.HEALTHY -> Color(0xFF10B981).copy(alpha = 0.15f)
                        ProviderConnectionStatus.SANDBOX_ACTIVE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ProviderConnectionStatus.DEGRADED -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                        ProviderConnectionStatus.REQUIRES_EXTERNAL_PROVIDER -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                        ProviderConnectionStatus.DISCONNECTED -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = provider.status.displayName.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (provider.status) {
                            ProviderConnectionStatus.HEALTHY -> Color(0xFF10B981)
                            ProviderConnectionStatus.SANDBOX_ACTIVE -> MaterialTheme.colorScheme.primary
                            ProviderConnectionStatus.DEGRADED -> Color(0xFFD97706)
                            ProviderConnectionStatus.REQUIRES_EXTERNAL_PROVIDER -> Color(0xFFD97706)
                            ProviderConnectionStatus.DISCONNECTED -> MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Latency: ${provider.latencyMs}ms · Error Rate: ${provider.errorRatePercent}%",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (provider.isSandbox) "Mode: SANDBOX" else "Mode: PRODUCTION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (provider.isSandbox) MaterialTheme.colorScheme.primary else Color(0xFF10B981)
                )
            }

            if (provider.externalProviderNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = provider.externalProviderNotes,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(4.dp)).padding(6.dp)
                )
            }
        }
    }
}

@Composable
private fun CountryGovernanceCard(
    config: CountryMarketplaceConfig,
    onToggleIntl: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${config.countryName} (${config.countryCode})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Currency: ${config.currency} · Tax: ${config.taxRatePercent}% · Return Window: ${config.returnPolicyDays} days",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = if (config.internationalMarketplaceEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (config.internationalMarketplaceEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.clickable { onToggleIntl() }
                ) {
                    Text(
                        text = if (config.internationalMarketplaceEnabled) "Intl Mkt: ON" else "Intl Mkt: LOCKED OFF",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (config.internationalMarketplaceEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Allowed Categories: ${config.allowedProductCategories.joinToString(", ")}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Restricted: ${config.restrictedProductCategories.joinToString(", ")}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
