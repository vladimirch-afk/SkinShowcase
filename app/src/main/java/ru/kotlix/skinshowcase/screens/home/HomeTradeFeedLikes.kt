package ru.kotlix.skinshowcase.screens.home

import ru.kotlix.skinshowcase.core.domain.Skin

internal fun tradeFeedLikeKey(skin: Skin): String =
    listOf(
        skin.offerOwnerSteamId.orEmpty(),
        skin.id,
        skin.inventoryAssetId.orEmpty()
    ).joinToString("|")

internal fun matchesTradeFeedSkinSlot(a: Skin, b: Skin): Boolean {
    if (a.id != b.id) return false
    if (a.offerOwnerSteamId != b.offerOwnerSteamId) return false
    val aid = a.inventoryAssetId?.trim()?.takeIf { it.isNotEmpty() }
    val bid = b.inventoryAssetId?.trim()?.takeIf { it.isNotEmpty() }
    return aid == bid
}
