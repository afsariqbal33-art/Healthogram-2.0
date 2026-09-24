package com.example.healthogram.ui.delivery

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
 * Customer Delivery Support, Dispute Resolution & Returns Management View.
 * Connects directly with Step 14 refund workflows when returns are approved.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDeliverySupportView(
    orderId: String = "ord_hgm_88219",
    customerUid: String = "usr_patient_01",
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { DeliveryRepository.getInstance() }
    val engine = remember { DeliveryEngine(repository) }

    val disputesMap by repository.disputes.collectAsState()
    val returnsMap by repository.customerReturns.collectAsState()

    val myDisputes = remember(disputesMap, customerUid) {
        disputesMap.values.filter { it.customerUid == customerUid }
    }
    val myReturns = remember(returnsMap, customerUid) {
        returnsMap.values.filter { it.customerUid == customerUid }
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Disputes, 1: Returns, 2: File New
    var disputeType by remember { mutableStateOf("not_received") }
    var disputeDescription by remember { mutableStateOf("") }

    var returnReason by remember { mutableStateOf("Item arrived damaged in transit") }
    var returnCondition by remember { mutableStateOf("UNOPENED") }
    var statusFeedback by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Delivery Support & Returns",
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("support_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HealthogramTheme.colors.surface)
            )
        },
        containerColor = HealthogramTheme.colors.background,
        modifier = modifier.testTag("customer_delivery_support_view")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = HealthogramTheme.colors.surface,
                contentColor = HealthogramTheme.colors.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Disputes (${myDisputes.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Returns (${myReturns.size})") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Report Issue") }
                )
            }

            if (statusFeedback != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HealthogramTheme.colors.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statusFeedback!!,
                            style = HealthogramTheme.typography.bodySmall,
                            color = HealthogramTheme.colors.onPrimaryContainer
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    // Disputes list
                    if (myDisputes.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No open delivery disputes", style = HealthogramTheme.typography.bodyMedium, color = HealthogramTheme.colors.textSecondary)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(myDisputes) { dispute ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Dispute: ${dispute.disputeType.replace("_", " ").uppercase()}",
                                                style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = HealthogramTheme.colors.textPrimary
                                            )
                                            Surface(
                                                color = if (dispute.status == "RESOLVED") Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text(
                                                    text = dispute.status,
                                                    style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (dispute.status == "RESOLVED") Color(0xFF047857) else Color(0xFFB45309),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = dispute.description, style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textSecondary)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Order: ${dispute.orderId} • Carrier: ${dispute.provider.uppercase()}",
                                            style = HealthogramTheme.typography.labelSmall,
                                            color = HealthogramTheme.colors.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Returns list
                    if (myReturns.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No active return requests", style = HealthogramTheme.typography.bodyMedium, color = HealthogramTheme.colors.textSecondary)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(myReturns) { ret ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = ret.status.label,
                                                style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = HealthogramTheme.colors.textPrimary
                                            )
                                            Text(
                                                text = "Condition: ${ret.condition}",
                                                style = HealthogramTheme.typography.labelSmall,
                                                color = HealthogramTheme.colors.textSecondary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Reason: ${ret.reason}", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textSecondary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Method: ${ret.returnMethod} • Refund: ${ret.refundStatus}",
                                            style = HealthogramTheme.typography.labelSmall,
                                            color = HealthogramTheme.colors.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // File New Issue Form
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "File Delivery Dispute or Return",
                                style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = HealthogramTheme.colors.textPrimary
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = disputeDescription,
                                onValueChange = { disputeDescription = it },
                                label = { Text("Describe the delivery issue") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("dispute_description_input")
                            )
                        }

                        item {
                            Text(
                                text = "Or Request Item Return",
                                style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = HealthogramTheme.colors.textPrimary
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = returnReason,
                                onValueChange = { returnReason = it },
                                label = { Text("Return Reason") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("return_reason_input")
                            )
                        }

                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = returnCondition == "UNOPENED",
                                    onClick = { returnCondition = "UNOPENED" },
                                    label = { Text("Unopened & Sealed") }
                                )
                                FilterChip(
                                    selected = returnCondition == "DAMAGED",
                                    onClick = { returnCondition = "DAMAGED" },
                                    label = { Text("Damaged on Arrival") }
                                )
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    if (disputeDescription.isNotBlank()) {
                                        engine.fileDeliveryDispute(
                                            orderId = orderId,
                                            shipmentId = "sh_991823",
                                            customerUid = customerUid,
                                            sellerUid = "seller_alnoor_pharmacy",
                                            provider = "internal_fleet",
                                            disputeType = disputeType,
                                            description = disputeDescription,
                                            evidence = "https://healthogram.local/evidence/dispute.jpg"
                                        )
                                        statusFeedback = "Delivery dispute filed. Healthogram support agent assigned."
                                        disputeDescription = ""
                                        selectedTab = 0
                                    } else {
                                        engine.requestCustomerReturn(
                                            orderId = orderId,
                                            suborderId = "subord_001",
                                            customerUid = customerUid,
                                            sellerUid = "seller_alnoor_pharmacy",
                                            productIds = listOf("prod_001"),
                                            reason = returnReason,
                                            condition = returnCondition,
                                            evidenceReference = "https://healthogram.local/evidence/return.jpg"
                                        )
                                        statusFeedback = "Return request submitted! Seller has 24 hours to review."
                                        selectedTab = 1
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_support_ticket_button")
                            ) {
                                Text("Submit Request")
                            }
                        }
                    }
                }
            }
        }
    }
}
