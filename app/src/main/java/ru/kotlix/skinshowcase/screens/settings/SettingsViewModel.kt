package ru.kotlix.skinshowcase.screens.settings

import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.settings.PrivacyPreferences

data class SettingsUiState(
    val darkTheme: Boolean = false,
    val showProfile: Boolean = true,
    val showOffers: Boolean = true
)

class SettingsViewModel : BaseViewModel<SettingsUiState>() {

    override fun initialState(): SettingsUiState = SettingsUiState(
        showProfile = PrivacyPreferences.getShowProfile(),
        showOffers = PrivacyPreferences.getShowOffers()
    )

    fun setShowProfile(show: Boolean) {
        PrivacyPreferences.setShowProfile(show)
        updateState { it.copy(showProfile = show) }
    }

    fun setShowOffers(show: Boolean) {
        PrivacyPreferences.setShowOffers(show)
        updateState { it.copy(showOffers = show) }
    }
}
