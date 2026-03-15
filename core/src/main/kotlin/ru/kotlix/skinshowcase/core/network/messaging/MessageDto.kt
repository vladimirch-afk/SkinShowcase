package ru.kotlix.skinshowcase.core.network.messaging

import com.google.gson.annotations.SerializedName

/**
 * DTO сообщения с бэкенда.
 */
data class MessageDto(
    @SerializedName("id") val id: String,
    @SerializedName("text") val text: String,
    @SerializedName("isOutgoing") val isOutgoing: Boolean,
    @SerializedName("timeMillis") val timeMillis: Long
)
