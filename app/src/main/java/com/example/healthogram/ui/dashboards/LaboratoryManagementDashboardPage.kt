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
import com.example.healthogram.organization.LabTestItem
import com.example.healthogram.profile.components.EmptyProfileState
import com.example.healthogram.profile.model.OrganizationProfile
import com.example.healthogram.profile.repository.ProfileRepository

/**
 * Laboratory Management Dashboard Foundation (Section 20).
 * Sections: Overview, Tests, Services, Pricing, Orders/Requests, Patients, Reports, Doctors, Appointments/Bookings, Content, Analytics, AI Tools, Settings.
 *
 * Strict Rule: Laboratory accounts manage diagnostic tests, turnaround times, and verified report distribution.
 * Laboratory accounts DO NOT own personal patient health passports.
 */
@Composable
fun LaboratoryManagementDashboardPage(
    onBack: () -> Unit,
    onNavigateToMembers: () -> Unit = {},
    onNavigateToDevices: () -> Unit = {},
    onOpenAIStudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { ProfileRepository.getInstance() }
    var orgProfile by remember { mutableStateOf<OrganizationProfile?>(null) }

    LaunchedEffect(Unit) {
        orgProfile = repository.getOrganizationProfile("org_lab_demo")
    }

    val sections = listOf(
        "Overview", "Tests", "Services", "Pricing",
        "Orders/Requests", "Patients", "Reports", "Doctors",
        "Appointments/Bookings", "Content", "Analytics", "AI Tools", "Settings"
    )
    var activeSection by remember { mutableStateOf("Overview") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("laboratory_management_dashboard")
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
                        text = orgProfile?.name ?: "Apex Precision Diagnostics",
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Clinical Diagnostic Laboratory Management",
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
                            title = "Diagnostic Processing Status",
                            value = "CAP & CLIA Accredited Laboratory",
                            trendPercentage = "Average Turnaround: 4.8 Hours",
                            icon = Icons.Default.Biotech
                        )
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            HealthogramPrimaryButton(
                                text = "Add Lab Test",
                                onClick = { activeSection = "Tests" },
                                icon = Icons.Default.AddCircle,
                                modifier = Modifier.weight(1f),
                                size = ButtonSize.SMALL
                            )
                            HealthogramOutlineButton(
                                text = "Lab Technologists",
                                onClick = onNavigateToMembers,
                                modifier = Modifier.weight(1f),
                                size = ButtonSize.SMALL
                            )
                        }
                    }

                    item {
                        Text("Active Diagnostic Test Catalog", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    items(orgProfile?.testCatalog ?: emptyList()) { test ->
                        HealthogramBasicCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(test.testName, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text("Code: ${test.testCode} • Sample: ${test.sampleType} • Fasting: ${if (test.requiresFasting) "Yes" else "No"}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                    Text("TAT: ${test.standardTurnaroundHours}h", style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                                }
                                Text("${test.currency} ${String.format("%.2f", test.price)}", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                            }
                        }
                    }
                }

                "Tests", "Pricing" -> {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Standard Test Directory", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            TextButton(onClick = { /* add test */ }) {
                                Text("+ Add Item", style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                    items(orgProfile?.testCatalog ?: emptyList()) { test ->
                        HealthogramBasicCard {
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(test.testName, style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("${test.currency} ${test.price}", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Sample Type: ${test.sampleType}", style = HealthogramTheme.typography.bodySmall)
                                Text("Standard Turnaround: ${test.standardTurnaroundHours} hours", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            }
                        }
                    }
                }

                "Reports" -> {
                    item {
                        Text("Patient Diagnostic Releases", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    items(listOf(
                        Triple("Report #DX-99014", "Complete Blood Count (CBC)", "Released to Patient"),
                        Triple("Report #DX-99015", "Comprehensive Metabolic Panel (CMP)", "Pending Pathologist Sign-off"),
                        Triple("Report #DX-99016", "HbA1c Glycated Hemoglobin", "Processing in Analyzer")
                    )) { (id, name, status) ->
                        HealthogramBasicCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(id, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text(name, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                }
                                Text(status, style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                            }
                        }
                    }
                }

                "AI Tools" -> {
                    item {
                        HealthogramBasicCard {
                            Text("Pathology & Specimen AI Triage", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Accelerate critical value alerts, format patient plain-language test explainers, and summarize trending metabolic indices.", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
                            Spacer(modifier = Modifier.height(12.dp))
                            HealthogramPrimaryButton(text = "Open Lab AI Tools", onClick = onOpenAIStudio, size = ButtonSize.SMALL)
                        }
                    }
                }

                else -> {
                    item {
                        EmptyProfileState(
                            icon = Icons.Default.CheckCircle,
                            title = "$activeSection Ready",
                            message = "Laboratory operations module ready for orders and physician integrations."
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
