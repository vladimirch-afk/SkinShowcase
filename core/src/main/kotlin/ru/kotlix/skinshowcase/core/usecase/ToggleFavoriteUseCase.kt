package ru.kotlix.skinshowcase.core.usecase

import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.repository.SkinsRepository

/**
 * Use case: add or remove skin from favorites.
 */
class ToggleFavoriteUseCase(
    private val repository: SkinsRepository
) {

    suspend fun add(skin: Skin) {
        repository.addToFavorites(skin)
    }

    suspend fun remove(skinId: String) {
        repository.removeFromFavorites(skinId)
    }
}
