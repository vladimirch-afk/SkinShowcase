package ru.kotlix.skinshowcase.screens.skindetail

import ru.kotlix.skinshowcase.core.domain.Skin

data class SkinDetailUiState(
    val skin: Skin? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val sellerNickname: String? = null,
    val sellerSteamId: String? = null,
    val sellerTradeLink: String? = null,
    val isCreatingOffer: Boolean = false,
    val navigateToMyOffers: Boolean = false,
    val isSubmittingOffer: Boolean = false,
    /** Карточка открыта из ленты трейдов — без избранного, продавец по steamId из фида. */
    val isTradeFeedOffer: Boolean = false
)
