package ru.kotlix.skinshowcase.screens.tradelink

import ru.kotlix.skinshowcase.core.BaseViewModel
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
        updateState { it.copy(draft = value) }
    }

    fun save() {
        updateState { state ->
            val link = state.draft.trim()
            TradeLinkPreferences.setTradeLink(if (link.isEmpty()) null else link)
            val newCurrent = TradeLinkPreferences.getTradeLink()
            state.copy(currentLink = newCurrent, draft = newCurrent ?: "", isSaved = true)
        }
    }

    fun delete() {
        TradeLinkPreferences.setTradeLink(null)
        updateState { it.copy(currentLink = null, draft = "", isSaved = true) }
    }
}
