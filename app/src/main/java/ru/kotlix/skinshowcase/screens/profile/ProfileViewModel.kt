package ru.kotlix.skinshowcase.screens.profile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.core.network.RetrofitProvider
import ru.kotlix.skinshowcase.core.network.auth.AuthApiService
import ru.kotlix.skinshowcase.core.network.auth.AvatarUrls
import ru.kotlix.skinshowcase.core.network.auth.CurrentUser
import ru.kotlix.skinshowcase.core.network.auth.PatchMyAvatarRequestDto
import ru.kotlix.skinshowcase.core.network.bestApiMessage
import ru.kotlix.skinshowcase.core.network.auth.UpdateDisplayNameRequestDto
import ru.kotlix.skinshowcase.core.network.auth.resolveAvatarDisplayUrl
import ru.kotlix.skinshowcase.data.ProfileDataProvider
import ru.kotlix.skinshowcase.settings.PrivacyPreferences

class ProfileViewModel : BaseViewModel<ProfileUiState>() {

    override fun initialState(): ProfileUiState = ProfileUiState(
        steamNickname = "",
        steamAvatarUrl = AvatarUrls.userAvatarUrl(null),
        steamId = null,
        activeOffers = emptyList(),
        dealHistory = emptyList(),
        showProfile = PrivacyPreferences.getShowProfile()
    )

    fun refreshPrivacy() {
        updateState {
            it.copy(showProfile = PrivacyPreferences.getShowProfile())
        }
    }

    fun refreshProfile() {
        launch {
            updateState { it.copy(isRefreshing = true, refreshError = null) }
            try {
                refreshPrivacy()
                val profileState = ProfileDataProvider.getProfileState()
                val auth = RetrofitProvider.create(AuthApiService::class.java)
                val docs = withContext(Dispatchers.IO) {
                    runCatching { auth.getLegalDocuments() }.getOrElse { emptyList() }
                }
                val presets = withContext(Dispatchers.IO) {
                    runCatching { auth.getAvatarPresets() }.getOrElse { emptyList() }
                }
                updateState {
                    profileState.copy(
                        isRefreshing = false,
                        legalDocumentsFromApi = docs,
                        avatarPresets = presets
                    )
                }
            } catch (e: Exception) {
                updateState { it.copy(isRefreshing = false, refreshError = e.bestApiMessage()) }
            }
        }
    }

    fun clearRefreshError() {
        updateState { it.copy(refreshError = null) }
    }

    fun updateDisplayName(name: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        launch {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) {
                onError("Введите имя")
                return@launch
            }
            runCatching {
                withContext(Dispatchers.IO) {
                    RetrofitProvider.create(AuthApiService::class.java)
                        .patchDisplayName(UpdateDisplayNameRequestDto(trimmed))
                }
            }.fold(
                onSuccess = { me ->
                    updateState {
                        it.copy(steamNickname = me.displayName?.trim().orEmpty())
                    }
                    onSuccess()
                },
                onFailure = { e -> onError(e.bestApiMessage()) }
            )
        }
    }

    fun clearRefreshing() {
        updateState { it.copy(isRefreshing = false) }
    }

    fun selectAvatarPreset(presetId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        launch {
            val num = presetId.trim().toIntOrNull()
            if (num == null || num !in 1..8) {
                onError("Некорректный пресет")
                return@launch
            }
            runCatching {
                withContext(Dispatchers.IO) {
                    RetrofitProvider.create(AuthApiService::class.java)
                        .patchMyAvatar(PatchMyAvatarRequestDto.preset(num))
                }
            }.fold(
                onSuccess = { me ->
                    ProfileDataProvider.applyMeAvatarToCurrentUser(me)
                    val fromServer = me.presetAvatarId?.coerceIn(1, 8)
                        ?: me.avatarPresetId?.trim()?.toIntOrNull()?.coerceIn(1, 8)
                    val resolved = fromServer ?: num
                    val presetStr = resolved.toString()
                    CurrentUser.avatarSource = "PRESET"
                    CurrentUser.avatarPresetId = presetStr
                    CurrentUser.steamAvatarUrl = null
                    updateState {
                        it.copy(
                            avatarSource = "PRESET",
                            avatarPresetId = presetStr,
                            steamAvatarUrl = AvatarUrls.presetImageUrl(presetStr),
                            steamProfileImageUrl = me.steamAvatarUrl?.trim()?.takeIf { s -> s.isNotEmpty() }
                        )
                    }
                    onSuccess()
                },
                onFailure = { e -> onError(e.bestApiMessage()) }
            )
        }
    }

    fun selectSteamAvatar(onSuccess: () -> Unit, onError: (String) -> Unit) {
        launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    RetrofitProvider.create(AuthApiService::class.java)
                        .patchMyAvatar(PatchMyAvatarRequestDto.steam())
                }
            }.fold(
                onSuccess = { me ->
                    ProfileDataProvider.applyMeAvatarToCurrentUser(me)
                    val steamU = me.steamAvatarUrl?.trim()?.takeIf { it.isNotEmpty() }
                    CurrentUser.avatarSource = "STEAM"
                    CurrentUser.avatarPresetId = null
                    CurrentUser.steamAvatarUrl = steamU
                    val displayUrl = steamU ?: me.resolveAvatarDisplayUrl()
                    updateState {
                        it.copy(
                            avatarSource = "STEAM",
                            avatarPresetId = null,
                            steamAvatarUrl = displayUrl,
                            steamProfileImageUrl = steamU
                        )
                    }
                    onSuccess()
                },
                onFailure = { e -> onError(e.bestApiMessage()) }
            )
        }
    }

    // --- данные-заглушки (отключены, данные через api-gateway) ---
    // private fun sampleOffers(): List<OfferSummary> = listOf(...)
    // private fun sampleDeals(): List<DealSummary> = listOf(...)
}
