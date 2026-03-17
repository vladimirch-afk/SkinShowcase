package ru.kotlix.skinshowcase.settings

import android.content.Context
import android.content.SharedPreferences
import ru.kotlix.skinshowcase.core.network.DebugAuthToken

/**
 * Хранение JWT для запросов к API (чаты, профиль). Только для отладки/разработки.
 * При сохранении обновляет [DebugAuthToken.token].
 */
object AuthTokenPreferences {

    private const val PREFS_NAME = "auth_token_prefs"
    private const val KEY_API_TOKEN = "api_token"

    @Volatile
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun getToken(): String? {
        val raw = prefs?.getString(KEY_API_TOKEN, null) ?: return null
        val trimmed = raw.trim()
        return if (trimmed.isEmpty()) null else trimmed
    }

    fun setToken(value: String?) {
        val trimmed = value?.trim()?.takeIf { it.isNotEmpty() }
        prefs?.edit()?.putString(KEY_API_TOKEN, trimmed)?.apply()
        DebugAuthToken.token = trimmed
    }
}
