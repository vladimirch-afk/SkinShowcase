package ru.kotlix.skinshowcase.core.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    private const val DB_NAME = "skins_showcase.db"

    fun create(context: Context): AppDatabase =
        Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
            .fallbackToDestructiveMigration()
            .build()
}
