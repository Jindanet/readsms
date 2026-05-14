package com.readsms.app.sync

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.readsms.app.MainActivity
import com.readsms.app.R
import com.readsms.app.data.SettingsStore
import com.readsms.app.data.SmsFileQueue
import com.readsms.app.data.SmsReader
import com.readsms.app.net.ApiClient
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CollectorForegroundService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var loopStarted = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification("Starting keep-alive sync"))
        if (!loopStarted) {
            loopStarted = true
            scope.launch { runLoop() }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private suspend fun runLoop() {
        while (true) {
            val status = runCatching { syncOnce() }
                .getOrElse { "Sync failed: ${it.message ?: it.javaClass.simpleName}" }
            updateNotification(status)
            delay(SYNC_INTERVAL_MS)
        }
    }

    private suspend fun syncOnce(): String = withContext(Dispatchers.IO) {
        val settings = SettingsStore(applicationContext)
        if (settings.role != "collector") {
            settings.role = "collector"
        }
        if (settings.baseUrl.isBlank() || settings.apiToken.isBlank()) {
            return@withContext "Waiting for server setup"
        }
        if (!hasSmsPermission()) {
            return@withContext "Waiting for SMS permission"
        }

        val queue = SmsFileQueue(applicationContext)
        val since = Instant.now().minusSeconds(24L * 60L * 60L).toEpochMilli()
        val rows = SmsReader.readInboxSince(applicationContext, since)
        val added = queue.append(rows)

        var synced = 0
        val client = ApiClient(settings)
        repeat(10) {
            val batch = queue.readPending(limit = 100)
            if (batch.isEmpty()) return@repeat
            client.sync(batch)
            queue.markSynced(batch)
            synced += batch.size
        }

        val pending = queue.countPending()
        "Read ${rows.size}, queued $added, synced $synced, pending $pending"
    }

    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Collector keep-alive sync",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Keeps SMS sync running on restricted Android devices"
            setSound(null, null)
            enableVibration(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(status: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sms_notification)
            .setContentTitle("ReadSMS Collector is running")
            .setContentText(status)
            .setStyle(NotificationCompat.BigTextStyle().bigText(status))
            .setContentIntent(openAppIntent())
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun updateNotification(status: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(status))
    }

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val CHANNEL_ID = "collector_keep_alive_sync"
        private const val NOTIFICATION_ID = 2101
        private const val SYNC_INTERVAL_MS = 10_000L

        fun start(context: Context) {
            val appContext = context.applicationContext
            val intent = Intent(appContext, CollectorForegroundService::class.java)
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appContext.startForegroundService(intent)
                } else {
                    appContext.startService(intent)
                }
            }
        }

        fun stop(context: Context) {
            context.applicationContext.stopService(
                Intent(context.applicationContext, CollectorForegroundService::class.java),
            )
        }
    }
}
