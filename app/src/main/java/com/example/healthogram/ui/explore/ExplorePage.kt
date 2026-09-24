package com.example.healthogram.ui.explore

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.core.AccountType
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.social.ContentCategory
import com.example.healthogram.social.SocialFeedEngine
import com.example.healthogram.social.SocialPost

@Composable
fun ExplorePage(
    modifier: Modifier = Modifier,
    onDoctorBookClick: (String) -> Unit = {},
    onPostClick: (SocialPost) -> Unit = {}
) {
    val engine = remember { SocialFeedEngine.getInstance() }
    val allPosts by engine.posts.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ContentCategory.ALL) }

    val trendingTopics = remember {
        listOf(
            "#Zone2Cardio" to "14.2K posts",
            "#SpatialComputing" to "38.5K posts",
            "#CleanEating" to "52.1K posts",
            "#RoboticCardiology" to "6.1K posts",
            "#TravelReels" to "89.4K posts",
            "#DeskWorkout" to "19.8K posts"
        )
    }

    val filteredPosts = remember(allPosts, searchQuery, selectedCategory) {
        allPosts.filter { post ->
            val matchesCategory = selectedCategory == ContentCategory.ALL || post.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                    post.caption.contains(searchQuery, ignoreCase = true) ||
                    post.authorName.contains(searchQuery, ignoreCase = true) ||
                    post.authorUsername.contains(searchQuery, ignoreCase = true) ||
                    post.hashtags.any { it.contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesQuery
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("explore_page"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Field
        item {
            Spacer(modifier = Modifier.height(12.dp))
            HealthogramSearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Explore creators, tech, lifestyle, doctors, tags..."
            )
        }

        // Horizontal Category Filter Pills (General + Healthcare)
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ContentCategory.entries) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = HealthogramTheme.shapes.pill,
                        color = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.surfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.borderLight
                        ),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat.displayName,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = HealthogramTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) Color.White else HealthogramTheme.colors.textPrimary
                        )
                    }
                }
            }
        }

        // Trending Topics
        if (searchQuery.isBlank()) {
            item {
                Text(
                    text = "Trending on Healthogram",
                    style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.textPrimary
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(trendingTopics) { (tag, count) ->
                        Surface(
                            shape = HealthogramTheme.shapes.medium,
                            color = HealthogramTheme.colors.surface,
                            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
                            modifier = Modifier.clickable { searchQuery = tag }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                Text(
                                    text = tag,
                                    style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = HealthogramTheme.colors.primary
                                )
                                Text(
                                    text = count,
                                    style = HealthogramTheme.typography.caption,
                                    color = HealthogramTheme.colors.textMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Explore Content Grid Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (searchQuery.isNotBlank()) "Search Results (${filteredPosts.size})" else "Discover",
                    style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.textPrimary
                )
                if (selectedCategory != ContentCategory.ALL) {
                    Text(
                        text = "Filtered by ${selectedCategory.displayName}",
                        style = HealthogramTheme.typography.caption,
                        color = HealthogramTheme.colors.primary
                    )
                }
            }
        }

        // 2-Column Explore Post Grid
        item {
            if (filteredPosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No content found for '$searchQuery'",
                        style = HealthogramTheme.typography.bodyMedium,
                        color = HealthogramTheme.colors.textMuted
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val chunked = filteredPosts.chunked(2)
                    chunked.forEach { rowPosts ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPosts.forEach { post ->
                                ExplorePostGridTile(
                                    post = post,
                                    onClick = { onPostClick(post) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowPosts.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Featured Verified Medical Specialists
        item {
            Text(
                text = "Verified Medical Specialists",
                style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textPrimary
            )
        }

        item {
            HealthogramDoctorCard(
                doctorName = "Dr. Sarah Jenkins, MD",
                specialization = "Interventional Cardiology",
                hospitalOrClinic = "City General Hospital",
                rating = 4.95f,
                fee = 75.0,
                onBookClick = { onDoctorBookClick("doc_1") }
            )
        }

        // Featured Healthcare Facilities
        item {
            Text(
                text = "Featured Healthcare Facilities",
                style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textPrimary
            )
        }

        item {
            HealthogramFacilityCard(
                facilityName = "City General Hospital",
                accountType = AccountType.HOSPITAL,
                address = "Downtown Medical Plaza, Suite 400",
                highlightMetric = "24/7 Robotic Surgery & Emergency Unit"
            )
        }

        item {
            HealthogramFacilityCard(
                facilityName = "Nova Clinical Diagnostics",
                accountType = AccountType.LABORATORY,
                address = "East Wing Pathology Center",
                highlightMetric = "Same-Day Bloodwork & Direct Passport Delivery"
            )
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun ExplorePostGridTile(
    post: SocialPost,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(150.dp)
            .clickable { onClick() }
            .testTag("explore_tile_${post.postId}"),
        shape = RoundedCornerShape(12.dp),
        color = HealthogramTheme.colors.surfaceVariant,
        border = BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background category icon indicator
            Icon(
                imageVector = when (post.category) {
                    ContentCategory.HEALTHCARE -> Icons.Default.MedicalServices
                    ContentCategory.TECHNOLOGY -> Icons.Default.Computer
                    ContentCategory.SPORTS -> Icons.Default.FitnessCenter
                    ContentCategory.MUSIC -> Icons.Default.MusicNote
                    ContentCategory.TRAVEL -> Icons.Default.Flight
                    else -> Icons.Default.Spa
                },
                contentDescription = null,
                tint = HealthogramTheme.colors.primary.copy(alpha = 0.25f),
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.Center)
            )

            // Top category pill
            Surface(
                shape = RoundedCornerShape(bottomEnd = 8.dp),
                color = HealthogramTheme.colors.primary,
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = post.category.displayName,
                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Bottom Caption & Likes Overlay
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.65f)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = post.caption,
                        style = HealthogramTheme.typography.caption.copy(fontSize = 11.sp),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "@${post.authorUsername}",
                            style = HealthogramTheme.typography.caption.copy(fontSize = 10.sp),
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${post.likesCount}",
                                style = HealthogramTheme.typography.caption.copy(fontSize = 10.sp),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
