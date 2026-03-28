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
import pl.edu.ur.blokur.ui.android.resident.states.ResidentMainState

@Composable
fun BottomNavBar(
    state : ResidentMainState,
    onItemClicked: (NavBarOption) -> Unit,
    ) {
    var selectedOption = NavBarOption.NONE

    when(state){
        is ResidentMainState.Error -> selectedOption = NavBarOption.NONE
        is ResidentMainState.Loading -> selectedOption = NavBarOption.NONE
        is ResidentMainState.ViewingAnnouncements -> selectedOption = NavBarOption.ANNOUNCEMENTS
        is ResidentMainState.ViewingFinances -> selectedOption = NavBarOption.FINANCES
        is ResidentMainState.ViewingProfile -> selectedOption = NavBarOption.PROFILE
        is ResidentMainState.ViewingTickets -> selectedOption = NavBarOption.TICKETS
        is ResidentMainState.ViewingWelcome -> selectedOption = NavBarOption.NONE
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