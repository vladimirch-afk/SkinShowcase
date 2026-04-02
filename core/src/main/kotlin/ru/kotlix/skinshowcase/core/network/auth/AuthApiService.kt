package ru.kotlix.skinshowcase.core.network.auth

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

/**
 * API авторизации (api-gateway → auth). Все ручки требуют Bearer (OpenAPI security: bearerAuth).
 */
interface AuthApiService {

    @GET("auth/me")
    suspend fun getMe(): MeResponseDto

    @GET("auth/me/privacy")
    suspend fun getPrivacy(): PrivacyResponseDto

    @PATCH("auth/me/privacy")
    suspend fun patchPrivacy(@Body body: UpdatePrivacyRequestDto): Unit

    @PATCH("auth/me/last-online")
    suspend fun patchLastOnline(): Unit

    @GET("auth/users/by-username/{username}")
    suspend fun getSteamIdByUsername(@Path("username") username: String): UserSteamIdResponseDto

    @PATCH("auth/me/trade-link")
    suspend fun patchTradeLink(@Body body: UpdateTradeLinkRequestDto): MeResponseDto

    @GET("auth/users/{steamId}/trade-link")
    suspend fun getUserTradeLink(@Path("steamId") steamId: String): UserTradeLinkResponseDto
}
