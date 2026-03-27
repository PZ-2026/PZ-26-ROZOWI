package pl.edu.ur.blokur.ui.android.resident

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavBarOption {
    NONE,
    ANNOUNCEMENTS,
    FINANCES,
    PROFILE,
    TICKETS
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val option: NavBarOption
)

val bottomNavItems = listOf(
    BottomNavItem(
        label = "Zgłoszenia",
        icon = Icons.Outlined.Build,
        option = NavBarOption.TICKETS
    ),
    BottomNavItem(
        label = "Finanse",
        icon = Icons.Outlined.DateRange,
        option = NavBarOption.FINANCES
    ),
    BottomNavItem(
        label = "Ogłoszenia",
        icon = Icons.Outlined.Notifications,
        option = NavBarOption.ANNOUNCEMENTS
    ),
    BottomNavItem(
        label = "Profil",
        icon = Icons.Outlined.AccountCircle,
        option = NavBarOption.PROFILE
    )
)