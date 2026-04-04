package pl.edu.ur.blokur.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector,
)

val bottomNavItems =
    listOf(
        BottomNavItem(
            label = "Zgłoszenia",
            route = AppRoute.Tickets.route,
            icon = Icons.Outlined.Article,
        ),
        BottomNavItem(
            label = "Finanse",
            route = AppRoute.Finances.route,
            icon = Icons.Outlined.Payments,
        ),
        BottomNavItem(
            label = "Ogłoszenia",
            route = AppRoute.Announcements.route,
            icon = Icons.Outlined.Campaign,
        ),
        BottomNavItem(
            label = "Profil",
            route = AppRoute.Profile.route,
            icon = Icons.Outlined.AccountCircle,
        ),
    )
