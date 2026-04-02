package ru.kotlix.skinshowcase.core.network.messaging

import ru.kotlix.skinshowcase.core.network.auth.CurrentUser

/**
 * Преобразование MessageResponseDto (ответ сервера) в MessageDto (для UI и кэша).
 * isOutgoing вычисляется по CurrentUser.steamId.
 */
fun MessageResponseDto.toMessageDto(): MessageDto {
    val mySteamId = CurrentUser.steamId
    val isOutgoing = mySteamId != null && senderSteamId == mySteamId
    val timeMillis = parseCreatedAtToMillis(createdAt)
    return MessageDto(
        id = id,
        text = text,
        isOutgoing = isOutgoing,
        timeMillis = timeMillis
    )
}

private fun parseCreatedAtToMillis(createdAt: String?): Long {
    if (createdAt.isNullOrBlank()) return 0L
    return try {
        java.time.Instant.parse(createdAt).toEpochMilli()
    } catch (_: Exception) {
        0L
    }
}
