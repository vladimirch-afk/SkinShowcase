package ru.kotlix.skinshowcase.settings

import android.content.Context
import android.content.SharedPreferences

/**
 * Хранение ссылки для обмена (trade link) на устройстве.
 */
object TradeLinkPreferences {

    private const val PREFS_NAME = "trade_link_prefs"
    private const val KEY_TRADE_LINK = "trade_link"

    @Volatile
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun getTradeLink(): String? {
        val raw = prefs?.getString(KEY_TRADE_LINK, null) ?: return null
        val trimmed = raw.trim()
        return if (trimmed.isEmpty()) null else trimmed
    }

    fun setTradeLink(value: String?) {
        prefs?.edit()?.putString(KEY_TRADE_LINK, value?.trim()?.takeIf { it.isNotEmpty() })?.apply()
    }

    /** Сброс при выходе из аккаунта — ссылка привязана к пользователю, не к устройству. */
    fun clearTradeLink() {
        prefs?.edit()?.remove(KEY_TRADE_LINK)?.apply()
    }
}
