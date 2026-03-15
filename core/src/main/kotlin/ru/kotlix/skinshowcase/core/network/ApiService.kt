package ru.kotlix.skinshowcase.core.network

import retrofit2.http.GET

/**
 * Placeholder REST API for skins.
 * Replace with real endpoints (e.g. Steam/CS skins).
 */
interface ApiService {

    @GET("skins")
    suspend fun getSkins(): List<SkinDto>
}
