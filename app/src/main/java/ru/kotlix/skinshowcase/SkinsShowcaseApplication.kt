package ru.kotlix.skinshowcase

import android.app.Application
import io.appmetrica.analytics.push.AppMetricaPush
import ru.kotlix.skinshowcase.analytics.AppAnalytics
import ru.kotlix.skinshowcase.core.network.DebugAuthToken
import ru.kotlix.skinshowcase.core.network.SkinsProvider
import ru.kotlix.skinshowcase.core.network.messaging.MessagingProvider
import ru.kotlix.skinshowcase.mock.MockServer
import ru.kotlix.skinshowcase.settings.PrivacyPreferences
import ru.kotlix.skinshowcase.settings.TradeLinkPreferences

class SkinsShowcaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppAnalytics.init(this, BuildConfig.APPMETRICA_API_KEY)
        AppMetricaPush.activate(applicationContext)
        PrivacyPreferences.init(this)
        TradeLinkPreferences.init(this)
        // В debug (USE_MOCK_SERVER=true) используем моки: скины, чаты, профиль, документы — без сервера.
        initMockIfEnabled()
        SkinsProvider.init(this, if (BuildConfig.USE_MOCK_SERVER) MockServer.getApiService() else null)
        MessagingProvider.init(this, if (BuildConfig.USE_MOCK_SERVER) MockServer.getMessagingApiService() else null)
        setDebugAuthTokenFromBuildConfig()
    }

    private fun initMockIfEnabled() {
        if (BuildConfig.USE_MOCK_SERVER) {
            MockServer.setEnabled(true)
        }
    }

    private fun setDebugAuthTokenFromBuildConfig() {
        val token = BuildConfig.MESSAGING_DEBUG_TOKEN
        if (token.isNotBlank()) {
            DebugAuthToken.token = token
        }
    }
}
