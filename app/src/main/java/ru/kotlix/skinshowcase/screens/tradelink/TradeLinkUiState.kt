package ru.kotlix.skinshowcase.screens.tradelink

data class TradeLinkUiState(
    val currentLink: String? = null,
    val draft: String = "",
    val isSaved: Boolean = false
)
