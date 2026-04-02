package ru.kotlix.skinshowcase.screens.profile

/**
 * Краткое описание оффера для отображения в профиле.
 */
data class OfferSummary(
    val id: String,
    /** classId или запасной ключ для навигации в деталку. */
    val skinId: String,
    val skinName: String,
    val skinImageUrl: String? = null,
    val priceRub: Double? = null,
    /** Для DELETE …/selection/{steamId}/items */
    val assetId: String? = null,
    val classId: String? = null
)

/**
 * Краткое описание сделки для отображения в профиле.
 */
data class DealSummary(
    val id: String,
    val summary: String,
    val counterpartName: String? = null
)

/**
 * Данные продавца оффера (для чужого оффера).
 */
data class SellerInfo(
    val nickname: String,
    val steamId: String,
    val tradeLink: String? = null
)
