package ru.kotlix.skinshowcase.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.kotlix.skinshowcase.core.database.entity.CachedMessageEntity

@Dao
interface MessageCacheDao {

    @Query("SELECT * FROM cached_messages WHERE chatId = :chatId ORDER BY timeMillis ASC")
    suspend fun getByChatId(chatId: String): List<CachedMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CachedMessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CachedMessageEntity)

    @Query("DELETE FROM cached_messages WHERE chatId = :chatId")
    suspend fun deleteByChatId(chatId: String)

    @Query("DELETE FROM cached_messages WHERE chatId = :chatId AND id = :messageId")
    suspend fun deleteMessage(chatId: String, messageId: String)
}
