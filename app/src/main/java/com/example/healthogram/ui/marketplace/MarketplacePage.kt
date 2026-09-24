package com.example.healthogram.ui.marketplace

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.healthogram.marketplace.*
import com.example.healthogram.ui.marketplace.pages.*

/**
 * HEALTHOGRAM — STEP 08: MARKETPLACE CUSTOMER CONTROLLER & NAVIGATION
 * Houses the authoritative customer flow across all 28 marketplace screens.
 */

enum class MarketplaceScreen {
    HOME,
    SEARCH,
    SEARCH_RESULTS,
    CATEGORIES,
    CATEGORY_PRODUCTS,
    PRODUCT_DETAILS,
    SELLER_PROFILE,
    WISHLIST,
    CART,
    CHECKOUT,
    ADDRESSES,
    ADD_ADDRESS,
    PAYMENT,
    ORDER_CONFIRMATION,
    ORDERS,
    ORDER_DETAILS,
    ORDER_TRACKING,
    INVOICE,
    REVIEWS,
    WRITE_REVIEW,
    RETURNS,
    REFUND_STATUS,
    DEALS,
    FLASH_SALES,
    RECENTLY_VIEWED,
    NOTIFICATIONS,
    CUSTOMER_SUPPORT,
    SETTINGS
}

@Composable
fun MarketplacePage(
    modifier: Modifier = Modifier,
    onProductClick: (MarketplaceProduct) -> Unit = {},
    onSellerDashboardClick: () -> Unit = {}
) {
    val repository = remember { MarketplaceRepository() }
    var currentScreen by remember { mutableStateOf(MarketplaceScreen.HOME) }
    var screenStack by remember { mutableStateOf(listOf(MarketplaceScreen.HOME)) }

    // Navigation state payloads
    var selectedProduct by remember { mutableStateOf<MarketplaceProduct?>(null) }
    var selectedCategory by remember { mutableStateOf<MarketplaceCategory?>(null) }
    var selectedSeller by remember { mutableStateOf<SellerProfile?>(null) }
    var selectedOrder by remember { mutableStateOf<MarketplaceOrderSnapshot?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val products by repository.products.collectAsState()

    fun navigateTo(screen: MarketplaceScreen) {
        screenStack = screenStack + screen
        currentScreen = screen
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            screenStack = screenStack.dropLast(1)
            currentScreen = screenStack.last()
        } else {
            currentScreen = MarketplaceScreen.HOME
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
    ) {
        // Quick Navigation Bar (Tabs for testing & fast browsing between customer hubs)
        Surface(
            color = HealthogramTheme.colors.surface,
            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val topHubs = listOf(
                    MarketplaceScreen.HOME to "Market Home",
                    MarketplaceScreen.CATEGORIES to "Categories",
                    MarketplaceScreen.DEALS to "Deals",
                    MarketplaceScreen.CART to "Cart",
                    MarketplaceScreen.ORDERS to "Orders",
                    MarketplaceScreen.WISHLIST to "Wishlist",
                    MarketplaceScreen.ADDRESSES to "Addresses",
                    MarketplaceScreen.RETURNS to "Returns",
                    MarketplaceScreen.CUSTOMER_SUPPORT to "Support",
                    MarketplaceScreen.SETTINGS to "Settings"
                )

                items(topHubs) { (scr, label) ->
                    val isSelected = currentScreen == scr
                    Surface(
                        shape = HealthogramTheme.shapes.pill,
                        color = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.surfaceVariant,
                        modifier = Modifier.clickable {
                            currentScreen = scr
                            screenStack = listOf(MarketplaceScreen.HOME, scr)
                        }
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = HealthogramTheme.typography.caption.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                            color = if (isSelected) Color.White else HealthogramTheme.colors.textPrimary
                        )
                    }
                }

                item {
                    // Quick button to Seller Dashboard
                    Surface(
                        shape = HealthogramTheme.shapes.pill,
                        color = HealthogramTheme.colors.secondary.copy(alpha = 0.15f),
                        modifier = Modifier.clickable { onSellerDashboardClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = HealthogramTheme.colors.secondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Seller Center", style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.secondary)
                        }
                    }
                }
            }
        }

        // Animated Screen Container
        Box(modifier = Modifier.weight(1f)) {
            Crossfade(targetState = currentScreen, label = "marketplace_navigation") { screen ->
                when (screen) {
                    // Part 1 Pages
                    MarketplaceScreen.HOME -> MarketplaceHomePage(
                        repository = repository,
                        onNavigateToSearch = { navigateTo(MarketplaceScreen.SEARCH) },
                        onNavigateToCategories = { navigateTo(MarketplaceScreen.CATEGORIES) },
                        onNavigateToDeals = { navigateTo(MarketplaceScreen.DEALS) },
                        onNavigateToFlashSales = { navigateTo(MarketplaceScreen.FLASH_SALES) },
                        onNavigateToCart = { navigateTo(MarketplaceScreen.CART) },
                        onNavigateToWishlist = { navigateTo(MarketplaceScreen.WISHLIST) },
                        onNavigateToNotifications = { navigateTo(MarketplaceScreen.NOTIFICATIONS) },
                        onProductClick = { prod ->
                            selectedProduct = prod
                            onProductClick(prod)
                            navigateTo(MarketplaceScreen.PRODUCT_DETAILS)
                        },
                        onSellerClick = { seller ->
                            selectedSeller = seller
                            navigateTo(MarketplaceScreen.SELLER_PROFILE)
                        }
                    )
                    MarketplaceScreen.SEARCH -> MarketplaceSearchPage(
                        repository = repository,
                        onSearchSubmit = { q ->
                            searchQuery = q
                            navigateTo(MarketplaceScreen.SEARCH_RESULTS)
                        },
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.SEARCH_RESULTS -> MarketplaceSearchResultsPage(
                        query = searchQuery,
                        repository = repository,
                        onProductClick = { prod ->
                            selectedProduct = prod
                            onProductClick(prod)
                            navigateTo(MarketplaceScreen.PRODUCT_DETAILS)
                        },
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.CATEGORIES -> MarketplaceCategoriesPage(
                        repository = repository,
                        onCategorySelected = { cat ->
                            selectedCategory = cat
                            navigateTo(MarketplaceScreen.CATEGORY_PRODUCTS)
                        },
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.CATEGORY_PRODUCTS -> MarketplaceCategoryProductsPage(
                        category = selectedCategory ?: repository.categories.first(),
                        repository = repository,
                        onProductClick = { prod ->
                            selectedProduct = prod
                            onProductClick(prod)
                            navigateTo(MarketplaceScreen.PRODUCT_DETAILS)
                        },
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.PRODUCT_DETAILS -> MarketplaceProductDetailsPage(
                        product = selectedProduct ?: products.first(),
                        repository = repository,
                        onBuyNow = { prod, qty ->
                            repository.addToCart(prod, qty)
                            navigateTo(MarketplaceScreen.CHECKOUT)
                        },
                        onSellerClick = { sellerUid ->
                            selectedSeller = repository.sellers.find { it.sellerUid == sellerUid } ?: repository.sellers.first()
                            navigateTo(MarketplaceScreen.SELLER_PROFILE)
                        },
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.SELLER_PROFILE -> MarketplaceSellerProfilePage(
                        seller = selectedSeller ?: repository.sellers.first(),
                        repository = repository,
                        onProductClick = { prod ->
                            selectedProduct = prod
                            onProductClick(prod)
                            navigateTo(MarketplaceScreen.PRODUCT_DETAILS)
                        },
                        onBack = { navigateBack() }
                    )

                    // Part 2 Pages
                    MarketplaceScreen.WISHLIST -> MarketplaceWishlistPage(
                        repository = repository,
                        onProductClick = { prod ->
                            selectedProduct = prod
                            onProductClick(prod)
                            navigateTo(MarketplaceScreen.PRODUCT_DETAILS)
                        },
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.CART -> MarketplaceCartPage(
                        repository = repository,
                        onProceedToCheckout = { navigateTo(MarketplaceScreen.CHECKOUT) },
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.CHECKOUT -> MarketplaceCheckoutPage(
                        repository = repository,
                        onNavigateToAddAddress = { navigateTo(MarketplaceScreen.ADD_ADDRESS) },
                        onProceedToPayment = { ord ->
                            selectedOrder = ord
                            navigateTo(MarketplaceScreen.PAYMENT)
                        },
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.ADDRESSES -> MarketplaceAddressPage(
                        repository = repository,
                        onAddNewAddress = { navigateTo(MarketplaceScreen.ADD_ADDRESS) },
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.ADD_ADDRESS -> MarketplaceAddAddressPage(
                        repository = repository,
                        onAddressSaved = { navigateBack() },
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.PAYMENT -> MarketplacePaymentPage(
                        order = selectedOrder ?: repository.orders.value.first(),
                        repository = repository,
                        onPaymentSuccess = { paidOrder ->
                            selectedOrder = paidOrder
                            navigateTo(MarketplaceScreen.ORDER_CONFIRMATION)
                        },
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.ORDER_CONFIRMATION -> MarketplaceOrderConfirmationPage(
                        order = selectedOrder ?: repository.orders.value.first(),
                        onTrackOrder = { navigateTo(MarketplaceScreen.ORDER_TRACKING) },
                        onViewInvoice = { navigateTo(MarketplaceScreen.INVOICE) },
                        onContinueShopping = {
                            screenStack = listOf(MarketplaceScreen.HOME)
                            currentScreen = MarketplaceScreen.HOME
                        }
                    )

                    // Part 3 Pages
                    MarketplaceScreen.ORDERS -> MarketplaceOrdersPage(
                        repository = repository,
                        onOrderClick = { ord ->
                            selectedOrder = ord
                            navigateTo(MarketplaceScreen.ORDER_DETAILS)
                        },
                        onTrackOrder = { ord ->
                            selectedOrder = ord
                            navigateTo(MarketplaceScreen.ORDER_TRACKING)
                        },
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.ORDER_DETAILS -> MarketplaceOrderDetailsPage(
                        order = selectedOrder ?: repository.orders.value.first(),
                        onTrackOrder = { navigateTo(MarketplaceScreen.ORDER_TRACKING) },
                        onViewInvoice = { navigateTo(MarketplaceScreen.INVOICE) },
                        onRequestReturn = { navigateTo(MarketplaceScreen.RETURNS) },
                        onWriteReview = { navigateTo(MarketplaceScreen.WRITE_REVIEW) },
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.ORDER_TRACKING -> MarketplaceOrderTrackingPage(
                        order = selectedOrder ?: repository.orders.value.first(),
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.INVOICE -> MarketplaceInvoicePage(
                        order = selectedOrder ?: repository.orders.value.first(),
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.REVIEWS -> MarketplaceReviewsPage(
                        repository = repository,
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.WRITE_REVIEW -> MarketplaceWriteReviewPage(
                        order = selectedOrder ?: repository.orders.value.first(),
                        repository = repository,
                        onReviewSubmitted = { navigateBack() },
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.RETURNS -> MarketplaceReturnsPage(
                        repository = repository,
                        onBack = { navigateBack() }
                    )

                    // Part 4 Pages
                    MarketplaceScreen.REFUND_STATUS -> MarketplaceRefundStatusPage(
                        repository = repository,
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.DEALS -> MarketplaceDealsPage(
                        repository = repository,
                        onProductClick = { prod ->
                            selectedProduct = prod
                            onProductClick(prod)
                            navigateTo(MarketplaceScreen.PRODUCT_DETAILS)
                        },
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.FLASH_SALES -> MarketplaceFlashSalesPage(
                        repository = repository,
                        onProductClick = { prod ->
                            selectedProduct = prod
                            onProductClick(prod)
                            navigateTo(MarketplaceScreen.PRODUCT_DETAILS)
                        },
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.RECENTLY_VIEWED -> MarketplaceRecentlyViewedPage(
                        repository = repository,
                        onProductClick = { prod ->
                            selectedProduct = prod
                            onProductClick(prod)
                            navigateTo(MarketplaceScreen.PRODUCT_DETAILS)
                        },
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.NOTIFICATIONS -> MarketplaceNotificationsPage(
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.CUSTOMER_SUPPORT -> MarketplaceCustomerSupportPage(
                        repository = repository,
                        onBack = { navigateBack() }
                    )
                    MarketplaceScreen.SETTINGS -> MarketplaceSettingsPage(
                        repository = repository,
                        onBack = { navigateBack() }
                    )
                }
            }
        }
    }
}
