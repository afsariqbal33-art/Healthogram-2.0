package com.example.healthogram.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.admin.*
import com.example.healthogram.core.AccountStatus
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationBadgeType
import com.example.healthogram.core.VerificationStatus
import java.text.SimpleDateFormat
import java.util.*

/**
 * 1. Overview Dashboard View.
 */
@Composable
fun AdminDashboardView(
    metrics: AdminDashboardMetrics,
    serviceHealthList: List<ServiceHealthRecord>,
    emergencyControls: List<EmergencyControlState>,
    onNavigateToSection: (AdminSection) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Emergency Banner if any control active
        val activeEmergency = emergencyControls.filter { it.isTriggered }
        if (activeEmergency.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Active Emergency Stop",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CRITICAL: ${activeEmergency.size} Emergency Kill Switch(es) Active",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = activeEmergency.joinToString { it.name },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            onClick = { onNavigateToSection(AdminSection.EMERGENCY_CONTROLS) }
                        ) {
                            Text("Manage")
                        }
                    }
                }
            }
        }

        // Metrics Grid (4 columns on wide screens, 2 on compact)
        item {
            Text(
                text = "Key Operational Metrics",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminMetricCard("Total Users", "${metrics.totalUsers}", Icons.Default.People, Color(0xFF2979FF), Modifier.weight(1f))
                AdminMetricCard("Active Users", "${metrics.activeUsers}", Icons.Default.Person, Color(0xFF00C853), Modifier.weight(1f))
                AdminMetricCard("Pending Verification", "${metrics.pendingVerifications}", Icons.Default.VerifiedUser, Color(0xFFFF9100), Modifier.weight(1f))
                AdminMetricCard("Open Reports", "${metrics.openReports}", Icons.Default.Shield, Color(0xFFFF5252), Modifier.weight(1f))
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminMetricCard("Orders", "${metrics.totalMarketplaceOrders}", Icons.Default.Storefront, Color(0xFF7C4DFF), Modifier.weight(1f))
                AdminMetricCard("Payments", "$${metrics.totalPaymentsMinor / 100}", Icons.Default.Payments, Color(0xFF00B0FF), Modifier.weight(1f))
                AdminMetricCard("Active Shipments", "${metrics.activeShipments}", Icons.Default.LocalShipping, Color(0xFF00E676), Modifier.weight(1f))
                AdminMetricCard("Security Alerts", "${metrics.healthSecurityEvents}", Icons.Default.Lock, Color(0xFFFF1744), Modifier.weight(1f))
            }
        }

        // System Health & Quick Actions
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // System Health column
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Service Infrastructure Health",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            TextButton(onClick = { onNavigateToSection(AdminSection.SYSTEM_HEALTH) }) {
                                Text("Details")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        serviceHealthList.take(5).forEach { record ->
                            ServiceHealthPill(record)
                        }
                    }
                }

                // Quick Navigation Hub
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Administrative Operations Hub",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val actions = listOf(
                            Triple("Verification Queue", "Review licensed institutions", AdminSection.VERIFICATION),
                            Triple("Social Moderation", "Resolve flagged content & reports", AdminSection.MODERATION),
                            Triple("Health Security Audit", "Inspect patient access consent logs", AdminSection.HEALTH_SECURITY),
                            Triple("Emergency Controls", "Platform maintenance & kill switches", AdminSection.EMERGENCY_CONTROLS)
                        )

                        actions.forEach { (title, sub, sec) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onNavigateToSection(sec) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                        Text(text = sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. Users Management View.
 */
@Composable
fun AdminUsersView(
    users: List<User>,
    onSelectUserToSuspend: (User) -> Unit,
    onRestoreUser: (User) -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val filtered = users.filter { u ->
        val catMatches = selectedCategoryFilter == "ALL" || u.accountType.name.equals(selectedCategoryFilter, ignoreCase = true)
        val searchMatches = searchQuery.isBlank() ||
                u.displayName.contains(searchQuery, ignoreCase = true) ||
                u.email.contains(searchQuery, ignoreCase = true) ||
                u.uid.contains(searchQuery, ignoreCase = true)
        catMatches && searchMatches
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Platform User Management",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Total Directory Accounts: ${users.size} • Enforces Country Scoping",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter users...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.width(260.dp).height(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ALL", "INDIVIDUAL", "DOCTOR", "CLINIC", "HOSPITAL", "LABORATORY").forEach { cat ->
                FilterChip(
                    selected = selectedCategoryFilter == cat,
                    onClick = { selectedCategoryFilter = cat },
                    label = { Text(cat) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered) { user ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.displayName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.displayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${user.accountType.name} • ${user.countryCode}",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${user.email} • UID: ${user.uid.take(12)}...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Status badge
                        Surface(
                            color = if (user.isSuspended) MaterialTheme.colorScheme.errorContainer else Color(0xFF00C853).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (user.isSuspended) "SUSPENDED" else user.accountStatus.name,
                                color = if (user.isSuspended) MaterialTheme.colorScheme.error else Color(0xFF00C853),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Action button
                        if (user.isSuspended) {
                            FilledTonalButton(onClick = { onRestoreUser(user) }) {
                                Text("Restore")
                            }
                        } else {
                            OutlinedButton(onClick = { onSelectUserToSuspend(user) }) {
                                Text("Enforce")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. Verification Center View.
 */
@Composable
fun AdminVerificationView(
    pendingUsers: List<User>,
    onReviewClick: (User) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Institutional & Professional Verification Center",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Doctors, Clinics, Hospitals, and Diagnostic Laboratories",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (pendingUsers.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00C853), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Verification Queue Clear", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("All pending medical licenses and institutional credentials have been reviewed.", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(pendingUsers) { user ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.displayName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${user.accountType.name} • ${user.countryName} (${user.countryCode})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Submitted Documents: Ministry Health License, Registration Cert",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Button(onClick = { onReviewClick(user) }) {
                                Text("Review & Decide")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4. Social Moderation & Appeals View.
 */
@Composable
fun AdminModerationView(
    reports: List<ModerationReport>,
    appeals: List<ModerationAppeal>,
    onResolveReport: (ModerationReport, actionTaken: Boolean, resolution: String) -> Unit,
    onResolveAppeal: (ModerationAppeal, approved: Boolean, resolution: String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Social Trust, Safety & Moderation Operations",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Posts, Reels, Stories, Comments, Live Streams, and Community Appeals",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Open Reports (${reports.count { it.status == ModerationReportStatus.PENDING }})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Appeals (${appeals.count { it.status == AppealStatus.SUBMITTED }})") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(reports) { report ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    color = if (report.priority == ModerationPriority.HIGH) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${report.contentType.name} • ${report.reasonCode.uppercase()} • ${report.priority.name}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = "Status: ${report.status.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = report.description,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Content ID: ${report.contentId} • Target User: ${report.reportedUid}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (report.status == ModerationReportStatus.PENDING) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = { onResolveReport(report, false, "Dismissed as within platform guidelines.") }
                                    ) {
                                        Text("Dismiss")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        onClick = { onResolveReport(report, true, "Content removed due to violation: ${report.reasonCode}") }
                                    ) {
                                        Text("Remove & Warn")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(appeals) { appeal ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Appeal for Action #${appeal.originalActionId}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = appeal.reason, style = MaterialTheme.typography.bodyMedium)

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(onClick = { onResolveAppeal(appeal, false, "Appeal rejected after review.") }) {
                                    Text("Reject")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = { onResolveAppeal(appeal, true, "Appeal accepted; restriction lifted.") }) {
                                    Text("Approve Appeal")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 5. Health Security & HIPAA/GDPR Compliance Center.
 */
@Composable
fun AdminHealthSecurityView(
    securityEvents: List<HealthSecurityEvent>,
    onResolveSecurityEvent: (HealthSecurityEvent, resolution: String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Health Security & Privacy Operations",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Strict Isolation: Clinical diagnosis and prescription records are never displayed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(securityEvents) { event ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                color = if (event.severity == HealthSecurityEventSeverity.HIGH) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "${event.eventType.name} • ${event.country}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = "Status: ${event.status.name}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = event.description,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Actor: ${event.actorUid} • Target: ${event.targetUid} • Session: ${event.healthAccessSessionId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (event.status == HealthSecurityEventStatus.DETECTED || event.status == HealthSecurityEventStatus.INVESTIGATING) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { onResolveSecurityEvent(event, "Marked as false positive after review.") }
                                ) {
                                    Text("False Positive")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { onResolveSecurityEvent(event, "Investigation complete; token revoked.") }
                                ) {
                                    Text("Resolve & Log")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 6. Emergency Controls Center (Owner / Super Admin).
 */
@Composable
fun AdminEmergencyControlsView(
    controls: List<EmergencyControlState>,
    onToggleControl: (EmergencyControlState) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Platform Emergency Operations & Kill Switches",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Highest authority platform switches. Activating requires MFA and documents an audit record.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(controls) { ctrl ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (ctrl.isTriggered) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (ctrl.isTriggered) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (ctrl.isTriggered) MaterialTheme.colorScheme.error else Color(0xFF00C853),
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ctrl.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (ctrl.isTriggered) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Key: ${ctrl.controlKey} • Status: ${if (ctrl.isTriggered) "TRIGGERED" else "NORMAL"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (ctrl.isTriggered) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (ctrl.isTriggered && ctrl.reason != null) {
                                Text(
                                    text = "Reason: ${ctrl.reason}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (ctrl.isTriggered) Color(0xFF00C853) else MaterialTheme.colorScheme.error
                            ),
                            onClick = { onToggleControl(ctrl) }
                        ) {
                            Text(if (ctrl.isTriggered) "Deactivate" else "Activate")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 7. Audit & Compliance Logs View.
 */
@Composable
fun AdminAuditLogsView(logs: List<AdminAuditLog>) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Platform Audit & Regulatory Compliance Ledger",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Append-only cryptographic record of all administrative interactions.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(logs) { log ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${log.module.uppercase()} • ${log.action.uppercase()}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = dateFormat.format(Date(log.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Actor: ${log.actorUid} (${log.actorRole}) • Target: ${log.targetType}:${log.targetId}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "Reason: ${log.reason}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
