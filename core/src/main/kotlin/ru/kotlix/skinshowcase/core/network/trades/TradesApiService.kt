package ru.kotlix.skinshowcase.core.network.trades

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TradesApiService {

    @GET("api/v1/trades/feed")
    suspend fun getFeed(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
        @Query("excludeSteamId") excludeSteamId: String? = null
    ): TradesFeedPageDto

    /** Текущий набор предметов пользователя для обмена. */
    @GET("api/v1/trades/selection/{steamId}")
    suspend fun getTradeSelection(@Path("steamId") steamId: String): TradeSelectionDto

    /** Создать или полностью заменить набор (клиент при добавлении оффера мержит список и шлёт итог). */
    @PUT("api/v1/trades/selection/{steamId}")
    suspend fun upsertTradeSelection(
        @Path("steamId") steamId: String,
        @Body body: CreateTradeSelectionRequestDto
    ): TradeSelectionDto

    /** Удалить указанные предметы из набора. */
    @HTTP(method = "DELETE", path = "api/v1/trades/selection/{steamId}/items", hasBody = true)
    suspend fun removeTradeSelectionItems(
        @Path("steamId") steamId: String,
        @Body body: CreateTradeSelectionRequestDto
    )
}
