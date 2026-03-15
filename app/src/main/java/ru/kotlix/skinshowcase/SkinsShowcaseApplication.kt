package ru.kotlix.skinshowcase

import android.app.Application
import ru.kotlix.skinshowcase.core.network.DebugAuthToken
import ru.kotlix.skinshowcase.core.network.SkinsProvider
import ru.kotlix.skinshowcase.settings.PrivacyPreferences

class SkinsShowcaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PrivacyPreferences.init(this)
        SkinsProvider.init(this)
        setDebugAuthTokenFromBuildConfig()
    }

    private fun setDebugAuthTokenFromBuildConfig() {
        val token = BuildConfig.MESSAGING_DEBUG_TOKEN
        if (token.isNotBlank()) {
            DebugAuthToken.token = token
        }
    }
}
