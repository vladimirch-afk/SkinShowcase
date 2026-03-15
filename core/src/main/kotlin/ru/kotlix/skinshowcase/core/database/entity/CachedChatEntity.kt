package ru.kotlix.skinshowcase.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Кэш чата из API для офлайн-доступа.
 */
@Entity(tableName = "cached_chats")
data class CachedChatEntity(
    @PrimaryKey val counterpartySteamId: String,
    val counterpartyNickname: String?,
    val lastMessagePreview: String?,
    val lastMessageAt: String?,
    val avatarUrl: String?
)
