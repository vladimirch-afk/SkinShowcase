package ru.kotlix.skinshowcase.core.network.messaging

import com.google.gson.annotations.SerializedName

/**
 * Тело запроса отправки сообщения.
 */
data class SendMessageRequest(
    @SerializedName("text") val text: String
)
