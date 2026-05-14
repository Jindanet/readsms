package com.readsms.app.net

import com.readsms.app.data.SettingsStore
import com.readsms.app.model.DeviceInfo
import com.readsms.app.model.SmsListResponse
import com.readsms.app.model.SmsPayload
import com.readsms.app.model.SmsSyncRequest
import com.readsms.app.model.SmsSyncResponse
import java.net.URLEncoder
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class ApiClient(private val settings: SettingsStore) {
    companion object {
        private val http = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .pingInterval(20, TimeUnit.SECONDS)
            .build()
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    fun validate() {
        val request = Request.Builder()
            .url("${settings.baseUrl}/api/validate")
            .header("Authorization", "Bearer ${settings.apiToken}")
            .get()
            .build()

        http.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) error("Validation failed: HTTP ${response.code} $body")
        }
    }

    fun sync(messages: List<SmsPayload>): SmsSyncResponse {
        val payload = SmsSyncRequest(
            device = DeviceInfo(
                id = settings.deviceId,
                name = settings.deviceName,
                role = settings.role,
            ),
            messages = messages,
        )
        val request = Request.Builder()
            .url("${settings.baseUrl}/api/sms/sync")
            .header("Authorization", "Bearer ${settings.apiToken}")
            .post(json.encodeToString(payload).toRequestBody(mediaType))
            .build()

        http.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) error("Sync failed: HTTP ${response.code} $body")
            return json.decodeFromString(SmsSyncResponse.serializer(), body)
        }
    }

    fun recent(limit: Int = 100): SmsListResponse {
        val request = Request.Builder()
            .url("${settings.baseUrl}/api/sms/recent?limit=$limit")
            .header("Authorization", "Bearer ${settings.apiToken}")
            .get()
            .build()

        http.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) error("Fetch failed: HTTP ${response.code} $body")
            return json.decodeFromString(SmsListResponse.serializer(), body)
        }
    }

    fun viewerSocket(listener: WebSocketListener): WebSocket {
        val url = settings.baseUrl
            .replaceFirst("https://", "wss://")
            .replaceFirst("http://", "ws://")
        val token = URLEncoder.encode(settings.apiToken, Charsets.UTF_8.name())
        val request = Request.Builder()
            .url("$url/ws/viewer?token=$token")
            .build()
        return http.newWebSocket(request, listener)
    }
}
