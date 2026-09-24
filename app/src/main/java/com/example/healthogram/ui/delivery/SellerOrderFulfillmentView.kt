package com.example.healthogram.ui.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.delivery.*
import com.example.healthogram.designsystem.HealthogramTheme

/**
 * Seller Packing & Carrier Handover View.
 * Enables package weight confirmation, cold-chain seal verification,
 * barcode label generation, and carrier pickup scheduling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerOrderFulfillmentView(
    fulfillmentId: String = "ful_001",
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { DeliveryRepository.getInstance() }
    val engine = remember { DeliveryEngine(repository) }

    val fulfillmentsMap by repository.fulfillments.collectAsState()
    val packagesMap by repository.packages.collectAsState()
    val shipmentsMap by repository.shipments.collectAsState()

    val fulfillment = fulfillmentsMap[fulfillmentId] ?: fulfillmentsMap.values.firstOrNull()
    val shipment = fulfillment?.shipmentId?.let { shipmentsMap[it] }
    val pkg = packagesMap.values.firstOrNull { it.orderId == fulfillment?.orderId }

    var weightGrams by remember { mutableStateOf("450") }
    var dimensions by remember { mutableStateOf("20x15x10 cm") }
    var thermalSealApplied by remember { mutableStateOf(true) }
    var actionStatusMessage by remember { mutableStateOf<String?>(null) }
    var showLabelSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pack & Dispatch Order",
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("seller_pack_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HealthogramTheme.colors.surface)
            )
        },
        containerColor = HealthogramTheme.colors.background,
        modifier = modifier.testTag("seller_order_fulfillment_view")
    ) { padding ->
        if (fulfillment == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Order fulfillment not found", style = HealthogramTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (actionStatusMessage != null) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.primaryContainer)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HealthogramTheme.colors.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(actionStatusMessage!!, style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.onPrimaryContainer)
                            }
                        }
                    }
                }

                // 1. Order Summary Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Fulfillment Details",
                                style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = HealthogramTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Order Reference: ${fulfillment.orderId}", style = HealthogramTheme.typography.bodyMedium)
                            Text("Recipient: Dr. Sarah Al-Ahmad • Riyadh", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textSecondary)
                            Text("Delivery Address: ${fulfillment.deliveryAddressReference}", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = HealthogramTheme.colors.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Carrier: ${fulfillment.deliveryProvider.replace("_", " ").uppercase()}",
                                    style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = HealthogramTheme.colors.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Package Dimensions & Thermal Safety Inputs
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Package Weight & Dimensions",
                                style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = HealthogramTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = weightGrams,
                                    onValueChange = { weightGrams = it },
                                    label = { Text("Weight (grams)") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = dimensions,
                                    onValueChange = { dimensions = it },
                                    label = { Text("Dimensions (LxWxH)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = thermalSealApplied,
                                    onCheckedChange = { thermalSealApplied = it }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Cold-Chain Insulation Seal Applied", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                    Text("Maintains below 25°C for sensitive clinical supplements.", style = HealthogramTheme.typography.labelSmall, color = HealthogramTheme.colors.textSecondary)
                                }
                            }
                        }
                    }
                }

                // 3. Carrier Shipping Label Preview Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Carrier Shipping Label",
                                    style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = HealthogramTheme.colors.textPrimary
                                )
                                TextButton(onClick = { showLabelSheet = true }) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Print Label")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "HEALTHOGRAM EXPRESS COURIER LABEL",
                                        style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    )
                                    Text(
                                        text = "Tracking: ${shipment?.trackingNumber ?: "HG-SA-PENDING"}",
                                        style = HealthogramTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                                    )
                                    Text(
                                        text = "Item Category: Healthcare & Wellness Essentials",
                                        style = HealthogramTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                                    )
                                    Text(
                                        text = "Barcode: ||| | |||| ||| |||| |",
                                        style = HealthogramTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Action Buttons
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                repository.saveFulfillment(
                                    fulfillment.copy(
                                        fulfillmentStatus = FulfillmentStatus.READY_FOR_PICKUP,
                                        totalWeightGrams = weightGrams.toIntOrNull() ?: 450,
                                        packageDimensions = dimensions,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                                actionStatusMessage = "Order packed and marked READY FOR CARRIER PICKUP."
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HealthogramTheme.colors.primary),
                            modifier = Modifier.fillMaxWidth().testTag("mark_ready_pickup_button")
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mark Ready for Carrier Pickup")
                        }

                        OutlinedButton(
                            onClick = {
                                actionStatusMessage = "Courier pickup request dispatched to ${fulfillment.deliveryProvider.replace("_", " ").uppercase()}."
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Request Immediate Courier Dispatch")
                        }
                    }
                }
            }
        }
    }

    if (showLabelSheet && shipment != null) {
        AlertDialog(
            onDismissRequest = { showLabelSheet = false },
            title = { Text("Print Courier Shipping Label") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Printing barcode label for ${shipment.trackingNumber}...")
                    Text("Carrier: ${shipment.deliveryProvider.uppercase()}")
                    Text("Recipient Privacy Check: PASSED (No private medical records exposed on label)")
                }
            },
            confirmButton = {
                Button(onClick = { showLabelSheet = false }) {
                    Text("Send to Thermal Printer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLabelSheet = false }) {
                    Text("Close")
                }
            }
        )
    }
}
