package ru.kotlix.skinshowcase.core.network.trades

import ru.kotlix.skinshowcase.core.domain.Skin

/**
 * Карточка ленты обменов: первый classId как id для GET /api/v1/items/{id}, владелец — для trade-link.
 */
fun TradeSelectionDto.toFeedSkin(): Skin? {
    val owner = steamId?.trim().orEmpty()
    if (owner.length != 17 || !owner.all { it.isDigit() }) return null
    val itemsList = items.orEmpty()
    val firstClass = itemsList.firstNotNullOfOrNull { it.classId?.trim()?.takeIf { c -> c.isNotEmpty() } }
    val skinId = firstClass ?: "feed-${id ?: 0}-$owner"
    val count = itemsList.size
    val short = if (owner.length >= 4) owner.takeLast(4) else owner
    val title = if (count > 0) "Обмен · $count предм. · …$short" else "Обмен · …$short"
    return Skin(
        id = skinId,
        name = title,
        imageUrl = null,
        price = null,
        offerOwnerSteamId = owner
    )
}
