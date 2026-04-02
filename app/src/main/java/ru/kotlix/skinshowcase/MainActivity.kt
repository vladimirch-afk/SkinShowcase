package ru.kotlix.skinshowcase

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import io.appmetrica.analytics.push.AppMetricaPush
import ru.kotlix.skinshowcase.analytics.AppAnalytics
import ru.kotlix.skinshowcase.core.network.auth.CurrentUser
import ru.kotlix.skinshowcase.core.network.auth.JwtSubjectParser
import ru.kotlix.skinshowcase.designsystem.theme.SkinShowcaseTheme
import ru.kotlix.skinshowcase.navigation.SkinsShowcaseNavHost
import ru.kotlix.skinshowcase.onboarding.OnboardingRoot
import ru.kotlix.skinshowcase.settings.AuthTokenPreferences

private const val AUTH_PREFS_NAME = "auth"
private const val KEY_AUTHORIZED = "authorized"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        processPushIntent(intent)
        setContent {
            SkinShowcaseTheme(darkTheme = true) {
                val context = LocalContext.current
                val prefs = remember {
                    context.getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
                }
                var showOnboarding by remember {
                    mutableStateOf(!prefs.getBoolean(KEY_AUTHORIZED, false))
                }
                if (showOnboarding) {
                    OnboardingRoot(
                        onAuthorized = { accessToken ->
                            AuthTokenPreferences.setToken(accessToken)
                            CurrentUser.steamId = JwtSubjectParser.parseSteamId(accessToken)
                            AppAnalytics.reportLoginSuccess()
                            prefs.edit().putBoolean(KEY_AUTHORIZED, true).apply()
                            showOnboarding = false
                        },
                        onLoginAttempt = { method -> AppAnalytics.reportLoginAttempt(method) },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    requestNotificationPermissionIfNeeded()
                    SkinsShowcaseNavHost(
                        onLogout = {
                            AppAnalytics.reportEvent("logout")
                            AuthTokenPreferences.setToken(null)
                            CurrentUser.steamId = null
                            prefs.edit().putBoolean(KEY_AUTHORIZED, false).apply()
                            showOnboarding = true
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        processPushIntent(intent)
    }

    private fun processPushIntent(intent: android.content.Intent?) {
        if (intent == null) return
        val action = intent.action
        if (action == AppMetricaPush.OPEN_DEFAULT_ACTIVITY_ACTION) {
            AppAnalytics.reportEvent("push_open_default", null)
        }
        val payload = intent.getStringExtra(AppMetricaPush.EXTRA_PAYLOAD)
        if (!payload.isNullOrBlank()) {
            AppAnalytics.reportEvent("push_open", mapOf("payload" to payload))
        }
    }

    @Composable
    private fun requestNotificationPermissionIfNeeded() {
        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }
        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}