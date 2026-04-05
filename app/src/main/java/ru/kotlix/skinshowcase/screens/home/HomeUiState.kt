package ru.kotlix.skinshowcase.screens.home

import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.domain.SkinFilter
import ru.kotlix.skinshowcase.core.network.auth.AvatarUrls

data class HomeUiState(
    val searchQuery: String = "",
    val skins: List<Skin> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val filter: SkinFilter = SkinFilter(),
    val filterSheetVisible: Boolean = false,
    val sortOption: SortOption = SortOption.DEFAULT,
    /** true — главная грузит GET /api/v1/trades/feed; фильтр/сортировка применяются на клиенте к полученному списку. */
    val tradeFeedMode: Boolean = true,
    /** Пресетная аватарка текущего пользователя (URL GET /auth/avatars/{id}). */
    val userAvatarUrl: String = AvatarUrls.userAvatarUrl(null)
)
