package ru.kotlix.skinshowcase.core.domain

/**
 * Domain model for a skin (shared between API and DB).
 * Опциональные поля CS2 используются для фильтрации.
 */
data class Skin(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val price: Double? = null,
    val isFavorite: Boolean = false,
    val floatValue: Double? = null,
    val special: SkinSpecial? = null,
    val patternIndex: Int? = null,
    val stickerIds: List<String> = emptyList(),
    val stickerNames: List<String> = emptyList(),
    val hasKeychain: Boolean = false,
    val keychainNames: List<String> = emptyList(),
    val rarity: SkinRarity? = null,
    val collection: String? = null,
    val wear: SkinWear? = null
)
