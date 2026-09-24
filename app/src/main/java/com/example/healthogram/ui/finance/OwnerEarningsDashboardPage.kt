package com.example.healthogram.ui.finance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.healthogram.finance.*

/**
 * Healthogram Step 18: Owner Earnings Dashboard Page.
 * Displays primary revenue metrics, balance states (Pending, Available, Reserved, Withdrawn),
 * fee breakdowns, and withdrawal action.
 */
@Composable
fun OwnerEarningsDashboardPage(
    engine: FinancialLedgerEngine = remember { FinancialLedgerEngine.getInstance() },
    onNavigateToFinancialDashboard: () -> Unit = {},
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val ownerAccount by engine.ownerAccount.collectAsState()
    val payoutAccounts by engine.ownerPayoutAccounts.collectAsState()
    var selectedPeriod by remember { mutableStateOf(TimePeriodFilter.THIS_MONTH) }
    var selectedCountry by remember { mutableStateOf("ALL") }
    var selectedCurrency by remember { mutableStateOf("SAR") }

    var showWithdrawalDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val overview = remember(ownerAccount, selectedCountry, selectedCurrency) {
        engine.getOwnerEarningsOverview(selectedCountry, selectedCurrency)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .testTag("owner_earnings_dashboard_page")
    ) {
        val isDesktop = maxWidth > 840.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Surface(
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Close", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Owner Earnings & Revenue",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFF0284C7).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "FINANCIAL CONSOLE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF38BDF8),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Authoritative double-entry platform revenue engine",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showWithdrawalDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("open_withdrawal_dialog_button")
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Withdraw Funds", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToFinancialDashboard,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("open_financial_master_button")
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Master Ledger & Ops", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Body
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status toast/banner if any
                if (statusMessage != null) {
                    item {
                        Surface(
                            color = Color(0xFF065F46),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(statusMessage!!, color = Color.White, fontSize = 13.sp)
                                IconButton(onClick = { statusMessage = null }) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                                }
                            }
                        }
                    }
                }

                // Filter Bar
                item {
                    FinancialFilterBar(
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = { selectedPeriod = it },
                        selectedCountry = selectedCountry,
                        onCountrySelected = { selectedCountry = it }
                    )
                }

                // Row 1: Balance States
                item {
                    Text(
                        text = "OWNER BALANCES & LIQUIDITY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isDesktop) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AvailableBalanceCard(overview.availableBalance, selectedCurrency, Modifier.weight(1f))
                            PendingBalanceCard(overview.pendingBalance, selectedCurrency, Modifier.weight(1f))
                            ReservedBalanceCard(overview.reservedBalance, selectedCurrency, Modifier.weight(1f))
                            BalanceCard("Withdrawn To Date", overview.withdrawnBalance, selectedCurrency, "TRANSFERRED", Color(0xFF38BDF8), Icons.Default.CheckCircle, Modifier.weight(1f))
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                AvailableBalanceCard(overview.availableBalance, selectedCurrency, Modifier.weight(1f))
                                PendingBalanceCard(overview.pendingBalance, selectedCurrency, Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                ReservedBalanceCard(overview.reservedBalance, selectedCurrency, Modifier.weight(1f))
                                BalanceCard("Withdrawn To Date", overview.withdrawnBalance, selectedCurrency, "TRANSFERRED", Color(0xFF38BDF8), Icons.Default.CheckCircle, Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Row 2: Main Revenue & Deductions Cards
                item {
                    Text(
                        text = "PLATFORM REVENUE BREAKDOWN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val cards = listOf(
                        Triple("Gross Platform GMV", overview.grossGmv, Color(0xFF38BDF8)),
                        Triple("Platform Revenue", overview.platformRevenue, Color(0xFF10B981)),
                        Triple("Net Platform Earnings", overview.netPlatformEarnings, Color(0xFF22C55E)),
                        Triple("Seller Commissions", overview.sellerCommissions, Color(0xFF6366F1)),
                        Triple("Platform Service Fees", overview.platformFees, Color(0xFF0EA5E9)),
                        Triple("Delivery Service Fees", overview.deliveryFees, Color(0xFFA855F7)),
                        Triple("AI Studio Revenue", overview.aiRevenue, Color(0xFFEC4899)),
                        Triple("Subscription Revenue", overview.subscriptionRevenue, Color(0xFFF59E0B)),
                        Triple("Promotional Revenue", overview.promotionalRevenue, Color(0xFFEAB308)),
                        Triple("Refunds Disbursed", overview.refunds, Color(0xFFEF4444)),
                        Triple("Chargeback Reserves", overview.chargebacks, Color(0xFFF97316)),
                        Triple("Payment Costs", overview.paymentProcessingCosts, Color(0xFFF43F5E)),
                        Triple("Taxes & VAT Accrued", overview.taxes, Color(0xFF64748B)),
                        Triple("Manual Adjustments", overview.adjustments, Color(0xFF8B5CF6))
                    )

                    val columns = if (isDesktop) 4 else 2
                    for (row in cards.chunked(columns)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            for (c in row) {
                                FinancialSummaryCard(
                                    title = c.first,
                                    amount = c.second,
                                    currency = selectedCurrency,
                                    icon = Icons.Default.TrendingUp,
                                    accentColor = c.third,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill empty slots if last row has fewer items
                            for (i in 0 until (columns - row.size)) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Row 3: Revenue Trend Chart
                item {
                    RevenueChart(modifier = Modifier.fillMaxWidth())
                }

                // Row 4: Quick Action Bar
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Complete Financial Ledger & Country Breakdown", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Audit double-entry records, manage seller payouts, and export reports", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            }
                            Button(
                                onClick = onNavigateToFinancialDashboard,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Text("Open Master Console")
                            }
                        }
                    }
                }
            }
        }

        // Withdrawal Dialog
        if (showWithdrawalDialog) {
            WithdrawalConfirmationDialog(
                availableBalance = overview.availableBalance,
                currency = selectedCurrency,
                payoutAccounts = payoutAccounts,
                onDismiss = { showWithdrawalDialog = false },
                onConfirm = { amount, accountId, pin ->
                    try {
                        val req = engine.requestOwnerWithdrawal(
                            ownerUid = "owner_root_001",
                            amount = amount,
                            payoutAccountId = accountId,
                            pin = pin,
                            currency = selectedCurrency,
                            country = if (selectedCountry == "ALL") "SA" else selectedCountry
                        )
                        statusMessage = "Withdrawal request of ${formatMoney(amount, selectedCurrency)} submitted! Funds moved to RESERVED."
                        showWithdrawalDialog = false
                    } catch (e: Exception) {
                        statusMessage = "Withdrawal failed: ${e.message}"
                    }
                }
            )
        }
    }
}
