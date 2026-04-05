package ru.kotlix.skinshowcase.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.kotlix.skinshowcase.core.database.dao.ChatCacheDao
import ru.kotlix.skinshowcase.core.database.dao.MessageCacheDao
import ru.kotlix.skinshowcase.core.database.dao.SkinCacheDao
import ru.kotlix.skinshowcase.core.database.entity.CachedChatEntity
import ru.kotlix.skinshowcase.core.database.entity.CachedMessageEntity
import ru.kotlix.skinshowcase.core.database.entity.CachedSkinEntity

@Database(
    entities = [
        CachedSkinEntity::class,
        CachedChatEntity::class,
        CachedMessageEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun skinCacheDao(): SkinCacheDao
    abstract fun chatCacheDao(): ChatCacheDao
    abstract fun messageCacheDao(): MessageCacheDao
}
