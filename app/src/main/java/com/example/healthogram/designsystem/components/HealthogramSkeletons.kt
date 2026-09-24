package com.example.healthogram.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.healthogram.designsystem.HealthogramTheme

/**
 * HEALTHOGRAM STEP 21: REUSABLE SKELETON LOADERS
 * Eliminates layout shifts and full-screen blocking progress bars.
 */

@Composable
fun PostSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("post_skeleton"),
        shape = HealthogramTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Avatar + Username + Time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HealthogramShimmerBox(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    HealthogramShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.45f)
                            .height(14.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    HealthogramShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.25f)
                            .height(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body text lines
            HealthogramShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HealthogramShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(14.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Media aspect ratio placeholder
            HealthogramShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = HealthogramTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action bar (Like, Comment, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    HealthogramShimmerBox(modifier = Modifier.size(24.dp), shape = CircleShape)
                    Spacer(modifier = Modifier.width(16.dp))
                    HealthogramShimmerBox(modifier = Modifier.size(24.dp), shape = CircleShape)
                    Spacer(modifier = Modifier.width(16.dp))
                    HealthogramShimmerBox(modifier = Modifier.size(24.dp), shape = CircleShape)
                }
                HealthogramShimmerBox(modifier = Modifier.size(24.dp), shape = CircleShape)
            }
        }
    }
}

@Composable
fun ReelSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("reel_skeleton")
    ) {
        // Main video shimmer placeholder
        HealthogramShimmerBox(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(0.dp)
        )

        // Overlay actions on the right side
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            repeat(4) {
                HealthogramShimmerBox(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape
                )
            }
        }

        // Bottom creator info + caption
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.75f)
                .padding(start = 16.dp, bottom = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HealthogramShimmerBox(modifier = Modifier.size(36.dp), shape = CircleShape)
                Spacer(modifier = Modifier.width(10.dp))
                HealthogramShimmerBox(modifier = Modifier.width(100.dp).height(14.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            HealthogramShimmerBox(modifier = Modifier.fillMaxWidth().height(12.dp))
            Spacer(modifier = Modifier.height(6.dp))
            HealthogramShimmerBox(modifier = Modifier.fillMaxWidth(0.6f).height(12.dp))
        }
    }
}

@Composable
fun ProductSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_skeleton"),
        shape = HealthogramTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface)
    ) {
        Column {
            // 1:1 Product Image Placeholder
            HealthogramShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
            )

            Column(modifier = Modifier.padding(12.dp)) {
                // Category / Merchant tag
                HealthogramShimmerBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Title line
                HealthogramShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                HealthogramShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                // Price + CTA button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HealthogramShimmerBox(
                        modifier = Modifier
                            .width(70.dp)
                            .height(18.dp)
                    )
                    HealthogramShimmerBox(
                        modifier = Modifier
                            .size(32.dp),
                        shape = CircleShape
                    )
                }
            }
        }
    }
}

@Composable
fun OrderSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("order_skeleton"),
        shape = HealthogramTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Order ID + Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HealthogramShimmerBox(modifier = Modifier.width(110.dp).height(14.dp))
                HealthogramShimmerBox(modifier = Modifier.width(80.dp).height(24.dp), shape = RoundedCornerShape(12.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            // Product item summary row
            Row(verticalAlignment = Alignment.CenterVertically) {
                HealthogramShimmerBox(modifier = Modifier.size(56.dp), shape = HealthogramTheme.shapes.small)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    HealthogramShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(14.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    HealthogramShimmerBox(modifier = Modifier.width(90.dp).height(12.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = HealthogramTheme.colors.border.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            // Total price row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HealthogramShimmerBox(modifier = Modifier.width(60.dp).height(12.dp))
                HealthogramShimmerBox(modifier = Modifier.width(85.dp).height(16.dp))
            }
        }
    }
}

@Composable
fun MessageSkeleton(
    modifier: Modifier = Modifier,
    isOutgoing: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("message_skeleton"),
        contentAlignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
        ) {
            if (!isOutgoing) {
                HealthogramShimmerBox(modifier = Modifier.size(28.dp), shape = CircleShape)
                Spacer(modifier = Modifier.width(8.dp))
            }
            HealthogramShimmerBox(
                modifier = Modifier
                    .width(if (isOutgoing) 180.dp else 220.dp)
                    .height(44.dp),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun ProfileSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("profile_skeleton")
    ) {
        // Avatar + Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HealthogramShimmerBox(modifier = Modifier.size(80.dp), shape = CircleShape)
            repeat(3) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HealthogramShimmerBox(modifier = Modifier.width(36.dp).height(16.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    HealthogramShimmerBox(modifier = Modifier.width(48.dp).height(10.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Name & Bio
        HealthogramShimmerBox(modifier = Modifier.width(140.dp).height(16.dp))
        Spacer(modifier = Modifier.height(6.dp))
        HealthogramShimmerBox(modifier = Modifier.fillMaxWidth(0.9f).height(12.dp))
        Spacer(modifier = Modifier.height(4.dp))
        HealthogramShimmerBox(modifier = Modifier.fillMaxWidth(0.6f).height(12.dp))
        Spacer(modifier = Modifier.height(16.dp))
        // Action Buttons
        Row(modifier = Modifier.fillMaxWidth()) {
            HealthogramShimmerBox(modifier = Modifier.weight(1f).height(38.dp), shape = RoundedCornerShape(8.dp))
            Spacer(modifier = Modifier.width(10.dp))
            HealthogramShimmerBox(modifier = Modifier.weight(1f).height(38.dp), shape = RoundedCornerShape(8.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        // 3-Column Grid Placeholders
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) {
                HealthogramShimmerBox(modifier = Modifier.weight(1f).aspectRatio(1f), shape = RoundedCornerShape(4.dp))
            }
        }
    }
}

@Composable
fun HealthRecordSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("health_record_skeleton"),
        shape = HealthogramTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Pill (e.g., Lab Report / Diagnosis)
                HealthogramShimmerBox(modifier = Modifier.width(90.dp).height(20.dp), shape = RoundedCornerShape(10.dp))
                // Encrypted badge pill
                HealthogramShimmerBox(modifier = Modifier.width(70.dp).height(16.dp), shape = RoundedCornerShape(8.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Clinical Record Title
            HealthogramShimmerBox(modifier = Modifier.fillMaxWidth(0.75f).height(16.dp))
            Spacer(modifier = Modifier.height(6.dp))
            // Hospital / Doctor name
            HealthogramShimmerBox(modifier = Modifier.fillMaxWidth(0.45f).height(12.dp))
            Spacer(modifier = Modifier.height(12.dp))
            // Summary text box
            HealthogramShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                shape = RoundedCornerShape(6.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            // Document attachment indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                HealthogramShimmerBox(modifier = Modifier.size(20.dp), shape = CircleShape)
                Spacer(modifier = Modifier.width(8.dp))
                HealthogramShimmerBox(modifier = Modifier.width(120.dp).height(12.dp))
            }
        }
    }
}

@Composable
fun DashboardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("dashboard_skeleton")
    ) {
        // KPI Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HealthogramShimmerBox(modifier = Modifier.weight(1f).height(90.dp), shape = HealthogramTheme.shapes.medium)
            HealthogramShimmerBox(modifier = Modifier.weight(1f).height(90.dp), shape = HealthogramTheme.shapes.medium)
        }
        Spacer(modifier = Modifier.height(12.dp))
        // KPI Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HealthogramShimmerBox(modifier = Modifier.weight(1f).height(90.dp), shape = HealthogramTheme.shapes.medium)
            HealthogramShimmerBox(modifier = Modifier.weight(1f).height(90.dp), shape = HealthogramTheme.shapes.medium)
        }
        Spacer(modifier = Modifier.height(20.dp))
        // Chart container skeleton
        HealthogramShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = HealthogramTheme.shapes.large
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Table list rows
        repeat(3) {
            HealthogramShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

@Composable
fun NotificationSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("notification_skeleton"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HealthogramShimmerBox(modifier = Modifier.size(44.dp), shape = CircleShape)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            HealthogramShimmerBox(modifier = Modifier.fillMaxWidth(0.8f).height(14.dp))
            Spacer(modifier = Modifier.height(6.dp))
            HealthogramShimmerBox(modifier = Modifier.fillMaxWidth(0.35f).height(10.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        HealthogramShimmerBox(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(6.dp))
    }
}
