package ru.kotlix.skinshowcase.navigation

import androidx.annotation.StringRes
import ru.kotlix.skinshowcase.R

enum class BottomNavItem(
    val route: String,
    @StringRes val labelRes: Int,
    val iconRes: Int
) {
    HOME(TabRoutes.HOME, R.string.nav_home, R.drawable.ic_nav_home),
    SKINS(TabRoutes.SKINS, R.string.nav_skins, R.drawable.ic_nav_skins),
    MESSAGES(TabRoutes.MESSAGES, R.string.nav_messages, R.drawable.ic_nav_messages),
    PROFILE(TabRoutes.PROFILE, R.string.nav_profile, R.drawable.ic_nav_profile)
}
