package ru.kotlix.skinshowcase.core.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    private const val DB_NAME = "skins_showcase.db"

    @Volatile
    private var instance: AppDatabase? = null

    fun create(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DB_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}
