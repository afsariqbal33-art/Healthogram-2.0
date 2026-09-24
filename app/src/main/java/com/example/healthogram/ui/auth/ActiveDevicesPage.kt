package com.example.healthogram.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.auth.DeviceSession
import com.example.healthogram.auth.FirebaseAuthManager
import com.example.healthogram.auth.components.AuthErrorMessage
import com.example.healthogram.auth.components.DeviceListItem
import com.example.healthogram.auth.components.SuccessMessage
import com.example.healthogram.designsystem.HealthogramTheme
import kotlinx.coroutines.launch

/**
 * Active Devices Management Page.
 *
 * Enforces Section 19 & 20:
 * - Displays all active device sessions.
 * - Enforces the strict maximum 4 active devices limit.
 * - Allows user to revoke sessions or sign out other devices.
 */
@Composable
fun ActiveDevicesPage(
    authManager: FirebaseAuthManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val currentUser by authManager.currentUser.collectAsState()
    var devices by remember { mutableStateOf<List<DeviceSession>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun refreshDevices() {
        val uid = currentUser?.uid ?: return
        coroutineScope.launch {
            isLoading = true
            val result = authManager.getActiveDevices(uid)
            isLoading = false
            if (result.isSuccess) {
                devices = result.getOrDefault(emptyList())
            } else {
                errorMessage = "Failed to load active devices."
            }
        }
    }

    LaunchedEffect(currentUser?.uid) {
        refreshDevices()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 20.dp)
            .testTag("active_devices_page")
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Active Devices",
                style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4-Device Limit Status Card
        Card(
            shape = HealthogramTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.primary.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Devices, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Device Limit: ${devices.size} / ${FirebaseAuthManager.MAX_ACTIVE_DEVICES} Active",
                        style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "For security, Healthogram permits up to 4 simultaneous active device sessions. To connect a new device, revoke an existing session.",
                        style = HealthogramTheme.typography.bodySmall,
                        color = HealthogramTheme.colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (feedbackMessage != null) {
            SuccessMessage(message = feedbackMessage!!)
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (errorMessage != null) {
            AuthErrorMessage(message = errorMessage!!)
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HealthogramTheme.colors.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(devices, key = { it.deviceId }) { session ->
                    DeviceListItem(
                        session = session,
                        onRevoke = {
                            val uid = currentUser?.uid ?: return@DeviceListItem
                            coroutineScope.launch {
                                val result = authManager.revokeDevice(uid, session.deviceId)
                                if (result.isSuccess) {
                                    feedbackMessage = "Device '${session.deviceName}' session revoked."
                                    refreshDevices()
                                } else {
                                    errorMessage = "Failed to revoke session."
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
