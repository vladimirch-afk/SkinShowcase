package ru.kotlix.skinshowcase.core.network.inventory

import com.google.gson.annotations.SerializedName

data class InventoryItemDto(
    @SerializedName("assetId") val assetId: String? = null,
    @SerializedName("classId") val classId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("marketHashName") val marketHashName: String? = null,
    @SerializedName("iconUrl") val iconUrl: String? = null,
    @SerializedName("floatValue") val floatValue: Double? = null,
    @SerializedName("wearName") val wearName: String? = null
)
