package com.example.healthogram.ui.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.owner.*

/**
 * Healthogram Step 17: Owner Global Feature Flags Management View.
 */
@Composable
fun OwnerFeatureFlagsView(
    service: PlatformConfigurationService,
    engine: OwnerControlEngine,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    val flagsMap by service.featureFlags.collectAsState()
    var selectedCategory by remember { mutableStateOf<FeatureCategory?>(null) }
    var selectedFlagForEdit by remember { mutableStateOf<GlobalFeatureFlag?>(null) }

    val filteredFlags = remember(flagsMap, selectedCategory, searchQuery) {
        flagsMap.values.filter { flag ->
            val matchesCategory = selectedCategory == null || flag.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    flag.featureKey.contains(searchQuery, ignoreCase = true) ||
                    flag.displayName.contains(searchQuery, ignoreCase = true) ||
                    flag.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }.sortedBy { it.displayName }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("ALL (${flagsMap.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White
                    )
                )
            }
            items(FeatureCategory.entries) { cat ->
                val count = flagsMap.values.count { it.category == cat }
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text("${cat.name.replace("_", " ")} ($count)", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Feature Flags List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredFlags, key = { it.flagId }) { flag ->
                FeatureFlagCard(
                    flag = flag,
                    onEdit = { selectedFlagForEdit = flag },
                    onQuickToggle = {
                        val nextStatus = if (flag.status == FeatureFlagStatus.ON) FeatureFlagStatus.OFF else FeatureFlagStatus.ON
                        engine.updateFeatureFlagStatus(
                            actorUid = "owner_root_001",
                            featureKey = flag.featureKey,
                            newStatus = nextStatus,
                            reason = "Owner quick-toggle from UI"
                        )
                    }
                )
            }
        }
    }

    // Flag Details & Editor Dialog
    if (selectedFlagForEdit != null) {
        val flag = selectedFlagForEdit!!
        FeatureFlagEditDialog(
            flag = flag,
            onDismiss = { selectedFlagForEdit = null },
            onSave = { newStatus, rollout, maintenanceMsg, reason, pin ->
                val updatedFlag = flag.copy(
                    rolloutPercentage = rollout,
                    maintenanceMessage = maintenanceMsg
                )
                service.updateFeatureFlag(updatedFlag)
                engine.updateFeatureFlagStatus(
                    actorUid = "owner_root_001",
                    featureKey = flag.featureKey,
                    newStatus = newStatus,
                    reason = reason,
                    pin = pin
                )
                selectedFlagForEdit = null
            }
        )
    }
}

@Composable
private fun FeatureFlagCard(
    flag: GlobalFeatureFlag,
    onEdit: () -> Unit,
    onQuickToggle: () -> Unit
) {
    val statusColor = when (flag.status) {
        FeatureFlagStatus.ON -> Color(0xFF10B981)
        FeatureFlagStatus.OFF -> Color(0xFFEF4444)
        FeatureFlagStatus.MAINTENANCE -> Color(0xFFF59E0B)
        FeatureFlagStatus.BETA -> Color(0xFF38BDF8)
        FeatureFlagStatus.COMING_SOON -> Color(0xFFA855F7)
    }

    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, Color(0xFF334155)),
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
                        text = flag.displayName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFF334155),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = flag.category.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (flag.rolloutPercentage < 100) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFF0369A1),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${flag.rolloutPercentage}% Rollout",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE0F2FE),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${flag.featureKey} — ${flag.description}",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
                if (flag.maintenanceMessage != null && flag.status == FeatureFlagStatus.MAINTENANCE) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Notice: ${flag.maintenanceMessage}",
                        fontSize = 10.sp,
                        color = Color(0xFFFBBF24)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Status badge
                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = flag.status.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Quick Toggle
                IconButton(onClick = onQuickToggle, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (flag.status == FeatureFlagStatus.ON) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                        contentDescription = "Toggle",
                        tint = if (flag.status == FeatureFlagStatus.ON) Color(0xFF10B981) else Color(0xFF64748B),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Edit details button
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Configure",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureFlagEditDialog(
    flag: GlobalFeatureFlag,
    onDismiss: () -> Unit,
    onSave: (status: FeatureFlagStatus, rollout: Int, maintenanceMsg: String?, reason: String, pin: String) -> Unit
) {
    var status by remember { mutableStateOf(flag.status) }
    var rollout by remember { mutableStateOf(flag.rolloutPercentage.toFloat()) }
    var maintenanceMsg by remember { mutableStateOf(flag.maintenanceMessage ?: "") }
    var reason by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("9900") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        title = {
            Column {
                Text(text = "Configure Feature: ${flag.displayName}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                Text(text = flag.featureKey, fontSize = 11.sp, color = Color(0xFF94A3B8))
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Target Status:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FeatureFlagStatus.entries.forEach { st ->
                        val selected = st == status
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) Color(0xFF0284C7) else Color(0xFF1E293B))
                                .clickable { status = st }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = st.name,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Staged Rollout: ${rollout.toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                Slider(
                    value = rollout,
                    onValueChange = { rollout = it },
                    valueRange = 0f..100f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF38BDF8),
                        activeTrackColor = Color(0xFF0284C7),
                        inactiveTrackColor = Color(0xFF334155)
                    )
                )

                if (status == FeatureFlagStatus.MAINTENANCE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Maintenance Message:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    OutlinedTextField(
                        value = maintenanceMsg,
                        onValueChange = { maintenanceMsg = it },
                        placeholder = { Text("Reason shown to customers...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF59E0B)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Audit Reason (Mandatory):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = { Text("Explain business justification...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text("Owner MFA PIN (Default: 9900):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8)
                    )
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(error!!, color = Color(0xFFEF4444), fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isBlank()) {
                        error = "Audit reason is strictly required."
                        return@Button
                    }
                    onSave(status, rollout.toInt(), maintenanceMsg.ifBlank { null }, reason, pin)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Save Configuration")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(6.dp)) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}
