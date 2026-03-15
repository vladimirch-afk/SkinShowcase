package ru.kotlix.skinshowcase.core.usecase

import kotlinx.coroutines.flow.Flow
import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.repository.SkinsRepository

/**
 * Use case: observe favorite skins from local DB.
 */
class ObserveFavoritesUseCase(
    private val repository: SkinsRepository
) {

    fun execute(): Flow<List<Skin>> = repository.observeFavorites()
}
