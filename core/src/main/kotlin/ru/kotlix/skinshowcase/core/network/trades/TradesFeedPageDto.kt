package ru.kotlix.skinshowcase.core.network.trades

import com.google.gson.annotations.SerializedName

/** Spring Data Page для GET /api/v1/trades/feed (нужны только content и пагинация). */
data class TradesFeedPageDto(
    @SerializedName("content") val content: List<TradeSelectionDto> = emptyList(),
    @SerializedName("totalElements") val totalElements: Long? = null,
    @SerializedName("totalPages") val totalPages: Int? = null,
    @SerializedName("number") val number: Int? = null,
    @SerializedName("size") val size: Int? = null
)
