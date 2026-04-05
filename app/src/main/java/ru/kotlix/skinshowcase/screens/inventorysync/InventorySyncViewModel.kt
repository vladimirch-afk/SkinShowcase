package ru.kotlix.skinshowcase.screens.inventorysync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.core.network.RetrofitProvider
import ru.kotlix.skinshowcase.core.network.bestApiMessage
import ru.kotlix.skinshowcase.core.network.auth.AuthApiService
import ru.kotlix.skinshowcase.core.network.auth.CurrentUser
import ru.kotlix.skinshowcase.core.network.inventory.InventoryApiService

class InventorySyncViewModel : BaseViewModel<InventorySyncUiState>() {

    override fun initialState(): InventorySyncUiState = InventorySyncUiState()

    fun sync() {
        launch {
            updateState { InventorySyncUiState(isLoading = true, errorMessage = null, itemCount = null) }
            runCatching {
                withContext(Dispatchers.IO) {
                    val auth = RetrofitProvider.create(AuthApiService::class.java)
                    val steamId = CurrentUser.steamId?.trim()?.takeIf { it.length == 17 }
                        ?: auth.getMe().steamId?.trim()?.takeIf { it.length == 17 }
                    if (steamId == null) {
                        throw IllegalStateException("Нет Steam ID — войдите через Steam")
                    }
                    CurrentUser.steamId = steamId
                    val inv = RetrofitProvider.create(InventoryApiService::class.java).getInventory(steamId)
                    inv.items?.size ?: 0
                }
            }.fold(
                onSuccess = { count ->
                    updateState {
                        InventorySyncUiState(isLoading = false, itemCount = count, errorMessage = null)
                    }
                },
                onFailure = { e ->
                    updateState {
                        InventorySyncUiState(
                            isLoading = false,
                            itemCount = null,
                            errorMessage = e.bestApiMessage()
                        )
                    }
                }
            )
        }
    }
}
