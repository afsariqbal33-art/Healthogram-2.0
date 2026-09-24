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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.owner.*

/**
 * Healthogram Step 17: Master Owner Control Panel & Operations Center.
 */
@Composable
fun OwnerControlPanelPage(
    service: PlatformConfigurationService = PlatformConfigurationService.getInstance(),
    engine: OwnerControlEngine = remember { OwnerControlEngine(service) },
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSection by remember { mutableStateOf(OwnerNavigationSection.DASHBOARD) }
    var searchQuery by remember { mutableStateOf("") }
    val activeEnvironment by service.activeEnvironment.collectAsState()
    val emergencySwitches by service.emergencySwitches.collectAsState()
    val activeEmergencyCount = emergencySwitches.values.count { it.isTriggered }

    var showReauthDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        val isDesktop = maxWidth > 840.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // Sidebar for desktop or navigation
            if (isDesktop) {
                OwnerSidebar(
                    currentSection = currentSection,
                    onSectionSelected = { currentSection = it },
                    activeEnvironment = activeEnvironment,
                    onEnvironmentChange = { service.updateEnvironment(it) },
                    onClose = onClose
                )
            }

            // Main Content Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF0B0F19))
            ) {
                // Top bar
                OwnerTopBar(
                    title = currentSection.title,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    activeEnvironment = activeEnvironment,
                    emergencyCount = activeEmergencyCount,
                    onReauthClick = { showReauthDialog = true }
                )

                // Mobile Navigation Bar if not on desktop
                if (!isDesktop) {
                    ScrollableTabRow(
                        selectedTabIndex = currentSection.ordinal,
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color(0xFF38BDF8),
                        edgePadding = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OwnerNavigationSection.entries.forEach { section ->
                            Tab(
                                selected = currentSection == section,
                                onClick = { currentSection = section },
                                text = { Text(section.title, fontSize = 11.sp, fontWeight = if (currentSection == section) FontWeight.Bold else FontWeight.Normal) },
                                icon = { Icon(section.icon, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }

                // Section Body
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (currentSection) {
                        OwnerNavigationSection.DASHBOARD -> {
                            OwnerDashboardView(
                                service = service,
                                engine = engine,
                                onNavigateToSection = { currentSection = it }
                            )
                        }
                        OwnerNavigationSection.FEATURE_FLAGS -> {
                            OwnerFeatureFlagsView(
                                service = service,
                                engine = engine,
                                searchQuery = searchQuery
                            )
                        }
                        OwnerNavigationSection.COUNTRIES -> {
                            OwnerCountryAndMarketplaceView(
                                service = service,
                                engine = engine,
                                searchQuery = searchQuery
                            )
                        }
                        OwnerNavigationSection.ACCOUNT_CATEGORIES -> {
                            OwnerCountryAndMarketplaceView(
                                service = service,
                                engine = engine,
                                searchQuery = searchQuery
                            )
                        }
                        OwnerNavigationSection.MARKETPLACE -> {
                            OwnerCountryAndMarketplaceView(
                                service = service,
                                engine = engine,
                                searchQuery = searchQuery
                            )
                        }
                        OwnerNavigationSection.MAINTENANCE_EMERGENCY -> {
                            OwnerEmergencyControlsView(
                                service = service,
                                engine = engine
                            )
                        }
                        OwnerNavigationSection.ROLLBACK_VERSIONS -> {
                            OwnerVersioningAndRollbackView(
                                service = service,
                                engine = engine
                            )
                        }
                        OwnerNavigationSection.AUDIT_LOGS -> {
                            OwnerAuditLogsView(
                                service = service,
                                searchQuery = searchQuery
                            )
                        }
                        OwnerNavigationSection.PAYMENTS -> {
                            OwnerPaymentsSubsystemView(service = service, engine = engine)
                        }
                        OwnerNavigationSection.DELIVERY -> {
                            OwnerDeliverySubsystemView(service = service, engine = engine)
                        }
                        OwnerNavigationSection.COMMUNICATION -> {
                            OwnerCommunicationSubsystemView(service = service, engine = engine)
                        }
                        OwnerNavigationSection.HEALTH_PASSPORT -> {
                            OwnerHealthPassportSubsystemView(service = service, engine = engine)
                        }
                        OwnerNavigationSection.AI_AND_TRANSLATION -> {
                            OwnerAiAndTranslationSubsystemView(service = service, engine = engine)
                        }
                        OwnerNavigationSection.SECURITY_DELEGATES -> {
                            OwnerSecurityDelegatesView(service = service, engine = engine)
                        }
                        OwnerNavigationSection.PERFORMANCE -> {
                            PerformanceDashboardPage()
                        }
                        OwnerNavigationSection.QA_ACCEPTANCE -> {
                            QAAcceptanceDashboardPage()
                        }
                    }
                }
            }
        }
    }

    // Reauth Dialog
    if (showReauthDialog) {
        OwnerReauthChallengeDialog(
            engine = engine,
            onDismiss = { showReauthDialog = false }
        )
    }
}

@Composable
fun OwnerPaymentsSubsystemView(
    service: PlatformConfigurationService,
    engine: OwnerControlEngine,
    modifier: Modifier = Modifier
) {
    val providers by service.providers.collectAsState()
    val paymentProviders = providers.filter { it.serviceType == "PAYMENT" }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Zero Raw Secrets Guarantee", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Payment gateway secret keys, webhook signing secrets, and bank credentials are strictly managed in Google Cloud Secret Manager / Firebase Cloud Functions environment. No raw secrets are stored in Firestore or exposed to client devices.",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        item {
            Text("Integrated Payment Gateways & Telemetry", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        items(paymentProviders, key = { it.providerId }) { prov ->
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = prov.providerName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "Jurisdiction: ${prov.country} · Priority: ${prov.priority} · Fallback: ${prov.fallbackProviderId ?: "None"}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = prov.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerDeliverySubsystemView(
    service: PlatformConfigurationService,
    engine: OwnerControlEngine,
    modifier: Modifier = Modifier
) {
    val providers by service.providers.collectAsState()
    val deliveryProviders = providers.filter { it.serviceType == "DELIVERY" }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Logistics & Courier Telemetry", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Real-time telemetry and priority routing across national and 3PL courier partners", fontSize = 11.sp, color = Color(0xFF94A3B8))
        }

        items(deliveryProviders, key = { it.providerId }) { prov ->
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = prov.providerName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "Region: ${prov.country} · Dispatch Route Priority: ${prov.priority}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                    Surface(color = Color(0xFF10B981).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                        Text(text = "OPERATIONAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerCommunicationSubsystemView(
    service: PlatformConfigurationService,
    engine: OwnerControlEngine,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Real-Time Communication & Telehealth Infrastructure", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("WebRTC voice/video calling, Agora signaling channels, and encrypted messaging", fontSize = 11.sp, color = Color(0xFF94A3B8))
        }

        item {
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Graceful Active Call Shutdown Policy", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "When video calling feature flags or maintenance modes are updated, ongoing active telehealth sessions are NOT immediately severed. Instead, new incoming call setup is blocked, while active consultations are allotted an in-app 5-minute countdown grace period before soft termination.",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }
    }
}

@Composable
fun OwnerHealthPassportSubsystemView(
    service: PlatformConfigurationService,
    engine: OwnerControlEngine,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                color = Color(0xFF0C243B),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF0284C7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Strict Zero Clinical Record Access Mandate", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF38BDF8))
                        Text(
                            "The Owner Control Panel manages platform availability, QR encryption keys, and facility access authorization policies. In accordance with HIPAA and GDPR sovereign healthcare regulations, the Owner Control Panel strictly possesses ZERO access to patient medical records, diagnostic scans, or clinical notes.",
                            fontSize = 11.sp,
                            color = Color(0xFFBAE6FD)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerAiAndTranslationSubsystemView(
    service: PlatformConfigurationService,
    engine: OwnerControlEngine,
    modifier: Modifier = Modifier
) {
    val providers by service.providers.collectAsState()
    val aiProviders = providers.filter { it.serviceType == "AI" || it.serviceType == "TRANSLATION" }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("AI Studio & Universal Translation Engines", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Google Cloud Vertex AI (Gemini 1.5 Pro/Flash) token limits and Google Cloud Translation API v3", fontSize = 11.sp, color = Color(0xFF94A3B8))
        }

        item {
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Translation Resilience & Non-Blocking Fallback", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "If the universal translation engine experiences third-party rate limiting or latency degradation, original language messages and audio consultation streams are preserved in their native format without interrupting user communication.",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        items(aiProviders, key = { it.providerId }) { prov ->
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = prov.providerName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "Category: ${prov.serviceType} · Status: ${prov.status}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                    Surface(color = Color(0xFF10B981).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                        Text(text = "HEALTHY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerSecurityDelegatesView(
    service: PlatformConfigurationService,
    engine: OwnerControlEngine,
    modifier: Modifier = Modifier
) {
    var showFullSecurityDashboard by remember { mutableStateOf(false) }

    if (showFullSecurityDashboard) {
        OwnerSecurityDashboardPage(
            onNavigateBack = { showFullSecurityDashboard = false },
            modifier = modifier
        )
        return
    }

    val delegates by service.delegates.collectAsState()
    val owner by service.currentOwner.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Sovereign Owner Identity & Access Delegation", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Platform Owner identity and delegated operator roles with time-bounded least privilege access", fontSize = 11.sp, color = Color(0xFF94A3B8))
        }

        item {
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF059669)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Step 19 Zero-Trust Production Security Center", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Scorecard (100/100 PASS), Real-Time Alerts, 4-Session Enforcement, Incident Lifecycle & Emergency Kill Switches",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Button(
                        onClick = { showFullSecurityDashboard = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        modifier = Modifier.testTag("launch_security_dashboard_button")
                    ) {
                        Text("Open Center", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFE11D48)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = owner.displayName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(color = Color(0xFFE11D48), shape = RoundedCornerShape(4.dp)) {
                                Text("ROOT OWNER", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Text(text = "${owner.email} · MFA: ${if (owner.mfaEnabled) "Active" else "Disabled"}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
        }

        item {
            Text("Delegated Operators", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        items(delegates, key = { it.delegateId }) { del ->
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
                        Text(text = del.displayName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Surface(color = Color(0xFF0284C7).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                            Text(text = del.role.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${del.email} · Permissions: ${del.permissions.size} assigned", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }
        }
    }
}

@Composable
fun OwnerReauthChallengeDialog(
    engine: OwnerControlEngine,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        title = {
            Text("Owner Re-Authentication Challenge", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        },
        text = {
            Column {
                Text(
                    "Enter your sovereign root PIN to refresh your 15-minute administrative operational session.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    placeholder = { Text("Default: 9900", fontSize = 12.sp, color = Color(0xFF64748B)) },
                    modifier = Modifier.fillMaxWidth().testTag("owner_reauth_pin_input"),
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
                if (success) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Re-authentication successful. Session refreshed.", color = Color(0xFF10B981), fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (engine.verifyOwnerReauth("owner_root_001", pin)) {
                        success = true
                        error = null
                    } else {
                        error = "Invalid PIN. Re-authentication challenge rejected."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                Text("Verify PIN")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF94A3B8))
            }
        }
    )
}
