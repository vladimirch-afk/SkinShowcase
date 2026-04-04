package ru.kotlix.skinshowcase.core.network

import retrofit2.http.GET
import retrofit2.http.Path


interface ApiService {

    @GET("api/v1/items")
    suspend fun getSkins(): List<SkinDto>

    /** Ответ items-сервиса: `itemId`, `name`, `minPriceUsd` — см. [ItemResponseDto.toSkinDto]. */
    @GET("api/v1/items/{id}")
    suspend fun getSkinById(@Path("id") id: String): ItemResponseDto
}
