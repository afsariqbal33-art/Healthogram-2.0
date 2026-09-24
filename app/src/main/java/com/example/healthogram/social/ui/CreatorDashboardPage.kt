package com.example.healthogram.social.ui

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.social.SocialFeedEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorDashboardPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = remember { SocialFeedEngine.getInstance() }
    val analytics by engine.analytics.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Professional Dashboard",
                            style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Creator Studio & Performance Analytics",
                            style = HealthogramTheme.typography.caption,
                            color = HealthogramTheme.colors.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("creator_dashboard_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Fund Eligible",
                                style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HealthogramTheme.colors.surface,
                    titleContentColor = HealthogramTheme.colors.textPrimary
                )
            )
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("creator_dashboard_page")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(HealthogramTheme.colors.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Overview Metric Banner
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = HealthogramTheme.colors.surface,
                    border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Last 30 Days Activity",
                            style = HealthogramTheme.typography.labelMedium,
                            color = HealthogramTheme.colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${analytics.totalImpressions}",
                                style = HealthogramTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                color = HealthogramTheme.colors.textPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "+${analytics.followerGrowthRate}%",
                                    style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text(
                            text = "Total Impressions across general & creator channels",
                            style = HealthogramTheme.typography.caption,
                            color = HealthogramTheme.colors.textMuted
                        )
                    }
                }
            }

            // 4-Card Analytics Grid
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnalyticsCard(
                        title = "Accounts Reached",
                        value = "${analytics.totalReach}",
                        change = "+8.4%",
                        icon = Icons.Default.Visibility,
                        modifier = Modifier.weight(1f)
                    )
                    AnalyticsCard(
                        title = "Engagement Rate",
                        value = "${analytics.engagementRatePercentage}%",
                        change = "+1.2%",
                        icon = Icons.Default.ThumbUp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnalyticsCard(
                        title = "Total Likes",
                        value = "${analytics.totalLikes}",
                        change = "+15.3%",
                        icon = Icons.Default.Favorite,
                        modifier = Modifier.weight(1f)
                    )
                    AnalyticsCard(
                        title = "Shares & Saves",
                        value = "${analytics.totalShares}",
                        change = "+22.0%",
                        icon = Icons.Default.Share,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Category Affinity Breakdown
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = HealthogramTheme.colors.surface,
                    border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Audience Category Affinity",
                            style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.textPrimary
                        )
                        Text(
                            text = "Content distribution across topics",
                            style = HealthogramTheme.typography.caption,
                            color = HealthogramTheme.colors.textMuted
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        analytics.categoryBreakdown.forEach { (category, percentage) ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = category.displayName,
                                        style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = HealthogramTheme.colors.textPrimary
                                    )
                                    Text(
                                        text = "$percentage%",
                                        style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = HealthogramTheme.colors.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { percentage / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = HealthogramTheme.colors.primary,
                                    trackColor = HealthogramTheme.colors.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Monthly Performance Trend
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = HealthogramTheme.colors.surface,
                    border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Growth Trajectory (Views)",
                            style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            analytics.monthlyViews.forEach { (month, views) ->
                                val barHeightFraction = (views.toFloat() / 150000f).coerceIn(0.1f, 1f)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Text(
                                        text = "${views / 1000}k",
                                        style = HealthogramTheme.typography.caption.copy(fontSize = 10.sp),
                                        color = HealthogramTheme.colors.textMuted
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(28.dp)
                                            .fillMaxHeight(barHeightFraction)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(HealthogramTheme.colors.primary)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = month,
                                        style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                                        color = HealthogramTheme.colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Creator Studio Quick Tools
            item {
                Text(
                    text = "Creator Tools",
                    style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.textPrimary
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CreatorToolTile(title = "Audio Library", icon = Icons.Default.MusicNote, modifier = Modifier.weight(1f))
                    CreatorToolTile(title = "Collaborations", icon = Icons.Default.Handshake, modifier = Modifier.weight(1f))
                    CreatorToolTile(title = "Fund Payouts", icon = Icons.Default.MonetizationOn, modifier = Modifier.weight(1f))
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun AnalyticsCard(
    title: String,
    value: String,
    change: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(20.dp))
                Text(
                    text = change,
                    style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF10B981)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = HealthogramTheme.typography.caption,
                color = HealthogramTheme.colors.textMuted
            )
        }
    }
}

@Composable
fun CreatorToolTile(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(76.dp),
        shape = RoundedCornerShape(12.dp),
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textPrimary
            )
        }
    }
}
