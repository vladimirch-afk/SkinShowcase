package ru.kotlix.skinshowcase.screens.home

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.core.domain.SkinFilter
import ru.kotlix.skinshowcase.core.network.SkinsProvider

class HomeViewModel : BaseViewModel<HomeUiState>() {

    override fun initialState(): HomeUiState = HomeUiState(
        skins = emptyList(),
        isLoading = true
        // Данные-заглушки отключены — загрузка через api-gateway (SkinsProvider.repository)
        // defaultSkins() закомментировано ниже
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
                val list = withTimeout(15_000) {
                    withContext(Dispatchers.IO) {
                        SkinsProvider.repository.getSkinsFromApi()
                    }
                }
                withContext(Dispatchers.Main.immediate) {
                    updateState {
                        it.copy(skins = list, isLoading = false, isRefreshing = false, errorMessage = null)
                    }
                }
            } catch (e: Throwable) {
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

    // --- данные-заглушки (отключены, данные через api-gateway) ---
    // private fun defaultSkins(): List<Skin> = listOf(
    //     Skin(id = "ak47-redline", name = "АК-47 | Красная линия", imageUrl = null, price = 40_000.0),
    //     Skin(id = "butterfly-gradient", name = "Butterfly Knife | Градиент", imageUrl = null, price = 100_000.0),
    //     Skin(id = "awp-lighting", name = "AWP | Молния", imageUrl = null, price = 55_000.0)
    // )
}
