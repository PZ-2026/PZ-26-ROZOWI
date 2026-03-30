package pl.edu.ur.blokur.presentation.resident.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import pl.edu.ur.blokur.presentation.common.component.TopBar
import pl.edu.ur.blokur.presentation.resident.state.NavBarOption
import pl.edu.ur.blokur.presentation.resident.state.ResidentMainEvent
import pl.edu.ur.blokur.presentation.resident.content.BottomNavBar
import pl.edu.ur.blokur.presentation.resident.viewmodel.ResidentMainViewModel

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
        topBar = { TopBar(state.toString()) },
        bottomBar = {
            BottomNavBar(state, viewModel::onOptionClicked)
        }
    ) {
        innerPadding -> innerContent(Modifier.padding(innerPadding))
    }
}