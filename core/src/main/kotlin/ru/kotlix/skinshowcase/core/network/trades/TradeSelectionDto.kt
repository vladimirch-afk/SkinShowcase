package ru.kotlix.skinshowcase.core.network.trades

import com.google.gson.annotations.SerializedName

data class TradeSelectionDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("steamId") val steamId: String? = null,
    @SerializedName("items") val items: List<SelectedItemDto>? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)
