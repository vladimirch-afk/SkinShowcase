package ru.kotlix.skinshowcase.core.network.inventory

import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.domain.SkinExtraInfoLine
import ru.kotlix.skinshowcase.core.domain.SkinRarity
import ru.kotlix.skinshowcase.core.domain.SkinSpecial
import ru.kotlix.skinshowcase.core.domain.SkinWear
import ru.kotlix.skinshowcase.core.domain.mapper.toDomain as dtoToDomain
import ru.kotlix.skinshowcase.core.network.SkinDto
import java.util.Locale

fun InventoryItemDetailResponseDto.toSkinDto(): SkinDto? {
    val inv = item ?: return null
    val resolvedId = inv.classId?.trim()?.takeIf { it.isNotEmpty() }
        ?: catalogPrice?.itemId?.trim()?.takeIf { it.isNotEmpty() }
        ?: return null
    val title = inv.fullItemName?.trim()?.takeIf { it.isNotEmpty() }
        ?: inv.name?.trim()?.takeIf { it.isNotEmpty() }
        ?: catalogPrice?.name?.trim()?.takeIf { it.isNotEmpty() }
        ?: inv.marketHashName?.trim()?.takeIf { it.isNotEmpty() }
        ?: "Предмет"
    val imageUrl = steamEconomyImageUrl(inv.iconUrl)
    val stickerNames = inv.stickers.orEmpty()
        .mapNotNull { s -> s.name?.trim()?.takeIf { it.isNotEmpty() } }
    val patternIndex = inv.pattern ?: inv.paintSeed
    val rarityStr = mapRarityForSkinDto(inv.rarityName)
        ?: inferRarityFromSteamType(inv.type)?.name
    return SkinDto(
        id = resolvedId,
        name = title,
        imageUrl = imageUrl,
        price = catalogPrice?.minPriceUsd,
        floatValue = inv.floatValue,
        stickerNames = stickerNames.takeIf { it.isNotEmpty() },
        collection = inv.collectionName?.trim()?.takeIf { it.isNotEmpty() },
        rarity = rarityStr,
        wear = mapWearForSkinDto(inv.wearName),
        special = mapSpecialForSkinDto(inv.qualityName),
        patternIndex = patternIndex,
        keychainNames = null
    )
}

fun InventoryItemDetailResponseDto.toSkin(offerOwnerSteamId: String?): Skin? {
    val inv = item ?: return null
    val dto = toSkinDto() ?: return null
    val base = dto.dtoToDomain()
    val asset = inv.assetId?.trim()?.takeIf { it.isNotEmpty() }
    val containerLike = isContainerLikeSteamItem(inv.type, inv.marketHashName, inv.name)
    val descAndExtras = buildDescriptionAndExtraLines(inv.extraAttributes)
    return base.copy(
        inventoryAssetId = asset,
        offerOwnerSteamId = offerOwnerSteamId,
        steamItemType = inv.type?.trim()?.takeIf { it.isNotEmpty() },
        marketHashName = inv.marketHashName?.trim()?.takeIf { it.isNotEmpty() },
        inspectLink = inv.inspectLink?.trim()?.takeIf { it.isNotEmpty() },
        amount = inv.amount?.takeIf { it > 0 },
        isContainerLikeItem = containerLike,
        itemDescription = descAndExtras.first,
        extraInfoLines = descAndExtras.second,
        rarity = base.rarity ?: inferRarityFromSteamType(inv.type)
    )
}

private fun isContainerLikeSteamItem(
    type: String?,
    marketHashName: String?,
    name: String?
): Boolean {
    val t = type?.lowercase(Locale.ROOT).orEmpty()
    if (t.contains("container")) return true
    val m = marketHashName?.lowercase(Locale.ROOT).orEmpty()
    if (m.endsWith(" case")) return true
    val n = name?.lowercase(Locale.ROOT).orEmpty()
    if (n.endsWith(" case")) return true
    return false
}

private fun buildDescriptionAndExtraLines(
    raw: Map<String, String>?
): Pair<String?, List<SkinExtraInfoLine>> {
    if (raw.isNullOrEmpty()) return Pair(null, emptyList())
    val desc = raw["description"]?.trim()?.takeIf { it.isNotEmpty() }
    val lines = mutableListOf<SkinExtraInfoLine>()
    for ((k, v) in raw) {
        val valTrim = v.trim()
        if (valTrim.isEmpty()) continue
        if (k == "description") continue
        if (k == "blank" || k == "attribute") continue
        when {
            k == "itemset_name" -> lines.add(SkinExtraInfoLine("Набор", valTrim))
            k.startsWith("attr: ") -> {
                val label = k.removePrefix("attr: ").trim().humanizeAttrLabel()
                lines.add(SkinExtraInfoLine(label, valTrim))
            }
            else -> {
                val label = k.replace("_", " ").humanizeAttrLabel()
                lines.add(SkinExtraInfoLine(label, valTrim))
            }
        }
    }
    return Pair(desc, lines.sortedBy { it.label })
}

private fun String.humanizeAttrLabel(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

private fun inferRarityFromSteamType(type: String?): SkinRarity? {
    if (type.isNullOrBlank()) return null
    val t = type.trim()
    for (e in SkinRarity.values()) {
        if (t.startsWith(e.displayName, ignoreCase = true)) return e
    }
    if (t.startsWith("Mil-Spec", ignoreCase = true)) return SkinRarity.MIL_SPEC
    if (t.startsWith("Mil Spec", ignoreCase = true)) return SkinRarity.MIL_SPEC
    return null
}

private fun mapRarityForSkinDto(rarityName: String?): String? {
    if (rarityName.isNullOrBlank()) return null
    val t = rarityName.trim()
    for (e in SkinRarity.values()) {
        if (e.displayName.equals(t, ignoreCase = true)) return e.name
    }
    val compact = t.uppercase().replace(" ", "_").replace("-", "_")
    runCatching { return SkinRarity.valueOf(compact).name }
    return null
}

private fun mapWearForSkinDto(wearName: String?): String? {
    if (wearName.isNullOrBlank()) return null
    val t = wearName.trim()
    for (e in SkinWear.values()) {
        if (e.displayName.equals(t, ignoreCase = true)) return e.name
    }
    val compact = t.uppercase().replace(" ", "_").replace("-", "_")
    runCatching { return SkinWear.valueOf(compact).name }
    return null
}

private fun mapSpecialForSkinDto(qualityName: String?): String? {
    if (qualityName.isNullOrBlank()) return null
    val t = qualityName.trim()
    if (t.equals("Normal", ignoreCase = true)) return null
    if (t.equals("Unique", ignoreCase = true)) return null
    for (e in SkinSpecial.values()) {
        if (e.displayName.equals(t, ignoreCase = true)) return e.name
    }
    if (t.contains("StatTrak", ignoreCase = true)) return SkinSpecial.STATTRAK.name
    if (t.contains("Souvenir", ignoreCase = true)) return SkinSpecial.SOUVENIR.name
    return null
}
