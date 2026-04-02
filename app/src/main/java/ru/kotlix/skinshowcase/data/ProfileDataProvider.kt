package ru.kotlix.skinshowcase.data

import ru.kotlix.skinshowcase.core.network.RetrofitProvider
import ru.kotlix.skinshowcase.core.network.auth.AuthApiService
import ru.kotlix.skinshowcase.core.network.auth.CurrentUser
import ru.kotlix.skinshowcase.screens.profile.OfferSummary
import ru.kotlix.skinshowcase.screens.profile.ProfileUiState
import ru.kotlix.skinshowcase.screens.profile.SellerInfo
import ru.kotlix.skinshowcase.settings.PrivacyPreferences
import ru.kotlix.skinshowcase.settings.TradeLinkPreferences

/**
 * Единый источник данных профиля и офферов.
 * Профиль: GET auth/me через api-gateway (при наличии Bearer). Офферы/сделки/продавец — с бэкенда пока нет ручек.
 */
object ProfileDataProvider {

    @Volatile
    private var offersNeedRefresh = false

    fun markOffersNeedRefresh() {
        offersNeedRefresh = true
    }

    fun consumeOffersNeedRefresh(): Boolean = offersNeedRefresh.also { offersNeedRefresh = false }

    suspend fun getProfileState(): ProfileUiState {
        val tradeLinkPref = TradeLinkPreferences.getTradeLink()
        val me = runCatching {
            RetrofitProvider.create(AuthApiService::class.java).getMe()
        }.getOrNull()
        me?.steamId?.trim()?.takeIf { it.length == 17 }?.let { CurrentUser.steamId = it }
        val serverLink = me?.steamTradeLink?.trim()?.takeIf { it.isNotEmpty() }
        if (serverLink != null) {
            TradeLinkPreferences.setTradeLink(serverLink)
        }
        val tradeLink = serverLink ?: tradeLinkPref
        return ProfileUiState(
            steamNickname = "",
            steamAvatarUrl = null,
            steamId = me?.steamId,
            tradeLink = tradeLink,
            activeOffers = emptyList(),
            dealHistory = emptyList(),
            showProfile = PrivacyPreferences.getShowProfile(),
            showOffers = PrivacyPreferences.getShowOffers()
        )
    }

    suspend fun getTradeLinkForSteamId(steamId: String): String? {
        val trimmed = steamId.trim()
        if (trimmed.length != 17) return null
        return runCatching {
            RetrofitProvider.create(AuthApiService::class.java).getUserTradeLink(trimmed).tradeUrl?.trim()?.takeIf { it.isNotEmpty() }
        }.getOrNull()
    }

    suspend fun getOffers(): List<OfferSummary> = emptyList()

    suspend fun createOffer(skinId: String): Boolean = false

    suspend fun deleteOffer(id: String): Boolean = false

    suspend fun getSellerForSkin(skinId: String): SellerInfo? = null
}
