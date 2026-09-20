package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Genel", // Sağlık, Spor, Çalışma, Alışkanlık, Finans, Farkındalık
    val targetCount: Int = 1,
    val currentCount: Int = 0,
    val unit: String = "Kez", // Bardak, Adım, Sayfa, Dakika, vb.
    val isCompleted: Boolean = false,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val hasReminder: Boolean = false,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val isAlarm: Boolean = false, // True: yüksek sesli alarm; False: normal bildirim
    val createdTimestamp: Long = System.currentTimeMillis(),
    val lastCompletedDate: String = "", // YYYY-MM-DD
    val historyDates: String = "", // Virgülle ayrılmış YYYY-MM-DD
    val colorHex: String = "#10B981"
) {
    val progress: Float
        get() = if (targetCount <= 0) 1f else (currentCount.toFloat() / targetCount).coerceIn(0f, 1f)

    val formattedReminderTime: String
        get() = String.format("%02d:%02d", reminderHour, reminderMinute)
}

enum class AppThemeMode(val displayName: String, val primaryHex: String) {
    EMERALD("Zümrüt Yeşili", "#10B981"),
    OCEAN("Okyanus Mavisi", "#0284C7"),
    CYBER_VIOLET("Siber Mor", "#8B5CF6"),
    SUNSET("Gün Batımı", "#F97316"),
    OBSIDIAN("Obsidiyen Siyah", "#475569")
}

enum class DarkModePreference(val displayName: String) {
    SYSTEM("Sistem"),
    LIGHT("Açık"),
    DARK("Koyu"),
    AMOLED("AMOLED Siyah")
}

data class CategoryPreset(
    val name: String,
    val defaultUnit: String,
    val defaultTarget: Int,
    val colorHex: String,
    val iconName: String
)
