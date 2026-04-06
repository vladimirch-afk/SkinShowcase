package ru.kotlix.skinshowcase.core.network.messaging

import com.google.gson.annotations.SerializedName
import ru.kotlix.skinshowcase.core.network.auth.AvatarUrls

/**
 * DTO чата с бэкенда (список чатов). Совпадает с ChatSummaryDto на messaging (в т.ч. preset / support).
 */
data class ChatDto(
    @SerializedName("counterpartySteamId") val counterpartySteamId: String,
    @SerializedName("counterpartyNickname") val counterpartyNickname: String? = null,
    @SerializedName("lastMessagePreview") val lastMessagePreview: String? = null,
    @SerializedName("lastMessageAt") val lastMessageAt: String? = null,
    @SerializedName("support") val support: Boolean = false,
    @SerializedName("counterpartyPresetAvatarId") val counterpartyPresetAvatarId: Int? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null
)

/** URL аватарки собеседника для списка чатов: не для поддержки; иначе пресет из ответа или явный avatarUrl. */
fun ChatDto.resolvedCounterpartyAvatarUrl(): String? {
    if (support) return null
    if (!avatarUrl.isNullOrBlank()) return avatarUrl
    return AvatarUrls.presetUrlFromAuthBatch(counterpartyPresetAvatarId)
}
