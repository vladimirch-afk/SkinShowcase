package ru.kotlix.skinshowcase.core.network.inventory

import com.google.gson.annotations.SerializedName

data class InventoryResponseDto(
    @SerializedName("steamId") val steamId: String? = null,
    @SerializedName("appId") val appId: Int? = null,
    @SerializedName("contextId") val contextId: Int? = null,
    @SerializedName("items") val items: List<InventoryItemDto>? = null
)
