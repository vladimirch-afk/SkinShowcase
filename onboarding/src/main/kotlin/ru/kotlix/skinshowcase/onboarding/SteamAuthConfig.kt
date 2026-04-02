package ru.kotlix.skinshowcase.onboarding

import android.net.Uri
import ru.kotlix.skinshowcase.core.network.ApiConfig

/**
 * OAuth-поток как на auth-сервисе: старт с api-gateway, финиш с токеном во fragment.
 *
 * 1. WebView открывает [gatewaySteamLoginUrl] → `GET {BASE_URL}auth/steam` → редирект на Steam.
 * 2. Steam → `GET .../auth/steam/callback` на gateway → auth выдаёт JWT.
 * 3. Ответ: редирект на [postLoginRedirectBase]`#token=`JWT (как [AUTH_FRONTEND_REDIRECT_URL] на сервере).
 *
 * Значение [postLoginRedirectBase] должно побайтно совпадать с `AUTH_FRONTEND_REDIRECT_URL` у auth
 * (без `#` и без лишнего слэша в конце, либо с тем же слэшей, что и на сервере — сравнение нормализует).
 *
 * Если на сервере в `AUTH_STEAM_RETURN_TO` указан `http://localhost:8080/...`, WebView на эмуляторе
 * не достучится до ПК — тогда используйте [rewriteSteamCallbackToGatewayBaseIfLocalhost].
 */
object SteamAuthConfig {

    private const val STEAM_CALLBACK_PATH = "/auth/steam/callback"

    /**
     * URI без fragment. Пример для приложения: `skinshowcase://auth/callback`.
     * На сервере: `AUTH_FRONTEND_REDIRECT_URL` то же значение.
     */
    var postLoginRedirectBase: String = "skinshowcase://auth/callback"

    /** Старт входа: единая точка gateway (как в OpenAPI). */
    fun gatewaySteamLoginUrl(): String {
        val base = ApiConfig.BASE_URL.trimEnd('/')
        return "$base/auth/steam"
    }

    /**
     * Steam редиректит на `return_to` с хостом из конфига бэкенда (часто `localhost`).
     * На эмуляторе/телефоне это не хост с Docker — подменяем scheme/host/port на [ApiConfig.BASE_URL].
     * @return новый URL для `WebView.loadUrl` или null, если переписывать не нужно
     */
    fun rewriteSteamCallbackToGatewayBaseIfLocalhost(url: String): String? {
        val uri = Uri.parse(url)
        val path = uri.path?.trimEnd('/') ?: return null
        if (!path.equals(STEAM_CALLBACK_PATH, ignoreCase = true)) return null
        val host = uri.host?.lowercase() ?: return null
        if (host != "localhost" && host != "127.0.0.1") return null
        val base = Uri.parse(ApiConfig.BASE_URL.trimEnd('/'))
        val authority = base.encodedAuthority ?: return null
        return uri.buildUpon()
            .scheme(base.scheme)
            .encodedAuthority(authority)
            .build()
            .toString()
    }

    private fun normalizeBase(url: String): String = url.trimEnd('/').lowercase()

    private fun matchesPostLoginRedirect(urlWithoutFragment: String): Boolean {
        val a = normalizeBase(urlWithoutFragment)
        val b = normalizeBase(postLoginRedirectBase)
        return a == b
    }

    /**
     * Бэкенд часто задаёт `AUTH_FRONTEND_REDIRECT_URL=http://localhost:3000` — для браузера на ПК это ок,
     * но в WebView на эмуляторе/телефоне localhost не тот хост. Если база — loopback, всё равно забираем JWT из fragment.
     */
    private fun isLoopbackAuthFrontendBase(urlWithoutFragment: String): Boolean {
        val uri = Uri.parse(urlWithoutFragment)
        val host = uri.host?.lowercase() ?: return false
        if (host != "localhost" && host != "127.0.0.1") return false
        val scheme = uri.scheme?.lowercase() ?: return false
        return scheme == "http" || scheme == "https"
    }

    /** Из полного URL после редиректа auth (с `#token=...`) достаёт JWT или null. */
    fun extractTokenFromRedirectUrl(fullUrl: String): String? {
        val hashIdx = fullUrl.indexOf('#')
        if (hashIdx < 0) return null
        val basePart = fullUrl.substring(0, hashIdx)
        val acceptBase = matchesPostLoginRedirect(basePart) || isLoopbackAuthFrontendBase(basePart)
        if (!acceptBase) return null
        val fragment = fullUrl.substring(hashIdx + 1)
        val prefix = "token="
        if (!fragment.startsWith(prefix)) return null
        val token = fragment.substring(prefix.length).trim()
        return token.takeIf { it.isNotEmpty() }
    }
}
