package ru.kotlix.skinshowcase

import android.app.Application
import io.appmetrica.analytics.push.AppMetricaPush
import ru.kotlix.skinshowcase.analytics.AppAnalytics
import ru.kotlix.skinshowcase.core.network.DebugAuthToken
import ru.kotlix.skinshowcase.core.network.SkinsProvider
import ru.kotlix.skinshowcase.core.network.auth.CurrentUser
import ru.kotlix.skinshowcase.core.network.auth.JwtSubjectParser
import ru.kotlix.skinshowcase.core.network.messaging.MessagingProvider
import ru.kotlix.skinshowcase.onboarding.SteamAuthConfig
import ru.kotlix.skinshowcase.settings.AuthTokenPreferences
import ru.kotlix.skinshowcase.settings.PrivacyPreferences
import ru.kotlix.skinshowcase.settings.TradeLinkPreferences

class SkinsShowcaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppAnalytics.init(this, BuildConfig.APPMETRICA_API_KEY)
        AppMetricaPush.activate(applicationContext)
        PrivacyPreferences.init(this)
        TradeLinkPreferences.init(this)
        AuthTokenPreferences.init(this)
        SteamAuthConfig.postLoginRedirectBase = BuildConfig.AUTH_FRONTEND_REDIRECT_URL.trimEnd('/')
        restoreApiTokenFromPreferences()
        // Моки отключены: скины и сообщения идут через api-gateway (RetrofitProvider, ApiConfig.BASE_URL).
        // SkinsProvider.init(this, MockServer.getApiService())
        // MessagingProvider.init(this, MockServer.getMessagingApiService())
        SkinsProvider.init(this, apiService = null)
        MessagingProvider.init(this, api = null)
        setDebugAuthTokenFromBuildConfig()
    }

    private fun restoreApiTokenFromPreferences() {
        val stored = AuthTokenPreferences.getToken()
        if (!stored.isNullOrBlank()) {
            DebugAuthToken.token = stored
            CurrentUser.steamId = JwtSubjectParser.parseSteamId(stored)
        }
    }

    private fun setDebugAuthTokenFromBuildConfig() {
        val token = BuildConfig.MESSAGING_DEBUG_TOKEN
        if (token.isNotBlank()) {
            DebugAuthToken.token = token
            CurrentUser.steamId = JwtSubjectParser.parseSteamId(token)
        }
    }
}
