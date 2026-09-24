package com.example.healthogram.ui.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.healthogram.designsystem.HealthogramTheme

/**
 * Granular Category Settings Page.
 * Allows fine-grained customization of individual event triggers within categories.
 */
@Composable
fun NotificationCategorySettingsPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("Social", "Messaging", "Marketplace", "Seller", "Telehealth")
    var selectedCategory by remember { mutableStateOf("Social") }

    // Mock local toggles for granular sub-events
    var socialFollows by remember { mutableStateOf(true) }
    var socialLikes by remember { mutableStateOf(true) }
    var socialComments by remember { mutableStateOf(true) }
    var socialMentions by remember { mutableStateOf(true) }
    var socialLives by remember { mutableStateOf(true) }

    var msgDirect by remember { mutableStateOf(true) }
    var msgRequests by remember { mutableStateOf(true) }
    var msgVoiceNotes by remember { mutableStateOf(true) }
    var msgReactions by remember { mutableStateOf(false) }

    var mktOrders by remember { mutableStateOf(true) }
    var mktShipping by remember { mutableStateOf(true) }
    var mktPriceDrops by remember { mutableStateOf(true) }
    var mktFlashDeals by remember { mutableStateOf(false) }

    var sellerNewOrders by remember { mutableStateOf(true) }
    var sellerLowStock by remember { mutableStateOf(true) }
    var sellerPayouts by remember { mutableStateOf(true) }
    var sellerCompliance by remember { mutableStateOf(true) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("notification_category_settings_page"),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = HealthogramTheme.colors.surface,
                border = BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Category Sub-Event Controls",
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(HealthogramTheme.colors.background)
        ) {
            // Category Chips Row
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.surfaceVariant,
                        border = BorderStroke(1.dp, if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.borderLight),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = HealthogramTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                            color = if (isSelected) Color.White else HealthogramTheme.colors.textPrimary
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                    border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        when (selectedCategory) {
                            "Social" -> {
                                SubEventToggle("New Followers", "Notify when a verified user or creator follows you", socialFollows) { socialFollows = it }
                                Divider(color = HealthogramTheme.colors.borderLight, modifier = Modifier.padding(vertical = 10.dp))
                                SubEventToggle("Post Likes & Reactions", "Notify when users react to your published stories", socialLikes) { socialLikes = it }
                                Divider(color = HealthogramTheme.colors.borderLight, modifier = Modifier.padding(vertical = 10.dp))
                                SubEventToggle("Comments & Discussions", "Notify on replies to your clinical and fitness updates", socialComments) { socialComments = it }
                                Divider(color = HealthogramTheme.colors.borderLight, modifier = Modifier.padding(vertical = 10.dp))
                                SubEventToggle("Mentions & Tags", "Notify when tagged in community health questions", socialMentions) { socialMentions = it }
                                Divider(color = HealthogramTheme.colors.borderLight, modifier = Modifier.padding(vertical = 10.dp))
                                SubEventToggle("Live Telehealth & Creator Streams", "Alerts when creators you follow begin streaming", socialLives) { socialLives = it }
                            }
                            "Messaging" -> {
                                SubEventToggle("Direct Message Notifications", "Notifications for incoming one-on-one encrypted chats", msgDirect) { msgDirect = it }
                                Divider(color = HealthogramTheme.colors.borderLight, modifier = Modifier.padding(vertical = 10.dp))
                                SubEventToggle("Message Requests", "Notifications for incoming messages from new contacts", msgRequests) { msgRequests = it }
                                Divider(color = HealthogramTheme.colors.borderLight, modifier = Modifier.padding(vertical = 10.dp))
                                SubEventToggle("Voice Notes & Clinical Media", "Alerts when secure voice notes or photos are received", msgVoiceNotes) { msgVoiceNotes = it }
                                Divider(color = HealthogramTheme.colors.borderLight, modifier = Modifier.padding(vertical = 10.dp))
                                SubEventToggle("Message Emoji Reactions", "Notify when someone adds a reaction to your bubble", msgReactions) { msgReactions = it }
                            }
                            "Marketplace" -> {
                                SubEventToggle("Order Confirmation & Invoicing", "Instant receipts and purchase confirmations", mktOrders) { mktOrders = it }
                                Divider(color = HealthogramTheme.colors.borderLight, modifier = Modifier.padding(vertical = 10.dp))
                                SubEventToggle("Shipping & Live Courier Tracking", "Notifications when medical devices or vitamins dispatch", mktShipping) { mktShipping = it }
                                Divider(color = HealthogramTheme.colors.borderLight, modifier = Modifier.padding(vertical = 10.dp))
                                SubEventToggle("Wishlist Price Drops", "Alert when saved wellness equipment goes on discount", mktPriceDrops) { mktPriceDrops = it }
                                Divider(color = HealthogramTheme.colors.borderLight, modifier = Modifier.padding(vertical = 10.dp))
                                SubEventToggle("Flash Sales & Seasonal Campaigns", "Optional marketing alerts for seasonal wellness sales", mktFlashDeals) { mktFlashDeals = it }
                            }
                            "Seller" -> {
                                SubEventToggle("New Customer Orders", "High-priority alert when customer places an order", sellerNewOrders) { sellerNewOrders = it }
                                Divider(color = HealthogramTheme.colors.borderLight, modifier = Modifier.padding(vertical = 10.dp))
                                SubEventToggle("Low Inventory Threshold Warnings", "Alert when stock falls below reorder minimums", sellerLowStock) { sellerLowStock = it }
                                Divider(color = HealthogramTheme.colors.borderLight, modifier = Modifier.padding(vertical = 10.dp))
                                SubEventToggle("Bank Payout Transfers", "Confirmations when payouts clear to your bank account", sellerPayouts) { sellerPayouts = it }
                                Divider(color = HealthogramTheme.colors.borderLight, modifier = Modifier.padding(vertical = 10.dp))
                                SubEventToggle("Store Compliance & Policy Notices", "Important alerts regarding certifications and reviews", sellerCompliance) { sellerCompliance = it }
                            }
                            else -> {
                                Text("Telehealth Consultation Reminders: 24h before, 1h before, and 15m before appointment start. Managed automatically.", style = HealthogramTheme.typography.bodyMedium, color = HealthogramTheme.colors.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubEventToggle(title: String, desc: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(desc, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
