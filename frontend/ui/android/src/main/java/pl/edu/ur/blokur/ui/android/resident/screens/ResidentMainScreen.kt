package pl.edu.ur.blokur.ui.android.resident.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import pl.edu.ur.blokur.ui.android.common.components.CommonEmptyState
import pl.edu.ur.blokur.ui.android.resident.NavBarOption
import pl.edu.ur.blokur.ui.android.resident.contents.BottomNavBar
import pl.edu.ur.blokur.ui.android.resident.states.ResidentMainEvent
import pl.edu.ur.blokur.ui.android.resident.viewmodels.ResidentMainViewModel

@Composable
fun ResidentMainScreen(
    viewModel: ResidentMainViewModel,
    onNavBarItemClicked: (NavBarOption) -> Unit,
    innerContent: @Composable (Modifier) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when(event) {
                is ResidentMainEvent.ChangeResidentView -> onNavBarItemClicked(event.option)
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(state,onNavBarItemClicked)
        }
    ) {
        innerPadding -> innerContent(Modifier.padding(innerPadding))
    }
}