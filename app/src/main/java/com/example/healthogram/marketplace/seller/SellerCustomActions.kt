package com.example.healthogram.marketplace.seller

import com.example.healthogram.marketplace.*
import java.util.UUID

/**
 * HEALTHOGRAM — STEP 09: FLUTTERFLOW CUSTOM ACTIONS FOR SELLER CENTER
 * Encapsulates client-invoked actions coordinating between repository,
 * security screenings, validation rules, and simulated Cloud Function triggers.
 */
object SellerCustomActions {

    suspend fun submitSellerApplication(
        repository: SellerRepository,
        sellerType: SellerType,
        countryCode: String = "SA"
    ): MarketplaceSellerApplication {
        val app = MarketplaceSellerApplication(
            applicationId = "app_${UUID.randomUUID().toString().take(8)}",
            sellerUid = repository.currentSellerUid,
            sellerType = sellerType,
            countryCode = countryCode,
            status = SellerVerificationStatus.SUBMITTED,
            submittedAt = System.currentTimeMillis()
        )
        // Set profile & verification to submitted
        repository.updateProfile(
            repository.sellerProfile.value.copy(
                sellerType = sellerType,
                verificationStatus = SellerVerificationStatus.SUBMITTED,
                sellerStatus = SellerStatus.PENDING
            )
        )
        return app
    }

    suspend fun uploadSellerDocument(
        repository: SellerRepository,
        documentType: SellerDocumentType,
        fileName: String,
        fileSize: Long,
        docNumberLast4: String,
        issuingAuthority: String
    ): MarketplaceSellerDocument {
        val docId = "doc_${UUID.randomUUID().toString().take(8)}"
        val doc = MarketplaceSellerDocument(
            documentId = docId,
            sellerUid = repository.currentSellerUid,
            countryCode = repository.sellerProfile.value.countryCode,
            documentType = documentType,
            storagePath = "marketplace_private/${repository.currentSellerUid}/verification/$docId.pdf",
            fileName = fileName,
            fileSize = fileSize,
            documentNumberLast4 = docNumberLast4,
            issuingAuthority = issuingAuthority,
            status = DocumentStatus.PENDING
        )
        return doc
    }

    suspend fun createSellerProduct(
        repository: SellerRepository,
        title: String,
        description: String,
        categoryId: String,
        price: Double,
        compareAtPrice: Double?,
        stock: Int,
        sku: String,
        tags: List<String>
    ): SellerSecurityAndValidation.ValidationResult {
        val contentValidation = SellerSecurityAndValidation.validateProductContent(title, description, tags)
        if (!contentValidation.isValid) {
            return contentValidation
        }

        val product = MarketplaceProduct(
            productId = "prod_${UUID.randomUUID().toString().take(8)}",
            sellerUid = repository.currentSellerUid,
            sellerStoreName = repository.sellerProfile.value.storeName,
            title = title,
            shortDescription = description.take(120),
            description = description,
            categoryId = categoryId,
            price = price,
            compareAtPrice = compareAtPrice,
            stockQuantity = stock,
            sku = sku,
            countryCode = repository.sellerProfile.value.countryCode,
            currency = repository.sellerProfile.value.currency,
            status = ProductStatus.DRAFT,
            tags = tags
        )

        repository.addProduct(product)
        return SellerSecurityAndValidation.ValidationResult(true)
    }

    suspend fun submitProductForReview(
        repository: SellerRepository,
        productId: String
    ): Boolean {
        val prod = repository.sellerProducts.value.find { it.productId == productId } ?: return false
        if (prod.sellerUid != repository.currentSellerUid) return false

        repository.updateProduct(prod.copy(status = ProductStatus.PENDING_REVIEW))
        return true
    }

    suspend fun updateSellerInventory(
        repository: SellerRepository,
        productId: String,
        delta: Int
    ): Boolean {
        return repository.updateInventoryQuantity(productId, delta)
    }

    suspend fun createSellerOffer(
        repository: SellerRepository,
        productId: String,
        offerType: SellerOfferType,
        value: Double,
        minimumQuantity: Int
    ): MarketplaceSellerOffer {
        val offer = MarketplaceSellerOffer(
            sellerUid = repository.currentSellerUid,
            productId = productId,
            offerType = offerType,
            value = value,
            minimumQuantity = minimumQuantity,
            status = SellerOfferStatus.ACTIVE
        )
        repository.addOffer(offer)
        return offer
    }

    suspend fun scheduleFlashSale(
        repository: SellerRepository,
        productId: String,
        discountPercent: Double,
        durationDays: Int = 3
    ): MarketplaceSellerOffer {
        val offer = MarketplaceSellerOffer(
            sellerUid = repository.currentSellerUid,
            productId = productId,
            offerType = SellerOfferType.LIMITED_TIME,
            value = discountPercent,
            startAt = System.currentTimeMillis(),
            endAt = System.currentTimeMillis() + (86400000L * durationDays),
            status = SellerOfferStatus.SCHEDULED
        )
        repository.addOffer(offer)
        return offer
    }

    suspend fun processSellerOrder(
        repository: SellerRepository,
        orderId: String,
        targetStatus: MarketplaceOrderStatus
    ): SellerSecurityAndValidation.ValidationResult {
        val order = repository.sellerOrders.value.find { it.orderId == orderId }
            ?: return SellerSecurityAndValidation.ValidationResult(false, "Order not found")

        val currentOrderStatus = when (order.orderStatus) {
            "CONFIRMED" -> MarketplaceOrderStatus.CONFIRMED
            "PROCESSING" -> MarketplaceOrderStatus.PROCESSING
            "PACKED" -> MarketplaceOrderStatus.PACKED
            "SHIPPED" -> MarketplaceOrderStatus.SHIPPED
            else -> MarketplaceOrderStatus.CONFIRMED
        }

        val validation = SellerSecurityAndValidation.validateSellerOrderStatusTransition(
            currentStatus = currentOrderStatus,
            targetStatus = targetStatus
        )

        if (!validation.isValid) {
            return validation
        }

        repository.updateOrderStatus(orderId, targetStatus.name)
        return SellerSecurityAndValidation.ValidationResult(true)
    }

    suspend fun requestSellerPayout(
        repository: SellerRepository,
        amount: Double
    ): SellerSecurityAndValidation.ValidationResult {
        val validation = SellerSecurityAndValidation.validatePayoutRequest(
            sellerProfile = repository.sellerProfile.value,
            sellerVerification = repository.sellerVerification.value,
            payoutAccount = repository.payoutAccounts.value.firstOrNull(),
            balance = repository.sellerBalance.value,
            requestedAmount = amount
        )

        if (!validation.isValid) {
            return validation
        }

        val success = repository.submitPayoutRequest(amount)
        return if (success) {
            SellerSecurityAndValidation.ValidationResult(true)
        } else {
            SellerSecurityAndValidation.ValidationResult(false, "Failed to submit payout request")
        }
    }

    fun generateSellerReport(
        reportType: String,
        sellerUid: String,
        format: String = "PDF"
    ): String {
        return "https://storage.healthogram.app/marketplace_private/$sellerUid/reports/report_${reportType.lowercase()}_${System.currentTimeMillis()}.$format"
    }

    fun generateSellerInvoice(
        orderId: String,
        sellerUid: String
    ): String {
        return "https://storage.healthogram.app/marketplace_private/$sellerUid/invoices/inv_seller_${orderId}.pdf"
    }

    suspend fun openSellerSupport(
        repository: SellerRepository,
        subject: String,
        description: String,
        category: SellerSupportCategory
    ): SellerSupportTicket {
        return repository.createSupportTicket(subject, description, category)
    }
}
