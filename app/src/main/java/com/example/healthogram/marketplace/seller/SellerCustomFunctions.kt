package com.example.healthogram.marketplace.seller

import java.text.DecimalFormat
import java.util.Locale

/**
 * HEALTHOGRAM — STEP 09: FLUTTERFLOW CUSTOM FUNCTIONS FOR SELLER CENTER
 * UI/Helper functions only — strictly client-side presentation without authoritative financial/security impact.
 */
object SellerCustomFunctions {

    private val currencyFormatter = DecimalFormat("#,##0.00")
    private val integerFormatter = DecimalFormat("#,##0")

    fun formatSellerRevenue(amount: Double, currency: String = "SAR"): String {
        return "${currencyFormatter.format(amount)} $currency"
    }

    fun formatSellerBalance(amount: Double, currency: String = "SAR"): String {
        return "${currencyFormatter.format(amount)} $currency"
    }

    fun getSellerStatusLabel(status: SellerStatus): String {
        return when (status) {
            SellerStatus.PENDING -> "Verification Pending"
            SellerStatus.ACTIVE -> "Active Seller"
            SellerStatus.SUSPENDED -> "Account Suspended"
            SellerStatus.RESTRICTED -> "Account Restricted"
            SellerStatus.CLOSED -> "Account Closed"
        }
    }

    fun getProductStatusLabel(status: String): String {
        return when (status.lowercase(Locale.ROOT)) {
            "draft" -> "Draft"
            "pending_review" -> "Under Review"
            "approved" -> "Approved"
            "active" -> "Live & Active"
            "paused" -> "Paused"
            "rejected" -> "Action Required / Rejected"
            else -> status.replace('_', ' ').capitalize(Locale.ROOT)
        }
    }

    fun getInventoryStatusLabel(status: InventoryStatus): String {
        return when (status) {
            InventoryStatus.IN_STOCK -> "In Stock"
            InventoryStatus.LOW_STOCK -> "Low Stock Warning"
            InventoryStatus.OUT_OF_STOCK -> "Out of Stock"
            InventoryStatus.PAUSED -> "Listing Paused"
        }
    }

    fun getPayoutStatusLabel(status: PayoutStatus): String {
        return when (status) {
            PayoutStatus.REQUESTED -> "Requested"
            PayoutStatus.UNDER_REVIEW -> "Reviewing Compliance"
            PayoutStatus.APPROVED -> "Approved for Payout"
            PayoutStatus.PROCESSING -> "Bank Transfer Processing"
            PayoutStatus.PAID -> "Completed & Settled"
            PayoutStatus.FAILED -> "Transfer Failed"
            PayoutStatus.CANCELLED -> "Cancelled"
        }
    }

    fun getOrderStatusLabel(status: String): String {
        return when (status.uppercase(Locale.ROOT)) {
            "CONFIRMED" -> "Confirmed by Customer"
            "PROCESSING" -> "Processing & Picking"
            "PACKED" -> "Packed & Ready"
            "READY_FOR_SHIPMENT" -> "Ready for Carrier"
            "SHIPPED" -> "In Transit"
            "DELIVERED" -> "Delivered to Customer"
            "CANCELLED_BY_SELLER" -> "Cancelled by Seller"
            else -> status.replace('_', ' ').capitalize(Locale.ROOT)
        }
    }

    fun calculateDisplayedCommission(grossAmount: Double, ratePercent: Double): Double {
        return (grossAmount * (ratePercent / 100.0)).coerceAtLeast(0.0)
    }

    fun calculateStockPercentage(available: Int, lowStockThreshold: Int): Float {
        val totalCapacityEstimate = (lowStockThreshold * 5).coerceAtLeast(20)
        return (available.toFloat() / totalCapacityEstimate.toFloat()).coerceIn(0f, 1f)
    }

    fun getLowStockMessage(available: Int, lowThreshold: Int): String {
        return when {
            available <= 0 -> "Critical: Product is currently out of stock!"
            available <= lowThreshold -> "Warning: Only $available units remaining (below threshold of $lowThreshold)."
            else -> "Stock healthy: $available units available."
        }
    }

    fun getSellerVerificationLabel(status: SellerVerificationStatus): String {
        return when (status) {
            SellerVerificationStatus.NOT_STARTED -> "Not Started"
            SellerVerificationStatus.DRAFT -> "Application in Draft"
            SellerVerificationStatus.SUBMITTED -> "Application Submitted"
            SellerVerificationStatus.UNDER_REVIEW -> "Compliance Under Review"
            SellerVerificationStatus.ADDITIONAL_INFORMATION_REQUIRED -> "Additional Documents Needed"
            SellerVerificationStatus.VERIFIED -> "Verified Seller"
            SellerVerificationStatus.REJECTED -> "Verification Denied"
            SellerVerificationStatus.SUSPENDED -> "Verification Suspended"
            SellerVerificationStatus.EXPIRED -> "Permit Expired"
            SellerVerificationStatus.REVOKED -> "Verification Revoked"
        }
    }
}
