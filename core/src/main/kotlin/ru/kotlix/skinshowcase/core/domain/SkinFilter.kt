package ru.kotlix.skinshowcase.core.domain

/**
 * Параметры фильтрации скинов (CS2).
 * Все поля опциональны: null/empty = критерий не применяется.
 */
data class SkinFilter(
    val priceMin: Double? = null,
    val priceMax: Double? = null,
    val floatMin: Double? = null,
    val floatMax: Double? = null,
    val nameContains: String = "",
    val nameExcludes: List<String> = emptyList(),
    val specials: Set<SkinSpecial> = emptySet(),
    val patternIndices: List<Int> = emptyList(),
    val requiredStickerNames: List<String> = emptyList(),
    val requireKeychain: Boolean? = null,
    val keychainNames: List<String> = emptyList(),
    val rarities: Set<SkinRarity> = emptySet(),
    val collections: List<String> = emptyList(),
    val wears: Set<SkinWear> = emptySet()
) {
    fun isEmpty(): Boolean =
        priceMin == null && priceMax == null &&
            floatMin == null && floatMax == null &&
            nameContains.isBlank() && nameExcludes.isEmpty() &&
            specials.isEmpty() && patternIndices.isEmpty() &&
            requiredStickerNames.isEmpty() && requireKeychain == null &&
            keychainNames.isEmpty() && rarities.isEmpty() &&
            collections.isEmpty() && wears.isEmpty()
}
