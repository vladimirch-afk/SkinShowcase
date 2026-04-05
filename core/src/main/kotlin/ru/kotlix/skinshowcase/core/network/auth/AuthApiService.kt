package ru.kotlix.skinshowcase.core.network.auth

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Auth-сервис через **api-gateway**: gateway отдаёт префикс `/auth/` на сервис auth.
 * Все ручки, кроме Steam OpenID (`GET auth/steam`, `GET auth/steam/callback`), ожидают `Authorization: Bearer …`.
 *
 * PATCH `auth/me/privacy` на сервере **переключает** флаг приватности (тело не используется).
 */
interface AuthApiService {

    @GET("auth/me")
    suspend fun getMe(): MeResponseDto

    @GET("auth/avatars")
    suspend fun getAvatarPresets(): List<AvatarPresetDto>

    @PATCH("auth/me/avatar")
    suspend fun patchMyAvatar(@Body body: PatchMyAvatarRequestDto): MeResponseDto

    @GET("auth/me/privacy")
    suspend fun getPrivacy(): PrivacyResponseDto

    @PATCH("auth/me/privacy")
    suspend fun patchPrivacy(): Unit

    @PATCH("auth/me/last-online")
    suspend fun patchLastOnline(): Unit

    @GET("auth/users/by-username/{username}")
    suspend fun getSteamIdByUsername(@Path("username") username: String): UserSteamIdResponseDto

    @PATCH("auth/me/trade-link")
    suspend fun patchTradeLink(@Body body: UpdateTradeLinkRequestDto): MeResponseDto

    @PATCH("auth/me/display-name")
    suspend fun patchDisplayName(@Body body: UpdateDisplayNameRequestDto): MeResponseDto

    @GET("auth/documents")
    suspend fun getLegalDocuments(): List<LegalDocumentSummaryDto>

    @GET("auth/documents/{slug}")
    suspend fun getLegalDocument(@Path("slug") slug: String): LegalDocumentContentDto

    @POST("auth/users/{steamId}/report")
    suspend fun reportUser(
        @Path("steamId") steamId: String,
        @Body body: ReportUserRequestDto
    )

    @GET("auth/users/{steamId}/trade-link")
    suspend fun getUserTradeLink(@Path("steamId") steamId: String): UserTradeLinkResponseDto
}
