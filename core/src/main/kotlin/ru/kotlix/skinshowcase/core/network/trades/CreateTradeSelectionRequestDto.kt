package ru.kotlix.skinshowcase.core.network.trades

import com.google.gson.annotations.SerializedName

/** Тело PUT `/api/v1/trades/selection/{steamId}` — набор предметов из инвентаря пользователя. */
data class CreateTradeSelectionRequestDto(
    @SerializedName("items") val items: List<SelectedItemDto>
)
