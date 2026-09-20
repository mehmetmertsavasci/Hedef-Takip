package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Goal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayCompletion(
    val dayName: String, // Pzt, Sal, Çar, Per, Cum, Cmt, Paz
    val dateStr: String,
    val completedCount: Int,
    val totalCount: Int,
    val percent: Float
)

@Composable
fun CustomWeeklyBarChart(
    goals: List<Goal>,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary
) {
    val weeklyData = remember(goals) {
        val days = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        // Find Monday of this week
        val currentDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // 0 for Mon, 6 for Sun
        cal.add(Calendar.DAY_OF_YEAR, -currentDayOfWeek)

        val list = mutableListOf<DayCompletion>()
        for (i in 0 until 7) {
            val dateStr = sdf.format(cal.time)
            // Count completed goals on that date
            val completed = goals.count { g ->
                g.historyDates.split(",").contains(dateStr) || (g.isCompleted && g.lastCompletedDate == dateStr)
            }
            val total = goals.size.coerceAtLeast(1)
            val percent = (completed.toFloat() / total).coerceIn(0f, 1f)
            list.add(DayCompletion(days[i], dateStr, completed, total, percent))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(goals) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(700))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Haftalık İlerleme Grafiği",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            val avg = if (weeklyData.isNotEmpty()) (weeklyData.map { it.percent }.average() * 100).toInt() else 0
            Text(
                text = "Haftalık Ort: %$avg",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = primaryColor
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val barWidth = size.width / (weeklyData.size * 2)
                val spacing = size.width / weeklyData.size
                val maxHeight = size.height - 24.dp.toPx()

                // Draw horizontal background guide lines
                val lineSteps = listOf(0.25f, 0.5f, 0.75f, 1f)
                for (step in lineSteps) {
                    val y = maxHeight * (1f - step)
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.15f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                weeklyData.forEachIndexed { index, day ->
                    val centerX = (index * spacing) + (spacing / 2)
                    val barHeight = maxHeight * day.percent * animatedProgress.value

                    // Draw background track bar
                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.12f),
                        topLeft = Offset(centerX - barWidth / 2, 0f),
                        size = Size(barWidth, maxHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )

                    // Draw active bar
                    if (barHeight > 0f) {
                        drawRoundRect(
                            color = primaryColor,
                            topLeft = Offset(centerX - barWidth / 2, maxHeight - barHeight),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Days of week label row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            weeklyData.forEach { day ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = day.dayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${(day.percent * 100).toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (day.percent > 0.7f) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
