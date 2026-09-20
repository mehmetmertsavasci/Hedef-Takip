package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.api.ExternalApiHub
import com.example.model.Goal
import com.example.security.CryptoManager
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Hedef Takip", appName)
    }

    @Test
    fun `crypto manager e2ee encryption and decryption roundtrip`() {
        val originalText = "Günde 2 Litre Su İç ve 10 Sayfa Kitap Oku"
        val passphrase = "TestGizliParola@2026!"

        val encryptedEnvelope = CryptoManager.encrypt(originalText, passphrase)
        assertNotNull(encryptedEnvelope)

        val json = JSONObject(encryptedEnvelope)
        assertTrue(json.has("salt"))
        assertTrue(json.has("iv"))
        assertTrue(json.has("cipher"))
        assertEquals("AES-256-GCM", json.getString("version"))

        val decryptResult = CryptoManager.decrypt(encryptedEnvelope, passphrase)
        assertTrue(decryptResult.isSuccess)
        assertEquals(originalText, decryptResult.getOrNull())
    }

    @Test
    fun `external api hub generates valid simulated responses`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val hub = ExternalApiHub(context)

        val testGoals = listOf(
            Goal(id = 1, title = "Hedef 1", targetCount = 5, currentCount = 5, isCompleted = true, category = "Spor")
        )

        val response = hub.executeSimulatedApiCall("GET /api/v1/goals", testGoals)
        assertEquals(200, response.statusCode)
        assertTrue(response.responseBody.contains("Hedef 1"))
        assertTrue(response.responseBody.contains("success"))
    }
}
