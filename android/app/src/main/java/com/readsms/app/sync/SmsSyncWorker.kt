package com.readsms.app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.readsms.app.data.SettingsStore
import com.readsms.app.data.SmsFileQueue
import com.readsms.app.net.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }
}
