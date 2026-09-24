package com.example.healthogram.healthpassport

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Step 37 / REQ-005: Native Jetpack Compose Canvas Trend Chart
 * Provides high-performance vector rendering for vital signs (Blood Pressure, Glucose, Heart Rate).
 * Fully offline-capable, zero third-party webview reliance, zero unnecessary recompositions.
 */
@Immutable
data class VitalTrendPoint(
    val timestamp: Long,
    val value: Float,
    val label: String
)

@Immutable
data class VitalTrendData(
    val metricName: String,
    val unit: String,
    val points: List<VitalTrendPoint>,
    val normalRangeMin: Float,
    val normalRangeMax: Float,
    val accentColor: Color = Color(0xFF00796B) // Teal clinical primary
)

@Composable
fun HealthPassportCanvasTrendChart(
    data: VitalTrendData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vital_trend_chart_${data.metricName.lowercase().replace(" ", "_")}")
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = data.metricName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            val latestValue = data.points.lastOrNull()?.value
            Text(
                text = if (latestValue != null) "${latestValue.toInt()} ${data.unit}" else "--",
                style = MaterialTheme.typography.titleSmall,
                color = data.accentColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (data.points.size < 2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Insufficient clinical readings for trend visualization",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val minVal = (data.points.minOf { it.value }.coerceAtMost(data.normalRangeMin) - 5f).coerceAtLeast(0f)
            val maxVal = data.points.maxOf { it.value }.coerceAtLeast(data.normalRangeMax) + 5f
            val valRange = (maxVal - minVal).coerceAtLeast(1f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .testTag("vital_trend_canvas")
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val pointSpacing = canvasWidth / (data.points.size - 1)

                // 1. Draw Normal Reference Range Shading
                val normTopY = canvasHeight - ((data.normalRangeMax - minVal) / valRange * canvasHeight)
                val normBottomY = canvasHeight - ((data.normalRangeMin - minVal) / valRange * canvasHeight)
                drawRect(
                    color = Color(0xFF4CAF50).copy(alpha = 0.10f),
                    topLeft = Offset(0f, normTopY),
                    size = androidx.compose.ui.geometry.Size(canvasWidth, (normBottomY - normTopY).coerceAtLeast(2f))
                )

                // 2. Draw Horizontal Grid Guideline
                drawLine(
                    color = Color.Gray.copy(alpha = 0.25f),
                    start = Offset(0f, normTopY),
                    end = Offset(canvasWidth, normTopY),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color.Gray.copy(alpha = 0.25f),
                    start = Offset(0f, normBottomY),
                    end = Offset(canvasWidth, normBottomY),
                    strokeWidth = 1.dp.toPx()
                )

                // 3. Draw Trend Line Path
                val trendPath = Path()
                data.points.forEachIndexed { index, point ->
                    val x = index * pointSpacing
                    val y = canvasHeight - ((point.value - minVal) / valRange * canvasHeight)
                    if (index == 0) {
                        trendPath.moveTo(x, y)
                    } else {
                        val prevX = (index - 1) * pointSpacing
                        val prevY = canvasHeight - ((data.points[index - 1].value - minVal) / valRange * canvasHeight)
                        val cx = (prevX + x) / 2
                        trendPath.cubicTo(cx, prevY, cx, y, x, y)
                    }
                }

                drawPath(
                    path = trendPath,
                    color = data.accentColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // 4. Draw Anchor Points
                data.points.forEachIndexed { index, point ->
                    val x = index * pointSpacing
                    val y = canvasHeight - ((point.value - minVal) / valRange * canvasHeight)
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = data.accentColor,
                        radius = 2.5.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Normal range footer label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ref: ${data.normalRangeMin.toInt()} - ${data.normalRangeMax.toInt()} ${data.unit}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${data.points.size} entries",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
