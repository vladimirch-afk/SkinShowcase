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
    const val SKIN_DETAIL = "skin/{skinId}/{isOwnOffer}/{isCreatingOffer}/{offerOwnerSteamId}/{inventoryAssetId}"
    const val CREATE_OFFER = "create_offer"
    const val CHAT = "chat/{chatId}"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val FAVORITES = "favorites"
    const val DEAL_HISTORY = "deal_history"
    const val DOCUMENT = "document/{documentId}"
    const val TRADE_LINK = "trade_link"
    const val SUPPORT_PROJECT = "support_project"
    const val INVENTORY_SYNC = "inventory_sync"
}

object NavRoutes {
    const val SKIN_DETAIL_ID_ARG = "skinId"
    const val SKIN_DETAIL_IS_OWN_OFFER_ARG = "isOwnOffer"
    const val SKIN_DETAIL_IS_CREATING_OFFER_ARG = "isCreatingOffer"
    const val SKIN_DETAIL_OFFER_OWNER_STEAM_ID_ARG = "offerOwnerSteamId"
    const val SKIN_DETAIL_INVENTORY_ASSET_ID_ARG = "inventoryAssetId"
    const val CHAT_ID_ARG = "chatId"
    const val DOCUMENT_ID_ARG = "documentId"
}

fun skinDetailRoute(
    skinId: String,
    isOwnOffer: Boolean = false,
    isCreatingOffer: Boolean = false,
    offerOwnerSteamId: String? = null,
    inventoryAssetId: String? = null
) = "skin/$skinId/$isOwnOffer/$isCreatingOffer/${offerOwnerSteamId ?: "_"}/${inventoryAssetId ?: "_"}"

fun chatRoute(chatId: String) = "chat/$chatId"

fun documentRoute(documentId: String) = "document/$documentId"
