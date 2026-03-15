package ru.kotlix.skinshowcase.core.network

/**
 * Опциональный Bearer-токен для запросов к API (только для отладки).
 * Устанавливается из приложения, например из BuildConfig.MESSAGING_DEBUG_TOKEN.
 * Если не пустой — все запросы через [RetrofitProvider] получают заголовок Authorization.
 */
object DebugAuthToken {
    var token: String? = null
}
