package com.example.healthogram.ui.marketplace.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.marketplace.*

/**
 * HEALTHOGRAM — STEP 08: 24 REUSABLE MARKETPLACE COMPONENTS (Section 60)
 */

// 1. MarketplaceHeader
@Composable
fun MarketplaceHeader(
    cartCount: Int,
    wishlistCount: Int,
    notificationCount: Int,
    onWishlistClick: () -> Unit,
    onCartClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("marketplace_header"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Healthogram Marketplace",
                style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textPrimary
            )
            Text(
                text = "Certified Healthcare & Medical Store",
                style = HealthogramTheme.typography.caption,
                color = HealthogramTheme.colors.textMuted
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(
                onClick = onWishlistClick,
                modifier = Modifier.testTag("header_wishlist_btn")
            ) {
                BadgedBox(badge = {
                    if (wishlistCount > 0) {
                        Badge { Text("$wishlistCount") }
                    }
                }) {
                    Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Wishlist", tint = HealthogramTheme.colors.textPrimary)
                }
            }

            IconButton(
                onClick = onCartClick,
                modifier = Modifier.testTag("header_cart_btn")
            ) {
                BadgedBox(badge = {
                    if (cartCount > 0) {
                        Badge { Text("$cartCount") }
                    }
                }) {
                    Icon(Icons.Outlined.ShoppingCart, contentDescription = "Cart", tint = HealthogramTheme.colors.textPrimary)
                }
            }

            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.testTag("header_notifications_btn")
            ) {
                BadgedBox(badge = {
                    if (notificationCount > 0) {
                        Badge { Text("$notificationCount") }
                    }
                }) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = HealthogramTheme.colors.textPrimary)
                }
            }
        }
    }
}

// 2. MarketplaceSearchBar
@Composable
fun MarketplaceSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    placeholder: String = "Search healthcare products...",
    modifier: Modifier = Modifier
) {
    Surface(
        shape = HealthogramTheme.shapes.pill,
        color = HealthogramTheme.colors.surfaceVariant,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSearchClick() }
            .testTag("marketplace_search_bar")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = HealthogramTheme.colors.textMuted)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (query.isNotBlank()) query else placeholder,
                style = HealthogramTheme.typography.bodyMedium,
                color = if (query.isNotBlank()) HealthogramTheme.colors.textPrimary else HealthogramTheme.colors.textMuted,
                modifier = Modifier.weight(1f)
            )
            if (query.isNotBlank()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = HealthogramTheme.colors.textMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// 3. MarketplaceCategoryCard
@Composable
fun MarketplaceCategoryCard(
    category: MarketplaceCategory,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = HealthogramTheme.shapes.medium,
        color = if (isSelected) HealthogramTheme.colors.primary.copy(alpha = 0.1f) else HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.borderLight),
        modifier = modifier
            .clickable { onClick() }
            .testTag("category_card_${category.categoryId}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(HealthogramTheme.colors.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = MarketplaceCustomFunctions.getMarketplaceCategoryIcon(category.categoryId),
                    contentDescription = category.name,
                    tint = HealthogramTheme.colors.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = HealthogramTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// 4. MarketplaceProductCard
@Composable
fun MarketplaceProductCard(
    product: MarketplaceProduct,
    isWishlisted: Boolean,
    onWishlistToggle: () -> Unit,
    onAddToCart: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("product_card_${product.productId}"),
        shape = HealthogramTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(HealthogramTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = MarketplaceCustomFunctions.getMarketplaceCategoryIcon(product.categoryId),
                    contentDescription = null,
                    tint = HealthogramTheme.colors.primary,
                    modifier = Modifier.size(48.dp)
                )

                // Top badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (product.isFlashSale) {
                        MarketplaceDiscountBadge(text = "FLASH SALE", isFlashSale = true)
                    } else if (product.discountPrice != null) {
                        MarketplaceDiscountBadge(
                            text = MarketplaceCustomFunctions.calculateDisplayedDiscount(product.price, product.discountPrice)
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    MarketplaceWishlistButton(
                        isWishlisted = isWishlisted,
                        onToggle = onWishlistToggle
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.sellerStoreName,
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.title,
                    style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = HealthogramTheme.colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))

                MarketplaceRating(rating = product.rating, reviewCount = product.reviewCount)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MarketplacePrice(
                        price = product.discountPrice ?: product.price,
                        originalPrice = if (product.discountPrice != null) product.price else null,
                        currency = product.currency
                    )

                    Surface(
                        shape = CircleShape,
                        color = HealthogramTheme.colors.primary,
                        modifier = Modifier
                            .size(34.dp)
                            .clickable { onAddToCart() }
                            .testTag("add_to_cart_${product.productId}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = "Add to cart", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

// 5. MarketplaceProductGrid
@Composable
fun MarketplaceProductGrid(
    products: List<MarketplaceProduct>,
    wishlist: Set<String>,
    onWishlistToggle: (String) -> Unit,
    onAddToCart: (MarketplaceProduct) -> Unit,
    onProductClick: (MarketplaceProduct) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        products.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { prod ->
                    Box(modifier = Modifier.weight(1f)) {
                        MarketplaceProductCard(
                            product = prod,
                            isWishlisted = wishlist.contains(prod.productId),
                            onWishlistToggle = { onWishlistToggle(prod.productId) },
                            onAddToCart = { onAddToCart(prod) },
                            onClick = { onProductClick(prod) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// 6. MarketplaceDealCard
@Composable
fun MarketplaceDealCard(
    dealTitle: String,
    subtitle: String,
    code: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("deal_card"),
        shape = HealthogramTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.primary.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, HealthogramTheme.colors.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(HealthogramTheme.colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LocalOffer, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(dealTitle, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text(subtitle, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                }
            }

            Surface(
                shape = HealthogramTheme.shapes.small,
                color = HealthogramTheme.colors.surface,
                border = BorderStroke(1.dp, HealthogramTheme.colors.primary)
            ) {
                Text(
                    text = code,
                    style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// 7. MarketplaceFlashSaleCard
@Composable
fun MarketplaceFlashSaleCard(
    countdownText: String = "Ends in 04:12:39",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = HealthogramTheme.shapes.medium,
        color = HealthogramTheme.colors.errorContainer,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("flash_sale_card")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = HealthogramTheme.colors.error, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Daily Health Flash Sale", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.error)
                    Text("Up to 40% off certified medical supplies", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                }
            }

            Surface(
                shape = HealthogramTheme.shapes.pill,
                color = HealthogramTheme.colors.error
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(countdownText, style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = Color.White)
                }
            }
        }
    }
}

// 8. MarketplaceSellerCard
@Composable
fun MarketplaceSellerCard(
    seller: PublicSellerProfile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("seller_card_${seller.sellerUid}"),
        shape = HealthogramTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(HealthogramTheme.colors.secondary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Storefront, contentDescription = null, tint = HealthogramTheme.colors.secondary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(seller.sellerName, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    if (seller.isVerifiedSeller) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Verified, contentDescription = "Verified Seller", tint = HealthogramTheme.colors.primary, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text("Rating: ${seller.rating} ★ (${seller.reviewCount} reviews)", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                Text(seller.deliveryInformation, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthogramTheme.colors.textMuted)
        }
    }
}

// 9. MarketplaceRating
@Composable
fun MarketplaceRating(
    rating: Float,
    reviewCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = String.format("%.1f", rating),
            style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
            color = HealthogramTheme.colors.textPrimary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "($reviewCount)",
            style = HealthogramTheme.typography.caption,
            color = HealthogramTheme.colors.textMuted
        )
    }
}

// 10. MarketplacePrice
@Composable
fun MarketplacePrice(
    price: Double,
    originalPrice: Double? = null,
    currency: String = "SAR",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = MarketplaceCustomFunctions.formatMarketplacePrice(price, currency),
            style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = HealthogramTheme.colors.primary
        )
        if (originalPrice != null && originalPrice > price) {
            Text(
                text = MarketplaceCustomFunctions.formatMarketplacePrice(originalPrice, currency),
                style = HealthogramTheme.typography.caption.copy(
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                ),
                color = HealthogramTheme.colors.textMuted
            )
        }
    }
}

// 11. MarketplaceDiscountBadge
@Composable
fun MarketplaceDiscountBadge(
    text: String,
    isFlashSale: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = HealthogramTheme.shapes.small,
        color = if (isFlashSale) HealthogramTheme.colors.error else HealthogramTheme.colors.primary,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// 12. MarketplaceWishlistButton
@Composable
fun MarketplaceWishlistButton(
    isWishlisted: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = HealthogramTheme.colors.surface.copy(alpha = 0.9f),
        modifier = modifier
            .size(32.dp)
            .clickable { onToggle() }
            .testTag("wishlist_toggle_btn")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Wishlist",
                tint = if (isWishlisted) HealthogramTheme.colors.error else HealthogramTheme.colors.textMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// 13. MarketplaceCartItem
@Composable
fun MarketplaceCartItem(
    item: MarketplaceCartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cart_item_${item.itemId}"),
        shape = HealthogramTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(HealthogramTheme.shapes.small)
                    .background(HealthogramTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.productTitleSnapshot, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(2.dp))
                Text(MarketplaceCustomFunctions.formatMarketplacePrice(item.unitPriceSnapshot, item.currency), style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.primary)

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrement, modifier = Modifier.size(28.dp).clip(CircleShape).background(HealthogramTheme.colors.surfaceVariant)) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                    }
                    Text("${item.quantity}", modifier = Modifier.padding(horizontal = 12.dp), style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = onIncrement, modifier = Modifier.size(28.dp).clip(CircleShape).background(HealthogramTheme.colors.surfaceVariant)) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                    }
                }
            }

            IconButton(onClick = onRemove, modifier = Modifier.testTag("cart_item_remove_${item.itemId}")) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = HealthogramTheme.colors.error)
            }
        }
    }
}

// 14. MarketplaceCartSummary
@Composable
fun MarketplaceCartSummary(
    subtotal: Double,
    currency: String,
    onProceedToCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = HealthogramTheme.shapes.large,
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
        modifier = modifier
            .fillMaxWidth()
            .testTag("cart_summary")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Subtotal", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                Text(MarketplaceCustomFunctions.formatMarketplacePrice(subtotal, currency), style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
            }

            Button(
                onClick = onProceedToCheckout,
                shape = HealthogramTheme.shapes.pill,
                modifier = Modifier.testTag("checkout_btn")
            ) {
                Text("Checkout")
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// 15. MarketplaceAddressCard
@Composable
fun MarketplaceAddressCard(
    address: MarketplaceAddress,
    isSelected: Boolean = false,
    onSelect: () -> Unit,
    onEdit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("address_card_${address.addressId}"),
        shape = HealthogramTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = if (isSelected) HealthogramTheme.colors.primary.copy(alpha = 0.05f) else HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.borderLight)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (address.label.equals("Work", true)) Icons.Default.Work else Icons.Default.Home,
                        contentDescription = null,
                        tint = HealthogramTheme.colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(address.label, style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    if (address.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(shape = HealthogramTheme.shapes.small, color = HealthogramTheme.colors.primary.copy(alpha = 0.15f)) {
                            Text("Default", style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }

                RadioButton(selected = isSelected, onClick = onSelect)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(address.fullName, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
            Text("${address.addressLine1}, ${address.district}, ${address.city}", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
            Text(address.phone, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
        }
    }
}

// 16. MarketplaceCheckoutSummary
@Composable
fun MarketplaceCheckoutSummary(
    validationResult: CartValidationResult,
    appliedCouponCode: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("checkout_summary"),
        shape = HealthogramTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Order Summary", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Divider(color = HealthogramTheme.colors.borderLight)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
                Text(MarketplaceCustomFunctions.formatMarketplacePrice(validationResult.validatedSubtotal, validationResult.currency), style = HealthogramTheme.typography.bodySmall)
            }

            if (validationResult.validatedDiscount > 0.0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Discount ${appliedCouponCode?.let { "($it)" } ?: ""}", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.primary)
                    Text("-${MarketplaceCustomFunctions.formatMarketplacePrice(validationResult.validatedDiscount, validationResult.currency)}", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.primary)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Estimated Delivery", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
                Text(
                    text = if (validationResult.validatedDelivery == 0.0) "FREE" else MarketplaceCustomFunctions.formatMarketplacePrice(validationResult.validatedDelivery, validationResult.currency),
                    style = HealthogramTheme.typography.bodySmall
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("VAT (15% Saudi Standard)", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
                Text(MarketplaceCustomFunctions.formatMarketplacePrice(validationResult.validatedTax, validationResult.currency), style = HealthogramTheme.typography.bodySmall)
            }

            Divider(color = HealthogramTheme.colors.borderLight)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Grand Total", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text(MarketplaceCustomFunctions.formatMarketplacePrice(validationResult.authoritativeGrandTotal, validationResult.currency), style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
            }
        }
    }
}

// 17. MarketplacePaymentCard
@Composable
fun MarketplacePaymentCard(
    methodName: String,
    methodIcon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("payment_method_${methodName.lowercase()}"),
        shape = HealthogramTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = if (isSelected) HealthogramTheme.colors.primary.copy(alpha = 0.05f) else HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.borderLight)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(methodIcon, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(methodName, style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            }
            RadioButton(selected = isSelected, onClick = onSelect)
        }
    }
}

// 18. MarketplaceOrderCard
@Composable
fun MarketplaceOrderCard(
    order: MarketplaceOrderSnapshot,
    onClick: () -> Unit,
    onTrackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("order_card_${order.orderId}"),
        shape = HealthogramTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(order.orderNumber, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Surface(
                    shape = HealthogramTheme.shapes.small,
                    color = when (order.orderStatus) {
                        MarketplaceOrderStatus.DELIVERED -> HealthogramTheme.colors.primary.copy(alpha = 0.15f)
                        MarketplaceOrderStatus.SHIPPED, MarketplaceOrderStatus.OUT_FOR_DELIVERY -> HealthogramTheme.colors.secondary.copy(alpha = 0.15f)
                        else -> HealthogramTheme.colors.surfaceVariant
                    }
                ) {
                    Text(
                        text = order.orderStatus.label,
                        style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                        color = when (order.orderStatus) {
                            MarketplaceOrderStatus.DELIVERED -> HealthogramTheme.colors.primary
                            MarketplaceOrderStatus.SHIPPED, MarketplaceOrderStatus.OUT_FOR_DELIVERY -> HealthogramTheme.colors.secondary
                            else -> HealthogramTheme.colors.textPrimary
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Recipient: ${order.shippingAddressSnapshot.fullName}", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
            Text("Total: ${MarketplaceCustomFunctions.formatMarketplacePrice(order.grandTotal, order.currency)}", style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onTrackClick,
                    shape = HealthogramTheme.shapes.pill,
                    modifier = Modifier.testTag("track_order_${order.orderId}")
                ) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Track Order")
                }
            }
        }
    }
}

// 19. MarketplaceOrderTimeline
@Composable
fun MarketplaceOrderTimeline(
    orderStatus: MarketplaceOrderStatus,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        "Order Placed" to (orderStatus != MarketplaceOrderStatus.CANCELLED),
        "Payment Confirmed" to (orderStatus.ordinal >= MarketplaceOrderStatus.PAID.ordinal),
        "Packed at Pharmacy" to (orderStatus.ordinal >= MarketplaceOrderStatus.PACKED.ordinal),
        "Shipped & In Transit" to (orderStatus.ordinal >= MarketplaceOrderStatus.SHIPPED.ordinal),
        "Delivered" to (orderStatus == MarketplaceOrderStatus.DELIVERED || orderStatus == MarketplaceOrderStatus.COMPLETED)
    )

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        steps.forEachIndexed { idx, (label, completed) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (completed) HealthogramTheme.colors.primary else HealthogramTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (completed) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    } else {
                        Text("${idx + 1}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    style = HealthogramTheme.typography.bodyMedium.copy(
                        fontWeight = if (completed) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (completed) HealthogramTheme.colors.textPrimary else HealthogramTheme.colors.textMuted
                )
            }
        }
    }
}

// 20. MarketplaceReturnCard
@Composable
fun MarketplaceReturnCard(
    returnReq: MarketplaceReturn,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("return_card_${returnReq.returnId}"),
        shape = HealthogramTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Return #${returnReq.returnId.take(8)}", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Surface(shape = HealthogramTheme.shapes.small, color = HealthogramTheme.colors.secondary.copy(alpha = 0.15f)) {
                    Text(returnReq.status.label, style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.secondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Reason: ${returnReq.reasonCode}", style = HealthogramTheme.typography.bodySmall)
            Text(returnReq.description, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
        }
    }
}

// 21. MarketplaceRefundCard
@Composable
fun MarketplaceRefundCard(
    refund: MarketplaceRefund,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("refund_card_${refund.refundId}"),
        shape = HealthogramTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Refund #${refund.refundId.take(8)}", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text("Reason: ${refund.reason}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(MarketplaceCustomFunctions.formatMarketplacePrice(refund.amount, refund.currency), style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                Text(refund.status.label, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.secondary)
            }
        }
    }
}

// 22. MarketplaceReviewCard
@Composable
fun MarketplaceReviewCard(
    review: MarketplaceReview,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("review_card_${review.reviewId}"),
        shape = HealthogramTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(review.rating) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                    }
                }

                if (review.verifiedPurchase) {
                    Surface(shape = HealthogramTheme.shapes.small, color = HealthogramTheme.colors.primary.copy(alpha = 0.1f)) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Verified Purchase", style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(review.title, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(2.dp))
            Text(review.reviewText, style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textPrimary)
        }
    }
}

// 23. MarketplaceEmptyState
@Composable
fun MarketplaceEmptyState(
    icon: ImageVector = Icons.Default.ShoppingCart,
    title: String,
    message: String,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
            .testTag("marketplace_empty_state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(HealthogramTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.textPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        Text(message, style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)

        if (actionButtonText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onActionClick, shape = HealthogramTheme.shapes.pill) {
                Text(actionButtonText)
            }
        }
    }
}

// 24. MarketplaceLoadingState & ErrorState
@Composable
fun MarketplaceLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
            .testTag("marketplace_loading_state"),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = HealthogramTheme.colors.primary)
    }
}

@Composable
fun MarketplaceErrorState(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .testTag("marketplace_error_state"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = HealthogramTheme.colors.error, modifier = Modifier.size(44.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text("An error occurred", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Text(errorMessage, style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}
