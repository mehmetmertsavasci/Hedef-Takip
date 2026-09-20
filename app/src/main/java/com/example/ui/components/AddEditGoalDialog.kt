package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Goal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditGoalDialog(
    initialGoal: Goal? = null,
    onDismiss: () -> Unit,
    onSave: (Goal) -> Unit
) {
    var title by remember { mutableStateOf(initialGoal?.title ?: "") }
    var description by remember { mutableStateOf(initialGoal?.description ?: "") }
    var category by remember { mutableStateOf(initialGoal?.category ?: "Sağlık") }
    var targetCountText by remember { mutableStateOf(initialGoal?.targetCount?.toString() ?: "1") }
    var unit by remember { mutableStateOf(initialGoal?.unit ?: "Kez") }
    var hasReminder by remember { mutableStateOf(initialGoal?.hasReminder ?: false) }
    var reminderHour by remember { mutableIntStateOf(initialGoal?.reminderHour ?: 9) }
    var reminderMinute by remember { mutableIntStateOf(initialGoal?.reminderMinute ?: 0) }
    var isAlarm by remember { mutableStateOf(initialGoal?.isAlarm ?: false) }

    val categories = listOf(
        "Sağlık" to "#0EA5E9",
        "Spor" to "#10B981",
        "Çalışma" to "#F59E0B",
        "Farkındalık" to "#8B5CF6",
        "Finans" to "#EC4899",
        "Alışkanlık" to "#14B8A6"
    )

    val commonUnits = listOf("Kez", "Bardak", "Adım", "Sayfa", "Dakika", "Saat")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialGoal == null) "Yeni Günlük Hedef" else "Hedefi Düzenle",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Hedef Başlığı *") },
                    placeholder = { Text("Örn: Günde 2L Su İç") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_goal_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Açıklama veya Motivasyon") },
                    placeholder = { Text("Örn: Sağlıklı kalmak için") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selection
                Text(
                    text = "Kategori",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { (catName, colorHex) ->
                        val isSelected = category == catName
                        val catColor = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { Color(0xFF10B981) }
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) catColor else catColor.copy(alpha = 0.12f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { category = catName }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = catName,
                                color = if (isSelected) Color.White else catColor,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Target and Unit
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = targetCountText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) targetCountText = it },
                        label = { Text("Hedef Miktar") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Birim") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Unit selector chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    commonUnits.forEach { u ->
                        Box(
                            modifier = Modifier
                                .background(
                                    if (unit == u) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { unit = u }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = u,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reminder Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bildirim Hatırlatıcı",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                    Switch(
                        checked = hasReminder,
                        onCheckedChange = { hasReminder = it }
                    )
                }

                if (hasReminder) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Time Picker (Hour & Minute Stepper)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hatırlatma Saati:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Hour
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { reminderHour = (reminderHour - 1 + 24) % 24 },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) { Text("-") }
                                Text(
                                    text = String.format("%02d", reminderHour),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Button(
                                    onClick = { reminderHour = (reminderHour + 1) % 24 },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) { Text("+") }
                            }
                            Text(text = " : ", fontWeight = FontWeight.Bold)
                            // Minute
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { reminderMinute = (reminderMinute - 15 + 60) % 60 },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) { Text("-") }
                                Text(
                                    text = String.format("%02d", reminderMinute),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Button(
                                    onClick = { reminderMinute = (reminderMinute + 15) % 60 },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) { Text("+") }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Alarm Toggle (Sesli & Titreşimli Alarm)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isAlarm) Color(0xFFEF4444).copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = if (isAlarm) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Yüksek Sesli Alarm Modu",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isAlarm) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Alarm sesi ve erteleme seçeneğiyle çalar",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Switch(
                            checked = isAlarm,
                            onCheckedChange = { isAlarm = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val target = targetCountText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                        val selectedCategoryColor = categories.find { it.first == category }?.second ?: "#10B981"
                        val goal = initialGoal?.copy(
                            title = title.trim(),
                            description = description.trim(),
                            category = category,
                            targetCount = target,
                            unit = unit.trim().ifBlank { "Kez" },
                            hasReminder = hasReminder,
                            reminderHour = reminderHour,
                            reminderMinute = reminderMinute,
                            isAlarm = isAlarm,
                            colorHex = selectedCategoryColor
                        ) ?: Goal(
                            title = title.trim(),
                            description = description.trim(),
                            category = category,
                            targetCount = target,
                            unit = unit.trim().ifBlank { "Kez" },
                            hasReminder = hasReminder,
                            reminderHour = reminderHour,
                            reminderMinute = reminderMinute,
                            isAlarm = isAlarm,
                            colorHex = selectedCategoryColor
                        )
                        onSave(goal)
                    }
                },
                modifier = Modifier.testTag("dialog_save_goal_btn")
            ) {
                Text(if (initialGoal == null) "Hedef Ekle" else "Kaydet")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
