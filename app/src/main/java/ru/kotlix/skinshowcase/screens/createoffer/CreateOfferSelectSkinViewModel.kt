package ru.kotlix.skinshowcase.screens.createoffer

import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.network.SkinsProvider

data class CreateOfferSelectSkinUiState(
    val skins: List<Skin> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class CreateOfferSelectSkinViewModel : BaseViewModel<CreateOfferSelectSkinUiState>() {

    override fun initialState(): CreateOfferSelectSkinUiState =
        CreateOfferSelectSkinUiState(skins = emptyList(), isLoading = true)

    fun loadSkins() {
        launch {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                SkinsProvider.repository.getSkinsFromApi()
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
}
