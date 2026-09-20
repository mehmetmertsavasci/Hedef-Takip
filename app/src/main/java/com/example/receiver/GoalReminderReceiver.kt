package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.alarm.AlarmScheduler
import com.example.data.GoalDatabase
import com.example.data.GoalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoalReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_GOAL_REMINDER = "com.example.ACTION_GOAL_REMINDER"
        const val ACTION_GOAL_ALARM = "com.example.ACTION_GOAL_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.example.ACTION_SNOOZE_ALARM"
        const val ACTION_COMPLETE_GOAL = "com.example.ACTION_COMPLETE_GOAL"

        const val EXTRA_GOAL_ID = "extra_goal_id"
        const val EXTRA_GOAL_TITLE = "extra_goal_title"
        const val EXTRA_GOAL_CATEGORY = "extra_goal_category"
        const val EXTRA_IS_ALARM = "extra_is_alarm"

        const val CHANNEL_REMINDERS_ID = "channel_goal_reminders"
        const val CHANNEL_ALARMS_ID = "channel_goal_alarms"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val goalId = intent.getLongExtra(EXTRA_GOAL_ID, -1L)
        val goalTitle = intent.getStringExtra(EXTRA_GOAL_TITLE) ?: "Günlük Hedef"
        val isAlarm = intent.getBooleanExtra(EXTRA_IS_ALARM, false)

        when (action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // Reschedule all active goals with reminders
                val scope = CoroutineScope(Dispatchers.IO)
                val db = GoalDatabase.getDatabase(context, scope)
                val repo = GoalRepository(db.goalDao())
                scope.launch {
                    val goalsWithReminders = repo.getGoalsWithReminders()
                    for (g in goalsWithReminders) {
                        AlarmScheduler.schedule(context, g)
                    }
                }
            }

            ACTION_COMPLETE_GOAL -> {
                if (goalId != -1L) {
                    val scope = CoroutineScope(Dispatchers.IO)
                    val db = GoalDatabase.getDatabase(context, scope)
                    val repo = GoalRepository(db.goalDao())
                    scope.launch {
                        repo.incrementProgress(goalId)
                    }
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(goalId.toInt())
                }
            }

            ACTION_SNOOZE_ALARM -> {
                if (goalId != -1L) {
                    AlarmScheduler.snooze(context, goalId, goalTitle, 10)
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(goalId.toInt())
                }
            }

            ACTION_GOAL_REMINDER, ACTION_GOAL_ALARM -> {
                showNotification(context, goalId, goalTitle, isAlarm)
            }
        }
    }

    private fun showNotification(context: Context, goalId: Long, goalTitle: String, isAlarm: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channels for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(if (isAlarm) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                "Hedef Hatırlatıcıları",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Günlük hedefler için zamanlanmış hatırlatıcı bildirimleri"
                enableVibration(true)
            }

            val alarmChannel = NotificationChannel(
                CHANNEL_ALARMS_ID,
                "Hedef Alarmları (Yüksek Öncelik)",
                NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = "Kritik hedefler için alarm sesi ve titreşim"
                setSound(alarmSoundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(alarmChannel)
        }

        // Open App Intent
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            (goalId * 10).toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Tamamla" Intent
        val completeIntent = Intent(context, GoalReminderReceiver::class.java).apply {
            action = ACTION_COMPLETE_GOAL
            putExtra(EXTRA_GOAL_ID, goalId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            (goalId * 10 + 1).toInt(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (isAlarm) CHANNEL_ALARMS_ID else CHANNEL_REMINDERS_ID

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(if (isAlarm) "🚨 Hedef Alarmı: $goalTitle" else "🎯 Hedef Hatırlatıcı: $goalTitle")
            .setContentText("Bugünkü hedefinizi tamamlamak için şimdi harekete geçin!")
            .setPriority(if (isAlarm) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setCategory(if (isAlarm) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.checkbox_on_background, "Tamamla (+1)", completePendingIntent)

        if (isAlarm) {
            // "10 Dk Ertele" Intent
            val snoozeIntent = Intent(context, GoalReminderReceiver::class.java).apply {
                action = ACTION_SNOOZE_ALARM
                putExtra(EXTRA_GOAL_ID, goalId)
                putExtra(EXTRA_GOAL_TITLE, goalTitle)
            }
            val snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                (goalId * 10 + 2).toInt(),
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_menu_recent_history, "10 Dk Ertele", snoozePendingIntent)
            builder.setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
        }

        notificationManager.notify(goalId.toInt().coerceAtLeast(1), builder.build())
    }
}
