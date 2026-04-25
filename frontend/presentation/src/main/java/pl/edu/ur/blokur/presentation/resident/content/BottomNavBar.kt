package pl.edu.ur.blokur.presentation.resident.content

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.presentation.resident.util.BottomNavItem
import pl.edu.ur.blokur.presentation.resident.util.NavBarOption
import pl.edu.ur.blokur.presentation.resident.util.ResidentMainState

/**
 * Dolna nawigacja głównego ekranu.
 *
 * Wyświetla wyłącznie zakładki przekazane przez [items], dzięki czemu zestaw
 * zakładek może być filtrowany zależnie od roli użytkownika.
 *
 * @param state       aktualny stan ekranu – decyduje o wybranej zakładce.
 * @param items       lista zakładek do wyświetlenia (filtrowana per rola w ViewModelu).
 * @param onItemClicked callback wywoływany po kliknięciu zakładki.
 */
@Composable
fun BottomNavBar(
    state: ResidentMainState,
    items: List<BottomNavItem>,
    onItemClicked: (NavBarOption) -> Unit,
) {
    val selectedOption = when (state) {
        is ResidentMainState.ViewingAnnouncements -> NavBarOption.ANNOUNCEMENTS
        is ResidentMainState.ViewingFinances -> NavBarOption.FINANCES
        is ResidentMainState.ViewingPropertyTree -> NavBarOption.PROPERTY_TREE
        is ResidentMainState.ViewingProfile -> NavBarOption.PROFILE
        is ResidentMainState.ViewingTickets -> NavBarOption.TICKETS
        else -> NavBarOption.NONE
    }

    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedOption == item.option,
                onClick = { onItemClicked(item.option) },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.label)
                },
                label = {
                    Text(text = item.label, style = MaterialTheme.typography.labelLarge)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
