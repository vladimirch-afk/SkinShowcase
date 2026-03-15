package ru.kotlix.skinshowcase.core.network

/**
 * API base URL and timeouts.
 * In production load from BuildConfig / env; never hardcode secrets.
 * На эмуляторе 10.0.2.2 — это хост-машина; на реальном устройстве — IP компа в сети.
 */
object ApiConfig {
    const val BASE_URL = "http://10.0.2.2:8082/api/"
    const val CONNECT_TIMEOUT_SEC = 15L
    const val READ_TIMEOUT_SEC = 30L
    const val WRITE_TIMEOUT_SEC = 30L
}
