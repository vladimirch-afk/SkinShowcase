package ru.kotlix.skinshowcase.core.network.auth

/**
 * Текущий пользователь (steamId после вызова GET /auth/me).
 * Используется для определения isOutgoing в сообщениях.
 */
object CurrentUser {
    @Volatile
    var steamId: String? = null
}
