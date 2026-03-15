package ru.kotlix.skinshowcase.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.kotlix.skinshowcase.core.database.entity.FavoriteSkinEntity

@Dao
interface FavoriteSkinDao {

    @Query("SELECT * FROM favorite_skins ORDER BY addedAtMillis DESC")
    fun observeAll(): Flow<List<FavoriteSkinEntity>>

    @Query("SELECT id FROM favorite_skins")
    suspend fun getAllIds(): List<String>

    @Query("SELECT * FROM favorite_skins WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): FavoriteSkinEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteSkinEntity)

    @Query("DELETE FROM favorite_skins WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM favorite_skins")
    suspend fun deleteAll()
}
