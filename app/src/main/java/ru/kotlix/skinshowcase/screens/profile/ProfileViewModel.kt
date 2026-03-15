package ru.kotlix.skinshowcase.screens.profile

import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.data.ProfileDataProvider
import ru.kotlix.skinshowcase.settings.PrivacyPreferences

class ProfileViewModel : BaseViewModel<ProfileUiState>() {

    override fun initialState(): ProfileUiState = ProfileUiState(
        steamNickname = "",
        steamAvatarUrl = null,
        steamId = null,
        activeOffers = emptyList(),
        dealHistory = emptyList(),
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

    fun refreshProfile() {
        launch {
            updateState { it.copy(isRefreshing = true) }
            try {
                refreshPrivacy()
                val profileState = ProfileDataProvider.getProfileState()
                updateState { profileState.copy(isRefreshing = false) }
            } catch (_: Exception) {
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
