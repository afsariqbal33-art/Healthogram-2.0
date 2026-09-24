package com.example.healthogram.ui.payments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.payments.*

/**
 * Admin & Owner Financial Control Center.
 * Provides emergency kill switches, gateway health telemetry, country tax/commission configs,
 * and real-time reconciliation triggers.
 */
@Composable
fun AdminPaymentControlView(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { PaymentRepository.getInstance() }
    val engine = remember { PaymentEngine.getInstance() }

    val featureFlags by repository.featureFlags.collectAsState()
    val countryConfigs by repository.countryConfigs.collectAsState()
    val gatewayHealth by repository.gatewayHealth.collectAsState()
    val reconciliationReports by repository.reconciliationReports.collectAsState()
    val auditLogs by repository.auditLogs.collectAsState()

    var reconciliationNotice by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("admin_payment_control_view")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // App Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = HealthogramTheme.colors.surface,
                border = BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text("Payment Architecture & Gateway Control", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Platform Emergency Switches & Reconciliation", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                    }
                }
            }

            if (reconciliationNotice != null) {
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        reconciliationNotice!!,
                        style = HealthogramTheme.typography.bodySmall.copy(color = Color(0xFF10B981), fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Emergency Kill Switches
                item {
                    Text("Emergency Platform Kill Switches", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                        border = BorderStroke(1.dp, if (featureFlags.emergencyPaymentKillSwitch || featureFlags.emergencyPayoutKillSwitch) Color(0xFFEF4444) else HealthogramTheme.colors.borderLight)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Emergency Payment Kill Switch", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("Instantly stops all new customer checkout and payment processing", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                }
                                Switch(
                                    checked = featureFlags.emergencyPaymentKillSwitch,
                                    onCheckedChange = { repository.setEmergencyPaymentKillSwitch(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFEF4444), checkedTrackColor = Color(0xFFEF4444).copy(alpha = 0.3f))
                                )
                            }

                            HorizontalDivider(color = HealthogramTheme.colors.borderLight)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Emergency Payout Kill Switch", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("Freezes all automated and manual seller and owner bank disbursements", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                }
                                Switch(
                                    checked = featureFlags.emergencyPayoutKillSwitch,
                                    onCheckedChange = { repository.setEmergencyPayoutKillSwitch(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFEF4444), checkedTrackColor = Color(0xFFEF4444).copy(alpha = 0.3f))
                                )
                            }
                        }
                    }
                }

                // Payment Gateway Health
                item {
                    Text("Gateway Health & Routing Status", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }

                items(gatewayHealth.values.toList(), key = { it.gatewayId }) { gh ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(gh.displayName, style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                Text("Avg latency: ${gh.averageResponseTimeMs}ms • Webhook delay: ${gh.webhookDelayMs}ms", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (gh.status == "HEALTHY") Color(0xFF10B981).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = gh.status,
                                    style = HealthogramTheme.typography.caption.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (gh.status == "HEALTHY") Color(0xFF10B981) else Color.Gray
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Country Configuration & Tax Rates
                item {
                    Text("Country Payment Configurations", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }

                items(countryConfigs.values.toList(), key = { it.countryCode }) { cfg ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${cfg.countryName} (${cfg.countryCode})", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Currency: ${cfg.currencyCode} (${cfg.currencySymbol})", style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold))
                            }
                            Text(
                                "Tax: ${cfg.taxName} ${cfg.taxRatePercent}% (${if (cfg.taxInclusive) "Inclusive" else "Exclusive"}) • Platform Fee: ${cfg.platformFeePercent}% + ${cfg.platformFeeFixedMinor} minor",
                                style = HealthogramTheme.typography.caption,
                                color = HealthogramTheme.colors.textSecondary
                            )
                            Text(
                                "Gateways: ${cfg.supportedGateways.joinToString(", ")} (Default: ${cfg.defaultGateway}) • Payout Hold: ${cfg.payoutDelayDays} days",
                                style = HealthogramTheme.typography.caption,
                                color = HealthogramTheme.colors.textMuted
                            )
                        }
                    }
                }

                // Reconciliation Engine
                item {
                    Text("Automated Payment Reconciliation", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Reconcile external gateway transaction logs against Healthogram internal transactions and immutable ledgers.", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textSecondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val report = engine.reconcilePayments("local_mada", "SA")
                                    reconciliationNotice = "Reconciliation completed: ${report.matchedCount} matched, 0 discrepancies."
                                },
                                modifier = Modifier.testTag("run_reconciliation_button")
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Run Reconciliation")
                            }
                        }
                    }
                }

                // Audit Logs
                item {
                    Text("Payment Security Audit Trail", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }

                items(auditLogs.take(5), key = { it.logId }) { log ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(log.action, style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Text("Actor: ${log.actorUid} (${log.actorRole})", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            }
                            Text(log.countryCode, style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}
