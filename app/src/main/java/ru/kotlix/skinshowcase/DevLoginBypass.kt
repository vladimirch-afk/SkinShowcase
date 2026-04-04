package ru.kotlix.skinshowcase

import android.app.Application
import android.content.Context
import android.util.Base64
import ru.kotlix.skinshowcase.core.network.auth.CurrentUser
import ru.kotlix.skinshowcase.settings.AuthTokenPreferences

/**
 * Отладочный автологин отключён ([ENABLED] = false). Вызов из [SkinsShowcaseApplication] снят.
 * Не включайте без необходимости: ломает нормальный Steam OAuth и Bearer на gateway.
 */
object DevLoginBypass {

    const val ENABLED: Boolean = false

    /** Должен совпадать с `sub` в JWT (17 цифр SteamID64). */
    const val STEAM_ID: String = "76561198000000001"

    private const val AUTH_PREFS_NAME = "auth"
    private const val KEY_AUTHORIZED = "authorized"

    fun applyIfEnabled(app: Application) {
        if (!BuildConfig.DEBUG || !ENABLED) return
        val jwt = minimalJwtWithSub(STEAM_ID)
        AuthTokenPreferences.setToken(jwt)
        CurrentUser.steamId = STEAM_ID
        app.getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_AUTHORIZED, true)
            .apply()
    }

    private fun minimalJwtWithSub(steamId: String): String {
        val headerJson = """{"alg":"none","typ":"JWT"}"""
        val payloadJson = """{"sub":"$steamId"}"""
        val header = base64UrlNoPadding(headerJson.toByteArray(Charsets.UTF_8))
        val payload = base64UrlNoPadding(payloadJson.toByteArray(Charsets.UTF_8))
        return "$header.$payload.dev"
    }

    private fun base64UrlNoPadding(data: ByteArray): String =
        Base64.encodeToString(data, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
}
