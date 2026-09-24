package com.example.healthogram.ui.finance

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.finance.*

enum class FinancialDashboardTab(val title: String) {
    OVERVIEW("Overview"),
    LEDGER("Double-Entry Ledger"),
    COMMISSIONS("Commission Engine"),
    SELLER_PAYOUTS("Seller Payouts"),
    WITHDRAWALS("Owner Withdrawals"),
    REFUNDS_DISPUTES("Refunds & Chargebacks"),
    RECONCILIATION("Reconciliation"),
    REPORTS("Reports & Exports"),
    ALERTS_INTEGRITY("Alerts & Integrity")
}

/**
 * Healthogram Step 18: Master Owner Financial Dashboard.
 * Enterprise command center for double-entry bookkeeping, multi-country commission rules,
 * seller settlements, payout execution, reconciliation, and compliance reports.
 */
@Composable
fun OwnerFinancialDashboardPage(
    engine: FinancialLedgerEngine = remember { FinancialLedgerEngine.getInstance() },
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(FinancialDashboardTab.OVERVIEW) }
    val ownerAccount by engine.ownerAccount.collectAsState()
    val ledgerEntries by engine.ledgerEntries.collectAsState()
    val commissionRules by engine.commissionRules.collectAsState()
    val sellerPayouts by engine.sellerPayoutRequests.collectAsState()
    val ownerWithdrawals by engine.ownerWithdrawalRequests.collectAsState()
    val refunds by engine.refunds.collectAsState()
    val chargebacks by engine.chargebacks.collectAsState()
    val reconciliationRuns by engine.reconciliationRuns.collectAsState()
    val alerts by engine.financialAlerts.collectAsState()
    val exportJobs by engine.exportJobs.collectAsState()

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .testTag("owner_financial_dashboard_page")
    ) {
        val isDesktop = maxWidth > 840.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Navigation & App Bar
            Surface(
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
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
                                Text(
                                    text = "Financial Master Console",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Double-Entry Ledger • Multi-Country Settlement • Payouts",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Search and Quick Actions
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search Txn, Seller, Account...", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .width(220.dp)
                                    .height(44.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF334155)
                                )
                            )
                        }
                    }

                    // Tab Row
                    ScrollableTabRow(
                        selectedTabIndex = activeTab.ordinal,
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color(0xFF38BDF8),
                        edgePadding = 16.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FinancialDashboardTab.entries.forEach { tab ->
                            Tab(
                                selected = activeTab == tab,
                                onClick = { activeTab = tab },
                                text = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Body Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (activeTab) {
                    FinancialDashboardTab.OVERVIEW -> {
                        OverviewTab(engine = engine)
                    }
                    FinancialDashboardTab.LEDGER -> {
                        LedgerTab(
                            entries = ledgerEntries,
                            searchQuery = searchQuery,
                            onReverse = { entryId ->
                                try {
                                    engine.reverseLedgerEntry(entryId, "owner_root_001", "Owner manual reversal via Master Console")
                                    statusMessage = "Ledger entry $entryId successfully reversed!"
                                } catch (e: Exception) {
                                    statusMessage = "Reversal error: ${e.message}"
                                }
                            }
                        )
                    }
                    FinancialDashboardTab.COMMISSIONS -> {
                        CommissionTab(rules = commissionRules)
                    }
                    FinancialDashboardTab.SELLER_PAYOUTS -> {
                        SellerPayoutsTab(
                            requests = sellerPayouts,
                            onApprove = { id ->
                                engine.approveSellerPayout(id)
                                statusMessage = "Seller payout $id approved."
                            },
                            onComplete = { id ->
                                engine.completeSellerPayout(id)
                                statusMessage = "Seller payout $id completed."
                            }
                        )
                    }
                    FinancialDashboardTab.WITHDRAWALS -> {
                        OwnerWithdrawalsTab(
                            withdrawals = ownerWithdrawals,
                            onComplete = { id ->
                                engine.completeOwnerWithdrawal(id)
                                statusMessage = "Withdrawal $id completed."
                            },
                            onCancel = { id ->
                                engine.failOrCancelOwnerWithdrawal(id, "Cancelled by Owner")
                                statusMessage = "Withdrawal $id cancelled and funds restored."
                            }
                        )
                    }
                    FinancialDashboardTab.REFUNDS_DISPUTES -> {
                        RefundsAndChargebacksTab(refunds = refunds, chargebacks = chargebacks)
                    }
                    FinancialDashboardTab.RECONCILIATION -> {
                        ReconciliationTab(
                            runs = reconciliationRuns,
                            onRunReconciliation = {
                                engine.runFinancialReconciliation()
                                statusMessage = "Reconciliation run completed."
                            }
                        )
                    }
                    FinancialDashboardTab.REPORTS -> {
                        ReportsTab(
                            exportJobs = exportJobs,
                            onExport = { name, format ->
                                engine.exportFinancialReport(name, format, "Last 30 Days", "SA", "SAR")
                                statusMessage = "Export job queued for $name ($format)."
                            }
                        )
                    }
                    FinancialDashboardTab.ALERTS_INTEGRITY -> {
                        AlertsAndIntegrityTab(engine = engine, alerts = alerts)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 0: OVERVIEW & COUNTRY ANALYTICS
// -------------------------------------------------------------
@Composable
private fun OverviewTab(engine: FinancialLedgerEngine) {
    val countries = remember { engine.getCountryFinancialSummaries() }
    val categories = remember { engine.getCategoryRevenueBreakdown() }
    val sources by engine.revenueSources.collectAsState()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("COUNTRY-WISE FINANCIAL ANALYTICS", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    countries.forEach { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFF0284C7).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(c.countryCode, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), modifier = Modifier.padding(4.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(c.countryName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Gross GMV: ${formatMoney(c.grossGmv, c.currency)}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatMoney(c.netEarnings, c.currency), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                                Text("Commissions: ${formatMoney(c.commissions, c.currency)}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }
                        if (c != countries.last()) {
                            Divider(color = Color(0xFF334155), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        item {
            Text("REVENUE BY HEALTHCARE CATEGORY", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(cat.categoryName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Gross GMV: ${formatMoney(cat.grossSales, cat.currency)}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatMoney(cat.commissionEarned, cat.currency), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                Text("${cat.percentageOfRevenue}% of platform", fontSize = 11.sp, color = Color(0xFF10B981))
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: DOUBLE-ENTRY FINANCIAL LEDGER
// -------------------------------------------------------------
@Composable
private fun LedgerTab(
    entries: List<FinancialLedgerEntry>,
    searchQuery: String,
    onReverse: (String) -> Unit
) {
    val filtered = remember(entries, searchQuery) {
        if (searchQuery.isBlank()) entries else {
            entries.filter {
                it.entryId.contains(searchQuery, true) ||
                        it.entryType.contains(searchQuery, true) ||
                        it.accountId.contains(searchQuery, true) ||
                        it.description.contains(searchQuery, true)
            }
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "POSTED IMMUTABLE LEDGER ENTRIES (${filtered.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "Double-Entry Balanced Bookkeeping",
                    fontSize = 11.sp,
                    color = Color(0xFF38BDF8)
                )
            }
        }

        items(filtered) { entry ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(
                    1.dp,
                    if (entry.status == LedgerEntryStatus.REVERSED) Color(0xFFA855F7).copy(alpha = 0.5f) else Color(0xFF334155)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (entry.direction == LedgerDirection.DEBIT) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = entry.direction.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (entry.direction == LedgerDirection.DEBIT) Color(0xFFEF4444) else Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(entry.entryType, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            if (entry.status == LedgerEntryStatus.REVERSED) {
                                Surface(color = Color(0xFFA855F7).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                    Text("REVERSED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA855F7), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${entry.entryId} • Account: ${entry.accountId} • Txn: ${entry.ledgerTransactionId}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = entry.description,
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${if (entry.direction == LedgerDirection.DEBIT) "+" else "-"}${formatMoney(entry.amount, entry.currency)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = if (entry.direction == LedgerDirection.DEBIT) Color(0xFF38BDF8) else Color(0xFF10B981)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (entry.status == LedgerEntryStatus.POSTED) {
                            OutlinedButton(
                                onClick = { onReverse(entry.entryId) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF97316)),
                                border = BorderStroke(1.dp, Color(0xFFF97316)),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Reverse", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: COMMISSION ENGINE & RULES
// -------------------------------------------------------------
@Composable
private fun CommissionTab(rules: List<CommissionRule>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("COUNTRY-WISE MARKETPLACE COMMISSION RULES", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
        }
        items(rules) { rule ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(rule.ruleId, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(color = Color(0xFF0284C7).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                Text(rule.country, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Category: ${rule.productCategory} • Seller Type: ${rule.sellerType}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text("Cap: ${formatMoney(rule.maximumFee, rule.currency)} max fee", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${(rule.commissionRate * 100).toInt()}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF10B981)
                        )
                        Text(rule.commissionType.name, fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: SELLER PAYOUTS
// -------------------------------------------------------------
@Composable
private fun SellerPayoutsTab(
    requests: List<SellerPayoutRequest>,
    onApprove: (String) -> Unit,
    onComplete: (String) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("SELLER PAYOUT REQUESTS & DISPATCH", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
        }
        items(requests) { req ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(req.sellerName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            PayoutStatusBadge(req.status)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Payout ID: ${req.payoutId} • Seller ID: ${req.sellerId}", fontSize = 11.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(formatMoney(req.requestedAmount, req.currency), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (req.status == SellerPayoutStatus.UNDER_REVIEW || req.status == SellerPayoutStatus.REQUESTED) {
                                Button(
                                    onClick = { onApprove(req.payoutId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Approve", fontSize = 10.sp)
                                }
                            }
                            if (req.status == SellerPayoutStatus.APPROVED) {
                                Button(
                                    onClick = { onComplete(req.payoutId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Dispatch", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 4: OWNER WITHDRAWALS
// -------------------------------------------------------------
@Composable
private fun OwnerWithdrawalsTab(
    withdrawals: List<OwnerWithdrawalRequest>,
    onComplete: (String) -> Unit,
    onCancel: (String) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("PLATFORM OWNER WITHDRAWAL AUDIT TRAIL", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
        }
        items(withdrawals) { wd ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(wd.withdrawalId, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.width(8.dp))
                            WithdrawalStatusBadge(wd.status)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(wd.destinationReference, fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text(formatTimestamp(wd.requestedAt), fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(formatMoney(wd.requestedAmount, wd.currency), fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Spacer(modifier = Modifier.height(6.dp))
                        if (wd.status == OwnerWithdrawalStatus.REQUESTED) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { onComplete(wd.withdrawalId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Complete", fontSize = 10.sp)
                                }
                                OutlinedButton(
                                    onClick = { onCancel(wd.withdrawalId) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Cancel", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 5: REFUNDS & CHARGEBACKS
// -------------------------------------------------------------
@Composable
private fun RefundsAndChargebacksTab(
    refunds: List<FinancialRefund>,
    chargebacks: List<FinancialChargeback>
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("REFUNDS DISBURSED (${refunds.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(8.dp))
            if (refunds.isEmpty()) {
                Text("No recent refund claims recorded.", fontSize = 12.sp, color = Color(0xFF64748B))
            } else {
                refunds.forEach { ref ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Refund: ${ref.refundId} • Order: ${ref.orderId}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(ref.reason, fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                            Text(formatMoney(ref.refundAmount, ref.currency), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }

        item {
            Text("CHARGEBACK LIABILITIES (${chargebacks.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(8.dp))
            if (chargebacks.isEmpty()) {
                Text("No active cardholder chargebacks.", fontSize = 12.sp, color = Color(0xFF64748B))
            } else {
                chargebacks.forEach { chg ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Chargeback: ${chg.chargebackId} • Status: ${chg.status.name}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(chg.reason, fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                            Text(formatMoney(chg.amount, chg.currency), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF97316))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 6: RECONCILIATION
// -------------------------------------------------------------
@Composable
private fun ReconciliationTab(
    runs: List<FinancialReconciliationRun>,
    onRunReconciliation: () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PAYMENT GATEWAY & LEDGER RECONCILIATION", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                Button(
                    onClick = onRunReconciliation,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Run Full Reconciliation", fontSize = 11.sp)
                }
            }
        }

        items(runs) { run ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Run ID: ${run.runId} • Gateway: ${run.provider}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(formatTimestamp(run.runAt), fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                        ReconciliationStatusBadge(run.status)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Checked: ${run.totalRecordsChecked}", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                        Text("Matched: ${run.matchedRecords}", fontSize = 11.sp, color = Color(0xFF10B981))
                        Text("Mismatches: ${run.mismatchedRecords}", fontSize = 11.sp, color = Color(0xFFEF4444))
                    }

                    if (run.discrepancies.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Discrepancy Details:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                        run.discrepancies.forEach { d ->
                            Text("• ${d.transactionRef} (${d.provider}): Diff ${formatMoney(d.discrepancyAmount, run.currency)} - ${d.reason}", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 7: REPORTS & EXPORTS
// -------------------------------------------------------------
@Composable
private fun ReportsTab(
    exportJobs: List<FinancialExportJob>,
    onExport: (name: String, format: ReportExportFormat) -> Unit
) {
    val reportTemplates = listOf(
        "Daily Revenue & Settlement Report",
        "Monthly Owner Financial Ledger Snapshot",
        "Country-Wise VAT & Tax Liability Summary",
        "Seller Payout & Commission Reconciliation",
        "Payment Processing Cost & Margin Analysis"
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("FINANCIAL COMPLIANCE REPORTS & AUDIT SNAPSHOTS", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
        }

        items(reportTemplates) { reportName ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(reportName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Certified server-side financial snapshot", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { onExport(reportName, ReportExportFormat.CSV) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8))
                        ) {
                            Text("CSV", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { onExport(reportName, ReportExportFormat.XLSX) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981)),
                            border = BorderStroke(1.dp, Color(0xFF10B981))
                        ) {
                            Text("XLSX", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { onExport(reportName, ReportExportFormat.PDF) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF97316)),
                            border = BorderStroke(1.dp, Color(0xFFF97316))
                        ) {
                            Text("PDF", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text("GENERATED EXPORT JOBS (${exportJobs.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
        }

        items(exportJobs) { job ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(job.reportName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("File: ${job.fileUri} • Date: ${formatTimestamp(job.createdAt)}", fontSize = 10.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
                    }
                    Surface(color = Color(0xFF10B981).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                        Text("READY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 8: ALERTS & DOUBLE-ENTRY INTEGRITY CHECK
// -------------------------------------------------------------
@Composable
private fun AlertsAndIntegrityTab(
    engine: FinancialLedgerEngine,
    alerts: List<FinancialAlert>
) {
    var integrityCheck by remember { mutableStateOf(engine.runFinancialIntegrityCheck()) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, if (integrityCheck.isBalanced) Color(0xFF10B981) else Color(0xFFEF4444)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (integrityCheck.isBalanced) Icons.Default.VerifiedUser else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (integrityCheck.isBalanced) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Double-Entry Bookkeeping Integrity Verification", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Verifies Total Posted Debits == Total Posted Credits across all accounts", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }
                        Button(
                            onClick = { integrityCheck = engine.runFinancialIntegrityCheck() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            Text("Re-verify", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column {
                            Text("Total Debits:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(formatMoney(integrityCheck.totalDebits, "SAR"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                        }
                        Column {
                            Text("Total Credits:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(formatMoney(integrityCheck.totalCredits, "SAR"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                        Column {
                            Text("Imbalance Variance:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(formatMoney(integrityCheck.imbalanceDifference, "SAR"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (integrityCheck.isBalanced) Color(0xFF10B981) else Color(0xFFEF4444))
                        }
                        Column {
                            Text("Status:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(integrityCheck.status, fontSize = 14.sp, fontWeight = FontWeight.Black, color = if (integrityCheck.isBalanced) Color(0xFF10B981) else Color(0xFFEF4444))
                        }
                    }
                }
            }
        }

        item {
            Text("FINANCIAL ALERTS & ANOMALIES (${alerts.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
        }

        items(alerts) { alert ->
            val color = when (alert.severity) {
                AlertSeverity.CRITICAL -> Color(0xFFEF4444)
                AlertSeverity.WARNING -> Color(0xFFF59E0B)
                AlertSeverity.INFO -> Color(0xFF38BDF8)
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(alert.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(alert.message, fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    }
                }
            }
        }
    }
}
