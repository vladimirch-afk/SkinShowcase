package ru.kotlix.skinshowcase.navigation

/**
 * Routes that show the bottom navigation bar (main tabs).
 */
object TabRoutes {
    const val HOME = "home"
    const val SKINS = "skins"
    const val MESSAGES = "messages"
    const val PROFILE = "profile"
}

/**
 * Routes that are shown above the tab content (full screen, bottom bar hidden).
 */
object OverlayRoutes {
    const val SKIN_DETAIL = "skin/{skinId}/{isOwnOffer}"
    const val CREATE_OFFER = "create_offer"
    const val CHAT = "chat/{chatId}"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val FAVORITES = "favorites"
}

object NavRoutes {
    const val SKIN_DETAIL_ID_ARG = "skinId"
    const val SKIN_DETAIL_IS_OWN_OFFER_ARG = "isOwnOffer"
    const val CHAT_ID_ARG = "chatId"
}

fun skinDetailRoute(skinId: String, isOwnOffer: Boolean = false) = "skin/$skinId/$isOwnOffer"

fun chatRoute(chatId: String) = "chat/$chatId"
