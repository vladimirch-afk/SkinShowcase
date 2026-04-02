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
import ru.kotlix.skinshowcase.core.network.RetrofitProvider
import ru.kotlix.skinshowcase.core.network.SkinsProvider
import ru.kotlix.skinshowcase.core.network.auth.CurrentUser
import ru.kotlix.skinshowcase.core.network.trades.TradesApiService
import ru.kotlix.skinshowcase.core.network.trades.toFeedSkin

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
        val api = RetrofitProvider.create(TradesApiService::class.java)
        val exclude = CurrentUser.steamId?.trim()?.takeIf { it.length == 17 }
        val page = api.getFeed(page = 0, size = 50, excludeSteamId = exclude)
        return page.content.mapNotNull { it.toFeedSkin() }
    }

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
