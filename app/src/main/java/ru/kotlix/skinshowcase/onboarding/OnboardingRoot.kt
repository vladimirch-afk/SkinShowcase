package ru.kotlix.skinshowcase.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ru.kotlix.skinshowcase.onboarding.SteamAuthWebViewScreen

/**
 * Корневой экран онбординга: экран входа через Steam или WebView.
 * Вход идёт через api-gateway `GET /auth/steam`, как на auth-сервисе; JWT приходит во fragment `#token=`.
 */
@Composable
fun OnboardingRoot(
    onAuthorized: (accessToken: String) -> Unit,
    onLoginAttempt: (method: String) -> Unit = {},
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    var showSteamWebView by remember { mutableStateOf(false) }

    if (showSteamWebView) {
        SteamAuthWebViewScreen(
            onAuthSuccess = onAuthorized,
            onBack = { showSteamWebView = false },
            modifier = modifier
        )
    } else {
        OnboardingScreen(
            onSteamLoginClick = {
                onLoginAttempt("steam")
                showSteamWebView = true
            },
            modifier = modifier
        )
    }
}
