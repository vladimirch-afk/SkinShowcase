package ru.kotlix.skinshowcase.core.network.auth

import com.google.gson.annotations.SerializedName

data class UpdatePrivacyRequestDto(
    @SerializedName("private") val privateProfile: Boolean
)
