package com.readsms.app.sync

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.readsms.app.data.SettingsStore
import com.readsms.app.data.SmsFileQueue
import com.readsms.app.data.SmsReader
import com.readsms.app.net.ApiClient
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.content.ContextCompat

class SmsSyncWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val settings = SettingsStore(applicationContext)
        if (settings.apiToken.isBlank() || settings.baseUrl.isBlank()) {
            return@withContext Result.success()
        }

        val queue = SmsFileQueue(applicationContext)
        val client = ApiClient(settings)

        runCatching {
            if (settings.role == "collector" && hasSmsPermission(applicationContext)) {
                val since = Instant.now().minusSeconds(24L * 60L * 60L).toEpochMilli()
                queue.append(SmsReader.readInboxSince(applicationContext, since))
            }

            var batches = 0
            while (batches < 10) {
                val batch = queue.readPending(limit = 100)
                if (batch.isEmpty()) break
                client.sync(batch)
                queue.markSynced(batch)
                batches += 1
            }
            if (queue.countPending() > 0) {
                SyncScheduler.enqueueNow(applicationContext)
            }
            if (settings.role == "collector") {
                SyncScheduler.scheduleCollectorWatchdog(applicationContext)
            }
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }

    private fun hasSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
    }
}
