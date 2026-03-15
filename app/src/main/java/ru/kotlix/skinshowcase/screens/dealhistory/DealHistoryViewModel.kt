package ru.kotlix.skinshowcase.screens.dealhistory

import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.data.ProfileDataProvider
import ru.kotlix.skinshowcase.screens.profile.DealSummary

class DealHistoryViewModel : BaseViewModel<DealHistoryUiState>() {

    override fun initialState(): DealHistoryUiState = DealHistoryUiState(isLoading = true)

    init {
        loadDeals()
    }

    fun loadDeals() {
        launch {
            updateState { it.copy(isLoading = true) }
            try {
                val profileState = ProfileDataProvider.getProfileState()
                updateState {
                    it.copy(
                        deals = profileState.dealHistory,
                        isLoading = false
                    )
                }
            } catch (_: Exception) {
                updateState { it.copy(deals = emptyList(), isLoading = false) }
            }
        }
    }
}
