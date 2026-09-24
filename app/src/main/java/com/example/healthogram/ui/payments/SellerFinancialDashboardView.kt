package com.example.healthogram.ui.payments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.payments.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Seller Financial Management & Payout Dashboard.
 * Displays available balance, pending settlements, lifetime sales, fee deductions, and payout dispatch.
 */
@Composable
fun SellerFinancialDashboardView(
    sellerUid: String = "seller_pharmacy_demo",
    sellerName: String = "Apex Healthcare Pharmacy",
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { PaymentRepository.getInstance() }
    val balances by repository.sellerBalances.collectAsState()
    val sellerPayouts by repository.sellerPayouts.collectAsState()
    val sellerLedger by repository.sellerLedger.collectAsState()

    val balance = balances[sellerUid] ?: SellerBalance(sellerUid = sellerUid, currencyCode = "SAR")
    val currency = remember(balance.currencyCode) { repository.getCurrency(balance.currencyCode) }
    val myPayouts = remember(sellerPayouts, sellerUid) { sellerPayouts.filter { it.sellerUid == sellerUid } }
    val myLedger = remember(sellerLedger, sellerUid) { sellerLedger.filter { it.sellerUid == sellerUid } }

    var showPayoutDialog by remember { mutableStateOf(false) }
    var payoutAmountInput by remember { mutableStateOf("") }
    var selectedPayoutMethod by remember { mutableStateOf("Saudi IBAN Direct (SARIE)") }
    var payoutStatusMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("seller_financial_dashboard_view")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // App Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = HealthogramTheme.colors.surface,
                border = BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text("Seller Financial Center", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(sellerName, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                    }
                }
            }

            if (payoutStatusMessage != null) {
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        payoutStatusMessage!!,
                        style = HealthogramTheme.typography.bodySmall.copy(color = Color(0xFF10B981), fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Balance Overview Card
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Available for Payout", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                    Text(
                                        currency.formatMinor(balance.availableBalanceMinor),
                                        style = HealthogramTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            color = HealthogramTheme.colors.primary
                                        )
                                    )
                                }
                                Button(
                                    onClick = { showPayoutDialog = true },
                                    enabled = balance.availableBalanceMinor > 0L,
                                    modifier = Modifier.testTag("request_payout_button")
                                ) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Request Payout")
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = HealthogramTheme.colors.borderLight)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Pending Clearance", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                    Text(currency.formatMinor(balance.pendingBalanceMinor), style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("7-day risk hold", style = HealthogramTheme.typography.caption.copy(color = HealthogramTheme.colors.textMuted))
                                }
                                Column {
                                    Text("Lifetime Sales", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                    Text(currency.formatMinor(balance.lifetimeSalesMinor), style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("Gross proceeds", style = HealthogramTheme.typography.caption.copy(color = HealthogramTheme.colors.textMuted))
                                }
                                Column {
                                    Text("Total Paid Out", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                    Text(currency.formatMinor(balance.lifetimePayoutsMinor), style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("Transferred", style = HealthogramTheme.typography.caption.copy(color = HealthogramTheme.colors.textMuted))
                                }
                            }
                        }
                    }
                }

                // Recent Payout Requests
                item {
                    Text("Recent Payouts", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }

                if (myPayouts.isEmpty()) {
                    item {
                        Text("No payout history yet.", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
                    }
                } else {
                    items(myPayouts, key = { it.payoutId }) { payout ->
                        val dateFormatted = remember(payout.requestedAt) {
                            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(payout.requestedAt))
                        }
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(payout.payoutMethod, style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                    Text(dateFormatted, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(currency.formatMinor(payout.amountMinor), style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("COMPLETED", style = HealthogramTheme.typography.caption.copy(color = Color(0xFF10B981), fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }

                // Seller Immutable Ledger Activity
                item {
                    Text("Recent Financial Ledger Activity", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }

                if (myLedger.isEmpty()) {
                    item {
                        Text("No ledger transactions recorded.", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
                    }
                } else {
                    items(myLedger, key = { it.ledgerEntryId }) { entry ->
                        val dateFormatted = remember(entry.createdAt) {
                            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(entry.createdAt))
                        }
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entry.description, style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                                    Text("${entry.entryType.name} • $dateFormatted", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                }
                                val isCredit = entry.amountMinor >= 0
                                Text(
                                    text = (if (isCredit) "+" else "") + currency.formatMinor(entry.amountMinor),
                                    style = HealthogramTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCredit) Color(0xFF10B981) else Color(0xFFEF4444)
                                    )
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }

        // Payout Request Dialog
        if (showPayoutDialog) {
            AlertDialog(
                onDismissRequest = { showPayoutDialog = false },
                title = { Text("Request Seller Payout") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Available balance: ${currency.formatMinor(balance.availableBalanceMinor)}",
                            style = HealthogramTheme.typography.bodySmall,
                            color = HealthogramTheme.colors.textSecondary
                        )
                        OutlinedTextField(
                            value = payoutAmountInput,
                            onValueChange = { payoutAmountInput = it },
                            label = { Text("Amount to withdraw") },
                            placeholder = { Text("e.g. 500.00") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Payout Destination", style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HealthogramTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(selectedPayoutMethod, style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                                Text("IBAN: SA0380000000608010167519", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val requestedMajor = payoutAmountInput.toDoubleOrNull() ?: 0.0
                            val requestedMinor = (requestedMajor * 100).toLong()
                            if (requestedMinor > 0 && requestedMinor <= balance.availableBalanceMinor) {
                                try {
                                    val payout = PaymentCustomActions.requestSellerPayout(
                                        sellerUid = sellerUid,
                                        amountMinor = requestedMinor,
                                        payoutMethod = selectedPayoutMethod,
                                        actorUid = sellerUid,
                                        idempotencyKey = "payout_${UUID.randomUUID().toString().substring(0, 10)}"
                                    )
                                    payoutStatusMessage = "Payout of ${currency.formatMinor(payout.amountMinor)} submitted to bank."
                                    showPayoutDialog = false
                                } catch (e: Exception) {
                                    payoutStatusMessage = "Error: ${e.message}"
                                }
                            }
                        },
                        enabled = (payoutAmountInput.toDoubleOrNull() ?: 0.0) > 0.0
                    ) {
                        Text("Submit Payout")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPayoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
