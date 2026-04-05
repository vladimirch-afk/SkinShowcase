package ru.kotlix.skinshowcase.screens.tradelink

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.core.network.RetrofitProvider
import ru.kotlix.skinshowcase.core.network.bestApiMessage
import ru.kotlix.skinshowcase.core.network.auth.AuthApiService
import ru.kotlix.skinshowcase.core.network.auth.UpdateTradeLinkRequestDto
import ru.kotlix.skinshowcase.settings.TradeLinkPreferences

class TradeLinkViewModel : BaseViewModel<TradeLinkUiState>() {

    override fun initialState(): TradeLinkUiState {
        val current = TradeLinkPreferences.getTradeLink()
        return TradeLinkUiState(
            currentLink = current,
            draft = current ?: ""
        )
    }

    fun updateDraft(value: String) {
        updateState { it.copy(draft = value, errorMessage = null) }
    }

    fun save() {
        launch {
            val link = uiState.value.draft.trim()
            updateState { it.copy(isSaving = true, errorMessage = null, isSaved = false) }
            runCatching {
                withContext(Dispatchers.IO) {
                    RetrofitProvider.create(AuthApiService::class.java).patchTradeLink(
                        UpdateTradeLinkRequestDto(if (link.isEmpty()) null else link)
                    )
                }
            }.fold(
                onSuccess = { me ->
                    val saved = me.steamTradeLink?.trim()?.takeIf { it.isNotEmpty() }
                    TradeLinkPreferences.setTradeLink(saved)
                    updateState {
                        TradeLinkUiState(
                            currentLink = saved,
                            draft = saved ?: "",
                            isSaved = true,
                            isSaving = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { e ->
                    updateState {
                        it.copy(isSaving = false, isSaved = false, errorMessage = e.bestApiMessage())
                    }
                }
            )
        }
    }

    fun delete() {
        launch {
            updateState { it.copy(isSaving = true, errorMessage = null, isSaved = false) }
            runCatching {
                withContext(Dispatchers.IO) {
                    RetrofitProvider.create(AuthApiService::class.java).patchTradeLink(UpdateTradeLinkRequestDto(null))
                }
            }.fold(
                onSuccess = {
                    TradeLinkPreferences.setTradeLink(null)
                    updateState {
                        TradeLinkUiState(
                            currentLink = null,
                            draft = "",
                            isSaved = true,
                            isSaving = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { e ->
                    updateState {
                        it.copy(isSaving = false, isSaved = false, errorMessage = e.bestApiMessage())
                    }
                }
            )
        }
    }
}
