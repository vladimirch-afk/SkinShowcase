package ru.kotlix.skinshowcase.screens.favorites

import ru.kotlix.skinshowcase.core.domain.Skin

data class FavoritesUiState(
    val skins: List<Skin> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
