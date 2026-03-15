package ru.kotlix.skinshowcase.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Кэш сообщения из API для офлайн-доступа.
 */
@Entity(
    tableName = "cached_messages",
    primaryKeys = ["chatId", "id"],
    indices = [Index("chatId")]
)
data class CachedMessageEntity(
    val chatId: String,
    val id: String,
    val text: String,
    val isOutgoing: Boolean,
    val timeMillis: Long
)
