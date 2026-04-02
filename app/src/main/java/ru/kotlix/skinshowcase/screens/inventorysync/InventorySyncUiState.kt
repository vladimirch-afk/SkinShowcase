package ru.kotlix.skinshowcase.screens.inventorysync

data class InventorySyncUiState(
    val isLoading: Boolean = false,
    val itemCount: Int? = null,
    val errorMessage: String? = null
)
