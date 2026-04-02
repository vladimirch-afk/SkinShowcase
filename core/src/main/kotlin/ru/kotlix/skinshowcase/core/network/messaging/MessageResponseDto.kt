package ru.kotlix.skinshowcase.core.network.messaging

import com.google.gson.annotations.SerializedName

/**
 * Ответ api-gateway → messaging: сообщение (id, senderSteamId, recipientSteamId, text, createdAt).
 * isOutgoing вычисляется на клиенте по CurrentUser.steamId.
 */
data class MessageResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("senderSteamId") val senderSteamId: String,
    @SerializedName("recipientSteamId") val recipientSteamId: String,
    @SerializedName("text") val text: String,
    @SerializedName("createdAt") val createdAt: String? = null
)
