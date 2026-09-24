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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.core.AccountType
import com.example.healthogram.owner.*

/**
 * Healthogram Step 17: Sovereign Country & Marketplace Management View.
 */
@Composable
fun OwnerCountryAndMarketplaceView(
    service: PlatformConfigurationService,
    engine: OwnerControlEngine,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    val countriesMap by service.countryConfigs.collectAsState()
    val accountControlsMap by service.accountCategoryControls.collectAsState()
    val globalConfig by service.globalConfig.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Countries, 1 = Account Categories, 2 = Marketplace Rules
    var countryToDeactivate by remember { mutableStateOf<SovereignCountryConfig?>(null) }
    var selectedCountryForEdit by remember { mutableStateOf<SovereignCountryConfig?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf("Sovereign Countries (${countriesMap.size})", "Account Categories (5)", "Marketplace Governance", "Integrations & Providers")
            tabs.forEachIndexed { index, label ->
                val selected = activeTab == index
                Surface(
                    color = if (selected) Color(0xFF0284C7) else Color(0xFF1E293B),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clickable { activeTab = index }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Color.White else Color(0xFF94A3B8)
                    )
                }
            }
        }

        when (activeTab) {
            0 -> {
                // Country List
                val filteredCountries = remember(countriesMap, searchQuery) {
                    countriesMap.values.filter {
                        searchQuery.isBlank() ||
                                it.countryName.contains(searchQuery, ignoreCase = true) ||
                                it.countryCode.contains(searchQuery, ignoreCase = true)
                    }.sortedBy { it.countryName }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredCountries, key = { it.countryCode }) { country ->
                        CountryCard(
                            country = country,
                            onToggleActive = {
                                if (country.active) {
                                    countryToDeactivate = country
                                } else {
                                    engine.updateCountryActiveStatus(
                                        actorUid = "owner_root_001",
                                        countryCode = country.countryCode,
                                        active = true,
                                        reason = "Owner activated ${country.countryName}",
                                        pin = "9900"
                                    )
                                }
                            },
                            onToggleIntlMarketplace = {
                                engine.setInternationalMarketplace(
                                    actorUid = "owner_root_001",
                                    countryCode = country.countryCode,
                                    enabled = !country.internationalMarketplaceEnabled,
                                    reason = "Toggled international marketplace for ${country.countryCode}",
                                    pin = "9900"
                                )
                            }
                        )
                    }
                }
            }
            1 -> {
                // Account Category Feature Matrix (Exactly 5 categories)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = "Platform Security Mandate: Healthogram account categories are strictly limited to Individual, Doctor, Clinic, Hospital, and Laboratory. Commercial pharmacy or unregulated suppliers are forbidden as core categories.",
                                fontSize = 11.sp,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    items(accountControlsMap.values.toList(), key = { it.category.name }) { controls ->
                        AccountCategoryCard(
                            controls = controls,
                            onToggleEnabled = {
                                val updated = controls.copy(enabled = !controls.enabled)
                                service.updateAccountCategoryControls(updated)
                            },
                            onUpdateMaxDevices = { newLimit ->
                                val updated = controls.copy(maxConcurrentDevices = newLimit)
                                service.updateAccountCategoryControls(updated)
                            }
                        )
                    }
                }
            }
            2 -> {
                // Marketplace Governance Controls
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.5.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Global Marketplace Master Controls", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Domestic Marketplace", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Allows registered customers to purchase within their home jurisdiction", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    }
                                    Switch(
                                        checked = globalConfig.marketplaceEnabled,
                                        onCheckedChange = { checked ->
                                            service.updateGlobalConfig(globalConfig.copy(marketplaceEnabled = checked))
                                            val flag = service.featureFlags.value["marketplace"]
                                            if (flag != null) {
                                                engine.updateFeatureFlagStatus(
                                                    "owner_root_001",
                                                    "marketplace",
                                                    if (checked) FeatureFlagStatus.ON else FeatureFlagStatus.OFF,
                                                    "Owner changed global marketplace toggle"
                                                )
                                            }
                                        }
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF334155))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("International Marketplace", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Cross-border health product procurement and international customs logistics (Strictly OFF by default)", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    }
                                    Switch(
                                        checked = globalConfig.internationalMarketplaceEnabled,
                                        onCheckedChange = { checked ->
                                            engine.setInternationalMarketplace(
                                                actorUid = "owner_root_001",
                                                countryCode = null,
                                                enabled = checked,
                                                reason = "Owner toggled global international marketplace",
                                                pin = "9900"
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.5.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Merchant Governance & Financial Parameters", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(12.dp))

                                ParameterRow("Standard Seller Commission", "${service.pricingConfig.value.sellerCommissionPercent}%", "Deducted automatically from gross cart volume")
                                ParameterRow("Telehealth Consultation Commission", "${service.pricingConfig.value.telehealthCommissionPercent}%", "Platform fee on virtual appointments")
                                ParameterRow("Seller Payout Hold Period", "14 Days", "Anti-fraud escrow holding duration before bank release")
                                ParameterRow("Customer Return Window", "7 Days", "Eligible window for undamaged health product returns")
                                ParameterRow("Cash on Delivery (COD) Surcharge", "$1.50 / 5.50 SAR", "Risk buffer for courier collection failure")
                            }
                        }
                    }
                }
            }
            3 -> {
                // Provider & Integration Governance (Step 51)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.5.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Step 51 Integration Master Controls", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Sovereign provider toggles, cost thresholds, and failover governance", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("AI Studio Multimodal Quota Guard", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Strict token rate limit & Zero-PHI barrier on creator and marketplace content", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    }
                                    Surface(color = Color(0xFF10B981).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                        Text("10M TOKENS / MO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), modifier = Modifier.padding(6.dp))
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF334155))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Neural Translation Privacy Sandbox", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("16 regional languages with mandatory medical disclaimer enforcement", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    }
                                    Surface(color = Color(0xFF10B981).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                        Text("ENFORCED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), modifier = Modifier.padding(6.dp))
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF334155))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Payment & Payout Production Lock", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Keeps payment gateways in sandbox mode; prevents automated live payouts without dual-key authorization", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    }
                                    Surface(color = Color(0xFF38BDF8).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                        Text("SANDBOX LOCKED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), modifier = Modifier.padding(6.dp))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.5.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Emergency Integration Kill Switches", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                Spacer(modifier = Modifier.height(10.dp))
                                ParameterRow("Immediate Payment Kill Switch", "ARMED", "Instantly halts all client checkout sessions worldwide")
                                ParameterRow("Immediate Logistics Kill Switch", "ARMED", "Cancels automated dispatch for pending carrier shipments")
                                ParameterRow("Immediate AI API Kill Switch", "ARMED", "Ceases external model calls; falls back to cached templates")
                            }
                        }
                    }
                }
            }
        }
    }

    // Country Deactivation Confirmation Dialog
    if (countryToDeactivate != null) {
        val country = countryToDeactivate!!
        OwnerDangerConfirmationDialog(
            title = "Deactivate Healthogram in ${country.countryName}?",
            description = "Warning: Deactivating ${country.countryName} (${country.countryCode}) will immediately halt user registration, digital payments, marketplace checkouts, delivery logistics, and new clinical appointments across this entire sovereign jurisdiction.",
            typedConfirmationPrompt = "Type 'DISABLE ${country.countryCode}' to confirm:",
            expectedConfirmation = "DISABLE ${country.countryCode}",
            onConfirm = { pin, typedText ->
                engine.updateCountryActiveStatus(
                    actorUid = "owner_root_001",
                    countryCode = country.countryCode,
                    active = false,
                    reason = "Owner deactivated country jurisdiction with typed verification",
                    pin = pin
                )
                countryToDeactivate = null
            },
            onDismiss = { countryToDeactivate = null }
        )
    }
}

@Composable
private fun CountryCard(
    country: SovereignCountryConfig,
    onToggleActive: () -> Unit,
    onToggleIntlMarketplace: () -> Unit
) {
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
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = country.countryCode, fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color(0xFF38BDF8))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = country.countryName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = "Currency: ${country.defaultCurrency} · Languages: ${country.supportedLanguages.joinToString(", ")} · TZ: ${country.timezone}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = if (country.active) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (country.active) "ACTIVE" else "DEACTIVATED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (country.active) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    Button(
                        onClick = onToggleActive,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (country.active) Color(0xFFDC2626) else Color(0xFF10B981)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(if (country.active) "Deactivate" else "Activate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF334155), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Subsystem service flags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ServiceChip("Marketplace", country.marketplaceEnabled)
                    ServiceChip("Payments", country.paymentsEnabled)
                    ServiceChip("Delivery", country.deliveryEnabled)
                    ServiceChip("AI", country.aiEnabled)
                    ServiceChip("Health Passport", country.healthPassportEnabled)
                }

                // International Marketplace Toggle Pill
                Surface(
                    color = if (country.internationalMarketplaceEnabled) Color(0xFF0284C7).copy(alpha = 0.2f) else Color(0xFF334155),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (country.internationalMarketplaceEnabled) Color(0xFF0284C7) else Color(0xFF475569)),
                    modifier = Modifier.clickable { onToggleIntlMarketplace() }
                ) {
                    Text(
                        text = if (country.internationalMarketplaceEnabled) "Intl Mkt: ON" else "Intl Mkt: OFF",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (country.internationalMarketplaceEnabled) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceChip(label: String, enabled: Boolean) {
    Surface(
        color = if (enabled) Color(0xFF0F172A) else Color(0xFF1E1E2D),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, if (enabled) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFEF4444).copy(alpha = 0.4f))
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = if (enabled) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun AccountCategoryCard(
    controls: AccountCategoryFeatureControls,
    onToggleEnabled: () -> Unit,
    onUpdateMaxDevices: (Int) -> Unit
) {
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
                    Icon(
                        imageVector = when (controls.category) {
                            AccountType.INDIVIDUAL -> Icons.Default.Person
                            AccountType.DOCTOR -> Icons.Default.MedicalServices
                            AccountType.CLINIC -> Icons.Default.LocalHospital
                            AccountType.HOSPITAL -> Icons.Default.Domain
                            AccountType.LABORATORY -> Icons.Default.Biotech
                            else -> Icons.Default.AccountCircle
                        },
                        contentDescription = controls.category.name,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = controls.category.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = "Max Concurrent Devices: ${controls.maxConcurrentDevices} · QR Scanning: ${if (controls.qrScanningEnabled) "Permitted" else "Restricted"}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Switch(checked = controls.enabled, onCheckedChange = { onToggleEnabled() })
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF334155), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (controls.verificationRequired) {
                        Surface(color = Color(0xFF0369A1), shape = RoundedCornerShape(4.dp)) {
                            Text("Accreditation Required", fontSize = 9.sp, color = Color.White, modifier = Modifier.padding(4.dp))
                        }
                    }
                    if (controls.organizationManagementEnabled) {
                        Surface(color = Color(0xFF0F766E), shape = RoundedCornerShape(4.dp)) {
                            Text("Facility Multi-Member", fontSize = 9.sp, color = Color.White, modifier = Modifier.padding(4.dp))
                        }
                    }
                    if (controls.labReportSubmissionEnabled) {
                        Surface(color = Color(0xFF7E22CE), shape = RoundedCornerShape(4.dp)) {
                            Text("Diagnostic Uploads", fontSize = 9.sp, color = Color.White, modifier = Modifier.padding(4.dp))
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Device Cap:", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    listOf(4, 8).forEach { cap ->
                        val selected = controls.maxConcurrentDevices == cap
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (selected) Color(0xFF0284C7) else Color(0xFF334155))
                                .clickable { onUpdateMaxDevices(cap) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("$cap", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParameterRow(label: String, value: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(text = description, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
        Surface(color = Color(0xFF0F172A), shape = RoundedCornerShape(6.dp)) {
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
        }
    }
}
