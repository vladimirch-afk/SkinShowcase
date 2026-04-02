package ru.kotlix.skinshowcase.onboarding

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import java.util.concurrent.atomic.AtomicBoolean
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import ru.kotlix.skinshowcase.onboarding.R

private const val STEAM_CALLBACK_LOG_TAG = "SteamAuthCallback"

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SteamAuthWebViewScreen(
    onAuthSuccess: (accessToken: String) -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tokenDelivered = remember { AtomicBoolean(false) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.onboarding_steam_webview_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.onboarding_back),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = createSteamAuthWebViewClient(tokenDelivered, onAuthSuccess)
                }
            },
            update = { webView ->
                if (webView.url.isNullOrBlank()) {
                    val startUrl = SteamAuthConfig.gatewaySteamLoginUrl()
                    Log.d(STEAM_CALLBACK_LOG_TAG, "Load auth start: $startUrl")
                    webView.loadUrl(startUrl)
                }
            }
        )
    }
}

private fun createSteamAuthWebViewClient(
    tokenDelivered: AtomicBoolean,
    onAuthSuccess: (String) -> Unit
): WebViewClient {
    return object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url?.toString() ?: return false
            val rewritten = SteamAuthConfig.rewriteSteamCallbackToGatewayBaseIfLocalhost(url)
            if (rewritten != null) {
                Log.d(STEAM_CALLBACK_LOG_TAG, "Rewrite localhost Steam callback -> $rewritten")
                view?.loadUrl(rewritten)
                return true
            }
            return handleAuthUrl(url, tokenDelivered, onAuthSuccess)
        }

        @Suppress("DEPRECATION")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (url == null) return false
            val rewritten = SteamAuthConfig.rewriteSteamCallbackToGatewayBaseIfLocalhost(url)
            if (rewritten != null) {
                Log.d(STEAM_CALLBACK_LOG_TAG, "Rewrite localhost Steam callback -> $rewritten")
                view?.loadUrl(rewritten)
                return true
            }
            return handleAuthUrl(url, tokenDelivered, onAuthSuccess)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
            if (url == null) return
            val rewritten = SteamAuthConfig.rewriteSteamCallbackToGatewayBaseIfLocalhost(url)
            if (rewritten != null) {
                Log.d(STEAM_CALLBACK_LOG_TAG, "Rewrite localhost Steam callback (onPageStarted) -> $rewritten")
                view?.loadUrl(rewritten)
                return
            }
            handleAuthUrl(url, tokenDelivered, onAuthSuccess)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            if (url == null) return
            val rewritten = SteamAuthConfig.rewriteSteamCallbackToGatewayBaseIfLocalhost(url)
            if (rewritten != null) {
                view?.loadUrl(rewritten)
                return
            }
            handleAuthUrl(url, tokenDelivered, onAuthSuccess)
        }
    }
}

private fun handleAuthUrl(
    url: String,
    tokenDelivered: AtomicBoolean,
    onAuthSuccess: (String) -> Unit
): Boolean {
    val token = SteamAuthConfig.extractTokenFromRedirectUrl(url)
    if (token != null) {
        if (tokenDelivered.compareAndSet(false, true)) {
            Log.d(STEAM_CALLBACK_LOG_TAG, "Received JWT from auth redirect")
            onAuthSuccess(token)
        }
        return true
    }
    return false
}
