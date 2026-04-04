package ru.kotlix.skinshowcase.screens.createoffer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.core.network.RetrofitProvider
import ru.kotlix.skinshowcase.core.network.auth.AuthApiService
import ru.kotlix.skinshowcase.core.network.auth.CurrentUser
import ru.kotlix.skinshowcase.core.network.inventory.InventoryApiService
import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.domain.SkinFilter
import ru.kotlix.skinshowcase.core.network.inventory.toInventorySkin
import ru.kotlix.skinshowcase.screens.home.SortOption

data class CreateOfferSelectSkinUiState(
    val skins: List<Skin> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    /** Группировать карточки по названию предмета и показывать счётчик. */
    val groupByName: Boolean = false,
    val searchQuery: String = "",
    val filter: SkinFilter = SkinFilter(),
    val filterSheetVisible: Boolean = false,
    val sortOption: SortOption = SortOption.DEFAULT
)

class CreateOfferSelectSkinViewModel : BaseViewModel<CreateOfferSelectSkinUiState>() {

    override fun initialState(): CreateOfferSelectSkinUiState =
        CreateOfferSelectSkinUiState(skins = emptyList(), isLoading = true)

    fun loadSkins() {
        launch {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                withContext(Dispatchers.IO) {
                    val auth = RetrofitProvider.create(AuthApiService::class.java)
                    val steamId = CurrentUser.steamId?.trim()?.takeIf { it.length == 17 }
                        ?: auth.getMe().steamId?.trim()?.takeIf { it.length == 17 }
                    if (steamId == null) {
                        throw IllegalStateException("Войдите через Steam, чтобы загрузить инвентарь")
                    }
                    CurrentUser.steamId = steamId
                    val inv = RetrofitProvider.create(InventoryApiService::class.java).getInventory(steamId)
                    inv.items.orEmpty().mapIndexed { index, dto -> dto.toInventorySkin(index) }
                }
            }.fold(
                onSuccess = { list ->
                    updateState {
                        it.copy(skins = list, isLoading = false, errorMessage = null)
                    }
                },
                onFailure = { e ->
                    updateState {
                        it.copy(
                            skins = emptyList(),
                            isLoading = false,
                            errorMessage = e.message ?: "Ошибка загрузки"
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        updateState { it.copy(errorMessage = null) }
    }

    fun setGroupByName(enabled: Boolean) {
        updateState { it.copy(groupByName = enabled) }
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
}
