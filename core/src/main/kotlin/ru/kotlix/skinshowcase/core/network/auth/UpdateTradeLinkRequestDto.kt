package ru.kotlix.skinshowcase.core.network.auth

import com.google.gson.annotations.SerializedName

data class UpdateTradeLinkRequestDto(
    @SerializedName("tradeUrl") val tradeUrl: String?
)
