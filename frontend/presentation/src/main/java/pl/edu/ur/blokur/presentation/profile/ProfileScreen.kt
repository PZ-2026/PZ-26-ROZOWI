package pl.edu.ur.blokur.presentation.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import pl.edu.ur.blokur.presentation.common.component.EmptyState

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->

        }
    }
    EmptyState("PRofile")
    //contents
}