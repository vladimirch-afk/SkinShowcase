package ru.kotlix.skinshowcase.core.network.auth

import com.google.gson.annotations.SerializedName

/**
 * Ответ GET /auth/me (api-gateway → auth): профиль аутентифицированного пользователя.
 */
data class MeResponseDto(
    @SerializedName("steamId") val steamId: String?,
    @SerializedName("privateProfile") val privateProfile: Boolean? = null,
    @SerializedName("successfulTradesCount") val successfulTradesCount: Int? = null,
    @SerializedName("lastOnlineAt") val lastOnlineAt: String? = null
)
