package ru.kotlix.skinshowcase.core.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.kotlix.skinshowcase.core.database.dao.FavoriteSkinDao
import ru.kotlix.skinshowcase.core.database.dao.SkinCacheDao
import ru.kotlix.skinshowcase.core.database.mapper.toCachedSkinEntity
import ru.kotlix.skinshowcase.core.database.mapper.toSkinDto
import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.domain.mapper.toDomain
import ru.kotlix.skinshowcase.core.domain.mapper.toFavoriteEntity
import ru.kotlix.skinshowcase.core.network.ApiService
import ru.kotlix.skinshowcase.core.network.toSkinDto
import ru.kotlix.skinshowcase.core.network.inventory.InventoryApiService
import ru.kotlix.skinshowcase.core.network.inventory.toSkin
import ru.kotlix.skinshowcase.core.domain.mapper.toDomain as dtoToDomain

/**
 * Single source of truth: remote API + local cache (Room) + favorites (Room).
 */
class SkinsRepository(
    private val api: ApiService,
    private val inventoryApi: InventoryApiService,
    private val favoriteDao: FavoriteSkinDao,
    private val skinCacheDao: SkinCacheDao
) {

    private companion object {
        /** Как на steam-gateway: SteamID64, 17 цифр, начинается с 765. */
        private val STEAM_ID_64 = Regex("^765\\d{14}$")
    }

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

    suspend fun getSkinByIdFromApi(
        id: String,
        inventoryOwnerSteamId: String? = null,
        inventoryAssetId: String? = null,
        offerOwnerSteamId: String? = null
    ): Skin? {
        val isFav = favoriteDao.getById(id) != null
        val fromInventory = fetchInventoryItemDetailSkin(
            classId = id,
            ownerSteamId = inventoryOwnerSteamId,
            assetId = inventoryAssetId,
            isFavorite = isFav,
            offerOwnerSteamId = offerOwnerSteamId
        )
        if (fromInventory != null) {
            skinCacheDao.insert(fromInventory.toCachedSkinEntity(orderIndex = 0))
            return fromInventory
        }
        val fromApi = runCatching { api.getSkinById(id).toSkinDto() }
        return when {
            fromApi.isSuccess -> {
                val dto = fromApi.getOrThrow()
                skinCacheDao.insert(dto.toCachedSkinEntity(orderIndex = 0))
                dto.dtoToDomain(isFavorite = isFav).withInventoryContext(inventoryAssetId, offerOwnerSteamId)
            }
            else -> {
                val cached = skinCacheDao.getById(id) ?: return null
                cached.toSkinDto().dtoToDomain(isFavorite = isFav).withInventoryContext(inventoryAssetId, offerOwnerSteamId)
            }
        }
    }

    private suspend fun fetchInventoryItemDetailSkin(
        classId: String,
        ownerSteamId: String?,
        assetId: String?,
        isFavorite: Boolean,
        offerOwnerSteamId: String?
    ): Skin? {
        val owner = ownerSteamId?.trim()?.takeIf { STEAM_ID_64.matches(it) } ?: return null
        val cid = classId.trim().takeIf { it.isNotEmpty() } ?: return null
        val aid = assetId?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val dto = runCatching {
            inventoryApi.getInventoryItemDetail(
                steamId = owner,
                assetId = aid,
                classId = cid
            )
        }.getOrNull() ?: return null
        return dto.toSkin(isFavorite = isFavorite, offerOwnerSteamId = offerOwnerSteamId)
    }

    private fun Skin.withInventoryContext(assetId: String?, offerOwnerSteamId: String?): Skin {
        var result = this
        val a = assetId?.trim()?.takeIf { it.isNotEmpty() }
        if (a != null) result = result.copy(inventoryAssetId = a)
        val o = offerOwnerSteamId?.trim()?.takeIf { it.isNotEmpty() && it != "_" }
        if (o != null) result = result.copy(offerOwnerSteamId = o)
        return result
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
