package ru.kotlix.skinshowcase.settings

import android.content.Context
import android.content.SharedPreferences

/**
 * Хранение настроек конфиденциальности (видимость профиля).
 */
object PrivacyPreferences {

    private const val PREFS_NAME = "privacy_prefs"
    private const val KEY_SHOW_PROFILE = "show_profile"

    @Volatile
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun getShowProfile(): Boolean = prefs?.getBoolean(KEY_SHOW_PROFILE, true) ?: true

    fun setShowProfile(value: Boolean) {
        prefs?.edit()?.putBoolean(KEY_SHOW_PROFILE, value)?.apply()
    }
}
