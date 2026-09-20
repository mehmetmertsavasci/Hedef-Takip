package com.example.api

import android.content.Context
import com.example.model.Goal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

data class ApiResponse(
    val statusCode: Int,
    val latencyMs: Long,
    val responseBody: String,
    val endpoint: String
)

class ExternalApiHub(private val context: Context) {

    private val prefs = context.getSharedPreferences("api_hub_prefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    fun getApiKey(): String {
        var key = prefs.getString("api_key", null)
        if (key == null) {
            key = "ht_live_" + UUID.randomUUID().toString().replace("-", "").take(24)
            prefs.edit().putString("api_key", key).apply()
        }
        return key
    }

    fun regenerateApiKey(): String {
        val newKey = "ht_live_" + UUID.randomUUID().toString().replace("-", "").take(24)
        prefs.edit().putString("api_key", newKey).apply()
        return newKey
    }

    fun getWebhookUrl(): String {
        return prefs.getString("webhook_url", "https://api.ornek.com/v1/goal-events") ?: ""
    }

    fun setWebhookUrl(url: String) {
        prefs.edit().putString("webhook_url", url).apply()
    }

    fun isWebhookEnabled(): Boolean {
        return prefs.getBoolean("webhook_enabled", false)
    }

    fun setWebhookEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("webhook_enabled", enabled).apply()
    }

    suspend fun dispatchGoalCompletedWebhook(goal: Goal) {
        val webhookUrl = getWebhookUrl()
        if (!isWebhookEnabled() || webhookUrl.isBlank() || !webhookUrl.startsWith("http")) return

        withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject().apply {
                    put("event", "goal.completed")
                    put("timestamp", System.currentTimeMillis())
                    put("data", JSONObject().apply {
                        put("id", goal.id)
                        put("title", goal.title)
                        put("category", goal.category)
                        put("streak", goal.streak)
                        put("target", goal.targetCount)
                        put("current", goal.currentCount)
                    })
                }
                val body = payload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(webhookUrl)
                    .addHeader("Authorization", "Bearer ${getApiKey()}")
                    .post(body)
                    .build()
                client.newCall(request).execute().close()
            } catch (e: Exception) {
                // Ignore failure in background
            }
        }
    }

    suspend fun executeSimulatedApiCall(
        endpoint: String,
        currentGoals: List<Goal>
    ): ApiResponse = withContext(Dispatchers.Default) {
        val start = System.currentTimeMillis()

        val (code, json) = when (endpoint) {
            "GET /api/v1/goals" -> {
                val array = JSONArray()
                for (g in currentGoals) {
                    array.put(JSONObject().apply {
                        put("id", g.id)
                        put("title", g.title)
                        put("category", g.category)
                        put("target", g.targetCount)
                        put("current", g.currentCount)
                        put("unit", g.unit)
                        put("isCompleted", g.isCompleted)
                        put("streak", g.streak)
                    })
                }
                200 to JSONObject().apply {
                    put("status", "success")
                    put("count", currentGoals.size)
                    put("data", array)
                }.toString(2)
            }

            "GET /api/v1/stats" -> {
                val total = currentGoals.size
                val completed = currentGoals.count { it.isCompleted }
                val rate = if (total > 0) (completed * 100 / total) else 0
                val bestStreak = currentGoals.maxOfOrNull { it.bestStreak } ?: 0

                200 to JSONObject().apply {
                    put("status", "success")
                    put("summary", JSONObject().apply {
                        put("total_goals", total)
                        put("completed_today", completed)
                        put("completion_rate_percent", rate)
                        put("best_active_streak", bestStreak)
                    })
                }.toString(2)
            }

            "POST /api/v1/export" -> {
                200 to JSONObject().apply {
                    put("export_format", "REST_STANDARD_V1")
                    put("schema", "https://schema.hedeftakip.io/v1/goals.json")
                    put("timestamp", System.currentTimeMillis())
                    put("item_count", currentGoals.size)
                }.toString(2)
            }

            else -> {
                404 to JSONObject().apply {
                    put("error", "Endpoint bulunamadı")
                }.toString(2)
            }
        }

        val latency = System.currentTimeMillis() - start + 45 // realistic latency
        ApiResponse(
            statusCode = code,
            latencyMs = latency,
            responseBody = json,
            endpoint = endpoint
        )
    }
}
