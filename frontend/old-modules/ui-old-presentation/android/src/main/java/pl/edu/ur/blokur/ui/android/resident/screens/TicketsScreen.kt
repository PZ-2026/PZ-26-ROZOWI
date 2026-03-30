package pl.edu.ur.blokur.ui.android.resident.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import pl.edu.ur.blokur.ui.android.common.components.CommonEmptyState
import pl.edu.ur.blokur.ui.android.resident.viewmodels.TicketsViewModel

@Composable
fun TicketsScreen(
    viewModel: TicketsViewModel,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->

        }
    }
    CommonEmptyState("tickets")
    //contents
}