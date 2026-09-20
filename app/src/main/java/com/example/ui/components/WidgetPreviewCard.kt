package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Goal

enum class WidgetStyle(val title: String, val desc: String) {
    COMPACT_PROGRESS("Kompakt İlerleme", "İlerleme çubuğu ve günlük özet"),
    DETAILED_LIST("Ayrıntılı Hedefler", "Hedef listesi ve sonraki görev"),
    MINIMAL_STREAK("Seri Rozeti", "Odaklanmış seri sayısı ve halka")
}

@Composable
fun WidgetPreviewCard(
    goals: List<Goal>,
    onUpdateWidget: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStyle by remember { mutableStateOf(WidgetStyle.COMPACT_PROGRESS) }
    var showStreak by remember { mutableStateOf(true) }
    var showNextGoal by remember { mutableStateOf(true) }
    var widgetThemeIndex by remember { mutableIntStateOf(0) } // 0: Dark, 1: Emerald, 2: Ocean

    val total = goals.size
    val completed = goals.count { it.isCompleted }
    val percent = if (total > 0) (completed * 100 / total) else 0
    val bestStreak = goals.maxOfOrNull { it.streak } ?: 0
    val nextIncomplete = goals.firstOrNull { !it.isCompleted }

    val widgetBgBrush = when (widgetThemeIndex) {
        0 -> Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
        1 -> Brush.linearGradient(listOf(Color(0xFF065F46), Color(0xFF022C22)))
        else -> Brush.linearGradient(listOf(Color(0xFF075985), Color(0xFF082F49)))
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Özelleştirilebilir Widget Stüdyosu",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Widget Style Switcher Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WidgetStyle.values().forEach { style ->
                FilterChip(
                    selected = selectedStyle == style,
                    onClick = { selectedStyle = style },
                    label = { Text(style.title, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Simulated Android Home Widget Preview Box
        Text(
            text = "Canlı Widget Önizlemesi (Ana Ekran Görünümü):",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(widgetBgBrush)
                    .padding(16.dp)
            ) {
                when (selectedStyle) {
                    WidgetStyle.COMPACT_PROGRESS -> {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color(0xFF10B981), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Hedef Takip",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                }
                                if (showStreak) {
                                    Text(
                                        text = "🔥 $bestStreak Gün Seri",
                                        color = Color(0xFFFBBF24),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Bugün: $completed / $total Tamamlandı (%$percent)",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { if (total > 0) completed.toFloat() / total else 0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF10B981),
                                trackColor = Color.White.copy(alpha = 0.2f),
                                strokeCap = StrokeCap.Round
                            )

                            if (showNextGoal && nextIncomplete != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Sıradaki: ${nextIncomplete.title} (${nextIncomplete.currentCount}/${nextIncomplete.targetCount} ${nextIncomplete.unit})",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    WidgetStyle.DETAILED_LIST -> {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Bugünkü Görevler ($completed/$total)",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "%$percent",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981),
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            goals.take(3).forEach { g ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(if (g.isCompleted) Color(0xFF10B981) else Color.White.copy(alpha = 0.3f), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = g.title,
                                        color = if (g.isCompleted) Color.White.copy(alpha = 0.5f) else Color.White,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${g.currentCount}/${g.targetCount}",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    WidgetStyle.MINIMAL_STREAK -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Aktif Seri",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "🔥 $bestStreak Gün",
                                    color = Color(0xFFFBBF24),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                )
                                Text(
                                    text = "Hedef Başarı Oranı: %$percent",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "%$percent",
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Widget Customization Options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Widget Teması:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    0 to Color(0xFF1E293B),
                    1 to Color(0xFF065F46),
                    2 to Color(0xFF075985)
                ).forEach { (idx, color) ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(color, CircleShape)
                            .border(
                                if (widgetThemeIndex == idx) 2.dp else 0.dp,
                                if (widgetThemeIndex == idx) MaterialTheme.colorScheme.primary else Color.Transparent,
                                CircleShape
                            )
                            .clickable { widgetThemeIndex = idx }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Seri Sayısını Göster", fontSize = 12.sp)
            Switch(checked = showStreak, onCheckedChange = { showStreak = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Sıradaki Hedefi Göster", fontSize = 12.sp)
            Switch(checked = showNextGoal, onCheckedChange = { showNextGoal = it })
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Button: Update Widget
        Button(
            onClick = onUpdateWidget,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("update_home_widget_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ana Ekran Widget'ını Şimdi Güncelle")
        }
    }
}
