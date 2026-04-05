package ru.kotlix.skinshowcase.core.repository

import ru.kotlix.skinshowcase.core.database.dao.SkinCacheDao
import ru.kotlix.skinshowcase.core.database.mapper.toCachedSkinEntity
import ru.kotlix.skinshowcase.core.database.mapper.toSkinDto
import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.domain.mapper.toDomain
import ru.kotlix.skinshowcase.core.network.ApiService
import ru.kotlix.skinshowcase.core.network.isApiForbidden
import ru.kotlix.skinshowcase.core.network.toSkinDto
import ru.kotlix.skinshowcase.core.network.inventory.InventoryApiService
import ru.kotlix.skinshowcase.core.network.inventory.toSkin
import ru.kotlix.skinshowcase.core.domain.mapper.toDomain as dtoToDomain

/**
 * Single source of truth: remote API + local cache (Room).
 */
class SkinsRepository(
    private val api: ApiService,
    private val inventoryApi: InventoryApiService,
    private val skinCacheDao: SkinCacheDao
) {

    private companion object {
        /** Как на steam-gateway: SteamID64, 17 цифр, начинается с 765. */
        private val STEAM_ID_64 = Regex("^765\\d{14}$")
    }

    suspend fun getSkinsFromApi(): List<Skin> {
        val remote = runCatching { api.getSkins() }
        if (remote.isFailure) {
            val e = remote.exceptionOrNull()
            if (e != null && e.isApiForbidden()) throw e
        }
        return when {
            remote.isSuccess -> {
                val list = remote.getOrThrow()
                skinCacheDao.deleteAll()
                skinCacheDao.insertAll(list.mapIndexed { index, dto -> dto.toCachedSkinEntity(orderIndex = index) })
                list.map { dto -> dto.dtoToDomain() }
            }
            else -> {
                val cached = skinCacheDao.getAll()
                cached.map { it.toSkinDto().dtoToDomain() }
            }
        }
    }

    suspend fun getSkinByIdFromApi(
        id: String,
        inventoryOwnerSteamId: String? = null,
        inventoryAssetId: String? = null,
        offerOwnerSteamId: String? = null
    ): Skin? {
        val fromInventory = fetchInventoryItemDetailSkin(
            classId = id,
            ownerSteamId = inventoryOwnerSteamId,
            assetId = inventoryAssetId,
            offerOwnerSteamId = offerOwnerSteamId
        )
        if (fromInventory != null) {
            skinCacheDao.insert(fromInventory.toCachedSkinEntity(orderIndex = 0))
            return fromInventory
        }
        val fromApi = runCatching { api.getSkinById(id).toSkinDto() }
        if (fromApi.isFailure) {
            val e = fromApi.exceptionOrNull()
            if (e != null && e.isApiForbidden()) throw e
        }
        return when {
            fromApi.isSuccess -> {
                val dto = fromApi.getOrThrow()
                skinCacheDao.insert(dto.toCachedSkinEntity(orderIndex = 0))
                dto.dtoToDomain().withInventoryContext(inventoryAssetId, offerOwnerSteamId)
            }
            else -> {
                val cached = skinCacheDao.getById(id) ?: return null
                cached.toSkinDto().dtoToDomain().withInventoryContext(inventoryAssetId, offerOwnerSteamId)
            }
        }
    }

    private suspend fun fetchInventoryItemDetailSkin(
        classId: String,
        ownerSteamId: String?,
        assetId: String?,
        offerOwnerSteamId: String?
    ): Skin? {
        val owner = ownerSteamId?.trim()?.takeIf { STEAM_ID_64.matches(it) } ?: return null
        val cid = classId.trim().takeIf { it.isNotEmpty() } ?: return null
        val aid = assetId?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val inv = runCatching {
            inventoryApi.getInventoryItemDetail(
                steamId = owner,
                assetId = aid,
                classId = cid
            )
        }
        if (inv.isFailure) {
            val e = inv.exceptionOrNull()
            if (e != null && e.isApiForbidden()) throw e
        }
        val dto = inv.getOrNull() ?: return null
        return dto.toSkin(offerOwnerSteamId = offerOwnerSteamId)
    }

    private fun Skin.withInventoryContext(assetId: String?, offerOwnerSteamId: String?): Skin {
        var result = this
        val a = assetId?.trim()?.takeIf { it.isNotEmpty() }
        if (a != null) result = result.copy(inventoryAssetId = a)
        val o = offerOwnerSteamId?.trim()?.takeIf { it.isNotEmpty() && it != "_" }
        if (o != null) result = result.copy(offerOwnerSteamId = o)
        return result
    }
}
