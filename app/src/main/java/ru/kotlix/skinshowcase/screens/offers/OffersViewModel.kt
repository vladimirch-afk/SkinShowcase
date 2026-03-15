package ru.kotlix.skinshowcase.screens.offers

import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.screens.profile.OfferSummary

class OffersViewModel : BaseViewModel<OffersUiState>() {

    override fun initialState(): OffersUiState = OffersUiState(offers = sampleOffers())

    fun removeOffer(id: String) {
        updateState { it.copy(offers = it.offers.filter { o -> o.id != id }) }
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
}
