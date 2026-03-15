package ru.kotlix.skinshowcase.screens.home

import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.domain.SkinFilter

data class HomeUiState(
    val searchQuery: String = "",
    val skins: List<Skin> = emptyList(),
    val isLoading: Boolean = false,
    val filter: SkinFilter = SkinFilter(),
    val filterSheetVisible: Boolean = false,
    val sortOption: SortOption = SortOption.DEFAULT
)
