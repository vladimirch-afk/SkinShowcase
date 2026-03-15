package ru.kotlix.skinshowcase.message.domain

/**
 * Элемент списка чатов (заголовок, последнее сообщение, время).
 */
data class ChatItem(
    val id: String,
    val title: String,
    val lastMessage: String,
    val lastMessageTimeMillis: Long,
    val unreadCount: Int = 0
)
