package ru.kotlix.skinshowcase.core.network.auth

import com.google.gson.annotations.SerializedName

/**
 * Ответ GET /auth/users/by-username/{username} (OpenAPI): steamId пользователя.
 */
data class UserSteamIdResponseDto(
    @SerializedName("steamId") val steamId: String
)
