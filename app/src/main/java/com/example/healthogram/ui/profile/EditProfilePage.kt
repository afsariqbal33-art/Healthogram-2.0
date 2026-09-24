package com.example.healthogram.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.profile.components.ProfileAvatar
import com.example.healthogram.profile.model.PrivateProfile
import com.example.healthogram.profile.model.ProfessionalProfile
import com.example.healthogram.profile.model.PublicProfile
import com.example.healthogram.profile.repository.ProfileRepository
import kotlinx.coroutines.launch

/**
 * Edit Profile Page.
 * Allows editing appropriate public and private profile details based on account type.
 * Enforces security: client cannot edit verified status or change primary accountType here.
 */
@Composable
fun EditProfilePage(
    user: User?,
    onBack: () -> Unit,
    onSaved: () -> Unit = onBack,
    modifier: Modifier = Modifier
) {
    val repository = remember { ProfileRepository.getInstance() }
    val scope = rememberCoroutineScope()

    var displayName by remember { mutableStateOf(user?.displayName ?: "") }
    var bio by remember { mutableStateOf(user?.bio ?: "") }
    var website by remember { mutableStateOf("https://healthogram.me/${user?.username ?: ""}") }
    var city by remember { mutableStateOf(user?.city ?: "") }
    var publicEmail by remember { mutableStateOf(user?.email ?: "") }
    var publicPhone by remember { mutableStateOf(user?.phoneNumber ?: "") }

    // Professional fields (for Doctors and Organizations)
    var professionalTitle by remember { mutableStateOf("Specialist Physician") }
    var specializationsText by remember { mutableStateOf("Cardiology, Preventive Care") }
    var businessHours by remember { mutableStateOf("Mon-Fri: 09:00 - 17:00") }
    var isTelehealthEnabled by remember { mutableStateOf(true) }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val accountType = user?.accountType ?: AccountType.INDIVIDUAL

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("edit_profile_page")
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = HealthogramTheme.colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Profile",
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                TextButton(
                    onClick = {
                        scope.launch {
                            isSaving = true
                            errorMessage = null
                            try {
                                val currentUid = user?.uid ?: "user_default"
                                val updatedPublic = PublicProfile(
                                    uid = currentUid,
                                    username = user?.username ?: "user",
                                    displayName = displayName,
                                    bio = bio,
                                    website = website,
                                    city = city,
                                    publicEmail = publicEmail,
                                    publicPhone = publicPhone,
                                    accountType = accountType,
                                    isVerified = user?.isVerified ?: false
                                )
                                repository.savePublicProfile(updatedPublic)

                                if (accountType == AccountType.DOCTOR || user?.isProfessional == true) {
                                    val specs = specializationsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                    val prof = ProfessionalProfile(
                                        uid = currentUid,
                                        accountType = accountType,
                                        professionalTitle = professionalTitle,
                                        specializations = specs,
                                        businessHours = businessHours,
                                        onlineConsultationEnabled = isTelehealthEnabled
                                    )
                                    repository.saveProfessionalProfile(prof)
                                }

                                successMessage = "Profile updated successfully"
                                onSaved()
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Failed to update profile"
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Avatar change header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileAvatar(displayName = displayName.ifBlank { "User" }, size = 84.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { /* pick photo */ }) {
                        Text("Change Profile Photo", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            if (errorMessage != null) {
                item {
                    Surface(
                        shape = HealthogramTheme.shapes.small,
                        color = HealthogramTheme.colors.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = HealthogramTheme.colors.error,
                            modifier = Modifier.padding(12.dp),
                            style = HealthogramTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Public Information Section
            item {
                Text("Public Information", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }

            item {
                HealthogramTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = "Display Name",
                    leadingIcon = Icons.Default.Person
                )
            }

            item {
                HealthogramTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = "Bio",
                    leadingIcon = Icons.Default.EditNote
                )
            }

            item {
                HealthogramTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = "Website / Portfolio",
                    leadingIcon = Icons.Default.Language
                )
            }

            item {
                HealthogramTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = "City / Region",
                    leadingIcon = Icons.Default.LocationCity
                )
            }

            // Contact Info
            item {
                Text("Public Contact Info", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }

            item {
                HealthogramTextField(
                    value = publicEmail,
                    onValueChange = { publicEmail = it },
                    label = "Public Email",
                    leadingIcon = Icons.Default.Email
                )
            }

            item {
                HealthogramTextField(
                    value = publicPhone,
                    onValueChange = { publicPhone = it },
                    label = "Public Phone",
                    leadingIcon = Icons.Default.Phone
                )
            }

            // Professional Details (Conditional on Doctor or Healthcare Org)
            if (accountType != AccountType.INDIVIDUAL || user?.isProfessional == true) {
                item {
                    Divider(color = HealthogramTheme.colors.borderLight)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Professional Practice Details", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }

                item {
                    HealthogramTextField(
                        value = professionalTitle,
                        onValueChange = { professionalTitle = it },
                        label = "Professional Title",
                        leadingIcon = Icons.Default.Badge
                    )
                }

                item {
                    HealthogramTextField(
                        value = specializationsText,
                        onValueChange = { specializationsText = it },
                        label = "Specializations (comma separated)",
                        leadingIcon = Icons.Default.MedicalServices
                    )
                }

                item {
                    HealthogramTextField(
                        value = businessHours,
                        onValueChange = { businessHours = it },
                        label = "Working Hours",
                        leadingIcon = Icons.Default.Schedule
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Online Telehealth Consultations", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Allow patients to book remote video sessions", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                        }
                        Switch(
                            checked = isTelehealthEnabled,
                            onCheckedChange = { isTelehealthEnabled = it }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
