package com.example.healthogram.ui.payments

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.payments.*
import java.util.UUID

/**
 * Healthogram Country-aware Marketplace Checkout & Payment Screen.
 * Demonstrates:
 * - Server-authoritative price breakdown (Subtotal + Shipping + Tax - Discount = Total)
 * - Country-specific payment methods (Cards, Apple Pay, Google Pay, Mada)
 * - Anti-double-tap idempotency protection
 * - Safe processing, 3DS authentication prompt, and seamless result transitions.
 */
@Composable
fun MarketplaceCheckoutPaymentView(
    orderId: String = "ord_${System.currentTimeMillis().toString().takeLast(6)}",
    customerUid: String = "current_user",
    sellerUid: String = "seller_pharmacy_demo",
    sellerName: String = "Apex Healthcare Pharmacy",
    productTitle: String = "Digital Bluetooth Pulse Oximeter & SpO2 Monitor",
    initialSubtotalMinor: Long = 10000L, // SAR 100.00
    onBack: () -> Unit = {},
    onPaymentSuccess: (PaymentTransaction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { PaymentRepository.getInstance() }
    val engine = remember { PaymentEngine.getInstance() }

    var selectedCountryCode by remember { mutableStateOf("SA") }
    var selectedCurrencyCode by remember { mutableStateOf("SAR") }
    var discountCodeInput by remember { mutableStateOf("") }
    var appliedDiscountCode by remember { mutableStateOf<String?>(null) }
    var selectedMethodType by remember { mutableStateOf<PaymentMethodType>(PaymentMethodType.LOCAL_PAYMENT_METHOD) }

    val countryConfig = remember(selectedCountryCode) { repository.getCountryConfig(selectedCountryCode) }
    val currency = remember(selectedCurrencyCode) { repository.getCurrency(selectedCurrencyCode) }
    val availableMethods = remember(selectedCountryCode) { repository.getPaymentMethodsForCountry(selectedCountryCode) }

    // Authoritative Server-side Price Breakdown
    val pricing = remember(selectedCountryCode, selectedCurrencyCode, initialSubtotalMinor, appliedDiscountCode) {
        engine.calculateCheckoutTotal(
            countryCode = selectedCountryCode,
            currencyCode = selectedCurrencyCode,
            subtotalMinor = initialSubtotalMinor,
            shippingMinor = 1500L, // SAR 15.00
            discountCode = appliedDiscountCode
        )
    }

    // Checkout Flow States
    var isProcessing by remember { mutableStateOf(false) }
    var activeTransaction by remember { mutableStateOf<PaymentTransaction?>(null) }
    var show3dsDialog by remember { mutableStateOf(false) }
    var threeDsOtpInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var completedSuccessTx by remember { mutableStateOf<PaymentTransaction?>(null) }
    val idempotencyKey = remember { "chk_${UUID.randomUUID().toString().substring(0, 12)}" }

    if (completedSuccessTx != null) {
        PaymentSuccessView(
            transaction = completedSuccessTx!!,
            onDone = {
                onPaymentSuccess(completedSuccessTx!!)
            }
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("marketplace_checkout_payment_view")
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
                    IconButton(onClick = onBack, enabled = !isProcessing) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text("Secure Checkout", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(
                            "Order #$orderId • ${countryConfig.countryName}",
                            style = HealthogramTheme.typography.caption,
                            color = HealthogramTheme.colors.textMuted
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Country / Currency Switcher Preview
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Public, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Country & Currency", style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    Text("${countryConfig.countryName} (${countryConfig.currencyCode})", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = selectedCountryCode == "SA",
                                    onClick = {
                                        selectedCountryCode = "SA"
                                        selectedCurrencyCode = "SAR"
                                        selectedMethodType = PaymentMethodType.LOCAL_PAYMENT_METHOD
                                    },
                                    label = { Text("KSA (SAR)") }
                                )
                                FilterChip(
                                    selected = selectedCountryCode == "AE",
                                    onClick = {
                                        selectedCountryCode = "AE"
                                        selectedCurrencyCode = "AED"
                                        selectedMethodType = PaymentMethodType.CARD
                                    },
                                    label = { Text("UAE (AED)") }
                                )
                                FilterChip(
                                    selected = selectedCountryCode == "US",
                                    onClick = {
                                        selectedCountryCode = "US"
                                        selectedCurrencyCode = "USD"
                                        selectedMethodType = PaymentMethodType.CARD
                                    },
                                    label = { Text("USA ($)") }
                                )
                            }
                        }
                    }
                }

                // Order Items Summary
                item {
                    Text("Order Summary", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(HealthogramTheme.colors.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = HealthogramTheme.colors.primary)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(productTitle, style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 2)
                                    Text("Sold by: $sellerName", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Qty: 1 • ${currency.formatMinor(initialSubtotalMinor)}", style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                                }
                            }
                        }
                    }
                }

                // Discount Code Box
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = discountCodeInput,
                            onValueChange = { discountCodeInput = it },
                            placeholder = { Text("Promo code (e.g. HEALTH10)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { appliedDiscountCode = discountCodeInput.trim() },
                            enabled = discountCodeInput.isNotBlank()
                        ) {
                            Text("Apply")
                        }
                    }
                    if (appliedDiscountCode != null && pricing.discountMinor > 0) {
                        Text(
                            "Promo code applied: Saved ${currency.formatMinor(pricing.discountMinor)}!",
                            style = HealthogramTheme.typography.caption.copy(color = Color(0xFF10B981), fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }
                }

                // Payment Method Selection
                item {
                    Text("Payment Method", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        availableMethods.forEach { method ->
                            val isSelected = selectedMethodType == method.methodType
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) HealthogramTheme.colors.primary.copy(alpha = 0.08f) else HealthogramTheme.colors.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.borderLight
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMethodType = method.methodType }
                                    .testTag("payment_method_${method.paymentMethodId}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedMethodType = method.methodType }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = when (method.methodType) {
                                            PaymentMethodType.LOCAL_PAYMENT_METHOD -> Icons.Default.CreditCard
                                            PaymentMethodType.CARD -> Icons.Default.Payment
                                            PaymentMethodType.APPLE_PAY -> Icons.Default.PhoneIphone
                                            PaymentMethodType.GOOGLE_PAY -> Icons.Default.AccountBalanceWallet
                                            else -> Icons.Default.Payment
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.textSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(method.displayName, style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                        Text(
                                            if (method.methodType == PaymentMethodType.LOCAL_PAYMENT_METHOD) "Local Saudi Network with 3DS" else "Tokenized & PCI Compliant",
                                            style = HealthogramTheme.typography.caption,
                                            color = HealthogramTheme.colors.textMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Authoritative Price Breakdown Card
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Payment Breakdown", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(10.dp))

                            BreakdownRow("Subtotal", currency.formatMinor(pricing.subtotalMinor))
                            if (pricing.discountMinor > 0) {
                                BreakdownRow("Discount", "-${currency.formatMinor(pricing.discountMinor)}", valueColor = Color(0xFF10B981))
                            }
                            BreakdownRow("Shipping & Handling", currency.formatMinor(pricing.shippingMinor))
                            BreakdownRow(
                                "${countryConfig.taxName} (${countryConfig.taxRatePercent}%)",
                                if (countryConfig.taxInclusive) "Included (${currency.formatMinor(pricing.taxMinor)})" else currency.formatMinor(pricing.taxMinor)
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = HealthogramTheme.colors.borderLight)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Total Payable", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("Verified Server Amount", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                }
                                Text(
                                    currency.formatMinor(pricing.totalPayableMinor),
                                    style = HealthogramTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = HealthogramTheme.colors.primary
                                    )
                                )
                            }
                        }
                    }
                }

                // Security & PCI Badge
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "256-Bit SSL Encrypted • PCI-DSS Tokenized • Zero Card Storage",
                            style = HealthogramTheme.typography.caption.copy(fontSize = 11.sp, color = HealthogramTheme.colors.textMuted)
                        )
                    }
                }

                if (errorMessage != null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HealthogramTheme.colors.error.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, HealthogramTheme.colors.error.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = errorMessage!!,
                                style = HealthogramTheme.typography.bodySmall.copy(color = HealthogramTheme.colors.error),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }

            // Bottom Sticky "Pay Now" Button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = HealthogramTheme.colors.surface,
                shadowElevation = 8.dp,
                border = BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Final Amount", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                        Text(currency.formatMinor(pricing.totalPayableMinor), style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    Button(
                        onClick = {
                            isProcessing = true
                            errorMessage = null
                            try {
                                val tx = PaymentCustomActions.createPaymentSession(
                                    orderId = orderId,
                                    customerUid = customerUid,
                                    sellerUid = sellerUid,
                                    countryCode = selectedCountryCode,
                                    currencyCode = selectedCurrencyCode,
                                    paymentMethodType = selectedMethodType,
                                    subtotalMinor = pricing.subtotalMinor,
                                    shippingMinor = pricing.shippingMinor,
                                    discountCode = appliedDiscountCode,
                                    idempotencyKey = idempotencyKey
                                )
                                activeTransaction = tx

                                if (tx.status == PaymentTransactionStatus.REQUIRES_ACTION) {
                                    show3dsDialog = true
                                } else {
                                    // Direct Capture
                                    val captured = PaymentCustomActions.confirmPayment(tx.paymentTransactionId)
                                    completedSuccessTx = captured
                                }
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Payment initialization failed. Please try again."
                            } finally {
                                isProcessing = false
                            }
                        },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = HealthogramTheme.colors.primary),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("pay_securely_button")
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing...")
                        } else {
                            Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pay Securely (${currency.formatMinor(pricing.totalPayableMinor)})")
                        }
                    }
                }
            }
        }

        // 3D Secure / OTP Simulation Dialog
        if (show3dsDialog && activeTransaction != null) {
            AlertDialog(
                onDismissRequest = { /* Must complete or cancel */ },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = HealthogramTheme.colors.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("3D Secure Bank Verification")
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Your issuing bank requires Strong Customer Authentication for this transaction of ${currency.formatMinor(activeTransaction!!.totalMinor)}.",
                            style = HealthogramTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value = threeDsOtpInput,
                            onValueChange = { threeDsOtpInput = it },
                            label = { Text("One-Time Password (OTP)") },
                            placeholder = { Text("Enter 123456 to verify") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isProcessing = true
                            try {
                                val captured = PaymentCustomActions.confirmPayment(
                                    activeTransaction!!.paymentTransactionId,
                                    threeDsOtpInput
                                )
                                show3dsDialog = false
                                completedSuccessTx = captured
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Verification failed."
                            } finally {
                                isProcessing = false
                            }
                        },
                        enabled = threeDsOtpInput.isNotBlank()
                    ) {
                        Text("Verify & Confirm")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            show3dsDialog = false
                            errorMessage = "Payment cancelled during 3DS verification."
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
    valueColor: Color = HealthogramTheme.colors.textPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textSecondary)
        Text(value, style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = valueColor))
    }
}

/**
 * Payment Success View with invoice/receipt details.
 */
@Composable
fun PaymentSuccessView(
    transaction: PaymentTransaction,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = PaymentRepository.getInstance().getCurrency(transaction.currencyCode)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("payment_success_view"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Payment Successful!", style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text("Your healthcare order has been confirmed.", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textSecondary)

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HealthogramTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Amount Paid", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            Text(currency.formatMinor(transaction.totalMinor), style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Order ID", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            Text(transaction.orderId, style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Payment Gateway", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            Text(transaction.gateway.uppercase(), style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Payment Method", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            Text(transaction.paymentMethod.name, style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("payment_success_continue_button")
                ) {
                    Text("Continue to Orders")
                }
            }
        }
    }
}
