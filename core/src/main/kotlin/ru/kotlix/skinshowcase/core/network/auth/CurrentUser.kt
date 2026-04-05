package ru.kotlix.skinshowcase.core.network.auth

/**
 * Текущий пользователь (steamId после вызова GET /auth/me).
 * Используется для определения isOutgoing в сообщениях.
 */
object CurrentUser {
    @Volatile
    var steamId: String? = null

    /** STEAM, PRESET или null (старый ответ без source). */
    @Volatile
    var avatarSource: String? = null

    /** Если source=STEAM и сервер отдал URL. */
    @Volatile
    var steamAvatarUrl: String? = null

    /** Для PRESET — строка "1"…"8"; при STEAM обычно null. */
    @Volatile
    var avatarPresetId: String? = null
}
