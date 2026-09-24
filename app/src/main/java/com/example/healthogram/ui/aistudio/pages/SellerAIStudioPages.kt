package com.example.healthogram.ui.aistudio.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.aistudio.*
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.ui.aistudio.components.*
import kotlinx.coroutines.launch

@Composable
fun SellerAIStudioPage(
    onNavigateToSellerTool: (AIToolType) -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("seller_ai_studio_page"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AIStudioHeader(
                title = "Seller AI Studio",
                subtitle = "Product catalog optimization, copywriting & compliant marketing",
                onBack = onBack
            )
        }

        item {
            Surface(
                shape = HealthogramTheme.shapes.medium,
                color = HealthogramTheme.colors.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, HealthogramTheme.colors.warning)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = HealthogramTheme.colors.warning,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Marketplace Compliance Mandate: AI Studio will NEVER generate uncertified clinical cures, fake FDA/SFDA approvals, or synthetic medical licenses. All specifications must match physical documentation.",
                        style = HealthogramTheme.typography.caption,
                        color = HealthogramTheme.colors.textPrimary
                    )
                }
            }
        }

        item {
            Text(
                text = "Product Catalog Generators",
                style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            AISellerToolCard(
                title = "Product Title Optimizer",
                description = "Craft high-converting, policy-compliant e-commerce titles with essential specs.",
                icon = Icons.Default.Title,
                onClick = { onNavigateToSellerTool(AIToolType.PRODUCT_TITLE) }
            )
        }

        item {
            AISellerToolCard(
                title = "Compliant Description & Specs Writer",
                description = "Generate clear product descriptions, ergonomic highlights, and user safety guides.",
                icon = Icons.Default.Description,
                onClick = { onNavigateToSellerTool(AIToolType.PRODUCT_DESCRIPTION) }
            )
        }

        item {
            AISellerToolCard(
                title = "Marketplace Search Tags & Keywords",
                description = "Extract targeted medical equipment & wellness search terms.",
                icon = Icons.Default.Tag,
                onClick = { onNavigateToSellerTool(AIToolType.PRODUCT_TAGS) }
            )
        }

        item {
            Text(
                text = "Visuals & Marketing Banners",
                style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            AISellerToolCard(
                title = "E-Commerce Photo Enhancer & Studio Backdrop",
                description = "Isolate products and render clean white studio backdrops with soft floor shadows.",
                icon = Icons.Default.CameraAlt,
                onClick = { onNavigateToSellerTool(AIToolType.PRODUCT_IMAGE_ENHANCE) }
            )
        }

        item {
            AISellerToolCard(
                title = "Promotional Banner & Campaign Copy",
                description = "Draft headlines and banner copy for seasonal health storefront events.",
                icon = Icons.Default.Campaign,
                onClick = { onNavigateToSellerTool(AIToolType.PROMOTIONAL_BANNER) }
            )
        }

        item {
            AISellerToolCard(
                title = "Product Video & Showcase Assistant",
                description = "Generate 30-second product demonstration outlines, hooks & feature walkthroughs.",
                icon = Icons.Default.VideoCall,
                onClick = { onNavigateToSellerTool(AIToolType.PRODUCT_VIDEO_ASSISTANCE) }
            )
        }
    }
}

@Composable
fun SellerAIProductTitlePage(
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var brand by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Medical Equipment & Devices") }
    var specs by remember { mutableStateOf("") }

    var result by remember { mutableStateOf<AIProductTitleResult?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("seller_ai_product_title_page"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AIStudioHeader(
                title = "Product Title Optimizer",
                subtitle = "Compliant e-commerce titles with specifications",
                onBack = onBack
            )
        }

        item {
            HealthogramTextField(
                value = brand,
                onValueChange = { brand = it },
                placeholder = "Brand Name (e.g. Omron, Apex Medical, MedTech)..."
            )
        }

        item {
            HealthogramTextField(
                value = productName,
                onValueChange = { productName = it },
                placeholder = "Base Product Name (e.g. Digital Blood Pressure Monitor)..."
            )
        }

        item {
            HealthogramTextField(
                value = specs,
                onValueChange = { specs = it },
                placeholder = "Key Specs (e.g. Upper Arm, Bluetooth Sync, Large Cuff)..."
            )
        }

        item {
            HealthogramPrimaryButton(
                text = "Generate Optimized Title",
                icon = Icons.Default.AutoAwesome,
                onClick = {
                    if (productName.isBlank()) return@HealthogramPrimaryButton
                    coroutineScope.launch {
                        isGenerating = true
                        errorMessage = null
                        try {
                            result = AIStudioCustomActions.generateProductTitle(
                                brand = brand,
                                productName = productName,
                                category = category,
                                specs = specs
                            )
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to generate title"
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                state = if (isGenerating) ButtonState.LOADING else ButtonState.NORMAL,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (errorMessage != null) {
            item {
                AIErrorCard(message = errorMessage!!, onRetry = { errorMessage = null })
            }
        }

        if (result != null) {
            item {
                AIResultPreview(
                    title = "Primary Title Recommendation",
                    content = result!!.suggestedTitle,
                    hashtags = result!!.keywords
                )
            }

            if (result!!.alternativeTitles.isNotEmpty()) {
                item {
                    HealthogramBasicCard {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Alternative Titles", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(8.dp))
                            result!!.alternativeTitles.forEach { alt ->
                                Text("• $alt", style = HealthogramTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }

            item {
                AIResultActionBar(
                    onCopy = {},
                    onUseInPost = {},
                    onRegenerate = {}
                )
            }
        }
    }
}

@Composable
fun SellerAIProductDescriptionPage(
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Orthopedic Support") }
    var specs by remember { mutableStateOf("") }
    var features by remember { mutableStateOf("") }

    var result by remember { mutableStateOf<AIProductDescriptionResult?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("seller_ai_product_description_page"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AIStudioHeader(
                title = "Product Description & Specs Writer",
                subtitle = "Compliant, detailed e-commerce product copy",
                onBack = onBack
            )
        }

        item {
            HealthogramTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = "Product Title (e.g. Ergonomic Lumbar Back Support Cushion)..."
            )
        }

        item {
            HealthogramTextField(
                value = specs,
                onValueChange = { specs = it },
                placeholder = "Material & Dimensions (e.g. High-density Memory Foam, 45x40x12 cm)..."
            )
        }

        item {
            HealthogramTextField(
                value = features,
                onValueChange = { features = it },
                placeholder = "Key features (e.g. Breathable mesh cover, dual adjustable straps)..."
            )
        }

        item {
            HealthogramPrimaryButton(
                text = "Draft Compliant Description",
                icon = Icons.Default.Description,
                onClick = {
                    if (title.isBlank()) return@HealthogramPrimaryButton
                    coroutineScope.launch {
                        isGenerating = true
                        errorMessage = null
                        try {
                            result = AIStudioCustomActions.generateProductDescription(
                                title = title,
                                category = category,
                                specs = specs,
                                features = features
                            )
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to generate description"
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                state = if (isGenerating) ButtonState.LOADING else ButtonState.NORMAL,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (errorMessage != null) {
            item {
                AIErrorCard(message = errorMessage!!, onRetry = { errorMessage = null })
            }
        }

        if (result != null) {
            item {
                HealthogramBasicCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Detailed Description", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(result!!.detailedDescription, style = HealthogramTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Bullet Points for Listing", style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        result!!.bulletPoints.forEach { bullet ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• $bullet", style = HealthogramTheme.typography.caption)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Usage & Regulatory Notice", style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Text(result!!.usageInformation, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                    }
                }
            }

            item {
                AIResultActionBar(
                    onCopy = {},
                    onUseInPost = {},
                    onRegenerate = {}
                )
            }
        }
    }
}

@Composable
fun SellerAIProductTagsPage(
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var specs by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<AIProductTagsResult?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("seller_ai_product_tags_page"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AIStudioHeader(
                title = "Product Search Tags Generator",
                subtitle = "Generate high-relevance search keywords",
                onBack = onBack
            )
        }

        item {
            HealthogramTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = "Product Name..."
            )
        }

        item {
            HealthogramPrimaryButton(
                text = "Curate Search Tags",
                icon = Icons.Default.Tag,
                onClick = {
                    if (title.isBlank()) return@HealthogramPrimaryButton
                    coroutineScope.launch {
                        isGenerating = true
                        result = AIStudioCustomActions.generateProductTags(title = title, category = "Medical", specs = specs)
                        isGenerating = false
                    }
                },
                state = if (isGenerating) ButtonState.LOADING else ButtonState.NORMAL,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (result != null) {
            item {
                AIResultPreview(
                    title = "Suggested Tags & Keywords",
                    content = result!!.tags.joinToString(", "),
                    hashtags = result!!.searchKeywords
                )
            }
        }
    }
}

@Composable
fun SellerAIMarketingCreativePage(
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var productName by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("Seasonal Wellness Campaign") }
    var discount by remember { mutableStateOf("20") }
    var result by remember { mutableStateOf<AIMarketingCreativeResult?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("seller_ai_marketing_creative_page"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AIStudioHeader(
                title = "Marketing Banner & Copy Studio",
                subtitle = "High-converting storefront promotional copy",
                onBack = onBack
            )
        }

        item {
            HealthogramTextField(
                value = productName,
                onValueChange = { productName = it },
                placeholder = "Featured Product or Category..."
            )
        }

        item {
            HealthogramTextField(
                value = discount,
                onValueChange = { discount = it },
                placeholder = "Discount percentage (optional, e.g. 25)..."
            )
        }

        item {
            HealthogramPrimaryButton(
                text = "Generate Campaign Copy",
                icon = Icons.Default.Campaign,
                onClick = {
                    if (productName.isBlank()) return@HealthogramPrimaryButton
                    coroutineScope.launch {
                        isGenerating = true
                        result = AIStudioCustomActions.generateMarketingCreative(
                            productName = productName,
                            promotionGoal = goal,
                            discountPercent = discount
                        )
                        isGenerating = false
                    }
                },
                state = if (isGenerating) ButtonState.LOADING else ButtonState.NORMAL,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (result != null) {
            item {
                HealthogramBasicCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(result!!.headline, style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(result!!.bannerText, style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(result!!.copyText, style = HealthogramTheme.typography.caption)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Button CTA: ${result!!.callToAction}", style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.secondary)
                    }
                }
            }
        }
    }
}

@Composable
fun SellerAIProductImagePage(
    onBack: () -> Unit
) {
    AIStudioImageEditorPage(onBack = onBack)
}

@Composable
fun SellerAIProductVideoPage(
    onBack: () -> Unit
) {
    AIStudioVideoEditorPage(onBack = onBack)
}
