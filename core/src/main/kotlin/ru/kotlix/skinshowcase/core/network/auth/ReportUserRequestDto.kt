package ru.kotlix.skinshowcase.core.network.auth

import com.google.gson.annotations.SerializedName

/** Тело POST /auth/users/{steamId}/report */
data class ReportUserRequestDto(
    @SerializedName("reason") val reason: String,
    @SerializedName("details") val details: String? = null
)
