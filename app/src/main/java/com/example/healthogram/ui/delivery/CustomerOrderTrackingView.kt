package com.example.healthogram.ui.delivery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import com.example.healthogram.delivery.*
import com.example.healthogram.designsystem.HealthogramTheme
import java.text.SimpleDateFormat
import java.util.*

/**
 * Customer Order Tracking and Real-Time Delivery Timeline View.
 * Displays carrier status, contactless OTP code, temperature safety indicators,
 * and delivery timeline events.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrderTrackingView(
    shipmentId: String = "sh_991823",
    onBack: () -> Unit = {},
    onNavigateToSupport: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { DeliveryRepository.getInstance() }
    val engine = remember { DeliveryEngine(repository) }

    val shipments by repository.shipments.collectAsState()
    val trackingEventsMap by repository.trackingEvents.collectAsState()
    val otps by repository.deliveryOtps.collectAsState()

    val currentShipment = shipments[shipmentId] ?: shipments.values.firstOrNull()
    val events = currentShipment?.let { trackingEventsMap[it.shipmentId] } ?: emptyList()
    val currentOtp = currentShipment?.let { otps[it.shipmentId] }

    var showRescheduleDialog by remember { mutableStateOf(false) }
    var rescheduleDate by remember { mutableStateOf("Tomorrow 2:00 PM - 5:00 PM") }
    var rescheduleReason by remember { mutableStateOf("Not available at home") }
    var actionMessage by remember { mutableStateOf<String?>(null) }

    var otpInput by remember { mutableStateOf("") }
    var showOtpDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Track Order",
                            style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.textPrimary
                        )
                        Text(
                            text = currentShipment?.trackingNumber ?: "Shipment Tracking",
                            style = HealthogramTheme.typography.bodySmall,
                            color = HealthogramTheme.colors.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("track_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = HealthogramTheme.colors.textPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { currentShipment?.let { onNavigateToSupport(it.orderId) } },
                        modifier = Modifier.testTag("support_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HeadsetMic,
                            contentDescription = "Delivery Support",
                            tint = HealthogramTheme.colors.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HealthogramTheme.colors.surface
                )
            )
        },
        containerColor = HealthogramTheme.colors.background,
        modifier = modifier.testTag("customer_order_tracking_view")
    ) { padding ->
        if (currentShipment == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active shipment found",
                    style = HealthogramTheme.typography.bodyLarge,
                    color = HealthogramTheme.colors.textSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Action notification banner
                if (actionMessage != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.primaryContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = HealthogramTheme.colors.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = actionMessage!!,
                                    style = HealthogramTheme.typography.bodySmall,
                                    color = HealthogramTheme.colors.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // 1. Current Status Header Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = when (currentShipment.status) {
                                        ShipmentStatus.DELIVERED -> Color(0xFF10B981)
                                        ShipmentStatus.OUT_FOR_DELIVERY -> Color(0xFF0284C7)
                                        ShipmentStatus.FAILED -> Color(0xFFEF4444)
                                        else -> HealthogramTheme.colors.primary
                                    }.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        text = currentShipment.status.label,
                                        style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = when (currentShipment.status) {
                                            ShipmentStatus.DELIVERED -> Color(0xFF047857)
                                            ShipmentStatus.OUT_FOR_DELIVERY -> Color(0xFF0369A1)
                                            ShipmentStatus.FAILED -> Color(0xFFB91C1C)
                                            else -> HealthogramTheme.colors.primary
                                        },
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }

                                Text(
                                    text = "Carrier: ${currentShipment.deliveryProvider.replace("_", " ").uppercase()}",
                                    style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = HealthogramTheme.colors.textSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (currentShipment.status == ShipmentStatus.DELIVERED) "Package Delivered" else "Estimated Delivery",
                                style = HealthogramTheme.typography.bodySmall,
                                color = HealthogramTheme.colors.textSecondary
                            )
                            Text(
                                text = SimpleDateFormat("EEEE, MMM dd • hh:mm a", Locale.getDefault())
                                    .format(Date(currentShipment.estimatedDeliveryAt)),
                                style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = HealthogramTheme.colors.textPrimary
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = HealthogramTheme.colors.divider)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Tracking Number",
                                        style = HealthogramTheme.typography.labelSmall,
                                        color = HealthogramTheme.colors.textSecondary
                                    )
                                    Text(
                                        text = currentShipment.trackingNumber,
                                        style = HealthogramTheme.typography.bodyMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = HealthogramTheme.colors.textPrimary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Delivery Mode",
                                        style = HealthogramTheme.typography.labelSmall,
                                        color = HealthogramTheme.colors.textSecondary
                                    )
                                    Text(
                                        text = currentShipment.deliveryMode.label,
                                        style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = HealthogramTheme.colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Contactless Delivery OTP Card (Doorstep Handoff Protection)
                if (currentShipment.status != ShipmentStatus.DELIVERED && currentOtp != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.primary.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, HealthogramTheme.colors.primary.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = HealthogramTheme.colors.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Delivery OTP Code",
                                            style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = HealthogramTheme.colors.textPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Share this 6-digit code with the courier upon package arrival to confirm proof of delivery.",
                                        style = HealthogramTheme.typography.bodySmall,
                                        color = HealthogramTheme.colors.textSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Surface(
                                    color = HealthogramTheme.colors.primary,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = currentOtp.hashedOtp,
                                        style = HealthogramTheme.typography.titleLarge.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 2.sp
                                        ),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Package Safety & Privacy Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = HealthogramTheme.colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Healthcare Privacy Protected",
                                    style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = HealthogramTheme.colors.textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Package description is sanitized for privacy: 'Healthcare and Wellness Essentials'. Medical records and diagnosis notes are never printed on carrier labels.",
                                style = HealthogramTheme.typography.bodySmall,
                                color = HealthogramTheme.colors.textSecondary
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Cold-Chain Monitored", style = HealthogramTheme.typography.labelSmall) },
                                    icon = { Icon(Icons.Default.AcUnit, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                )
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Tamper-Proof Seal", style = HealthogramTheme.typography.labelSmall) },
                                    icon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                )
                            }
                        }
                    }
                }

                // 4. Tracking Timeline Events
                item {
                    Text(
                        text = "Shipment Timeline",
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.textPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(events.reversed()) { event ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(28.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(HealthogramTheme.colors.primary, CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(54.dp)
                                    .background(HealthogramTheme.colors.divider)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.padding(bottom = 16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = event.description,
                                    style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = HealthogramTheme.colors.textPrimary
                                )
                                Text(
                                    text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(event.eventTime)),
                                    style = HealthogramTheme.typography.labelSmall,
                                    color = HealthogramTheme.colors.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${event.locationText} • ${event.provider.uppercase()}",
                                style = HealthogramTheme.typography.bodySmall,
                                color = HealthogramTheme.colors.textSecondary
                            )
                        }
                    }
                }

                // 5. Actions: Reschedule & Driver OTP Simulation
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showRescheduleDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reschedule_delivery_button")
                        ) {
                            Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reschedule")
                        }

                        Button(
                            onClick = { showOtpDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HealthogramTheme.colors.primary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("verify_otp_button")
                        ) {
                            Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (currentShipment.status == ShipmentStatus.DELIVERED) "Delivered" else "Simulate POD")
                        }
                    }
                }
            }
        }
    }

    // Reschedule Dialog
    if (showRescheduleDialog && currentShipment != null) {
        AlertDialog(
            onDismissRequest = { showRescheduleDialog = false },
            title = { Text("Reschedule Delivery Slot") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select a new delivery window for shipment ${currentShipment.trackingNumber}:")
                    OutlinedTextField(
                        value = rescheduleDate,
                        onValueChange = { rescheduleDate = it },
                        label = { Text("New Date & Time Window") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = rescheduleReason,
                        onValueChange = { rescheduleReason = it },
                        label = { Text("Reason for Rescheduling") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        engine.rescheduleDelivery(
                            shipmentId = currentShipment.shipmentId,
                            newDate = rescheduleDate,
                            reason = rescheduleReason
                        )
                        actionMessage = "Delivery successfully rescheduled to $rescheduleDate"
                        showRescheduleDialog = false
                    }
                ) {
                    Text("Confirm Reschedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRescheduleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // OTP Verification / POD Simulation Dialog
    if (showOtpDialog && currentShipment != null) {
        AlertDialog(
            onDismissRequest = { showOtpDialog = false },
            title = { Text("Confirm Proof of Delivery") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Enter recipient OTP code provided to courier (or press Auto-Fill):")
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { otpInput = it },
                        label = { Text("6-Digit Delivery OTP") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        onClick = { currentOtp?.let { otpInput = it.hashedOtp } }
                    ) {
                        Text("Auto-fill with customer's active OTP: ${currentOtp?.hashedOtp ?: ""}")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = engine.verifyDeliveryOtp(
                            shipmentId = currentShipment.shipmentId,
                            enteredOtp = otpInput,
                            recipientName = "Dr. Sarah Al-Ahmad"
                        )
                        actionMessage = if (success) "Proof of Delivery confirmed! Order marked as DELIVERED." else "Invalid OTP code. Please try again."
                        showOtpDialog = false
                    }
                ) {
                    Text("Verify & Deliver")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOtpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
