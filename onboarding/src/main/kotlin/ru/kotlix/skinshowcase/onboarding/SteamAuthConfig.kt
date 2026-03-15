package ru.kotlix.skinshowcase.onboarding

/**
 * Конфигурация Steam OpenID.
 *
 * Steam принимает только HTTPS для return_to и realm. Кастомные схемы (skinshowcase://)
 * отклоняются и приводят к «ошибке запроса». Поэтому используем HTTPS callback URL
 * и перехватываем редирект в WebView, не загружая страницу.
 *
 * Задайте [callbackBaseHttps] из приложения (например в Application.onCreate), если
 * у вас есть свой HTTPS-домен. Иначе используется значение по умолчанию — его нужно
 * заменить на ваш домен (бэкенд или ngrok для разработки).
 */
object SteamAuthConfig {

    private const val DEFAULT_CALLBACK_BASE_HTTPS = "https://skinshowcase.app"

    /**
     * Базовый HTTPS URL для callback (без завершающего слэша).
     * Установите из приложения, например: SteamAuthConfig.callbackBaseHttps = BuildConfig.STEAM_CALLBACK_BASE_HTTPS
     */
    var callbackBaseHttps: String = DEFAULT_CALLBACK_BASE_HTTPS

    const val CALLBACK_PATH = "/steam-callback"

    /** Полный callback URL для Steam OpenID return_to (HTTPS). */
    val callbackUrl: String
        get() = "$callbackBaseHttps$CALLBACK_PATH"

    /** Realm для Steam (совпадает с origin callback URL). */
    val realm: String
        get() = callbackBaseHttps

    /** Проверка, что URL — наш callback (для перехвата в WebView). */
    fun isCallbackUrl(url: String): Boolean =
        url.trim().startsWith(callbackUrl, ignoreCase = true)

    private const val STEAM_OPENID_BASE = "https://steamcommunity.com/openid/login"

    fun buildSteamLoginUrl(): String {
        val returnTo = java.net.URLEncoder.encode(callbackUrl, "UTF-8")
        val realmEnc = java.net.URLEncoder.encode(realm, "UTF-8")
        return "$STEAM_OPENID_BASE?openid.ns=http://specs.openid.net/auth/2.0" +
                "&openid.mode=checkid_setup" +
                "&openid.return_to=$returnTo" +
                "&openid.realm=$realmEnc" +
                "&openid.identity=http://specs.openid.net/auth/2.0/identifier_select" +
                "&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select"
    }
}
