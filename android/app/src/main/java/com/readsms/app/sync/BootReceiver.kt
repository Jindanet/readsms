package com.readsms.app.sync

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.readsms.app.data.SettingsStore

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                SyncScheduler.schedulePeriodic(context)
                SyncScheduler.scheduleCollectorWatchdog(context)
                SyncScheduler.enqueueNow(context)
                if (SettingsStore(context).role == "collector") {
                    CollectorForegroundService.start(context)
                }
            }
        }
    }
}
