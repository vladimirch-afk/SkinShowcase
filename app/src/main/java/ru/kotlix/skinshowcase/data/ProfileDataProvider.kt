package ru.kotlix.skinshowcase.data

import ru.kotlix.skinshowcase.core.network.RetrofitProvider
import ru.kotlix.skinshowcase.core.network.auth.AuthApiService
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
        val tradeLink = TradeLinkPreferences.getTradeLink()
        val me = runCatching {
            RetrofitProvider.create(AuthApiService::class.java).getMe()
        }.getOrNull()
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

    suspend fun getOffers(): List<OfferSummary> = emptyList()

    suspend fun createOffer(skinId: String): Boolean = false

    suspend fun deleteOffer(id: String): Boolean = false

    suspend fun getSellerForSkin(skinId: String): SellerInfo? = null
}
