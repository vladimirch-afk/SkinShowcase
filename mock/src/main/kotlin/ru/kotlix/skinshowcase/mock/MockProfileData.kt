package ru.kotlix.skinshowcase.mock

/**
 * Мок-данные профиля и офферов (не зависят от app UI-моделей).
 */
data class MockOfferSummary(
    val id: String,
    val skinId: String,
    val skinName: String,
    val skinImageUrl: String?,
    val priceRub: Double?
)

data class MockDealSummary(
    val id: String,
    val summary: String,
    val counterpartName: String?
)

data class MockProfileData(
    val steamNickname: String,
    val steamAvatarUrl: String?,
    val steamId: String? = null,
    val activeOffers: List<MockOfferSummary>,
    val dealHistory: List<MockDealSummary>
)

/** Данные продавца оффера (для чужого оффера). */
data class MockSellerInfo(
    val nickname: String,
    val steamId: String,
    val tradeLink: String? = null
)
