package com.example.healthogram.ui.owner

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
import com.example.healthogram.owner.*

/**
 * Healthogram Step 17: Emergency Controls, Versioning, and Subsystem Views.
 */

@Composable
fun OwnerEmergencyControlsView(
    service: PlatformConfigurationService,
    engine: OwnerControlEngine,
    modifier: Modifier = Modifier
) {
    val emergencySwitches by service.emergencySwitches.collectAsState()
    var selectedSwitchToTrigger by remember { mutableStateOf<EmergencySwitchKey?>(null) }
    var selectedSwitchToDeactivate by remember { mutableStateOf<EmergencySwitchKey?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                color = Color(0xFF450A0A),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "Emergency Protocol", tint = Color(0xFFF87171), modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("EMERGENCY KILL SWITCH LAYER", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFFF87171))
                        Text(
                            "Emergency kill switches immediately override all lower precedence tiers (User, Category, Country, Global flags). Every activation requires typed confirmation and records an append-only audit event.",
                            fontSize = 11.sp,
                            color = Color(0xFFFECACA)
                        )
                    }
                }
            }
        }

        items(EmergencySwitchKey.entries) { key ->
            val state = emergencySwitches[key] ?: EmergencyKillSwitchState(key = key)
            EmergencyKillSwitchCard(
                state = state,
                onTrigger = { selectedSwitchToTrigger = key },
                onDeactivate = { selectedSwitchToDeactivate = key }
            )
        }
    }

    if (selectedSwitchToTrigger != null) {
        val key = selectedSwitchToTrigger!!
        OwnerDangerConfirmationDialog(
            title = "TRIGGER EMERGENCY LOCK: ${key.displayName}?",
            description = "Warning: This action will immediately halt all operations related to ${key.displayName} across all platforms and countries. This overrides all other configuration flags.",
            typedConfirmationPrompt = "Type '${key.typedConfirmationText}' to confirm:",
            expectedConfirmation = key.typedConfirmationText,
            onConfirm = { pin, typedText ->
                engine.triggerEmergencyKillSwitch(
                    actorUid = "owner_root_001",
                    key = key,
                    typedConfirmation = typedText,
                    reason = "Owner manual emergency trigger via Control Panel",
                    pin = pin
                )
                selectedSwitchToTrigger = null
            },
            onDismiss = { selectedSwitchToTrigger = null }
        )
    }

    if (selectedSwitchToDeactivate != null) {
        val key = selectedSwitchToDeactivate!!
        OwnerDangerConfirmationDialog(
            title = "RESTORE SERVICE: ${key.displayName}?",
            description = "This will lift the emergency lock for ${key.displayName} and restore standard precedence rules.",
            typedConfirmationPrompt = "Type 'RESTORE ${key.name}' to confirm:",
            expectedConfirmation = "RESTORE ${key.name}",
            onConfirm = { pin, typedText ->
                engine.deactivateEmergencyKillSwitch(
                    actorUid = "owner_root_001",
                    key = key,
                    reason = "Owner verified emergency resolved and restored service",
                    pin = pin
                )
                selectedSwitchToDeactivate = null
            },
            onDismiss = { selectedSwitchToDeactivate = null }
        )
    }
}

@Composable
private fun EmergencyKillSwitchCard(
    state: EmergencyKillSwitchState,
    onTrigger: () -> Unit,
    onDeactivate: () -> Unit
) {
    val isTriggered = state.isTriggered
    val borderColor = if (isTriggered) Color(0xFFDC2626) else Color(0xFF334155)
    val bgColor = if (isTriggered) Color(0xFF1E1014) else Color(0xFF1E293B)

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.key.displayName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTriggered) Color(0xFFFCA5A5) else Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (isTriggered) Color(0xFFDC2626) else Color(0xFF0F766E),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isTriggered) "TRIGGERED" else "STANDBY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Key: ${state.key.name} · Confirmation: \"${state.key.typedConfirmationText}\"",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
                if (isTriggered && state.reason != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Triggered by ${state.triggeredBy} · Reason: ${state.reason}",
                        fontSize = 10.sp,
                        color = Color(0xFFF87171)
                    )
                }
            }

            Button(
                onClick = { if (isTriggered) onDeactivate() else onTrigger() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTriggered) Color(0xFF10B981) else Color(0xFFDC2626)
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isTriggered) "Restore Service" else "Trigger Lock",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun OwnerVersioningAndRollbackView(
    service: PlatformConfigurationService,
    engine: OwnerControlEngine,
    modifier: Modifier = Modifier
) {
    val versions by service.configurationVersions.collectAsState()
    var selectedVersionForRollback by remember { mutableStateOf<ConfigurationVersion?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Configuration Snapshots & Rollback History", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Every high-risk configuration change generates an immutable snapshot with version number and diff", fontSize = 11.sp, color = Color(0xFF94A3B8))
        }

        items(versions, key = { it.versionId }) { version ->
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = Color(0xFF0284C7), shape = RoundedCornerShape(4.dp)) {
                                Text(
                                    text = "v${version.versionNumber}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = version.configurationType, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = "By ${version.createdBy} · Prev: v${version.previousVersion}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }

                        if (version.rollbackAvailable) {
                            OutlinedButton(
                                onClick = { selectedVersionForRollback = version },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Rollback to this", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Reason: ${version.reason}", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                }
            }
        }
    }

    if (selectedVersionForRollback != null) {
        val ver = selectedVersionForRollback!!
        OwnerDangerConfirmationDialog(
            title = "Rollback to Configuration v${ver.versionNumber}?",
            description = "This will restore the configuration values captured in snapshot v${ver.versionNumber} (${ver.configurationType}). A new version snapshot will be published to maintain audit continuity.",
            typedConfirmationPrompt = "Type 'ROLLBACK v${ver.versionNumber}' to confirm:",
            expectedConfirmation = "ROLLBACK v${ver.versionNumber}",
            onConfirm = { pin, typedText ->
                engine.rollbackToVersion(
                    actorUid = "owner_root_001",
                    targetVersionNumber = ver.versionNumber,
                    reason = "Owner executed rollback to snapshot v${ver.versionNumber}",
                    pin = pin
                )
                selectedVersionForRollback = null
            },
            onDismiss = { selectedVersionForRollback = null }
        )
    }
}

@Composable
fun OwnerAuditLogsView(
    service: PlatformConfigurationService,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    val logs by service.auditLogs.collectAsState()
    val filteredLogs = remember(logs, searchQuery) {
        logs.filter {
            searchQuery.isBlank() ||
                    it.action.contains(searchQuery, ignoreCase = true) ||
                    it.targetId.contains(searchQuery, ignoreCase = true) ||
                    it.reason.contains(searchQuery, ignoreCase = true) ||
                    it.ownerUid.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Owner Append-Only Compliance Ledger", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Complete immutable history of all administrative, financial, and emergency actions", fontSize = 11.sp, color = Color(0xFF94A3B8))
        }

        items(filteredLogs, key = { it.logId }) { log ->
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = log.action, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                        Surface(color = Color(0xFF0F172A), shape = RoundedCornerShape(4.dp)) {
                            Text(text = "v${log.configurationVersion}", fontSize = 9.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Actor: ${log.ownerUid} (${log.ownerRole}) · Target: ${log.targetType}/${log.targetId}",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = "Change: [${log.oldValue}] → [${log.newValue}]",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "Audit Reason: ${log.reason}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFBBF24)
                    )
                }
            }
        }
    }
}
