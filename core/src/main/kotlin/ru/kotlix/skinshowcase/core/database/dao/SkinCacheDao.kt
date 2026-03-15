package ru.kotlix.skinshowcase.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.kotlix.skinshowcase.core.database.entity.CachedSkinEntity

@Dao
interface SkinCacheDao {

    @Query("SELECT * FROM cached_skins ORDER BY orderIndex ASC")
    suspend fun getAll(): List<CachedSkinEntity>

    @Query("SELECT * FROM cached_skins WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CachedSkinEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CachedSkinEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CachedSkinEntity)

    @Query("DELETE FROM cached_skins")
    suspend fun deleteAll()
}
