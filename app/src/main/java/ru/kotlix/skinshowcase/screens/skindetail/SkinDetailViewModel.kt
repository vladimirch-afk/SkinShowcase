package ru.kotlix.skinshowcase.screens.skindetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.domain.SkinRarity
import ru.kotlix.skinshowcase.core.domain.SkinSpecial
import ru.kotlix.skinshowcase.core.domain.SkinWear
import ru.kotlix.skinshowcase.navigation.NavRoutes

class SkinDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val skinId: String = savedStateHandle.get<String>(NavRoutes.SKIN_DETAIL_ID_ARG) ?: ""

    private val _uiState = MutableStateFlow(SkinDetailUiState())
    val uiState: StateFlow<SkinDetailUiState> = _uiState.asStateFlow()

    init {
        loadSkin(skinId)
    }

    private fun loadSkin(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            // TODO: load from repository by id
            _uiState.update {
                it.copy(
                    skin = sampleSkin(id),
                    isLoading = false
                )
            }
        }
    }

    fun getSkinId(): String = skinId

    private fun sampleSkin(id: String): Skin = Skin(
        id = id,
        name = "АК-47 | Красная линия",
        imageUrl = null,
        price = 40_000.0,
        isFavorite = false,
        floatValue = 0.12345,
        special = SkinSpecial.STATTRAK,
        patternIndex = 661,
        stickerIds = emptyList(),
        stickerNames = listOf("Sticker 1", "Foil"),
        hasKeychain = true,
        keychainNames = listOf("Брелок"),
        rarity = SkinRarity.CLASSIFIED,
        collection = "The Phoenix Collection",
        wear = SkinWear.FT
    )
}
