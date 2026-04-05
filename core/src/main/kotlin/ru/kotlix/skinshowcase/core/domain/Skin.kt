package ru.kotlix.skinshowcase.core.domain

/**
 * Строка доп. сведений с инвентарной карточки (extraAttributes и т.п.).
 */
data class SkinExtraInfoLine(val label: String, val value: String)

/**
 * Domain model for a skin (shared between API and DB).
 * Опциональные поля CS2 используются для фильтрации.
 */
data class Skin(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val price: Double? = null,
    val floatValue: Double? = null,
    val special: SkinSpecial? = null,
    val patternIndex: Int? = null,
    val stickerIds: List<String> = emptyList(),
    val stickerNames: List<String> = emptyList(),
    val hasKeychain: Boolean = false,
    val keychainNames: List<String> = emptyList(),
    val rarity: SkinRarity? = null,
    val collection: String? = null,
    val wear: SkinWear? = null,
    /** Владелец набора в ленте обменов (для GET trade-link при открытии карточки). */
    val offerOwnerSteamId: String? = null,
    /** Экземпляр из Steam-инвентаря (assetId), если строка пришла с GET /api/v1/inventory. */
    val inventoryAssetId: String? = null,
    /** Steam `type`, напр. «Consumer Grade Shotgun», «Base Grade Container». */
    val steamItemType: String? = null,
    /** `marketHashName` из инвентаря. */
    val marketHashName: String? = null,
    /** `inspectLink` (steam://…). */
    val inspectLink: String? = null,
    /** Количество стека в инвентаре. */
    val amount: Int? = null,
    /**
     * Кейс / контейнер: на экране деталей скрываем float, износ, паттерн, стикеры и т.д.
     */
    val isContainerLikeItem: Boolean = false,
    /** Текст из `extraAttributes.description`. */
    val itemDescription: String? = null,
    /** Отфильтрованные пары для блока «дополнительно». */
    val extraInfoLines: List<SkinExtraInfoLine> = emptyList()
)
