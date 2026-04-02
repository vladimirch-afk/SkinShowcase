package ru.kotlix.skinshowcase.core.network.inventory

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface InventoryApiService {

    @GET("api/v1/inventory/{steamId}")
    suspend fun getInventory(
        @Path("steamId") steamId: String,
        @Query("appId") appId: Int = 730,
        @Query("contextId") contextId: Int = 2
    ): InventoryResponseDto
}
