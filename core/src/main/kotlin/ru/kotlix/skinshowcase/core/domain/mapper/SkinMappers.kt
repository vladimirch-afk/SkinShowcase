package ru.kotlix.skinshowcase.core.domain.mapper

import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.domain.SkinRarity
import ru.kotlix.skinshowcase.core.domain.SkinSpecial
import ru.kotlix.skinshowcase.core.domain.SkinWear
import ru.kotlix.skinshowcase.core.network.SkinDto

fun SkinDto.toDomain(): Skin =
    Skin(
        id = id,
        name = name,
        imageUrl = imageUrl,
        price = price,
        floatValue = floatValue,
        stickerNames = stickerNames.orEmpty(),
        collection = collection,
        rarity = parseRarity(rarity),
        wear = parseWear(wear),
        special = parseSpecial(special),
        patternIndex = patternIndex,
        hasKeychain = !keychainNames.isNullOrEmpty(),
        keychainNames = keychainNames.orEmpty()
    )

private fun parseRarity(s: String?): SkinRarity? =
    if (s.isNullOrBlank()) null else runCatching { SkinRarity.valueOf(s.trim()) }.getOrNull()

private fun parseWear(s: String?): SkinWear? =
    if (s.isNullOrBlank()) null else runCatching { SkinWear.valueOf(s.trim()) }.getOrNull()

private fun parseSpecial(s: String?): SkinSpecial? =
    if (s.isNullOrBlank()) null else runCatching { SkinSpecial.valueOf(s.trim()) }.getOrNull()
