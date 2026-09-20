package com.example.ui.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun StreakHeatmap(
    goals: List<Goal>,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary
) {
    // 28 days (4 weeks x 7 days)
    val past28Days = remember(goals) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -27)

        val list = mutableListOf<Pair<String, Int>>() // DateStr, CompletedCount
        for (i in 0 until 28) {
            val dateStr = sdf.format(cal.time)
            val count = goals.count { g ->
                g.historyDates.split(",").contains(dateStr) || (g.isCompleted && g.lastCompletedDate == dateStr)
            }
            list.add(dateStr to count)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val maxCompleted = goals.size.coerceAtLeast(1)

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
                text = "Son 28 Gün Tutarlılık Matrisi",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            val activeDays = past28Days.count { it.second > 0 }
            Text(
                text = "$activeDays / 28 Gün Aktif",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = primaryColor
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Heatmap Grid: 4 rows (weeks) x 7 cols (days)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val cols = 7
                val rows = 4
                val totalWidth = size.width
                val totalHeight = size.height
                val cellSpacing = 6.dp.toPx()
                val cellWidth = (totalWidth - (cellSpacing * (cols - 1))) / cols
                val cellHeight = (totalHeight - (cellSpacing * (rows - 1))) / rows
                val cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())

                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val index = r * cols + c
                        if (index < past28Days.size) {
                            val count = past28Days[index].second
                            val intensity = (count.toFloat() / maxCompleted).coerceIn(0f, 1f)

                            val cellColor = when {
                                count == 0 -> primaryColor.copy(alpha = 0.08f)
                                intensity < 0.35f -> primaryColor.copy(alpha = 0.35f)
                                intensity < 0.70f -> primaryColor.copy(alpha = 0.65f)
                                else -> primaryColor
                            }

                            val x = c * (cellWidth + cellSpacing)
                            val y = r * (cellHeight + cellSpacing)

                            drawRoundRect(
                                color = cellColor,
                                topLeft = Offset(x, y),
                                size = Size(cellWidth, cellHeight),
                                cornerRadius = cornerRadius
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Legend row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Düşük",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(primaryColor.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.size(4.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(primaryColor.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.size(4.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(primaryColor.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.size(4.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(primaryColor, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = "Yüksek",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
