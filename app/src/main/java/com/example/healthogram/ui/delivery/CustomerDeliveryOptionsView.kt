package com.example.healthogram.ui.delivery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.delivery.*
import com.example.healthogram.designsystem.HealthogramTheme

/**
 * Customer Marketplace Delivery Options & Checkout Shipping Quote Selector.
 * Allows choosing delivery mode, calculating server-authoritative rates,
 * validating zones, and picking scheduled time slots.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDeliveryOptionsView(
    customerUid: String = "usr_patient_01",
    onBack: () -> Unit = {},
    onDeliverySelected: (ShippingQuote) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { DeliveryRepository.getInstance() }
    val engine = remember { DeliveryEngine(repository) }

    val customerAddressesMap by repository.customerAddresses.collectAsState()
    val addresses = customerAddressesMap[customerUid] ?: emptyList()
    var selectedAddress by remember(addresses) { mutableStateOf(addresses.firstOrNull()) }

    var selectedServiceType by remember { mutableStateOf(DeliveryServiceType.SAME_DAY) }
    var selectedMode by remember { mutableStateOf(DeliveryMode.PLATFORM_MANAGED) }

    val slots by repository.deliverySlots.collectAsState()
    var selectedSlotId by remember(slots) { mutableStateOf(slots.firstOrNull()?.slotId) }

    // Mock cart items for shipping calculation
    val cartProducts = remember {
        listOf(
            SuborderProductItem(
                productId = "prod_001",
                productName = "Cold-Chain Bio-Complex Supplements",
                quantity = 2,
                unitPriceMinor = 14500L,
                weightGrams = 250,
                temperatureSensitive = true
            )
        )
    }

    val sellerAddress = remember {
        CustomerDeliveryAddress(
            addressId = "addr_seller_01",
            customerUid = "seller_alnoor_pharmacy",
            countryCode = "SA",
            fullName = "Al-Noor Central Pharmacy",
            phoneReference = "+966 11 222 3333",
            addressLine1 = "King Fahd Medical District",
            city = "Riyadh",
            region = "Riyadh Province",
            postalCode = "11564"
        )
    }

    // Server-calculated quote
    val shippingQuote = remember(selectedAddress, selectedMode, selectedServiceType) {
        if (selectedAddress != null) {
            try {
                engine.calculateShippingRate(
                    customerAddress = selectedAddress!!,
                    sellerAddress = sellerAddress,
                    products = cartProducts,
                    deliveryMode = selectedMode,
                    serviceType = selectedServiceType
                )
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Delivery Options",
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("delivery_options_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HealthogramTheme.colors.surface)
            )
        },
        bottomBar = {
            Surface(
                color = HealthogramTheme.colors.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Shipping Fee",
                            style = HealthogramTheme.typography.bodySmall,
                            color = HealthogramTheme.colors.textSecondary
                        )
                        Text(
                            text = if (shippingQuote != null) {
                                DeliveryCustomActions.formatShippingAmount(shippingQuote.totalShippingMinor, shippingQuote.currencyCode)
                            } else "SAR 0.00",
                            style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.primary
                        )
                    }

                    Button(
                        onClick = {
                            if (shippingQuote != null) {
                                onDeliverySelected(shippingQuote)
                            }
                        },
                        enabled = shippingQuote != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HealthogramTheme.colors.primary),
                        modifier = Modifier.testTag("confirm_delivery_button")
                    ) {
                        Text("Confirm Delivery Option")
                    }
                }
            }
        },
        containerColor = HealthogramTheme.colors.background,
        modifier = modifier.testTag("customer_delivery_options_view")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Delivery Address Card
            item {
                Text(
                    text = "Delivery Destination",
                    style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.textPrimary
                )
            }

            items(addresses) { address ->
                val isSelected = selectedAddress?.addressId == address.addressId
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) HealthogramTheme.colors.primary.copy(alpha = 0.08f) else HealthogramTheme.colors.surface
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.divider
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedAddress = address }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedAddress = address }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = address.fullName,
                                    style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = HealthogramTheme.colors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (address.isDefault) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("Default", style = HealthogramTheme.typography.labelSmall) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${address.addressLine1}, ${address.city} (${address.postalCode})",
                                style = HealthogramTheme.typography.bodySmall,
                                color = HealthogramTheme.colors.textSecondary
                            )
                            Text(
                                text = "Country: ${address.countryCode} • Phone: ${address.phoneReference}",
                                style = HealthogramTheme.typography.labelSmall,
                                color = HealthogramTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }

            // 2. Delivery Service Selection
            item {
                Text(
                    text = "Choose Fulfillment Speed",
                    style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.textPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Same Day Express Fleet
            item {
                ServiceOptionCard(
                    title = "Healthogram Express Fleet (Same-Day)",
                    subtitle = "Climate-controlled delivery within 4–6 hours.",
                    priceText = "SAR 34.50",
                    badgeText = "Recommended for Medical Items",
                    icon = Icons.Default.ElectricBolt,
                    isSelected = selectedServiceType == DeliveryServiceType.SAME_DAY,
                    onClick = {
                        selectedServiceType = DeliveryServiceType.SAME_DAY
                        selectedMode = DeliveryMode.PLATFORM_MANAGED
                    }
                )
            }

            // Standard Carrier
            item {
                ServiceOptionCard(
                    title = "Standard Carrier (Aramex / SMSA)",
                    subtitle = "Delivered within 1–2 business days.",
                    priceText = "SAR 17.25",
                    badgeText = "Standard Courier",
                    icon = Icons.Default.LocalShipping,
                    isSelected = selectedServiceType == DeliveryServiceType.STANDARD,
                    onClick = {
                        selectedServiceType = DeliveryServiceType.STANDARD
                        selectedMode = DeliveryMode.THIRD_PARTY
                    }
                )
            }

            // Clinic / Seller Pickup
            item {
                ServiceOptionCard(
                    title = "Clinic / Store Self-Pickup",
                    subtitle = "Collect directly from seller's clinic with zero delivery fee.",
                    priceText = "FREE",
                    badgeText = "Instant Collection",
                    icon = Icons.Default.Storefront,
                    isSelected = selectedServiceType == DeliveryServiceType.PICKUP,
                    onClick = {
                        selectedServiceType = DeliveryServiceType.PICKUP
                        selectedMode = DeliveryMode.CUSTOMER_PICKUP
                    }
                )
            }

            // 3. Delivery Time Window Slots
            if (selectedServiceType != DeliveryServiceType.PICKUP) {
                item {
                    Text(
                        text = "Preferred Delivery Window",
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.textPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(slots) { slot ->
                    val isSlotSelected = selectedSlotId == slot.slotId
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSlotSelected) HealthogramTheme.colors.primary.copy(alpha = 0.08f) else HealthogramTheme.colors.surface
                        ),
                        border = BorderStroke(
                            width = if (isSlotSelected) 2.dp else 1.dp,
                            color = if (isSlotSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.divider
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSlotId = slot.slotId }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isSlotSelected,
                                    onClick = { selectedSlotId = slot.slotId }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${slot.date} (${slot.startTime} - ${slot.endTime})",
                                        style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = HealthogramTheme.colors.textPrimary
                                    )
                                    Text(
                                        text = "${slot.availableCapacity} courier slots remaining",
                                        style = HealthogramTheme.typography.labelSmall,
                                        color = HealthogramTheme.colors.textSecondary
                                    )
                                }
                            }
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = HealthogramTheme.colors.primary)
                        }
                    }
                }
            }

            // 4. Rate Breakdown Summary Card
            if (shippingQuote != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Shipping Cost Summary",
                                style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = HealthogramTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Base Shipping Rate", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textSecondary)
                                Text(
                                    DeliveryCustomActions.formatShippingAmount(shippingQuote.shippingFeeMinor, shippingQuote.currencyCode),
                                    style = HealthogramTheme.typography.bodySmall,
                                    color = HealthogramTheme.colors.textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("VAT & Local Surcharge", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textSecondary)
                                Text(
                                    DeliveryCustomActions.formatShippingAmount(shippingQuote.taxMinor, shippingQuote.currencyCode),
                                    style = HealthogramTheme.typography.bodySmall,
                                    color = HealthogramTheme.colors.textPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = HealthogramTheme.colors.divider)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Calculated Shipping Total", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.textPrimary)
                                Text(
                                    DeliveryCustomActions.formatShippingAmount(shippingQuote.totalShippingMinor, shippingQuote.currencyCode),
                                    style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = HealthogramTheme.colors.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceOptionCard(
    title: String,
    subtitle: String,
    priceText: String,
    badgeText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) HealthogramTheme.colors.primary.copy(alpha = 0.08f) else HealthogramTheme.colors.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.divider
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.textSecondary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = HealthogramTheme.typography.bodySmall,
                    color = HealthogramTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                SuggestionChip(
                    onClick = {},
                    label = { Text(badgeText, style = HealthogramTheme.typography.labelSmall) }
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = priceText,
                style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.textPrimary
            )
        }
    }
}
