package ru.kotlix.skinshowcase.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import ru.kotlix.skinshowcase.R
import ru.kotlix.skinshowcase.screens.about.AboutScreen
import ru.kotlix.skinshowcase.screens.home.HomeScreen
import ru.kotlix.skinshowcase.message.chats.ChatsListScreen
import ru.kotlix.skinshowcase.message.chat.ChatScreen
import ru.kotlix.skinshowcase.screens.profile.ProfileScreen
import ru.kotlix.skinshowcase.screens.favorites.FavoritesScreen
import ru.kotlix.skinshowcase.screens.settings.SettingsScreen
import ru.kotlix.skinshowcase.screens.offers.OffersScreen
import ru.kotlix.skinshowcase.screens.skindetail.SkinDetailScreen

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
                    onSkinClick = { skinId -> navController.navigate(skinDetailRoute(skinId)) }
                )
            }
            composable(TabRoutes.SKINS) {
                OffersScreen(
                    onOfferClick = { skinId -> navController.navigate(skinDetailRoute(skinId, isOwnOffer = true)) },
                    onCreateOffer = { }
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
                    onViewAllOffers = {
                        navController.navigate(TabRoutes.SKINS) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onLogout = onLogout
                )
            }

            composable(
                route = OverlayRoutes.SKIN_DETAIL,
                arguments = listOf(
                    navArgument(NavRoutes.SKIN_DETAIL_ID_ARG) { type = NavType.StringType },
                    navArgument(NavRoutes.SKIN_DETAIL_IS_OWN_OFFER_ARG) { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val skinId = backStackEntry.arguments?.getString(NavRoutes.SKIN_DETAIL_ID_ARG) ?: ""
                val isOwnOffer = backStackEntry.arguments?.getBoolean(NavRoutes.SKIN_DETAIL_IS_OWN_OFFER_ARG) ?: false
                SkinDetailScreen(
                    skinId = skinId,
                    isOwnOffer = isOwnOffer,
                    onBack = { navController.popBackStack() }
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
            composable(OverlayRoutes.FAVORITES) {
                FavoritesScreen(
                    onSkinClick = { skinId -> navController.navigate(skinDetailRoute(skinId)) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(OverlayRoutes.ABOUT) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
