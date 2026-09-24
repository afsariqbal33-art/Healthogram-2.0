package com.example.healthogram.ui.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.healthogram.owner.*

/**
 * Healthogram Step 17: Owner Executive Dashboard View.
 */
@Composable
fun OwnerDashboardView(
    service: PlatformConfigurationService,
    engine: OwnerControlEngine,
    onNavigateToSection: (OwnerNavigationSection) -> Unit,
    modifier: Modifier = Modifier
) {
    val globalConfig by service.globalConfig.collectAsState()
    val flags by service.featureFlags.collectAsState()
    val countries by service.countryConfigs.collectAsState()
    val emergencySwitches by service.emergencySwitches.collectAsState()
    val providers by service.providers.collectAsState()
    val auditLogs by service.auditLogs.collectAsState()

    val activeEmergencyCount = emergencySwitches.values.count { it.isTriggered }
    val activeCountriesCount = countries.values.count { it.active }
    val onFlagsCount = flags.values.count { it.status == FeatureFlagStatus.ON }
    val betaFlagsCount = flags.values.count { it.status == FeatureFlagStatus.BETA }

    var showMaintenanceDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Global Maintenance Notice if active
        if (globalConfig.maintenanceMode) {
            item {
                OwnerMaintenanceBanner(
                    message = globalConfig.maintenanceMessage,
                    onDisable = {
                        engine.setPlatformMaintenanceMode(
                            actorUid = "owner_root_001",
                            enabled = false,
                            message = "",
                            reason = "Maintenance window completed",
                            pin = "9900"
                        )
                    }
                )
            }
        }

        // Top Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OwnerStatusCard(
                    label = "Platform Status",
                    value = if (activeEmergencyCount > 0) "EMERGENCY" else if (globalConfig.maintenanceMode) "MAINTENANCE" else "ONLINE",
                    icon = if (activeEmergencyCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                    color = if (activeEmergencyCount > 0) Color(0xFFEF4444) else if (globalConfig.maintenanceMode) Color(0xFFF59E0B) else Color(0xFF10B981),
                    subtext = "${providers.count { it.status == "ONLINE" }} / ${providers.size} Services Healthy",
                    modifier = Modifier.weight(1f)
                )

                OwnerStatusCard(
                    label = "Active Countries",
                    value = "$activeCountriesCount / ${countries.size}",
                    icon = Icons.Default.Public,
                    color = Color(0xFF38BDF8),
                    subtext = if (globalConfig.internationalMarketplaceEnabled) "Intl Marketplace ON" else "Intl Marketplace OFF",
                    modifier = Modifier.weight(1f)
                )

                OwnerStatusCard(
                    label = "Feature Flags",
                    value = "${flags.size} Flags",
                    icon = Icons.Default.ToggleOn,
                    color = Color(0xFF818CF8),
                    subtext = "$onFlagsCount ON · $betaFlagsCount Beta",
                    modifier = Modifier.weight(1f)
                )

                OwnerStatusCard(
                    label = "Emergency Locks",
                    value = "$activeEmergencyCount Active",
                    icon = Icons.Default.Lock,
                    color = if (activeEmergencyCount > 0) Color(0xFFEF4444) else Color(0xFF64748B),
                    subtext = if (activeEmergencyCount > 0) "Immediate action required" else "All fail-safes nominal",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Subsystems Health & Quick Controls
        item {
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.5.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Sovereign Subsystem Telemetry", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Real-time operational status across Healthogram functional modules", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                        Button(
                            onClick = { onNavigateToSection(OwnerNavigationSection.FEATURE_FLAGS) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Manage All Flags", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val subsystems = listOf(
                        SubsystemHealth("Social & Feed Engine", globalConfig.socialEnabled, "posts, reels, stories", Icons.Default.Share),
                        SubsystemHealth("Health Passport & QR", globalConfig.healthPassportEnabled, "HIPAA/GDPR ledger, dynamic QR", Icons.Default.HealthAndSafety),
                        SubsystemHealth("Marketplace Core", globalConfig.marketplaceEnabled, "domestic retail, cart, checkout", Icons.Default.Storefront),
                        SubsystemHealth("International Marketplace", globalConfig.internationalMarketplaceEnabled, "cross-border commercial checkout", Icons.Default.FlightTakeoff),
                        SubsystemHealth("AI Studio & Vertex AI", globalConfig.aiEnabled, "Gemini 1.5 Pro, clinical summarizer", Icons.Default.Psychology),
                        SubsystemHealth("Universal Translation", globalConfig.translationEnabled, "live captions, bilingual calls", Icons.Default.Translate),
                        SubsystemHealth("Encrypted Messaging", globalConfig.messagingEnabled, "1-on-1, clinical groups", Icons.Default.Forum),
                        SubsystemHealth("Telehealth Video Calling", globalConfig.callingEnabled, "WebRTC, Agora infrastructure", Icons.Default.VideoCall)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        subsystems.chunked(2).forEach { row ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                row.forEach { item ->
                                    SubsystemHealthPill(item, Modifier.weight(1f))
                                }
                                if (row.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Audit Logs Stream
        item {
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.5.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recent Owner Configuration Audits", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        TextButton(onClick = { onNavigateToSection(OwnerNavigationSection.AUDIT_LOGS) }) {
                            Text("View Full Ledger", fontSize = 11.sp, color = Color(0xFF38BDF8))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (auditLogs.isEmpty()) {
                        Text("No audit events recorded yet.", fontSize = 12.sp, color = Color(0xFF64748B))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            auditLogs.take(5).forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = log.action, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF1F5F9))
                                        Text(text = "${log.targetType}: ${log.targetId} · ${log.reason}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    }
                                    Text(
                                        text = log.ownerRole,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class SubsystemHealth(
    val title: String,
    val enabled: Boolean,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun SubsystemHealthPill(item: SubsystemHealth, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, if (item.enabled) Color(0xFF10B981).copy(alpha = 0.3f) else Color(0xFFEF4444).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = if (item.enabled) Color(0xFF38BDF8) else Color(0xFF64748B),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = item.description, fontSize = 10.sp, color = Color(0xFF94A3B8))
            }
            Surface(
                color = if (item.enabled) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (item.enabled) "ACTIVE" else "OFF",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = if (item.enabled) Color(0xFF10B981) else Color(0xFFEF4444),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
