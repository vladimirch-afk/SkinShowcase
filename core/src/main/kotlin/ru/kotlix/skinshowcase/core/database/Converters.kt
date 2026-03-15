package ru.kotlix.skinshowcase.core.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Конвертеры для Room: List<String> <-> JSON.
 */
class Converters {

    private val gson = Gson()
    private val listType = object : TypeToken<List<String>>() {}.type

    @TypeConverter
    fun stringListToJson(value: List<String>?): String? =
        if (value == null) null else gson.toJson(value)

    @TypeConverter
    fun jsonToStringList(value: String?): List<String>? =
        if (value == null) null else gson.fromJson(value, listType)
}
