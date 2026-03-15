package ru.kotlix.skinshowcase.screens.dealhistory

import ru.kotlix.skinshowcase.screens.profile.DealSummary

data class DealHistoryUiState(
    val deals: List<DealSummary> = emptyList(),
    val isLoading: Boolean = true
)
