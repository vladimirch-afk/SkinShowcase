package ru.kotlix.skinshowcase.core.database.mapper

import ru.kotlix.skinshowcase.core.database.entity.CachedChatEntity
import ru.kotlix.skinshowcase.core.database.entity.CachedMessageEntity
import ru.kotlix.skinshowcase.core.database.entity.CachedSkinEntity
import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.network.SkinDto
import ru.kotlix.skinshowcase.core.network.messaging.ChatDto
import ru.kotlix.skinshowcase.core.network.messaging.MessageDto

fun SkinDto.toCachedSkinEntity(orderIndex: Int): CachedSkinEntity =
    CachedSkinEntity(
        id = id,
        name = name,
        imageUrl = imageUrl,
        price = price,
        floatValue = floatValue,
        stickerNamesJson = listToJson(stickerNames),
        collection = collection,
        rarity = rarity,
        wear = wear,
        special = special,
        patternIndex = patternIndex,
        keychainNamesJson = listToJson(keychainNames),
        orderIndex = orderIndex
    )

fun Skin.toCachedSkinEntity(orderIndex: Int): CachedSkinEntity =
    CachedSkinEntity(
        id = id,
        name = name,
        imageUrl = imageUrl,
        price = price,
        floatValue = floatValue,
        stickerNamesJson = listToJson(stickerNames.takeIf { it.isNotEmpty() }),
        collection = collection,
        rarity = rarity?.name,
        wear = wear?.name,
        special = special?.name,
        patternIndex = patternIndex,
        keychainNamesJson = listToJson(keychainNames.takeIf { it.isNotEmpty() }),
        orderIndex = orderIndex
    )

fun CachedSkinEntity.toSkinDto(): SkinDto =
    SkinDto(
        id = id,
        name = name,
        imageUrl = imageUrl,
        price = price,
        floatValue = floatValue,
        stickerNames = jsonToStringList(stickerNamesJson),
        collection = collection,
        rarity = rarity,
        wear = wear,
        special = special,
        patternIndex = patternIndex,
        keychainNames = jsonToStringList(keychainNamesJson)
    )

fun ChatDto.toCachedChatEntity(): CachedChatEntity =
    CachedChatEntity(
        counterpartySteamId = counterpartySteamId,
        counterpartyNickname = counterpartyNickname,
        lastMessagePreview = lastMessagePreview,
        lastMessageAt = lastMessageAt,
        avatarUrl = avatarUrl
    )

fun CachedChatEntity.toChatDto(): ChatDto =
    ChatDto(
        counterpartySteamId = counterpartySteamId,
        counterpartyNickname = counterpartyNickname,
        lastMessagePreview = lastMessagePreview,
        lastMessageAt = lastMessageAt,
        avatarUrl = avatarUrl
    )

fun MessageDto.toCachedMessageEntity(chatId: String): CachedMessageEntity =
    CachedMessageEntity(
        chatId = chatId,
        id = id,
        text = text,
        isOutgoing = isOutgoing,
        timeMillis = timeMillis
    )

fun CachedMessageEntity.toMessageDto(): MessageDto =
    MessageDto(
        id = id,
        text = text,
        isOutgoing = isOutgoing,
        timeMillis = timeMillis
    )

private fun listToJson(list: List<String>?): String? {
    if (list == null) return null
    return com.google.gson.Gson().toJson(list)
}

private fun jsonToStringList(json: String?): List<String>? {
    if (json.isNullOrBlank()) return null
    val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
    return com.google.gson.Gson().fromJson(json, type)
}
