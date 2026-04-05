package ru.kotlix.skinshowcase.screens.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.core.network.RetrofitProvider
import ru.kotlix.skinshowcase.core.network.bestApiMessage
import ru.kotlix.skinshowcase.core.network.auth.AuthApiService
import ru.kotlix.skinshowcase.settings.PrivacyPreferences

data class SettingsUiState(
    val darkTheme: Boolean = false,
    val showProfile: Boolean = true,
    val isLoadingPrivacy: Boolean = false,
    val privacyLoadError: String? = null,
    val privacySyncError: String? = null
)

class SettingsViewModel : BaseViewModel<SettingsUiState>() {

    override fun initialState(): SettingsUiState = SettingsUiState(
        showProfile = PrivacyPreferences.getShowProfile()
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
                            privacyLoadError = e.bestApiMessage()
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
                    RetrofitProvider.create(AuthApiService::class.java).patchPrivacy()
                }
            }.fold(
                onSuccess = {
                    PrivacyPreferences.setShowProfile(show)
                    updateState { it.copy(showProfile = show) }
                },
                onFailure = { e ->
                    updateState { it.copy(privacySyncError = e.bestApiMessage()) }
                }
            )
        }
    }

    fun clearPrivacyErrors() {
        updateState { it.copy(privacyLoadError = null, privacySyncError = null) }
    }
}
