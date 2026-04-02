package ru.kotlix.skinshowcase.data

import ru.kotlix.skinshowcase.core.network.ApiService
import ru.kotlix.skinshowcase.core.network.RetrofitProvider
import ru.kotlix.skinshowcase.core.network.SkinDto
import ru.kotlix.skinshowcase.core.network.auth.AuthApiService
import ru.kotlix.skinshowcase.core.network.auth.CurrentUser
import ru.kotlix.skinshowcase.core.network.trades.CreateTradeSelectionRequestDto
import ru.kotlix.skinshowcase.core.network.inventory.InventoryApiService
import ru.kotlix.skinshowcase.core.network.inventory.InventoryItemDto
import ru.kotlix.skinshowcase.core.network.inventory.steamEconomyImageUrl
import ru.kotlix.skinshowcase.core.network.trades.SelectedItemDto
import ru.kotlix.skinshowcase.core.network.trades.TradesApiService
import ru.kotlix.skinshowcase.screens.profile.OfferSummary
import ru.kotlix.skinshowcase.screens.profile.ProfileUiState
import ru.kotlix.skinshowcase.screens.profile.SellerInfo
import ru.kotlix.skinshowcase.settings.PrivacyPreferences
import ru.kotlix.skinshowcase.settings.TradeLinkPreferences

/**
 * Единый источник данных профиля и офферов.
 * Набор для обмена: GET/PUT/DELETE api/v1/trades/selection/{steamId} (+ …/items для удаления позиций).
 */
object ProfileDataProvider {

    @Volatile
    private var offersNeedRefresh = false

    fun markOffersNeedRefresh() {
        offersNeedRefresh = true
    }

    fun consumeOffersNeedRefresh(): Boolean = offersNeedRefresh.also { offersNeedRefresh = false }

    private suspend fun resolveSteamId(): String? {
        CurrentUser.steamId?.trim()?.takeIf { it.length == 17 }?.let { return it }
        return runCatching {
            RetrofitProvider.create(AuthApiService::class.java).getMe().steamId?.trim()?.takeIf { it.length == 17 }
        }.getOrNull()?.also { CurrentUser.steamId = it }
    }

    suspend fun getProfileState(): ProfileUiState {
        val tradeLinkPref = TradeLinkPreferences.getTradeLink()
        val me = runCatching {
            RetrofitProvider.create(AuthApiService::class.java).getMe()
        }.getOrNull()
        me?.steamId?.trim()?.takeIf { it.length == 17 }?.let { CurrentUser.steamId = it }
        val serverLink = me?.steamTradeLink?.trim()?.takeIf { it.isNotEmpty() }
        if (serverLink != null) {
            TradeLinkPreferences.setTradeLink(serverLink)
        }
        val tradeLink = serverLink ?: tradeLinkPref
        return ProfileUiState(
            steamNickname = "",
            steamAvatarUrl = null,
            steamId = me?.steamId,
            tradeLink = tradeLink,
            activeOffers = emptyList(),
            dealHistory = emptyList(),
            showProfile = PrivacyPreferences.getShowProfile(),
            showOffers = PrivacyPreferences.getShowOffers()
        )
    }

    suspend fun getTradeLinkForSteamId(steamId: String): String? {
        val trimmed = steamId.trim()
        if (trimmed.length != 17) return null
        return runCatching {
            RetrofitProvider.create(AuthApiService::class.java).getUserTradeLink(trimmed).tradeUrl?.trim()?.takeIf { it.isNotEmpty() }
        }.getOrNull()
    }

    suspend fun getOffers(): List<OfferSummary> {
        val steamId = resolveSteamId() ?: return emptyList()
        val selection = runCatching {
            RetrofitProvider.create(TradesApiService::class.java).getTradeSelection(steamId)
        }.getOrNull() ?: return emptyList()
        val rawItems = selection.items.orEmpty()
        if (rawItems.isEmpty()) return emptyList()

        val invList = runCatching {
            RetrofitProvider.create(InventoryApiService::class.java).getInventory(steamId).items.orEmpty()
        }.getOrElse { emptyList() }
        val invByAsset = invList.mapNotNull { inv ->
            val a = inv.assetId?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            a to inv
        }.toMap()
        val invByClass = invList.groupBy { it.classId?.trim()?.takeIf { c -> c.isNotEmpty() } ?: "" }
            .filterKeys { it.isNotEmpty() }

        val metaByClassId = linkedMapOf<String, SkinDto>()
        val itemsApi = RetrofitProvider.create(ApiService::class.java)
        for (sel in rawItems) {
            val cid = sel.classId?.trim()?.takeIf { it.isNotEmpty() } ?: continue
            if (metaByClassId.containsKey(cid)) continue
            runCatching { itemsApi.getSkinById(cid) }.getOrNull()?.let { metaByClassId[cid] = it }
        }

        return rawItems.mapIndexed { index, sel ->
            val base = sel.toOfferSummary(index)
            val inv = resolveInventoryRow(sel, invByAsset, invByClass)
            val cid = base.classId
            val skinMeta = cid?.let { metaByClassId[it] }

            val name = displayNameForOffer(skinMeta, inv, base.skinName)
            val image = displayImageForOffer(skinMeta, inv)
            val price = skinMeta?.price ?: base.priceRub

            base.copy(
                skinName = name,
                skinImageUrl = image,
                priceRub = price
            )
        }
    }

    private fun resolveInventoryRow(
        sel: SelectedItemDto,
        invByAsset: Map<String, InventoryItemDto>,
        invByClass: Map<String, List<InventoryItemDto>>
    ): InventoryItemDto? {
        val aid = sel.assetId?.trim()?.takeIf { it.isNotEmpty() }
        if (aid != null) {
            invByAsset[aid]?.let { return it }
        }
        val cid = sel.classId?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return invByClass[cid]?.firstOrNull()
    }

    private fun displayNameForOffer(
        skinMeta: SkinDto?,
        inv: InventoryItemDto?,
        fallback: String
    ): String {
        val fromCatalog = skinMeta?.name?.trim()?.takeIf { it.isNotEmpty() }
        if (fromCatalog != null) return fromCatalog
        val fromInv = inv?.name?.trim()?.takeIf { it.isNotEmpty() }
            ?: inv?.marketHashName?.trim()?.takeIf { it.isNotEmpty() }
        if (fromInv != null) return fromInv
        return fallback
    }

    private fun displayImageForOffer(skinMeta: SkinDto?, inv: InventoryItemDto?): String? {
        val fromCatalog = skinMeta?.imageUrl?.trim()?.takeIf { it.isNotEmpty() }
        if (fromCatalog != null) return fromCatalog
        return steamEconomyImageUrl(inv?.iconUrl)
    }

    /**
     * Добавить предмет в набор: GET текущего набора, мерж, PUT полный список.
     */
    suspend fun createOffer(classId: String, inventoryAssetId: String?): Result<Unit> {
        return runCatching {
            val steamId = resolveSteamId() ?: error("Войдите через Steam")
            val c = classId.trim()
            val a = inventoryAssetId?.trim()?.takeIf { it.isNotEmpty() }
            if (c.isEmpty() && a.isNullOrEmpty()) {
                error("Не удалось определить предмет")
            }
            val newItem = SelectedItemDto(assetId = a, classId = c.takeIf { it.isNotEmpty() })
            val api = RetrofitProvider.create(TradesApiService::class.java)
            val existing = runCatching { api.getTradeSelection(steamId) }.getOrNull()
            val merged = mergeSelectionItems(existing?.items.orEmpty(), newItem)
            api.upsertTradeSelection(steamId, CreateTradeSelectionRequestDto(items = merged))
            markOffersNeedRefresh()
        }
    }

    suspend fun deleteOffer(offer: OfferSummary): Boolean {
        val steamId = resolveSteamId() ?: return false
        val cid = offer.classId?.trim()?.takeIf { it.isNotEmpty() }
            ?: offer.skinId.trim().takeIf { it.isNotEmpty() }
        val aid = offer.assetId?.trim()?.takeIf { it.isNotEmpty() }
        if (cid.isNullOrEmpty() && aid.isNullOrEmpty()) return false
        val item = SelectedItemDto(assetId = aid, classId = cid)
        return runCatching {
            RetrofitProvider.create(TradesApiService::class.java).removeTradeSelectionItems(
                steamId,
                CreateTradeSelectionRequestDto(items = listOf(item))
            )
            true
        }.getOrDefault(false)
    }

    private fun mergeSelectionItems(
        existing: List<SelectedItemDto>,
        newItem: SelectedItemDto
    ): List<SelectedItemDto> {
        if (existing.any { selectionItemSame(it, newItem) }) {
            return existing
        }
        return existing + newItem
    }

    private fun selectionItemSame(a: SelectedItemDto, b: SelectedItemDto): Boolean {
        val aa = a.assetId?.trim().orEmpty()
        val ba = b.assetId?.trim().orEmpty()
        if (aa.isNotEmpty() && ba.isNotEmpty()) {
            return aa == ba
        }
        val ac = a.classId?.trim().orEmpty()
        val bc = b.classId?.trim().orEmpty()
        return ac.isNotEmpty() && ac == bc && aa == ba
    }

    private fun SelectedItemDto.toOfferSummary(index: Int): OfferSummary {
        val cid = classId?.trim().orEmpty()
        val aid = assetId?.trim().orEmpty()
        val rowId = when {
            aid.isNotEmpty() -> aid
            cid.isNotEmpty() -> "${cid}_$index"
            else -> "item-$index"
        }
        val title = when {
            cid.isNotEmpty() && aid.isNotEmpty() -> "Предмет · $cid"
            cid.isNotEmpty() -> "Предмет · $cid"
            aid.isNotEmpty() -> "Asset $aid"
            else -> "Позиция #$index"
        }
        val navId = cid.ifEmpty { rowId }
        return OfferSummary(
            id = rowId,
            skinId = navId,
            skinName = title,
            skinImageUrl = null,
            priceRub = null,
            assetId = aid.ifEmpty { null },
            classId = cid.ifEmpty { null }
        )
    }

    suspend fun getSellerForSkin(skinId: String): SellerInfo? = null
}
