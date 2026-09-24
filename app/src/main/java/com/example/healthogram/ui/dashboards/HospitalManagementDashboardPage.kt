package com.example.healthogram.ui.dashboards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.profile.components.EmptyProfileState
import com.example.healthogram.profile.model.OrganizationProfile
import com.example.healthogram.profile.repository.ProfileRepository

/**
 * Hospital Management Dashboard Foundation (Section 18).
 * Sections: Overview, Appointments, Doctors, Departments, Services, Patients, Schedules, Consultations, Staff, Content, AI Studio, Analytics, Settings.
 */
@Composable
fun HospitalManagementDashboardPage(
    onBack: () -> Unit,
    onNavigateToMembers: () -> Unit = {},
    onNavigateToDevices: () -> Unit = {},
    onOpenAIStudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { ProfileRepository.getInstance() }
    var orgProfile by remember { mutableStateOf<OrganizationProfile?>(null) }

    LaunchedEffect(Unit) {
        orgProfile = repository.getOrganizationProfile("org_hospital_demo")
    }

    val sections = listOf(
        "Overview", "Appointments", "Doctors", "Departments",
        "Services", "Patients", "Schedules", "Consultations",
        "Staff", "Content", "AI Studio", "Analytics", "Settings"
    )
    var activeSection by remember { mutableStateOf("Overview") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("hospital_management_dashboard")
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
                        text = orgProfile?.name ?: "Metropolitan Central Hospital",
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Hospital Center Administration",
                        style = HealthogramTheme.typography.caption,
                        color = HealthogramTheme.colors.textMuted
                    )
                }
            }
        }

        // Navigation Tabs
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
                        Text(text = section, style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
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
                            title = "Inpatient & ER Status",
                            value = "Level 1 Trauma Center Ready",
                            trendPercentage = "24/7 Active Operating Suites",
                            icon = Icons.Default.LocalHospital
                        )
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            HealthogramPrimaryButton(
                                text = "Hospital Staff",
                                onClick = onNavigateToMembers,
                                icon = Icons.Default.Badge,
                                modifier = Modifier.weight(1f),
                                size = ButtonSize.SMALL
                            )
                            HealthogramOutlineButton(
                                text = "Terminal Stations (4/8)",
                                onClick = onNavigateToDevices,
                                modifier = Modifier.weight(1f),
                                size = ButtonSize.SMALL
                            )
                        }
                    }

                    item {
                        Text("Hospital Clinical Departments", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    items(orgProfile?.departments ?: emptyList()) { dept ->
                        HealthogramBasicCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(dept.name, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text("Head: ${dept.headDoctorName} • ${dept.activeDoctorCount} Physicians", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                }
                                Icon(Icons.Default.Apartment, contentDescription = null, tint = HealthogramTheme.colors.primary)
                            }
                        }
                    }
                }

                "Departments" -> {
                    items(orgProfile?.departments ?: emptyList()) { dept ->
                        HealthogramBasicCard {
                            Column {
                                Text(dept.name, style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Chief of Department: ${dept.headDoctorName}", style = HealthogramTheme.typography.bodySmall)
                                Text("Active Physicians: ${dept.activeDoctorCount}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            }
                        }
                    }
                }

                "Staff" -> {
                    item {
                        HealthogramPrimaryButton(text = "Add Staff Member", onClick = onNavigateToMembers, icon = Icons.Default.PersonAdd)
                    }
                    items(listOf(
                        Pair("Arthur Sterling", "Chief Executive Officer (Owner)"),
                        Pair("Dr. Lisa Chang", "Chief Medical Officer (Admin)"),
                        Pair("James O'Connor", "Director of Clinical Nursing (Manager)")
                    )) { (name, role) ->
                        HealthogramBasicCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(name, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text(role, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                }
                            }
                        }
                    }
                }

                "Analytics" -> {
                    item {
                        EmptyProfileState(
                            icon = Icons.Default.Timeline,
                            title = "Hospital Bed & Flow Analytics",
                            message = "Department telemetry will populate as patient admissions and digital discharge passes are verified."
                        )
                    }
                }

                "AI Studio" -> {
                    item {
                        HealthogramBasicCard {
                            Text("Hospital Enterprise AI Services", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Hospital-wide summarization pipelines, multi-lingual patient communication sheets, and clinical trial intake aids.", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
                            Spacer(modifier = Modifier.height(12.dp))
                            HealthogramPrimaryButton(text = "Launch Hospital AI Studio", onClick = onOpenAIStudio, size = ButtonSize.SMALL)
                        }
                    }
                }

                else -> {
                    item {
                        EmptyProfileState(
                            icon = Icons.Default.CheckCircle,
                            title = "$activeSection Module Ready",
                            message = "This hospital module is configured and active."
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
