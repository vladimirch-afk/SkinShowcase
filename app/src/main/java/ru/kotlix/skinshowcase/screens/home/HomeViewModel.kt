package ru.kotlix.skinshowcase.screens.home

import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.domain.SkinFilter

class HomeViewModel : BaseViewModel<HomeUiState>() {

    override fun initialState(): HomeUiState = HomeUiState(
        skins = defaultSkins()
    )

    fun updateSearch(query: String) {
        updateState { it.copy(searchQuery = query) }
    }

    fun openFilterSheet() {
        updateState { it.copy(filterSheetVisible = true) }
    }

    fun dismissFilterSheet() {
        updateState { it.copy(filterSheetVisible = false) }
    }

    fun applyFilter(filter: SkinFilter) {
        updateState {
            it.copy(filter = filter, filterSheetVisible = false)
        }
    }

    fun setSortOption(option: SortOption) {
        updateState { it.copy(sortOption = option) }
    }

    private fun defaultSkins(): List<Skin> = listOf(
        Skin(
            id = "ak47-redline",
            name = "АК-47 | Красная линия",
            imageUrl = null,
            price = 40_000.0
        ),
        Skin(
            id = "butterfly-gradient",
            name = "Butterfly Knife | Градиент",
            imageUrl = null,
            price = 100_000.0
        ),
        Skin(
            id = "awp-lighting",
            name = "AWP | Молния",
            imageUrl = null,
            price = 55_000.0
        )
    )
}
