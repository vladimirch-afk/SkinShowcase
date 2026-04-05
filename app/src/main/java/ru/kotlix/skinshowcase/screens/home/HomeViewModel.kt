package ru.kotlix.skinshowcase.screens.home

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import ru.kotlix.skinshowcase.analytics.AppAnalytics
import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.domain.SkinFilter
import ru.kotlix.skinshowcase.core.domain.mapper.toDomain
import ru.kotlix.skinshowcase.core.network.ApiService
import ru.kotlix.skinshowcase.core.network.RetrofitProvider
import ru.kotlix.skinshowcase.core.network.SkinsProvider
import ru.kotlix.skinshowcase.core.network.auth.AvatarUrls
import ru.kotlix.skinshowcase.core.network.auth.CurrentUser
import ru.kotlix.skinshowcase.core.network.bestApiMessage
import ru.kotlix.skinshowcase.core.network.inventory.InventoryApiService
import ru.kotlix.skinshowcase.core.network.inventory.toSkin
import ru.kotlix.skinshowcase.core.network.toSkinDto
import ru.kotlix.skinshowcase.core.network.trades.TradeSelectionDto
import ru.kotlix.skinshowcase.core.network.trades.TradesApiService
import ru.kotlix.skinshowcase.data.ProfileDataProvider

class HomeViewModel : BaseViewModel<HomeUiState>() {

    override fun initialState(): HomeUiState = HomeUiState(
        skins = emptyList(),
        isLoading = true
    )

    fun loadSkins() {
        launch {
            updateState { state ->
                val wasContent = state.skins.isNotEmpty()
                state.copy(
                    isLoading = !wasContent,
                    isRefreshing = wasContent,
                    errorMessage = null
                )
            }
            try {
                val useFeed = uiState.value.tradeFeedMode
                val list = withTimeout(15_000) {
                    withContext(Dispatchers.IO) {
                        if (useFeed) {
                            loadTradeFeedSkins()
                        } else {
                            SkinsProvider.repository.getSkinsFromApi()
                        }
                    }
                }
                withContext(Dispatchers.Main.immediate) {
                    updateState {
                        it.copy(skins = list, isLoading = false, isRefreshing = false, errorMessage = null)
                    }
                }
                refreshUserAvatarUrl()
            } catch (e: Throwable) {
                if (e !is CancellationException) {
                    AppAnalytics.reportErrorWithMessage("loadSkins", e)
                }
                withContext(NonCancellable) {
                    withContext(Dispatchers.Main.immediate) {
                        updateState {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = e.bestApiMessage()
                            )
                        }
                    }
                }
                if (e is CancellationException) throw e
            } finally {
                withContext(NonCancellable) {
                    withContext(Dispatchers.Main.immediate) {
                        updateState { it.copy(isLoading = false, isRefreshing = false) }
                    }
                }
            }
        }
    }

    private suspend fun refreshUserAvatarUrl() {
        withContext(Dispatchers.IO) {
            ProfileDataProvider.syncCurrentUserFromAuthMe()
        }
        val url = AvatarUrls.currentUserAvatarDisplayUrl()
        withContext(Dispatchers.Main.immediate) {
            updateState { it.copy(userAvatarUrl = url) }
        }
    }

    /** Без сети: подтянуть URL из [CurrentUser] при возврате на вкладку (после смены аватара в профиле). */
    fun refreshUserAvatarFromCurrentUser() {
        updateState { it.copy(userAvatarUrl = AvatarUrls.currentUserAvatarDisplayUrl()) }
    }

    private suspend fun loadTradeFeedSkins(): List<Skin> {
        val tradesApi = RetrofitProvider.create(TradesApiService::class.java)
        val itemsApi = RetrofitProvider.create(ApiService::class.java)
        val exclude = CurrentUser.steamId?.trim()?.takeIf { it.length == 17 }
        val content = tradesApi.getFeed(page = 0, size = 50, excludeSteamId = exclude).content
        val inventoryApi = RetrofitProvider.create(InventoryApiService::class.java)
        return hydrateTradeFeedSelections(content, itemsApi, inventoryApi)
    }

    /**
     * Карточки ленты: сначала каталог [ApiService.getSkinById] (название/цена),
     * если предмета нет в БД items (404) — [InventoryApiService.getInventoryItemDetail] по владельцу ленты,
     * иначе заглушка, чтобы оффер всё равно отображался.
     */
    private suspend fun hydrateTradeFeedSelections(
        content: List<TradeSelectionDto>,
        itemsApi: ApiService,
        inventoryApi: InventoryApiService
    ): List<Skin> {
        val favoriteIds = SkinsProvider.repository.getFavoriteSkinIds()
        val catalogByClassId = mutableMapOf<String, Skin>()
        val result = mutableListOf<Skin>()
        for (selection in content) {
            val owner = selection.steamId?.trim().orEmpty()
            if (owner.length != 17 || !owner.all { it.isDigit() }) continue
            for (item in selection.items.orEmpty()) {
                val classId = item.classId?.trim()?.takeIf { it.isNotEmpty() } ?: continue
                val assetId = item.assetId?.trim()?.takeIf { it.isNotEmpty() }
                val resolved = catalogByClassId[classId] ?: run {
                    val fromCatalog = runCatching {
                        itemsApi.getSkinById(classId).toSkinDto().toDomain(isFavorite = false)
                    }.getOrNull()
                    if (fromCatalog != null) {
                        catalogByClassId[classId] = fromCatalog
                        fromCatalog
                    } else if (!assetId.isNullOrEmpty()) {
                        runCatching {
                            inventoryApi.getInventoryItemDetail(
                                steamId = owner,
                                assetId = assetId,
                                classId = classId
                            ).toSkin(isFavorite = false, offerOwnerSteamId = owner)
                        }.getOrNull()
                    } else {
                        null
                    }
                } ?: tradeFeedPlaceholderSkin(classId, assetId)
                result.add(
                    resolved.copy(
                        offerOwnerSteamId = owner,
                        inventoryAssetId = assetId,
                        isFavorite = resolved.id in favoriteIds
                    )
                )
            }
        }
        return result
    }

    private fun tradeFeedPlaceholderSkin(classId: String, assetId: String?): Skin =
        Skin(
            id = classId,
            name = "Предмет",
            imageUrl = null,
            price = null,
            inventoryAssetId = assetId
        )

    fun clearError() {
        updateState { it.copy(errorMessage = null) }
    }

    fun clearRefreshing() {
        updateState { it.copy(isRefreshing = false) }
    }

    fun updateSearch(query: String) {
        updateState { it.copy(searchQuery = query) }
    }

    fun openFilterSheet() {
        updateState { it.copy(filterSheetVisible = true) }
    }

    fun dismissFilterSheet() {
        updateState { it.copy(filterSheetVisible = false) }
    }

    fun applyFilter(filter: SkinFilter) {
        updateState {
            it.copy(filter = filter, filterSheetVisible = false)
        }
    }

    fun setSortOption(option: SortOption) {
        updateState { it.copy(sortOption = option) }
    }

    fun toggleFavorite(skin: Skin) {
        launch {
            val repo = SkinsProvider.repository
            val added = !skin.isFavorite
            if (skin.isFavorite) repo.removeFromFavorites(skin.id)
            else repo.addToFavorites(skin)
            val likeKey = tradeFeedLikeKey(skin)
            updateState { state ->
                val nextLikes = if (skin.offerOwnerSteamId != null && added) {
                    state.tradeFeedLocalLikes - likeKey
                } else {
                    state.tradeFeedLocalLikes
                }
                state.copy(
                    skins = state.skins.map {
                        if (matchesTradeFeedSkinSlot(it, skin)) it.copy(isFavorite = !it.isFavorite) else it
                    },
                    tradeFeedLocalLikes = nextLikes
                )
            }
            AppAnalytics.reportEvent(if (added) "favorite_added" else "favorite_removed", mapOf("skin_id" to skin.id))
        }
    }

    fun toggleTradeFeedLocalLike(skin: Skin) {
        if (skin.offerOwnerSteamId == null || skin.isFavorite) return
        val key = tradeFeedLikeKey(skin)
        updateState { state ->
            val next = if (key in state.tradeFeedLocalLikes) {
                state.tradeFeedLocalLikes - key
            } else {
                state.tradeFeedLocalLikes + key
            }
            state.copy(tradeFeedLocalLikes = next)
        }
    }

    fun commitTradeFeedLikesForOwner(ownerSteamId: String) {
        launch {
            val repo = SkinsProvider.repository
            val snapshot = uiState.value
            val likes = snapshot.tradeFeedLocalLikes
            val toAdd = snapshot.skins.filter { candidate ->
                candidate.offerOwnerSteamId == ownerSteamId &&
                    tradeFeedLikeKey(candidate) in likes &&
                    !candidate.isFavorite
            }
            for (item in toAdd) {
                repo.addToFavorites(item)
            }
            val keysDone = toAdd.map { tradeFeedLikeKey(it) }.toSet()
            updateState { state ->
                state.copy(
                    tradeFeedLocalLikes = state.tradeFeedLocalLikes - keysDone,
                    skins = state.skins.map { skin ->
                        val mark = toAdd.any { matchesTradeFeedSkinSlot(it, skin) }
                        if (mark) skin.copy(isFavorite = true) else skin
                    }
                )
            }
            if (toAdd.isNotEmpty()) {
                AppAnalytics.reportEvent(
                    "trade_feed_owner_commit_likes",
                    mapOf("owner_steam_id" to ownerSteamId, "count" to toAdd.size.toString())
                )
            }
        }
    }
}
