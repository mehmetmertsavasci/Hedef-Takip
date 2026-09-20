package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.GoalDatabase
import com.example.data.GoalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class GoalAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, GoalAppWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            updateWidgets(context, appWidgetManager, allWidgetIds)
        }

        private fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            val scope = CoroutineScope(Dispatchers.IO)
            val db = GoalDatabase.getDatabase(context, scope)
            val repo = GoalRepository(db.goalDao())

            scope.launch {
                val goals = repo.allGoals.firstOrNull() ?: emptyList()
                val total = goals.size
                val completed = goals.count { it.isCompleted }
                val percent = if (total > 0) (completed * 100 / total) else 0
                val bestStreak = goals.maxOfOrNull { it.streak } ?: 0
                val nextIncomplete = goals.firstOrNull { !it.isCompleted }

                for (widgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.goal_app_widget)

                    // Set click to open MainActivity
                    val intent = Intent(context, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                    views.setTextViewText(R.id.widget_title, "Hedef Takip")
                    views.setTextViewText(R.id.widget_streak, "🔥 $bestStreak Gün Seri")
                    views.setTextViewText(R.id.widget_progress_text, "Bugün: $completed / $total Tamamlandı (%$percent)")
                    views.setProgressBar(R.id.widget_progress_bar, 100, percent, false)

                    val subtext = if (nextIncomplete != null) {
                        "Sıradaki: ${nextIncomplete.title} (${nextIncomplete.currentCount}/${nextIncomplete.targetCount} ${nextIncomplete.unit})"
                    } else if (total > 0 && completed == total) {
                        "🎉 Tebrikler! Tüm hedefler tamamlandı!"
                    } else {
                        "Henüz hedef eklenmedi."
                    }
                    views.setTextViewText(R.id.widget_subtext, subtext)

                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            }
        }
    }
}
