package com.example.healthogram.ui.marketplace.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.marketplace.*
import com.example.healthogram.ui.marketplace.components.*

/**
 * HEALTHOGRAM — STEP 08: MARKETPLACE CUSTOMER PAGES (PART 1)
 * 1. MarketplaceHomePage
 * 2. MarketplaceSearchPage
 * 3. MarketplaceSearchResultsPage
 * 4. MarketplaceCategoriesPage
 * 5. MarketplaceCategoryProductsPage
 * 6. MarketplaceProductDetailsPage
 * 7. MarketplaceSellerProfilePage
 */

// 1. MarketplaceHomePage
@Composable
fun MarketplaceHomePage(
    repository: MarketplaceRepository,
    onNavigateToSearch: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToDeals: () -> Unit,
    onNavigateToFlashSales: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onProductClick: (MarketplaceProduct) -> Unit,
    onSellerClick: (SellerProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val products by repository.products.collectAsState()
    val wishlist by repository.wishlist.collectAsState()
    val cartItems by repository.cartItems.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("marketplace_home_page"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            MarketplaceHeader(
                cartCount = MarketplaceCustomFunctions.getCartItemCount(cartItems),
                wishlistCount = wishlist.size,
                notificationCount = 3,
                onWishlistClick = onNavigateToWishlist,
                onCartClick = onNavigateToCart,
                onNotificationsClick = onNavigateToNotifications
            )
        }

        // Search Bar Hero
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                MarketplaceSearchBar(
                    query = "",
                    onQueryChange = {},
                    onSearchClick = onNavigateToSearch
                )
            }
        }

        // Flash Sale Hero Banner
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                MarketplaceFlashSaleCard(onClick = onNavigateToFlashSales)
            }
        }

        // Category Horizontal Row
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Categories", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    TextButton(onClick = onNavigateToCategories) {
                        Text("See All", color = HealthogramTheme.colors.primary)
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(repository.categories) { cat ->
                        MarketplaceCategoryCard(
                            category = cat,
                            onClick = onNavigateToCategories
                        )
                    }
                }
            }
        }

        // Top Deals Banner
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                MarketplaceDealCard(
                    dealTitle = "10% OFF Vital Signs Monitors",
                    subtitle = "Use code HEALTH10 at checkout",
                    code = "HEALTH10",
                    onClick = onNavigateToDeals
                )
            }
        }

        // Flash Sale Products Section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = HealthogramTheme.colors.error, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Flash Deals", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    TextButton(onClick = onNavigateToFlashSales) {
                        Text("View All", color = HealthogramTheme.colors.error)
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products.filter { it.isFlashSale }) { prod ->
                        MarketplaceProductCard(
                            product = prod,
                            isWishlisted = wishlist.contains(prod.productId),
                            onWishlistToggle = { repository.toggleWishlist(prod.productId) },
                            onAddToCart = { repository.addToCart(prod) },
                            onClick = {
                                repository.recordView(prod.productId)
                                onProductClick(prod)
                            },
                            modifier = Modifier.width(175.dp)
                        )
                    }
                }
            }
        }

        // Featured Medical Products (Grid)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Featured Healthcare Catalog", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))
                MarketplaceProductGrid(
                    products = products.filter { it.isFeatured },
                    wishlist = wishlist,
                    onWishlistToggle = { repository.toggleWishlist(it) },
                    onAddToCart = { repository.addToCart(it) },
                    onProductClick = {
                        repository.recordView(it.productId)
                        onProductClick(it)
                    }
                )
            }
        }

        // Verified Healthcare Sellers
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Certified Healthcare Sellers", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))
                repository.sellers.forEach { seller ->
                    MarketplaceSellerCard(
                        seller = seller.toPublicProfile(),
                        onClick = { onSellerClick(seller) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

// 2. MarketplaceSearchPage
@Composable
fun MarketplaceSearchPage(
    repository: MarketplaceRepository,
    onSearchSubmit: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val recentSearches = remember { mutableStateListOf("Blood Pressure Monitor", "Pulse Oximeter", "Nebulizer", "First Aid") }
    val popularSearches = listOf("HbA1c Kit", "Infrared Thermometer", "Lumbar Cushion", "Omron", "BioSense")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_search_page")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            MarketplaceSearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearchClick = {},
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { if (query.isNotBlank()) onSearchSubmit(query) },
                shape = HealthogramTheme.shapes.pill,
                modifier = Modifier.testTag("search_submit_btn")
            ) {
                Text("Search")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Recent Searches", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))
        recentSearches.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        query = item
                        onSearchSubmit(item)
                    }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = HealthogramTheme.colors.textMuted, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(item, style = HealthogramTheme.typography.bodyMedium)
                }
                Icon(Icons.Default.NorthWest, contentDescription = null, tint = HealthogramTheme.colors.textMuted, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Popular Searches", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(popularSearches) { pop ->
                SuggestionChip(
                    onClick = {
                        query = pop
                        onSearchSubmit(pop)
                    },
                    label = { Text(pop) }
                )
            }
        }
    }
}

// 3. MarketplaceSearchResultsPage
@Composable
fun MarketplaceSearchResultsPage(
    query: String,
    repository: MarketplaceRepository,
    onProductClick: (MarketplaceProduct) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val products by repository.products.collectAsState()
    val wishlist by repository.wishlist.collectAsState()
    var sortOrder by remember { mutableStateOf(SearchSortOrder.RELEVANCE) }
    var filterOnlyDeals by remember { mutableStateOf(false) }

    val filtered = remember(products, query, sortOrder, filterOnlyDeals) {
        var res = products.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true) ||
            it.brand.contains(query, ignoreCase = true)
        }
        if (filterOnlyDeals) res = res.filter { it.isDeal }
        when (sortOrder) {
            SearchSortOrder.RELEVANCE -> res
            SearchSortOrder.PRICE_LOW_HIGH -> res.sortedBy { it.discountPrice ?: it.price }
            SearchSortOrder.PRICE_HIGH_LOW -> res.sortedByDescending { it.discountPrice ?: it.price }
            SearchSortOrder.RATING -> res.sortedByDescending { it.rating }
            else -> res
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_search_results_page")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Search: \"$query\"", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
            Text("${filtered.size} results", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            FilterChip(
                selected = filterOnlyDeals,
                onClick = { filterOnlyDeals = !filterOnlyDeals },
                label = { Text("Deals Only") }
            )
            FilterChip(
                selected = sortOrder == SearchSortOrder.PRICE_LOW_HIGH,
                onClick = { sortOrder = if (sortOrder == SearchSortOrder.PRICE_LOW_HIGH) SearchSortOrder.RELEVANCE else SearchSortOrder.PRICE_LOW_HIGH },
                label = { Text("Price: Low to High") }
            )
            FilterChip(
                selected = sortOrder == SearchSortOrder.RATING,
                onClick = { sortOrder = if (sortOrder == SearchSortOrder.RATING) SearchSortOrder.RELEVANCE else SearchSortOrder.RATING },
                label = { Text("Rating") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (filtered.isEmpty()) {
            MarketplaceEmptyState(
                icon = Icons.Default.SearchOff,
                title = "No products found",
                message = "Try different keywords or browse our certified categories."
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    MarketplaceProductGrid(
                        products = filtered,
                        wishlist = wishlist,
                        onWishlistToggle = { repository.toggleWishlist(it) },
                        onAddToCart = { repository.addToCart(it) },
                        onProductClick = onProductClick
                    )
                }
            }
        }
    }
}

// 4. MarketplaceCategoriesPage
@Composable
fun MarketplaceCategoriesPage(
    repository: MarketplaceRepository,
    onCategorySelected: (MarketplaceCategory) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_categories_page")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("All Healthcare Categories", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(repository.categories) { cat ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategorySelected(cat) }
                        .testTag("category_list_item_${cat.categoryId}"),
                    shape = HealthogramTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                    border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(HealthogramTheme.colors.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = MarketplaceCustomFunctions.getMarketplaceCategoryIcon(cat.categoryId),
                                contentDescription = null,
                                tint = HealthogramTheme.colors.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(cat.name, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(cat.description, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthogramTheme.colors.textMuted)
                    }
                }
            }
        }
    }
}

// 5. MarketplaceCategoryProductsPage
@Composable
fun MarketplaceCategoryProductsPage(
    category: MarketplaceCategory,
    repository: MarketplaceRepository,
    onProductClick: (MarketplaceProduct) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val products by repository.products.collectAsState()
    val wishlist by repository.wishlist.collectAsState()
    val categoryProducts = products.filter { it.categoryId == category.categoryId || it.category.name.contains(category.name.take(4), true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_category_products_page")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Column {
                Text(category.name, style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text("${categoryProducts.size} certified items", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (categoryProducts.isEmpty()) {
            MarketplaceEmptyState(
                icon = Icons.Default.Category,
                title = "No products in this category yet",
                message = "New certified medical supplies are added weekly."
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    MarketplaceProductGrid(
                        products = categoryProducts,
                        wishlist = wishlist,
                        onWishlistToggle = { repository.toggleWishlist(it) },
                        onAddToCart = { repository.addToCart(it) },
                        onProductClick = onProductClick
                    )
                }
            }
        }
    }
}

// 6. MarketplaceProductDetailsPage
@Composable
fun MarketplaceProductDetailsPage(
    product: MarketplaceProduct,
    repository: MarketplaceRepository,
    onBuyNow: (MarketplaceProduct, Int) -> Unit,
    onSellerClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wishlist by repository.wishlist.collectAsState()
    val isWishlisted = wishlist.contains(product.productId)
    var quantity by remember { mutableIntStateOf(1) }
    val productReviews = repository.reviews.collectAsState().value.filter { it.productId == product.productId }

    Scaffold(
        bottomBar = {
            Surface(
                color = HealthogramTheme.colors.surface,
                border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { repository.addToCart(product, quantity) },
                        shape = HealthogramTheme.shapes.pill,
                        modifier = Modifier.weight(1f).testTag("details_add_to_cart_btn")
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add to Cart")
                    }

                    Button(
                        onClick = { onBuyNow(product, quantity) },
                        shape = HealthogramTheme.shapes.pill,
                        modifier = Modifier.weight(1f).testTag("details_buy_now_btn")
                    ) {
                        Text("Buy Now")
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(HealthogramTheme.colors.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .testTag("marketplace_product_details_page"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Row {
                        MarketplaceWishlistButton(
                            isWishlisted = isWishlisted,
                            onToggle = { repository.toggleWishlist(product.productId) }
                        )
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                }
            }

            // Big Product Showcase Area
            item {
                Surface(
                    shape = HealthogramTheme.shapes.large,
                    color = HealthogramTheme.colors.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = MarketplaceCustomFunctions.getMarketplaceCategoryIcon(product.categoryId),
                            contentDescription = null,
                            tint = HealthogramTheme.colors.primary,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }

            // Title, Rating & Price
            item {
                Column {
                    Text(product.title, style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))
                    MarketplaceRating(rating = product.rating, reviewCount = product.reviewCount)
                    Spacer(modifier = Modifier.height(10.dp))
                    MarketplacePrice(
                        price = product.discountPrice ?: product.price,
                        originalPrice = if (product.discountPrice != null) product.price else null,
                        currency = product.currency
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = MarketplaceCustomFunctions.getProductStockLabel(product.stockQuantity),
                        style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                        color = if (product.stockQuantity > 10) HealthogramTheme.colors.primary else HealthogramTheme.colors.error
                    )
                }
            }

            // Quantity Selector
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Quantity", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (quantity > 1) quantity-- }, modifier = Modifier.size(32.dp).clip(CircleShape).background(HealthogramTheme.colors.surfaceVariant)) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        Text("$quantity", modifier = Modifier.padding(horizontal = 16.dp), style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        IconButton(onClick = { if (quantity < product.stockQuantity) quantity++ }, modifier = Modifier.size(32.dp).clip(CircleShape).background(HealthogramTheme.colors.surfaceVariant)) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }
            }

            // Seller Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onSellerClick(product.sellerUid) },
                    shape = HealthogramTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                    border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = HealthogramTheme.colors.secondary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.sellerStoreName, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text("Verified Medical Supplier • 1-3 days delivery", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthogramTheme.colors.textMuted)
                    }
                }
            }

            // Description & Medical Compliance Protection
            item {
                Column {
                    Text("Product Description", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(product.description, style = HealthogramTheme.typography.bodyMedium, color = HealthogramTheme.colors.textPrimary)

                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = HealthogramTheme.shapes.small,
                        color = HealthogramTheme.colors.primary.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, HealthogramTheme.colors.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Medical Claims Policy Compliant: Evaluated under Healthogram Safety Standards. Does not replace professional physician diagnosis.",
                                style = HealthogramTheme.typography.caption,
                                color = HealthogramTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }

            // Customer Reviews Section
            item {
                Column {
                    Text("Customer Reviews (${productReviews.size})", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(10.dp))
                    if (productReviews.isEmpty()) {
                        Text("No reviews yet. Be the first to review!", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
                    } else {
                        productReviews.forEach { rev ->
                            MarketplaceReviewCard(review = rev)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}

// 7. MarketplaceSellerProfilePage
@Composable
fun MarketplaceSellerProfilePage(
    seller: SellerProfile,
    repository: MarketplaceRepository,
    onProductClick: (MarketplaceProduct) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val products by repository.products.collectAsState()
    val sellerProducts = products.filter { it.sellerUid == seller.sellerUid }
    val wishlist by repository.wishlist.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(16.dp)
            .testTag("marketplace_seller_profile_page")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Seller Profile", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = HealthogramTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(HealthogramTheme.colors.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(30.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(seller.businessName, style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            if (seller.isVerifiedSeller) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = HealthogramTheme.colors.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                        Text("Certified Medical Merchant • ${seller.country}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = HealthogramTheme.colors.borderLight)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Delivery Info: ${seller.deliveryInformation}", style = HealthogramTheme.typography.caption)
                Text("Return Policy: ${seller.returnInformation}", style = HealthogramTheme.typography.caption)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Products by this Seller (${sellerProducts.size})", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                MarketplaceProductGrid(
                    products = sellerProducts,
                    wishlist = wishlist,
                    onWishlistToggle = { repository.toggleWishlist(it) },
                    onAddToCart = { repository.addToCart(it) },
                    onProductClick = onProductClick
                )
            }
        }
    }
}
