package ru.kotlix.skinshowcase.screens.profile

/**
 * Краткое описание оффера для отображения в профиле.
 */
data class OfferSummary(
    val id: String,
    val skinName: String,
    val skinImageUrl: String? = null,
    val priceRub: Double? = null
)

/**
 * Краткое описание сделки для отображения в профиле.
 */
data class DealSummary(
    val id: String,
    val summary: String,
    val counterpartName: String? = null
)
