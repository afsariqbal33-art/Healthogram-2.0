package com.example.healthogram.ui.appointments

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsListPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit,
    onBookNewClick: () -> Unit = {},
    onJoinTelehealth: (AppointmentItem23) -> Unit = {}
) {
    val service = remember { Appointment23Service.instance }
    var appointments by remember { mutableStateOf(service.getPatientAppointments(patientUid)) }
    var selectedTab by remember { mutableStateOf("UPCOMING") }
    var appointmentToCancel by remember { mutableStateOf<AppointmentItem23?>(null) }
    var cancellationReason by remember { mutableStateOf("") }

    val filteredList = remember(appointments, selectedTab) {
        val now = System.currentTimeMillis()
        when (selectedTab) {
            "UPCOMING" -> appointments.filter {
                it.status != AppointmentStatus23.CANCELLED &&
                it.status != AppointmentStatus23.COMPLETED &&
                it.status != AppointmentStatus23.NO_SHOW
            }
            "PAST" -> appointments.filter {
                it.status == AppointmentStatus23.COMPLETED ||
                it.status == AppointmentStatus23.CANCELLED ||
                it.status == AppointmentStatus23.NO_SHOW
            }
            else -> appointments
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Appointments",
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("appointments_list_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = HealthogramTheme.colors.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onBookNewClick, modifier = Modifier.testTag("book_new_appointment_header_btn")) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Book Appointment",
                            tint = HealthogramTheme.colors.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HealthogramTheme.colors.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = if (selectedTab == "UPCOMING") 0 else 1,
                containerColor = HealthogramTheme.colors.surface,
                contentColor = HealthogramTheme.colors.primary
            ) {
                Tab(
                    selected = selectedTab == "UPCOMING",
                    onClick = { selectedTab = "UPCOMING" },
                    text = { Text("Upcoming (${appointments.count { it.status == AppointmentStatus23.CONFIRMED || it.status == AppointmentStatus23.REQUESTED }})") }
                )
                Tab(
                    selected = selectedTab == "PAST",
                    onClick = { selectedTab = "PAST" },
                    text = { Text("Past History") }
                )
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = HealthogramTheme.colors.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedTab == "UPCOMING") "No upcoming appointments." else "No appointment history.",
                            style = HealthogramTheme.typography.bodyLarge,
                            color = HealthogramTheme.colors.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HealthogramPrimaryButton(
                            text = "Find & Book Doctor",
                            onClick = onBookNewClick,
                            size = ButtonSize.SMALL
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList, key = { it.appointmentId }) { appt ->
                        AppointmentCard(
                            appointment = appt,
                            onCheckIn = {
                                service.updateStatus(appt.appointmentId, patientUid, AppointmentStatus23.CHECKED_IN)
                                appointments = service.getPatientAppointments(patientUid)
                            },
                            onCancel = {
                                appointmentToCancel = appt
                                cancellationReason = ""
                            },
                            onJoinTelehealth = { onJoinTelehealth(appt) }
                        )
                    }
                }
            }
        }
    }

    // Cancellation Dialog
    if (appointmentToCancel != null) {
        AlertDialog(
            onDismissRequest = { appointmentToCancel = null },
            title = { Text("Cancel Appointment") },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to cancel your appointment with ${appointmentToCancel!!.providerName}?",
                        style = HealthogramTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = cancellationReason,
                        onValueChange = { cancellationReason = it },
                        label = { Text("Reason for cancellation (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toCancel = appointmentToCancel!!
                        service.updateStatus(
                            toCancel.appointmentId,
                            patientUid,
                            AppointmentStatus23.CANCELLED,
                            cancellationReason.ifBlank { "Patient requested cancellation" }
                        )
                        appointments = service.getPatientAppointments(patientUid)
                        appointmentToCancel = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Cancellation")
                }
            },
            dismissButton = {
                TextButton(onClick = { appointmentToCancel = null }) {
                    Text("Keep Appointment")
                }
            }
        )
    }
}

@Composable
fun AppointmentCard(
    appointment: AppointmentItem23,
    onCheckIn: () -> Unit,
    onCancel: () -> Unit,
    onJoinTelehealth: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("appointment_card_${appointment.appointmentId}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(HealthogramTheme.colors.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (appointment.consultationType == ConsultationType.VIRTUAL_TELEHEALTH)
                                Icons.Default.Videocam else Icons.Default.Apartment,
                            contentDescription = null,
                            tint = HealthogramTheme.colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = appointment.providerName,
                            style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = appointment.providerSpecialty,
                            style = HealthogramTheme.typography.bodySmall,
                            color = HealthogramTheme.colors.primary
                        )
                    }
                }

                StatusChip(status = appointment.status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = HealthogramTheme.colors.divider)
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = HealthogramTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = appointment.formattedDateTime,
                    style = HealthogramTheme.typography.bodySmall,
                    color = HealthogramTheme.colors.onSurface
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = HealthogramTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = appointment.facilityName,
                    style = HealthogramTheme.typography.bodySmall,
                    color = HealthogramTheme.colors.onSurfaceVariant
                )
            }

            if (appointment.status == AppointmentStatus23.CONFIRMED || appointment.status == AppointmentStatus23.REQUESTED) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (appointment.consultationType == ConsultationType.VIRTUAL_TELEHEALTH) {
                        HealthogramPrimaryButton(
                            text = "Join Telehealth Video",
                            onClick = onJoinTelehealth,
                            size = ButtonSize.SMALL,
                            modifier = Modifier.weight(1.5f)
                        )
                    } else {
                        HealthogramOutlineButton(
                            text = "Check In",
                            onClick = onCheckIn,
                            size = ButtonSize.SMALL,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    TextButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Text("Cancel", style = HealthogramTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: AppointmentStatus23) {
    val (bg, fg) = when (status) {
        AppointmentStatus23.CONFIRMED -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        AppointmentStatus23.CHECKED_IN -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        AppointmentStatus23.IN_PROGRESS -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
        AppointmentStatus23.COMPLETED -> Color(0xFFF3E5F5) to Color(0xFF7B1FA2)
        AppointmentStatus23.CANCELLED -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        else -> Color(0xFFECEFF1) to Color(0xFF455A64)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.labelEn,
            style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
            color = fg
        )
    }
}
