package ru.kotlix.skinshowcase.core.network.auth

import com.google.gson.annotations.SerializedName

/** Тело PATCH /auth/me/display-name */
data class UpdateDisplayNameRequestDto(
    @SerializedName("displayName") val displayName: String
)
