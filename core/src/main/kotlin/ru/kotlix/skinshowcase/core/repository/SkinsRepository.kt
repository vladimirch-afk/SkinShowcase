package ru.kotlix.skinshowcase.core.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.kotlix.skinshowcase.core.database.dao.FavoriteSkinDao
import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.domain.mapper.toDomain
import ru.kotlix.skinshowcase.core.domain.mapper.toFavoriteEntity
import ru.kotlix.skinshowcase.core.network.ApiService
import ru.kotlix.skinshowcase.core.domain.mapper.toDomain as dtoToDomain

/**
 * Single source of truth: remote API + local favorites (Room).
 */
class SkinsRepository(
    private val api: ApiService,
    private val favoriteDao: FavoriteSkinDao
) {

    fun observeFavorites(): Flow<List<Skin>> =
        favoriteDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getSkinsFromApi(): List<Skin> {
        val favoriteIds = favoriteDao.getAllIds().toSet()
        val remote = api.getSkins()
        return remote.map { dto -> dto.dtoToDomain(isFavorite = dto.id in favoriteIds) }
    }

    suspend fun addToFavorites(skin: Skin) {
        favoriteDao.insert(skin.toFavoriteEntity(addedAtMillis = System.currentTimeMillis()))
    }

    suspend fun removeFromFavorites(skinId: String) {
        favoriteDao.deleteById(skinId)
    }

    suspend fun isFavorite(skinId: String): Boolean =
        favoriteDao.getById(skinId) != null
}
