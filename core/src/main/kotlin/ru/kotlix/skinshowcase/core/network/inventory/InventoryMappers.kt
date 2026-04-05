package ru.kotlix.skinshowcase.core.network.inventory

import ru.kotlix.skinshowcase.core.domain.Skin

private const val STEAM_ECONOMY_IMAGE_BASE = "https://community.cloudflare.steamstatic.com/economy/image/"

fun steamEconomyImageUrl(iconUrl: String?): String? {
    val raw = iconUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    if (raw.startsWith("http://", ignoreCase = true) || raw.startsWith("https://", ignoreCase = true)) {
        return raw
    }
    val normalized = raw.trimStart('/')
    return STEAM_ECONOMY_IMAGE_BASE + normalized
}

/**
 * id — [InventoryItemDto.classId] для дальнейшего GET /api/v1/items/{id}; при отсутствии — assetId или запасной ключ.
 */
fun InventoryItemDto.toInventorySkin(index: Int): Skin {
    val asset = assetId?.trim()?.takeIf { it.isNotEmpty() }.orEmpty()
    val clazz = classId?.trim()?.takeIf { it.isNotEmpty() }.orEmpty()
    val resolvedId = clazz.ifEmpty { asset.ifEmpty { "inv-$index" } }
    val title = name?.trim()?.takeIf { it.isNotEmpty() }
        ?: marketHashName?.trim()?.takeIf { it.isNotEmpty() }
        ?: "Предмет"
    return Skin(
        id = resolvedId,
        name = title,
        imageUrl = steamEconomyImageUrl(iconUrl),
        price = catalogMinPriceUsd,
        floatValue = floatValue,
        inventoryAssetId = asset.ifEmpty { null }
    )
}
