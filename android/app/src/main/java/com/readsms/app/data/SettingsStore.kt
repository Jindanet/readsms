package com.readsms.app.data

import android.content.Context
import android.provider.Settings

class SettingsStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("readsms_settings", Context.MODE_PRIVATE)

    var baseUrl: String
        get() = prefs.getString("base_url", "") ?: ""
        set(value) = prefs.edit().putString("base_url", value.trim().trimEnd('/')).apply()

    var apiToken: String
        get() = prefs.getString("api_token", "") ?: ""
        set(value) = prefs.edit().putString("api_token", value.trim()).apply()

    var role: String
        get() = prefs.getString("role", "collector") ?: "collector"
        set(value) = prefs.edit().putString("role", value).apply()

    var ownerReadInitialized: Boolean
        get() = prefs.getBoolean("owner_read_initialized", false)
        set(value) = prefs.edit().putBoolean("owner_read_initialized", value).apply()

    var ownerReadMessageKeys: Set<String>
        get() = prefs.getStringSet("owner_read_message_keys", emptySet())?.toSet().orEmpty()
        set(value) = prefs.edit().putStringSet("owner_read_message_keys", value).apply()

    var deviceName: String
        get() = prefs.getString("device_name", android.os.Build.MODEL ?: "Android") ?: "Android"
        set(value) = prefs.edit().putString("device_name", value.trim()).apply()

    var deviceId: String
        get() {
            val existing = prefs.getString("device_id", null)
            if (!existing.isNullOrBlank()) return existing

            val androidId = Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
                ?: java.util.UUID.randomUUID().toString()
            val generated = "android-$androidId"
            prefs.edit().putString("device_id", generated).apply()
            return generated
        }
        set(value) = prefs.edit().putString("device_id", value.trim()).apply()
}
