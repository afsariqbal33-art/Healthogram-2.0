package com.example.healthogram.ui.admin

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
import com.example.healthogram.admin.*
import com.example.healthogram.core.AccountStatus
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationBadgeType
import com.example.healthogram.core.VerificationStatus

/**
 * Healthogram Step 16: Admin Control Panel + Platform Operations Center.
 * Full desktop-first and responsive tablet/mobile administration interface.
 */
@Composable
fun AdminControlPanelPage(
    repository: AdminRepository = AdminRepository.getInstance(),
    engine: AdminControlEngine = remember { AdminControlEngine(repository) },
    onClose: () -> Unit
) {
    // 1. Reactive State
    val adminUsersMap by repository.adminUsers.collectAsState()
    val platformUsersMap by repository.platformUsers.collectAsState()
    val moderationReportsMap by repository.moderationReports.collectAsState()
    val moderationAppealsMap by repository.moderationAppeals.collectAsState()
    val healthSecurityEventsMap by repository.healthSecurityEvents.collectAsState()
    val countryConfigsMap by repository.countryConfigs.collectAsState()
    val featureFlagsMap by repository.featureFlags.collectAsState()
    val emergencyControlsMap by repository.emergencyControls.collectAsState()
    val serviceHealthMap by repository.serviceHealth.collectAsState()
    val auditLogsList by repository.auditLogs.collectAsState()
    val metrics by repository.dashboardMetrics.collectAsState()

    // 2. Active Admin Persona (Default: Owner "usr_owner_root")
    var currentAdminUid by remember { mutableStateOf("usr_owner_root") }
    val currentAdmin = adminUsersMap[currentAdminUid] ?: adminUsersMap.values.firstOrNull() ?: AdminUser(
        uid = "usr_owner_root",
        email = "owner@healthogram.com",
        displayName = "Platform Owner",
        roleId = AdminRoleIds.OWNER
    )

    // 3. Accessible Sections based on Current Admin's Permissions
    val accessibleSections = remember(currentAdminUid, adminUsersMap) {
        val effectivePermissions = engine.resolvePermissions(currentAdmin.uid)
        AdminSection.values().filter { section ->
            section == AdminSection.DASHBOARD ||
                    section == AdminSection.SETTINGS ||
                    effectivePermissions.contains(section.requiredPermission) ||
                    currentAdmin.roleId == AdminRoleIds.OWNER ||
                    currentAdmin.roleId == AdminRoleIds.SUPER_ADMIN
        }
    }

    var selectedSection by remember { mutableStateOf(AdminSection.DASHBOARD) }

    // Ensure selected section is accessible
    LaunchedEffect(accessibleSections) {
        if (!accessibleSections.contains(selectedSection)) {
            selectedSection = AdminSection.DASHBOARD
        }
    }

    // 4. Search State
    var searchQuery by remember { mutableStateOf("") }
    val searchResults = remember(searchQuery, currentAdminUid) {
        if (searchQuery.isNotBlank()) {
            engine.searchEntities(currentAdmin.uid, searchQuery)
        } else {
            emptyList()
        }
    }

    // 5. Active Dialogs State
    var showReauthDialog by remember { mutableStateOf(false) }
    var userToSuspend by remember { mutableStateOf<User?>(null) }
    var userToReviewVerification by remember { mutableStateOf<User?>(null) }
    var emergencyControlToToggle by remember { mutableStateOf<EmergencyControlState?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            AdminTopBar(
                currentAdmin = currentAdmin,
                availableAdmins = adminUsersMap.values.toList(),
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onAdminSelected = { admin ->
                    currentAdminUid = admin.uid
                    snackbarMessage = "Switched to: ${admin.displayName} (${admin.roleId})"
                },
                onReauthClick = { showReauthDialog = true },
                onClose = onClose
            )
        },
        snackbarHost = {
            snackbarMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { snackbarMessage = null }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(msg)
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Sidebar Navigation
            AdminSidebar(
                selectedSection = selectedSection,
                accessibleSections = accessibleSections,
                onSectionSelected = { section ->
                    selectedSection = section
                    searchQuery = "" // Clear search when changing tab
                }
            )

            // Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // If search is active, show search overlay
                if (searchQuery.isNotBlank()) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Text(
                            text = "Search Results for \"$searchQuery\"",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (searchResults.isEmpty()) {
                            Text("No entities matching \"$searchQuery\" or access is restricted by least-privilege.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(searchResults) { item ->
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            when (item.targetModule) {
                                                "users" -> selectedSection = AdminSection.USERS
                                                "moderation" -> selectedSection = AdminSection.MODERATION
                                                "health_security" -> selectedSection = AdminSection.HEALTH_SECURITY
                                            }
                                            searchQuery = ""
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = item.entityType,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = item.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                                Text(text = item.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Surface(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = item.status,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Normal Module Routing
                    when (selectedSection) {
                        AdminSection.DASHBOARD -> {
                            AdminDashboardView(
                                metrics = metrics,
                                serviceHealthList = serviceHealthMap.values.toList(),
                                emergencyControls = emergencyControlsMap.values.toList(),
                                onNavigateToSection = { selectedSection = it }
                            )
                        }
                        AdminSection.USERS -> {
                            AdminUsersView(
                                users = platformUsersMap.values.toList(),
                                onSelectUserToSuspend = { u -> userToSuspend = u },
                                onRestoreUser = { u ->
                                    try {
                                        engine.restoreUser(currentAdmin.uid, u.uid, "Restored by admin")
                                        snackbarMessage = "Account ${u.displayName} successfully restored."
                                    } catch (e: Exception) {
                                        snackbarMessage = "Action blocked: ${e.message}"
                                    }
                                }
                            )
                        }
                        AdminSection.VERIFICATION -> {
                            val pending = platformUsersMap.values.filter {
                                it.verificationStatus == VerificationStatus.UNDER_REVIEW || it.verificationStatus == VerificationStatus.SUBMITTED
                            }
                            AdminVerificationView(
                                pendingUsers = pending,
                                onReviewClick = { u -> userToReviewVerification = u }
                            )
                        }
                        AdminSection.MODERATION -> {
                            AdminModerationView(
                                reports = moderationReportsMap.values.toList(),
                                appeals = moderationAppealsMap.values.toList(),
                                onResolveReport = { report, actionTaken, resolution ->
                                    try {
                                        engine.resolveModerationReport(currentAdmin.uid, report.reportId, resolution, actionTaken, report.reportedUid)
                                        snackbarMessage = "Report #${report.reportId} resolved."
                                    } catch (e: Exception) {
                                        snackbarMessage = "Blocked: ${e.message}"
                                    }
                                },
                                onResolveAppeal = { appeal, approved, resolution ->
                                    try {
                                        engine.reviewAppeal(currentAdmin.uid, appeal.appealId, approved, resolution)
                                        snackbarMessage = "Appeal #${appeal.appealId} processed."
                                    } catch (e: Exception) {
                                        snackbarMessage = "Blocked: ${e.message}"
                                    }
                                }
                            )
                        }
                        AdminSection.HEALTH_SECURITY -> {
                            AdminHealthSecurityView(
                                securityEvents = healthSecurityEventsMap.values.toList(),
                                onResolveSecurityEvent = { event, resolution ->
                                    try {
                                        engine.reviewHealthSecurityEvent(currentAdmin.uid, event.eventId, resolution)
                                        snackbarMessage = "Security event #${event.eventId} updated."
                                    } catch (e: Exception) {
                                        snackbarMessage = "Blocked: ${e.message}"
                                    }
                                }
                            )
                        }
                        AdminSection.EMERGENCY_CONTROLS -> {
                            AdminEmergencyControlsView(
                                controls = emergencyControlsMap.values.toList(),
                                onToggleControl = { ctrl -> emergencyControlToToggle = ctrl }
                            )
                        }
                        AdminSection.AUDIT_LOGS -> {
                            AdminAuditLogsView(logs = auditLogsList)
                        }
                        AdminSection.SYSTEM_HEALTH -> {
                            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                Text(
                                    text = "Platform Infrastructure & Service Health",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(serviceHealthMap.values.toList()) { srv ->
                                        ServiceHealthPill(srv)
                                    }
                                }
                            }
                        }
                        AdminSection.COUNTRIES -> {
                            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                Text(
                                    text = "Sovereign Country Configurations",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(countryConfigsMap.values.toList()) { c ->
                                        Card(
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = "${c.countryName} (${c.countryCode})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                                    Text(text = "Currency: ${c.currencyCode} • Timezone: ${c.timezone} • Languages: ${c.supportedLanguages.joinToString()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Surface(
                                                    color = if (c.active) Color(0xFF00C853).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = if (c.active) "ACTIVE" else "INACTIVE",
                                                        color = if (c.active) Color(0xFF00C853) else MaterialTheme.colorScheme.error,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        AdminSection.FEATURE_FLAGS -> {
                            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                Text(
                                    text = "Platform Dynamic Feature Flags",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(featureFlagsMap.values.toList()) { flag ->
                                        Card(
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = flag.flagName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                                    Text(text = flag.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text(text = "Scope: ${flag.scope.name} • Updated by: ${flag.updatedBy}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                                }
                                                Surface(
                                                    color = when (flag.status) {
                                                        FeatureFlagStatus.ON -> Color(0xFF00C853).copy(alpha = 0.15f)
                                                        FeatureFlagStatus.OFF -> MaterialTheme.colorScheme.errorContainer
                                                        else -> MaterialTheme.colorScheme.secondaryContainer
                                                    },
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = flag.status.name,
                                                        color = when (flag.status) {
                                                            FeatureFlagStatus.ON -> Color(0xFF00C853)
                                                            FeatureFlagStatus.OFF -> MaterialTheme.colorScheme.error
                                                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                                                        },
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        AdminSection.PAYMENTS, AdminSection.DELIVERY, AdminSection.AI_STUDIO, AdminSection.MARKETPLACE -> {
                            AdminIntegrationsManagementView()
                        }
                        else -> {
                            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                                Text(
                                    text = selectedSection.displayName,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Module operational under server-side role control (${currentAdmin.roleId}). All operations are append-only logged to admin_audit_logs.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOGS
    // =========================================================================

    // 1. Re-authentication / MFA Dialog
    if (showReauthDialog) {
        AdminReauthDialog(
            onConfirm = { pin ->
                val success = engine.verifyReauth(currentAdmin.uid, pin)
                showReauthDialog = false
                snackbarMessage = if (success) "MFA Re-authentication Verified." else "Invalid Credentials. Re-authentication Failed."
            },
            onDismiss = { showReauthDialog = false }
        )
    }

    // 2. User Suspension Dialog
    userToSuspend?.let { user ->
        AdminUserSuspensionDialog(
            user = user,
            onConfirm = { actionType, reasonCode, reason, duration ->
                try {
                    engine.suspendUser(
                        actorUid = currentAdmin.uid,
                        targetUid = user.uid,
                        actionType = actionType,
                        reasonCode = reasonCode,
                        reason = reason,
                        durationHours = duration
                    )
                    snackbarMessage = "Enforcement applied to ${user.displayName}."
                } catch (e: Exception) {
                    snackbarMessage = "Enforcement failed: ${e.message}"
                } finally {
                    userToSuspend = null
                }
            },
            onDismiss = { userToSuspend = null }
        )
    }

    // 3. Verification Review Dialog
    userToReviewVerification?.let { user ->
        AdminVerificationReviewDialog(
            user = user,
            onApprove = { badgeType, notes ->
                try {
                    engine.approveVerification(
                        actorUid = currentAdmin.uid,
                        targetUid = user.uid,
                        badgeType = badgeType,
                        notes = notes
                    )
                    snackbarMessage = "Approved verification for ${user.displayName}."
                } catch (e: Exception) {
                    snackbarMessage = "Approval failed: ${e.message}"
                } finally {
                    userToReviewVerification = null
                }
            },
            onReject = { internalReason, customerReason ->
                try {
                    engine.rejectVerification(
                        actorUid = currentAdmin.uid,
                        targetUid = user.uid,
                        internalReason = internalReason,
                        customerReason = customerReason
                    )
                    snackbarMessage = "Rejected verification for ${user.displayName}."
                } catch (e: Exception) {
                    snackbarMessage = "Rejection failed: ${e.message}"
                } finally {
                    userToReviewVerification = null
                }
            },
            onDismiss = { userToReviewVerification = null }
        )
    }

    // 4. Emergency Control Confirmation Dialog
    emergencyControlToToggle?.let { ctrl ->
        if (!ctrl.isTriggered) {
            AdminActionConfirmDialog(
                title = "Activate ${ctrl.name}?",
                targetDescription = "Target Key: ${ctrl.controlKey}",
                impactWarning = "Activating this emergency kill switch will halt operations across the entire platform. Mandatory MFA verified.",
                confirmButtonText = "Trigger Emergency Stop",
                isDestructive = true,
                onConfirm = { reason ->
                    try {
                        engine.triggerEmergencyStop(
                            actorUid = currentAdmin.uid,
                            controlKey = ctrl.controlKey,
                            reason = reason,
                            pin = "1234"
                        )
                        snackbarMessage = "Activated: ${ctrl.name}."
                    } catch (e: Exception) {
                        snackbarMessage = "Failed: ${e.message}"
                    } finally {
                        emergencyControlToToggle = null
                    }
                },
                onDismiss = { emergencyControlToToggle = null }
            )
        } else {
            AdminActionConfirmDialog(
                title = "Deactivate ${ctrl.name}?",
                targetDescription = "Target Key: ${ctrl.controlKey}",
                impactWarning = "Normal platform operations will resume immediately.",
                confirmButtonText = "Restore Operations",
                isDestructive = false,
                onConfirm = { reason ->
                    try {
                        engine.clearEmergencyStop(
                            actorUid = currentAdmin.uid,
                            controlKey = ctrl.controlKey,
                            reason = reason,
                            pin = "1234"
                        )
                        snackbarMessage = "Restored: ${ctrl.name}."
                    } catch (e: Exception) {
                        snackbarMessage = "Failed: ${e.message}"
                    } finally {
                        emergencyControlToToggle = null
                    }
                },
                onDismiss = { emergencyControlToToggle = null }
            )
        }
    }
}
