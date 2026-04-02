package ru.kotlix.skinshowcase.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.kotlix.skinshowcase.navigation.chatRoute
import ru.kotlix.skinshowcase.navigation.documentRoute
import ru.kotlix.skinshowcase.R
import ru.kotlix.skinshowcase.screens.about.AboutScreen
import ru.kotlix.skinshowcase.screens.dealhistory.DealHistoryScreen
import ru.kotlix.skinshowcase.screens.document.DocumentViewerScreen
import ru.kotlix.skinshowcase.screens.home.HomeScreen
import ru.kotlix.skinshowcase.message.chats.ChatsListScreen
import ru.kotlix.skinshowcase.message.chat.ChatScreen
import ru.kotlix.skinshowcase.screens.profile.ProfileScreen
import ru.kotlix.skinshowcase.screens.favorites.FavoritesScreen
import ru.kotlix.skinshowcase.screens.tradelink.TradeLinkScreen
import ru.kotlix.skinshowcase.screens.settings.SettingsScreen
import ru.kotlix.skinshowcase.message.chats.ChatsListViewModel
import ru.kotlix.skinshowcase.screens.createoffer.CreateOfferSelectSkinScreen
import ru.kotlix.skinshowcase.data.ProfileDataProvider
import ru.kotlix.skinshowcase.analytics.AppAnalytics
import ru.kotlix.skinshowcase.screens.offers.OffersScreen
import ru.kotlix.skinshowcase.screens.skindetail.SkinDetailScreen
import ru.kotlix.skinshowcase.screens.supportproject.SupportProjectScreen
import ru.kotlix.skinshowcase.screens.inventorysync.InventorySyncScreen

private const val TAB_ANIMATION_DURATION_MS = 300

@Composable
fun SkinsShowcaseNavHost(
    navController: NavHostController = rememberNavController(),
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isTabRoute = currentRoute in setOf(
        TabRoutes.HOME,
        TabRoutes.SKINS,
        TabRoutes.MESSAGES,
        TabRoutes.PROFILE
    )

    LaunchedEffect(currentRoute) {
        currentRoute?.let { route -> AppAnalytics.reportScreen(route) }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            AnimatedVisibility(visible = isTabRoute) {
                AnimatedBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TabRoutes.HOME,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                val spec = tween<IntOffset>(TAB_ANIMATION_DURATION_MS)
                slideInHorizontally(spec) { it }
            },
            exitTransition = {
                val spec = tween<IntOffset>(TAB_ANIMATION_DURATION_MS)
                slideOutHorizontally(spec) { -it }
            },
            popEnterTransition = {
                val spec = tween<IntOffset>(TAB_ANIMATION_DURATION_MS)
                slideInHorizontally(spec) { -it }
            },
            popExitTransition = {
                val spec = tween<IntOffset>(TAB_ANIMATION_DURATION_MS)
                slideOutHorizontally(spec) { it }
            }
        ) {
            composable(TabRoutes.HOME) {
                HomeScreen(
                    onSkinClick = { skinId, offerOwnerSteamId ->
                        navController.navigate(skinDetailRoute(skinId, offerOwnerSteamId = offerOwnerSteamId))
                    },
                    onCreateOffer = { navController.navigate(OverlayRoutes.CREATE_OFFER) }
                )
            }
            composable(TabRoutes.SKINS) {
                OffersScreen(
                    onOfferClick = { skinId -> navController.navigate(skinDetailRoute(skinId, isOwnOffer = true)) },
                    onCreateOffer = { navController.navigate(OverlayRoutes.CREATE_OFFER) }
                )
            }
            composable(TabRoutes.MESSAGES) {
                ChatsListScreen(
                    onChatClick = { chatId ->
                        navController.navigate(chatRoute(chatId))
                    }
                )
            }
            composable(TabRoutes.PROFILE) {
                ProfileScreen(
                    onNavigateToSettings = { navController.navigate(OverlayRoutes.SETTINGS) },
                    onNavigateToAbout = { navController.navigate(OverlayRoutes.ABOUT) },
                    onNavigateToFavorites = { navController.navigate(OverlayRoutes.FAVORITES) },
                    onNavigateToTradeLink = { navController.navigate(OverlayRoutes.TRADE_LINK) },
                    onViewAllOffers = {
                        navController.navigate(TabRoutes.SKINS) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onCreateOffer = {
                        navController.navigate(TabRoutes.SKINS) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        navController.navigate(OverlayRoutes.CREATE_OFFER)
                    },
                    onContactSupport = { navController.navigate(chatRoute(ChatsListViewModel.SUPPORT_CHAT_ID)) },
                    onSupportProject = { navController.navigate(OverlayRoutes.SUPPORT_PROJECT) },
                    onViewFullHistory = { navController.navigate(OverlayRoutes.DEAL_HISTORY) },
                    onDocumentClick = { documentId ->
                        navController.navigate(documentRoute(documentId))
                    },
                    onNavigateToInventorySync = { navController.navigate(OverlayRoutes.INVENTORY_SYNC) },
                    onLogout = onLogout
                )
            }

            composable(
                route = OverlayRoutes.SKIN_DETAIL,
                arguments = listOf(
                    navArgument(NavRoutes.SKIN_DETAIL_ID_ARG) { type = NavType.StringType },
                    navArgument(NavRoutes.SKIN_DETAIL_IS_OWN_OFFER_ARG) { type = NavType.BoolType; defaultValue = false },
                    navArgument(NavRoutes.SKIN_DETAIL_IS_CREATING_OFFER_ARG) { type = NavType.BoolType; defaultValue = false },
                    navArgument(NavRoutes.SKIN_DETAIL_OFFER_OWNER_STEAM_ID_ARG) {
                        type = NavType.StringType
                        defaultValue = "_"
                    },
                    navArgument(NavRoutes.SKIN_DETAIL_INVENTORY_ASSET_ID_ARG) {
                        type = NavType.StringType
                        defaultValue = "_"
                    }
                )
            ) { backStackEntry ->
                val skinId = backStackEntry.arguments?.getString(NavRoutes.SKIN_DETAIL_ID_ARG) ?: ""
                val isOwnOffer = backStackEntry.arguments?.getBoolean(NavRoutes.SKIN_DETAIL_IS_OWN_OFFER_ARG) ?: false
                val isCreatingOffer = backStackEntry.arguments?.getBoolean(NavRoutes.SKIN_DETAIL_IS_CREATING_OFFER_ARG) ?: false
                SkinDetailScreen(
                    skinId = skinId,
                    isOwnOffer = isOwnOffer,
                    isCreatingOffer = isCreatingOffer,
                    onBack = { navController.popBackStack() },
                    onOfferCreated = {
                        ProfileDataProvider.markOffersNeedRefresh()
                        navController.popBackStack()
                        navController.navigate(TabRoutes.SKINS) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenChatWithSeller = { chatId -> navController.navigate(chatRoute(chatId)) }
                )
            }
            composable(
                route = OverlayRoutes.CHAT,
                arguments = listOf(navArgument(NavRoutes.CHAT_ID_ARG) { type = NavType.StringType })
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString(NavRoutes.CHAT_ID_ARG) ?: ""
                ChatScreen(
                    chatId = chatId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(OverlayRoutes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(OverlayRoutes.TRADE_LINK) {
                TradeLinkScreen(onBack = { navController.popBackStack() })
            }
            composable(OverlayRoutes.INVENTORY_SYNC) {
                InventorySyncScreen(onBack = { navController.popBackStack() })
            }
            composable(OverlayRoutes.FAVORITES) {
                FavoritesScreen(
                    onSkinClick = { skinId -> navController.navigate(skinDetailRoute(skinId)) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(OverlayRoutes.CREATE_OFFER) {
                CreateOfferSelectSkinScreen(
                    onBack = { navController.popBackStack() },
                    onSkinClick = { skinId, inventoryAssetId ->
                        navController.navigate(
                            skinDetailRoute(
                                skinId = skinId,
                                isOwnOffer = true,
                                isCreatingOffer = true,
                                inventoryAssetId = inventoryAssetId
                            )
                        ) {
                            popUpTo(OverlayRoutes.CREATE_OFFER) { inclusive = true }
                        }
                    }
                )
            }
            composable(OverlayRoutes.ABOUT) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
            composable(OverlayRoutes.SUPPORT_PROJECT) {
                SupportProjectScreen(onBack = { navController.popBackStack() })
            }
            composable(OverlayRoutes.DEAL_HISTORY) {
                DealHistoryScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = OverlayRoutes.DOCUMENT,
                arguments = listOf(navArgument(NavRoutes.DOCUMENT_ID_ARG) { type = NavType.StringType })
            ) { backStackEntry ->
                val documentId = backStackEntry.arguments?.getString(NavRoutes.DOCUMENT_ID_ARG) ?: ""
                DocumentViewerScreen(
                    documentId = documentId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
