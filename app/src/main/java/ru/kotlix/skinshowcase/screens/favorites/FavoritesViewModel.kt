package ru.kotlix.skinshowcase.screens.favorites

import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.network.SkinsProvider

class FavoritesViewModel : BaseViewModel<FavoritesUiState>() {

    override fun initialState(): FavoritesUiState = FavoritesUiState(isLoading = true)

    init {
        launch {
            SkinsProvider.repository.observeFavorites().collect { list ->
                updateState { it.copy(skins = list, isLoading = false) }
            }
        }
    }

    fun toggleFavorite(skin: Skin) {
        launch {
            val repo = SkinsProvider.repository
            if (skin.isFavorite) repo.removeFromFavorites(skin.id)
            else repo.addToFavorites(skin)
            // State will update via observeFavorites() flow
        }
    }
}
