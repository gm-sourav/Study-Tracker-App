package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayStudyStat
import com.example.data.model.SubjectStats
import kotlin.math.max

@Composable
fun WeeklyBarChart(
    dailyStats: List<DayStudyStat>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = MaterialTheme.colorScheme.secondary
) {
    if (dailyStats.isEmpty()) return

    val maxMinutes = remember(dailyStats) {
        val maxVal = dailyStats.maxOfOrNull { it.totalMinutes } ?: 60
        max(maxVal, 60)
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(dailyStats) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(700))
    }

    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                val totalBars = dailyStats.size
                val availableWidth = size.width
                val barSpacing = availableWidth / (totalBars * 2.2f)
                val barWidth = (availableWidth - (barSpacing * (totalBars + 1))) / totalBars
                val chartHeight = size.height - 20f

                // Draw subtle grid line for 50% and 100%
                val gridY50 = chartHeight * 0.5f
                drawLine(
                    color = surfaceVariant.copy(alpha = 0.6f),
                    start = Offset(0f, gridY50),
                    end = Offset(size.width, gridY50),
                    strokeWidth = 1f
                )

                dailyStats.forEachIndexed { index, stat ->
                    val x = barSpacing + index * (barWidth + barSpacing)
                    val rawBarHeight = (stat.totalMinutes.toFloat() / maxMinutes.toFloat()) * chartHeight
                    val animatedBarHeight = (rawBarHeight * animationProgress.value).coerceAtLeast(6f)
                    val y = chartHeight - animatedBarHeight

                    val isToday = index == dailyStats.lastIndex
                    val gradient = Brush.verticalGradient(
                        colors = if (isToday) {
                            listOf(accentColor, accentColor.copy(alpha = 0.7f))
                        } else {
                            listOf(barColor, barColor.copy(alpha = 0.6f))
                        }
                    )

                    // Draw bar background track
                    drawRoundRect(
                        color = surfaceVariant.copy(alpha = 0.35f),
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, chartHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )

                    // Draw animated bar fill
                    drawRoundRect(
                        brush = gradient,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, animatedBarHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Labels row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dailyStats.forEachIndexed { index, stat ->
                val isToday = index == dailyStats.lastIndex
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stat.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) accentColor else onSurfaceVariant
                    )
                    Text(
                        text = if (stat.totalMinutes >= 60) {
                            "${stat.totalMinutes / 60}h"
                        } else if (stat.totalMinutes > 0) {
                            "${stat.totalMinutes}m"
                        } else {
                            "-"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubjectDonutChart(
    subjectBreakdown: List<SubjectStats>,
    modifier: Modifier = Modifier
) {
    val nonZeroStats = remember(subjectBreakdown) {
        subjectBreakdown.filter { it.totalMinutes > 0 }
    }
    val totalMinutes = remember(nonZeroStats) {
        nonZeroStats.sumOf { it.totalMinutes }
    }

    if (totalMinutes == 0) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Log study sessions to see subject distribution",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(170.dp)) {
                val strokeWidth = 24.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val centerOffset = Offset(size.width / 2, size.height / 2)

                var startAngle = -90f

                nonZeroStats.forEach { stat ->
                    val sweepAngle = (stat.totalMinutes.toFloat() / totalMinutes.toFloat()) * 360f
                    drawArc(
                        color = Color(stat.subject.colorHex),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle - 2f, // Small gap
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${totalMinutes / 60}h ${totalMinutes % 60}m",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total Logged",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            nonZeroStats.forEach { stat ->
                val percentage = ((stat.totalMinutes.toFloat() / totalMinutes.toFloat()) * 100).toInt()
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(stat.subject.colorHex))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${stat.subject.name} ($percentage%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
