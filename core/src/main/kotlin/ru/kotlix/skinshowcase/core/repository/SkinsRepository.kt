package ru.kotlix.skinshowcase.core.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import ru.kotlix.skinshowcase.core.database.dao.FavoriteSkinDao
import ru.kotlix.skinshowcase.core.database.dao.SkinCacheDao
import ru.kotlix.skinshowcase.core.database.mapper.toCachedSkinEntity
import ru.kotlix.skinshowcase.core.database.mapper.toSkinDto
import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.domain.mapper.toDomain
import ru.kotlix.skinshowcase.core.domain.mapper.toFavoriteEntity
import ru.kotlix.skinshowcase.core.network.ApiService
import ru.kotlix.skinshowcase.core.domain.mapper.toDomain as dtoToDomain

/**
 * Single source of truth: remote API + local cache (Room) + favorites (Room).
 */
class SkinsRepository(
    private val api: ApiService,
    private val favoriteDao: FavoriteSkinDao,
    private val skinCacheDao: SkinCacheDao
) {

    fun observeFavorites(): Flow<List<Skin>> =
        favoriteDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getSkinsFromApi(): List<Skin> {
        val favoriteIds = favoriteDao.getAllIds().toSet()
        val remote = runCatching { api.getSkins() }
        return when {
            remote.isSuccess -> {
                val list = remote.getOrThrow()
                skinCacheDao.deleteAll()
                skinCacheDao.insertAll(list.mapIndexed { index, dto -> dto.toCachedSkinEntity(orderIndex = index) })
                list.map { dto -> dto.dtoToDomain(isFavorite = dto.id in favoriteIds) }
            }
            else -> {
                val cached = skinCacheDao.getAll()
                cached.map { it.toSkinDto().dtoToDomain(isFavorite = it.id in favoriteIds) }
            }
        }
    }

    suspend fun getSkinByIdFromApi(id: String): Skin? {
        val isFav = favoriteDao.getById(id) != null
        val fromApi = runCatching { api.getSkinById(id) }
        return when {
            fromApi.isSuccess -> {
                val dto = fromApi.getOrThrow()
                skinCacheDao.insert(dto.toCachedSkinEntity(orderIndex = 0))
                dto.dtoToDomain(isFavorite = isFav)
            }
            else -> {
                val cached = skinCacheDao.getById(id) ?: return null
                cached.toSkinDto().dtoToDomain(isFavorite = isFav)
            }
        }
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
