package com.example.healthogram.ui.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.healthogram.ui.finance.OwnerEarningsDashboardPage
import com.example.healthogram.ui.finance.OwnerFinancialDashboardPage

enum class OwnerFinancialViewMode {
    EARNINGS_DASHBOARD,
    MASTER_FINANCIAL_CONSOLE
}

/**
 * Platform Owner Financial Dashboard & Revenue Intelligence.
 * Step 18: Enhanced with server-authoritative double-entry financial ledger,
 * multi-country commission rules, seller settlements, and owner fund reservations.
 */
@Composable
fun OwnerEarningsDashboardView(
    ownerUid: String = "owner_root_001",
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(OwnerFinancialViewMode.EARNINGS_DASHBOARD) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .testTag("owner_earnings_dashboard_view")
    ) {
        when (viewMode) {
            OwnerFinancialViewMode.EARNINGS_DASHBOARD -> {
                OwnerEarningsDashboardPage(
                    onNavigateToFinancialDashboard = {
                        viewMode = OwnerFinancialViewMode.MASTER_FINANCIAL_CONSOLE
                    },
                    onClose = onBack
                )
            }
            OwnerFinancialViewMode.MASTER_FINANCIAL_CONSOLE -> {
                OwnerFinancialDashboardPage(
                    onClose = {
                        viewMode = OwnerFinancialViewMode.EARNINGS_DASHBOARD
                    }
                )
            }
        }
    }
}
