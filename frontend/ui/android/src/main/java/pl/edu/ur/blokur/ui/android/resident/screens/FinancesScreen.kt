package pl.edu.ur.blokur.ui.android.resident.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import pl.edu.ur.blokur.ui.android.common.components.CommonEmptyState
import pl.edu.ur.blokur.ui.android.resident.viewmodels.FinancesViewModel

@Composable
fun FinancesScreen(
    viewModel: FinancesViewModel,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->

        }
    }
    CommonEmptyState("Finances")
    //contents
}