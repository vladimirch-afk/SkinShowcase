package ru.kotlix.skinshowcase.core.usecase

import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.repository.SkinsRepository

/**
 * Use case: load skins from API (и кэш Room при недоступности сети).
 */
class GetSkinsUseCase(
    private val repository: SkinsRepository
) {

    suspend fun execute(): List<Skin> = repository.getSkinsFromApi()
}
