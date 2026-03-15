package ru.kotlix.skinshowcase.core.network.messaging

import com.google.gson.annotations.SerializedName

/**
 * DTO чата с бэкенда (список чатов). Совпадает с ChatSummaryDto на сервере.
 */
data class ChatDto(
    @SerializedName("counterpartySteamId") val counterpartySteamId: String,
    @SerializedName("counterpartyNickname") val counterpartyNickname: String? = null,
    @SerializedName("lastMessagePreview") val lastMessagePreview: String? = null,
    @SerializedName("lastMessageAt") val lastMessageAt: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null
)
