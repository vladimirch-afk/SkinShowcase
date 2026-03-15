package ru.kotlix.skinshowcase.message.domain

/**
 * Сообщение в переписке.
 */
data class MessageItem(
    val id: String,
    val text: String,
    val isOutgoing: Boolean,
    val timeMillis: Long
)
