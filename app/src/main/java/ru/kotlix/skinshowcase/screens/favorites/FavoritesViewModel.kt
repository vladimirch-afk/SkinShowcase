package ru.kotlix.skinshowcase.screens.favorites

import ru.kotlix.skinshowcase.core.BaseViewModel

class FavoritesViewModel : BaseViewModel<FavoritesUiState>() {

    override fun initialState(): FavoritesUiState = FavoritesUiState()
}
