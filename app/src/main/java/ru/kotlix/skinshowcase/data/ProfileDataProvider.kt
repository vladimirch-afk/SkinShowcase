package ru.kotlix.skinshowcase.data

import ru.kotlix.skinshowcase.core.Result
import ru.kotlix.skinshowcase.core.network.ApiService
import ru.kotlix.skinshowcase.core.network.RetrofitProvider
import ru.kotlix.skinshowcase.core.network.SkinDto
import ru.kotlix.skinshowcase.core.network.toSkinDto
import ru.kotlix.skinshowcase.core.network.isApiForbidden
import ru.kotlix.skinshowcase.core.network.auth.AuthApiService
import ru.kotlix.skinshowcase.core.network.auth.AvatarUrls
import ru.kotlix.skinshowcase.core.network.auth.CurrentUser
import ru.kotlix.skinshowcase.core.network.auth.MeResponseDto
import ru.kotlix.skinshowcase.core.network.trades.CreateTradeSelectionRequestDto
import java.util.Locale
import ru.kotlix.skinshowcase.core.network.inventory.InventoryApiService
import ru.kotlix.skinshowcase.core.network.inventory.InventoryItemDto
import ru.kotlix.skinshowcase.core.network.inventory.steamEconomyImageUrl
import retrofit2.HttpException
import ru.kotlix.skinshowcase.core.network.trades.SelectedItemDto
import ru.kotlix.skinshowcase.core.network.messaging.MessagingProvider
import ru.kotlix.skinshowcase.core.network.trades.TradesApiService
import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.screens.profile.OfferSummary
import ru.kotlix.skinshowcase.screens.profile.ProfileUiState
import ru.kotlix.skinshowcase.screens.profile.SellerInfo
import ru.kotlix.skinshowcase.settings.PrivacyPreferences
import ru.kotlix.skinshowcase.settings.TradeLinkPreferences

/**
 * Единый источник данных профиля и офферов.
 * Набор для обмена: GET / PUT / DELETE `api/v1/trades/selection/{steamId}`; DELETE `…/items` — точечное удаление.
 */
object ProfileDataProvider {

    const val MAX_TRADE_SELECTION_ITEMS: Int = 5

    @Volatile
    private var offersNeedRefresh = false

    fun markOffersNeedRefresh() {
        offersNeedRefresh = true
    }

    fun consumeOffersNeedRefresh(): Boolean = offersNeedRefresh.also { offersNeedRefresh = false }

    private suspend fun resolveSteamId(): String? {
        CurrentUser.steamId?.trim()?.takeIf { it.length == 17 }?.let { return it }
        val r = runCatching {
            RetrofitProvider.create(AuthApiService::class.java).getMe().steamId?.trim()?.takeIf { it.length == 17 }
        }
        return if (r.isSuccess) {
            r.getOrNull()?.also { CurrentUser.steamId = it }
        } else {
            val e = r.exceptionOrNull()
            if (e != null && e.isApiForbidden()) throw e
            null
        }
    }

    /** SteamID64 для запросов к своему инвентарю (кэш + GET /me при необходимости). */
    suspend fun resolveCurrentUserSteamId(): String? = resolveSteamId()

    /**
     * Синхронизация полей аватарки в [CurrentUser] из ответа /auth/me.
     * Поля без данных в ответе не затирают локальный выбор (например после PATCH с неполным GET).
     */
    fun applyMeAvatarToCurrentUser(me: MeResponseDto?) {
        if (me == null) return
        val srcRaw = me.source?.trim()?.uppercase(Locale.US)?.takeIf { it.isNotEmpty() }
        val legacyPreset = me.avatarPresetId
        val presetFromServer = when {
            me.presetAvatarId != null -> me.presetAvatarId.toString()
            !legacyPreset.isNullOrBlank() -> legacyPreset.trim()
            else -> null
        }
        val steamFromServer = me.steamAvatarUrl?.trim()?.takeIf { it.isNotEmpty() }

        if (srcRaw != null) {
            CurrentUser.avatarSource = srcRaw
            if (srcRaw == "PRESET") {
                if (presetFromServer != null) {
                    CurrentUser.avatarPresetId = presetFromServer
                }
                CurrentUser.steamAvatarUrl = null
            } else if (srcRaw == "STEAM") {
                CurrentUser.avatarPresetId = null
                if (steamFromServer != null) {
                    CurrentUser.steamAvatarUrl = steamFromServer
                }
            }
            return
        }

        if (presetFromServer != null) {
            CurrentUser.avatarPresetId = presetFromServer
            if (CurrentUser.avatarSource == null) {
                CurrentUser.avatarSource = "PRESET"
            }
            CurrentUser.steamAvatarUrl = null
            return
        }

        if (steamFromServer != null) {
            CurrentUser.steamAvatarUrl = steamFromServer
            if (CurrentUser.avatarSource == null) {
                CurrentUser.avatarSource = "STEAM"
            }
            return
        }
    }

    /** Обновить [CurrentUser] из GET /auth/me (steamId, аватар). */
    suspend fun syncCurrentUserFromAuthMe() {
        val r = runCatching {
            RetrofitProvider.create(AuthApiService::class.java).getMe()
        }
        val me = if (r.isSuccess) {
            r.getOrNull()
        } else {
            val e = r.exceptionOrNull()
            if (e != null && e.isApiForbidden()) throw e
            null
        }
        me?.steamId?.trim()?.takeIf { it.length == 17 }?.let { CurrentUser.steamId = it }
        applyMeAvatarToCurrentUser(me)
    }

    suspend fun getProfileState(): ProfileUiState {
        val tradeLinkPref = TradeLinkPreferences.getTradeLink()
        val meResult = runCatching {
            RetrofitProvider.create(AuthApiService::class.java).getMe()
        }
        val me = if (meResult.isSuccess) {
            meResult.getOrNull()
        } else {
            val e = meResult.exceptionOrNull()
            if (e != null && e.isApiForbidden()) throw e
            null
        }
        me?.steamId?.trim()?.takeIf { it.length == 17 }?.let { CurrentUser.steamId = it }
        val serverLink = me?.steamTradeLink?.trim()?.takeIf { it.isNotEmpty() }
        if (serverLink != null) {
            TradeLinkPreferences.setTradeLink(serverLink)
        } else if (me != null) {
            TradeLinkPreferences.setTradeLink(null)
        }
        val tradeLink = when {
            serverLink != null -> serverLink
            me != null -> null
            else -> tradeLinkPref
        }
        val activeOffers = getOffers()
        applyMeAvatarToCurrentUser(me)
        val avatarUrl = AvatarUrls.currentUserAvatarDisplayUrl()
        val steamProfileImg = me?.steamAvatarUrl?.trim()?.takeIf { it.isNotEmpty() }
            ?: CurrentUser.steamAvatarUrl?.trim()?.takeIf { it.isNotEmpty() }
        return ProfileUiState(
            steamNickname = me?.displayName?.trim().orEmpty(),
            steamAvatarUrl = avatarUrl,
            steamProfileImageUrl = steamProfileImg,
            avatarPresetId = CurrentUser.avatarPresetId,
            avatarSource = CurrentUser.avatarSource,
            steamId = me?.steamId,
            tradeLink = tradeLink,
            activeOffers = activeOffers,
            dealHistory = emptyList(),
            showProfile = PrivacyPreferences.getShowProfile()
        )
    }

    suspend fun getTradeLinkForSteamId(steamId: String): String? {
        val trimmed = steamId.trim()
        if (trimmed.length != 17) return null
        return runCatching {
            RetrofitProvider.create(AuthApiService::class.java).getUserTradeLink(trimmed).tradeUrl?.trim()?.takeIf { it.isNotEmpty() }
        }.getOrNull()
    }

    /**
     * Ник из GET /api/chats (если с пользователем уже есть чат).
     */
    suspend fun getCounterpartyNicknameFromChats(steamId: String): String? {
        val id = steamId.trim()
        if (id.length != 17) return null
        return when (val r = MessagingProvider.repository.getChats()) {
            is Result.Success ->
                r.data
                    .find { it.counterpartySteamId.trim() == id }
                    ?.counterpartyNickname
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
            else -> null
        }
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
            runCatching { itemsApi.getSkinById(cid).toSkinDto() }.getOrNull()?.let { metaByClassId[cid] = it }
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
     * Добавить предмет в оффер: прочитать текущий набор, объединить с новым,
     * затем `DELETE /api/v1/trades/selection/{steamId}` и `PUT` полным списком (старые + новый).
     */
    suspend fun createOffer(classId: String, inventoryAssetId: String?): kotlin.Result<Unit> {
        return runCatching {
            val steamId = resolveSteamId() ?: error("Войдите через Steam")
            val c = classId.trim()
            val a = inventoryAssetId?.trim()?.takeIf { it.isNotEmpty() }
            if (c.isEmpty() && a.isNullOrEmpty()) {
                error("Не удалось определить предмет")
            }
            val newItem = SelectedItemDto(assetId = a, classId = c.takeIf { it.isNotEmpty() })
            val api = RetrofitProvider.create(TradesApiService::class.java)
            val existingItems = try {
                api.getTradeSelection(steamId).items.orEmpty()
            } catch (e: HttpException) {
                if (e.code() == 404) emptyList() else throw e
            }
            if (!existingItems.any { selectionItemSame(it, newItem) } &&
                existingItems.size >= MAX_TRADE_SELECTION_ITEMS
            ) {
                error("В оффере не больше $MAX_TRADE_SELECTION_ITEMS предметов")
            }
            val merged = mergeSelectionItems(existingItems, newItem)
            try {
                api.deleteTradeSelection(steamId)
            } catch (e: HttpException) {
                if (e.code() != 404) throw e
            }
            api.upsertTradeSelection(steamId, CreateTradeSelectionRequestDto(items = merged))
            markOffersNeedRefresh()
        }
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

    private fun tradeSelectionItemFromSkin(skin: Skin): SelectedItemDto {
        val c = skin.id.trim()
        val a = skin.inventoryAssetId?.trim()?.takeIf { it.isNotEmpty() }
        return SelectedItemDto(assetId = a, classId = c.takeIf { it.isNotEmpty() })
    }

    fun skinMatchesTradeSelectionItem(skin: Skin, item: SelectedItemDto): Boolean {
        return selectionItemSame(item, tradeSelectionItemFromSkin(skin))
    }

    /** Два снимка набора совпадают как мультимножество (порядок не важен). */
    fun tradeSelectionSetsEqual(a: List<SelectedItemDto>, b: List<SelectedItemDto>): Boolean {
        if (a.size != b.size) return false
        val remaining = b.toMutableList()
        for (x in a) {
            val idx = remaining.indexOfFirst { selectionItemSame(x, it) }
            if (idx < 0) return false
            remaining.removeAt(idx)
        }
        return true
    }

    suspend fun getTradeSelectionSnapshot(): List<SelectedItemDto> {
        val steamId = resolveSteamId() ?: return emptyList()
        val api = RetrofitProvider.create(TradesApiService::class.java)
        return runCatching {
            try {
                api.getTradeSelection(steamId).items.orEmpty()
            } catch (e: HttpException) {
                if (e.code() == 404) emptyList() else throw e
            }
        }.getOrDefault(emptyList())
    }

    suspend fun removeSkinFromTradeSelection(skin: Skin): kotlin.Result<Unit> {
        return runCatching {
            val steamId = resolveSteamId() ?: error("Войдите через Steam")
            val item = tradeSelectionItemFromSkin(skin)
            val cid = item.classId?.trim()?.takeIf { it.isNotEmpty() }
            val aid = item.assetId?.trim()?.takeIf { it.isNotEmpty() }
            if (cid.isNullOrEmpty() && aid.isNullOrEmpty()) {
                error("Не удалось определить предмет")
            }
            RetrofitProvider.create(TradesApiService::class.java).removeTradeSelectionItems(
                steamId,
                CreateTradeSelectionRequestDto(items = listOf(item))
            )
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
