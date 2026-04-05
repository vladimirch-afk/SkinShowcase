package ru.kotlix.skinshowcase.screens.skins

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.core.network.SkinsProvider
import ru.kotlix.skinshowcase.core.network.bestApiMessage

class SkinsViewModel : BaseViewModel<SkinsUiState>() {

    override fun initialState(): SkinsUiState = SkinsUiState(
        skins = emptyList(),
        isLoading = true
        // данные-заглушки отключены — загрузка через api-gateway
    )

    fun loadSkins() {
        launch {
            updateState { state ->
                val hadContent = state.skins.isNotEmpty()
                state.copy(
                    isLoading = !hadContent,
                    isRefreshing = hadContent,
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
                updateState {
                    it.copy(
                        skins = it.skins,
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = e.bestApiMessage()
                    )
                }
                if (e is CancellationException) throw e
            } finally {
                updateState { it.copy(isLoading = false, isRefreshing = false) }
            }
        }
    }

    fun clearRefreshing() {
        updateState { it.copy(isRefreshing = false) }
    }

    fun clearError() {
        updateState { it.copy(errorMessage = null) }
    }

    // --- данные-заглушки (отключены) ---
    // private fun defaultSkins(): List<Skin> = listOf(...)
}
