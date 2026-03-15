package ru.kotlix.skinshowcase.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Кэш скина из API для офлайн-доступа и быстрого отображения.
 */
@Entity(tableName = "cached_skins")
data class CachedSkinEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String?,
    val price: Double?,
    val floatValue: Double?,
    val stickerNamesJson: String?,
    val collection: String?,
    val rarity: String?,
    val wear: String?,
    val special: String?,
    val patternIndex: Int?,
    val keychainNamesJson: String?,
    val orderIndex: Int
)
