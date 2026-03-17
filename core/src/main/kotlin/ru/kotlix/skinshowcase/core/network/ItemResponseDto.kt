package ru.kotlix.skinshowcase.core.network

import com.google.gson.annotations.SerializedName

/**
 * Ответ api-gateway → items: один предмет (itemId, name, minPriceUsd, updatedAt).
 */
data class ItemResponseDto(
    @SerializedName("itemId") val itemId: String,
    @SerializedName("name") val name: String,
    @SerializedName("minPriceUsd") val minPriceUsd: Double? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)
