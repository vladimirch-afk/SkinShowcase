package ru.kotlix.skinshowcase.screens.offers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.core.network.bestApiMessage
import ru.kotlix.skinshowcase.core.network.auth.AvatarUrls
import ru.kotlix.skinshowcase.data.ProfileDataProvider
import ru.kotlix.skinshowcase.screens.profile.OfferSummary

class OffersViewModel : BaseViewModel<OffersUiState>() {

    override fun initialState(): OffersUiState = OffersUiState(offers = emptyList())

    fun removeOffer(offer: OfferSummary) {
        launch {
            val deleted = ProfileDataProvider.deleteOffer(offer)
            if (deleted) refreshOffers()
        }
    }

    fun refreshOffers() {
        launch {
            updateState { it.copy(isRefreshing = true, loadError = null) }
            try {
                withContext(Dispatchers.IO) {
                    ProfileDataProvider.syncCurrentUserFromAuthMe()
                }
                val offers = ProfileDataProvider.getOffers()
                val avatarUrl = AvatarUrls.currentUserAvatarDisplayUrl()
                updateState {
                    it.copy(
                        offers = offers,
                        isRefreshing = false,
                        userAvatarUrl = avatarUrl,
                        loadError = null
                    )
                }
            } catch (e: Exception) {
                val avatarUrl = AvatarUrls.currentUserAvatarDisplayUrl()
                updateState {
                    it.copy(
                        isRefreshing = false,
                        userAvatarUrl = avatarUrl,
                        loadError = e.bestApiMessage()
                    )
                }
            }
        }
    }

    fun clearLoadError() {
        updateState { it.copy(loadError = null) }
    }

    fun refreshUserAvatarFromCurrentUser() {
        updateState { it.copy(userAvatarUrl = AvatarUrls.currentUserAvatarDisplayUrl()) }
    }

    fun clearRefreshing() {
        updateState { it.copy(isRefreshing = false) }
    }

    // --- данные-заглушки (отключены) ---
    // private fun sampleOffers(): List<OfferSummary> = listOf(...)
}
