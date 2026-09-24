package com.example.healthogram.ui.dashboards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.core.AccountType
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*

/**
 * Universal Organization & Professional Dashboard Selector
 */
@Composable
fun OrganizationDashboardHub(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentType by remember { mutableStateOf(AccountType.DOCTOR) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("org_dashboard_hub")
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
                Text(
                    text = "Professional Dashboard",
                    style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Account Switcher Preview Bar (Doctor, Clinic, Hospital, Laboratory)
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val orgTypes = listOf(AccountType.DOCTOR, AccountType.CLINIC, AccountType.HOSPITAL, AccountType.LABORATORY)
            items(orgTypes) { type ->
                val isSelected = currentType == type
                Surface(
                    shape = HealthogramTheme.shapes.pill,
                    color = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.surfaceVariant,
                    border = BorderStroke(1.dp, if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.borderLight),
                    modifier = Modifier.clickable { currentType = type }
                ) {
                    Text(
                        text = type.displayName,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = HealthogramTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.White else HealthogramTheme.colors.textPrimary
                    )
                }
            }
        }

        when (currentType) {
            AccountType.DOCTOR -> DoctorDashboardContent()
            AccountType.CLINIC -> ClinicDashboardContent()
            AccountType.HOSPITAL -> HospitalDashboardContent()
            AccountType.LABORATORY -> LaboratoryDashboardContent()
            else -> DoctorDashboardContent()
        }
    }
}

/**
 * Doctor Dashboard (Section 19)
 */
@Composable
fun DoctorDashboardContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HealthogramAnalyticsCard(
                title = "Total Consultations",
                value = "128",
                trendPercentage = "+14%",
                icon = Icons.Default.MedicalServices
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HealthogramScanQRButton(
                    onClick = { /* scan patient QR */ },
                    modifier = Modifier.weight(1f)
                )
                HealthogramPrimaryButton(
                    text = "Calendar",
                    onClick = { /* open schedule */ },
                    icon = Icons.Default.CalendarToday,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text("Upcoming Patient Appointments", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        items(3) { idx ->
            HealthogramBasicCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(if (idx == 0) "Jane Mercer" else "Robert Vance", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text("Follow-up Consultation • Room 204", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                    }
                    Text(if (idx == 0) "11:00 AM" else "02:30 PM", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

/**
 * Clinic Dashboard (Section 20)
 */
@Composable
fun ClinicDashboardContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HealthogramAnalyticsCard(
                title = "Active Clinic Doctors & Staff",
                value = "16 Specialists",
                trendPercentage = "+2 Doctors",
                icon = Icons.Default.Apartment
            )
        }

        item {
            Text("Clinic Services Management", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        item {
            HealthogramBasicCard {
                Text("Cardiology Consultation Clinic", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text("4 Active Practitioners • Slot Availability: 92%", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
            }
        }

        item {
            HealthogramBasicCard {
                Text("Pediatric & Family Wellness", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text("6 Active Practitioners • Slot Availability: 84%", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

/**
 * Hospital Dashboard (Section 21)
 */
@Composable
fun HospitalDashboardContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HealthogramAnalyticsCard(
                title = "Hospital Inpatient & Outpatient Volume",
                value = "1,480 Admissions",
                trendPercentage = "+8.5%",
                icon = Icons.Default.LocalHospital
            )
        }

        item {
            Text("Hospital Departments & Wings", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        items(listOf("Robotic Surgery & Cardiology Wing", "Emergency & Trauma Center", "Oncology & Immunotherapy", "Neurology Care Unit")) { dept ->
            HealthogramBasicCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(dept, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text("Operational • 24/7 On-Duty Medical Teams", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.success)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthogramTheme.colors.textMuted)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

/**
 * Laboratory Dashboard (Section 22)
 * CRITICAL RULE: Laboratories MUST NOT have personal Health Passports.
 * They manage diagnostic catalogs, test orders, prices, report issuance, and patient scanners.
 */
@Composable
fun LaboratoryDashboardContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                shape = HealthogramTheme.shapes.medium,
                color = HealthogramTheme.colors.surfaceVariant,
                border = BorderStroke(1.dp, HealthogramTheme.colors.infoContainer)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Biotech, contentDescription = null, tint = HealthogramTheme.colors.info)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Diagnostic Laboratory Portal", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text("Manage test catalog, pricing, sample orders & digital report delivery", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                    }
                }
            }
        }

        item {
            HealthogramAnalyticsCard(
                title = "Diagnostic Tests Processed",
                value = "342 Reports",
                trendPercentage = "+22%",
                icon = Icons.Default.Assessment
            )
        }

        item {
            HealthogramScanQRButton(
                onClick = { /* scan patient QR ticket */ },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text("Active Laboratory Test Catalog", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        items(listOf(
            Triple("Complete Blood Count (CBC) Panel", "$28.00", "Turnaround: 4 hours"),
            Triple("Comprehensive Metabolic & Lipid Panel", "$45.00", "Turnaround: 6 hours"),
            Triple("Thyroid Function Panel (TSH, Free T3/T4)", "$38.00", "Turnaround: 12 hours")
        )) { (name, price, tat) ->
            HealthogramBasicCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(name, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(tat, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                    }
                    Text(price, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}
