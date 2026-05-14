package com.readsms.app.data

import android.content.Context
import android.provider.Telephony
import com.readsms.app.model.SmsPayload
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object SmsReader {
    private val isoFormatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    fun readInboxSince(context: Context, sinceMillis: Long): List<SmsPayload> {
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
        )
        val cursor = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            projection,
            "${Telephony.Sms.DATE} >= ?",
            arrayOf(sinceMillis.toString()),
            "${Telephony.Sms.DATE} DESC",
        ) ?: return emptyList()

        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(Telephony.Sms._ID)
            val senderIndex = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val rows = mutableListOf<SmsPayload>()

            while (it.moveToNext()) {
                val dateMillis = it.getLong(dateIndex)
                rows += SmsPayload(
                    smsId = "android-${it.getLong(idIndex)}",
                    sender = it.getString(senderIndex),
                    body = it.getString(bodyIndex).orEmpty(),
                    receivedAt = isoFormatter.format(Instant.ofEpochMilli(dateMillis).atOffset(ZoneOffset.UTC)),
                    direction = "inbox",
                )
            }
            return rows
        }
    }
}
