package com.example.healthogram.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.admin.*

/**
 * Navigation Sections for the Admin Control Panel.
 */
enum class AdminSection(val displayName: String, val icon: ImageVector, val requiredPermission: String) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "dashboard.read"),
    USERS("Users", Icons.Default.People, "users.read"),
    VERIFICATION("Verification", Icons.Default.VerifiedUser, "verification.read"),
    MODERATION("Social Moderation", Icons.Default.Shield, "moderation.read"),
    HEALTH_SECURITY("Health Security", Icons.Default.Lock, "health_security.read"),
    MARKETPLACE("Marketplace", Icons.Default.Storefront, "marketplace.read"),
    PAYMENTS("Payments & Finance", Icons.Default.Payments, "payments.read"),
    DELIVERY("Delivery & Fleet", Icons.Default.LocalShipping, "delivery.read"),
    AI_STUDIO("AI & Translation", Icons.Default.Psychology, "ai.read"),
    COUNTRIES("Countries", Icons.Default.Public, "countries.manage"),
    FEATURE_FLAGS("Feature Flags", Icons.Default.Flag, "feature_flags.manage"),
    EMERGENCY_CONTROLS("Emergency Controls", Icons.Default.Warning, "emergency.manage"),
    SYSTEM_HEALTH("System Health", Icons.Default.MonitorHeart, "system_health.read"),
    AUDIT_LOGS("Audit Logs", Icons.Default.HistoryEdu, "audit_logs.read"),
    SETTINGS("Settings", Icons.Default.Settings, "dashboard.read")
}

/**
 * Top Navigation Bar for Healthogram Admin Operations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopBar(
    currentAdmin: AdminUser,
    availableAdmins: List<AdminUser>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAdminSelected: (AdminUser) -> Unit,
    onReauthClick: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Exit Admin Panel"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "HEALTHOGRAM",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "OPS CENTER",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Role: ${currentAdmin.roleId.replace("_", " ").uppercase()} • Scope: ${if (currentAdmin.countryScope.isEmpty()) "GLOBAL" else currentAdmin.countryScope.joinToString()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Global search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search users, reports, orders...", style = MaterialTheme.typography.bodySmall) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .height(44.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Re-Auth / MFA Indicator
            Button(
                onClick = onReauthClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "MFA",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("MFA Active", style = MaterialTheme.typography.labelMedium)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Role switcher menu
            var showMenu by remember { mutableStateOf(false) }
            Box {
                FilterChip(
                    selected = true,
                    onClick = { showMenu = true },
                    label = {
                        Text(
                            text = currentAdmin.displayName.take(15),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Switch Admin"
                        )
                    }
                )

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    Text(
                        text = "Switch Admin Persona (Least Privilege):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    HorizontalDivider()
                    availableAdmins.forEach { admin ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = admin.displayName,
                                        fontWeight = if (admin.uid == currentAdmin.uid) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = "${admin.roleId} • ${if (admin.countryScope.isEmpty()) "Global" else admin.countryScope.joinToString()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onAdminSelected(admin)
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modern Desktop / Tablet Sidebar Navigation Rail.
 */
@Composable
fun AdminSidebar(
    selectedSection: AdminSection,
    accessibleSections: List<AdminSection>,
    onSectionSelected: (AdminSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier.width(220.dp).fillMaxHeight()
    ) {
        Column(modifier = Modifier.fillMaxHeight().padding(vertical = 12.dp, horizontal = 8.dp)) {
            Text(
                text = "PLATFORM MODULES",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(accessibleSections) { section ->
                    val isSelected = section == selectedSection
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable { onSectionSelected(section) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = section.icon,
                                contentDescription = section.displayName,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = section.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00C853))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "App Check: VERIFIED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Overview Dashboard Metric Card.
 */
@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Service Uptime & Health Badge.
 */
@Composable
fun ServiceHealthPill(record: ServiceHealthRecord) {
    val (statusColor, statusText) = when (record.status) {
        ServiceUptimeStatus.OPERATIONAL -> Color(0xFF00C853) to "Operational"
        ServiceUptimeStatus.DEGRADED -> Color(0xFFFFB300) to "Degraded"
        ServiceUptimeStatus.DOWN -> Color(0xFFD50000) to "Outage"
        ServiceUptimeStatus.MAINTENANCE -> Color(0xFF2979FF) to "Maintenance"
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = record.serviceName,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${record.latencyMs}ms",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = statusText,
                    color = statusColor,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
