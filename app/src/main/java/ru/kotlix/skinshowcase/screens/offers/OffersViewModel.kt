package ru.kotlix.skinshowcase.screens.offers

import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.screens.profile.OfferSummary

class OffersViewModel : BaseViewModel<OffersUiState>() {

    override fun initialState(): OffersUiState = OffersUiState(offers = emptyList())
    // данные-заглушки отключены — офферы через api-gateway (TODO: эндпоинт)

    fun removeOffer(id: String) {
        updateState { it.copy(offers = it.offers.filter { o -> o.id != id }) }
    }

    fun refreshOffers() {
        launch {
            updateState { it.copy(isRefreshing = true) }
            try {
                kotlinx.coroutines.delay(400)
                // TODO: загрузка офферов с api-gateway
            } finally {
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
