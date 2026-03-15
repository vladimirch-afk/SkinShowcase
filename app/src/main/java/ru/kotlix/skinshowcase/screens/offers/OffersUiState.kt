package ru.kotlix.skinshowcase.screens.offers

import ru.kotlix.skinshowcase.screens.profile.OfferSummary

data class OffersUiState(
    val offers: List<OfferSummary> = emptyList(),
    val isRefreshing: Boolean = false
)
