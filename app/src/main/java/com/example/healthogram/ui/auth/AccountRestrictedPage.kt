package com.example.healthogram.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.healthogram.auth.FirebaseAuthManager
import com.example.healthogram.auth.components.PrimaryAuthButton
import com.example.healthogram.auth.components.SecondaryAuthButton
import com.example.healthogram.designsystem.HealthogramTheme

/**
 * Account Restricted / Suspended Page.
 *
 * Enforces Section 9 & 31:
 * Displays clear, friendly notification without leaking internal system traces.
 */
@Composable
fun AccountRestrictedPage(
    authManager: FirebaseAuthManager,
    reason: String = "Account restricted due to security policy review.",
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 24.dp)
            .testTag("account_restricted_page"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(HealthogramTheme.colors.error.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Account Restricted",
                tint = HealthogramTheme.colors.error,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Account Access Restricted",
            style = HealthogramTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = HealthogramTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = reason,
            style = HealthogramTheme.typography.bodyMedium,
            color = HealthogramTheme.colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryAuthButton(
            text = "Contact Support / Request Review",
            onClick = { /* Contact support workflow */ }
        )

        Spacer(modifier = Modifier.height(12.dp))

        SecondaryAuthButton(
            text = "Sign Out",
            onClick = {
                authManager.signOut()
                onSignOut()
            }
        )
    }
}
