package com.example.healthogram.ui.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.performance.*

/**
 * HEALTHOGRAM STEP 21: SECTION 91 PRODUCTION PERFORMANCE & SCALABILITY DASHBOARD
 */
@Composable
fun PerformanceDashboardPage(
    service: PerformanceMonitoringService = remember { PerformanceMonitoringService.getInstance() },
    modifier: Modifier = Modifier
) {
    val platformStatus by service.platformStatus.collectAsState()
    val emergencyControls by service.emergencyControls.collectAsState()
    val domainSummaries = remember(platformStatus) { service.getDomainSummaries() }
    val allJobs = remember { service.getAllJobs() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
            .testTag("performance_dashboard_page"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Overall Health Status Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("overall_health_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (platformStatus) {
                        PerformanceStatus.GREEN -> Color(0xFF064E3B)
                        PerformanceStatus.YELLOW -> Color(0xFF78350F)
                        PerformanceStatus.RED -> Color(0xFF7F1D1D)
                    }
                ),
                border = BorderStroke(
                    1.dp,
                    when (platformStatus) {
                        PerformanceStatus.GREEN -> Color(0xFF10B981)
                        PerformanceStatus.YELLOW -> Color(0xFFF59E0B)
                        PerformanceStatus.RED -> Color(0xFFEF4444)
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                when (platformStatus) {
                                    PerformanceStatus.GREEN -> Color(0xFF34D399)
                                    PerformanceStatus.YELLOW -> Color(0xFFFBBF24)
                                    PerformanceStatus.RED -> Color(0xFFF87171)
                                },
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Platform Scalability Status: ${platformStatus.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (platformStatus) {
                                PerformanceStatus.GREEN -> "All systems operating within SLO latency bounds (<500ms P95). Zero-Trust security intact."
                                PerformanceStatus.YELLOW -> "Minor latency elevated on secondary nodes. Non-essential operations throttled."
                                PerformanceStatus.RED -> "Critical degradation threshold exceeded! Emergency performance safeguard available below."
                            },
                            fontSize = 13.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }

        // 2. High-Level Metrics Summary Row (P50, P95, RPS, Total Daily Cost)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PerformanceMetricCard(
                    title = "Target P50 SLO",
                    value = "180 ms",
                    subtitle = "Median Latency",
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
                PerformanceMetricCard(
                    title = "Target P95 SLO",
                    value = "480 ms",
                    subtitle = "95th Percentile",
                    color = Color(0xFFA78BFA),
                    modifier = Modifier.weight(1f)
                )
                PerformanceMetricCard(
                    title = "Peak Traffic",
                    value = "5,180 RPS",
                    subtitle = "Sovereign GCC Route",
                    color = Color(0xFF34D399),
                    modifier = Modifier.weight(1f)
                )
                PerformanceMetricCard(
                    title = "Cost Efficiency",
                    value = "$182.50",
                    subtitle = "Daily Infra Spend",
                    color = Color(0xFFFBBF24),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Domain Performance Latency Matrix
        item {
            Text(
                text = "Domain Latency & Throughput Matrix (SLO Compliance)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items(domainSummaries) { domain ->
            DomainLatencyCard(domain = domain)
        }

        // 4. Emergency Performance Controls (Section 93)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("emergency_performance_controls_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Emergency Performance Controls",
                            tint = Color(0xFFF59E0B)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Emergency Performance Controls (Load Relief)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Instantly shedding non-critical computational load during flash sale surges or viral traffic events. Cryptographic Health Passport security is never weakened.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    EmergencyPerformanceControl.values().forEach { control ->
                        val isEnabled = emergencyControls[control] ?: false
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = control.title,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = control.description,
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { service.setEmergencyControl(control, it) },
                                modifier = Modifier.testTag("switch_${control.controlKey}")
                            )
                        }
                        HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.5f))
                    }
                }
            }
        }

        // 5. Asynchronous Job State Machine Queue Status (Section 44)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("async_jobs_queue_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Asynchronous Worker Queue (Off-Thread Engine)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "All heavy workloads (video transcoding, bulk PDF exports, Gemini AI fine-tuning) processed strictly through state-machine worker queues.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (allJobs.isEmpty()) {
                        Text(
                            text = "No active background tasks in flight. System idle.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    } else {
                        allJobs.forEach { job ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${job.jobType} (${job.jobId.take(8)})",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Status: ${job.state} • ${job.progressPercent}%",
                                        fontSize = 11.sp,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(job.state.name, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceMetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 11.sp, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 10.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
private fun DomainLatencyCard(domain: DomainPerformanceSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("domain_card_${domain.domainName.replace(" ", "_").lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = domain.domainName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "P50: ${domain.p50Ms}ms",
                        fontSize = 12.sp,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = "P95: ${domain.p95Ms}ms",
                        fontSize = 12.sp,
                        color = Color(0xFFA78BFA)
                    )
                    Text(
                        text = "Err: ${String.format("%.1f", domain.errorRatePercent)}%",
                        fontSize = 12.sp,
                        color = if (domain.errorRatePercent > 1.0) Color(0xFFF87171) else Color(0xFF34D399)
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (domain.status) {
                    PerformanceStatus.GREEN -> Color(0xFF064E3B)
                    PerformanceStatus.YELLOW -> Color(0xFF78350F)
                    PerformanceStatus.RED -> Color(0xFF7F1D1D)
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = domain.status.name,
                    color = when (domain.status) {
                        PerformanceStatus.GREEN -> Color(0xFF34D399)
                        PerformanceStatus.YELLOW -> Color(0xFFFBBF24)
                        PerformanceStatus.RED -> Color(0xFFF87171)
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
