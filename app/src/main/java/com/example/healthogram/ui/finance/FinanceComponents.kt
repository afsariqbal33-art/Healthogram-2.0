package com.example.healthogram.ui.finance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.finance.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Healthogram Step 18: Reusable Material 3 Components for Owner Earnings & Financial Ledger.
 */

// Helper to format currency numbers
fun formatMoney(amount: Double, currency: String = "SAR"): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "${formatter.format(amount)} $currency"
}

fun formatTimestamp(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}

@Composable
fun FinancialSummaryCard(
    title: String,
    amount: Double,
    currency: String,
    icon: ImageVector,
    accentColor: Color,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("financial_summary_card_${title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = formatMoney(amount, currency),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
fun BalanceCard(
    title: String,
    amount: Double,
    currency: String,
    badgeText: String,
    badgeColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("balance_card_${title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE2E8F0)
                    )
                }
                Surface(
                    color = badgeColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = formatMoney(amount, currency),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
fun PendingBalanceCard(amount: Double, currency: String, modifier: Modifier = Modifier) {
    BalanceCard(
        title = "Pending Settlement",
        amount = amount,
        currency = currency,
        badgeText = "UNSETTLED",
        badgeColor = Color(0xFFF59E0B),
        icon = Icons.Default.HourglassTop,
        modifier = modifier
    )
}

@Composable
fun AvailableBalanceCard(amount: Double, currency: String, modifier: Modifier = Modifier) {
    BalanceCard(
        title = "Available For Payout",
        amount = amount,
        currency = currency,
        badgeText = "WITHDRAWABLE",
        badgeColor = Color(0xFF10B981),
        icon = Icons.Default.AccountBalanceWallet,
        modifier = modifier
    )
}

@Composable
fun ReservedBalanceCard(amount: Double, currency: String, modifier: Modifier = Modifier) {
    BalanceCard(
        title = "Reserved In Transit",
        amount = amount,
        currency = currency,
        badgeText = "LOCKED",
        badgeColor = Color(0xFF6366F1),
        icon = Icons.Default.Lock,
        modifier = modifier
    )
}

@Composable
fun PayoutStatusBadge(status: SellerPayoutStatus) {
    val (color, text) = when (status) {
        SellerPayoutStatus.REQUESTED -> Color(0xFFF59E0B) to "REQUESTED"
        SellerPayoutStatus.UNDER_REVIEW -> Color(0xFF38BDF8) to "REVIEWING"
        SellerPayoutStatus.APPROVED -> Color(0xFF10B981) to "APPROVED"
        SellerPayoutStatus.PROCESSING -> Color(0xFF6366F1) to "PROCESSING"
        SellerPayoutStatus.SENT -> Color(0xFF0EA5E9) to "SENT"
        SellerPayoutStatus.COMPLETED -> Color(0xFF22C55E) to "COMPLETED"
        SellerPayoutStatus.FAILED -> Color(0xFFEF4444) to "FAILED"
        SellerPayoutStatus.CANCELLED -> Color(0xFF64748B) to "CANCELLED"
        SellerPayoutStatus.HELD -> Color(0xFFF97316) to "HELD"
        SellerPayoutStatus.REVERSED -> Color(0xFFA855F7) to "REVERSED"
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun WithdrawalStatusBadge(status: OwnerWithdrawalStatus) {
    val (color, text) = when (status) {
        OwnerWithdrawalStatus.REQUESTED -> Color(0xFFF59E0B) to "REQUESTED"
        OwnerWithdrawalStatus.UNDER_REVIEW -> Color(0xFF38BDF8) to "UNDER REVIEW"
        OwnerWithdrawalStatus.APPROVED -> Color(0xFF10B981) to "APPROVED"
        OwnerWithdrawalStatus.PROCESSING -> Color(0xFF6366F1) to "DISPATCHING"
        OwnerWithdrawalStatus.COMPLETED -> Color(0xFF22C55E) to "SETTLED"
        OwnerWithdrawalStatus.FAILED -> Color(0xFFEF4444) to "FAILED"
        OwnerWithdrawalStatus.CANCELLED -> Color(0xFF64748B) to "CANCELLED"
        OwnerWithdrawalStatus.HELD -> Color(0xFFF97316) to "HELD"
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun ReconciliationStatusBadge(status: ReconciliationStatus) {
    val (color, text) = when (status) {
        ReconciliationStatus.MATCHED -> Color(0xFF22C55E) to "MATCHED (100%)"
        ReconciliationStatus.MISMATCH -> Color(0xFFEF4444) to "MISMATCH DETECTED"
        ReconciliationStatus.PENDING -> Color(0xFFF59E0B) to "PENDING AUDIT"
        ReconciliationStatus.RESOLVED -> Color(0xFF38BDF8) to "RESOLVED"
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun FinancialFilterBar(
    selectedPeriod: TimePeriodFilter,
    onPeriodSelected: (TimePeriodFilter) -> Unit,
    selectedCountry: String,
    onCountrySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(TimePeriodFilter.entries) { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { onPeriodSelected(period) },
                        label = { Text(period.title, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0284C7),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF0F172A),
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Country Quick Switcher
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("ALL", "SA", "AE", "KW", "US").forEach { code ->
                    Surface(
                        modifier = Modifier.clickable { onCountrySelected(code) },
                        color = if (selectedCountry == code) Color(0xFF38BDF8) else Color(0xFF0F172A),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = code,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedCountry == code) Color(0xFF0F172A) else Color(0xFFCBD5E1),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueChart(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.testTag("revenue_trend_chart"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Revenue & Net Platform Earnings Trend",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "30-day comparative volume vs net commission yields",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "+18.4% vs last month",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visual bar indicator mock for historical days
            val days = listOf("Day 1", "Day 5", "Day 10", "Day 15", "Day 20", "Day 25", "Day 30")
            val gmvHeights = listOf(0.4f, 0.65f, 0.5f, 0.85f, 0.7f, 0.95f, 1.0f)
            val netHeights = listOf(0.12f, 0.20f, 0.15f, 0.28f, 0.22f, 0.32f, 0.35f)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEachIndexed { index, day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.height(100.dp)
                        ) {
                            // Gross GMV bar
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .fillMaxHeight(gmvHeights[index])
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(Color(0xFF38BDF8))
                            )
                            // Net Earnings bar
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .fillMaxHeight(netHeights[index])
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(Color(0xFF10B981))
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(day, fontSize = 9.sp, color = Color(0xFF64748B))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF38BDF8), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gross Platform GMV", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF10B981), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Net Platform Earnings", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }
        }
    }
}

@Composable
fun WithdrawalConfirmationDialog(
    availableBalance: Double,
    currency: String,
    payoutAccounts: List<OwnerPayoutAccount>,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, accountId: String, pin: String) -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(payoutAccounts.firstOrNull()?.accountId ?: "") }
    var pinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val amount = amountInput.toDoubleOrNull() ?: 0.0
    val remainingBalance = maxOf(0.0, availableBalance - amount)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LockReset, contentDescription = null, tint = Color(0xFF38BDF8))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Execute Owner Withdrawal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Funds will be atomically placed in RESERVED state until settlement completes via SWIFT/banking network.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Available Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Available to Withdraw:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Text(formatMoney(availableBalance, currency), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Amount ($currency)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF475569)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("withdrawal_amount_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Payout Destination Account", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(4.dp))
                payoutAccounts.forEach { acc ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAccountId = acc.accountId }
                            .background(
                                if (selectedAccountId == acc.accountId) Color(0xFF0284C7).copy(alpha = 0.2f) else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(6.dp)
                    ) {
                        RadioButton(
                            selected = selectedAccountId == acc.accountId,
                            onClick = { selectedAccountId = acc.accountId }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(acc.provider, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(acc.maskedDestination, fontSize = 10.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { pinInput = it },
                    label = { Text("Owner Security PIN (e.g. 9900)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF475569)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("withdrawal_pin_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMessage!!, fontSize = 11.sp, color = Color(0xFFEF4444))
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Remaining Balance:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Text(formatMoney(remainingBalance, currency), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amount <= 0.0) {
                        errorMessage = "Enter a valid withdrawal amount."
                    } else if (amount > availableBalance) {
                        errorMessage = "Requested amount exceeds available balance."
                    } else if (pinInput.isEmpty()) {
                        errorMessage = "Owner Security PIN is required."
                    } else {
                        onConfirm(amount, selectedAccountId, pinInput)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.testTag("submit_withdrawal_button")
            ) {
                Text("Reserve & Authorize")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}
