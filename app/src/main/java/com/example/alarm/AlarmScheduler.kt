package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.model.Goal
import com.example.receiver.GoalReminderReceiver
import java.util.Calendar

object AlarmScheduler {
    const val TAG = "AlarmScheduler"

    fun schedule(context: Context, goal: Goal) {
        if (!goal.hasReminder) {
            cancel(context, goal.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, goal.reminderHour)
            set(Calendar.MINUTE, goal.reminderMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If the time has already passed today, schedule for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, GoalReminderReceiver::class.java).apply {
            action = if (goal.isAlarm) GoalReminderReceiver.ACTION_GOAL_ALARM else GoalReminderReceiver.ACTION_GOAL_REMINDER
            putExtra(GoalReminderReceiver.EXTRA_GOAL_ID, goal.id)
            putExtra(GoalReminderReceiver.EXTRA_GOAL_TITLE, goal.title)
            putExtra(GoalReminderReceiver.EXTRA_GOAL_CATEGORY, goal.category)
            putExtra(GoalReminderReceiver.EXTRA_IS_ALARM, goal.isAlarm)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            goal.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled ${if (goal.isAlarm) "Alarm" else "Reminder"} for Goal ${goal.id} at ${calendar.time}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Exact alarm permission not granted", e)
        }
    }

    fun cancel(context: Context, goalId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, GoalReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            goalId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm for Goal $goalId")
        }
    }

    fun snooze(context: Context, goalId: Long, goalTitle: String, minutes: Int = 10) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerAt = System.currentTimeMillis() + (minutes * 60 * 1000)

        val intent = Intent(context, GoalReminderReceiver::class.java).apply {
            action = GoalReminderReceiver.ACTION_GOAL_ALARM
            putExtra(GoalReminderReceiver.EXTRA_GOAL_ID, goalId)
            putExtra(GoalReminderReceiver.EXTRA_GOAL_TITLE, goalTitle)
            putExtra(GoalReminderReceiver.EXTRA_IS_ALARM, true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (goalId + 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to snooze alarm", e)
        }
    }
}
