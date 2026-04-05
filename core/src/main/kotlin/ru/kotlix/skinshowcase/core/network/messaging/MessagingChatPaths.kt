package ru.kotlix.skinshowcase.core.network.messaging

import ru.kotlix.skinshowcase.core.BuildConfig

/**
 * Путь API: `api/chats/{chatId}/messages` — [chatId] это SteamID64 (17 цифр).
 *
 * В UI закреплён чат «Поддержка» с плейсхолдером [SUPPORT_PLACEHOLDER] или id из Gradle;
 * на messaging-сервисе переписка с поддержкой ведётся с собеседником [SUPPORT_MESSAGING_STEAM_ID]
 * (17 нулей). Список чатов с сервера с этим id не показываем — дубликат закреплённой карточки.
 */
object MessagingChatPaths {

    val configuredSupportSteamId: String = BuildConfig.MESSAGING_SUPPORT_STEAM_ID.trim()

    /** SteamID64 «бота» поддержки на сервере (контракт messaging). */
    const val SUPPORT_MESSAGING_STEAM_ID: String = "00000000000000000"

    fun isValidSteamId17(s: String): Boolean = s.length == 17 && s.all { it.isDigit() }

    /** Чат с поддержкой в ответе GET /api/chats — скрываем в списке (остаётся только закреплённый UI). */
    fun isSupportMessagingSteamId(id: String): Boolean {
        val t = id.trim()
        return t.length == 17 && t.all { ch -> ch == '0' }
    }

    /** Steam ID для сегмента пути Retrofit (отправка и история с поддержкой — всегда [SUPPORT_MESSAGING_STEAM_ID]). */
    fun steamIdForApiPath(clientChatId: String): String {
        val t = clientChatId.trim()
        if (t == SUPPORT_PLACEHOLDER) return SUPPORT_MESSAGING_STEAM_ID
        if (isValidSteamId17(configuredSupportSteamId) && t == configuredSupportSteamId) {
            return SUPPORT_MESSAGING_STEAM_ID
        }
        return t
    }

    const val SUPPORT_PLACEHOLDER = "support"
}
