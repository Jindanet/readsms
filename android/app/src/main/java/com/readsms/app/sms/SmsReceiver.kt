package com.readsms.app.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.readsms.app.data.SmsFileQueue
import com.readsms.app.model.SmsPayload
import com.readsms.app.sync.CollectorForegroundService
import com.readsms.app.sync.SyncScheduler
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (parts.isEmpty()) return

        val sender = parts.firstOrNull()?.originatingAddress
        val body = parts.joinToString(separator = "") { it.messageBody.orEmpty() }
        val timestamp = parts.minOfOrNull { it.timestampMillis } ?: System.currentTimeMillis()
        val receivedAt = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
            Instant.ofEpochMilli(timestamp).atOffset(ZoneOffset.UTC),
        )
        val smsId = "rx-${sha256("${sender.orEmpty()}|$timestamp|$body")}"

        SmsFileQueue(context).append(
            listOf(
                SmsPayload(
                    smsId = smsId,
                    sender = sender,
                    body = body,
                    receivedAt = receivedAt,
                    direction = "inbox",
                ),
            ),
        )
        SyncScheduler.enqueueNow(context)
        SyncScheduler.scheduleCollectorWatchdog(context)
        CollectorForegroundService.start(context)
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }.take(32)
    }
}
