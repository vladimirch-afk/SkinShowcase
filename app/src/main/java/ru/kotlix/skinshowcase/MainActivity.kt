package ru.kotlix.skinshowcase

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ru.kotlix.skinshowcase.designsystem.theme.SkinShowcaseTheme
import ru.kotlix.skinshowcase.navigation.SkinsShowcaseNavHost
import ru.kotlix.skinshowcase.onboarding.OnboardingRoot

private const val AUTH_PREFS_NAME = "auth"
private const val KEY_AUTHORIZED = "authorized"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkinShowcaseTheme(darkTheme = true) {
                val context = LocalContext.current
                val prefs = remember {
                    context.getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
                }
                // Онбординг показывается только при первом запуске (authorized = false).
                // После входа флаг сохраняется — при следующих запусках сразу основной экран.
                // Сбросить: «Профиль» → «Выйти» или очистить данные приложения.
                var showOnboarding by remember {
                    mutableStateOf(!prefs.getBoolean(KEY_AUTHORIZED, false))
                }
                if (showOnboarding) {
                    OnboardingRoot(
                        onAuthorized = {
                            prefs.edit().putBoolean(KEY_AUTHORIZED, true).apply()
                            showOnboarding = false
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    SkinsShowcaseNavHost(
                        onLogout = {
                            prefs.edit().putBoolean(KEY_AUTHORIZED, false).apply()
                            showOnboarding = true
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}