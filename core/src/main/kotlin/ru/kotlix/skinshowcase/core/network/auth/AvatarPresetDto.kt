package ru.kotlix.skinshowcase.core.network.auth

import com.google.gson.annotations.SerializedName

/** Элемент списка GET /auth/avatars. */
data class AvatarPresetDto(
    @SerializedName("id") val id: String?
)
