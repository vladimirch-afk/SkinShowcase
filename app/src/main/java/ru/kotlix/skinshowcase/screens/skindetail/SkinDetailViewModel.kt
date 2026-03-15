package ru.kotlix.skinshowcase.screens.skindetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.kotlix.skinshowcase.core.network.SkinsProvider
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
        if (id.isBlank()) {
            _uiState.update {
                it.copy(isLoading = false, errorMessage = "Не указан id скина")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                SkinsProvider.repository.getSkinByIdFromApi(id)
            }.fold(
                onSuccess = { skin ->
                    _uiState.update {
                        it.copy(
                            skin = skin,
                            isLoading = false,
                            errorMessage = if (skin == null) "Скин не найден" else null
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            skin = null,
                            isLoading = false,
                            errorMessage = e.message ?: "Ошибка загрузки"
                        )
                    }
                }
            )
        }
    }

    fun getSkinId(): String = skinId

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // --- данные-заглушки (отключены, загрузка через api-gateway) ---
    // private fun sampleSkin(id: String): Skin = Skin(...)
}
