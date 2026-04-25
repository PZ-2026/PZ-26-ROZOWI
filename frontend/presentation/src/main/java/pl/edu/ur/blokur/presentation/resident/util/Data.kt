package pl.edu.ur.blokur.presentation.resident.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.ui.graphics.vector.ImageVector
import pl.edu.ur.blokur.domain.model.UserRole

enum class NavBarOption {
    NONE,
    ANNOUNCEMENTS,
    FINANCES,
    PROPERTY_TREE,
    PROFILE,
    TICKETS
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val option: NavBarOption
)

/** Pełny zestaw zakładek – dla mieszkańca i zarządcy. */
val bottomNavItems = listOf(
    BottomNavItem("Zgłoszenia", Icons.Outlined.Build, NavBarOption.TICKETS),
    BottomNavItem("Finanse", Icons.Outlined.DateRange, NavBarOption.FINANCES),
    BottomNavItem("Ogłoszenia", Icons.Outlined.Notifications, NavBarOption.ANNOUNCEMENTS),
    BottomNavItem("Profil", Icons.Outlined.AccountCircle, NavBarOption.PROFILE)
)

/** Zestaw zakładek widocznych dla zarządcy, rozszerzony o drzewo nieruchomości. */
val managerNavItems = listOf(
    BottomNavItem("Drzewo", Icons.Outlined.AccountTree, NavBarOption.PROPERTY_TREE),
    BottomNavItem("Zgłoszenia", Icons.Outlined.Build, NavBarOption.TICKETS),
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
    UserRole.ZARZADCA -> managerNavItems
    else -> bottomNavItems
}
