package com.example.healthogram.ui.verification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.ui.verification.components.*
import com.example.healthogram.ui.verification.pages.*
import com.example.healthogram.verification.*

enum class VerificationFlowScreen {
    CENTER_HOME,
    INTRODUCTION,
    ACCOUNT_TYPE,
    COUNTRY,
    REQUIREMENTS,
    APPLICATION_FORM,
    DOCUMENT_UPLOAD,
    DOCUMENT_PREVIEW,
    REVIEW,
    SUBMITTED,
    ADDITIONAL_INFO,
    SUCCESS,
    REJECTED,
    HISTORY,
    PRIVACY,
    BADGE_INFO,
    STAFF_DASHBOARD
}

/**
 * VerificationCenterPage
 * Main entry point and flow coordinator for Step 07 Platform Verification.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationCenterPage(
    user: User,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = remember { VerificationEngine.getInstance() }
    val profilesState by engine.profiles.collectAsState()
    val applicationsState by engine.applications.collectAsState()

    var currentScreen by remember { mutableStateOf(VerificationFlowScreen.CENTER_HOME) }
    var selectedAccountType by remember { mutableStateOf(user.accountType) }
    var selectedCountryCode by remember { mutableStateOf(user.countryCode) }
    var activeApplication by remember { mutableStateOf<VerificationApplication?>(null) }

    val profile = profilesState[user.uid] ?: remember(user) { engine.getOrCreateProfile(user) }
    val userRole = remember(user.uid) { engine.getUserRole(user.uid) }
    val isStaff = userRole in listOf(VerificationRole.VERIFICATION_REVIEWER, VerificationRole.VERIFICATION_MANAGER, VerificationRole.ADMIN, VerificationRole.OWNER)

    when (currentScreen) {
        VerificationFlowScreen.INTRODUCTION -> {
            VerificationIntroductionPage(
                user = user,
                onBack = { currentScreen = VerificationFlowScreen.CENTER_HOME },
                onStartVerification = { currentScreen = VerificationFlowScreen.ACCOUNT_TYPE },
                onViewPrivacy = { currentScreen = VerificationFlowScreen.PRIVACY }
            )
        }
        VerificationFlowScreen.ACCOUNT_TYPE -> {
            VerificationAccountTypePage(
                currentType = selectedAccountType,
                onBack = { currentScreen = VerificationFlowScreen.INTRODUCTION },
                onSelectAccountType = {
                    selectedAccountType = it
                    currentScreen = VerificationFlowScreen.COUNTRY
                }
            )
        }
        VerificationFlowScreen.COUNTRY -> {
            VerificationCountryPage(
                currentCountryCode = selectedCountryCode,
                onBack = { currentScreen = VerificationFlowScreen.ACCOUNT_TYPE },
                onSelectCountry = {
                    selectedCountryCode = it
                    currentScreen = VerificationFlowScreen.REQUIREMENTS
                }
            )
        }
        VerificationFlowScreen.REQUIREMENTS -> {
            VerificationRequirementsPage(
                accountType = selectedAccountType,
                countryCode = selectedCountryCode,
                onBack = { currentScreen = VerificationFlowScreen.COUNTRY },
                onContinue = { currentScreen = VerificationFlowScreen.APPLICATION_FORM }
            )
        }
        VerificationFlowScreen.APPLICATION_FORM -> {
            VerificationApplicationPage(
                user = user,
                countryCode = selectedCountryCode,
                onBack = { currentScreen = VerificationFlowScreen.REQUIREMENTS },
                onApplicationCreated = { app ->
                    activeApplication = app
                    currentScreen = VerificationFlowScreen.DOCUMENT_UPLOAD
                }
            )
        }
        VerificationFlowScreen.DOCUMENT_UPLOAD -> {
            val app = activeApplication ?: return
            VerificationDocumentUploadPage(
                user = user,
                application = app,
                onBack = { currentScreen = VerificationFlowScreen.DOCUMENT_PREVIEW },
                onDocumentUploaded = { currentScreen = VerificationFlowScreen.DOCUMENT_PREVIEW }
            )
        }
        VerificationFlowScreen.DOCUMENT_PREVIEW -> {
            val app = activeApplication ?: return
            VerificationDocumentPreviewPage(
                user = user,
                application = app,
                onBack = { currentScreen = VerificationFlowScreen.APPLICATION_FORM },
                onAddMoreDocs = { currentScreen = VerificationFlowScreen.DOCUMENT_UPLOAD },
                onProceedToReview = { currentScreen = VerificationFlowScreen.REVIEW }
            )
        }
        VerificationFlowScreen.REVIEW -> {
            val app = activeApplication ?: return
            VerificationReviewPage(
                user = user,
                application = app,
                onBack = { currentScreen = VerificationFlowScreen.DOCUMENT_PREVIEW },
                onSubmitSuccess = { submitted ->
                    activeApplication = submitted
                    currentScreen = VerificationFlowScreen.SUBMITTED
                }
            )
        }
        VerificationFlowScreen.SUBMITTED -> {
            val app = activeApplication ?: return
            VerificationSubmittedPage(
                application = app,
                onDone = { currentScreen = VerificationFlowScreen.CENTER_HOME }
            )
        }
        VerificationFlowScreen.ADDITIONAL_INFO -> {
            VerificationAdditionalInfoPage(
                user = user,
                profile = profile,
                onBack = { currentScreen = VerificationFlowScreen.CENTER_HOME },
                onUploadReplacement = {
                    val app = applicationsState.values.find { it.uid == user.uid }
                    if (app != null) {
                        activeApplication = app
                        currentScreen = VerificationFlowScreen.DOCUMENT_UPLOAD
                    } else {
                        currentScreen = VerificationFlowScreen.APPLICATION_FORM
                    }
                }
            )
        }
        VerificationFlowScreen.SUCCESS -> {
            VerificationSuccessPage(
                user = user,
                profile = profile,
                onDone = { currentScreen = VerificationFlowScreen.CENTER_HOME }
            )
        }
        VerificationFlowScreen.REJECTED -> {
            VerificationRejectedPage(
                user = user,
                profile = profile,
                onBack = { currentScreen = VerificationFlowScreen.CENTER_HOME },
                onReapply = { currentScreen = VerificationFlowScreen.ACCOUNT_TYPE }
            )
        }
        VerificationFlowScreen.HISTORY -> {
            VerificationHistoryPage(
                user = user,
                onBack = { currentScreen = VerificationFlowScreen.CENTER_HOME }
            )
        }
        VerificationFlowScreen.PRIVACY -> {
            VerificationPrivacyPage(
                onBack = { currentScreen = VerificationFlowScreen.CENTER_HOME }
            )
        }
        VerificationFlowScreen.BADGE_INFO -> {
            VerificationBadgeInfoPage(
                onBack = { currentScreen = VerificationFlowScreen.CENTER_HOME }
            )
        }
        VerificationFlowScreen.STAFF_DASHBOARD -> {
            VerificationDashboardPage(
                staffUser = user,
                onBack = { currentScreen = VerificationFlowScreen.CENTER_HOME }
            )
        }
        VerificationFlowScreen.CENTER_HOME -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Verification Center", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBack, modifier = Modifier.testTag("verification_center_back_button")) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { currentScreen = VerificationFlowScreen.BADGE_INFO },
                                modifier = Modifier.testTag("badge_info_icon_button")
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "About Badge")
                            }
                        }
                    )
                },
                modifier = modifier.testTag("verification_center_page")
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        VerificationExpiryBanner(
                            expiresAt = profile.verificationExpiresAt,
                            onRenewClick = { currentScreen = VerificationFlowScreen.ACCOUNT_TYPE }
                        )
                    }

                    item {
                        VerificationStatusCard(
                            profile = profile,
                            onActionClick = {
                                when (profile.verificationStatus) {
                                    VerificationStatus.NOT_STARTED -> currentScreen = VerificationFlowScreen.INTRODUCTION
                                    VerificationStatus.DRAFT -> currentScreen = VerificationFlowScreen.APPLICATION_FORM
                                    VerificationStatus.ADDITIONAL_INFORMATION_REQUIRED -> currentScreen = VerificationFlowScreen.ADDITIONAL_INFO
                                    VerificationStatus.VERIFIED, VerificationStatus.APPROVED -> currentScreen = VerificationFlowScreen.SUCCESS
                                    VerificationStatus.REJECTED -> currentScreen = VerificationFlowScreen.REJECTED
                                    VerificationStatus.EXPIRED -> currentScreen = VerificationFlowScreen.ACCOUNT_TYPE
                                    else -> currentScreen = VerificationFlowScreen.HISTORY
                                }
                            }
                        )
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HealthogramTheme.colors.divider)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Account Profile", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Category:", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.onSurfaceVariant)
                                    Text(user.accountType.displayName, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Country:", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.onSurfaceVariant)
                                    Text("${user.countryName} (${user.countryCode})", fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Verified Badge:", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.onSurfaceVariant)
                                    VerifiedBadge(status = profile.verificationStatus, showLabel = true)
                                }
                            }
                        }
                    }

                    item {
                        Text("Navigation & Resources", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            MenuNavigationRow(
                                icon = Icons.Default.History,
                                title = "Verification History & Audits",
                                onClick = { currentScreen = VerificationFlowScreen.HISTORY }
                            )
                            MenuNavigationRow(
                                icon = Icons.Default.Security,
                                title = "Privacy & Data Protection",
                                onClick = { currentScreen = VerificationFlowScreen.PRIVACY }
                            )
                            MenuNavigationRow(
                                icon = Icons.Default.Shield,
                                title = "What the Verified Badge Means",
                                onClick = { currentScreen = VerificationFlowScreen.BADGE_INFO }
                            )
                            if (isStaff) {
                                MenuNavigationRow(
                                    icon = Icons.Default.AdminPanelSettings,
                                    title = "Reviewer Staff Portal",
                                    onClick = { currentScreen = VerificationFlowScreen.STAFF_DASHBOARD }
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuNavigationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = HealthogramTheme.colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
        }
    }
}
