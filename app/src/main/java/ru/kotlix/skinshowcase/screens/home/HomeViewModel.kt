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
import ru.kotlix.skinshowcase.core.network.auth.CurrentUser
import ru.kotlix.skinshowcase.core.network.inventory.InventoryApiService
import ru.kotlix.skinshowcase.core.network.inventory.toSkin
import ru.kotlix.skinshowcase.core.network.toSkinDto
import ru.kotlix.skinshowcase.core.network.trades.TradeSelectionDto
import ru.kotlix.skinshowcase.core.network.trades.TradesApiService

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
                                errorMessage = e.message ?: "Ошибка загрузки"
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
                        isFavorite = false
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
        if (skin.offerOwnerSteamId != null) return
        launch {
            val repo = SkinsProvider.repository
            val added = !skin.isFavorite
            if (skin.isFavorite) repo.removeFromFavorites(skin.id)
            else repo.addToFavorites(skin)
            updateState { state ->
                state.copy(
                    skins = state.skins.map {
                        if (it.id == skin.id) it.copy(isFavorite = !it.isFavorite) else it
                    }
                )
            }
            AppAnalytics.reportEvent(if (added) "favorite_added" else "favorite_removed", mapOf("skin_id" to skin.id))
        }
    }
}
