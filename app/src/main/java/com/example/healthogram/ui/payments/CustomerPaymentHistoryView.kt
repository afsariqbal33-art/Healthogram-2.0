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
 * Customer Payment History and Refund Request Management View.
 */
@Composable
fun CustomerPaymentHistoryView(
    customerUid: String = "current_user",
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { PaymentRepository.getInstance() }
    val transactions by repository.transactions.collectAsState()
    val myTransactions = remember(transactions, customerUid) {
        transactions.filter { it.customerUid == customerUid }
    }

    var selectedTxForRefund by remember { mutableStateOf<PaymentTransaction?>(null) }
    var refundReason by remember { mutableStateOf("") }
    var refundStatusMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("customer_payment_history_view")
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
                        Text("Payment History", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("${myTransactions.size} transactions recorded", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                    }
                }
            }

            if (refundStatusMessage != null) {
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        refundStatusMessage!!,
                        style = HealthogramTheme.typography.bodySmall.copy(color = Color(0xFF10B981), fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (myTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.ReceiptLong, contentDescription = null, tint = HealthogramTheme.colors.textMuted, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No payment transactions yet", style = HealthogramTheme.typography.bodyMedium, color = HealthogramTheme.colors.textSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(myTransactions, key = { it.paymentTransactionId }) { tx ->
                        val currency = repository.getCurrency(tx.currencyCode)
                        val formattedDate = remember(tx.createdAt) {
                            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(tx.createdAt))
                        }

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("payment_item_${tx.paymentTransactionId}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Order #${tx.orderId}", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(formattedDate, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                    }
                                    StatusChip(status = tx.status)
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = HealthogramTheme.colors.borderLight)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Total Amount", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                        Text(currency.formatMinor(tx.totalMinor), style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Method", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                        Text("${tx.paymentMethod.name} (${tx.gateway.uppercase()})", style = HealthogramTheme.typography.bodySmall)
                                    }
                                }

                                if (tx.status == PaymentTransactionStatus.CAPTURED || tx.status == PaymentTransactionStatus.SETTLED) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        OutlinedButton(
                                            onClick = { selectedTxForRefund = tx },
                                            modifier = Modifier.testTag("request_refund_button_${tx.paymentTransactionId}")
                                        ) {
                                            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Request Refund")
                                        }
                                    }
                                } else if (tx.status == PaymentTransactionStatus.REFUNDED) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Refund of ${currency.formatMinor(tx.amountRefundedMinor)} successfully processed.",
                                        style = HealthogramTheme.typography.caption.copy(color = Color(0xFF10B981))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Refund Dialog
        if (selectedTxForRefund != null) {
            val tx = selectedTxForRefund!!
            val currency = repository.getCurrency(tx.currencyCode)
            AlertDialog(
                onDismissRequest = { selectedTxForRefund = null },
                title = { Text("Request Refund") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Submit a refund request for Order #${tx.orderId} in the amount of ${currency.formatMinor(tx.totalMinor)}.",
                            style = HealthogramTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value = refundReason,
                            onValueChange = { refundReason = it },
                            label = { Text("Reason for refund") },
                            placeholder = { Text("e.g., Damaged item, Incorrect delivery") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            try {
                                val refund = PaymentCustomActions.requestRefund(
                                    transactionId = tx.paymentTransactionId,
                                    amountMinor = tx.totalMinor,
                                    refundType = RefundType.FULL,
                                    reason = refundReason.ifBlank { "Customer requested return" },
                                    customerUid = customerUid
                                )
                                refundStatusMessage = "Refund of ${currency.formatMinor(refund.amountMinor)} processed successfully!"
                                selectedTxForRefund = null
                                refundReason = ""
                            } catch (e: Exception) {
                                refundStatusMessage = "Refund error: ${e.message}"
                                selectedTxForRefund = null
                            }
                        },
                        enabled = refundReason.isNotBlank()
                    ) {
                        Text("Confirm Refund")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedTxForRefund = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun StatusChip(status: PaymentTransactionStatus) {
    val (bgColor, textColor, label) = when (status) {
        PaymentTransactionStatus.CAPTURED, PaymentTransactionStatus.SETTLED ->
            Triple(Color(0xFF10B981).copy(alpha = 0.15f), Color(0xFF10B981), "Paid")
        PaymentTransactionStatus.REQUIRES_ACTION ->
            Triple(Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFF59E0B), "3DS Required")
        PaymentTransactionStatus.PAYMENT_PENDING, PaymentTransactionStatus.CREATED ->
            Triple(Color(0xFF3B82F6).copy(alpha = 0.15f), Color(0xFF3B82F6), "Pending")
        PaymentTransactionStatus.REFUNDED, PaymentTransactionStatus.PARTIALLY_REFUNDED ->
            Triple(Color(0xFF8B5CF6).copy(alpha = 0.15f), Color(0xFF8B5CF6), "Refunded")
        PaymentTransactionStatus.FAILED, PaymentTransactionStatus.CANCELLED ->
            Triple(Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFEF4444), "Failed")
        else -> Triple(Color.Gray.copy(alpha = 0.15f), Color.Gray, status.name)
    }

    Surface(shape = RoundedCornerShape(12.dp), color = bgColor) {
        Text(
            text = label,
            style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold, color = textColor),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
