package ru.kotlix.skinshowcase.message.domain

/**
 * Элемент списка чатов (ник, Steam ID, последнее сообщение, аватар).
 */
data class ChatItem(
    val id: String,
    val nickname: String,
    val lastMessage: String,
    val lastMessageTimeMillis: Long,
    val unreadCount: Int = 0,
    val avatarUrl: String? = null
)
