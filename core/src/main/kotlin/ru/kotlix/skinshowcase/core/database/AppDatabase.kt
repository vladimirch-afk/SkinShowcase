package ru.kotlix.skinshowcase.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.kotlix.skinshowcase.core.database.dao.FavoriteSkinDao
import ru.kotlix.skinshowcase.core.database.entity.FavoriteSkinEntity

@Database(
    entities = [FavoriteSkinEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteSkinDao(): FavoriteSkinDao
}
