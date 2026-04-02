package ru.kotlix.skinshowcase.core.network.auth

import com.google.gson.annotations.SerializedName

/** Ответ GET /auth/users/{steamId}/trade-link */
data class UserTradeLinkResponseDto(
    @SerializedName("tradeUrl") val tradeUrl: String? = null
)
