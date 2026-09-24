package com.example.healthogram.ui.verification.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.HealthogramTextField
import com.example.healthogram.ui.verification.components.*
import com.example.healthogram.verification.*

/**
 * VerificationIntroductionPage
 * Step 07 Section 2 & 9: Explains the platform verification process,
 * disclaimers, and trusted community standards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationIntroductionPage(
    user: User,
    onBack: () -> Unit,
    onStartVerification: () -> Unit,
    onViewPrivacy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Get Verified", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("intro_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("verification_intro_page")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(HealthogramTheme.colors.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = HealthogramTheme.colors.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            item {
                Text(
                    text = "Verify Your Account on Healthogram",
                    style = HealthogramTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Build trust across the healthcare community with a verified platform badge, unlock the Health Passport QR scanner, and confirm your professional identity.",
                    style = HealthogramTheme.typography.bodyMedium,
                    color = HealthogramTheme.colors.onSurfaceVariant
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "What Platform Verification Means",
                            style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This account has completed Healthogram's verification process based on the information and documents submitted to the platform.\n\nHealthogram verification does not replace government licensing, regulatory approval, or professional medical guarantees.",
                            style = HealthogramTheme.typography.bodySmall,
                            color = HealthogramTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureItem(
                        icon = Icons.Default.Verified,
                        title = "Healthogram Verified Badge",
                        desc = "Displayed across your profile, posts, reels, and search listings."
                    )
                    FeatureItem(
                        icon = Icons.Default.QrCodeScanner,
                        title = "Healthcare QR Scanner Access",
                        desc = "Verified Doctors, Clinics, Hospitals, and Labs can scan patient QR access tickets."
                    )
                    FeatureItem(
                        icon = Icons.Default.Lock,
                        title = "Zero-Knowledge Private Storage",
                        desc = "Verification documents are encrypted in private storage and never exposed publicly."
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewPrivacy() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Read our Verification Privacy & Data Policy",
                        style = HealthogramTheme.typography.labelMedium,
                        color = HealthogramTheme.colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onStartVerification,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("start_verification_cta_button")
                ) {
                    Text("Continue to Verification", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun FeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(HealthogramTheme.colors.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Text(text = desc, style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.onSurfaceVariant)
        }
    }
}

/**
 * VerificationAccountTypePage
 * Confirms or selects the account category for verification.
 * ARCHITECTURAL RULE: Pharmacy is completely excluded.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationAccountTypePage(
    currentType: AccountType,
    onBack: () -> Unit,
    onSelectAccountType: (AccountType) -> Unit,
    modifier: Modifier = Modifier
) {
    val allowedTypes = listOf(
        AccountType.INDIVIDUAL,
        AccountType.DOCTOR,
        AccountType.CLINIC,
        AccountType.HOSPITAL,
        AccountType.LABORATORY
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Category", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("account_type_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("verification_account_type_page")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            VerificationProgressCard(currentStep = 1, totalSteps = 5)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select Verification Category",
                style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Verification requirements and credential standards adapt dynamically based on your category.",
                style = HealthogramTheme.typography.bodyMedium,
                color = HealthogramTheme.colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(allowedTypes) { type ->
                    val isSelected = type == currentType
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) HealthogramTheme.colors.primary.copy(alpha = 0.08f) else HealthogramTheme.colors.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.divider
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectAccountType(type) }
                            .testTag("select_account_type_${type.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = type.displayName,
                                    style = HealthogramTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = VerificationFunctions.getAccountVerificationType(type),
                                    style = HealthogramTheme.typography.bodySmall,
                                    color = HealthogramTheme.colors.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = HealthogramTheme.colors.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * VerificationCountryPage
 * Selects jurisdiction for regulatory document mapping.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationCountryPage(
    currentCountryCode: String,
    onBack: () -> Unit,
    onSelectCountry: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = remember { VerificationEngine.getInstance() }
    val countriesState by engine.countries.collectAsState()
    val countryList = countriesState.values.toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Jurisdiction", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("country_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("verification_country_page")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            VerificationProgressCard(currentStep = 2, totalSteps = 5)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Country of Practice or Registration",
                style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Requirements are strictly mapped to the statutory health authority of your selected country.",
                style = HealthogramTheme.typography.bodyMedium,
                color = HealthogramTheme.colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(countryList) { country ->
                    val isSelected = country.countryCode.equals(currentCountryCode, ignoreCase = true)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) HealthogramTheme.colors.primary.copy(alpha = 0.08f) else HealthogramTheme.colors.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.divider
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectCountry(country.countryCode) }
                            .testTag("select_country_${country.countryCode.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = country.countryName,
                                    style = HealthogramTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.onSurface
                                )
                                Text(
                                    text = "Jurisdiction Code: ${country.countryCode}",
                                    style = HealthogramTheme.typography.bodySmall,
                                    color = HealthogramTheme.colors.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = HealthogramTheme.colors.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * VerificationRequirementsPage
 * Shows the dynamic checklist of mandatory and optional documents.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationRequirementsPage(
    accountType: AccountType,
    countryCode: String,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = remember { VerificationEngine.getInstance() }
    val requirements = remember(accountType, countryCode) {
        engine.getRequirementsForAccount(accountType, countryCode)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Required Documents", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("requirements_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 4.dp) {
                Box(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Button(
                        onClick = onContinue,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("requirements_continue_button")
                    ) {
                        Text("Prepare Application", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        },
        modifier = modifier.testTag("verification_requirements_page")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                VerificationProgressCard(currentStep = 3, totalSteps = 5)
            }

            item {
                Text(
                    text = "Document Checklist for $countryCode",
                    style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Please ensure all scanned copies are clear, in focus, and within their active validity period.",
                    style = HealthogramTheme.typography.bodySmall,
                    color = HealthogramTheme.colors.onSurfaceVariant
                )
            }

            items(requirements) { req ->
                VerificationRequirementCard(
                    requirement = req,
                    isUploaded = false,
                    onUploadClick = onContinue
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Accepted File Formats", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PDF, JPG, JPEG, and PNG. Maximum size per file: 10MB.",
                            style = HealthogramTheme.typography.bodySmall,
                            color = HealthogramTheme.colors.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

/**
 * VerificationApplicationPage
 * Step 07 Section 10: Form details for applicant, legal name, license number masking.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationApplicationPage(
    user: User,
    countryCode: String,
    onBack: () -> Unit,
    onApplicationCreated: (VerificationApplication) -> Unit,
    modifier: Modifier = Modifier
) {
    var legalName by remember { mutableStateOf(user.displayName) }
    var specialty by remember { mutableStateOf("") }
    var facilityAddress by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val engine = remember { VerificationEngine.getInstance() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Application Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("app_details_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("verification_application_page")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                VerificationProgressCard(currentStep = 3, totalSteps = 5)
            }

            item {
                Text(
                    text = "Professional & Entity Information",
                    style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Ensure names match the official registration documents exactly.",
                    style = HealthogramTheme.typography.bodySmall,
                    color = HealthogramTheme.colors.onSurfaceVariant
                )
            }

            item {
                HealthogramTextField(
                    value = legalName,
                    onValueChange = { legalName = it },
                    label = if (user.accountType == AccountType.INDIVIDUAL || user.accountType == AccountType.DOCTOR) "Full Legal Name" else "Legal Entity Name",
                    modifier = Modifier.fillMaxWidth().testTag("app_legal_name_input")
                )
            }

            if (user.accountType == AccountType.DOCTOR) {
                item {
                    HealthogramTextField(
                        value = specialty,
                        onValueChange = { specialty = it },
                        label = "Medical Specialty (e.g. Cardiology, Pediatrics)",
                        modifier = Modifier.fillMaxWidth().testTag("app_specialty_input")
                    )
                }
            }

            if (user.accountType in listOf(AccountType.CLINIC, AccountType.HOSPITAL, AccountType.LABORATORY)) {
                item {
                    HealthogramTextField(
                        value = facilityAddress,
                        onValueChange = { facilityAddress = it },
                        label = "Facility Registered Address",
                        modifier = Modifier.fillMaxWidth().testTag("app_facility_address_input")
                    )
                }
            }

            if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage!!,
                        style = HealthogramTheme.typography.bodySmall,
                        color = HealthogramTheme.colors.error
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (legalName.isBlank()) {
                            errorMessage = "Legal name is required."
                            return@Button
                        }
                        val result = engine.startApplication(
                            applicant = user,
                            countryCode = countryCode,
                            legalName = legalName,
                            specialty = specialty,
                            facilityAddress = facilityAddress
                        )
                        result.onSuccess { app ->
                            onApplicationCreated(app)
                        }.onFailure { err ->
                            errorMessage = err.message ?: "Failed to start application."
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_and_continue_app_button")
                ) {
                    Text("Save & Upload Documents", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
