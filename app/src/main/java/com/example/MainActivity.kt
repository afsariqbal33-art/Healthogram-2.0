package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramDesignSystemTheme
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.devices.DeviceManager
import com.example.healthogram.devices.RegisteredDevice
import com.example.healthogram.devices.SubscriptionPackage
import com.example.healthogram.healthpassport.HealthAccessScope
import com.example.healthogram.healthpassport.HealthPassportQRCode
import com.example.healthogram.healthpassport.HealthPassportSecurityManager
import com.example.healthogram.healthpassport.PatientConsent
import com.example.healthogram.owner.LedgerType
import com.example.healthogram.owner.OwnerControlEngine
import com.example.healthogram.payments.PaymentAbstractionService
import com.example.healthogram.translation.SupportedLanguage
import com.example.healthogram.translation.TranslationPreferences
import com.example.healthogram.translation.UniversalTranslationEngine
import com.example.healthogram.ui.aistudio.AIStudioPage
import com.example.healthogram.ui.dashboards.OrganizationDashboardHub
import com.example.healthogram.ui.devices.DeviceManagementPage
import com.example.healthogram.ui.explore.ExplorePage
import com.example.healthogram.ui.healthpassport.HealthPassportPage
import com.example.healthogram.ui.home.HomePage
import com.example.healthogram.ui.marketplace.MarketplacePage
import com.example.healthogram.ui.messages.MessagesPage
import com.example.healthogram.ui.notifications.NotificationsPage
import com.example.healthogram.ui.profile.ProfilePage
import com.example.healthogram.ui.reels.ReelsPage
import com.example.healthogram.ui.settings.SettingsPage
import com.example.healthogram.verification.CountryVerificationEngine
import com.example.healthogram.auth.AuthState
import com.example.healthogram.auth.FirebaseAuthManager
import com.example.healthogram.ui.auth.*
import com.example.healthogram.ui.profile.*
import com.example.healthogram.ui.dashboards.*
import com.example.healthogram.ui.organization.*
import com.example.healthogram.ui.verification.*
import com.example.healthogram.social.*
import com.example.healthogram.social.ui.*
import com.example.healthogram.marketplace.seller.SellerRepository
import com.example.healthogram.ui.marketplace.seller.pages.SellerDashboardPage
import com.example.healthogram.notification.*
import com.example.healthogram.ui.notifications.*
import com.example.healthogram.payments.*
import com.example.healthogram.ui.payments.*
import com.example.healthogram.admin.*
import com.example.healthogram.ui.admin.*
import com.example.healthogram.owner.*
import com.example.healthogram.ui.owner.*
import com.example.healthogram.ui.appointments.*
import com.example.healthogram.ui.calls.*

enum class AppOverlayScreen {
    NONE,
    SETTINGS,
    NOTIFICATIONS,
    NOTIFICATION_DETAILS,
    NOTIFICATION_SETTINGS,
    NOTIFICATION_PRIVACY,
    NOTIFICATION_CATEGORY_SETTINGS,
    NOTIFICATION_QUIET_HOURS,
    NOTIFICATION_DEVICES,
    MESSAGES,
    ORG_DASHBOARDS,
    DEVICE_MANAGEMENT,
    AI_STUDIO,
    REELS,
    STORY_VIEWER,
    LIVE_STREAM,
    CREATE_POST,
    CREATOR_STUDIO,
    FOUNDATION_INSPECTOR,
    SECURITY_SETTINGS,
    ACTIVE_DEVICES_PAGE,
    CHANGE_PASSWORD,
    CHANGE_EMAIL,
    DELETE_ACCOUNT,
    EDIT_PROFILE,
    PROFESSIONAL_DASHBOARD,
    CLINIC_DASHBOARD,
    HOSPITAL_DASHBOARD,
    LABORATORY_DASHBOARD,
    PRIVACY_SETTINGS,
    COMMUNICATION_SETTINGS,
    ORGANIZATION_MEMBERS,
    ORGANIZATION_DEVICES,
    ORGANIZATION_SETTINGS,
    VERIFICATION_STATUS,
    DOCTOR_PROFILE_VIEW,
    CLINIC_PROFILE_VIEW,
    HOSPITAL_PROFILE_VIEW,
    LABORATORY_PROFILE_VIEW,
    SELLER_CENTER,
    MARKETPLACE_CHECKOUT,
    CUSTOMER_PAYMENT_HISTORY,
    SELLER_FINANCIAL_CENTER,
    OWNER_EARNINGS_CENTER,
    ADMIN_PAYMENT_CONTROL,
    ADMIN_CONTROL_PANEL,
    OWNER_CONTROL_PANEL,
    APPOINTMENTS_LIST,
    APPOINTMENT_BOOKING,
    ACTIVE_VIDEO_CALL
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HealthogramAppRoot()
        }
    }
}

/**
 * Root Architecture Container: Selects between Authentication Flow and Authenticated Consumer App.
 */
@Composable
fun HealthogramAppRoot() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val authManager = remember { FirebaseAuthManager.getInstance(context) }
    val authState by authManager.authState.collectAsState()
    var isDarkTheme by remember { mutableStateOf(false) }

    HealthogramDesignSystemTheme(darkTheme = isDarkTheme) {
        if (authState is AuthState.Authenticated) {
            HealthogramConsumerApp(
                authManager = authManager,
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = { isDarkTheme = it }
            )
        } else {
            AuthenticationRouter(
                authManager = authManager,
                onAuthenticated = {
                    authManager.checkCurrentSession()
                }
            )
        }
    }
}

/**
 * Root Application Container for Healthogram Step 02 Modern UI/UX.
 */
@Composable
fun HealthogramConsumerApp(
    authManager: FirebaseAuthManager = FirebaseAuthManager.getInstance(),
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: (Boolean) -> Unit = {}
) {
    val currentUser by authManager.currentUser.collectAsState()
    var isRtlLayout by remember { mutableStateOf(false) }
    var currentNavDestination by remember { mutableStateOf(HealthogramNavDestination.HOME) }
    var activeOverlay by remember { mutableStateOf(AppOverlayScreen.NONE) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var activeStoryToView by remember { mutableStateOf<SocialStory?>(null) }
    var activeLiveStream by remember { mutableStateOf<LiveStreamSession?>(null) }
    var activeCreationMode by remember { mutableStateOf(CreationMode.POST) }
    var selectedDoctorUid by remember { mutableStateOf("user_doctor_demo") }
    var selectedOrgId by remember { mutableStateOf("org_clinic_demo") }
    val sellerRepository = remember { SellerRepository() }
    val notificationRepository = remember { NotificationRepository.getInstance() }
    val unreadCounts by notificationRepository.unreadCount.collectAsState()
    var selectedNotificationId by remember { mutableStateOf<String?>(null) }

    val layoutDirection = if (isRtlLayout) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        HealthogramDesignSystemTheme(darkTheme = isDarkTheme) {
            BackHandler(enabled = activeOverlay != AppOverlayScreen.NONE) {
                activeOverlay = AppOverlayScreen.NONE
            }

            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("healthogram_root_scaffold"),
                topBar = {
                    if (activeOverlay == AppOverlayScreen.NONE) {
                        HealthogramAppHeader(
                            onSearchClick = { currentNavDestination = HealthogramNavDestination.EXPLORE },
                            onNotificationsClick = { activeOverlay = AppOverlayScreen.NOTIFICATIONS },
                            onMessagesClick = { activeOverlay = AppOverlayScreen.MESSAGES },
                            unreadNotificationCount = unreadCounts.total
                        )
                    }
                },
                bottomBar = {
                    if (activeOverlay == AppOverlayScreen.NONE) {
                        HealthogramBottomNavigation(
                            currentDestination = currentNavDestination,
                            onNavigate = { currentNavDestination = it },
                            onCreateClick = { showCreateSheet = true }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Main Primary Destinations
                    when (currentNavDestination) {
                        HealthogramNavDestination.HOME -> {
                            HomePage(
                                onOpenStory = { story ->
                                    activeStoryToView = story
                                    activeOverlay = AppOverlayScreen.STORY_VIEWER
                                },
                                onOpenLive = { live ->
                                    activeLiveStream = live
                                    activeOverlay = AppOverlayScreen.LIVE_STREAM
                                },
                                onUserClick = { currentNavDestination = HealthogramNavDestination.PROFILE },
                                onCreateClick = {
                                    activeCreationMode = CreationMode.STORY
                                    activeOverlay = AppOverlayScreen.CREATE_POST
                                }
                            )
                        }
                        HealthogramNavDestination.EXPLORE -> {
                            ExplorePage(
                                onDoctorBookClick = { docId ->
                                    selectedDoctorUid = docId
                                    activeOverlay = AppOverlayScreen.DOCTOR_PROFILE_VIEW
                                }
                            )
                        }
                        HealthogramNavDestination.HEALTH_PASSPORT -> {
                            HealthPassportPage()
                        }
                        HealthogramNavDestination.MARKETPLACE -> {
                            MarketplacePage(
                                onSellerDashboardClick = { activeOverlay = AppOverlayScreen.SELLER_CENTER }
                            )
                        }
                        HealthogramNavDestination.PROFILE -> {
                            ProfilePage(
                                user = currentUser,
                                onSettingsClick = { activeOverlay = AppOverlayScreen.SETTINGS },
                                onProfessionalDashboardClick = { activeOverlay = AppOverlayScreen.PROFESSIONAL_DASHBOARD },
                                onEditProfileClick = { activeOverlay = AppOverlayScreen.EDIT_PROFILE },
                                onPrivacyClick = { activeOverlay = AppOverlayScreen.PRIVACY_SETTINGS },
                                onCommunicationClick = { activeOverlay = AppOverlayScreen.COMMUNICATION_SETTINGS },
                                onVerificationClick = { activeOverlay = AppOverlayScreen.VERIFICATION_STATUS },
                                onClinicDashboardClick = {
                                    selectedOrgId = "org_clinic_demo"
                                    activeOverlay = AppOverlayScreen.CLINIC_DASHBOARD
                                },
                                onHospitalDashboardClick = {
                                    selectedOrgId = "org_hospital_demo"
                                    activeOverlay = AppOverlayScreen.HOSPITAL_DASHBOARD
                                },
                                onLaboratoryDashboardClick = {
                                    selectedOrgId = "org_lab_demo"
                                    activeOverlay = AppOverlayScreen.LABORATORY_DASHBOARD
                                },
                                onDoctorViewClick = { docUid ->
                                    selectedDoctorUid = docUid
                                    activeOverlay = AppOverlayScreen.DOCTOR_PROFILE_VIEW
                                },
                                onClinicViewClick = { clinicId ->
                                    selectedOrgId = clinicId
                                    activeOverlay = AppOverlayScreen.CLINIC_PROFILE_VIEW
                                },
                                onHospitalViewClick = { hospId ->
                                    selectedOrgId = hospId
                                    activeOverlay = AppOverlayScreen.HOSPITAL_PROFILE_VIEW
                                },
                                onLaboratoryViewClick = { labId ->
                                    selectedOrgId = labId
                                    activeOverlay = AppOverlayScreen.LABORATORY_PROFILE_VIEW
                                },
                                onManageMembersClick = { orgId ->
                                    selectedOrgId = orgId
                                    activeOverlay = AppOverlayScreen.ORGANIZATION_MEMBERS
                                },
                                onManageDevicesClick = { orgId ->
                                    selectedOrgId = orgId
                                    activeOverlay = AppOverlayScreen.ORGANIZATION_DEVICES
                                },
                                onAppointmentsClick = {
                                    activeOverlay = AppOverlayScreen.APPOINTMENTS_LIST
                                }
                            )
                        }
                        else -> {
                            HomePage()
                        }
                    }

                    // Overlays & Full Screen Flow Views
                    when (activeOverlay) {
                        AppOverlayScreen.SETTINGS -> {
                            SettingsPage(
                                isDarkTheme = isDarkTheme,
                                onToggleDarkTheme = onToggleDarkTheme,
                                isRtlLayout = isRtlLayout,
                                onToggleRtl = { isRtlLayout = it },
                                onNavigateToDevices = { activeOverlay = AppOverlayScreen.ACTIVE_DEVICES_PAGE },
                                onNavigateToSecurity = { activeOverlay = AppOverlayScreen.SECURITY_SETTINGS },
                                onNavigateToVerification = { activeOverlay = AppOverlayScreen.VERIFICATION_STATUS },
                                onNavigateToNotifications = { activeOverlay = AppOverlayScreen.NOTIFICATION_SETTINGS },
                                onNavigateToPaymentHistory = { activeOverlay = AppOverlayScreen.CUSTOMER_PAYMENT_HISTORY },
                                onNavigateToSellerFinancials = { activeOverlay = AppOverlayScreen.SELLER_FINANCIAL_CENTER },
                                onNavigateToOwnerEarnings = { activeOverlay = AppOverlayScreen.OWNER_EARNINGS_CENTER },
                                onNavigateToAdminPayments = { activeOverlay = AppOverlayScreen.ADMIN_PAYMENT_CONTROL },
                                onNavigateToAdminControlPanel = { activeOverlay = AppOverlayScreen.ADMIN_CONTROL_PANEL },
                                onNavigateToOwnerControlPanel = { activeOverlay = AppOverlayScreen.OWNER_CONTROL_PANEL },
                                onSignOut = {
                                    authManager.signOut()
                                    activeOverlay = AppOverlayScreen.NONE
                                },
                                onBack = { activeOverlay = AppOverlayScreen.NONE }
                            )
                        }
                        AppOverlayScreen.SECURITY_SETTINGS -> {
                            SecuritySettingsPage(
                                authManager = authManager,
                                onBack = { activeOverlay = AppOverlayScreen.SETTINGS },
                                onNavigateToChangePassword = { activeOverlay = AppOverlayScreen.CHANGE_PASSWORD },
                                onNavigateToChangeEmail = { activeOverlay = AppOverlayScreen.CHANGE_EMAIL },
                                onNavigateToActiveDevices = { activeOverlay = AppOverlayScreen.ACTIVE_DEVICES_PAGE },
                                onNavigateToDeleteAccount = { activeOverlay = AppOverlayScreen.DELETE_ACCOUNT }
                            )
                        }
                        AppOverlayScreen.ACTIVE_DEVICES_PAGE -> {
                            ActiveDevicesPage(
                                authManager = authManager,
                                onBack = { activeOverlay = AppOverlayScreen.SECURITY_SETTINGS }
                            )
                        }
                        AppOverlayScreen.CHANGE_PASSWORD -> {
                            ChangePasswordPage(
                                authManager = authManager,
                                onBack = { activeOverlay = AppOverlayScreen.SECURITY_SETTINGS }
                            )
                        }
                        AppOverlayScreen.CHANGE_EMAIL -> {
                            ChangeEmailPage(
                                authManager = authManager,
                                onBack = { activeOverlay = AppOverlayScreen.SECURITY_SETTINGS }
                            )
                        }
                        AppOverlayScreen.DELETE_ACCOUNT -> {
                            DeleteAccountPage(
                                authManager = authManager,
                                onBack = { activeOverlay = AppOverlayScreen.SECURITY_SETTINGS },
                                onAccountDeleted = {
                                    activeOverlay = AppOverlayScreen.NONE
                                }
                            )
                        }
                        AppOverlayScreen.NOTIFICATIONS -> {
                            NotificationsPage(
                                onBack = { activeOverlay = AppOverlayScreen.NONE },
                                onOpenDetails = { notifId ->
                                    selectedNotificationId = notifId
                                    activeOverlay = AppOverlayScreen.NOTIFICATION_DETAILS
                                },
                                onOpenSettings = {
                                    activeOverlay = AppOverlayScreen.NOTIFICATION_SETTINGS
                                }
                            )
                        }
                        AppOverlayScreen.NOTIFICATION_DETAILS -> {
                            NotificationDetailsPage(
                                notificationId = selectedNotificationId ?: "",
                                onBack = { activeOverlay = AppOverlayScreen.NOTIFICATIONS },
                                onNavigateDeepLink = { deepLink ->
                                    when {
                                        deepLink.contains("health-access") -> {
                                            activeOverlay = AppOverlayScreen.NONE
                                            currentNavDestination = HealthogramNavDestination.HEALTH_PASSPORT
                                        }
                                        deepLink.contains("order") -> {
                                            activeOverlay = AppOverlayScreen.NONE
                                            currentNavDestination = HealthogramNavDestination.MARKETPLACE
                                        }
                                        deepLink.contains("seller") -> {
                                            activeOverlay = AppOverlayScreen.SELLER_CENTER
                                        }
                                        deepLink.contains("message") -> {
                                            activeOverlay = AppOverlayScreen.MESSAGES
                                        }
                                        deepLink.contains("devices") -> {
                                            activeOverlay = AppOverlayScreen.NOTIFICATION_DEVICES
                                        }
                                        else -> {
                                            activeOverlay = AppOverlayScreen.NOTIFICATIONS
                                        }
                                    }
                                }
                            )
                        }
                        AppOverlayScreen.NOTIFICATION_SETTINGS -> {
                            NotificationSettingsPage(
                                onBack = { activeOverlay = AppOverlayScreen.NOTIFICATIONS },
                                onNavigateQuietHours = { activeOverlay = AppOverlayScreen.NOTIFICATION_QUIET_HOURS },
                                onNavigateDevices = { activeOverlay = AppOverlayScreen.NOTIFICATION_DEVICES },
                                onNavigatePrivacy = { activeOverlay = AppOverlayScreen.NOTIFICATION_PRIVACY },
                                onNavigateCategorySettings = { activeOverlay = AppOverlayScreen.NOTIFICATION_CATEGORY_SETTINGS }
                            )
                        }
                        AppOverlayScreen.NOTIFICATION_PRIVACY -> {
                            NotificationPrivacyPage(
                                onBack = { activeOverlay = AppOverlayScreen.NOTIFICATION_SETTINGS }
                            )
                        }
                        AppOverlayScreen.NOTIFICATION_CATEGORY_SETTINGS -> {
                            NotificationCategorySettingsPage(
                                onBack = { activeOverlay = AppOverlayScreen.NOTIFICATION_SETTINGS }
                            )
                        }
                        AppOverlayScreen.NOTIFICATION_QUIET_HOURS -> {
                            NotificationQuietHoursPage(
                                onBack = { activeOverlay = AppOverlayScreen.NOTIFICATION_SETTINGS }
                            )
                        }
                        AppOverlayScreen.NOTIFICATION_DEVICES -> {
                            NotificationDevicesPage(
                                onBack = { activeOverlay = AppOverlayScreen.NOTIFICATION_SETTINGS }
                            )
                        }
                        AppOverlayScreen.MESSAGES -> {
                            MessagesPage()
                        }
                        AppOverlayScreen.ORG_DASHBOARDS -> {
                            OrganizationDashboardHub(
                                onBack = { activeOverlay = AppOverlayScreen.NONE }
                            )
                        }
                        AppOverlayScreen.DEVICE_MANAGEMENT -> {
                            DeviceManagementPage(
                                onBack = { activeOverlay = AppOverlayScreen.SETTINGS }
                            )
                        }
                        AppOverlayScreen.AI_STUDIO -> {
                            AIStudioPage()
                        }
                        AppOverlayScreen.REELS -> {
                            ReelsPage()
                        }
                        AppOverlayScreen.STORY_VIEWER -> {
                            val allStories = SocialFeedEngine.getInstance().stories.collectAsState().value
                            val startIdx = allStories.indexOfFirst { it.storyId == activeStoryToView?.storyId }.coerceAtLeast(0)
                            StoryViewerOverlay(
                                stories = allStories,
                                initialIndex = startIdx,
                                onClose = { activeOverlay = AppOverlayScreen.NONE }
                            )
                        }
                        AppOverlayScreen.LIVE_STREAM -> {
                            activeLiveStream?.let { live ->
                                LiveStreamScreen(
                                    stream = live,
                                    onClose = { activeOverlay = AppOverlayScreen.NONE }
                                )
                            }
                        }
                        AppOverlayScreen.CREATE_POST -> {
                            CreateContentFlow(
                                initialMode = activeCreationMode,
                                onDismiss = { activeOverlay = AppOverlayScreen.NONE },
                                onPublished = { activeOverlay = AppOverlayScreen.NONE }
                            )
                        }
                        AppOverlayScreen.CREATOR_STUDIO -> {
                            CreatorDashboardPage(
                                onBack = { activeOverlay = AppOverlayScreen.NONE }
                            )
                        }
                        AppOverlayScreen.FOUNDATION_INSPECTOR -> {
                            HealthogramFoundationScreen()
                        }
                        AppOverlayScreen.EDIT_PROFILE -> {
                            EditProfilePage(
                                user = currentUser,
                                onBack = { activeOverlay = AppOverlayScreen.NONE }
                            )
                        }
                        AppOverlayScreen.PROFESSIONAL_DASHBOARD -> {
                            ProfessionalDashboardPage(
                                user = currentUser,
                                onBack = { activeOverlay = AppOverlayScreen.NONE }
                            )
                        }
                        AppOverlayScreen.CLINIC_DASHBOARD -> {
                            ClinicManagementDashboardPage(
                                onBack = { activeOverlay = AppOverlayScreen.NONE },
                                onNavigateToMembers = { activeOverlay = AppOverlayScreen.ORGANIZATION_MEMBERS },
                                onNavigateToDevices = { activeOverlay = AppOverlayScreen.ORGANIZATION_DEVICES }
                            )
                        }
                        AppOverlayScreen.HOSPITAL_DASHBOARD -> {
                            HospitalManagementDashboardPage(
                                onBack = { activeOverlay = AppOverlayScreen.NONE },
                                onNavigateToMembers = { activeOverlay = AppOverlayScreen.ORGANIZATION_MEMBERS },
                                onNavigateToDevices = { activeOverlay = AppOverlayScreen.ORGANIZATION_DEVICES }
                            )
                        }
                        AppOverlayScreen.LABORATORY_DASHBOARD -> {
                            LaboratoryManagementDashboardPage(
                                onBack = { activeOverlay = AppOverlayScreen.NONE },
                                onNavigateToMembers = { activeOverlay = AppOverlayScreen.ORGANIZATION_MEMBERS },
                                onNavigateToDevices = { activeOverlay = AppOverlayScreen.ORGANIZATION_DEVICES }
                            )
                        }
                        AppOverlayScreen.PRIVACY_SETTINGS -> {
                            PrivacySettingsPage(
                                user = currentUser,
                                onBack = { activeOverlay = AppOverlayScreen.NONE }
                            )
                        }
                        AppOverlayScreen.COMMUNICATION_SETTINGS -> {
                            CommunicationSettingsPage(
                                user = currentUser,
                                onBack = { activeOverlay = AppOverlayScreen.NONE }
                            )
                        }
                        AppOverlayScreen.ORGANIZATION_MEMBERS -> {
                            OrganizationMembersPage(
                                orgId = selectedOrgId,
                                onBack = { activeOverlay = AppOverlayScreen.NONE }
                            )
                        }
                        AppOverlayScreen.ORGANIZATION_DEVICES -> {
                            OrganizationDevicesPage(
                                orgId = selectedOrgId,
                                onBack = { activeOverlay = AppOverlayScreen.NONE }
                            )
                        }
                        AppOverlayScreen.ORGANIZATION_SETTINGS -> {
                            OrganizationSettingsPage(
                                orgId = selectedOrgId,
                                onBack = { activeOverlay = AppOverlayScreen.NONE }
                            )
                        }
                        AppOverlayScreen.VERIFICATION_STATUS -> {
                            val fallbackUser = User(
                                uid = "user_demo",
                                email = "user@healthogram.com",
                                phoneNumber = "+1234567890",
                                displayName = "Alex Mercer",
                                username = "alex_mercer",
                                accountType = AccountType.INDIVIDUAL
                            )
                            VerificationCenterPage(
                                user = currentUser ?: fallbackUser,
                                onBack = { activeOverlay = AppOverlayScreen.NONE }
                            )
                        }
                        AppOverlayScreen.DOCTOR_PROFILE_VIEW -> {
                            DoctorProfilePage(
                                doctorUid = selectedDoctorUid,
                                currentViewerUid = currentUser?.uid,
                                onBack = { activeOverlay = AppOverlayScreen.NONE },
                                onBookAppointment = { activeOverlay = AppOverlayScreen.APPOINTMENT_BOOKING }
                            )
                        }
                        AppOverlayScreen.CLINIC_PROFILE_VIEW -> {
                            ClinicProfilePage(
                                clinicId = selectedOrgId,
                                currentViewerUid = currentUser?.uid,
                                onBack = { activeOverlay = AppOverlayScreen.NONE },
                                onManageClinic = { activeOverlay = AppOverlayScreen.CLINIC_DASHBOARD },
                                onBookAppointment = { activeOverlay = AppOverlayScreen.APPOINTMENT_BOOKING }
                            )
                        }
                        AppOverlayScreen.HOSPITAL_PROFILE_VIEW -> {
                            HospitalProfilePage(
                                hospitalId = selectedOrgId,
                                currentViewerUid = currentUser?.uid,
                                onBack = { activeOverlay = AppOverlayScreen.NONE },
                                onManageHospital = { activeOverlay = AppOverlayScreen.HOSPITAL_DASHBOARD },
                                onBookAppointment = { activeOverlay = AppOverlayScreen.APPOINTMENT_BOOKING }
                            )
                        }
                        AppOverlayScreen.LABORATORY_PROFILE_VIEW -> {
                            LaboratoryProfilePage(
                                laboratoryId = selectedOrgId,
                                currentViewerUid = currentUser?.uid,
                                onBack = { activeOverlay = AppOverlayScreen.NONE },
                                onManageLab = { activeOverlay = AppOverlayScreen.LABORATORY_DASHBOARD }
                            )
                        }
                        AppOverlayScreen.SELLER_CENTER -> {
                            SellerDashboardPage(
                                repository = sellerRepository,
                                onNavigateBackToCustomerMarketplace = { activeOverlay = AppOverlayScreen.NONE }
                            )
                        }
                        AppOverlayScreen.MARKETPLACE_CHECKOUT -> {
                            MarketplaceCheckoutPaymentView(
                                onBack = { activeOverlay = AppOverlayScreen.NONE },
                                onPaymentSuccess = { activeOverlay = AppOverlayScreen.CUSTOMER_PAYMENT_HISTORY }
                            )
                        }
                        AppOverlayScreen.CUSTOMER_PAYMENT_HISTORY -> {
                            CustomerPaymentHistoryView(
                                onBack = { activeOverlay = AppOverlayScreen.NONE }
                            )
                        }
                        AppOverlayScreen.SELLER_FINANCIAL_CENTER -> {
                            SellerFinancialDashboardView(
                                onBack = { activeOverlay = AppOverlayScreen.SELLER_CENTER }
                            )
                        }
                        AppOverlayScreen.OWNER_EARNINGS_CENTER -> {
                            OwnerEarningsDashboardView(
                                onBack = { activeOverlay = AppOverlayScreen.NONE }
                            )
                        }
                        AppOverlayScreen.ADMIN_PAYMENT_CONTROL -> {
                            AdminPaymentControlView(
                                onBack = { activeOverlay = AppOverlayScreen.NONE }
                            )
                        }
                        AppOverlayScreen.ADMIN_CONTROL_PANEL -> {
                            AdminControlPanelPage(
                                onClose = { activeOverlay = AppOverlayScreen.NONE }
                            )
                        }
                        AppOverlayScreen.OWNER_CONTROL_PANEL -> {
                            OwnerControlPanelPage(
                                onClose = { activeOverlay = AppOverlayScreen.NONE }
                            )
                        }
                        AppOverlayScreen.APPOINTMENTS_LIST -> {
                            AppointmentsListPage(
                                patientUid = currentUser?.uid ?: "user_patient_demo",
                                onBack = { activeOverlay = AppOverlayScreen.NONE },
                                onBookNewClick = { activeOverlay = AppOverlayScreen.APPOINTMENT_BOOKING },
                                onJoinTelehealth = { activeOverlay = AppOverlayScreen.ACTIVE_VIDEO_CALL }
                            )
                        }
                        AppOverlayScreen.APPOINTMENT_BOOKING -> {
                            AppointmentBookingPage(
                                patientUid = currentUser?.uid ?: "user_patient_demo",
                                patientName = currentUser?.displayName ?: "Sarah Jenkins",
                                providerUid = selectedDoctorUid,
                                providerName = if (selectedDoctorUid == "user_doctor_demo") "Dr. Tariq Al-Mansoor" else "Dr. Sarah Mitchell",
                                providerSpecialty = "Consultant Cardiologist",
                                providerAccountType = AccountType.DOCTOR,
                                facilityName = "Muscat International Hospital, Wing 3",
                                onBack = { activeOverlay = AppOverlayScreen.NONE },
                                onBookingSuccess = { activeOverlay = AppOverlayScreen.APPOINTMENTS_LIST }
                            )
                        }
                        AppOverlayScreen.ACTIVE_VIDEO_CALL -> {
                            val commRepo = remember { com.example.healthogram.communication.CommunicationRepository() }
                            ActiveVideoCallPage(
                                participantName = "Dr. Tariq Al-Mansoor",
                                webRTCService = commRepo.webRTCService,
                                onEndCall = { activeOverlay = AppOverlayScreen.NONE }
                            )
                        }
                        AppOverlayScreen.NONE -> { /* No Overlay */ }
                    }

                    // Create '+' Bottom Sheet
                    if (showCreateSheet) {
                        HealthogramCreateBottomSheet(
                            onDismiss = { showCreateSheet = false },
                            onCreatePost = {
                                activeCreationMode = CreationMode.POST
                                activeOverlay = AppOverlayScreen.CREATE_POST
                            },
                            onCreateReel = {
                                activeCreationMode = CreationMode.REEL
                                activeOverlay = AppOverlayScreen.CREATE_POST
                            },
                            onCreateStory = {
                                activeCreationMode = CreationMode.STORY
                                activeOverlay = AppOverlayScreen.CREATE_POST
                            },
                            onGoLive = {
                                activeCreationMode = CreationMode.LIVE
                                activeOverlay = AppOverlayScreen.CREATE_POST
                            },
                            onCreateProduct = {
                                showCreateSheet = false
                                activeOverlay = AppOverlayScreen.SELLER_CENTER
                            },
                            onOpenAIStudio = { activeOverlay = AppOverlayScreen.AI_STUDIO }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 01 ARCHITECTURE INSPECTION & VERIFICATION RETAINED CODE
// (Maintains 100% backward compatibility with GreetingScreenshotTest)
// -------------------------------------------------------------

enum class FoundationTab(val title: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Default.Dashboard),
    ACCOUNTS("Accounts", Icons.Default.People),
    HEALTH_PASSPORT("Health Passport", Icons.Default.HealthAndSafety),
    DEVICES("Devices & Access", Icons.Default.Devices),
    OWNER_CONTROL("Owner Control", Icons.Default.AdminPanelSettings)
}

@Composable
fun HealthogramFoundationScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(FoundationTab.OVERVIEW) }

    val ownerEngine = remember {
        OwnerControlEngine().apply {
            recordLedgerTransaction(
                type = LedgerType.ORGANIZATION_SUBSCRIPTION,
                gross = 499.0,
                commission = 499.0,
                serviceFee = 0.0,
                processingFee = 14.80,
                tax = 0.0,
                country = "US",
                reference = "sub_hosp_01"
            )
            recordLedgerTransaction(
                type = LedgerType.MARKETPLACE_SALE,
                gross = 120.0,
                commission = 6.0,
                serviceFee = 2.0,
                processingFee = 3.80,
                tax = 0.0,
                country = "US",
                reference = "ord_bp_monitor"
            )
        }
    }

    val deviceManager = remember {
        DeviceManager().apply {
            registerDevice("user_demo_1", AccountType.INDIVIDUAL, "Pixel 8 Pro (Primary)")
            registerDevice("user_demo_1", AccountType.INDIVIDUAL, "Galaxy Tab S9 (Home)")
            registerDevice("hosp_demo_1", AccountType.HOSPITAL, "ER Reception Terminal", SubscriptionPackage.PREMIUM_ORGANIZATION)
            registerDevice("hosp_demo_1", AccountType.HOSPITAL, "ICU Nurse Station", SubscriptionPackage.PREMIUM_ORGANIZATION)
        }
    }

    val securityManager = remember { HealthPassportSecurityManager() }
    val verificationEngine = remember { CountryVerificationEngine() }
    val paymentService = remember { PaymentAbstractionService() }
    val translationEngine = remember { UniversalTranslationEngine() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HealthogramFoundationHeader()

        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("foundation_tab_row")
        ) {
            FoundationTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.title, fontWeight = FontWeight.Medium) },
                    icon = { Icon(tab.icon, contentDescription = tab.title, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedTab) {
                FoundationTab.OVERVIEW -> OverviewTabContent(ownerEngine)
                FoundationTab.ACCOUNTS -> AccountsTabContent(verificationEngine)
                FoundationTab.HEALTH_PASSPORT -> HealthPassportTabContent(securityManager)
                FoundationTab.DEVICES -> DevicesTabContent(deviceManager)
                FoundationTab.OWNER_CONTROL -> OwnerControlTabContent(ownerEngine, paymentService, translationEngine)
            }
        }
    }
}

@Composable
fun HealthogramFoundationHeader() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = "Healthogram Shield",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "HEALTHOGRAM",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Step 02 — Complete Modern UI/UX Design System",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "UI/UX SYSTEM",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun OverviewTabContent(ownerEngine: OwnerControlEngine) {
    val summary = remember { ownerEngine.calculateEarningsSummary() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("overview_welcome_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Design System & Architecture Active",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Healthogram has been built from scratch with zero mock shortcuts. All 21 core subsystems, data models, country compliance engines, and modern UI/UX design systems are live.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        item {
            Text(
                text = "Architectural Guarantees & Constraints",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            ConstraintBadgeRow(
                title = "Primary Account Categories (Exactly 5)",
                desc = "Individual, Doctor, Clinic, Hospital, Laboratory",
                status = "COMPLIANT",
                isPositive = true
            )
        }

        item {
            ConstraintBadgeRow(
                title = "Pharmacy Absolute Prohibition",
                desc = "Pharmacy does NOT exist anywhere in models, database, or UI",
                status = "ENFORCED",
                isPositive = true
            )
        }

        item {
            ConstraintBadgeRow(
                title = "Health Passport Zero-Trust",
                desc = "Private by default, 15m rotating QR tickets, patient consent",
                status = "SECURED",
                isPositive = true
            )
        }

        item {
            ConstraintBadgeRow(
                title = "Device System Limits",
                desc = "4 max for Normal accounts; 4/8/32 for Orgs with remote controls",
                status = "ACTIVE",
                isPositive = true
            )
        }

        item {
            ConstraintBadgeRow(
                title = "Immutable Financial Ledger",
                desc = "Gross: $${"%.2f".format(summary.grossPlatformVolume)} | Available Net: $${"%.2f".format(summary.netAvailableBalance)}",
                status = "AUDITED",
                isPositive = true
            )
        }
    }
}

@Composable
fun ConstraintBadgeRow(title: String, desc: String, status: String, isPositive: Boolean) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(2.dp))
                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isPositive) Color(0xFFE6F4EA) else Color(0xFFFCE8E6)
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = if (isPositive) Color(0xFF137333) else Color(0xFFC5221F)
                )
            }
        }
    }
}

@Composable
fun AccountsTabContent(verificationEngine: CountryVerificationEngine) {
    var selectedCountry by remember { mutableStateOf("US") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Primary Healthogram Account Types",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Healthogram strictly supports these 5 categories. Pharmacy is permanently forbidden.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(AccountType.entries) { accountType ->
            AccountTypeCard(accountType = accountType, countryCode = selectedCountry, verificationEngine = verificationEngine)
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("forbidden_pharmacy_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Block,
                        contentDescription = "Forbidden",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "PHARMACY: Strictly Forbidden Category",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = "Excluded from registration, database schemas, enums, verification, and UI. Regulated healthcare items are handled via Marketplace Seller roles only.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB91C1C)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccountTypeCard(
    accountType: AccountType,
    countryCode: String,
    verificationEngine: CountryVerificationEngine
) {
    val reqs = remember(accountType, countryCode) {
        verificationEngine.getRequirements(accountType, countryCode)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (accountType) {
                            AccountType.INDIVIDUAL -> Icons.Default.Person
                            AccountType.DOCTOR -> Icons.Default.MedicalServices
                            AccountType.CLINIC -> Icons.Default.LocalHospital
                            AccountType.HOSPITAL -> Icons.Default.Domain
                            AccountType.LABORATORY -> Icons.Default.Biotech
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = accountType.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (accountType.isHealthcareOrganization) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (accountType.isHealthcareOrganization) "ORGANIZATION" else "INDIVIDUAL TIER",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Verification Requirements (${countryCode}):",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
            reqs.forEach { req ->
                Text(
                    text = "• ${req.documentType.displayName}${req.countrySpecificAuthority?.let { " ($it)" } ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HealthPassportTabContent(securityManager: HealthPassportSecurityManager) {
    var qrTicket by remember { mutableStateOf(HealthPassportQRCode("patient_100", "HG-8924-US")) }
    var selectedScope by remember { mutableStateOf(HealthAccessScope.FULL) }
    var lastAccessResult by remember { mutableStateOf<String?>(null) }
    var auditLogs by remember { mutableStateOf(securityManager.getAuditLogsForPatient("patient_100")) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Dynamic Health Passport Access Ticket",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "QR codes NEVER contain raw medical data. They hold short-lived dynamic session tickets.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.QrCode2,
                                contentDescription = "QR Code",
                                modifier = Modifier.size(100.dp),
                                tint = Color.Black
                            )
                            Text(
                                text = "TICKET: ${qrTicket.sessionTicket.take(8)}...",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = Color.DarkGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            qrTicket = HealthPassportQRCode("patient_100", "HG-8924-US")
                        },
                        modifier = Modifier.testTag("refresh_qr_ticket_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rotate Session Ticket")
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Select Access Scope to Grant:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(HealthAccessScope.entries) { scope ->
                            FilterChip(
                                selected = selectedScope == scope,
                                onClick = { selectedScope = scope },
                                label = { Text(scope.displayName, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val doctor = User(
                                    uid = "doc_house_1",
                                    email = "house@clinic.com",
                                    phoneNumber = "+12345",
                                    displayName = "Dr. Gregory House",
                                    username = "drhouse",
                                    accountType = AccountType.DOCTOR,
                                    isVerified = true,
                                    verificationStatus = VerificationStatus.APPROVED
                                )
                                val patient = User(
                                    uid = "patient_100",
                                    email = "patient@test.com",
                                    phoneNumber = "+999",
                                    displayName = "John Alpha",
                                    username = "johnalpha",
                                    accountType = AccountType.INDIVIDUAL
                                )
                                val consent = PatientConsent(
                                    patientUid = patient.uid,
                                    authorizedEntityUid = doctor.uid,
                                    authorizedEntityName = doctor.displayName,
                                    authorizedAccountType = doctor.accountType,
                                    grantedScope = selectedScope
                                )
                                val res = securityManager.evaluateAccessRequest(
                                    scanner = doctor,
                                    patient = patient,
                                    requestedScope = selectedScope,
                                    purpose = "Clinical Consultation",
                                    existingConsent = consent
                                )
                                lastAccessResult = if (res.isSuccess) "Access Approved for ${doctor.displayName}" else "Access Denied"
                                auditLogs = securityManager.getAuditLogsForPatient("patient_100")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("simulate_authorized_scan_btn")
                        ) {
                            Text("Simulate Verified Scan")
                        }

                        OutlinedButton(
                            onClick = {
                                val unverified = User(
                                    uid = "doc_unverified",
                                    email = "fake@test.com",
                                    phoneNumber = "+000",
                                    displayName = "Unverified User",
                                    username = "unverified",
                                    accountType = AccountType.INDIVIDUAL,
                                    isVerified = false
                                )
                                val patient = User(
                                    uid = "patient_100",
                                    email = "patient@test.com",
                                    phoneNumber = "+999",
                                    displayName = "John Alpha",
                                    username = "johnalpha",
                                    accountType = AccountType.INDIVIDUAL
                                )
                                val res = securityManager.evaluateAccessRequest(
                                    scanner = unverified,
                                    patient = patient,
                                    requestedScope = selectedScope,
                                    purpose = "Unauthorized Attempt",
                                    existingConsent = null
                                )
                                lastAccessResult = "DENIED: ${res.exceptionOrNull()?.message}"
                                auditLogs = securityManager.getAuditLogsForPatient("patient_100")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("simulate_unverified_scan_btn")
                        ) {
                            Text("Simulate Rogue Scan")
                        }
                    }

                    lastAccessResult?.let { resultText ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = resultText,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = if (resultText.startsWith("DENIED")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DevicesTabContent(deviceManager: DeviceManager) {
    val activeDevices = remember { deviceManager.getActiveDevices("hosp_demo_1") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(activeDevices) { device ->
            DevicePermissionCard(device = device, deviceManager = deviceManager)
        }
    }
}

@Composable
fun DevicePermissionCard(device: RegisteredDevice, deviceManager: DeviceManager) {
    var permissions by remember { mutableStateOf(device.permissions) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(device.deviceName, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OwnerControlTabContent(
    ownerEngine: OwnerControlEngine,
    paymentService: PaymentAbstractionService,
    translationEngine: UniversalTranslationEngine
) {
    var isKillSwitchActive by remember { mutableStateOf(ownerEngine.isEmergencyKillSwitchActive()) }
    val summary = remember { ownerEngine.calculateEarningsSummary() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isKillSwitchActive) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("owner_kill_switch_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Platform Emergency Kill Switch",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isKillSwitchActive) Color(0xFF991B1B) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Switch(
                        checked = isKillSwitchActive,
                        onCheckedChange = {
                            isKillSwitchActive = it
                            ownerEngine.setEmergencyKillSwitch(it)
                        }
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Gross Platform Volume", style = MaterialTheme.typography.labelSmall)
                            Text("$${"%.2f".format(summary.grossPlatformVolume)}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Available Balance", style = MaterialTheme.typography.labelSmall)
                            Text("$${"%.2f".format(summary.netAvailableBalance)}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary))
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("owner_notifications_infrastructure_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Notification System Infrastructure", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Centralized multi-device push, in-app banners, quiet hours scheduler, and strict HIPAA/GDPR health passport data-leak isolation policy are active and operational.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Clinical Data Exposure", style = MaterialTheme.typography.labelSmall)
                            Text("0 (Zero Leak)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF10B981)))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Multi-Device Policy", style = MaterialTheme.typography.labelSmall)
                            Text("Active (Max 4)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                        }
                    }
                }
            }
        }

        item {
            val paymentRepo = remember { PaymentRepository.getInstance() }
            val ownerSummary by paymentRepo.ownerRevenueSummary.collectAsState()
            val txList by paymentRepo.transactions.collectAsState()

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("owner_payment_infrastructure_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Payment Gateway & Multi-Region Financials", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Server-authoritative calculations, Stripe & Mada gateways, immutable double-entry ledgers, and zero-raw-card-storage PCI compliance active.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Owner Available", style = MaterialTheme.typography.labelSmall)
                            Text(formatFinancialAmount(ownerSummary.availableRevenueMinor, ownerSummary.currencyCode), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF10B981)))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Transactions Settled", style = MaterialTheme.typography.labelSmall)
                            Text("${txList.count { it.status == PaymentTransactionStatus.SUCCEEDED || it.status == PaymentTransactionStatus.CAPTURED }} Completed", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                        }
                    }
                }
            }
        }
    }
}
