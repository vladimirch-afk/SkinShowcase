package ru.kotlix.skinshowcase.onboarding

/**
 * Callback URL для Steam OpenID (перехватывается в WebView).
 * При редиректе на этот URL приложение логирует полную ссылку в консоль.
 */
object SteamAuthConfig {

    const val CALLBACK_SCHEME = "skinshowcase"
    const val CALLBACK_HOST = "steam"
    const val CALLBACK_PATH = "/callback"

    /** Полный callback URL для Steam OpenID return_to. */
    val callbackUrl: String
        get() = "$CALLBACK_SCHEME://$CALLBACK_HOST$CALLBACK_PATH"

    /** Базовый URL авторизации Steam OpenID. */
    private const val STEAM_OPENID_BASE = "https://steamcommunity.com/openid/login"

    fun buildSteamLoginUrl(): String {
        val returnTo = java.net.URLEncoder.encode(callbackUrl, "UTF-8")
        val realm = java.net.URLEncoder.encode("$CALLBACK_SCHEME://$CALLBACK_HOST", "UTF-8")
        return "$STEAM_OPENID_BASE?openid.ns=http://specs.openid.net/auth/2.0" +
                "&openid.mode=checkid_setup" +
                "&openid.return_to=$returnTo" +
                "&openid.realm=$realm" +
                "&openid.identity=http://specs.openid.net/auth/2.0/identifier_select" +
                "&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select"
    }
}
