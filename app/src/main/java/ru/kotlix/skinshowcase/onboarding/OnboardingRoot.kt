package ru.kotlix.skinshowcase.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ru.kotlix.skinshowcase.onboarding.OnboardingScreen
import ru.kotlix.skinshowcase.onboarding.SteamAuthWebViewScreen

/**
 * Корневой экран онбординга: либо форма входа, либо WebView Steam.
 * После успешного входа (логин/пароль или callback Steam) вызывается [onAuthorized].
 */
@Composable
fun OnboardingRoot(
    onAuthorized: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    var showSteamWebView by remember { mutableStateOf(false) }

    if (showSteamWebView) {
        SteamAuthWebViewScreen(
            onCallbackReceived = { _ ->
                onAuthorized()
            },
            onBack = { showSteamWebView = false },
            modifier = modifier
        )
    } else {
        OnboardingScreen(
            onLoginSuccess = onAuthorized,
            onSteamLoginClick = { showSteamWebView = true },
            modifier = modifier
        )
    }
}
