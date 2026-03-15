package ru.kotlix.skinshowcase.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel with common [UiState] handling.
 * Feature modules can extend and expose [uiState].
 */
abstract class BaseViewModel<State : Any> : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    protected abstract fun initialState(): State

    protected fun updateState(block: (State) -> State) {
        _uiState.value = block(_uiState.value)
    }

    protected fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
