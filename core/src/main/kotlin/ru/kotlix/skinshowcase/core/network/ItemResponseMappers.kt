package ru.kotlix.skinshowcase.core.network

/**
 * Маппинг ответа items API (ItemResponseDto) в SkinDto для кэша и домена.
 */
fun ItemResponseDto.toSkinDto(): SkinDto =
    SkinDto(
        id = itemId,
        name = name,
        imageUrl = null,
        price = minPriceUsd,
        floatValue = null,
        stickerNames = null,
        collection = null,
        rarity = null,
        wear = null,
        special = null,
        patternIndex = null,
        keychainNames = null
    )
