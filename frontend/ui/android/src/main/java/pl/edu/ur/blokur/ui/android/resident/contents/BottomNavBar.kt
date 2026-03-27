package pl.edu.ur.blokur.ui.android.resident.contents

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.android.resident.NavBarOption
import pl.edu.ur.blokur.ui.android.resident.bottomNavItems
import pl.edu.ur.blokur.ui.android.resident.states.MainState

@Composable
fun BottomNavBar(
    state : MainState,
    onItemClicked: (NavBarOption) -> Unit,
    ) {
    var selectedOption = NavBarOption.NONE

    when(state){
        is MainState.Error -> selectedOption = NavBarOption.NONE
        is MainState.Loading -> selectedOption = NavBarOption.NONE
        is MainState.ViewingAnnouncements -> selectedOption = NavBarOption.ANNOUNCEMENTS
        is MainState.ViewingFinances -> selectedOption = NavBarOption.FINANCES
        is MainState.ViewingProfile -> selectedOption = NavBarOption.PROFILE
        is MainState.ViewingTickets -> selectedOption = NavBarOption.TICKETS
        is MainState.ViewingWelcome -> selectedOption = NavBarOption.NONE
    }

    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = selectedOption == item.option,
                onClick = { onItemClicked(item.option) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
               },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelLarge
                    )
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