package pl.edu.ur.blokur.ui.android.resident

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.android.common.BlokurTheme
import pl.edu.ur.blokur.ui.android.resident.contents.BottomNavBar
import pl.edu.ur.blokur.ui.android.resident.states.MainState

@Preview(showBackground = true)
@Composable
private fun PreviewBottomNavBar(
    darkTheme: Boolean = false,
) {
    BlokurTheme(darkTheme = darkTheme) {
        Column(
            modifier = Modifier.padding(40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BottomNavBar(MainState.ViewingWelcome) { }
            BottomNavBar(MainState.ViewingTickets) { }
            BottomNavBar(MainState.ViewingFinances) { }
            BottomNavBar(MainState.ViewingAnnouncements) { }
            BottomNavBar(MainState.ViewingProfile) { }
        }
    }
}
