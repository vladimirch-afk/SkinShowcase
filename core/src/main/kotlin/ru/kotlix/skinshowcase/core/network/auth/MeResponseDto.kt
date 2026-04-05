package ru.kotlix.skinshowcase.core.network.auth

import com.google.gson.annotations.SerializedName

/**
 * Ответ GET /auth/me (api-gateway → auth): профиль аутентифицированного пользователя.
 */
data class MeResponseDto(
    @SerializedName("steamId") val steamId: String?,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("privateProfile") val privateProfile: Boolean? = null,
    @SerializedName("successfulTradesCount") val successfulTradesCount: Int? = null,
    @SerializedName("lastOnlineAt") val lastOnlineAt: String? = null,
    @SerializedName("steamTradeLink") val steamTradeLink: String? = null,
    /**
     * Auth-сервис (Jackson): `avatarSource`. Тело PATCH по-прежнему может отдавать только `source`.
     */
    @SerializedName(value = "avatarSource", alternate = ["source"]) val source: String? = null,
    /**
     * Auth-сервис: `selectedPresetAvatarId`. Альтернатива — `presetAvatarId` из тела PATCH.
     */
    @SerializedName(value = "selectedPresetAvatarId", alternate = ["presetAvatarId"]) val presetAvatarId: Int? = null,
    /** Публичный URL картинки (пресет или Steam), если сервер отдаёт. */
    @SerializedName("effectiveAvatarUrl") val effectiveAvatarUrl: String? = null,
    /** Для source=STEAM — прямая ссылка на картинку, если сервер отдаёт. */
    @SerializedName("steamAvatarUrl") val steamAvatarUrl: String? = null,
    /** Устаревшее строковое id пресета; используется, если нет [presetAvatarId]. */
    @SerializedName("avatarPresetId") val avatarPresetId: String? = null
)
