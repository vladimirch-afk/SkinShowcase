package ru.kotlix.skinshowcase.screens.profile

import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.settings.PrivacyPreferences

class ProfileViewModel : BaseViewModel<ProfileUiState>() {

    override fun initialState(): ProfileUiState = ProfileUiState(
        steamNickname = "SteamUser",
        activeOffers = sampleOffers(),
        dealHistory = sampleDeals(),
        showProfile = PrivacyPreferences.getShowProfile(),
        showOffers = PrivacyPreferences.getShowOffers()
    )

    fun refreshPrivacy() {
        updateState {
            it.copy(
                showProfile = PrivacyPreferences.getShowProfile(),
                showOffers = PrivacyPreferences.getShowOffers()
            )
        }
    }

    private fun sampleOffers(): List<OfferSummary> = listOf(
        OfferSummary(
            id = "1",
            skinName = "АК-47 | Красная линия",
            skinImageUrl = null,
            priceRub = 40_000.0
        ),
        OfferSummary(
            id = "2",
            skinName = "AWP | Dragon Lore",
            skinImageUrl = null,
            priceRub = 850_000.0
        ),
        OfferSummary(
            id = "3",
            skinName = "M4A4 | Кактус",
            skinImageUrl = null,
            priceRub = 12_500.0
        )
    )

    private fun sampleDeals(): List<DealSummary> = listOf(
        DealSummary(
            id = "1",
            summary = "АК-47 | Красная линия ↔ 12 500 ₽",
            counterpartName = "SteamUser_42"
        ),
        DealSummary(
            id = "2",
            summary = "AWP | Dragon Lore → продажа",
            counterpartName = "Trader_Pro"
        ),
        DealSummary(
            id = "3",
            summary = "M4A4 | Кактус ↔ обмен на AWP",
            counterpartName = "SkinHunter"
        )
    )
}
