package ru.kotlix.skinshowcase.core.network.auth

import com.google.gson.annotations.SerializedName

/**
 * Ответ GET /auth/me/privacy (OpenAPI). Поле в JSON — "private".
 */
data class PrivacyResponseDto(
    @SerializedName("private") val privateProfile: Boolean
)
