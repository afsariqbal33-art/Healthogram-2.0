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
import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationBadgeType
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.ui.verification.components.*
import com.example.healthogram.verification.*

/**
 * VerificationSuccessPage
 * Step 07 Section 17: Celebration page when verified badge is active.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationSuccessPage(
    user: User,
    profile: VerificationProfile,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Verified", fontWeight = FontWeight.Bold) }
            )
        },
        modifier = modifier.testTag("verification_success_page")
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
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(HealthogramTheme.colors.successContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = HealthogramTheme.colors.success,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Congratulations!",
                style = HealthogramTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your account has been officially verified on Healthogram.",
                style = HealthogramTheme.typography.bodyMedium,
                color = HealthogramTheme.colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Verified Badge:", style = HealthogramTheme.typography.bodyMedium)
                        VerifiedBadge(status = VerificationStatus.VERIFIED, showLabel = true)
                    }

                    if (profile.verificationExpiresAt != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Validity Period:", style = HealthogramTheme.typography.bodyMedium)
                            Text(
                                text = VerificationFunctions.getVerificationExpiryMessage(profile.verificationExpiresAt),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (user.accountType in listOf(AccountType.DOCTOR, AccountType.CLINIC, AccountType.HOSPITAL, AccountType.LABORATORY)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Health Passport Scanner:", style = HealthogramTheme.typography.bodyMedium)
                            Text("UNLOCKED", fontWeight = FontWeight.Bold, color = HealthogramTheme.colors.success)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = onDone,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("success_done_button")
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * VerificationRejectedPage
 * Step 07 Section 18: Displays standardized reason code safely without exposing internal notes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationRejectedPage(
    user: User,
    profile: VerificationProfile,
    onBack: () -> Unit,
    onReapply: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reason = RejectionReasonCode.fromCode(profile.rejectionReasonCode)
    val canReapply = profile.nextAllowedSubmissionAt == null || System.currentTimeMillis() >= profile.nextAllowedSubmissionAt

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verification Status", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("rejected_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("verification_rejected_page")
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
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.errorContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = HealthogramTheme.colors.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Application Not Approved", fontWeight = FontWeight.Bold, style = HealthogramTheme.typography.titleSmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = reason.safeUserExplanation,
                        style = HealthogramTheme.typography.bodyMedium
                    )
                }
            }

            Text(
                text = "How to Successfully Reapply",
                style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "1. Verify that your document image is sharp, clear, and uncropped.\n2. Confirm that your license registration is currently active in official records.\n3. Make sure names and identifiers match your profile details exactly.",
                style = HealthogramTheme.typography.bodySmall,
                color = HealthogramTheme.colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onReapply,
                enabled = canReapply,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("reapply_verification_button")
            ) {
                if (canReapply) {
                    Text("Reapply for Verification", fontWeight = FontWeight.Bold)
                } else {
                    val remainingMins = ((profile.nextAllowedSubmissionAt ?: 0L) - System.currentTimeMillis()) / 60000
                    Text("Cooldown Active ($remainingMins mins remaining)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * VerificationHistoryPage
 * Step 07 Section 22: Displays complete history of applications and audit trail.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationHistoryPage(
    user: User,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = remember { VerificationEngine.getInstance() }
    val applicationsState by engine.applications.collectAsState()
    val userApps = applicationsState.values.filter { it.uid == user.uid }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verification History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("history_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("verification_history_page")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Past Applications & Audits",
                    style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (userApps.isEmpty()) {
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
                            Text("No application history available.", color = HealthogramTheme.colors.onSurfaceVariant)
                        }
                    }
                }
            }

            items(userApps) { app ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthogramTheme.colors.divider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(app.accountType.displayName, fontWeight = FontWeight.Bold)
                            Surface(
                                color = when (app.status) {
                                    VerificationStatus.VERIFIED, VerificationStatus.APPROVED -> HealthogramTheme.colors.successContainer
                                    VerificationStatus.REJECTED -> HealthogramTheme.colors.errorContainer
                                    else -> HealthogramTheme.colors.warningContainer
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = VerificationFunctions.getVerificationStatusLabel(app.status),
                                    style = HealthogramTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text("Application ID: ${app.applicationId.take(16)}...", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.onSurfaceVariant)
                        Text("Jurisdiction: ${app.countryCode}", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/**
 * VerificationPrivacyPage
 * Step 07 Section 23: Verification privacy disclosure.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationPrivacyPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verification Privacy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("privacy_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("verification_privacy_page")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Data Protection & Privacy Policy",
                    style = HealthogramTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("1. Zero Public Storage", fontWeight = FontWeight.Bold)
                        Text("All verification identity and license files are saved in strict private storage buckets. They are never rendered in public feeds or media directories.", style = HealthogramTheme.typography.bodySmall)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = HealthogramTheme.colors.divider)

                        Text("2. Masked Identifiers", fontWeight = FontWeight.Bold)
                        Text("National ID and medical license registration numbers are tokenized. Only the final 4 digits are retained in metadata.", style = HealthogramTheme.typography.bodySmall)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = HealthogramTheme.colors.divider)

                        Text("3. Strict Role-Based Access", fontWeight = FontWeight.Bold)
                        Text("Only accredited verification reviewers with explicit custom security claims may inspect submitted credentials.", style = HealthogramTheme.typography.bodySmall)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = HealthogramTheme.colors.divider)

                        Text("4. Health Passport Isolation", fontWeight = FontWeight.Bold)
                        Text("Verification records are completely isolated from Health Passport clinical data. Verification reviewers cannot access medical histories or patient consultations.", style = HealthogramTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

/**
 * VerificationBadgeInfoPage
 * Step 07 Section 24: Standalone page detailing the verified badge meaning.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationBadgeInfoPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Verified Badge", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("badge_info_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("verification_badge_info_page")
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(HealthogramTheme.colors.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Healthogram Verified Badge", style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Official Platform Meaning",
                            style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "The Healthogram verified badge means: 'This account has passed Healthogram's platform verification process.'\n\nHealthogram verification does not replace government licensing or regulatory approval.",
                            style = HealthogramTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item {
                Text(
                    text = "What the badge does NOT mean:",
                    style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("• Government endorsement or affiliation", style = HealthogramTheme.typography.bodyMedium)
                    Text("• Medical guarantee or clinical diagnosis accuracy promise", style = HealthogramTheme.typography.bodyMedium)
                    Text("• Product or service quality guarantee", style = HealthogramTheme.typography.bodyMedium)
                    Text("• Professional competence or legal certification", style = HealthogramTheme.typography.bodyMedium)
                    Text("• Safety guarantee for procedures or medications", style = HealthogramTheme.typography.bodyMedium)
                    Text("• Approval of medical advice or opinions", style = HealthogramTheme.typography.bodyMedium)
                }
            }
        }
    }
}

/**
 * VerificationDashboardPage
 * Step 07 Section 25 & 46: Staff Reviewer and Manager operations portal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationDashboardPage(
    staffUser: User,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = remember { VerificationEngine.getInstance() }
    val queueState by engine.queue.collectAsState()
    val applicationsState by engine.applications.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pending Queue (${queueState.count { it.status == "unassigned" }})", "In Review (${queueState.count { it.status == "in_review" }})", "Completed (${queueState.count { it.status == "completed" }})")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verification Portal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("dashboard_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("verification_dashboard_page")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, style = HealthogramTheme.typography.labelSmall) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val filteredQueue = when (selectedTab) {
                    0 -> queueState.filter { it.status == "unassigned" }
                    1 -> queueState.filter { it.status == "in_review" }
                    else -> queueState.filter { it.status == "completed" }
                }

                if (filteredQueue.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No applications in this category.", color = HealthogramTheme.colors.onSurfaceVariant)
                        }
                    }
                }

                items(filteredQueue) { item ->
                    val app = applicationsState[item.applicationId]
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HealthogramTheme.colors.divider),
                        modifier = Modifier.fillMaxWidth().testTag("queue_item_${item.queueId}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(item.accountType.displayName, fontWeight = FontWeight.Bold)
                                Text("Priority: ${item.priority.uppercase()}", style = HealthogramTheme.typography.labelSmall, color = HealthogramTheme.colors.primary)
                            }
                            Text("Jurisdiction: ${item.countryCode}", style = HealthogramTheme.typography.bodySmall)
                            Text("Applicant UID: ${item.uid.take(12)}...", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.onSurfaceVariant)

                            if (app != null && item.status != "completed") {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { engine.approveApplication(staffUser, app.applicationId) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = HealthogramTheme.colors.success),
                                        modifier = Modifier.testTag("staff_approve_btn_${app.applicationId}")
                                    ) {
                                        Text("Approve", style = HealthogramTheme.typography.labelSmall)
                                    }
                                    OutlinedButton(
                                        onClick = { engine.requestAdditionalInformation(staffUser, app.applicationId, "Please upload clearer medical license image.") },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Request Info", style = HealthogramTheme.typography.labelSmall)
                                    }
                                    OutlinedButton(
                                        onClick = { engine.rejectApplication(staffUser, app.applicationId, RejectionReasonCode.DOCUMENT_UNREADABLE) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HealthogramTheme.colors.error)
                                    ) {
                                        Text("Reject", style = HealthogramTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
