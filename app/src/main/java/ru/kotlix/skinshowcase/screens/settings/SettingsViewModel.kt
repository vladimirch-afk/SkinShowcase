package ru.kotlix.skinshowcase.screens.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.core.network.RetrofitProvider
import ru.kotlix.skinshowcase.core.network.auth.AuthApiService
import ru.kotlix.skinshowcase.core.network.auth.UpdatePrivacyRequestDto
import ru.kotlix.skinshowcase.settings.PrivacyPreferences

data class SettingsUiState(
    val darkTheme: Boolean = false,
    val showProfile: Boolean = true,
    val showOffers: Boolean = true,
    val isLoadingPrivacy: Boolean = false,
    val privacyLoadError: String? = null,
    val privacySyncError: String? = null
)

class SettingsViewModel : BaseViewModel<SettingsUiState>() {

    override fun initialState(): SettingsUiState = SettingsUiState(
        showProfile = PrivacyPreferences.getShowProfile(),
        showOffers = PrivacyPreferences.getShowOffers()
    )

    init {
        loadPrivacyFromServer()
    }

    fun loadPrivacyFromServer() {
        launch {
            updateState { it.copy(isLoadingPrivacy = true, privacyLoadError = null) }
            runCatching {
                withContext(Dispatchers.IO) {
                    RetrofitProvider.create(AuthApiService::class.java).getPrivacy()
                }
            }.fold(
                onSuccess = { dto ->
                    val showPublic = !dto.privateProfile
                    PrivacyPreferences.setShowProfile(showPublic)
                    updateState {
                        it.copy(
                            showProfile = showPublic,
                            isLoadingPrivacy = false,
                            privacyLoadError = null
                        )
                    }
                },
                onFailure = { e ->
                    updateState {
                        it.copy(
                            isLoadingPrivacy = false,
                            privacyLoadError = e.message
                        )
                    }
                }
            )
        }
    }

    fun setShowProfile(show: Boolean) {
        launch {
            updateState { it.copy(privacySyncError = null) }
            runCatching {
                withContext(Dispatchers.IO) {
                    RetrofitProvider.create(AuthApiService::class.java).patchPrivacy(
                        UpdatePrivacyRequestDto(privateProfile = !show)
                    )
                }
            }.fold(
                onSuccess = {
                    PrivacyPreferences.setShowProfile(show)
                    updateState { it.copy(showProfile = show) }
                },
                onFailure = { e ->
                    updateState { it.copy(privacySyncError = e.message) }
                }
            )
        }
    }

    fun setShowOffers(show: Boolean) {
        PrivacyPreferences.setShowOffers(show)
        updateState { it.copy(showOffers = show) }
    }

    fun clearPrivacyErrors() {
        updateState { it.copy(privacyLoadError = null, privacySyncError = null) }
    }
}
