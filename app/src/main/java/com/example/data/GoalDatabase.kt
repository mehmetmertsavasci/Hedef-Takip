package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.Goal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Database(entities = [Goal::class], version = 1, exportSchema = false)
abstract class GoalDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: GoalDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): GoalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GoalDatabase::class.java,
                    "goals_database"
                )
                    .addCallback(GoalDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class GoalDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialGoals(database.goalDao())
                    }
                }
            }

            private suspend fun populateInitialGoals(dao: GoalDao) {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val twoDaysAgo = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

                val initialGoals = listOf(
                    Goal(
                        title = "Günde 2.5 Litre Su İç",
                        description = "Vücut hidrasyonunu korumak için gün içine yay",
                        category = "Sağlık",
                        targetCount = 8,
                        currentCount = 5,
                        unit = "Bardak",
                        isCompleted = false,
                        streak = 5,
                        bestStreak = 12,
                        hasReminder = true,
                        reminderHour = 11,
                        reminderMinute = 30,
                        isAlarm = false,
                        colorHex = "#0EA5E9",
                        lastCompletedDate = yesterday,
                        historyDates = "$twoDaysAgo,$yesterday"
                    ),
                    Goal(
                        title = "Sabah 15 Dk Meditasyon",
                        description = "Güne zinde ve odaklanmış başla",
                        category = "Farkındalık",
                        targetCount = 15,
                        currentCount = 15,
                        unit = "Dakika",
                        isCompleted = true,
                        streak = 8,
                        bestStreak = 14,
                        hasReminder = true,
                        reminderHour = 7,
                        reminderMinute = 15,
                        isAlarm = true,
                        colorHex = "#8B5CF6",
                        lastCompletedDate = today,
                        historyDates = "$twoDaysAgo,$yesterday,$today"
                    ),
                    Goal(
                        title = "10.000 Adım Yürüyüş",
                        description = "Günlük kardiyo ve hareketlilik hedefi",
                        category = "Spor",
                        targetCount = 10000,
                        currentCount = 7450,
                        unit = "Adım",
                        isCompleted = false,
                        streak = 3,
                        bestStreak = 9,
                        hasReminder = true,
                        reminderHour = 18,
                        reminderMinute = 0,
                        isAlarm = false,
                        colorHex = "#10B981",
                        lastCompletedDate = yesterday,
                        historyDates = "$yesterday"
                    ),
                    Goal(
                        title = "30 Sayfa Kitap Oku",
                        description = "Kişisel gelişim veya mesleki okuma",
                        category = "Çalışma",
                        targetCount = 30,
                        currentCount = 20,
                        unit = "Sayfa",
                        isCompleted = false,
                        streak = 11,
                        bestStreak = 21,
                        hasReminder = true,
                        reminderHour = 21,
                        reminderMinute = 30,
                        isAlarm = false,
                        colorHex = "#F59E0B",
                        lastCompletedDate = yesterday,
                        historyDates = "$twoDaysAgo,$yesterday"
                    ),
                    Goal(
                        title = "Günlük Bütçe Kontrolü",
                        description = "Günün gelir ve giderlerini kaydet",
                        category = "Finans",
                        targetCount = 1,
                        currentCount = 1,
                        unit = "Kez",
                        isCompleted = true,
                        streak = 4,
                        bestStreak = 8,
                        hasReminder = true,
                        reminderHour = 22,
                        reminderMinute = 0,
                        isAlarm = false,
                        colorHex = "#EC4899",
                        lastCompletedDate = today,
                        historyDates = "$twoDaysAgo,$yesterday,$today"
                    )
                )
                dao.insertAll(initialGoals)
            }
        }
    }
}
