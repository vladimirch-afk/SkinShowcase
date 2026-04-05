package ru.kotlix.skinshowcase.screens.offers

import ru.kotlix.skinshowcase.core.network.auth.AvatarUrls
import ru.kotlix.skinshowcase.screens.profile.OfferSummary

data class OffersUiState(
    val offers: List<OfferSummary> = emptyList(),
    val isRefreshing: Boolean = false,
    val userAvatarUrl: String = AvatarUrls.currentUserAvatarDisplayUrl(),
    val loadError: String? = null
)
