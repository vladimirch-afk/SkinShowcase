package ru.kotlix.skinshowcase.screens.skins

import ru.kotlix.skinshowcase.core.domain.Skin

data class SkinsUiState(
    val skins: List<Skin> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)
