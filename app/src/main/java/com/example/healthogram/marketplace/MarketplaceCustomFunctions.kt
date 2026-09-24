package com.example.healthogram.marketplace

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Locale

/**
 * HEALTHOGRAM — STEP 08: FLUTTERFLOW CUSTOM FUNCTIONS (Section 58)
 * Client presentation formatting & helper calculations.
 */
object MarketplaceCustomFunctions {

    /**
     * Formats price with currency awareness (e.g., "149.50 SAR")
     */
    fun formatMarketplacePrice(amount: Double, currency: String = "SAR"): String {
        return String.format(Locale.US, "%.2f %s", amount, currency)
    }

    /**
     * Formats discount badge string (e.g., "25% OFF")
     */
    fun calculateDisplayedDiscount(originalPrice: Double, currentPrice: Double): String {
        if (originalPrice <= 0.0 || currentPrice >= originalPrice) return ""
        val pct = (((originalPrice - currentPrice) / originalPrice) * 100).toInt()
        return "$pct% OFF"
    }

    /**
     * Returns human-readable stock availability status
     */
    fun getProductStockLabel(quantity: Int, lowStockThreshold: Int = 10): String {
        return when {
            quantity <= 0 -> "Out of Stock"
            quantity <= lowStockThreshold -> "Only $quantity left in stock"
            else -> "In Stock ($quantity available)"
        }
    }

    /**
     * Formats human-readable order status label
     */
    fun getOrderStatusLabel(status: MarketplaceOrderStatus): String {
        return status.label
    }

    /**
     * Formats human-readable payment status label
     */
    fun getPaymentStatusLabel(status: PaymentStatus): String {
        return status.label
    }

    /**
     * Formats delivery status label
     */
    fun getDeliveryStatusLabel(status: String): String {
        return status.replace("_", " ").split(" ")
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }

    /**
     * Formats return status label
     */
    fun getReturnStatusLabel(status: ReturnStatus): String {
        return status.label
    }

    /**
     * Resolves Material icon for marketplace category
     */
    fun getMarketplaceCategoryIcon(categoryId: String): ImageVector {
        return when (categoryId.lowercase()) {
            "medical_equipment", "diagnostic_equipment", "patient_monitoring" -> Icons.Default.MedicalServices
            "health_monitors", "smart_health_monitors" -> Icons.Default.MonitorHeart
            "wellness_and_vitamins", "personal_care" -> Icons.Default.Spa
            "first_aid", "emergency_equipment" -> Icons.Default.Healing
            "fitness_and_rehab", "hospital_furniture" -> Icons.Default.FitnessCenter
            "clinical_supplies" -> Icons.Default.Biotech
            else -> Icons.Default.LocalHospital
        }
    }

    /**
     * Validates if a deal is currently within its active time window
     */
    fun isDealActive(deal: MarketplaceDeal): Boolean {
        val now = System.currentTimeMillis()
        return deal.active && now in deal.startAt..deal.endAt
    }

    /**
     * Validates if a flash sale is active and has available units
     */
    fun isFlashSaleActive(flashSale: MarketplaceFlashSale): Boolean {
        val now = System.currentTimeMillis()
        return flashSale.status == FlashSaleStatus.ACTIVE &&
                now in flashSale.startAt..flashSale.endAt &&
                flashSale.quantitySold < flashSale.quantityLimit
    }

    /**
     * Computes the total quantity of items in the cart
     */
    fun getCartItemCount(items: List<MarketplaceCartItem>): Int {
        return items.sumOf { it.quantity }
    }
}
