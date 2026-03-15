package ru.kotlix.skinshowcase.screens.profile

import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.settings.PrivacyPreferences

class ProfileViewModel : BaseViewModel<ProfileUiState>() {

    override fun initialState(): ProfileUiState = ProfileUiState(
        steamNickname = "",
        activeOffers = emptyList(),
        dealHistory = emptyList(),
        showProfile = PrivacyPreferences.getShowProfile(),
        showOffers = PrivacyPreferences.getShowOffers()
        // данные-заглушки отключены — профиль/офферы/сделки через api-gateway (TODO: эндпоинты)
    )

    fun refreshPrivacy() {
        updateState {
            it.copy(
                showProfile = PrivacyPreferences.getShowProfile(),
                showOffers = PrivacyPreferences.getShowOffers()
            )
        }
    }

    fun refreshProfile() {
        launch {
            updateState { it.copy(isRefreshing = true) }
            try {
                refreshPrivacy()
                kotlinx.coroutines.delay(300)
            } finally {
                updateState { it.copy(isRefreshing = false) }
            }
        }
    }

    fun clearRefreshing() {
        updateState { it.copy(isRefreshing = false) }
    }

    // --- данные-заглушки (отключены, данные через api-gateway) ---
    // private fun sampleOffers(): List<OfferSummary> = listOf(...)
    // private fun sampleDeals(): List<DealSummary> = listOf(...)
}
