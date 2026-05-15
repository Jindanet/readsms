package com.readsms.app.sync

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.readsms.app.data.SettingsStore

class CollectorWatchdogReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_COLLECTOR_WATCHDOG) return

        val settings = SettingsStore(context)
        SyncScheduler.schedulePeriodic(context)
        if (settings.role == "collector") {
            SyncScheduler.enqueueNow(context)
            CollectorForegroundService.start(context)
        }
        SyncScheduler.scheduleCollectorWatchdog(context)
    }

    companion object {
        const val ACTION_COLLECTOR_WATCHDOG = "com.readsms.app.action.COLLECTOR_WATCHDOG"
    }
}
