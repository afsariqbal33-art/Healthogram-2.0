package com.example.healthogram.ui.marketplace.seller.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.marketplace.*
import com.example.healthogram.marketplace.seller.*
import com.example.healthogram.ui.marketplace.seller.components.*

/**
 * HEALTHOGRAM — STEP 09: SELLER PRODUCT CREATION & MANAGEMENT PAGES
 * 12 screens covering Product Listing, Creation, Editing, Compliance, Inventory, Offers, and Flash Sales.
 */

// 1. SellerProductsPage
@Composable
fun SellerProductsPage(
    products: List<MarketplaceProduct>,
    onCreateProductClick: () -> Unit = {},
    onProductClick: (MarketplaceProduct) -> Unit = {},
    onEditProductClick: (MarketplaceProduct) -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateProductClick,
                containerColor = HealthogramTheme.colors.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Product")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("Your Product Catalog", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
                Text("Manage listings, update stock levels, and review compliance status.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (products.isEmpty()) {
                item {
                    SellerEmptyState(
                        title = "No Products Listed",
                        subtitle = "Begin listing certified healthcare, monitoring, or wellness devices.",
                        actionLabel = "Create First Product",
                        onAction = onCreateProductClick
                    )
                }
            } else {
                items(products) { prod ->
                    SellerProductCard(
                        product = prod,
                        onClick = { onProductClick(prod) },
                        onEditClick = { onEditProductClick(prod) }
                    )
                }
            }
        }
    }
}

// 2. SellerCreateProductPage
@Composable
fun SellerCreateProductPage(
    onSaveDraft: (title: String, desc: String, price: Double, stock: Int, sku: String) -> Unit,
    onSubmitForReview: (title: String, desc: String, price: Double, stock: Int, sku: String) -> Unit,
    onCancel: () -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var stockStr by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                }
                Text("Create New Product", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (errorMsg != null) {
            item {
                SellerErrorState(errorMessage = errorMsg!!)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Product Title *") },
                placeholder = { Text("e.g. Omron Blood Pressure Monitor") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = sku,
                onValueChange = { sku = it },
                label = { Text("SKU (Stock Keeping Unit) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Detailed Description *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Price (SAR) *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { stockStr = it },
                    label = { Text("Initial Stock *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        val p = priceStr.toDoubleOrNull() ?: 0.0
                        val s = stockStr.toIntOrNull() ?: 0
                        onSaveDraft(title, description, p, s, sku)
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = HealthogramTheme.shapes.pill
                ) {
                    Text("Save Draft")
                }
                Button(
                    onClick = {
                        val validation = SellerSecurityAndValidation.validateProductContent(title, description)
                        if (!validation.isValid) {
                            errorMsg = validation.reason
                            return@Button
                        }
                        val p = priceStr.toDoubleOrNull() ?: 0.0
                        val s = stockStr.toIntOrNull() ?: 0
                        if (p <= 0.0) {
                            errorMsg = "Price must be greater than zero."
                            return@Button
                        }
                        onSubmitForReview(title, description, p, s, sku)
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = HealthogramTheme.shapes.pill
                ) {
                    Text("Submit For Review")
                }
            }
        }
    }
}

// 3. SellerEditProductPage
@Composable
fun SellerEditProductPage(
    product: MarketplaceProduct,
    onSave: (MarketplaceProduct) -> Unit,
    onBack: () -> Unit = {}
) {
    var title by remember { mutableStateOf(product.title) }
    var priceStr by remember { mutableStateOf(product.price.toString()) }
    var stockStr by remember { mutableStateOf(product.stock.toString()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Edit Product", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Product Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = priceStr,
            onValueChange = { priceStr = it },
            label = { Text("Selling Price (SAR)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = stockStr,
            onValueChange = { stockStr = it },
            label = { Text("Available Stock Units") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                val p = priceStr.toDoubleOrNull() ?: product.price
                val s = stockStr.toIntOrNull() ?: product.stockQuantity
                onSave(product.copy(title = title, price = p, stockQuantity = s))
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = HealthogramTheme.shapes.pill
        ) {
            Text("Save Changes")
        }
    }
}

// 4. SellerProductPreviewPage
@Composable
fun SellerProductPreviewPage(
    product: MarketplaceProduct,
    onBack: () -> Unit = {}
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text("Customer View Preview", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)).background(HealthogramTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(64.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(product.title, style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))
            Text("${product.price} ${product.currency}", style = HealthogramTheme.typography.h5.copy(fontWeight = FontWeight.ExtraBold), color = HealthogramTheme.colors.primary)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Description", style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold))
            Text(product.description, style = HealthogramTheme.typography.body2, color = HealthogramTheme.colors.textSecondary)
        }
    }
}

// 5. SellerProductStatusPage
@Composable
fun SellerProductStatusPage(
    product: MarketplaceProduct,
    onBack: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            Text("Listing Status Audit", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Surface(
            color = HealthogramTheme.colors.surface,
            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(product.title, style = HealthogramTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                SellerProductStatusBadge(status = product.status.name)
                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = HealthogramTheme.colors.borderLight)
                Spacer(modifier = Modifier.height(14.dp))
                Text("Product ID: ${product.productId}", style = HealthogramTheme.typography.caption)
                Text("SKU: ${product.sku}", style = HealthogramTheme.typography.caption)
                Text("Jurisdiction: ${product.countryCode}", style = HealthogramTheme.typography.caption)
            }
        }
    }
}

// 6. SellerProductCompliancePage
@Composable
fun SellerProductCompliancePage(
    product: MarketplaceProduct,
    onBack: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            Text("Regulatory Compliance Audit", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("SFDA Regulatory Standards", style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Certified for sale under Saudi Food and Drug Authority medical device guidelines (Medical Devices Sector).", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = HealthogramTheme.colors.success)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("No Prohibited Medical Claims Detected", style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.success)
                }
            }
        }
    }
}

// 7. SellerProductImagesPage
@Composable
fun SellerProductImagesPage(
    images: List<String>,
    onUploadImage: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            Text("Product Gallery & Visuals", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(16.dp))
        SellerProductImageUploader(images = images, onUploadClick = onUploadImage)
    }
}

// 8. SellerProductVariantsPage
@Composable
fun SellerProductVariantsPage(onBack: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            Text("Product Variants & Sizes", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Standard Cuff (22-32cm) • Large Cuff (32-42cm)", style = HealthogramTheme.typography.body2)
    }
}

// 9. SellerInventoryPage
@Composable
fun SellerInventoryPage(
    inventoryList: List<MarketplaceInventory>,
    onAdjustStock: (productId: String, delta: Int) -> Unit = { _, _ -> }
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Stock & Inventory Control", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
            Text("Real-time available quantities synchronized with order reservations.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(inventoryList) { inv ->
            SellerInventoryCard(
                inventory = inv,
                onAdjustStock = { delta -> onAdjustStock(inv.productId, delta) }
            )
        }
    }
}

// 10. SellerPricingPage
@Composable
fun SellerPricingPage(
    products: List<MarketplaceProduct>,
    onBack: () -> Unit = {}
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Price Management", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(products) { prod ->
            SellerPriceEditor(
                price = prod.price,
                compareAtPrice = prod.compareAtPrice,
                onPriceChange = {},
                currency = prod.currency
            )
        }
    }
}

// 11. SellerOffersPage
@Composable
fun SellerOffersPage(
    offers: List<MarketplaceSellerOffer>,
    onCreateOffer: () -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateOffer,
                containerColor = HealthogramTheme.colors.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Offer")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("Promotions & Offers", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
                Text("Configure bundle deals, volume discounts, and percentage savings.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(offers) { offer ->
                SellerOfferCard(offer = offer)
            }
        }
    }
}

// 12. SellerFlashSalePage
@Composable
fun SellerFlashSalePage(
    onParticipate: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            Text("Marketplace Flash Sales", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(16.dp))
        SellerFlashSaleCard(
            saleTitle = "National Health & Wellness Flash Week",
            discountPercent = 25.0,
            onParticipate = onParticipate
        )
    }
}
