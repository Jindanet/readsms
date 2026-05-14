package com.readsms.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfo(
    val id: String,
    val name: String? = null,
    val role: String = "collector",
)

@Serializable
data class SmsPayload(
    @SerialName("sms_id") val smsId: String,
    val sender: String? = null,
    val body: String,
    @SerialName("received_at") val receivedAt: String,
    @SerialName("sim_slot") val simSlot: Int? = null,
    val direction: String = "inbox",
)

@Serializable
data class SmsSyncRequest(
    val device: DeviceInfo,
    val messages: List<SmsPayload>,
)

@Serializable
data class SmsSyncResponse(
    val inserted: Int,
    val duplicates: Int,
    val messages: List<SmsRow> = emptyList(),
)

@Serializable
data class SmsListResponse(
    val count: Int,
    val messages: List<SmsRow>,
)

@Serializable
data class SmsRow(
    val id: Int,
    @SerialName("device_id") val deviceId: String,
    @SerialName("sms_id") val smsId: String,
    val sender: String? = null,
    val body: String,
    @SerialName("received_at") val receivedAt: String,
    @SerialName("received_at_ms") val receivedAtMs: Long,
    @SerialName("sim_slot") val simSlot: Int? = null,
    val direction: String = "inbox",
    @SerialName("created_at") val createdAt: String,
)
