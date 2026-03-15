package ru.kotlix.skinshowcase.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_skins")
data class FavoriteSkinEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String?,
    val price: Double?,
    val addedAtMillis: Long
)
