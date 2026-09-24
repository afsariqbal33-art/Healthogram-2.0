package com.example.healthogram.ui.organization

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
import com.example.healthogram.profile.model.OrganizationMember
import com.example.healthogram.profile.model.OrganizationRole
import com.example.healthogram.profile.repository.ProfileRepository
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Organization Staff & Member Management Page (Section 65).
 * Allows managing organizational hierarchy: Owner, Admin, Manager, Doctor, Staff, Reception, Content Manager.
 */
@Composable
fun OrganizationMembersPage(
    orgId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { ProfileRepository.getInstance() }
    val scope = rememberCoroutineScope()

    var members by remember { mutableStateOf<List<OrganizationMember>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }

    var newMemberName by remember { mutableStateOf("") }
    var newMemberEmail by remember { mutableStateOf("") }
    var newMemberRole by remember { mutableStateOf(OrganizationRole.STAFF) }

    LaunchedEffect(orgId) {
        members = repository.getOrganizationMembers(orgId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("organization_members_page")
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Organization Staff",
                        style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Member", tint = HealthogramTheme.colors.primary)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text("Registered Staff & Practitioners (${members.size})", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }

            items(members) { member ->
                HealthogramBasicCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.displayName, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(member.email, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = HealthogramTheme.shapes.pill,
                                color = HealthogramTheme.colors.primaryContainer
                            ) {
                                Text(
                                    text = member.role.displayName,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                                    color = HealthogramTheme.colors.primary
                                )
                            }
                        }

                        if (member.role != OrganizationRole.OWNER) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        repository.removeOrganizationMember(orgId, member.uid)
                                        members = repository.getOrganizationMembers(orgId)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Remove Staff", tint = HealthogramTheme.colors.error)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Organization Member", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HealthogramTextField(
                        value = newMemberName,
                        onValueChange = { newMemberName = it },
                        label = "Full Name"
                    )
                    HealthogramTextField(
                        value = newMemberEmail,
                        onValueChange = { newMemberEmail = it },
                        label = "Email Address"
                    )
                    Text("Assigned Role", style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Column {
                        listOf(
                            OrganizationRole.DOCTOR,
                            OrganizationRole.MANAGER,
                            OrganizationRole.STAFF,
                            OrganizationRole.RECEPTION
                        ).forEach { r ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = newMemberRole == r,
                                    onClick = { newMemberRole = r }
                                )
                                Text(r.displayName, style = HealthogramTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newMemberName.isNotBlank() && newMemberEmail.isNotBlank()) {
                            scope.launch {
                                val newMember = OrganizationMember(
                                    membershipId = "mem_${UUID.randomUUID().toString().take(8)}",
                                    organizationId = orgId,
                                    uid = "user_${UUID.randomUUID().toString().take(8)}",
                                    displayName = newMemberName,
                                    email = newMemberEmail,
                                    role = newMemberRole
                                )
                                repository.addOrganizationMember(newMember)
                                members = repository.getOrganizationMembers(orgId)
                                showAddDialog = false
                                newMemberName = ""
                                newMemberEmail = ""
                            }
                        }
                    }
                ) {
                    Text("Add Member", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
