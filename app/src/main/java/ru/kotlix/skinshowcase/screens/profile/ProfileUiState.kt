package ru.kotlix.skinshowcase.screens.profile

import ru.kotlix.skinshowcase.core.network.auth.AvatarPresetDto
import ru.kotlix.skinshowcase.core.network.auth.LegalDocumentSummaryDto

data class ProfileUiState(
    val steamAvatarUrl: String? = null,
    /** Прямая ссылка на аватар Steam из API (для превью в диалоге). */
    val steamProfileImageUrl: String? = null,
    /** null — на сервере не задано, в UI используется первая пресетная аватарка. */
    val avatarPresetId: String? = null,
    /** STEAM или PRESET из GET /auth/me. */
    val avatarSource: String? = null,
    val steamNickname: String = "",
    val steamId: String? = null,
    val tradeLink: String? = null,
    val activeOffers: List<OfferSummary> = emptyList(),
    val dealHistory: List<DealSummary> = emptyList(),
    val showProfile: Boolean = true,
    val isRefreshing: Boolean = false,
    /** Ошибка загрузки профиля (в т.ч. HTTP 403 — заблокированный аккаунт). */
    val refreshError: String? = null,
    /** GET /auth/documents; пусто при ошибке — в UI остаются локальные PDF. */
    val legalDocumentsFromApi: List<LegalDocumentSummaryDto> = emptyList(),
    /** GET /auth/avatars — для выбора аватарки. */
    val avatarPresets: List<AvatarPresetDto> = emptyList()
) {
    val firstOffer: OfferSummary? get() = activeOffers.firstOrNull()
    val firstDeal: DealSummary? get() = dealHistory.firstOrNull()
    val hasActiveOffers: Boolean get() = activeOffers.isNotEmpty()
    val hasDeals: Boolean get() = dealHistory.isNotEmpty()
}
