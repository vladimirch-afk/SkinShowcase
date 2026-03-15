package ru.kotlix.skinshowcase.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.kotlix.skinshowcase.core.database.entity.CachedChatEntity

@Dao
interface ChatCacheDao {

    @Query("SELECT * FROM cached_chats ORDER BY lastMessageAt DESC")
    suspend fun getAll(): List<CachedChatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CachedChatEntity>)

    @Query("DELETE FROM cached_chats")
    suspend fun deleteAll()

    @Query("DELETE FROM cached_chats WHERE counterpartySteamId = :chatId")
    suspend fun deleteByChatId(chatId: String)
}
