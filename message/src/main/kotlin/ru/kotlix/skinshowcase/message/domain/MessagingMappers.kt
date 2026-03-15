package ru.kotlix.skinshowcase.message.domain

import ru.kotlix.skinshowcase.core.network.messaging.ChatDto
import ru.kotlix.skinshowcase.core.network.messaging.MessageDto

fun ChatDto.toChatItem(): ChatItem = ChatItem(
    id = counterpartySteamId,
    nickname = counterpartyNickname?.takeIf { it.isNotBlank() } ?: counterpartySteamId,
    lastMessage = lastMessagePreview ?: "",
    lastMessageTimeMillis = parseLastMessageAtToMillis(lastMessageAt),
    unreadCount = 0,
    avatarUrl = avatarUrl
)

private fun parseLastMessageAtToMillis(iso8601: String?): Long {
    if (iso8601 == null) return 0L
    return try {
        java.time.Instant.parse(iso8601).toEpochMilli()
    } catch (_: Exception) {
        0L
    }
}

fun MessageDto.toMessageItem(): MessageItem = MessageItem(
    id = id,
    text = text,
    isOutgoing = isOutgoing,
    timeMillis = timeMillis
)
