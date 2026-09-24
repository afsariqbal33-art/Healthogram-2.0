package com.example.healthogram.ui.appointments

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.healthogram.core.AccountType
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentBookingPage(
    patientUid: String = "user_patient_demo",
    patientName: String = "Sarah Jenkins",
    providerUid: String = "user_doctor_demo",
    providerName: String = "Dr. Tariq Al-Mansoor",
    providerSpecialty: String = "Consultant Cardiologist",
    providerAccountType: AccountType = AccountType.DOCTOR,
    facilityName: String = "Muscat International Hospital, Wing 3",
    onBack: () -> Unit,
    onBookingSuccess: (AppointmentItem23) -> Unit = {}
) {
    val service = remember { Appointment23Service.instance }
    val availableSlots = remember(providerUid) { service.getAvailableSlots(providerUid) }

    var selectedConsultationType by remember { mutableStateOf(ConsultationType.IN_PERSON) }
    var selectedSlot by remember { mutableStateOf<ProviderAvailableSlot?>(availableSlots.firstOrNull()) }
    var clinicalReason by remember { mutableStateOf("Routine cardiovascular follow-up and prescription review") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var confirmedAppointment by remember { mutableStateOf<AppointmentItem23?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Book Appointment",
                            style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = providerName,
                            style = HealthogramTheme.typography.bodySmall,
                            color = HealthogramTheme.colors.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("appointment_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = HealthogramTheme.colors.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HealthogramTheme.colors.surface)
            )
        }
    ) { innerPadding ->
        if (confirmedAppointment != null) {
            // Success Confirmation View
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                    border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().widthIn(max = 500.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(HealthogramTheme.colors.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Confirmed",
                                tint = HealthogramTheme.colors.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Appointment Confirmed!",
                            style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.onSurface
                        )

                        Text(
                            text = "Your clinical booking has been verified and registered.",
                            style = HealthogramTheme.typography.bodyMedium,
                            color = HealthogramTheme.colors.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                        )

                        HorizontalDivider(color = HealthogramTheme.colors.divider)

                        Spacer(modifier = Modifier.height(16.dp))

                        InfoRow(label = "Provider", value = confirmedAppointment!!.providerName)
                        InfoRow(label = "Specialty", value = confirmedAppointment!!.providerSpecialty)
                        InfoRow(label = "Date & Time", value = confirmedAppointment!!.formattedDateTime)
                        InfoRow(label = "Type", value = confirmedAppointment!!.consultationType.labelEn)
                        InfoRow(label = "Facility", value = confirmedAppointment!!.facilityName)

                        Spacer(modifier = Modifier.height(24.dp))

                        HealthogramPrimaryButton(
                            text = "Done",
                            onClick = { onBookingSuccess(confirmedAppointment!!) },
                            modifier = Modifier.fillMaxWidth().testTag("booking_success_done_button")
                        )
                    }
                }
            }
        } else {
            // Main Booking Form
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Provider Summary Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(HealthogramTheme.colors.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = HealthogramTheme.colors.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = providerName,
                                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Provider",
                                        tint = HealthogramTheme.colors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = providerSpecialty,
                                    style = HealthogramTheme.typography.bodySmall,
                                    color = HealthogramTheme.colors.primary
                                )
                                Text(
                                    text = facilityName,
                                    style = HealthogramTheme.typography.bodySmall,
                                    color = HealthogramTheme.colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Consultation Type",
                        style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ConsultationTypeCard(
                            type = ConsultationType.IN_PERSON,
                            icon = Icons.Default.Apartment,
                            isSelected = selectedConsultationType == ConsultationType.IN_PERSON,
                            onClick = { selectedConsultationType = ConsultationType.IN_PERSON },
                            modifier = Modifier.weight(1f)
                        )
                        ConsultationTypeCard(
                            type = ConsultationType.VIRTUAL_TELEHEALTH,
                            icon = Icons.Default.Videocam,
                            isSelected = selectedConsultationType == ConsultationType.VIRTUAL_TELEHEALTH,
                            onClick = { selectedConsultationType = ConsultationType.VIRTUAL_TELEHEALTH },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Text(
                        text = "Select Time Slot",
                        style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.onSurface
                    )
                    Text(
                        text = "Available Tomorrow (Timezone: Asia/Riyadh)",
                        style = HealthogramTheme.typography.bodySmall,
                        color = HealthogramTheme.colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (availableSlots.isEmpty()) {
                        Text(
                            text = "No open slots found for this date.",
                            style = HealthogramTheme.typography.bodyMedium,
                            color = HealthogramTheme.colors.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(availableSlots) { slot ->
                                val isSelected = selectedSlot?.slotId == slot.slotId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedSlot = slot },
                                    label = { Text(slot.timeLabel) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = HealthogramTheme.colors.primary,
                                        selectedLabelColor = HealthogramTheme.colors.textOnPrimary,
                                        selectedLeadingIconColor = HealthogramTheme.colors.textOnPrimary
                                    ),
                                    modifier = Modifier.testTag("slot_chip_${slot.slotId}")
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Reason for Visit",
                        style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = clinicalReason,
                        onValueChange = { clinicalReason = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("appointment_reason_input"),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Brief clinical reason (e.g. checkup, prescription renewal)") },
                        maxLines = 3
                    )
                }

                if (errorMessage != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage!!,
                                    style = HealthogramTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    HealthogramPrimaryButton(
                        text = if (isSubmitting) "Securing Slot..." else "Confirm Booking",
                        state = if (isSubmitting) ButtonState.LOADING else if (selectedSlot == null) ButtonState.DISABLED else ButtonState.NORMAL,
                        onClick = {
                            if (selectedSlot == null) return@HealthogramPrimaryButton
                            isSubmitting = true
                            errorMessage = null

                            val idempotencyKey = "appt_book_" + UUID.randomUUID().toString()
                            val result = service.bookAppointment(
                                patientUid = patientUid,
                                patientName = patientName,
                                providerUid = providerUid,
                                providerName = providerName,
                                providerSpecialty = providerSpecialty,
                                providerAccountType = providerAccountType,
                                facilityName = facilityName,
                                consultationType = selectedConsultationType,
                                slotTimestampMs = selectedSlot!!.timestampMs,
                                clinicalReason = clinicalReason,
                                idempotencyKey = idempotencyKey
                            )

                            isSubmitting = false
                            if (result.isSuccess) {
                                confirmedAppointment = result.getOrNull()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Failed to secure slot."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_booking_button")
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ConsultationTypeCard(
    type: ConsultationType,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) HealthogramTheme.colors.primary.copy(alpha = 0.1f) else HealthogramTheme.colors.surface
        ),
        border = BorderStroke(
            1.5.dp,
            if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.borderLight
        ),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = type.labelEn,
                style = HealthogramTheme.typography.bodySmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                color = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.onSurface
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = HealthogramTheme.typography.bodySmall,
            color = HealthogramTheme.colors.onSurfaceVariant
        )
        Text(
            text = value,
            style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = HealthogramTheme.colors.onSurface
        )
    }
}
