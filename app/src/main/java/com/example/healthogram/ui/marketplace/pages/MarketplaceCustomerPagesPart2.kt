package com.example.healthogram.ui.marketplace.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.marketplace.*
import com.example.healthogram.payments.*
import com.example.healthogram.ui.marketplace.components.*

/**
 * HEALTHOGRAM — STEP 08: MARKETPLACE CUSTOMER PAGES (PART 2)
 * 8. MarketplaceWishlistPage
 * 9. MarketplaceCartPage
 * 10. MarketplaceCheckoutPage
 * 11. MarketplaceAddressPage
 * 12. MarketplaceAddAddressPage
 * 13. MarketplacePaymentPage
 * 14. MarketplaceOrderConfirmationPage
 */

// 8. MarketplaceWishlistPage
@Composable
fun MarketplaceWishlistPage(
    repository: MarketplaceRepository,
    onProductClick: (MarketplaceProduct) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wishlist by repository.wishlist.collectAsState()
    val products by repository.products.collectAsState()
    val wishlistedProducts = products.filter { wishlist.contains(it.productId) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_wishlist_page")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("My Wishlist (${wishlistedProducts.size})", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (wishlistedProducts.isEmpty()) {
            MarketplaceEmptyState(
                icon = Icons.Default.FavoriteBorder,
                title = "Your wishlist is empty",
                message = "Explore certified health devices and tap the heart icon to save products."
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    MarketplaceProductGrid(
                        products = wishlistedProducts,
                        wishlist = wishlist,
                        onWishlistToggle = { repository.toggleWishlist(it) },
                        onAddToCart = { repository.addToCart(it) },
                        onProductClick = onProductClick
                    )
                }
            }
        }
    }
}

// 9. MarketplaceCartPage
@Composable
fun MarketplaceCartPage(
    repository: MarketplaceRepository,
    onProceedToCheckout: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cartItems by repository.cartItems.collectAsState()
    val subtotal = cartItems.sumOf { it.lineTotal }

    Scaffold(
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                MarketplaceCartSummary(
                    subtotal = subtotal,
                    currency = cartItems.firstOrNull()?.currency ?: "SAR",
                    onProceedToCheckout = onProceedToCheckout
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(HealthogramTheme.colors.background)
                .padding(paddingValues)
                .padding(16.dp)
                .testTag("marketplace_cart_page")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text("Shopping Cart (${MarketplaceCustomFunctions.getCartItemCount(cartItems)})", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }

                if (cartItems.isNotEmpty()) {
                    TextButton(onClick = { repository.clearCart() }) {
                        Text("Clear All", color = HealthogramTheme.colors.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (cartItems.isEmpty()) {
                MarketplaceEmptyState(
                    icon = Icons.Default.ShoppingCart,
                    title = "Your Cart is Empty",
                    message = "Browse our catalog of certified monitors, supplements, and supplies to add products."
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(cartItems) { item ->
                        MarketplaceCartItem(
                            item = item,
                            onIncrement = { repository.updateCartItemQuantity(item.itemId, item.quantity + 1) },
                            onDecrement = { repository.updateCartItemQuantity(item.itemId, item.quantity - 1) },
                            onRemove = { repository.updateCartItemQuantity(item.itemId, 0) }
                        )
                    }
                }
            }
        }
    }
}

// 10. MarketplaceCheckoutPage
@Composable
fun MarketplaceCheckoutPage(
    repository: MarketplaceRepository,
    onNavigateToAddAddress: () -> Unit,
    onProceedToPayment: (MarketplaceOrderSnapshot) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cartItems by repository.cartItems.collectAsState()
    val addresses by repository.addresses.collectAsState()
    var selectedAddressId by remember { mutableStateOf(addresses.firstOrNull { it.isDefault }?.addressId ?: addresses.firstOrNull()?.addressId ?: "") }
    var couponInput by remember { mutableStateOf("") }
    var appliedCoupon by remember { mutableStateOf<MarketplaceCoupon?>(null) }
    var couponMessage by remember { mutableStateOf<String?>(null) }

    val products by repository.products.collectAsState()
    val catalogMap = remember(products) { products.associateBy { it.productId } }

    val validationResult = remember(cartItems, appliedCoupon) {
        MarketplaceSecurityAndValidation.validateAndCalculateCheckout(cartItems, catalogMap, appliedCoupon)
    }

    val selectedAddress = addresses.find { it.addressId == selectedAddressId } ?: addresses.firstOrNull()

    Scaffold(
        bottomBar = {
            Surface(
                color = HealthogramTheme.colors.surface,
                border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Payable", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                        Text(MarketplaceCustomFunctions.formatMarketplacePrice(validationResult.authoritativeGrandTotal, validationResult.currency), style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                    }

                    Button(
                        onClick = {
                            if (selectedAddress != null && validationResult.isValid) {
                                val order = MarketplaceCustomActions.createMarketplaceOrder(
                                    customerUid = repository.currentCustomerUid,
                                    cartItems = cartItems,
                                    shippingAddress = selectedAddress,
                                    validationResult = validationResult
                                )
                                onProceedToPayment(order)
                            }
                        },
                        enabled = selectedAddress != null && validationResult.isValid,
                        shape = HealthogramTheme.shapes.pill,
                        modifier = Modifier.testTag("proceed_to_payment_btn")
                    ) {
                        Text("Continue to Payment")
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(HealthogramTheme.colors.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text("Checkout & Review", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }

            // 1. Delivery Address Section
            item {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("1. Delivery Address", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        TextButton(onClick = onNavigateToAddAddress) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Address")
                        }
                    }

                    addresses.forEach { addr ->
                        MarketplaceAddressCard(
                            address = addr,
                            isSelected = addr.addressId == selectedAddressId,
                            onSelect = { selectedAddressId = addr.addressId }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // 2. Coupon Box
            item {
                Column {
                    Text("2. Promo / Coupon Code", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = couponInput,
                            onValueChange = { couponInput = it },
                            placeholder = { Text("Enter coupon e.g. HEALTH10") },
                            modifier = Modifier.weight(1f).testTag("coupon_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val (c, res) = MarketplaceCustomActions.applyMarketplaceCoupon(
                                    couponInput,
                                    validationResult.validatedSubtotal,
                                    repository.currentCustomerUid,
                                    repository.availableCoupons
                                )
                                appliedCoupon = c
                                couponMessage = res.message
                            },
                            shape = HealthogramTheme.shapes.pill,
                            modifier = Modifier.testTag("apply_coupon_btn")
                        ) {
                            Text("Apply")
                        }
                    }
                    couponMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = msg,
                            style = HealthogramTheme.typography.caption,
                            color = if (appliedCoupon != null) HealthogramTheme.colors.primary else HealthogramTheme.colors.error
                        )
                    }
                }
            }

            // 3. Checkout Summary Card
            item {
                Text("3. Order Total Review", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                MarketplaceCheckoutSummary(
                    validationResult = validationResult,
                    appliedCouponCode = appliedCoupon?.code
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// 11. MarketplaceAddressPage
@Composable
fun MarketplaceAddressPage(
    repository: MarketplaceRepository,
    onAddNewAddress: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val addresses by repository.addresses.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_address_page")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text("Saved Addresses", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            IconButton(onClick = onAddNewAddress, modifier = Modifier.testTag("add_new_address_top_btn")) {
                Icon(Icons.Default.Add, contentDescription = "Add Address", tint = HealthogramTheme.colors.primary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(addresses) { address ->
                MarketplaceAddressCard(
                    address = address,
                    isSelected = address.isDefault,
                    onSelect = { repository.setDefaultAddress(address.addressId) }
                )
            }
        }
    }
}

// 12. MarketplaceAddAddressPage
@Composable
fun MarketplaceAddAddressPage(
    repository: MarketplaceRepository,
    onAddressSaved: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var label by remember { mutableStateOf("Home") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Riyadh") }
    var district by remember { mutableStateOf("") }
    var addressLine by remember { mutableStateOf("") }
    var deliveryInstructions by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_add_address_page")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Add Delivery Address", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Home", "Work", "Other").forEach { l ->
                        FilterChip(
                            selected = label == l,
                            onClick = { label = l },
                            label = { Text(l) }
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Recipient Full Name") },
                    modifier = Modifier.fillMaxWidth().testTag("address_full_name")
                )
            }

            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone (+966...)") },
                    modifier = Modifier.fillMaxWidth().testTag("address_phone")
                )
            }

            item {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    modifier = Modifier.fillMaxWidth().testTag("address_city")
                )
            }

            item {
                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text("District / Neighborhood") },
                    modifier = Modifier.fillMaxWidth().testTag("address_district")
                )
            }

            item {
                OutlinedTextField(
                    value = addressLine,
                    onValueChange = { addressLine = it },
                    label = { Text("Street Address, Building, Apt") },
                    modifier = Modifier.fillMaxWidth().testTag("address_line")
                )
            }

            item {
                OutlinedTextField(
                    value = deliveryInstructions,
                    onValueChange = { deliveryInstructions = it },
                    label = { Text("Delivery Instructions (Optional)") },
                    modifier = Modifier.fillMaxWidth().testTag("address_instructions")
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (fullName.isNotBlank() && phone.isNotBlank() && addressLine.isNotBlank()) {
                            val newAddr = MarketplaceAddress(
                                customerUid = repository.currentCustomerUid,
                                label = label,
                                fullName = fullName,
                                phone = phone,
                                city = city,
                                district = district,
                                addressLine1 = addressLine,
                                deliveryInstructions = deliveryInstructions.ifBlank { null },
                                isDefault = false
                            )
                            repository.addAddress(newAddr)
                            onAddressSaved()
                        }
                    },
                    shape = HealthogramTheme.shapes.pill,
                    modifier = Modifier.fillMaxWidth().testTag("save_address_btn")
                ) {
                    Text("Save Address")
                }
            }
        }
    }
}

// 13. MarketplacePaymentPage
@Composable
fun MarketplacePaymentPage(
    order: MarketplaceOrderSnapshot,
    repository: MarketplaceRepository,
    onPaymentSuccess: (MarketplaceOrderSnapshot) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMethod by remember { mutableStateOf("MADA") }
    var isProcessing by remember { mutableStateOf(false) }
    val paymentProvider = remember { SaudiMarketplacePaymentProvider() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_payment_page")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Select Payment Method", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = HealthogramTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.primary.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, HealthogramTheme.colors.primary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total to Pay", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Text(MarketplaceCustomFunctions.formatMarketplacePrice(order.grandTotal, order.currency), style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Payment Gateways (Saudi National Compliant)", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(10.dp))

        MarketplacePaymentCard(
            methodName = "Mada Debit Card",
            methodIcon = Icons.Default.CreditCard,
            isSelected = selectedMethod == "MADA",
            onSelect = { selectedMethod = "MADA" }
        )
        Spacer(modifier = Modifier.height(8.dp))

        MarketplacePaymentCard(
            methodName = "Apple Pay",
            methodIcon = Icons.Default.PhoneIphone,
            isSelected = selectedMethod == "APPLE_PAY",
            onSelect = { selectedMethod = "APPLE_PAY" }
        )
        Spacer(modifier = Modifier.height(8.dp))

        MarketplacePaymentCard(
            methodName = "STC Pay Digital Wallet",
            methodIcon = Icons.Default.AccountBalanceWallet,
            isSelected = selectedMethod == "STC_PAY",
            onSelect = { selectedMethod = "STC_PAY" }
        )
        Spacer(modifier = Modifier.height(8.dp))

        MarketplacePaymentCard(
            methodName = "Credit Card (Visa / Mastercard)",
            methodIcon = Icons.Default.Payment,
            isSelected = selectedMethod == "CREDIT_CARD",
            onSelect = { selectedMethod = "CREDIT_CARD" }
        )

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                isProcessing = true
                coroutineScope.launch {
                    val res = MarketplaceCustomActions.startMarketplacePayment(order, paymentProvider, selectedMethod)
                    paymentProvider.confirmPayment(res.paymentId, "token_demo")
                    val pType = when (selectedMethod) {
                        "MADA" -> PaymentMethodType.LOCAL_PAYMENT_METHOD
                        "APPLE_PAY" -> PaymentMethodType.APPLE_PAY
                        "STC_PAY" -> PaymentMethodType.LOCAL_WALLET
                        else -> PaymentMethodType.CARD
                    }
                    try {
                        val tx = PaymentCustomActions.createPaymentSession(
                            orderId = order.orderId,
                            customerUid = order.customerUid,
                            sellerUid = "seller_default",
                            countryCode = order.countryCode,
                            currencyCode = order.currency,
                            paymentMethodType = pType,
                            subtotalMinor = (order.subtotal * 100).toLong(),
                            shippingMinor = (order.deliveryTotal * 100).toLong(),
                            discountCode = null,
                            idempotencyKey = "ord_pay_${order.orderId}"
                        )
                        PaymentCustomActions.confirmPayment(tx.paymentTransactionId)
                    } catch (_: Exception) {}
                    val paidOrder = order.copy(
                        paymentStatus = PaymentStatus.PAID,
                        orderStatus = MarketplaceOrderStatus.PAID,
                        confirmedAt = System.currentTimeMillis()
                    )
                    repository.addOrder(paidOrder)
                    repository.clearCart()
                    isProcessing = false
                    onPaymentSuccess(paidOrder)
                }
            },
            enabled = !isProcessing,
            shape = HealthogramTheme.shapes.pill,
            modifier = Modifier.fillMaxWidth().testTag("confirm_pay_btn")
        ) {
            if (isProcessing) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text("Pay ${MarketplaceCustomFunctions.formatMarketplacePrice(order.grandTotal, order.currency)}")
            }
        }
    }
}

// 14. MarketplaceOrderConfirmationPage
@Composable
fun MarketplaceOrderConfirmationPage(
    order: MarketplaceOrderSnapshot,
    onTrackOrder: () -> Unit,
    onViewInvoice: () -> Unit,
    onContinueShopping: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(24.dp)
            .testTag("marketplace_order_confirmation_page"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(HealthogramTheme.colors.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Order Confirmed!", style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(6.dp))
        Text("Thank you for your order. Your medical shipment is being prepared.", style = HealthogramTheme.typography.bodyMedium, color = HealthogramTheme.colors.textMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)

        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            shape = HealthogramTheme.shapes.medium,
            color = HealthogramTheme.colors.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Order Number: ${order.orderNumber}", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text("Amount Paid: ${MarketplaceCustomFunctions.formatMarketplacePrice(order.grandTotal, order.currency)}", style = HealthogramTheme.typography.bodySmall)
                Text("Delivery To: ${order.shippingAddressSnapshot.fullName}, ${order.shippingAddressSnapshot.city}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onViewInvoice, shape = HealthogramTheme.shapes.pill) {
                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("View Invoice")
            }
            Button(onClick = onTrackOrder, shape = HealthogramTheme.shapes.pill) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Track Order")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onContinueShopping) {
            Text("Continue Shopping")
        }
    }
}
