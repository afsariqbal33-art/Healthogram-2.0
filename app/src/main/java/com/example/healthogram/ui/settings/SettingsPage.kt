package com.example.healthogram.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*

@Composable
fun SettingsPage(
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    isRtlLayout: Boolean,
    onToggleRtl: (Boolean) -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToSecurity: () -> Unit = {},
    onNavigateToVerification: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPaymentHistory: () -> Unit = {},
    onNavigateToSellerFinancials: () -> Unit = {},
    onNavigateToOwnerEarnings: () -> Unit = {},
    onNavigateToAdminPayments: () -> Unit = {},
    onNavigateToAdminControlPanel: () -> Unit = {},
    onNavigateToOwnerControlPanel: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var biometricEnabled by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("English (US)") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("settings_page")
    ) {
        // App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = HealthogramTheme.colors.surface,
            border = BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Settings & Privacy",
                    style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Appearance & Theme Group (Light/Dark mode & RTL Arabic layout)
            item {
                Text("Appearance & Display", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
            }

            item {
                HealthogramBasicCard {
                    // Dark Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = HealthogramTheme.colors.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Dark Mode", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text("High contrast night theme", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            }
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = onToggleDarkTheme,
                            modifier = Modifier.testTag("dark_mode_toggle")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = HealthogramTheme.colors.borderLight)

                    // RTL Layout Preview Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FormatTextdirectionRToL, contentDescription = null, tint = HealthogramTheme.colors.secondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Right-to-Left (RTL) Layout", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text("Arabic & Hebrew mirror rendering", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            }
                        }
                        Switch(
                            checked = isRtlLayout,
                            onCheckedChange = onToggleRtl,
                            modifier = Modifier.testTag("rtl_layout_toggle")
                        )
                    }
                }
            }

            // Security & Privacy Group
            item {
                Text("Security & Health Passport Vault", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
            }

            item {
                HealthogramBasicCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = HealthogramTheme.colors.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Biometric Lock (Face / Fingerprint)", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text("Require biometric scan to reveal Health Passport QR", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            }
                        }
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = { biometricEnabled = it }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = HealthogramTheme.colors.borderLight)

                    SettingsRowItem(
                        icon = Icons.Default.Security,
                        title = "Account Security & Credentials",
                        subtitle = "Password, Email, Active Devices (Max 4), MFA & Audit Logs",
                        onClick = onNavigateToSecurity
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = HealthogramTheme.colors.borderLight)

                    SettingsRowItem(
                        icon = Icons.Default.Devices,
                        title = "Active Devices & Permissions",
                        subtitle = "Review authorized phones, tablets & limit enforcement",
                        onClick = onNavigateToDevices
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = HealthogramTheme.colors.borderLight)

                    SettingsRowItem(
                        icon = Icons.Default.VerifiedUser,
                        title = "Healthcare Platform Verification",
                        subtitle = "Official badge, licensing review & scanner eligibility",
                        onClick = onNavigateToVerification
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = HealthogramTheme.colors.borderLight)

                    SettingsRowItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications & Quiet Hours",
                        subtitle = "Push channels, patient privacy guard & multi-device alerts",
                        onClick = onNavigateToNotifications
                    )
                }
            }

            // Translation & Multilingual Group
            item {
                Text("Multilingual & Translation Engine", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
            }

            item {
                HealthogramBasicCard {
                    SettingsRowItem(
                        icon = Icons.Default.Translate,
                        title = "Primary App Language",
                        subtitle = selectedLanguage,
                        onClick = { /* language picker */ }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = HealthogramTheme.colors.borderLight)
                    SettingsRowItem(
                        icon = Icons.Default.GTranslate,
                        title = "Live Conversation Translation",
                        subtitle = "Automatic medical terminology translation enabled",
                        onClick = { /* translation engine */ }
                    )
                }
            }

            // Payments & Financial Infrastructure
            item {
                Text("Payments & Financial Infrastructure", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
            }

            item {
                HealthogramBasicCard {
                    SettingsRowItem(
                        icon = Icons.Default.ReceiptLong,
                        title = "Payment History & Invoices",
                        subtitle = "Receipts, payment breakdown, refunds & chargeback requests",
                        onClick = onNavigateToPaymentHistory
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = HealthogramTheme.colors.borderLight)

                    SettingsRowItem(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "Seller Financial Dashboard",
                        subtitle = "Earnings, net balances, ledger splits & bank payouts",
                        onClick = onNavigateToSellerFinancials
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = HealthogramTheme.colors.borderLight)

                    SettingsRowItem(
                        icon = Icons.Default.MonetizationOn,
                        title = "Platform Revenue & Treasury",
                        subtitle = "Owner commission, platform fees & tax reconciliation",
                        onClick = onNavigateToOwnerEarnings
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = HealthogramTheme.colors.borderLight)

                    SettingsRowItem(
                        icon = Icons.Default.Tune,
                        title = "Payment Gateway & Emergency Switches",
                        subtitle = "Stripe / Mada telemetry, tax configurations & kill switches",
                        onClick = onNavigateToAdminPayments
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = HealthogramTheme.colors.borderLight)

                    SettingsRowItem(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "Admin Control Panel & Ops Center",
                        subtitle = "Centralized platform operations, verification, moderation & emergency controls",
                        onClick = onNavigateToAdminControlPanel
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = HealthogramTheme.colors.borderLight)

                    SettingsRowItem(
                        icon = Icons.Default.Shield,
                        title = "Owner Control Center & Global Flags",
                        subtitle = "Sovereign platform governance, emergency kill switches & feature flag hierarchy",
                        onClick = onNavigateToOwnerControlPanel
                    )
                }
            }

            // Danger Zone
            item {
                HealthogramDangerButton(
                    text = "Sign Out of Healthogram",
                    onClick = onSignOut,
                    icon = Icons.Default.Logout,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }
}

@Composable
fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = HealthogramTheme.colors.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text(subtitle, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthogramTheme.colors.textMuted)
    }
}
