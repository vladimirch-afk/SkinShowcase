package ru.kotlix.skinshowcase.core.network.messaging

import ru.kotlix.skinshowcase.core.BuildConfig

/**
 * Путь API: `api/chats/{chatId}/messages` — [chatId] это SteamID64 (17 цифр).
 * Псевдоним `support` из UI мапится на [BuildConfig.MESSAGING_SUPPORT_STEAM_ID] (как `messaging.support.steam-id` на сервере).
 */
object MessagingChatPaths {

    val configuredSupportSteamId: String = BuildConfig.MESSAGING_SUPPORT_STEAM_ID.trim()

    fun isValidSteamId17(s: String): Boolean = s.length == 17 && s.all { it.isDigit() }

    /** Steam ID для сегмента пути Retrofit. */
    fun steamIdForApiPath(clientChatId: String): String {
        if (clientChatId != SUPPORT_PLACEHOLDER) return clientChatId
        return if (isValidSteamId17(configuredSupportSteamId)) configuredSupportSteamId else clientChatId
    }

    const val SUPPORT_PLACEHOLDER = "support"
}
