package ru.kotlix.skinshowcase.core.network.trades

import retrofit2.http.GET
import retrofit2.http.Query

interface TradesApiService {

    @GET("api/v1/trades/feed")
    suspend fun getFeed(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
        @Query("excludeSteamId") excludeSteamId: String? = null
    ): TradesFeedPageDto
}
