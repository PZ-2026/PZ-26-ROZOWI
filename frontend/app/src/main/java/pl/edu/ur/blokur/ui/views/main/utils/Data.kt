package pl.edu.ur.blokur.ui.views.main.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.ui.graphics.vector.ImageVector
import pl.edu.ur.blokur.dtos.UserRole

enum class NavBarOption {
    NONE,
    ANNOUNCEMENTS,
    FINANCES,
    PROFILE,
    TICKETS,
    PROPERTIES
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val option: NavBarOption
)

/** Pełny zestaw zakładek – dla mieszkańca. */
val bottomNavItems = listOf(
    BottomNavItem("Zgłoszenia", Icons.Outlined.Build, NavBarOption.TICKETS),
    BottomNavItem("Finanse", Icons.Outlined.DateRange, NavBarOption.FINANCES),
    BottomNavItem("Ogłoszenia", Icons.Outlined.Notifications, NavBarOption.ANNOUNCEMENTS),
    BottomNavItem("Profil", Icons.Outlined.AccountCircle, NavBarOption.PROFILE)
)

/** Zestaw zakładek – dla zarządcy (z nieruchomościami). */
val zarzadcaNavItems = listOf(
    BottomNavItem("Zgłoszenia", Icons.Outlined.Build, NavBarOption.TICKETS),
    BottomNavItem("Nieruchomości", Icons.Outlined.Domain, NavBarOption.PROPERTIES),
    BottomNavItem("Finanse", Icons.Outlined.DateRange, NavBarOption.FINANCES),
    BottomNavItem("Ogłoszenia", Icons.Outlined.Notifications, NavBarOption.ANNOUNCEMENTS),
    BottomNavItem("Profil", Icons.Outlined.AccountCircle, NavBarOption.PROFILE)
)

/** Uproszczony zestaw zakładek – dla konserwatora (tylko zgłoszenia i profil). */
val konserwatorNavItems = listOf(
    BottomNavItem("Zgłoszenia", Icons.Outlined.Build, NavBarOption.TICKETS),
    BottomNavItem("Profil", Icons.Outlined.AccountCircle, NavBarOption.PROFILE)
)

/**
 * Zwraca listę zakładek nawigacji dolnej odpowiednią dla danej roli użytkownika.
 *
 * @param role rola użytkownika; `null` traktowany jak [UserRole.MIESZKANIEC].
 */
fun navItemsForRole(role: UserRole?): List<BottomNavItem> = when (role) {
    UserRole.KONSERWATOR -> konserwatorNavItems
    UserRole.ZARZADCA -> zarzadcaNavItems
    else -> bottomNavItems
}
