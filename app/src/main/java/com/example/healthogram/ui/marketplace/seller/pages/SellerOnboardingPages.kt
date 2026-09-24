package com.example.healthogram.ui.marketplace.seller.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.healthogram.marketplace.seller.*
import com.example.healthogram.ui.marketplace.seller.components.*

/**
 * HEALTHOGRAM — STEP 09: SELLER ONBOARDING & VERIFICATION FLOW
 * Covers full country-aware onboarding journey across 10 specialized screens.
 */

// 1. SellerOnboardingPage
@Composable
fun SellerOnboardingPage(
    onStartOnboarding: () -> Unit = {},
    onCheckStatus: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(HealthogramTheme.colors.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Storefront,
                    contentDescription = null,
                    tint = HealthogramTheme.colors.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Become a Healthogram Seller",
                style = HealthogramTheme.typography.h5.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Join our verified healthcare and medical devices marketplace in Saudi Arabia. Sell certified wellness, diagnostic, and home care products directly to customers.",
                style = HealthogramTheme.typography.body2,
                color = HealthogramTheme.colors.textSecondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(28.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Why Sell on Healthogram?", style = HealthogramTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(12.dp))

                    val benefits = listOf(
                        "Verified Healthcare Marketplace" to "Strict quality standards ensure customer confidence and premium positioning.",
                        "Fast Automated Settlements" to "Direct local bank transfer payouts in Saudi Riyals (SAR) with automated accounting.",
                        "Direct Regulatory Compliance" to "Built-in SFDA device license workflows to keep your catalog fully compliant."
                    )

                    benefits.forEach { (title, desc) ->
                        Row(modifier = Modifier.padding(vertical = 6.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HealthogramTheme.colors.success, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(title, style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold))
                                Text(desc, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }

        item {
            Button(
                onClick = onStartOnboarding,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = HealthogramTheme.shapes.pill
            ) {
                Text("Start Seller Application", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onCheckStatus,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = HealthogramTheme.shapes.pill
            ) {
                Text("Check Application Status")
            }
        }
    }
}

// 2. SellerTypePage
@Composable
fun SellerTypePage(
    selectedType: SellerType,
    onSelectType: (SellerType) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Select Seller Account Type", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
        Text("Choose the registration type that matches your legal standing in Saudi Arabia.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
        Spacer(modifier = Modifier.height(20.dp))

        // Individual Seller Option
        Surface(
            color = if (selectedType == SellerType.INDIVIDUAL_SELLER) HealthogramTheme.colors.primary.copy(alpha = 0.08f) else HealthogramTheme.colors.surface,
            border = BorderStroke(
                if (selectedType == SellerType.INDIVIDUAL_SELLER) 2.dp else 1.dp,
                if (selectedType == SellerType.INDIVIDUAL_SELLER) HealthogramTheme.colors.primary else HealthogramTheme.colors.borderLight
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().clickable { onSelectType(SellerType.INDIVIDUAL_SELLER) }
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selectedType == SellerType.INDIVIDUAL_SELLER, onClick = { onSelectType(SellerType.INDIVIDUAL_SELLER) })
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Individual Seller / Freelance", style = HealthogramTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold))
                    Text("Requires National ID / Iqama and Freelance Certificate (Wathiqah). Best for solo wellness professionals and independent artisans.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Business Seller Option
        Surface(
            color = if (selectedType == SellerType.BUSINESS_SELLER) HealthogramTheme.colors.primary.copy(alpha = 0.08f) else HealthogramTheme.colors.surface,
            border = BorderStroke(
                if (selectedType == SellerType.BUSINESS_SELLER) 2.dp else 1.dp,
                if (selectedType == SellerType.BUSINESS_SELLER) HealthogramTheme.colors.primary else HealthogramTheme.colors.borderLight
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().clickable { onSelectType(SellerType.BUSINESS_SELLER) }
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selectedType == SellerType.BUSINESS_SELLER, onClick = { onSelectType(SellerType.BUSINESS_SELLER) })
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Registered Commercial Business (Est. / LLC)", style = HealthogramTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold))
                    Text("Requires Commercial Registration (CR) from Ministry of Commerce and ZATCA VAT Certificate. Highest order limits and corporate payout support.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = HealthogramTheme.shapes.pill
        ) {
            Text("Continue")
        }
    }
}

// 3. SellerBusinessInformationPage
@Composable
fun SellerBusinessInformationPage(
    legalName: String,
    onLegalNameChange: (String) -> Unit,
    crNumber: String,
    onCrChange: (String) -> Unit,
    taxNumber: String,
    onTaxChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Business Information", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
        Text("Provide verified enterprise details matching your regulatory filings.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = legalName,
            onValueChange = onLegalNameChange,
            label = { Text("Legal Entity / Commercial Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
            value = crNumber,
            onValueChange = onCrChange,
            label = { Text("Commercial Registration Number (10 Digits)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
            value = taxNumber,
            onValueChange = onTaxChange,
            label = { Text("ZATCA VAT Identification Number (15 Digits)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = HealthogramTheme.shapes.pill,
            enabled = legalName.isNotBlank()
        ) {
            Text("Next: Identity Verification")
        }
    }
}

// 4. SellerIdentityPage
@Composable
fun SellerIdentityPage(
    repName: String,
    onRepNameChange: (String) -> Unit,
    nationalId: String,
    onNationalIdChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Authorized Representative", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
        Text("Information of the legal representative authorized to operate this seller account.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = repName,
            onValueChange = onRepNameChange,
            label = { Text("Full Name as per National ID / Iqama") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
            value = nationalId,
            onValueChange = onNationalIdChange,
            label = { Text("National ID / Iqama Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = HealthogramTheme.shapes.pill,
            enabled = repName.isNotBlank() && nationalId.isNotBlank()
        ) {
            Text("Next: Healthcare Compliance")
        }
    }
}

// 5. SellerCompliancePage
@Composable
fun SellerCompliancePage(
    hasSfdaLicense: Boolean,
    onToggleSfda: (Boolean) -> Unit,
    agreedToPolicy: Boolean,
    onToggleAgreed: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Healthcare Compliance & Ethics", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
        Text("Healthogram enforces strict consumer protection and medical safety guidelines.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Prohibited Listing Policies", style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Prescription medications are strictly prohibited.\n• Unverified claims to cure or diagnose medical conditions will lead to immediate store suspension.\n• All diagnostic devices must hold valid SFDA authorization.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = hasSfdaLicense, onCheckedChange = onToggleSfda)
            Spacer(modifier = Modifier.width(8.dp))
            Text("My business holds an SFDA Medical Devices Establishment License", style = HealthogramTheme.typography.body2)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = agreedToPolicy, onCheckedChange = onToggleAgreed)
            Spacer(modifier = Modifier.width(8.dp))
            Text("I agree to the Healthogram Medical Seller Terms & Code of Conduct", style = HealthogramTheme.typography.body2)
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = HealthogramTheme.shapes.pill,
            enabled = agreedToPolicy
        ) {
            Text("Next: Document Upload")
        }
    }
}

// 6. SellerDocumentUploadPage
@Composable
fun SellerDocumentUploadPage(
    documents: List<MarketplaceSellerDocument>,
    onUploadClick: (SellerDocumentType) -> Unit,
    onNext: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Upload Regulatory Documents", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
            Text("Uploaded documents are securely stored in private storage and never exposed to customers.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
            Spacer(modifier = Modifier.height(16.dp))
        }

        val requiredTypes = listOf(
            SellerDocumentType.COMMERCIAL_REGISTRATION,
            SellerDocumentType.TAX_CERTIFICATE,
            SellerDocumentType.HEALTHCARE_SELLER_PERMIT
        )

        items(requiredTypes) { docType ->
            val existing = documents.find { it.documentType == docType }
            Surface(
                color = HealthogramTheme.colors.surface,
                border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(docType.title, style = HealthogramTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold))
                        Text(
                            if (existing != null) "Uploaded: ${existing.fileName}" else "Required document missing",
                            style = HealthogramTheme.typography.caption,
                            color = if (existing != null) HealthogramTheme.colors.success else HealthogramTheme.colors.error
                        )
                    }
                    Button(
                        onClick = { onUploadClick(docType) },
                        colors = ButtonDefaults.buttonColors(containerColor = if (existing != null) HealthogramTheme.colors.surfaceVariant else HealthogramTheme.colors.primary),
                        shape = HealthogramTheme.shapes.pill
                    ) {
                        Text(if (existing != null) "Replace" else "Upload", color = if (existing != null) HealthogramTheme.colors.textPrimary else Color.White)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = HealthogramTheme.shapes.pill
            ) {
                Text("Next: Storefront Setup")
            }
        }
    }
}

// 7. SellerStoreSetupPage
@Composable
fun SellerStoreSetupPage(
    storeName: String,
    onStoreNameChange: (String) -> Unit,
    storeDescription: String,
    onStoreDescriptionChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Setup Your Storefront", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
        Text("Customize the public brand appearance seen by Healthogram customers.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = storeName,
            onValueChange = onStoreNameChange,
            label = { Text("Display Store Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
            value = storeDescription,
            onValueChange = onStoreDescriptionChange,
            label = { Text("Store Description & Specialty") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = HealthogramTheme.shapes.pill,
            enabled = storeName.isNotBlank()
        ) {
            Text("Review Application")
        }
    }
}

// 8. SellerReviewPage
@Composable
fun SellerReviewPage(
    profile: MarketplaceSellerProfile,
    onSubmit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Review Application Details", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
        Text("Confirm all submitted information before dispatching for compliance verification.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Account Type: ${profile.sellerType.displayName}", style = HealthogramTheme.typography.body2.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Store Name: ${profile.storeName}", style = HealthogramTheme.typography.body2)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Legal Name: ${profile.legalName}", style = HealthogramTheme.typography.body2)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Operating Jurisdiction: Saudi Arabia (SAR)", style = HealthogramTheme.typography.body2)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = HealthogramTheme.shapes.pill
        ) {
            Text("Submit Application for Review", fontWeight = FontWeight.Bold)
        }
    }
}

// 9. SellerSubmittedPage
@Composable
fun SellerSubmittedPage(
    onGoToDashboard: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(HealthogramTheme.colors.success.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HealthogramTheme.colors.success, modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("Application Received!", style = HealthogramTheme.typography.h5.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Your marketplace seller application has been submitted to Healthogram Compliance. Verification typically completes within 1 to 2 business days.",
            style = HealthogramTheme.typography.body2,
            color = HealthogramTheme.colors.textSecondary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onGoToDashboard,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = HealthogramTheme.shapes.pill
        ) {
            Text("Explore Seller Dashboard")
        }
    }
}

// 10. SellerVerificationStatusPage
@Composable
fun SellerVerificationStatusPage(
    verification: MarketplaceSellerVerification,
    onBack: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Verification Audit Status", style = HealthogramTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            color = HealthogramTheme.colors.surface,
            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Current Status", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                SellerVerificationBadge(status = verification.status)
                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = HealthogramTheme.colors.borderLight)
                Spacer(modifier = Modifier.height(14.dp))
                Text("Application ID: ${verification.applicationId}", style = HealthogramTheme.typography.caption)
                Text("Jurisdiction: ${verification.countryCode}", style = HealthogramTheme.typography.caption)
                if (verification.reviewerUid != null) {
                    Text("Assigned Reviewer: ${verification.reviewerUid}", style = HealthogramTheme.typography.caption)
                }
            }
        }
    }
}
