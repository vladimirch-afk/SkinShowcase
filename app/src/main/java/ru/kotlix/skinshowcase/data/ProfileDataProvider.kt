package ru.kotlix.skinshowcase.data

import kotlinx.coroutines.delay
import ru.kotlix.skinshowcase.mock.MockDealSummary
import ru.kotlix.skinshowcase.mock.MockOfferSummary
import ru.kotlix.skinshowcase.mock.MockServer
import ru.kotlix.skinshowcase.screens.profile.DealSummary
import ru.kotlix.skinshowcase.screens.profile.OfferSummary
import ru.kotlix.skinshowcase.screens.profile.ProfileUiState
import ru.kotlix.skinshowcase.screens.profile.SellerInfo
import ru.kotlix.skinshowcase.settings.PrivacyPreferences
import ru.kotlix.skinshowcase.settings.TradeLinkPreferences

/**
 * Единый источник данных профиля и офферов.
 * В режиме мок-сервера отдаёт [MockServer], иначе — пустые/локальные данные.
 */
object ProfileDataProvider {

    private const val MOCK_DELAY_MS = 200L

    @Volatile
    private var offersNeedRefresh = false

    fun markOffersNeedRefresh() {
        offersNeedRefresh = true
    }

    fun consumeOffersNeedRefresh(): Boolean = offersNeedRefresh.also { offersNeedRefresh = false }

    suspend fun getProfileState(): ProfileUiState {
        val tradeLink = TradeLinkPreferences.getTradeLink()
        if (!MockServer.isEnabled()) {
            return ProfileUiState(
                steamNickname = "",
                steamAvatarUrl = null,
                steamId = null,
                tradeLink = tradeLink,
                activeOffers = emptyList(),
                dealHistory = emptyList(),
                showProfile = PrivacyPreferences.getShowProfile(),
                showOffers = PrivacyPreferences.getShowOffers()
            )
        }
        delay(MOCK_DELAY_MS)
        val mock = MockServer.getProfileData()
        return ProfileUiState(
            steamNickname = mock.steamNickname,
            steamAvatarUrl = mock.steamAvatarUrl,
            steamId = mock.steamId,
            tradeLink = tradeLink,
            activeOffers = mock.activeOffers.map(::toOfferSummary),
            dealHistory = mock.dealHistory.map(::toDealSummary),
            showProfile = PrivacyPreferences.getShowProfile(),
            showOffers = PrivacyPreferences.getShowOffers()
        )
    }

    suspend fun getOffers(): List<OfferSummary> {
        if (!MockServer.isEnabled()) return emptyList()
        delay(MOCK_DELAY_MS)
        return MockServer.getOffers().map(::toOfferSummary)
    }

    /** Создаёт оффер по skinId. В моке добавляет оффер в список. Возвращает true при успехе. */
    suspend fun createOffer(skinId: String): Boolean {
        if (!MockServer.isEnabled()) return false
        delay(MOCK_DELAY_MS)
        return MockServer.createOffer(skinId) != null
    }

    /** Удаляет оффер по id. В моке удаляет из списка. Возвращает true при успехе. */
    suspend fun deleteOffer(id: String): Boolean {
        if (!MockServer.isEnabled()) return false
        delay(MOCK_DELAY_MS)
        return MockServer.deleteOffer(id)
    }

    private fun toOfferSummary(m: MockOfferSummary): OfferSummary =
        OfferSummary(m.id, m.skinId, m.skinName, m.skinImageUrl, m.priceRub)

    private fun toDealSummary(m: MockDealSummary): DealSummary =
        DealSummary(m.id, m.summary, m.counterpartName)

    /** Данные продавца для чужого оффера по skinId. В не-мок режиме возвращает null. */
    suspend fun getSellerForSkin(skinId: String): SellerInfo? {
        if (!MockServer.isEnabled()) return null
        delay(MOCK_DELAY_MS)
        val mock = MockServer.getSellerForSkin(skinId)
        return SellerInfo(
            nickname = mock.nickname,
            steamId = mock.steamId,
            tradeLink = mock.tradeLink
        )
    }
}
