package ru.kotlix.skinshowcase.screens.profile

data class ProfileUiState(
    val steamAvatarUrl: String? = null,
    val steamNickname: String = "",
    val activeOffers: List<OfferSummary> = emptyList(),
    val dealHistory: List<DealSummary> = emptyList(),
    val showProfile: Boolean = true,
    val showOffers: Boolean = true
) {
    val firstOffer: OfferSummary? get() = activeOffers.firstOrNull()
    val firstDeal: DealSummary? get() = dealHistory.firstOrNull()
    val hasActiveOffers: Boolean get() = activeOffers.isNotEmpty()
    val hasDeals: Boolean get() = dealHistory.isNotEmpty()
}
