package ru.kotlix.skinshowcase.core.network.inventory

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Инвентарь: **api-gateway**, префикс пути `/api/v1/inventory/` → steam-gateway. */
interface InventoryApiService {

    @GET("api/v1/inventory/{steamId}")
    suspend fun getInventory(
        @Path("steamId") steamId: String,
        @Query("appId") appId: Int = 730,
        @Query("contextId") contextId: Int = 2
    ): InventoryResponseDto

    /**
     * Один предмет инвентаря с ценой из каталога items.
     * Query [assetId] и [classId] обязательны (как при сохранении набора trades).
     */
    @GET("api/v1/inventory/{steamId}/item")
    suspend fun getInventoryItemDetail(
        @Path("steamId") steamId: String,
        @Query("assetId") assetId: String,
        @Query("classId") classId: String,
        @Query("appId") appId: Int = 730,
        @Query("contextId") contextId: Int = 2
    ): InventoryItemDetailResponseDto
}
