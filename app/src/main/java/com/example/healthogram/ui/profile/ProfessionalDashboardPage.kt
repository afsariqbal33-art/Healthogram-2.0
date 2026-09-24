package com.example.healthogram.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.core.User
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.profile.components.EmptyProfileState

/**
 * Professional Dashboard Foundation (Section 11).
 * Supports Creator Tools, Audience Insights, AI Studio Tools, and Content Analytics.
 *
 * Strict Rule: Do NOT use fake analytics numbers.
 * Displays "Analytics will appear once you start publishing content" when data is fresh.
 */
@Composable
fun ProfessionalDashboardPage(
    user: User?,
    onBack: () -> Unit,
    onOpenAIStudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf("Overview") }
    val sections = listOf("Overview", "Content", "Analytics", "Audience", "Creator Tools", "AI Studio", "Settings")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("professional_dashboard_page")
    ) {
        // App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = HealthogramTheme.colors.surface,
            border = BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Professional Dashboard",
                    style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Section Selector Tabs
        ScrollableTabRow(
            selectedTabIndex = sections.indexOf(selectedSection).coerceAtLeast(0),
            containerColor = HealthogramTheme.colors.surface,
            contentColor = HealthogramTheme.colors.primary,
            edgePadding = 16.dp
        ) {
            sections.forEach { title ->
                Tab(
                    selected = selectedSection == title,
                    onClick = { selectedSection = title },
                    text = {
                        Text(
                            text = title,
                            style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            when (selectedSection) {
                "Overview" -> {
                    item {
                        Surface(
                            shape = HealthogramTheme.shapes.medium,
                            color = HealthogramTheme.colors.primaryContainer,
                            border = BorderStroke(1.dp, HealthogramTheme.colors.primary.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = HealthogramTheme.colors.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Professional Creator Suite Active",
                                        style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = HealthogramTheme.colors.onPrimaryContainer
                                    )
                                    Text(
                                        "Access deep audience metrics, scheduled broadcasts, and AI Studio assists.",
                                        style = HealthogramTheme.typography.caption,
                                        color = HealthogramTheme.colors.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text("Performance & Insights", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    item {
                        // Section 11 mandate: Strictly no fake numbers. Real empty state.
                        EmptyProfileState(
                            icon = Icons.Outlined.Insights,
                            title = "No Content Published Yet",
                            message = "Analytics will appear once you start publishing content, reels, or video broadcasts.",
                            actionLabel = "Open AI Studio Tools",
                            onAction = onOpenAIStudio
                        )
                    }

                    item {
                        Text("Quick Creator Actions", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DashboardActionCard(
                                title = "AI Studio Tools",
                                subtitle = "Generate medical illustrations & scripts",
                                icon = Icons.Default.SmartToy,
                                onClick = onOpenAIStudio,
                                modifier = Modifier.weight(1f)
                            )
                            DashboardActionCard(
                                title = "Content Manager",
                                subtitle = "Schedule posts & health articles",
                                icon = Icons.Default.Article,
                                onClick = { selectedSection = "Content" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                "Analytics", "Audience" -> {
                    item {
                        EmptyProfileState(
                            icon = Icons.Outlined.BarChart,
                            title = "Audience Analytics Pending",
                            message = "Analytics will appear once you start publishing content. Healthogram computes real verified views and engagement metrics."
                        )
                    }
                }

                "Creator Tools" -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            ToolItem(title = "Health Knowledge Publishing", desc = "Peer-reviewed health article template tools", icon = Icons.Default.MenuBook)
                            ToolItem(title = "Patient Broadcast Channel", desc = "Direct updates to your active followers", icon = Icons.Default.Podcasts)
                            ToolItem(title = "Monetization Architecture Foundation", desc = "Prepare consultation rates and premium subscriber perks", icon = Icons.Default.Payments)
                        }
                    }
                }

                "AI Studio" -> {
                    item {
                        Surface(
                            shape = HealthogramTheme.shapes.medium,
                            color = HealthogramTheme.colors.surface,
                            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Gemini AI Studio Healthcare Assistant", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Leverage server-side Gemini 3.8 Flash to format medical summaries, structure patient educational brochures, and translate healthcare advice into 40+ languages.",
                                    style = HealthogramTheme.typography.bodySmall,
                                    color = HealthogramTheme.colors.textMuted
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                HealthogramPrimaryButton(
                                    text = "Launch AI Studio Module",
                                    onClick = onOpenAIStudio,
                                    icon = Icons.Default.AutoAwesome
                                )
                            }
                        }
                    }
                }

                else -> {
                    item {
                        EmptyProfileState(
                            icon = Icons.Default.Construction,
                            title = "$selectedSection Management",
                            message = "This module foundation is configured and will connect with the complete Social Media engine in Step 05."
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun DashboardActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = 110.dp),
        shape = HealthogramTheme.shapes.medium,
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
        }
    }
}

@Composable
private fun ToolItem(title: String, desc: String, icon: ImageVector) {
    Surface(
        shape = HealthogramTheme.shapes.medium,
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text(desc, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthogramTheme.colors.textMuted)
        }
    }
}
