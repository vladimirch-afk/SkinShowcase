package ru.kotlix.skinshowcase.screens.createoffer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.kotlix.skinshowcase.R
import ru.kotlix.skinshowcase.core.network.RetrofitProvider
import ru.kotlix.skinshowcase.core.network.bestApiMessage
import ru.kotlix.skinshowcase.core.network.auth.AuthApiService
import ru.kotlix.skinshowcase.core.network.auth.CurrentUser
import ru.kotlix.skinshowcase.core.network.inventory.InventoryApiService
import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.domain.SkinFilter
import ru.kotlix.skinshowcase.core.network.inventory.toInventorySkin
import ru.kotlix.skinshowcase.core.network.trades.SelectedItemDto
import ru.kotlix.skinshowcase.data.ProfileDataProvider

data class CreateOfferSelectSkinUiState(
    val skins: List<Skin> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    /** Текущий набор оффера на сервере (до 5 предметов). */
    val tradeSelectionItems: List<SelectedItemDto> = emptyList(),
    /** Снимок набора при открытии экрана (для кнопки «Создать оффер» после изменений). */
    val baselineTradeSelectionItems: List<SelectedItemDto> = emptyList(),
    val togglingSkinKey: String? = null,
    /** Одноразовая подсказка / ошибка переключения (snackbar). */
    val selectionHint: String? = null,
    /** Группировать карточки по названию предмета и показывать счётчик. */
    val groupByName: Boolean = false,
    val searchQuery: String = "",
    val filter: SkinFilter = SkinFilter(),
    val filterSheetVisible: Boolean = false
)

class CreateOfferSelectSkinViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        CreateOfferSelectSkinUiState(skins = emptyList(), isLoading = true)
    )
    val uiState: StateFlow<CreateOfferSelectSkinUiState> = _uiState.asStateFlow()

    fun loadSkins() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
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
                    val list = inv.items.orEmpty().mapIndexed { index, dto -> dto.toInventorySkin(index) }
                    val selection = ProfileDataProvider.getTradeSelectionSnapshot()
                    Pair(list, selection)
                }
            }.fold(
                onSuccess = { (list, selection) ->
                    _uiState.update {
                        it.copy(
                            skins = list,
                            tradeSelectionItems = selection,
                            baselineTradeSelectionItems = selection.toList(),
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            skins = emptyList(),
                            tradeSelectionItems = emptyList(),
                            baselineTradeSelectionItems = emptyList(),
                            isLoading = false,
                            errorMessage = e.bestApiMessage()
                        )
                    }
                }
            )
        }
    }

    fun toggleOfferSelection(skin: Skin) {
        val key = skinToggleKey(skin)
        viewModelScope.launch {
            val cur = _uiState.value
            if (cur.togglingSkinKey != null) return@launch
            val items = cur.tradeSelectionItems
            val selected = items.any { ProfileDataProvider.skinMatchesTradeSelectionItem(skin, it) }
            if (!selected && items.size >= ProfileDataProvider.MAX_TRADE_SELECTION_ITEMS) {
                _uiState.update {
                    it.copy(
                        selectionHint = getApplication<Application>().getString(
                            R.string.create_offer_max_five_items
                        )
                    )
                }
                return@launch
            }
            _uiState.update { it.copy(togglingSkinKey = key) }
            if (selected) {
                val r = ProfileDataProvider.removeSkinFromTradeSelection(skin)
                if (r.isSuccess) {
                    refreshTradeSelectionAfterToggle()
                } else {
                    val e = r.exceptionOrNull()
                    _uiState.update {
                        it.copy(
                            togglingSkinKey = null,
                            selectionHint = e?.bestApiMessage()
                                ?: getApplication<Application>().getString(
                                    R.string.create_offer_toggle_failed
                                )
                        )
                    }
                }
            } else {
                val r = ProfileDataProvider.createOffer(skin.id, skin.inventoryAssetId)
                if (r.isSuccess) {
                    refreshTradeSelectionAfterToggle()
                } else {
                    val e = r.exceptionOrNull()
                    _uiState.update {
                        it.copy(
                            togglingSkinKey = null,
                            selectionHint = e?.bestApiMessage()
                                ?: getApplication<Application>().getString(
                                    R.string.create_offer_toggle_failed
                                )
                        )
                    }
                }
            }
        }
    }

    private suspend fun refreshTradeSelectionAfterToggle() {
        val selection = withContext(Dispatchers.IO) {
            ProfileDataProvider.getTradeSelectionSnapshot()
        }
        _uiState.update {
            it.copy(tradeSelectionItems = selection, togglingSkinKey = null)
        }
    }

    fun clearSelectionHint() {
        _uiState.update { it.copy(selectionHint = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun setGroupByName(enabled: Boolean) {
        _uiState.update { it.copy(groupByName = enabled) }
    }

    fun updateSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun openFilterSheet() {
        _uiState.update { it.copy(filterSheetVisible = true) }
    }

    fun dismissFilterSheet() {
        _uiState.update { it.copy(filterSheetVisible = false) }
    }

    fun applyFilter(filter: SkinFilter) {
        _uiState.update {
            it.copy(filter = filter, filterSheetVisible = false)
        }
    }
}

fun skinToggleKey(skin: Skin): String {
    val asset = skin.inventoryAssetId?.trim()?.takeIf { it.isNotEmpty() }
    if (asset != null) return asset
    return skin.id.trim().ifEmpty { "unknown" }
}
