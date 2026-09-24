package com.example.healthogram.ui.marketplace.seller.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.marketplace.MarketplaceProduct
import com.example.healthogram.marketplace.ProductStatus
import com.example.healthogram.marketplace.seller.*
import com.example.healthogram.ui.marketplace.seller.components.*

sealed class SellerNavigationDest {
    object MainDashboard : SellerNavigationDest()
    object CreateProduct : SellerNavigationDest()
    data class EditProduct(val product: MarketplaceProduct) : SellerNavigationDest()
    data class ProductPreview(val product: MarketplaceProduct) : SellerNavigationDest()
    data class OrderDetails(val order: SellerOrderView) : SellerNavigationDest()
    object RequestPayout : SellerNavigationDest()
    object FullLedger : SellerNavigationDest()
    object Support : SellerNavigationDest()
    object Settings : SellerNavigationDest()
    object Onboarding : SellerNavigationDest()
    object VerificationStatus : SellerNavigationDest()
}

/**
 * HEALTHOGRAM — STEP 09: SELLER CENTER MASTER DASHBOARD
 * Central operating cockpit for verified medical and health sellers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardPage(
    repository: SellerRepository,
    onNavigateBackToCustomerMarketplace: () -> Unit = {}
) {
    var currentDest by remember { mutableStateOf<SellerNavigationDest>(SellerNavigationDest.MainDashboard) }
    var selectedTab by remember { mutableStateOf(0) }

    val profile by repository.sellerProfile.collectAsState()
    val verification by repository.sellerVerification.collectAsState()
    val balance by repository.sellerBalance.collectAsState()
    val products by repository.sellerProducts.collectAsState()
    val inventory by repository.sellerInventory.collectAsState()
    val orders by repository.sellerOrders.collectAsState()
    val ledger by repository.sellerLedger.collectAsState()
    val payoutRequests by repository.payoutRequests.collectAsState()
    val supportTickets by repository.sellerSupportTickets.collectAsState()

    val lowStockCount = inventory.count { it.availableQuantity <= it.lowStockThreshold }
    val pendingOrdersCount = orders.count { it.orderStatus == "CONFIRMED" || it.orderStatus == "PROCESSING" }

    when (val dest = currentDest) {
        is SellerNavigationDest.CreateProduct -> {
            SellerCreateProductPage(
                onSaveDraft = { title, desc, price, stock, sku ->
                    val prod = MarketplaceProduct(
                        productId = "prod_${System.currentTimeMillis()}",
                        sellerUid = repository.currentSellerUid,
                        title = title,
                        shortDescription = desc.take(100),
                        description = desc,
                        categoryId = "cat_med_equip",
                        price = price,
                        stockQuantity = stock,
                        sku = sku,
                        status = ProductStatus.DRAFT
                    )
                    repository.addProduct(prod)
                    currentDest = SellerNavigationDest.MainDashboard
                },
                onSubmitForReview = { title, desc, price, stock, sku ->
                    val prod = MarketplaceProduct(
                        productId = "prod_${System.currentTimeMillis()}",
                        sellerUid = repository.currentSellerUid,
                        title = title,
                        shortDescription = desc.take(100),
                        description = desc,
                        categoryId = "cat_med_equip",
                        price = price,
                        stockQuantity = stock,
                        sku = sku,
                        status = ProductStatus.PENDING_REVIEW
                    )
                    repository.addProduct(prod)
                    currentDest = SellerNavigationDest.MainDashboard
                },
                onCancel = { currentDest = SellerNavigationDest.MainDashboard }
            )
        }
        is SellerNavigationDest.EditProduct -> {
            SellerEditProductPage(
                product = dest.product,
                onSave = { updated ->
                    repository.updateProduct(updated)
                    currentDest = SellerNavigationDest.MainDashboard
                },
                onBack = { currentDest = SellerNavigationDest.MainDashboard }
            )
        }
        is SellerNavigationDest.ProductPreview -> {
            SellerProductPreviewPage(
                product = dest.product,
                onBack = { currentDest = SellerNavigationDest.MainDashboard }
            )
        }
        is SellerNavigationDest.OrderDetails -> {
            SellerOrderDetailsPage(
                order = dest.order,
                onBack = { currentDest = SellerNavigationDest.MainDashboard },
                onAdvanceStatus = { nextStatus ->
                    repository.updateOrderStatus(dest.order.orderId, nextStatus)
                }
            )
        }
        is SellerNavigationDest.RequestPayout -> {
            SellerRequestPayoutPage(
                balance = balance,
                payoutAccount = repository.payoutAccounts.value.firstOrNull(),
                onSubmitPayout = { amt ->
                    repository.submitPayoutRequest(amt)
                    currentDest = SellerNavigationDest.MainDashboard
                },
                onBack = { currentDest = SellerNavigationDest.MainDashboard }
            )
        }
        is SellerNavigationDest.FullLedger -> {
            SellerLedgerPage(
                ledger = ledger,
                onBack = { currentDest = SellerNavigationDest.MainDashboard }
            )
        }
        is SellerNavigationDest.Support -> {
            SellerSupportPage(
                tickets = supportTickets,
                onCreateTicket = { subj, desc, cat ->
                    repository.createSupportTicket(subj, desc, cat)
                },
                onBack = { currentDest = SellerNavigationDest.MainDashboard }
            )
        }
        is SellerNavigationDest.Settings -> {
            SellerSettingsPage(
                profile = profile,
                onSaveProfile = { updated ->
                    repository.updateProfile(updated)
                },
                onBack = { currentDest = SellerNavigationDest.MainDashboard }
            )
        }
        is SellerNavigationDest.Onboarding -> {
            SellerOnboardingPage(
                onStartOnboarding = { currentDest = SellerNavigationDest.MainDashboard },
                onCheckStatus = { currentDest = SellerNavigationDest.VerificationStatus }
            )
        }
        is SellerNavigationDest.VerificationStatus -> {
            SellerVerificationStatusPage(
                verification = verification,
                onBack = { currentDest = SellerNavigationDest.MainDashboard }
            )
        }
        SellerNavigationDest.MainDashboard -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text("Seller Center", style = HealthogramTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold))
                                Text(profile.storeName, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBackToCustomerMarketplace) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Return to Marketplace")
                            }
                        },
                        actions = {
                            IconButton(onClick = { currentDest = SellerNavigationDest.Support }) {
                                Icon(Icons.Default.HelpOutline, contentDescription = "Seller Support")
                            }
                            IconButton(onClick = { currentDest = SellerNavigationDest.Settings }) {
                                Icon(Icons.Default.Settings, contentDescription = "Store Settings")
                            }
                        }
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = HealthogramTheme.colors.surface,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Overview") },
                            label = { Text("Overview") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.Inventory2, contentDescription = "Products") },
                            label = { Text("Catalog") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (pendingOrdersCount > 0) {
                                            Badge { Text("$pendingOrdersCount") }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.LocalShipping, contentDescription = "Orders")
                                }
                            },
                            label = { Text("Orders") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Finance") },
                            label = { Text("Finance") }
                        )
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    when (selectedTab) {
                        0 -> {
                            // Dashboard Overview
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                item {
                                    SellerDashboardHeader(
                                        profile = profile,
                                        onStoreClick = {},
                                        onSettingsClick = { currentDest = SellerNavigationDest.Settings }
                                    )
                                }

                                item {
                                    SellerRevenueCard(
                                        balance = balance,
                                        onPayoutClick = { currentDest = SellerNavigationDest.RequestPayout }
                                    )
                                }

                                if (lowStockCount > 0) {
                                    item {
                                        SellerLowStockCard(
                                            lowStockCount = lowStockCount,
                                            onClick = { selectedTab = 1 }
                                        )
                                    }
                                }

                                item {
                                    SellerOrdersCard(
                                        totalOrders = orders.size,
                                        pendingOrders = pendingOrdersCount,
                                        onClick = { selectedTab = 2 }
                                    )
                                }

                                item {
                                    Text("Top Performing Listings", style = HealthogramTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold))
                                }

                                items(products.take(3)) { prod ->
                                    SellerProductCard(
                                        product = prod,
                                        onClick = { currentDest = SellerNavigationDest.ProductPreview(prod) },
                                        onEditClick = { currentDest = SellerNavigationDest.EditProduct(prod) }
                                    )
                                }
                            }
                        }
                        1 -> {
                            // Products & Inventory
                            SellerProductsPage(
                                products = products,
                                onCreateProductClick = { currentDest = SellerNavigationDest.CreateProduct },
                                onProductClick = { prod -> currentDest = SellerNavigationDest.ProductPreview(prod) },
                                onEditProductClick = { prod -> currentDest = SellerNavigationDest.EditProduct(prod) }
                            )
                        }
                        2 -> {
                            // Orders Fulfillment
                            SellerOrdersPage(
                                orders = orders,
                                onOrderClick = { ord -> currentDest = SellerNavigationDest.OrderDetails(ord) },
                                onAdvanceOrderStatus = { orderId, nextStatus ->
                                    repository.updateOrderStatus(orderId, nextStatus)
                                }
                            )
                        }
                        3 -> {
                            // Finance & Authoritative Ledger
                            SellerFinancialDashboardPage(
                                balance = balance,
                                ledger = ledger,
                                payoutRequests = payoutRequests,
                                onRequestPayoutClick = { currentDest = SellerNavigationDest.RequestPayout },
                                onLedgerClick = { currentDest = SellerNavigationDest.FullLedger }
                            )
                        }
                    }
                }
            }
        }
    }
}
