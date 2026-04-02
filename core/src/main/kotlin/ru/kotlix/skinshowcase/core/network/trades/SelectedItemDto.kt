package ru.kotlix.skinshowcase.core.network.trades

import com.google.gson.annotations.SerializedName

data class SelectedItemDto(
    @SerializedName("assetId") val assetId: String? = null,
    @SerializedName("classId") val classId: String? = null
)
