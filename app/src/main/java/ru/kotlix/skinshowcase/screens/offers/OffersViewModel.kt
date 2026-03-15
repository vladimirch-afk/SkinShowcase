package ru.kotlix.skinshowcase.screens.offers

import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.data.ProfileDataProvider
import ru.kotlix.skinshowcase.screens.profile.OfferSummary

class OffersViewModel : BaseViewModel<OffersUiState>() {

    override fun initialState(): OffersUiState = OffersUiState(offers = emptyList())

    fun removeOffer(id: String) {
        launch {
            val deleted = ProfileDataProvider.deleteOffer(id)
            if (deleted) refreshOffers()
            else updateState { it.copy(offers = it.offers.filter { o -> o.id != id }) }
        }
    }

    fun refreshOffers() {
        launch {
            updateState { it.copy(isRefreshing = true) }
            try {
                val offers = ProfileDataProvider.getOffers()
                updateState { it.copy(offers = offers, isRefreshing = false) }
            } catch (_: Exception) {
                updateState { it.copy(isRefreshing = false) }
            }
        }
    }

    fun clearRefreshing() {
        updateState { it.copy(isRefreshing = false) }
    }

    // --- данные-заглушки (отключены) ---
    // private fun sampleOffers(): List<OfferSummary> = listOf(...)
}
