package com.example.healthogram.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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

enum class HealthogramNavDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    EXPLORE("Explore", Icons.Filled.Explore, Icons.Outlined.Explore, "nav_explore"),
    CREATE("Create", Icons.Filled.Add, Icons.Filled.Add, "nav_create"),
    HEALTH_PASSPORT("Passport", Icons.Filled.HealthAndSafety, Icons.Outlined.HealthAndSafety, "nav_passport"),
    MARKETPLACE("Market", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag, "nav_market"),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
}

/**
 * Global App Header
 */
@Composable
fun HealthogramAppHeader(
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onMessagesClick: () -> Unit,
    modifier: Modifier = Modifier,
    unreadNotificationCount: Int = 3,
    unreadMessageCount: Int = 2
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("healthogram_app_header"),
        color = HealthogramTheme.colors.surface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight),
        shadowElevation = HealthogramTheme.elevation.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Logo & Wordmark
            HealthogramLogo(
                size = HealthogramLogoSize.MEDIUM,
                showWordmark = true
            )

            // Right: Clean quick action icons (Search, Notifications with Badge, Messages with Badge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.testTag("header_search_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = HealthogramTheme.colors.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.testTag("header_notifications_btn")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotificationCount > 0) {
                                Badge(containerColor = HealthogramTheme.colors.error) {
                                    Text(unreadNotificationCount.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = HealthogramTheme.colors.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onMessagesClick,
                    modifier = Modifier.testTag("header_messages_btn")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadMessageCount > 0) {
                                Badge(containerColor = HealthogramTheme.colors.primary) {
                                    Text(unreadMessageCount.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Messages",
                            tint = HealthogramTheme.colors.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modern Bottom Navigation Bar with Visually Prominent '+' Button
 */
@Composable
fun HealthogramBottomNavigation(
    currentDestination: HealthogramNavDestination,
    onNavigate: (HealthogramNavDestination) -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("healthogram_bottom_nav"),
        color = HealthogramTheme.colors.surface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight),
        shadowElevation = HealthogramTheme.elevation.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HealthogramNavDestination.entries.forEach { destination ->
                if (destination == HealthogramNavDestination.CREATE) {
                    // Elevated, Visually Prominent '+' Button
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(brush = HealthogramTheme.colors.brandGradient)
                            .clickable { onCreateClick() }
                            .testTag(destination.testTag),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    val isSelected = currentDestination == destination
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onNavigate(destination) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag(destination.testTag)
                    ) {
                        Icon(
                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = destination.title,
                            tint = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.textMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = destination.title,
                            style = HealthogramTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp
                            ),
                            color = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.textMuted
                        )
                    }
                }
            }
        }
    }
}

/**
 * Universal Create '+' Modal Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthogramCreateBottomSheet(
    onDismiss: () -> Unit,
    onCreatePost: () -> Unit,
    onCreateReel: () -> Unit,
    onCreateStory: () -> Unit,
    onGoLive: () -> Unit,
    onCreateProduct: () -> Unit,
    onOpenAIStudio: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = HealthogramTheme.colors.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = HealthogramTheme.colors.border) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("create_bottom_sheet")
        ) {
            Text(
                text = "Create with Healthogram",
                style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textPrimary
            )
            Text(
                text = "Publish media, broadcast live, launch products, or enhance with AI",
                style = HealthogramTheme.typography.bodySmall,
                color = HealthogramTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Social & Media Creation Row
            Text(
                text = "Social & Media",
                style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CreateOptionCard(title = "Post", icon = Icons.Default.GridOn, onClick = { onDismiss(); onCreatePost() }, modifier = Modifier.weight(1f))
                CreateOptionCard(title = "Reel", icon = Icons.Default.Movie, onClick = { onDismiss(); onCreateReel() }, modifier = Modifier.weight(1f))
                CreateOptionCard(title = "Story", icon = Icons.Default.CameraAlt, onClick = { onDismiss(); onCreateStory() }, modifier = Modifier.weight(1f))
                CreateOptionCard(title = "Live", icon = Icons.Default.LiveTv, onClick = { onDismiss(); onGoLive() }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Marketplace & AI Studio Row
            Text(
                text = "Commerce & AI Studio",
                style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CreateOptionCard(title = "List Product", icon = Icons.Default.AddBusiness, onClick = { onDismiss(); onCreateProduct() }, modifier = Modifier.weight(1f))
                CreateOptionCard(title = "AI Studio", icon = Icons.Default.AutoAwesome, onClick = { onDismiss(); onOpenAIStudio() }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun CreateOptionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(76.dp)
            .clickable { onClick() },
        shape = HealthogramTheme.shapes.medium,
        color = HealthogramTheme.colors.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textPrimary
            )
        }
    }
}
