package com.example.healthogram.ui.verification.pages

import androidx.compose.foundation.background
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
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.HealthogramTextField
import com.example.healthogram.ui.verification.components.*
import com.example.healthogram.verification.*

/**
 * VerificationDocumentUploadPage
 * Handles document selection, file metadata, masked identifier, and expiry date.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationDocumentUploadPage(
    user: User,
    application: VerificationApplication,
    onBack: () -> Unit,
    onDocumentUploaded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = remember { VerificationEngine.getInstance() }
    val requirements = remember { engine.getRequirementsForAccount(application.accountType, application.countryCode) }

    var selectedDocType by remember { mutableStateOf(requirements.firstOrNull()?.documentType ?: "national_id") }
    var fileName by remember { mutableStateOf("license_scan.pdf") }
    var mimeType by remember { mutableStateOf("application/pdf") }
    var documentNumber by remember { mutableStateOf("") }
    var issuingAuthority by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Documents", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("upload_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("verification_document_upload_page")
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
                    text = "Select Document Type",
                    style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(requirements) { req ->
                val isSelected = req.documentType == selectedDocType
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) HealthogramTheme.colors.primary.copy(alpha = 0.08f) else HealthogramTheme.colors.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.divider
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedDocType = req.documentType }
                        .testTag("select_doctype_${req.documentType}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(req.displayName, fontWeight = FontWeight.Bold, style = HealthogramTheme.typography.bodyMedium)
                            Text(req.description, style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.onSurfaceVariant)
                        }
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HealthogramTheme.colors.primary)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Document File & Metadata",
                    style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(fileName, fontWeight = FontWeight.Bold)
                        Text("PDF Document • 2.4 MB", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    fileName = "scanned_document.pdf"
                                    mimeType = "application/pdf"
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("PDF", style = HealthogramTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = {
                                    fileName = "document_photo.jpg"
                                    mimeType = "image/jpeg"
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Photo (JPG)", style = HealthogramTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            item {
                HealthogramTextField(
                    value = documentNumber,
                    onValueChange = { documentNumber = it },
                    label = "Document or License Number (Masked in secure storage)",
                    modifier = Modifier.fillMaxWidth().testTag("doc_number_input")
                )
            }

            item {
                HealthogramTextField(
                    value = issuingAuthority,
                    onValueChange = { issuingAuthority = it },
                    label = "Issuing Authority or Government Board",
                    modifier = Modifier.fillMaxWidth().testTag("doc_authority_input")
                )
            }

            if (errorMessage != null) {
                item {
                    Text(errorMessage!!, color = HealthogramTheme.colors.error, style = HealthogramTheme.typography.bodySmall)
                }
            }

            if (successMessage != null) {
                item {
                    Text(successMessage!!, color = HealthogramTheme.colors.success, style = HealthogramTheme.typography.bodySmall)
                }
            }

            item {
                Button(
                    onClick = {
                        val result = engine.uploadDocument(
                            applicant = user,
                            applicationId = application.applicationId,
                            documentType = selectedDocType,
                            fileName = fileName,
                            mimeType = mimeType,
                            fileSize = 2_400_000L,
                            documentNumberFull = documentNumber,
                            issuingAuthority = issuingAuthority,
                            expiryDate = System.currentTimeMillis() + 365L * 24 * 3600 * 1000L
                        )
                        result.onSuccess {
                            successMessage = "Document uploaded successfully to private storage!"
                            errorMessage = null
                            onDocumentUploaded()
                        }.onFailure {
                            errorMessage = it.message ?: "Failed to upload document."
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_upload_doc_button")
                ) {
                    Text("Attach Document", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * VerificationDocumentPreviewPage
 * Step 07 Section 11 & 12: Preview uploaded documents, file metadata, delete or add more.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationDocumentPreviewPage(
    user: User,
    application: VerificationApplication,
    onBack: () -> Unit,
    onAddMoreDocs: () -> Unit,
    onProceedToReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = remember { VerificationEngine.getInstance() }
    val documentsState by engine.documents.collectAsState()
    val appDocs = documentsState.values.filter { it.applicationId == application.applicationId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attached Documents", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("preview_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 4.dp) {
                Box(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Button(
                        onClick = onProceedToReview,
                        enabled = appDocs.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("proceed_to_review_button")
                    ) {
                        Text("Review Application", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        },
        modifier = modifier.testTag("verification_document_preview_page")
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Uploaded Files (${appDocs.size})",
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = onAddMoreDocs, modifier = Modifier.testTag("add_more_docs_button")) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Document")
                    }
                }
            }

            if (appDocs.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = HealthogramTheme.colors.onSurfaceVariant, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No documents uploaded yet", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Please upload the mandatory documents to proceed.", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.onSurfaceVariant)
                        }
                    }
                }
            }

            items(appDocs) { doc ->
                VerificationDocumentCard(
                    document = doc,
                    canDelete = application.status == VerificationStatus.DRAFT,
                    onDelete = { engine.deleteDocument(user, doc.documentId) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

/**
 * VerificationReviewPage
 * Step 07 Section 13: Summary review before submission, terms declaration, submit trigger.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationReviewPage(
    user: User,
    application: VerificationApplication,
    onBack: () -> Unit,
    onSubmitSuccess: (VerificationApplication) -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = remember { VerificationEngine.getInstance() }
    val documentsState by engine.documents.collectAsState()
    val appDocs = documentsState.values.filter { it.applicationId == application.applicationId }

    var termsAccepted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review & Submit", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("review_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("verification_review_page")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                VerificationProgressCard(currentStep = 4, totalSteps = 5)
            }

            item {
                Text(
                    text = "Application Summary",
                    style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Category:", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.onSurfaceVariant)
                            Text(application.accountType.displayName, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Jurisdiction:", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.onSurfaceVariant)
                            Text(application.countryCode, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Legal Name:", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.onSurfaceVariant)
                            Text(application.legalName.ifEmpty { user.displayName }, fontWeight = FontWeight.Bold)
                        }
                        if (application.specialty.isNotEmpty()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Specialty:", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.onSurfaceVariant)
                                Text(application.specialty, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Attached Documents:", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.onSurfaceVariant)
                            Text("${appDocs.size} Document(s)", fontWeight = FontWeight.Bold, color = HealthogramTheme.colors.primary)
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { termsAccepted = !termsAccepted }
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it },
                            modifier = Modifier.testTag("verification_declaration_checkbox")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "I declare that all submitted information and credentials are authentic, current, and issued by an authorized regulatory body. I understand false submissions will result in immediate revocation.",
                            style = HealthogramTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (errorMessage != null) {
                item {
                    Text(errorMessage!!, color = HealthogramTheme.colors.error, style = HealthogramTheme.typography.bodySmall)
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (!termsAccepted) {
                            errorMessage = "Please accept the verification accuracy declaration."
                            return@Button
                        }
                        isSubmitting = true
                        val result = engine.submitApplication(user, application.applicationId)
                        isSubmitting = false
                        result.onSuccess { submitted ->
                            onSubmitSuccess(submitted)
                        }.onFailure {
                            errorMessage = it.message ?: "Failed to submit verification application."
                        }
                    },
                    enabled = termsAccepted && !isSubmitting,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("final_submit_application_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Submit Application for Audit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * VerificationSubmittedPage
 * Step 07 Section 14: Confirmation screen after application is submitted.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationSubmittedPage(
    application: VerificationApplication,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Application Submitted", fontWeight = FontWeight.Bold) }
            )
        },
        modifier = modifier.testTag("verification_submitted_page")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(HealthogramTheme.colors.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = HealthogramTheme.colors.primary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Application Under Review",
                style = HealthogramTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your application (ID: ${application.applicationId.take(12)}...) has been queued for verification audit.",
                style = HealthogramTheme.typography.bodyMedium,
                color = HealthogramTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Estimated Review Time", fontWeight = FontWeight.Bold, style = HealthogramTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("1–3 business days for individual and medical license verifications.", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = onDone,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submitted_done_button")
            ) {
                Text("Return to Verification Center", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * VerificationAdditionalInfoPage
 * Step 07 Section 16: Additional information required screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationAdditionalInfoPage(
    user: User,
    profile: VerificationProfile,
    onBack: () -> Unit,
    onUploadReplacement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Information Required", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("add_info_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("verification_additional_info_page")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.warningContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HelpCenter, contentDescription = null, tint = HealthogramTheme.colors.warning)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Action Required by Reviewer", fontWeight = FontWeight.Bold, style = HealthogramTheme.typography.titleSmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = profile.additionalInformationRequired ?: "The verification board requested updated or clearer documentation before approving your application.",
                        style = HealthogramTheme.typography.bodyMedium
                    )
                }
            }

            Text(
                text = "Next Steps",
                style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "1. Prepare a clear, updated copy of the requested document.\n2. Ensure all 4 corners and the issuing stamp are visible.\n3. Upload the document and submit for priority re-audit.",
                style = HealthogramTheme.typography.bodySmall,
                color = HealthogramTheme.colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onUploadReplacement,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("provide_replacement_doc_button")
            ) {
                Text("Upload Requested Document", fontWeight = FontWeight.Bold)
            }
        }
    }
}
