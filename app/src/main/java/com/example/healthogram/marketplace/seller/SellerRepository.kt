package com.example.healthogram.marketplace.seller

import com.example.healthogram.marketplace.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * HEALTHOGRAM — STEP 09: SELLER IN-MEMORY REACTIVE REPOSITORY
 * Provides real-time reactive StateFlows for the Seller Center, isolating
 * seller-specific catalog items, orders, financial ledger entries, balance,
 * inventory movements, documents, and support tickets.
 */
class SellerRepository(
    val currentSellerUid: String = DEFAULT_SELLER_UID
) {
    companion object {
        const val DEFAULT_SELLER_UID = "seller_healthogram_riyadh"
    }

    private val isDefaultSeller: Boolean = currentSellerUid == DEFAULT_SELLER_UID

    // 1. Seller Profile
    private val _sellerProfile = MutableStateFlow(
        MarketplaceSellerProfile(
            sellerUid = currentSellerUid,
            sellerType = SellerType.BUSINESS_SELLER,
            legalName = "Al-Shifa Healthcare Solutions Trading Est.",
            storeName = "Al-Shifa Health & Medical Store",
            storeSlug = "al-shifa-health",
            logoUrl = "https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=300",
            coverImageUrl = "https://images.unsplash.com/photo-1505751172876-fa1923c5c528?w=800",
            description = "Authorized Saudi distributor of certified home medical devices, orthotics, ergonomic healthcare supplies, and first aid equipment.",
            countryCode = "SA",
            currency = "SAR",
            email = "seller@alshifa-health.sa",
            phone = "+966 11 445 8899",
            businessAddress = "King Fahd Road, Al Olaya District, Riyadh, Kingdom of Saudi Arabia",
            verificationStatus = SellerVerificationStatus.VERIFIED,
            sellerStatus = SellerStatus.ACTIVE,
            storeStatus = StoreStatus.ACTIVE,
            ratingAverage = 4.88,
            reviewCount = 142,
            totalOrders = 389,
            totalProducts = 6
        )
    )
    val sellerProfile: StateFlow<MarketplaceSellerProfile> = _sellerProfile.asStateFlow()

    // 2. Seller Verification & Documents
    private val _sellerVerification = MutableStateFlow(
        MarketplaceSellerVerification(
            sellerUid = currentSellerUid,
            sellerType = SellerType.BUSINESS_SELLER,
            countryCode = "SA",
            status = SellerVerificationStatus.VERIFIED,
            applicationId = "app_sa_99812",
            submittedAt = System.currentTimeMillis() - 86400000L * 30,
            reviewedAt = System.currentTimeMillis() - 86400000L * 28,
            reviewerUid = "admin_compliance_sa_01",
            verifiedAt = System.currentTimeMillis() - 86400000L * 28,
            expiresAt = System.currentTimeMillis() + 86400000L * 335
        )
    )
    val sellerVerification: StateFlow<MarketplaceSellerVerification> = _sellerVerification.asStateFlow()

    private val _sellerDocuments = MutableStateFlow(
        if (isDefaultSeller) {
            listOf(
                MarketplaceSellerDocument(
                documentId = "doc_cr_01",
                sellerUid = currentSellerUid,
                applicationId = "app_sa_99812",
                countryCode = "SA",
                documentType = SellerDocumentType.COMMERCIAL_REGISTRATION,
                storagePath = "marketplace_private/$currentSellerUid/verification/doc_cr_01.pdf",
                fileName = "Commercial_Registration_AlShifa_1010.pdf",
                fileSize = 1048576L,
                documentNumberLast4 = "1010",
                issuedDate = "2024-01-15",
                expiryDate = "2027-01-14",
                issuingAuthority = "Ministry of Commerce, KSA",
                status = DocumentStatus.APPROVED
            ),
            MarketplaceSellerDocument(
                documentId = "doc_vat_02",
                sellerUid = currentSellerUid,
                applicationId = "app_sa_99812",
                countryCode = "SA",
                documentType = SellerDocumentType.TAX_CERTIFICATE,
                storagePath = "marketplace_private/$currentSellerUid/verification/doc_vat_02.pdf",
                fileName = "ZATCA_VAT_Certificate.pdf",
                fileSize = 524288L,
                documentNumberLast4 = "3001",
                issuedDate = "2024-02-01",
                expiryDate = "2026-02-01",
                issuingAuthority = "ZATCA Tax Authority",
                status = DocumentStatus.APPROVED
            ),
            MarketplaceSellerDocument(
                documentId = "doc_permit_03",
                sellerUid = currentSellerUid,
                applicationId = "app_sa_99812",
                countryCode = "SA",
                documentType = SellerDocumentType.HEALTHCARE_SELLER_PERMIT,
                storagePath = "marketplace_private/$currentSellerUid/verification/doc_permit_03.pdf",
                fileName = "SFDA_Medical_Devices_Establishment_License.pdf",
                fileSize = 2097152L,
                documentNumberLast4 = "8821",
                issuedDate = "2024-03-10",
                expiryDate = "2027-03-09",
                issuingAuthority = "Saudi Food and Drug Authority (SFDA)",
                status = DocumentStatus.APPROVED
            )
        )
        } else {
            emptyList()
        }
    )
    val sellerDocuments: StateFlow<List<MarketplaceSellerDocument>> = _sellerDocuments.asStateFlow()

    // 3. Products Owned by Seller
    private val _sellerProducts = MutableStateFlow(
        if (isDefaultSeller) {
            listOf(
                MarketplaceProduct(
                productId = "prod_bp_monitor_01",
                sellerUid = currentSellerUid,
                title = "Omron Evolv Wireless Blood Pressure Monitor",
                shortDescription = "Clinically validated upper arm Bluetooth blood pressure monitor.",
                description = "Compact, one-piece blood pressure monitor with no tubes or wires. Features Bluetooth synchronization and Intellisense cuff technology for clinical-grade precision.",
                categoryId = "cat_monitors",
                price = 389.0,
                compareAtPrice = 450.0,
                stockQuantity = 45,
                sku = "OMR-EV-01",
                countryCode = "SA",
                currency = "SAR",
                status = ProductStatus.ACTIVE,
                isFeatured = true,
                ratingAverage = 4.8,
                reviewCount = 94,
                images = listOf(
                    "https://images.unsplash.com/photo-1576091160550-2173dba999ef?w=600",
                    "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=600"
                ),
                specifications = mapOf(
                    "Display" to "OLED High-Contrast Screen",
                    "Connectivity" to "Bluetooth Low Energy",
                    "Cuff Size" to "22 - 42 cm (Standard to Large)",
                    "Battery" to "4x AAA included"
                ),
                tags = listOf("blood pressure", "vital signs", "bluetooth", "sfda approved")
            ),
            MarketplaceProduct(
                productId = "prod_pulse_ox_02",
                sellerUid = currentSellerUid,
                title = "Beurer Fingertip Pulse Oximeter PO 30",
                shortDescription = "Certified medical SpO2 and pulse heart rate monitor.",
                description = "High precision optical photoplethysmography sensor for determining arterial oxygen saturation (SpO2) and heart rate. Suitable for sports and medical monitoring.",
                categoryId = "cat_monitors",
                price = 149.0,
                compareAtPrice = 189.0,
                stockQuantity = 12,
                sku = "BEU-PO-30",
                countryCode = "SA",
                currency = "SAR",
                status = ProductStatus.ACTIVE,
                ratingAverage = 4.7,
                reviewCount = 58,
                images = listOf("https://images.unsplash.com/photo-1584515979956-d9f6e5d09982?w=600"),
                specifications = mapOf("Accuracy" to "SpO2: ±2%", "Display" to "Color Graphic Display"),
                tags = listOf("oximeter", "oxygen", "pulse", "vital signs")
            ),
            MarketplaceProduct(
                productId = "prod_thermo_03",
                sellerUid = currentSellerUid,
                title = "Braun ThermoScan 7 Infrared Ear Thermometer",
                shortDescription = "Age Precision infrared ear thermometer with pre-warmed tip.",
                description = "The gold standard in clinical ear thermometry with Age Precision fever guidance, ExacTemp positioning system, and disposable lens filters for optimal hygiene.",
                categoryId = "cat_monitors",
                price = 289.0,
                compareAtPrice = 320.0,
                stockQuantity = 4, // Low stock for dashboard testing!
                sku = "BRN-TS-07",
                countryCode = "SA",
                currency = "SAR",
                status = ProductStatus.ACTIVE,
                ratingAverage = 4.9,
                reviewCount = 112,
                images = listOf("https://images.unsplash.com/photo-1588776814546-1ffcf47267a5?w=600"),
                tags = listOf("thermometer", "fever", "pediatric", "infrared")
            ),
            MarketplaceProduct(
                productId = "prod_first_aid_04",
                sellerUid = currentSellerUid,
                title = "Comprehensive Workplace & Home First Aid Kit",
                shortDescription = "180-piece emergency trauma & sterile wound care kit.",
                description = "Heavy duty water-resistant case containing sterile gauze pads, medical tape, shears, antiseptic wipes, burn dressings, and CPR face shield.",
                categoryId = "cat_first_aid",
                price = 129.0,
                compareAtPrice = 160.0,
                stockQuantity = 85,
                sku = "MED-FAK-180",
                countryCode = "SA",
                currency = "SAR",
                status = ProductStatus.ACTIVE,
                ratingAverage = 4.6,
                reviewCount = 37,
                images = listOf("https://images.unsplash.com/photo-1603398938378-e54eab446dde?w=600"),
                tags = listOf("first aid", "trauma", "emergency", "bandages")
            ),
            MarketplaceProduct(
                productId = "prod_draft_nebulizer",
                sellerUid = currentSellerUid,
                title = "Portable Mesh Nebulizer Inhaler Pro",
                shortDescription = "Silent ultrasonic vibrating mesh nebulizer for respiratory therapy.",
                description = "Portable respiratory device with micro-pore spray tech producing particles under 3.7 microns for targeted absorption.",
                categoryId = "cat_med_equip",
                price = 199.0,
                compareAtPrice = 240.0,
                stockQuantity = 30,
                sku = "NEB-MESH-01",
                countryCode = "SA",
                currency = "SAR",
                status = ProductStatus.DRAFT,
                ratingAverage = 0.0,
                reviewCount = 0,
                images = listOf("https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=600"),
                tags = listOf("nebulizer", "respiratory", "inhaler")
            ),
            MarketplaceProduct(
                productId = "prod_review_lumbar",
                sellerUid = currentSellerUid,
                title = "Medical Ergonomic Lumbar Support Spine Orthosis",
                shortDescription = "Breathable double-pull compression brace with steel stays.",
                description = "Orthopedic lower back brace designed for disc herniation and postural stabilization. Built with dual adjustable compression straps.",
                categoryId = "cat_fitness_rehab",
                price = 210.0,
                compareAtPrice = 260.0,
                stockQuantity = 25,
                sku = "ORTHO-LUMB-02",
                countryCode = "SA",
                currency = "SAR",
                status = ProductStatus.PENDING_REVIEW,
                ratingAverage = 0.0,
                reviewCount = 0,
                images = listOf("https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=600"),
                tags = listOf("orthopedic", "lumbar", "spine", "rehabilitation")
            )
        )
        } else {
            emptyList()
        }
    )
    val sellerProducts: StateFlow<List<MarketplaceProduct>> = _sellerProducts.asStateFlow()

    // 4. Inventory List
    private val _sellerInventory = MutableStateFlow(
        if (isDefaultSeller) {
            listOf(
                MarketplaceInventory(
                sellerUid = currentSellerUid,
                productId = "prod_bp_monitor_01",
                sku = "OMR-EV-01",
                availableQuantity = 45,
                reservedQuantity = 3,
                soldQuantity = 120,
                lowStockThreshold = 10,
                inventoryStatus = InventoryStatus.IN_STOCK
            ),
            MarketplaceInventory(
                sellerUid = currentSellerUid,
                productId = "prod_pulse_ox_02",
                sku = "BEU-PO-30",
                availableQuantity = 12,
                reservedQuantity = 2,
                soldQuantity = 85,
                lowStockThreshold = 10,
                inventoryStatus = InventoryStatus.IN_STOCK
            ),
            MarketplaceInventory(
                sellerUid = currentSellerUid,
                productId = "prod_thermo_03",
                sku = "BRN-TS-07",
                availableQuantity = 4,
                reservedQuantity = 1,
                soldQuantity = 140,
                lowStockThreshold = 10,
                inventoryStatus = InventoryStatus.LOW_STOCK
            ),
            MarketplaceInventory(
                sellerUid = currentSellerUid,
                productId = "prod_first_aid_04",
                sku = "MED-FAK-180",
                availableQuantity = 85,
                reservedQuantity = 0,
                soldQuantity = 44,
                lowStockThreshold = 15,
                inventoryStatus = InventoryStatus.IN_STOCK
            )
        )
        } else {
            emptyList()
        }
    )
    val sellerInventory: StateFlow<List<MarketplaceInventory>> = _sellerInventory.asStateFlow()

    // 5. Authoritative Financial Balance
    private val _sellerBalance = MutableStateFlow(
        if (isDefaultSeller) {
            MarketplaceSellerBalance(
                sellerUid = currentSellerUid,
                currency = "SAR",
                grossSales = 148500.0,
                refunds = 2490.0,
                fees = 11880.0,
                taxes = 0.0,
                pendingBalance = 4320.0,
                availableBalance = 31250.0,
                paidOut = 98560.0
            )
        } else {
            MarketplaceSellerBalance(
                sellerUid = currentSellerUid,
                currency = "SAR",
                grossSales = 0.0,
                refunds = 0.0,
                fees = 0.0,
                taxes = 0.0,
                pendingBalance = 0.0,
                availableBalance = 0.0,
                paidOut = 0.0
            )
        }
    )
    val sellerBalance: StateFlow<MarketplaceSellerBalance> = _sellerBalance.asStateFlow()

    // 6. Append-Oriented Financial Ledger
    private val _sellerLedger = MutableStateFlow(
        if (isDefaultSeller) {
            listOf(
                MarketplaceSellerLedgerEntry(
                ledgerId = "ledg_001",
                sellerUid = currentSellerUid,
                orderId = "ord_demo_101",
                transactionType = LedgerTransactionType.SALE,
                grossAmount = 389.0,
                commission = 31.12,
                paymentFee = 6.84,
                refundAmount = 0.0,
                adjustment = 0.0,
                taxAmount = 0.0,
                netAmount = 351.04,
                currency = "SAR",
                status = LedgerStatus.SETTLED,
                createdAt = System.currentTimeMillis() - 86400000L * 2
            ),
            MarketplaceSellerLedgerEntry(
                ledgerId = "ledg_002",
                sellerUid = currentSellerUid,
                orderId = "ord_demo_102",
                transactionType = LedgerTransactionType.SALE,
                grossAmount = 538.0,
                commission = 43.04,
                paymentFee = 9.07,
                refundAmount = 0.0,
                adjustment = 0.0,
                taxAmount = 0.0,
                netAmount = 485.89,
                currency = "SAR",
                status = LedgerStatus.SETTLED,
                createdAt = System.currentTimeMillis() - 86400000L * 1
            ),
            MarketplaceSellerLedgerEntry(
                ledgerId = "ledg_003",
                sellerUid = currentSellerUid,
                orderId = "ord_demo_099",
                transactionType = LedgerTransactionType.REFUND,
                grossAmount = 0.0,
                commission = -11.92,
                paymentFee = 0.0,
                refundAmount = 149.0,
                adjustment = 0.0,
                taxAmount = 0.0,
                netAmount = -137.08,
                currency = "SAR",
                status = LedgerStatus.SETTLED,
                createdAt = System.currentTimeMillis() - 86400000L * 3
            ),
            MarketplaceSellerLedgerEntry(
                ledgerId = "ledg_004",
                sellerUid = currentSellerUid,
                transactionType = LedgerTransactionType.PAYOUT,
                grossAmount = 0.0,
                commission = 0.0,
                paymentFee = 0.0,
                refundAmount = 0.0,
                adjustment = 0.0,
                taxAmount = 0.0,
                netAmount = -15000.0,
                currency = "SAR",
                status = LedgerStatus.SETTLED,
                createdAt = System.currentTimeMillis() - 86400000L * 7
            )
        )
        } else {
            emptyList()
        }
    )
    val sellerLedger: StateFlow<List<MarketplaceSellerLedgerEntry>> = _sellerLedger.asStateFlow()

    // 7. Seller Orders (Safe seller-view with zero clinical data)
    private val _sellerOrders = MutableStateFlow(
        if (isDefaultSeller) {
            listOf(
                SellerOrderView(
                orderId = "ord_demo_101",
                orderNumber = "HG-ORD-89210",
                orderDate = System.currentTimeMillis() - 3600000L * 4,
                orderStatus = "CONFIRMED",
                paymentStatus = "PAID",
                recipientName = "Faisal Al-Otaibi",
                deliveryAddressLine = "Al Olaya District, 7234 King Fahd Branch Rd",
                deliveryCity = "Riyadh",
                customerNotes = "Please deliver before 4 PM",
                items = listOf(
                    SellerOrderItemView(
                        itemId = "item_01",
                        productId = "prod_bp_monitor_01",
                        productTitle = "Omron Evolv Wireless Blood Pressure Monitor",
                        productSku = "OMR-EV-01",
                        unitPrice = 389.0,
                        quantity = 1,
                        totalPrice = 389.0
                    )
                ),
                subtotal = 389.0,
                platformCommission = 31.12,
                sellerNetEarnings = 351.04
            ),
            SellerOrderView(
                orderId = "ord_demo_102",
                orderNumber = "HG-ORD-89211",
                orderDate = System.currentTimeMillis() - 3600000L * 12,
                orderStatus = "PROCESSING",
                paymentStatus = "PAID",
                recipientName = "Noura Al-Dosari",
                deliveryAddressLine = "An Nakheel District, Northern Ring Rd",
                deliveryCity = "Riyadh",
                customerNotes = "Leave with reception if not home",
                items = listOf(
                    SellerOrderItemView(
                        itemId = "item_02",
                        productId = "prod_pulse_ox_02",
                        productTitle = "Beurer Fingertip Pulse Oximeter PO 30",
                        productSku = "BEU-PO-30",
                        unitPrice = 149.0,
                        quantity = 1,
                        totalPrice = 149.0
                    ),
                    SellerOrderItemView(
                        itemId = "item_03",
                        productId = "prod_thermo_03",
                        productTitle = "Braun ThermoScan 7 Infrared Ear Thermometer",
                        productSku = "BRN-TS-07",
                        unitPrice = 289.0,
                        quantity = 1,
                        totalPrice = 289.0
                    )
                ),
                subtotal = 438.0,
                platformCommission = 35.04,
                sellerNetEarnings = 394.20
            ),
            SellerOrderView(
                orderId = "ord_demo_103",
                orderNumber = "HG-ORD-89195",
                orderDate = System.currentTimeMillis() - 86400000L * 2,
                orderStatus = "PACKED",
                paymentStatus = "PAID",
                recipientName = "Mohammed Al-Zahrani",
                deliveryAddressLine = "Al Rawdah District, Khurais Rd",
                deliveryCity = "Riyadh",
                items = listOf(
                    SellerOrderItemView(
                        itemId = "item_04",
                        productId = "prod_first_aid_04",
                        productTitle = "Comprehensive Workplace & Home First Aid Kit",
                        productSku = "MED-FAK-180",
                        unitPrice = 129.0,
                        quantity = 2,
                        totalPrice = 258.0
                    )
                ),
                subtotal = 258.0,
                platformCommission = 20.64,
                sellerNetEarnings = 232.20
            ),
            SellerOrderView(
                orderId = "ord_demo_104",
                orderNumber = "HG-ORD-89150",
                orderDate = System.currentTimeMillis() - 86400000L * 4,
                orderStatus = "READY_FOR_SHIPMENT",
                paymentStatus = "PAID",
                recipientName = "Abdullah Al-Ghamdi",
                deliveryAddressLine = "Al Malqa District, Anas Ibn Malik Rd",
                deliveryCity = "Riyadh",
                items = listOf(
                    SellerOrderItemView(
                        itemId = "item_05",
                        productId = "prod_bp_monitor_01",
                        productTitle = "Omron Evolv Wireless Blood Pressure Monitor",
                        productSku = "OMR-EV-01",
                        unitPrice = 389.0,
                        quantity = 1,
                        totalPrice = 389.0
                    )
                ),
                subtotal = 389.0,
                platformCommission = 31.12,
                sellerNetEarnings = 351.04
            )
        )
        } else {
            emptyList()
        }
    )
    val sellerOrders: StateFlow<List<SellerOrderView>> = _sellerOrders.asStateFlow()

    // 8. Payout Accounts & Requests
    private val _payoutAccounts = MutableStateFlow(
        if (isDefaultSeller) {
            listOf(
                MarketplaceSellerPayoutAccount(
                    payoutAccountId = "payout_acc_01",
                    sellerUid = currentSellerUid,
                    countryCode = "SA",
                    provider = "Saudi National Bank (SNB)",
                    accountType = "Corporate IBAN",
                    maskedDestination = "SA82 1000 0001 •••• •••• 4912",
                    verificationStatus = "verified",
                    isDefault = true
                )
            )
        } else {
            emptyList()
        }
    )
    val payoutAccounts: StateFlow<List<MarketplaceSellerPayoutAccount>> = _payoutAccounts.asStateFlow()

    private val _payoutRequests = MutableStateFlow(
        if (isDefaultSeller) {
            listOf(
                MarketplaceSellerPayoutRequest(
                    requestId = "pay_req_01",
                    sellerUid = currentSellerUid,
                    amount = 15000.0,
                    currency = "SAR",
                    payoutAccountId = "payout_acc_01",
                    status = PayoutStatus.PAID,
                    requestedAt = System.currentTimeMillis() - 86400000L * 7,
                    approvedAt = System.currentTimeMillis() - 86400000L * 6,
                    processedAt = System.currentTimeMillis() - 86400000L * 5,
                    providerPayoutId = "SAR-TRF-994812"
                )
            )
        } else {
            emptyList()
        }
    )
    val payoutRequests: StateFlow<List<MarketplaceSellerPayoutRequest>> = _payoutRequests.asStateFlow()

    // 9. Seller Offers & Flash Sales
    private val _sellerOffers = MutableStateFlow(
        if (isDefaultSeller) {
            listOf(
                MarketplaceSellerOffer(
                    offerId = "offer_01",
                    sellerUid = currentSellerUid,
                    productId = "prod_bp_monitor_01",
                    offerType = SellerOfferType.PERCENTAGE_DISCOUNT,
                    value = 15.0, // 15% off
                    minimumQuantity = 1,
                    startAt = System.currentTimeMillis() - 86400000L,
                    endAt = System.currentTimeMillis() + 86400000L * 6,
                    status = SellerOfferStatus.ACTIVE
                ),
                MarketplaceSellerOffer(
                    offerId = "offer_02",
                    sellerUid = currentSellerUid,
                    productId = "prod_first_aid_04",
                    offerType = SellerOfferType.BUY_X_GET_Y,
                    value = 1.0, // Buy 2 Get 1 Free
                    minimumQuantity = 2,
                    status = SellerOfferStatus.SCHEDULED
                )
            )
        } else {
            emptyList()
        }
    )
    val sellerOffers: StateFlow<List<MarketplaceSellerOffer>> = _sellerOffers.asStateFlow()

    // 10. Support Tickets
    private val _sellerSupportTickets = MutableStateFlow(
        if (isDefaultSeller) {
            listOf(
                SellerSupportTicket(
                    ticketId = "ticket_sel_01",
                    sellerUid = currentSellerUid,
                    category = SellerSupportCategory.PRODUCT,
                    subject = "Request expedited review for Lumbar Spine Orthosis listing",
                    description = "We have uploaded the SFDA medical device certificate for product prod_review_lumbar. Please assist in approving the listing.",
                    priority = "HIGH",
                    status = "in_progress",
                    createdAt = System.currentTimeMillis() - 86400000L
                )
            )
        } else {
            emptyList()
        }
    )
    val sellerSupportTickets: StateFlow<List<SellerSupportTicket>> = _sellerSupportTickets.asStateFlow()

    // 11. Audit Logs
    private val _auditLogs = MutableStateFlow(
        if (isDefaultSeller) {
            listOf(
                MarketplaceSellerAuditLog(
                    logId = "log_01",
                    sellerUid = currentSellerUid,
                    action = "store_profile_updated",
                    resourceType = "marketplace_seller_profiles",
                    resourceId = currentSellerUid,
                    performedByUid = currentSellerUid,
                    beforeSummary = "Store description updated",
                    afterSummary = "Added SFDA certification details"
                ),
                MarketplaceSellerAuditLog(
                    logId = "log_02",
                    sellerUid = currentSellerUid,
                    action = "inventory_adjusted",
                    resourceType = "marketplace_inventory",
                    resourceId = "prod_first_aid_04",
                    performedByUid = currentSellerUid,
                    beforeSummary = "Available: 60",
                    afterSummary = "Available: 85 (Added 25 units stock)"
                )
            )
        } else {
            emptyList()
        }
    )
    val auditLogs: StateFlow<List<MarketplaceSellerAuditLog>> = _auditLogs.asStateFlow()

    // ==========================================
    // MUTATION FUNCTIONS
    // ==========================================

    fun updateProfile(updated: MarketplaceSellerProfile) {
        _sellerProfile.value = updated.copy(updatedAt = System.currentTimeMillis())
    }

    fun addProduct(product: MarketplaceProduct): Boolean {
        // Enforce seller ownership
        val ownedProduct = product.copy(sellerUid = currentSellerUid)
        _sellerProducts.value = _sellerProducts.value + ownedProduct

        // Initialize inventory record
        val inv = MarketplaceInventory(
            sellerUid = currentSellerUid,
            productId = ownedProduct.productId,
            sku = ownedProduct.sku.ifBlank { "SKU-${UUID.randomUUID().toString().take(6)}" },
            availableQuantity = ownedProduct.stock,
            lowStockThreshold = 5,
            inventoryStatus = if (ownedProduct.stock > 0) InventoryStatus.IN_STOCK else InventoryStatus.OUT_OF_STOCK
        )
        _sellerInventory.value = _sellerInventory.value + inv
        return true
    }

    fun updateProduct(product: MarketplaceProduct) {
        if (product.sellerUid != currentSellerUid) return
        _sellerProducts.value = _sellerProducts.value.map {
            if (it.productId == product.productId) product else it
        }
    }

    fun updateInventoryQuantity(productId: String, delta: Int): Boolean {
        var success = false
        _sellerInventory.value = _sellerInventory.value.map { inv ->
            if (inv.productId == productId && inv.sellerUid == currentSellerUid) {
                val newQty = (inv.availableQuantity + delta).coerceAtLeast(0)
                val newStatus = when {
                    newQty == 0 -> InventoryStatus.OUT_OF_STOCK
                    newQty <= inv.lowStockThreshold -> InventoryStatus.LOW_STOCK
                    else -> InventoryStatus.IN_STOCK
                }
                success = true
                inv.copy(
                    availableQuantity = newQty,
                    inventoryStatus = newStatus,
                    updatedAt = System.currentTimeMillis()
                )
            } else inv
        }

        // Keep product stock in sync
        if (success) {
            _sellerProducts.value = _sellerProducts.value.map { prod ->
                if (prod.productId == productId) {
                    prod.copy(stockQuantity = (prod.stockQuantity + delta).coerceAtLeast(0))
                } else prod
            }
        }
        return success
    }

    fun updateOrderStatus(orderId: String, newStatus: String): Boolean {
        var updated = false
        _sellerOrders.value = _sellerOrders.value.map { ord ->
            if (ord.orderId == orderId) {
                updated = true
                ord.copy(orderStatus = newStatus)
            } else ord
        }
        return updated
    }

    fun submitPayoutRequest(amount: Double): Boolean {
        val currentBal = _sellerBalance.value
        val defaultAccount = _payoutAccounts.value.firstOrNull() ?: return false
        if (amount <= 0 || amount > currentBal.availableBalance) return false

        val req = MarketplaceSellerPayoutRequest(
            sellerUid = currentSellerUid,
            amount = amount,
            currency = currentBal.currency,
            payoutAccountId = defaultAccount.payoutAccountId,
            status = PayoutStatus.REQUESTED
        )
        _payoutRequests.value = listOf(req) + _payoutRequests.value

        // Deduct from available, transfer to pending
        _sellerBalance.value = currentBal.copy(
            availableBalance = currentBal.availableBalance - amount,
            pendingBalance = currentBal.pendingBalance + amount,
            updatedAt = System.currentTimeMillis()
        )
        return true
    }

    fun addOffer(offer: MarketplaceSellerOffer) {
        _sellerOffers.value = listOf(offer.copy(sellerUid = currentSellerUid)) + _sellerOffers.value
    }

    fun createSupportTicket(subject: String, description: String, category: SellerSupportCategory): SellerSupportTicket {
        val ticket = SellerSupportTicket(
            sellerUid = currentSellerUid,
            category = category,
            subject = subject,
            description = description
        )
        _sellerSupportTickets.value = listOf(ticket) + _sellerSupportTickets.value
        return ticket
    }
}
