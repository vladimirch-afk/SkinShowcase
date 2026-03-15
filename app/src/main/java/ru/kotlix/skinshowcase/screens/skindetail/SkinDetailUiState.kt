package ru.kotlix.skinshowcase.screens.skindetail

import ru.kotlix.skinshowcase.core.domain.Skin

data class SkinDetailUiState(
    val skin: Skin? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
