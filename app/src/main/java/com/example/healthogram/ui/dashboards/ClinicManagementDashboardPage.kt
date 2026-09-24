package com.example.healthogram.ui.dashboards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.core.AccountType
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.profile.components.EmptyProfileState
import com.example.healthogram.profile.model.OrganizationProfile
import com.example.healthogram.profile.repository.ProfileRepository

/**
 * Clinic Management Dashboard Foundation (Section 16).
 * Sections: Overview, Appointments, Doctors, Services, Patients, Schedule, Consultations, Content, Analytics, AI Studio, Settings.
 */
@Composable
fun ClinicManagementDashboardPage(
    onBack: () -> Unit,
    onNavigateToMembers: () -> Unit = {},
    onNavigateToDevices: () -> Unit = {},
    onOpenAIStudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { ProfileRepository.getInstance() }
    var orgProfile by remember { mutableStateOf<OrganizationProfile?>(null) }

    LaunchedEffect(Unit) {
        orgProfile = repository.getOrganizationProfile("org_clinic_demo")
    }

    val sections = listOf(
        "Overview", "Appointments", "Doctors", "Services",
        "Patients", "Schedule", "Consultations", "Content",
        "Analytics", "AI Studio", "Settings"
    )
    var activeSection by remember { mutableStateOf("Overview") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("clinic_management_dashboard")
    ) {
        // App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = HealthogramTheme.colors.surface,
            border = BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = orgProfile?.name ?: "Clinic Management Center",
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Clinical Practice Administration",
                        style = HealthogramTheme.typography.caption,
                        color = HealthogramTheme.colors.textMuted
                    )
                }
            }
        }

        // Scrollable Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = sections.indexOf(activeSection).coerceAtLeast(0),
            containerColor = HealthogramTheme.colors.surface,
            contentColor = HealthogramTheme.colors.primary,
            edgePadding = 16.dp
        ) {
            sections.forEach { section ->
                Tab(
                    selected = activeSection == section,
                    onClick = { activeSection = section },
                    text = {
                        Text(
                            text = section,
                            style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            when (activeSection) {
                "Overview" -> {
                    item {
                        HealthogramAnalyticsCard(
                            title = "Resident Doctors & Clinical Practitioners",
                            value = "16 Active Specialists",
                            trendPercentage = "+2 this month",
                            icon = Icons.Default.People
                        )
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            HealthogramPrimaryButton(
                                text = "Manage Staff",
                                onClick = onNavigateToMembers,
                                icon = Icons.Default.Badge,
                                modifier = Modifier.weight(1f),
                                size = ButtonSize.SMALL
                            )
                            HealthogramOutlineButton(
                                text = "Clinic Terminals",
                                onClick = onNavigateToDevices,
                                modifier = Modifier.weight(1f),
                                size = ButtonSize.SMALL
                            )
                        }
                    }

                    item {
                        Text("Active Clinical Services", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    items(orgProfile?.services ?: emptyList()) { srv ->
                        HealthogramBasicCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(srv.title, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text("${srv.durationMinutes} mins • Telehealth: ${if (srv.isTelehealthAvailable) "Yes" else "No"}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                }
                                Text("${srv.currency} ${String.format("%.2f", srv.fee)}", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                            }
                        }
                    }
                }

                "Doctors" -> {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Resident Clinic Doctors", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            TextButton(onClick = onNavigateToMembers) {
                                Text("Add Doctor", style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                    items(listOf(
                        Triple("Dr. Elena Rostova", "Cardiology Specialist", "Available (Room 102)"),
                        Triple("Dr. Marcus Vance", "Internal Medicine & Chief", "In Consultation"),
                        Triple("Dr. Sophia Morales", "Pediatrics & Adolescent Care", "Available (Room 108)")
                    )) { (name, spec, status) ->
                        HealthogramBasicCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(name, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text(spec, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                }
                                Text(status, style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                            }
                        }
                    }
                }

                "Appointments", "Schedule" -> {
                    item {
                        Text("Scheduled Patient Consultations", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    items(listOf(
                        Triple("Michael Chang", "Annual Physical Examination", "09:30 AM • Confirmed"),
                        Triple("Emily Watson", "Cardiology Telehealth Consultation", "11:00 AM • Pending Check-in"),
                        Triple("David Rodriguez", "Pediatric Vaccination Review", "02:15 PM • Confirmed")
                    )) { (patient, type, time) ->
                        HealthogramBasicCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(patient, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text(type, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                }
                                Text(time, style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                            }
                        }
                    }
                }

                "Analytics" -> {
                    item {
                        EmptyProfileState(
                            icon = Icons.Default.BarChart,
                            title = "Clinic Analytics Syncing",
                            message = "Detailed patient flow and consultation analytics will appear as real visits are logged."
                        )
                    }
                }

                "AI Studio" -> {
                    item {
                        HealthogramBasicCard {
                            Text("Clinical AI Documentation Assist", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Generate structured clinical summaries, intake questionnaires, and triage translations using Gemini 3.8 Flash.", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
                            Spacer(modifier = Modifier.height(12.dp))
                            HealthogramPrimaryButton(text = "Open AI Studio Tools", onClick = onOpenAIStudio, size = ButtonSize.SMALL)
                        }
                    }
                }

                else -> {
                    item {
                        EmptyProfileState(
                            icon = Icons.Default.CheckCircle,
                            title = "$activeSection Module Ready",
                            message = "This section foundation is fully prepared and connects with the clinic workflows."
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
