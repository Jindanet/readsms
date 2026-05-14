package com.readsms.app.data

import android.content.Context
import com.readsms.app.model.SmsPayload
import java.io.File
import kotlinx.serialization.json.Json

class SmsFileQueue(context: Context) {
    private val pendingFile = File(context.filesDir, "pending_sms.jsonl")
    private val syncedFile = File(context.filesDir, "synced_sms.jsonl")
    private val json = Json { ignoreUnknownKeys = true }

    @Synchronized
    fun append(messages: List<SmsPayload>): Int {
        if (messages.isEmpty()) return 0
        pendingFile.parentFile?.mkdirs()
        val pendingIds = readPending(limit = Int.MAX_VALUE).map { it.smsId }.toSet()
        val newMessages = messages.filterNot { it.smsId in pendingIds }
        if (newMessages.isEmpty()) return 0
        pendingFile.appendText(newMessages.joinToString(separator = "\n") { json.encodeToString(SmsPayload.serializer(), it) } + "\n")
        return newMessages.size
    }

    @Synchronized
    fun readPending(limit: Int = 100): List<SmsPayload> {
        if (!pendingFile.exists()) return emptyList()
        return pendingFile.readLines()
            .asSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { runCatching { json.decodeFromString(SmsPayload.serializer(), it) }.getOrNull() }
            .take(limit)
            .toList()
    }

    @Synchronized
    fun markSynced(sent: List<SmsPayload>) {
        if (sent.isEmpty() || !pendingFile.exists()) return
        val sentIds = sent.map { it.smsId }.toSet()
        val all = pendingFile.readLines()
            .asSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { runCatching { json.decodeFromString(SmsPayload.serializer(), it) }.getOrNull() }
            .toList()

        val remaining = all.filterNot { it.smsId in sentIds }
        pendingFile.writeText(
            if (remaining.isEmpty()) "" else remaining.joinToString(separator = "\n") {
                json.encodeToString(SmsPayload.serializer(), it)
            } + "\n"
        )

        syncedFile.appendText(sent.joinToString(separator = "\n") { json.encodeToString(SmsPayload.serializer(), it) } + "\n")
    }

    @Synchronized
    fun countPending(): Int {
        if (!pendingFile.exists()) return 0
        return pendingFile.readLines().count { it.isNotBlank() }
    }
}
