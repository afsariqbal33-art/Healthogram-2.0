package com.example.healthogram.ui.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.owner.*

/**
 * Healthogram Step 17: Enterprise UI Components for Owner Control Panel.
 */

enum class OwnerNavigationSection(val title: String, val icon: ImageVector) {
    DASHBOARD("Executive Dashboard", Icons.Default.Dashboard),
    FEATURE_FLAGS("Global Feature Flags", Icons.Default.ToggleOn),
    COUNTRIES("Sovereign Countries", Icons.Default.Public),
    ACCOUNT_CATEGORIES("Account Categories", Icons.Default.GroupWork),
    MARKETPLACE("Marketplace Controls", Icons.Default.Storefront),
    PAYMENTS("Payment Gateways", Icons.Default.Payments),
    DELIVERY("Delivery Logistics", Icons.Default.LocalShipping),
    COMMUNICATION("Messaging & Calls", Icons.Default.Forum),
    HEALTH_PASSPORT("Health Passport & QR", Icons.Default.HealthAndSafety),
    AI_AND_TRANSLATION("AI Studio & Translation", Icons.Default.Psychology),
    MAINTENANCE_EMERGENCY("Emergency & Maintenance", Icons.Default.Warning),
    ROLLBACK_VERSIONS("Versioning & Rollback", Icons.Default.History),
    AUDIT_LOGS("Compliance Audit Logs", Icons.Default.FactCheck),
    SECURITY_DELEGATES("Security & Delegation", Icons.Default.Security),
    PERFORMANCE("Performance & Scalability", Icons.Default.Speed),
    QA_ACCEPTANCE("QA & Production Acceptance", Icons.Default.Verified)
}

@Composable
fun OwnerSidebar(
    currentSection: OwnerNavigationSection,
    onSectionSelected: (OwnerNavigationSection) -> Unit,
    activeEnvironment: AppEnvironment,
    onEnvironmentChange: (AppEnvironment) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight(),
        color = Color(0xFF0F172A), // Dark slate enterprise surface
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with Healthogram Owner Brand
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE11D48)), // Sovereign crimson
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Owner Badge",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "HEALTHOGRAM",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Owner Control Center",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Environment Selector Pill
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ENV:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    AppEnvironment.entries.forEach { env ->
                        val isSelected = env == activeEnvironment
                        val envColor = when (env) {
                            AppEnvironment.PRODUCTION -> Color(0xFFE11D48)
                            AppEnvironment.STAGING -> Color(0xFFF59E0B)
                            AppEnvironment.DEVELOPMENT -> Color(0xFF10B981)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) envColor.copy(alpha = 0.25f) else Color.Transparent)
                                .clickable { onEnvironmentChange(env) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = env.name.take(4),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) envColor else Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF334155), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Links
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OwnerNavigationSection.entries.forEach { section ->
                    val isSelected = section == currentSection
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSectionSelected(section) }
                            .testTag("owner_nav_${section.name.lowercase()}"),
                        color = if (isSelected) Color(0xFF1E293B) else Color.Transparent,
                        contentColor = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = section.icon,
                                contentDescription = section.title,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = section.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Exit button
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Exit", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exit Owner Mode", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun OwnerTopBar(
    title: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    activeEnvironment: AppEnvironment,
    emergencyCount: Int,
    onReauthClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF0F172A),
        border = BorderStroke(0.5.dp, Color(0xFF334155))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Healthogram Enterprise Architecture Layer",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search flags, countries, audit...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                    modifier = Modifier
                        .width(240.dp)
                        .height(44.dp)
                        .testTag("owner_search_input"),
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontSize = 12.sp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    )
                )

                // Emergency warning pill if any kill switch is active
                if (emergencyCount > 0) {
                    Surface(
                        color = Color(0xFFDC2626),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Emergency", tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$emergencyCount EMERGENCY LOCKS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }

                // MFA / Reauth status badge
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                    modifier = Modifier.clickable { onReauthClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("MFA ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerStatusCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    subtext: String? = null
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF94A3B8))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            if (subtext != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtext, fontSize = 10.sp, color = Color(0xFF64748B))
            }
        }
    }
}

@Composable
fun OwnerMaintenanceBanner(
    message: String,
    onDisable: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF78350F), // Dark amber
        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Engineering, contentDescription = "Maintenance", tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("GLOBAL MAINTENANCE ACTIVE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFFFBBF24))
                    Text(message, fontSize = 11.sp, color = Color.White)
                }
            }
            Button(
                onClick = onDisable,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("End Maintenance", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OwnerDangerConfirmationDialog(
    title: String,
    description: String,
    typedConfirmationPrompt: String,
    expectedConfirmation: String,
    onConfirm: (pin: String, typedText: String) -> Unit,
    onDismiss: () -> Unit
) {
    var typedText by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        titleContentColor = Color(0xFFEF4444),
        textContentColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Dangerous, contentDescription = "Danger", tint = Color(0xFFEF4444))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(description, fontSize = 13.sp, color = Color(0xFFCBD5E1))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Type \"$expectedConfirmation\" to confirm:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF87171)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = typedText,
                    onValueChange = { typedText = it },
                    placeholder = { Text(expectedConfirmation, fontSize = 12.sp, color = Color(0xFF64748B)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFEF4444),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text("Owner MFA PIN (Root PIN: 9900):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    placeholder = { Text("4-digit PIN", fontSize = 12.sp, color = Color(0xFF64748B)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
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
                    if (typedText.trim() != expectedConfirmation) {
                        error = "Typed confirmation does not match exactly."
                        return@Button
                    }
                    if (pin.length < 4) {
                        error = "Please enter valid 4-digit MFA PIN."
                        return@Button
                    }
                    onConfirm(pin, typedText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626), contentColor = Color.White),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Confirm Action", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(6.dp)) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}
