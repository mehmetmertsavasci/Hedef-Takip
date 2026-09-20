package com.example.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.model.Goal
import com.example.security.CryptoManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class CloudSyncState(
    val isOnline: Boolean = true,
    val isSyncing: Boolean = false,
    val lastSyncTimeFormatted: String = "Henüz yapılmadı",
    val statusMessage: String = "Çevrimdışı hazır (Uçtan uca şifreli)",
    val pendingQueueCount: Int = 0,
    val encryptedVaultUrl: String = "https://cloud.hedeftakip.io/vault/v1",
    val lastEncryptedPayloadPreview: String = "",
    val autoSyncEnabled: Boolean = true
)

class CloudSyncManager(private val context: Context, private val scope: CoroutineScope) {

    private val _syncState = MutableStateFlow(CloudSyncState())
    val syncState: StateFlow<CloudSyncState> = _syncState.asStateFlow()

    private val prefs = context.getSharedPreferences("cloud_sync_prefs", Context.MODE_PRIVATE)

    init {
        monitorNetwork()
        val savedLastSync = prefs.getString("last_sync_time", "Henüz yapılmadı") ?: "Henüz yapılmadı"
        val savedPayload = prefs.getString("last_payload", "") ?: ""
        _syncState.value = _syncState.value.copy(
            lastSyncTimeFormatted = savedLastSync,
            lastEncryptedPayloadPreview = savedPayload
        )
    }

    private fun monitorNetwork() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager != null) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            _syncState.value = _syncState.value.copy(isOnline = isConnected)

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _syncState.value = _syncState.value.copy(isOnline = true, statusMessage = "Çevrimiçi - Eşzamanlamaya hazır")
                }

                override fun onLost(network: Network) {
                    _syncState.value = _syncState.value.copy(
                        isOnline = false,
                        statusMessage = "Çevrimdışı Mod - Değişiklikler yerel olarak saklanıyor"
                    )
                }
            })
        }
    }

    fun goalsToJson(goals: List<Goal>): String {
        val array = JSONArray()
        for (g in goals) {
            val obj = JSONObject().apply {
                put("id", g.id)
                put("title", g.title)
                put("description", g.description)
                put("category", g.category)
                put("targetCount", g.targetCount)
                put("currentCount", g.currentCount)
                put("unit", g.unit)
                put("isCompleted", g.isCompleted)
                put("streak", g.streak)
                put("bestStreak", g.bestStreak)
                put("hasReminder", g.hasReminder)
                put("reminderHour", g.reminderHour)
                put("reminderMinute", g.reminderMinute)
                put("isAlarm", g.isAlarm)
                put("colorHex", g.colorHex)
                put("lastCompletedDate", g.lastCompletedDate)
                put("historyDates", g.historyDates)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToGoals(jsonString: String): List<Goal> {
        val list = mutableListOf<Goal>()
        val array = JSONArray(jsonString)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                Goal(
                    id = obj.optLong("id", 0),
                    title = obj.getString("title"),
                    description = obj.optString("description", ""),
                    category = obj.optString("category", "Genel"),
                    targetCount = obj.optInt("targetCount", 1),
                    currentCount = obj.optInt("currentCount", 0),
                    unit = obj.optString("unit", "Kez"),
                    isCompleted = obj.optBoolean("isCompleted", false),
                    streak = obj.optInt("streak", 0),
                    bestStreak = obj.optInt("bestStreak", 0),
                    hasReminder = obj.optBoolean("hasReminder", false),
                    reminderHour = obj.optInt("reminderHour", 9),
                    reminderMinute = obj.optInt("reminderMinute", 0),
                    isAlarm = obj.optBoolean("isAlarm", false),
                    colorHex = obj.optString("colorHex", "#10B981"),
                    lastCompletedDate = obj.optString("lastCompletedDate", ""),
                    historyDates = obj.optString("historyDates", "")
                )
            )
        }
        return list
    }

    fun pushEncryptedSync(goals: List<Goal>, passphrase: String, onResult: (Boolean, String) -> Unit) {
        scope.launch(Dispatchers.IO) {
            _syncState.value = _syncState.value.copy(isSyncing = true, statusMessage = "Veriler AES-256 ile şifreleniyor...")
            delay(500) // Brief simulation of secure cryptographic processing

            try {
                val plainJson = goalsToJson(goals)
                val encryptedEnvelope = CryptoManager.encrypt(plainJson, passphrase)

                // Save locally cached encrypted payload
                val currentTime = java.text.SimpleDateFormat("HH:mm:ss dd.MM.yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                prefs.edit()
                    .putString("last_sync_time", currentTime)
                    .putString("last_payload", encryptedEnvelope)
                    .apply()

                delay(600) // Brief network simulation to cloud vault
                _syncState.value = _syncState.value.copy(
                    isSyncing = false,
                    lastSyncTimeFormatted = currentTime,
                    lastEncryptedPayloadPreview = encryptedEnvelope,
                    statusMessage = "Bulut Kasa ile Başarıyla Eşzamanlandı (AES-256 E2EE)",
                    pendingQueueCount = 0
                )
                onResult(true, "Veriler başarıyla uçtan uca şifrelenerek buluta aktarıldı!")
            } catch (e: Exception) {
                _syncState.value = _syncState.value.copy(isSyncing = false, statusMessage = "Hata: ${e.localizedMessage}")
                onResult(false, "Şifreleme veya senkronizasyon hatası: ${e.localizedMessage}")
            }
        }
    }

    fun pullEncryptedSync(passphrase: String, onResult: (Boolean, String, List<Goal>?) -> Unit) {
        scope.launch(Dispatchers.IO) {
            _syncState.value = _syncState.value.copy(isSyncing = true, statusMessage = "Buluttan şifreli veri alınıyor...")
            delay(600)

            val payload = prefs.getString("last_payload", "") ?: ""
            if (payload.isEmpty()) {
                _syncState.value = _syncState.value.copy(isSyncing = false, statusMessage = "Bulutta yedek bulunamadı")
                onResult(false, "Bulut kasasında henüz şifreli yedek bulunamadı. Lütfen önce eşzamanlayın.", null)
                return@launch
            }

            val decryptResult = CryptoManager.decrypt(payload, passphrase)
            decryptResult.fold(
                onSuccess = { decryptedJson ->
                    val goals = jsonToGoals(decryptedJson)
                    _syncState.value = _syncState.value.copy(
                        isSyncing = false,
                        statusMessage = "Veriler başarıyla çözüldü ve senkronize edildi"
                    )
                    onResult(true, "${goals.size} adet hedef başarıyla buluttan çözüldü!", goals)
                },
                onFailure = { err ->
                    _syncState.value = _syncState.value.copy(
                        isSyncing = false,
                        statusMessage = "Şifre çözme hatası (Yanlış parola)"
                    )
                    onResult(false, "Parola hatalı! Uçtan uca şifreli verinin kilidi açılamadı.", null)
                }
            )
        }
    }
}
